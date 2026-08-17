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

package it.uniroma2.isw2.openjpa.labeling;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ProportionTotalDiagnostic {

    private static final Path RELEASE_CATALOG =
            Path.of(
                    "isw2",
                    "datasets",
                    "release_catalog_raw.csv"
            );

    private static final Path DEFECT_CATALOG =
            Path.of(
                    "isw2",
                    "datasets",
                    "defect_ticket_catalog_raw.csv"
            );

    private ProportionTotalDiagnostic() {
        // Utility class.
    }

    public static void main(String[] args) throws Exception {

        Path repository =
                args.length == 0
                        ? Path.of(".")
                        : Path.of(args[0]);

        repository =
                repository
                        .toAbsolutePath()
                        .normalize();

        List<Release> releases =
                readReleases(
                        repository.resolve(
                                RELEASE_CATALOG
                        )
                );

        List<Defect> defects =
                readDefects(
                        repository.resolve(
                                DEFECT_CATALOG
                        )
                );

        Map<String, Release> releaseByVersion =
                buildReleaseMap(releases);

        DiagnosticResult result =
                calculate(
                        releases,
                        releaseByVersion,
                        defects
                );

        printResult(
                releases,
                defects,
                result
        );
    }

    private static DiagnosticResult calculate(
            List<Release> releases,
            Map<String, Release> releaseByVersion,
            List<Defect> defects
    ) {

        List<ProportionRow> usableRows =
                new ArrayList<>();

        Map<String, Integer> unmappedAffectedVersions =
                new HashMap<>();

        Map<String, Integer> unmappedFixVersions =
                new HashMap<>();

        int noOpeningRelease = 0;
        int noAffectedVersion = 0;
        int noFixVersion = 0;

        int affectedPresentButUnmapped = 0;
        int fixPresentButUnmapped = 0;

        int ivAfterOv = 0;
        int ovAfterFv = 0;
        int ivAtOrAfterFv = 0;

        for (Defect defect : defects) {

            Release openingRelease =
                    findOpeningRelease(
                            releases,
                            defect.createdAt()
                    );

            List<String> affectedNames =
                    splitVersions(
                            defect.affectedVersions()
                    );

            List<String> fixNames =
                    splitVersions(
                            defect.fixVersions()
                    );

            recordUnmappedVersions(
                    affectedNames,
                    releaseByVersion,
                    unmappedAffectedVersions
            );

            recordUnmappedVersions(
                    fixNames,
                    releaseByVersion,
                    unmappedFixVersions
            );

            List<Release> affectedReleases =
                    mapVersions(
                            affectedNames,
                            releaseByVersion
                    );

            List<Release> fixReleases =
                    mapVersions(
                            fixNames,
                            releaseByVersion
                    );

            if (openingRelease == null) {

                noOpeningRelease++;
                continue;
            }

            if (affectedNames.isEmpty()) {

                noAffectedVersion++;
                continue;
            }

            if (affectedReleases.isEmpty()) {

                affectedPresentButUnmapped++;
                continue;
            }

            if (fixNames.isEmpty()) {

                noFixVersion++;
                continue;
            }

            if (fixReleases.isEmpty()) {

                fixPresentButUnmapped++;
                continue;
            }

            Release ivRelease =
                    affectedReleases.getFirst();

            Release fvRelease =
                    fixReleases.getFirst();

            int iv =
                    ivRelease.index();

            int ov =
                    openingRelease.index();

            int fv =
                    fvRelease.index();

            /*
             * A valid lifecycle for Proportion requires:
             *
             * IV <= OV <= FV
             * IV < FV
             */

            if (iv > ov) {

                ivAfterOv++;
                continue;
            }

            if (ov > fv) {

                ovAfterFv++;
                continue;
            }

            if (iv >= fv) {

                ivAtOrAfterFv++;
                continue;
            }

            int denominator =
                    fv - ov;

            /*
             * If OV == FV, the defect was opened during the
             * same release interval in which it was fixed.
             *
             * The conventional Proportion implementation uses
             * distance 1 instead of division by zero.
             */
            if (denominator == 0) {
                denominator = 1;
            }

            double p =
                    (fv - iv)
                            / (double) denominator;

            usableRows.add(
                    new ProportionRow(
                            defect.issueKey(),
                            iv,
                            ivRelease.version(),
                            ov,
                            openingRelease.version(),
                            fv,
                            fvRelease.version(),
                            p
                    )
            );
        }

        List<Double> values =
                usableRows.stream()
                        .map(ProportionRow::p)
                        .sorted()
                        .toList();

        double mean =
                values.stream()
                        .mapToDouble(
                                Double::doubleValue
                        )
                        .average()
                        .orElse(Double.NaN);

        double minimum =
                values.isEmpty()
                        ? Double.NaN
                        : values.getFirst();

        double maximum =
                values.isEmpty()
                        ? Double.NaN
                        : values.getLast();

        double median =
                median(values);

        return new DiagnosticResult(
                List.copyOf(usableRows),
                noOpeningRelease,
                noAffectedVersion,
                noFixVersion,
                affectedPresentButUnmapped,
                fixPresentButUnmapped,
                ivAfterOv,
                ovAfterFv,
                ivAtOrAfterFv,
                mean,
                median,
                minimum,
                maximum,
                Map.copyOf(unmappedAffectedVersions),
                Map.copyOf(unmappedFixVersions)
        );
    }

    private static Release findOpeningRelease(
            List<Release> releases,
            OffsetDateTime createdAt
    ) {

        Release result = null;

        for (Release release : releases) {

            if (!release.releaseDate()
                    .isAfter(
                            createdAt.toLocalDate()
                    )) {

                result = release;

            } else {

                break;
            }
        }

        return result;
    }

    private static void recordUnmappedVersions(
            List<String> versions,
            Map<String, Release> releaseByVersion,
            Map<String, Integer> destination
    ) {

        for (String version : versions) {

            if (!releaseByVersion.containsKey(
                    normalizeVersion(version)
            )) {

                destination.merge(
                        version,
                        1,
                        Integer::sum
                );
            }
        }
    }

    private static List<Release> mapVersions(
            List<String> versions,
            Map<String, Release> releaseByVersion
    ) {

        return versions.stream()
                .map(version ->
                        releaseByVersion.get(
                                normalizeVersion(version)
                        )
                )
                .filter(release ->
                        release != null
                )
                .distinct()
                .sorted(
                        Comparator.comparingInt(
                                Release::index
                        )
                )
                .toList();
    }

    private static Map<String, Release> buildReleaseMap(
            List<Release> releases
    ) {

        Map<String, Release> result =
                new HashMap<>();

        for (Release release : releases) {

            String key =
                    normalizeVersion(
                            release.version()
                    );

            Release previous =
                    result.put(
                            key,
                            release
                    );

            if (previous != null) {

                throw new IllegalStateException(
                        "Duplicate release version: "
                                + release.version()
                );
            }
        }

        return result;
    }

    private static List<Release> readReleases(
            Path input
    ) throws IOException {

        CsvTable table =
                readCsv(input);

        List<Release> releases =
                new ArrayList<>();

        for (List<String> row : table.rows()) {

            String version =
                    value(
                            table,
                            row,
                            "Version"
                    );

            String indexText =
                    firstValue(
                            table,
                            row,
                            "ChronologicalIndex",
                            "ReleaseIndex",
                            "Index"
                    );

            String dateText =
                    firstValue(
                            table,
                            row,
                            "ReleaseDate",
                            "Date"
                    );

            if (version.isBlank()
                    || indexText.isBlank()
                    || dateText.isBlank()) {

                throw new IllegalStateException(
                        "Incomplete release row."
                );
            }

            releases.add(
                    new Release(
                            Integer.parseInt(
                                    indexText
                            ),
                            version.trim(),
                            java.time.LocalDate.parse(
                                    dateText.trim()
                            )
                    )
            );
        }

        releases.sort(
                Comparator.comparingInt(
                        Release::index
                )
        );

        return List.copyOf(releases);
    }

    private static List<Defect> readDefects(
            Path input
    ) throws IOException {

        CsvTable table =
                readCsv(input);

        List<Defect> defects =
                new ArrayList<>();

        for (List<String> row : table.rows()) {

            String issueKey =
                    firstValue(
                            table,
                            row,
                            "IssueKey",
                            "Key"
                    );

            String createdText =
                    firstValue(
                            table,
                            row,
                            "CreatedAt",
                            "CreatedDate",
                            "Created"
                    );

            String affected =
                    firstValue(
                            table,
                            row,
                            "AffectedVersions",
                            "AffectedVersion",
                            "Affected Version/s"
                    );

            String fix =
                    firstValue(
                            table,
                            row,
                            "FixVersions",
                            "FixVersion",
                            "Fix Version/s"
                    );

            if (issueKey.isBlank()) {

                throw new IllegalStateException(
                        "Defect without IssueKey."
                );
            }

            if (createdText.isBlank()) {

                throw new IllegalStateException(
                        "Defect without creation date: "
                                + issueKey
                );
            }

            OffsetDateTime createdAt;

            try {

                createdAt =
                        OffsetDateTime.parse(
                                createdText.trim()
                        );

            } catch (DateTimeParseException exception) {

                throw new IllegalStateException(
                        "Invalid creation date for "
                                + issueKey
                                + ": "
                                + createdText,
                        exception
                );
            }

            defects.add(
                    new Defect(
                            issueKey.trim(),
                            createdAt,
                            affected,
                            fix
                    )
            );
        }

        return List.copyOf(defects);
    }

    private static List<String> splitVersions(
            String value
    ) {

        if (value == null
                || value.isBlank()) {

            return List.of();
        }

        String normalized =
                value.trim();

        /*
         * Catalogs generated by the analyzer use '|'
         * between multiple Jira versions.
         */
        String[] tokens =
                normalized.split("\\s*\\|\\s*");

        List<String> result =
                new ArrayList<>();

        for (String token : tokens) {

            String version =
                    token.trim();

            if (!version.isBlank()) {
                result.add(version);
            }
        }

        return List.copyOf(result);
    }

    private static String normalizeVersion(
            String version
    ) {

        return version
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private static double median(
            List<Double> values
    ) {

        if (values.isEmpty()) {
            return Double.NaN;
        }

        int size =
                values.size();

        if (size % 2 == 1) {

            return values.get(
                    size / 2
            );
        }

        return (
                values.get(
                        size / 2 - 1
                )
                        +
                        values.get(
                                size / 2
                        )
        ) / 2.0;
    }

    private static void printResult(
            List<Release> releases,
            List<Defect> defects,
            DiagnosticResult result
    ) {

        System.out.println();
        System.out.println(
                "===== PROPORTION TOTAL DIAGNOSTIC ====="
        );

        System.out.println(
                "Release universe              : "
                        + releases.size()
        );

        System.out.println(
                "First release                 : "
                        + releases.getFirst().index()
                        + " / "
                        + releases.getFirst().version()
        );

        System.out.println(
                "Last release                  : "
                        + releases.getLast().index()
                        + " / "
                        + releases.getLast().version()
        );

        System.out.println();

        System.out.println(
                "Eligible JIRA defects          : "
                        + defects.size()
        );

        System.out.println(
                "Usable defects for P_TOTAL     : "
                        + result.rows().size()
        );

        System.out.println();

        System.out.println(
                "No opening release             : "
                        + result.noOpeningRelease()
        );

        System.out.println(
                "No AffectedVersions            : "
                        + result.noAffectedVersion()
        );

        System.out.println(
                "AffectedVersions unmapped       : "
                        + result.affectedPresentButUnmapped()
        );

        System.out.println(
                "No FixVersions                 : "
                        + result.noFixVersion()
        );

        System.out.println(
                "FixVersions unmapped            : "
                        + result.fixPresentButUnmapped()
        );

        System.out.println(
                "IV > OV                        : "
                        + result.ivAfterOv()
        );

        System.out.println(
                "OV > FV                        : "
                        + result.ovAfterFv()
        );

        System.out.println(
                "IV >= FV                       : "
                        + result.ivAtOrAfterFv()
        );

        System.out.println();

        System.out.println(
                "P_TOTAL (mean)                 : "
                        + result.mean()
        );

        System.out.println(
                "P median                       : "
                        + result.median()
        );

        System.out.println(
                "P minimum                      : "
                        + result.minimum()
        );

        System.out.println(
                "P maximum                      : "
                        + result.maximum()
        );

        System.out.println();

        System.out.println(
                "First 20 usable defects:"
        );

        result.rows()
                .stream()
                .limit(20)
                .forEach(row ->
                        System.out.printf(
                                "%s | IV=%d/%s | OV=%d/%s | "
                                        + "FV=%d/%s | P=%.6f%n",
                                row.issueKey(),
                                row.iv(),
                                row.ivVersion(),
                                row.ov(),
                                row.ovVersion(),
                                row.fv(),
                                row.fvVersion(),
                                row.p()
                        )
                );

        System.out.println();
        System.out.println(
                "===== UNMAPPED AFFECTED VERSIONS ====="
        );

        result.unmappedAffectedVersions()
                .entrySet()
                .stream()
                .sorted(
                        Map.Entry
                                .<String, Integer>comparingByValue()
                                .reversed()
                                .thenComparing(
                                        Map.Entry.comparingByKey()
                                )
                )
                .forEach(entry ->
                        System.out.printf(
                                "%-30s : %d%n",
                                entry.getKey(),
                                entry.getValue()
                        )
                );

        System.out.println();
        System.out.println(
                "===== UNMAPPED FIX VERSIONS ====="
        );

        result.unmappedFixVersions()
                .entrySet()
                .stream()
                .sorted(
                        Map.Entry
                                .<String, Integer>comparingByValue()
                                .reversed()
                                .thenComparing(
                                        Map.Entry.comparingByKey()
                                )
                )
                .forEach(entry ->
                        System.out.printf(
                                "%-30s : %d%n",
                                entry.getKey(),
                                entry.getValue()
                        )
                );

        System.out.println();
        System.out.println(
                "===== TOP 10 P VALUES ====="
        );

        result.rows()
                .stream()
                .sorted(
                        Comparator
                                .comparingDouble(
                                        ProportionRow::p
                                )
                                .reversed()
                                .thenComparing(
                                        ProportionRow::issueKey
                                )
                )
                .limit(10)
                .forEach(row ->
                        System.out.printf(
                                "%s | IV=%d/%s | OV=%d/%s | "
                                        + "FV=%d/%s | P=%.6f%n",
                                row.issueKey(),
                                row.iv(),
                                row.ivVersion(),
                                row.ov(),
                                row.ovVersion(),
                                row.fv(),
                                row.fvVersion(),
                                row.p()
                        )
                );

        System.out.println(
                "========================================="
        );
    }

    private static CsvTable readCsv(
            Path input
    ) throws IOException {

        if (!Files.isRegularFile(input)) {

            throw new IllegalArgumentException(
                    "CSV not found: "
                            + input
            );
        }

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             input,
                             StandardCharsets.UTF_8
                     )) {

            String headerLine =
                    reader.readLine();

            if (headerLine == null) {

                throw new IllegalStateException(
                        "Empty CSV: "
                                + input
                );
            }

            List<String> headers =
                    parseCsvLine(
                            removeBom(headerLine)
                    );

            Map<String, Integer> columns =
                    new HashMap<>();

            for (int index = 0;
                 index < headers.size();
                 index++) {

                columns.put(
                        headers.get(index).trim(),
                        index
                );
            }

            List<List<String>> rows =
                    new ArrayList<>();

            String line;

            while ((line = reader.readLine())
                    != null) {

                if (!line.isBlank()) {

                    rows.add(
                            parseCsvLine(line)
                    );
                }
            }

            return new CsvTable(
                    Map.copyOf(columns),
                    List.copyOf(rows)
            );
        }
    }

    private static String value(
            CsvTable table,
            List<String> row,
            String column
    ) {

        Integer index =
                table.columns().get(column);

        if (index == null) {

            throw new IllegalArgumentException(
                    "Missing CSV column: "
                            + column
            );
        }

        if (index >= row.size()) {
            return "";
        }

        return row.get(index).trim();
    }

    private static String firstValue(
            CsvTable table,
            List<String> row,
            String... columns
    ) {

        for (String column : columns) {

            Integer index =
                    table.columns().get(column);

            if (index == null
                    || index >= row.size()) {

                continue;
            }

            String result =
                    row.get(index).trim();

            if (!result.isBlank()) {
                return result;
            }
        }

        return "";
    }

    private static List<String> parseCsvLine(
            String line
    ) {

        List<String> result =
                new ArrayList<>();

        StringBuilder current =
                new StringBuilder();

        boolean quoted = false;

        for (int index = 0;
             index < line.length();
             index++) {

            char character =
                    line.charAt(index);

            if (character == '"') {

                if (quoted
                        && index + 1
                        < line.length()
                        && line.charAt(index + 1)
                        == '"') {

                    current.append('"');
                    index++;

                } else {

                    quoted = !quoted;
                }

            } else if (character == ','
                    && !quoted) {

                result.add(
                        current.toString()
                );

                current.setLength(0);

            } else {

                current.append(character);
            }
        }

        if (quoted) {

            throw new IllegalArgumentException(
                    "Malformed CSV line."
            );
        }

        result.add(
                current.toString()
        );

        return result;
    }

    private static String removeBom(
            String value
    ) {

        if (!value.isEmpty()
                && value.charAt(0)
                == '\uFEFF') {

            return value.substring(1);
        }

        return value;
    }

    private record Release(
            int index,
            String version,
            java.time.LocalDate releaseDate
    ) {
    }

    private record Defect(
            String issueKey,
            OffsetDateTime createdAt,
            String affectedVersions,
            String fixVersions
    ) {
    }

    private record ProportionRow(
            String issueKey,
            int iv,
            String ivVersion,
            int ov,
            String ovVersion,
            int fv,
            String fvVersion,
            double p
    ) {
    }

    private record DiagnosticResult(
            List<ProportionRow> rows,
            int noOpeningRelease,
            int noAffectedVersion,
            int noFixVersion,
            int affectedPresentButUnmapped,
            int fixPresentButUnmapped,
            int ivAfterOv,
            int ovAfterFv,
            int ivAtOrAfterFv,
            double mean,
            double median,
            double minimum,
            double maximum,
            Map<String, Integer> unmappedAffectedVersions,
            Map<String, Integer> unmappedFixVersions
    ) {
    }

    private record CsvTable(
            Map<String, Integer> columns,
            List<List<String>> rows
    ) {
    }
}
