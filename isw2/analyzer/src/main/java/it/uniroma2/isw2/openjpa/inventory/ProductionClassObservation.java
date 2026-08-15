package it.uniroma2.isw2.openjpa.inventory;

public record ProductionClassObservation(
        int releaseIndex,
        String version,
        String commitId,
        String classPath
) {
}
