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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;

/**
 * QUICK classifier runner for Milestone 2.
 *
 * <p>The runner trains RandomForest, NaiveBayes and IBk on the same QUICK
 * fold plan and on the same four preprocessing configurations. It stores
 * one out-of-fold prediction for every original observation, including
 * P(BUGGY=YES) and LOC, so the following metrics stage can compute
 * Precision, Recall, AUC, Kappa and NPofB20 without retraining models.</p>
 */
public final class M2ClassifierRunner {

    private static final int EXPECTED_ROWS = 12_836;
    private static final int EXPECTED_CONFIGURATIONS = 4;
    private static final int EXPECTED_CLASSIFIERS = 3;
    private static final int EXPECTED_QUICK_FOLDS = 2;
    private static final int EXPECTED_MODEL_TRAININGS =
            EXPECTED_CONFIGURATIONS
                    * EXPECTED_CLASSIFIERS
                    * EXPECTED_QUICK_FOLDS;

    private static final int EXPECTED_PREDICTIONS =
            EXPECTED_ROWS
                    * EXPECTED_CONFIGURATIONS
                    * EXPECTED_CLASSIFIERS;

    private static final Path RESULT_DIRECTORY =
            Path.of("isw2", "results", "m2", "classification");

    private M2ClassifierRunner() {
        // Utility class.
    }

    public enum Model {

        RANDOM_FOREST("RandomForest"),
        NAIVE_BAYES("NaiveBayes"),
        IBK("IBk");

        private final String displayName;

        Model(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }

    /**
     * Creates one classifier using Weka defaults, except for the
     * RandomForest seed which is fixed explicitly for reproducibility.
     *
     * <p>The RF seed depends only on repetition/fold, not on preprocessing
     * configuration, so the same split receives the same RF seed in C1-C4.</p>
     */
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

        return repetition * 1000 + fold;
    }

