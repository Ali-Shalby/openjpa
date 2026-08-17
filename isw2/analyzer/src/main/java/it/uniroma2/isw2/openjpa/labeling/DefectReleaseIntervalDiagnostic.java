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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
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

public final class DefectReleaseIntervalDiagnostic {

    private static final double P_TOTAL =
            1.9688220484114205;

    private static final String BASELINE_REF =
            "4.1.1";

    private static final Path RAW_RELEASE_CATALOG =
            Path.of(
                    "isw2",
                    "datasets",
                    "release_catalog_raw.csv"
            );

    private static final Path STABLE_RELEASE_CATALOG =
            Path.of(
                    "isw2",
                    "datasets",
                    "release_catalog.csv"
            );

    private static final Path DEFECT_CATALOG =
            Path.of(
                    "isw2",
                    "datasets",
                    "defect_ticket_catalog_raw.csv"
            );

    private static final Path SZZ_EVIDENCE =
            Path.of(
                    "isw2",
                    "datasets",
                    "szz_evidence.csv"
            );

    private DefectReleaseIntervalDiagnostic() {
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

        if (!Files.isDirectory(
                repository.resolve(".git")
        )) {

            throw new IllegalArgumentException(
                    "Not a Git repository root: "
                            + repository
            );
        }

        List<RawRelease> rawReleases =
                readRawReleases(
                        repository.resolve(
                                RAW_RELEASE_CATALOG
                        )
                );

        Map<String, StableRelease> stableByVersion =
                readStableReleases(
                        repository.resolve(
                                STABLE_RELEASE_CATALOG
                        )
                );

        List<ResolvedRelease> releases =
                resolveReleaseSnapshots(
                        repository,
                        rawReleases,
                        stableByVersion
                );

        validateStableSnapshots(
                releases,
                stableByVersion
        );

        Map<String, ResolvedRelease> releaseByVersion =
                buildReleaseMap(
                        releases
                );

        Set<String> datasetVersions =
                readDatasetVersions(
                        repository.resolve(
                                STABLE_RELEASE_CATALOG
                        )
                );

        Set<Integer> datasetRawIndices =
                mapDatasetVersionsToRawIndices(
                        datasetVersions,
                        releaseByVersion
                );

        Map<String, Defect> defectByIssue =
                readDefects(
                        repository.resolve(
                                DEFECT_CATALOG
                        )
                );

        Map<String, List<SzzRow>> szzByIssue =
                readSzzEvidence(
                        repository.resolve(
                                SZZ_EVIDENCE
                        )
                );

        Map<String, Boolean> ancestryCache =
                new HashMap<>();

        List<IntervalRow> rows =
                calculate(
                        repository,
                        releases,
                        releaseByVersion,
                        datasetRawIndices,
                        defectByIssue,
                        szzByIssue,
                        ancestryCache
                );

        validate(
                releases,
                datasetVersions,
                datasetRawIndices,
                szzByIssue,
                rows
        );

        printSummary(
                releases,
                datasetVersions,
                datasetRawIndices,
                rows
        );
    }

