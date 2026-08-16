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
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ReleaseCatalogGenerator {

    private static final String OUTPUT_RELATIVE_PATH =
            "isw2/datasets/release_catalog_raw.csv";

    public static void main(String[] args) {

        try {
            Path repositoryRoot = resolveRepositoryRoot(args);

            JiraReleaseClient jiraClient = new JiraReleaseClient();
            List<ReleaseInfo> releases =
                    jiraClient.fetchReleasedVersions();

            Set<String> gitTags =
                    loadGitTags(repositoryRoot);

            Path outputPath =
                    repositoryRoot.resolve(OUTPUT_RELATIVE_PATH);

            writeCatalog(
                    releases,
                    gitTags,
                    outputPath
            );

            printSummary(
                    releases,
                    gitTags,
                    outputPath
            );

        } catch (Exception exception) {
            System.err.println(
                    "Unable to generate release catalog: "
                            + exception.getMessage()
            );

            exception.printStackTrace();
            System.exit(1);
        }
    }

    private static Path resolveRepositoryRoot(String[] args) {

        if (args.length == 0) {
            throw new IllegalArgumentException(
                    "Repository root path is required."
            );
        }

        Path repositoryRoot =
                Paths.get(args[0])
                        .toAbsolutePath()
                        .normalize();

        if (!Files.isDirectory(repositoryRoot.resolve(".git"))) {
            throw new IllegalArgumentException(
                    "Not a Git repository root: "
                            + repositoryRoot
            );
        }

        return repositoryRoot;
    }

    private static Set<String> loadGitTags(Path repositoryRoot)
            throws IOException, InterruptedException {

        Process process = new ProcessBuilder(
                "git",
                "-C",
                repositoryRoot.toString(),
                "tag",
                "--list"
        )
                .redirectErrorStream(true)
                .start();

        List<String> lines;

        try (BufferedReader reader =
                     process.inputReader(StandardCharsets.UTF_8)) {

            lines = reader.lines()
                    .collect(Collectors.toList());
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException(
                    "Unable to read Git tags. Exit code: "
                            + exitCode
            );
        }

        return new HashSet<>(lines);
    }

    private static void writeCatalog(
            List<ReleaseInfo> releases,
            Set<String> gitTags,
            Path outputPath
    ) throws IOException {

        Files.createDirectories(outputPath.getParent());

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             outputPath,
                             StandardCharsets.UTF_8
                     )) {

            writer.write(
                    "ChronologicalIndex,"
                            + "JiraVersionId,"
                            + "Version,"
                            + "ReleaseDate,"
                            + "GitTag,"
                            + "GitTagMatched"
            );

            writer.newLine();

            for (int index = 0; index < releases.size(); index++) {

                ReleaseInfo release =
                        releases.get(index);

                String matchingTag =
                        findMatchingTag(
                                release.getVersion(),
                                gitTags
                        );

                boolean tagMatched =
                        !matchingTag.isEmpty();

                writer.write(
                        (index + 1)
                                + ","
                                + csv(release.getJiraId())
                                + ","
                                + csv(release.getVersion())
                                + ","
                                + release.getReleaseDate()
                                + ","
                                + csv(matchingTag)
                                + ","
                                + tagMatched
                );

                writer.newLine();
            }
        }
    }

    private static String findMatchingTag(
            String version,
            Set<String> gitTags
    ) {

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

    private static String csv(String value) {

        if (value == null) {
            return "";
        }

        String escaped =
                value.replace("\"", "\"\"");

        return "\"" + escaped + "\"";
    }

    private static void printSummary(
            List<ReleaseInfo> releases,
            Set<String> gitTags,
            Path outputPath
    ) {

        long matched =
                releases.stream()
                        .filter(release ->
                                !findMatchingTag(
                                        release.getVersion(),
                                        gitTags
                                ).isEmpty()
                        )
                        .count();

        System.out.println();
        System.out.println(
                "===== OPENJPA RELEASE CATALOG ====="
        );

        System.out.println(
                "Released JIRA versions : "
                        + releases.size()
        );

        System.out.println(
                "Matching Git tags       : "
                        + matched
                        + "/"
                        + releases.size()
        );

        if (!releases.isEmpty()) {

            ReleaseInfo first =
                    releases.get(0);

            ReleaseInfo last =
                    releases.get(
                            releases.size() - 1
                    );

            System.out.println(
                    "First release          : "
                            + first.getVersion()
                            + " ("
                            + first.getReleaseDate()
                            + ")"
            );

            System.out.println(
                    "Last release           : "
                            + last.getVersion()
                            + " ("
                            + last.getReleaseDate()
                            + ")"
            );
        }

        System.out.println(
                "Output                  : "
                        + outputPath
        );

        System.out.println(
                "=================================="
        );
    }
}