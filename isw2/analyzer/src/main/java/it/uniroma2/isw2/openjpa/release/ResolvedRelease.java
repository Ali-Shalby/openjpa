package it.uniroma2.isw2.openjpa.release;

public class ResolvedRelease {

    private final ReleaseInfo release;
    private final String gitTag;
    private final String releaseCommit;
    private final String releaseCommitDate;
    private final String resolutionMethod;

    public ResolvedRelease(
            ReleaseInfo release,
            String gitTag,
            String releaseCommit,
            String releaseCommitDate,
            String resolutionMethod
    ) {
        this.release = release;
        this.gitTag = gitTag;
        this.releaseCommit = releaseCommit;
        this.releaseCommitDate = releaseCommitDate;
        this.resolutionMethod = resolutionMethod;
    }

    public ReleaseInfo getRelease() {
        return release;
    }

    public String getGitTag() {
        return gitTag;
    }

    public boolean isGitTagMatched() {
        return gitTag != null && !gitTag.isBlank();
    }

    public String getReleaseCommit() {
        return releaseCommit;
    }

    public String getReleaseCommitDate() {
        return releaseCommitDate;
    }

    public String getResolutionMethod() {
        return resolutionMethod;
    }
}