package it.uniroma2.isw2.openjpa.metrics;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class JavaLocCounter {

    private final Path repositoryRoot;

    public JavaLocCounter(Path repositoryRoot) {

        this.repositoryRoot =
                repositoryRoot
                        .toAbsolutePath()
                        .normalize();

        if (!Files.isDirectory(
                this.repositoryRoot.resolve(".git")
        )) {
            throw new IllegalArgumentException(
                    "Not a Git repository root: "
                            + this.repositoryRoot
            );
        }
    }

    public int count(
            String commitId,
            String filePath
    ) throws IOException, InterruptedException {

        String source =
                runGit(
                        "show",
                        commitId + ":" + filePath
                );

        return countSourceLoc(source);
    }

    static int countSourceLoc(String source) {

        if (source == null || source.isEmpty()) {
            return 0;
        }

        boolean inBlockComment = false;
        int loc = 0;

        String[] lines =
                source.split("\\R", -1);

        for (String line : lines) {

            LineResult result =
                    containsCode(
                            line,
                            inBlockComment
                    );

            inBlockComment =
                    result.inBlockComment();

            if (result.hasCode()) {
                loc++;
            }
        }

        return loc;
    }

    private static LineResult containsCode(
            String line,
            boolean initialBlockComment
    ) {

        boolean inBlockComment =
                initialBlockComment;

        boolean inString = false;
        boolean inChar = false;
        boolean escaped = false;
        boolean hasCode = false;

        for (int index = 0;
             index < line.length();
             index++) {

            char current =
                    line.charAt(index);

            char next =
                    index + 1 < line.length()
                            ? line.charAt(index + 1)
                            : '\0';

            if (inBlockComment) {

                if (current == '*'
                        && next == '/') {

                    inBlockComment = false;
                    index++;
                }

                continue;
            }

            if (inString) {

                hasCode = true;

                if (escaped) {

                    escaped = false;

                } else if (current == '\\') {

                    escaped = true;

                } else if (current == '"') {

                    inString = false;
                }

                continue;
            }

            if (inChar) {

                hasCode = true;

                if (escaped) {

                    escaped = false;

                } else if (current == '\\') {

                    escaped = true;

                } else if (current == '\'') {

                    inChar = false;
                }

                continue;
            }

            if (current == '/'
                    && next == '/') {

                break;
            }

            if (current == '/'
                    && next == '*') {

                inBlockComment = true;
                index++;
                continue;
            }

            if (current == '"') {

                inString = true;
                hasCode = true;
                continue;
            }

            if (current == '\'') {

                inChar = true;
                hasCode = true;
                continue;
            }

            if (!Character.isWhitespace(current)) {

                hasCode = true;
            }
        }

        return new LineResult(
                hasCode,
                inBlockComment
        );
    }

    private String runGit(
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
                            );
        }

        int exitCode =
                process.waitFor();

        if (exitCode != 0) {

            throw new IOException(
                    "Git command failed (exit "
                            + exitCode
                            + "): "
                            + String.join(" ", command)
                            + System.lineSeparator()
                            + output
            );
        }

        return output;
    }

    private record LineResult(
            boolean hasCode,
            boolean inBlockComment
    ) {
    }
}
