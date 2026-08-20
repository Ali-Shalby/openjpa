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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Deterministic stratified fold planner for Milestone 2.
 *
 * <p>The planner operates only on original Dataset A row indices and BUGGY
 * labels. It does not inspect predictor values and therefore cannot leak
 * feature information into the split. Each repetition uses a deterministic
 * seed. The resulting plan can be reused unchanged by every classifier and
 * preprocessing configuration.</p>
 */
public final class M2FoldPlanner {

    private static final int EXPECTED_ROWS = 12_836;
    private static final int EXPECTED_BUGGY_YES = 2_010;
    private static final int EXPECTED_BUGGY_NO = 10_826;

    private static final Path RESULT_DIRECTORY =
            Path.of("isw2", "results", "m2", "folds");

    private M2FoldPlanner() {
        // Utility class.
    }

    public enum Mode {
        QUICK(1, 2),
        FULL(10, 10);

        private final int repetitions;
        private final int folds;

        Mode(int repetitions, int folds) {
            this.repetitions = repetitions;
            this.folds = folds;
        }

        public int repetitions() {
            return repetitions;
        }

        public int folds() {
            return folds;
        }

        public static Mode parse(String value) {
            if (value == null || value.isBlank()) {
                return QUICK;
            }

            return switch (value.trim().toUpperCase(Locale.ROOT)) {
                case "QUICK" -> QUICK;
                case "FULL" -> FULL;
                default -> throw new IllegalArgumentException(
                        "Unknown mode '" + value
                                + "'. Expected QUICK or FULL."
                );
            };
        }
    }

    /**
     * Creates a deterministic stratified plan.
     *
     * @param loaded validated Dataset A loaded by {@link M2DatasetLoader}
     * @param mode QUICK or FULL
     * @return immutable fold plan
     */
    public static FoldPlan createPlan(
            M2DatasetLoader.LoadedDataset loaded,
            Mode mode
    ) {

        if (loaded.data().numInstances() != EXPECTED_ROWS) {
            throw new IllegalStateException(
                    "Unexpected dataset size: "
                            + loaded.data().numInstances()
            );
        }

        List<Integer> positiveIndices =
                new ArrayList<>(EXPECTED_BUGGY_YES);

        List<Integer> negativeIndices =
                new ArrayList<>(EXPECTED_BUGGY_NO);

        for (M2DatasetLoader.RowMetadata row
                : loaded.metadata()) {

            if (row.buggy()) {
                positiveIndices.add(row.originalIndex());
            } else {
                negativeIndices.add(row.originalIndex());
            }
        }

        if (positiveIndices.size() != EXPECTED_BUGGY_YES) {
            throw new IllegalStateException(
                    "Unexpected BUGGY=YES count: "
                            + positiveIndices.size()
            );
        }

        if (negativeIndices.size() != EXPECTED_BUGGY_NO) {
            throw new IllegalStateException(
                    "Unexpected BUGGY=NO count: "
                            + negativeIndices.size()
            );
        }

        List<RepetitionPlan> repetitions =
                new ArrayList<>(mode.repetitions());

        for (int repetition = 1;
             repetition <= mode.repetitions();
             repetition++) {

            int seed = repetition;

            repetitions.add(
                    createRepetition(
                            repetition,
                            seed,
                            mode.folds(),
                            positiveIndices,
                            negativeIndices
                    )
            );
        }

        FoldPlan plan =
                new FoldPlan(
                        mode,
                        List.copyOf(repetitions),
                        fingerprint(
                                mode,
                                repetitions
                        )
                );

        validatePlan(
                plan,
                loaded.metadata()
        );

        return plan;
    }

