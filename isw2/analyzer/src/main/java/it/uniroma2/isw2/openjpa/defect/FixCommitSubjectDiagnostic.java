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

import it.uniroma2.isw2.openjpa.inventory.ClassScope;
import it.uniroma2.isw2.openjpa.inventory.JavaClassScopeClassifier;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class FixCommitSubjectDiagnostic {

    private static final String BASELINE =
            "origin/baseline-4.1.1";

    private static final Pattern ISSUE_PATTERN =
            Pattern.compile(
                    "\\bOPENJPA-\\d+\\b",
                    Pattern.CASE_INSENSITIVE
            );

    private FixCommitSubjectDiagnostic() {
        // Utility class.
    }

    public static void main(String[] args) throws Exception {

        List<DefectTicket> defects =
                new JiraDefectClient()
                        .fetchFixedBugTickets();

        List<FixCommit> productionCandidates =
                new GitFixCommitResolver(
                        Path.of(".")
                )
                        .resolve(
                                BASELINE,
                                defects
                        )
                        .stream()
                        .filter(
                                FixCommitSubjectDiagnostic
                                        ::touchesProduction
                        )
                        .toList();

        List<FixCommit> subjectMatches =
                productionCandidates.stream()
                        .filter(
                                FixCommitSubjectDiagnostic
                                        ::issueAppearsInSubject
                        )
                        .toList();

        List<FixCommit> bodyOnlyMatches =
                productionCandidates.stream()
                        .filter(candidate ->
                                !issueAppearsInSubject(candidate)
                        )
                        .toList();

        Set<String> ticketsWithSubjectMatch =
                subjectMatches.stream()
                        .map(FixCommit::issueKey)
                        .collect(Collectors.toSet());

        Set<String> ticketsWithBodyOnlyMatch =
                bodyOnlyMatches.stream()
                        .map(FixCommit::issueKey)
                        .collect(Collectors.toSet());

        Map<String, List<FixCommit>> byTicket =
                productionCandidates.stream()
                        .collect(
                                Collectors.groupingBy(
                                        FixCommit::issueKey
                                )
                        );

        long ticketsOnlyBody =
                byTicket.entrySet()
                        .stream()
                        .filter(entry ->
                                entry.getValue()
                                        .stream()
                                        .noneMatch(
                                                FixCommitSubjectDiagnostic
                                                        ::issueAppearsInSubject
                                        )
                        )
                        .count();

        System.out.println();
        System.out.println(
                "===== FIX COMMIT SUBJECT CHECK ====="
        );

        System.out.println(
                "Production mappings             : "
                        + productionCandidates.size()
        );

        System.out.println(
                "Issue key in subject            : "
                        + subjectMatches.size()
        );

        System.out.println(
                "Issue key only in body          : "
                        + bodyOnlyMatches.size()
        );

        System.out.println(
                "Tickets with subject match      : "
                        + ticketsWithSubjectMatch.size()
        );

        System.out.println(
                "Tickets with body-only mapping  : "
                        + ticketsWithBodyOnlyMatch.size()
        );

        System.out.println(
                "Tickets having ONLY body matches: "
                        + ticketsOnlyBody
        );

        System.out.println();
        System.out.println(
                "First 20 body-only production mappings:"
        );

        bodyOnlyMatches.stream()
                .limit(20)
                .forEach(candidate ->
                        System.out.println(
                                candidate.issueKey()
                                        + " | "
                                        + candidate.commitId()
                                        + " | "
                                        + candidate.subject()
                        )
                );

        System.out.println(
                "===================================="
        );
    }

    private static boolean issueAppearsInSubject(
            FixCommit candidate
    ) {

        Matcher matcher =
                ISSUE_PATTERN.matcher(
                        candidate.subject()
                );

        String target =
                candidate.issueKey()
                        .toUpperCase(Locale.ROOT);

        while (matcher.find()) {

            if (matcher.group()
                    .toUpperCase(Locale.ROOT)
                    .equals(target)) {

                return true;
            }
        }

        return false;
    }

    private static boolean touchesProduction(
            FixCommit candidate
    ) {

        return candidate.changedFiles()
                .stream()
                .filter(file ->
                        file.endsWith(".java")
                )
                .anyMatch(file ->
                        JavaClassScopeClassifier
                                .classify(file)
                                == ClassScope.PRODUCTION
                );
    }
}
