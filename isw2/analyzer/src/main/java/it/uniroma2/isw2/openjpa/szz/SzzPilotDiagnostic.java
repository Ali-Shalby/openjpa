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

package it.uniroma2.isw2.openjpa.szz;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class SzzPilotDiagnostic {

    private static final String ISSUE_KEY =
            "OPENJPA-14";

    private static final String FIX_COMMIT =
            "dcb47f0fe406c5d926c2d0e654b6690a15111cf1";

    private SzzPilotDiagnostic() {
        // Utility class.
    }

    public static void main(String[] args) throws Exception {

        Path repository =
                Path.of(".")
                        .toAbsolutePath()
                        .normalize();

        SzzAnalyzer analyzer =
                new SzzAnalyzer(repository);

        List<SzzEvidence> evidence =
                analyzer.analyze(
                        ISSUE_KEY,
                        FIX_COMMIT
                );

        Set<String> parentCommits =
                evidence.stream()
                        .map(SzzEvidence::parentCommitId)
                        .collect(Collectors.toSet());

        Set<String> fixedFiles =
                evidence.stream()
                        .map(SzzEvidence::fixedFilePath)
                        .collect(Collectors.toSet());

        Set<String> blamedFiles =
                evidence.stream()
                        .map(SzzEvidence::blamedFilePath)
                        .collect(Collectors.toSet());

        Set<String> bugIntroducingCommits =
                evidence.stream()
                        .map(
                                SzzEvidence
                                        ::bugIntroducingCommitId
                        )
                        .collect(Collectors.toSet());

        int blamedLines =
                evidence.stream()
                        .mapToInt(
                                SzzEvidence::blamedLineCount
                        )
                        .sum();

        System.out.println();
        System.out.println(
                "===== SZZ PILOT CHECK ====="
        );

        System.out.println(
                "Issue                    : "
                        + ISSUE_KEY
        );

        System.out.println(
                "Fix commit               : "
                        + FIX_COMMIT
        );

        System.out.println(
                "Evidence rows             : "
                        + evidence.size()
        );

        System.out.println(
                "Parent commits            : "
                        + parentCommits.size()
        );

        System.out.println(
                "Fixed production files    : "
                        + fixedFiles.size()
        );

        System.out.println(
                "Blamed files              : "
                        + blamedFiles.size()
        );

        System.out.println(
                "Bug-introducing commits   : "
                        + bugIntroducingCommits.size()
        );

        System.out.println(
                "Total blamed lines        : "
                        + blamedLines
        );

        System.out.println();

        if (evidence.isEmpty()) {

            System.out.println(
                    "No deleted/modified production "
                            + "lines produced SZZ evidence."
            );

        } else {

            System.out.println("Evidence:");

            for (SzzEvidence row : evidence) {

                System.out.println();

                System.out.println(
                        "  Fixed file : "
                                + row.fixedFilePath()
                );

                System.out.println(
                        "  Blamed file: "
                                + row.blamedFilePath()
                );

                System.out.println(
                        "  Parent     : "
                                + row.parentCommitId()
                );

                System.out.println(
                        "  BIC        : "
                                + row.bugIntroducingCommitId()
                );

                System.out.println(
                        "  Lines      : "
                                + row.blamedLineCount()
                );
            }
        }

        System.out.println();
        System.out.println(
                "==========================="
        );
    }
}