    private static List<IntervalRow> calculate(
            Path repository,
            List<ResolvedRelease> releases,
            Map<String, ResolvedRelease> releaseByVersion,
            Set<Integer> datasetRawIndices,
            Map<String, Defect> defectByIssue,
            Map<String, List<SzzRow>> szzByIssue,
            Map<String, Boolean> ancestryCache
    ) throws IOException, InterruptedException {

        List<IntervalRow> result =
                new ArrayList<>();

        List<String> issueKeys =
                szzByIssue.keySet()
                        .stream()
                        .sorted()
                        .toList();

        int processed = 0;

        for (String issueKey : issueKeys) {

            Defect defect =
                    defectByIssue.get(
                            issueKey
                    );

            if (defect == null) {

                throw new IllegalStateException(
                        "SZZ issue missing from defect catalog: "
                                + issueKey
                );
            }

            List<SzzRow> szzRows =
                    szzByIssue.get(
                            issueKey
                    );

            Set<String> fixIds =
                    szzRows.stream()
                            .map(
                                    SzzRow::fixCommitId
                            )
                            .collect(
                                    Collectors.toCollection(
                                            LinkedHashSet::new
                                    )
                            );

            /*
             * ----------------------------------------------------
             * 1. OPENING VERSION
             * ----------------------------------------------------
             */

            ResolvedRelease openingRelease =
                    findOpeningRelease(
                            releases,
                            defect.createdAt()
                    );

            Integer ov =
                    openingRelease == null
                            ? null
                            : openingRelease.index();

            /*
             * ----------------------------------------------------
             * 2. OBSERVED AFFECTED VERSION
             * ----------------------------------------------------
             */

            List<ResolvedRelease> affectedReleases =
                    mapVersions(
                            splitVersions(
                                    defect.affectedVersions()
                            ),
                            releaseByVersion
                    );

            ResolvedRelease observedIvRelease =
                    affectedReleases.isEmpty()
                            ? null
                            : affectedReleases.getFirst();

            Integer observedIv =
                    observedIvRelease == null
                            ? null
                            : observedIvRelease.index();

            /*
             * ----------------------------------------------------
             * 3. JIRA FIX VERSION
             * ----------------------------------------------------
             */

            List<ResolvedRelease> jiraFixReleases =
                    mapVersions(
                            splitVersions(
                                    defect.fixVersions()
                            ),
                            releaseByVersion
                    );

            ResolvedRelease jiraFvRelease =
                    jiraFixReleases.isEmpty()
                            ? null
                            : jiraFixReleases.getFirst();

            Integer jiraFv =
                    jiraFvRelease == null
                            ? null
                            : jiraFvRelease.index();

            /*
             * ----------------------------------------------------
             * 4. EFFECTIVE FV
             * ----------------------------------------------------
             *
             * Jira remains the primary source.
             *
             * If Jira FV is absent, or none of the SZZ-producing
             * fix commits is contained in that release snapshot,
             * fall back to the earliest release containing at
             * least one SZZ-producing fix commit.
             */

            ResolvedRelease effectiveFvRelease =
                    jiraFvRelease;

            String fvSource =
                    jiraFvRelease == null
                            ? "UNRESOLVED"
                            : "JIRA_FIX_VERSION";

            Set<String> alignedFixIds =
                    alignedFixIds(
                            repository,
                            fixIds,
                            effectiveFvRelease,
                            ancestryCache
                    );

            if (effectiveFvRelease == null
                    || alignedFixIds.isEmpty()) {

                FallbackCandidate fallback =
                        findGitFallback(
                                repository,
                                releases,
                                fixIds,
                                ancestryCache
                        );

                if (fallback != null) {

                    effectiveFvRelease =
                            fallback.release();

                    if (jiraFvRelease == null) {

                        fvSource =
                                "GIT_CONTAINMENT_NO_JIRA_FV";

                    } else {

                        fvSource =
                                "GIT_CONTAINMENT_JIRA_MISMATCH";
                    }

                    alignedFixIds =
                            alignedFixIds(
                                    repository,
                                    fixIds,
                                    effectiveFvRelease,
                                    ancestryCache
                            );

                } else {

                    effectiveFvRelease =
                            null;

                    fvSource =
                            "UNRESOLVED";

                    alignedFixIds =
                            Set.of();
                }
            }

            Integer effectiveFv =
                    effectiveFvRelease == null
                            ? null
                            : effectiveFvRelease.index();

            /*
             * Count only evidence belonging to fix commits that
             * are actually contained in the selected FV.
             */

            int alignedSzzRows = 0;

            for (SzzRow row : szzRows) {

                if (alignedFixIds.contains(
                        row.fixCommitId()
                )) {

                    alignedSzzRows++;
                }
            }

            /*
             * ----------------------------------------------------
             * 5. EFFECTIVE IV
             * ----------------------------------------------------
             */

            boolean observedIvConsistent =
                    observedIv != null
                            && ov != null
                            && effectiveFv != null
                            && observedIv <= ov
                            && ov <= effectiveFv
                            && observedIv < effectiveFv;

            String ivSource =
                    "UNRESOLVED";

            Double lav =
                    null;

            Integer effectiveIv =
                    null;

            boolean clampedToFirstRelease =
                    false;

            if (observedIvConsistent) {

                /*
                 * Affected Version provided by Jira and
                 * temporally coherent with OV and Effective FV.
                 */

                ivSource =
                        "JIRA_AFFECTED_VERSION";

                effectiveIv =
                        observedIv;

            } else if (ov == null
                    && effectiveFv != null) {

                /*
                 * The defect was opened before the first official
                 * release represented in our release timeline.
                 *
                 * There is therefore no representable OV.
                 *
                 * The earliest buggy release that can be observed
                 * in the project history is release index 1.
                 *
                 * This is intentionally NOT classified as
                 * PROPORTION_TOTAL because no Proportion formula
                 * can be applied without OV.
                 */

                ivSource =
                        "PRE_FIRST_RELEASE";

                effectiveIv =
                        1;

            } else if (ov != null
                    && effectiveFv != null
                    && ov <= effectiveFv) {

                /*
                 * Affected Version is missing, unmapped or
                 * inconsistent.
                 *
                 * Estimate IV through Proportion Total.
                 */

                ivSource =
                        "PROPORTION_TOTAL";

                int distance =
                        effectiveFv - ov;

                /*
                 * If OV == FV, use unit distance.
                 */
                if (distance == 0) {

                    distance = 1;
                }

                lav =
                        effectiveFv
                                - (
                                distance
                                        * P_TOTAL
                        );

                /*
                 * LAV may be fractional.
                 *
                 * ceil selects the first discrete release index
                 * compatible with the estimated location.
                 */
                effectiveIv =
                        (int) Math.ceil(
                                lav
                        );

                /*
                 * Proportion may estimate an IV before the first
                 * observable release.
                 */
                if (effectiveIv < 1) {

                    effectiveIv =
                            1;

                    clampedToFirstRelease =
                            true;
                }
            }

            boolean overlapsDataset =
                    overlapsDataset(
                            effectiveIv,
                            effectiveFv,
                            datasetRawIndices
                    );

            result.add(
                    new IntervalRow(
                            issueKey,

                            observedIv,
                            observedIvRelease == null
                                    ? ""
                                    : observedIvRelease.version(),

                            ov,
                            openingRelease == null
                                    ? ""
                                    : openingRelease.version(),

                            jiraFv,
                            jiraFvRelease == null
                                    ? ""
                                    : jiraFvRelease.version(),

                            effectiveFv,
                            effectiveFvRelease == null
                                    ? ""
                                    : effectiveFvRelease.version(),

                            fvSource,

                            fixIds.size(),
                            alignedFixIds.size(),

                            szzRows.size(),
                            alignedSzzRows,

                            ivSource,

                            lav,
                            effectiveIv,

                            clampedToFirstRelease,
                            overlapsDataset,

                            defect.affectedVersions(),
                            defect.fixVersions()
                    )
            );

            processed++;

            if (processed % 50 == 0
                    || processed == issueKeys.size()) {

                System.out.printf(
                        "Interval progress: %d / %d%n",
                        processed,
                        issueKeys.size()
                );
            }
        }

        return List.copyOf(
                result
        );
    }

    private static Set<String> alignedFixIds(
            Path repository,
            Set<String> fixIds,
            ResolvedRelease fvRelease,
            Map<String, Boolean> ancestryCache
    ) throws IOException, InterruptedException {

        if (fvRelease == null) {

            return Set.of();
        }

        Set<String> result =
                new LinkedHashSet<>();

        for (String fixId : fixIds) {

            if (isAncestorCached(
                    repository,
                    fixId,
                    fvRelease.snapshotCommit(),
                    ancestryCache
            )) {

                result.add(
                        fixId
                );
            }
        }

        return Set.copyOf(
                result
        );
    }

