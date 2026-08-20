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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

/**
 * Loads the final Milestone 1 Dataset A into a Weka-ready dataset for
 * Milestone 2.
 *
 * <p>The identifiers Project, Class and ReleaseIndex are kept only as
 * metadata and are deliberately excluded from the Weka predictor matrix.
 * The resulting Weka dataset therefore contains exactly 18 numeric
 * predictors plus the nominal BUGGY target.</p>
 */
public final class M2DatasetLoader {

    private static final String PROJECT = "OPENJPA";

    private static final int EXPECTED_ROWS = 12_836;
    private static final int EXPECTED_RELEASES = 12;
    private static final int EXPECTED_PREDICTORS = 18;
    private static final int EXPECTED_BUGGY_YES = 2_010;
    private static final int EXPECTED_BUGGY_NO = 10_826;

    private static final Path DATASET_PATH =
            Path.of("isw2", "datasets", "openjpa_dataset_a.csv");

    private static final Path VALIDATION_REPORT =
            Path.of("isw2", "results", "m2", "dataset_loader_validation.txt");

    private static final List<String> EXPECTED_HEADERS = List.of(
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

    private static final List<String> PREDICTOR_COLUMNS = List.of(
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
            "NFIX"
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

    private M2DatasetLoader() {
        // Utility class.
    }

    /**
     * Loads and validates Dataset A.
     *
     * @param repository repository root
     * @return Weka-ready data plus row metadata
     * @throws IOException if the CSV cannot be read
     */
    public static LoadedDataset load(Path repository) throws IOException {
        Path normalizedRepository =
                repository.toAbsolutePath().normalize();

        Path input =
                normalizedRepository.resolve(DATASET_PATH);

        if (!Files.isRegularFile(input)) {
            throw new IllegalArgumentException(
                    "Dataset A not found: " + input
            );
        }

        ArrayList<Attribute> attributes =
                new ArrayList<>();

        for (String predictor : PREDICTOR_COLUMNS) {
            attributes.add(
                    new Attribute(predictor)
            );
        }

        Attribute classAttribute =
                new Attribute(
                        "BUGGY",
                        List.of("NO", "YES")
                );

        attributes.add(classAttribute);

        Instances data =
                new Instances(
                        "OPENJPA_M2",
                        attributes,
                        EXPECTED_ROWS
                );

        data.setClassIndex(
                data.numAttributes() - 1
        );

        List<RowMetadata> metadata =
                new ArrayList<>(EXPECTED_ROWS);

        Set<ObservationKey> keys =
                new HashSet<>(EXPECTED_ROWS);

        Map<Integer, Integer> rowsByRelease =
                new LinkedHashMap<>();

        int yesCount = 0;
        int noCount = 0;

        double minLoc =
                Double.POSITIVE_INFINITY;

        double maxLoc =
                Double.NEGATIVE_INFINITY;

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             input,
                             StandardCharsets.UTF_8
                     )) {

            String headerLine =
                    reader.readLine();

            if (headerLine == null) {
                throw new IllegalStateException(
                        "Dataset A is empty: " + input
                );
            }

            List<String> headers =
                    parseCsvLine(
                            removeBom(headerLine)
                    );

            if (!headers.equals(EXPECTED_HEADERS)) {
                throw new IllegalStateException(
                        "Dataset A header mismatch."
                                + System.lineSeparator()
                                + "Expected: "
                                + EXPECTED_HEADERS
                                + System.lineSeparator()
                                + "Actual  : "
                                + headers
                );
            }

            Map<String, Integer> columnIndex =
                    new HashMap<>();

            for (int index = 0;
                 index < headers.size();
                 index++) {

                columnIndex.put(
                        headers.get(index),
                        index
                );
            }

            String line;
            int lineNumber = 1;
            int originalIndex = 0;

            while ((line = reader.readLine())
                    != null) {

                lineNumber++;

                if (line.isBlank()) {
                    continue;
                }

                List<String> row =
                        parseCsvLine(line);

                if (row.size()
                        != EXPECTED_HEADERS.size()) {

                    throw new IllegalStateException(
                            "Column-count mismatch at line "
                                    + lineNumber
                                    + ". Expected "
                                    + EXPECTED_HEADERS.size()
                                    + ", found "
                                    + row.size()
                    );
                }

                String project =
                        value(
                                row,
                                columnIndex,
                                "Project"
                        );

                String classPath =
                        value(
                                row,
                                columnIndex,
                                "Class"
                        );

                int releaseIndex =
                        parseInteger(
                                value(
                                        row,
                                        columnIndex,
                                        "ReleaseIndex"
                                ),
                                "ReleaseIndex",
                                lineNumber
                        );

                String buggy =
                        value(
                                row,
                                columnIndex,
                                "BUGGY"
                        );

                validateIdentifiers(
                        project,
                        classPath,
                        releaseIndex,
                        lineNumber
                );

                ObservationKey key =
                        new ObservationKey(
                                releaseIndex,
                                classPath
                        );

                if (!keys.add(key)) {
                    throw new IllegalStateException(
                            "Duplicate observation at line "
                                    + lineNumber
                                    + ": "
                                    + key
                    );
                }

                double[] values =
                        new double[
                                EXPECTED_PREDICTORS + 1
                        ];

                double loc = Double.NaN;

                for (int predictorIndex = 0;
                     predictorIndex
                             < PREDICTOR_COLUMNS.size();
                     predictorIndex++) {

                    String predictor =
                            PREDICTOR_COLUMNS.get(
                                    predictorIndex
                            );

                    double numeric =
                            parseFiniteDouble(
                                    value(
                                            row,
                                            columnIndex,
                                            predictor
                                    ),
                                    predictor,
                                    lineNumber
                            );

                    values[predictorIndex] =
                            numeric;

                    if ("LOC".equals(predictor)) {
                        loc = numeric;
                    }
                }

                if (!(loc > 0.0)) {
                    throw new IllegalStateException(
                            "LOC must be > 0 at line "
                                    + lineNumber
                                    + ", found "
                                    + loc
                    );
                }

                minLoc =
                        Math.min(
                                minLoc,
                                loc
                        );

                maxLoc =
                        Math.max(
                                maxLoc,
                                loc
                        );

                final boolean actualBuggy;

                if ("YES".equals(buggy)) {

                    actualBuggy = true;
                    yesCount++;

                    values[EXPECTED_PREDICTORS] =
                            classAttribute.indexOfValue(
                                    "YES"
                            );

                } else if ("NO".equals(buggy)) {

                    actualBuggy = false;
                    noCount++;

                    values[EXPECTED_PREDICTORS] =
                            classAttribute.indexOfValue(
                                    "NO"
                            );

                } else {

                    throw new IllegalStateException(
                            "Invalid BUGGY value at line "
                                    + lineNumber
                                    + ": "
                                    + buggy
                    );
                }

                data.add(
                        new DenseInstance(
                                1.0,
                                values
                        )
                );

                metadata.add(
                        new RowMetadata(
                                originalIndex,
                                project,
                                classPath,
                                releaseIndex,
                                loc,
                                actualBuggy
                        )
                );

                rowsByRelease.merge(
                        releaseIndex,
                        1,
                        Integer::sum
                );

                originalIndex++;
            }
        }

        validateFinalDataset(
                data,
                metadata,
                keys,
                rowsByRelease,
                yesCount,
                noCount
        );

        return new LoadedDataset(
                data,
                List.copyOf(metadata),
                data.classAttribute()
                        .indexOfValue("YES"),
                Map.copyOf(rowsByRelease),
                yesCount,
                noCount,
                minLoc,
                maxLoc
        );
    }