    private static RepetitionPlan createRepetition(
            int repetition,
            int seed,
            int foldCount,
            List<Integer> positiveIndices,
            List<Integer> negativeIndices
    ) {

        List<Integer> shuffledPositive =
                new ArrayList<>(positiveIndices);

        List<Integer> shuffledNegative =
                new ArrayList<>(negativeIndices);

        Random random =
                new Random(seed);

        Collections.shuffle(
                shuffledPositive,
                random
        );

        Collections.shuffle(
                shuffledNegative,
                random
        );

        List<List<Integer>> testBuckets =
                new ArrayList<>(foldCount);

        for (int fold = 0;
             fold < foldCount;
             fold++) {

            testBuckets.add(
                    new ArrayList<>()
            );
        }

        distributeRoundRobin(
                shuffledPositive,
                testBuckets
        );

        distributeRoundRobin(
                shuffledNegative,
                testBuckets
        );

        List<FoldSplit> splits =
                new ArrayList<>(foldCount);

        for (int fold = 0;
             fold < foldCount;
             fold++) {

            List<Integer> testIndices =
                    new ArrayList<>(
                            testBuckets.get(fold)
                    );

            Collections.shuffle(
                    testIndices,
                    new Random(
                            seed * 1009L + fold + 1L
                    )
            );

            boolean[] inTest =
                    new boolean[EXPECTED_ROWS];

            for (int index : testIndices) {
                inTest[index] = true;
            }

            List<Integer> trainingIndices =
                    new ArrayList<>(
                            EXPECTED_ROWS
                                    - testIndices.size()
                    );

            for (int index = 0;
                 index < EXPECTED_ROWS;
                 index++) {

                if (!inTest[index]) {
                    trainingIndices.add(index);
                }
            }

            splits.add(
                    new FoldSplit(
                            repetition,
                            seed,
                            fold + 1,
                            List.copyOf(trainingIndices),
                            List.copyOf(testIndices)
                    )
            );
        }

        return new RepetitionPlan(
                repetition,
                seed,
                List.copyOf(splits)
        );
    }

    private static void distributeRoundRobin(
            List<Integer> source,
            List<List<Integer>> buckets
    ) {

        for (int index = 0;
             index < source.size();
             index++) {

            buckets
                    .get(index % buckets.size())
                    .add(source.get(index));
        }
    }

    private static void validatePlan(
            FoldPlan plan,
            List<M2DatasetLoader.RowMetadata> metadata
    ) {

        if (plan.repetitions().size()
                != plan.mode().repetitions()) {

            throw new IllegalStateException(
                    "Repetition count mismatch."
            );
        }

        for (RepetitionPlan repetition
                : plan.repetitions()) {

            if (repetition.seed()
                    != repetition.repetition()) {

                throw new IllegalStateException(
                        "Seed policy mismatch at repetition "
                                + repetition.repetition()
                );
            }

            if (repetition.folds().size()
                    != plan.mode().folds()) {

                throw new IllegalStateException(
                        "Fold count mismatch at repetition "
                                + repetition.repetition()
                );
            }

            int[] testOccurrences =
                    new int[EXPECTED_ROWS];

            int testRowsAcrossFolds = 0;

            int minYes =
                    Integer.MAX_VALUE;

            int maxYes =
                    Integer.MIN_VALUE;

            int minNo =
                    Integer.MAX_VALUE;

            int maxNo =
                    Integer.MIN_VALUE;

            for (FoldSplit split
                    : repetition.folds()) {

                validateFold(
                        split,
                        metadata
                );

                testRowsAcrossFolds +=
                        split.testIndices().size();

                int yes = 0;
                int no = 0;

                for (int index
                        : split.testIndices()) {

                    testOccurrences[index]++;

                    if (metadata.get(index).buggy()) {
                        yes++;
                    } else {
                        no++;
                    }
                }

                minYes =
                        Math.min(minYes, yes);

                maxYes =
                        Math.max(maxYes, yes);

                minNo =
                        Math.min(minNo, no);

                maxNo =
                        Math.max(maxNo, no);
            }

            if (testRowsAcrossFolds
                    != EXPECTED_ROWS) {

                throw new IllegalStateException(
                        "Test coverage mismatch at repetition "
                                + repetition.repetition()
                                + ". Expected "
                                + EXPECTED_ROWS
                                + ", found "
                                + testRowsAcrossFolds
                );
            }

            for (int index = 0;
                 index < testOccurrences.length;
                 index++) {

                if (testOccurrences[index] != 1) {
                    throw new IllegalStateException(
                            "Observation "
                                    + index
                                    + " appears "
                                    + testOccurrences[index]
                                    + " times in test folds of repetition "
                                    + repetition.repetition()
                    );
                }
            }

            if (maxYes - minYes > 1) {
                throw new IllegalStateException(
                        "Positive stratification imbalance at repetition "
                                + repetition.repetition()
                                + ": min="
                                + minYes
                                + ", max="
                                + maxYes
                );
            }

            if (maxNo - minNo > 1) {
                throw new IllegalStateException(
                        "Negative stratification imbalance at repetition "
                                + repetition.repetition()
                                + ": min="
                                + minNo
                                + ", max="
                                + maxNo
                );
            }
        }
    }

