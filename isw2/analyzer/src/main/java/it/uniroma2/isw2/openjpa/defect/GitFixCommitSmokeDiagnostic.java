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

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class GitFixCommitSmokeDiagnostic {

    private static final String DEFAULT_BASELINE =
            "origin/baseline-4.1.1";

    private GitFixCommitSmokeDiagnostic() {
        // Utility class.
    }

    public static void main(String[] args) throws Exception {

        String baselineRef = args.length > 0
                ? args[0]
                : DEFAULT_BASELINE;

        Path repository = Path.of(".")
                .toAbsolutePath()
                .normalize();

        System.out.println(
                "Repository   : " + repository
        );
        System.out.println(
                "Baseline ref : " + baselineRef
        );

        JiraDefectClient jiraClient =
                new JiraDefectClient();

        List<DefectTicket> defects =
                jiraClient.fetchFixedBugTickets();

        GitFixCommitResolver resolver =
                new GitFixCommitResolver(repository);

        List<FixCommit> candidates =
                resolver.resolve(
                        baselineRef,
                        defects
                );

        Set<String> ticketsWithCandidates =
                candidates.stream()
                        .map(FixCommit::issueKey)
                        .collect(Collectors.toSet());

        Set<String> uniqueCommits =
                candidates.stream()
                        .map(FixCommit::commitId)
                        .collect(Collectors.toSet());

        long mergeMappings = candidates.stream()
                .filter(FixCommit::merge)
                .count();

        long revertMappings = candidates.stream()
                .filter(FixCommit::revert)
                .count();

        Map<String, Long> commitsPerTicket =
                candidates.stream()
                        .collect(
                                Collectors.groupingBy(
                                        FixCommit::issueKey,
                                        Collectors.counting()
                                )
                        );

        long ticketsWithMultipleCandidates =
                commitsPerTicket.values()
                        .stream()
                        .filter(count -> count > 1)
                        .count();

        Map<String, Long> ticketsPerCommit =
                candidates.stream()
                        .collect(
                                Collectors.groupingBy(
                                        FixCommit::commitId,
                                        Collectors.counting()
                                )
                        );

        long commitsMentioningMultipleDefects =
                ticketsPerCommit.values()
                        .stream()
                        .filter(count -> count > 1)
                        .count();

        long candidatesWithJavaFiles =
                candidates.stream()
                        .filter(candidate ->
                                candidate.changedFiles()
                                        .stream()
                                        .anyMatch(file ->
                                                file.endsWith(".java")
                                        )
                        )
                        .count();

        long ticketsWithoutCandidates =
                defects.size()
                        - ticketsWithCandidates.size();

        System.out.println();
        System.out.println(
                "===== FIX COMMIT SMOKE CHECK ====="
        );
        System.out.println(
                "Validated JIRA defects          : "
                        + defects.size()
        );
        System.out.println(
                "Candidate mappings              : "
                        + candidates.size()
        );
        System.out.println(
                "Tickets with candidate commit   : "
                        + ticketsWithCandidates.size()
        );
        System.out.println(
                "Tickets without candidate       : "
                        + ticketsWithoutCandidates
        );
        System.out.println(
                "Unique candidate commits        : "
                        + uniqueCommits.size()
        );
        System.out.println(
                "Merge mappings                  : "
                        + mergeMappings
        );
        System.out.println(
                "Revert mappings                 : "
                        + revertMappings
        );
        System.out.println(
                "Tickets with >1 candidate       : "
                        + ticketsWithMultipleCandidates
        );
        System.out.println(
                "Commits mentioning >1 defect    : "
                        + commitsMentioningMultipleDefects
        );
        System.out.println(
                "Mappings touching Java          : "
                        + candidatesWithJavaFiles
        );

        System.out.println();
        System.out.println("First 10 candidate mappings:");

        candidates.stream()
                .limit(10)
                .forEach(candidate ->
                        System.out.println(
                                candidate.issueKey()
                                        + " | "
                                        + candidate.commitId()
                                        + " | merge="
                                        + candidate.merge()
                                        + " | revert="
                                        + candidate.revert()
                                        + " | files="
                                        + candidate.changedFiles().size()
                                        + " | "
                                        + candidate.subject()
                        )
                );

        System.out.println(
                "=================================="
        );
    }
}