    private static FallbackCandidate findGitFallback(
            Path repository,
            List<ResolvedRelease> releases,
            Set<String> fixIds,
            Map<String, Boolean> ancestryCache
    ) throws IOException, InterruptedException {

        FallbackCandidate best =
                null;

        for (String fixId : fixIds) {

            for (ResolvedRelease release : releases) {

                if (isAncestorCached(
                        repository,
                        fixId,
                        release.snapshotCommit(),
                        ancestryCache
                )) {

                    FallbackCandidate candidate =
                            new FallbackCandidate(
                                    release,
                                    fixId
                            );

                    if (best == null
                            || candidate.release().index()
                            < best.release().index()) {

                        best =
                                candidate;
                    }

                    break;
                }
            }
        }

        return best;
    }

    private static boolean isAncestorCached(
            Path repository,
            String ancestor,
            String descendant,
            Map<String, Boolean> cache
    ) throws IOException, InterruptedException {

        String key =
                ancestor
                        + "\u0000"
                        + descendant;

        Boolean cached =
                cache.get(
                        key
                );

        if (cached != null) {

            return cached;
        }

        Process process =
                new ProcessBuilder(
                        "git",
                        "-C",
                        repository.toString(),
                        "merge-base",
                        "--is-ancestor",
                        ancestor,
                        descendant
                )
                        .redirectErrorStream(
                                true
                        )
                        .start();

        String output;

        try (BufferedReader reader =
                     process.inputReader(
                             StandardCharsets.UTF_8
                     )) {

            output =
                    reader.lines()
                            .collect(
                                    Collectors.joining(
                                            System.lineSeparator()
                                    )
                            );
        }

        int exitCode =
                process.waitFor();

        boolean result;

        if (exitCode == 0) {

            result =
                    true;

        } else if (exitCode == 1) {

            result =
                    false;

        } else {

            throw new IOException(
                    "git merge-base --is-ancestor failed "
                            + "for "
                            + ancestor
                            + " -> "
                            + descendant
                            + System.lineSeparator()
                            + output
            );
        }

        cache.put(
                key,
                result
        );

        return result;
    }

    private static boolean overlapsDataset(
            Integer effectiveIv,
            Integer effectiveFv,
            Set<Integer> datasetRawIndices
    ) {

        if (effectiveIv == null
                || effectiveFv == null) {

            return false;
        }

        /*
         * Buggy interval:
         *
         * [IV, FV)
         *
         * FV itself is excluded because the defect is considered
         * fixed in that release.
         */

        for (Integer releaseIndex : datasetRawIndices) {

            if (releaseIndex >= effectiveIv
                    && releaseIndex < effectiveFv) {

                return true;
            }
        }

        return false;
    }

