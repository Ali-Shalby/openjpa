package it.uniroma2.isw2.openjpa.release;

import java.time.LocalDate;

public class ReleaseInfo {

    private final String jiraId;
    private final String version;
    private final LocalDate releaseDate;

    public ReleaseInfo(String jiraId, String version, LocalDate releaseDate) {
        this.jiraId = jiraId;
        this.version = version;
        this.releaseDate = releaseDate;
    }

    public String getJiraId() {
        return jiraId;
    }

    public String getVersion() {
        return version;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }
}