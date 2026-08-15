package it.uniroma2.isw2.openjpa.release;

public record DatasetRelease(
        int releaseIndex,
        String version,
        String commitId
) {
}