    private static void validate(
            List<ResolvedRelease> releases,
            Set<String> datasetVersions,
            Set<Integer> datasetRawIndices,
            Map<String, List<SzzRow>> szzByIssue,
            List<IntervalRow> rows
    ) {

        if (releases.size() != 42) {

            throw new IllegalStateException(
                    "Expected 42 raw releases, found "
                            + releases.size()
            );
        }

        if (datasetVersions.size() != 12) {

            throw new IllegalStateException(
                    "Expected 12 Dataset A releases, found "
                            + datasetVersions.size()
            );
        }

        if (datasetRawIndices.size()
                != datasetVersions.size()) {

            throw new IllegalStateException(
                    "Dataset raw-index mapping mismatch."
            );
        }

        if (rows.size()
                != szzByIssue.size()) {

            throw new IllegalStateException(
                    "Interval row count mismatch. "
                            + "SZZ defects="
                            + szzByIssue.size()
                            + ", rows="
                            + rows.size()
            );
        }

        Set<String> issueKeys =
                new HashSet<>();

        long expectedSzzRows =
                szzByIssue.values()
                        .stream()
                        .mapToLong(
                                List::size
                        )
                        .sum();

        long actualSzzRows =
                rows.stream()
                        .mapToLong(
                                IntervalRow::szzRowsTotal
                        )
                        .sum();

        if (expectedSzzRows
                != actualSzzRows) {

            throw new IllegalStateException(
                    "Input SZZ row count mismatch. "
                            + "Expected="
                            + expectedSzzRows
                            + ", found="
                            + actualSzzRows
            );
        }

        for (IntervalRow row : rows) {

            if (!issueKeys.add(
                    row.issueKey()
            )) {

                throw new IllegalStateException(
                        "Duplicate interval issue: "
                                + row.issueKey()
                );
            }

            if (!Set.of(
                            "JIRA_FIX_VERSION",
                            "GIT_CONTAINMENT_NO_JIRA_FV",
                            "GIT_CONTAINMENT_JIRA_MISMATCH",
                            "UNRESOLVED"
                    )
                    .contains(
                            row.fvSource()
                    )) {

                throw new IllegalStateException(
                        "Unexpected FV source for "
                                + row.issueKey()
                                + ": "
                                + row.fvSource()
                );
            }

            if (!Set.of(
                            "JIRA_AFFECTED_VERSION",
                            "PROPORTION_TOTAL",
                            "PRE_FIRST_RELEASE",
                            "UNRESOLVED"
                    )
                    .contains(
                            row.ivSource()
                    )) {

                throw new IllegalStateException(
                        "Unexpected IV source for "
                                + row.issueKey()
                                + ": "
                                + row.ivSource()
                );
            }

            /*
             * Every resolved Effective FV must contain at least
             * one SZZ-producing fix.
             */

            if (!"UNRESOLVED"
                    .equals(
                            row.fvSource()
                    )) {

                if (row.effectiveFv() == null) {

                    throw new IllegalStateException(
                            "Resolved FV without EffectiveFV: "
                                    + row.issueKey()
                    );
                }

                if (row.szzFixesAligned() <= 0
                        || row.szzRowsAligned() <= 0) {

                    throw new IllegalStateException(
                            "Resolved FV with zero aligned SZZ: "
                                    + row.issueKey()
                    );
                }
            }

            if (row.szzFixesAligned()
                    > row.szzFixesTotal()) {

                throw new IllegalStateException(
                        "Aligned fixes exceed total fixes: "
                                + row.issueKey()
                );
            }

            if (row.szzRowsAligned()
                    > row.szzRowsTotal()) {

                throw new IllegalStateException(
                        "Aligned SZZ rows exceed total rows: "
                                + row.issueKey()
                );
            }

            /*
             * ----------------------------------------------------
             * JIRA AFFECTED VERSION VALIDATION
             * ----------------------------------------------------
             */

            if ("JIRA_AFFECTED_VERSION"
                    .equals(
                            row.ivSource()
                    )) {

                if (row.observedIv() == null
                        || row.ov() == null
                        || row.effectiveFv() == null
                        || row.effectiveIv() == null) {

                    throw new IllegalStateException(
                            "Incomplete observed IV lifecycle: "
                                    + row.issueKey()
                    );
                }

                if (!row.observedIv()
                        .equals(
                                row.effectiveIv()
                        )) {

                    throw new IllegalStateException(
                            "Observed/effective IV mismatch: "
                                    + row.issueKey()
                    );
                }

                if (!(row.observedIv() <= row.ov()
                        && row.ov() <= row.effectiveFv()
                        && row.observedIv()
                        < row.effectiveFv())) {

                    throw new IllegalStateException(
                            "Invalid observed IV lifecycle: "
                                    + row.issueKey()
                    );
                }
            }

            /*
             * ----------------------------------------------------
             * PROPORTION TOTAL VALIDATION
             * ----------------------------------------------------
             */

            if ("PROPORTION_TOTAL"
                    .equals(
                            row.ivSource()
                    )) {

                if (row.ov() == null
                        || row.effectiveFv() == null
                        || row.lav() == null
                        || row.effectiveIv() == null) {

                    throw new IllegalStateException(
                            "Incomplete Proportion lifecycle: "
                                    + row.issueKey()
                    );
                }

                if (row.ov()
                        > row.effectiveFv()) {

                    throw new IllegalStateException(
                            "OV > EffectiveFV in Proportion row: "
                                    + row.issueKey()
                    );
                }

                if (row.effectiveIv() < 1) {

                    throw new IllegalStateException(
                            "EffectiveIV below first release: "
                                    + row.issueKey()
                    );
                }

                if (row.effectiveIv()
                        >= row.effectiveFv()) {

                    throw new IllegalStateException(
                            "Empty/negative Proportion interval: "
                                    + row.issueKey()
                                    + " | IV="
                                    + row.effectiveIv()
                                    + " | FV="
                                    + row.effectiveFv()
                    );
                }
            }

            /*
             * ----------------------------------------------------
             * PRE-FIRST-RELEASE VALIDATION
             * ----------------------------------------------------
             */

            if ("PRE_FIRST_RELEASE"
                    .equals(
                            row.ivSource()
                    )) {

                if (row.ov() != null) {

                    throw new IllegalStateException(
                            "PRE_FIRST_RELEASE unexpectedly has OV: "
                                    + row.issueKey()
                    );
                }

                if (row.effectiveFv() == null
                        || row.effectiveIv() == null) {

                    throw new IllegalStateException(
                            "Incomplete PRE_FIRST_RELEASE interval: "
                                    + row.issueKey()
                    );
                }

                if (row.effectiveIv() != 1) {

                    throw new IllegalStateException(
                            "PRE_FIRST_RELEASE IV must be 1: "
                                    + row.issueKey()
                    );
                }

                if (row.effectiveIv()
                        > row.effectiveFv()) {

                    throw new IllegalStateException(
                            "PRE_FIRST_RELEASE IV is after FV: "
                                    + row.issueKey()
                    );
                }
            }

            if ("UNRESOLVED"
                    .equals(
                            row.ivSource()
                    )
                    && row.effectiveIv() != null) {

                throw new IllegalStateException(
                        "UNRESOLVED IV has EffectiveIV: "
                                + row.issueKey()
                );
            }

            if (row.overlapsDataset()
                    && (
                    row.effectiveIv() == null
                            || row.effectiveFv() == null
            )) {

                throw new IllegalStateException(
                        "Dataset overlap without complete interval: "
                                + row.issueKey()
                );
            }
        }
    }

