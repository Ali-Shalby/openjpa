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

package it.uniroma2.isw2.openjpa.dataset;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DatasetAGenerator {

    private static final String PROJECT =
            "OPENJPA";

    private static final int EXPECTED_ROWS =
            12_836;

    private static final int EXPECTED_RELEASES =
            12;

    private static final int EXPECTED_BUGGY_YES =
            2_010;

    private static final int EXPECTED_BUGGY_NO =
            10_826;

    private static final long EXPECTED_NSMELLS_SUM =
            94_308L;

    private static final long EXPECTED_NFIX_SUM =
            7_523L;

    private static final Path METRICS_INPUT =
            Path.of(
                    "isw2",
                    "datasets",
                    "class_metrics_with_smells.csv"
            );

    private static final Path NFIX_INPUT =
            Path.of(
                    "isw2",
                    "datasets",
                    "nfix_metrics.csv"
            );

    private static final Path BUGGINESS_INPUT =
            Path.of(
                    "isw2",
                    "datasets",
                    "bugginess_labels.csv"
            );

    private static final Path OUTPUT =
            Path.of(
                    "isw2",
                    "datasets",
                    "openjpa_dataset_a.csv"
            );

    private static final Path VALIDATION_OUTPUT =
            Path.of(
                    "isw2",
                    "results",
                    "dataset",
                    "dataset_a_validation.txt"
            );

    private static final List<String> METRICS_HEADERS =
            List.of(
                    "ReleaseIndex",
                    "Version",
                    "CommitId",
                    "Class",
                    "LOC",
                    "LOC_TOUCHED",
                    "NR",
                    "NAUTH",
                    "LOC_ADDED",
                    "MAX_LOC_ADDED",
                    "AVG_LOC_ADDED",
                    "CHURN",
                    "MAX_CHURN",
                    "AVG_CHURN",
                    "CHANGE_SET_SIZE",
                    "MAX_CHANGE_SET",
                    "AVG_CHANGE_SET",
                    "AGE_WEEKS",
                    "WEIGHTED_AGE_WEEKS",
                    "IGNORED_ZERO_LOC_REVS",
                    "NSmells"
            );

    private static final List<String> NFIX_HEADERS =
            List.of(
                    "ReleaseIndex",
                    "Version",
                    "CommitId",
                    "Class",
                    "NFIX"
            );

    private static final List<String> BUGGINESS_HEADERS =
            List.of(
                    "ReleaseIndex",
                    "Version",
                    "CommitId",
                    "Class",
                    "BUGGY"
            );

    private static final List<String> FEATURE_COLUMNS =
            List.of(
                    "LOC",
                    "LOC_TOUCHED",
                    "NR",
                    "NAUTH",
                    "LOC_ADDED",
                    "MAX_LOC_ADDED",
                    "AVG_LOC_ADDED",
                    "CHURN",
                    "MAX_CHURN",
                    "AVG_CHURN",
                    "CHANGE_SET_SIZE",
                    "MAX_CHANGE_SET",
                    "AVG_CHANGE_SET",
                    "AGE_WEEKS",
                    "WEIGHTED_AGE_WEEKS",
                    "IGNORED_ZERO_LOC_REVS",
                    "NSmells"
            );

    private static final Map<Integer, Integer> EXPECTED_ROWS_BY_RELEASE =
            Map.ofEntries(
                    Map.entry(1, 932),
                    Map.entry(2, 949),
                    Map.entry(3, 948),
                    Map.entry(4, 996),
                    Map.entry(5, 1029),
                    Map.entry(6, 1058),
                    Map.entry(7, 1045),
                    Map.entry(8, 1050),
                    Map.entry(9, 1051),
                    Map.entry(10, 1185),
                    Map.entry(11, 1300),
                    Map.entry(12, 1293)
            );

    private static final Map<Integer, Integer> EXPECTED_BUGGY_BY_RELEASE =
            Map.ofEntries(
                    Map.entry(1, 63),
                    Map.entry(2, 100),
                    Map.entry(3, 158),
                    Map.entry(4, 123),
                    Map.entry(5, 113),
                    Map.entry(6, 136),
                    Map.entry(7, 232),
                    Map.entry(8, 234),
                    Map.entry(9, 210),
                    Map.entry(10, 350),
                    Map.entry(11, 159),
                    Map.entry(12, 132)
            );

    private DatasetAGenerator() {
        // Utility class.
    }

    public static void main(String[] args) throws Exception {

        Path repository =
                args.length == 0
                        ? Path.of(".")
                        : Path.of(args[0]);

        repository =
                repository
                        .toAbsolutePath()
                        .normalize();

        CsvTable metrics =
                readCsv(
                        repository.resolve(
                                METRICS_INPUT
                        )
                );

        CsvTable nfix =
                readCsv(
                        repository.resolve(
                                NFIX_INPUT
                        )
                );

        CsvTable bugginess =
                readCsv(
                        repository.resolve(
                                BUGGINESS_INPUT
                        )
                );

        validateHeaders(
                metrics,
                METRICS_HEADERS,
                "class_metrics_with_smells.csv"
        );

        validateHeaders(
                nfix,
                NFIX_HEADERS,
                "nfix_metrics.csv"
        );

        validateHeaders(
                bugginess,
                BUGGINESS_HEADERS,
                "bugginess_labels.csv"
        );

        validateInputRowCount(
                metrics,
                "class_metrics_with_smells.csv"
        );

        validateInputRowCount(
                nfix,
                "nfix_metrics.csv"
        );

        validateInputRowCount(
                bugginess,
                "bugginess_labels.csv"
        );

        Map<Key, List<String>> nfixByKey =
                indexByKey(
                        nfix,
                        "nfix_metrics.csv"
                );

        Map<Key, List<String>> buggyByKey =
                indexByKey(
                        bugginess,
                        "bugginess_labels.csv"
                );

        DatasetResult result =
                join(
                        metrics,
                        nfix,
                        nfixByKey,
                        bugginess,
                        buggyByKey
                );

        validateFinalResult(
                result,
                nfixByKey,
                buggyByKey
        );

        Path output =
                repository.resolve(
                        OUTPUT
                );

        writeDataset(
                output,
                result.rows()
        );

        Path validationOutput =
                repository.resolve(
                        VALIDATION_OUTPUT
                );

        writeValidationReport(
                validationOutput,
                result,
                output
        );

        printSummary(
                result,
                output,
                validationOutput
        );
    }

    private static DatasetResult join(
            CsvTable metrics,
            CsvTable nfix,
            Map<Key, List<String>> nfixByKey,
            CsvTable bugginess,
            Map<Key, List<String>> buggyByKey
    ) {

        List<List<String>> finalRows =
                new ArrayList<>();

        Set<Key> metricKeys =
                new LinkedHashSet<>();

        Map<Integer, Integer> rowsByRelease =
                new LinkedHashMap<>();

        Map<Integer, Integer> buggyByRelease =
                new LinkedHashMap<>();

        Set<Integer> releases =
                new LinkedHashSet<>();

        int buggyYes = 0;
        int buggyNo = 0;

        long sumNSmells = 0;
        long sumNfix = 0;

        for (List<String> metricRow
                : metrics.rows()) {

            Key key =
                    keyOf(
                            metrics,
                            metricRow
                    );

            if (!metricKeys.add(key)) {

                throw new IllegalStateException(
                        "Duplicate metrics key: "
                                + key
                );
            }

            List<String> nfixRow =
                    nfixByKey.get(key);

            if (nfixRow == null) {

                throw new IllegalStateException(
                        "NFIX match missing for "
                                + key
                );
            }

            List<String> buggyRow =
                    buggyByKey.get(key);

            if (buggyRow == null) {

                throw new IllegalStateException(
                        "Bugginess match missing for "
                                + key
                );
            }

            validateMetadataMatch(
                    metrics,
                    metricRow,
                    nfix,
                    nfixRow,
                    "NFIX",
                    key
            );

            validateMetadataMatch(
                    metrics,
                    metricRow,
                    bugginess,
                    buggyRow,
                    "Bugginess",
                    key
            );

            validateMetricValues(
                    metrics,
                    metricRow,
                    key
            );

            int nfixValue =
                    parseNonNegativeInteger(
                            value(
                                    nfix,
                                    nfixRow,
                                    "NFIX"
                            ),
                            "NFIX",
                            key
                    );

            int nSmellsValue =
                    parseNonNegativeInteger(
                            value(
                                    metrics,
                                    metricRow,
                                    "NSmells"
                            ),
                            "NSmells",
                            key
                    );

            String buggy =
                    value(
                            bugginess,
                            buggyRow,
                            "BUGGY"
                    );

            if (!buggy.equals("YES")
                    && !buggy.equals("NO")) {

                throw new IllegalStateException(
                        "Invalid BUGGY value for "
                                + key
                                + ": "
                                + buggy
                );
            }

            if (buggy.equals("YES")) {

                buggyYes++;

                buggyByRelease.merge(
                        key.releaseIndex(),
                        1,
                        Integer::sum
                );

            } else {

                buggyNo++;
            }

            releases.add(
                    key.releaseIndex()
            );

            rowsByRelease.merge(
                    key.releaseIndex(),
                    1,
                    Integer::sum
            );

            sumNSmells +=
                    nSmellsValue;

            sumNfix +=
                    nfixValue;

            List<String> finalRow =
                    new ArrayList<>();

            finalRow.add(
                    PROJECT
            );

            finalRow.add(
                    key.classPath()
            );

            finalRow.add(
                    Integer.toString(
                            key.releaseIndex()
                    )
            );

            for (String feature
                    : FEATURE_COLUMNS) {

                finalRow.add(
                        value(
                                metrics,
                                metricRow,
                                feature
                        )
                );
            }

            finalRow.add(
                    Integer.toString(
                            nfixValue
                    )
            );

            finalRow.add(
                    buggy
            );

            finalRows.add(
                    List.copyOf(
                            finalRow
                    )
            );
        }

        return new DatasetResult(
                List.copyOf(finalRows),
                Set.copyOf(metricKeys),
                Set.copyOf(releases),
                Map.copyOf(rowsByRelease),
                Map.copyOf(buggyByRelease),
                buggyYes,
                buggyNo,
                sumNSmells,
                sumNfix
        );
    }

    private static void validateFinalResult(
            DatasetResult result,
            Map<Key, List<String>> nfixByKey,
            Map<Key, List<String>> buggyByKey
    ) {

        if (result.rows().size()
                != EXPECTED_ROWS) {

            throw new IllegalStateException(
                    "Final dataset row mismatch. Expected "
                            + EXPECTED_ROWS
                            + ", found "
                            + result.rows().size()
            );
        }

        if (result.keys().size()
                != EXPECTED_ROWS) {

            throw new IllegalStateException(
                    "Final dataset unique-key mismatch. Expected "
                            + EXPECTED_ROWS
                            + ", found "
                            + result.keys().size()
            );
        }

        if (nfixByKey.size()
                != EXPECTED_ROWS) {

            throw new IllegalStateException(
                    "NFIX unique-key mismatch. Expected "
                            + EXPECTED_ROWS
                            + ", found "
                            + nfixByKey.size()
            );
        }

        if (buggyByKey.size()
                != EXPECTED_ROWS) {

            throw new IllegalStateException(
                    "Bugginess unique-key mismatch. Expected "
                            + EXPECTED_ROWS
                            + ", found "
                            + buggyByKey.size()
            );
        }

        if (!result.keys()
                .equals(
                        nfixByKey.keySet()
                )) {

            throw new IllegalStateException(
                    "Metrics and NFIX key sets differ."
            );
        }

        if (!result.keys()
                .equals(
                        buggyByKey.keySet()
                )) {

            throw new IllegalStateException(
                    "Metrics and Bugginess key sets differ."
            );
        }

        if (result.releases().size()
                != EXPECTED_RELEASES) {

            throw new IllegalStateException(
                    "Release-count mismatch. Expected "
                            + EXPECTED_RELEASES
                            + ", found "
                            + result.releases().size()
            );
        }

        if (result.buggyYes()
                != EXPECTED_BUGGY_YES) {

            throw new IllegalStateException(
                    "BUGGY=YES mismatch. Expected "
                            + EXPECTED_BUGGY_YES
                            + ", found "
                            + result.buggyYes()
            );
        }

        if (result.buggyNo()
                != EXPECTED_BUGGY_NO) {

            throw new IllegalStateException(
                    "BUGGY=NO mismatch. Expected "
                            + EXPECTED_BUGGY_NO
                            + ", found "
                            + result.buggyNo()
            );
        }

        if (result.sumNSmells()
                != EXPECTED_NSMELLS_SUM) {

            throw new IllegalStateException(
                    "Sum(NSmells) mismatch. Expected "
                            + EXPECTED_NSMELLS_SUM
                            + ", found "
                            + result.sumNSmells()
            );
        }

        if (result.sumNfix()
                != EXPECTED_NFIX_SUM) {

            throw new IllegalStateException(
                    "Sum(NFIX) mismatch. Expected "
                            + EXPECTED_NFIX_SUM
                            + ", found "
                            + result.sumNfix()
            );
        }

        for (int release = 1;
             release <= EXPECTED_RELEASES;
             release++) {

            int actualRows =
                    result.rowsByRelease()
                            .getOrDefault(
                                    release,
                                    0
                            );

            int expectedRows =
                    EXPECTED_ROWS_BY_RELEASE
                            .get(release);

            if (actualRows != expectedRows) {

                throw new IllegalStateException(
                        "Release "
                                + release
                                + " row mismatch. Expected "
                                + expectedRows
                                + ", found "
                                + actualRows
                );
            }

            int actualBuggy =
                    result.buggyByRelease()
                            .getOrDefault(
                                    release,
                                    0
                            );

            int expectedBuggy =
                    EXPECTED_BUGGY_BY_RELEASE
                            .get(release);

            if (actualBuggy != expectedBuggy) {

                throw new IllegalStateException(
                        "Release "
                                + release
                                + " BUGGY mismatch. Expected "
                                + expectedBuggy
                                + ", found "
                                + actualBuggy
                );
            }
        }
    }

    private static void validateMetricValues(
            CsvTable table,
            List<String> row,
            Key key
    ) {

        for (String feature
                : FEATURE_COLUMNS) {

            String text =
                    value(
                            table,
                            row,
                            feature
                    );

            if (text.isBlank()) {

                throw new IllegalStateException(
                        "Missing "
                                + feature
                                + " for "
                                + key
                );
            }

            double numericValue;

            try {

                numericValue =
                        Double.parseDouble(
                                text
                        );

            } catch (NumberFormatException exception) {

                throw new IllegalStateException(
                        "Non-numeric "
                                + feature
                                + " for "
                                + key
                                + ": "
                                + text,
                        exception
                );
            }

            if (!Double.isFinite(
                    numericValue
            )) {

                throw new IllegalStateException(
                        "Non-finite "
                                + feature
                                + " for "
                                + key
                                + ": "
                                + text
                );
            }
        }
    }

    private static int parseNonNegativeInteger(
            String text,
            String column,
            Key key
    ) {

        final int result;

        try {

            result =
                    Integer.parseInt(
                            text
                    );

        } catch (NumberFormatException exception) {

            throw new IllegalStateException(
                    "Invalid integer "
                            + column
                            + " for "
                            + key
                            + ": "
                            + text,
                    exception
            );
        }

        if (result < 0) {

            throw new IllegalStateException(
                    "Negative "
                            + column
                            + " for "
                            + key
                            + ": "
                            + result
            );
        }

        return result;
    }

    private static Map<Key, List<String>> indexByKey(
            CsvTable table,
            String source
    ) {

        Map<Key, List<String>> result =
                new HashMap<>();

        for (List<String> row
                : table.rows()) {

            Key key =
                    keyOf(
                            table,
                            row
                    );

            List<String> previous =
                    result.put(
                            key,
                            row
                    );

            if (previous != null) {

                throw new IllegalStateException(
                        "Duplicate key in "
                                + source
                                + ": "
                                + key
                );
            }
        }

        return Map.copyOf(
                result
        );
    }

    private static Key keyOf(
            CsvTable table,
            List<String> row
    ) {

        int releaseIndex;

        try {

            releaseIndex =
                    Integer.parseInt(
                            value(
                                    table,
                                    row,
                                    "ReleaseIndex"
                            )
                    );

        } catch (NumberFormatException exception) {

            throw new IllegalStateException(
                    "Invalid ReleaseIndex.",
                    exception
            );
        }

        String classPath =
                value(
                        table,
                        row,
                        "Class"
                );

        if (classPath.isBlank()) {

            throw new IllegalStateException(
                    "Blank Class at release "
                            + releaseIndex
            );
        }

        return new Key(
                releaseIndex,
                classPath
        );
    }

    private static void validateMetadataMatch(
            CsvTable referenceTable,
            List<String> referenceRow,
            CsvTable candidateTable,
            List<String> candidateRow,
            String source,
            Key key
    ) {

        for (String column
                : List.of(
                "ReleaseIndex",
                "Version",
                "CommitId",
                "Class"
        )) {

            String reference =
                    value(
                            referenceTable,
                            referenceRow,
                            column
                    );

            String candidate =
                    value(
                            candidateTable,
                            candidateRow,
                            column
                    );

            if (!reference.equals(
                    candidate
            )) {

                throw new IllegalStateException(
                        source
                                + " metadata mismatch for "
                                + key
                                + " / "
                                + column
                                + ": metrics='"
                                + reference
                                + "', candidate='"
                                + candidate
                                + "'"
                );
            }
        }
    }

    private static void validateInputRowCount(
            CsvTable table,
            String source
    ) {

        if (table.rows().size()
                != EXPECTED_ROWS) {

            throw new IllegalStateException(
                    source
                            + " row mismatch. Expected "
                            + EXPECTED_ROWS
                            + ", found "
                            + table.rows().size()
            );
        }
    }

    private static void validateHeaders(
            CsvTable table,
            List<String> expected,
            String source
    ) {

        if (!table.headers()
                .equals(
                        expected
                )) {

            throw new IllegalStateException(
                    source
                            + " header mismatch."
                            + System.lineSeparator()
                            + "Expected: "
                            + expected
                            + System.lineSeparator()
                            + "Actual  : "
                            + table.headers()
            );
        }
    }

    private static CsvTable readCsv(
            Path input
    ) throws IOException {

        if (!Files.isRegularFile(
                input
        )) {

            throw new IllegalArgumentException(
                    "CSV not found: "
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
                        "Empty CSV: "
                                + input
                );
            }

            List<String> headers =
                    parseCsvLine(
                            removeBom(
                                    headerLine
                            )
                    );

            Set<String> uniqueHeaders =
                    new HashSet<>(
                            headers
                    );

            if (uniqueHeaders.size()
                    != headers.size()) {

                throw new IllegalStateException(
                        "Duplicate CSV headers in "
                                + input
                );
            }

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

            List<List<String>> rows =
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
                            "CSV column-count mismatch at "
                                    + input
                                    + ":"
                                    + lineNumber
                                    + ". Expected "
                                    + headers.size()
                                    + ", found "
                                    + row.size()
                    );
                }

                rows.add(
                        List.copyOf(
                                row
                        )
                );
            }

            return new CsvTable(
                    List.copyOf(
                            headers
                    ),
                    Map.copyOf(
                            columns
                    ),
                    List.copyOf(
                            rows
                    )
            );
        }
    }

    private static String value(
            CsvTable table,
            List<String> row,
            String column
    ) {

        Integer index =
                table.columns()
                        .get(
                                column
                        );

        if (index == null) {

            throw new IllegalArgumentException(
                    "Missing CSV column: "
                            + column
            );
        }

        return row.get(index)
                .trim();
    }

    private static List<String> parseCsvLine(
            String line
    ) {

        List<String> result =
                new ArrayList<>();

        StringBuilder current =
                new StringBuilder();

        boolean quoted =
                false;

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

    private static void writeDataset(
            Path output,
            List<List<String>> rows
    ) throws IOException {

        Files.createDirectories(
                output.getParent()
        );

        List<String> outputLines =
                new ArrayList<>();

        List<String> header =
                new ArrayList<>();

        header.add(
                "Project"
        );

        header.add(
                "Class"
        );

        header.add(
                "ReleaseIndex"
        );

        header.addAll(
                FEATURE_COLUMNS
        );

        header.add(
                "NFIX"
        );

        header.add(
                "BUGGY"
        );

        outputLines.add(
                toCsvLine(
                        header
                )
        );

        for (List<String> row
                : rows) {

            outputLines.add(
                    toCsvLine(
                            row
                    )
            );
        }

        Files.write(
                output,
                outputLines,
                StandardCharsets.UTF_8
        );
    }

    private static String toCsvLine(
            List<String> values
    ) {

        return values.stream()
                .map(
                        DatasetAGenerator::escapeCsv
                )
                .reduce(
                        (left, right) ->
                                left + "," + right
                )
                .orElse("");
    }

    private static String escapeCsv(
            String value
    ) {

        boolean requiresQuotes =
                value.contains(",")
                        || value.contains("\"")
                        || value.contains("\n")
                        || value.contains("\r");

        if (!requiresQuotes) {
            return value;
        }

        return "\""
                + value.replace(
                "\"",
                "\"\""
        )
                + "\"";
    }

    private static void writeValidationReport(
            Path output,
            DatasetResult result,
            Path datasetPath
    ) throws IOException {

        Files.createDirectories(
                output.getParent()
        );

        List<String> lines =
                new ArrayList<>();

        lines.add(
                "===== OPENJPA DATASET A VALIDATION ====="
        );

        lines.add(
                "Project              : "
                        + PROJECT
        );

        lines.add(
                "Dataset              : "
                        + datasetPath
        );

        lines.add(
                "Rows                 : "
                        + result.rows().size()
        );

        lines.add(
                "Unique observations  : "
                        + result.keys().size()
        );

        lines.add(
                "Releases             : "
                        + result.releases().size()
        );

        lines.add(
                "Feature columns      : "
                        + (FEATURE_COLUMNS.size() + 1)
        );

        lines.add(
                "Sum(NSmells)         : "
                        + result.sumNSmells()
        );

        lines.add(
                "Sum(NFIX)            : "
                        + result.sumNfix()
        );

        lines.add(
                "BUGGY=YES            : "
                        + result.buggyYes()
        );

        lines.add(
                "BUGGY=NO             : "
                        + result.buggyNo()
        );

        lines.add(
                ""
        );

        lines.add(
                "===== BY RELEASE ====="
        );

        for (int release = 1;
             release <= EXPECTED_RELEASES;
             release++) {

            lines.add(
                    String.format(
                            "Release %2d | rows=%4d | BUGGY=YES=%3d | BUGGY=NO=%4d",
                            release,
                            result.rowsByRelease()
                                    .getOrDefault(
                                            release,
                                            0
                                    ),
                            result.buggyByRelease()
                                    .getOrDefault(
                                            release,
                                            0
                                    ),
                            result.rowsByRelease()
                                    .getOrDefault(
                                            release,
                                            0
                                    )
                                    - result.buggyByRelease()
                                    .getOrDefault(
                                            release,
                                            0
                                    )
                    )
            );
        }

        lines.add(
                ""
        );

        lines.add(
                "ValidationPassed=True"
        );

        lines.add(
                "======================================"
        );

        Files.write(
                output,
                lines,
                StandardCharsets.UTF_8
        );
    }

    private static void printSummary(
            DatasetResult result,
            Path output,
            Path validationOutput
    ) {

        System.out.println(
                "===== OPENJPA DATASET A ====="
        );

        System.out.println(
                "Project              : "
                        + PROJECT
        );

        System.out.println(
                "Rows                 : "
                        + result.rows().size()
        );

        System.out.println(
                "Unique observations  : "
                        + result.keys().size()
        );

        System.out.println(
                "Releases             : "
                        + result.releases().size()
        );

        System.out.println(
                "Feature columns      : "
                        + (FEATURE_COLUMNS.size() + 1)
        );

        System.out.println(
                "Sum(NSmells)         : "
                        + result.sumNSmells()
        );

        System.out.println(
                "Sum(NFIX)            : "
                        + result.sumNfix()
        );

        System.out.println(
                "BUGGY=YES            : "
                        + result.buggyYes()
        );

        System.out.println(
                "BUGGY=NO             : "
                        + result.buggyNo()
        );

        System.out.println(
                ""
        );

        for (int release = 1;
             release <= EXPECTED_RELEASES;
             release++) {

            int rows =
                    result.rowsByRelease()
                            .getOrDefault(
                                    release,
                                    0
                            );

            int yes =
                    result.buggyByRelease()
                            .getOrDefault(
                                    release,
                                    0
                            );

            System.out.printf(
                    "R%02d | rows=%4d | YES=%3d | NO=%4d%n",
                    release,
                    rows,
                    yes,
                    rows - yes
            );
        }

        System.out.println(
                ""
        );

        System.out.println(
                "Dataset output       : "
                        + output
        );

        System.out.println(
                "Validation report    : "
                        + validationOutput
        );

        System.out.println(
                "ValidationPassed     : True"
        );

        System.out.println(
                "============================="
        );
    }

    private record Key(
            int releaseIndex,
            String classPath
    ) {
    }

    private record CsvTable(
            List<String> headers,
            Map<String, Integer> columns,
            List<List<String>> rows
    ) {
    }

    private record DatasetResult(
            List<List<String>> rows,
            Set<Key> keys,
            Set<Integer> releases,
            Map<Integer, Integer> rowsByRelease,
            Map<Integer, Integer> buggyByRelease,
            int buggyYes,
            int buggyNo,
            long sumNSmells,
            long sumNfix
    ) {
    }
}
