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
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NfixMetricsGenerator {

    private static final String INVENTORY =
            "isw2/datasets/java_class_inventory.csv";

    private static final String FIX_CATALOG =
            "isw2/datasets/fix_commit_catalog.csv";

    private static final String FULL_OUTPUT =
            "isw2/datasets/nfix_metrics.csv";

    private static final String PILOT_OUTPUT =
            "isw2/results/metrics/nfix_release_01_pilot.csv";

    private NfixMetricsGenerator() {
        // Utility class.
    }

    public static void main(String[] args) {

        try {

            Path repositoryRoot =
                    resolveRepositoryRoot(args);

            Integer releaseFilter =
                    resolveReleaseFilter(args);

            Path inventoryPath =
                    repositoryRoot.resolve(INVENTORY);

            Path fixCatalogPath =
                    repositoryRoot.resolve(FIX_CATALOG);

            List<InventoryRow> observations =
                    readInventory(inventoryPath);

            if (releaseFilter != null) {

                observations =
                        observations.stream()
                                .filter(row ->
                                        row.releaseIndex()
                                                == releaseFilter
                                )
                                .toList();
            }

            if (observations.isEmpty()) {
                throw new IllegalStateException(
                        "No production observations selected."
                );
            }

            Map<String, Set<String>> issuesByCommit =
                    readFixCatalog(fixCatalogPath);

            List<NfixRow> output =
                    calculate(
                            repositoryRoot,
                            observations,
                            issuesByCommit
                    );

            validate(
                    observations,
                    output
            );

            Path outputPath =
                    repositoryRoot.resolve(
                            releaseFilter == null
                                    ? FULL_OUTPUT
                                    : PILOT_OUTPUT
                    );

            writeCsv(
                    outputPath,
                    output
            );

            printSummary(
                    releaseFilter,
                    output,
                    outputPath
            );

        } catch (Exception exception) {

            System.err.println(
                    "Unable to calculate NFIX: "
                            + exception.getMessage()
            );

            exception.printStackTrace();

            System.exit(1);
        }
    }

    private static List<NfixRow> calculate(
            Path repositoryRoot,
            List<InventoryRow> observations,
            Map<String, Set<String>> issuesByCommit
    ) throws IOException, InterruptedException {

        List<NfixRow> result =
                new ArrayList<>();

        int processed = 0;

        for (InventoryRow observation : observations) {

            Set<String> history =
                    readClassHistory(
                            repositoryRoot,
                            observation.commitId(),
                            observation.classPath()
                    );

            Set<String> defectIssues =
                    new HashSet<>();

            for (String commitId : history) {

                Set<String> issueKeys =
                        issuesByCommit.get(commitId);

                if (issueKeys != null) {
                    defectIssues.addAll(issueKeys);
                }
            }

            result.add(
                    new NfixRow(
                            observation.releaseIndex(),
                            observation.version(),
                            observation.commitId(),
                            observation.classPath(),
                            defectIssues.size()
                    )
            );

            processed++;

            if (processed % 100 == 0
                    || processed == observations.size()) {

                System.out.printf(
                        "NFIX progress: %d / %d%n",
                        processed,
                        observations.size()
                );
            }
        }

        return result;
    }

    private static Set<String> readClassHistory(
            Path repositoryRoot,
            String releaseCommit,
            String classPath
    ) throws IOException, InterruptedException {

        String output =
                runGit(
                        repositoryRoot,
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

        Set<String> commits =
                new HashSet<>();

        if (output.isBlank()) {
            return commits;
        }

        for (String line : output.lines().toList()) {

            String commit =
                    line.trim();

            if (!commit.isBlank()) {
                commits.add(commit);
            }
        }

        return commits;
    }

    private static Map<String, Set<String>> readFixCatalog(
            Path input
    ) throws IOException {

        if (!Files.isRegularFile(input)) {
            throw new IllegalArgumentException(
                    "Fix commit catalog not found: "
                            + input
            );
        }

        Map<String, Set<String>> issuesByCommit =
                new HashMap<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             input,
                             StandardCharsets.UTF_8
                     )) {

            String headerLine =
                    reader.readLine();

            if (headerLine == null) {
                throw new IOException(
                        "Empty fix catalog: " + input
                );
            }

            List<String> headers =
                    parseCsvLine(headerLine);

            Map<String, Integer> columns =
                    buildColumnMap(headers);

            requireColumn(columns, "IssueKey");
            requireColumn(columns, "CommitId");

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                List<String> values =
                        parseCsvLine(line);

                String issueKey =
                        get(
                                values,
                                columns,
                                "IssueKey"
                        );

                String commitId =
                        get(
                                values,
                                columns,
                                "CommitId"
                        );

                issuesByCommit
                        .computeIfAbsent(
                                commitId,
                                ignored ->
                                        new HashSet<>()
                        )
                        .add(issueKey);
            }
        }

        return issuesByCommit;
    }

    private static List<InventoryRow> readInventory(
            Path input
    ) throws IOException {

        if (!Files.isRegularFile(input)) {
            throw new IllegalArgumentException(
                    "Production inventory not found: "
                            + input
            );
        }

        List<InventoryRow> rows =
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
                        "Empty production inventory: "
                                + input
                );
            }

            List<String> headers =
                    parseCsvLine(headerLine);

            Map<String, Integer> columns =
                    buildColumnMap(headers);

            requireColumn(columns, "ReleaseIndex");
            requireColumn(columns, "Version");
            requireColumn(columns, "CommitId");
            requireColumn(columns, "Class");

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                List<String> values =
                        parseCsvLine(line);

                rows.add(
                        new InventoryRow(
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
                                        "Version"
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
                                )
                        )
                );
            }
        }

        return rows;
    }

    private static void validate(
            List<InventoryRow> input,
            List<NfixRow> output
    ) {

        if (input.size() != output.size()) {
            throw new IllegalStateException(
                    "NFIX row count mismatch. Expected "
                            + input.size()
                            + ", found "
                            + output.size()
            );
        }

        Set<String> keys =
                new HashSet<>();

        for (NfixRow row : output) {

            if (row.nfix() < 0) {
                throw new IllegalStateException(
                        "Negative NFIX for "
                                + row.releaseIndex()
                                + " / "
                                + row.classPath()
                );
            }

            String key =
                    row.releaseIndex()
                            + "\u0000"
                            + row.classPath();

            if (!keys.add(key)) {
                throw new IllegalStateException(
                        "Duplicate NFIX key: "
                                + row.releaseIndex()
                                + " / "
                                + row.classPath()
                );
            }
        }
    }

    private static void writeCsv(
            Path output,
            List<NfixRow> rows
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
                    "ReleaseIndex,"
                            + "Version,"
                            + "CommitId,"
                            + "Class,"
                            + "NFIX"
            );

            writer.newLine();

            for (NfixRow row : rows) {

                writer.write(
                        row.releaseIndex()
                                + ","
                                + csv(row.version())
                                + ","
                                + csv(row.commitId())
                                + ","
                                + csv(row.classPath())
                                + ","
                                + row.nfix()
                );

                writer.newLine();
            }
        }
    }

    private static void printSummary(
            Integer releaseFilter,
            List<NfixRow> rows,
            Path output
    ) {

        long positive =
                rows.stream()
                        .filter(row ->
                                row.nfix() > 0
                        )
                        .count();

        int maximum =
                rows.stream()
                        .mapToInt(NfixRow::nfix)
                        .max()
                        .orElse(0);

        long sum =
                rows.stream()
                        .mapToLong(NfixRow::nfix)
                        .sum();

        System.out.println();
        System.out.println(
                "===== NFIX METRICS ====="
        );

        System.out.println(
                "Mode                    : "
                        + (releaseFilter == null
                        ? "FULL"
                        : "RELEASE " + releaseFilter)
        );

        System.out.println(
                "NFIX observations       : "
                        + rows.size()
        );

        System.out.println(
                "Observations NFIX > 0   : "
                        + positive
        );

        System.out.println(
                "Sum(NFIX)               : "
                        + sum
        );

        System.out.println(
                "Maximum NFIX            : "
                        + maximum
        );

        System.out.println(
                "Duplicate keys          : 0"
        );

        System.out.println(
                "Negative NFIX           : 0"
        );

        System.out.println(
                "History mode            : "
                        + "--follow --no-merges"
        );

        System.out.println(
                "Output                  : "
                        + output
                        .toAbsolutePath()
        );

        System.out.println(
                "========================"
        );
    }

    private static Integer resolveReleaseFilter(
            String[] args
    ) {

        if (args.length < 2) {
            return null;
        }

        int release =
                Integer.parseInt(args[1]);

        if (release < 1 || release > 12) {
            throw new IllegalArgumentException(
                    "Release index must be in 1..12."
            );
        }

        return release;
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
                Paths.get(args[0])
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

    private static String runGit(
            Path repositoryRoot,
            String... arguments
    ) throws IOException, InterruptedException {

        List<String> command =
                new ArrayList<>();

        command.add("git");
        command.add("-C");
        command.add(
                repositoryRoot.toString()
        );

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

        return output.trim();
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

        int index =
                columns.get(name);

        if (index >= values.size()) {
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

    private record InventoryRow(
            int releaseIndex,
            String version,
            String commitId,
            String classPath
    ) {
    }

    private record NfixRow(
            int releaseIndex,
            String version,
            String commitId,
            String classPath,
            int nfix
    ) {
    }
}
