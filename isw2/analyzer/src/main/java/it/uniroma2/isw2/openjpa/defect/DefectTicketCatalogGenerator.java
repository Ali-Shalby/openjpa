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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class DefectTicketCatalogGenerator {

    private static final Path DEFAULT_OUTPUT =
            Path.of(
                    "isw2",
                    "datasets",
                    "defect_ticket_catalog_raw.csv"
            );

    private DefectTicketCatalogGenerator() {
        // Utility class.
    }

    public static void main(String[] args) throws Exception {

        Path output = args.length > 0
                ? Path.of(args[0])
                : DEFAULT_OUTPUT;

        JiraDefectClient client = new JiraDefectClient();

        List<DefectTicket> tickets =
                client.fetchFixedBugTickets();

        validateTickets(tickets);

        writeCsv(output, tickets);

        long withoutAffectedVersions = tickets.stream()
                .filter(ticket ->
                        ticket.affectedVersions().isEmpty())
                .count();

        long withoutFixVersions = tickets.stream()
                .filter(ticket ->
                        ticket.fixVersions().isEmpty())
                .count();

        long withoutResolutionDate = tickets.stream()
                .filter(ticket ->
                        ticket.resolutionDate() == null)
                .count();

        System.out.println();
        System.out.println(
                "===== DEFECT TICKET CATALOG ====="
        );
        System.out.println(
                "Tickets                  : "
                        + tickets.size()
        );
        System.out.println(
                "Without affected versions: "
                        + withoutAffectedVersions
        );
        System.out.println(
                "Without fix versions     : "
                        + withoutFixVersions
        );
        System.out.println(
                "Without resolution date  : "
                        + withoutResolutionDate
        );
        System.out.println(
                "Output                   : "
                        + output.toAbsolutePath()
        );
        System.out.println(
                "================================="
        );
    }

    private static void validateTickets(
            List<DefectTicket> tickets
    ) {

        long duplicateKeys = tickets.stream()
                .collect(
                        Collectors.groupingBy(
                                DefectTicket::issueKey,
                                Collectors.counting()
                        )
                )
                .values()
                .stream()
                .filter(count -> count > 1)
                .count();

        if (duplicateKeys != 0) {
            throw new IllegalStateException(
                    "Duplicate JIRA issue keys: "
                            + duplicateKeys
            );
        }

        long invalidStatus = tickets.stream()
                .filter(ticket ->
                        !ticket.status().equals("Closed")
                                && !ticket.status()
                                .equals("Resolved"))
                .count();

        if (invalidStatus != 0) {
            throw new IllegalStateException(
                    "Unexpected ticket statuses: "
                            + invalidStatus
            );
        }

        long invalidResolution = tickets.stream()
                .filter(ticket ->
                        !ticket.resolution().equals("Fixed"))
                .count();

        if (invalidResolution != 0) {
            throw new IllegalStateException(
                    "Unexpected ticket resolutions: "
                            + invalidResolution
            );
        }

        long missingRequiredFields = tickets.stream()
                .filter(ticket ->
                        ticket.issueId() == null
                                || ticket.issueId().isBlank()
                                || ticket.issueKey() == null
                                || ticket.issueKey().isBlank()
                                || ticket.createdDate() == null)
                .count();

        if (missingRequiredFields != 0) {
            throw new IllegalStateException(
                    "Tickets with missing required fields: "
                            + missingRequiredFields
            );
        }
    }

    private static void writeCsv(
            Path output,
            List<DefectTicket> tickets
    ) throws IOException {

        Path parent = output.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        List<DefectTicket> orderedTickets =
                tickets.stream()
                        .sorted(
                                Comparator
                                        .comparing(
                                                DefectTicket::createdDate
                                        )
                                        .thenComparing(
                                                DefectTicket::issueKey
                                        )
                        )
                        .toList();

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             output,
                             StandardCharsets.UTF_8
                     )) {

            writer.write(
                    "IssueId,"
                            + "IssueKey,"
                            + "Summary,"
                            + "Status,"
                            + "Resolution,"
                            + "CreatedDate,"
                            + "ResolutionDate,"
                            + "AffectedVersions,"
                            + "FixVersions"
            );

            writer.newLine();

            for (DefectTicket ticket : orderedTickets) {

                writer.write(
                        csv(ticket.issueId())
                                + ","
                                + csv(ticket.issueKey())
                                + ","
                                + csv(ticket.summary())
                                + ","
                                + csv(ticket.status())
                                + ","
                                + csv(ticket.resolution())
                                + ","
                                + csv(
                                ticket.createdDate()
                                        .toString()
                        )
                                + ","
                                + csv(
                                ticket.resolutionDate()
                                        == null
                                        ? ""
                                        : ticket
                                        .resolutionDate()
                                        .toString()
                        )
                                + ","
                                + csv(
                                joinVersions(
                                        ticket.affectedVersions()
                                )
                        )
                                + ","
                                + csv(
                                joinVersions(
                                        ticket.fixVersions()
                                )
                        )
                );

                writer.newLine();
            }
        }
    }

    private static String joinVersions(
            List<String> versions
    ) {

        return versions.stream()
                .sorted()
                .collect(Collectors.joining("|"));
    }

    private static String csv(String value) {

        String safe = value == null ? "" : value;

        boolean requiresQuotes =
                safe.contains(",")
                        || safe.contains("\"")
                        || safe.contains("\n")
                        || safe.contains("\r");

        safe = safe.replace("\"", "\"\"");

        if (requiresQuotes) {
            return "\"" + safe + "\"";
        }

        return safe;
    }
}
