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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
 * direct-enhancement and generated-subclass strategies.
 *
 * Category Partition frames: TBB-027..TBB-028.
 */
class PCEnhancerBlackBoxStrategyTest {

    /**
     * Purpose-built fixture valid for both direct enhancement and
     * runtime subclass generation.
     *
     * PROPERTY access is deliberately used with public non-final
     * getter/setter methods so that the fixture satisfies the public
     * subclassing requirements independently of the strategy under test.
     */
    public static class StrategyTarget {

        private String value;

        protected StrategyTarget() {
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Test
    void tbb027DirectEnhancementUsesManagedTypeAsPersistenceCapableType() {

        try (EnhancerContext context =
                     createEnhancer(false, false)) {

            PCEnhancer enhancer = context.enhancer();

            assertFalse(
                    enhancer.getRedefine(),
                    "Direct-enhancement frame requires redefine=false"
            );

            assertFalse(
                    enhancer.getCreateSubclass(),
                    "Direct-enhancement frame requires createSubclass=false"
            );

            String managedTypeBefore =
                    className(
                            enhancer.getManagedTypeBytecode()
                    );

            String pcTypeBefore =
                    className(
                            enhancer.getPCBytecode()
                    );

            assertEquals(
                    managedTypeBefore,
                    pcTypeBefore,
                    "Before direct enhancement, managed and PC bytecode must refer to the same type"
            );

            int result = enhancer.run();

            assertEquals(
                    PCEnhancer.ENHANCE_PC,
                    result
            );

            String managedTypeAfter =
                    className(
                            enhancer.getManagedTypeBytecode()
                    );

            String pcTypeAfter =
                    className(
                            enhancer.getPCBytecode()
                    );

            assertEquals(
                    StrategyTarget.class.getName(),
                    managedTypeAfter
            );

            assertEquals(
                    managedTypeAfter,
                    pcTypeAfter,
                    "Direct enhancement must make the managed type itself persistence-capable"
            );

            assertFalse(
                    PCEnhancer.isPCSubclassName(pcTypeAfter),
                    "Direct enhancement must not replace the target with a generated PC subclass"
            );
        }
    }

    @Test
    void tbb028RedefineAndCreateSubclassGenerateSeparatePersistenceCapableSubclass() {

        try (EnhancerContext context =
                     createEnhancer(true, true)) {

            PCEnhancer enhancer = context.enhancer();

            assertTrue(
                    enhancer.getRedefine(),
                    "Subclass frame requires redefine=true"
            );

            assertTrue(
                    enhancer.getCreateSubclass(),
                    "Subclass frame requires createSubclass=true"
            );

            int result = enhancer.run();

            assertEquals(
                    PCEnhancer.ENHANCE_PC,
                    result
            );

            String managedTypeName =
                    className(
                            enhancer.getManagedTypeBytecode()
                    );

            String pcTypeName =
                    className(
                            enhancer.getPCBytecode()
                    );

            assertEquals(
                    StrategyTarget.class.getName(),
                    managedTypeName,
                    "The managed type must remain identifiable as the original target"
            );

            assertNotEquals(
                    managedTypeName,
                    pcTypeName,
                    "Subclass mode must produce separate persistence-capable bytecode"
            );

            assertTrue(
                    PCEnhancer.isPCSubclassName(pcTypeName),
                    "The generated persistence-capable type must use the public PC-subclass naming contract"
            );

            assertEquals(
                    enhancer.getManagedTypeBytecode()
                            .getClassNode()
                            .name,
                    enhancer.getPCBytecode()
                            .getClassNode()
                            .superName,
                    "The generated persistence-capable bytecode must subclass the managed type"
            );
        }
    }

    private static EnhancerContext createEnhancer(
            boolean redefine,
            boolean createSubclass) {

        OpenJPAConfigurationImpl configuration =
                new OpenJPAConfigurationImpl();

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
         * PROPERTY access gives the subclass validator a conventional,
         * externally meaningful getter/setter contract.
         */
        ClassMetaData metaData =
                repository.addMetaData(
                        StrategyTarget.class,
                        ClassMetaData.ACCESS_PROPERTY
                );

        EnhancementProject project =
                new EnhancementProject();

        ClassNodeTracker typeBytecode =
                project.loadClass(
                        StrategyTarget.class
                );

        PCEnhancer enhancer =
                new PCEnhancer(
                        repository,
                        typeBytecode,
                        metaData
                );

        enhancer.setRedefine(
                redefine
        );

        enhancer.setCreateSubclass(
                createSubclass
        );

        return new EnhancerContext(
                configuration,
                enhancer
        );
    }

    private static String className(
            ClassNodeTracker bytecode) {

        return bytecode
                .getClassNode()
                .name
                .replace('/', '.');
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
