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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Builds the Milestone 3 what-if datasets from Dataset A.
 *
 * <p>Definitions:</p>
 * <ul>
 *   <li>B+ = observations from A with NSmells &gt; 0</li>
 *   <li>C  = observations from A with NSmells = 0</li>
 *   <li>B  = a copy of B+ where only NSmells is set to 0</li>
 * </ul>
 *
 * <p>The builder preserves the original row order and schema. B and B+ are
 * validated column-by-column and must differ exclusively in NSmells.</p>
 */
public final class M3DatasetBuilder {

    private static final String PROJECT = "OPENJPA";

    private static final Path DATASET_A =
            Path.of(
                    "isw2",
                    "datasets",
                    "openjpa_dataset_a.csv"
            );

    private static final Path OUTPUT_B_PLUS =
            Path.of(
                    "isw2",
                    "datasets",
                    "openjpa_m3_bplus.csv"
            );

    private static final Path OUTPUT_B =
            Path.of(
                    "isw2",
                    "datasets",
                    "openjpa_m3_b.csv"
            );

    private static final Path OUTPUT_C =
            Path.of(
                    "isw2",
                    "datasets",
                    "openjpa_m3_c.csv"
            );

    private static final Path VALIDATION_OUTPUT =
            Path.of(
                    "isw2",
                    "results",
                    "m3",
                    "dataset_builder_validation.txt"
            );

    private static final List<String> EXPECTED_HEADERS =
            List.of(
                    "Project",
                    "Class",
                    "ReleaseIndex",
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
                    "NSmells",
                    "NFIX",
                    "BUGGY"
            );

    private static final int EXPECTED_A_ROWS = 12_836;
    private static final int EXPECTED_A_BUGGY_YES = 2_010;
    private static final int EXPECTED_A_BUGGY_NO = 10_826;
    private static final int EXPECTED_RELEASES = 12;

    private M3DatasetBuilder() {
        // Utility class.
    }

    public static void main(
            String[] args
    ) throws Exception {

        Path repository =
                args.length >= 1
                        ? Path.of(args[0])
                        : Path.of(".");

        BuildResult result =
                build(
                        repository
                );

        writeDatasets(
                repository,
                result
        );

        writeValidationReport(
                repository,
                result
        );

        printSummary(
                result
        );
    }

    public static BuildResult build(
            Path repository
    ) throws IOException {

        Path normalizedRepository =
                repository
                        .toAbsolutePath()
                        .normalize();

        Path input =
                normalizedRepository.resolve(
                        DATASET_A
                );

        DatasetA datasetA =
                readDatasetA(
                        input
                );

        List<CsvRow> bPlus =
                new ArrayList<>();

        List<CsvRow> b =
                new ArrayList<>();

        List<CsvRow> c =
                new ArrayList<>();

        int smellIndex =
                datasetA.columnIndex()
                        .get(
                                "NSmells"
                        );

        for (CsvRow row
                : datasetA.rows()) {

            double smells =
                    parseFiniteDouble(
                            row.values()
                                    .get(
                                            smellIndex
                                    ),
                            "NSmells",
                            row.lineNumber()
                    );

            if (smells < 0.0) {
                throw new IllegalStateException(
                        "Negative NSmells at Dataset A line "
                                + row.lineNumber()
                );
            }

            if (Math.rint(smells)
                    != smells) {

                throw new IllegalStateException(
                        "NSmells is not an integer count at Dataset A line "
                                + row.lineNumber()
                                + ": "
                                + smells
                );
            }

            if (smells > 0.0) {

                CsvRow original =
                        copyRow(
                                row
                        );

                bPlus.add(
                        original
                );

                List<String> manipulatedValues =
                        new ArrayList<>(
                                row.values()
                        );

                manipulatedValues.set(
                        smellIndex,
                        "0"
                );

                b.add(
                        new CsvRow(
                                row.lineNumber(),
                                List.copyOf(
                                        manipulatedValues
                                )
                        )
                );

            } else {

                c.add(
                        copyRow(
                                row
                        )
                );
            }
        }

        BuildResult result =
                new BuildResult(
                        datasetA.headers(),
                        datasetA.columnIndex(),
                        datasetA.rows(),
                        List.copyOf(
                                bPlus
                        ),
                        List.copyOf(
                                b
                        ),
                        List.copyOf(
                                c
                        )
                );

        validateBuild(
                result
        );

        return result;
    }

