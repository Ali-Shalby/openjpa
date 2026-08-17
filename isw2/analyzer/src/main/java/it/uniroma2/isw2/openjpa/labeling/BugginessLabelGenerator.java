/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package it.uniroma2.isw2.openjpa.labeling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class BugginessLabelGenerator {

    /*
     * Final validated invariants.
     */
    private static final int EXPECTED_INVENTORY_ROWS =
            12836;

    private static final int EXPECTED_EXACT_BUGGY_PAIRS =
            2007;

    private static final int EXPECTED_DIRECT_RENAME_PAIRS =
            4;

    private static final int EXPECTED_NEW_RENAME_PAIRS =
            3;

    private static final int EXPECTED_FINAL_BUGGY_PAIRS =
            2010;

    private static final int EXPECTED_FINAL_CLEAN_PAIRS =
            EXPECTED_INVENTORY_ROWS
                    - EXPECTED_FINAL_BUGGY_PAIRS;

    /*
     * Inputs.
     */
    private static final Path CLASS_INVENTORY =
            Path.of(
                    "isw2",
                    "datasets",
                    "java_class_inventory.csv"
            );

    private static final Path PATH_PRESENCE_DIAGNOSTIC =
            Path.of(
                    "isw2",
                    "results",
                    "labeling",
                    "bugginess_path_presence_diagnostic.csv"
            );

    private static final Path DIRECT_RENAME_VALID =
            Path.of(
                    "isw2",
                    "results",
                    "labeling",
                    "bugginess_direct_rename_valid.csv"
            );

    /*
     * Final output.
     */
    private static final Path OUTPUT =
            Path.of(
                    "isw2",
                    "datasets",
                    "bugginess_labels.csv"
            );

    private BugginessLabelGenerator() {
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

        System.out.println();
        System.out.println(
                "===== BUGGINESS LABEL GENERATOR ====="
        );

        System.out.println(
                "Repository : "
                        + repository
        );

        /*
         * ----------------------------------------------------
         * 1. Production inventory
         * ----------------------------------------------------
         */

        List<InventoryRow> inventory =
                readInventory(
                        repository.resolve(
                                CLASS_INVENTORY
                        )
                );

        validateInventory(
                inventory
        );

        System.out.println(
                "Production observations : "
                        + inventory.size()
        );

        /*
         * ----------------------------------------------------
         * 2. Exact SZZ path mappings
         * ----------------------------------------------------
         */

        Set<String> exactBuggyKeys =
                readExactBuggyKeys(
                        repository.resolve(
                                PATH_PRESENCE_DIAGNOSTIC
                        )
                );

        if (exactBuggyKeys.size()
                != EXPECTED_EXACT_BUGGY_PAIRS) {

            throw new IllegalStateException(
                    "Unexpected exact BUGGY count. "
                            + "Expected="
                            + EXPECTED_EXACT_BUGGY_PAIRS
                            + ", found="
                            + exactBuggyKeys.size()
            );
        }

        System.out.println(
                "Exact BUGGY pairs       : "
                        + exactBuggyKeys.size()
        );

        /*
         * ----------------------------------------------------
         * 3. Valid direct Git rename mappings
         * ----------------------------------------------------
         */

        Set<String> renameBuggyKeys =
                readDirectRenameBuggyKeys(
                        repository.resolve(
                                DIRECT_RENAME_VALID
                        )
                );

        if (renameBuggyKeys.size()
                != EXPECTED_DIRECT_RENAME_PAIRS) {

            throw new IllegalStateException(
                    "Unexpected direct-rename pair count. "
                            + "Expected="
                            + EXPECTED_DIRECT_RENAME_PAIRS
                            + ", found="
                            + renameBuggyKeys.size()
            );
        }

        System.out.println(
                "Direct rename pairs     : "
                        + renameBuggyKeys.size()
        );

        /*
         * ----------------------------------------------------
         * 4. Verify rename contribution
         * ----------------------------------------------------
         */

        Set<String> newRenameKeys =
                renameBuggyKeys.stream()
                        .filter(key ->
                                !exactBuggyKeys.contains(
                                        key
                                )
                        )
                        .collect(
                                Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        );

        if (newRenameKeys.size()
                != EXPECTED_NEW_RENAME_PAIRS) {

            throw new IllegalStateException(
                    "Unexpected new rename contribution. "
                            + "Expected="
                            + EXPECTED_NEW_RENAME_PAIRS
                            + ", found="
                            + newRenameKeys.size()
            );
        }

        System.out.println(
                "New rename pairs        : "
                        + newRenameKeys.size()
        );

        /*
         * ----------------------------------------------------
         * 5. Final BUGGY set
         * ----------------------------------------------------
         */

        Set<String> finalBuggyKeys =
                new LinkedHashSet<>(
                        exactBuggyKeys
                );

        finalBuggyKeys.addAll(
                renameBuggyKeys
        );

        if (finalBuggyKeys.size()
                != EXPECTED_FINAL_BUGGY_PAIRS) {

            throw new IllegalStateException(
                    "Unexpected final BUGGY count. "
                            + "Expected="
                            + EXPECTED_FINAL_BUGGY_PAIRS
                            + ", found="
                            + finalBuggyKeys.size()
            );
        }

        /*
         * Every BUGGY key must correspond to a real
         * production observation.
         */
        Set<String> inventoryKeys =
                inventory.stream()
                        .map(
                                BugginessLabelGenerator
                                        ::observationKey
                        )
                        .collect(
                                Collectors.toSet()
                        );

        Set<String> missingBuggyKeys =
                finalBuggyKeys.stream()
                        .filter(key ->
                                !inventoryKeys.contains(
                                        key
                                )
                        )
                        .collect(
                                Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        );

        if (!missingBuggyKeys.isEmpty()) {

            throw new IllegalStateException(
                    "BUGGY keys absent from production inventory: "
                            + missingBuggyKeys
            );
        }

        /*
         * ----------------------------------------------------
         * 6. Generate final labels
         * ----------------------------------------------------
         */

        List<LabeledRow> labeledRows =
                inventory.stream()
                        .map(row ->
                                new LabeledRow(
                                        row.releaseIndex(),
                                        row.version(),
                                        row.commitId(),
                                        row.classPath(),
                                        finalBuggyKeys.contains(
                                                observationKey(
                                                        row
                                                )
                                        )
                                                ? "YES"
                                                : "NO"
                                )
                        )
                        .toList();

        validateFinalLabels(
                labeledRows
        );

        /*
         * ----------------------------------------------------
         * 7. Write final CSV
         * ----------------------------------------------------
         */

        Path output =
                repository.resolve(
                        OUTPUT
                );

        writeLabels(
                labeledRows,
                output
        );

        /*
         * ----------------------------------------------------
         * 8. Final summary
         * ----------------------------------------------------
         */

        printSummary(
                labeledRows,
                exactBuggyKeys,
                renameBuggyKeys,
                newRenameKeys,
                output
        );
    }

    private static List<InventoryRow> readInventory(
            Path input
    ) throws IOException {

        CsvTable table =
                readCsv(
                        input
                );

        List<InventoryRow> result =
                new ArrayList<>();

        for (List<String> row : table.rows()) {

            int releaseIndex =
                    Integer.parseInt(
                            required(
                                    table,
                                    row,
                                    "ReleaseIndex"
                            )
                    );

            String version =
                    required(
                            table,
                            row,
                            "Version"
                    );

            String commitId =
                    normalizeCommit(
                            required(
                                    table,
                                    row,
                                    "CommitId"
                            )
                    );

            String classPath =
                    normalizePath(
                            required(
                                    table,
                                    row,
                                    "Class"
                            )
                    );

            result.add(
                    new InventoryRow(
                            releaseIndex,
                            version,
                            commitId,
                            classPath
                    )
            );
        }

        return List.copyOf(
                result
        );
    }

    private static Set<String> readExactBuggyKeys(
            Path input
    ) throws IOException {

        CsvTable table =
                readCsv(
                        input
                );

        Set<String> result =
                new LinkedHashSet<>();

        for (List<String> row : table.rows()) {

            boolean pathExists =
                    Boolean.parseBoolean(
                            required(
                                    table,
                                    row,
                                    "PathExists"
                            )
                    );

            if (!pathExists) {

                continue;
            }

            int datasetReleaseIndex =
                    Integer.parseInt(
                            required(
                                    table,
                                    row,
                                    "DatasetReleaseIndex"
                            )
                    );

            String blamedFilePath =
                    normalizePath(
                            required(
                                    table,
                                    row,
                                    "BlamedFilePath"
                            )
                    );

            result.add(
                    observationKey(
                            datasetReleaseIndex,
                            blamedFilePath
                    )
            );
        }

        return Set.copyOf(
                result
        );
    }

    private static Set<String> readDirectRenameBuggyKeys(
            Path input
    ) throws IOException {

        CsvTable table =
                readCsv(
                        input
                );

        Set<String> result =
                new LinkedHashSet<>();

        for (List<String> row : table.rows()) {

            int datasetReleaseIndex =
                    Integer.parseInt(
                            required(
                                    table,
                                    row,
                                    "DatasetReleaseIndex"
                            )
                    );

            String recoveredClass =
                    normalizePath(
                            required(
                                    table,
                                    row,
                                    "RecoveredClass"
                            )
                    );

            boolean productionPresent =
                    Boolean.parseBoolean(
                            required(
                                    table,
                                    row,
                                    "ProductionPresent"
                            )
                    );

            if (!productionPresent) {

                throw new IllegalStateException(
                        "Direct rename mapping outside "
                                + "production inventory: "
                                + datasetReleaseIndex
                                + " / "
                                + recoveredClass
                );
            }

            result.add(
                    observationKey(
                            datasetReleaseIndex,
                            recoveredClass
                    )
            );
        }

        return Set.copyOf(
                result
        );
    }

    private static void validateInventory(
            List<InventoryRow> inventory
    ) {

        if (inventory.size()
                != EXPECTED_INVENTORY_ROWS) {

            throw new IllegalStateException(
                    "Unexpected production inventory size. "
                            + "Expected="
                            + EXPECTED_INVENTORY_ROWS
                            + ", found="
                            + inventory.size()
            );
        }

        Set<String> uniqueKeys =
                new HashSet<>();

        for (InventoryRow row : inventory) {

            String key =
                    observationKey(
                            row
                    );

            if (!uniqueKeys.add(
                    key
            )) {

                throw new IllegalStateException(
                        "Duplicate production observation: "
                                + key
                );
            }

            if (row.releaseIndex() < 1
                    || row.releaseIndex() > 12) {

                throw new IllegalStateException(
                        "Invalid Dataset A ReleaseIndex: "
                                + row.releaseIndex()
                );
            }

            if (row.version().isBlank()
                    || row.commitId().isBlank()
                    || row.classPath().isBlank()) {

                throw new IllegalStateException(
                        "Incomplete production observation: "
                                + row
                );
            }

            if (!row.classPath()
                    .endsWith(
                            ".java"
                    )) {

                throw new IllegalStateException(
                        "Non-Java production class: "
                                + row.classPath()
                );
            }
        }

        if (uniqueKeys.size()
                != EXPECTED_INVENTORY_ROWS) {

            throw new IllegalStateException(
                    "Production unique-key mismatch."
            );
        }
    }

    private static void validateFinalLabels(
            List<LabeledRow> rows
    ) {

        if (rows.size()
                != EXPECTED_INVENTORY_ROWS) {

            throw new IllegalStateException(
                    "Final labeling row count mismatch."
            );
        }

        Set<String> keys =
                new HashSet<>();

        long buggyYes =
                0;

        long buggyNo =
                0;

        for (LabeledRow row : rows) {

            String key =
                    observationKey(
                            row.releaseIndex(),
                            row.classPath()
                    );

            if (!keys.add(
                    key
            )) {

                throw new IllegalStateException(
                        "Duplicate final label row: "
                                + key
                );
            }

            if ("YES".equals(
                    row.buggy()
            )) {

                buggyYes++;

            } else if ("NO".equals(
                    row.buggy()
            )) {

                buggyNo++;

            } else {

                throw new IllegalStateException(
                        "Unexpected BUGGY value: "
                                + row.buggy()
                );
            }
        }

        if (keys.size()
                != EXPECTED_INVENTORY_ROWS) {

            throw new IllegalStateException(
                    "Final label unique-key mismatch."
            );
        }

        if (buggyYes
                != EXPECTED_FINAL_BUGGY_PAIRS) {

            throw new IllegalStateException(
                    "Unexpected BUGGY=YES count. "
                            + "Expected="
                            + EXPECTED_FINAL_BUGGY_PAIRS
                            + ", found="
                            + buggyYes
            );
        }

        if (buggyNo
                != EXPECTED_FINAL_CLEAN_PAIRS) {

            throw new IllegalStateException(
                    "Unexpected BUGGY=NO count. "
                            + "Expected="
                            + EXPECTED_FINAL_CLEAN_PAIRS
                            + ", found="
                            + buggyNo
            );
        }

        if (buggyYes
                + buggyNo
                != EXPECTED_INVENTORY_ROWS) {

            throw new IllegalStateException(
                    "YES + NO does not equal total rows."
            );
        }
    }

    private static void writeLabels(
            List<LabeledRow> rows,
            Path output
    ) throws IOException {

        Files.createDirectories(
                output.getParent()
        );

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             output,
                             StandardCharsets.UTF_8
                     )) {

            writer.write(
                    "ReleaseIndex,"
                            + "Version,"
                            + "CommitId,"
                            + "Class,"
                            + "BUGGY"
            );

            writer.newLine();

            for (LabeledRow row : rows) {

                writer.write(
                        String.join(
                                ",",
                                Integer.toString(
                                        row.releaseIndex()
                                ),
                                csv(
                                        row.version()
                                ),
                                csv(
                                        row.commitId()
                                ),
                                csv(
                                        row.classPath()
                                ),
                                row.buggy()
                        )
                );

                writer.newLine();
            }
        }
    }

    private static void printSummary(
            List<LabeledRow> rows,
            Set<String> exactBuggyKeys,
            Set<String> renameBuggyKeys,
            Set<String> newRenameKeys,
            Path output
    ) {

        long buggyYes =
                rows.stream()
                        .filter(row ->
                                "YES".equals(
                                        row.buggy()
                                )
                        )
                        .count();

        long buggyNo =
                rows.size()
                        - buggyYes;

        Map<Integer, Long> yesByRelease =
                rows.stream()
                        .filter(row ->
                                "YES".equals(
                                        row.buggy()
                                )
                        )
                        .collect(
                                Collectors.groupingBy(
                                        LabeledRow::releaseIndex,
                                        LinkedHashMap::new,
                                        Collectors.counting()
                                )
                        );

        Map<Integer, Long> totalByRelease =
                rows.stream()
                        .collect(
                                Collectors.groupingBy(
                                        LabeledRow::releaseIndex,
                                        LinkedHashMap::new,
                                        Collectors.counting()
                                )
                        );

        System.out.println();
        System.out.println(
                "===== FINAL BUGGINESS LABELING ====="
        );

        System.out.println(
                "Rows                    : "
                        + rows.size()
        );

        System.out.println(
                "Unique observations     : "
                        + rows.size()
        );

        System.out.println();

        System.out.println(
                "Exact BUGGY pairs       : "
                        + exactBuggyKeys.size()
        );

        System.out.println(
                "Direct rename pairs     : "
                        + renameBuggyKeys.size()
        );

        System.out.println(
                "New rename pairs        : "
                        + newRenameKeys.size()
        );

        System.out.println();

        System.out.println(
                "BUGGY = YES             : "
                        + buggyYes
        );

        System.out.println(
                "BUGGY = NO              : "
                        + buggyNo
        );

        System.out.println();

        System.out.println(
                "===== BUGGY BY DATASET RELEASE ====="
        );

        List<Integer> releases =
                totalByRelease
                        .keySet()
                        .stream()
                        .sorted()
                        .toList();

        for (Integer release : releases) {

            long total =
                    totalByRelease.getOrDefault(
                            release,
                            0L
                    );

            long yes =
                    yesByRelease.getOrDefault(
                            release,
                            0L
                    );

            long no =
                    total - yes;

            double percentage =
                    total == 0
                            ? 0.0
                            : yes
                            * 100.0
                            / total;

            System.out.printf(
                    Locale.ROOT,
                    "Release %2d | rows=%4d | YES=%4d | "
                            + "NO=%4d | buggy=%.2f%%%n",
                    release,
                    total,
                    yes,
                    no,
                    percentage
            );
        }

        System.out.println();

        System.out.println(
                "Output                  : "
                        + output
        );

        System.out.println(
                "===================================="
        );
    }

    private static String observationKey(
            InventoryRow row
    ) {

        return observationKey(
                row.releaseIndex(),
                row.classPath()
        );
    }

    private static String observationKey(
            int releaseIndex,
            String classPath
    ) {

        return releaseIndex
                + "\u0000"
                + normalizePath(
                classPath
        );
    }

    private static String normalizeCommit(
            String value
    ) {

        return value
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private static String normalizePath(
            String value
    ) {

        return value
                .trim()
                .replace(
                        '\\',
                        '/'
                );
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

            Map<String, Integer> columns =
                    new HashMap<>();

            for (int index = 0;
                 index < headers.size();
                 index++) {

                columns.put(
                        headers.get(index)
                                .trim(),
                        index
                );
            }

            List<List<String>> rows =
                    new ArrayList<>();

            String line;

            while ((line = reader.readLine())
                    != null) {

                if (!line.isBlank()) {

                    rows.add(
                            parseCsvLine(
                                    line
                            )
                    );
                }
            }

            return new CsvTable(
                    Map.copyOf(
                            columns
                    ),
                    List.copyOf(
                            rows
                    )
            );
        }
    }

    private static String required(
            CsvTable table,
            List<String> row,
            String column
    ) {

        String value =
                firstValue(
                        table,
                        row,
                        column
                );

        if (value.isBlank()) {

            throw new IllegalArgumentException(
                    "Missing CSV value for column: "
                            + column
            );
        }

        return value;
    }

    private static String firstValue(
            CsvTable table,
            List<String> row,
            String... columns
    ) {

        for (String column : columns) {

            Integer index =
                    table.columns()
                            .get(
                                    column
                            );

            if (index == null
                    || index >= row.size()) {

                continue;
            }

            String value =
                    row.get(index)
                            .trim();

            if (!value.isBlank()) {

                return value;
            }
        }

        return "";
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
                    line.charAt(
                            index
                    );

            if (character == '"') {

                if (quoted
                        && index + 1
                        < line.length()
                        && line.charAt(
                        index + 1
                ) == '"') {

                    current.append(
                            '"'
                    );

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

    private static String csv(
            String value
    ) {

        return "\""
                + value.replace(
                "\"",
                "\"\""
        )
                + "\"";
    }

    private record InventoryRow(
            int releaseIndex,
            String version,
            String commitId,
            String classPath
    ) {
    }

    private record LabeledRow(
            int releaseIndex,
            String version,
            String commitId,
            String classPath,
            String buggy
    ) {
    }

    private record CsvTable(
            Map<String, Integer> columns,
            List<List<String>> rows
    ) {
    }
}
