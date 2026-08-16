# ISW2 OpenJPA - SonarQube Cloud release processor (fixed v4)
# Validated after the Release 1 pilot and Release 2 confirmation.
#
# Key safeguards in this version:
# - waits up to 30 minutes for the exact pushed Git revision;
# - searches the latest Sonar analyses, not only the single newest item;
# - retrieves only OPEN maintainability issues from Sonar;
# - waits for the issue index to stabilize and retries pagination atomically;
# - treats Sonar IssueKey values as case-sensitive (Ordinal comparer);
# - supports -ResumeExistingAnalysis when analysis succeeded but extraction failed;
# - computes NSmells only from OPEN CODE_SMELL issues;
# - rejects non-OPEN responses, unmapped Java smells, duplicates and negative counts;
# - records the exact Sonar analysis key/revision used for every class.
#
param(
    [Parameter(Mandatory = $false)]
    [ValidateRange(1, 12)]
    [int]$ReleaseIndex = 2,

    # Reuse the snapshot already present on the auxiliary repository and the
    # Sonar analysis matching its current HEAD. This is useful when Sonar
    # analysis succeeded but post-analysis extraction failed.
    [switch]$ResumeExistingAnalysis
)

$ErrorActionPreference = "Stop"

$mainRepo = "C:\ISW2\openjpa"
$sonarRepo = "C:\ISW2\openjpa-sonar-pilot"
$inventoryPath = Join-Path $mainRepo "isw2\datasets\java_class_inventory.csv"
$projectKey = "Ali-Shalby_openjpa-isw2-sonar"
$expectedOrigin = "https://github.com/Ali-Shalby/openjpa-isw2-sonar.git"
$baseUrl = "https://sonarcloud.io"
$stageDir = "C:\ISW2\openjpa-sonar-stage-$ReleaseIndex"
$pageSize = 500
$pollSeconds = 15
$pollTimeoutMinutes = 30

if ([string]::IsNullOrWhiteSpace($env:SONAR_TOKEN)) {
    throw "SONAR_TOKEN is not set in this PowerShell session."
}

foreach ($path in @($mainRepo, $sonarRepo, $inventoryPath)) {
    if (-not (Test-Path $path)) {
        throw "Required path not found: $path"
    }
}

$headers = @{
    Authorization = "Bearer $env:SONAR_TOKEN"
}

function Invoke-SonarGet {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Uri
    )

    for ($attempt = 1; $attempt -le 5; $attempt++) {
        try {
            return Invoke-RestMethod -Uri $Uri -Headers $headers -Method Get
        }
        catch {
            $statusCode = $null
            try {
                $statusCode = [int]$_.Exception.Response.StatusCode
            }
            catch {}

            if ($statusCode -eq 429 -and $attempt -lt 5) {
                Write-Host "SonarQube Cloud rate limit reached. Waiting 30 seconds..."
                Start-Sleep -Seconds 30
                continue
            }

            throw
        }
    }
}

function Test-MaintainabilityImpact {
    param($Issue)

    foreach ($impact in @($Issue.impacts)) {
        if ($impact.softwareQuality -eq "MAINTAINABILITY") {
            return $true
        }
    }

    return $false
}


