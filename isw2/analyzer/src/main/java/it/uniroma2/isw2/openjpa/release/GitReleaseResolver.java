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

package it.uniroma2.isw2.openjpa.release;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class GitReleaseResolver {

    private static final String BASELINE_REF = "4.1.1";

    private final Path repositoryRoot;
    private final Set<String> gitTags;

    public GitReleaseResolver(Path repositoryRoot)
            throws IOException, InterruptedException {

        this.repositoryRoot =
                repositoryRoot.toAbsolutePath().normalize();

        if (!Files.isDirectory(this.repositoryRoot.resolve(".git"))) {
            throw new IllegalArgumentException(
                    "Not a Git repository root: " + this.repositoryRoot
            );
        }

        this.gitTags = loadGitTags();
    }

    public ResolvedRelease resolve(ReleaseInfo release)
            throws IOException, InterruptedException {

        String matchingTag =
                findMatchingTag(release.getVersion());

        LocalDate dayAfterRelease =
                release.getReleaseDate().plusDays(1);

        String cutoff =
                dayAfterRelease + "T00:00:00Z";

        String commitHash = runGit(
                "rev-list",
                "-1",
                "--before=" + cutoff,
                BASELINE_REF
        );

        if (commitHash.isBlank()) {
            throw new IOException(
                    "No Git commit found for release "
                            + release.getVersion()
                            + " ("
                            + release.getReleaseDate()
                            + ")"
            );
        }

        String commitDate = runGit(
                "show",
                "-s",
                "--format=%cI",
                commitHash
        );

        return new ResolvedRelease(
                release,
                matchingTag,
                commitHash,
                commitDate,
                "DATE_CUTOFF"
        );
    }
    private Set<String> loadGitTags()
            throws IOException, InterruptedException {

        String output = runGit(
                "tag",
                "--list"
        );

        if (output.isBlank()) {
            return new HashSet<>();
        }

        return output.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toSet());
    }

    private String findMatchingTag(String version) {

        if (gitTags.contains(version)) {
            return version;
        }

        String openJpaPrefixed =
                "openjpa-" + version;

        if (gitTags.contains(openJpaPrefixed)) {
            return openJpaPrefixed;
        }

        String vPrefixed =
                "v" + version;

        if (gitTags.contains(vPrefixed)) {
            return vPrefixed;
        }

        return "";
    }

    private String runGit(String... arguments)
            throws IOException, InterruptedException {

        String[] command =
                new String[arguments.length + 3];

        command[0] = "git";
        command[1] = "-C";
        command[2] = repositoryRoot.toString();

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
                     process.inputReader(StandardCharsets.UTF_8)) {

            output = reader.lines()
                    .collect(Collectors.joining(System.lineSeparator()))
                    .trim();
        }

        int exitCode =
                process.waitFor();

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
}