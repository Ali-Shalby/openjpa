/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.uniroma2.isw2.openjpa.testing.pcenhancer.bb;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;

import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.lib.util.Options;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Manual black-box test for invalid PCEnhancer tool options.
 *
 * Category Partition frame: TBB-026.
 */
class PCEnhancerBlackBoxInvalidOptionsTest {

    @Test
    void tbb026UnknownOptionIsRejected(
            @TempDir Path outputDirectory) throws Exception {

        OpenJPAConfigurationImpl configuration =
                new OpenJPAConfigurationImpl();

        try {
            configuration.setLog(
                    "File=stdout, DefaultLevel=WARN"
            );

            configuration.setMetaDataFactory("jpa");

            Options options =
                    new Options();

            /*
             * Valid support options keep the run isolated and prevent
             * modification of the original fixture bytecode.
             */
            options.setProperty(
                    "directory",
                    outputDirectory.toString()
            );

            options.setProperty(
                    "tmpClassLoader",
                    "false"
            );

            /*
             * Frozen representative of the invalid-option partition.
             *
             * This option is deliberately not part of PCEnhancer.Flags
             * and is not a bean property of OpenJPAConfiguration.
             */
            options.setProperty(
                    "definitelyNotAValidOpenJPAOption",
                    "x"
            );

            boolean result =
                    PCEnhancer.run(
                            configuration,
                            new String[]{
                                    PCEnhancerBlackBoxInvalidOptionsTarget.class
                                            .getName()
                            },
                            options
                    );

            assertFalse(
                    result,
                    "The public PCEnhancer.run(..., Options) contract states "
                            + "that invalid options must return false"
            );
        } finally {
            configuration.close();
        }
    }
}

/**
 * Disposable purpose-built target for TBB-026.
 */
class PCEnhancerBlackBoxInvalidOptionsTarget {

    private String value;

    protected PCEnhancerBlackBoxInvalidOptionsTarget() {
    }
}