    private static void validateIdentifiers(
            String project,
            String classPath,
            int releaseIndex,
            int lineNumber
    ) {

        if (!PROJECT.equals(project)) {
            throw new IllegalStateException(
                    "Unexpected Project at line "
                            + lineNumber
                            + ": "
                            + project
            );
        }

        if (classPath.isBlank()) {
            throw new IllegalStateException(
                    "Blank Class at line "
                            + lineNumber
            );
        }

        if (releaseIndex < 1
                || releaseIndex > EXPECTED_RELEASES) {

            throw new IllegalStateException(
                    "ReleaseIndex out of range at line "
                            + lineNumber
                            + ": "
                            + releaseIndex
            );
        }
    }

    private static void validateFinalDataset(
            Instances data,
            List<RowMetadata> metadata,
            Set<ObservationKey> keys,
            Map<Integer, Integer> rowsByRelease,
            int yesCount,
            int noCount
    ) {

        if (data.numInstances()
                != EXPECTED_ROWS) {

            throw new IllegalStateException(
                    "Weka row mismatch. Expected "
                            + EXPECTED_ROWS
                            + ", found "
                            + data.numInstances()
            );
        }

        if (metadata.size()
                != EXPECTED_ROWS) {

            throw new IllegalStateException(
                    "Metadata row mismatch. Expected "
                            + EXPECTED_ROWS
                            + ", found "
                            + metadata.size()
            );
        }

        if (keys.size()
                != EXPECTED_ROWS) {

            throw new IllegalStateException(
                    "Unique observation mismatch. Expected "
                            + EXPECTED_ROWS
                            + ", found "
                            + keys.size()
            );
        }

        if (data.numAttributes()
                != EXPECTED_PREDICTORS + 1) {

            throw new IllegalStateException(
                    "Weka attribute mismatch. Expected "
                            + (EXPECTED_PREDICTORS + 1)
                            + " including BUGGY, found "
                            + data.numAttributes()
            );
        }

        if (data.classIndex()
                != data.numAttributes() - 1) {

            throw new IllegalStateException(
                    "BUGGY is not the last/class attribute."
            );
        }

        if (!"BUGGY".equals(
                data.classAttribute().name()
        )) {

            throw new IllegalStateException(
                    "Unexpected class attribute: "
                            + data.classAttribute().name()
            );
        }

        if (!data.classAttribute()
                .isNominal()) {

            throw new IllegalStateException(
                    "BUGGY must be nominal."
            );
        }

        if (data.classAttribute()
                .indexOfValue("YES") < 0
                || data.classAttribute()
                .indexOfValue("NO") < 0) {

            throw new IllegalStateException(
                    "BUGGY must contain YES and NO."
            );
        }

        for (int attributeIndex = 0;
             attributeIndex < data.numAttributes() - 1;
             attributeIndex++) {

            Attribute attribute =
                    data.attribute(
                            attributeIndex
                    );

            if (!attribute.isNumeric()) {
                throw new IllegalStateException(
                        "Predictor is not numeric: "
                                + attribute.name()
                );
            }
        }

        if (yesCount
                != EXPECTED_BUGGY_YES) {

            throw new IllegalStateException(
                    "BUGGY=YES mismatch. Expected "
                            + EXPECTED_BUGGY_YES
                            + ", found "
                            + yesCount
            );
        }

        if (noCount
                != EXPECTED_BUGGY_NO) {

            throw new IllegalStateException(
                    "BUGGY=NO mismatch. Expected "
                            + EXPECTED_BUGGY_NO
                            + ", found "
                            + noCount
            );
        }

        if (rowsByRelease.size()
                != EXPECTED_RELEASES) {

            throw new IllegalStateException(
                    "Release count mismatch. Expected "
                            + EXPECTED_RELEASES
                            + ", found "
                            + rowsByRelease.size()
            );
        }

        for (int release = 1;
             release <= EXPECTED_RELEASES;
             release++) {

            int expected =
                    EXPECTED_ROWS_BY_RELEASE.get(
                            release
                    );

            int actual =
                    rowsByRelease.getOrDefault(
                            release,
                            0
                    );

            if (actual != expected) {
                throw new IllegalStateException(
                        "Release "
                                + release
                                + " row mismatch. Expected "
                                + expected
                                + ", found "
                                + actual
                );
            }
        }

        for (int rowIndex = 0;
             rowIndex < data.numInstances();
             rowIndex++) {

            if (data.instance(rowIndex)
                    .hasMissingValue()) {

                throw new IllegalStateException(
                        "Missing Weka value at row "
                                + rowIndex
                );
            }
        }
    }

