package it.uniroma2.isw2.openjpa.inventory;

import it.uniroma2.isw2.openjpa.release.DatasetRelease;
import it.uniroma2.isw2.openjpa.release.DatasetReleaseCatalogReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JavaClassInventoryGenerator {

    private static final String RELEASE_CATALOG =
            "isw2/datasets/release_catalog.csv";

    private static final String OUTPUT =
            "isw2/datasets/java_class_inventory_raw.csv";

    public static void main(String[] args) {

        try {

            Path repositoryRoot =
                    resolveRepositoryRoot(args);

            Path catalog =
                    repositoryRoot.resolve(
                            RELEASE_CATALOG
                    );

            DatasetReleaseCatalogReader reader =
                    new DatasetReleaseCatalogReader();

            List<DatasetRelease> releases =
                    reader.readIncludedReleases(catalog);

            if (releases.isEmpty()) {
                throw new IllegalStateException(
                        "No dataset releases found."
                );
            }

            List<InventoryRow> rows =
                    buildInventory(
                            repositoryRoot,
                            releases
                    );

            validate(
                    releases,
                    rows
            );

            Path output =
                    repositoryRoot.resolve(OUTPUT);

            writeCsv(
                    rows,
                    output
            );

            printSummary(
                    releases,
                    rows,
                    output
            );

        } catch (Exception exception) {

            System.err.println(
                    "Unable to generate Java class inventory: "
                            + exception.getMessage()
            );

            exception.printStackTrace();

            System.exit(1);
        }
    }

    private static List<InventoryRow> buildInventory(
            Path repositoryRoot,
            List<DatasetRelease> releases
    ) throws IOException, InterruptedException {

        List<InventoryRow> rows =
                new ArrayList<>();

        for (DatasetRelease release : releases) {

            List<String> javaFiles =
                    listJavaFiles(
                            repositoryRoot,
                            release.commitId()
                    );

            if (javaFiles.isEmpty()) {
                throw new IllegalStateException(
                        "No Java files found for release "
                                + release.version()
                                + " at "
                                + release.commitId()
                );
            }

            for (String filePath : javaFiles) {

                rows.add(
                        new InventoryRow(
                                release.releaseIndex(),
                                release.version(),
                                release.commitId(),
                                filePath,
                                classify(filePath)
                        )
                );
            }
        }

        return rows;
    }

    private static List<String> listJavaFiles(
            Path repositoryRoot,
            String commitId
    ) throws IOException, InterruptedException {

        String output = runGit(
                repositoryRoot,
                "ls-tree",
                "-r",
                "--name-only",
                commitId
        );

        if (output.isBlank()) {
            return List.of();
        }

        return output.lines()
                .map(String::trim)
                .filter(path ->
                        path.endsWith(".java")
                )
                .sorted()
                .toList();
    }

    private static ClassScope classify(
            String filePath
    ) {

        String path =
                filePath
                        .replace('\\', '/')
                        .toLowerCase(Locale.ROOT);

        String fileName =
                Path.of(path)
                        .getFileName()
                        .toString();

        if (fileName.equals("package-info.java")
                || fileName.equals("module-info.java")) {

            return ClassScope.NON_CLASS;
        }

        String normalized =
                "/" + path + "/";

        if (normalized.contains("/src/test/")
                || normalized.contains("/src/test-java/")
                || normalized.contains("/src/it/")
                || normalized.contains("/src/itests/")
                || normalized.contains("/itests/")
                || normalized.contains("-itests/")
                || normalized.contains("/tests/")
                || normalized.contains("-tests/")) {

            return ClassScope.TEST;
        }

        if (normalized.contains("/examples/")
                || normalized.contains("/example/")
                || normalized.contains("/openjpa-examples/")) {

            return ClassScope.EXAMPLE;
        }

        if (normalized.contains("/generated-sources/")
                || normalized.contains("/src/generated/")
                || normalized.contains("/generated/")) {

            return ClassScope.GENERATED;
        }

        if (normalized.contains("/src/main/jjtree/")
                || normalized.contains("/src/main/javacc/")) {

            return ClassScope.PARSER_SOURCE;
        }

        if (normalized.contains("/src/main/java/")
                || normalized.contains("/src/java/")) {

            return ClassScope.PRODUCTION;
        }

        return ClassScope.OTHER;
    }

    private static void validate(
            List<DatasetRelease> releases,
            List<InventoryRow> rows
    ) {

        Set<String> keys =
                new HashSet<>();

        for (InventoryRow row : rows) {

            String key =
                    row.releaseIndex()
                            + "\u0000"
                            + row.filePath();

            if (!keys.add(key)) {
                throw new IllegalStateException(
                        "Duplicate inventory key: "
                                + row.releaseIndex()
                                + " / "
                                + row.filePath()
                );
            }
        }

        for (DatasetRelease release : releases) {

            long count =
                    rows.stream()
                            .filter(row ->
                                    row.releaseIndex()
                                            == release.releaseIndex()
                            )
                            .count();

            if (count == 0) {
                throw new IllegalStateException(
                        "Release without Java files: "
                                + release.version()
                );
            }
        }
    }

    private static void writeCsv(
            List<InventoryRow> rows,
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
                            + "FilePath,"
                            + "Scope"
            );

            writer.newLine();

            for (InventoryRow row : rows) {

                writer.write(
                        row.releaseIndex()
                                + ","
                                + csv(row.version())
                                + ","
                                + csv(row.commitId())
                                + ","
                                + csv(row.filePath())
                                + ","
                                + row.scope()
                );

                writer.newLine();
            }
        }
    }

    private static void printSummary(
            List<DatasetRelease> releases,
            List<InventoryRow> rows,
            Path output
    ) {

        Map<ClassScope, Long> counts =
                new EnumMap<>(ClassScope.class);

        for (ClassScope scope : ClassScope.values()) {

            long count =
                    rows.stream()
                            .filter(row ->
                                    row.scope() == scope
                            )
                            .count();

            counts.put(
                    scope,
                    count
            );
        }

        System.out.println();
        System.out.println(
                "===== OPENJPA JAVA CLASS INVENTORY ====="
        );

        System.out.println(
                "Dataset releases : "
                        + releases.size()
        );

        System.out.println(
                "Java files       : "
                        + rows.size()
        );

        for (ClassScope scope : ClassScope.values()) {

            System.out.printf(
                    "%-16s : %d%n",
                    scope,
                    counts.get(scope)
            );
        }

        System.out.println();

        System.out.println(
                "Files per release:"
        );

        for (DatasetRelease release : releases) {

            long count =
                    rows.stream()
                            .filter(row ->
                                    row.releaseIndex()
                                            == release.releaseIndex()
                            )
                            .count();

            System.out.printf(
                    "%2d %-10s : %d%n",
                    release.releaseIndex(),
                    release.version(),
                    count
            );
        }

        System.out.println();

        System.out.println(
                "Duplicate keys   : 0"
        );

        System.out.println(
                "Output           : "
                        + output
        );

        System.out.println(
                "======================================="
        );
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

    private static String runGit(
            Path repositoryRoot,
            String... arguments
    ) throws IOException, InterruptedException {

        String[] command =
                new String[arguments.length + 3];

        command[0] = "git";
        command[1] = "-C";
        command[2] =
                repositoryRoot.toString();

        System.arraycopy(
                arguments,
                0,
                command,
                3,
                arguments.length
        );

        Process process =
                new ProcessBuilder(command)
                        .redirectErrorStream(true)
                        .start();

        String output;

        try (BufferedReader reader =
                     process.inputReader(
                             StandardCharsets.UTF_8
                     )) {

            output =
                    reader.lines()
                            .collect(
                                    Collectors.joining(
                                            System.lineSeparator()
                                    )
                            )
                            .trim();
        }

        int exitCode =
                process.waitFor();

        if (exitCode != 0) {
            throw new IOException(
                    "Git command failed (exit "
                            + exitCode
                            + "): "
                            + String.join(
                            " ",
                            command
                    )
                            + System.lineSeparator()
                            + output
            );
        }

        return output;
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

    private record InventoryRow(
            int releaseIndex,
            String version,
            String commitId,
            String filePath,
            ClassScope scope
    ) {
    }
}
