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

package it.uniroma2.isw2.openjpa.metrics;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

public class HistoricalMetricsSmokeDiagnostic {

    public static void main(String[] args) {

        if (args.length != 4) {
            throw new IllegalArgumentException(
                    "Usage: <repository-root> "
                            + "<release-commit> "
                            + "<release-date> "
                            + "<file-path>"
            );
        }

        try {

            Path repositoryRoot =
                    Paths.get(args[0]);

            String releaseCommit =
                    args[1];

            LocalDate releaseDate =
                    LocalDate.parse(
                            args[2]
                    );

            String filePath =
                    args[3];

            GitHistoricalMetricsReader reader =
                    new GitHistoricalMetricsReader(
                            repositoryRoot
                    );

            HistoricalMetrics metrics =
                    reader.compute(
                            releaseCommit,
                            releaseDate,
                            filePath
                    );

            System.out.println();
            System.out.println(
                    "===== HISTORICAL METRICS SMOKE TEST ====="
            );

            System.out.println(
                    "NR                    : "
                            + metrics.nr()
            );

            System.out.println(
                    "NAUTH                 : "
                            + metrics.nauth()
            );

            System.out.println(
                    "LOC_TOUCHED           : "
                            + metrics.locTouched()
            );

            System.out.println(
                    "LOC_ADDED             : "
                            + metrics.locAdded()
            );

            System.out.printf(
                    "AVG_LOC_ADDED         : %.6f%n",
                    metrics.avgLocAdded()
            );

            System.out.println(
                    "MAX_LOC_ADDED         : "
                            + metrics.maxLocAdded()
            );

            System.out.println(
                    "CHURN                 : "
                            + metrics.churn()
            );

            System.out.printf(
                    "AVG_CHURN             : %.6f%n",
                    metrics.avgChurn()
            );

            System.out.println(
                    "MAX_CHURN             : "
                            + metrics.maxChurn()
            );

            System.out.println(
                    "CHANGE_SET_SIZE       : "
                            + metrics.changeSetSize()
            );

            System.out.printf(
                    "AVG_CHANGE_SET        : %.6f%n",
                    metrics.avgChangeSet()
            );

            System.out.println(
                    "MAX_CHANGE_SET        : "
                            + metrics.maxChangeSet()
            );

            System.out.printf(
                    "AGE_WEEKS             : %.6f%n",
                    metrics.ageWeeks()
            );

            System.out.printf(
                    "WEIGHTED_AGE_WEEKS    : %.6f%n",
                    metrics.weightedAgeWeeks()
            );

            System.out.println(
                    "IGNORED_ZERO_LOC_REVS : "
                            + metrics.ignoredZeroLocRevisions()
            );

            System.out.println(
                    "========================================="
            );

        } catch (Exception exception) {

            exception.printStackTrace();
            System.exit(1);
        }
    }
}
