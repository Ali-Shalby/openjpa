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

import it.uniroma2.isw2.openjpa.inventory.ClassScope;
import it.uniroma2.isw2.openjpa.inventory.JavaClassScopeClassifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SzzAnalyzer {

    private static final Pattern HUNK_PATTERN =
            Pattern.compile(
                    "^@@ -(\\d+)(?:,(\\d+))? "
                            + "\\+(\\d+)(?:,(\\d+))? @@.*$"
            );

    private static final Pattern BLAME_HEADER_PATTERN =
            Pattern.compile(
                    "^([0-9a-fA-F]{40,64}) "
                            + "\\d+ \\d+(?: \\d+)?$"
            );

    private final Path repository;

    public SzzAnalyzer(Path repository) {

        this.repository =
                repository
                        .toAbsolutePath()
                        .normalize();
    }

    public List<SzzEvidence> analyze(
            String issueKey,
            String fixCommitId
    ) throws IOException, InterruptedException {

        String parentCommitId =
                resolveSingleParent(fixCommitId);

        String diff =
                runGit(
                        "diff",
                        "--no-ext-diff",
                        "--no-color",
                        "--find-renames",
                        "--unified=0",
                        parentCommitId,
                        fixCommitId,
                        "--"
                );

        List<DeletedFileLines> deletedLines =
                parseDeletedProductionLines(diff);

        List<SzzEvidence> result =
                new ArrayList<>();

        for (DeletedFileLines file : deletedLines) {

            Map<String, Integer> blamedCommits =
                    blameDeletedLines(
                            parentCommitId,
                            file.blamedFilePath(),
                            file.lineNumbers()
                    );

            for (Map.Entry<String, Integer> entry
                    : blamedCommits.entrySet()) {

                result.add(
                        new SzzEvidence(
                                issueKey,
                                fixCommitId,
                                parentCommitId,
                                file.fixedFilePath(),
                                file.blamedFilePath(),
                                entry.getKey(),
                                entry.getValue()
                        )
                );
            }
        }

        return result.stream()
                .sorted(
                        Comparator
                                .comparing(
                                        SzzEvidence::fixedFilePath
                                )
                                .thenComparing(
                                        SzzEvidence
                                                ::bugIntroducingCommitId
                                )
                )
                .toList();
    }

    private String resolveSingleParent(
            String fixCommitId
    ) throws IOException, InterruptedException {

        String output =
                runGit(
                        "rev-list",
                        "--parents",
                        "-n",
                        "1",
                        fixCommitId
                ).trim();

        String[] tokens =
                output.split("\\s+");

        if (tokens.length != 2) {
            throw new IOException(
                    "SZZ currently requires a non-merge "
                            + "fix commit with exactly one parent: "
                            + fixCommitId
                            + " (parents="
                            + (tokens.length - 1)
                            + ")"
            );
        }

        return tokens[1];
    }

    private List<DeletedFileLines>
    parseDeletedProductionLines(
            String diff
    ) {

        List<DeletedFileLines> result =
                new ArrayList<>();

        PatchState current =
                null;

        int oldLine = -1;
        int newLine = -1;

        for (String line : diff.lines().toList()) {

            if (line.startsWith("diff --git ")) {

                addPatchIfRelevant(
                        current,
                        result
                );

                current =
                        new PatchState();

                oldLine = -1;
                newLine = -1;

                continue;
            }

            if (current == null) {
                continue;
            }

            if (line.startsWith("--- ")) {

                current.oldPath =
                        parseDiffPath(
                                line.substring(4)
                        );

                continue;
            }

            if (line.startsWith("+++ ")) {

                current.newPath =
                        parseDiffPath(
                                line.substring(4)
                        );

                continue;
            }

            Matcher hunkMatcher =
                    HUNK_PATTERN.matcher(line);

            if (hunkMatcher.matches()) {

                oldLine =
                        Integer.parseInt(
                                hunkMatcher.group(1)
                        );

                newLine =
                        Integer.parseInt(
                                hunkMatcher.group(3)
                        );

                continue;
            }

            if (oldLine < 0 || newLine < 0) {
                continue;
            }

            if (line.startsWith("-")) {

                current.deletedLines.add(
                        oldLine
                );

                oldLine++;
                continue;
            }

            if (line.startsWith("+")) {

                newLine++;
                continue;
            }

            if (line.startsWith(" ")) {

                oldLine++;
                newLine++;
                continue;
            }

            /*
             * Example:
             *
             * \ No newline at end of file
             *
             * This line does not consume either source line.
             */
            if (line.startsWith("\\")) {
                continue;
            }
        }

        addPatchIfRelevant(
                current,
                result
        );

        return result;
    }

    private static void addPatchIfRelevant(
            PatchState patch,
            List<DeletedFileLines> result
    ) {

        if (patch == null
                || patch.deletedLines.isEmpty()
                || patch.oldPath == null) {

            return;
        }

        String fixedPath =
                patch.newPath == null
                        ? patch.oldPath
                        : patch.newPath;

        if (!isProductionJava(
                patch.oldPath,
                patch.newPath
        )) {
            return;
        }

        result.add(
                new DeletedFileLines(
                        fixedPath,
                        patch.oldPath,
                        List.copyOf(
                                patch.deletedLines
                        )
                )
        );
    }

    private Map<String, Integer> blameDeletedLines(
            String parentCommitId,
            String filePath,
            List<Integer> lineNumbers
    ) throws IOException, InterruptedException {

        Map<String, Integer> blamed =
                new LinkedHashMap<>();

        for (LineRange range
                : contiguousRanges(lineNumbers)) {

            String blame =
                    runGit(
                            "blame",
                            "--line-porcelain",
                            "-L",
                            range.start()
                                    + ","
                                    + range.end(),
                            parentCommitId,
                            "--",
                            filePath
                    );

            for (String line
                    : blame.lines().toList()) {

                Matcher matcher =
                        BLAME_HEADER_PATTERN.matcher(
                                line
                        );

                if (!matcher.matches()) {
                    continue;
                }

                String commitId =
                        matcher.group(1)
                                .toLowerCase();

                if (isZeroCommit(commitId)) {
                    continue;
                }

                blamed.merge(
                        commitId,
                        1,
                        Integer::sum
                );
            }
        }

        return blamed;
    }

    private static List<LineRange> contiguousRanges(
            List<Integer> lineNumbers
    ) {

        if (lineNumbers.isEmpty()) {
            return List.of();
        }

        List<Integer> sorted =
                lineNumbers.stream()
                        .distinct()
                        .sorted()
                        .toList();

        List<LineRange> ranges =
                new ArrayList<>();

        int start =
                sorted.getFirst();

        int previous =
                start;

        for (int index = 1;
             index < sorted.size();
             index++) {

            int current =
                    sorted.get(index);

            if (current == previous + 1) {

                previous =
                        current;

                continue;
            }

            ranges.add(
                    new LineRange(
                            start,
                            previous
                    )
            );

            start =
                    current;

            previous =
                    current;
        }

        ranges.add(
                new LineRange(
                        start,
                        previous
                )
        );

        return ranges;
    }

    private static boolean isProductionJava(
            String oldPath,
            String newPath
    ) {

        return isProductionJava(oldPath)
                || isProductionJava(newPath);
    }

    private static boolean isProductionJava(
            String path
    ) {

        if (path == null
                || !path.endsWith(".java")) {

            return false;
        }

        return JavaClassScopeClassifier
                .classify(path)
                == ClassScope.PRODUCTION;
    }

    private static String parseDiffPath(
            String value
    ) {

        String path =
                value.trim();

        if ("/dev/null".equals(path)) {
            return null;
        }

        /*
         * Normal Git patch format:
         *
         * a/module/src/.../Foo.java
         * b/module/src/.../Foo.java
         */
        if (path.startsWith("a/")
                || path.startsWith("b/")) {

            path =
                    path.substring(2);
        }

        if (path.length() >= 2
                && path.startsWith("\"")
                && path.endsWith("\"")) {

            path =
                    path.substring(
                            1,
                            path.length() - 1
                    );
        }

        return path;
    }

    private static boolean isZeroCommit(
            String commitId
    ) {

        for (int index = 0;
             index < commitId.length();
             index++) {

            if (commitId.charAt(index) != '0') {
                return false;
            }
        }

        return true;
    }

    private String runGit(
            String... arguments
    ) throws IOException, InterruptedException {

        List<String> command =
                new ArrayList<>();

        command.add("git");
        command.add("-C");
        command.add(
                repository.toString()
        );

        for (String argument : arguments) {
            command.add(argument);
        }

        Process process =
                new ProcessBuilder(command)
                        .redirectErrorStream(true)
                        .start();

        byte[] bytes =
                process
                        .getInputStream()
                        .readAllBytes();

        int exitCode =
                process.waitFor();

        String output =
                new String(
                        bytes,
                        StandardCharsets.UTF_8
                );

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

    private static final class PatchState {

        private String oldPath;
        private String newPath;

        private final List<Integer> deletedLines =
                new ArrayList<>();
    }

    private record DeletedFileLines(
            String fixedFilePath,
            String blamedFilePath,
            List<Integer> lineNumbers
    ) {
    }

    private record LineRange(
            int start,
            int end
    ) {
    }
}
