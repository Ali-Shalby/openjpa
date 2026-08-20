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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Produces the final Milestone 3 what-if table and prevention estimates
 * from the validated per-observation prediction CSV.
 */
public final class M3SummaryGenerator {

    private static final Path INPUT =
            Path.of(
                    "isw2",
                    "results",
                    "m3",
                    "what_if_predictions.csv"
            );

    private static final Path OUTPUT_DIRECTORY =
            Path.of(
                    "isw2",
                    "results",
                    "m3"
            );

    private static final int EXPECTED_A_ROWS = 12_836;
    private static final int EXPECTED_B_PLUS_ROWS = 8_933;
    private static final int EXPECTED_B_ROWS = 8_933;
    private static final int EXPECTED_C_ROWS = 3_903;

    private static final int EXPECTED_A_REFERENCE_YES = 2_010;
    private static final int EXPECTED_B_PLUS_REFERENCE_YES = 1_723;
    private static final int EXPECTED_C_REFERENCE_YES = 287;

    private M3SummaryGenerator() {
        // Utility class.
    }

    public static void main(
            String[] args
    ) throws Exception {

        Path repository =
                args.length >= 1
                        ? Path.of(args[0])
                        : Path.of(".");

        Result result =
                generate(
                        repository
                );

        writeOutputs(
                repository,
                result
        );

        printSummary(
                result
        );
    }

    public static Result generate(
            Path repository
    ) throws IOException {

        Path input =
                repository
                        .toAbsolutePath()
                        .normalize()
                        .resolve(
                                INPUT
                        );

        List<Prediction> predictions =
                readPredictions(
                        input
                );

        Map<String, List<Prediction>> byDataset =
                new LinkedHashMap<>();

        for (Prediction prediction
                : predictions) {

            byDataset.computeIfAbsent(
                    prediction.dataset(),
                    ignored -> new ArrayList<>()
            ).add(
                    prediction
            );
        }

        DatasetStats a =
                stats(
                        "A",
                        require(
                                byDataset,
                                "A",
                                EXPECTED_A_ROWS
                        )
                );

        DatasetStats bPlus =
                stats(
                        "B+",
                        require(
                                byDataset,
                                "B+",
                                EXPECTED_B_PLUS_ROWS
                        )
                );

        DatasetStats b =
                stats(
                        "B",
                        require(
                                byDataset,
                                "B",
                                EXPECTED_B_ROWS
                        )
                );

        DatasetStats c =
                stats(
                        "C",
                        require(
                                byDataset,
                                "C",
                                EXPECTED_C_ROWS
                        )
                );

        if (a.referenceYes()
                != EXPECTED_A_REFERENCE_YES
                || bPlus.referenceYes()
                != EXPECTED_B_PLUS_REFERENCE_YES
                || c.referenceYes()
                != EXPECTED_C_REFERENCE_YES) {

            throw new IllegalStateException(
                    "Unexpected reference BUGGY counts."
            );
        }

        PairwiseTransitions transitions =
                transitions(
                        byDataset.get("B+"),
                        byDataset.get("B")
                );

        int netPrevented =
                bPlus.predictedYes()
                        - b.predictedYes();

        if (netPrevented
                != transitions.yesToNo()
                - transitions.noToYes()) {

            throw new IllegalStateException(
                    "Net prevention does not match transition matrix."
            );
        }

        if (netPrevented < 0) {
            throw new IllegalStateException(
                    "What-if increased the net predicted BUGGY count."
            );
        }

        double proportionOfAllBuggy =
                (double) netPrevented
                        / a.referenceYes();

        double proportionOfPreventable =
                (double) netPrevented
                        / bPlus.referenceYes();

        double probabilityReduction =
                bPlus.meanProbabilityYes()
                        - b.meanProbabilityYes();

        Result result =
                new Result(
                        a,
                        bPlus,
                        b,
                        c,
                        transitions,
                        netPrevented,
                        proportionOfAllBuggy,
                        proportionOfPreventable,
                        probabilityReduction
                );

        validate(
                result
        );

        return result;
    }

