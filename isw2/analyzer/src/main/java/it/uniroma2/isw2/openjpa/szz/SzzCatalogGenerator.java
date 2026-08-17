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

package it.uniroma2.isw2.openjpa.szz;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class SzzCatalogGenerator {

    private static final Path FIX_CATALOG =
            Path.of(
                    "isw2",
                    "datasets",
                    "fix_commit_catalog.csv"
            );

    private static final Path EVIDENCE_OUTPUT =
            Path.of(
                    "isw2",
                    "datasets",
                    "szz_evidence.csv"
            );

    private static final Path AUDIT_OUTPUT =
            Path.of(
                    "isw2",
                    "results",
                    "szz",
                    "szz_fix_audit.csv"
            );

    private static final Pattern COMMIT_PATTERN =
            Pattern.compile(
                    "^[0-9a-fA-F]{40,64}$"
            );

    private SzzCatalogGenerator() {
        // Utility class.
    }

    public static void main(String[] args) {

        try {

            Path repository =
                    resolveRepositoryRoot(args);

            List<FixMapping> mappings =
                    readFixMappings(
                            repository.resolve(FIX_CATALOG)
                    );

            validateFixMappings(mappings);

            Map<String, Set<String>> fixCommitsByIssue =
                    buildFixCommitsByIssue(mappings);

            SzzAnalyzer analyzer =
                    new SzzAnalyzer(repository);

            AnalysisResult result =
                    analyzeAll(
                            analyzer,
                            mappings,
                            fixCommitsByIssue
                    );

            validateResult(
                    mappings,
                    result,
                    fixCommitsByIssue
            );

            Path evidenceOutput =
                    repository.resolve(EVIDENCE_OUTPUT);

            Path auditOutput =
                    repository.resolve(AUDIT_OUTPUT);

            writeEvidence(
                    evidenceOutput,
                    result.evidence()
            );

            writeAudit(
                    auditOutput,
                    result.audit()
            );

            printSummary(
                    mappings,
                    result,
                    evidenceOutput,
                    auditOutput
            );

        } catch (Exception exception) {

            System.err.println(
                    "Unable to generate SZZ catalog: "
                            + exception.getMessage()
            );

            exception.printStackTrace();

            System.exit(1);
        }
    }

    private static AnalysisResult analyzeAll(
            SzzAnalyzer analyzer,
            List<FixMapping> mappings,
            Map<String, Set<String>> fixCommitsByIssue
    ) throws IOException, InterruptedException {

        List<SzzEvidence> allEvidence =
                new ArrayList<>();

        List<FixAudit> audit =
                new ArrayList<>();

        /*
         * The Git structure of a fix commit is independent
         * of the JIRA issue associated with it.
         *
         * If the same commit belongs to multiple validated
         * defects, analyze Git only once and then relabel
         * the structural evidence for the current issue.
         */
        Map<String, List<SzzEvidence>> cache =
                new HashMap<>();

        int processed = 0;
        int cacheHits = 0;

        int totalSameIssueFixRowsSkipped = 0;
        long totalSameIssueFixBlamedLinesSkipped = 0;

        for (FixMapping mapping : mappings) {

            List<SzzEvidence> structuralEvidence =
                    cache.get(
                            mapping.fixCommitId()
                    );

            if (structuralEvidence == null) {

                structuralEvidence =
                        analyzer.analyze(
                                mapping.issueKey(),
                                mapping.fixCommitId()
                        );

                cache.put(
                        mapping.fixCommitId(),
                        structuralEvidence
                );

            } else {

                cacheHits++;
            }

            List<SzzEvidence> issueEvidence =
                    relabelEvidence(
                            structuralEvidence,
                            mapping.issueKey()
                    );

            Set<String> sameIssueFixCommits =
                    fixCommitsByIssue.getOrDefault(
                            mapping.issueKey(),
                            Set.of()
                    );

            List<SzzEvidence> acceptedEvidence =
                    new ArrayList<>();

            int sameIssueFixRowsSkipped = 0;
            long sameIssueFixBlamedLinesSkipped = 0;

            for (SzzEvidence evidence : issueEvidence) {

                String bic =
                        normalizeCommit(
                                evidence
                                        .bugIntroducingCommitId()
                        );

                /*
                 * Critical SZZ safeguard:
                 *
                 * A commit already classified as a fix of the
                 * SAME defect cannot also be used as the
                 * bug-introducing commit of that same defect.
                 *
                 * This commonly occurs when one ticket is fixed
                 * through a sequence of multiple commits.
                 */
                if (sameIssueFixCommits.contains(bic)) {

                    sameIssueFixRowsSkipped++;

                    sameIssueFixBlamedLinesSkipped +=
                            evidence.blamedLineCount();

                    continue;
                }

                acceptedEvidence.add(evidence);
            }

            totalSameIssueFixRowsSkipped +=
                    sameIssueFixRowsSkipped;

            totalSameIssueFixBlamedLinesSkipped +=
                    sameIssueFixBlamedLinesSkipped;

            allEvidence.addAll(
                    acceptedEvidence
            );

            Set<String> fixedFiles =
                    acceptedEvidence.stream()
                            .map(
                                    SzzEvidence::fixedFilePath
                            )
                            .collect(Collectors.toSet());

            Set<String> bics =
                    acceptedEvidence.stream()
                            .map(
                                    SzzEvidence
                                            ::bugIntroducingCommitId
                            )
                            .collect(Collectors.toSet());

            int blamedLines =
                    acceptedEvidence.stream()
                            .mapToInt(
                                    SzzEvidence::blamedLineCount
                            )
                            .sum();

            audit.add(
                    new FixAudit(
                            mapping.issueKey(),
                            mapping.fixCommitId(),
                            acceptedEvidence.isEmpty()
                                    ? "NO_EVIDENCE"
                                    : "EVIDENCE",
                            acceptedEvidence.size(),
                            fixedFiles.size(),
                            bics.size(),
                            blamedLines,
                            sameIssueFixRowsSkipped,
                            sameIssueFixBlamedLinesSkipped
                    )
            );

            processed++;

            if (processed % 25 == 0
                    || processed == mappings.size()) {

                System.out.printf(
                        "SZZ progress: %d / %d "
                                + "(unique commits analyzed: %d)%n",
                        processed,
                        mappings.size(),
                        cache.size()
                );
            }
        }

        return new AnalysisResult(
                List.copyOf(allEvidence),
                List.copyOf(audit),
                cache.size(),
                cacheHits,
                totalSameIssueFixRowsSkipped,
                totalSameIssueFixBlamedLinesSkipped
        );
    }

    private static Map<String, Set<String>>
    buildFixCommitsByIssue(
            List<FixMapping> mappings
    ) {

        Map<String, Set<String>> result =
                new HashMap<>();

        for (FixMapping mapping : mappings) {

            result.computeIfAbsent(
                            mapping.issueKey(),
                            ignored ->
                                    new HashSet<>()
                    )
                    .add(
                            normalizeCommit(
                                    mapping.fixCommitId()
                            )
                    );
        }

        return result;
    }

    private static List<SzzEvidence> relabelEvidence(
            List<SzzEvidence> evidence,
            String issueKey
    ) {

        List<SzzEvidence> result =
                new ArrayList<>();

        for (SzzEvidence row : evidence) {

            result.add(
                    new SzzEvidence(
                            issueKey,
                            row.fixCommitId(),
                            row.parentCommitId(),
                            row.fixedFilePath(),
                            row.blamedFilePath(),
                            row.bugIntroducingCommitId(),
                            row.blamedLineCount()
                    )
            );
        }

        return result;
    }

    private static void validateFixMappings(
            List<FixMapping> mappings
    ) {

        if (mappings.isEmpty()) {

            throw new IllegalStateException(
                    "No fix mappings found."
            );
        }

        Set<String> keys =
                new HashSet<>();

        for (FixMapping mapping : mappings) {

            if (mapping.issueKey() == null
                    || mapping.issueKey().isBlank()) {

                throw new IllegalStateException(
                        "Blank issue key in fix catalog."
                );
            }

            if (mapping.fixCommitId() == null
                    || !COMMIT_PATTERN
                    .matcher(mapping.fixCommitId())
                    .matches()) {

                throw new IllegalStateException(
                        "Invalid fix commit id: "
                                + mapping.fixCommitId()
                );
            }

            String key =
                    mapping.issueKey()
                            + "\u0000"
                            + normalizeCommit(
                            mapping.fixCommitId()
                    );

            if (!keys.add(key)) {

                throw new IllegalStateException(
                        "Duplicate fix mapping: "
                                + mapping.issueKey()
                                + " / "
                                + mapping.fixCommitId()
                );
            }
        }
    }

    private static void validateResult(
            List<FixMapping> mappings,
            AnalysisResult result,
            Map<String, Set<String>> fixCommitsByIssue
    ) {

        if (result.audit().size()
                != mappings.size()) {

            throw new IllegalStateException(
                    "SZZ audit row count mismatch. Expected "
                            + mappings.size()
                            + ", found "
                            + result.audit().size()
            );
        }

        Set<String> auditKeys =
                new HashSet<>();

        for (FixAudit row : result.audit()) {

            String key =
                    row.issueKey()
                            + "\u0000"
                            + normalizeCommit(
                            row.fixCommitId()
                    );

            if (!auditKeys.add(key)) {

                throw new IllegalStateException(
                        "Duplicate SZZ audit row: "
                                + row.issueKey()
                                + " / "
                                + row.fixCommitId()
                );
            }

            if (!"EVIDENCE".equals(row.status())
                    && !"NO_EVIDENCE".equals(
                    row.status()
            )) {

                throw new IllegalStateException(
                        "Unexpected SZZ audit status: "
                                + row.status()
                );
            }

            if (row.sameIssueFixRowsSkipped() < 0
                    || row.sameIssueFixBlamedLinesSkipped() < 0) {

                throw new IllegalStateException(
                        "Negative same-issue skip counters: "
                                + row.issueKey()
                                + " / "
                                + row.fixCommitId()
                );
            }

            if ("NO_EVIDENCE".equals(row.status())
                    && (row.evidenceRows() != 0
                    || row.fixedFiles() != 0
                    || row.bugIntroducingCommits() != 0
                    || row.blamedLines() != 0)) {

                throw new IllegalStateException(
                        "NO_EVIDENCE row contains accepted evidence: "
                                + row.issueKey()
                                + " / "
                                + row.fixCommitId()
                );
            }

            if ("EVIDENCE".equals(row.status())
                    && row.evidenceRows() <= 0) {

                throw new IllegalStateException(
                        "EVIDENCE row has no evidence: "
                                + row.issueKey()
                                + " / "
                                + row.fixCommitId()
                );
            }
        }

        Set<String> evidenceKeys =
                new HashSet<>();

        int sameIssueFixBicsRemaining = 0;

        for (SzzEvidence row : result.evidence()) {

            if (!COMMIT_PATTERN
                    .matcher(row.fixCommitId())
                    .matches()) {

                throw new IllegalStateException(
                        "Invalid SZZ fix commit: "
                                + row.fixCommitId()
                );
            }

            if (!COMMIT_PATTERN
                    .matcher(row.parentCommitId())
                    .matches()) {

                throw new IllegalStateException(
                        "Invalid SZZ parent commit: "
                                + row.parentCommitId()
                );
            }

            if (!COMMIT_PATTERN
                    .matcher(
                            row.bugIntroducingCommitId()
                    )
                    .matches()) {

                throw new IllegalStateException(
                        "Invalid SZZ bug-introducing commit: "
                                + row.bugIntroducingCommitId()
                );
            }

            if (row.blamedLineCount() <= 0) {

                throw new IllegalStateException(
                        "Non-positive blamed line count."
                );
            }

            if (row.fixedFilePath() == null
                    || row.fixedFilePath().isBlank()
                    || row.blamedFilePath() == null
                    || row.blamedFilePath().isBlank()) {

                throw new IllegalStateException(
                        "Missing SZZ file path."
                );
            }

            Set<String> sameIssueFixCommits =
                    fixCommitsByIssue.getOrDefault(
                            row.issueKey(),
                            Set.of()
                    );

            if (sameIssueFixCommits.contains(
                    normalizeCommit(
                            row.bugIntroducingCommitId()
                    )
            )) {

                sameIssueFixBicsRemaining++;
            }

            String key =
                    row.issueKey()
                            + "\u0000"
                            + normalizeCommit(
                            row.fixCommitId()
                    )
                            + "\u0000"
                            + row.fixedFilePath()
                            + "\u0000"
                            + row.blamedFilePath()
                            + "\u0000"
                            + normalizeCommit(
                            row.bugIntroducingCommitId()
                    );

            if (!evidenceKeys.add(key)) {

                throw new IllegalStateException(
                        "Duplicate SZZ evidence row: "
                                + row.issueKey()
                                + " / "
                                + row.fixCommitId()
                                + " / "
                                + row.fixedFilePath()
                                + " / "
                                + row.bugIntroducingCommitId()
                );
            }
        }

        if (sameIssueFixBicsRemaining != 0) {

            throw new IllegalStateException(
                    "Same-issue fix commits still used as BIC: "
                            + sameIssueFixBicsRemaining
            );
        }

        long auditSkippedRows =
                result.audit()
                        .stream()
                        .mapToLong(
                                FixAudit::sameIssueFixRowsSkipped
                        )
                        .sum();

        long auditSkippedLines =
                result.audit()
                        .stream()
                        .mapToLong(
                                FixAudit
                                        ::sameIssueFixBlamedLinesSkipped
                        )
                        .sum();

        if (auditSkippedRows
                != result.sameIssueFixRowsSkipped()) {

            throw new IllegalStateException(
                    "Same-issue skipped row count mismatch. "
                            + "Audit="
                            + auditSkippedRows
                            + ", result="
                            + result.sameIssueFixRowsSkipped()
            );
        }

        if (auditSkippedLines
                != result.sameIssueFixBlamedLinesSkipped()) {

            throw new IllegalStateException(
                    "Same-issue skipped blamed-line count mismatch. "
                            + "Audit="
                            + auditSkippedLines
                            + ", result="
                            + result
                            .sameIssueFixBlamedLinesSkipped()
            );
        }
    }

    private static List<FixMapping> readFixMappings(
            Path input
    ) throws IOException {

        if (!Files.isRegularFile(input)) {

            throw new IllegalArgumentException(
                    "Fix commit catalog not found: "
                            + input
            );
        }

        List<FixMapping> result =
                new ArrayList<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             input,
                             StandardCharsets.UTF_8
                     )) {

            String headerLine =
                    reader.readLine();

            if (headerLine == null) {

                throw new IOException(
                        "Empty fix catalog: "
                                + input
                );
            }

            List<String> headers =
                    parseCsvLine(headerLine);

            Map<String, Integer> columns =
                    buildColumnMap(headers);

            requireColumn(
                    columns,
                    "IssueKey"
            );

            requireColumn(
                    columns,
                    "CommitId"
            );

            String line;

            while ((line = reader.readLine())
                    != null) {

                if (line.isBlank()) {
                    continue;
                }

                List<String> values =
                        parseCsvLine(line);

                result.add(
                        new FixMapping(
                                get(
                                        values,
                                        columns,
                                        "IssueKey"
                                ),
                                get(
                                        values,
                                        columns,
                                        "CommitId"
                                )
                        )
                );
            }
        }

        return result.stream()
                .sorted(
                        Comparator
                                .comparing(
                                        FixMapping::issueKey
                                )
                                .thenComparing(
                                        FixMapping::fixCommitId
                                )
                )
                .toList();
    }

    private static void writeEvidence(
            Path output,
            List<SzzEvidence> evidence
    ) throws IOException {

        Files.createDirectories(
                output.getParent()
        );

        List<SzzEvidence> ordered =
                evidence.stream()
                        .sorted(
                                Comparator
                                        .comparing(
                                                SzzEvidence::issueKey
                                        )
                                        .thenComparing(
                                                SzzEvidence::fixCommitId
                                        )
                                        .thenComparing(
                                                SzzEvidence::fixedFilePath
                                        )
                                        .thenComparing(
                                                SzzEvidence
                                                        ::bugIntroducingCommitId
                                        )
                        )
                        .toList();

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             output,
                             StandardCharsets.UTF_8
                     )) {

            writer.write(
                    "IssueKey,"
                            + "FixCommitId,"
                            + "ParentCommitId,"
                            + "FixedFilePath,"
                            + "BlamedFilePath,"
                            + "BugIntroducingCommitId,"
                            + "BlamedLineCount"
            );

            writer.newLine();

            for (SzzEvidence row : ordered) {

                writer.write(
                        csv(row.issueKey())
                                + ","
                                + csv(row.fixCommitId())
                                + ","
                                + csv(row.parentCommitId())
                                + ","
                                + csv(row.fixedFilePath())
                                + ","
                                + csv(row.blamedFilePath())
                                + ","
                                + csv(
                                row.bugIntroducingCommitId()
                        )
                                + ","
                                + row.blamedLineCount()
                );

                writer.newLine();
            }
        }
    }

    private static void writeAudit(
            Path output,
            List<FixAudit> audit
    ) throws IOException {

        Files.createDirectories(
                output.getParent()
        );

        List<FixAudit> ordered =
                audit.stream()
                        .sorted(
                                Comparator
                                        .comparing(
                                                FixAudit::issueKey
                                        )
                                        .thenComparing(
                                                FixAudit::fixCommitId
                                        )
                        )
                        .toList();

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             output,
                             StandardCharsets.UTF_8
                     )) {

            writer.write(
                    "IssueKey,"
                            + "FixCommitId,"
                            + "Status,"
                            + "EvidenceRows,"
                            + "FixedFiles,"
                            + "BugIntroducingCommits,"
                            + "BlamedLines,"
                            + "SameIssueFixRowsSkipped,"
                            + "SameIssueFixBlamedLinesSkipped"
            );

            writer.newLine();

            for (FixAudit row : ordered) {

                writer.write(
                        csv(row.issueKey())
                                + ","
                                + csv(row.fixCommitId())
                                + ","
                                + row.status()
                                + ","
                                + row.evidenceRows()
                                + ","
                                + row.fixedFiles()
                                + ","
                                + row.bugIntroducingCommits()
                                + ","
                                + row.blamedLines()
                                + ","
                                + row.sameIssueFixRowsSkipped()
                                + ","
                                + row.sameIssueFixBlamedLinesSkipped()
                );

                writer.newLine();
            }
        }
    }

    private static void printSummary(
            List<FixMapping> mappings,
            AnalysisResult result,
            Path evidenceOutput,
            Path auditOutput
    ) {

        long mappingsWithEvidence =
                result.audit()
                        .stream()
                        .filter(row ->
                                "EVIDENCE".equals(
                                        row.status()
                                )
                        )
                        .count();

        long mappingsWithoutEvidence =
                result.audit().size()
                        - mappingsWithEvidence;

        Set<String> defectsWithEvidence =
                result.evidence()
                        .stream()
                        .map(
                                SzzEvidence::issueKey
                        )
                        .collect(Collectors.toSet());

        Set<String> fixCommitsWithEvidence =
                result.evidence()
                        .stream()
                        .map(
                                SzzEvidence::fixCommitId
                        )
                        .collect(Collectors.toSet());

        Set<String> bugIntroducingCommits =
                result.evidence()
                        .stream()
                        .map(
                                SzzEvidence
                                        ::bugIntroducingCommitId
                        )
                        .collect(Collectors.toSet());

        Set<String> fixedFiles =
                result.evidence()
                        .stream()
                        .map(
                                SzzEvidence::fixedFilePath
                        )
                        .collect(Collectors.toSet());

        long totalBlamedLines =
                result.evidence()
                        .stream()
                        .mapToLong(
                                SzzEvidence::blamedLineCount
                        )
                        .sum();

        System.out.println();
        System.out.println(
                "===== SZZ CATALOG ====="
        );

        System.out.println(
                "Fix mappings processed             : "
                        + mappings.size()
        );

        System.out.println(
                "Unique fix commits analyzed        : "
                        + result.uniqueCommitsAnalyzed()
        );

        System.out.println(
                "Cache reuses                       : "
                        + result.cacheHits()
        );

        System.out.println(
                "Mappings with evidence             : "
                        + mappingsWithEvidence
        );

        System.out.println(
                "Mappings without evidence          : "
                        + mappingsWithoutEvidence
        );

        System.out.println(
                "SZZ evidence rows                  : "
                        + result.evidence().size()
        );

        System.out.println(
                "Defects with evidence              : "
                        + defectsWithEvidence.size()
        );

        System.out.println(
                "Fix commits with evidence          : "
                        + fixCommitsWithEvidence.size()
        );

        System.out.println(
                "Distinct fixed files               : "
                        + fixedFiles.size()
        );

        System.out.println(
                "Bug-introducing commits            : "
                        + bugIntroducingCommits.size()
        );

        System.out.println(
                "Total blamed lines                 : "
                        + totalBlamedLines
        );

        System.out.println();

        System.out.println(
                "Same-issue fix BIC rows skipped    : "
                        + result.sameIssueFixRowsSkipped()
        );

        System.out.println(
                "Same-issue blamed lines skipped    : "
                        + result
                        .sameIssueFixBlamedLinesSkipped()
        );

        System.out.println(
                "Same-issue fix BIC remaining       : 0"
        );

        System.out.println(
                "Validation failures                : 0"
        );

        System.out.println();

        System.out.println(
                "Evidence output                    : "
                        + evidenceOutput.toAbsolutePath()
        );

        System.out.println(
                "Audit output                       : "
                        + auditOutput.toAbsolutePath()
        );

        System.out.println(
                "======================================"
        );
    }

    private static String normalizeCommit(
            String commitId
    ) {

        return commitId
                .toLowerCase(Locale.ROOT);
    }

    private static Path resolveRepositoryRoot(
            String[] args
    ) {

        if (args.length == 0) {

            throw new IllegalArgumentException(
                    "Repository root path is required."
            );
        }

        Path root =
                Path.of(args[0])
                        .toAbsolutePath()
                        .normalize();

        if (!Files.isDirectory(
                root.resolve(".git")
        )) {

            throw new IllegalArgumentException(
                    "Not a Git repository root: "
                            + root
            );
        }

        return root;
    }

    private static Map<String, Integer> buildColumnMap(
            List<String> headers
    ) {

        Map<String, Integer> columns =
                new HashMap<>();

        for (int index = 0;
             index < headers.size();
             index++) {

            columns.put(
                    headers.get(index),
                    index
            );
        }

        return columns;
    }

    private static void requireColumn(
            Map<String, Integer> columns,
            String name
    ) {

        if (!columns.containsKey(name)) {

            throw new IllegalArgumentException(
                    "Missing CSV column: "
                            + name
            );
        }
    }

    private static String get(
            List<String> values,
            Map<String, Integer> columns,
            String name
    ) {

        Integer index =
                columns.get(name);

        if (index == null
                || index >= values.size()) {

            throw new IllegalArgumentException(
                    "Missing CSV value: "
                            + name
            );
        }

        return values.get(index);
    }

    private static List<String> parseCsvLine(
            String line
    ) {

        List<String> values =
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

                values.add(
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

        values.add(
                current.toString()
        );

        return values;
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

    private record FixMapping(
            String issueKey,
            String fixCommitId
    ) {
    }

    private record FixAudit(
            String issueKey,
            String fixCommitId,
            String status,
            int evidenceRows,
            int fixedFiles,
            int bugIntroducingCommits,
            int blamedLines,
            int sameIssueFixRowsSkipped,
            long sameIssueFixBlamedLinesSkipped
    ) {
    }

    private record AnalysisResult(
            List<SzzEvidence> evidence,
            List<FixAudit> audit,
            int uniqueCommitsAnalyzed,
            int cacheHits,
            int sameIssueFixRowsSkipped,
            long sameIssueFixBlamedLinesSkipped
    ) {
    }
}
