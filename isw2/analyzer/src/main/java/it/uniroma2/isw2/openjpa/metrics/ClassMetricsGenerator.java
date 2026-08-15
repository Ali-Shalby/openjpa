package it.uniroma2.isw2.openjpa.metrics;

import it.uniroma2.isw2.openjpa.inventory.ProductionClassInventoryReader;
import it.uniroma2.isw2.openjpa.inventory.ProductionClassObservation;
import it.uniroma2.isw2.openjpa.release.DatasetRelease;
import it.uniroma2.isw2.openjpa.release.DatasetReleaseCatalogReader;

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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ClassMetricsGenerator {

    private static final String RELEASE_CATALOG =
            "isw2/datasets/release_catalog.csv";

    private static final String CLASS_INVENTORY =
            "isw2/datasets/java_class_inventory.csv";

    public static void main(String[] args) {

        if (args.length != 2) {
            throw new IllegalArgumentException(
                    "Usage: <repository-root> <release-index|ALL>"
            );
        }

        try {

            Path repositoryRoot =
                    Paths.get(args[0])
                            .toAbsolutePath()
                            .normalize();

            String selection =
                    args[1];

            DatasetReleaseCatalogReader releaseReader =
                    new DatasetReleaseCatalogReader();

            List<DatasetRelease> releases =
                    releaseReader.readIncludedReleases(
                            repositoryRoot.resolve(
                                    RELEASE_CATALOG
                            )
                    );

            Map<Integer, DatasetRelease> releasesByIndex =
                    new HashMap<>();

            for (DatasetRelease release : releases) {
                releasesByIndex.put(
                        release.releaseIndex(),
                        release
                );
            }

            ProductionClassInventoryReader inventoryReader =
                    new ProductionClassInventoryReader();

            List<ProductionClassObservation> inventory =
                    inventoryReader.read(
                            repositoryRoot.resolve(
                                    CLASS_INVENTORY
                            )
                    );

            List<ProductionClassObservation> selected =
                    selectObservations(
                            inventory,
                            selection
                    );

            if (selected.isEmpty()) {
                throw new IllegalStateException(
                        "No observations selected."
                );
            }

            Path output =
                    resolveOutput(
                            repositoryRoot,
                            selection
                    );

            Path failuresOutput =
                    resolveFailuresOutput(
                            repositoryRoot,
                            selection
                    );

            GitHistoricalMetricsReader historyReader =
                    new GitHistoricalMetricsReader(
                            repositoryRoot
                    );

            JavaLocCounter locCounter =
                    new JavaLocCounter(
                            repositoryRoot
                    );

            List<MetricRow> rows =
                    new ArrayList<>();

            List<FailureRow> failureRows =
                    new ArrayList<>();

            long startNanos =
                    System.nanoTime();

            int processed = 0;

            try (BufferedWriter metricsWriter =
                         openMetricsWriter(output);
                 BufferedWriter failuresWriter =
                         openFailuresWriter(failuresOutput)) {

                for (ProductionClassObservation observation : selected) {

                    processed++;

                    DatasetRelease release =
                            releasesByIndex.get(
                                    observation.releaseIndex()
                            );

                    if (release == null) {
                        throw new IllegalStateException(
                                "Release metadata not found: "
                                        + observation.releaseIndex()
                        );
                    }

                    try {

                        int loc =
                                locCounter.count(
                                        observation.commitId(),
                                        observation.classPath()
                                );

                        HistoricalMetrics historical =
                                historyReader.compute(
                                        observation.commitId(),
                                        release.releaseDate(),
                                        observation.classPath()
                                );

                        MetricRow row =
                                new MetricRow(
                                        observation,
                                        loc,
                                        historical
                                );

                        rows.add(row);

                        writeMetricRow(
                                metricsWriter,
                                row
                        );

                    } catch (Exception exception) {

                        FailureRow failure =
                                new FailureRow(
                                        observation.releaseIndex(),
                                        observation.version(),
                                        observation.commitId(),
                                        observation.classPath(),
                                        exception.getClass()
                                                .getSimpleName(),
                                        sanitize(
                                                exception.getMessage()
                                        )
                                );

                        failureRows.add(failure);

                        writeFailureRow(
                                failuresWriter,
                                failure
                        );
                    }

                    if (processed % 50 == 0
                            || processed == selected.size()) {

                        metricsWriter.flush();
                        failuresWriter.flush();

                        long elapsedSeconds =
                                (System.nanoTime() - startNanos)
                                        / 1_000_000_000L;

                        long estimatedTotalSeconds =
                                processed == 0
                                        ? 0
                                        : Math.round(
                                        (double) elapsedSeconds
                                                * selected.size()
                                                / processed
                                );

                        long remainingSeconds =
                                Math.max(
                                        0,
                                        estimatedTotalSeconds
                                                - elapsedSeconds
                                );

                        System.out.printf(
                                Locale.ROOT,
                                "[%d/%d] "
                                        + "release=%s (%d/12) "
                                        + "success=%d "
                                        + "failures=%d "
                                        + "elapsed=%s "
                                        + "eta=%s%n",
                                processed,
                                selected.size(),
                                observation.version(),
                                observation.releaseIndex(),
                                rows.size(),
                                failureRows.size(),
                                formatDuration(elapsedSeconds),
                                formatDuration(remainingSeconds)
                        );
                    }
                }
            }

            validateSuccessfulRows(rows);

            long totalElapsedSeconds =
                    (System.nanoTime() - startNanos)
                            / 1_000_000_000L;

            printSummary(
                    selection,
                    selected.size(),
                    rows,
                    failureRows,
                    output,
                    failuresOutput,
                    totalElapsedSeconds
            );

        } catch (Exception exception) {

            System.err.println(
                    "Unable to generate class metrics: "
                            + exception.getMessage()
            );

            exception.printStackTrace();

            System.exit(1);
        }
    }

    private static List<ProductionClassObservation> selectObservations(
            List<ProductionClassObservation> inventory,
            String selection
    ) {

        if ("ALL".equalsIgnoreCase(selection)) {
            return inventory;
        }

        int releaseIndex =
                Integer.parseInt(selection);

        return inventory.stream()
                .filter(observation ->
                        observation.releaseIndex()
                                == releaseIndex
                )
                .toList();
    }

    private static Path resolveOutput(
            Path repositoryRoot,
            String selection
    ) {

        if ("ALL".equalsIgnoreCase(selection)) {

            return repositoryRoot.resolve(
                    "isw2/datasets/class_metrics.csv"
            );
        }

        return repositoryRoot.resolve(
                "isw2/results/metrics/class_metrics_release_"
                        + selection
                        + ".csv"
        );
    }

    private static Path resolveFailuresOutput(
            Path repositoryRoot,
            String selection
    ) {

        return repositoryRoot.resolve(
                "isw2/results/metrics/class_metrics_"
                        + selection
                        + "_failures.csv"
        );
    }

    private static BufferedWriter openMetricsWriter(
            Path output
    ) throws IOException {

        Files.createDirectories(
                output.getParent()
        );

        BufferedWriter writer =
                Files.newBufferedWriter(
                        output,
                        StandardCharsets.UTF_8
                );

        writer.write(
                "ReleaseIndex,"
                        + "Version,"
                        + "CommitId,"
                        + "Class,"
                        + "LOC,"
                        + "LOC_TOUCHED,"
                        + "NR,"
                        + "NAUTH,"
                        + "LOC_ADDED,"
                        + "MAX_LOC_ADDED,"
                        + "AVG_LOC_ADDED,"
                        + "CHURN,"
                        + "MAX_CHURN,"
                        + "AVG_CHURN,"
                        + "CHANGE_SET_SIZE,"
                        + "MAX_CHANGE_SET,"
                        + "AVG_CHANGE_SET,"
                        + "AGE_WEEKS,"
                        + "WEIGHTED_AGE_WEEKS,"
                        + "IGNORED_ZERO_LOC_REVS"
        );

        writer.newLine();
        writer.flush();

        return writer;
    }

    private static BufferedWriter openFailuresWriter(
            Path output
    ) throws IOException {

        Files.createDirectories(
                output.getParent()
        );

        BufferedWriter writer =
                Files.newBufferedWriter(
                        output,
                        StandardCharsets.UTF_8
                );

        writer.write(
                "ReleaseIndex,"
                        + "Version,"
                        + "CommitId,"
                        + "Class,"
                        + "Exception,"
                        + "Message"
        );

        writer.newLine();
        writer.flush();

        return writer;
    }

    private static void writeMetricRow(
            BufferedWriter writer,
            MetricRow row
    ) throws IOException {

        ProductionClassObservation observation =
                row.observation();

        HistoricalMetrics metrics =
                row.historical();

        writer.write(
                observation.releaseIndex()
                        + ","
                        + csv(observation.version())
                        + ","
                        + csv(observation.commitId())
                        + ","
                        + csv(observation.classPath())
                        + ","
                        + row.loc()
                        + ","
                        + metrics.locTouched()
                        + ","
                        + metrics.nr()
                        + ","
                        + metrics.nauth()
                        + ","
                        + metrics.locAdded()
                        + ","
                        + metrics.maxLocAdded()
                        + ","
                        + number(metrics.avgLocAdded())
                        + ","
                        + metrics.churn()
                        + ","
                        + metrics.maxChurn()
                        + ","
                        + number(metrics.avgChurn())
                        + ","
                        + metrics.changeSetSize()
                        + ","
                        + metrics.maxChangeSet()
                        + ","
                        + number(metrics.avgChangeSet())
                        + ","
                        + number(metrics.ageWeeks())
                        + ","
                        + number(
                        metrics.weightedAgeWeeks()
                )
                        + ","
                        + metrics.ignoredZeroLocRevisions()
        );

        writer.newLine();
    }

    private static void writeFailureRow(
            BufferedWriter writer,
            FailureRow failure
    ) throws IOException {

        writer.write(
                failure.releaseIndex()
                        + ","
                        + csv(failure.version())
                        + ","
                        + csv(failure.commitId())
                        + ","
                        + csv(failure.classPath())
                        + ","
                        + csv(failure.exception())
                        + ","
                        + csv(failure.message())
        );

        writer.newLine();
    }

    private static void validateSuccessfulRows(
            List<MetricRow> rows
    ) {

        if (rows.isEmpty()) {
            throw new IllegalStateException(
                    "No successful metric rows generated."
            );
        }

        Set<String> keys =
                new HashSet<>();

        for (MetricRow row : rows) {

            ProductionClassObservation observation =
                    row.observation();

            if (row.loc() <= 0) {
                throw new IllegalStateException(
                        "LOC <= 0 for "
                                + observation.classPath()
                );
            }

            if (row.historical().nr() <= 0) {
                throw new IllegalStateException(
                        "NR <= 0 for "
                                + observation.classPath()
                );
            }

            if (row.historical().nauth() <= 0) {
                throw new IllegalStateException(
                        "NAUTH <= 0 for "
                                + observation.classPath()
                );
            }

            String key =
                    observation.releaseIndex()
                            + "\u0000"
                            + observation.classPath();

            if (!keys.add(key)) {
                throw new IllegalStateException(
                        "Duplicate metric key: "
                                + observation.releaseIndex()
                                + " / "
                                + observation.classPath()
                );
            }
        }
    }

    private static void printSummary(
            String selection,
            int expected,
            List<MetricRow> rows,
            List<FailureRow> failures,
            Path output,
            Path failuresOutput,
            long elapsedSeconds
    ) {

        long zeroLocRevisions =
                rows.stream()
                        .mapToLong(row ->
                                row.historical()
                                        .ignoredZeroLocRevisions()
                        )
                        .sum();

        System.out.println();
        System.out.println(
                "===== OPENJPA CLASS METRICS ====="
        );

        System.out.println(
                "Selection             : "
                        + selection
        );

        System.out.println(
                "Expected observations : "
                        + expected
        );

        System.out.println(
                "Successful            : "
                        + rows.size()
        );

        System.out.println(
                "Failures              : "
                        + failures.size()
        );

        System.out.println(
                "Ignored zero-LOC revs : "
                        + zeroLocRevisions
        );

        System.out.println(
                "Elapsed time          : "
                        + formatDuration(elapsedSeconds)
        );

        System.out.println(
                "Output                : "
                        + output
        );

        System.out.println(
                "Failure diagnostics   : "
                        + failuresOutput
        );

        System.out.println(
                "================================="
        );
    }

    private static String formatDuration(
            long totalSeconds
    ) {

        long hours =
                totalSeconds / 3600;

        long minutes =
                (totalSeconds % 3600) / 60;

        long seconds =
                totalSeconds % 60;

        return String.format(
                Locale.ROOT,
                "%02d:%02d:%02d",
                hours,
                minutes,
                seconds
        );
    }

    private static String sanitize(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .replace('\r', ' ')
                .replace('\n', ' ');
    }

    private static String number(
            double value
    ) {

        return String.format(
                Locale.ROOT,
                "%.6f",
                value
        );
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

    private record MetricRow(
            ProductionClassObservation observation,
            int loc,
            HistoricalMetrics historical
    ) {
    }

    private record FailureRow(
            int releaseIndex,
            String version,
            String commitId,
            String classPath,
            String exception,
            String message
    ) {
    }
}
