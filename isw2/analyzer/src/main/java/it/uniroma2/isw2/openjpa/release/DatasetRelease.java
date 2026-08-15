package it.uniroma2.isw2.openjpa.release;

import java.time.LocalDate;

public record DatasetRelease(
        int releaseIndex,
        String version,
        String commitId,
        LocalDate releaseDate
) {
}
