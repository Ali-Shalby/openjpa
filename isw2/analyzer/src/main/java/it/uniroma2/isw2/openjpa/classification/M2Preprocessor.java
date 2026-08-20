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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.instance.SMOTE;

/**
 * Milestone 2 preprocessing pipeline.
 *
 * <p>Feature Selection is fitted only on the training fold using
 * CfsSubsetEval + BestFirst. When balancing is enabled, SMOTE is applied
 * only to the already-transformed training fold. The test fold is never
 * oversampled and never participates in fitting the feature selector.</p>
 */
public final class M2Preprocessor {

    private static final String POSITIVE_CLASS = "YES";
    private static final String NEGATIVE_CLASS = "NO";

    private static final int EXPECTED_ORIGINAL_PREDICTORS = 18;

    private static final Path RESULT_DIRECTORY =
            Path.of("isw2", "results", "m2", "preprocessing");

    private M2Preprocessor() {
        // Utility class.
    }

    public enum Configuration {

        C1_NO_FS_NO_BALANCING(
                "C1",
                false,
                false
        ),

        C2_FS_ONLY(
                "C2",
                true,
                false
        ),

        C3_SMOTE_ONLY(
                "C3",
                false,
                true
        ),

        C4_FS_SMOTE(
                "C4",
                true,
                true
        );

        private final String id;
        private final boolean featureSelection;
        private final boolean balancing;

        Configuration(
                String id,
                boolean featureSelection,
                boolean balancing
        ) {
            this.id = id;
            this.featureSelection = featureSelection;
            this.balancing = balancing;
        }

        public String id() {
            return id;
        }

        public boolean featureSelection() {
            return featureSelection;
        }

        public boolean balancing() {
            return balancing;
        }
    }

    /**
     * Applies one preprocessing configuration to a materialized fold.
     *
     * @param fold materialized original train/test split
     * @param split split metadata
     * @param configuration preprocessing configuration
     * @return validated preprocessed fold
     * @throws Exception if Weka preprocessing fails
     */
    public static PreprocessedFold apply(
            M2FoldPlanner.MaterializedFold fold,
            M2FoldPlanner.FoldSplit split,
            Configuration configuration
    ) throws Exception {

        Instances originalTraining =
                new Instances(fold.training());

        Instances originalTest =
                new Instances(fold.test());

        ensureClassIndex(originalTraining);
        ensureClassIndex(originalTest);

        ClassCounts trainBefore =
                countClasses(originalTraining);

        ClassCounts testBefore =
                countClasses(originalTest);

        List<Double> originalTestClassVector =
                classVector(originalTest);

        Instances transformedTraining =
                new Instances(originalTraining);

        Instances transformedTest =
                new Instances(originalTest);

        List<String> selectedPredictors =
                predictorNames(
                        originalTraining
                );

        if (configuration.featureSelection()) {

            FeatureSelectionResult selection =
                    applyFeatureSelection(
                            originalTraining,
                            originalTest
                    );

            transformedTraining =
                    selection.training();

            transformedTest =
                    selection.test();

            selectedPredictors =
                    selection.selectedPredictors();
        }

        int predictorsAfterSelection =
                transformedTraining.numAttributes() - 1;

        if (predictorsAfterSelection <= 0) {
            throw new IllegalStateException(
                    "Feature selection produced zero predictors in repetition "
                            + split.repetition()
                            + ", fold "
                            + split.fold()
            );
        }

        if (transformedTraining.numAttributes()
                != transformedTest.numAttributes()) {

            throw new IllegalStateException(
                    "Train/test attribute mismatch after feature selection."
            );
        }

        ClassCounts trainAfterFeatureSelection =
                countClasses(
                        transformedTraining
                );

        ClassCounts testAfterFeatureSelection =
                countClasses(
                        transformedTest
                );

        if (!trainAfterFeatureSelection.equals(trainBefore)) {
            throw new IllegalStateException(
                    "Feature selection changed training class counts."
            );
        }

        if (!testAfterFeatureSelection.equals(testBefore)) {
            throw new IllegalStateException(
                    "Feature selection changed test class counts."
            );
        }

        double smotePercentage = 0.0;
        int smoteSeed = 0;

        Instances finalTraining =
                transformedTraining;

        if (configuration.balancing()) {

            if (trainAfterFeatureSelection.yes()
                    >= trainAfterFeatureSelection.no()) {

                throw new IllegalStateException(
                        "Expected YES to be the minority class before SMOTE."
                );
            }

            smotePercentage =
                    100.0
                            * (
                            trainAfterFeatureSelection.no()
                                    - trainAfterFeatureSelection.yes()
                    )
                            / trainAfterFeatureSelection.yes();

            smoteSeed =
                    split.repetition() * 1000
                            + split.fold();

            SMOTE smote =
                    new SMOTE();

            smote.setPercentage(
                    smotePercentage
            );

            smote.setRandomSeed(
                    smoteSeed
            );

            /*
             * Default class-value setting is 0, which tells Weka SMOTE to
             * auto-detect the non-empty minority class. We validate above
             * that YES is the minority class before applying the filter.
             */
            smote.setInputFormat(
                    transformedTraining
            );

            finalTraining =
                    Filter.useFilter(
                            transformedTraining,
                            smote
                    );

            ensureClassIndex(
                    finalTraining
            );
        }

        Instances finalTest =
                transformedTest;

        ensureClassIndex(
                finalTraining
        );

        ensureClassIndex(
                finalTest
        );

        ClassCounts trainAfter =
                countClasses(finalTraining);

        ClassCounts testAfter =
                countClasses(finalTest);

        validateResult(
                originalTraining,
                originalTest,
                finalTraining,
                finalTest,
                originalTestClassVector,
                trainBefore,
                testBefore,
                trainAfter,
                testAfter,
                configuration,
                selectedPredictors,
                split
        );

        return new PreprocessedFold(
                configuration,
                split.repetition(),
                split.fold(),
                finalTraining,
                finalTest,
                fold.testMetadata(),
                List.copyOf(selectedPredictors),
                EXPECTED_ORIGINAL_PREDICTORS,
                predictorsAfterSelection,
                trainBefore,
                trainAfter,
                testBefore,
                testAfter,
                smotePercentage,
                smoteSeed
        );
    }