    private static void printSummary(
            List<ResolvedRelease> releases,
            Set<String> datasetVersions,
            Set<Integer> datasetRawIndices,
            List<IntervalRow> rows
    ) {

        long fvJira =
                countFvSource(
                        rows,
                        "JIRA_FIX_VERSION"
                );

        long fvGitNoJira =
                countFvSource(
                        rows,
                        "GIT_CONTAINMENT_NO_JIRA_FV"
                );

        long fvGitMismatch =
                countFvSource(
                        rows,
                        "GIT_CONTAINMENT_JIRA_MISMATCH"
                );

        long fvUnresolved =
                countFvSource(
                        rows,
                        "UNRESOLVED"
                );

        long ivJira =
                countIvSource(
                        rows,
                        "JIRA_AFFECTED_VERSION"
                );

        long ivProportion =
                countIvSource(
                        rows,
                        "PROPORTION_TOTAL"
                );

        long ivPreFirstRelease =
                countIvSource(
                        rows,
                        "PRE_FIRST_RELEASE"
                );

        long ivUnresolved =
                countIvSource(
                        rows,
                        "UNRESOLVED"
                );

        long noOv =
                rows.stream()
                        .filter(row ->
                                row.ov() == null
                        )
                        .count();

        long observedIvInconsistent =
                rows.stream()
                        .filter(row ->
                                row.observedIv() != null
                                        && row.ov() != null
                                        && row.effectiveFv() != null
                                        && !(
                                        row.observedIv()
                                                <= row.ov()
                                                && row.ov()
                                                <= row.effectiveFv()
                                                && row.observedIv()
                                                < row.effectiveFv()
                                )
                        )
                        .count();

        long clamped =
                rows.stream()
                        .filter(
                                IntervalRow
                                        ::clampedToFirstRelease
                        )
                        .count();

        long datasetOverlap =
                rows.stream()
                        .filter(
                                IntervalRow
                                        ::overlapsDataset
                        )
                        .count();

        long inputSzzRows =
                rows.stream()
                        .mapToLong(
                                IntervalRow::szzRowsTotal
                        )
                        .sum();

        long alignedSzzRows =
                rows.stream()
                        .mapToLong(
                                IntervalRow::szzRowsAligned
                        )
                        .sum();

        long inputFixes =
                rows.stream()
                        .mapToLong(
                                IntervalRow::szzFixesTotal
                        )
                        .sum();

        long alignedFixes =
                rows.stream()
                        .mapToLong(
                                IntervalRow::szzFixesAligned
                        )
                        .sum();

        System.out.println();

        System.out.println(
                "===== EFFECTIVE DEFECT RELEASE INTERVAL DIAGNOSTIC ====="
        );

        System.out.println(
                "P_TOTAL                          : "
                        + P_TOTAL
        );

        System.out.println(
                "Release universe                 : "
                        + releases.size()
        );

        System.out.println(
                "Dataset releases                 : "
                        + datasetVersions.size()
        );

        System.out.println(
                "Dataset raw indices              : "
                        + datasetRawIndices
                        .stream()
                        .sorted()
                        .map(
                                String::valueOf
                        )
                        .collect(
                                Collectors.joining(
                                        ", "
                                )
                        )
        );

        System.out.println();

        System.out.println(
                "SZZ defects                      : "
                        + rows.size()
        );

        System.out.println(
                "SZZ fix commits before alignment : "
                        + inputFixes
        );

        System.out.println(
                "SZZ fix commits after alignment  : "
                        + alignedFixes
        );

        System.out.println(
                "SZZ rows before alignment        : "
                        + inputSzzRows
        );

        System.out.println(
                "SZZ rows after alignment         : "
                        + alignedSzzRows
        );

        System.out.println();

        System.out.println(
                "===== EFFECTIVE FV ====="
        );

        System.out.println(
                "JIRA_FIX_VERSION                 : "
                        + fvJira
        );

        System.out.println(
                "GIT_CONTAINMENT_NO_JIRA_FV       : "
                        + fvGitNoJira
        );

        System.out.println(
                "GIT_CONTAINMENT_JIRA_MISMATCH    : "
                        + fvGitMismatch
        );

        System.out.println(
                "FV UNRESOLVED                    : "
                        + fvUnresolved
        );

        System.out.println();

        System.out.println(
                "===== EFFECTIVE IV ====="
        );

        System.out.println(
                "JIRA_AFFECTED_VERSION            : "
                        + ivJira
        );

        System.out.println(
                "PROPORTION_TOTAL                 : "
                        + ivProportion
        );

        System.out.println(
                "PRE_FIRST_RELEASE                : "
                        + ivPreFirstRelease
        );

        System.out.println(
                "IV UNRESOLVED                    : "
                        + ivUnresolved
        );

        System.out.println(
                "Without OV                       : "
                        + noOv
        );

        System.out.println(
                "Inconsistent observed IV         : "
                        + observedIvInconsistent
        );

        System.out.println(
                "Proportion IV clamped to 1       : "
                        + clamped
        );

        System.out.println();

        System.out.println(
                "Lifecycle overlapping Dataset A  : "
                        + datasetOverlap
        );

        System.out.println(
                "Lifecycle outside Dataset A      : "
                        + (
                        rows.size()
                                - datasetOverlap
                )
        );

        System.out.println();

        System.out.println(
                "===== PRE-FIRST-RELEASE CASES ====="
        );

        rows.stream()
                .filter(row ->
                        "PRE_FIRST_RELEASE"
                                .equals(
                                        row.ivSource()
                                )
                )
                .forEach(row ->
                        System.out.printf(
                                "%s | EffectiveIV=%d | "
                                        + "EffectiveFV=%d/%s | "
                                        + "FVSource=%s | "
                                        + "interval=[%d,%d) | "
                                        + "DatasetOverlap=%s%n",

                                row.issueKey(),

                                row.effectiveIv(),

                                row.effectiveFv(),
                                row.effectiveFvVersion(),

                                row.fvSource(),

                                row.effectiveIv(),
                                row.effectiveFv(),

                                row.overlapsDataset()
                        )
                );

        System.out.println();

        System.out.println(
                "===== UNRESOLVED IV CASES ====="
        );

        rows.stream()
                .filter(row ->
                        "UNRESOLVED"
                                .equals(
                                        row.ivSource()
                                )
                )
                .limit(100)
                .forEach(row ->
                        System.out.printf(
                                "%s | ObservedIV=%s/%s | OV=%s/%s | "
                                        + "JiraFV=%s/%s | EffectiveFV=%s/%s | "
                                        + "FVSource=%s | AV=[%s] | FIX=[%s]%n",

                                row.issueKey(),

                                nullableInt(
                                        row.observedIv()
                                ),
                                row.observedIvVersion(),

                                nullableInt(
                                        row.ov()
                                ),
                                row.ovVersion(),

                                nullableInt(
                                        row.jiraFv()
                                ),
                                row.jiraFvVersion(),

                                nullableInt(
                                        row.effectiveFv()
                                ),
                                row.effectiveFvVersion(),

                                row.fvSource(),

                                nullToEmpty(
                                        row.jiraAffectedVersions()
                                ),

                                nullToEmpty(
                                        row.jiraFixVersions()
                                )
                        )
                );

        System.out.println();

        System.out.println(
                "===== PROPORTION SAMPLE ====="
        );

        rows.stream()
                .filter(row ->
                        "PROPORTION_TOTAL"
                                .equals(
                                        row.ivSource()
                                )
                )
                .limit(30)
                .forEach(row ->
                        System.out.printf(
                                "%s | ObservedIV=%s/%s | "
                                        + "OV=%d/%s | "
                                        + "EffectiveFV=%d/%s | "
                                        + "FVSource=%s | "
                                        + "LAV=%.6f | "
                                        + "EffectiveIV=%d | "
                                        + "Clamped=%s | "
                                        + "DatasetOverlap=%s%n",

                                row.issueKey(),

                                nullableInt(
                                        row.observedIv()
                                ),
                                row.observedIvVersion(),

                                row.ov(),
                                row.ovVersion(),

                                row.effectiveFv(),
                                row.effectiveFvVersion(),

                                row.fvSource(),

                                row.lav(),

                                row.effectiveIv(),

                                row.clampedToFirstRelease(),

                                row.overlapsDataset()
                        )
                );

        System.out.println();

        System.out.println(
                "===== FV FALLBACK SAMPLE ====="
        );

        rows.stream()
                .filter(row ->
                        row.fvSource()
                                .startsWith(
                                        "GIT_CONTAINMENT"
                                )
                )
                .limit(50)
                .forEach(row ->
                        System.out.printf(
                                "%s | JiraFV=%s/%s | "
                                        + "EffectiveFV=%d/%s | "
                                        + "source=%s | "
                                        + "fixes=%d -> %d | "
                                        + "rows=%d -> %d%n",

                                row.issueKey(),

                                nullableInt(
                                        row.jiraFv()
                                ),
                                row.jiraFvVersion(),

                                row.effectiveFv(),
                                row.effectiveFvVersion(),

                                row.fvSource(),

                                row.szzFixesTotal(),
                                row.szzFixesAligned(),

                                row.szzRowsTotal(),
                                row.szzRowsAligned()
                        )
                );

        System.out.println();

        System.out.println(
                "===== DATASET OVERLAP SAMPLE ====="
        );

        rows.stream()
                .filter(
                        IntervalRow
                                ::overlapsDataset
                )
                .limit(30)
                .forEach(row ->
                        System.out.printf(
                                "%s | IV=%d | FV=%d | "
                                        + "IVSource=%s | "
                                        + "FVSource=%s | "
                                        + "interval=[%d,%d)%n",

                                row.issueKey(),

                                row.effectiveIv(),
                                row.effectiveFv(),

                                row.ivSource(),
                                row.fvSource(),

                                row.effectiveIv(),
                                row.effectiveFv()
                        )
                );

        System.out.println(
                "========================================================"
        );
    }

