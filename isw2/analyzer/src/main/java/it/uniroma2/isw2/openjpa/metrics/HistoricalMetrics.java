package it.uniroma2.isw2.openjpa.metrics;

public record HistoricalMetrics(
        long locTouched,
        int nr,
        int nauth,
        long locAdded,
        long maxLocAdded,
        double avgLocAdded,
        long churn,
        long maxChurn,
        double avgChurn,
        long changeSetSize,
        int maxChangeSet,
        double avgChangeSet,
        double ageWeeks,
        double weightedAgeWeeks,
        int ignoredZeroLocRevisions
) {
}
