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
import java.util.Set;
import java.util.stream.Collectors;

public final class FixCommitProductionScopeDiagnostic {

    private static final String BASELINE =
            "origin/baseline-4.1.1";

    private FixCommitProductionScopeDiagnostic() {
        // Utility class.
    }

    public static void main(String[] args) throws Exception {

        List<DefectTicket> defects =
                new JiraDefectClient()
                        .fetchFixedBugTickets();

        List<FixCommit> candidates =
                new GitFixCommitResolver(
                        Path.of(".")
                ).resolve(
                        BASELINE,
                        defects
                );

        long touchingAnyJava = candidates.stream()
                .filter(
                        FixCommitProductionScopeDiagnostic
                                ::touchesAnyJava
                )
                .count();

        long touchingProduction = candidates.stream()
                .filter(
                        FixCommitProductionScopeDiagnostic
                                ::touchesProductionJava
                )
                .count();

        long javaButNoProduction = candidates.stream()
                .filter(
                        FixCommitProductionScopeDiagnostic
                                ::touchesAnyJava
                )
                .filter(candidate ->
                        !touchesProductionJava(candidate)
                )
                .count();

        long noJava = candidates.stream()
                .filter(candidate ->
                        !touchesAnyJava(candidate)
                )
                .count();

        Set<String> ticketsWithProduction =
                candidates.stream()
                        .filter(
                                FixCommitProductionScopeDiagnostic
                                        ::touchesProductionJava
                        )
                        .map(FixCommit::issueKey)
                        .collect(Collectors.toSet());

        Set<String> productionCommits =
                candidates.stream()
                        .filter(
                                FixCommitProductionScopeDiagnostic
                                        ::touchesProductionJava
                        )
                        .map(FixCommit::commitId)
                        .collect(Collectors.toSet());

        long productionMergeMappings =
                candidates.stream()
                        .filter(
                                FixCommitProductionScopeDiagnostic
                                        ::touchesProductionJava
                        )
                        .filter(FixCommit::merge)
                        .count();

        long productionRevertMappings =
                candidates.stream()
                        .filter(
                                FixCommitProductionScopeDiagnostic
                                        ::touchesProductionJava
                        )
                        .filter(FixCommit::revert)
                        .count();

        System.out.println();
        System.out.println(
                "===== FIX COMMIT PRODUCTION SCOPE CHECK ====="
        );

        System.out.println(
                "Candidate mappings              : "
                        + candidates.size()
        );

        System.out.println(
                "Mappings touching any Java      : "
                        + touchingAnyJava
        );

        System.out.println(
                "Mappings touching PRODUCTION    : "
                        + touchingProduction
        );

        System.out.println(
                "Java but no PRODUCTION          : "
                        + javaButNoProduction
        );

        System.out.println(
                "Mappings without Java           : "
                        + noJava
        );

        System.out.println(
                "Tickets with PRODUCTION commit  : "
                        + ticketsWithProduction.size()
        );

        System.out.println(
                "Unique PRODUCTION commits       : "
                        + productionCommits.size()
        );

        System.out.println(
                "PRODUCTION merge mappings       : "
                        + productionMergeMappings
        );

        System.out.println(
                "PRODUCTION revert mappings      : "
                        + productionRevertMappings
        );

        System.out.println();
        System.out.println(
                "Examples Java but no PRODUCTION:"
        );

        candidates.stream()
                .filter(
                        FixCommitProductionScopeDiagnostic
                                ::touchesAnyJava
                )
                .filter(candidate ->
                        !touchesProductionJava(candidate)
                )
                .limit(10)
                .forEach(candidate ->
                        printCandidate(candidate)
                );

        System.out.println();
        System.out.println(
                "Examples without Java:"
        );

        candidates.stream()
                .filter(candidate ->
                        !touchesAnyJava(candidate)
                )
                .limit(10)
                .forEach(candidate ->
                        printCandidate(candidate)
                );

        System.out.println(
                "=============================================="
        );
    }

    private static boolean touchesAnyJava(
            FixCommit candidate
    ) {

        return candidate.changedFiles()
                .stream()
                .anyMatch(file ->
                        file.endsWith(".java")
                );
    }

    private static boolean touchesProductionJava(
            FixCommit candidate
    ) {

        return candidate.changedFiles()
                .stream()
                .filter(file ->
                        file.endsWith(".java")
                )
                .anyMatch(file ->
                        JavaClassScopeClassifier.classify(file)
                                == ClassScope.PRODUCTION
                );
    }

    private static void printCandidate(
            FixCommit candidate
    ) {

        List<String> javaFiles =
                candidate.changedFiles()
                        .stream()
                        .filter(file ->
                                file.endsWith(".java")
                        )
                        .toList();

        System.out.println(
                candidate.issueKey()
                        + " | "
                        + candidate.commitId()
                        + " | java="
                        + javaFiles.size()
                        + " | "
                        + candidate.subject()
        );

        javaFiles.stream()
                .limit(5)
                .forEach(file ->
                        System.out.println(
                                "    "
                                        + JavaClassScopeClassifier
                                        .classify(file)
                                        + " | "
                                        + file
                        )
                );
    }
}
