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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class FixCommitCatalogGenerator {

    private static final String BASELINE =
            "origin/baseline-4.1.1";

    private static final Path DEFAULT_OUTPUT =
            Path.of(
                    "isw2",
                    "datasets",
                    "fix_commit_catalog.csv"
            );

    private FixCommitCatalogGenerator() {
        // Utility class.
    }

    public static void main(String[] args) throws Exception {

        Path repository =
                Path.of(".")
                        .toAbsolutePath()
                        .normalize();

        Path output =
                args.length > 0
                        ? Path.of(args[0])
                        : DEFAULT_OUTPUT;

        List<DefectTicket> defects =
                new JiraDefectClient()
                        .fetchFixedBugTickets();

        Map<String, DefectTicket> defectByKey =
                buildDefectMap(defects);

        List<FixCommit> allCandidates =
                new GitFixCommitResolver(repository)
                        .resolve(
                                BASELINE,
                                defects
                        );

        List<FixCommit> productionMappings =
                allCandidates.stream()
                        .filter(
                                FixCommitCatalogGenerator
                                        ::touchesProduction
                        )
                        .sorted(
                                Comparator
                                        .comparing(
                                                FixCommit::issueKey
                                        )
                                        .thenComparing(
                                                FixCommit::commitDate
                                        )
                                        .thenComparing(
                                                FixCommit::commitId
                                        )
                        )
                        .toList();

        ValidationResult validation =
                validate(
                        productionMappings,
                        defectByKey
                );

        writeCsv(
                output,
                productionMappings,
                defectByKey
        );

        printSummary(
                defects,
                allCandidates,
                productionMappings,
                validation,
                output
        );
    }

    private static Map<String, DefectTicket> buildDefectMap(
            List<DefectTicket> defects
    ) {

        Map<String, DefectTicket> result =
                new HashMap<>();

        for (DefectTicket defect : defects) {

            DefectTicket previous =
                    result.put(
                            defect.issueKey(),
                            defect
                    );

            if (previous != null) {
                throw new IllegalStateException(
                        "Duplicate defect ticket: "
                                + defect.issueKey()
                );
            }
        }

        return result;
    }

    private static ValidationResult validate(
            List<FixCommit> mappings,
            Map<String, DefectTicket> defectByKey
    ) {

        Set<String> mappingKeys =
                new HashSet<>();

        Set<String> tickets =
                new HashSet<>();

        Set<String> commits =
                new HashSet<>();

        int duplicateMappings = 0;
        int missingTickets = 0;
        int noProductionFiles = 0;
        int mergeMappings = 0;
        int revertMappings = 0;
        int issueNotInSubject = 0;

        int beforeCreation = 0;
        int insideLifecycle = 0;
        int afterResolution = 0;

        for (FixCommit mapping : mappings) {

            String mappingKey =
                    mapping.issueKey()
                            + "\u0000"
                            + mapping.commitId();

            if (!mappingKeys.add(mappingKey)) {
                duplicateMappings++;
            }

            DefectTicket ticket =
                    defectByKey.get(mapping.issueKey());

            if (ticket == null) {
                missingTickets++;
                continue;
            }

            tickets.add(mapping.issueKey());
            commits.add(mapping.commitId());

            if (!touchesProduction(mapping)) {
                noProductionFiles++;
            }

            if (mapping.merge()) {
                mergeMappings++;
            }

            if (mapping.revert()) {
                revertMappings++;
            }

            if (!issueAppearsInSubject(mapping)) {
                issueNotInSubject++;
            }

            TemporalRelation temporalRelation =
                    temporalRelation(
                            mapping,
                            ticket
                    );

            switch (temporalRelation) {
                case BEFORE_CREATION ->
                        beforeCreation++;

                case INSIDE_LIFECYCLE ->
                        insideLifecycle++;

                case AFTER_RESOLUTION ->
                        afterResolution++;
            }
        }

        if (duplicateMappings != 0) {
            throw new IllegalStateException(
                    "Duplicate IssueKey/CommitId mappings: "
                            + duplicateMappings
            );
        }

        if (missingTickets != 0) {
            throw new IllegalStateException(
                    "Mappings without JIRA ticket: "
                            + missingTickets
            );
        }

        if (noProductionFiles != 0) {
            throw new IllegalStateException(
                    "Mappings without production Java: "
                            + noProductionFiles
            );
        }

        if (mergeMappings != 0) {
            throw new IllegalStateException(
                    "Unexpected production merge mappings: "
                            + mergeMappings
            );
        }

        if (revertMappings != 0) {
            throw new IllegalStateException(
                    "Unexpected production revert mappings: "
                            + revertMappings
            );
        }

        if (issueNotInSubject != 0) {
            throw new IllegalStateException(
                    "Production mappings without issue key "
                            + "in subject: "
                            + issueNotInSubject
            );
        }

        return new ValidationResult(
                mappings.size(),
                tickets.size(),
                commits.size(),
                duplicateMappings,
                missingTickets,
                noProductionFiles,
                mergeMappings,
                revertMappings,
                issueNotInSubject,
                beforeCreation,
                insideLifecycle,
                afterResolution
        );
    }

    private static void writeCsv(
            Path output,
            List<FixCommit> mappings,
            Map<String, DefectTicket> defectByKey
    ) throws IOException {

        Path parent =
                output.toAbsolutePath()
                        .normalize()
                        .getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             output,
                             StandardCharsets.UTF_8
                     )) {

            writer.write(
                    "IssueId,"
                            + "IssueKey,"
                            + "CommitId,"
                            + "CommitDate,"
                            + "Subject,"
                            + "CreatedDate,"
                            + "ResolutionDate,"
                            + "TemporalRelation,"
                            + "HoursAfterResolution,"
                            + "AffectedVersions,"
                            + "FixVersions,"
                            + "ChangedFiles,"
                            + "ChangedJavaFiles,"
                            + "ChangedProductionJavaFiles,"
                            + "ProductionFiles"
            );

            writer.newLine();

            for (FixCommit mapping : mappings) {

                DefectTicket ticket =
                        defectByKey.get(
                                mapping.issueKey()
                        );

                List<String> productionFiles =
                        productionFiles(mapping);

                long javaFiles =
                        mapping.changedFiles()
                                .stream()
                                .filter(file ->
                                        file.endsWith(".java")
                                )
                                .count();

                TemporalRelation relation =
                        temporalRelation(
                                mapping,
                                ticket
                        );

                String hoursAfterResolution = "";

                if (relation
                        == TemporalRelation.AFTER_RESOLUTION) {

                    hoursAfterResolution =
                            Long.toString(
                                    Duration.between(
                                            ticket.resolutionDate(),
                                            mapping.commitDate()
                                    ).toHours()
                            );
                }

                writer.write(
                        csv(ticket.issueId())
                                + ","
                                + csv(mapping.issueKey())
                                + ","
                                + csv(mapping.commitId())
                                + ","
                                + csv(
                                mapping.commitDate()
                                        .toString()
                        )
                                + ","
                                + csv(mapping.subject())
                                + ","
                                + csv(
                                ticket.createdDate()
                                        .toString()
                        )
                                + ","
                                + csv(
                                ticket.resolutionDate()
                                        .toString()
                        )
                                + ","
                                + relation
                                + ","
                                + hoursAfterResolution
                                + ","
                                + csv(
                                join(
                                        ticket.affectedVersions()
                                )
                        )
                                + ","
                                + csv(
                                join(
                                        ticket.fixVersions()
                                )
                        )
                                + ","
                                + mapping.changedFiles().size()
                                + ","
                                + javaFiles
                                + ","
                                + productionFiles.size()
                                + ","
                                + csv(
                                join(productionFiles)
                        )
                );

                writer.newLine();
            }
        }
    }

    private static List<String> productionFiles(
            FixCommit mapping
    ) {

        return mapping.changedFiles()
                .stream()
                .filter(file ->
                        file.endsWith(".java")
                )
                .filter(file ->
                        JavaClassScopeClassifier
                                .classify(file)
                                == ClassScope.PRODUCTION
                )
                .distinct()
                .sorted()
                .toList();
    }

    private static boolean touchesProduction(
            FixCommit mapping
    ) {

        return !productionFiles(mapping)
                .isEmpty();
    }

    private static boolean issueAppearsInSubject(
            FixCommit mapping
    ) {

        String regex =
                "\\b"
                        + Pattern.quote(
                        mapping.issueKey()
                )
                        + "\\b";

        return Pattern.compile(
                        regex,
                        Pattern.CASE_INSENSITIVE
                )
                .matcher(mapping.subject())
                .find();
    }

    private static TemporalRelation temporalRelation(
            FixCommit mapping,
            DefectTicket ticket
    ) {

        if (mapping.commitDate()
                .isBefore(ticket.createdDate())) {

            return TemporalRelation.BEFORE_CREATION;
        }

        if (mapping.commitDate()
                .isAfter(ticket.resolutionDate())) {

            return TemporalRelation.AFTER_RESOLUTION;
        }

        return TemporalRelation.INSIDE_LIFECYCLE;
    }

    private static String join(
            List<String> values
    ) {

        return values.stream()
                .distinct()
                .sorted()
                .collect(
                        Collectors.joining("|")
                );
    }

    private static String csv(
            String value
    ) {

        String safe =
                value == null
                        ? ""
                        : value;

        boolean quote =
                safe.contains(",")
                        || safe.contains("\"")
                        || safe.contains("\n")
                        || safe.contains("\r");

        safe =
                safe.replace(
                        "\"",
                        "\"\""
                );

        if (quote) {
            return "\""
                    + safe
                    + "\"";
        }

        return safe;
    }

    private static void printSummary(
            List<DefectTicket> defects,
            List<FixCommit> allCandidates,
            List<FixCommit> productionMappings,
            ValidationResult validation,
            Path output
    ) {

        System.out.println();
        System.out.println(
                "===== FIX COMMIT CATALOG ====="
        );

        System.out.println(
                "Validated JIRA defects        : "
                        + defects.size()
        );

        System.out.println(
                "Raw candidate mappings        : "
                        + allCandidates.size()
        );

        System.out.println(
                "Production mappings           : "
                        + productionMappings.size()
        );

        System.out.println(
                "Tickets represented           : "
                        + validation.ticketCount()
        );

        System.out.println(
                "Unique fix commits            : "
                        + validation.commitCount()
        );

        System.out.println(
                "Duplicate mappings            : "
                        + validation.duplicateMappings()
        );

        System.out.println(
                "Mappings without ticket       : "
                        + validation.missingTickets()
        );

        System.out.println(
                "Mappings without production   : "
                        + validation.noProductionFiles()
        );

        System.out.println(
                "Merge mappings                : "
                        + validation.mergeMappings()
        );

        System.out.println(
                "Revert mappings               : "
                        + validation.revertMappings()
        );

        System.out.println(
                "Issue absent from subject     : "
                        + validation.issueNotInSubject()
        );

        System.out.println();
        System.out.println(
                "Temporal diagnostics:"
        );

        System.out.println(
                "Before creation               : "
                        + validation.beforeCreation()
        );

        System.out.println(
                "Inside lifecycle              : "
                        + validation.insideLifecycle()
        );

        System.out.println(
                "After resolution              : "
                        + validation.afterResolution()
        );

        System.out.println();
        System.out.println(
                "Output                        : "
                        + output.toAbsolutePath()
        );

        System.out.println(
                "=============================="
        );
    }

    private enum TemporalRelation {

        BEFORE_CREATION,
        INSIDE_LIFECYCLE,
        AFTER_RESOLUTION
    }

    private record ValidationResult(
            int mappingCount,
            int ticketCount,
            int commitCount,
            int duplicateMappings,
            int missingTickets,
            int noProductionFiles,
            int mergeMappings,
            int revertMappings,
            int issueNotInSubject,
            int beforeCreation,
            int insideLifecycle,
            int afterResolution
    ) {
    }
}