    public static QuickRunResult runQuick(
            Path repository
    ) throws Exception {

        M2DatasetLoader.LoadedDataset loaded =
                M2DatasetLoader.load(
                        repository
                );

        M2FoldPlanner.FoldPlan plan =
                M2FoldPlanner.createPlan(
                        loaded,
                        M2FoldPlanner.Mode.QUICK
                );

        if (plan.repetitions().size() != 1
                || plan.repetitions()
                .getFirst()
                .folds()
                .size() != EXPECTED_QUICK_FOLDS) {

            throw new IllegalStateException(
                    "Unexpected QUICK fold plan."
            );
        }

        List<PredictionRow> predictions =
                new ArrayList<>(
                        EXPECTED_PREDICTIONS
                );

        List<ModelRun> modelRuns =
                new ArrayList<>(
                        EXPECTED_MODEL_TRAININGS
                );

        for (M2FoldPlanner.RepetitionPlan repetition
                : plan.repetitions()) {

            for (M2FoldPlanner.FoldSplit split
                    : repetition.folds()) {

                M2FoldPlanner.MaterializedFold materialized =
                        M2FoldPlanner.materialize(
                                loaded,
                                split
                        );

                for (M2Preprocessor.Configuration configuration
                        : M2Preprocessor.Configuration.values()) {

                    M2Preprocessor.PreprocessedFold preprocessed =
                            M2Preprocessor.apply(
                                    materialized,
                                    split,
                                    configuration
                            );

                    for (Model model
                            : Model.values()) {

                        Classifier classifier =
                                createClassifier(
                                        model,
                                        split.repetition(),
                                        split.fold()
                                );

                        classifier.buildClassifier(
                                preprocessed.training()
                        );

                        String options =
                                classifierOptions(
                                        classifier
                                );

                        int positiveIndex =
                                preprocessed.test()
                                        .classAttribute()
                                        .indexOfValue("YES");

                        int negativeIndex =
                                preprocessed.test()
                                        .classAttribute()
                                        .indexOfValue("NO");

                        if (positiveIndex < 0
                                || negativeIndex < 0) {

                            throw new IllegalStateException(
                                    "BUGGY class values YES/NO missing."
                            );
                        }

                        int predictedYes = 0;
                        int predictedNo = 0;

                        for (int testRow = 0;
                             testRow < preprocessed.test().numInstances();
                             testRow++) {

                            Instance instance =
                                    preprocessed.test()
                                            .instance(testRow);

                            M2DatasetLoader.RowMetadata metadata =
                                    preprocessed.testMetadata()
                                            .get(testRow);

                            double[] distribution =
                                    classifier
                                            .distributionForInstance(
                                                    instance
                                            );

                            validateDistribution(
                                    distribution,
                                    preprocessed.test(),
                                    configuration,
                                    model,
                                    split,
                                    metadata
                            );

                            double probabilityYes =
                                    distribution[positiveIndex];

                            double probabilityNo =
                                    distribution[negativeIndex];

                            double predictedValue =
                                    classifier.classifyInstance(
                                            instance
                                    );

                            int predictedIndex =
                                    (int) predictedValue;

                            int argMax =
                                    Utils.maxIndex(
                                            distribution
                                    );

                            if (predictedIndex != argMax) {
                                throw new IllegalStateException(
                                        "classifyInstance/distribution mismatch for "
                                                + model.displayName()
                                                + " "
                                                + configuration.id()
                                                + " repetition "
                                                + split.repetition()
                                                + " fold "
                                                + split.fold()
                                                + " originalIndex "
                                                + metadata.originalIndex()
                                );
                            }

                            String predictedClass =
                                    preprocessed.test()
                                            .classAttribute()
                                            .value(
                                                    predictedIndex
                                            );

                            if ("YES".equals(predictedClass)) {
                                predictedYes++;
                            } else if ("NO".equals(predictedClass)) {
                                predictedNo++;
                            } else {
                                throw new IllegalStateException(
                                        "Unexpected predicted class: "
                                                + predictedClass
                                );
                            }

                            String actualClass =
                                    metadata.buggy()
                                            ? "YES"
                                            : "NO";

                            String instanceActual =
                                    preprocessed.test()
                                            .classAttribute()
                                            .value(
                                                    (int) instance.classValue()
                                            );

                            if (!actualClass.equals(instanceActual)) {
                                throw new IllegalStateException(
                                        "Metadata/test actual-class mismatch for originalIndex "
                                                + metadata.originalIndex()
                                );
                            }

                            predictions.add(
                                    new PredictionRow(
                                            configuration.id(),
                                            configuration.featureSelection(),
                                            configuration.balancing(),
                                            model.displayName(),
                                            split.repetition(),
                                            split.fold(),
                                            metadata.originalIndex(),
                                            metadata.releaseIndex(),
                                            metadata.classPath(),
                                            actualClass,
                                            predictedClass,
                                            probabilityNo,
                                            probabilityYes,
                                            metadata.loc(),
                                            preprocessed.predictorsAfter()
                                    )
                            );
                        }

                        if (predictedYes + predictedNo
                                != preprocessed.test()
                                .numInstances()) {

                            throw new IllegalStateException(
                                    "Prediction-count mismatch in "
                                            + model.displayName()
                                            + " "
                                            + configuration.id()
                            );
                        }

                        modelRuns.add(
                                new ModelRun(
                                        configuration.id(),
                                        model.displayName(),
                                        split.repetition(),
                                        split.fold(),
                                        preprocessed.training()
                                                .numInstances(),
                                        preprocessed.test()
                                                .numInstances(),
                                        preprocessed.predictorsAfter(),
                                        options,
                                        model == Model.RANDOM_FOREST
                                                ? classifierSeed(
                                                split.repetition(),
                                                split.fold()
                                        )
                                                : 0,
                                        predictedYes,
                                        predictedNo
                                )
                        );
                    }
                }
            }
        }

        QuickRunResult result =
                new QuickRunResult(
                        plan.fingerprint(),
                        List.copyOf(predictions),
                        List.copyOf(modelRuns)
                );

        validateQuickResult(
                result
        );

        return result;
    }

