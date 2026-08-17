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
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class FixCommitTemporalDiagnostic {

    private static final String BASELINE =
            "origin/baseline-4.1.1";

    private FixCommitTemporalDiagnostic() {
        // Utility class.
    }

    public static void main(String[] args) throws Exception {

        JiraDefectClient jiraClient =
                new JiraDefectClient();

        List<DefectTicket> defects =
                jiraClient.fetchFixedBugTickets();

        Map<String, DefectTicket> defectByKey =
                defects.stream()
                        .collect(
                                Collectors.toMap(
                                        DefectTicket::issueKey,
                                        Function.identity()
                                )
                        );

        GitFixCommitResolver resolver =
                new GitFixCommitResolver(
                        Path.of(".")
                );

        List<FixCommit> candidates =
                resolver.resolve(
                        BASELINE,
                        defects
                );

        long beforeCreation = 0;
        long afterResolution = 0;
        long insideLifecycle = 0;

        long afterResolutionWithinOneDay = 0;
        long afterResolutionBeyondOneDay = 0;

        for (FixCommit candidate : candidates) {

            DefectTicket ticket =
                    defectByKey.get(candidate.issueKey());

            if (ticket == null) {
                throw new IllegalStateException(
                        "Missing JIRA ticket for "
                                + candidate.issueKey()
                );
            }

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
                    afterResolutionWithinOneDay++;
                } else {
                    afterResolutionBeyondOneDay++;
                }

                continue;
            }

            insideLifecycle++;
        }

        System.out.println();
        System.out.println(
                "===== FIX COMMIT TEMPORAL CHECK ====="
        );

        System.out.println(
                "Candidate mappings              : "
                        + candidates.size()
        );

        System.out.println(
                "Inside ticket lifecycle         : "
                        + insideLifecycle
        );

        System.out.println(
                "Before ticket creation          : "
                        + beforeCreation
        );

        System.out.println(
                "After ticket resolution         : "
                        + afterResolution
        );

        System.out.println(
                "  <= 24h after resolution       : "
                        + afterResolutionWithinOneDay
        );

        System.out.println(
                "  > 24h after resolution        : "
                        + afterResolutionBeyondOneDay
        );

        System.out.println();
        System.out.println(
                "Examples before creation:"
        );

        candidates.stream()
                .filter(candidate -> {
                    DefectTicket ticket =
                            defectByKey.get(
                                    candidate.issueKey()
                            );

                    return candidate.commitDate()
                            .isBefore(
                                    ticket.createdDate()
                            );
                })
                .limit(10)
                .forEach(candidate -> {

                    DefectTicket ticket =
                            defectByKey.get(
                                    candidate.issueKey()
                            );

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
                "Examples >24h after resolution:"
        );

        candidates.stream()
                .filter(candidate -> {

                    DefectTicket ticket =
                            defectByKey.get(
                                    candidate.issueKey()
                            );

                    if (!candidate.commitDate()
                            .isAfter(
                                    ticket.resolutionDate()
                            )) {
                        return false;
                    }

                    Duration delay =
                            Duration.between(
                                    ticket.resolutionDate(),
                                    candidate.commitDate()
                            );

                    return delay.toHours() > 24;
                })
                .limit(10)
                .forEach(candidate -> {

                    DefectTicket ticket =
                            defectByKey.get(
                                    candidate.issueKey()
                            );

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
                "====================================="
        );
    }
}
