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

package it.uniroma2.isw2.openjpa.inventory;

import java.nio.file.Path;
import java.util.Locale;

public final class JavaClassScopeClassifier {

    private JavaClassScopeClassifier() {
        // Utility class.
    }

    public static ClassScope classify(String filePath) {

        String path =
                filePath
                        .replace('\\', '/')
                        .toLowerCase(Locale.ROOT);

        String fileName =
                Path.of(path)
                        .getFileName()
                        .toString();

        if (fileName.equals("package-info.java")
                || fileName.equals("module-info.java")) {

            return ClassScope.NON_CLASS;
        }

        String normalized =
                "/" + path + "/";

        if (normalized.contains("/src/test/")
                || normalized.contains("/src/test-java/")
                || normalized.contains("/src/it/")
                || normalized.contains("/src/itests/")
                || normalized.contains("/itests/")
                || normalized.contains("-itests/")
                || normalized.contains("/tests/")
                || normalized.contains("-tests/")) {

            return ClassScope.TEST;
        }

        if (normalized.contains("/examples/")
                || normalized.contains("/example/")
                || normalized.contains("/openjpa-examples/")) {

            return ClassScope.EXAMPLE;
        }

        if (normalized.contains("/generated-sources/")
                || normalized.contains("/src/generated/")
                || normalized.contains("/generated/")) {

            return ClassScope.GENERATED;
        }

        if (normalized.contains("/src/main/jjtree/")
                || normalized.contains("/src/main/javacc/")) {

            return ClassScope.PARSER_SOURCE;
        }

        if (normalized.contains("/src/main/java/")
                || normalized.contains("/src/java/")) {

            return ClassScope.PRODUCTION;
        }

        return ClassScope.OTHER;
    }
}