    private static void validateFold(
            FoldSplit split,
            List<M2DatasetLoader.RowMetadata> metadata
    ) {

        Set<Integer> trainingSet =
                new HashSet<>(
                        split.trainingIndices()
                );

        Set<Integer> testSet =
                new HashSet<>(
                        split.testIndices()
                );

        if (trainingSet.size()
                != split.trainingIndices().size()) {

            throw new IllegalStateException(
                    "Duplicate training index in repetition "
                            + split.repetition()
                            + ", fold "
                            + split.fold()
            );
        }

        if (testSet.size()
                != split.testIndices().size()) {

            throw new IllegalStateException(
                    "Duplicate test index in repetition "
                            + split.repetition()
                            + ", fold "
                            + split.fold()
            );
        }

        for (int index : testSet) {
            if (trainingSet.contains(index)) {
                throw new IllegalStateException(
                        "Train/test overlap in repetition "
                                + split.repetition()
                                + ", fold "
                                + split.fold()
                                + ", row "
                                + index
                );
            }
        }

        if (trainingSet.size()
                + testSet.size()
                != EXPECTED_ROWS) {

            throw new IllegalStateException(
                    "Train/test partition size mismatch in repetition "
                            + split.repetition()
                            + ", fold "
                            + split.fold()
            );
        }

        validateIndices(
                trainingSet,
                metadata.size(),
                "training",
                split
        );

        validateIndices(
                testSet,
                metadata.size(),
                "test",
                split
        );

        int testYes = 0;
        int testNo = 0;

        for (int index : split.testIndices()) {
            if (metadata.get(index).buggy()) {
                testYes++;
            } else {
                testNo++;
            }
        }

        int trainYes =
                EXPECTED_BUGGY_YES - testYes;

        int trainNo =
                EXPECTED_BUGGY_NO - testNo;

        if (trainYes <= 0
                || trainNo <= 0
                || testYes <= 0
                || testNo <= 0) {

            throw new IllegalStateException(
                    "A fold is missing a class in repetition "
                            + split.repetition()
                            + ", fold "
                            + split.fold()
            );
        }
    }

    private static void validateIndices(
            Set<Integer> indices,
            int rowCount,
            String type,
            FoldSplit split
    ) {

        for (int index : indices) {
            if (index < 0
                    || index >= rowCount) {

                throw new IllegalStateException(
                        "Out-of-range "
                                + type
                                + " index "
                                + index
                                + " in repetition "
                                + split.repetition()
                                + ", fold "
                                + split.fold()
                );
            }
        }
    }

    /**
     * Materializes one train/test split from the original Weka dataset.
     *
     * <p>The returned instances are copies; subsequent preprocessing cannot
     * mutate the original Dataset A loaded in memory.</p>
     *
     * @param loaded loaded Dataset A
     * @param split split to materialize
     * @return train/test Weka instances
     */
    public static MaterializedFold materialize(
            M2DatasetLoader.LoadedDataset loaded,
            FoldSplit split
    ) {

        weka.core.Instances training =
                new weka.core.Instances(
                        loaded.data(),
                        split.trainingIndices().size()
                );

        for (int index
                : split.trainingIndices()) {

            training.add(
                    loaded.data()
                            .instance(index)
            );
        }

        weka.core.Instances test =
                new weka.core.Instances(
                        loaded.data(),
                        split.testIndices().size()
                );

        List<M2DatasetLoader.RowMetadata> testMetadata =
                new ArrayList<>(
                        split.testIndices().size()
                );

        for (int index
                : split.testIndices()) {

            test.add(
                    loaded.data()
                            .instance(index)
            );

            testMetadata.add(
                    loaded.metadata()
                            .get(index)
            );
        }

        training.setClassIndex(
                training.numAttributes() - 1
        );

        test.setClassIndex(
                test.numAttributes() - 1
        );

        return new MaterializedFold(
                training,
                test,
                List.copyOf(testMetadata)
        );
    }