    private static long countFvSource(
            List<IntervalRow> rows,
            String source
    ) {

        return rows.stream()
                .filter(row ->
                        source.equals(
                                row.fvSource()
                        )
                )
                .count();
    }

    private static long countIvSource(
            List<IntervalRow> rows,
            String source
    ) {

        return rows.stream()
                .filter(row ->
                        source.equals(
                                row.ivSource()
                        )
                )
                .count();
    }

    private static ResolvedRelease findOpeningRelease(
            List<ResolvedRelease> releases,
            OffsetDateTime createdAt
    ) {

        ResolvedRelease result =
                null;

        for (ResolvedRelease release : releases) {

            if (!release.releaseDate()
                    .isAfter(
                            createdAt.toLocalDate()
                    )) {

                result =
                        release;

            } else {

                break;
            }
        }

        return result;
    }

    private static List<ResolvedRelease> resolveReleaseSnapshots(
            Path repository,
            List<RawRelease> rawReleases,
            Map<String, StableRelease> stableByVersion
    ) throws IOException, InterruptedException {

        List<ResolvedRelease> result =
                new ArrayList<>();

        for (RawRelease raw : rawReleases) {

            StableRelease stable =
                    stableByVersion.get(
                            normalizeVersion(
                                    raw.version()
                            )
                    );

            String snapshotCommit;
            String source;

            if (stable != null) {

                snapshotCommit =
                        stable.releaseCommit();

                source =
                        "VALIDATED_STABLE_CATALOG";

            } else {

                snapshotCommit =
                        resolveByDateCutoff(
                                repository,
                                raw.releaseDate()
                        );

                source =
                        "DATE_CUTOFF_RAW";
            }

            OffsetDateTime commitDate =
                    readCommitDate(
                            repository,
                            snapshotCommit
                    );

            if (commitDate.toLocalDate()
                    .isAfter(
                            raw.releaseDate()
                    )) {

                throw new IllegalStateException(
                        "Resolved snapshot is after release date: "
                                + raw.version()
                                + " | releaseDate="
                                + raw.releaseDate()
                                + " | commitDate="
                                + commitDate
                );
            }

            result.add(
                    new ResolvedRelease(
                            raw.index(),
                            raw.version(),
                            raw.releaseDate(),
                            snapshotCommit,
                            commitDate,
                            source
                    )
            );
        }

        result.sort(
                Comparator.comparingInt(
                        ResolvedRelease::index
                )
        );

        result.sort(
                Comparator.comparingInt(
                        ResolvedRelease::index
                )
        );

        return List.copyOf(
                result
        );
    }

    private static String resolveByDateCutoff(
            Path repository,
            LocalDate releaseDate
    ) throws IOException, InterruptedException {

        String cutoff =
                releaseDate
                        .plusDays(1)
                        .atStartOfDay()
                        .toString();

        String output =
                runGit(
                        repository,
                        "rev-list",
                        "-1",
                        "--before=" + cutoff,
                        BASELINE_REF
                )
                        .trim();

        if (output.isBlank()) {

            throw new IllegalStateException(
                    "No commit found before release date "
                            + releaseDate
            );
        }

        return output;
    }

