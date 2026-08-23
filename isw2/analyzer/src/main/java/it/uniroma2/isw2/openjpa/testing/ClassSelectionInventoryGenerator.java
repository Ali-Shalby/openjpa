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

package it.uniroma2.isw2.openjpa.testing;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.Trees;
import it.uniroma2.isw2.openjpa.inventory.ClassScope;
import it.uniroma2.isw2.openjpa.inventory.JavaClassScopeClassifier;

import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class ClassSelectionInventoryGenerator {

    private static final String DEFAULT_REF = "4.1.1";

    private static final Path OUTPUT =
            Path.of(
                    "isw2",
                    "datasets",
                    "testing",
                    "class_inventory_4.1.1.csv"
            );

    private static final Path SUMMARY =
            Path.of(
                    "isw2",
                    "datasets",
                    "testing",
                    "class_inventory_4.1.1_summary.txt"
            );

    private ClassSelectionInventoryGenerator() {
        // Utility class.
    }

    public static void main(String[] args) {

        try {

            Path repositoryRoot =
                    resolveRepositoryRoot(args);

            String ref =
                    args.length >= 2
                            ? args[1]
                            : DEFAULT_REF;

            String commitId =
                    resolveCommit(
                            repositoryRoot,
                            ref
                    );

            List<String> trackedJavaPaths =
                    listTrackedJavaFiles(
                            repositoryRoot,
                            commitId
                    );

            List<String> productionPaths =
                    trackedJavaPaths.stream()
                            .filter(path ->
                                    JavaClassScopeClassifier.classify(path)
                                            == ClassScope.PRODUCTION
                            )
                            .sorted()
                            .toList();

            if (productionPaths.isEmpty()) {
                throw new IllegalStateException(
                        "No production Java files found at "
                                + ref
                );
            }

            JavaCompiler compiler =
                    ToolProvider.getSystemJavaCompiler();

            if (compiler == null) {
                throw new IllegalStateException(
                        "A full JDK is required. "
                                + "System Java compiler is unavailable."
                );
            }

            List<InventoryRow> rows =
                    new ArrayList<>();

            List<SkippedRow> skipped =
                    new ArrayList<>();

            int processed = 0;

            for (String sourcePath : productionPaths) {

                String source =
                        readSourceAtCommit(
                                repositoryRoot,
                                commitId,
                                sourcePath
                        );

                ParseResult parsed =
                        parsePrimaryType(
                                compiler,
                                sourcePath,
                                source
                        );

                if (parsed.row() != null) {

                    rows.add(
                            parsed.row()
                    );

                } else {

                    skipped.add(
                            new SkippedRow(
                                    sourcePath,
                                    parsed.reason()
                            )
                    );
                }

                processed++;

                if (processed % 100 == 0) {

                    System.out.printf(
                            Locale.ROOT,
                            "Parsed %d / %d production source files%n",
                            processed,
                            productionPaths.size()
                    );
                }
            }

            rows.sort(
                    Comparator.comparing(
                                    InventoryRow::fqcn
                            )
                            .thenComparing(
                                    InventoryRow::filePath
                            )
            );

            Validation validation =
                    validate(
                            productionPaths,
                            rows
                    );

            Path output =
                    repositoryRoot.resolve(
                            OUTPUT
                    );

            Path summary =
                    repositoryRoot.resolve(
                            SUMMARY
                    );

            writeCsv(
                    rows,
                    output,
                    ref,
                    commitId
            );

            writeSummary(
                    trackedJavaPaths,
                    productionPaths,
                    rows,
                    skipped,
                    validation,
                    summary,
                    ref,
                    commitId
            );

            printSummary(
                    trackedJavaPaths,
                    productionPaths,
                    rows,
                    skipped,
                    validation,
                    output,
                    summary,
                    ref,
                    commitId
            );

        } catch (Exception exception) {

            System.err.println(
                    "Unable to generate class-selection inventory: "
                            + exception.getMessage()
            );

            exception.printStackTrace();

            System.exit(1);
        }
    }

    private static ParseResult parsePrimaryType(
            JavaCompiler compiler,
            String sourcePath,
            String source
    ) throws IOException {

        String fileName =
                sourcePath.substring(
                        sourcePath.lastIndexOf('/') + 1
                );

        String simpleName =
                fileName.substring(
                        0,
                        fileName.length() - ".java".length()
                );

        DiagnosticCollector<JavaFileObject> diagnostics =
                new DiagnosticCollector<>();

        JavaFileObject sourceObject =
                new StringJavaFileObject(
                        simpleName,
                        source
                );

        JavacTask task =
                (JavacTask) compiler.getTask(
                        null,
                        null,
                        diagnostics,
                        List.of(
                                "-proc:none"
                        ),
                        null,
                        List.of(
                                sourceObject
                        )
                );

        Iterable<? extends CompilationUnitTree> parsedUnits =
                task.parse();

        CompilationUnitTree unit =
                parsedUnits.iterator().hasNext()
                        ? parsedUnits.iterator().next()
                        : null;

        if (unit == null) {

            return new ParseResult(
                    null,
                    "No compilation unit"
            );
        }

        ClassTree primary =
                null;

        for (Tree declaration : unit.getTypeDecls()) {

            if (declaration instanceof ClassTree classTree
                    && simpleName.contentEquals(
                    classTree.getSimpleName()
            )) {

                primary =
                        classTree;

                break;
            }
        }

        if (primary == null) {

            String diagnosticText =
                    diagnostics.getDiagnostics()
                            .stream()
                            .filter(diagnostic ->
                                    diagnostic.getKind()
                                            == Diagnostic.Kind.ERROR
                            )
                            .limit(3)
                            .map(diagnostic ->
                                    diagnostic.getMessage(
                                            Locale.ROOT
                                    )
                            )
                            .collect(
                                    Collectors.joining(
                                            " | "
                                    )
                            );

            String reason =
                    diagnosticText.isBlank()
                            ? "No primary top-level type matching file name"
                            : "No primary type; parser errors: "
                            + diagnosticText;

            return new ParseResult(
                    null,
                    reason
            );
        }

        Trees trees =
                Trees.instance(
                        task
                );

        SourcePositions positions =
                trees.getSourcePositions();

        long start =
                positions.getStartPosition(
                        unit,
                        primary
                );

        long end =
                positions.getEndPosition(
                        unit,
                        primary
                );

        if (start == Diagnostic.NOPOS
                || end == Diagnostic.NOPOS
                || end <= start) {

            return new ParseResult(
                    null,
                    "Primary type source positions unavailable"
            );
        }

        long startLine =
                unit.getLineMap()
                        .getLineNumber(
                                start
                        );

        long endLine =
                unit.getLineMap()
                        .getLineNumber(
                                end - 1
                        );

        int typeLoc =
                Math.toIntExact(
                        endLine
                                - startLine
                                + 1
                );

        int declaredMethods = 0;
        int constructors = 0;

        for (Tree member : primary.getMembers()) {

            if (member instanceof MethodTree method) {

                if (method.getReturnType() == null) {

                    constructors++;

                } else {

                    declaredMethods++;
                }
            }
        }

        Set<Modifier> modifiers =
                primary.getModifiers()
                        .getFlags();

        String packageName =
                unit.getPackageName() == null
                        ? ""
                        : unit.getPackageName()
                        .toString();

        String fqcn =
                packageName.isBlank()
                        ? simpleName
                        : packageName
                        + "."
                        + simpleName;

        InventoryRow row =
                new InventoryRow(
                        fqcn,
                        simpleName,
                        sourcePath,
                        moduleFromPath(
                                sourcePath
                        ),
                        primary.getKind()
                                .name(),
                        modifiers.contains(
                                Modifier.PUBLIC
                        ),
                        modifiers.contains(
                                Modifier.ABSTRACT
                        ),
                        modifiers.contains(
                                Modifier.FINAL
                        ),
                        typeLoc,
                        declaredMethods,
                        constructors,
                        declaredMethods
                                + constructors
                );

        return new ParseResult(
                row,
                ""
        );
    }

    private static Validation validate(
            List<String> productionPaths,
            List<InventoryRow> rows
    ) {

        if (rows.isEmpty()) {
            throw new IllegalStateException(
                    "No primary Java types were parsed."
            );
        }

        Set<String> paths =
                new HashSet<>();

        Map<String, Long> fqcnCounts =
                rows.stream()
                        .collect(
                                Collectors.groupingBy(
                                        InventoryRow::fqcn,
                                        Collectors.counting()
                                )
                        );

        for (InventoryRow row : rows) {

            if (!paths.add(
                    row.filePath()
            )) {

                throw new IllegalStateException(
                        "Duplicate source path: "
                                + row.filePath()
                );
            }

            if (row.typeLoc() <= 0) {

                throw new IllegalStateException(
                        "Non-positive TypeLOC: "
                                + row.fqcn()
                );
            }

            if (row.declaredMethods() < 0
                    || row.constructors() < 0) {

                throw new IllegalStateException(
                        "Negative operation count: "
                                + row.fqcn()
                );
            }

            if (JavaClassScopeClassifier.classify(
                    row.filePath()
            ) != ClassScope.PRODUCTION) {

                throw new IllegalStateException(
                        "Non-production row in inventory: "
                                + row.filePath()
                );
            }
        }

        if (rows.size() > productionPaths.size()) {

            throw new IllegalStateException(
                    "Inventory has more rows than production source files."
            );
        }

        long duplicateFqcnGroups =
                fqcnCounts.values()
                        .stream()
                        .filter(count ->
                                count > 1
                        )
                        .count();

        return new Validation(
                duplicateFqcnGroups
        );
    }

    private static void writeCsv(
            List<InventoryRow> rows,
            Path output,
            String ref,
            String commitId
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
                    "ReleaseTag,"
                            + "CommitId,"
                            + "FQCN,"
                            + "SimpleName,"
                            + "FilePath,"
                            + "Module,"
                            + "TypeKind,"
                            + "Public,"
                            + "Abstract,"
                            + "Final,"
                            + "TypeLOC,"
                            + "DeclaredMethods,"
                            + "Constructors,"
                            + "TotalOperations"
            );

            writer.newLine();

            for (InventoryRow row : rows) {

                writer.write(
                        csv(ref)
                                + ","
                                + csv(commitId)
                                + ","
                                + csv(row.fqcn())
                                + ","
                                + csv(row.simpleName())
                                + ","
                                + csv(row.filePath())
                                + ","
                                + csv(row.module())
                                + ","
                                + csv(row.typeKind())
                                + ","
                                + row.isPublic()
                                + ","
                                + row.isAbstract()
                                + ","
                                + row.isFinal()
                                + ","
                                + row.typeLoc()
                                + ","
                                + row.declaredMethods()
                                + ","
                                + row.constructors()
                                + ","
                                + row.totalOperations()
                );

                writer.newLine();
            }
        }
    }

    private static void writeSummary(
            List<String> trackedJavaPaths,
            List<String> productionPaths,
            List<InventoryRow> rows,
            List<SkippedRow> skipped,
            Validation validation,
            Path summary,
            String ref,
            String commitId
    ) throws IOException {

        Files.createDirectories(
                summary.getParent()
        );

        Map<Tree.Kind, Long> kindCounts =
                new EnumMap<>(
                        Tree.Kind.class
                );

        for (InventoryRow row : rows) {

            Tree.Kind kind =
                    Tree.Kind.valueOf(
                            row.typeKind()
                    );

            kindCounts.merge(
                    kind,
                    1L,
                    Long::sum
            );
        }

        long classes =
                rows.stream()
                        .filter(row ->
                                "CLASS".equals(
                                        row.typeKind()
                                )
                        )
                        .count();

        long concreteClasses =
                rows.stream()
                        .filter(row ->
                                "CLASS".equals(
                                        row.typeKind()
                                )
                                        && !row.isAbstract()
                        )
                        .count();

        long abstractClasses =
                rows.stream()
                        .filter(row ->
                                "CLASS".equals(
                                        row.typeKind()
                                )
                                        && row.isAbstract()
                        )
                        .count();

        List<String> lines =
                new ArrayList<>();

        lines.add(
                "OpenJPA class-selection structural inventory"
        );

        lines.add(
                "ReleaseTag="
                        + ref
        );

        lines.add(
                "CommitId="
                        + commitId
        );

        lines.add(
                "TrackedJavaFiles="
                        + trackedJavaPaths.size()
        );

        lines.add(
                "ProductionJavaFiles="
                        + productionPaths.size()
        );

        lines.add(
                "PrimaryTypeRows="
                        + rows.size()
        );

        lines.add(
                "SkippedFiles="
                        + skipped.size()
        );

        lines.add(
                "DuplicateFqcnGroups="
                        + validation.duplicateFqcnGroups()
        );

        lines.add(
                "ClassRows="
                        + classes
        );

        lines.add(
                "ConcreteClassRows="
                        + concreteClasses
        );

        lines.add(
                "AbstractClassRows="
                        + abstractClasses
        );

        for (Map.Entry<Tree.Kind, Long> entry
                : kindCounts.entrySet()) {

            lines.add(
                    "TypeKind."
                            + entry.getKey()
                            + "="
                            + entry.getValue()
            );
        }

        if (!skipped.isEmpty()) {

            lines.add(
                    ""
            );

            lines.add(
                    "Skipped files:"
            );

            for (SkippedRow row : skipped) {

                lines.add(
                        row.filePath()
                                + " | "
                                + row.reason()
                );
            }
        }

        Files.write(
                summary,
                lines,
                StandardCharsets.UTF_8
        );
    }

    private static void printSummary(
            List<String> trackedJavaPaths,
            List<String> productionPaths,
            List<InventoryRow> rows,
            List<SkippedRow> skipped,
            Validation validation,
            Path output,
            Path summary,
            String ref,
            String commitId
    ) {

        long classes =
                rows.stream()
                        .filter(row ->
                                "CLASS".equals(
                                        row.typeKind()
                                )
                        )
                        .count();

        long concreteClasses =
                rows.stream()
                        .filter(row ->
                                "CLASS".equals(
                                        row.typeKind()
                                )
                                        && !row.isAbstract()
                        )
                        .count();

        long abstractClasses =
                rows.stream()
                        .filter(row ->
                                "CLASS".equals(
                                        row.typeKind()
                                )
                                        && row.isAbstract()
                        )
                        .count();

        System.out.println();
        System.out.println(
                "===== OPENJPA 4.1.1 CLASS-SELECTION STRUCTURAL INVENTORY ====="
        );

        System.out.println(
                "Release tag          : "
                        + ref
        );

        System.out.println(
                "Commit               : "
                        + commitId
        );

        System.out.println(
                "Tracked Java files   : "
                        + trackedJavaPaths.size()
        );

        System.out.println(
                "Production Java files: "
                        + productionPaths.size()
        );

        System.out.println(
                "Primary type rows    : "
                        + rows.size()
        );

        System.out.println(
                "Skipped files        : "
                        + skipped.size()
        );

        System.out.println(
                "Duplicate FQCN groups: "
                        + validation.duplicateFqcnGroups()
        );

        System.out.println(
                "CLASS rows           : "
                        + classes
        );

        System.out.println(
                "Concrete CLASS rows  : "
                        + concreteClasses
        );

        System.out.println(
                "Abstract CLASS rows  : "
                        + abstractClasses
        );

        System.out.println(
                "Output CSV           : "
                        + output
        );

        System.out.println(
                "Summary              : "
                        + summary
        );

        System.out.println(
                "============================================================"
        );
    }

    private static List<String> listTrackedJavaFiles(
            Path repositoryRoot,
            String commitId
    ) throws IOException, InterruptedException {

        String output =
                runGit(
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
                .map(
                        String::trim
                )
                .filter(path ->
                        path.endsWith(
                                ".java"
                        )
                )
                .sorted()
                .toList();
    }

    private static String readSourceAtCommit(
            Path repositoryRoot,
            String commitId,
            String sourcePath
    ) throws IOException, InterruptedException {

        return runGit(
                repositoryRoot,
                "show",
                commitId
                        + ":"
                        + sourcePath
        );
    }

    private static String resolveCommit(
            Path repositoryRoot,
            String ref
    ) throws IOException, InterruptedException {

        String commit =
                runGit(
                        repositoryRoot,
                        "rev-parse",
                        ref
                                + "^{commit}"
                )
                        .trim();

        if (commit.isBlank()) {

            throw new IllegalStateException(
                    "Unable to resolve Git ref: "
                            + ref
            );
        }

        return commit;
    }

    private static String moduleFromPath(
            String sourcePath
    ) {

        String normalized =
                sourcePath.replace(
                        '\\',
                        '/'
                );

        for (String marker : List.of(
                "/src/main/java/",
                "/src/java/"
        )) {

            int index =
                    normalized.indexOf(
                            marker
                    );

            if (index >= 0) {

                String module =
                        normalized.substring(
                                0,
                                index
                        );

                return module.isBlank()
                        ? "."
                        : module;
            }
        }

        if (normalized.startsWith(
                "src/main/java/"
        ) || normalized.startsWith(
                "src/java/"
        )) {

            return ".";
        }

        throw new IllegalArgumentException(
                "Unexpected production source path: "
                        + sourcePath
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
                Paths.get(
                                args[0]
                        )
                        .toAbsolutePath()
                        .normalize();

        if (!Files.isDirectory(
                root.resolve(
                        ".git"
                )
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
                new String[
                        arguments.length
                                + 3
                        ];

        command[0] =
                "git";

        command[1] =
                "-C";

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
                new ProcessBuilder(
                        command
                )
                        .redirectErrorStream(
                                true
                        )
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
                            );
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
            String fqcn,
            String simpleName,
            String filePath,
            String module,
            String typeKind,
            boolean isPublic,
            boolean isAbstract,
            boolean isFinal,
            int typeLoc,
            int declaredMethods,
            int constructors,
            int totalOperations
    ) {
    }

    private record ParseResult(
            InventoryRow row,
            String reason
    ) {
    }

    private record SkippedRow(
            String filePath,
            String reason
    ) {
    }

    private record Validation(
            long duplicateFqcnGroups
    ) {
    }

    private static final class StringJavaFileObject
            extends SimpleJavaFileObject {

        private final String source;

        private StringJavaFileObject(
                String simpleName,
                String source
        ) {

            super(
                    URI.create(
                            "string:///"
                                    + simpleName
                                    + Kind.SOURCE.extension
                    ),
                    Kind.SOURCE
            );

            this.source =
                    source;
        }

        @Override
        public CharSequence getCharContent(
                boolean ignoreEncodingErrors
        ) {

            return source;
        }
    }
}
