package it.uniroma2.isw2.openjpa.inventory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductionClassInventoryReader {

    public List<ProductionClassObservation> read(
            Path inventory
    ) throws IOException {

        if (!Files.isRegularFile(inventory)) {
            throw new IllegalArgumentException(
                    "Inventory not found: " + inventory
            );
        }

        List<ProductionClassObservation> observations =
                new ArrayList<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             inventory,
                             StandardCharsets.UTF_8
                     )) {

            String headerLine =
                    reader.readLine();

            if (headerLine == null) {
                throw new IOException(
                        "Empty inventory: " + inventory
                );
            }

            List<String> headers =
                    parseCsvLine(headerLine);

            Map<String, Integer> columns =
                    buildColumnMap(headers);

            requireColumn(columns, "ReleaseIndex");
            requireColumn(columns, "Version");
            requireColumn(columns, "CommitId");
            requireColumn(columns, "Class");

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                List<String> values =
                        parseCsvLine(line);

                observations.add(
                        new ProductionClassObservation(
                                Integer.parseInt(
                                        get(
                                                values,
                                                columns,
                                                "ReleaseIndex"
                                        )
                                ),
                                get(
                                        values,
                                        columns,
                                        "Version"
                                ),
                                get(
                                        values,
                                        columns,
                                        "CommitId"
                                ),
                                get(
                                        values,
                                        columns,
                                        "Class"
                                )
                        )
                );
            }
        }

        return observations;
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

        int index =
                columns.get(name);

        if (index >= values.size()) {
            throw new IllegalArgumentException(
                    "Missing value for column: " + name
            );
        }

        return values.get(index);
    }

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
