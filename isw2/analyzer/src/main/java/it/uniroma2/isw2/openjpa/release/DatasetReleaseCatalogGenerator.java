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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DatasetReleaseCatalogGenerator {

    private static final String OUTPUT_RELATIVE_PATH =
            "isw2/datasets/release_catalog.csv";

    private static final Pattern STABLE_VERSION_PATTERN =
            Pattern.compile("^\\d+\\.\\d+\\.\\d+$");

    /*
     * Falessi: ignore the last 66% of releases.
     * Therefore we retain the first 34%.
     */
    private static final double DATASET_FRACTION =
            0.33;

    public static void main(String[] args) {

        try {

            Path repositoryRoot =
                    resolveRepositoryRoot(args);

            JiraReleaseClient jiraClient =
                    new JiraReleaseClient();

            List<ReleaseInfo> allReleasedVersions =
                    jiraClient.fetchReleasedVersions();

            List<ReleaseInfo> stableReleases =
                    filterStableReleases(allReleasedVersions);

            GitReleaseResolver gitResolver =
                    new GitReleaseResolver(repositoryRoot);

            List<ResolvedRelease> resolvedReleases =
                    resolveReleases(
                            stableReleases,
                            gitResolver
                    );

            int datasetReleaseCount =
                    computeDatasetReleaseCount(
                            resolvedReleases.size()
                    );

            Path outputPath =
                    repositoryRoot.resolve(
                            OUTPUT_RELATIVE_PATH
                    );

            writeCatalog(
                    resolvedReleases,
                    datasetReleaseCount,
                    outputPath
            );

            printSummary(
                    allReleasedVersions,
                    resolvedReleases,
                    datasetReleaseCount,
                    outputPath
            );

        } catch (Exception exception) {

            System.err.println(
                    "Unable to generate dataset release catalog: "
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

        if (!Files.isDirectory(
                repositoryRoot.resolve(".git")
        )) {
            throw new IllegalArgumentException(
                    "Not a Git repository root: "
                            + repositoryRoot
            );
        }

        return repositoryRoot;
    }

    private static List<ReleaseInfo> filterStableReleases(
            List<ReleaseInfo> releases
    ) {

        return releases.stream()
                .filter(release ->
                        STABLE_VERSION_PATTERN
                                .matcher(
                                        release.getVersion()
                                )
                                .matches()
                )
                .toList();
    }

    private static List<ResolvedRelease> resolveReleases(
            List<ReleaseInfo> releases,
            GitReleaseResolver resolver
    ) throws IOException, InterruptedException {

        List<ResolvedRelease> resolved =
                new ArrayList<>();

        for (ReleaseInfo release : releases) {

            ResolvedRelease resolvedRelease =
                    resolver.resolve(release);

            resolved.add(resolvedRelease);
        }

        return resolved;
    }

    private static int computeDatasetReleaseCount(
            int totalReleases
    ) {

        return (int) Math.ceil(
                totalReleases * DATASET_FRACTION
        );
    }

    private static void writeCatalog(
            List<ResolvedRelease> releases,
            int datasetReleaseCount,
            Path outputPath
    ) throws IOException {

        Files.createDirectories(
                outputPath.getParent()
        );

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
                            + "GitTagMatched,"
                            + "ReleaseCommit,"
                            + "ReleaseCommitDate,"
                            + "ResolutionMethod,"
                            + "DatasetIncluded"
            );

            writer.newLine();

            for (int index = 0;
                 index < releases.size();
                 index++) {

                ResolvedRelease resolved =
                        releases.get(index);

                ReleaseInfo release =
                        resolved.getRelease();

                boolean datasetIncluded =
                        index < datasetReleaseCount;

                writer.write(
                        (index + 1)
                                + ","
                                + csv(release.getJiraId())
                                + ","
                                + csv(release.getVersion())
                                + ","
                                + release.getReleaseDate()
                                + ","
                                + csv(resolved.getGitTag())
                                + ","
                                + resolved.isGitTagMatched()
                                + ","
                                + csv(resolved.getReleaseCommit())
                                + ","
                                + csv(resolved.getReleaseCommitDate())
                                + ","
                                + csv(resolved.getResolutionMethod())
                                + ","
                                + datasetIncluded
                );

                writer.newLine();
            }
        }
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
            List<ReleaseInfo> allReleasedVersions,
            List<ResolvedRelease> stableReleases,
            int datasetReleaseCount,
            Path outputPath
    ) {

        long tagResolved =
                stableReleases.stream()
                        .filter(ResolvedRelease::isGitTagMatched)
                        .count();

        long dateResolved =
                stableReleases.size()
                        - tagResolved;

        System.out.println();
        System.out.println(
                "===== OPENJPA DATASET RELEASE CATALOG ====="
        );

        System.out.println(
                "Released JIRA versions : "
                        + allReleasedVersions.size()
        );

        System.out.println(
                "Stable X.Y.Z releases   : "
                        + stableReleases.size()
        );

        System.out.println(
                "Releases with Git tag   : "
                        + tagResolved
        );

        System.out.println(
                "Release resolution      : DATE_CUTOFF"
        );

        System.out.println(
                "Dataset fraction        : "
                        + DATASET_FRACTION
        );

        System.out.println(
                "Dataset releases        : "
                        + datasetReleaseCount
        );

        if (datasetReleaseCount > 0) {

            ResolvedRelease first =
                    stableReleases.get(0);

            ResolvedRelease lastIncluded =
                    stableReleases.get(
                            datasetReleaseCount - 1
                    );

            System.out.println(
                    "First dataset release  : "
                            + first.getRelease().getVersion()
                            + " ("
                            + first.getRelease().getReleaseDate()
                            + ")"
            );

            System.out.println(
                    "Last dataset release   : "
                            + lastIncluded.getRelease().getVersion()
                            + " ("
                            + lastIncluded.getRelease().getReleaseDate()
                            + ")"
            );
        }

        System.out.println(
                "Output                  : "
                        + outputPath
        );

        System.out.println(
                "========================================="
        );
    }
}