    private static String fingerprint(
            Mode mode,
            List<RepetitionPlan> repetitions
    ) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            updateDigest(
                    digest,
                    mode.name()
            );

            for (RepetitionPlan repetition
                    : repetitions) {

                updateDigest(
                        digest,
                        Integer.toString(
                                repetition.repetition()
                        )
                );

                updateDigest(
                        digest,
                        Integer.toString(
                                repetition.seed()
                        )
                );

                for (FoldSplit split
                        : repetition.folds()) {

                    updateDigest(
                            digest,
                            Integer.toString(
                                    split.fold()
                            )
                    );

                    for (int index
                            : split.testIndices()) {

                        updateDigest(
                                digest,
                                Integer.toString(index)
                        );
                    }
                }
            }

            byte[] bytes =
                    digest.digest();

            StringBuilder builder =
                    new StringBuilder(
                            bytes.length * 2
                    );

            for (byte value : bytes) {
                builder.append(
                        String.format(
                                "%02x",
                                value
                        )
                );
            }

            return builder.toString();

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 unavailable.",
                    exception
            );
        }
    }

    private static void updateDigest(
            MessageDigest digest,
            String value
    ) {

        digest.update(
                value.getBytes(
                        StandardCharsets.UTF_8
                )
        );

        digest.update(
                (byte) 0
        );
    }

    private static void writeOutputs(
            Path repository,
            FoldPlan plan,
            List<M2DatasetLoader.RowMetadata> metadata
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

        String suffix =
                plan.mode()
                        .name()
                        .toLowerCase(Locale.ROOT);

        Path planCsv =
                directory.resolve(
                        "fold_plan_"
                                + suffix
                                + ".csv"
                );

        Path validationTxt =
                directory.resolve(
                        "fold_validation_"
                                + suffix
                                + ".txt"
                );

        List<String> csv =
                new ArrayList<>();

        csv.add(
                "Mode,Repetition,Seed,Fold,OriginalIndex,ReleaseIndex,Class,BUGGY"
        );

        for (RepetitionPlan repetition
                : plan.repetitions()) {

            for (FoldSplit split
                    : repetition.folds()) {

                for (int originalIndex
                        : split.testIndices()) {

                    M2DatasetLoader.RowMetadata row =
                            metadata.get(
                                    originalIndex
                            );

                    csv.add(
                            escapeCsv(plan.mode().name())
                                    + ","
                                    + repetition.repetition()
                                    + ","
                                    + repetition.seed()
                                    + ","
                                    + split.fold()
                                    + ","
                                    + originalIndex
                                    + ","
                                    + row.releaseIndex()
                                    + ","
                                    + escapeCsv(row.classPath())
                                    + ","
                                    + (row.buggy()
                                    ? "YES"
                                    : "NO")
                    );
                }
            }
        }

        Files.write(
                planCsv,
                csv,
                StandardCharsets.UTF_8
        );

        List<String> report =
                buildValidationReport(
                        plan,
                        metadata,
                        planCsv
                );

        Files.write(
                validationTxt,
                report,
                StandardCharsets.UTF_8
        );
    }

    private static List<String> buildValidationReport(
            FoldPlan plan,
            List<M2DatasetLoader.RowMetadata> metadata,
            Path planCsv
    ) {

        List<String> lines =
                new ArrayList<>();

        lines.add(
                "===== OPENJPA M2 FOLD PLAN VALIDATION ====="
        );

        lines.add(
                "Mode                 : "
                        + plan.mode()
        );

        lines.add(
                "Repetitions          : "
                        + plan.mode().repetitions()
        );

        lines.add(
                "Folds/repetition     : "
                        + plan.mode().folds()
        );

        lines.add(
                "Rows                 : "
                        + EXPECTED_ROWS
        );

        lines.add(
                "BUGGY=YES            : "
                        + EXPECTED_BUGGY_YES
        );

        lines.add(
                "BUGGY=NO             : "
                        + EXPECTED_BUGGY_NO
        );

        lines.add(
                "Fold plan fingerprint: "
                        + plan.fingerprint()
        );

        lines.add(
                "Plan CSV             : "
                        + planCsv
        );

        lines.add("");

        for (RepetitionPlan repetition
                : plan.repetitions()) {

            lines.add(
                    "Repetition "
                            + repetition.repetition()
                            + " | seed="
                            + repetition.seed()
            );

            for (FoldSplit split
                    : repetition.folds()) {

                int testYes = 0;
                int testNo = 0;

                for (int index
                        : split.testIndices()) {

                    if (metadata.get(index).buggy()) {
                        testYes++;
                    } else {
                        testNo++;
                    }
                }

                lines.add(
                        String.format(
                                Locale.ROOT,
                                "  Fold %2d | train=%5d | test=%4d | test YES=%3d | test NO=%4d",
                                split.fold(),
                                split.trainingIndices().size(),
                                split.testIndices().size(),
                                testYes,
                                testNo
                        )
                );
            }
        }

        lines.add("");
        lines.add("Train/test overlap       : 0");
        lines.add("Missing test observations: 0");
        lines.add("Duplicate test coverage  : 0");
        lines.add("Class stratification     : PASSED");
        lines.add("Seed policy              : repetition 1..N");
        lines.add("Reusable plan            : True");
        lines.add("ValidationPassed=True");
        lines.add("===========================================");

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

    private static void printSummary(
            FoldPlan plan,
            List<M2DatasetLoader.RowMetadata> metadata
    ) {

        System.out.println(
                "===== OPENJPA M2 FOLD PLANNER ====="
        );

        System.out.println(
                "Mode                 : "
                        + plan.mode()
        );

        System.out.println(
                "Repetitions          : "
                        + plan.mode().repetitions()
        );

        System.out.println(
                "Folds/repetition     : "
                        + plan.mode().folds()
        );

        System.out.println(
                "Rows/repetition      : "
                        + EXPECTED_ROWS
        );

        System.out.println(
                "Fold plan fingerprint: "
                        + plan.fingerprint()
        );

        System.out.println("");

        for (RepetitionPlan repetition
                : plan.repetitions()) {

            System.out.println(
                    "Repetition "
                            + repetition.repetition()
                            + " | seed="
                            + repetition.seed()
            );

            for (FoldSplit split
                    : repetition.folds()) {

                int yes = 0;
                int no = 0;

                for (int index
                        : split.testIndices()) {

                    if (metadata.get(index).buggy()) {
                        yes++;
                    } else {
                        no++;
                    }
                }

                System.out.printf(
                        Locale.ROOT,
                        "  Fold %2d | train=%5d | test=%4d | YES=%3d | NO=%4d%n",
                        split.fold(),
                        split.trainingIndices().size(),
                        split.testIndices().size(),
                        yes,
                        no
                );
            }
        }

        System.out.println("");
        System.out.println(
                "Train/test overlap       : 0"
        );

        System.out.println(
                "Missing test observations: 0"
        );

        System.out.println(
                "Duplicate test coverage  : 0"
        );

        System.out.println(
                "Class stratification     : PASSED"
        );

        System.out.println(
                "Reusable plan            : True"
        );

        System.out.println(
                "ValidationPassed         : True"
        );

        System.out.println(
                "=================================="
        );
    }

    public static void main(
            String[] args
    ) throws Exception {

        Path repository =
                args.length >= 1
                        ? Path.of(args[0])
                        : Path.of(".");

        Mode mode =
                args.length >= 2
                        ? Mode.parse(args[1])
                        : Mode.QUICK;

        M2DatasetLoader.LoadedDataset loaded =
                M2DatasetLoader.load(
                        repository
                );

        FoldPlan plan =
                createPlan(
                        loaded,
                        mode
                );

        writeOutputs(
                repository,
                plan,
                loaded.metadata()
        );

        printSummary(
                plan,
                loaded.metadata()
        );
    }

    public record FoldPlan(
            Mode mode,
            List<RepetitionPlan> repetitions,
            String fingerprint
    ) {
    }

    public record RepetitionPlan(
            int repetition,
            int seed,
            List<FoldSplit> folds
    ) {
    }

    public record FoldSplit(
            int repetition,
            int seed,
            int fold,
            List<Integer> trainingIndices,
            List<Integer> testIndices
    ) {
    }

    public record MaterializedFold(
            weka.core.Instances training,
            weka.core.Instances test,
            List<M2DatasetLoader.RowMetadata> testMetadata
    ) {
    }
}
