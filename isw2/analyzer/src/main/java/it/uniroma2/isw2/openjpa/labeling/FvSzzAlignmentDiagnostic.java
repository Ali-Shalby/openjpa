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

public final class FvSzzAlignmentDiagnostic {

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

    private FvSzzAlignmentDiagnostic() {
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

        Map<String, ResolvedRelease> releaseByVersion =
                buildReleaseMap(
                        releases
                );

        Map<String, Boolean> ancestryCache =
                new HashMap<>();

        List<AlignmentRow> alignment =
                align(
                        repository,
                        releases,
                        releaseByVersion,
                        defectByIssue,
                        szzByIssue,
                        ancestryCache
                );

        validateAlignment(
                alignment,
                szzByIssue
        );

        printSummary(
                releases,
                stableByVersion,
                alignment
        );
    }

    private static List<AlignmentRow> align(
            Path repository,
            List<ResolvedRelease> releases,
            Map<String, ResolvedRelease> releaseByVersion,
            Map<String, Defect> defectByIssue,
            Map<String, List<SzzRow>> szzByIssue,
            Map<String, Boolean> ancestryCache
    ) throws IOException, InterruptedException {

        List<AlignmentRow> result =
                new ArrayList<>();

        List<String> issueKeys =
                szzByIssue.keySet()
                        .stream()
                        .sorted()
                        .toList();

        int processed = 0;

        for (String issueKey : issueKeys) {

            Defect defect =
                    defectByIssue.get(issueKey);

            if (defect == null) {

                throw new IllegalStateException(
                        "SZZ issue missing from defect catalog: "
                                + issueKey
                );
            }

            List<SzzRow> rows =
                    szzByIssue.get(issueKey);

            Set<String> fixIds =
                    rows.stream()
                            .map(
                                    SzzRow::fixCommitId
                            )
                            .collect(
                                    Collectors.toCollection(
                                            LinkedHashSet::new
                                    )
                            );

            /*
             * Map all Jira FixVersions that belong to the
             * validated 42-release timeline and select the
             * earliest one.
             */
            List<ResolvedRelease> jiraFixVersions =
                    mapVersions(
                            splitVersions(
                                    defect.fixVersions()
                            ),
                            releaseByVersion
                    );

            ResolvedRelease jiraFvRelease =
                    jiraFixVersions.isEmpty()
                            ? null
                            : jiraFixVersions.getFirst();

            ResolvedRelease fvRelease =
                    jiraFvRelease;

            String fvSource =
                    jiraFvRelease == null
                            ? "UNRESOLVED"
                            : "JIRA_FIX_VERSION";

            /*
             * First test the SZZ-producing fix commits against
             * the Jira FV, if one exists.
             */
            Set<String> alignedFixIds =
                    alignedFixIds(
                            repository,
                            fixIds,
                            fvRelease,
                            ancestryCache
                    );

            /*
             * Important safeguard.
             *
             * Jira remains the primary source for FV, but a
             * mapped Jira FV cannot be used for SZZ labeling if
             * NONE of the validated SZZ-producing fix commits
             * is contained in that release snapshot.
             *
             * Two situations therefore trigger Git fallback:
             *
             * 1. no usable Jira FV;
             * 2. Jira FV exists, but zero SZZ fix commits are
             *    contained in that release.
             *
             * In both situations, FV becomes the earliest
             * official release snapshot containing at least
             * one SZZ-producing fix commit.
             */
            if (fvRelease == null
                    || alignedFixIds.isEmpty()) {

                FallbackCandidate fallback =
                        findGitFallback(
                                repository,
                                releases,
                                fixIds,
                                ancestryCache
                        );

                if (fallback != null) {

                    fvRelease =
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
                                    fvRelease,
                                    ancestryCache
                            );

                } else {

                    fvRelease =
                            null;

                    fvSource =
                            "UNRESOLVED";

                    alignedFixIds =
                            Set.of();
                }
            }

            int alignedRows = 0;

            for (SzzRow row : rows) {

                if (alignedFixIds.contains(
                        row.fixCommitId()
                )) {

                    alignedRows++;
                }
            }

            result.add(
                    new AlignmentRow(
                            issueKey,
                            fvRelease == null
                                    ? null
                                    : fvRelease.index(),
                            fvRelease == null
                                    ? ""
                                    : fvRelease.version(),
                            fvSource,
                            fixIds.size(),
                            alignedFixIds.size(),
                            rows.size(),
                            alignedRows,
                            alignedRows > 0
                    )
            );

            processed++;

            if (processed % 50 == 0
                    || processed == issueKeys.size()) {

                System.out.printf(
                        "FV/SZZ progress: %d / %d%n",
                        processed,
                        issueKeys.size()
                );
            }
        }