function Get-StableOpenMaintainabilityIssues {
    param(
        [int]$StablePollsRequired = 3,
        [int]$SettlePollSeconds = 10,
        [int]$SettleTimeoutMinutes = 5,
        [int]$MaxExtractionAttempts = 5
    )

    $queryPrefix = "$baseUrl/api/issues/search?projects=$([uri]::EscapeDataString($projectKey))&issueStatuses=OPEN&impactSoftwareQualities=MAINTAINABILITY"

    # Sonar's issue index can still be settling immediately after a new
    # analysis becomes visible. Wait until the filtered total is unchanged
    # for several consecutive polls before starting page-by-page extraction.
    Write-Host ""
    Write-Host "Waiting for OPEN maintainability issue index to stabilize..."

    $settleDeadline = (Get-Date).AddMinutes($SettleTimeoutMinutes)
    $lastTotal = $null
    $stablePolls = 0
    $stableTotal = $null

    while ((Get-Date) -lt $settleDeadline) {
        $countResponse = Invoke-SonarGet -Uri "$queryPrefix&p=1&ps=1"
        $currentTotal = [int]$countResponse.paging.total

        if ($null -ne $lastTotal -and $currentTotal -eq $lastTotal) {
            $stablePolls++
        }
        else {
            $stablePolls = 1
        }

        Write-Host "OPEN maintainability total: $currentTotal (stable poll $stablePolls / $StablePollsRequired)"

        $lastTotal = $currentTotal

        if ($stablePolls -ge $StablePollsRequired) {
            $stableTotal = $currentTotal
            break
        }

        Start-Sleep -Seconds $SettlePollSeconds
    }

    if ($null -eq $stableTotal) {
        throw "Sonar issue index did not stabilize within $SettleTimeoutMinutes minutes."
    }

    if ($stableTotal -gt 10000) {
        throw "OPEN maintainability issue count exceeds 10,000. This release needs partitioned API retrieval."
    }

    for ($attempt = 1; $attempt -le $MaxExtractionAttempts; $attempt++) {
        Write-Host ""
        Write-Host "Stable issue extraction attempt $attempt / $MaxExtractionAttempts..."

        $issues = New-Object System.Collections.Generic.List[object]

        # Sonar issue keys are case-sensitive. A normal PowerShell hashtable is
        # case-insensitive, which incorrectly collapses distinct keys that only
        # differ by letter case. Use Ordinal comparison explicitly.
        $seenKeys = [System.Collections.Generic.HashSet[string]]::new(
            [System.StringComparer]::Ordinal
        )

        $expectedTotal = $null
        $pageCount = $null
        $unstable = $false

        # Read the first page and lock the total for this extraction attempt.
        $firstResponse = Invoke-SonarGet -Uri "$queryPrefix&p=1&ps=$pageSize&additionalFields=_all"
        $expectedTotal = [int]$firstResponse.paging.total

        if ($expectedTotal -gt 10000) {
            throw "OPEN maintainability issue count exceeds 10,000. This release needs partitioned API retrieval."
        }

        $pageCount = [Math]::Max(
            1,
            [int][Math]::Ceiling($expectedTotal / [double]$pageSize)
        )

        for ($page = 1; $page -le $pageCount; $page++) {
            if ($page -eq 1) {
                $response = $firstResponse
            }
            else {
                $response = Invoke-SonarGet -Uri "$queryPrefix&p=$page&ps=$pageSize&additionalFields=_all"
            }

            $pageTotal = [int]$response.paging.total

            # If total changes during pagination, page boundaries may have
            # shifted. Discard this whole pass and retry from page 1.
            if ($pageTotal -ne $expectedTotal) {
                Write-Host "Issue total changed during pagination: $expectedTotal -> $pageTotal." -ForegroundColor Yellow
                $unstable = $true
                break
            }

            foreach ($issue in @($response.issues)) {
                $key = [string]$issue.key

                # HashSet.Add() returns False only for an exact, case-sensitive
                # duplicate because the set uses StringComparer.Ordinal.
                if (-not $seenKeys.Add($key)) {
                    Write-Host "Real duplicate issue key detected during pagination: $key" -ForegroundColor Yellow
                    $unstable = $true
                    break
                }

                $issues.Add($issue)
            }

            if ($unstable) {
                break
            }

            Write-Host "Fetched $($issues.Count) / $expectedTotal"
        }

        if (-not $unstable) {
            $endCountResponse = Invoke-SonarGet -Uri "$queryPrefix&p=1&ps=1"
            $endTotal = [int]$endCountResponse.paging.total

            if (
                $endTotal -eq $expectedTotal -and
                $issues.Count -eq $expectedTotal -and
                $seenKeys.Count -eq $expectedTotal
            ) {
                Write-Host "Stable issue extraction completed: $expectedTotal unique issues." -ForegroundColor Green
                return $issues.ToArray()
            }

            Write-Host (
                "Issue snapshot changed during extraction. " +
                "start=$expectedTotal end=$endTotal rows=$($issues.Count) unique=$($seenKeys.Count)"
            ) -ForegroundColor Yellow
        }

        if ($attempt -lt $MaxExtractionAttempts) {
            Write-Host "Waiting 20 seconds before retrying the complete issue extraction..."
            Start-Sleep -Seconds 20
        }
    }

    throw "Could not obtain a stable paginated OPEN-maintainability issue snapshot after $MaxExtractionAttempts attempts."
}

# ----- Preconditions on the dedicated Sonar repository -----

$status = (& git -C $sonarRepo status --porcelain)
if ($LASTEXITCODE -ne 0) {
    throw "Cannot read Git status of $sonarRepo"
}

if (-not [string]::IsNullOrWhiteSpace(($status -join "`n"))) {
    throw "The Sonar auxiliary repository is not clean. Stop before changing releases."
}

$branch = (& git -C $sonarRepo branch --show-current).Trim()
if ($branch -ne "main") {
    throw "Expected branch 'main' in Sonar repository, found '$branch'."
}