    private static FeatureSelectionResult applyFeatureSelection(
            Instances training,
            Instances test
    ) throws Exception {

        AttributeSelection selector =
                new AttributeSelection();

        selector.setEvaluator(
                new CfsSubsetEval()
        );

        selector.setSearch(
                new BestFirst()
        );

        /*
         * The selector sees only the training fold.
         */
        selector.SelectAttributes(
                training
        );

        Instances reducedTraining =
                selector.reduceDimensionality(
                        training
                );

        /*
         * The already-fitted selector is reused for the test fold.
         * No fitting occurs on test data.
         */
        Instances reducedTest =
                selector.reduceDimensionality(
                        test
                );

        ensureClassIndex(
                reducedTraining
        );

        ensureClassIndex(
                reducedTest
        );

        int[] selectedIndices =
                selector.selectedAttributes();

        if (selectedIndices.length <= 1) {
            throw new IllegalStateException(
                    "CfsSubsetEval + BestFirst selected no predictors."
            );
        }

        List<String> selectedPredictors =
                predictorNames(
                        reducedTraining
                );

        return new FeatureSelectionResult(
                reducedTraining,
                reducedTest,
                List.copyOf(selectedPredictors),
                Arrays.copyOf(
                        selectedIndices,
                        selectedIndices.length
                )
        );
    }