    private static void validate(
            Result result
    ) {

        if (result.a().rows()
                != result.bPlus().rows()
                + result.c().rows()) {

            throw new IllegalStateException(
                    "A rows != B+ rows + C rows."
            );
        }

        if (result.b().rows()
                != result.bPlus().rows()) {

            throw new IllegalStateException(
                    "B/B+ row mismatch."
            );
        }

        if (result.a().predictedYes()
                != result.bPlus().predictedYes()
                + result.c().predictedYes()) {

            throw new IllegalStateException(
                    "A predicted YES != B+ + C predicted YES."
            );
        }

        if (result.netPrevented()
                != 423) {

            throw new IllegalStateException(
                    "Unexpected net prevented count: "
                            + result.netPrevented()
            );
        }

        if (result.transitions().yesToNo()
                != 425
                || result.transitions().noToYes()
                != 2) {

            throw new IllegalStateException(
                    "Unexpected B+/B transition matrix."
            );
        }

        checkProportion(
                result.proportionOfAllBuggy()
        );

        checkProportion(
                result.proportionOfPreventable()
        );

        if (!Double.isFinite(
                result.meanProbabilityReduction()
        )) {

            throw new IllegalStateException(
                    "Non-finite probability reduction."
            );
        }
    }

    private static void checkProportion(
            double value
    ) {

        if (!Double.isFinite(value)
                || value < 0.0
                || value > 1.0) {

            throw new IllegalStateException(
                    "Invalid proportion: "
                            + value
            );
        }
    }

    private static PairwiseTransitions transitions(
            List<Prediction> bPlus,
            List<Prediction> b
    ) {

        Map<String, Prediction> bByKey =
                new HashMap<>();

        for (Prediction prediction
                : b) {

            String key =
                    key(
                            prediction
                    );

            if (bByKey.put(
                    key,
                    prediction
            ) != null) {

                throw new IllegalStateException(
                        "Duplicate B key: "
                                + key
                );
            }
        }

        int noToNo = 0;
        int noToYes = 0;
        int yesToNo = 0;
        int yesToYes = 0;

        for (Prediction plus
                : bPlus) {

            Prediction manipulated =
                    bByKey.get(
                            key(
                                    plus
                            )
                    );

            if (manipulated == null) {
                throw new IllegalStateException(
                        "Missing B prediction for "
                                + key(
                                plus
                        )
                );
            }

            if (!plus.referenceBuggy()
                    .equals(
                            manipulated.referenceBuggy()
                    )) {

                throw new IllegalStateException(
                        "B/B+ reference BUGGY mismatch."
                );
            }

            if (!(plus.nSmells() > 0.0)
                    || manipulated.nSmells()
                    != 0.0) {

                throw new IllegalStateException(
                        "Unexpected B/B+ NSmells relation."
                );
            }

            String from =
                    plus.predictedBuggy();

            String to =
                    manipulated.predictedBuggy();

            if ("NO".equals(from)
                    && "NO".equals(to)) {

                noToNo++;

            } else if ("NO".equals(from)
                    && "YES".equals(to)) {

                noToYes++;

            } else if ("YES".equals(from)
                    && "NO".equals(to)) {

                yesToNo++;

            } else if ("YES".equals(from)
                    && "YES".equals(to)) {

                yesToYes++;

            } else {

                throw new IllegalStateException(
                        "Unexpected predicted class transition."
                );
            }
        }

        if (noToNo
                + noToYes
                + yesToNo
                + yesToYes
                != EXPECTED_B_PLUS_ROWS) {

            throw new IllegalStateException(
                    "Transition total mismatch."
            );
        }

        return new PairwiseTransitions(
                noToNo,
                noToYes,
                yesToNo,
                yesToYes
        );
    }