        return List.copyOf(result);
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

        return Set.copyOf(result);
    }

    private static FallbackCandidate findGitFallback(
            Path repository,
            List<ResolvedRelease> releases,
            Set<String> fixIds,
            Map<String, Boolean> ancestryCache
    ) throws IOException, InterruptedException {

        FallbackCandidate best =
                null;

        /*
         * For each SZZ-producing fix commit, find the first
         * official release snapshot that contains it.
         *
         * Across all fixes belonging to the defect, retain the
         * earliest such release.
         */
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
                cache.get(key);

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
                        .redirectErrorStream(true)
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

            result = true;

        } else if (exitCode == 1) {

            result = false;

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

                /*
                 * Stable releases already have a validated
                 * ReleaseCommit in release_catalog.csv.
                 */
                snapshotCommit =
                        stable.releaseCommit();

                source =
                        "VALIDATED_STABLE_CATALOG";

            } else {

                /*
                 * Milestones/betas are present only in the RAW
                 * Jira release timeline. Resolve their snapshot
                 * with the same DATE_CUTOFF principle.
                 */
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
                        "Resolved raw snapshot is after ReleaseDate: "
                                + raw.version()
                                + " | release="
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

        return List.copyOf(result);
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

        return OffsetDateTime.parse(output);
    }

    private static void validateStableSnapshots(
            List<ResolvedRelease> releases,
            Map<String, StableRelease> stableByVersion
    ) {

        int checked = 0;

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
                    "Not all stable snapshots were validated. "
                            + "Expected="
                            + stableByVersion.size()
                            + ", checked="
                            + checked
            );
        }
    }

    private static void validateAlignment(
            List<AlignmentRow> alignment,
            Map<String, List<SzzRow>> szzByIssue
    ) {

        if (alignment.size()
                != szzByIssue.size()) {

            throw new IllegalStateException(
                    "Alignment row count mismatch."
            );
        }

        Set<String> issueKeys =
                new HashSet<>();

        for (AlignmentRow row : alignment) {

            if (!issueKeys.add(
                    row.issueKey()
            )) {

                throw new IllegalStateException(
                        "Duplicate alignment issue: "
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
                        "Unexpected FV source: "
                                + row.fvSource()
                );
            }

            if ("UNRESOLVED"
                    .equals(
                            row.fvSource()
                    )) {

                if (row.fv() != null
                        || row.hasAlignedEvidence()) {

                    throw new IllegalStateException(
                            "Unresolved FV contains resolved data: "
                                    + row.issueKey()
                    );
                }
            }

            if (row.szzFixesAligned()
                    > row.szzFixesTotal()) {

                throw new IllegalStateException(
                        "Aligned fix count exceeds total for "
                                + row.issueKey()
                );
            }

            if (row.szzRowsAligned()
                    > row.szzRowsTotal()) {

                throw new IllegalStateException(
                        "Aligned row count exceeds total for "
                                + row.issueKey()
                );
            }

            if (row.hasAlignedEvidence()
                    != (row.szzRowsAligned() > 0)) {

                throw new IllegalStateException(
                        "HasAlignedEvidence mismatch for "
                                + row.issueKey()
                );
            }

            /*
             * After the fallback logic, a resolved FV must
             * contain at least one SZZ evidence row.
             */
            if (!"UNRESOLVED"
                    .equals(
                            row.fvSource()
                    )
                    && !row.hasAlignedEvidence()) {

                throw new IllegalStateException(
                        "Resolved FV has zero aligned SZZ evidence: "
                                + row.issueKey()
                );
            }
        }
    }

    private static void printSummary(
            List<ResolvedRelease> releases,
            Map<String, StableRelease> stableByVersion,
            List<AlignmentRow> alignment
    ) {

        long fvFromJira =
                alignment.stream()
                        .filter(row ->
                                "JIRA_FIX_VERSION"
                                        .equals(
                                                row.fvSource()
                                        )
                        )
                        .count();

        long fvFromGitNoJira =
                alignment.stream()
                        .filter(row ->
                                "GIT_CONTAINMENT_NO_JIRA_FV"
                                        .equals(
                                                row.fvSource()
                                        )
                        )
                        .count();

        long fvFromGitJiraMismatch =
                alignment.stream()
                        .filter(row ->
                                "GIT_CONTAINMENT_JIRA_MISMATCH"
                                        .equals(
                                                row.fvSource()
                                        )
                        )
                        .count();

        long fvFromGit =
                fvFromGitNoJira
                        + fvFromGitJiraMismatch;

        long fvUnresolved =
                alignment.stream()
                        .filter(row ->
                                "UNRESOLVED"
                                        .equals(
                                                row.fvSource()
                                        )
                        )
                        .count();

        long zeroAligned =
                alignment.stream()
                        .filter(row ->
                                !row.hasAlignedEvidence()
                        )
                        .count();

        long partialAligned =
                alignment.stream()
                        .filter(row ->
                                row.szzFixesAligned() > 0
                                        && row.szzFixesAligned()
                                        < row.szzFixesTotal()
                        )
                        .count();

        long allAligned =
                alignment.stream()
                        .filter(row ->
                                row.szzFixesAligned()
                                        == row.szzFixesTotal()
                        )
                        .count();

        long rowsBefore =
                alignment.stream()
                        .mapToLong(
                                AlignmentRow::szzRowsTotal
                        )
                        .sum();

        long rowsAfter =
                alignment.stream()
                        .mapToLong(
                                AlignmentRow::szzRowsAligned
                        )
                        .sum();

        long rawDateCutoff =
                releases.stream()
                        .filter(row ->
                                "DATE_CUTOFF_RAW"
                                        .equals(
                                                row.snapshotSource()
                                        )
                        )
                        .count();

        System.out.println();
        System.out.println(
                "===== FV / SZZ ALIGNMENT DIAGNOSTIC ====="
        );

        System.out.println(
                "Raw release timeline             : "
                        + releases.size()
        );

        System.out.println(
                "Validated stable snapshots       : "
                        + stableByVersion.size()
        );

        System.out.println(
                "Raw-only DATE_CUTOFF snapshots   : "
                        + rawDateCutoff
        );

        System.out.println();

        System.out.println(
                "Defects with SZZ                 : "
                        + alignment.size()
        );

        System.out.println(
                "FV from Jira                     : "
                        + fvFromJira
        );

        System.out.println(
                "FV from Git containment          : "
                        + fvFromGit
        );

        System.out.println(
                "  - no usable Jira FV            : "
                        + fvFromGitNoJira
        );

        System.out.println(
                "  - Jira FV/SZZ mismatch         : "
                        + fvFromGitJiraMismatch
        );

        System.out.println(
                "FV unresolved                    : "
                        + fvUnresolved
        );

        System.out.println();

        System.out.println(
                "Defects with ZERO aligned SZZ    : "
                        + zeroAligned
        );

        System.out.println(
                "Defects with PARTIAL alignment   : "
                        + partialAligned
        );

        System.out.println(
                "Defects with ALL fixes aligned   : "
                        + allAligned
        );

        System.out.println(
                "SZZ rows before FV alignment     : "
                        + rowsBefore
        );

        System.out.println(
                "SZZ rows after FV alignment      : "
                        + rowsAfter
        );

        System.out.println();

        System.out.println(
                "===== GIT-CONTAINMENT FV ====="
        );

        alignment.stream()
                .filter(row ->
                        row.fvSource()
                                .startsWith(
                                        "GIT_CONTAINMENT"
                                )
                )
                .limit(100)
                .forEach(row ->
                        System.out.printf(
                                "%s | FV=%d/%s | source=%s | "
                                        + "fixes=%d | aligned=%d | "
                                        + "rows=%d -> %d%n",
                                row.issueKey(),
                                row.fv(),
                                row.fvVersion(),
                                row.fvSource(),
                                row.szzFixesTotal(),
                                row.szzFixesAligned(),
                                row.szzRowsTotal(),
                                row.szzRowsAligned()
                        )
                );

        System.out.println();

        System.out.println(
                "===== ZERO-ALIGNMENT CASES ====="
        );

        alignment.stream()
                .filter(row ->
                        !row.hasAlignedEvidence()
                )
                .limit(100)
                .forEach(row ->
                        System.out.printf(
                                "%s | FV=%s/%s | source=%s | "
                                        + "fixes=%d | aligned=%d | "
                                        + "rows=%d -> %d%n",
                                row.issueKey(),
                                row.fv() == null
                                        ? ""
                                        : row.fv().toString(),
                                row.fvVersion(),
                                row.fvSource(),
                                row.szzFixesTotal(),
                                row.szzFixesAligned(),
                                row.szzRowsTotal(),
                                row.szzRowsAligned()
                        )
                );

        System.out.println();

        System.out.println(
                "===== PARTIAL-ALIGNMENT SAMPLE ====="
        );

        alignment.stream()
                .filter(row ->
                        row.szzFixesAligned() > 0
                                && row.szzFixesAligned()
                                < row.szzFixesTotal()
                )
                .limit(50)
                .forEach(row ->
                        System.out.printf(
                                "%s | FV=%d/%s | source=%s | "
                                        + "fixes=%d -> %d | rows=%d -> %d%n",
                                row.issueKey(),
                                row.fv(),
                                row.fvVersion(),
                                row.fvSource(),
                                row.szzFixesTotal(),
                                row.szzFixesAligned(),
                                row.szzRowsTotal(),
                                row.szzRowsAligned()
                        )
                );

        System.out.println(
                "============================================="
        );
    }

    private static List<RawRelease> readRawReleases(
            Path input
    ) throws IOException {

        CsvTable table =
                readCsv(input);

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
                        "Incomplete raw release row."
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

        return List.copyOf(result);
    }

    private static Map<String, StableRelease> readStableReleases(
            Path input
    ) throws IOException {

        CsvTable table =
                readCsv(input);

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

        return Map.copyOf(result);
    }

    private static Map<String, Defect> readDefects(
            Path input
    ) throws IOException {

        CsvTable table =
                readCsv(input);

        Map<String, Defect> result =
                new HashMap<>();

        for (List<String> row : table.rows()) {

            String issueKey =
                    firstValue(
                            table,
                            row,
                            "IssueKey"
                    );

            String fixVersions =
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

            Defect previous =
                    result.put(
                            issueKey,
                            new Defect(
                                    issueKey,
                                    fixVersions
                            )
                    );

            if (previous != null) {

                throw new IllegalStateException(
                        "Duplicate defect: "
                                + issueKey
                );
            }
        }

        return Map.copyOf(result);
    }

    private static Map<String, List<SzzRow>> readSzzEvidence(
            Path input
    ) throws IOException {

        CsvTable table =
                readCsv(input);

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

        return Map.copyOf(result);
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
                        "Duplicate raw release version: "
                                + release.version()
                );
            }
        }

        return Map.copyOf(result);
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

        return List.copyOf(result);
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

    private static String runGit(
            Path repository,
            String... arguments
    ) throws IOException, InterruptedException {

        List<String> command =
                new ArrayList<>();

        command.add("git");
        command.add("-C");
        command.add(repository.toString());

        for (String argument : arguments) {

            command.add(argument);
        }

        Process process =
                new ProcessBuilder(command)
                        .redirectErrorStream(true)
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

        if (!Files.isRegularFile(input)) {

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
                        headers.get(index).trim(),
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
                            parseCsvLine(line)
                    );
                }
            }

            return new CsvTable(
                    Map.copyOf(columns),
                    List.copyOf(rows)
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
                    table.columns().get(
                            column
                    );

            if (index == null
                    || index >= row.size()) {

                continue;
            }

            String value =
                    row.get(index).trim();

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

        boolean quoted = false;

        for (int index = 0;
             index < line.length();
             index++) {

            char character =
                    line.charAt(index);

            if (character == '"') {

                if (quoted
                        && index + 1 < line.length()
                        && line.charAt(index + 1)
                        == '"') {

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

            return value.substring(1);
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

    private record AlignmentRow(
            String issueKey,
            Integer fv,
            String fvVersion,
            String fvSource,
            int szzFixesTotal,
            int szzFixesAligned,
            int szzRowsTotal,
            int szzRowsAligned,
            boolean hasAlignedEvidence
    ) {
    }

    private record CsvTable(
            Map<String, Integer> columns,
            List<List<String>> rows
    ) {
    }
}