    private static OffsetDateTime readCommitDate(
            Path repository,
            String commitId
    ) throws IOException, InterruptedException {

        String output =
                runGit(
                        repository,
                        "show",
                        "-s",
                        "--format=%cI",
                        commitId
                )
                        .trim();

        return OffsetDateTime.parse(
                output
        );
    }

    private static void validateStableSnapshots(
            List<ResolvedRelease> releases,
            Map<String, StableRelease> stableByVersion
    ) {

        int checked =
                0;

        for (ResolvedRelease release : releases) {

            StableRelease stable =
                    stableByVersion.get(
                            normalizeVersion(
                                    release.version()
                            )
                    );

            if (stable == null) {

                continue;
            }

            checked++;

            if (!stable.releaseCommit()
                    .equalsIgnoreCase(
                            release.snapshotCommit()
                    )) {

                throw new IllegalStateException(
                        "Stable snapshot mismatch for "
                                + release.version()
                );
            }
        }

        if (checked
                != stableByVersion.size()) {

            throw new IllegalStateException(
                    "Not all stable release snapshots validated. "
                            + "Expected="
                            + stableByVersion.size()
                            + ", checked="
                            + checked
            );
        }
    }

    private static List<RawRelease> readRawReleases(
            Path input
    ) throws IOException {

        CsvTable table =
                readCsv(
                        input
                );

        List<RawRelease> result =
                new ArrayList<>();

        for (List<String> row : table.rows()) {

            String indexText =
                    firstValue(
                            table,
                            row,
                            "ChronologicalIndex",
                            "ReleaseIndex",
                            "Index"
                    );

            String version =
                    firstValue(
                            table,
                            row,
                            "Version"
                    );

            String releaseDate =
                    firstValue(
                            table,
                            row,
                            "ReleaseDate"
                    );

            if (indexText.isBlank()
                    || version.isBlank()
                    || releaseDate.isBlank()) {

                throw new IllegalStateException(
                        "Incomplete RAW release row."
                );
            }

            result.add(
                    new RawRelease(
                            Integer.parseInt(
                                    indexText
                            ),
                            version,
                            LocalDate.parse(
                                    releaseDate
                            )
                    )
            );
        }

        result.sort(
                Comparator.comparingInt(
                        RawRelease::index
                )
        );

        return List.copyOf(
                result
        );
    }

    private static Map<String, StableRelease> readStableReleases(
            Path input
    ) throws IOException {

        CsvTable table =
                readCsv(
                        input
                );

        Map<String, StableRelease> result =
                new HashMap<>();

        for (List<String> row : table.rows()) {

            String version =
                    firstValue(
                            table,
                            row,
                            "Version"
                    );

            String releaseCommit =
                    firstValue(
                            table,
                            row,
                            "ReleaseCommit",
                            "CommitId"
                    );

            if (version.isBlank()
                    || releaseCommit.isBlank()) {

                throw new IllegalStateException(
                        "Incomplete stable release row."
                );
            }

            StableRelease previous =
                    result.put(
                            normalizeVersion(
                                    version
                            ),
                            new StableRelease(
                                    version,
                                    releaseCommit
                            )
                    );

            if (previous != null) {

                throw new IllegalStateException(
                        "Duplicate stable release: "
                                + version
                );
            }
        }

        return Map.copyOf(
                result
        );
    }

    private static Set<String> readDatasetVersions(
            Path input
    ) throws IOException {

        CsvTable table =
                readCsv(
                        input
                );

        Set<String> result =
                new LinkedHashSet<>();

        for (List<String> row : table.rows()) {

            String included =
                    firstValue(
                            table,
                            row,
                            "DatasetIncluded"
                    );

            if (!Boolean.parseBoolean(
                    included
            )) {

                continue;
            }

            String version =
                    firstValue(
                            table,
                            row,
                            "Version"
                    );

            if (version.isBlank()) {

                throw new IllegalStateException(
                        "DatasetIncluded row without Version."
                );
            }

            result.add(
                    version
            );
        }

        return Set.copyOf(
                result
        );
    }

    private static Set<Integer> mapDatasetVersionsToRawIndices(
            Set<String> datasetVersions,
            Map<String, ResolvedRelease> releaseByVersion
    ) {

        Set<Integer> result =
                new LinkedHashSet<>();

        for (String version : datasetVersions) {

            ResolvedRelease release =
                    releaseByVersion.get(
                            normalizeVersion(
                                    version
                            )
                    );

            if (release == null) {

                throw new IllegalStateException(
                        "Dataset release missing from RAW timeline: "
                                + version
                );
            }

            result.add(
                    release.index()
            );
        }

        return Set.copyOf(
                result
        );
    }

    private static Map<String, Defect> readDefects(
            Path input
    ) throws IOException {

        CsvTable table =
                readCsv(
                        input
                );

        Map<String, Defect> result =
                new HashMap<>();

        for (List<String> row : table.rows()) {

            String issueKey =
                    firstValue(
                            table,
                            row,
                            "IssueKey",
                            "Key"
                    );

            String createdText =
                    firstValue(
                            table,
                            row,
                            "CreatedAt",
                            "CreatedDate",
                            "Created"
                    );

            String affected =
                    firstValue(
                            table,
                            row,
                            "AffectedVersions",
                            "AffectedVersion",
                            "Affected Version/s"
                    );

            String fix =
                    firstValue(
                            table,
                            row,
                            "FixVersions",
                            "FixVersion",
                            "Fix Version/s"
                    );

            if (issueKey.isBlank()) {

                throw new IllegalStateException(
                        "Defect without IssueKey."
                );
            }

            if (createdText.isBlank()) {

                throw new IllegalStateException(
                        "Defect without CreatedAt: "
                                + issueKey
                );
            }

            OffsetDateTime createdAt;

            try {

                createdAt =
                        OffsetDateTime.parse(
                                createdText
                        );

            } catch (DateTimeParseException exception) {

                throw new IllegalStateException(
                        "Invalid CreatedAt for "
                                + issueKey
                                + ": "
                                + createdText,
                        exception
                );
            }

            Defect previous =
                    result.put(
                            issueKey,
                            new Defect(
                                    issueKey,
                                    createdAt,
                                    affected,
                                    fix
                            )
                    );

            if (previous != null) {

                throw new IllegalStateException(
                        "Duplicate defect IssueKey: "
                                + issueKey
                );
            }
        }

        return Map.copyOf(
                result
        );
    }

