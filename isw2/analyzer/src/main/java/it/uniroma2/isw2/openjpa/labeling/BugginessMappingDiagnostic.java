package it.uniroma2.isw2.openjpa.labeling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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

public final class BugginessMappingDiagnostic {

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

    private static final Path CLASS_INVENTORY =
            Path.of(
                    "isw2",
                    "datasets",
                    "java_class_inventory.csv"
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

    private static final Path OUTPUT =
            Path.of(
                    "isw2",
                    "results",
                    "labeling",
                    "bugginess_mapping_stage1.csv"
            );

    private BugginessMappingDiagnostic() {
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

        System.out.println(
                "===== BUGGINESS MAPPING STAGE 1 ====="
        );

        System.out.println(
                "Repository: "
                        + repository
        );

        System.out.println();

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

        Map<String, ResolvedRelease> releaseByVersion =
                buildReleaseMap(
                        releases
                );

        List<InventoryRow> inventory =
                readInventory(
                        repository.resolve(
                                CLASS_INVENTORY
                        ),
                        releaseByVersion
                );

        Map<Integer, ReleaseInventory> inventoryByDatasetRelease =
                buildInventoryIndex(
                        inventory
                );

        Map<String, Defect> defects =
                readDefects(
                        repository.resolve(
                                DEFECT_CATALOG
                        )
                );

        Map<String, List<SzzRow>> szzByIssue =
                readSzz(
                        repository.resolve(
                                SZZ_EVIDENCE
                        )
                );

        /*
         * Instead of running thousands of
         * git merge-base --is-ancestor commands,
         * collect all relevant SZZ commits and build
         * a reachability index for the release snapshots.
         */
        Set<String> relevantCommits =
                collectRelevantCommits(
                        szzByIssue
                );

        ReachabilityIndex reachability =
                buildReachabilityIndex(
                        repository,
                        releases,
                        relevantCommits
                );

        Map<String, Interval> intervals =
                buildIntervals(
                        releases,
                        releaseByVersion,
                        defects,
                        szzByIssue,
                        reachability
                );

        MappingResult mapping =
                mapExactStage(
                        inventoryByDatasetRelease,
                        intervals,
                        szzByIssue,
                        reachability
                );

        validate(
                inventory,
                intervals,
                szzByIssue,
                mapping
        );

        Path output =
                repository.resolve(
                        OUTPUT
                );

        writeDiagnostics(
                mapping.rows(),
                output
        );

        printSummary(
                inventory,
                intervals,
                szzByIssue,
                mapping,
                output
        );
    }

    private static ReachabilityIndex buildReachabilityIndex(
            Path repository,
            List<ResolvedRelease> releases,
            Set<String> relevantCommits
    ) throws IOException, InterruptedException {

        Map<String, Set<String>> bySnapshot =
                new LinkedHashMap<>();

        List<String> uniqueSnapshots =
                releases.stream()
                        .map(
                                ResolvedRelease::snapshotCommit
                        )
                        .map(
                                BugginessMappingDiagnostic::normalizeCommit
                        )
                        .distinct()
                        .toList();

        System.out.println(
                "Building Git reachability index..."
        );

        System.out.println(
                "Relevant SZZ commits : "
                        + relevantCommits.size()
        );

        System.out.println(
                "Unique snapshots     : "
                        + uniqueSnapshots.size()
        );

        int done = 0;

        for (String snapshot : uniqueSnapshots) {

            Set<String> contained =
                    collectRelevantAncestors(
                            repository,
                            snapshot,
                            relevantCommits
                    );

            bySnapshot.put(
                    snapshot,
                    contained
            );

            done++;

            System.out.printf(
                    "Reachability snapshots: %d / %d | relevant ancestors=%d%n",
                    done,
                    uniqueSnapshots.size(),
                    contained.size()
            );
        }

        System.out.println(
                "Git reachability index complete."
        );

        System.out.println();

        return new ReachabilityIndex(
                Map.copyOf(
                        bySnapshot
                )
        );
    }

    private static Set<String> collectRelevantAncestors(
            Path repository,
            String snapshotCommit,
            Set<String> relevantCommits
    ) throws IOException, InterruptedException {

        Process process =
                new ProcessBuilder(
                        "git",
                        "-C",
                        repository.toString(),
                        "rev-list",
                        snapshotCommit
                )
                        .redirectErrorStream(
                                true
                        )
                        .start();

        Set<String> contained =
                new HashSet<>();

        try (BufferedReader reader =
                     process.inputReader(
                             StandardCharsets.UTF_8
                     )) {

            String line;

            while ((line = reader.readLine())
                    != null) {

                String commit =
                        normalizeCommit(
                                line
                        );

                if (relevantCommits.contains(
                        commit
                )) {

                    contained.add(
                            commit
                    );
                }
            }
        }

        int exitCode =
                process.waitFor();

        if (exitCode != 0) {

            throw new IOException(
                    "git rev-list failed for snapshot "
                            + snapshotCommit
            );
        }

        return Set.copyOf(
                contained
        );
    }

    private static Set<String> collectRelevantCommits(
            Map<String, List<SzzRow>> szzByIssue
    ) {

        Set<String> result =
                new HashSet<>();

        for (List<SzzRow> rows
                : szzByIssue.values()) {

            for (SzzRow row : rows) {

                result.add(
                        normalizeCommit(
                                row.fixCommitId()
                        )
                );

                result.add(
                        normalizeCommit(
                                row.bugIntroducingCommitId()
                        )
                );
            }
        }

        return Set.copyOf(
                result
        );
    }

    private static Map<String, Interval> buildIntervals(
            List<ResolvedRelease> releases,
            Map<String, ResolvedRelease> releaseByVersion,
            Map<String, Defect> defects,
            Map<String, List<SzzRow>> szzByIssue,
            ReachabilityIndex reachability
    ) {

        Map<String, Interval> result =
                new LinkedHashMap<>();

        List<String> issueKeys =
                szzByIssue
                        .keySet()
                        .stream()
                        .sorted()
                        .toList();

        int processed = 0;

        for (String issueKey : issueKeys) {

            Defect defect =
                    defects.get(
                            issueKey
                    );

            if (defect == null) {

                throw new IllegalStateException(
                        "SZZ issue missing from defect catalog: "
                                + issueKey
                );
            }

            List<SzzRow> issueRows =
                    szzByIssue.get(
                            issueKey
                    );

            Set<String> fixIds =
                    issueRows.stream()
                            .map(
                                    SzzRow::fixCommitId
                            )
                            .map(
                                    BugginessMappingDiagnostic
                                            ::normalizeCommit
                            )
                            .collect(
                                    Collectors.toCollection(
                                            LinkedHashSet::new
                                    )
                            );

            /*
             * ----------------------------
             * Opening Version
             * ----------------------------
             */

            ResolvedRelease opening =
                    findOpeningRelease(
                            releases,
                            defect.createdAt()
                    );

            Integer ov =
                    opening == null
                            ? null
                            : opening.index();

            /*
             * ----------------------------
             * Observed IV
             * ----------------------------
             */

            List<ResolvedRelease> affected =
                    mapVersions(
                            splitVersions(
                                    defect.affectedVersions()
                            ),
                            releaseByVersion
                    );

            ResolvedRelease observedIvRelease =
                    affected.isEmpty()
                            ? null
                            : affected.get(0);

            Integer observedIv =
                    observedIvRelease == null
                            ? null
                            : observedIvRelease.index();

            /*
             * ----------------------------
             * Jira FV
             * ----------------------------
             */

            List<ResolvedRelease> jiraFixes =
                    mapVersions(
                            splitVersions(
                                    defect.fixVersions()
                            ),
                            releaseByVersion
                    );

            ResolvedRelease jiraFv =
                    jiraFixes.isEmpty()
                            ? null
                            : jiraFixes.get(0);

            /*
             * ----------------------------
             * Effective FV
             * ----------------------------
             */

            ResolvedRelease effectiveFv =
                    jiraFv;

            String fvSource =
                    jiraFv == null
                            ? "UNRESOLVED"
                            : "JIRA_FIX_VERSION";

            Set<String> alignedFixIds =
                    alignedFixIds(
                            fixIds,
                            effectiveFv,
                            reachability
                    );

            if (effectiveFv == null
                    || alignedFixIds.isEmpty()) {

                ResolvedRelease fallback =
                        findGitFallback(
                                releases,
                                fixIds,
                                reachability
                        );

                if (fallback == null) {

                    throw new IllegalStateException(
                            "No effective FV for SZZ defect: "
                                    + issueKey
                    );
                }

                fvSource =
                        jiraFv == null
                                ? "GIT_CONTAINMENT_NO_JIRA_FV"
                                : "GIT_CONTAINMENT_JIRA_MISMATCH";

                effectiveFv =
                        fallback;

                alignedFixIds =
                        alignedFixIds(
                                fixIds,
                                effectiveFv,
                                reachability
                        );
            }

            if (alignedFixIds.isEmpty()) {

                throw new IllegalStateException(
                        "Effective FV has zero aligned fixes: "
                                + issueKey
                );
            }

            int fv =
                    effectiveFv.index();

            /*
             * ----------------------------
             * Effective IV
             * ----------------------------
             */

            boolean observedIvConsistent =
                    observedIv != null
                            && ov != null
                            && observedIv <= ov
                            && ov <= fv
                            && observedIv < fv;

            int effectiveIv;
            String ivSource;

            if (observedIvConsistent) {

                effectiveIv =
                        observedIv;

                ivSource =
                        "JIRA_AFFECTED_VERSION";

            } else if (ov == null) {

                effectiveIv =
                        1;

                ivSource =
                        "PRE_FIRST_RELEASE";

            } else if (ov <= fv) {

                int distance =
                        fv - ov;

                if (distance == 0) {

                    distance =
                            1;
                }

                double lav =
                        fv
                                - distance
                                * P_TOTAL;

                effectiveIv =
                        Math.max(
                                1,
                                (int) Math.ceil(
                                        lav
                                )
                        );

                ivSource =
                        "PROPORTION_TOTAL";

            } else {

                throw new IllegalStateException(
                        "Cannot resolve IV because OV > EffectiveFV for "
                                + issueKey
                                + " | OV="
                                + ov
                                + " | FV="
                                + fv
                );
            }

            if (effectiveIv > fv) {

                throw new IllegalStateException(
                        "EffectiveIV > EffectiveFV for "
                                + issueKey
                                + " | IV="
                                + effectiveIv
                                + " | FV="
                                + fv
                );
            }

            result.put(
                    issueKey,
                    new Interval(
                            issueKey,
                            effectiveIv,
                            fv,
                            ivSource,
                            fvSource,
                            Set.copyOf(
                                    alignedFixIds
                            )
                    )
            );

            processed++;

            if (processed % 50 == 0
                    || processed == issueKeys.size()) {

                System.out.printf(
                        "Intervals: %d / %d%n",
                        processed,
                        issueKeys.size()
                );
            }
        }

        System.out.println(
                "Interval reconstruction complete."
        );

        System.out.println();

        return Map.copyOf(
                result
        );
    }

    private static MappingResult mapExactStage(
            Map<Integer, ReleaseInventory> inventoryByDatasetRelease,
            Map<String, Interval> intervals,
            Map<String, List<SzzRow>> szzByIssue,
            ReachabilityIndex reachability
    ) {

        List<MappingRow> rows =
                new ArrayList<>();

        Set<String> buggyObservationKeys =
                new LinkedHashSet<>();

        Set<String> mappedDefects =
                new LinkedHashSet<>();

        long candidatePairs = 0;
        long bicNotContained = 0;
        long exactPath = 0;
        long pathAbsent = 0;

        /*
         * These are ONLY diagnostic counters.
         *
         * We are not accepting same-filename candidates as
         * valid mappings in this stage.
         */
        long sameNameZero = 0;
        long sameNameOne = 0;
        long sameNameMany = 0;

        /*
         * Also diagnostic only.
         *
         * FixedFilePath is NOT used to label the class.
         */
        long fixedPathPresentWhenBlamedAbsent = 0;

        List<ReleaseInventory> datasetReleases =
                inventoryByDatasetRelease
                        .values()
                        .stream()
                        .sorted(
                                Comparator.comparingInt(
                                        ReleaseInventory
                                                ::datasetReleaseIndex
                                )
                        )
                        .toList();

        List<String> issueKeys =
                intervals
                        .keySet()
                        .stream()
                        .sorted()
                        .toList();

        int processed = 0;

        for (String issueKey : issueKeys) {

            Interval interval =
                    intervals.get(
                            issueKey
                    );

            List<SzzRow> alignedRows =
                    szzByIssue
                            .get(
                                    issueKey
                            )
                            .stream()
                            .filter(row ->
                                    interval
                                            .alignedFixIds()
                                            .contains(
                                                    normalizeCommit(
                                                            row.fixCommitId()
                                                    )
                                            )
                            )
                            .toList();

            for (SzzRow szz : alignedRows) {

                for (ReleaseInventory release
                        : datasetReleases) {

                    int rawIndex =
                            release.rawReleaseIndex();

                    /*
                     * Buggy interval:
                     *
                     * [IV, FV)
                     */
                    if (rawIndex
                            < interval.effectiveIv()
                            || rawIndex
                            >= interval.effectiveFv()) {

                        continue;
                    }

                    candidatePairs++;

                    /*
                     * SZZ gives us a concrete BIC.
                     *
                     * Even if the Jira/Proportion interval says
                     * that the defect includes a release, the
                     * BIC must already exist in that release.
                     */
                    String bic =
                            normalizeCommit(
                                    szz.bugIntroducingCommitId()
                            );

                    if (!reachability.contains(
                            release.commitId(),
                            bic
                    )) {

                        bicNotContained++;

                        rows.add(
                                MappingRow.unmapped(
                                        szz,
                                        release,
                                        interval,
                                        "BIC_NOT_CONTAINED",
                                        0,
                                        ""
                                )
                        );

                        continue;
                    }

                    /*
                     * Stage 1 accepts ONLY the exact SZZ
                     * BlamedFilePath.
                     */
                    String blamedPath =
                            normalizePath(
                                    szz.blamedFilePath()
                            );

                    InventoryRow exact =
                            release
                                    .byPath()
                                    .get(
                                            blamedPath
                                    );

                    if (exact != null) {

                        exactPath++;

                        mappedDefects.add(
                                issueKey
                        );

                        buggyObservationKeys.add(
                                observationKey(
                                        exact
                                )
                        );

                        rows.add(
                                MappingRow.mapped(
                                        szz,
                                        release,
                                        interval,
                                        exact.classPath(),
                                        "EXACT_BLAMED_PATH",
                                        1,
                                        exact.classPath()
                                )
                        );

                        continue;
                    }

                    /*
                     * Path is absent.
                     *
                     * DO NOT infer a mapping yet.
                     *
                     * We only collect evidence to understand
                     * what actually happens in OpenJPA.
                     */
                    pathAbsent++;

                    String fileName =
                            fileName(
                                    blamedPath
                            );

                    List<InventoryRow> sameNameCandidates =
                            release
                                    .byFileName()
                                    .getOrDefault(
                                            fileName,
                                            List.of()
                                    );

                    int sameNameCount =
                            sameNameCandidates.size();

                    if (sameNameCount == 0) {

                        sameNameZero++;

                    } else if (sameNameCount == 1) {

                        sameNameOne++;

                    } else {

                        sameNameMany++;
                    }

                    /*
                     * Diagnostic only:
                     *
                     * is the path touched by the fixing commit
                     * present in this release?
                     *
                     * We do NOT use it to map BUGGY.
                     */
                    String fixedPath =
                            normalizePath(
                                    szz.fixedFilePath()
                            );

                    boolean fixedPathPresent =
                            release
                                    .byPath()
                                    .containsKey(
                                            fixedPath
                                    );

                    if (fixedPathPresent) {

                        fixedPathPresentWhenBlamedAbsent++;
                    }

                    String candidatePaths =
                            sameNameCandidates
                                    .stream()
                                    .map(
                                            InventoryRow::classPath
                                    )
                                    .sorted()
                                    .collect(
                                            Collectors.joining(
                                                    "|"
                                            )
                                    );

                    rows.add(
                            MappingRow.unmapped(
                                    szz,
                                    release,
                                    interval,
                                    "BLAMED_PATH_ABSENT",
                                    sameNameCount,
                                    candidatePaths
                            )
                    );
                }
            }

            processed++;

            if (processed % 25 == 0
                    || processed == issueKeys.size()) {

                System.out.printf(
                        "Mapping defects: %d / %d | "
                                + "candidates=%d | "
                                + "exact=%d | "
                                + "unresolvedPath=%d%n",
                        processed,
                        issueKeys.size(),
                        candidatePairs,
                        exactPath,
                        pathAbsent
                );
            }
        }

        return new MappingResult(
                List.copyOf(
                        rows
                ),
                Set.copyOf(
                        buggyObservationKeys
                ),
                Set.copyOf(
                        mappedDefects
                ),
                candidatePairs,
                bicNotContained,
                exactPath,
                pathAbsent,
                sameNameZero,
                sameNameOne,
                sameNameMany,
                fixedPathPresentWhenBlamedAbsent
        );
    }

    private static Set<String> alignedFixIds(
            Set<String> fixIds,
            ResolvedRelease fv,
            ReachabilityIndex reachability
    ) {

        if (fv == null) {

            return Set.of();
        }

        Set<String> result =
                new LinkedHashSet<>();

        for (String fixId : fixIds) {

            if (reachability.contains(
                    fv.snapshotCommit(),
                    fixId
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

    private static ResolvedRelease findGitFallback(
            List<ResolvedRelease> releases,
            Set<String> fixIds,
            ReachabilityIndex reachability
    ) {

        for (ResolvedRelease release : releases) {

            for (String fixId : fixIds) {

                if (reachability.contains(
                        release.snapshotCommit(),
                        fixId
                )) {

                    return release;
                }
            }
        }

        return null;
    }

    private static void validate(
            List<InventoryRow> inventory,
            Map<String, Interval> intervals,
            Map<String, List<SzzRow>> szzByIssue,
            MappingResult mapping
    ) {

        if (inventory.size()
                != 12836) {

            throw new IllegalStateException(
                    "Expected 12836 production observations, found "
                            + inventory.size()
            );
        }

        if (intervals.size()
                != 766) {

            throw new IllegalStateException(
                    "Expected 766 effective defect intervals, found "
                            + intervals.size()
            );
        }

        if (intervals.size()
                != szzByIssue.size()) {

            throw new IllegalStateException(
                    "Interval/SZZ defect mismatch."
            );
        }

        if (intervals
                .values()
                .stream()
                .anyMatch(interval ->
                        interval.effectiveIv()
                                > interval.effectiveFv()
                )) {

            throw new IllegalStateException(
                    "Invalid effective interval found."
            );
        }

        long classified =
                mapping.bicNotContained()
                        + mapping.exactPath()
                        + mapping.pathAbsent();

        if (classified
                != mapping.candidatePairs()) {

            throw new IllegalStateException(
                    "Mapping accounting mismatch: candidates="
                            + mapping.candidatePairs()
                            + ", classified="
                            + classified
            );
        }

        if (mapping.sameNameZero()
                + mapping.sameNameOne()
                + mapping.sameNameMany()
                != mapping.pathAbsent()) {

            throw new IllegalStateException(
                    "Unresolved-path filename accounting mismatch."
            );
        }

        Set<String> inventoryKeys =
                inventory.stream()
                        .map(
                                BugginessMappingDiagnostic
                                        ::observationKey
                        )
                        .collect(
                                Collectors.toSet()
                        );

        if (!inventoryKeys.containsAll(
                mapping.buggyObservationKeys()
        )) {

            throw new IllegalStateException(
                    "A mapped BUGGY pair is absent from inventory."
            );
        }
    }

    private static void printSummary(
            List<InventoryRow> inventory,
            Map<String, Interval> intervals,
            Map<String, List<SzzRow>> szzByIssue,
            MappingResult mapping,
            Path output
    ) {

        long alignedSzzRows =
                szzByIssue
                        .entrySet()
                        .stream()
                        .mapToLong(entry -> {

                            Interval interval =
                                    intervals.get(
                                            entry.getKey()
                                    );

                            return entry
                                    .getValue()
                                    .stream()
                                    .filter(row ->
                                            interval
                                                    .alignedFixIds()
                                                    .contains(
                                                            normalizeCommit(
                                                                    row.fixCommitId()
                                                            )
                                                    )
                                    )
                                    .count();
                        })
                        .sum();

        long overlappingDefects =
                intervals
                        .values()
                        .stream()
                        .filter(interval ->
                                interval.effectiveIv()
                                        < interval.effectiveFv()
                        )
                        .filter(interval ->
                                inventory
                                        .stream()
                                        .anyMatch(row ->
                                                row.rawReleaseIndex()
                                                        >= interval.effectiveIv()
                                                        && row.rawReleaseIndex()
                                                        < interval.effectiveFv()
                                        )
                        )
                        .count();

        System.out.println();

        System.out.println(
                "===== BUGGINESS MAPPING STAGE 1 RESULT ====="
        );

        System.out.println(
                "P_TOTAL                         : "
                        + P_TOTAL
        );

        System.out.println(
                "Production observations         : "
                        + inventory.size()
        );

        System.out.println(
                "Effective defect intervals      : "
                        + intervals.size()
        );

        System.out.println(
                "Aligned SZZ evidence rows       : "
                        + alignedSzzRows
        );

        System.out.println(
                "Defects overlapping Dataset A   : "
                        + overlappingDefects
        );

        System.out.println();

        System.out.println(
                "Candidate evidence-release pairs: "
                        + mapping.candidatePairs()
        );

        System.out.println(
                "BIC_NOT_CONTAINED               : "
                        + mapping.bicNotContained()
        );

        System.out.println(
                "EXACT_BLAMED_PATH               : "
                        + mapping.exactPath()
        );

        System.out.println(
                "BLAMED_PATH_ABSENT              : "
                        + mapping.pathAbsent()
        );

        System.out.println();

        System.out.println(
                "Among BLAMED_PATH_ABSENT only:"
        );

        System.out.println(
                "  same filename candidates = 0  : "
                        + mapping.sameNameZero()
        );

        System.out.println(
                "  same filename candidates = 1  : "
                        + mapping.sameNameOne()
        );

        System.out.println(
                "  same filename candidates > 1  : "
                        + mapping.sameNameMany()
        );

        System.out.println(
                "  FixedFilePath present          : "
                        + mapping
                        .fixedPathPresentWhenBlamedAbsent()
        );

        System.out.println();

        System.out.println(
                "Stage-1 mapped defects           : "
                        + mapping.mappedDefects().size()
        );

        System.out.println(
                "Unique exact BUGGY pairs         : "
                        + mapping
                        .buggyObservationKeys()
                        .size()
        );

        System.out.println(
                "Diagnostic CSV                   : "
                        + output
        );

        System.out.println(
                "=============================================="
        );

        System.out.println();

        System.out.println(
                "First 30 BLAMED_PATH_ABSENT cases:"
        );

        mapping.rows()
                .stream()
                .filter(row ->
                        "BLAMED_PATH_ABSENT"
                                .equals(
                                        row.strategy()
                                )
                )
                .limit(30)
                .forEach(row ->
                        System.out.printf(
                                "%s | dataset=%d/%s | raw=%d | "
                                        + "blamed=%s | fixed=%s | "
                                        + "sameNameCandidates=%d | "
                                        + "candidates=[%s] | BIC=%s%n",

                                row.issueKey(),

                                row.datasetReleaseIndex(),
                                row.version(),

                                row.rawReleaseIndex(),

                                row.blamedFilePath(),
                                row.fixedFilePath(),

                                row.sameFileNameCandidateCount(),
                                row.sameFileNameCandidates(),

                                row.bugIntroducingCommitId()
                        )
                );
    }

    private static void writeDiagnostics(
            List<MappingRow> rows,
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
                    "IssueKey,"
                            + "DatasetReleaseIndex,"
                            + "Version,"
                            + "RawReleaseIndex,"
                            + "EffectiveIV,"
                            + "EffectiveFV,"
                            + "IVSource,"
                            + "FVSource,"
                            + "FixCommitId,"
                            + "FixedFilePath,"
                            + "BlamedFilePath,"
                            + "BugIntroducingCommitId,"
                            + "MappedClass,"
                            + "Strategy,"
                            + "SameFileNameCandidateCount,"
                            + "SameFileNameCandidates"
            );

            writer.newLine();

            for (MappingRow row : rows) {

                writer.write(
                        String.join(
                                ",",
                                csv(
                                        row.issueKey()
                                ),
                                Integer.toString(
                                        row.datasetReleaseIndex()
                                ),
                                csv(
                                        row.version()
                                ),
                                Integer.toString(
                                        row.rawReleaseIndex()
                                ),
                                Integer.toString(
                                        row.effectiveIv()
                                ),
                                Integer.toString(
                                        row.effectiveFv()
                                ),
                                csv(
                                        row.ivSource()
                                ),
                                csv(
                                        row.fvSource()
                                ),
                                csv(
                                        row.fixCommitId()
                                ),
                                csv(
                                        row.fixedFilePath()
                                ),
                                csv(
                                        row.blamedFilePath()
                                ),
                                csv(
                                        row.bugIntroducingCommitId()
                                ),
                                csv(
                                        row.mappedClass()
                                ),
                                csv(
                                        row.strategy()
                                ),
                                Integer.toString(
                                        row.sameFileNameCandidateCount()
                                ),
                                csv(
                                        row.sameFileNameCandidates()
                                )
                        )
                );

                writer.newLine();
            }
        }
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

            String snapshotCommit =
                    stable != null
                            ? stable.releaseCommit()
                            : resolveByDateCutoff(
                            repository,
                            raw.releaseDate()
                    );

            result.add(
                    new ResolvedRelease(
                            raw.index(),
                            raw.version(),
                            raw.releaseDate(),
                            normalizeCommit(
                                    snapshotCommit
                            )
                    )
            );
        }

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
                        "Duplicate raw release: "
                                + release.version()
                );
            }
        }