    private static DatasetA readDatasetA(
            Path input
    ) throws IOException {

        if (!Files.isRegularFile(
                input
        )) {

            throw new IllegalArgumentException(
                    "Dataset A not found: "
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
                        "Dataset A is empty."
                );
            }

            List<String> headers =
                    parseCsvLine(
                            removeBom(
                                    headerLine
                            )
                    );

            if (!headers.equals(
                    EXPECTED_HEADERS
            )) {

                throw new IllegalStateException(
                        "Unexpected Dataset A schema.\nExpected: "
                                + EXPECTED_HEADERS
                                + "\nFound   : "
                                + headers
                );
            }

            Map<String, Integer> columnIndex =
                    new LinkedHashMap<>();

            for (int index = 0;
                 index < headers.size();
                 index++) {

                columnIndex.put(
                        headers.get(index),
                        index
                );
            }

            List<CsvRow> rows =
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
                            "Column count mismatch at Dataset A line "
                                    + lineNumber
                                    + ". Expected "
                                    + headers.size()
                                    + ", found "
                                    + values.size()
                    );
                }

                rows.add(
                        new CsvRow(
                                lineNumber,
                                List.copyOf(
                                        values
                                )
                        )
                );
            }

            DatasetA result =
                    new DatasetA(
                            List.copyOf(
                                    headers
                            ),
                            Map.copyOf(
                                    columnIndex
                            ),
                            List.copyOf(
                                    rows
                            )
                    );

            validateDatasetA(
                    result
            );

            return result;
        }
    }

    private static void validateDatasetA(
            DatasetA dataset
    ) {

        if (dataset.rows().size()
                != EXPECTED_A_ROWS) {

            throw new IllegalStateException(
                    "Dataset A row mismatch. Expected "
                            + EXPECTED_A_ROWS
                            + ", found "
                            + dataset.rows()
                            .size()
            );
        }

        int projectIndex =
                dataset.columnIndex()
                        .get(
                                "Project"
                        );

        int classIndex =
                dataset.columnIndex()
                        .get(
                                "Class"
                        );

        int releaseIndex =
                dataset.columnIndex()
                        .get(
                                "ReleaseIndex"
                        );

        int buggyIndex =
                dataset.columnIndex()
                        .get(
                                "BUGGY"
                        );

        Set<String> uniqueKeys =
                new HashSet<>();

        Set<Integer> releases =
                new HashSet<>();

        int buggyYes = 0;
        int buggyNo = 0;

        for (CsvRow row
                : dataset.rows()) {

            String project =
                    row.values()
                            .get(
                                    projectIndex
                            );

            if (!PROJECT.equals(
                    project
            )) {

                throw new IllegalStateException(
                        "Unexpected Project at Dataset A line "
                                + row.lineNumber()
                                + ": "
                                + project
                );
            }

            String classPath =
                    row.values()
                            .get(
                                    classIndex
                            );

            if (classPath.isBlank()) {
                throw new IllegalStateException(
                        "Blank Class at Dataset A line "
                                + row.lineNumber()
                );
            }

            int release =
                    parseInteger(
                            row.values()
                                    .get(
                                            releaseIndex
                                    ),
                            "ReleaseIndex",
                            row.lineNumber()
                    );

            if (release < 1
                    || release
                    > EXPECTED_RELEASES) {

                throw new IllegalStateException(
                        "ReleaseIndex out of expected range at Dataset A line "
                                + row.lineNumber()
                );
            }

            releases.add(
                    release
            );

            String key =
                    release
                            + "|"
                            + classPath;

            if (!uniqueKeys.add(
                    key
            )) {

                throw new IllegalStateException(
                        "Duplicate (ReleaseIndex, Class) in Dataset A: "
                                + key
                );
            }

            String buggy =
                    row.values()
                            .get(
                                    buggyIndex
                            );

            if ("YES".equals(
                    buggy
            )) {
                buggyYes++;

            } else if ("NO".equals(
                    buggy
            )) {
                buggyNo++;

            } else {
                throw new IllegalStateException(
                        "Unexpected BUGGY value at Dataset A line "
                                + row.lineNumber()
                                + ": "
                                + buggy
                );
            }
        }

        if (releases.size()
                != EXPECTED_RELEASES) {

            throw new IllegalStateException(
                    "Expected "
                            + EXPECTED_RELEASES
                            + " releases in Dataset A, found "
                            + releases.size()
            );
        }

        if (buggyYes
                != EXPECTED_A_BUGGY_YES
                || buggyNo
                != EXPECTED_A_BUGGY_NO) {

            throw new IllegalStateException(
                    "Dataset A BUGGY distribution mismatch. YES="
                            + buggyYes
                            + ", NO="
                            + buggyNo
            );
        }
    }

    private static void validateBuild(
            BuildResult result
    ) {

        if (result.bPlus().size()
                + result.c().size()
                != result.a().size()) {

            throw new IllegalStateException(
                    "|B+| + |C| != |A|"
            );
        }

        if (result.b().size()
                != result.bPlus().size()) {

            throw new IllegalStateException(
                    "|B| != |B+|"
            );
        }

        int smellIndex =
                result.columnIndex()
                        .get(
                                "NSmells"
                        );

        int buggyIndex =
                result.columnIndex()
                        .get(
                                "BUGGY"
                        );

        int classIndex =
                result.columnIndex()
                        .get(
                                "Class"
                        );

        int releaseIndex =
                result.columnIndex()
                        .get(
                                "ReleaseIndex"
                        );

        for (CsvRow row
                : result.bPlus()) {

            double smells =
                    parseFiniteDouble(
                            row.values()
                                    .get(
                                            smellIndex
                                    ),
                            "NSmells",
                            row.lineNumber()
                    );

            if (!(smells > 0.0)) {
                throw new IllegalStateException(
                        "B+ contains NSmells <= 0 at source line "
                                + row.lineNumber()
                );
            }
        }

        for (CsvRow row
                : result.c()) {

            double smells =
                    parseFiniteDouble(
                            row.values()
                                    .get(
                                            smellIndex
                                    ),
                            "NSmells",
                            row.lineNumber()
                    );

            if (smells != 0.0) {
                throw new IllegalStateException(
                        "C contains NSmells != 0 at source line "
                                + row.lineNumber()
                );
            }
        }

        for (int rowIndex = 0;
             rowIndex
                     < result.bPlus().size();
             rowIndex++) {

            CsvRow plus =
                    result.bPlus()
                            .get(
                                    rowIndex
                            );

            CsvRow manipulated =
                    result.b()
                            .get(
                                    rowIndex
                            );

            if (!"0".equals(
                    manipulated.values()
                            .get(
                                    smellIndex
                            )
            )) {

                throw new IllegalStateException(
                        "B contains NSmells != 0 at row "
                                + rowIndex
                );
            }

            for (int column = 0;
                 column
                         < result.headers()
                         .size();
                 column++) {

                if (column
                        == smellIndex) {

                    continue;
                }

                String plusValue =
                        plus.values()
                                .get(
                                        column
                                );

                String manipulatedValue =
                        manipulated.values()
                                .get(
                                        column
                                );

                if (!plusValue.equals(
                        manipulatedValue
                )) {

                    throw new IllegalStateException(
                            "B/B+ mismatch outside NSmells at row "
                                    + rowIndex
                                    + ", column "
                                    + result.headers()
                                    .get(
                                            column
                                    )
                    );
                }
            }

            if (!plus.values()
                    .get(
                            buggyIndex
                    )
                    .equals(
                            manipulated.values()
                                    .get(
                                            buggyIndex
                                    )
                    )) {

                throw new IllegalStateException(
                        "BUGGY changed between B+ and B at row "
                                + rowIndex
                );
            }

            if (!plus.values()
                    .get(
                            classIndex
                    )
                    .equals(
                            manipulated.values()
                                    .get(
                                            classIndex
                                    )
                    )
                    || !plus.values()
                    .get(
                            releaseIndex
                    )
                    .equals(
                            manipulated.values()
                                    .get(
                                            releaseIndex
                                    )
                    )) {

                throw new IllegalStateException(
                        "Observation identity changed between B+ and B."
                );
            }
        }

        validatePartitionMembership(
                result
        );
    }

    private static void validatePartitionMembership(
            BuildResult result
    ) {

        int classIndex =
                result.columnIndex()
                        .get(
                                "Class"
                        );

        int releaseIndex =
                result.columnIndex()
                        .get(
                                "ReleaseIndex"
                        );

        Set<String> bPlusKeys =
                keys(
                        result.bPlus(),
                        classIndex,
                        releaseIndex
                );

        Set<String> bKeys =
                keys(
                        result.b(),
                        classIndex,
                        releaseIndex
                );

        Set<String> cKeys =
                keys(
                        result.c(),
                        classIndex,
                        releaseIndex
                );

        if (!bPlusKeys.equals(
                bKeys
        )) {

            throw new IllegalStateException(
                    "B and B+ do not contain the same observations."
            );
        }

        Set<String> intersection =
                new HashSet<>(
                        bPlusKeys
                );

        intersection.retainAll(
                cKeys
        );

        if (!intersection.isEmpty()) {
            throw new IllegalStateException(
                    "B+ and C are not disjoint."
            );
        }

        Set<String> union =
                new HashSet<>(
                        bPlusKeys
                );

        union.addAll(
                cKeys
        );

        Set<String> aKeys =
                keys(
                        result.a(),
                        classIndex,
                        releaseIndex
                );

        if (!union.equals(
                aKeys
        )) {

            throw new IllegalStateException(
                    "B+ union C does not reconstruct A."
            );
        }
    }

    private static Set<String> keys(
            List<CsvRow> rows,
            int classIndex,
            int releaseIndex
    ) {

        Set<String> result =
                new HashSet<>();

        for (CsvRow row
                : rows) {

            String key =
                    row.values()
                            .get(
                                    releaseIndex
                            )
                            + "|"
                            + row.values()
                            .get(
                                    classIndex
                            );

            if (!result.add(
                    key
            )) {

                throw new IllegalStateException(
                        "Duplicate observation key in generated subset: "
                                + key
                );
            }
        }

        return result;
    }

    private static void writeDatasets(
            Path repository,
            BuildResult result
    ) throws IOException {

        Path normalizedRepository =
                repository
                        .toAbsolutePath()
                        .normalize();

        writeCsv(
                normalizedRepository.resolve(
                        OUTPUT_B_PLUS
                ),
                result.headers(),
                result.bPlus()
        );

        writeCsv(
                normalizedRepository.resolve(
                        OUTPUT_B
                ),
                result.headers(),
                result.b()
        );

        writeCsv(
                normalizedRepository.resolve(
                        OUTPUT_C
                ),
                result.headers(),
                result.c()
        );
    }

    private static void writeCsv(
            Path output,
            List<String> headers,
            List<CsvRow> rows
    ) throws IOException {

        Files.createDirectories(
                output.getParent()
        );

        List<String> lines =
                new ArrayList<>(
                        rows.size()
                                + 1
                );

        lines.add(
                toCsvLine(
                        headers
                )
        );

        for (CsvRow row
                : rows) {

            lines.add(
                    toCsvLine(
                            row.values()
                    )
            );
        }

        Files.write(
                output,
                lines,
                StandardCharsets.UTF_8
        );
    }

    private static void writeValidationReport(
            Path repository,
            BuildResult result
    ) throws IOException {

        Path output =
                repository
                        .toAbsolutePath()
                        .normalize()
                        .resolve(
                                VALIDATION_OUTPUT
                        );

        Files.createDirectories(
                output.getParent()
        );

        Counts aCounts =
                counts(
                        result.a(),
                        result.columnIndex()
                );

        Counts bPlusCounts =
                counts(
                        result.bPlus(),
                        result.columnIndex()
                );

        Counts bCounts =
                counts(
                        result.b(),
                        result.columnIndex()
                );

        Counts cCounts =
                counts(
                        result.c(),
                        result.columnIndex()
                );

        List<String> lines =
                new ArrayList<>();

        lines.add(
                "===== OPENJPA M3 DATASET BUILDER VALIDATION ====="
        );

        lines.add(
                "Dataset A rows          : "
                        + result.a()
                        .size()
        );

        lines.add(
                "Dataset B+ rows         : "
                        + result.bPlus()
                        .size()
        );

        lines.add(
                "Dataset B rows          : "
                        + result.b()
                        .size()
        );

        lines.add(
                "Dataset C rows          : "
                        + result.c()
                        .size()
        );

        lines.add(
                "B+ + C = A              : "
                        + (
                        result.bPlus()
                                .size()
                                + result.c()
                                .size()
                                == result.a()
                                .size()
                )
        );

        lines.add(
                "B rows = B+ rows         : "
                        + (
                        result.b()
                                .size()
                                == result.bPlus()
                                .size()
                )
        );

        lines.add("");

        lines.add(
                "A  BUGGY YES/NO          : "
                        + aCounts.buggyYes()
                        + " / "
                        + aCounts.buggyNo()
        );

        lines.add(
                "B+ BUGGY YES/NO          : "
                        + bPlusCounts.buggyYes()
                        + " / "
                        + bPlusCounts.buggyNo()
        );

        lines.add(
                "B  BUGGY YES/NO          : "
                        + bCounts.buggyYes()
                        + " / "
                        + bCounts.buggyNo()
        );

        lines.add(
                "C  BUGGY YES/NO          : "
                        + cCounts.buggyYes()
                        + " / "
                        + cCounts.buggyNo()
        );

        lines.add("");

        lines.add(
                "A  Sum(NSmells)          : "
                        + formatCount(
                        aCounts.sumSmells()
                )
        );

        lines.add(
                "B+ Sum(NSmells)          : "
                        + formatCount(
                        bPlusCounts.sumSmells()
                )
        );

        lines.add(
                "B  Sum(NSmells)          : "
                        + formatCount(
                        bCounts.sumSmells()
                )
        );

        lines.add(
                "C  Sum(NSmells)          : "
                        + formatCount(
                        cCounts.sumSmells()
                )
        );

        lines.add("");

        lines.add(
                "B+ condition NSmells > 0 : PASSED"
        );

        lines.add(
                "C condition NSmells = 0  : PASSED"
        );

        lines.add(
                "B condition NSmells = 0  : PASSED"
        );

        lines.add(
                "B/B+ same observations   : PASSED"
        );

        lines.add(
                "B/B+ only NSmells differs: PASSED"
        );

        lines.add(
                "B/B+ BUGGY unchanged     : PASSED"
        );

        lines.add(
                "B+ and C disjoint        : PASSED"
        );

        lines.add(
                "B+ union C = A            : PASSED"
        );

        lines.add(
                "Schema preserved          : PASSED"
        );

        lines.add(
                "ValidationPassed=True"
        );

        lines.add(
                "================================================="
        );

        Files.write(
                output,
                lines,
                StandardCharsets.UTF_8
        );
    }

    private static Counts counts(
            List<CsvRow> rows,
            Map<String, Integer> columnIndex
    ) {

        int buggyIndex =
                columnIndex.get(
                        "BUGGY"
                );

        int smellIndex =
                columnIndex.get(
                        "NSmells"
                );

        int yes = 0;
        int no = 0;
        double sumSmells = 0.0;

        for (CsvRow row
                : rows) {

            String buggy =
                    row.values()
                            .get(
                                    buggyIndex
                            );

            if ("YES".equals(
                    buggy
            )) {
                yes++;
            } else if ("NO".equals(
                    buggy
            )) {
                no++;
            } else {
                throw new IllegalStateException(
                        "Unexpected BUGGY value in generated dataset."
                );
            }

            sumSmells +=
                    parseFiniteDouble(
                            row.values()
                                    .get(
                                            smellIndex
                                    ),
                            "NSmells",
                            row.lineNumber()
                    );
        }

        return new Counts(
                yes,
                no,
                sumSmells
        );
    }

    private static void printSummary(
            BuildResult result
    ) {

        Counts aCounts =
                counts(
                        result.a(),
                        result.columnIndex()
                );

        Counts bPlusCounts =
                counts(
                        result.bPlus(),
                        result.columnIndex()
                );

        Counts bCounts =
                counts(
                        result.b(),
                        result.columnIndex()
                );

        Counts cCounts =
                counts(
                        result.c(),
                        result.columnIndex()
                );

        System.out.println(
                "===== OPENJPA M3 DATASET BUILDER ====="
        );

        System.out.println(
                "A rows                : "
                        + result.a()
                        .size()
        );

        System.out.println(
                "B+ rows (NSmells > 0) : "
                        + result.bPlus()
                        .size()
        );

        System.out.println(
                "B rows  (what-if)     : "
                        + result.b()
                        .size()
        );

        System.out.println(
                "C rows  (NSmells = 0) : "
                        + result.c()
                        .size()
        );

        System.out.println("");

        System.out.printf(
                Locale.ROOT,
                "A  BUGGY YES/NO       : %d / %d%n",
                aCounts.buggyYes(),
                aCounts.buggyNo()
        );

        System.out.printf(
                Locale.ROOT,
                "B+ BUGGY YES/NO       : %d / %d%n",
                bPlusCounts.buggyYes(),
                bPlusCounts.buggyNo()
        );

        System.out.printf(
                Locale.ROOT,
                "B  BUGGY YES/NO       : %d / %d%n",
                bCounts.buggyYes(),
                bCounts.buggyNo()
        );

        System.out.printf(
                Locale.ROOT,
                "C  BUGGY YES/NO       : %d / %d%n",
                cCounts.buggyYes(),
                cCounts.buggyNo()
        );

        System.out.println("");

        System.out.println(
                "A  Sum(NSmells)       : "
                        + formatCount(
                        aCounts.sumSmells()
                )
        );

        System.out.println(
                "B+ Sum(NSmells)       : "
                        + formatCount(
                        bPlusCounts.sumSmells()
                )
        );

        System.out.println(
                "B  Sum(NSmells)       : "
                        + formatCount(
                        bCounts.sumSmells()
                )
        );

        System.out.println(
                "C  Sum(NSmells)       : "
                        + formatCount(
                        cCounts.sumSmells()
                )
        );

        System.out.println("");

        System.out.println(
                "B+ + C = A            : True"
        );

        System.out.println(
                "B/B+ same observations: True"
        );

        System.out.println(
                "Only NSmells changed  : True"
        );

        System.out.println(
                "ValidationPassed      : True"
        );

        System.out.println(
                "====================================="
        );
    }

    private static CsvRow copyRow(
            CsvRow row
    ) {

        return new CsvRow(
                row.lineNumber(),
                List.copyOf(
                        row.values()
                )
        );
    }

    private static String formatCount(
            double value
    ) {

        if (Math.rint(
                value
        ) == value) {

            return Long.toString(
                    Math.round(
                            value
                    )
            );
        }

        return String.format(
                Locale.ROOT,
                "%.6f",
                value
        );
    }

    private static int parseInteger(
            String value,
            String column,
            int lineNumber
    ) {

        try {
            return Integer.parseInt(
                    value.trim()
            );

        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "Invalid integer "
                            + column
                            + " at line "
                            + lineNumber
                            + ": "
                            + value,
                    exception
            );
        }
    }

    private static double parseFiniteDouble(
            String value,
            String column,
            int lineNumber
    ) {

        final double parsed;

        try {
            parsed =
                    Double.parseDouble(
                            value.trim()
                    );

        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "Invalid numeric "
                            + column
                            + " at line "
                            + lineNumber
                            + ": "
                            + value,
                    exception
            );
        }

        if (!Double.isFinite(
                parsed
        )) {

            throw new IllegalStateException(
                    "Non-finite "
                            + column
                            + " at line "
                            + lineNumber
            );
        }

        return parsed;
    }

    private static String toCsvLine(
            List<String> values
    ) {

        List<String> escaped =
                new ArrayList<>(
                        values.size()
                );

        for (String value
                : values) {

            escaped.add(
                    escapeCsv(
                            value
                    )
            );
        }

        return String.join(
                ",",
                escaped
        );
    }

    private static String escapeCsv(
            String value
    ) {

        if (value.contains(",")
                || value.contains("\"")
                || value.contains("\n")
                || value.contains("\r")) {

            return "\""
                    + value.replace(
                    "\"",
                    "\"\""
            )
                    + "\"";
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
                    "Malformed CSV line with unmatched quote."
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

    public record BuildResult(
            List<String> headers,
            Map<String, Integer> columnIndex,
            List<CsvRow> a,
            List<CsvRow> bPlus,
            List<CsvRow> b,
            List<CsvRow> c
    ) {
    }

    private record DatasetA(
            List<String> headers,
            Map<String, Integer> columnIndex,
            List<CsvRow> rows
    ) {
    }

    private record CsvRow(
            int lineNumber,
            List<String> values
    ) {
    }

    private record Counts(
            int buggyYes,
            int buggyNo,
            double sumSmells
    ) {
    }
}
