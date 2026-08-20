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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.OptionHandler;
import weka.core.Utils;

/**
 * Milestone 2 experiment runner for the validated 10-fold pipeline.
 *
 * <p>Two execution modes are supported:</p>
 *
 * <ul>
 *   <li>ONE_REP: repetition 1 of the validated FULL 10-fold plan. This is a
 *       validation run for the real 10-fold path and is not used as a final
 *       experimental result.</li>
 *   <li>FULL: all 10 repetitions x 10 folds required by the milestone.</li>
 * </ul>
 *
 * <p>No full prediction CSV is persisted. OOF predictions are kept only in
 * memory for one repetition at a time, used to calculate the five metrics,
 * and then discarded. This avoids producing a very large intermediate file
 * while retaining exact OOF metric semantics.</p>
 */
public final class M2ExperimentRunner {

    private static final int EXPECTED_ROWS = 12_836;
    private static final int EXPECTED_BUGGY_YES = 2_010;
    private static final int EXPECTED_BUGGY_NO = 10_826;

    private static final int CONFIGURATION_COUNT = 4;
    private static final int CLASSIFIER_COUNT = 3;
    private static final int FOLDS = 10;

    private static final double LOC_BUDGET_FRACTION = 0.20;

    private static final Path RESULT_DIRECTORY =
            Path.of("isw2", "results", "m2", "full");

    private M2ExperimentRunner() {
        // Utility class.
    }

    public enum RunMode {

        ONE_REP(
                "one_rep",
                1
        ),

        FULL(
                "full",
                10
        );

        private final String suffix;
        private final int repetitions;

        RunMode(
                String suffix,
                int repetitions
        ) {
            this.suffix = suffix;
            this.repetitions = repetitions;
        }

        public String suffix() {
            return suffix;
        }

        public int repetitions() {
            return repetitions;
        }

        public static RunMode parse(
                String value
        ) {

            if (value == null
                    || value.isBlank()) {

                return ONE_REP;
            }

            return switch (
                    value.trim()
                            .toUpperCase(
                                    Locale.ROOT
                            )
            ) {
                case "ONE_REP", "ONEREP", "SMOKE" ->
                        ONE_REP;

                case "FULL" ->
                        FULL;

                default ->
                        throw new IllegalArgumentException(
                                "Unknown run mode '"
                                        + value
                                        + "'. Expected ONE_REP or FULL."
                        );
            };
        }
    }

    public enum Model {

        RANDOM_FOREST("RandomForest"),
        NAIVE_BAYES("NaiveBayes"),
        IBK("IBk");

        private final String displayName;

        Model(
                String displayName
        ) {
            this.displayName =
                    displayName;
        }

        public String displayName() {
            return displayName;
        }
    }

