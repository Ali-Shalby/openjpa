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

package it.uniroma2.isw2.openjpa.defect;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GitFixCommitResolver {

    /*
     * Exact JIRA-key extraction.
     *
     * Case insensitive because historical commit messages may not always
     * use exactly the same capitalization.
     *
     * Word boundaries prevent OPENJPA-75 from matching OPENJPA-750.
     */
    private static final Pattern ISSUE_KEY_PATTERN =
            Pattern.compile(
                    "\\bOPENJPA-\\d+\\b",
                    Pattern.CASE_INSENSITIVE
            );

    /*
     * Git pretty-format separators.
     *
     * Record Separator = one Git commit
     * Unit Separator   = fields inside the commit
     */
    private static final char RECORD_SEPARATOR = '\u001e';
    private static final char FIELD_SEPARATOR = '\u001f';

    private final Path repository;

    public GitFixCommitResolver(Path repository) {
        this.repository = repository.toAbsolutePath().normalize();
    }

    public List<FixCommit> resolve(
            String baselineRef,
            List<DefectTicket> defects
    ) throws IOException, InterruptedException {

        if (baselineRef == null || baselineRef.isBlank()) {
            throw new IllegalArgumentException(
                    "baselineRef must not be blank."
            );
        }

        Set<String> validIssueKeys = new HashSet<>();

        for (DefectTicket defect : defects) {
            validIssueKeys.add(
                    defect.issueKey().toUpperCase(Locale.ROOT)
            );
        }

        verifyGitRepository();
        verifyRef(baselineRef);

        String history = runGit(
                "log",
                baselineRef,
                "--date=iso-strict",
                "--name-only",
                "--pretty=format:%x1e%H%x1f%cI%x1f%P%x1f%s%x1f%B%x1f"
        );

        return parseHistory(
                history,
                validIssueKeys
        );
    }

    private List<FixCommit> parseHistory(
            String history,
            Set<String> validIssueKeys
    ) {

        List<FixCommit> result = new ArrayList<>();

        String[] commitRecords =
                history.split(
                        String.valueOf(RECORD_SEPARATOR)
                );

        for (String rawRecord : commitRecords) {

            if (rawRecord.isBlank()) {
                continue;
            }

            ParsedCommit commit = parseCommit(rawRecord);

            Set<String> mentionedIssueKeys =
                    extractIssueKeys(
                            commit.message(),
                            validIssueKeys
                    );

            for (String issueKey : mentionedIssueKeys) {

                result.add(
                        new FixCommit(
                                issueKey,
                                commit.commitId(),
                                commit.commitDate(),
                                commit.subject(),
                                commit.merge(),
                                commit.revert(),
                                List.copyOf(
                                        commit.changedFiles()
                                )
                        )
                );
            }
        }

        return result;
    }

    private ParsedCommit parseCommit(String rawRecord) {

        /*
         * Limit = 6 is important:
         * the sixth field contains the file list produced by --name-only.
         */
        String[] fields = rawRecord.split(
                String.valueOf(FIELD_SEPARATOR),
                6
        );

        if (fields.length < 6) {
            throw new IllegalStateException(
                    "Unexpected git log record."
            );
        }

        String commitId = fields[0].trim();
        OffsetDateTime commitDate =
                OffsetDateTime.parse(fields[1].trim());

        String parents = fields[2].trim();
        String subject = fields[3].trim();
        String body = fields[4];

        List<String> changedFiles =
                parseChangedFiles(fields[5]);

        boolean merge = !parents.isBlank()
                && parents.split("\\s+").length > 1;

        boolean revert = isRevert(subject, body);

        String message;

        if (body == null || body.isBlank()) {
            message = subject;
        } else {
            message = subject + "\n" + body;
        }

        return new ParsedCommit(
                commitId,
                commitDate,
                subject,
                message,
                merge,
                revert,
                changedFiles
        );
    }

    private static List<String> parseChangedFiles(
            String filesSection
    ) {

        List<String> files = new ArrayList<>();

        String[] lines = filesSection.split("\\R");

        for (String line : lines) {

            String file = line.trim();

            if (!file.isBlank()) {
                files.add(file);
            }
        }

        return files;
    }

    private static Set<String> extractIssueKeys(
            String message,
            Set<String> validIssueKeys
    ) {

        Set<String> result =
                new LinkedHashSet<>();

        Matcher matcher =
                ISSUE_KEY_PATTERN.matcher(message);

        while (matcher.find()) {

            String issueKey =
                    matcher.group()
                            .toUpperCase(Locale.ROOT);

            /*
             * A commit mentioning OPENJPA-XXXX is considered only if
             * OPENJPA-XXXX belongs to the validated JIRA defect catalog.
             */
            if (validIssueKeys.contains(issueKey)) {
                result.add(issueKey);
            }
        }

        return result;
    }

    private static boolean isRevert(
            String subject,
            String body
    ) {

        String normalizedSubject =
                subject == null
                        ? ""
                        : subject
                        .strip()
                        .toLowerCase(Locale.ROOT);

        if (normalizedSubject.startsWith("revert")) {
            return true;
        }

        if (body == null) {
            return false;
        }

        String normalizedBody =
                body.toLowerCase(Locale.ROOT);

        return normalizedBody.contains(
                "this reverts commit"
        );
    }

    private void verifyGitRepository()
            throws IOException, InterruptedException {

        String result = runGit(
                "rev-parse",
                "--is-inside-work-tree"
        ).trim();

        if (!"true".equals(result)) {
            throw new IOException(
                    "Not a Git working tree: "
                            + repository
            );
        }
    }

    private void verifyRef(String ref)
            throws IOException, InterruptedException {

        runGit(
                "rev-parse",
                "--verify",
                ref + "^{commit}"
        );
    }

    private String runGit(String... arguments)
            throws IOException, InterruptedException {

        List<String> command =
                new ArrayList<>();

        command.add("git");
        command.add("-C");
        command.add(repository.toString());

        for (String argument : arguments) {
            command.add(argument);
        }

        ProcessBuilder builder =
                new ProcessBuilder(command);

        builder.redirectErrorStream(true);

        Process process = builder.start();

        byte[] outputBytes =
                process.getInputStream().readAllBytes();

        int exitCode = process.waitFor();

        String output =
                new String(
                        outputBytes,
                        StandardCharsets.UTF_8
                );

        if (exitCode != 0) {
            throw new IOException(
                    "Git command failed (exit "
                            + exitCode
                            + "): "
                            + String.join(" ", command)
                            + System.lineSeparator()
                            + output
            );
        }

        return output;
    }

    private record ParsedCommit(
            String commitId,
            OffsetDateTime commitDate,
            String subject,
            String message,
            boolean merge,
            boolean revert,
            List<String> changedFiles
    ) {
    }
}