    private static DatasetStats stats(
            String dataset,
            List<Prediction> predictions
    ) {

        int referenceYes = 0;
        int referenceNo = 0;
        int predictedYes = 0;
        int predictedNo = 0;
        double probabilityYesSum = 0.0;

        for (Prediction prediction
                : predictions) {

            if ("YES".equals(
                    prediction.referenceBuggy()
            )) {

                referenceYes++;

            } else if ("NO".equals(
                    prediction.referenceBuggy()
            )) {

                referenceNo++;

            } else {

                throw new IllegalStateException(
                        "Unexpected reference BUGGY."
                );
            }

            if ("YES".equals(
                    prediction.predictedBuggy()
            )) {

                predictedYes++;

            } else if ("NO".equals(
                    prediction.predictedBuggy()
            )) {

                predictedNo++;

            } else {

                throw new IllegalStateException(
                        "Unexpected predicted BUGGY."
                );
            }

            probabilityYesSum +=
                    prediction.probabilityYes();
        }

        return new DatasetStats(
                dataset,
                predictions.size(),
                referenceYes,
                referenceNo,
                predictedYes,
                predictedNo,
                probabilityYesSum
                        / predictions.size()
        );
    }

    private static List<Prediction> readPredictions(
            Path input
    ) throws IOException {

        if (!Files.isRegularFile(
                input
        )) {

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
                        "Prediction CSV is empty."
                );
            }

            List<String> headers =
                    parseCsvLine(
                            removeBom(
                                    headerLine
                            )
                    );

            Map<String, Integer> columns =
                    new LinkedHashMap<>();

            for (int index = 0;
                 index < headers.size();
                 index++) {

                columns.put(
                        headers.get(index),
                        index
                );
            }

            List<String> required =
                    List.of(
                            "Dataset",
                            "Project",
                            "Class",
                            "ReleaseIndex",
                            "ReferenceBUGGY",
                            "PredictedBUGGY",
                            "ProbabilityYES",
                            "NSmells"
                    );

            for (String column
                    : required) {

                if (!columns.containsKey(
                        column
                )) {

                    throw new IllegalStateException(
                            "Missing column: "
                                    + column
                    );
                }
            }

