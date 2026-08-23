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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataModes;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.util.asm.ClassNodeTracker;
import org.apache.openjpa.util.asm.EnhancementProject;
import org.junit.jupiter.api.Test;

/**
 * Manual black-box tests for the documented PCEnhancer
 * property-access restriction enforcement.
 *
 * Category Partition frames: TBB-016..TBB-018.
 */
class PCEnhancerBlackBoxPropertyAccessTest {

    /**
     * PROPERTY-access fixture that respects the documented restriction:
     * only getValue() and setValue() directly access the backing field.
     */
    static class CompliantPropertyTarget {

        private String value;

        protected CompliantPropertyTarget() {
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String readValue() {
            return getValue();
        }
    }

    /**
     * PROPERTY-access fixture that deliberately violates the documented
     * restriction: readValueDirectly() accesses the backing field instead
     * of invoking getValue().
     */
    static class ViolatingPropertyTarget {

        private String value;

        protected ViolatingPropertyTarget() {
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String readValueDirectly() {
            return value;
        }
    }

    @Test
    void tbb016CompliantPropertyAccessWithEnforcementEnabledIsAccepted() {

        try (EnhancerContext context =
                     createEnhancer(CompliantPropertyTarget.class, true)) {

            PCEnhancer enhancer = context.enhancer();

            assertTrue(
                    enhancer.getEnforcePropertyRestrictions(),
                    "Property restriction enforcement must be enabled"
            );

            assertEquals(
                    ClassMetaData.ACCESS_PROPERTY,
                    enhancer.getMetaData().getAccessType(),
                    "The fixture must use PROPERTY access"
            );

            int result = enhancer.run();

            assertEquals(
                    PCEnhancer.ENHANCE_PC,
                    result
            );
        }
    }

    @Test
    void tbb017ViolatingPropertyAccessWithEnforcementDisabledIsNotRejected() {

        try (EnhancerContext context =
                     createEnhancer(ViolatingPropertyTarget.class, false)) {

            PCEnhancer enhancer = context.enhancer();

            assertFalse(
                    enhancer.getEnforcePropertyRestrictions(),
                    "Property restriction enforcement must be disabled"
            );

            assertEquals(
                    ClassMetaData.ACCESS_PROPERTY,
                    enhancer.getMetaData().getAccessType(),
                    "The fixture must use PROPERTY access"
            );

            int result = enhancer.run();

            assertEquals(
                    PCEnhancer.ENHANCE_PC,
                    result,
                    "The documented property violation must not reject enhancement when enforcement is disabled"
            );
        }
    }

    @Test
    void tbb018ViolatingPropertyAccessWithEnforcementEnabledIsRejected() {

        try (EnhancerContext context =
                     createEnhancer(ViolatingPropertyTarget.class, true)) {

            PCEnhancer enhancer = context.enhancer();

            assertTrue(
                    enhancer.getEnforcePropertyRestrictions(),
                    "Property restriction enforcement must be enabled"
            );

            assertEquals(
                    ClassMetaData.ACCESS_PROPERTY,
                    enhancer.getMetaData().getAccessType(),
                    "The fixture must use PROPERTY access"
            );

            assertThrows(
                    RuntimeException.class,
                    enhancer::run,
                    "A documented property-access violation must reject enhancement when enforcement is enabled"
            );
        }
    }

    private static EnhancerContext createEnhancer(
            Class<?> type,
            boolean enforcePropertyRestrictions) {

        OpenJPAConfigurationImpl configuration =
                new OpenJPAConfigurationImpl();

        /*
         * Keep OpenJPA warnings visible while routing them to stdout,
         * avoiding PowerShell NativeCommandError output for normal WARNs.
         */
        configuration.setLog(
                "File=stdout, DefaultLevel=WARN"
        );

        configuration.setMetaDataFactory("jpa");

        MetaDataRepository repository =
                configuration.newMetaDataRepositoryInstance();

        repository.setSourceMode(
                MetaDataModes.MODE_META
        );

        /*
         * Explicitly populate metadata using PROPERTY access.
         *
         * The public MetaDataRepository API accepts the access strategy
         * to use when populating metadata. This ensures that getValue()
         * is the persistent backing member rather than the field itself.
         */
        ClassMetaData metaData =
                repository.addMetaData(
                        type,
                        ClassMetaData.ACCESS_PROPERTY
                );

        EnhancementProject project =
                new EnhancementProject();

        ClassNodeTracker typeBytecode =
                project.loadClass(type);

        PCEnhancer enhancer =
                new PCEnhancer(
                        repository,
                        typeBytecode,
                        metaData
                );

        enhancer.setEnforcePropertyRestrictions(
                enforcePropertyRestrictions
        );

        return new EnhancerContext(
                configuration,
                enhancer
        );
    }

    private record EnhancerContext(
            OpenJPAConfigurationImpl configuration,
            PCEnhancer enhancer)
            implements AutoCloseable {

        @Override
        public void close() {
            configuration.close();
        }
    }
}
