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

package it.uniroma2.isw2.openjpa.release;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

public class DatasetReleaseCatalogReader {

    public List<DatasetRelease> readIncludedReleases(Path catalog)
            throws IOException {

        if (!Files.isRegularFile(catalog)) {
            throw new IllegalArgumentException(
                    "Release catalog not found: " + catalog
            );
        }

        List<DatasetRelease> releases = new ArrayList<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             catalog,
                             StandardCharsets.UTF_8
                     )) {

            String headerLine = reader.readLine();

            if (headerLine == null) {
                throw new IOException(
                        "Empty release catalog: " + catalog
                );
            }

            List<String> headers =
                    parseCsvLine(headerLine);

            Map<String, Integer> columns =
                    buildColumnMap(headers);

            requireColumn(columns, "ChronologicalIndex");
            requireColumn(columns, "Version");
            requireColumn(columns, "ReleaseCommit");
            requireColumn(columns, "DatasetIncluded");

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                List<String> values =
                        parseCsvLine(line);

                boolean included =
                        Boolean.parseBoolean(
                                get(
                                        values,
                                        columns,
                                        "DatasetIncluded"
                                )
                        );

                if (!included) {
                    continue;
                }

                int releaseIndex =
                        Integer.parseInt(
                                get(
                                        values,
                                        columns,
                                        "ChronologicalIndex"
                                )
                        );

                String version =
                        get(
                                values,
                                columns,
                                "Version"
                        );

                String commitId =
                        get(
                                values,
                                columns,
                                "ReleaseCommit"
                        );

                LocalDate releaseDate =
                        LocalDate.parse(
                                get(
                                        values,
                                        columns,
                                        "ReleaseDate"
                                )
                        );

                releases.add(
                        new DatasetRelease(
                                releaseIndex,
                                version,
                                commitId,
                                releaseDate
                        )
                );
            }
        }

        releases.sort(
                (left, right) ->
                        Integer.compare(
                                left.releaseIndex(),
                                right.releaseIndex()
                        )
        );

        return releases;
    }

    private static Map<String, Integer> buildColumnMap(
            List<String> headers
    ) {

        Map<String, Integer> columns =
                new HashMap<>();

        for (int index = 0;
             index < headers.size();
             index++) {

            columns.put(
                    headers.get(index),
                    index
            );
        }

        return columns;
    }

    private static void requireColumn(
            Map<String, Integer> columns,
            String name
    ) {

        if (!columns.containsKey(name)) {
            throw new IllegalArgumentException(
                    "Missing CSV column: " + name
            );
        }
    }

    private static String get(
            List<String> values,
            Map<String, Integer> columns,
            String name
    ) {

        int index = columns.get(name);

        if (index >= values.size()) {
            throw new IllegalArgumentException(
                    "Missing value for column: " + name
            );
        }

        return values.get(index);
    }

    /*
     * Minimal CSV parser supporting quoted fields
     * and escaped double quotes ("").
     *
     * It is sufficient for catalogs generated by
     * the ISW2 analyzer itself.
     */
    private static List<String> parseCsvLine(
            String line
    ) {

        List<String> values =
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
                        && index + 1 < line.length()
                        && line.charAt(index + 1) == '"') {

                    current.append('"');
                    index++;

                } else {

                    quoted = !quoted;
                }

            } else if (character == ','
                    && !quoted) {

                values.add(
                        current.toString()
                );

                current.setLength(0);

            } else {

                current.append(character);
            }
        }

        if (quoted) {
            throw new IllegalArgumentException(
                    "Malformed CSV line: " + line
            );
        }

        values.add(
                current.toString()
        );

        return values;
    }
}
