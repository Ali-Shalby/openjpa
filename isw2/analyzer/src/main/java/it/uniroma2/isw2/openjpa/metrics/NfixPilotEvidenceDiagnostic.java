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

package it.uniroma2.isw2.openjpa.metrics;

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

public final class NfixPilotEvidenceDiagnostic {

    private static final Path PILOT =
            Path.of(
                    "isw2",
                    "results",
                    "metrics",
                    "nfix_release_01_pilot.csv"
            );

    private static final Path FIX_CATALOG =
            Path.of(
                    "isw2",
                    "datasets",
                    "fix_commit_catalog.csv"
            );

    private NfixPilotEvidenceDiagnostic() {
        // Utility class.
    }

    public static void main(String[] args) throws Exception {

        Path repository =
                Path.of(".")
                        .toAbsolutePath()
                        .normalize();

        List<PilotRow> positives =
                readPositivePilotRows(
                        repository.resolve(PILOT)
                );

        Map<String, List<FixEvidence>> evidenceByCommit =
                readFixEvidence(
                        repository.resolve(FIX_CATALOG)
                );

        System.out.println();
        System.out.println(
                "===== NFIX RELEASE 1 EVIDENCE CHECK ====="
        );

        System.out.println(
                "Positive pilot classes : "
                        + positives.size()
        );

        int mismatches = 0;

        for (PilotRow row : positives) {

            Set<String> history =
                    readClassHistory(
                            repository,
                            row.commitId(),
                            row.classPath()
                    );

            Map<String, List<FixEvidence>> byIssue =
                    new LinkedHashMap<>();

            for (String commit : history) {

                List<FixEvidence> evidence =
                        evidenceByCommit.get(commit);

                if (evidence == null) {
                    continue;
                }

                for (FixEvidence item : evidence) {

                    byIssue
                            .computeIfAbsent(
                                    item.issueKey(),
                                    ignored ->
                                            new ArrayList<>()
                            )
                            .add(item);
                }
            }

            int recomputedNfix =
                    byIssue.size();

            boolean matches =
                    recomputedNfix == row.nfix();

            if (!matches) {
                mismatches++;
            }

            System.out.println();
            System.out.println(row.classPath());

            System.out.println(
                    "  Pilot NFIX      : "
                            + row.nfix()
            );

            System.out.println(
                    "  Recomputed NFIX : "
                            + recomputedNfix
            );

            System.out.println(
                    "  Match           : "
                            + matches
            );

            for (Map.Entry<String, List<FixEvidence>> entry
                    : byIssue.entrySet()) {

                System.out.println(
                        "  "
                                + entry.getKey()
                                + " -> "
                                + entry.getValue().size()
                                + " fix commit(s)"
                );

                for (FixEvidence evidence
                        : entry.getValue()) {

                    System.out.println(
                            "      "
                                    + evidence.commitId()
                                    + " | "
                                    + evidence.subject()
                    );
                }
            }
        }

        System.out.println();
        System.out.println(
                "Positive classes   : "
                        + positives.size()
        );

        System.out.println(
                "NFIX mismatches     : "
                        + mismatches
        );

        System.out.println(
                "=========================================="
        );

        if (mismatches != 0) {
            throw new IllegalStateException(
                    "NFIX pilot evidence mismatch: "
                            + mismatches
            );
        }
    }

    private static List<PilotRow> readPositivePilotRows(
            Path input
    ) throws IOException {

        List<PilotRow> result =
                new ArrayList<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             input,
                             StandardCharsets.UTF_8
                     )) {

            List<String> headers =
                    parseCsvLine(reader.readLine());

            Map<String, Integer> columns =
                    buildColumnMap(headers);

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                List<String> values =
                        parseCsvLine(line);

                int nfix =
                        Integer.parseInt(
                                get(
                                        values,
                                        columns,
                                        "NFIX"
                                )
                        );

                if (nfix <= 0) {
                    continue;
                }

                result.add(
                        new PilotRow(
                                Integer.parseInt(
                                        get(
                                                values,
                                                columns,
                                                "ReleaseIndex"
                                        )
                                ),
                                get(
                                        values,
                                        columns,
                                        "CommitId"
                                ),
                                get(
                                        values,
                                        columns,
                                        "Class"
                                ),
                                nfix
                        )
                );
            }
        }

        return result;
    }

    private static Map<String, List<FixEvidence>> readFixEvidence(
            Path input
    ) throws IOException {

        Map<String, List<FixEvidence>> result =
                new HashMap<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             input,
                             StandardCharsets.UTF_8
                     )) {

            List<String> headers =
                    parseCsvLine(reader.readLine());

            Map<String, Integer> columns =
                    buildColumnMap(headers);

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                List<String> values =
                        parseCsvLine(line);

                FixEvidence evidence =
                        new FixEvidence(
                                get(
                                        values,
                                        columns,
                                        "IssueKey"
                                ),
                                get(
                                        values,
                                        columns,
                                        "CommitId"
                                ),
                                get(
                                        values,
                                        columns,
                                        "Subject"
                                )
                        );

                result.computeIfAbsent(
                                evidence.commitId(),
                                ignored ->
                                        new ArrayList<>()
                        )
                        .add(evidence);
            }
        }

        return result;
    }

    private static Set<String> readClassHistory(
            Path repository,
            String releaseCommit,
            String classPath
    ) throws IOException, InterruptedException {

        List<String> command =
                List.of(
                        "git",
                        "-C",
                        repository.toString(),
                        "-c",
                        "diff.renameLimit=0",
                        "log",
                        releaseCommit,
                        "--follow",
                        "--no-merges",
                        "--format=%H",
                        "--",
                        classPath
                );

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
                            .reduce(
                                    "",
                                    (left, right) ->
                                            left.isEmpty()
                                                    ? right
                                                    : left
                                                    + System.lineSeparator()
                                                    + right
                            );
        }

        int exitCode =
                process.waitFor();

        if (exitCode != 0) {
            throw new IOException(
                    "Git history command failed for "
                            + classPath
                            + System.lineSeparator()
                            + output
            );
        }

        Set<String> commits =
                new HashSet<>();

        output.lines()
                .map(String::trim)
                .filter(line ->
                        !line.isBlank()
                )
                .forEach(commits::add);

        return commits;
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

    private static String get(
            List<String> values,
            Map<String, Integer> columns,
            String name
    ) {

        Integer index =
                columns.get(name);

        if (index == null) {
            throw new IllegalArgumentException(
                    "Missing CSV column: "
                            + name
            );
        }

        return values.get(index);
    }

    private static List<String> parseCsvLine(
            String line
    ) {

        if (line == null) {
            throw new IllegalArgumentException(
                    "Empty CSV."
            );
        }

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

    private record PilotRow(
            int releaseIndex,
            String commitId,
            String classPath,
            int nfix
    ) {
    }

    private record FixEvidence(
            String issueKey,
            String commitId,
            String subject
    ) {
    }
}
