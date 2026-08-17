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

package it.uniroma2.isw2.openjpa.defect;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;

public class JiraDefectClient {

    private static final String SEARCH_URL =
            "https://issues.apache.org/jira/rest/api/2/search";

    private static final String JQL =
            "project = OPENJPA "
                    + "AND issuetype = Bug "
                    + "AND status IN (Closed, Resolved) "
                    + "AND resolution = Fixed "
                    + "ORDER BY created ASC, key ASC";

    private static final String FIELDS =
            "summary,status,resolution,created,resolutiondate,"
                    + "versions,fixVersions";

    private static final int PAGE_SIZE = 100;

    private static final DateTimeFormatter JIRA_DATE_FORMATTER =
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                    .optionalStart()
                    .appendPattern(".SSS")
                    .optionalEnd()
                    .appendOffset("+HHmm", "Z")
                    .toFormatter();

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public JiraDefectClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public List<DefectTicket> fetchFixedBugTickets()
            throws IOException, InterruptedException {

        List<DefectTicket> tickets = new ArrayList<>();

        int startAt = 0;
        int total;

        do {
            JsonNode root = fetchPage(startAt);

            total = root.path("total").asInt();

            JsonNode issues = root.path("issues");

            if (!issues.isArray()) {
                throw new IOException(
                        "Unexpected JIRA response: issues array not found."
                );
            }

            for (JsonNode issue : issues) {
                tickets.add(parseTicket(issue));
            }

            startAt += issues.size();

            if (issues.isEmpty() && startAt < total) {
                throw new IOException(
                        "JIRA pagination stopped before all issues "
                                + "were retrieved."
                );
            }

        } while (startAt < total);

        return tickets;
    }

    private JsonNode fetchPage(int startAt)
            throws IOException, InterruptedException {

        String url = SEARCH_URL
                + "?jql=" + encode(JQL)
                + "&startAt=" + startAt
                + "&maxResults=" + PAGE_SIZE
                + "&fields=" + encode(FIELDS);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new IOException(
                    "JIRA search request failed. HTTP status: "
                            + response.statusCode()
                            + ". Response: "
                            + response.body()
            );
        }

        return objectMapper.readTree(response.body());
    }

    private DefectTicket parseTicket(JsonNode issue)
            throws IOException {

        String issueId = requiredText(issue, "id");
        String issueKey = requiredText(issue, "key");

        JsonNode fields = issue.path("fields");

        if (fields.isMissingNode() || fields.isNull()) {
            throw new IOException(
                    "Missing fields for JIRA issue " + issueKey
            );
        }

        String summary = fields.path("summary").asText("");

        String status = fields
                .path("status")
                .path("name")
                .asText("");

        String resolution = fields
                .path("resolution")
                .path("name")
                .asText("");

        OffsetDateTime createdDate =
                parseDate(
                        fields.path("created").asText(null),
                        issueKey,
                        "created"
                );

        OffsetDateTime resolutionDate =
                parseNullableDate(
                        fields.path("resolutiondate").asText(null),
                        issueKey,
                        "resolutiondate"
                );

        List<String> affectedVersions =
                extractVersionNames(fields.path("versions"));

        List<String> fixVersions =
                extractVersionNames(fields.path("fixVersions"));

        return new DefectTicket(
                issueId,
                issueKey,
                summary,
                status,
                resolution,
                createdDate,
                resolutionDate,
                List.copyOf(affectedVersions),
                List.copyOf(fixVersions)
        );
    }

    private static List<String> extractVersionNames(JsonNode versionsNode) {

        List<String> versions = new ArrayList<>();

        if (!versionsNode.isArray()) {
            return versions;
        }

        for (JsonNode version : versionsNode) {
            String name = version.path("name").asText("");

            if (!name.isBlank()) {
                versions.add(name);
            }
        }

        return versions;
    }

    private static OffsetDateTime parseDate(
            String value,
            String issueKey,
            String field
    ) throws IOException {

        OffsetDateTime parsed =
                parseNullableDate(value, issueKey, field);

        if (parsed == null) {
            throw new IOException(
                    "Missing required JIRA field "
                            + field
                            + " for "
                            + issueKey
            );
        }

        return parsed;
    }

    private static OffsetDateTime parseNullableDate(
            String value,
            String issueKey,
            String field
    ) throws IOException {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return OffsetDateTime.parse(
                    value,
                    JIRA_DATE_FORMATTER
            );
        } catch (RuntimeException exception) {
            throw new IOException(
                    "Unable to parse JIRA field "
                            + field
                            + " for "
                            + issueKey
                            + ": "
                            + value,
                    exception
            );
        }
    }

    private static String requiredText(
            JsonNode node,
            String field
    ) throws IOException {

        JsonNode value = node.get(field);

        if (value == null
                || value.isNull()
                || value.asText().isBlank()) {

            throw new IOException(
                    "Missing required JIRA field: " + field
            );
        }

        return value.asText();
    }

    private static String encode(String value) {
        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }
}
