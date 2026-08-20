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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Aggregates the 120 FULL raw metric rows into the 12 final
 * Configuration x Classifier summaries.
 *
 * <p>For each metric, the summary reports mean, sample standard deviation,
 * minimum and maximum over the 10 repetitions.</p>
 */
public final class M2SummaryGenerator {

    private static final int EXPECTED_REPETITIONS = 10;
    private static final int EXPECTED_RAW_ROWS = 120;
    private static final int EXPECTED_SUMMARY_ROWS = 12;

    private static final Path INPUT =
            Path.of(
                    "isw2",
                    "results",
                    "m2",
                    "full",
                    "classifier_metrics_full.csv"
            );

    private static final Path OUTPUT_DIRECTORY =
            Path.of(
                    "isw2",
                    "results",
                    "m2",
                    "summary"
            );

    private static final List<String> METRICS =
            List.of(
                    "Precision",
                    "Recall",
                    "AUC",
                    "Kappa",
                    "NPofB20"
            );

    private M2SummaryGenerator() {
        // Utility class.
    }

    public static void main(
            String[] args
    ) throws Exception {

        Path repository =
                args.length >= 1
                        ? Path.of(args[0])
                        : Path.of(".");

        List<RawMetricRow> raw =
                readRaw(
                        repository
                                .toAbsolutePath()
                                .normalize()
                                .resolve(INPUT)
                );

        List<SummaryRow> summary =
                summarize(
                        raw
                );

        writeOutputs(
                repository,
                summary
        );

        printSummary(
                summary
        );
    }