    private static void validateResult(
            Instances originalTraining,
            Instances originalTest,
            Instances finalTraining,
            Instances finalTest,
            List<Double> originalTestClassVector,
            ClassCounts trainBefore,
            ClassCounts testBefore,
            ClassCounts trainAfter,
            ClassCounts testAfter,
            Configuration configuration,
            List<String> selectedPredictors,
            M2FoldPlanner.FoldSplit split
    ) {

        if (finalTest.numInstances()
                != originalTest.numInstances()) {

            throw new IllegalStateException(
                    "Test row count changed in "
                            + configuration.id()
                            + ", repetition "
                            + split.repetition()
                            + ", fold "
                            + split.fold()
            );
        }

        if (!testAfter.equals(testBefore)) {
            throw new IllegalStateException(
                    "Test class distribution changed in "
                            + configuration.id()
            );
        }

        if (!classVector(finalTest)
                .equals(originalTestClassVector)) {

            throw new IllegalStateException(
                    "Test class sequence changed in "
                            + configuration.id()
            );
        }

        if (configuration.featureSelection()) {

            if (selectedPredictors.size()
                    >= EXPECTED_ORIGINAL_PREDICTORS) {

                /*
                 * CFS is allowed to keep every predictor in principle, but in
                 * that case we still accept the result because selection was
                 * genuinely fitted on training. The check here therefore only
                 * protects impossible counts.
                 */
                if (selectedPredictors.size()
                        > EXPECTED_ORIGINAL_PREDICTORS) {

                    throw new IllegalStateException(
                            "Feature selection produced too many predictors."
                    );
                }
            }

        } else {

            if (selectedPredictors.size()
                    != EXPECTED_ORIGINAL_PREDICTORS) {

                throw new IllegalStateException(
                        "Predictor count changed with FS disabled."
                );
            }
        }

        if (finalTraining.numAttributes()
                != finalTest.numAttributes()) {

            throw new IllegalStateException(
                    "Final train/test attribute mismatch."
            );
        }

        if (configuration.balancing()) {

            if (trainAfter.no()
                    != trainBefore.no()) {

                throw new IllegalStateException(
                        "SMOTE changed the majority NO count."
                );
            }

            if (Math.abs(
                    trainAfter.yes()
                            - trainAfter.no()
            ) > 1) {

                throw new IllegalStateException(
                        "SMOTE did not reach approximately 1:1. YES="
                                + trainAfter.yes()
                                + ", NO="
                                + trainAfter.no()
                );
            }

            if (finalTraining.numInstances()
                    <= originalTraining.numInstances()) {

                throw new IllegalStateException(
                        "SMOTE did not increase training size."
                );
            }

        } else {

            if (finalTraining.numInstances()
                    != originalTraining.numInstances()) {

                throw new IllegalStateException(
                        "Training row count changed with balancing disabled."
                );
            }

            if (!trainAfter.equals(trainBefore)) {

                throw new IllegalStateException(
                        "Training class counts changed with balancing disabled."
                );
            }
        }

        if (finalTraining.classIndex()
                != finalTraining.numAttributes() - 1
                || finalTest.classIndex()
                != finalTest.numAttributes() - 1) {

            throw new IllegalStateException(
                    "BUGGY is not the final class attribute."
            );
        }

        if (!"BUGGY".equals(
                finalTraining.classAttribute().name()
        )
                || !"BUGGY".equals(
                finalTest.classAttribute().name()
        )) {

            throw new IllegalStateException(
                    "Unexpected final class attribute."
            );
        }

        if (containsMissingValue(finalTraining)
                || containsMissingValue(finalTest)) {

            throw new IllegalStateException(
                    "Preprocessing introduced missing values."
            );
        }
    }

    private static boolean containsMissingValue(
            Instances data
    ) {

        for (int row = 0;
             row < data.numInstances();
             row++) {

            if (data.instance(row)
                    .hasMissingValue()) {

                return true;
            }
        }

        return false;
    }

    private static ClassCounts countClasses(
            Instances data
    ) {

        ensureClassIndex(data);

        int yesIndex =
                data.classAttribute()
                        .indexOfValue(
                                POSITIVE_CLASS
                        );

        int noIndex =
                data.classAttribute()
                        .indexOfValue(
                                NEGATIVE_CLASS
                        );

        if (yesIndex < 0
                || noIndex < 0) {

            throw new IllegalStateException(
                    "BUGGY class does not contain YES and NO."
            );
        }

        int yes = 0;
        int no = 0;

        for (int row = 0;
             row < data.numInstances();
             row++) {

            Instance instance =
                    data.instance(row);

            int classValue =
                    (int) instance.classValue();

            if (classValue == yesIndex) {
                yes++;

            } else if (classValue == noIndex) {
                no++;

            } else {
                throw new IllegalStateException(
                        "Unexpected class value at row "
                                + row
                );
            }
        }

        return new ClassCounts(
                yes,
                no
        );
    }

    private static List<Double> classVector(
            Instances data
    ) {

        List<Double> values =
                new ArrayList<>(
                        data.numInstances()
                );

        for (int row = 0;
             row < data.numInstances();
             row++) {

            values.add(
                    data.instance(row)
                            .classValue()
            );
        }

        return List.copyOf(values);
    }

    private static List<String> predictorNames(
            Instances data
    ) {

        ensureClassIndex(data);

        List<String> names =
                new ArrayList<>();

        for (int attribute = 0;
             attribute < data.numAttributes();
             attribute++) {

            if (attribute != data.classIndex()) {
                names.add(
                        data.attribute(attribute)
                                .name()
                );
            }
        }

        return List.copyOf(names);
    }

    private static void ensureClassIndex(
            Instances data
    ) {

        if (data.classIndex() < 0) {
            data.setClassIndex(
                    data.numAttributes() - 1
            );
        }
    }

