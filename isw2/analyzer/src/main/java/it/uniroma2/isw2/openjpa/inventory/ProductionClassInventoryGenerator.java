package it.uniroma2.isw2.openjpa.inventory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProductionClassInventoryGenerator {

    private static final String RAW_INVENTORY =
            "isw2/datasets/java_class_inventory_raw.csv";

    private static final String OUTPUT =
            "isw2/datasets/java_class_inventory.csv";

    private static final String PRODUCTION_SCOPE =
            "PRODUCTION";

    public static void main(String[] args) {

        try {

            Path repositoryRoot =
                    resolveRepositoryRoot(args);

            Path input =
                    repositoryRoot.resolve(RAW_INVENTORY);

            Path output =
                    repositoryRoot.resolve(OUTPUT);

            List<ProductionClassRow> rows =
                    readProductionClasses(input);

            validate(rows);

            writeInventory(
                    rows,
                    output
            );

            printSummary(
                    rows,
                    output
            );

        } catch (Exception exception) {

            System.err.println(
                    "Unable to generate production class inventory: "
                            + exception.getMessage()
            );

            exception.printStackTrace();

            System.exit(1);
        }
    }

    private static List<ProductionClassRow> readProductionClasses(
            Path input
    ) throws IOException {

        if (!Files.isRegularFile(input)) {
            throw new IllegalArgumentException(
                    "Raw inventory not found: " + input
            );
        }

        List<ProductionClassRow> rows =
                new ArrayList<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             input,
                             StandardCharsets.UTF_8
                     )) {

            String headerLine =
                    reader.readLine();

            if (headerLine == null) {
                throw new IOException(
                        "Empty inventory: " + input
                );
            }

            List<String> headers =
                    parseCsvLine(headerLine);

            Map<String, Integer> columns =
                    buildColumnMap(headers);

            requireColumn(columns, "ReleaseIndex");
            requireColumn(columns, "Version");
            requireColumn(columns, "CommitId");
            requireColumn(columns, "FilePath");
            requireColumn(columns, "Scope");

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                List<String> values =
                        parseCsvLine(line);

                String scope =
                        get(
                                values,
                                columns,
                                "Scope"
                        );

                if (!PRODUCTION_SCOPE.equals(scope)) {
                    continue;
                }

                rows.add(
                        new ProductionClassRow(
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
                                        "FilePath"
                                )
                        )
                );
            }
        }

        return rows;
    }

    private static void validate(
            List<ProductionClassRow> rows
    ) {

        if (rows.isEmpty()) {
            throw new IllegalStateException(
                    "No production classes found."
            );
        }

        Set<String> keys =
                new HashSet<>();

        Set<Integer> releases =
                new HashSet<>();

        for (ProductionClassRow row : rows) {

            releases.add(
                    row.releaseIndex()
            );

            if (!row.filePath().endsWith(".java")) {
                throw new IllegalStateException(
                        "Non-Java file in production inventory: "
                                + row.filePath()
                );
            }

            String normalized =
                    "/"
                            + row.filePath()
                            .replace('\\', '/')
                            .toLowerCase()
                            + "/";

            if (!normalized.contains("/src/main/java/")
                    && !normalized.contains("/src/java/")) {

                throw new IllegalStateException(
                        "Unexpected production path: "
                                + row.filePath()
                );
            }

            if (normalized.contains("/src/test/")
                    || normalized.contains("/src/test-java/")
                    || normalized.contains("/src/it/")
                    || normalized.contains("/src/itests/")
                    || normalized.contains("/itests/")
                    || normalized.contains("-itests/")
                    || normalized.contains("/tests/")
                    || normalized.contains("-tests/")
                    || normalized.contains("/openjpa-examples/")
                    || normalized.contains("/generated/")
                    || normalized.contains("/src/main/jjtree/")
                    || normalized.contains("/src/main/javacc/")) {

                throw new IllegalStateException(
                        "Excluded source found in production inventory: "
                                + row.filePath()
                );
            }

            String key =
                    row.releaseIndex()
                            + "\u0000"
                            + row.filePath();

            if (!keys.add(key)) {
                throw new IllegalStateException(
                        "Duplicate production class: "
                                + row.releaseIndex()
                                + " / "
                                + row.filePath()
                );
            }
        }

        if (releases.size() != 12) {
            throw new IllegalStateException(
                    "Expected 12 releases, found "
                            + releases.size()
            );
        }
    }

    private static void writeInventory(
            List<ProductionClassRow> rows,
            Path output
    ) throws IOException {

        Files.createDirectories(
                output.getParent()
        );

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             output,
                             StandardCharsets.UTF_8
                     )) {

            writer.write(
                    "ReleaseIndex,"
                            + "Version,"
                            + "CommitId,"
                            + "Class"
            );

            writer.newLine();

            for (ProductionClassRow row : rows) {

                writer.write(
                        row.releaseIndex()
                                + ","
                                + csv(row.version())
                                + ","
                                + csv(row.commitId())
                                + ","
                                + csv(row.filePath())
                );

                writer.newLine();
            }
        }
    }

    private static void printSummary(
            List<ProductionClassRow> rows,
            Path output
    ) {

        System.out.println();
        System.out.println(
                "===== OPENJPA PRODUCTION CLASS INVENTORY ====="
        );

        System.out.println(
                "Production observations : "
                        + rows.size()
        );

        System.out.println(
                "Dataset releases         : 12"
        );

        System.out.println();

        System.out.println(
                "Classes per release:"
        );

        for (int releaseIndex = 1;
             releaseIndex <= 12;
             releaseIndex++) {

            int currentRelease =
                    releaseIndex;

            long count =
                    rows.stream()
                            .filter(row ->
                                    row.releaseIndex()
                                            == currentRelease
                            )
                            .count();

            String version =
                    rows.stream()
                            .filter(row ->
                                    row.releaseIndex()
                                            == currentRelease
                            )
                            .map(
                                    ProductionClassRow::version
                            )
                            .findFirst()
                            .orElse("?");

            System.out.printf(
                    "%2d %-10s : %d%n",
                    releaseIndex,
                    version,
                    count
            );
        }

        System.out.println();

        System.out.println(
                "Duplicate keys           : 0"
        );

        System.out.println(
                "Excluded scopes present  : 0"
        );

        System.out.println(
                "Output                   : "
                        + output
        );

        System.out.println(
                "=============================================="
        );
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
                    "Missing value for column: "
                            + name
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

    private static Path resolveRepositoryRoot(
            String[] args
    ) {

        if (args.length == 0) {
            throw new IllegalArgumentException(
                    "Repository root path is required."
            );
        }

        Path root =
                Paths.get(args[0])
                        .toAbsolutePath()
                        .normalize();

        if (!Files.isDirectory(
                root.resolve(".git")
        )) {
            throw new IllegalArgumentException(
                    "Not a Git repository root: "
                            + root
            );
        }

        return root;
    }

    private static String csv(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return "\""
                + value.replace(
                "\"",
                "\"\""
        )
                + "\"";
    }

    private record ProductionClassRow(
            int releaseIndex,
            String version,
            String commitId,
            String filePath
    ) {
    }
}
