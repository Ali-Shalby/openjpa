/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package it.uniroma2.isw2.openjpa.classification;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Computes Milestone 2 metrics from persisted out-of-fold predictions.
 *
 * <p>Metrics are calculated once per Configuration x Classifier x Repetition
 * on the complete OOF prediction set of that repetition. Fold-level metrics
 * are deliberately not averaged.</p>
 */
public final class M2Metrics {

    private static final int EXPECTED_ROWS = 12_836;
    private static final int EXPECTED_BUGGY_YES = 2_010;
    private static final int EXPECTED_BUGGY_NO = 10_826;

    private static final int EXPECTED_QUICK_CONFIGURATIONS = 4;
    private static final int EXPECTED_QUICK_CLASSIFIERS = 3;
    private static final int EXPECTED_QUICK_REPETITIONS = 1;
    private static final int EXPECTED_QUICK_METRIC_ROWS =
            EXPECTED_QUICK_CONFIGURATIONS
                    * EXPECTED_QUICK_CLASSIFIERS
                    * EXPECTED_QUICK_REPETITIONS;

    private static final double LOC_BUDGET_FRACTION = 0.20;

    private static final Path PREDICTIONS_QUICK =
            Path.of(
                    "isw2",
                    "results",
                    "m2",
                    "classification",
                    "predictions_quick.csv"
            );

    private static final Path RESULT_DIRECTORY =
            Path.of(
                    "isw2",
                    "results",
                    "m2",
                    "metrics"
            );

    private static final List<String> REQUIRED_HEADERS =
            List.of(
                    "Configuration",
                    "Classifier",
                    "Repetition",
                    "Fold",
                    "OriginalIndex",
                    "Actual",
                    "Predicted",
                    "ProbabilityNO",
                    "ProbabilityYES",
                    "LOC"
            );

    private M2Metrics() {
        // Utility class.
    }