$origin = (& git -C $sonarRepo remote get-url origin).Trim()
if ($origin.TrimEnd("/") -ne $expectedOrigin.TrimEnd("/")) {
    throw "Unexpected Sonar repository origin: $origin"
}

& git -C $sonarRepo fetch origin main --quiet
if ($LASTEXITCODE -ne 0) {
    throw "Could not fetch origin/main."
}

$localHead = (& git -C $sonarRepo rev-parse HEAD).Trim()
$remoteHead = (& git -C $sonarRepo rev-parse origin/main).Trim()

if ($localHead -ne $remoteHead) {
    throw "Local Sonar repository HEAD differs from origin/main. Resolve this before continuing."
}

# ----- Resolve the selected OpenJPA release from the validated inventory -----

$inventory = @(Import-Csv $inventoryPath)
$rows = @(
    $inventory |
        Where-Object { [int]$_.ReleaseIndex -eq $ReleaseIndex }
)

if ($rows.Count -eq 0) {
    throw "No inventory rows found for ReleaseIndex=$ReleaseIndex"
}

$versions = @($rows | Select-Object -ExpandProperty Version -Unique)
$commits = @($rows | Select-Object -ExpandProperty CommitId -Unique)

if ($versions.Count -ne 1) {
    throw "ReleaseIndex=$ReleaseIndex maps to multiple versions."
}
if ($commits.Count -ne 1) {
    throw "ReleaseIndex=$ReleaseIndex maps to multiple snapshot commits."
}

$version = [string]$versions[0]
$snapshotCommit = [string]$commits[0]
$expectedClasses = $rows.Count

& git -C $mainRepo cat-file -e "$snapshotCommit^{commit}"
if ($LASTEXITCODE -ne 0) {
    throw "Snapshot commit not found in the OpenJPA repository: $snapshotCommit"
}

$outputDir = Join-Path $mainRepo ("isw2\results\sonar\release-{0:D2}-{1}" -f $ReleaseIndex, $version)

Write-Host ""
Write-Host "===== SONAR RELEASE PROCESSOR ====="
Write-Host "Release index        : $ReleaseIndex"
Write-Host "OpenJPA version      : $version"
Write-Host "Snapshot commit      : $snapshotCommit"
Write-Host "Production classes   : $expectedClasses"
Write-Host "Sonar repository     : $sonarRepo"
Write-Host "Sonar project key    : $projectKey"
Write-Host "Output directory     : $outputDir"
Write-Host "==================================="
Write-Host ""

if (-not $ResumeExistingAnalysis -and (Test-Path $stageDir)) {
    throw "Temporary stage worktree already exists: $stageDir"
}

$stageCreated = $false
$releaseCommitted = $false
$pushedRevision = $null

