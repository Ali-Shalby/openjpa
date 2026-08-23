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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataModes;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.util.asm.ClassNodeTracker;
import org.apache.openjpa.util.asm.EnhancementProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Manual black-box tests for the documented PCEnhancer
 * bytecode recording destination.
 *
 * Category Partition frames: TBB-019..TBB-020.
 */
class PCEnhancerBlackBoxRecordingTest {

    private static final String FIXTURE_CLASS_NAME =
            PCEnhancerBlackBoxRecordingTarget.class.getName();

    @Test
    void tbb019NullDirectoryOverwritesOnlyDisposableOriginal(
            @TempDir Path tempDirectory) throws Exception {

        /*
         * Create an isolated copy of the fixture bytecode.
         *
         * The original class under target/test-classes is never passed to
         * record() with directory == null.
         */
        Path isolatedRoot =
                Files.createDirectory(tempDirectory.resolve("isolated-input"));

        Path disposableClassFile =
                copyFixtureClass(isolatedRoot);

        byte[] originalBytes =
                Files.readAllBytes(disposableClassFile);

        /*
         * Use a class loader whose class path contains only the disposable
         * fixture copy. The platform loader is used as parent so that the
         * system copy of the fixture cannot be selected parent-first.
         */
        URL[] urls = {
                isolatedRoot.toUri().toURL()
        };

        try (URLClassLoader isolatedLoader =
                     new URLClassLoader(
                             urls,
                             ClassLoader.getPlatformClassLoader()
                     )) {

            Class<?> isolatedType =
                    Class.forName(
                            FIXTURE_CLASS_NAME,
                            true,
                            isolatedLoader
                    );

            assertSame(
                    isolatedLoader,
                    isolatedType.getClassLoader(),
                    "The target must be loaded from the disposable location"
            );

            try (EnhancerContext context =
                         createEnhancer(isolatedType)) {

                PCEnhancer enhancer = context.enhancer();

                assertNull(
                        enhancer.getDirectory(),
                        "The default recording destination must be null"
                );

                int result = enhancer.run();

                assertEquals(
                        PCEnhancer.ENHANCE_PC,
                        result
                );

                enhancer.record();
            }
        }

        byte[] recordedBytes =
                Files.readAllBytes(disposableClassFile);

        assertFalse(
                Arrays.equals(originalBytes, recordedBytes),
                "record() with directory == null must replace the disposable original class file"
        );

        assertTrue(
                recordedBytes.length > 0,
                "The recorded class file must contain bytecode"
        );
    }

    @Test
    void tbb020ExplicitDirectoryWritesClassBelowPackageTree(
            @TempDir Path tempDirectory) throws Exception {

        Path outputDirectory =
                Files.createDirectory(
                        tempDirectory.resolve("enhanced-output")
                );

        /*
         * Preserve a copy of the normal fixture bytecode so that the test
         * also verifies that an explicit output directory does not modify
         * the source fixture.
         */
        byte[] originalFixtureBytes =
                readOriginalFixtureBytes();

        try (EnhancerContext context =
                     createEnhancer(
                             PCEnhancerBlackBoxRecordingTarget.class
                     )) {

            PCEnhancer enhancer = context.enhancer();

            enhancer.setDirectory(
                    outputDirectory.toFile()
            );

            assertEquals(
                    outputDirectory.toFile(),
                    enhancer.getDirectory()
            );

            int result = enhancer.run();

            assertEquals(
                    PCEnhancer.ENHANCE_PC,
                    result
            );

            enhancer.record();
        }

        Path expectedOutput =
                outputDirectory.resolve(
                        FIXTURE_CLASS_NAME.replace('.', '/')
                                + ".class"
                );

        assertTrue(
                Files.isRegularFile(expectedOutput),
                "The enhanced class must be written below the explicit directory using the package structure"
        );

        assertTrue(
                Files.size(expectedOutput) > 0,
                "The generated class file must contain bytecode"
        );

        assertArrayEquals(
                originalFixtureBytes,
                readOriginalFixtureBytes(),
                "Using an explicit output directory must not overwrite the original fixture class"
        );
    }

    private static EnhancerContext createEnhancer(
            Class<?> type) {

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

        return new EnhancerContext(
                configuration,
                enhancer
        );
    }

    private static Path copyFixtureClass(
            Path root) throws IOException {

        String resourceName =
                FIXTURE_CLASS_NAME.replace('.', '/')
                        + ".class";

        Path target =
                root.resolve(resourceName);

        Files.createDirectories(
                target.getParent()
        );

        try (InputStream input =
                     PCEnhancerBlackBoxRecordingTarget.class
                             .getClassLoader()
                             .getResourceAsStream(resourceName)) {

            if (input == null) {
                throw new IOException(
                        "Unable to locate fixture bytecode: "
                                + resourceName
                );
            }

            Files.copy(
                    input,
                    target
            );
        }

        return target;
    }

    private static byte[] readOriginalFixtureBytes()
            throws IOException {

        String resourceName =
                FIXTURE_CLASS_NAME.replace('.', '/')
                        + ".class";

        try (InputStream input =
                     PCEnhancerBlackBoxRecordingTarget.class
                             .getClassLoader()
                             .getResourceAsStream(resourceName)) {

            assertNotNull(
                    input,
                    "The original fixture bytecode must be available"
            );

            return input.readAllBytes();
        }
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

/**
 * Purpose-built persistent fixture used by F6.
 *
 * It is deliberately declared as a separate top-level class so that TBB-019
 * can copy and load its bytecode through an isolated ClassLoader without
 * introducing an enclosing-class dependency.
 */
class PCEnhancerBlackBoxRecordingTarget {

    private String persistentValue;

    protected PCEnhancerBlackBoxRecordingTarget() {
    }
}
