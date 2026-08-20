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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

/**
 * Runs the Milestone 3 what-if prediction experiment.
 *
 * <p>The classifier selected in Milestone 2 is RandomForest. Following the
 * Milestone 3 design, one final model (BClassifierA) is trained on the whole
 * Dataset A and then applied to A, B+, B and C.</p>
 *
 * <p>No feature selection and no balancing are applied here. All 18 Dataset A
 * predictors are retained, including NSmells, because NSmells is the variable
 * manipulated by the B+ -> B counterfactual.</p>
 */
public final class M3WhatIfRunner {

    private static final int RANDOM_FOREST_SEED = 1;

    private static final int EXPECTED_A_ROWS = 12_836;
    private static final int EXPECTED_B_PLUS_ROWS = 8_933;
    private static final int EXPECTED_B_ROWS = 8_933;
    private static final int EXPECTED_C_ROWS = 3_903;

    private static final int EXPECTED_A_BUGGY_YES = 2_010;
    private static final int EXPECTED_B_PLUS_BUGGY_YES = 1_723;
    private static final int EXPECTED_C_BUGGY_YES = 287;

    private static final List<String> PREDICTORS =
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
                    "NSmells",
                    "NFIX"
            );

    private static final List<String> REQUIRED_HEADERS =
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

    private static final Path DATASET_A =
            Path.of(
                    "isw2",
                    "datasets",
                    "openjpa_dataset_a.csv"
            );

    private static final Path DATASET_B_PLUS =
            Path.of(
                    "isw2",
                    "datasets",
                    "openjpa_m3_bplus.csv"
            );

    private static final Path DATASET_B =
            Path.of(
                    "isw2",
                    "datasets",
                    "openjpa_m3_b.csv"
            );

    private static final Path DATASET_C =
            Path.of(
                    "isw2",
                    "datasets",
                    "openjpa_m3_c.csv"
            );

    private static final Path OUTPUT_DIRECTORY =
            Path.of(
                    "isw2",
                    "results",
                    "m3"
            );

    private M3WhatIfRunner() {
        // Utility class.
    }

    public static void main(
            String[] args
    ) throws Exception {

        Path repository =
                args.length >= 1
                        ? Path.of(args[0])
                        : Path.of(".");

        RunResult result =
                run(
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

    public static RunResult run(
            Path repository
    ) throws Exception {

        Path normalizedRepository =
                repository
                        .toAbsolutePath()
                        .normalize();

        LoadedDataset a =
                loadDataset(
                        normalizedRepository.resolve(
                                DATASET_A
                        ),
                        "A",
                        EXPECTED_A_ROWS,
                        EXPECTED_A_BUGGY_YES
                );

        LoadedDataset bPlus =
                loadDataset(
                        normalizedRepository.resolve(
                                DATASET_B_PLUS
                        ),
                        "B+",
                        EXPECTED_B_PLUS_ROWS,
                        EXPECTED_B_PLUS_BUGGY_YES
                );

        LoadedDataset b =
                loadDataset(
                        normalizedRepository.resolve(
                                DATASET_B
                        ),
                        "B",
                        EXPECTED_B_ROWS,
                        EXPECTED_B_PLUS_BUGGY_YES
                );

        LoadedDataset c =
                loadDataset(
                        normalizedRepository.resolve(
                                DATASET_C
                        ),
                        "C",
                        EXPECTED_C_ROWS,
                        EXPECTED_C_BUGGY_YES
                );

        validateDatasetRelations(
                a,
                bPlus,
                b,
                c
        );

        RandomForest classifier =
                new RandomForest();

        classifier.setSeed(
                RANDOM_FOREST_SEED
        );

        /*
         * Milestone 3 BClassifierA:
         * one RandomForest trained on the complete Dataset A.
         *
         * There is deliberately no Feature Selection and no SMOTE here.
         */
        classifier.buildClassifier(
                a.instances()
        );

        List<PredictionRow> allPredictions =
                new ArrayList<>(
                        EXPECTED_A_ROWS
                                + EXPECTED_B_PLUS_ROWS
                                + EXPECTED_B_ROWS
                                + EXPECTED_C_ROWS
                );

        DatasetPrediction aPrediction =
                predict(
                        classifier,
                        a
                );

        DatasetPrediction bPlusPrediction =
                predict(
                        classifier,
                        bPlus
                );

        DatasetPrediction bPrediction =
                predict(
                        classifier,
                        b
                );

        DatasetPrediction cPrediction =
                predict(
                        classifier,
                        c
                );

        allPredictions.addAll(
                aPrediction.rows()
        );

        allPredictions.addAll(
                bPlusPrediction.rows()
        );

        allPredictions.addAll(
                bPrediction.rows()
        );

        allPredictions.addAll(
                cPrediction.rows()
        );

        validatePredictionRelations(
                aPrediction,
                bPlusPrediction,
                bPrediction,
                cPrediction
        );

        RunResult result =
                new RunResult(
                        RANDOM_FOREST_SEED,
                        classifierOptions(
                                classifier
                        ),
                        List.copyOf(
                                allPredictions
                        ),
                        List.of(
                                aPrediction.summary(),
                                bPlusPrediction.summary(),
                                bPrediction.summary(),
                                cPrediction.summary()
                        )
                );

        validateRunResult(
                result
        );

        return result;
    }

    private static LoadedDataset loadDataset(
            Path input,
            String name,
            int expectedRows,
            int expectedBuggyYes
    ) throws IOException {

        if (!Files.isRegularFile(
                input
        )) {

            throw new IllegalArgumentException(
                    "Dataset "
                            + name
                            + " not found: "
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
                        "Dataset "
                                + name
                                + " is empty."
                );
            }

            List<String> headers =
                    parseCsvLine(
                            removeBom(
                                    headerLine
                            )
                    );

            if (!headers.equals(
                    REQUIRED_HEADERS
            )) {

                throw new IllegalStateException(
                        "Unexpected schema for dataset "
                                + name
                                + "."
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

            ArrayList<Attribute> attributes =
                    new ArrayList<>();

            for (String predictor
                    : PREDICTORS) {

                attributes.add(
                        new Attribute(
                                predictor
                        )
                );
            }

            ArrayList<String> classValues =
                    new ArrayList<>();

            classValues.add(
                    "NO"
            );

            classValues.add(
                    "YES"
            );

            attributes.add(
                    new Attribute(
                            "BUGGY",
                            classValues
                    )
            );

            Instances instances =
                    new Instances(
                            "OPENJPA_M3_"
                                    + sanitizeRelationName(
                                    name
                            ),
                            attributes,
                            expectedRows
                    );

            instances.setClassIndex(
                    instances.numAttributes()
                            - 1
            );

            List<RowMetadata> metadata =
                    new ArrayList<>(
                            expectedRows
                    );

            String line;
            int lineNumber = 1;
            int datasetRowIndex = 0;

            int buggyYes = 0;
            int buggyNo = 0;

            Set<String> uniqueKeys =
                    new HashSet<>();

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
                            "Column mismatch in dataset "
                                    + name
                                    + " at line "
                                    + lineNumber
                    );
                }

                String project =
                        value(
                                values,
                                columnIndex,
                                "Project"
                        );

                if (!"OPENJPA".equals(
                        project
                )) {

                    throw new IllegalStateException(
                            "Unexpected Project in dataset "
                                    + name
                                    + " at line "
                                    + lineNumber
                    );
                }

                String classPath =
                        value(
                                values,
                                columnIndex,
                                "Class"
                        );

                if (classPath.isBlank()) {
                    throw new IllegalStateException(
                            "Blank Class in dataset "
                                    + name
                                    + " at line "
                                    + lineNumber
                    );
                }

                int releaseIndex =
                        parseInteger(
                                value(
                                        values,
                                        columnIndex,
                                        "ReleaseIndex"
                                ),
                                "ReleaseIndex",
                                name,
                                lineNumber
                        );

                String key =
                        observationKey(
                                releaseIndex,
                                classPath
                        );

                if (!uniqueKeys.add(
                        key
                )) {

                    throw new IllegalStateException(
                            "Duplicate observation in dataset "
                                    + name
                                    + ": "
                                    + key
                    );
                }

                double[] instanceValues =
                        new double[
                                PREDICTORS.size()
                                        + 1
                                ];

                Map<String, Double> predictorValues =
                        new LinkedHashMap<>();

                for (int predictorIndex = 0;
                     predictorIndex
                             < PREDICTORS.size();
                     predictorIndex++) {

                    String predictor =
                            PREDICTORS.get(
                                    predictorIndex
                            );

                    double numericValue =
                            parseDouble(
                                    value(
                                            values,
                                            columnIndex,
                                            predictor
                                    ),
                                    predictor,
                                    name,
                                    lineNumber
                            );

                    instanceValues[predictorIndex] =
                            numericValue;

                    predictorValues.put(
                            predictor,
                            numericValue
                    );
                }

                String buggy =
                        value(
                                values,
                                columnIndex,
                                "BUGGY"
                        );

                int classIndex;

                if ("YES".equals(
                        buggy
                )) {

                    classIndex = 1;
                    buggyYes++;

                } else if ("NO".equals(
                        buggy
                )) {

                    classIndex = 0;
                    buggyNo++;

                } else {

                    throw new IllegalStateException(
                            "Unexpected BUGGY in dataset "
                                    + name
                                    + " at line "
                                    + lineNumber
                    );
                }

                instanceValues[
                        PREDICTORS.size()
                        ] =
                        classIndex;

                DenseInstance instance =
                        new DenseInstance(
                                1.0,
                                instanceValues
                        );

                instances.add(
                        instance
                );

                metadata.add(
                        new RowMetadata(
                                datasetRowIndex,
                                project,
                                classPath,
                                releaseIndex,
                                buggy,
                                Map.copyOf(
                                        predictorValues
                                )
                        )
                );

                datasetRowIndex++;
            }

            if (instances.numInstances()
                    != expectedRows) {

                throw new IllegalStateException(
                        "Dataset "
                                + name
                                + " row count mismatch. Expected "
                                + expectedRows
                                + ", found "
                                + instances.numInstances()
                );
            }

            if (buggyYes
                    != expectedBuggyYes) {

                throw new IllegalStateException(
                        "Dataset "
                                + name
                                + " BUGGY=YES mismatch. Expected "
                                + expectedBuggyYes
                                + ", found "
                                + buggyYes
                );
            }

            int expectedBuggyNo =
                    expectedRows
                            - expectedBuggyYes;

            if (buggyNo
                    != expectedBuggyNo) {

                throw new IllegalStateException(
                        "Dataset "
                                + name
                                + " BUGGY=NO mismatch. Expected "
                                + expectedBuggyNo
                                + ", found "
                                + buggyNo
                );
            }

            if (instances.numAttributes()
                    != PREDICTORS.size()
                    + 1) {

                throw new IllegalStateException(
                        "Unexpected Weka attribute count for dataset "
                                + name
                );
            }

            if (instances.attribute(
                    "NSmells"
            ) == null) {

                throw new IllegalStateException(
                        "NSmells is missing from model predictors."
                );
            }

            return new LoadedDataset(
                    name,
                    instances,
                    List.copyOf(
                            metadata
                    )
            );
        }
    }

    private static void validateDatasetRelations(
            LoadedDataset a,
            LoadedDataset bPlus,
            LoadedDataset b,
            LoadedDataset c
    ) {

        Map<String, RowMetadata> aByKey =
                metadataByKey(
                        a
                );

        Map<String, RowMetadata> bPlusByKey =
                metadataByKey(
                        bPlus
                );

        Map<String, RowMetadata> bByKey =
                metadataByKey(
                        b
                );

        Map<String, RowMetadata> cByKey =
                metadataByKey(
                        c
                );

        if (!bPlusByKey.keySet()
                .equals(
                        bByKey.keySet()
                )) {

            throw new IllegalStateException(
                    "B and B+ do not contain the same observations."
            );
        }

        Set<String> intersection =
                new HashSet<>(
                        bPlusByKey.keySet()
                );

        intersection.retainAll(
                cByKey.keySet()
        );

        if (!intersection.isEmpty()) {
            throw new IllegalStateException(
                    "B+ and C are not disjoint."
            );
        }

        Set<String> union =
                new HashSet<>(
                        bPlusByKey.keySet()
                );

        union.addAll(
                cByKey.keySet()
        );

        if (!union.equals(
                aByKey.keySet()
        )) {

            throw new IllegalStateException(
                    "B+ union C does not reconstruct A."
            );
        }

        for (String key
                : bPlusByKey.keySet()) {

            RowMetadata plus =
                    bPlusByKey.get(
                            key
                    );

            RowMetadata manipulated =
                    bByKey.get(
                            key
                    );

            if (!plus.referenceBuggy()
                    .equals(
                            manipulated.referenceBuggy()
                    )) {

                throw new IllegalStateException(
                        "B/B+ BUGGY mismatch for "
                                + key
                );
            }

            for (String predictor
                    : PREDICTORS) {

                double plusValue =
                        plus.predictorValues()
                                .get(
                                        predictor
                                );

                double manipulatedValue =
                        manipulated.predictorValues()
                                .get(
                                        predictor
                                );

                if ("NSmells".equals(
                        predictor
                )) {

                    if (!(plusValue > 0.0)
                            || manipulatedValue
                            != 0.0) {

                        throw new IllegalStateException(
                                "Unexpected B/B+ NSmells relation for "
                                        + key
                        );
                    }

                } else if (Double.compare(
                        plusValue,
                        manipulatedValue
                ) != 0) {

                    throw new IllegalStateException(
                            "B/B+ differ outside NSmells for "
                                    + key
                                    + ", predictor "
                                    + predictor
                    );
                }
            }
        }
    }

    private static DatasetPrediction predict(
            RandomForest classifier,
            LoadedDataset dataset
    ) throws Exception {

        int positiveIndex =
                dataset.instances()
                        .classAttribute()
                        .indexOfValue(
                                "YES"
                        );

        int negativeIndex =
                dataset.instances()
                        .classAttribute()
                        .indexOfValue(
                                "NO"
                        );

        if (positiveIndex < 0
                || negativeIndex < 0) {

            throw new IllegalStateException(
                    "BUGGY class does not contain YES/NO."
            );
        }

        List<PredictionRow> rows =
                new ArrayList<>(
                        dataset.instances()
                                .numInstances()
                );

        int referenceYes = 0;
        int referenceNo = 0;
        int predictedYes = 0;
        int predictedNo = 0;
        double probabilityYesSum = 0.0;

        for (int rowIndex = 0;
             rowIndex
                     < dataset.instances()
                     .numInstances();
             rowIndex++) {

            Instance source =
                    dataset.instances()
                            .instance(
                                    rowIndex
                            );

            RowMetadata metadata =
                    dataset.metadata()
                            .get(
                                    rowIndex
                            );

            /*
             * Prediction is performed with the class value hidden.
             * The historical BUGGY label is retained only in metadata.
             */
            Instance predictionInstance =
                    (Instance) source.copy();

            predictionInstance.setDataset(
                    dataset.instances()
            );

            predictionInstance.setClassMissing();

            double[] distribution =
                    classifier
                            .distributionForInstance(
                                    predictionInstance
                            );

            if (distribution.length != 2) {
                throw new IllegalStateException(
                        "Unexpected probability-vector length in dataset "
                                + dataset.name()
                );
            }

            double probabilityYes =
                    distribution[positiveIndex];

            double probabilityNo =
                    distribution[negativeIndex];

            validateProbability(
                    dataset.name(),
                    metadata,
                    probabilityNo,
                    probabilityYes
            );

            int predictedIndex =
                    Utils.maxIndex(
                            distribution
                    );

            String predicted =
                    dataset.instances()
                            .classAttribute()
                            .value(
                                    predictedIndex
                            );

            if ("YES".equals(
                    metadata.referenceBuggy()
            )) {
                referenceYes++;
            } else {
                referenceNo++;
            }

            if ("YES".equals(
                    predicted
            )) {
                predictedYes++;
            } else if ("NO".equals(
                    predicted
            )) {
                predictedNo++;
            } else {
                throw new IllegalStateException(
                        "Unexpected predicted class."
                );
            }

            probabilityYesSum +=
                    probabilityYes;

            rows.add(
                    new PredictionRow(
                            dataset.name(),
                            rowIndex,
                            metadata.project(),
                            metadata.classPath(),
                            metadata.releaseIndex(),
                            metadata.referenceBuggy(),
                            predicted,
                            probabilityNo,
                            probabilityYes,
                            metadata.predictorValues()
                                    .get(
                                            "NSmells"
                                    )
                    )
            );
        }

        DatasetSummary summary =
                new DatasetSummary(
                        dataset.name(),
                        rows.size(),
                        referenceYes,
                        referenceNo,
                        predictedYes,
                        predictedNo,
                        probabilityYesSum
                                / rows.size()
                );

        return new DatasetPrediction(
                dataset.name(),
                List.copyOf(
                        rows
                ),
                summary
        );
    }

    private static void validateProbability(
            String dataset,
            RowMetadata metadata,
            double probabilityNo,
            double probabilityYes
    ) {

        if (!Double.isFinite(
                probabilityNo
        )
                || !Double.isFinite(
                probabilityYes
        )
                || probabilityNo < 0.0
                || probabilityNo > 1.0
                || probabilityYes < 0.0
                || probabilityYes > 1.0) {

            throw new IllegalStateException(
                    "Invalid probability for dataset "
                            + dataset
                            + ", observation "
                            + observationKey(
                            metadata.releaseIndex(),
                            metadata.classPath()
                    )
            );
        }

        if (Math.abs(
                probabilityNo
                        + probabilityYes
                        - 1.0
        ) > 1.0e-9) {

            throw new IllegalStateException(
                    "Probability sum differs from 1 for dataset "
                            + dataset
            );
        }
    }

    private static void validatePredictionRelations(
            DatasetPrediction a,
            DatasetPrediction bPlus,
            DatasetPrediction b,
            DatasetPrediction c
    ) {

        Map<String, PredictionRow> aByKey =
                predictionsByKey(
                        a
                );

        Map<String, PredictionRow> bPlusByKey =
                predictionsByKey(
                        bPlus
                );

        Map<String, PredictionRow> bByKey =
                predictionsByKey(
                        b
                );

        Map<String, PredictionRow> cByKey =
                predictionsByKey(
                        c
                );

        /*
         * B+ and C are unchanged subsets of A, therefore predictions from the
         * same trained model must be exactly identical to their predictions
         * when reached through A.
         */
        for (Map.Entry<String, PredictionRow> entry
                : bPlusByKey.entrySet()) {

            assertSamePrediction(
                    "A/B+",
                    aByKey.get(
                            entry.getKey()
                    ),
                    entry.getValue()
            );
        }

        for (Map.Entry<String, PredictionRow> entry
                : cByKey.entrySet()) {

            assertSamePrediction(
                    "A/C",
                    aByKey.get(
                            entry.getKey()
                    ),
                    entry.getValue()
            );
        }

        if (!bPlusByKey.keySet()
                .equals(
                        bByKey.keySet()
                )) {

            throw new IllegalStateException(
                    "B/B+ prediction keys differ."
            );
        }

        if (a.summary()
                .predictedYes()
                != bPlus.summary()
                .predictedYes()
                + c.summary()
                .predictedYes()) {

            throw new IllegalStateException(
                    "Predicted YES on A does not equal B+ + C."
            );
        }

        if (a.summary()
                .predictedNo()
                != bPlus.summary()
                .predictedNo()
                + c.summary()
                .predictedNo()) {

            throw new IllegalStateException(
                    "Predicted NO on A does not equal B+ + C."
            );
        }
    }

    private static void assertSamePrediction(
            String relation,
            PredictionRow left,
            PredictionRow right
    ) {

        if (left == null
                || right == null) {

            throw new IllegalStateException(
                    relation
                            + " prediction mapping missing."
            );
        }

        if (!left.predicted()
                .equals(
                        right.predicted()
                )) {

            throw new IllegalStateException(
                    relation
                            + " predicted class mismatch for "
                            + observationKey(
                            right.releaseIndex(),
                            right.classPath()
                    )
            );
        }

        if (Double.compare(
                left.probabilityYes(),
                right.probabilityYes()
        ) != 0
                || Double.compare(
                left.probabilityNo(),
                right.probabilityNo()
        ) != 0) {

            throw new IllegalStateException(
                    relation
                            + " probability mismatch for "
                            + observationKey(
                            right.releaseIndex(),
                            right.classPath()
                    )
            );
        }
    }

    private static void validateRunResult(
            RunResult result
    ) {

        int expectedPredictionRows =
                EXPECTED_A_ROWS
                        + EXPECTED_B_PLUS_ROWS
                        + EXPECTED_B_ROWS
                        + EXPECTED_C_ROWS;

        if (result.predictions()
                .size()
                != expectedPredictionRows) {

            throw new IllegalStateException(
                    "Total prediction row mismatch. Expected "
                            + expectedPredictionRows
                            + ", found "
                            + result.predictions()
                            .size()
            );
        }

        if (result.summaries()
                .size()
                != 4) {

            throw new IllegalStateException(
                    "Expected four dataset summaries."
            );
        }

        Map<String, DatasetSummary> byName =
                new HashMap<>();

        for (DatasetSummary summary
                : result.summaries()) {

            if (byName.put(
                    summary.dataset(),
                    summary
            ) != null) {

                throw new IllegalStateException(
                        "Duplicate dataset summary: "
                                + summary.dataset()
                );
            }

            if (summary.rows()
                    != summary.referenceYes()
                    + summary.referenceNo()) {

                throw new IllegalStateException(
                        "Reference count mismatch for "
                                + summary.dataset()
                );
            }

            if (summary.rows()
                    != summary.predictedYes()
                    + summary.predictedNo()) {

                throw new IllegalStateException(
                        "Prediction count mismatch for "
                                + summary.dataset()
                );
            }

            if (!Double.isFinite(
                    summary.meanProbabilityYes()
            )
                    || summary.meanProbabilityYes()
                    < 0.0
                    || summary.meanProbabilityYes()
                    > 1.0) {

                throw new IllegalStateException(
                        "Invalid mean P(YES) for "
                                + summary.dataset()
                );
            }
        }

        requireRows(
                byName,
                "A",
                EXPECTED_A_ROWS
        );

        requireRows(
                byName,
                "B+",
                EXPECTED_B_PLUS_ROWS
        );

        requireRows(
                byName,
                "B",
                EXPECTED_B_ROWS
        );

        requireRows(
                byName,
                "C",
                EXPECTED_C_ROWS
        );
    }

    private static void requireRows(
            Map<String, DatasetSummary> byName,
            String dataset,
            int expected
    ) {

        DatasetSummary summary =
                byName.get(
                        dataset
                );

        if (summary == null
                || summary.rows()
                != expected) {

            throw new IllegalStateException(
                    "Unexpected rows for dataset "
                            + dataset
            );
        }
    }

    private static Map<String, RowMetadata> metadataByKey(
            LoadedDataset dataset
    ) {

        Map<String, RowMetadata> result =
                new LinkedHashMap<>();

        for (RowMetadata metadata
                : dataset.metadata()) {

            String key =
                    observationKey(
                            metadata.releaseIndex(),
                            metadata.classPath()
                    );

            if (result.put(
                    key,
                    metadata
            ) != null) {

                throw new IllegalStateException(
                        "Duplicate metadata key in dataset "
                                + dataset.name()
                );
            }
        }

        return result;
    }

    private static Map<String, PredictionRow> predictionsByKey(
            DatasetPrediction prediction
    ) {

        Map<String, PredictionRow> result =
                new LinkedHashMap<>();

        for (PredictionRow row
                : prediction.rows()) {

            String key =
                    observationKey(
                            row.releaseIndex(),
                            row.classPath()
                    );

            if (result.put(
                    key,
                    row
            ) != null) {

                throw new IllegalStateException(
                        "Duplicate prediction key in dataset "
                                + prediction.dataset()
                );
            }
        }

        return result;
    }

    private static String observationKey(
            int releaseIndex,
            String classPath
    ) {

        return releaseIndex
                + "|"
                + classPath;
    }

    private static String classifierOptions(
            RandomForest classifier
    ) {

        return Utils.joinOptions(
                classifier.getOptions()
        ).trim();
    }

    private static void writeOutputs(
            Path repository,
            RunResult result
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

        Path predictionsCsv =
                directory.resolve(
                        "what_if_predictions.csv"
                );

        Path summaryCsv =
                directory.resolve(
                        "what_if_prediction_summary.csv"
                );

        Path validationTxt =
                directory.resolve(
                        "what_if_validation.txt"
                );

        writePredictions(
                predictionsCsv,
                result.predictions()
        );

        writeSummary(
                summaryCsv,
                result.summaries()
        );

        writeValidation(
                validationTxt,
                predictionsCsv,
                summaryCsv,
                result
        );
    }

    private static void writePredictions(
            Path output,
            List<PredictionRow> rows
    ) throws IOException {

        List<String> lines =
                new ArrayList<>(
                        rows.size()
                                + 1
                );

        lines.add(
                "Dataset,DatasetRowIndex,Project,Class,ReleaseIndex,"
                        + "ReferenceBUGGY,PredictedBUGGY,ProbabilityNO,"
                        + "ProbabilityYES,NSmells"
        );

        for (PredictionRow row
                : rows) {

            lines.add(
                    escapeCsv(
                            row.dataset()
                    )
                            + ","
                            + row.datasetRowIndex()
                            + ","
                            + escapeCsv(
                            row.project()
                    )
                            + ","
                            + escapeCsv(
                            row.classPath()
                    )
                            + ","
                            + row.releaseIndex()
                            + ","
                            + row.referenceBuggy()
                            + ","
                            + row.predicted()
                            + ","
                            + decimal(
                            row.probabilityNo()
                    )
                            + ","
                            + decimal(
                            row.probabilityYes()
                    )
                            + ","
                            + decimal(
                            row.nSmells()
                    )
            );
        }

        Files.write(
                output,
                lines,
                StandardCharsets.UTF_8
        );
    }

    private static void writeSummary(
            Path output,
            List<DatasetSummary> summaries
    ) throws IOException {

        List<String> lines =
                new ArrayList<>();

        lines.add(
                "Dataset,Rows,ReferenceYES,ReferenceNO,"
                        + "PredictedYES,PredictedNO,MeanProbabilityYES"
        );

        for (DatasetSummary summary
                : summaries) {

            lines.add(
                    escapeCsv(
                            summary.dataset()
                    )
                            + ","
                            + summary.rows()
                            + ","
                            + summary.referenceYes()
                            + ","
                            + summary.referenceNo()
                            + ","
                            + summary.predictedYes()
                            + ","
                            + summary.predictedNo()
                            + ","
                            + decimal(
                            summary.meanProbabilityYes()
                    )
            );
        }

        Files.write(
                output,
                lines,
                StandardCharsets.UTF_8
        );
    }

    private static void writeValidation(
            Path output,
            Path predictionsCsv,
            Path summaryCsv,
            RunResult result
    ) throws IOException {

        List<String> lines =
                new ArrayList<>();

        lines.add(
                "===== OPENJPA M3 WHAT-IF VALIDATION ====="
        );

        lines.add(
                "BClassifier            : RandomForest"
        );

        lines.add(
                "Training dataset       : A"
        );

        lines.add(
                "Training rows          : 12836"
        );

        lines.add(
                "Predictors             : 18"
        );

        lines.add(
                "NSmells predictor      : included"
        );

        lines.add(
                "Feature Selection      : False"
        );

        lines.add(
                "Balancing              : False"
        );

        lines.add(
                "RandomForest seed      : "
                        + result.randomForestSeed()
        );

        lines.add(
                "Model options          : "
                        + result.classifierOptions()
        );

        lines.add(
                "Prediction target seen : False"
        );

        lines.add(
                "Prediction CSV         : "
                        + predictionsCsv
        );

        lines.add(
                "Summary CSV            : "
                        + summaryCsv
        );

        lines.add("");

        for (DatasetSummary summary
                : result.summaries()) {

            lines.add(
                    String.format(
                            Locale.ROOT,
                            "%-2s | rows=%5d | ref YES/NO=%4d/%5d | "
                                    + "pred YES/NO=%4d/%5d | mean P(YES)=%.6f",
                            summary.dataset(),
                            summary.rows(),
                            summary.referenceYes(),
                            summary.referenceNo(),
                            summary.predictedYes(),
                            summary.predictedNo(),
                            summary.meanProbabilityYes()
                    )
            );
        }

        lines.add("");

        lines.add(
                "Total prediction rows      : "
                        + result.predictions()
                        .size()
        );

        lines.add(
                "A = B+ union C predictions : PASSED"
        );

        lines.add(
                "A/B+ unchanged prediction  : PASSED"
        );

        lines.add(
                "A/C unchanged prediction   : PASSED"
        );

        lines.add(
                "B/B+ observation alignment : PASSED"
        );

        lines.add(
                "B/B+ only NSmells differs  : PASSED"
        );

        lines.add(
                "Probability validation     : PASSED"
        );

        lines.add(
                "ValidationPassed=True"
        );

        lines.add(
                "=========================================="
        );

        Files.write(
                output,
                lines,
                StandardCharsets.UTF_8
        );
    }

    private static void printSummary(
            RunResult result
    ) {

        System.out.println(
                "===== OPENJPA M3 WHAT-IF RUNNER ====="
        );

        System.out.println(
                "BClassifier           : RandomForest"
        );

        System.out.println(
                "Training              : A (12836 rows)"
        );

        System.out.println(
                "Predictors            : 18"
        );

        System.out.println(
                "Feature Selection     : False"
        );

        System.out.println(
                "Balancing             : False"
        );

        System.out.println(
                "RandomForest seed     : "
                        + result.randomForestSeed()
        );

        System.out.println("");

        for (DatasetSummary summary
                : result.summaries()) {

            System.out.printf(
                    Locale.ROOT,
                    "%-2s rows=%5d | ref YES=%4d | pred YES=%4d | mean P(YES)=%.6f%n",
                    summary.dataset(),
                    summary.rows(),
                    summary.referenceYes(),
                    summary.predictedYes(),
                    summary.meanProbabilityYes()
            );
        }

        System.out.println("");

        System.out.println(
                "Prediction rows       : "
                        + result.predictions()
                        .size()
        );

        System.out.println(
                "A/B+/C consistency    : PASSED"
        );

        System.out.println(
                "B/B+ alignment        : PASSED"
        );

        System.out.println(
                "ValidationPassed      : True"
        );

        System.out.println(
                "======================================="
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
            String value,
            String column,
            String dataset,
            int lineNumber
    ) {

        try {
            return Integer.parseInt(
                    value
            );

        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "Invalid "
                            + column
                            + " in dataset "
                            + dataset
                            + " at line "
                            + lineNumber,
                    exception
            );
        }
    }

    private static double parseDouble(
            String value,
            String column,
            String dataset,
            int lineNumber
    ) {

        final double parsed;

        try {
            parsed =
                    Double.parseDouble(
                            value
                    );

        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "Invalid "
                            + column
                            + " in dataset "
                            + dataset
                            + " at line "
                            + lineNumber,
                    exception
            );
        }

        if (!Double.isFinite(
                parsed
        )) {

            throw new IllegalStateException(
                    "Non-finite "
                            + column
                            + " in dataset "
                            + dataset
                            + " at line "
                            + lineNumber
            );
        }

        return parsed;
    }

    private static String sanitizeRelationName(
            String value
    ) {

        return value.replace(
                "+",
                "PLUS"
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

    public record RunResult(
            int randomForestSeed,
            String classifierOptions,
            List<PredictionRow> predictions,
            List<DatasetSummary> summaries
    ) {
    }

    public record DatasetSummary(
            String dataset,
            int rows,
            int referenceYes,
            int referenceNo,
            int predictedYes,
            int predictedNo,
            double meanProbabilityYes
    ) {
    }

    public record PredictionRow(
            String dataset,
            int datasetRowIndex,
            String project,
            String classPath,
            int releaseIndex,
            String referenceBuggy,
            String predicted,
            double probabilityNo,
            double probabilityYes,
            double nSmells
    ) {
    }

    private record LoadedDataset(
            String name,
            Instances instances,
            List<RowMetadata> metadata
    ) {
    }

    private record RowMetadata(
            int datasetRowIndex,
            String project,
            String classPath,
            int releaseIndex,
            String referenceBuggy,
            Map<String, Double> predictorValues
    ) {
    }

    private record DatasetPrediction(
            String dataset,
            List<PredictionRow> rows,
            DatasetSummary summary
    ) {
    }
}