    private static void writeQuickOutputs(
            Path repository,
            List<PreprocessedFold> results
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

        Path csvOutput =
                directory.resolve(
                        "preprocessing_quick.csv"
                );

        Path validationOutput =
                directory.resolve(
                        "preprocessing_validation_quick.txt"
                );

        List<String> csv =
                new ArrayList<>();

        csv.add(
                "Configuration,FeatureSelection,Balancing,Repetition,Fold,"
                        + "PredictorsBefore,PredictorsAfter,SelectedPredictors,"
                        + "TrainRowsBefore,TrainYESBefore,TrainNOBefore,"
                        + "TrainRowsAfter,TrainYESAfter,TrainNOAfter,"
                        + "TestRowsBefore,TestYESBefore,TestNOBefore,"
                        + "TestRowsAfter,TestYESAfter,TestNOAfter,"
                        + "SMOTEPercentage,SMOTESeed"
        );

        for (PreprocessedFold result : results) {

            csv.add(
                    result.configuration().id()
                            + ","
                            + result.configuration()
                            .featureSelection()
                            + ","
                            + result.configuration()
                            .balancing()
                            + ","
                            + result.repetition()
                            + ","
                            + result.fold()
                            + ","
                            + result.predictorsBefore()
                            + ","
                            + result.predictorsAfter()
                            + ","
                            + escapeCsv(
                            String.join(
                                    "|",
                                    result.selectedPredictors()
                            )
                    )
                            + ","
                            + result.trainBefore().total()
                            + ","
                            + result.trainBefore().yes()
                            + ","
                            + result.trainBefore().no()
                            + ","
                            + result.trainAfter().total()
                            + ","
                            + result.trainAfter().yes()
                            + ","
                            + result.trainAfter().no()
                            + ","
                            + result.testBefore().total()
                            + ","
                            + result.testBefore().yes()
                            + ","
                            + result.testBefore().no()
                            + ","
                            + result.testAfter().total()
                            + ","
                            + result.testAfter().yes()
                            + ","
                            + result.testAfter().no()
                            + ","
                            + String.format(
                            Locale.ROOT,
                            "%.6f",
                            result.smotePercentage()
                    )
                            + ","
                            + result.smoteSeed()
            );
        }

        Files.write(
                csvOutput,
                csv,
                StandardCharsets.UTF_8
        );

        List<String> report =
                buildQuickValidationReport(
                        results,
                        csvOutput
                );

        Files.write(
                validationOutput,
                report,
                StandardCharsets.UTF_8
        );
    }