try {
    if ($ResumeExistingAnalysis) {
        Write-Host "RESUME MODE: reusing the current auxiliary repository snapshot and existing Sonar analysis." -ForegroundColor Cyan

        $releaseCommitted = $true
        $pushedRevision = (& git -C $sonarRepo rev-parse HEAD).Trim()

        $sourceMapPath = Join-Path $sonarRepo "source-map.csv"
        if (-not (Test-Path $sourceMapPath)) {
            throw "Resume mode requires source-map.csv in the auxiliary repository."
        }

        $map = @(Import-Csv $sourceMapPath)

        if ($map.Count -ne $expectedClasses) {
            throw "Resume source-map row mismatch. Expected $expectedClasses, found $($map.Count)."
        }

        $resumeReleaseIndices = @($map | Select-Object -ExpandProperty ReleaseIndex -Unique)
        $resumeVersions = @($map | Select-Object -ExpandProperty Version -Unique)
        $resumeCommits = @($map | Select-Object -ExpandProperty CommitId -Unique)

        if (
            $resumeReleaseIndices.Count -ne 1 -or
            [int]$resumeReleaseIndices[0] -ne $ReleaseIndex
        ) {
            throw "Resume source-map does not belong to ReleaseIndex=$ReleaseIndex."
        }

        if (
            $resumeVersions.Count -ne 1 -or
            [string]$resumeVersions[0] -ne $version
        ) {
            throw "Resume source-map version does not match OpenJPA $version."
        }

        if (
            $resumeCommits.Count -ne 1 -or
            [string]$resumeCommits[0] -ne $snapshotCommit
        ) {
            throw "Resume source-map snapshot commit does not match $snapshotCommit."
        }

        $duplicateResumePaths = @(
            $map |
                Group-Object SonarPath |
                Where-Object { $_.Count -gt 1 }
        )

        if ($duplicateResumePaths.Count -gt 0) {
            throw "Resume source-map contains duplicate SonarPath entries."
        }

        $javaFiles = @(
            Get-ChildItem (Join-Path $sonarRepo "src\main\java") -Recurse -Filter *.java
        )

        if ($javaFiles.Count -ne $expectedClasses) {
            throw "Resume Java count mismatch. Expected $expectedClasses, found $($javaFiles.Count)."
        }

        $analysisUrl = "$baseUrl/api/project_analyses/search?project=$([uri]::EscapeDataString($projectKey))&ps=100"
        $analysisResponse = Invoke-SonarGet -Uri $analysisUrl

        $matchedAnalysis = @(
            @($analysisResponse.analyses) |
                Where-Object { [string]$_.revision -eq $pushedRevision }
        ) | Select-Object -First 1

        if ($null -eq $matchedAnalysis) {
            throw "Resume mode could not find a Sonar analysis for current auxiliary HEAD $pushedRevision."
        }

        Write-Host ""
        Write-Host "Resume snapshot validated." -ForegroundColor Green
        Write-Host "Auxiliary HEAD : $pushedRevision"
        Write-Host "Analysis key   : $($matchedAnalysis.key)"
        Write-Host "Analysis date  : $($matchedAnalysis.date)"
        Write-Host "Revision       : $($matchedAnalysis.revision)"
        Write-Host ""
    }
    else {
        # ----- Build the exact production-source snapshot -----

        Write-Host "Creating detached worktree for OpenJPA $version..."
        & git -C $mainRepo worktree add --detach $stageDir $snapshotCommit
        if ($LASTEXITCODE -ne 0) {
            throw "Could not create temporary snapshot worktree."
        }
        $stageCreated = $true

        $srcDir = Join-Path $sonarRepo "src"
        if (Test-Path $srcDir) {
            Remove-Item $srcDir -Recurse -Force
        }

        $sourceMapPath = Join-Path $sonarRepo "source-map.csv"
        if (Test-Path $sourceMapPath) {
            Remove-Item $sourceMapPath -Force
        }

        $map = New-Object System.Collections.Generic.List[object]
        $targetPaths = @{}
        $copied = 0

        foreach ($row in $rows) {
            $original = ([string]$row.Class) -replace '\\','/'

            if ($original -notmatch 'src/main/java/(.+)$') {
                throw "Cannot map production class path: $original"
            }

            $relative = $Matches[1]
            $targetRelative = "src/main/java/$relative"

            if ($targetPaths.ContainsKey($targetRelative)) {
                throw "Target-path collision: $targetRelative"
            }
            $targetPaths[$targetRelative] = $true

            $source = Join-Path $stageDir ($original -replace '/', '\')
            $target = Join-Path $sonarRepo ($targetRelative -replace '/', '\')

            if (-not (Test-Path $source)) {
                throw "Source file missing from snapshot: $source"
            }

            New-Item -ItemType Directory -Force (Split-Path -Parent $target) | Out-Null
            Copy-Item -LiteralPath $source -Destination $target

            $sourceHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $source).Hash
            $targetHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $target).Hash

            if ($sourceHash -ne $targetHash) {
                throw "Byte-for-byte copy validation failed for $original"
            }

            $map.Add([PSCustomObject]@{
                ReleaseIndex = $ReleaseIndex
                Version = $version
                CommitId = $snapshotCommit
                OriginalClassPath = $original
                SonarPath = $targetRelative
            })

            $copied++
            if (($copied % 100) -eq 0) {
                Write-Host "Copied $copied / $expectedClasses"
            }
        }

        if ($map.Count -ne $expectedClasses) {
            throw "Source-map count mismatch. Expected $expectedClasses, found $($map.Count)."
        }

        $map |
            Export-Csv $sourceMapPath -NoTypeInformation -Encoding UTF8

        # Minimal Maven descriptor used only to make Automatic Analysis recognize
        # the extracted source tree as a Maven Java project.
        #
        # Deliberately avoid a PowerShell here-string here. In the script's
        # resume/normal branching this block is nested, and a here-string
        # terminator must start at column 1 in Windows PowerShell 5.1.
        $pomLines = @(
            '<?xml version="1.0" encoding="UTF-8"?>',
            '<project xmlns="http://maven.apache.org/POM/4.0.0"',
            '         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"',
            '         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">',
            '  <modelVersion>4.0.0</modelVersion>',
            '',
            '  <groupId>isw2.analysis</groupId>',
            '  <artifactId>openjpa-sonar-pilot</artifactId>',
            "  <version>$version</version>",
            '  <packaging>jar</packaging>',
            '',
            "  <name>OpenJPA $version - ISW2 Sonar analysis snapshot</name>",
            '',
            '  <properties>',
            '    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>',
            '    <maven.compiler.source>1.5</maven.compiler.source>',
            '    <maven.compiler.target>1.5</maven.compiler.target>',
            '  </properties>',
            '</project>'
        )
        $pom = $pomLines -join [Environment]::NewLine

        $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
        [System.IO.File]::WriteAllText(
            (Join-Path $sonarRepo "pom.xml"),
            $pom,
            $utf8NoBom
        )

        # Refresh the historical Apache license when the snapshot contains it.
        $licenseCandidates = @(
            "LICENSE",
            "LICENSE.txt",
            "openjpa-project\LICENSE.txt",
            "openjpa-project\LICENSE"
        )

        $licenseSource = $null
        foreach ($candidate in $licenseCandidates) {
            $candidatePath = Join-Path $stageDir $candidate
            if (Test-Path $candidatePath) {
                $licenseSource = $candidatePath
                break
            }
        }

        if ($null -ne $licenseSource) {
            Copy-Item -LiteralPath $licenseSource -Destination (Join-Path $sonarRepo "LICENSE.txt") -Force
        }

        $javaFiles = @(
            Get-ChildItem (Join-Path $sonarRepo "src\main\java") -Recurse -Filter *.java
        )

        if ($javaFiles.Count -ne $expectedClasses) {
            throw "Generated Java count mismatch. Expected $expectedClasses, found $($javaFiles.Count)."
        }

        $totalBytes = ($javaFiles | Measure-Object -Property Length -Sum).Sum
        Write-Host "Generated $($javaFiles.Count) Java files ($("{0:N2}" -f ($totalBytes / 1MB)) MB)."

        # ----- Commit and push the exact release snapshot -----

        & git -C $sonarRepo add -A
        if ($LASTEXITCODE -ne 0) {
            throw "git add failed."
        }

        $staged = (& git -C $sonarRepo diff --cached --name-only)
        if ([string]::IsNullOrWhiteSpace(($staged -join "`n"))) {
            throw "No repository changes detected for OpenJPA $version."
        }

        & git -C $sonarRepo commit -m "chore: analyze OpenJPA $version with Sonar"
        if ($LASTEXITCODE -ne 0) {
            throw "git commit failed."
        }

        $releaseCommitted = $true
        $pushedRevision = (& git -C $sonarRepo rev-parse HEAD).Trim()

        Write-Host ""
        Write-Host "Auxiliary commit : $pushedRevision"
        Write-Host "Pushing main..."

        & git -C $sonarRepo push origin main
        if ($LASTEXITCODE -ne 0) {
            throw "git push failed. The local auxiliary commit was kept so it can be inspected/retried."
        }

        # ----- Wait until Sonar confirms that exact auxiliary Git revision -----

        Write-Host ""
        Write-Host "Waiting for SonarQube Cloud to analyze revision $pushedRevision ..."
        Write-Host "Polling timeout: $pollTimeoutMinutes minutes."

        $deadline = (Get-Date).AddMinutes($pollTimeoutMinutes)
        $matchedAnalysis = $null
        $lastPrintedRevision = $null
        $lastPrintedDate = $null

        while ((Get-Date) -lt $deadline) {
            # Search several recent analyses instead of only the newest one.
            # This still requires an exact revision match, but is resilient if
            # another analysis appears while we are polling.
            $analysisUrl = "$baseUrl/api/project_analyses/search?project=$([uri]::EscapeDataString($projectKey))&ps=10"
            $analysisResponse = Invoke-SonarGet -Uri $analysisUrl
            $analyses = @($analysisResponse.analyses)

            $matchedAnalysis = @(
                $analyses |
                    Where-Object { [string]$_.revision -eq $pushedRevision }
            ) | Select-Object -First 1

            if ($null -ne $matchedAnalysis) {
                break
            }

            $latest = $analyses | Select-Object -First 1
            if ($null -ne $latest) {
                $currentRevision = [string]$latest.revision
                $currentDate = [string]$latest.date

                if (
                    $currentRevision -ne $lastPrintedRevision -or
                    $currentDate -ne $lastPrintedDate
                ) {
                    Write-Host ("Latest Sonar revision: {0} | {1}" -f $currentRevision, $currentDate)
                    $lastPrintedRevision = $currentRevision
                    $lastPrintedDate = $currentDate
                }
            }
            else {
                Write-Host "No Sonar analysis is visible yet."
            }

            Start-Sleep -Seconds $pollSeconds
        }

        if ($null -eq $matchedAnalysis) {
            Write-Host ""
            Write-Host "No exact Sonar analysis was found before timeout." -ForegroundColor Yellow

            # Best-effort diagnostic only; failure here must not hide the real timeout.
            try {
                $activityUrl = "$baseUrl/api/ce/activity?component=$([uri]::EscapeDataString($projectKey))&ps=5"
                $activity = Invoke-SonarGet -Uri $activityUrl

                Write-Host "Recent Sonar background tasks:"
                @($activity.tasks) |
                    Select-Object id, status, submittedAt, startedAt, executedAt, analysisId |
                    Format-Table -AutoSize
            }
            catch {
                Write-Host "Could not retrieve Sonar background-task diagnostics."
            }

            throw "Timed out waiting for Sonar to analyze exact auxiliary revision $pushedRevision."
        }

        Write-Host ""
        Write-Host "Sonar analysis matched exact pushed revision." -ForegroundColor Green
        Write-Host "Analysis key  : $($matchedAnalysis.key)"
        Write-Host "Analysis date : $($matchedAnalysis.date)"
        Write-Host "Revision      : $($matchedAnalysis.revision)"
        Write-Host ""

    }

    # ----- Retrieve a stable OPEN-maintainability issue snapshot -----

    # The project analysis may be visible before the issue search index has
    # fully settled. Retrieve only after the filtered total stabilizes, and
    # restart the whole pagination pass if Sonar changes the total mid-stream.
    $allStatusCountUrl = "$baseUrl/api/issues/search?projects=$([uri]::EscapeDataString($projectKey))&p=1&ps=1"
    $allStatusCountResponse = Invoke-SonarGet -Uri $allStatusCountUrl
    $totalIssuesAllStatuses = [int]$allStatusCountResponse.paging.total

    $allIssues = @(
        Get-StableOpenMaintainabilityIssues
    )

    $total = $allIssues.Count

    $nonOpenRetrievedIssues = @(
        $allIssues |
            Where-Object { [string]$_.issueStatus -ne "OPEN" }
    )

    $nonMaintainabilityRetrievedIssues = @(
        $allIssues |
            Where-Object { -not (Test-MaintainabilityImpact $_) }
    )

    if ($nonOpenRetrievedIssues.Count -gt 0) {
        throw "Sonar returned $($nonOpenRetrievedIssues.Count) non-OPEN issues despite issueStatuses=OPEN."
    }

    if ($nonMaintainabilityRetrievedIssues.Count -gt 0) {
        throw "Sonar returned $($nonMaintainabilityRetrievedIssues.Count) non-maintainability issues despite the maintainability filter."
    }

    $codeSmells = @(
        $allIssues |
            Where-Object {
                $_.type -eq "CODE_SMELL" -and
                $_.issueStatus -eq "OPEN"
            }
    )

    $maintainabilityIssues = @(
        $allIssues |
            Where-Object { Test-MaintainabilityImpact $_ }
    )

    $codeSmellMaintainability = @(
        $codeSmells |
            Where-Object { Test-MaintainabilityImpact $_ }
    )

    $codeSmellWithoutMaintainability = @(
        $codeSmells |
            Where-Object { -not (Test-MaintainabilityImpact $_) }
    )

    $maintainabilityWithoutCodeSmell = @(
        $maintainabilityIssues |
            Where-Object { $_.type -ne "CODE_SMELL" }
    )

    Write-Host ""
    Write-Host "===== ISSUE CLASSIFICATION ====="
    Write-Host "All statuses (metadata only)        : $totalIssuesAllStatuses"
    Write-Host "Stable OPEN maintainability         : $($allIssues.Count)"
    Write-Host "OPEN type = CODE_SMELL              : $($codeSmells.Count)"
    Write-Host "OPEN maintainability issues         : $($maintainabilityIssues.Count)"
    Write-Host "CODE_SMELL + maintainability        : $($codeSmellMaintainability.Count)"
    Write-Host "CODE_SMELL without maintainability  : $($codeSmellWithoutMaintainability.Count)"
    Write-Host "Maintainability without CODE_SMELL  : $($maintainabilityWithoutCodeSmell.Count)"
    Write-Host "Non-OPEN retrieved                  : $($nonOpenRetrievedIssues.Count)"
    Write-Host "Non-maintainability retrieved       : $($nonMaintainabilityRetrievedIssues.Count)"
    Write-Host "===================================="
    Write-Host ""

    # ----- Map CODE_SMELL issues back to the validated OpenJPA inventory -----

    $mapBySonarPath = @{}
    foreach ($mapped in $map) {
        $mapBySonarPath[([string]$mapped.SonarPath -replace '\\','/')] = $mapped
    }

    $evidence = New-Object System.Collections.Generic.List[object]
    $unmatched = New-Object System.Collections.Generic.List[object]
    $nonJava = New-Object System.Collections.Generic.List[object]

    foreach ($issue in $codeSmells) {
        $component = [string]$issue.component
        $prefix = "${projectKey}:"

        if (-not $component.StartsWith($prefix)) {
            $unmatched.Add([PSCustomObject]@{
                IssueKey = $issue.key
                Rule = $issue.rule
                Component = $component
                Reason = "Unexpected component prefix"
            })
            continue
        }

        $sonarPath = $component.Substring($prefix.Length) -replace '\\','/'

        if (-not $sonarPath.EndsWith(".java", [System.StringComparison]::OrdinalIgnoreCase)) {
            $nonJava.Add([PSCustomObject]@{
                IssueKey = $issue.key
                Rule = $issue.rule
                SonarPath = $sonarPath
            })
            continue
        }

        if (-not $mapBySonarPath.ContainsKey($sonarPath)) {
            $unmatched.Add([PSCustomObject]@{
                IssueKey = $issue.key
                Rule = $issue.rule
                Component = $component
                Reason = "Java component not present in source-map.csv"
            })
            continue
        }

        $mapped = $mapBySonarPath[$sonarPath]

        $evidence.Add([PSCustomObject]@{
            ReleaseIndex = $mapped.ReleaseIndex
            Version = $mapped.Version
            CommitId = $mapped.CommitId
            SonarAnalysisKey = $matchedAnalysis.key
            SonarRevision = $matchedAnalysis.revision
            Class = $mapped.OriginalClassPath
            SonarPath = $sonarPath
            IssueKey = $issue.key
            Rule = $issue.rule
            Type = $issue.type
            IssueStatus = $issue.issueStatus
            Line = $issue.line
            Message = $issue.message
        })
    }

    $countsByClass = @{}
    foreach ($item in $evidence) {
        $classPath = [string]$item.Class

        if (-not $countsByClass.ContainsKey($classPath)) {
            $countsByClass[$classPath] = 0
        }

        $countsByClass[$classPath]++
    }

    $metrics = @(
        foreach ($mapped in $map) {
            $classPath = [string]$mapped.OriginalClassPath
            $count = 0

            if ($countsByClass.ContainsKey($classPath)) {
                $count = [int]$countsByClass[$classPath]
            }

            [PSCustomObject]@{
                ReleaseIndex = $mapped.ReleaseIndex
                Version = $mapped.Version
                CommitId = $mapped.CommitId
                SonarAnalysisKey = $matchedAnalysis.key
                SonarRevision = $matchedAnalysis.revision
                Class = $classPath
                NSmells = $count
            }
        }
    )

    $duplicateMetricKeys = @(
        $metrics |
            Group-Object ReleaseIndex, Class |
            Where-Object { $_.Count -gt 1 }
    )

    $negativeCounts = @(
        $metrics |
            Where-Object { [int]$_.NSmells -lt 0 }
    )

    $nonOpenEvidence = @(
        $evidence |
            Where-Object { [string]$_.IssueStatus -ne "OPEN" }
    )

    $sumNSmells = ($metrics | Measure-Object -Property NSmells -Sum).Sum
    if ($null -eq $sumNSmells) {
        $sumNSmells = 0
    }

    $classesWithSmells = @(
        $metrics |
            Where-Object { [int]$_.NSmells -gt 0 }
    ).Count

    $classesWithoutSmells = $metrics.Count - $classesWithSmells

    $validationPassed = (
        $metrics.Count -eq $expectedClasses -and
        $duplicateMetricKeys.Count -eq 0 -and
        $negativeCounts.Count -eq 0 -and
        $nonOpenEvidence.Count -eq 0 -and
        $nonOpenRetrievedIssues.Count -eq 0 -and
        $nonMaintainabilityRetrievedIssues.Count -eq 0 -and
        $unmatched.Count -eq 0 -and
        $nonJava.Count -eq 0 -and
        [int]$sumNSmells -eq $evidence.Count -and
        $evidence.Count -eq $codeSmells.Count -and
        [string]$matchedAnalysis.revision -eq $pushedRevision
    )

    New-Item -ItemType Directory -Force $outputDir | Out-Null

    $metricsPath = Join-Path $outputDir "smell_metrics.csv"
    $evidencePath = Join-Path $outputDir "sonar_smell_evidence.csv"
    $summaryPath = Join-Path $outputDir "summary.txt"

    $metrics | Export-Csv $metricsPath -NoTypeInformation -Encoding UTF8
    $evidence | Export-Csv $evidencePath -NoTypeInformation -Encoding UTF8

    if ($unmatched.Count -gt 0) {
        $unmatched |
            Export-Csv (Join-Path $outputDir "unmatched_sonar_issues.csv") -NoTypeInformation -Encoding UTF8
    }

    if ($nonJava.Count -gt 0) {
        $nonJava |
            Export-Csv (Join-Path $outputDir "non_java_code_smells.csv") -NoTypeInformation -Encoding UTF8
    }

    $summaryLines = @(
        "OpenJPA Sonar release extraction",
        "ReleaseIndex=$ReleaseIndex",
        "Version=$version",
        "SnapshotCommit=$snapshotCommit",
        "SonarProjectKey=$projectKey",
        "SonarAnalysisKey=$($matchedAnalysis.key)",
        "SonarAnalysisDate=$($matchedAnalysis.date)",
        "SonarRevision=$($matchedAnalysis.revision)",
        "IssueQueryStatus=OPEN",
        "IssueQuerySoftwareQuality=MAINTAINABILITY",
        "TotalIssuesAllStatuses=$totalIssuesAllStatuses",
        "OpenMaintainabilityIssues=$($allIssues.Count)",
        "OpenCodeSmellIssues=$($codeSmells.Count)",
        "CodeSmellMaintainability=$($codeSmellMaintainability.Count)",
        "CodeSmellWithoutMaintainability=$($codeSmellWithoutMaintainability.Count)",
        "MaintainabilityWithoutCodeSmell=$($maintainabilityWithoutCodeSmell.Count)",
        "MetricRows=$($metrics.Count)",
        "EvidenceRows=$($evidence.Count)",
        "SumNSmells=$sumNSmells",
        "ClassesWithSmells=$classesWithSmells",
        "ClassesWithoutSmells=$classesWithoutSmells",
        "UnmatchedJavaCodeSmells=$($unmatched.Count)",
        "NonJavaCodeSmells=$($nonJava.Count)",
        "DuplicateMetricKeys=$($duplicateMetricKeys.Count)",
        "NegativeCounts=$($negativeCounts.Count)",
        "NonOpenEvidence=$($nonOpenEvidence.Count)",
        "NonOpenRetrievedIssues=$($nonOpenRetrievedIssues.Count)",
        "NonMaintainabilityRetrievedIssues=$($nonMaintainabilityRetrievedIssues.Count)",
        "ValidationPassed=$validationPassed"
    )

    [System.IO.File]::WriteAllLines(
        $summaryPath,
        $summaryLines,
        (New-Object System.Text.UTF8Encoding($false))
    )

    Write-Host ""
    Write-Host "===== RELEASE VALIDATION ====="
    Write-Host "Release                  : $ReleaseIndex / $version"
    Write-Host "Snapshot commit          : $snapshotCommit"
    Write-Host "Sonar revision           : $($matchedAnalysis.revision)"
    Write-Host "Metric rows              : $($metrics.Count)"
    Write-Host "Evidence rows            : $($evidence.Count)"
    Write-Host "Sum(NSmells)             : $sumNSmells"
    Write-Host "Classes with smells      : $classesWithSmells"
    Write-Host "Classes with NSmells=0   : $classesWithoutSmells"
    Write-Host "Unmatched Java smells    : $($unmatched.Count)"
    Write-Host "Non-Java code smells     : $($nonJava.Count)"
    Write-Host "Duplicate metric keys    : $($duplicateMetricKeys.Count)"
    Write-Host "Negative NSmells         : $($negativeCounts.Count)"
    Write-Host "Non-OPEN evidence        : $($nonOpenEvidence.Count)"
    Write-Host "Non-OPEN retrieved       : $($nonOpenRetrievedIssues.Count)"
    Write-Host "Validation passed        : $validationPassed"
    Write-Host "=============================="
    Write-Host ""

    if (-not $validationPassed) {
        throw "Release validation failed. Inspect $outputDir"
    }

    Write-Host "SONAR RELEASE $ReleaseIndex ($version): SUCCESS" -ForegroundColor Green
}
catch {
    # If generation failed before commit, restore the dedicated auxiliary
    # repository to its previously committed state.
    if (-not $releaseCommitted) {
        & git -C $sonarRepo reset --hard HEAD | Out-Null
        & git -C $sonarRepo clean -fd | Out-Null
    }

    throw
}
finally {
    if ($stageCreated -and (Test-Path $stageDir)) {
        Write-Host "Removing temporary OpenJPA worktree..."
        & git -C $mainRepo worktree remove --force $stageDir | Out-Null
        & git -C $mainRepo worktree prune | Out-Null
    }
}