    private static void validateDistribution(
            double[] distribution,
            Instances test,
            M2Preprocessor.Configuration configuration,
            Model model,
            M2FoldPlanner.FoldSplit split,
            M2DatasetLoader.RowMetadata metadata
    ) {

        if (distribution.length
                != test.classAttribute().numValues()) {

            throw new IllegalStateException(
                    "Probability-vector length mismatch."
            );
        }

        double sum = 0.0;

        for (double probability
                : distribution) {

            if (!Double.isFinite(probability)
                    || probability < 0.0
                    || probability > 1.0) {

                throw new IllegalStateException(
                        "Invalid probability "
                                + probability
                                + " for "
                                + model.displayName()
                                + " "
                                + configuration.id()
                                + " repetition "
                                + split.repetition()
                                + " fold "
                                + split.fold()
                                + " originalIndex "
                                + metadata.originalIndex()
                );
            }

            sum += probability;
        }

        if (Math.abs(sum - 1.0) > 1.0e-9) {
            throw new IllegalStateException(
                    "Probability vector does not sum to 1. Sum="
                            + sum
            );
        }
    }

    private static String classifierOptions(
            Classifier classifier
    ) {

        if (classifier instanceof OptionHandler optionHandler) {
            return Utils.joinOptions(
                    optionHandler.getOptions()
            ).trim();
        }

        return "";
    }

    private static void validateQuickResult(
            QuickRunResult result
    ) {

        if (result.modelRuns().size()
                != EXPECTED_MODEL_TRAININGS) {

            throw new IllegalStateException(
                    "Model-training count mismatch. Expected "
                            + EXPECTED_MODEL_TRAININGS
                            + ", found "
                            + result.modelRuns().size()
            );
        }

        if (result.predictions().size()
                != EXPECTED_PREDICTIONS) {

            throw new IllegalStateException(
                    "Prediction count mismatch. Expected "
                            + EXPECTED_PREDICTIONS
                            + ", found "
                            + result.predictions().size()
            );
        }

        Map<String, List<PredictionRow>> byExperiment =
                new HashMap<>();

        for (PredictionRow row
                : result.predictions()) {

            String key =
                    row.configuration()
                            + "|"
                            + row.classifier();

            byExperiment.computeIfAbsent(
                    key,
                    ignored -> new ArrayList<>()
            ).add(row);
        }

        int expectedExperiments =
                EXPECTED_CONFIGURATIONS
                        * EXPECTED_CLASSIFIERS;

        if (byExperiment.size()
                != expectedExperiments) {

            throw new IllegalStateException(
                    "Classifier/configuration experiment count mismatch. Expected "
                            + expectedExperiments
                            + ", found "
                            + byExperiment.size()
            );
        }

        Set<Integer> referenceOriginalIndices =
                null;

        for (Map.Entry<String, List<PredictionRow>> entry
                : byExperiment.entrySet()) {

            List<PredictionRow> rows =
                    entry.getValue();

            if (rows.size() != EXPECTED_ROWS) {
                throw new IllegalStateException(
                        "OOF coverage mismatch for "
                                + entry.getKey()
                                + ". Expected "
                                + EXPECTED_ROWS
                                + ", found "
                                + rows.size()
                );
            }

            Set<Integer> originalIndices =
                    new HashSet<>();

            int actualYes = 0;
            int actualNo = 0;

            for (PredictionRow row : rows) {

                if (!originalIndices.add(
                        row.originalIndex()
                )) {

                    throw new IllegalStateException(
                            "Duplicate OOF prediction for "
                                    + entry.getKey()
                                    + " originalIndex "
                                    + row.originalIndex()
                    );
                }

                if ("YES".equals(row.actual())) {
                    actualYes++;
                } else if ("NO".equals(row.actual())) {
                    actualNo++;
                } else {
                    throw new IllegalStateException(
                            "Unexpected actual class."
                    );
                }

                if (!(row.loc() > 0.0)) {
                    throw new IllegalStateException(
                            "Non-positive LOC in prediction row."
                    );
                }

                if (!Double.isFinite(
                        row.probabilityYes()
                )
                        || !Double.isFinite(
                        row.probabilityNo()
                )) {

                    throw new IllegalStateException(
                            "Non-finite prediction probability."
                    );
                }
            }

            if (originalIndices.size()
                    != EXPECTED_ROWS) {

                throw new IllegalStateException(
                        "Unique OOF coverage mismatch for "
                                + entry.getKey()
                );
            }

            if (actualYes != 2_010
                    || actualNo != 10_826) {

                throw new IllegalStateException(
                        "Actual class distribution mismatch for "
                                + entry.getKey()
                                + ": YES="
                                + actualYes
                                + ", NO="
                                + actualNo
                );
            }

            if (referenceOriginalIndices == null) {
                referenceOriginalIndices =
                        Set.copyOf(originalIndices);

            } else if (!referenceOriginalIndices
                    .equals(originalIndices)) {

                throw new IllegalStateException(
                        "Different OOF observation sets across experiments."
                );
            }
        }

        if (result.foldPlanFingerprint() == null
                || result.foldPlanFingerprint().isBlank()) {

            throw new IllegalStateException(
                    "Missing fold-plan fingerprint."
            );
        }
    }