            List<Prediction> result =
                    new ArrayList<>();

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine())
                    != null) {

                lineNumber++;

                if (line.isBlank()) {
                    continue;
                }

                List<String> values =
                        parseCsvLine(
                                line
                        );

                if (values.size()
                        != headers.size()) {

                    throw new IllegalStateException(
                            "Column mismatch at line "
                                    + lineNumber
                    );
                }

                result.add(
                        new Prediction(
                                value(
                                        values,
                                        columns,
                                        "Dataset"
                                ),
                                value(
                                        values,
                                        columns,
                                        "Project"
                                ),
                                value(
                                        values,
                                        columns,
                                        "Class"
                                ),
                                Integer.parseInt(
                                        value(
                                                values,
                                                columns,
                                                "ReleaseIndex"
                                        )
                                ),
                                value(
                                        values,
                                        columns,
                                        "ReferenceBUGGY"
                                ),
                                value(
                                        values,
                                        columns,
                                        "PredictedBUGGY"
                                ),
                                Double.parseDouble(
                                        value(
                                                values,
                                                columns,
                                                "ProbabilityYES"
                                        )
                                ),
                                Double.parseDouble(
                                        value(
                                                values,
                                                columns,
                                                "NSmells"
                                        )
                                )
                        )
                );
            }

            int expected =
                    EXPECTED_A_ROWS
                            + EXPECTED_B_PLUS_ROWS
                            + EXPECTED_B_ROWS
                            + EXPECTED_C_ROWS;

            if (result.size()
                    != expected) {

                throw new IllegalStateException(
                        "Prediction row mismatch. Expected "
                                + expected
                                + ", found "
                                + result.size()
                );
            }

            return List.copyOf(
                    result
            );
        }
    }

    private static List<Prediction> require(
            Map<String, List<Prediction>> byDataset,
            String name,
            int expectedRows
    ) {

        List<Prediction> result =
                byDataset.get(
                        name
                );

        if (result == null
                || result.size()
                != expectedRows) {

            throw new IllegalStateException(
                    "Unexpected rows for dataset "
                            + name
            );
        }

        return result;
    }

    private static String key(
            Prediction prediction
    ) {

        return prediction.releaseIndex()
                + "|"
                + prediction.classPath();
    }

    private static void writeOutputs(
            Path repository,
            Result result
    ) throws IOException {

        Path directory =
                repository
                        .toAbsolutePath()
                        .normalize()
                        .resolve(
                                OUTPUT_DIRECTORY
                        );

        Files.createDirectories(
                directory
        );

        Path tableCsv =
                directory.resolve(
                        "what_if_result.csv"
                );

        Path validationTxt =
                directory.resolve(
                        "what_if_result_validation.txt"
                );

        List<String> csv =
                new ArrayList<>();

        csv.add(
                "Dataset,Rows,ActualBuggy,EstimatedBuggy,MeanProbabilityYES"
        );

        csv.add(
                datasetLine(
                        result.a(),
                        true
                )
        );

        csv.add(
                datasetLine(
                        result.bPlus(),
                        true
                )
        );

        csv.add(
                "B,"
                        + result.b().rows()
                        + ",,"
                        + result.b().predictedYes()
                        + ","
                        + decimal(
                        result.b().meanProbabilityYes()
                )
        );

        csv.add(
                datasetLine(
                        result.c(),
                        true
                )
        );

        csv.add("");
        csv.add(
                "Measure,Value"
        );

        csv.add(
                "Gross YES->NO,"
                        + result.transitions().yesToNo()
        );

        csv.add(
                "Adverse NO->YES,"
                        + result.transitions().noToYes()
        );

        csv.add(
                "Net prevented,"
                        + result.netPrevented()
        );

        csv.add(
                "Net prevented / all actual buggy,"
                        + decimal(
                        result.proportionOfAllBuggy()
                )
        );

        csv.add(
                "Net prevented / preventable actual buggy,"
                        + decimal(
                        result.proportionOfPreventable()
                )
        );

        csv.add(
                "Mean P(YES) reduction B+->B,"
                        + decimal(
                        result.meanProbabilityReduction()
                )
        );

        Files.write(
                tableCsv,
                csv,
                StandardCharsets.UTF_8
        );

        List<String> report =
                new ArrayList<>();

        report.add(
                "===== OPENJPA M3 FINAL WHAT-IF RESULT ====="
        );

        report.add(
                "Dataset | Actual BUGGY | Estimated BUGGY"
        );

        report.add(
                String.format(
                        Locale.ROOT,
                        "A       | %4d         | %4d",
                        result.a().referenceYes(),
                        result.a().predictedYes()
                )
        );

        report.add(
                String.format(
                        Locale.ROOT,
                        "B+      | %4d         | %4d",
                        result.bPlus().referenceYes(),
                        result.bPlus().predictedYes()
                )
        );

        report.add(
                String.format(
                        Locale.ROOT,
                        "B       |    -         | %4d",
                        result.b().predictedYes()
                )
        );

        report.add(
                String.format(
                        Locale.ROOT,
                        "C       | %4d         | %4d",
                        result.c().referenceYes(),
                        result.c().predictedYes()
                )
        );

        report.add("");
        report.add(
                "B+ predicted YES -> B predicted NO : "
                        + result.transitions().yesToNo()
        );

        report.add(
                "B+ predicted NO  -> B predicted YES: "
                        + result.transitions().noToYes()
        );

        report.add(
                "Net estimated prevented             : "
                        + result.netPrevented()
        );

        report.add(
                String.format(
                        Locale.ROOT,
                        "Proportion of all actual BUGGY        : %.6f (%.2f%%)",
                        result.proportionOfAllBuggy(),
                        result.proportionOfAllBuggy()
                                * 100.0
                )
        );

        report.add(
                String.format(
                        Locale.ROOT,
                        "Out of preventable actual BUGGY       : %.6f (%.2f%%)",
                        result.proportionOfPreventable(),
                        result.proportionOfPreventable()
                                * 100.0
                )
        );

        report.add(
                String.format(
                        Locale.ROOT,
                        "Mean P(YES) B+                       : %.6f",
                        result.bPlus().meanProbabilityYes()
                )
        );

        report.add(
                String.format(
                        Locale.ROOT,
                        "Mean P(YES) B                        : %.6f",
                        result.b().meanProbabilityYes()
                )
        );

        report.add(
                String.format(
                        Locale.ROOT,
                        "Mean P(YES) reduction B+ -> B        : %.6f",
                        result.meanProbabilityReduction()
                )
        );

        report.add("");
        report.add(
                "Transition matrix                  : PASSED"
        );

        report.add(
                "Net = YES->NO - NO->YES            : PASSED"
        );

        report.add(
                "A = B+ union C predicted counts    : PASSED"
        );

        report.add(
                "Proportion ranges                  : PASSED"
        );

        report.add(
                "ValidationPassed=True"
        );

        report.add(
                "============================================"
        );

        Files.write(
                validationTxt,
                report,
                StandardCharsets.UTF_8
        );
    }

    private static String datasetLine(
            DatasetStats stats,
            boolean actualKnown
    ) {

        return stats.dataset()
                + ","
                + stats.rows()
                + ","
                + (
                actualKnown
                        ? stats.referenceYes()
                        : ""
        )
                + ","
                + stats.predictedYes()
                + ","
                + decimal(
                stats.meanProbabilityYes()
        );
    }

    private static void printSummary(
            Result result
    ) {

        System.out.println(
                "===== OPENJPA M3 FINAL WHAT-IF ====="
        );

        System.out.println(
                "A  actual/predicted BUGGY : "
                        + result.a().referenceYes()
                        + " / "
                        + result.a().predictedYes()
        );

        System.out.println(
                "B+ actual/predicted BUGGY : "
                        + result.bPlus().referenceYes()
                        + " / "
                        + result.bPlus().predictedYes()
        );

        System.out.println(
                "B  estimated BUGGY        : "
                        + result.b().predictedYes()
        );

        System.out.println(
                "C  actual/predicted BUGGY : "
                        + result.c().referenceYes()
                        + " / "
                        + result.c().predictedYes()
        );

        System.out.println("");

        System.out.println(
                "Gross YES->NO             : "
                        + result.transitions().yesToNo()
        );

        System.out.println(
                "Adverse NO->YES            : "
                        + result.transitions().noToYes()
        );

        System.out.println(
                "Net estimated prevented    : "
                        + result.netPrevented()
        );

        System.out.printf(
                Locale.ROOT,
                "Proportion of all BUGGY    : %.2f%%%n",
                result.proportionOfAllBuggy()
                        * 100.0
        );

        System.out.printf(
                Locale.ROOT,
                "Out of preventable BUGGY   : %.2f%%%n",
                result.proportionOfPreventable()
                        * 100.0
        );

        System.out.println("");

        System.out.println(
                "ValidationPassed           : True"
        );

        System.out.println(
                "===================================="
        );
    }

    private static String value(
            List<String> values,
            Map<String, Integer> columns,
            String column
    ) {

        Integer index =
                columns.get(
                        column
                );

        if (index == null) {
            throw new IllegalArgumentException(
                    "Missing column "
                            + column
            );
        }

        return values.get(index)
                .trim();
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
                        && index + 1
                        < line.length()
                        && line.charAt(
                        index + 1
                ) == '"') {

                    current.append('"');
                    index++;

                } else {

                    quoted =
                            !quoted;
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

    public record DatasetStats(
            String dataset,
            int rows,
            int referenceYes,
            int referenceNo,
            int predictedYes,
            int predictedNo,
            double meanProbabilityYes
    ) {
    }

    public record PairwiseTransitions(
            int noToNo,
            int noToYes,
            int yesToNo,
            int yesToYes
    ) {
    }

    public record Result(
            DatasetStats a,
            DatasetStats bPlus,
            DatasetStats b,
            DatasetStats c,
            PairwiseTransitions transitions,
            int netPrevented,
            double proportionOfAllBuggy,
            double proportionOfPreventable,
            double meanProbabilityReduction
    ) {
    }

    private record Prediction(
            String dataset,
            String project,
            String classPath,
            int releaseIndex,
            String referenceBuggy,
            String predictedBuggy,
            double probabilityYes,
            double nSmells
    ) {
    }
}
