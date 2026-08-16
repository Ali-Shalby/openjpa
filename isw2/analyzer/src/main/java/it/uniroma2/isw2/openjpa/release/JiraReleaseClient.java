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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JiraReleaseClient {

    private static final String OPENJPA_PROJECT_URL =
            "https://issues.apache.org/jira/rest/api/2/project/OPENJPA";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public JiraReleaseClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public List<ReleaseInfo> fetchReleasedVersions()
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENJPA_PROJECT_URL))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new IOException(
                    "JIRA request failed. HTTP status: "
                            + response.statusCode()
            );
        }

        JsonNode root = objectMapper.readTree(response.body());

        JsonNode versionsNode = root.get("versions");

        if (versionsNode == null || !versionsNode.isArray()) {
            throw new IOException(
                    "Unexpected JIRA response: versions array not found."
            );
        }

        List<ReleaseInfo> releases = new ArrayList<>();

        for (JsonNode versionNode : versionsNode) {

            boolean released =
                    versionNode.path("released").asBoolean(false);

            JsonNode releaseDateNode =
                    versionNode.get("releaseDate");

            if (!released
                    || releaseDateNode == null
                    || releaseDateNode.isNull()) {
                continue;
            }

            String jiraId =
                    versionNode.path("id").asText();

            String version =
                    versionNode.path("name").asText();

            LocalDate releaseDate =
                    LocalDate.parse(
                            releaseDateNode.asText()
                    );

            releases.add(
                    new ReleaseInfo(
                            jiraId,
                            version,
                            releaseDate
                    )
            );
        }

        releases.sort(
                Comparator
                        .comparing(ReleaseInfo::getReleaseDate)
                        .thenComparing(ReleaseInfo::getVersion)
        );

        return releases;
    }
}