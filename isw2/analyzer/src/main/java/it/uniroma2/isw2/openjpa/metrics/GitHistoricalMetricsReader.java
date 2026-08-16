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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GitHistoricalMetricsReader {

    private final Path repositoryRoot;

    private final Map<String, Integer> changeSetSizeCache =
            new HashMap<>();

    public GitHistoricalMetricsReader(Path repositoryRoot) {

        this.repositoryRoot =
                repositoryRoot
                        .toAbsolutePath()
                        .normalize();

        if (!Files.isDirectory(
                this.repositoryRoot.resolve(".git")
        )) {
            throw new IllegalArgumentException(
                    "Not a Git repository root: "
                            + this.repositoryRoot
            );
        }
    }

    public HistoricalMetrics compute(
            String releaseCommit,
            LocalDate releaseDate,
            String filePath
    ) throws IOException, InterruptedException {

        List<Revision> rawRevisions =
                readHistory(
                        releaseCommit,
                        filePath
                );

        List<Revision> revisions =
                rawRevisions.stream()
                        .filter(revision ->
                                revision.locTouched() > 0
                        )
                        .toList();

        if (revisions.isEmpty()) {
            throw new IllegalStateException(
                    "No effective source revisions found for "
                            + filePath
                            + " at "
                            + releaseCommit
            );
        }

        int ignoredZeroLocRevisions =
                rawRevisions.size()
                        - revisions.size();

        int nr =
                revisions.size();

        Set<String> authors =
                new HashSet<>();

        long locAdded = 0;
        long locDeleted = 0;
        long locTouched = 0;

        long maxLocAdded = 0;
        long maxChurn =
                Long.MIN_VALUE;

        long changeSetSize = 0;
        int maxChangeSet = 0;

        double weightedAgeNumerator = 0.0;

        LocalDate oldestRevisionDate =
                null;

        for (Revision revision : revisions) {

            authors.add(
                    revision.author()
                            .trim()
                            .toLowerCase(Locale.ROOT)
            );

            locAdded +=
                    revision.added();

            locDeleted +=
                    revision.deleted();

            locTouched +=
                    revision.locTouched();

            maxLocAdded =
                    Math.max(
                            maxLocAdded,
                            revision.added()
                    );

            long revisionChurn =
                    revision.churn();

            maxChurn =
                    Math.max(
                            maxChurn,
                            revisionChurn
                    );

            int revisionChangeSetSize =
                    getChangeSetSize(
                            revision.commitId()
                    );

            changeSetSize +=
                    revisionChangeSetSize;

            maxChangeSet =
                    Math.max(
                            maxChangeSet,
                            revisionChangeSetSize
                    );

            LocalDate revisionDate =
                    revision.commitDate()
                            .withOffsetSameInstant(
                                    ZoneOffset.UTC
                            )
                            .toLocalDate();

            if (oldestRevisionDate == null
                    || revisionDate.isBefore(
                    oldestRevisionDate
            )) {
                oldestRevisionDate =
                        revisionDate;
            }

            double revisionAgeWeeks =
                    ageWeeks(
                            revisionDate,
                            releaseDate
                    );

            weightedAgeNumerator +=
                    revisionAgeWeeks
                            * revision.locTouched();
        }

        long churn =
                locAdded - locDeleted;

        double avgLocAdded =
                (double) locAdded / nr;

        double avgChurn =
                (double) churn / nr;

        double avgChangeSet =
                (double) changeSetSize / nr;

        double ageWeeks =
                ageWeeks(
                        oldestRevisionDate,
                        releaseDate
                );

        double weightedAgeWeeks =
                locTouched == 0
                        ? ageWeeks
                        : weightedAgeNumerator
                        / locTouched;

        return new HistoricalMetrics(
                locTouched,
                nr,
                authors.size(),
                locAdded,
                maxLocAdded,
                avgLocAdded,
                churn,
                maxChurn,
                avgChurn,
                changeSetSize,
                maxChangeSet,
                avgChangeSet,
                ageWeeks,
                weightedAgeWeeks,
                ignoredZeroLocRevisions
        );
    }

    private List<Revision> readHistory(
            String releaseCommit,
            String filePath
    ) throws IOException, InterruptedException {

        String output = runGit(
                "-c",
                "diff.renameLimit=0",
                "log",
                releaseCommit,
                "--follow",
                "--no-merges",
                "--format=@@@%H%x1f%aE%x1f%cI",
                "--numstat",
                "--",
                filePath
        );

        List<Revision> revisions =
                new ArrayList<>();

        RevisionBuilder current =
                null;

        for (String line :
                output.split("\\R")) {

            if (line.startsWith("@@@")) {

                if (current != null) {
                    revisions.add(
                            current.build()
                    );
                }

                String[] fields =
                        line.substring(3)
                                .split(
                                        "\u001F",
                                        -1
                                );

                if (fields.length != 3) {
                    throw new IOException(
                            "Malformed Git history header: "
                                    + line
                    );
                }

                current =
                        new RevisionBuilder(
                                fields[0],
                                fields[1],
                                OffsetDateTime.parse(
                                        fields[2]
                                )
                        );

                continue;
            }

            if (line.isBlank()
                    || current == null) {
                continue;
            }

            String[] fields =
                    line.split(
                            "\t",
                            3
                    );

            if (fields.length < 2) {
                continue;
            }

            if ("-".equals(fields[0])
                    || "-".equals(fields[1])) {
                throw new IOException(
                        "Binary numstat encountered for Java file: "
                                + filePath
                );
            }

            try {

                long added =
                        Long.parseLong(
                                fields[0]
                        );

                long deleted =
                        Long.parseLong(
                                fields[1]
                        );

                current.add(
                        added,
                        deleted
                );

            } catch (NumberFormatException ignored) {
                // Not a numstat line.
            }
        }

        if (current != null) {
            revisions.add(
                    current.build()
            );
        }

        return revisions;
    }

    private int getChangeSetSize(
            String commitId
    ) throws IOException, InterruptedException {

        Integer cached =
                changeSetSizeCache.get(
                        commitId
                );

        if (cached != null) {
            return cached;
        }

        String output = runGit(
                "-c",
                "diff.renameLimit=0",
                "diff-tree",
                "--root",
                "--no-commit-id",
                "--name-only",
                "-r",
                "-M",
                commitId
        );

        int count;

        if (output.isBlank()) {

            count = 0;

        } else {

            count =
                    (int) output.lines()
                            .map(String::trim)
                            .filter(line ->
                                    !line.isEmpty()
                            )
                            .distinct()
                            .count();
        }

        changeSetSizeCache.put(
                commitId,
                count
        );

        return count;
    }

    private static double ageWeeks(
            LocalDate revisionDate,
            LocalDate releaseDate
    ) {

        long days =
                releaseDate.toEpochDay()
                        - revisionDate.toEpochDay();

        if (days < 0) {
            throw new IllegalStateException(
                    "Revision date "
                            + revisionDate
                            + " is after release date "
                            + releaseDate
            );
        }

        return days / 7.0;
    }

    private String runGit(
            String... arguments
    ) throws IOException, InterruptedException {

        String[] command =
                new String[arguments.length + 3];

        command[0] = "git";
        command[1] = "-C";
        command[2] =
                repositoryRoot.toString();

        System.arraycopy(
                arguments,
                0,
                command,
                3,
                arguments.length
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

    private record Revision(
            String commitId,
            String author,
            OffsetDateTime commitDate,
            long added,
            long deleted
    ) {

        long locTouched() {
            return added + deleted;
        }

        long churn() {
            return added - deleted;
        }
    }

    private static class RevisionBuilder {

        private final String commitId;
        private final String author;
        private final OffsetDateTime commitDate;

        private long added;
        private long deleted;

        RevisionBuilder(
                String commitId,
                String author,
                OffsetDateTime commitDate
        ) {
            this.commitId =
                    commitId;

            this.author =
                    author;

            this.commitDate =
                    commitDate;
        }

        void add(
                long added,
                long deleted
        ) {

            this.added +=
                    added;

            this.deleted +=
                    deleted;
        }

        Revision build() {

            return new Revision(
                    commitId,
                    author,
                    commitDate,
                    added,
                    deleted
            );
        }
    }
}