    public static ExperimentResult run(
            Path repository,
            RunMode mode
    ) throws Exception {

        Path normalizedRepository =
                repository
                        .toAbsolutePath()
                        .normalize();

        M2DatasetLoader.LoadedDataset loaded =
                M2DatasetLoader.load(
                        normalizedRepository
                );

        M2FoldPlanner.FoldPlan fullPlan =
                M2FoldPlanner.createPlan(
                        loaded,
                        M2FoldPlanner.Mode.FULL
                );

        int repetitionsToRun =
                mode.repetitions();

        if (repetitionsToRun
                > fullPlan.repetitions().size()) {

            throw new IllegalStateException(
                    "Requested more repetitions than available in FULL plan."
            );
        }

        int expectedPreprocessingRuns =
                repetitionsToRun
                        * FOLDS
                        * CONFIGURATION_COUNT;

        int expectedModelTrainings =
                expectedPreprocessingRuns
                        * CLASSIFIER_COUNT;

        int expectedMetricRows =
                repetitionsToRun
                        * CONFIGURATION_COUNT
                        * CLASSIFIER_COUNT;

        List<MetricRow> metrics =
                new ArrayList<>(
                        expectedMetricRows
                );

        List<PreprocessingAuditRow> preprocessingAudit =
                new ArrayList<>(
                        expectedPreprocessingRuns
                );

        List<ModelAuditRow> modelAudit =
                new ArrayList<>(
                        expectedModelTrainings
                );

        long startedAt =
                System.nanoTime();

        int completedTrainings = 0;

        System.out.println(
                "===== OPENJPA M2 EXPERIMENT ====="
        );

        System.out.println(
                "Mode                  : "
                        + mode
        );

        System.out.println(
                "Repetitions           : "
                        + repetitionsToRun
        );

        System.out.println(
                "Folds/repetition      : "
                        + FOLDS
        );

        System.out.println(
                "Configurations        : "
                        + CONFIGURATION_COUNT
        );

        System.out.println(
                "Classifiers           : "
                        + CLASSIFIER_COUNT
        );

        System.out.println(
                "Expected preprocess   : "
                        + expectedPreprocessingRuns
        );

        System.out.println(
                "Expected trainings    : "
                        + expectedModelTrainings
        );

        System.out.println(
                "Expected metric rows  : "
                        + expectedMetricRows
        );

        System.out.println(
                "FULL fold fingerprint : "
                        + fullPlan.fingerprint()
        );

        System.out.println("");

        for (int repetitionIndex = 0;
             repetitionIndex < repetitionsToRun;
             repetitionIndex++) {

            M2FoldPlanner.RepetitionPlan repetitionPlan =
                    fullPlan.repetitions()
                            .get(
                                    repetitionIndex
                            );

            int repetition =
                    repetitionPlan.repetition();

            Map<ExperimentKey, PredictionAccumulator> accumulators =
                    createAccumulators(
                            repetition
                    );

            System.out.println(
                    "----- Repetition "
                            + repetition
                            + " / "
                            + repetitionsToRun
                            + " | seed="
                            + repetitionPlan.seed()
                            + " -----"
            );

            for (M2FoldPlanner.FoldSplit split
                    : repetitionPlan.folds()) {

                M2FoldPlanner.MaterializedFold materialized =
                        M2FoldPlanner.materialize(
                                loaded,
                                split
                        );

                Map<M2Preprocessor.Configuration, M2Preprocessor.PreprocessedFold>
                        processedByConfiguration =
                        new LinkedHashMap<>();

                for (M2Preprocessor.Configuration configuration
                        : M2Preprocessor.Configuration.values()) {

                    M2Preprocessor.PreprocessedFold preprocessed =
                            M2Preprocessor.apply(
                                    materialized,
                                    split,
                                    configuration
                            );

                    processedByConfiguration.put(
                            configuration,
                            preprocessed
                    );

                    preprocessingAudit.add(
                            preprocessingAuditRow(
                                    preprocessed
                            )
                    );
                }

                validateConfigurationPairing(
                        split,
                        processedByConfiguration
                );

                for (Map.Entry<M2Preprocessor.Configuration,
                        M2Preprocessor.PreprocessedFold> entry
                        : processedByConfiguration.entrySet()) {

                    M2Preprocessor.Configuration configuration =
                            entry.getKey();

                    M2Preprocessor.PreprocessedFold preprocessed =
                            entry.getValue();

                    for (Model model
                            : Model.values()) {

                        Classifier classifier =
                                createClassifier(
                                        model,
                                        repetition,
                                        split.fold()
                                );

                        long modelStarted =
                                System.nanoTime();

                        classifier.buildClassifier(
                                preprocessed.training()
                        );

                        int positiveIndex =
                                preprocessed.test()
                                        .classAttribute()
                                        .indexOfValue(
                                                "YES"
                                        );

                        int negativeIndex =
                                preprocessed.test()
                                        .classAttribute()
                                        .indexOfValue(
                                                "NO"
                                        );

                        if (positiveIndex < 0
                                || negativeIndex < 0) {

                            throw new IllegalStateException(
                                    "BUGGY values YES/NO unavailable."
                            );
                        }

                        ExperimentKey experimentKey =
                                new ExperimentKey(
                                        configuration.id(),
                                        model.displayName(),
                                        repetition
                                );

                        PredictionAccumulator accumulator =
                                accumulators.get(
                                        experimentKey
                                );

                        if (accumulator == null) {
                            throw new IllegalStateException(
                                    "Missing accumulator for "
                                            + experimentKey
                            );
                        }

                        int predictedYes = 0;
                        int predictedNo = 0;

                        for (int testRow = 0;
                             testRow
                                     < preprocessed.test()
                                     .numInstances();
                             testRow++) {

                            Instance instance =
                                    preprocessed.test()
                                            .instance(
                                                    testRow
                                            );

                            M2DatasetLoader.RowMetadata metadata =
                                    preprocessed.testMetadata()
                                            .get(
                                                    testRow
                                            );

                            double[] distribution =
                                    classifier
                                            .distributionForInstance(
                                                    instance
                                            );

                            validateDistribution(
                                    distribution,
                                    positiveIndex,
                                    negativeIndex,
                                    experimentKey,
                                    split,
                                    metadata
                            );

                            int predictedIndex =
                                    Utils.maxIndex(
                                            distribution
                                    );

                            String predicted =
                                    preprocessed.test()
                                            .classAttribute()
                                            .value(
                                                    predictedIndex
                                            );

                            String actual =
                                    metadata.buggy()
                                            ? "YES"
                                            : "NO";

                            if (!actual.equals(
                                    preprocessed.test()
                                            .classAttribute()
                                            .value(
                                                    (int) instance.classValue()
                                            )
                            )) {

                                throw new IllegalStateException(
                                        "Actual label mismatch for originalIndex "
                                                + metadata.originalIndex()
                                );
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
                                        "Unexpected predicted class: "
                                                + predicted
                                );
                            }

                            accumulator.add(
                                    new Prediction(
                                            metadata.originalIndex(),
                                            actual,
                                            predicted,
                                            distribution[positiveIndex],
                                            metadata.loc()
                                    )
                            );
                        }

                        long modelElapsedNanos =
                                System.nanoTime()
                                        - modelStarted;

                        modelAudit.add(
                                new ModelAuditRow(
                                        configuration.id(),
                                        model.displayName(),
                                        repetition,
                                        split.fold(),
                                        preprocessed.training()
                                                .numInstances(),
                                        preprocessed.test()
                                                .numInstances(),
                                        preprocessed.predictorsAfter(),
                                        model == Model.RANDOM_FOREST
                                                ? classifierSeed(
                                                repetition,
                                                split.fold()
                                        )
                                                : 0,
                                        predictedYes,
                                        predictedNo,
                                        classifierOptions(
                                                classifier
                                        ),
                                        Duration.ofNanos(
                                                modelElapsedNanos
                                        ).toMillis()
                                )
                        );

                        completedTrainings++;
                    }
                }

                printProgress(
                        completedTrainings,
                        expectedModelTrainings,
                        repetition,
                        split.fold(),
                        startedAt
                );
            }

            for (Map.Entry<ExperimentKey, PredictionAccumulator> entry
                    : accumulators.entrySet()) {

                entry.getValue()
                        .validateComplete(
                                entry.getKey()
                        );

                metrics.add(
                        calculateMetrics(
                                entry.getKey(),
                                entry.getValue()
                                        .predictions()
                        )
                );
            }

            validateMetricRowsForRepetition(
                    metrics,
                    repetition
            );

            /*
             * Persist a checkpoint after each completed repetition.
             * FULL can be long; these files preserve validated completed work
             * even if a later repetition is interrupted.
             */
            writeCheckpoint(
                    normalizedRepository,
                    mode,
                    fullPlan.fingerprint(),
                    metrics,
                    preprocessingAudit,
                    modelAudit,
                    repetition,
                    false
            );

            System.out.println(
                    "Repetition "
                            + repetition
                            + " validated: 12 metric rows, "
                            + EXPECTED_ROWS
                            + " OOF predictions per experiment."
            );

            System.out.println("");
        }