    private static String value(
            List<String> row,
            Map<String, Integer> columnIndex,
            String column
    ) {

        Integer index =
                columnIndex.get(column);

        if (index == null) {
            throw new IllegalArgumentException(
                    "Missing column: " + column
            );
        }

        return row.get(index).trim();
    }

    private static int parseInteger(
            String text,
            String column,
            int lineNumber
    ) {

        try {
            return Integer.parseInt(text);

        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "Invalid integer in "
                            + column
                            + " at line "
                            + lineNumber
                            + ": "
                            + text,
                    exception
            );
        }
    }

    private static double parseFiniteDouble(
            String text,
            String column,
            int lineNumber
    ) {

        final double value;

        try {
            value = Double.parseDouble(text);

        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "Invalid numeric value in "
                            + column
                            + " at line "
                            + lineNumber
                            + ": "
                            + text,
                    exception
            );
        }

        if (!Double.isFinite(value)) {
            throw new IllegalStateException(
                    "Non-finite numeric value in "
                            + column
                            + " at line "
                            + lineNumber
                            + ": "
                            + text
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

                current.setLength(0);

            } else {
                current.append(character);
            }
        }

        if (quoted) {
            throw new IllegalArgumentException(
                    "Malformed CSV line: unclosed quote."
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
                && value.charAt(0) == '\uFEFF') {

            return value.substring(1);
        }

        return value;
    }

    private static void writeValidationReport(
            Path repository,
            LoadedDataset loaded
    ) throws IOException {

        Path output =
                repository
                        .toAbsolutePath()
                        .normalize()
                        .resolve(
                                VALIDATION_REPORT
                        );

        Files.createDirectories(
                output.getParent()
        );

        List<String> lines =
                new ArrayList<>();

        lines.add(
                "===== OPENJPA M2 DATASET LOADER VALIDATION ====="
        );

        lines.add(
                "Rows                 : "
                        + loaded.data().numInstances()
        );

        lines.add(
                "Weka attributes      : "
                        + loaded.data().numAttributes()
        );

        lines.add(
                "Predictors           : "
                        + (loaded.data().numAttributes() - 1)
        );

        lines.add(
                "Class attribute      : "
                        + loaded.data().classAttribute().name()
        );

        lines.add(
                "Positive class       : YES"
        );

        lines.add(
                "Positive class index : "
                        + loaded.positiveClassIndex()
        );

        lines.add(
                "BUGGY=YES            : "
                        + loaded.buggyYes()
        );

        lines.add(
                "BUGGY=NO             : "
                        + loaded.buggyNo()
        );

        lines.add(
                "Metadata rows        : "
                        + loaded.metadata().size()
        );

        lines.add(
                "Releases             : "
                        + loaded.rowsByRelease().size()
        );

        lines.add(
                "Min LOC              : "
                        + loaded.minLoc()
        );

        lines.add(
                "Max LOC              : "
                        + loaded.maxLoc()
        );

        lines.add("");
        lines.add("===== BY RELEASE =====");

        for (int release = 1;
             release <= EXPECTED_RELEASES;
             release++) {

            lines.add(
                    String.format(
                            "Release %2d : %d",
                            release,
                            loaded.rowsByRelease()
                                    .get(release)
                    )
            );
        }

        lines.add("");
        lines.add("Predictor names:");

        for (String predictor
                : PREDICTOR_COLUMNS) {

            lines.add(
                    "- " + predictor
            );
        }

        lines.add("");
        lines.add("ValidationPassed=True");
        lines.add("================================================");

        Files.write(
                output,
                lines,
                StandardCharsets.UTF_8
        );
    }

    private static void printSummary(
            LoadedDataset loaded
    ) {

        System.out.println(
                "===== OPENJPA M2 DATASET LOADER ====="
        );

        System.out.println(
                "Rows                 : "
                        + loaded.data().numInstances()
        );

        System.out.println(
                "Weka attributes      : "
                        + loaded.data().numAttributes()
        );

        System.out.println(
                "Predictors           : "
                        + (loaded.data().numAttributes() - 1)
        );

        System.out.println(
                "Identifiers in model : 0"
        );

        System.out.println(
                "Class attribute      : "
                        + loaded.data().classAttribute().name()
        );

        System.out.println(
                "Positive class       : YES"
        );

        System.out.println(
                "Positive class index : "
                        + loaded.positiveClassIndex()
        );

        System.out.println(
                "BUGGY=YES            : "
                        + loaded.buggyYes()
        );

        System.out.println(
                "BUGGY=NO             : "
                        + loaded.buggyNo()
        );

        System.out.println(
                "Metadata rows        : "
                        + loaded.metadata().size()
        );

        System.out.println(
                "Releases             : "
                        + loaded.rowsByRelease().size()
        );

        System.out.println(
                "Min LOC              : "
                        + loaded.minLoc()
        );

        System.out.println(
                "Max LOC              : "
                        + loaded.maxLoc()
        );

        System.out.println(
                "ValidationPassed     : True"
        );

        System.out.println(
                "======================================"
        );
    }

    public static void main(
            String[] args
    ) throws Exception {

        Path repository =
                args.length == 0
                        ? Path.of(".")
                        : Path.of(args[0]);

        LoadedDataset loaded =
                load(repository);

        writeValidationReport(
                repository,
                loaded
        );

        printSummary(
                loaded
        );
    }

    public record RowMetadata(
            int originalIndex,
            String project,
            String classPath,
            int releaseIndex,
            double loc,
            boolean buggy
    ) {
    }

    public record LoadedDataset(
            Instances data,
            List<RowMetadata> metadata,
            int positiveClassIndex,
            Map<Integer, Integer> rowsByRelease,
            int buggyYes,
            int buggyNo,
            double minLoc,
            double maxLoc
    ) {
    }

    private record ObservationKey(
            int releaseIndex,
            String classPath
    ) {
    }
}
