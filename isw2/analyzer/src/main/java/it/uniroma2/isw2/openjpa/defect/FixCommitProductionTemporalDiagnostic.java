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
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class FixCommitProductionTemporalDiagnostic {

    private static final String BASELINE =
            "origin/baseline-4.1.1";

    private FixCommitProductionTemporalDiagnostic() {
        // Utility class.
    }

    public static void main(String[] args) throws Exception {

        List<DefectTicket> defects =
                new JiraDefectClient()
                        .fetchFixedBugTickets();

        Map<String, DefectTicket> defectByKey =
                defects.stream()
                        .collect(
                                Collectors.toMap(
                                        DefectTicket::issueKey,
                                        Function.identity()
                                )
                        );

        List<FixCommit> allCandidates =
                new GitFixCommitResolver(
                        Path.of(".")
                ).resolve(
                        BASELINE,
                        defects
                );

        List<FixCommit> productionCandidates =
                allCandidates.stream()
                        .filter(
                                FixCommitProductionTemporalDiagnostic
                                        ::touchesProduction
                        )
                        .toList();

        long insideLifecycle = 0;
        long beforeCreation = 0;
        long afterResolution = 0;

        long afterWithin24Hours = 0;
        long afterBeyond24Hours = 0;

        for (FixCommit candidate : productionCandidates) {

            DefectTicket ticket =
                    defectByKey.get(candidate.issueKey());

            if (candidate.commitDate()
                    .isBefore(ticket.createdDate())) {

                beforeCreation++;
                continue;
            }

            if (candidate.commitDate()
                    .isAfter(ticket.resolutionDate())) {

                afterResolution++;

                Duration delay =
                        Duration.between(
                                ticket.resolutionDate(),
                                candidate.commitDate()
                        );

                if (delay.toHours() <= 24) {
                    afterWithin24Hours++;
                } else {
                    afterBeyond24Hours++;
                }

                continue;
            }

            insideLifecycle++;
        }

        System.out.println();
        System.out.println(
                "===== PRODUCTION FIX TEMPORAL CHECK ====="
        );

        System.out.println(
                "Production mappings            : "
                        + productionCandidates.size()
        );

        System.out.println(
                "Inside ticket lifecycle        : "
                        + insideLifecycle
        );

        System.out.println(
                "Before ticket creation         : "
                        + beforeCreation
        );

        System.out.println(
                "After ticket resolution        : "
                        + afterResolution
        );

        System.out.println(
                "  <= 24h after resolution      : "
                        + afterWithin24Hours
        );

        System.out.println(
                "  > 24h after resolution       : "
                        + afterBeyond24Hours
        );

        System.out.println();
        System.out.println(
                "Production mappings before creation:"
        );

        productionCandidates.stream()
                .filter(candidate -> {

                    DefectTicket ticket =
                            defectByKey.get(candidate.issueKey());

                    return candidate.commitDate()
                            .isBefore(ticket.createdDate());
                })
                .forEach(candidate -> {

                    DefectTicket ticket =
                            defectByKey.get(candidate.issueKey());

                    System.out.println(
                            candidate.issueKey()
                                    + " | commit="
                                    + candidate.commitDate()
                                    + " | created="
                                    + ticket.createdDate()
                                    + " | "
                                    + candidate.subject()
                    );
                });

        System.out.println();
        System.out.println(
                "First 15 production mappings >24h after resolution:"
        );

        productionCandidates.stream()
                .filter(candidate -> {

                    DefectTicket ticket =
                            defectByKey.get(candidate.issueKey());

                    if (!candidate.commitDate()
                            .isAfter(ticket.resolutionDate())) {
                        return false;
                    }

                    Duration delay =
                            Duration.between(
                                    ticket.resolutionDate(),
                                    candidate.commitDate()
                            );

                    return delay.toHours() > 24;
                })
                .limit(15)
                .forEach(candidate -> {

                    DefectTicket ticket =
                            defectByKey.get(candidate.issueKey());

                    System.out.println(
                            candidate.issueKey()
                                    + " | resolved="
                                    + ticket.resolutionDate()
                                    + " | commit="
                                    + candidate.commitDate()
                                    + " | "
                                    + candidate.subject()
                    );
                });

        System.out.println(
                "==========================================="
        );
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
                        JavaClassScopeClassifier.classify(file)
                                == ClassScope.PRODUCTION
                );
    }
}
