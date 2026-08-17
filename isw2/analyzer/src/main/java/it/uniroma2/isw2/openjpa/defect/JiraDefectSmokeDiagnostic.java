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

import java.util.List;

public final class JiraDefectSmokeDiagnostic {

    private JiraDefectSmokeDiagnostic() {
        // Utility class.
    }

    public static void main(String[] args) throws Exception {

        JiraDefectClient client = new JiraDefectClient();

        List<DefectTicket> tickets =
                client.fetchFixedBugTickets();

        long withoutAffectedVersions = tickets.stream()
                .filter(ticket -> ticket.affectedVersions().isEmpty())
                .count();

        long withoutFixVersions = tickets.stream()
                .filter(ticket -> ticket.fixVersions().isEmpty())
                .count();

        long withoutResolutionDate = tickets.stream()
                .filter(ticket -> ticket.resolutionDate() == null)
                .count();

        System.out.println();
        System.out.println("===== JIRA DEFECT SMOKE CHECK =====");
        System.out.println("Tickets                  : "
                + tickets.size());
        System.out.println("Without affected versions: "
                + withoutAffectedVersions);
        System.out.println("Without fix versions     : "
                + withoutFixVersions);
        System.out.println("Without resolution date  : "
                + withoutResolutionDate);
        System.out.println();

        System.out.println("First 5 tickets:");

        tickets.stream()
                .limit(5)
                .forEach(ticket -> {
                    System.out.println(
                            ticket.issueKey()
                                    + " | "
                                    + ticket.status()
                                    + " | "
                                    + ticket.resolution()
                                    + " | AV="
                                    + ticket.affectedVersions()
                                    + " | FV="
                                    + ticket.fixVersions()
                    );
                });

        System.out.println(
                "==================================="
        );
    }
}