        validateFinalResult(
                mode,
                metrics,
                preprocessingAudit,
                modelAudit,
                fullPlan.fingerprint()
        );

        writeCheckpoint(
                normalizedRepository,
                mode,
                fullPlan.fingerprint(),
                metrics,
                preprocessingAudit,
                modelAudit,
                repetitionsToRun,
                true
        );

        long elapsed =
                System.nanoTime()
                        - startedAt;

        ExperimentResult result =
                new ExperimentResult(
                        mode,
                        fullPlan.fingerprint(),
                        List.copyOf(metrics),
                        List.copyOf(preprocessingAudit),
                        List.copyOf(modelAudit),
                        Duration.ofNanos(
                                elapsed
                        )
                );

        printFinalSummary(
                result
        );

        return result;
    }

    private static Map<ExperimentKey, PredictionAccumulator>
    createAccumulators(
            int repetition
    ) {

        Map<ExperimentKey, PredictionAccumulator> result =
                new LinkedHashMap<>();

        for (M2Preprocessor.Configuration configuration
                : M2Preprocessor.Configuration.values()) {

            for (Model model
                    : Model.values()) {

                ExperimentKey key =
                        new ExperimentKey(
                                configuration.id(),
                                model.displayName(),
                                repetition
                        );

                result.put(
                        key,
                        new PredictionAccumulator()
                );
            }
        }

        return result;
    }

    private static Classifier createClassifier(
            Model model,
            int repetition,
            int fold
    ) {

        return switch (model) {

            case RANDOM_FOREST -> {
                RandomForest classifier =
                        new RandomForest();

                classifier.setSeed(
                        classifierSeed(
                                repetition,
                                fold
                        )
                );

                yield classifier;
            }

            case NAIVE_BAYES ->
                    new NaiveBayes();

            case IBK ->
                    new IBk();
        };
    }

    private static int classifierSeed(
            int repetition,
            int fold
    ) {

        return repetition * 1000
                + fold;
    }

    private static String classifierOptions(
            Classifier classifier
    ) {

        if (classifier
                instanceof OptionHandler optionHandler) {

            return Utils.joinOptions(
                    optionHandler.getOptions()
            ).trim();
        }

        return "";
    }

    private static void validateDistribution(
            double[] distribution,
            int positiveIndex,
            int negativeIndex,
            ExperimentKey key,
            M2FoldPlanner.FoldSplit split,
            M2DatasetLoader.RowMetadata metadata
    ) {

        if (distribution.length != 2) {
            throw new IllegalStateException(
                    "Expected binary probability vector for "
                            + key
            );
        }

        double probabilityYes =
                distribution[positiveIndex];

        double probabilityNo =
                distribution[negativeIndex];

        if (!Double.isFinite(
                probabilityYes
        )
                || !Double.isFinite(
                probabilityNo
        )
                || probabilityYes < 0.0
                || probabilityYes > 1.0
                || probabilityNo < 0.0
                || probabilityNo > 1.0) {

            throw new IllegalStateException(
                    "Invalid probability for "
                            + key
                            + " repetition "
                            + split.repetition()
                            + " fold "
                            + split.fold()
                            + " originalIndex "
                            + metadata.originalIndex()
            );
        }

        if (Math.abs(
                probabilityYes
                        + probabilityNo
                        - 1.0
        ) > 1.0e-9) {

            throw new IllegalStateException(
                    "Probability vector does not sum to 1 for "
                            + key
            );
        }
    }

    private static PreprocessingAuditRow preprocessingAuditRow(
            M2Preprocessor.PreprocessedFold preprocessed
    ) {

        return new PreprocessingAuditRow(
                preprocessed.configuration()
                        .id(),
                preprocessed.configuration()
                        .featureSelection(),
                preprocessed.configuration()
                        .balancing(),
                preprocessed.repetition(),
                preprocessed.fold(),
                preprocessed.predictorsBefore(),
                preprocessed.predictorsAfter(),
                String.join(
                        "|",
                        preprocessed.selectedPredictors()
                ),
                preprocessed.trainBefore()
                        .yes(),
                preprocessed.trainBefore()
                        .no(),
                preprocessed.trainAfter()
                        .yes(),
                preprocessed.trainAfter()
                        .no(),
                preprocessed.testBefore()
                        .yes(),
                preprocessed.testBefore()
                        .no(),
                preprocessed.testAfter()
                        .yes(),
                preprocessed.testAfter()
                        .no(),
                preprocessed.smotePercentage(),
                preprocessed.smoteSeed()
        );
    }

    /**
     * Since FS is fitted before SMOTE, C2 and C4 must select the same subset
     * on the same original training fold. C1 and C3 must retain all 18
     * predictors. This is a strong implementation check on preprocessing
     * ordering.
     */
    private static void validateConfigurationPairing(
            M2FoldPlanner.FoldSplit split,
            Map<M2Preprocessor.Configuration,
                    M2Preprocessor.PreprocessedFold> processed
    ) {

        M2Preprocessor.PreprocessedFold c1 =
                processed.get(
                        M2Preprocessor.Configuration.C1_NO_FS_NO_BALANCING
                );

        M2Preprocessor.PreprocessedFold c2 =
                processed.get(
                        M2Preprocessor.Configuration.C2_FS_ONLY
                );

        M2Preprocessor.PreprocessedFold c3 =
                processed.get(
                        M2Preprocessor.Configuration.C3_SMOTE_ONLY
                );

        M2Preprocessor.PreprocessedFold c4 =
                processed.get(
                        M2Preprocessor.Configuration.C4_FS_SMOTE
                );

        if (c1 == null
                || c2 == null
                || c3 == null
                || c4 == null) {

            throw new IllegalStateException(
                    "Missing preprocessing configuration in repetition "
                            + split.repetition()
                            + ", fold "
                            + split.fold()
            );
        }

        if (c1.predictorsAfter() != 18
                || c3.predictorsAfter() != 18) {

            throw new IllegalStateException(
                    "C1/C3 predictor count changed unexpectedly."
            );
        }

        if (!c1.selectedPredictors()
                .equals(
                        c3.selectedPredictors()
                )) {

            throw new IllegalStateException(
                    "C1/C3 predictor sets differ."
            );
        }

        if (!c2.selectedPredictors()
                .equals(
                        c4.selectedPredictors()
                )) {

            throw new IllegalStateException(
                    "C2/C4 selected different predictors in repetition "
                            + split.repetition()
                            + ", fold "
                            + split.fold()
                            + ". FS must be determined before SMOTE."
            );
        }
    }

    private static MetricRow calculateMetrics(
            ExperimentKey key,
            List<Prediction> predictions
    ) {

        int tp = 0;
        int fp = 0;
        int tn = 0;
        int fn = 0;

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

            if (actualYes
                    && predictedYes) {

                tp++;

            } else if (!actualYes
                    && predictedYes) {

                fp++;

            } else if (!actualYes) {

                tn++;

            } else {

                fn++;
            }
        }

        double precision =
                divide(
                        tp,
                        tp + fp
                );

        double recall =
                divide(
                        tp,
                        tp + fn
                );

        double auc =
                auc(
                        predictions
                );

        double kappa =
                kappa(
                        tp,
                        fp,
                        tn,
                        fn
                );

        NpofResult npof =
                npofB20(
                        predictions
                );

        MetricRow result =
                new MetricRow(
                        key.configuration(),
                        key.classifier(),
                        key.repetition(),
                        predictions.size(),
                        tp,
                        fp,
                        tn,
                        fn,
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

        validateMetricRow(
                result
        );

        return result;
    }

    private static double divide(
            int numerator,
            int denominator
    ) {

        if (denominator == 0) {
            return 0.0;
        }

        return (double) numerator
                / denominator;
    }

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

        int positives = 0;
        int negatives = 0;

        for (Prediction prediction
                : sorted) {

            if ("YES".equals(
                    prediction.actual()
            )) {

                positives++;

            } else {

                negatives++;
            }
        }

        if (positives == 0
                || negatives == 0) {

            throw new IllegalStateException(
                    "AUC requires both classes."
            );
        }

        double positiveRankSum = 0.0;

        int start = 0;

        while (start
                < sorted.size()) {

            int end =
                    start + 1;

            double probability =
                    sorted.get(start)
                            .probabilityYes();

            while (end
                    < sorted.size()
                    && Double.compare(
                    sorted.get(end)
                            .probabilityYes(),
                    probability
            ) == 0) {

                end++;
            }

            double averageRank =
                    (
                            start
                                    + 1.0
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
                        (double) positives
                                * (positives + 1)
                                / 2.0
                );

        return numerator
                / (
                (double) positives
                        * negatives
        );
    }

    private static double kappa(
            int tp,
            int fp,
            int tn,
            int fn
    ) {

        int total =
                tp + fp + tn + fn;

        int actualYes =
                tp + fn;

        int actualNo =
                tn + fp;

        int predictedYes =
                tp + fp;

        int predictedNo =
                tn + fn;

        double observed =
                (double) (
                        tp + tn
                ) / total;

        double expected =
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
                        - expected;

        if (Math.abs(
                denominator
        ) < 1.0e-15) {

            return 0.0;
        }

        return (
                observed
                        - expected
        ) / denominator;
    }

    private static NpofResult npofB20(
            List<Prediction> predictions
    ) {

        List<Prediction> ranked =
                new ArrayList<>(
                        predictions
                );

        ranked.sort(
                Comparator
                        .comparingDouble(
                                M2ExperimentRunner::normalizedScore
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

        double budget =
                totalLoc
                        * LOC_BUDGET_FRACTION;

        double inspectedLoc = 0.0;
        int inspectedRows = 0;
        int buggyFound = 0;

        for (Prediction prediction
                : ranked) {

            if (inspectedLoc
                    >= budget) {

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

        return new NpofResult(
                (double) buggyFound
                        / totalBuggy,
                totalLoc,
                budget,
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

    private static void validateMetricRow(
            MetricRow row
    ) {

        if (row.oofPredictions()
                != EXPECTED_ROWS) {

            throw new IllegalStateException(
                    "Metric OOF count mismatch for "
                            + row.configuration()
                            + " / "
                            + row.classifier()
                            + " / rep "
                            + row.repetition()
            );
        }

        int total =
                row.tp()
                        + row.fp()
                        + row.tn()
                        + row.fn();

        if (total
                != EXPECTED_ROWS) {

            throw new IllegalStateException(
                    "Confusion total mismatch."
            );
        }

        validateMetricRange(
                "Precision",
                row.precision(),
                0.0,
                1.0
        );

        validateMetricRange(
                "Recall",
                row.recall(),
                0.0,
                1.0
        );

        validateMetricRange(
                "AUC",
                row.auc(),
                0.0,
                1.0
        );

        validateMetricRange(
                "Kappa",
                row.kappa(),
                -1.0,
                1.0
        );

        validateMetricRange(
                "NPofB20",
                row.npofB20(),
                0.0,
                1.0
        );

        if (row.totalBuggy()
                != EXPECTED_BUGGY_YES) {

            throw new IllegalStateException(
                    "NPofB20 total BUGGY mismatch."
            );
        }

        if (row.inspectedLoc()
                < row.budgetLoc()) {

            throw new IllegalStateException(
                    "NPofB20 budget was not reached."
            );
        }
    }

    private static void validateMetricRange(
            String name,
            double value,
            double minimum,
            double maximum
    ) {

        if (!Double.isFinite(
                value
        )
                || value < minimum
                || value > maximum) {

            throw new IllegalStateException(
                    name
                            + " out of range: "
                            + value
            );
        }
    }

    private static void validateMetricRowsForRepetition(
            List<MetricRow> allRows,
            int repetition
    ) {

        long count =
                allRows.stream()
                        .filter(
                                row ->
                                        row.repetition()
                                                == repetition
                        )
                        .count();

        if (count
                != CONFIGURATION_COUNT
                * CLASSIFIER_COUNT) {

            throw new IllegalStateException(
                    "Metric row count mismatch for repetition "
                            + repetition
                            + ": "
                            + count
            );
        }
    }

    private static void validateFinalResult(
            RunMode mode,
            List<MetricRow> metrics,
            List<PreprocessingAuditRow> preprocessingAudit,
            List<ModelAuditRow> modelAudit,
            String fingerprint
    ) {

        int expectedMetricRows =
                mode.repetitions()
                        * CONFIGURATION_COUNT
                        * CLASSIFIER_COUNT;

        int expectedPreprocessing =
                mode.repetitions()
                        * FOLDS
                        * CONFIGURATION_COUNT;

        int expectedModels =
                expectedPreprocessing
                        * CLASSIFIER_COUNT;

        if (metrics.size()
                != expectedMetricRows) {

            throw new IllegalStateException(
                    "Final metric-row mismatch. Expected "
                            + expectedMetricRows
                            + ", found "
                            + metrics.size()
            );
        }

        if (preprocessingAudit.size()
                != expectedPreprocessing) {

            throw new IllegalStateException(
                    "Final preprocessing-row mismatch. Expected "
                            + expectedPreprocessing
                            + ", found "
                            + preprocessingAudit.size()
            );
        }

        if (modelAudit.size()
                != expectedModels) {

            throw new IllegalStateException(
                    "Final model-run mismatch. Expected "
                            + expectedModels
                            + ", found "
                            + modelAudit.size()
            );
        }

        if (fingerprint == null
                || fingerprint.isBlank()) {

            throw new IllegalStateException(
                    "Missing FULL fold fingerprint."
            );
        }

        for (int repetition = 1;
             repetition
                     <= mode.repetitions();
             repetition++) {

            validateMetricRowsForRepetition(
                    metrics,
                    repetition
            );
        }
    }

    private static void writeCheckpoint(
            Path repository,
            RunMode mode,
            String fingerprint,
            List<MetricRow> metrics,
            List<PreprocessingAuditRow> preprocessingAudit,
            List<ModelAuditRow> modelAudit,
            int completedRepetitions,
            boolean finalOutput
    ) throws IOException {

        Path directory =
                repository.resolve(
                        RESULT_DIRECTORY
                );

        Files.createDirectories(
                directory
        );

        String suffix =
                mode.suffix();

        Path metricsCsv =
                directory.resolve(
                        "classifier_metrics_"
                                + suffix
                                + ".csv"
                );

        Path preprocessingCsv =
                directory.resolve(
                        "preprocessing_audit_"
                                + suffix
                                + ".csv"
                );

        Path modelCsv =
                directory.resolve(
                        "model_runs_"
                                + suffix
                                + ".csv"
                );

        Path validationTxt =
                directory.resolve(
                        "experiment_validation_"
                                + suffix
                                + ".txt"
                );

        writeMetricsCsv(
                metricsCsv,
                metrics
        );

        writePreprocessingCsv(
                preprocessingCsv,
                preprocessingAudit
        );

        writeModelCsv(
                modelCsv,
                modelAudit
        );

        writeValidationReport(
                validationTxt,
                mode,
                fingerprint,
                metrics,
                preprocessingAudit,
                modelAudit,
                completedRepetitions,
                finalOutput
        );
    }

    private static void writeMetricsCsv(
            Path output,
            List<MetricRow> rows
    ) throws IOException {

        List<String> lines =
                new ArrayList<>(
                        rows.size()
                                + 1
                );

        lines.add(
                "Configuration,Classifier,Repetition,OOFPredictions,"
                        + "TP,FP,TN,FN,Precision,Recall,AUC,Kappa,NPofB20,"
                        + "TotalLOC,BudgetLOC,InspectedLOC,InspectedRows,"
                        + "BuggyFound,TotalBuggy"
        );

        for (MetricRow row
                : rows) {

            lines.add(
                    row.configuration()
                            + ","
                            + row.classifier()
                            + ","
                            + row.repetition()
                            + ","
                            + row.oofPredictions()
                            + ","
                            + row.tp()
                            + ","
                            + row.fp()
                            + ","
                            + row.tn()
                            + ","
                            + row.fn()
                            + ","
                            + decimal(
                            row.precision()
                    )
                            + ","
                            + decimal(
                            row.recall()
                    )
                            + ","
                            + decimal(
                            row.auc()
                    )
                            + ","
                            + decimal(
                            row.kappa()
                    )
                            + ","
                            + decimal(
                            row.npofB20()
                    )
                            + ","
                            + decimal(
                            row.totalLoc()
                    )
                            + ","
                            + decimal(
                            row.budgetLoc()
                    )
                            + ","
                            + decimal(
                            row.inspectedLoc()
                    )
                            + ","
                            + row.inspectedRows()
                            + ","
                            + row.buggyFound()
                            + ","
                            + row.totalBuggy()
            );
        }

        Files.write(
                output,
                lines,
                StandardCharsets.UTF_8
        );
    }

    private static void writePreprocessingCsv(
            Path output,
            List<PreprocessingAuditRow> rows
    ) throws IOException {

        List<String> lines =
                new ArrayList<>(
                        rows.size()
                                + 1
                );

        lines.add(
                "Configuration,FeatureSelection,Balancing,Repetition,Fold,"
                        + "PredictorsBefore,PredictorsAfter,SelectedPredictors,"
                        + "TrainYESBefore,TrainNOBefore,TrainYESAfter,TrainNOAfter,"
                        + "TestYESBefore,TestNOBefore,TestYESAfter,TestNOAfter,"
                        + "SMOTEPercentage,SMOTESeed"
        );

        for (PreprocessingAuditRow row
                : rows) {

            lines.add(
                    row.configuration()
                            + ","
                            + row.featureSelection()
                            + ","
                            + row.balancing()
                            + ","
                            + row.repetition()
                            + ","
                            + row.fold()
                            + ","
                            + row.predictorsBefore()
                            + ","
                            + row.predictorsAfter()
                            + ","
                            + escapeCsv(
                            row.selectedPredictors()
                    )
                            + ","
                            + row.trainYesBefore()
                            + ","
                            + row.trainNoBefore()
                            + ","
                            + row.trainYesAfter()
                            + ","
                            + row.trainNoAfter()
                            + ","
                            + row.testYesBefore()
                            + ","
                            + row.testNoBefore()
                            + ","
                            + row.testYesAfter()
                            + ","
                            + row.testNoAfter()
                            + ","
                            + decimal(
                            row.smotePercentage()
                    )
                            + ","
                            + row.smoteSeed()
            );
        }

        Files.write(
                output,
                lines,
                StandardCharsets.UTF_8
        );
    }

    private static void writeModelCsv(
            Path output,
            List<ModelAuditRow> rows
    ) throws IOException {

        List<String> lines =
                new ArrayList<>(
                        rows.size()
                                + 1
                );

        lines.add(
                "Configuration,Classifier,Repetition,Fold,"
                        + "TrainingRows,TestRows,Predictors,ClassifierSeed,"
                        + "PredictedYES,PredictedNO,ElapsedMs,Options"
        );

        for (ModelAuditRow row
                : rows) {

            lines.add(
                    row.configuration()
                            + ","
                            + row.classifier()
                            + ","
                            + row.repetition()
                            + ","
                            + row.fold()
                            + ","
                            + row.trainingRows()
                            + ","
                            + row.testRows()
                            + ","
                            + row.predictors()
                            + ","
                            + row.classifierSeed()
                            + ","
                            + row.predictedYes()
                            + ","
                            + row.predictedNo()
                            + ","
                            + row.elapsedMs()
                            + ","
                            + escapeCsv(
                            row.options()
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
            Path output,
            RunMode mode,
            String fingerprint,
            List<MetricRow> metrics,
            List<PreprocessingAuditRow> preprocessingAudit,
            List<ModelAuditRow> modelAudit,
            int completedRepetitions,
            boolean finalOutput
    ) throws IOException {

        List<String> lines =
                new ArrayList<>();

        lines.add(
                "===== OPENJPA M2 EXPERIMENT VALIDATION ====="
        );

        lines.add(
                "Mode                  : "
                        + mode
        );

        lines.add(
                "Requested repetitions : "
                        + mode.repetitions()
        );

        lines.add(
                "Completed repetitions : "
                        + completedRepetitions
        );

        lines.add(
                "Folds/repetition      : 10"
        );

        lines.add(
                "Configurations        : 4"
        );

        lines.add(
                "Classifiers           : 3"
        );

        lines.add(
                "Metric rows           : "
                        + metrics.size()
        );

        lines.add(
                "Preprocessing rows    : "
                        + preprocessingAudit.size()
        );

        lines.add(
                "Model runs            : "
                        + modelAudit.size()
        );

        lines.add(
                "FULL fold fingerprint : "
                        + fingerprint
        );

        lines.add(
                "Checkpoint/final      : "
                        + (
                        finalOutput
                                ? "FINAL"
                                : "CHECKPOINT"
                )
        );

        lines.add("");

        for (int repetition = 1;
             repetition <= completedRepetitions;
             repetition++) {

            lines.add(
                    "Repetition "
                            + repetition
                            + ":"
            );

            for (MetricRow row
                    : metrics) {

                if (row.repetition()
                        != repetition) {

                    continue;
                }

                lines.add(
                        String.format(
                                Locale.ROOT,
                                "  %s | %-12s | P=%.6f | R=%.6f | "
                                        + "AUC=%.6f | Kappa=%.6f | NPofB20=%.6f",
                                row.configuration(),
                                row.classifier(),
                                row.precision(),
                                row.recall(),
                                row.auc(),
                                row.kappa(),
                                row.npofB20()
                        )
                );
            }
        }

        lines.add("");

        lines.add(
                "OOF rows per metric row     : 12836"
        );

        lines.add(
                "Same FULL fold plan          : True"
        );

        lines.add(
                "FS-before-SMOTE invariant    : PASSED"
        );

        lines.add(
                "Test oversampling            : False"
        );

        lines.add(
                "Metric ranges                : PASSED"
        );

        lines.add(
                "Confusion totals             : PASSED"
        );

        lines.add(
                "NPofB20 LOC budget           : PASSED"
        );

        lines.add(
                "ValidationPassed="
                        + (
                        finalOutput
                                && completedRepetitions
                                == mode.repetitions()
                                ? "True"
                                : "Partial"
                )
        );

        lines.add(
                "============================================="
        );

        Files.write(
                output,
                lines,
                StandardCharsets.UTF_8
        );
    }

    private static void printProgress(
            int completed,
            int total,
            int repetition,
            int fold,
            long startedAt
    ) {

        double fraction =
                (double) completed
                        / total;

        long elapsedNanos =
                System.nanoTime()
                        - startedAt;

        double elapsedSeconds =
                elapsedNanos
                        / 1_000_000_000.0;

        double estimatedTotalSeconds =
                fraction > 0.0
                        ? elapsedSeconds
                        / fraction
                        : 0.0;

        double remainingSeconds =
                Math.max(
                        0.0,
                        estimatedTotalSeconds
                                - elapsedSeconds
                );

        System.out.printf(
                Locale.ROOT,
                "Progress %4d/%4d (%6.2f%%) | rep=%2d fold=%2d | "
                        + "elapsed=%s | ETA=%s%n",
                completed,
                total,
                fraction * 100.0,
                repetition,
                fold,
                formatDuration(
                        elapsedSeconds
                ),
                formatDuration(
                        remainingSeconds
                )
        );
    }

    private static String formatDuration(
            double seconds
    ) {

        long rounded =
                Math.max(
                        0L,
                        Math.round(
                                seconds
                        )
                );

        long hours =
                rounded / 3600;

        long minutes =
                (
                        rounded % 3600
                ) / 60;

        long remainingSeconds =
                rounded % 60;

        return String.format(
                Locale.ROOT,
                "%02d:%02d:%02d",
                hours,
                minutes,
                remainingSeconds
        );
    }

    private static void printFinalSummary(
            ExperimentResult result
    ) {

        System.out.println("");
        System.out.println(
                "===== OPENJPA M2 EXPERIMENT COMPLETE ====="
        );

        System.out.println(
                "Mode                  : "
                        + result.mode()
        );

        System.out.println(
                "Metric rows           : "
                        + result.metrics().size()
        );

        System.out.println(
                "Preprocessing rows    : "
                        + result.preprocessingAudit().size()
        );

        System.out.println(
                "Model runs            : "
                        + result.modelAudit().size()
        );

        System.out.println(
                "FULL fold fingerprint : "
                        + result.foldPlanFingerprint()
        );

        System.out.println(
                "Elapsed               : "
                        + formatDuration(
                        result.elapsed()
                                .toMillis()
                                / 1000.0
                )
        );

        System.out.println(
                "ValidationPassed      : True"
        );

        System.out.println(
                "=========================================="
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
                || value.contains("\r")
                || value.contains("|")) {

            return "\""
                    + value.replace(
                    "\"",
                    "\"\""
            )
                    + "\"";
        }

        return value;
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

        RunMode mode =
                args.length >= 2
                        ? RunMode.parse(
                                args[1]
                        )
                        : RunMode.ONE_REP;

        run(
                repository,
                mode
        );
    }

    private static final class PredictionAccumulator {

        private final List<Prediction> predictions =
                new ArrayList<>(
                        EXPECTED_ROWS
                );

        private final boolean[] seen =
                new boolean[
                        EXPECTED_ROWS
                ];

        private int actualYes;
        private int actualNo;

        void add(
                Prediction prediction
        ) {

            int index =
                    prediction.originalIndex();

            if (index < 0
                    || index >= EXPECTED_ROWS) {

                throw new IllegalStateException(
                        "Prediction originalIndex out of range: "
                                + index
                );
            }

            if (seen[index]) {
                throw new IllegalStateException(
                        "Duplicate OOF prediction for originalIndex "
                                + index
                );
            }

            seen[index] = true;

            predictions.add(
                    prediction
            );

            if ("YES".equals(
                    prediction.actual()
            )) {

                actualYes++;

            } else if ("NO".equals(
                    prediction.actual()
            )) {

                actualNo++;

            } else {

                throw new IllegalStateException(
                        "Unexpected actual class: "
                                + prediction.actual()
                );
            }
        }

        void validateComplete(
                ExperimentKey key
        ) {

            if (predictions.size()
                    != EXPECTED_ROWS) {

                throw new IllegalStateException(
                        "OOF prediction count mismatch for "
                                + key
                                + ". Expected "
                                + EXPECTED_ROWS
                                + ", found "
                                + predictions.size()
                );
            }

            if (actualYes
                    != EXPECTED_BUGGY_YES
                    || actualNo
                    != EXPECTED_BUGGY_NO) {

                throw new IllegalStateException(
                        "OOF actual class mismatch for "
                                + key
                                + ". YES="
                                + actualYes
                                + ", NO="
                                + actualNo
                );
            }

            for (int index = 0;
                 index < seen.length;
                 index++) {

                if (!seen[index]) {
                    throw new IllegalStateException(
                            "Missing OOF prediction for "
                                    + key
                                    + ", originalIndex "
                                    + index
                    );
                }
            }
        }

        List<Prediction> predictions() {
            return List.copyOf(
                    predictions
            );
        }
    }

    public record MetricRow(
            String configuration,
            String classifier,
            int repetition,
            int oofPredictions,
            int tp,
            int fp,
            int tn,
            int fn,
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

    public record PreprocessingAuditRow(
            String configuration,
            boolean featureSelection,
            boolean balancing,
            int repetition,
            int fold,
            int predictorsBefore,
            int predictorsAfter,
            String selectedPredictors,
            int trainYesBefore,
            int trainNoBefore,
            int trainYesAfter,
            int trainNoAfter,
            int testYesBefore,
            int testNoBefore,
            int testYesAfter,
            int testNoAfter,
            double smotePercentage,
            int smoteSeed
    ) {
    }

    public record ModelAuditRow(
            String configuration,
            String classifier,
            int repetition,
            int fold,
            int trainingRows,
            int testRows,
            int predictors,
            int classifierSeed,
            int predictedYes,
            int predictedNo,
            String options,
            long elapsedMs
    ) {
    }

    public record ExperimentResult(
            RunMode mode,
            String foldPlanFingerprint,
            List<MetricRow> metrics,
            List<PreprocessingAuditRow> preprocessingAudit,
            List<ModelAuditRow> modelAudit,
            Duration elapsed
    ) {
    }

    private record ExperimentKey(
            String configuration,
            String classifier,
            int repetition
    ) {
    }

    private record Prediction(
            int originalIndex,
            String actual,
            String predicted,
            double probabilityYes,
            double loc
    ) {
    }

    private record NpofResult(
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