        return Map.copyOf(
                result
        );
    }

    private static Map<Integer, ReleaseInventory> buildInventoryIndex(
            List<InventoryRow> inventory
    ) {

        Map<Integer, List<InventoryRow>> grouped =
                inventory.stream()
                        .collect(
                                Collectors.groupingBy(
                                        InventoryRow
                                                ::datasetReleaseIndex,
                                        LinkedHashMap::new,
                                        Collectors.toList()
                                )
                        );

        Map<Integer, ReleaseInventory> result =
                new LinkedHashMap<>();

        for (Map.Entry<Integer, List<InventoryRow>> entry
                : grouped.entrySet()) {

            List<InventoryRow> rows =
                    entry.getValue();

            Set<String> versions =
                    rows.stream()
                            .map(
                                    InventoryRow::version
                            )
                            .collect(
                                    Collectors.toSet()
                            );

            Set<String> commits =
                    rows.stream()
                            .map(
                                    InventoryRow::commitId
                            )
                            .collect(
                                    Collectors.toSet()
                            );

            Set<Integer> rawIndices =
                    rows.stream()
                            .map(
                                    InventoryRow::rawReleaseIndex
                            )
                            .collect(
                                    Collectors.toSet()
                            );

            if (versions.size() != 1
                    || commits.size() != 1
                    || rawIndices.size() != 1) {

                throw new IllegalStateException(
                        "Inconsistent inventory release "
                                + entry.getKey()
                );
            }

            Map<String, InventoryRow> byPath =
                    new HashMap<>();

            Map<String, List<InventoryRow>> byFileNameMutable =
                    new HashMap<>();

            for (InventoryRow row : rows) {

                String path =
                        normalizePath(
                                row.classPath()
                        );

                if (byPath.put(
                        path,
                        row
                ) != null) {

                    throw new IllegalStateException(
                            "Duplicate production class path: "
                                    + row.datasetReleaseIndex()
                                    + " / "
                                    + row.classPath()
                    );
                }

                byFileNameMutable
                        .computeIfAbsent(
                                fileName(
                                        path
                                ),
                                ignored ->
                                        new ArrayList<>()
                        )
                        .add(
                                row
                        );
            }

            Map<String, List<InventoryRow>> byFileName =
                    new HashMap<>();

            for (Map.Entry<String, List<InventoryRow>> nameEntry
                    : byFileNameMutable.entrySet()) {

                byFileName.put(
                        nameEntry.getKey(),
                        List.copyOf(
                                nameEntry.getValue()
                        )
                );
            }

            result.put(
                    entry.getKey(),
                    new ReleaseInventory(
                            entry.getKey(),
                            versions
                                    .iterator()
                                    .next(),
                            normalizeCommit(
                                    commits
                                            .iterator()
                                            .next()
                            ),
                            rawIndices
                                    .iterator()
                                    .next(),
                            Map.copyOf(
                                    byPath
                            ),
                            Map.copyOf(
                                    byFileName
                            )
                    )
            );
        }

        if (result.size()
                != 12) {

            throw new IllegalStateException(
                    "Expected 12 dataset releases, found "
                            + result.size()
            );
        }

        return Map.copyOf(
                result
        );
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
                    required(
                            table,
                            row,
                            "Version"
                    );

            String date =
                    required(
                            table,
                            row,
                            "ReleaseDate"
                    );

            if (indexText.isBlank()) {

                throw new IllegalArgumentException(
                        "Missing release index in "
                                + input
                );
            }

            result.add(
                    new RawRelease(
                            Integer.parseInt(
                                    indexText
                            ),
                            version,
                            LocalDate.parse(
                                    date
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
                    required(
                            table,
                            row,
                            "Version"
                    );

            String commit =
                    firstValue(
                            table,
                            row,
                            "ReleaseCommit",
                            "CommitId"
                    );

            if (commit.isBlank()) {

                throw new IllegalStateException(
                        "Stable release without commit: "
                                + version
                );
            }

            StableRelease previous =
                    result.put(
                            normalizeVersion(
                                    version
                            ),
                            new StableRelease(
                                    version,
                                    normalizeCommit(
                                            commit
                                    )
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

    private static List<InventoryRow> readInventory(
            Path input,
            Map<String, ResolvedRelease> releaseByVersion
    ) throws IOException {

        CsvTable table =
                readCsv(
                        input
                );

        List<InventoryRow> result =
                new ArrayList<>();

        for (List<String> row : table.rows()) {

            int datasetIndex =
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

            String commit =
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

            ResolvedRelease raw =
                    releaseByVersion.get(
                            normalizeVersion(
                                    version
                            )
                    );

            if (raw == null) {

                throw new IllegalStateException(
                        "Inventory version absent from raw catalog: "
                                + version
                );
            }

            if (!raw.snapshotCommit()
                    .equals(
                            commit
                    )) {

                throw new IllegalStateException(
                        "Inventory commit differs from resolved release snapshot for "
                                + version
                                + " | inventory="
                                + commit
                                + " | resolved="
                                + raw.snapshotCommit()
                );
            }

            result.add(
                    new InventoryRow(
                            datasetIndex,
                            version,
                            commit,
                            classPath,
                            raw.index()
                    )
            );
        }

        return List.copyOf(
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

            String issue =
                    required(
                            table,
                            row,
                            "IssueKey"
                    );

            String createdText =
                    firstValue(
                            table,
                            row,
                            "CreatedAt",
                            "CreatedDate",
                            "Created"
                    );

            if (createdText.isBlank()) {

                throw new IllegalStateException(
                        "Defect without creation date: "
                                + issue
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
                        "Invalid creation date for "
                                + issue
                                + ": "
                                + createdText,
                        exception
                );
            }

            Defect previous =
                    result.put(
                            issue,
                            new Defect(
                                    issue,
                                    createdAt,
                                    firstValue(
                                            table,
                                            row,
                                            "AffectedVersions",
                                            "AffectedVersion",
                                            "Affected Version/s"
                                    ),
                                    firstValue(
                                            table,
                                            row,
                                            "FixVersions",
                                            "FixVersion",
                                            "Fix Version/s"
                                    )
                            )
                    );

            if (previous != null) {

                throw new IllegalStateException(
                        "Duplicate defect: "
                                + issue
                );
            }
        }

        return Map.copyOf(
                result
        );
    }

    private static Map<String, List<SzzRow>> readSzz(
            Path input
    ) throws IOException {

        CsvTable table =
                readCsv(
                        input
                );

        Map<String, List<SzzRow>> mutable =
                new LinkedHashMap<>();

        for (List<String> row : table.rows()) {

            SzzRow value =
                    new SzzRow(
                            required(
                                    table,
                                    row,
                                    "IssueKey"
                            ),
                            normalizeCommit(
                                    required(
                                            table,
                                            row,
                                            "FixCommitId"
                                    )
                            ),
                            normalizePath(
                                    required(
                                            table,
                                            row,
                                            "FixedFilePath"
                                    )
                            ),
                            normalizePath(
                                    required(
                                            table,
                                            row,
                                            "BlamedFilePath"
                                    )
                            ),
                            normalizeCommit(
                                    required(
                                            table,
                                            row,
                                            "BugIntroducingCommitId"
                                    )
                            ),
                            Integer.parseInt(
                                    required(
                                            table,
                                            row,
                                            "BlamedLineCount"
                                    )
                            )
                    );

            mutable
                    .computeIfAbsent(
                            value.issueKey(),
                            ignored ->
                                    new ArrayList<>()
                    )
                    .add(
                            value
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

    private static List<String> splitVersions(
            String value
    ) {

        if (value == null
                || value.isBlank()) {

            return List.of();
        }

        List<String> result =
                new ArrayList<>();

        for (String token
                : value
                .trim()
                .split("\\s*\\|\\s*")) {

            if (!token.isBlank()) {

                result.add(
                        token.trim()
                );
            }
        }

        return List.copyOf(
                result
        );
    }

    private static String observationKey(
            InventoryRow row
    ) {

        return row.datasetReleaseIndex()
                + "\u0000"
                + normalizePath(
                row.classPath()
        );
    }

    private static String normalizeVersion(
            String value
    ) {

        return value
                .trim()
                .toLowerCase(
                        Locale.ROOT
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

    private static String fileName(
            String path
    ) {

        String normalized =
                normalizePath(
                        path
                );

        int slash =
                normalized.lastIndexOf(
                        '/'
                );

        String value =
                slash < 0
                        ? normalized
                        : normalized.substring(
                        slash + 1
                );

        return value.toLowerCase(
                Locale.ROOT
        );
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

            for (int i = 0;
                 i < headers.size();
                 i++) {

                columns.put(
                        headers.get(i)
                                .trim(),
                        i
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
                    table
                            .columns()
                            .get(
                                    column
                            );

            if (index != null
                    && index < row.size()) {

                String value =
                        row.get(
                                        index
                                )
                                .trim();

                if (!value.isBlank()) {

                    return value;
                }
            }
        }

        return "";
    }

    private static List<String> parseCsvLine(
            String line
    ) {

        List<String> values =
                new ArrayList<>();

        StringBuilder current =
                new StringBuilder();

        boolean quoted =
                false;

        for (int i = 0;
             i < line.length();
             i++) {

            char ch =
                    line.charAt(
                            i
                    );

            if (ch == '"') {

                if (quoted
                        && i + 1 < line.length()
                        && line.charAt(
                        i + 1
                ) == '"') {

                    current.append(
                            '"'
                    );

                    i++;

                } else {

                    quoted =
                            !quoted;
                }

            } else if (ch == ','
                    && !quoted) {

                values.add(
                        current.toString()
                );

                current.setLength(
                        0
                );

            } else {

                current.append(
                        ch
                );
            }
        }

        if (quoted) {

            throw new IllegalArgumentException(
                    "Malformed CSV line."
            );
        }

        values.add(
                current.toString()
        );

        return values;
    }

    private static String removeBom(
            String value
    ) {

        return !value.isEmpty()
                && value.charAt(0)
                == '\uFEFF'
                ? value.substring(
                1
        )
                : value;
    }

    private static String csv(
            String value
    ) {

        if (value == null) {

            return "";
        }

        return "\""
                + value.replace(
                "\"",
                "\"\""
        )
                + "\"";
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
            String snapshotCommit
    ) {
    }

    private record InventoryRow(
            int datasetReleaseIndex,
            String version,
            String commitId,
            String classPath,
            int rawReleaseIndex
    ) {
    }

    private record ReleaseInventory(
            int datasetReleaseIndex,
            String version,
            String commitId,
            int rawReleaseIndex,
            Map<String, InventoryRow> byPath,
            Map<String, List<InventoryRow>> byFileName
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
            String fixCommitId,
            String fixedFilePath,
            String blamedFilePath,
            String bugIntroducingCommitId,
            int blamedLineCount
    ) {
    }

    private record Interval(
            String issueKey,
            int effectiveIv,
            int effectiveFv,
            String ivSource,
            String fvSource,
            Set<String> alignedFixIds
    ) {
    }

    private record MappingRow(
            String issueKey,
            int datasetReleaseIndex,
            String version,
            int rawReleaseIndex,
            int effectiveIv,
            int effectiveFv,
            String ivSource,
            String fvSource,
            String fixCommitId,
            String fixedFilePath,
            String blamedFilePath,
            String bugIntroducingCommitId,
            String mappedClass,
            String strategy,
            int sameFileNameCandidateCount,
            String sameFileNameCandidates
    ) {

        private static MappingRow mapped(
                SzzRow szz,
                ReleaseInventory release,
                Interval interval,
                String mappedClass,
                String strategy,
                int sameFileNameCandidateCount,
                String sameFileNameCandidates
        ) {

            return new MappingRow(
                    szz.issueKey(),
                    release.datasetReleaseIndex(),
                    release.version(),
                    release.rawReleaseIndex(),
                    interval.effectiveIv(),
                    interval.effectiveFv(),
                    interval.ivSource(),
                    interval.fvSource(),
                    szz.fixCommitId(),
                    szz.fixedFilePath(),
                    szz.blamedFilePath(),
                    szz.bugIntroducingCommitId(),
                    mappedClass,
                    strategy,
                    sameFileNameCandidateCount,
                    sameFileNameCandidates
            );
        }

        private static MappingRow unmapped(
                SzzRow szz,
                ReleaseInventory release,
                Interval interval,
                String strategy,
                int sameFileNameCandidateCount,
                String sameFileNameCandidates
        ) {

            return mapped(
                    szz,
                    release,
                    interval,
                    null,
                    strategy,
                    sameFileNameCandidateCount,
                    sameFileNameCandidates
            );
        }
    }

    private record MappingResult(
            List<MappingRow> rows,
            Set<String> buggyObservationKeys,
            Set<String> mappedDefects,
            long candidatePairs,
            long bicNotContained,
            long exactPath,
            long pathAbsent,
            long sameNameZero,
            long sameNameOne,
            long sameNameMany,
            long fixedPathPresentWhenBlamedAbsent
    ) {
    }

    private record ReachabilityIndex(
            Map<String, Set<String>> bySnapshot
    ) {

        private boolean contains(
                String snapshotCommit,
                String commitId
        ) {

            Set<String> commits =
                    bySnapshot.get(
                            normalizeCommit(
                                    snapshotCommit
                            )
                    );

            if (commits == null) {

                throw new IllegalStateException(
                        "Snapshot absent from reachability index: "
                                + snapshotCommit
                );
            }

            return commits.contains(
                    normalizeCommit(
                            commitId
                    )
            );
        }
    }

    private record CsvTable(
            Map<String, Integer> columns,
            List<List<String>> rows
    ) {
    }
}
