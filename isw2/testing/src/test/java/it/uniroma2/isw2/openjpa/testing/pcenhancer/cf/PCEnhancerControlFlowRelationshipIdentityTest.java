/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.uniroma2.isw2.openjpa.testing.pcenhancer.cf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.junit.jupiter.api.Test;

/**
 * Coverage-guided control-flow test for application identity containing
 * a relationship-valued primary-key field.
 *
 * <p>The scenario was selected from the post-TCF-003 coverage-gap analysis.
 * A dedicated feasibility preflight confirmed that it reaches
 * addExtractObjectIdFieldValueCode.</p>
 */
class PCEnhancerControlFlowRelationshipIdentityTest {

    /**
     * TCF-004
     *
     * Exercises application-identity enhancement when one primary-key
     * attribute is itself a persistent relationship.
     */
    @Test
    void tcf004RelationshipValuedPrimaryKeyGeneratesIdentitySupport() {

        OpenJPAConfigurationImpl configuration =
                newConfiguration();

        try {

            PCEnhancer enhancer =
                    new PCEnhancer(
                            configuration,
                            PCEnhancerControlFlowRelationshipIdentityTarget.class);

            ClassMetaData metadata =
                    enhancer.getMetaData();

            assertNotNull(metadata);

            assertEquals(
                    ClassMetaData.ID_APPLICATION,
                    metadata.getIdentityType());

            assertFalse(
                    metadata.isOpenJPAIdentity());

            assertEquals(
                    PCEnhancerControlFlowRelationshipIdentityTarget
                            .RelationshipIdentityId.class,
                    metadata.getObjectIdType());

            FieldMetaData[] primaryKeyFields =
                    metadata.getPrimaryKeyFields();

            assertEquals(
                    2,
                    primaryKeyFields.length);

            FieldMetaData relationshipField =
                    findPrimaryKeyField(
                            primaryKeyFields,
                            "parent");

            assertNotNull(
                    relationshipField);

            assertEquals(
                    JavaTypes.PC,
                    relationshipField.getDeclaredTypeCode());

            assertEquals(
                    PCEnhancerControlFlowRelationshipIdentityParentTarget.class,
                    relationshipField.getDeclaredType());

            assertEquals(
                    long.class,
                    relationshipField.getObjectIdFieldType());

            assertNotNull(
                    relationshipField.getTypeMetaData());

            assertTrue(
                    relationshipField
                            .getTypeMetaData()
                            .isOpenJPAIdentity());

            assertFalse(
                    enhancer.getCreateSubclass());

            assertFalse(
                    enhancer.getRedefine());

            assertEquals(
                    PCEnhancer.ENHANCE_PC,
                    enhancer.run());

            long copyToMethods =
                    enhancer
                            .getPCBytecode()
                            .getClassNode()
                            .methods
                            .stream()
                            .filter(method ->
                                    method.name.equals(
                                            "pcCopyKeyFieldsToObjectId"))
                            .count();

            long copyFromMethods =
                    enhancer
                            .getPCBytecode()
                            .getClassNode()
                            .methods
                            .stream()
                            .filter(method ->
                                    method.name.equals(
                                            "pcCopyKeyFieldsFromObjectId"))
                            .count();

            long newObjectIdMethods =
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
                    copyToMethods);

            assertEquals(
                    2L,
                    copyFromMethods);

            assertEquals(
                    2L,
                    newObjectIdMethods);

        } finally {

            configuration.close();
        }
    }

    private static FieldMetaData findPrimaryKeyField(
            FieldMetaData[] fields,
            String name) {

        for (FieldMetaData field : fields) {

            if (field.getName().equals(name)) {
                return field;
            }
        }

        return null;
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
