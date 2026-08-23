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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataModes;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.ManagedInterface;
import org.apache.openjpa.util.asm.ClassNodeTracker;
import org.apache.openjpa.util.asm.EnhancementProject;
import org.junit.jupiter.api.Test;

/**
 * Manual black-box tests for the documented PCEnhancer
 * enhancement outcome according to target state.
 *
 * Category Partition frames: TBB-012..TBB-015.
 */
class PCEnhancerBlackBoxOutcomeTest {

    static class PersistentTarget {

        private String persistentValue;

        protected PersistentTarget() {
        }
    }

    static class PersistenceAwareTarget {

        protected PersistenceAwareTarget() {
        }
    }

    abstract static class AlreadyPersistenceCapable
            implements PersistenceCapable {
    }

    @ManagedInterface
    interface ManagedContract {

        String getName();

        void setName(String name);
    }

    @Test
    void tbb012TargetWithMetadataIsEnhancedAsPersistenceCapable() {

        try (EnhancerContext context =
                     createEnhancer(PersistentTarget.class, true)) {

            PCEnhancer enhancer = context.enhancer();

            assertNotNull(
                    enhancer.getMetaData(),
                    "The fixture must have persistence metadata"
            );

            int result = enhancer.run();

            assertEquals(
                    PCEnhancer.ENHANCE_PC,
                    result
            );
        }
    }

    @Test
    void tbb013TargetWithoutMetadataIsTreatedAsPersistenceAware() {

        try (EnhancerContext context =
                     createEnhancer(PersistenceAwareTarget.class, false)) {

            PCEnhancer enhancer = context.enhancer();

            assertNull(
                    enhancer.getMetaData(),
                    "The fixture must not have persistence metadata"
            );

            int result = enhancer.run();

            assertEquals(
                    PCEnhancer.ENHANCE_AWARE,
                    result
            );
        }
    }

    @Test
    void tbb014AlreadyPersistenceCapableTargetIsNotEnhancedAgain() {

        assertTrue(
                PersistenceCapable.class.isAssignableFrom(
                        AlreadyPersistenceCapable.class
                ),
                "The fixture must explicitly implement PersistenceCapable"
        );

        try (EnhancerContext context =
                     createEnhancer(
                             AlreadyPersistenceCapable.class,
                             false
                     )) {

            PCEnhancer enhancer = context.enhancer();

            int result = enhancer.run();

            assertEquals(
                    PCEnhancer.ENHANCE_NONE,
                    result
            );
        }
    }

    @Test
    void tbb015ManagedInterfaceReturnsEnhanceInterface() {

        assertTrue(
                ManagedContract.class.isInterface(),
                "The fixture must be an interface"
        );

        assertTrue(
                ManagedContract.class.isAnnotationPresent(
                        ManagedInterface.class
                ),
                "The fixture must be declared as an OpenJPA managed interface"
        );

        try (EnhancerContext context =
                     createEnhancer(ManagedContract.class, false)) {

            PCEnhancer enhancer = context.enhancer();

            int result = enhancer.run();

            assertEquals(
                    PCEnhancer.ENHANCE_INTERFACE,
                    result
            );
        }
    }

    private static EnhancerContext createEnhancer(
            Class<?> type,
            boolean withMetaData) {

        OpenJPAConfigurationImpl configuration =
                new OpenJPAConfigurationImpl();

        /*
         * Keep OpenJPA warnings visible, but route them to stdout.
         * This avoids PowerShell converting ordinary WARN messages
         * written to stderr into NativeCommandError output.
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

        ClassMetaData metaData = null;

        if (withMetaData) {

            metaData = repository.addMetaData(type);

            metaData.addDeclaredField(
                    "persistentValue",
                    String.class
            );
        }

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
