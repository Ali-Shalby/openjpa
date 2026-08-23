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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.meta.AbstractMetaDataFactory;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataModes;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.util.asm.BytecodeWriter;
import org.apache.openjpa.util.asm.ClassNodeTracker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Manual black-box tests for the documented PCEnhancer
 * target representations and target cardinality.
 *
 * Category Partition frames: TBB-021..TBB-025.
 */
class PCEnhancerBlackBoxTargetRepresentationTest {

    private static final Class<?> TARGET_TYPE =
            PCEnhancerBlackBoxToolTarget.class;

    private static final String TARGET_NAME =
            TARGET_TYPE.getName();

    private static final String TARGET_SIMPLE_NAME =
            TARGET_TYPE.getSimpleName();

    private static final String TARGET_PACKAGE =
            TARGET_TYPE.getPackageName();

    @Test
    void tbb021FullyQualifiedClassNameSelectsTarget()
            throws Exception {

        try (EnhancerContext context = createEnhancerContext()) {

            CaptureWriter writer = new CaptureWriter();

            boolean result = runEnhancer(
                    context,
                    new String[]{TARGET_NAME},
                    writer
            );

            assertTrue(
                    result,
                    "PCEnhancer must accept a fully-qualified class name"
            );

            assertExactlyTargetWasEnhanced(writer);
        }
    }

    @Test
    void tbb022JavaFilePathSelectsTarget(
            @TempDir Path tempDirectory)
            throws Exception {

        Path javaFile =
                tempDirectory.resolve(
                        TARGET_SIMPLE_NAME + ".java"
                );

        String source =
                "package " + TARGET_PACKAGE + ";"
                        + System.lineSeparator()
                        + "class " + TARGET_SIMPLE_NAME + " {"
                        + System.lineSeparator()
                        + "}"
                        + System.lineSeparator();

        Files.writeString(
                javaFile,
                source,
                StandardCharsets.UTF_8
        );

        try (EnhancerContext context = createEnhancerContext()) {

            CaptureWriter writer = new CaptureWriter();

            boolean result = runEnhancer(
                    context,
                    new String[]{javaFile.toString()},
                    writer
            );

            assertTrue(
                    result,
                    "PCEnhancer must accept a .java target path"
            );

            assertExactlyTargetWasEnhanced(writer);
        }
    }

    @Test
    void tbb023ClassFilePathSelectsTarget(
            @TempDir Path tempDirectory)
            throws Exception {

        Path classFile =
                copyTargetClass(
                        tempDirectory.resolve(
                                TARGET_SIMPLE_NAME + ".class"
                        )
                );

        try (EnhancerContext context = createEnhancerContext()) {

            CaptureWriter writer = new CaptureWriter();

            boolean result = runEnhancer(
                    context,
                    new String[]{classFile.toString()},
                    writer
            );

            assertTrue(
                    result,
                    "PCEnhancer must accept a .class target path"
            );

            assertExactlyTargetWasEnhanced(writer);
        }
    }

    @Test
    void tbb024JdoMetadataFileSelectsListedTarget(
            @TempDir Path tempDirectory)
            throws Exception {

        Path jdoFile =
                tempDirectory.resolve("targets.jdo");

        /*
         * PCEnhancer documents .jdo as an accepted metadata-file argument.
         *
         * ClassArgParser treats an existing non-.java/.class argument as
         * a metadata file. Under the JPA metadata factory used by this
         * isolated harness, "entity" is the public metadata element used
         * to list a managed class.
         *
         * The frame verifies the documented .jdo target representation;
         * it is not intended to validate a JDO XML schema.
         */
        String metadata =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + System.lineSeparator()
                        + "<entity-mappings>"
                        + System.lineSeparator()
                        + "    <entity class=\""
                        + TARGET_NAME
                        + "\"/>"
                        + System.lineSeparator()
                        + "</entity-mappings>"
                        + System.lineSeparator();

        Files.writeString(
                jdoFile,
                metadata,
                StandardCharsets.UTF_8
        );

        try (EnhancerContext context = createEnhancerContext()) {

            CaptureWriter writer = new CaptureWriter();

            boolean result = runEnhancer(
                    context,
                    new String[]{jdoFile.toString()},
                    writer
            );

            assertTrue(
                    result,
                    "PCEnhancer must accept a .jdo metadata-file target"
            );

            assertExactlyTargetWasEnhanced(writer);
        }
    }