    private static List<RawMetricRow> readRaw(
            Path input
    ) throws IOException {

        if (!Files.isRegularFile(input)) {
            throw new IllegalArgumentException(
                    "Raw FULL metrics not found: "
                            + input
            );
        }

        List<RawMetricRow> result =
                new ArrayList<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             input,
                             StandardCharsets.UTF_8
                     )) {

            String headerLine =
                    reader.readLine();

            if (headerLine == null) {
                throw new IllegalStateException(
                        "Empty FULL metric CSV."
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
                    new ArrayList<>();

            required.add(
                    "Configuration"
            );

            required.add(
                    "Classifier"
            );

            required.add(
                    "Repetition"
            );

            required.addAll(
                    METRICS
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
                        new RawMetricRow(
                                get(
                                        values,
                                        columns,
                                        "Configuration"
                                ),
                                get(
                                        values,
                                        columns,
                                        "Classifier"
                                ),
                                parseInt(
                                        get(
                                                values,
                                                columns,
                                                "Repetition"
                                        ),
                                        "Repetition",
                                        lineNumber
                                ),
                                parseDouble(
                                        get(
                                                values,
                                                columns,
                                                "Precision"
                                        ),
                                        "Precision",
                                        lineNumber
                                ),
                                parseDouble(
                                        get(
                                                values,
                                                columns,
                                                "Recall"
                                        ),
                                        "Recall",
                                        lineNumber
                                ),
                                parseDouble(
                                        get(
                                                values,
                                                columns,
                                                "AUC"
                                        ),
                                        "AUC",
                                        lineNumber
                                ),
                                parseDouble(
                                        get(
                                                values,
                                                columns,
                                                "Kappa"
                                        ),
                                        "Kappa",
                                        lineNumber
                                ),
                                parseDouble(
                                        get(
                                                values,
                                                columns,
                                                "NPofB20"
                                        ),
                                        "NPofB20",
                                        lineNumber
                                )
                        )
                );
            }
        }

        if (result.size()
                != EXPECTED_RAW_ROWS) {

            throw new IllegalStateException(
                    "Expected "
                            + EXPECTED_RAW_ROWS
                            + " raw FULL rows, found "
                            + result.size()
            );
        }

        return List.copyOf(
                result
        );
    }

    private static List<SummaryRow> summarize(
            List<RawMetricRow> raw
    ) {

        Map<GroupKey, List<RawMetricRow>> grouped =
                new LinkedHashMap<>();

        for (RawMetricRow row
                : raw) {

            GroupKey key =
                    new GroupKey(
                            row.configuration(),
                            row.classifier()
                    );

            grouped.computeIfAbsent(
                    key,
                    ignored -> new ArrayList<>()
            ).add(
                    row
            );
        }

        if (grouped.size()
                != EXPECTED_SUMMARY_ROWS) {

            throw new IllegalStateException(
                    "Expected "
                            + EXPECTED_SUMMARY_ROWS
                            + " summary groups, found "
                            + grouped.size()
            );
        }

        List<SummaryRow> result =
                new ArrayList<>(
                        grouped.size()
                );

        for (Map.Entry<GroupKey, List<RawMetricRow>> entry
                : grouped.entrySet()) {

            GroupKey key =
                    entry.getKey();

            List<RawMetricRow> rows =
                    entry.getValue();

            validateRepetitions(
                    key,
                    rows
            );

            result.add(
                    new SummaryRow(
                            key.configuration(),
                            key.classifier(),
                            rows.size(),
                            stats(
                                    rows.stream()
                                            .map(
                                                    RawMetricRow::precision
                                            )
                                            .toList()
                            ),
                            stats(
                                    rows.stream()
                                            .map(
                                                    RawMetricRow::recall
                                            )
                                            .toList()
                            ),
                            stats(
                                    rows.stream()
                                            .map(
                                                    RawMetricRow::auc
                                            )
                                            .toList()
                            ),
                            stats(
                                    rows.stream()
                                            .map(
                                                    RawMetricRow::kappa
                                            )
                                            .toList()
                            ),
                            stats(
                                    rows.stream()
                                            .map(
                                                    RawMetricRow::npofB20
                                            )
                                            .toList()
                            )
                    )
            );
        }

        result.sort(
                Comparator.comparing(
                                SummaryRow::configuration
                        )
                        .thenComparing(
                                SummaryRow::classifier
                        )
        );

        validateSummary(
                result
        );

        return List.copyOf(
                result
        );
    }

    private static void validateRepetitions(
            GroupKey key,
            List<RawMetricRow> rows
    ) {

        if (rows.size()
                != EXPECTED_REPETITIONS) {

            throw new IllegalStateException(
                    key
                            + ": expected "
                            + EXPECTED_REPETITIONS
                            + " repetitions, found "
                            + rows.size()
            );
        }

        boolean[] seen =
                new boolean[
                        EXPECTED_REPETITIONS
                                + 1
                ];

        for (RawMetricRow row
                : rows) {

            int repetition =
                    row.repetition();

            if (repetition < 1
                    || repetition
                    > EXPECTED_REPETITIONS) {

                throw new IllegalStateException(
                        key
                                + ": invalid repetition "
                                + repetition
                );
            }

            if (seen[repetition]) {
                throw new IllegalStateException(
                        key
                                + ": duplicate repetition "
                                + repetition
                );
            }

            seen[repetition] = true;
        }

        for (int repetition = 1;
             repetition
                     <= EXPECTED_REPETITIONS;
             repetition++) {

            if (!seen[repetition]) {
                throw new IllegalStateException(
                        key
                                + ": missing repetition "
                                + repetition
                );
            }
        }
    }

    private static Stats stats(
            List<Double> values
    ) {

        if (values.size()
                != EXPECTED_REPETITIONS) {

            throw new IllegalStateException(
                    "Unexpected metric sample size."
            );
        }

        double sum = 0.0;
        double minimum =
                Double.POSITIVE_INFINITY;
        double maximum =
                Double.NEGATIVE_INFINITY;

        for (double value
                : values) {

            if (!Double.isFinite(
                    value
            )) {

                throw new IllegalStateException(
                        "Non-finite metric value."
                );
            }

            sum += value;

            minimum =
                    Math.min(
                            minimum,
                            value
                    );

            maximum =
                    Math.max(
                            maximum,
                            value
                    );
        }

        double mean =
                sum
                        / values.size();

        double squaredDeviationSum =
                0.0;

        for (double value
                : values) {

            double difference =
                    value
                            - mean;

            squaredDeviationSum +=
                    difference
                            * difference;
        }

        /*
         * Sample standard deviation over the 10 repetitions.
         */
        double standardDeviation =
                Math.sqrt(
                        squaredDeviationSum
                                / (
                                values.size()
                                        - 1
                        )
                );

        return new Stats(
                mean,
                standardDeviation,
                minimum,
                maximum
        );
    }

    private static void validateSummary(
            List<SummaryRow> summary
    ) {

        if (summary.size()
                != EXPECTED_SUMMARY_ROWS) {

            throw new IllegalStateException(
                    "Summary row count mismatch."
            );
        }

        for (SummaryRow row
                : summary) {

            if (row.repetitions()
                    != EXPECTED_REPETITIONS) {

                throw new IllegalStateException(
                        "Unexpected repetitions in summary."
                );
            }

            validateStats(
                    "Precision",
                    row.precision(),
                    0.0,
                    1.0
            );

            validateStats(
                    "Recall",
                    row.recall(),
                    0.0,
                    1.0
            );

            validateStats(
                    "AUC",
                    row.auc(),
                    0.0,
                    1.0
            );

            validateStats(
                    "Kappa",
                    row.kappa(),
                    -1.0,
                    1.0
            );

            validateStats(
                    "NPofB20",
                    row.npofB20(),
                    0.0,
                    1.0
            );
        }
    }

    private static void validateStats(
            String name,
            Stats stats,
            double minimum,
            double maximum
    ) {

        if (!Double.isFinite(
                stats.mean()
        )
                || !Double.isFinite(
                stats.standardDeviation()
        )
                || !Double.isFinite(
                stats.minimum()
        )
                || !Double.isFinite(
                stats.maximum()
        )) {

            throw new IllegalStateException(
                    "Non-finite "
                            + name
                            + " summary."
            );
        }

        if (stats.mean() < minimum
                || stats.mean() > maximum
                || stats.minimum() < minimum
                || stats.maximum() > maximum
                || stats.minimum()
                > stats.maximum()
                || stats.standardDeviation()
                < 0.0) {

            throw new IllegalStateException(
                    "Invalid "
                            + name
                            + " summary."
            );
        }
    }

    private static void writeOutputs(
            Path repository,
            List<SummaryRow> summary
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

        Path summaryCsv =
                directory.resolve(
                        "classifier_summary_full.csv"
                );

        Path validationTxt =
                directory.resolve(
                        "summary_validation_full.txt"
                );

        List<String> csv =
                new ArrayList<>();

        csv.add(
                "Configuration,Classifier,Repetitions,"
                        + metricHeader(
                                "Precision"
                        )
                        + ","
                        + metricHeader(
                                "Recall"
                        )
                        + ","
                        + metricHeader(
                                "AUC"
                        )
                        + ","
                        + metricHeader(
                                "Kappa"
                        )
                        + ","
                        + metricHeader(
                                "NPofB20"
                        )
        );

        for (SummaryRow row
                : summary) {

            csv.add(
                    row.configuration()
                            + ","
                            + row.classifier()
                            + ","
                            + row.repetitions()
                            + ","
                            + statsCsv(
                            row.precision()
                    )
                            + ","
                            + statsCsv(
                            row.recall()
                    )
                            + ","
                            + statsCsv(
                            row.auc()
                    )
                            + ","
                            + statsCsv(
                            row.kappa()
                    )
                            + ","
                            + statsCsv(
                            row.npofB20()
                    )
            );
        }

        Files.write(
                summaryCsv,
                csv,
                StandardCharsets.UTF_8
        );

        List<String> report =
                new ArrayList<>();

        report.add(
                "===== OPENJPA M2 FULL SUMMARY VALIDATION ====="
        );

        report.add(
                "Raw metric rows       : "
                        + EXPECTED_RAW_ROWS
        );

        report.add(
                "Summary rows          : "
                        + summary.size()
        );

        report.add(
                "Repetitions/group     : "
                        + EXPECTED_REPETITIONS
        );

        report.add(
                "StdDev                : sample (n-1)"
        );

        report.add(
                "Summary CSV           : "
                        + summaryCsv
        );

        report.add("");

        for (SummaryRow row
                : summary) {

            report.add(
                    String.format(
                            Locale.ROOT,
                            "%s | %-12s | "
                                    + "P=%.6f±%.6f | "
                                    + "R=%.6f±%.6f | "
                                    + "AUC=%.6f±%.6f | "
                                    + "Kappa=%.6f±%.6f | "
                                    + "NPofB20=%.6f±%.6f",
                            row.configuration(),
                            row.classifier(),
                            row.precision().mean(),
                            row.precision().standardDeviation(),
                            row.recall().mean(),
                            row.recall().standardDeviation(),
                            row.auc().mean(),
                            row.auc().standardDeviation(),
                            row.kappa().mean(),
                            row.kappa().standardDeviation(),
                            row.npofB20().mean(),
                            row.npofB20().standardDeviation()
                    )
            );
        }

        report.add("");
        report.add(
                "Global winners by mean:"
        );

        for (String metric
                : METRICS) {

            SummaryRow winner =
                    summary.stream()
                            .max(
                                    Comparator.comparingDouble(
                                            row ->
                                                    metricMean(
                                                            row,
                                                            metric
                                                    )
                                    )
                            )
                            .orElseThrow();

            report.add(
                    String.format(
                            Locale.ROOT,
                            "  %-9s -> %s %s = %.6f",
                            metric,
                            winner.configuration(),
                            winner.classifier(),
                            metricMean(
                                    winner,
                                    metric
                            )
                    )
            );
        }

        report.add("");
        report.add(
                "Repetition coverage   : PASSED"
        );

        report.add(
                "Summary metric ranges : PASSED"
        );

        report.add(
                "ValidationPassed=True"
        );

        report.add(
                "==============================================="
        );

        Files.write(
                validationTxt,
                report,
                StandardCharsets.UTF_8
        );
    }

    private static String metricHeader(
            String metric
    ) {

        return metric
                + "Mean,"
                + metric
                + "StdDev,"
                + metric
                + "Min,"
                + metric
                + "Max";
    }

    private static String statsCsv(
            Stats stats
    ) {

        return decimal(
                stats.mean()
        )
                + ","
                + decimal(
                stats.standardDeviation()
        )
                + ","
                + decimal(
                stats.minimum()
        )
                + ","
                + decimal(
                stats.maximum()
        );
    }

    private static double metricMean(
            SummaryRow row,
            String metric
    ) {

        return switch (metric) {

            case "Precision" ->
                    row.precision()
                            .mean();

            case "Recall" ->
                    row.recall()
                            .mean();

            case "AUC" ->
                    row.auc()
                            .mean();

            case "Kappa" ->
                    row.kappa()
                            .mean();

            case "NPofB20" ->
                    row.npofB20()
                            .mean();

            default ->
                    throw new IllegalArgumentException(
                            "Unknown metric: "
                                    + metric
                    );
        };
    }

    private static void printSummary(
            List<SummaryRow> summary
    ) {

        System.out.println(
                "===== OPENJPA M2 FULL SUMMARY ====="
        );

        System.out.println(
                "Raw metric rows      : 120"
        );

        System.out.println(
                "Summary rows         : "
                        + summary.size()
        );

        System.out.println(
                "Repetitions/group    : 10"
        );

        System.out.println("");

        for (SummaryRow row
                : summary) {

            System.out.printf(
                    Locale.ROOT,
                    "%s %-12s | P=%.6f | R=%.6f | AUC=%.6f | "
                            + "Kappa=%.6f | NPofB20=%.6f%n",
                    row.configuration(),
                    row.classifier(),
                    row.precision().mean(),
                    row.recall().mean(),
                    row.auc().mean(),
                    row.kappa().mean(),
                    row.npofB20().mean()
            );
        }

        System.out.println("");
        System.out.println(
                "ValidationPassed     : True"
        );

        System.out.println(
                "=================================="
        );
    }

    private static String get(
            List<String> row,
            Map<String, Integer> columns,
            String name
    ) {

        Integer index =
                columns.get(
                        name
                );

        if (index == null) {
            throw new IllegalArgumentException(
                    "Missing column "
                            + name
            );
        }

        return row.get(index)
                .trim();
    }

    private static int parseInt(
            String value,
            String name,
            int line
    ) {

        try {
            return Integer.parseInt(
                    value
            );

        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "Invalid "
                            + name
                            + " at line "
                            + line,
                    exception
            );
        }
    }

    private static double parseDouble(
            String value,
            String name,
            int line
    ) {

        try {
            double parsed =
                    Double.parseDouble(
                            value
                    );

            if (!Double.isFinite(
                    parsed
            )) {

                throw new IllegalStateException(
                        "Non-finite "
                                + name
                                + " at line "
                                + line
                );
            }

            return parsed;

        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "Invalid "
                            + name
                            + " at line "
                            + line,
                    exception
            );
        }
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
                        && index + 1 < line.length()
                        && line.charAt(index + 1)
                        == '"') {

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

    public record Stats(
            double mean,
            double standardDeviation,
            double minimum,
            double maximum
    ) {
    }

    public record SummaryRow(
            String configuration,
            String classifier,
            int repetitions,
            Stats precision,
            Stats recall,
            Stats auc,
            Stats kappa,
            Stats npofB20
    ) {
    }

    private record GroupKey(
            String configuration,
            String classifier
    ) {
    }

    private record RawMetricRow(
            String configuration,
            String classifier,
            int repetition,
            double precision,
            double recall,
            double auc,
            double kappa,
            double npofB20
    ) {
    }
}