    public static List<MetricResult> calculateQuick(
            Path repository
    ) throws IOException {

        Path input =
                repository
                        .toAbsolutePath()
                        .normalize()
                        .resolve(
                                PREDICTIONS_QUICK
                        );

        List<Prediction> predictions =
                readPredictions(
                        input
                );

        Map<ExperimentKey, List<Prediction>> groups =
                new LinkedHashMap<>();

        for (Prediction prediction
                : predictions) {

            ExperimentKey key =
                    new ExperimentKey(
                            prediction.configuration(),
                            prediction.classifier(),
                            prediction.repetition()
                    );

            groups.computeIfAbsent(
                    key,
                    ignored -> new ArrayList<>()
            ).add(
                    prediction
            );
        }

        if (groups.size()
                != EXPECTED_QUICK_METRIC_ROWS) {

            throw new IllegalStateException(
                    "Unexpected QUICK metric group count. Expected "
                            + EXPECTED_QUICK_METRIC_ROWS
                            + ", found "
                            + groups.size()
            );
        }

        List<MetricResult> results =
                new ArrayList<>(
                        groups.size()
                );

        for (Map.Entry<ExperimentKey, List<Prediction>> entry
                : groups.entrySet()) {

            validateOofGroup(
                    entry.getKey(),
                    entry.getValue()
            );

            results.add(
                    calculate(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        results.sort(
                Comparator.comparing(
                                MetricResult::configuration
                        )
                        .thenComparing(
                                MetricResult::classifier
                        )
                        .thenComparingInt(
                                MetricResult::repetition
                        )
        );

        validateMetricResults(
                results
        );

        return List.copyOf(
                results
        );
    }

    private static MetricResult calculate(
            ExperimentKey key,
            List<Prediction> predictions
    ) {

        int truePositive = 0;
        int falsePositive = 0;
        int trueNegative = 0;
        int falseNegative = 0;

        for (Prediction prediction
                : predictions) {

            boolean actualYes =
                    "YES".equals(
                            prediction.actual()
                    );

            boolean predictedYes =
                    "YES".equals(
                            prediction.predicted()
                    );

            if (actualYes && predictedYes) {
                truePositive++;

            } else if (!actualYes && predictedYes) {
                falsePositive++;

            } else if (!actualYes) {
                trueNegative++;

            } else {
                falseNegative++;
            }
        }

        double precision =
                safeDivide(
                        truePositive,
                        truePositive
                                + falsePositive
                );

        double recall =
                safeDivide(
                        truePositive,
                        truePositive
                                + falseNegative
                );

        double auc =
                auc(
                        predictions
                );

        double kappa =
                kappa(
                        truePositive,
                        falsePositive,
                        trueNegative,
                        falseNegative
                );

        NpofB20Result npof =
                npofB20(
                        predictions
                );

        return new MetricResult(
                key.configuration(),
                key.classifier(),
                key.repetition(),
                predictions.size(),
                truePositive,
                falsePositive,
                trueNegative,
                falseNegative,
                precision,
                recall,
                auc,
                kappa,
                npof.value(),
                npof.totalLoc(),
                npof.budgetLoc(),
                npof.inspectedLoc(),
                npof.inspectedRows(),
                npof.buggyFound(),
                npof.totalBuggy()
        );
    }

    private static double safeDivide(
            int numerator,
            int denominator
    ) {

        if (denominator == 0) {
            return 0.0;
        }

        return (double) numerator
                / denominator;
    }

    /**
     * ROC AUC using the Mann-Whitney/rank formulation.
     *
     * <p>Equal probabilities receive their average rank, so ties contribute
     * exactly 0.5 as required by the standard AUC definition.</p>
     */
    private static double auc(
            List<Prediction> predictions
    ) {

        List<Prediction> sorted =
                new ArrayList<>(
                        predictions
                );

        sorted.sort(
                Comparator.comparingDouble(
                                Prediction::probabilityYes
                        )
                        .thenComparingInt(
                                Prediction::originalIndex
                        )
        );

        int positiveCount = 0;
        int negativeCount = 0;

        for (Prediction prediction
                : sorted) {

            if ("YES".equals(
                    prediction.actual()
            )) {
                positiveCount++;
            } else {
                negativeCount++;
            }
        }

        if (positiveCount == 0
                || negativeCount == 0) {

            throw new IllegalStateException(
                    "AUC requires both classes."
            );
        }

        double positiveRankSum = 0.0;

        int start = 0;

        while (start < sorted.size()) {

            int end = start + 1;

            double probability =
                    sorted.get(start)
                            .probabilityYes();

            while (end < sorted.size()
                    && Double.compare(
                    sorted.get(end)
                            .probabilityYes(),
                    probability
            ) == 0) {

                end++;
            }

            /*
             * Ranks are one-based.
             * Group [start, end) has ranks start+1 ... end.
             */
            double averageRank =
                    (
                            (start + 1.0)
                                    + end
                    ) / 2.0;

            for (int index = start;
                 index < end;
                 index++) {

                if ("YES".equals(
                        sorted.get(index)
                                .actual()
                )) {

                    positiveRankSum +=
                            averageRank;
                }
            }

            start = end;
        }

        double numerator =
                positiveRankSum
                        - (
                        (double) positiveCount
                                * (positiveCount + 1)
                                / 2.0
                );

        return numerator
                / (
                (double) positiveCount
                        * negativeCount
        );
    }

    private static double kappa(
            int truePositive,
            int falsePositive,
            int trueNegative,
            int falseNegative
    ) {

        int total =
                truePositive
                        + falsePositive
                        + trueNegative
                        + falseNegative;

        if (total <= 0) {
            throw new IllegalStateException(
                    "Cannot calculate Kappa on empty data."
            );
        }

        int actualYes =
                truePositive
                        + falseNegative;

        int actualNo =
                trueNegative
                        + falsePositive;

        int predictedYes =
                truePositive
                        + falsePositive;

        int predictedNo =
                trueNegative
                        + falseNegative;

        double observedAgreement =
                (double) (
                        truePositive
                                + trueNegative
                ) / total;

        double expectedAgreement =
                (
                        (double) actualYes
                                * predictedYes
                                +
                                (double) actualNo
                                        * predictedNo
                )
                        / (
                        (double) total
                                * total
                );

        double denominator =
                1.0
                        - expectedAgreement;

        if (Math.abs(denominator)
                < 1.0e-15) {

            return 0.0;
        }

        return (
                observedAgreement
                        - expectedAgreement
        ) / denominator;
    }

    /**
     * NPofB20:
     *
     * <pre>
     * score = P(BUGGY=YES) / LOC
     * </pre>
     *
     * <p>Rows are ranked by descending score. OriginalIndex is used only as
     * deterministic tie-breaker. Rows are inspected until cumulative LOC
     * reaches or exceeds 20% of total LOC. Because inspection is performed at
     * class granularity, the final class may make the inspected LOC slightly
     * exceed the exact budget.</p>
     */
    private static NpofB20Result npofB20(
            List<Prediction> predictions
    ) {

        List<Prediction> ranked =
                new ArrayList<>(
                        predictions
                );

        ranked.sort(
                Comparator
                        .comparingDouble(
                                M2Metrics::normalizedScore
                        )
                        .reversed()
                        .thenComparingInt(
                                Prediction::originalIndex
                        )
        );

        double totalLoc = 0.0;
        int totalBuggy = 0;

        for (Prediction prediction
                : ranked) {

            totalLoc +=
                    prediction.loc();

            if ("YES".equals(
                    prediction.actual()
            )) {
                totalBuggy++;
            }
        }

        if (!(totalLoc > 0.0)) {
            throw new IllegalStateException(
                    "Total LOC must be positive."
            );
        }

        if (totalBuggy <= 0) {
            throw new IllegalStateException(
                    "NPofB20 requires at least one BUGGY observation."
            );
        }

        double budgetLoc =
                totalLoc
                        * LOC_BUDGET_FRACTION;

        double inspectedLoc = 0.0;
        int inspectedRows = 0;
        int buggyFound = 0;

        for (Prediction prediction
                : ranked) {

            if (inspectedLoc
                    >= budgetLoc) {

                break;
            }

            inspectedLoc +=
                    prediction.loc();

            inspectedRows++;

            if ("YES".equals(
                    prediction.actual()
            )) {

                buggyFound++;
            }
        }

        double value =
                (double) buggyFound
                        / totalBuggy;

        return new NpofB20Result(
                value,
                totalLoc,
                budgetLoc,
                inspectedLoc,
                inspectedRows,
                buggyFound,
                totalBuggy
        );
    }

    private static double normalizedScore(
            Prediction prediction
    ) {

        return prediction.probabilityYes()
                / prediction.loc();
    }

    private static void validateOofGroup(
            ExperimentKey key,
            List<Prediction> predictions
    ) {

        if (predictions.size()
                != EXPECTED_ROWS) {

            throw new IllegalStateException(
                    key
                            + ": expected "
                            + EXPECTED_ROWS
                            + " OOF predictions, found "
                            + predictions.size()
            );
        }

        Set<Integer> originalIndices =
                new HashSet<>();

        int yes = 0;
        int no = 0;

        for (Prediction prediction
                : predictions) {

            if (!originalIndices.add(
                    prediction.originalIndex()
            )) {

                throw new IllegalStateException(
                        key
                                + ": duplicate originalIndex "
                                + prediction.originalIndex()
                );
            }

            if ("YES".equals(
                    prediction.actual()
            )) {
                yes++;

            } else if ("NO".equals(
                    prediction.actual()
            )) {
                no++;

            } else {
                throw new IllegalStateException(
                        key
                                + ": invalid actual class "
                                + prediction.actual()
                );
            }

            if (!"YES".equals(
                    prediction.predicted()
            )
                    && !"NO".equals(
                    prediction.predicted()
            )) {

                throw new IllegalStateException(
                        key
                                + ": invalid predicted class "
                                + prediction.predicted()
                );
            }

            if (!(prediction.loc() > 0.0)
                    || !Double.isFinite(
                    prediction.loc()
            )) {

                throw new IllegalStateException(
                        key
                                + ": invalid LOC "
                                + prediction.loc()
                );
            }

            if (!Double.isFinite(
                    prediction.probabilityYes()
            )
                    || prediction.probabilityYes() < 0.0
                    || prediction.probabilityYes() > 1.0) {

                throw new IllegalStateException(
                        key
                                + ": invalid ProbabilityYES "
                                + prediction.probabilityYes()
                );
            }

            if (!Double.isFinite(
                    prediction.probabilityNo()
            )
                    || prediction.probabilityNo() < 0.0
                    || prediction.probabilityNo() > 1.0) {

                throw new IllegalStateException(
                        key
                                + ": invalid ProbabilityNO "
                                + prediction.probabilityNo()
                );
            }

            if (Math.abs(
                    prediction.probabilityYes()
                            + prediction.probabilityNo()
                            - 1.0
            ) > 1.0e-9) {

                throw new IllegalStateException(
                        key
                                + ": probabilities do not sum to 1."
                );
            }
        }

        if (yes != EXPECTED_BUGGY_YES
                || no != EXPECTED_BUGGY_NO) {

            throw new IllegalStateException(
                    key
                            + ": actual class distribution mismatch. YES="
                            + yes
                            + ", NO="
                            + no
            );
        }

        if (originalIndices.size()
                != EXPECTED_ROWS) {

            throw new IllegalStateException(
                    key
                            + ": incomplete unique OOF coverage."
            );
        }
    }

    private static void validateMetricResults(
            List<MetricResult> results
    ) {

        if (results.size()
                != EXPECTED_QUICK_METRIC_ROWS) {

            throw new IllegalStateException(
                    "Metric row count mismatch."
            );
        }

        for (MetricResult result
                : results) {

            validateUnitInterval(
                    "Precision",
                    result.precision(),
                    result
            );

            validateUnitInterval(
                    "Recall",
                    result.recall(),
                    result
            );

            validateUnitInterval(
                    "AUC",
                    result.auc(),
                    result
            );

            /*
             * Cohen's Kappa can theoretically be negative.
             * It must however be finite and <= 1.
             */
            if (!Double.isFinite(
                    result.kappa()
            )
                    || result.kappa() > 1.0
                    || result.kappa() < -1.0) {

                throw new IllegalStateException(
                        "Invalid Kappa for "
                                + result
                );
            }

            validateUnitInterval(
                    "NPofB20",
                    result.npofB20(),
                    result
            );

            int confusionTotal =
                    result.truePositive()
                            + result.falsePositive()
                            + result.trueNegative()
                            + result.falseNegative();

            if (confusionTotal
                    != EXPECTED_ROWS) {

                throw new IllegalStateException(
                        "Confusion matrix total mismatch for "
                                + result.configuration()
                                + " / "
                                + result.classifier()
                );
            }

            if (result.totalBuggy()
                    != EXPECTED_BUGGY_YES) {

                throw new IllegalStateException(
                        "NPofB20 total buggy mismatch."
                );
            }

            if (result.inspectedLoc()
                    < result.budgetLoc()) {

                throw new IllegalStateException(
                        "NPofB20 did not reach the 20% LOC budget."
                );
            }

            if (result.inspectedRows() <= 0
                    || result.inspectedRows()
                    > EXPECTED_ROWS) {

                throw new IllegalStateException(
                        "Invalid NPofB20 inspected-row count."
                );
            }
        }
    }

    private static void validateUnitInterval(
            String metric,
            double value,
            MetricResult result
    ) {

        if (!Double.isFinite(value)
                || value < 0.0
                || value > 1.0) {

            throw new IllegalStateException(
                    metric
                            + " out of range for "
                            + result.configuration()
                            + " / "
                            + result.classifier()
                            + ": "
                            + value
            );
        }
    }

    private static List<Prediction> readPredictions(
            Path input
    ) throws IOException {

        if (!Files.isRegularFile(input)) {
            throw new IllegalArgumentException(
                    "Prediction CSV not found: "
                            + input
            );
        }

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             input,
                             StandardCharsets.UTF_8
                     )) {

            String headerLine =
                    reader.readLine();

            if (headerLine == null) {
                throw new IllegalStateException(
                        "Empty prediction CSV."
                );
            }

            List<String> headers =
                    parseCsvLine(
                            removeBom(
                                    headerLine
                            )
                    );

            Map<String, Integer> columns =
                    new HashMap<>();

            for (int index = 0;
                 index < headers.size();
                 index++) {

                columns.put(
                        headers.get(index),
                        index
                );
            }

            for (String required
                    : REQUIRED_HEADERS) {

                if (!columns.containsKey(
                        required
                )) {

                    throw new IllegalStateException(
                            "Missing prediction column: "
                                    + required
                    );
                }
            }

            List<Prediction> rows =
                    new ArrayList<>();

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine())
                    != null) {

                lineNumber++;

                if (line.isBlank()) {
                    continue;
                }

                List<String> row =
                        parseCsvLine(
                                line
                        );

                if (row.size()
                        != headers.size()) {

                    throw new IllegalStateException(
                            "Column-count mismatch at line "
                                    + lineNumber
                    );
                }

                rows.add(
                        new Prediction(
                                value(
                                        row,
                                        columns,
                                        "Configuration"
                                ),
                                value(
                                        row,
                                        columns,
                                        "Classifier"
                                ),
                                parseInteger(
                                        value(
                                                row,
                                                columns,
                                                "Repetition"
                                        ),
                                        "Repetition",
                                        lineNumber
                                ),
                                parseInteger(
                                        value(
                                                row,
                                                columns,
                                                "Fold"
                                        ),
                                        "Fold",
                                        lineNumber
                                ),
                                parseInteger(
                                        value(
                                                row,
                                                columns,
                                                "OriginalIndex"
                                        ),
                                        "OriginalIndex",
                                        lineNumber
                                ),
                                value(
                                        row,
                                        columns,
                                        "Actual"
                                ),
                                value(
                                        row,
                                        columns,
                                        "Predicted"
                                ),
                                parseDouble(
                                        value(
                                                row,
                                                columns,
                                                "ProbabilityNO"
                                        ),
                                        "ProbabilityNO",
                                        lineNumber
                                ),
                                parseDouble(
                                        value(
                                                row,
                                                columns,
                                                "ProbabilityYES"
                                        ),
                                        "ProbabilityYES",
                                        lineNumber
                                ),
                                parseDouble(
                                        value(
                                                row,
                                                columns,
                                                "LOC"
                                        ),
                                        "LOC",
                                        lineNumber
                                )
                        )
                );
            }

            int expectedRows =
                    EXPECTED_ROWS
                            * EXPECTED_QUICK_CONFIGURATIONS
                            * EXPECTED_QUICK_CLASSIFIERS;

            if (rows.size()
                    != expectedRows) {

                throw new IllegalStateException(
                        "Prediction CSV row mismatch. Expected "
                                + expectedRows
                                + ", found "
                                + rows.size()
                );
            }

            return List.copyOf(
                    rows
            );
        }
    }

    private static void writeOutputs(
            Path repository,
            List<MetricResult> results
    ) throws IOException {

        Path directory =
                repository
                        .toAbsolutePath()
                        .normalize()
                        .resolve(
                                RESULT_DIRECTORY
                        );

        Files.createDirectories(
                directory
        );

        Path metricsCsv =
                directory.resolve(
                        "metrics_quick.csv"
                );

        Path validationTxt =
                directory.resolve(
                        "metrics_validation_quick.txt"
                );

        List<String> csv =
                new ArrayList<>();

        csv.add(
                "Configuration,Classifier,Repetition,OOFPredictions,"
                        + "TP,FP,TN,FN,Precision,Recall,AUC,Kappa,NPofB20,"
                        + "TotalLOC,BudgetLOC,InspectedLOC,InspectedRows,"
                        + "BuggyFound,TotalBuggy"
        );

        for (MetricResult result
                : results) {

            csv.add(
                    result.configuration()
                            + ","
                            + result.classifier()
                            + ","
                            + result.repetition()
                            + ","
                            + result.oofPredictions()
                            + ","
                            + result.truePositive()
                            + ","
                            + result.falsePositive()
                            + ","
                            + result.trueNegative()
                            + ","
                            + result.falseNegative()
                            + ","
                            + decimal(result.precision())
                            + ","
                            + decimal(result.recall())
                            + ","
                            + decimal(result.auc())
                            + ","
                            + decimal(result.kappa())
                            + ","
                            + decimal(result.npofB20())
                            + ","
                            + decimal(result.totalLoc())
                            + ","
                            + decimal(result.budgetLoc())
                            + ","
                            + decimal(result.inspectedLoc())
                            + ","
                            + result.inspectedRows()
                            + ","
                            + result.buggyFound()
                            + ","
                            + result.totalBuggy()
            );
        }

        Files.write(
                metricsCsv,
                csv,
                StandardCharsets.UTF_8
        );

        List<String> report =
                new ArrayList<>();

        report.add(
                "===== OPENJPA M2 METRICS QUICK VALIDATION ====="
        );

        report.add(
                "Metric rows            : "
                        + results.size()
        );

        report.add(
                "OOF rows/metric row     : "
                        + EXPECTED_ROWS
        );

        report.add(
                "Positive class          : YES"
        );

        report.add(
                "NPofB20 ranking         : P(BUGGY=YES) / LOC"
        );

        report.add(
                "NPofB20 budget          : 20% total LOC"
        );

        report.add(
                "NPofB20 boundary        : include class that reaches/exceeds budget"
        );

        report.add(
                "NPofB20 tie-break       : OriginalIndex ascending"
        );

        report.add(
                "Metrics CSV             : "
                        + metricsCsv
        );

        report.add("");

        for (MetricResult result
                : results) {

            report.add(
                    String.format(
                            Locale.ROOT,
                            "%s | %-12s | P=%.6f | R=%.6f | AUC=%.6f | "
                                    + "Kappa=%.6f | NPofB20=%.6f | "
                                    + "TP=%d FP=%d TN=%d FN=%d | "
                                    + "budget=%.1f inspected=%.1f rows=%d buggy=%d/%d",
                            result.configuration(),
                            result.classifier(),
                            result.precision(),
                            result.recall(),
                            result.auc(),
                            result.kappa(),
                            result.npofB20(),
                            result.truePositive(),
                            result.falsePositive(),
                            result.trueNegative(),
                            result.falseNegative(),
                            result.budgetLoc(),
                            result.inspectedLoc(),
                            result.inspectedRows(),
                            result.buggyFound(),
                            result.totalBuggy()
                    )
            );
        }

        report.add("");
        report.add("Precision range       : PASSED");
        report.add("Recall range          : PASSED");
        report.add("AUC range             : PASSED");
        report.add("Kappa range           : PASSED");
        report.add("NPofB20 range         : PASSED");
        report.add("Confusion totals      : PASSED");
        report.add("OOF coverage          : PASSED");
        report.add("NPofB20 LOC budget    : PASSED");
        report.add("ValidationPassed=True");
        report.add("==============================================");

        Files.write(
                validationTxt,
                report,
                StandardCharsets.UTF_8
        );
    }

    private static String decimal(
            double value
    ) {

        return String.format(
                Locale.ROOT,
                "%.12f",
                value
        );
    }

    private static String value(
            List<String> row,
            Map<String, Integer> columns,
            String column
    ) {

        Integer index =
                columns.get(
                        column
                );

        if (index == null) {
            throw new IllegalArgumentException(
                    "Missing column: "
                            + column
            );
        }

        return row.get(index)
                .trim();
    }

    private static int parseInteger(
            String text,
            String column,
            int lineNumber
    ) {

        try {
            return Integer.parseInt(
                    text
            );

        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "Invalid integer "
                            + column
                            + " at line "
                            + lineNumber
                            + ": "
                            + text,
                    exception
            );
        }
    }

    private static double parseDouble(
            String text,
            String column,
            int lineNumber
    ) {

        final double value;

        try {
            value =
                    Double.parseDouble(
                            text
                    );

        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "Invalid number "
                            + column
                            + " at line "
                            + lineNumber
                            + ": "
                            + text,
                    exception
            );
        }

        if (!Double.isFinite(
                value
        )) {

            throw new IllegalStateException(
                    "Non-finite "
                            + column
                            + " at line "
                            + lineNumber
            );
        }

        return value;
    }

    private static List<String> parseCsvLine(
            String line
    ) {

        List<String> result =
                new ArrayList<>();

        StringBuilder current =
                new StringBuilder();

        boolean quoted = false;

        for (int index = 0;
             index < line.length();
             index++) {

            char character =
                    line.charAt(index);

            if (character == '"') {

                if (quoted
                        && index + 1 < line.length()
                        && line.charAt(index + 1) == '"') {

                    current.append('"');
                    index++;

                } else {
                    quoted = !quoted;
                }

            } else if (character == ','
                    && !quoted) {

                result.add(
                        current.toString()
                );

                current.setLength(
                        0
                );

            } else {
                current.append(
                        character
                );
            }
        }

        if (quoted) {
            throw new IllegalArgumentException(
                    "Malformed CSV line."
            );
        }

        result.add(
                current.toString()
        );

        return result;
    }

    private static String removeBom(
            String value
    ) {

        if (!value.isEmpty()
                && value.charAt(0)
                == '\uFEFF') {

            return value.substring(
                    1
            );
        }

        return value;
    }

    private static void printSummary(
            List<MetricResult> results
    ) {

        System.out.println(
                "===== OPENJPA M2 METRICS QUICK ====="
        );

        System.out.println(
                "Metric rows          : "
                        + results.size()
        );

        System.out.println(
                "OOF rows/experiment  : "
                        + EXPECTED_ROWS
        );

        System.out.println(
                "NPofB20 ranking      : P(YES) / LOC"
        );

        System.out.println(
                "NPofB20 budget       : 20% total LOC"
        );

        System.out.println("");

        for (MetricResult result
                : results) {

            System.out.printf(
                    Locale.ROOT,
                    "%s %-12s | P=%.6f | R=%.6f | AUC=%.6f | "
                            + "Kappa=%.6f | NPofB20=%.6f%n",
                    result.configuration(),
                    result.classifier(),
                    result.precision(),
                    result.recall(),
                    result.auc(),
                    result.kappa(),
                    result.npofB20()
            );
        }

        System.out.println("");
        System.out.println(
                "ValidationPassed     : True"
        );

        System.out.println(
                "===================================="
        );
    }

    public static void main(
            String[] args
    ) throws Exception {

        Path repository =
                args.length >= 1
                        ? Path.of(
                                args[0]
                        )
                        : Path.of(".");

        List<MetricResult> results =
                calculateQuick(
                        repository
                );

        writeOutputs(
                repository,
                results
        );

        printSummary(
                results
        );
    }

    public record MetricResult(
            String configuration,
            String classifier,
            int repetition,
            int oofPredictions,
            int truePositive,
            int falsePositive,
            int trueNegative,
            int falseNegative,
            double precision,
            double recall,
            double auc,
            double kappa,
            double npofB20,
            double totalLoc,
            double budgetLoc,
            double inspectedLoc,
            int inspectedRows,
            int buggyFound,
            int totalBuggy
    ) {
    }

    private record ExperimentKey(
            String configuration,
            String classifier,
            int repetition
    ) {
    }

    private record Prediction(
            String configuration,
            String classifier,
            int repetition,
            int fold,
            int originalIndex,
            String actual,
            String predicted,
            double probabilityNo,
            double probabilityYes,
            double loc
    ) {
    }

    private record NpofB20Result(
            double value,
            double totalLoc,
            double budgetLoc,
            double inspectedLoc,
            int inspectedRows,
            int buggyFound,
            int totalBuggy
    ) {
    }
}