    private static void writeOutputs(
            Path repository,
            QuickRunResult result
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

        Path predictionCsv =
                directory.resolve(
                        "predictions_quick.csv"
                );

        Path modelCsv =
                directory.resolve(
                        "model_runs_quick.csv"
                );

        Path validationTxt =
                directory.resolve(
                        "classification_validation_quick.txt"
                );

        writePredictionCsv(
                predictionCsv,
                result.predictions()
        );

        writeModelCsv(
                modelCsv,
                result.modelRuns()
        );

        writeValidationReport(
                validationTxt,
                predictionCsv,
                modelCsv,
                result
        );
    }

    private static void writePredictionCsv(
            Path output,
            List<PredictionRow> rows
    ) throws IOException {

        List<String> lines =
                new ArrayList<>(
                        rows.size() + 1
                );

        lines.add(
                "Configuration,FeatureSelection,Balancing,Classifier,"
                        + "Repetition,Fold,OriginalIndex,ReleaseIndex,Class,"
                        + "Actual,Predicted,ProbabilityNO,ProbabilityYES,LOC,"
                        + "PredictorsAfter"
        );

        for (PredictionRow row : rows) {

            lines.add(
                    row.configuration()
                            + ","
                            + row.featureSelection()
                            + ","
                            + row.balancing()
                            + ","
                            + row.classifier()
                            + ","
                            + row.repetition()
                            + ","
                            + row.fold()
                            + ","
                            + row.originalIndex()
                            + ","
                            + row.releaseIndex()
                            + ","
                            + escapeCsv(
                            row.classPath()
                    )
                            + ","
                            + row.actual()
                            + ","
                            + row.predicted()
                            + ","
                            + formatDouble(
                            row.probabilityNo()
                    )
                            + ","
                            + formatDouble(
                            row.probabilityYes()
                    )
                            + ","
                            + formatDouble(
                            row.loc()
                    )
                            + ","
                            + row.predictorsAfter()
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
            List<ModelRun> rows
    ) throws IOException {

        List<String> lines =
                new ArrayList<>(
                        rows.size() + 1
                );

        lines.add(
                "Configuration,Classifier,Repetition,Fold,"
                        + "TrainingRows,TestRows,Predictors,"
                        + "ClassifierSeed,PredictedYES,PredictedNO,Options"
        );

        for (ModelRun row : rows) {

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
            Path predictionCsv,
            Path modelCsv,
            QuickRunResult result
    ) throws IOException {

        List<String> lines =
                new ArrayList<>();

        lines.add(
                "===== OPENJPA M2 CLASSIFICATION QUICK VALIDATION ====="
        );

        lines.add(
                "Mode                  : QUICK"
        );

        lines.add(
                "Repetitions           : 1"
        );

        lines.add(
                "Folds                 : 2"
        );

        lines.add(
                "Configurations        : "
                        + EXPECTED_CONFIGURATIONS
        );

        lines.add(
                "Classifiers           : "
                        + EXPECTED_CLASSIFIERS
        );

        lines.add(
                "Model trainings       : "
                        + result.modelRuns().size()
        );

        lines.add(
                "Predictions           : "
                        + result.predictions().size()
        );

        lines.add(
                "OOF rows per experiment: "
                        + EXPECTED_ROWS
        );

        lines.add(
                "Fold plan fingerprint : "
                        + result.foldPlanFingerprint()
        );

        lines.add(
                "Prediction CSV        : "
                        + predictionCsv
        );

        lines.add(
                "Model-run CSV         : "
                        + modelCsv
        );

        lines.add("");

        for (ModelRun run : result.modelRuns()) {

            lines.add(
                    String.format(
                            Locale.ROOT,
                            "%s | %s | rep=%d fold=%d | train=%d test=%d | "
                                    + "predictors=%d | seed=%d | predicted YES=%d NO=%d",
                            run.configuration(),
                            run.classifier(),
                            run.repetition(),
                            run.fold(),
                            run.trainingRows(),
                            run.testRows(),
                            run.predictors(),
                            run.classifierSeed(),
                            run.predictedYes(),
                            run.predictedNo()
                    )
            );

            lines.add(
                    "  options: "
                            + run.options()
            );
        }

        lines.add("");
        lines.add("Same fold plan for all experiments : True");
        lines.add("OOF coverage per experiment        : 12836/12836");
        lines.add("Duplicate OOF predictions          : 0");
        lines.add("Probability range                  : PASSED");
        lines.add("Probability sum                    : PASSED");
        lines.add("Actual metadata/test consistency   : PASSED");
        lines.add("LOC linked to every prediction     : True");
        lines.add("ValidationPassed=True");
        lines.add("===================================================");

        Files.write(
                output,
                lines,
                StandardCharsets.UTF_8
        );
    }

    private static String formatDouble(
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

    private static void printSummary(
            QuickRunResult result
    ) {

        System.out.println(
                "===== OPENJPA M2 CLASSIFICATION QUICK ====="
        );

        System.out.println(
                "Repetitions           : 1"
        );

        System.out.println(
                "Folds                 : 2"
        );

        System.out.println(
                "Configurations        : 4"
        );

        System.out.println(
                "Classifiers           : 3"
        );

        System.out.println(
                "Model trainings       : "
                        + result.modelRuns().size()
        );

        System.out.println(
                "Predictions           : "
                        + result.predictions().size()
        );

        System.out.println(
                "OOF rows/experiment   : "
                        + EXPECTED_ROWS
        );

        System.out.println(
                "Fold plan fingerprint : "
                        + result.foldPlanFingerprint()
        );

        System.out.println("");

        for (ModelRun run
                : result.modelRuns()) {

            System.out.printf(
                    Locale.ROOT,
                    "%s %-12s | fold=%d | train=%5d | test=%4d | "
                            + "predictors=%2d | seed=%4d | predicted YES=%4d NO=%4d%n",
                    run.configuration(),
                    run.classifier(),
                    run.fold(),
                    run.trainingRows(),
                    run.testRows(),
                    run.predictors(),
                    run.classifierSeed(),
                    run.predictedYes(),
                    run.predictedNo()
            );
        }

        System.out.println("");
        System.out.println(
                "Same fold plan        : True"
        );

        System.out.println(
                "OOF coverage          : PASSED"
        );

        System.out.println(
                "Probability validation: PASSED"
        );

        System.out.println(
                "ValidationPassed      : True"
        );

        System.out.println(
                "=========================================="
        );
    }

    public static void main(
            String[] args
    ) throws Exception {

        Path repository =
                args.length >= 1
                        ? Path.of(args[0])
                        : Path.of(".");

        QuickRunResult result =
                runQuick(
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

    public record PredictionRow(
            String configuration,
            boolean featureSelection,
            boolean balancing,
            String classifier,
            int repetition,
            int fold,
            int originalIndex,
            int releaseIndex,
            String classPath,
            String actual,
            String predicted,
            double probabilityNo,
            double probabilityYes,
            double loc,
            int predictorsAfter
    ) {
    }

    public record ModelRun(
            String configuration,
            String classifier,
            int repetition,
            int fold,
            int trainingRows,
            int testRows,
            int predictors,
            String options,
            int classifierSeed,
            int predictedYes,
            int predictedNo
    ) {
    }

    public record QuickRunResult(
            String foldPlanFingerprint,
            List<PredictionRow> predictions,
            List<ModelRun> modelRuns
    ) {
    }
}
