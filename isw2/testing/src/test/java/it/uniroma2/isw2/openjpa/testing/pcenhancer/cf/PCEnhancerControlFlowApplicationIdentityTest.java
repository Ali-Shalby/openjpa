/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package it.uniroma2.isw2.openjpa.testing.pcenhancer.cf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.meta.ClassMetaData;
import org.junit.jupiter.api.Test;

/**
 * Coverage-guided control-flow test for the application-identity
 * enhancement cluster of PCEnhancer.
 *
 * <p>This test belongs to T_CF. The scenario was selected after the
 * frozen T_BB suite from the JaCoCo coverage-gap analysis.</p>
 */
class PCEnhancerControlFlowApplicationIdentityTest {

    /**
     * TCF-001
     *
     * Exercises the application-identity enhancement path using a
     * composite IdClass target.
     *
     * The purpose is to reach the cluster responsible for generating:
     *
     * - pcCopyKeyFieldsToObjectId(...)
     * - pcCopyKeyFieldsFromObjectId(...)
     * - pcNewObjectIdInstance(...)
     */
    @Test
    void tcf001CompositeApplicationIdentityGeneratesIdentitySupportMethods() {

        OpenJPAConfigurationImpl configuration =
                newConfiguration();

        try {

            PCEnhancer enhancer =
                    new PCEnhancer(
                            configuration,
                            PCEnhancerControlFlowApplicationIdentityTarget.class);

            ClassMetaData metadata =
                    enhancer.getMetaData();

            assertNotNull(metadata);

            assertEquals(
                    ClassMetaData.ID_APPLICATION,
                    metadata.getIdentityType());

            assertEquals(
                    PCEnhancerControlFlowApplicationIdentityTarget
                            .ApplicationIdentityId.class,
                    metadata.getObjectIdType());

            assertEquals(
                    PCEnhancer.ENHANCE_PC,
                    enhancer.run());

            long copyToObjectId =
                    enhancer
                            .getPCBytecode()
                            .getClassNode()
                            .methods
                            .stream()
                            .filter(method ->
                                    method.name.equals(
                                            "pcCopyKeyFieldsToObjectId"))
                            .count();

            long copyFromObjectId =
                    enhancer
                            .getPCBytecode()
                            .getClassNode()
                            .methods
                            .stream()
                            .filter(method ->
                                    method.name.equals(
                                            "pcCopyKeyFieldsFromObjectId"))
                            .count();

            long newObjectIdInstance =
                    enhancer
                            .getPCBytecode()
                            .getClassNode()
                            .methods
                            .stream()
                            .filter(method ->
                                    method.name.equals(
                                            "pcNewObjectIdInstance"))
                            .count();

            assertEquals(
                    2L,
                    copyToObjectId);

            assertEquals(
                    2L,
                    copyFromObjectId);

            assertEquals(
                    2L,
                    newObjectIdInstance);

        } finally {
            configuration.close();
        }
    }

    private static OpenJPAConfigurationImpl newConfiguration() {

        OpenJPAConfigurationImpl configuration =
                new OpenJPAConfigurationImpl();

        configuration.setLog(
                "File=stdout, DefaultLevel=WARN");

        configuration.setMetaDataFactory(
                "jpa");

        return configuration;
    }
}