    private static Map<String, List<SzzRow>> readSzzEvidence(
            Path input
    ) throws IOException {

        CsvTable table =
                readCsv(
                        input
                );

        Map<String, List<SzzRow>> mutable =
                new LinkedHashMap<>();

        for (List<String> row : table.rows()) {

            String issueKey =
                    firstValue(
                            table,
                            row,
                            "IssueKey"
                    );

            String fixCommitId =
                    firstValue(
                            table,
                            row,
                            "FixCommitId"
                    );

            if (issueKey.isBlank()
                    || fixCommitId.isBlank()) {

                throw new IllegalStateException(
                        "Incomplete SZZ evidence row."
                );
            }

            mutable.computeIfAbsent(
                            issueKey,
                            ignored ->
                                    new ArrayList<>()
                    )
                    .add(
                            new SzzRow(
                                    issueKey,
                                    fixCommitId
                            )
                    );
        }

        Map<String, List<SzzRow>> result =
                new LinkedHashMap<>();

        for (Map.Entry<String, List<SzzRow>> entry
                : mutable.entrySet()) {

            result.put(
                    entry.getKey(),
                    List.copyOf(
                            entry.getValue()
                    )
            );
        }

        return Map.copyOf(
                result
        );
    }

    private static Map<String, ResolvedRelease> buildReleaseMap(
            List<ResolvedRelease> releases
    ) {

        Map<String, ResolvedRelease> result =
                new HashMap<>();

        for (ResolvedRelease release : releases) {

            ResolvedRelease previous =
                    result.put(
                            normalizeVersion(
                                    release.version()
                            ),
                            release
                    );

            if (previous != null) {

                throw new IllegalStateException(
                        "Duplicate RAW release version: "
                                + release.version()
                );
            }
        }

        return Map.copyOf(
                result
        );
    }

    private static List<ResolvedRelease> mapVersions(
            List<String> versions,
            Map<String, ResolvedRelease> releaseByVersion
    ) {

        return versions.stream()
                .map(version ->
                        releaseByVersion.get(
                                normalizeVersion(
                                        version
                                )
                        )
                )
                .filter(release ->
                        release != null
                )
                .distinct()
                .sorted(
                        Comparator.comparingInt(
                                ResolvedRelease::index
                        )
                )
                .toList();
    }

    private static List<String> splitVersions(
            String value
    ) {

        if (value == null
                || value.isBlank()) {

            return List.of();
        }

        String[] tokens =
                value.trim()
                        .split(
                                "\\s*\\|\\s*"
                        );

        List<String> result =
                new ArrayList<>();

        for (String token : tokens) {

            String version =
                    token.trim();

            if (!version.isBlank()) {

                result.add(
                        version
                );
            }
        }

        return List.copyOf(
                result
        );
    }

    private static String normalizeVersion(
            String version
    ) {

        return version
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private static String nullableInt(
            Integer value
    ) {

        return value == null
                ? ""
                : value.toString();
    }

    private static String nullToEmpty(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }

    private static String runGit(
            Path repository,
            String... arguments
    ) throws IOException, InterruptedException {

        List<String> command =
                new ArrayList<>();

        command.add(
                "git"
        );

        command.add(
                "-C"
        );

        command.add(
                repository.toString()
        );

        for (String argument : arguments) {

            command.add(
                    argument
            );
        }

        Process process =
                new ProcessBuilder(
                        command
                )
                        .redirectErrorStream(
                                true
                        )
                        .start();

        String output;

        try (BufferedReader reader =
                     process.inputReader(
                             StandardCharsets.UTF_8
                     )) {

            output =
                    reader.lines()
                            .collect(
                                    Collectors.joining(
                                            System.lineSeparator()
                                    )
                            );
        }

        int exitCode =
                process.waitFor();

        if (exitCode != 0) {

            throw new IOException(
                    "Git command failed (exit "
                            + exitCode
                            + "): "
                            + String.join(
                            " ",
                            command
                    )
                            + System.lineSeparator()
                            + output
            );
        }

        return output;
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

    private record RawRelease(
            int index,
            String version,
            LocalDate releaseDate
    ) {
    }

    private record StableRelease(
            String version,
            String releaseCommit
    ) {
    }

    private record ResolvedRelease(
            int index,
            String version,
            LocalDate releaseDate,
            String snapshotCommit,
            OffsetDateTime snapshotCommitDate,
            String snapshotSource
    ) {
    }

    private record Defect(
            String issueKey,
            OffsetDateTime createdAt,
            String affectedVersions,
            String fixVersions
    ) {
    }

    private record SzzRow(
            String issueKey,
            String fixCommitId
    ) {
    }

    private record FallbackCandidate(
            ResolvedRelease release,
            String fixCommitId
    ) {
    }

    private record IntervalRow(
            String issueKey,

            Integer observedIv,
            String observedIvVersion,

            Integer ov,
            String ovVersion,

            Integer jiraFv,
            String jiraFvVersion,

            Integer effectiveFv,
            String effectiveFvVersion,

            String fvSource,

            int szzFixesTotal,
            int szzFixesAligned,

            int szzRowsTotal,
            int szzRowsAligned,

            String ivSource,

            Double lav,
            Integer effectiveIv,

            boolean clampedToFirstRelease,
            boolean overlapsDataset,

            String jiraAffectedVersions,
            String jiraFixVersions
    ) {
    }

    private record CsvTable(
            Map<String, Integer> columns,
            List<List<String>> rows
    ) {
    }
}