    @Test
    void tbb025ZeroExplicitTargetsUsesConfiguredPersistentTypeList()
            throws Exception {

        try (EnhancerContext context = createEnhancerContext()) {

            /*
             * Configure the repository with one persistent type.
             *
             * This represents the documented boundary:
             *
             * explicit target count = 0
             */
            AbstractMetaDataFactory metaDataFactory =
                    (AbstractMetaDataFactory)
                            context.repository()
                                    .getMetaDataFactory();

            metaDataFactory.setTypes(
                    Set.of(TARGET_NAME)
            );

            CaptureWriter writer = new CaptureWriter();

            boolean result = runEnhancer(
                    context,
                    new String[0],
                    writer
            );

            assertTrue(
                    result,
                    "With no explicit targets, PCEnhancer must use the configured persistent-type list"
            );

            assertExactlyTargetWasEnhanced(writer);
        }
    }

    private static EnhancerContext createEnhancerContext() {

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

        ClassMetaData metaData =
                repository.addMetaData(TARGET_TYPE);

        metaData.addDeclaredField(
                "persistentValue",
                String.class
        );

        return new EnhancerContext(
                configuration,
                repository
        );
    }

    private static boolean runEnhancer(
            EnhancerContext context,
            String[] arguments,
            CaptureWriter writer)
            throws IOException {

        PCEnhancer.Flags flags =
                new PCEnhancer.Flags();

        /*
         * Keep the same Class object and ClassLoader used by the
         * purpose-built repository metadata.
         *
         * Temporary-classloader behavior belongs to a different
         * configuration dimension and is not under test in F7.
         */
        flags.tmpClassLoader = false;

        return PCEnhancer.run(
                context.configuration(),
                arguments,
                flags,
                context.repository(),
                writer,
                TARGET_TYPE.getClassLoader()
        );
    }

    private static Path copyTargetClass(
            Path destination)
            throws IOException {

        String resourceName =
                TARGET_NAME.replace('.', '/')
                        + ".class";

        try (InputStream input =
                     TARGET_TYPE.getClassLoader()
                             .getResourceAsStream(resourceName)) {

            if (input == null) {
                throw new IOException(
                        "Unable to locate target bytecode: "
                                + resourceName
                );
            }

            Files.copy(
                    input,
                    destination
            );
        }

        return destination;
    }

    private static void assertExactlyTargetWasEnhanced(
            CaptureWriter writer) {

        assertEquals(
                1,
                writer.getWrittenTypes().size(),
                "Exactly one target must be enhanced"
        );

        assertEquals(
                TARGET_NAME,
                writer.getWrittenTypes().get(0),
                "The selected target must be the frozen F7 fixture"
        );
    }

    /**
     * Captures the class that PCEnhancer would normally record.
     *
     * No production or test bytecode is written to disk.
     */
    private static final class CaptureWriter
            implements BytecodeWriter {

        private final List<String> writtenTypes =
                new ArrayList<>();

        @Override
        public void write(ClassNodeTracker type) {

            writtenTypes.add(
                    type.getClassNode()
                            .name
                            .replace('/', '.')
            );
        }

        List<String> getWrittenTypes() {
            return writtenTypes;
        }
    }

    private record EnhancerContext(
            OpenJPAConfigurationImpl configuration,
            MetaDataRepository repository)
            implements AutoCloseable {

        @Override
        public void close() {
            configuration.close();
        }
    }
}

/**
 * Purpose-built persistent fixture for F7.
 *
 * It is a separate top-level class so that its FQCN, .java representation
 * and .class representation all refer to one unambiguous target.
 */
class PCEnhancerBlackBoxToolTarget {

    private String persistentValue;

    protected PCEnhancerBlackBoxToolTarget() {
    }
}