    private static List<String> buildQuickValidationReport(
            List<PreprocessedFold> results,
            Path csvOutput
    ) {

        List<String> lines =
                new ArrayList<>();

        lines.add(
                "===== OPENJPA M2 PREPROCESSING QUICK VALIDATION ====="
        );

        lines.add(
                "Configurations       : "
                        + Configuration.values().length
        );

        lines.add(
                "Folds                : 2"
        );

        lines.add(
                "Preprocessing runs   : "
                        + results.size()
        );

        lines.add(
                "Feature selection    : CfsSubsetEval + BestFirst"
        );

        lines.add(
                "Balancing            : SMOTE"
        );

        lines.add(
                "Order when combined  : FeatureSelection -> SMOTE"
        );

        lines.add(
                "SMOTE scope          : training only"
        );

        lines.add(
                "FS fit scope         : training only"
        );

        lines.add(
                "Test oversampling    : False"
        );

        lines.add(
                "CSV output           : "
                        + csvOutput
        );

        lines.add("");

        for (PreprocessedFold result : results) {

            lines.add(
                    String.format(
                            Locale.ROOT,
                            "%s | fold=%d | FS=%s | SMOTE=%s | predictors=%d->%d | "
                                    + "train %d/%d -> %d/%d | test %d/%d -> %d/%d | "
                                    + "SMOTE%%=%.6f | seed=%d",
                            result.configuration().id(),
                            result.fold(),
                            result.configuration()
                                    .featureSelection(),
                            result.configuration()
                                    .balancing(),
                            result.predictorsBefore(),
                            result.predictorsAfter(),
                            result.trainBefore().yes(),
                            result.trainBefore().no(),
                            result.trainAfter().yes(),
                            result.trainAfter().no(),
                            result.testBefore().yes(),
                            result.testBefore().no(),
                            result.testAfter().yes(),
                            result.testAfter().no(),
                            result.smotePercentage(),
                            result.smoteSeed()
                    )
            );

            lines.add(
                    "  selected: "
                            + String.join(
                            ", ",
                            result.selectedPredictors()
                    )
            );
        }

        lines.add("");
        lines.add("Test row count unchanged : True");
        lines.add("Test class labels unchanged: True");
        lines.add("SMOTE only on training    : True");
        lines.add("FS fitted only on training: True");
        lines.add("No missing values         : True");
        lines.add("ValidationPassed=True");
        lines.add("===================================================");

        return lines;
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

    private static void printQuickSummary(
            List<PreprocessedFold> results
    ) {

        System.out.println(
                "===== OPENJPA M2 PREPROCESSING QUICK ====="
        );

        System.out.println(
                "Configurations       : 4"
        );

        System.out.println(
                "Folds                : 2"
        );

        System.out.println(
                "Preprocessing runs   : "
                        + results.size()
        );

        System.out.println(
                "FS                   : CfsSubsetEval + BestFirst"
        );

        System.out.println(
                "Balancing            : SMOTE"
        );

        System.out.println(
                "Combined order       : FS -> SMOTE"
        );

        System.out.println("");

        for (PreprocessedFold result : results) {

            System.out.printf(
                    Locale.ROOT,
                    "%s fold=%d | predictors=%d->%d | train YES/NO=%d/%d -> %d/%d | "
                            + "test YES/NO=%d/%d -> %d/%d | SMOTE%%=%.3f | seed=%d%n",
                    result.configuration().id(),
                    result.fold(),
                    result.predictorsBefore(),
                    result.predictorsAfter(),
                    result.trainBefore().yes(),
                    result.trainBefore().no(),
                    result.trainAfter().yes(),
                    result.trainAfter().no(),
                    result.testBefore().yes(),
                    result.testBefore().no(),
                    result.testAfter().yes(),
                    result.testAfter().no(),
                    result.smotePercentage(),
                    result.smoteSeed()
            );

            if (result.configuration()
                    .featureSelection()) {

                System.out.println(
                        "  selected: "
                                + String.join(
                                ", ",
                                result.selectedPredictors()
                        )
                );
            }
        }

        System.out.println("");
        System.out.println(
                "Test oversampling    : False"
        );

        System.out.println(
                "FS fit scope         : training only"
        );

        System.out.println(
                "SMOTE scope          : training only"
        );

        System.out.println(
                "ValidationPassed     : True"
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

        M2DatasetLoader.LoadedDataset loaded =
                M2DatasetLoader.load(
                        repository
                );

        M2FoldPlanner.FoldPlan quickPlan =
                M2FoldPlanner.createPlan(
                        loaded,
                        M2FoldPlanner.Mode.QUICK
                );

        List<PreprocessedFold> results =
                new ArrayList<>();

        for (M2FoldPlanner.RepetitionPlan repetition
                : quickPlan.repetitions()) {

            for (M2FoldPlanner.FoldSplit split
                    : repetition.folds()) {

                M2FoldPlanner.MaterializedFold fold =
                        M2FoldPlanner.materialize(
                                loaded,
                                split
                        );

                for (Configuration configuration
                        : Configuration.values()) {

                    results.add(
                            apply(
                                    fold,
                                    split,
                                    configuration
                            )
                    );
                }
            }
        }

        int expectedRuns =
                quickPlan.mode().repetitions()
                        * quickPlan.mode().folds()
                        * Configuration.values().length;

        if (results.size() != expectedRuns) {
            throw new IllegalStateException(
                    "Unexpected preprocessing run count. Expected "
                            + expectedRuns
                            + ", found "
                            + results.size()
            );
        }

        writeQuickOutputs(
                repository,
                results
        );

        printQuickSummary(
                results
        );
    }

    public record ClassCounts(
            int yes,
            int no
    ) {
        public int total() {
            return yes + no;
        }
    }

    public record PreprocessedFold(
            Configuration configuration,
            int repetition,
            int fold,
            Instances training,
            Instances test,
            List<M2DatasetLoader.RowMetadata> testMetadata,
            List<String> selectedPredictors,
            int predictorsBefore,
            int predictorsAfter,
            ClassCounts trainBefore,
            ClassCounts trainAfter,
            ClassCounts testBefore,
            ClassCounts testAfter,
            double smotePercentage,
            int smoteSeed
    ) {
    }

    private record FeatureSelectionResult(
            Instances training,
            Instances test,
            List<String> selectedPredictors,
            int[] selectedIndices
    ) {
    }
}
