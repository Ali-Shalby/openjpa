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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataModes;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.util.asm.ClassNodeTracker;
import org.apache.openjpa.util.asm.EnhancementProject;
import org.apache.xbean.asm9.tree.MethodNode;
import org.junit.jupiter.api.Test;

/**
 * Manual black-box tests for the documented PCEnhancer
 * no-argument constructor policy.
 *
 * Category Partition frames: TBB-008..TBB-011.
 */
class PCEnhancerBlackBoxConstructorTest {

    static class WithNoArgConstructor {

        private String persistentValue;

        protected WithNoArgConstructor() {
        }

        WithNoArgConstructor(String persistentValue) {
            this.persistentValue = persistentValue;
        }
    }

    static class WithoutNoArgConstructor {

        private String persistentValue;

        WithoutNoArgConstructor(String persistentValue) {
            this.persistentValue = persistentValue;
        }
    }

    @Test
    void tbb008ExistingNoArgConstructorWithAdditionEnabledSucceeds() {

        try (EnhancerContext context =
                     createEnhancer(WithNoArgConstructor.class, true)) {

            PCEnhancer enhancer = context.enhancer();

            assertTrue(enhancer.getAddDefaultConstructor());

            assertNotNull(
                    findNoArgConstructor(enhancer),
                    "The fixture must initially contain a no-arg constructor"
            );

            int result = enhancer.run();

            assertEquals(
                    PCEnhancer.ENHANCE_PC,
                    result
            );

            assertNotNull(
                    findNoArgConstructor(enhancer),
                    "The existing no-arg constructor must remain available"
            );
        }
    }

    @Test
    void tbb009ExistingNoArgConstructorWithAdditionDisabledSucceeds() {

        try (EnhancerContext context =
                     createEnhancer(WithNoArgConstructor.class, false)) {

            PCEnhancer enhancer = context.enhancer();

            assertFalse(enhancer.getAddDefaultConstructor());

            assertNotNull(
                    findNoArgConstructor(enhancer),
                    "The fixture must initially contain a no-arg constructor"
            );

            int result = enhancer.run();

            assertEquals(
                    PCEnhancer.ENHANCE_PC,
                    result
            );

            assertNotNull(
                    findNoArgConstructor(enhancer),
                    "Disabling constructor generation must not remove an existing constructor"
            );
        }
    }

    @Test
    void tbb010MissingNoArgConstructorWithAdditionEnabledIsRepaired() {

        try (EnhancerContext context =
                     createEnhancer(WithoutNoArgConstructor.class, true)) {

            PCEnhancer enhancer = context.enhancer();

            assertTrue(enhancer.getAddDefaultConstructor());

            assertNull(
                    findNoArgConstructor(enhancer),
                    "The fixture must initially have no no-arg constructor"
            );

            int result = enhancer.run();

            assertEquals(
                    PCEnhancer.ENHANCE_PC,
                    result
            );

            assertNotNull(
                    findNoArgConstructor(enhancer),
                    "PCEnhancer must add the missing no-arg constructor"
            );
        }
    }

    @Test
    void tbb011MissingNoArgConstructorWithAdditionDisabledIsRejected() {

        try (EnhancerContext context =
                     createEnhancer(WithoutNoArgConstructor.class, false)) {

            PCEnhancer enhancer = context.enhancer();

            assertFalse(enhancer.getAddDefaultConstructor());

            assertNull(
                    findNoArgConstructor(enhancer),
                    "The fixture must initially have no no-arg constructor"
            );

            assertThrows(
                    RuntimeException.class,
                    enhancer::run
            );
        }
    }

    private static EnhancerContext createEnhancer(
            Class<?> type,
            boolean addDefaultConstructor) {

        OpenJPAConfigurationImpl configuration =
                new OpenJPAConfigurationImpl();

        /*
         * Keep OpenJPA warnings visible, but route them to stdout.
         * This prevents PowerShell from presenting normal WARN messages
         * as NativeCommandError entries.
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

        ClassMetaData metaData =
                repository.addMetaData(type);

        metaData.addDeclaredField(
                "persistentValue",
                String.class
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

        enhancer.setAddDefaultConstructor(
                addDefaultConstructor
        );

        return new EnhancerContext(
                configuration,
                enhancer
        );
    }

    private static MethodNode findNoArgConstructor(
            PCEnhancer enhancer) {

        return enhancer
                .getPCBytecode()
                .getClassNode()
                .methods
                .stream()
                .filter(method ->
                        "<init>".equals(method.name)
                                && "()V".equals(method.desc))
                .findFirst()
                .orElse(null);
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
