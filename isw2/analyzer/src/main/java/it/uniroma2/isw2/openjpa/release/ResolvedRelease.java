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