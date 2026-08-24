/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.uniroma2.isw2.openjpa.testing.pcenhancer.mt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.StateManager;
import org.junit.jupiter.api.Test;

/**
 * TMT-005.
 *
 * Runtime oracle for relationship-valued / derived application identity.
 *
 * The scenario reuses the controlled TCF-004 fixture but strengthens the
 * oracle from structural generation to actual runtime object-id semantics.
 */
class PCEnhancerMutationDerivedIdentityTest {

    private static final String PARENT_NAME =
            "it.uniroma2.isw2.openjpa.testing.pcenhancer.cf."
                    + "PCEnhancerControlFlowRelationshipIdentityParentTarget";

    private static final String TARGET_NAME =
            "it.uniroma2.isw2.openjpa.testing.pcenhancer.cf."
                    + "PCEnhancerControlFlowRelationshipIdentityTarget";

    @Test
    void relationshipPrimaryKeyPreservesDerivedIdentityRuntimeValues()
            throws Exception {

        ClassLoader originalLoader =
                PCEnhancerMutationDerivedIdentityTest.class
                        .getClassLoader();

        Class<?> originalParent =
                Class.forName(
                        PARENT_NAME,
                        true,
                        originalLoader);

        Class<?> originalTarget =
                Class.forName(
                        TARGET_NAME,
                        true,
                        originalLoader);

        OpenJPAConfigurationImpl configuration =
                new OpenJPAConfigurationImpl();

        Path output =
                Files.createTempDirectory(
                        "pcenhancer-tmt005-");

        try {
            configuration.setLog(
                    "File=stdout, DefaultLevel=WARN");

            configuration.setMetaDataFactory(
                    "jpa");

            /*
             * ==========================================================
             * A. ENHANCE RELATED PARENT
             * ==========================================================
             */

            PCEnhancer parentEnhancer =
                    new PCEnhancer(
                            configuration,
                            originalParent);

            parentEnhancer.setDirectory(
                    output.toFile());

            assertEquals(
                    PCEnhancer.ENHANCE_PC,
                    parentEnhancer.run(),
                    "Related parent must be enhanced.");

            assertNotNull(
                    parentEnhancer.getMetaData(),
                    "Parent metadata must be available.");

            Class<?> parentObjectIdType =
                    parentEnhancer
                            .getMetaData()
                            .getObjectIdType();

            assertNotNull(
                    parentObjectIdType,
                    "Parent object-id type must be available.");

            parentEnhancer.record();

            /*
             * ==========================================================
             * B. ENHANCE RELATIONSHIP-VALUED ID TARGET
             * ==========================================================
             */

            PCEnhancer targetEnhancer =
                    new PCEnhancer(
                            configuration,
                            originalTarget);

            targetEnhancer.setDirectory(
                    output.toFile());

            assertEquals(
                    PCEnhancer.ENHANCE_PC,
                    targetEnhancer.run(),
                    "Derived-identity target must be enhanced.");

            assertNotNull(
                    targetEnhancer.getMetaData(),
                    "Target metadata must be available.");

            assertNotNull(
                    targetEnhancer
                            .getMetaData()
                            .getObjectIdType(),
                    "Target IdClass must be available.");

            targetEnhancer.record();

            assertRecorded(
                    output,
                    PARENT_NAME);

            assertRecorded(
                    output,
                    TARGET_NAME);

            /*
             * ==========================================================
             * C. LOAD THE ACTUALLY ENHANCED BYTECODE
             * ==========================================================
             */

            Set<String> childFirst =
                    new HashSet<>(
                            Arrays.asList(
                                    PARENT_NAME,
                                    TARGET_NAME));

            try (SelectedChildFirstLoader loader =
                         new SelectedChildFirstLoader(
                                 new URL[] {
                                         output.toUri().toURL()
                                 },
                                 originalLoader,
                                 childFirst)) {

                Class<?> parentType =
                        Class.forName(
                                PARENT_NAME,
                                true,
                                loader);

                Class<?> targetType =
                        Class.forName(
                                TARGET_NAME,
                                true,
                                loader);

                assertTrue(
                        PersistenceCapable.class
                                .isAssignableFrom(parentType),
                        "Enhanced parent must implement "
                                + "PersistenceCapable.");

                assertTrue(
                        PersistenceCapable.class
                                .isAssignableFrom(targetType),
                        "Enhanced target must implement "
                                + "PersistenceCapable.");

                Object parent =
                        newInstance(parentType);

                Object target =
                        newInstance(targetType);

                /*
                 * ======================================================
                 * D. BUILD THE RELATED PARENT IDENTITY
                 *
                 * The controlled TCF-004 parent has a scalar long id.
                 * OpenJPA represents that single-field identity with an
                 * object-id wrapper. We construct the wrapper selected by
                 * metadata and make the related enhanced parent return it
                 * through its StateManager.
                 * ======================================================
                 */

                long parentId =
                        424242L;

                long sequenceId =
                        777L;
                setNumericField(
                        parent,
                        "id",
                        parentId);
                Object parentObjectId =
                        newNumericObjectId(
                                parentObjectIdType,
                                originalParent,
                                parentId);

                assertNotNull(
                        parentObjectId,
                        "Parent object id must be constructible.");

                StateManagerHandler stateManagerHandler =
                        new StateManagerHandler(
                                parentObjectId);

                StateManager stateManager =
                        (StateManager) Proxy.newProxyInstance(
                                StateManager.class.getClassLoader(),
                                new Class<?>[] {
                                        StateManager.class
                                },
                                stateManagerHandler);

                Method parentReplaceStateManager =
                        findMethod(
                                parentType,
                                "pcReplaceStateManager",
                                1);

                parentReplaceStateManager.invoke(
                        parent,
                        stateManager);

                /*
                 * ======================================================
                 * E. POPULATE RELATIONSHIP-VALUED PRIMARY KEY
                 * ======================================================
                 */

                setField(
                        target,
                        "parent",
                        parent);

                setNumericField(
                        target,
                        "sequenceId",
                        sequenceId);

                /*
                 * ======================================================
                 * F. pcNewObjectIdInstance
                 * ======================================================
                 */

                Method newObjectId =
                        findMethod(
                                targetType,
                                "pcNewObjectIdInstance",
                                0);

                Object targetObjectId =
                        newObjectId.invoke(
                                target);

                assertNotNull(
                        targetObjectId,
                        "pcNewObjectIdInstance must create "
                                + "the derived IdClass.");

                /*
                 * ======================================================
                 * G. DIRECT INSTANCE -> OBJECT ID COPY
                 *
                 * This is the key runtime oracle:
                 *
                 * relationship field:
                 *     Parent object
                 *
                 * must become:
                 *     parent's scalar object-id value
                 *
                 * inside the target IdClass.
                 * ======================================================
                 */

                Method copyToObjectId =
                        findOneArgumentMethod(
                                targetType,
                                "pcCopyKeyFieldsToObjectId");

                copyToObjectId.invoke(
                        target,
                        targetObjectId);

                /*
                 * pcNewObjectIdInstance() may return OpenJPA's shared
                 * ObjectId wrapper. The generated copy methods unwrap
                 * it internally; the test must do the same before
                 * inspecting the actual IdClass.
                 */
                Object actualTargetId =
                        targetObjectId;

                if (targetObjectId
                        instanceof org.apache.openjpa.util.ObjectId) {

                    actualTargetId =
                            ((org.apache.openjpa.util.ObjectId)
                                    targetObjectId)
                                    .getId();
                }

                assertNotNull(
                        actualTargetId,
                        "Wrapped target IdClass must be available.");

                Object copiedParentId =
                        getField(
                                actualTargetId,
                                "parent");

                Object copiedSequenceId =
                        getField(
                                actualTargetId,
                                "sequenceId");

                assertNumericEquals(
                        parentId,
                        copiedParentId,
                        "Relationship PK must be converted to "
                                + "the parent's scalar identity.");

                assertNumericEquals(
                        sequenceId,
                        copiedSequenceId,
                        "Scalar sequence key must be copied unchanged.");



                /*
                 * ======================================================
                 * H. OBJECT ID -> FIELD CONSUMER COPY
                 *
                 * Verify the reverse generated runtime protocol.
                 * ======================================================
                 */

                Method copyFromObjectId =
                        findTwoArgumentMethod(
                                targetType,
                                "pcCopyKeyFieldsFromObjectId");

                Class<?> consumerType =
                        copyFromObjectId
                                .getParameterTypes()[0];

                FieldConsumerHandler consumerHandler =
                        new FieldConsumerHandler();

                Object consumer =
                        Proxy.newProxyInstance(
                                consumerType.getClassLoader(),
                                new Class<?>[] {
                                        consumerType
                                },
                                consumerHandler);

                copyFromObjectId.invoke(
                        target,
                        consumer,
                        targetObjectId);

                assertEquals(
                        2,
                        consumerHandler.values.size(),
                        "Exactly two primary-key values must be "
                                + "exported from the IdClass.");

                assertContainsNumeric(
                        consumerHandler.values,
                        parentId,
                        "FieldConsumer must receive the parent "
                                + "scalar identity.");

                assertContainsNumeric(
                        consumerHandler.values,
                        sequenceId,
                        "FieldConsumer must receive sequenceId.");

                /*
                 * ======================================================
                 * I. OBSERVABLE EVIDENCE
                 * ======================================================
                 */

                System.out.println(
                        "TMT005_PARENT_OID_TYPE:"
                                + parentObjectId
                                        .getClass()
                                        .getName());

                System.out.println(
                        "TMT005_TARGET_OID_TYPE:"
                                + targetObjectId
                                        .getClass()
                                        .getName());

                System.out.println(
                        "TMT005_OBJECT_ID:"
                                + copiedParentId
                                + ","
                                + copiedSequenceId);

                System.out.println(
                        "TMT005_TARGET_OID_WRAPPER:"
                                + targetObjectId
                                .getClass()
                                .getName());

                System.out.println(
                        "TMT005_ACTUAL_IDCLASS:"
                                + actualTargetId
                                .getClass()
                                .getName());

                System.out.println(
                        "TMT005_CONSUMER_VALUES:"
                                + consumerHandler.values);

                System.out.println(
                        "TMT005_RUNTIME:VALID");
            }
        }
        finally {
            configuration.close();

            if (Files.exists(output)) {
                try (var stream = Files.walk(output)) {
                    stream
                            .sorted(
                                    Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.deleteIfExists(path);
                                }
                                catch (Exception ignored) {
                                    // Temporary enhanced output only.
                                }
                            });
                }
            }
        }
    }

    private static void assertRecorded(
            Path output,
            String className) {

        Path file =
                output.resolve(
                        className.replace(
                                '.',
                                File.separatorChar)
                                + ".class");

        assertTrue(
                Files.isRegularFile(file),
                "Enhanced class was not recorded: "
                        + className);
    }

    private static Object newInstance(
            Class<?> type)
            throws Exception {

        Constructor<?> constructor =
                type.getDeclaredConstructor();

        constructor.setAccessible(true);

        return constructor.newInstance();
    }

    private static Object newNumericObjectId(
            Class<?> objectIdType,
            Class<?> managedType,
            long value)
            throws Exception {

        for (Constructor<?> constructor :
                objectIdType.getDeclaredConstructors()) {

            Class<?>[] params =
                    constructor.getParameterTypes();

            if (params.length != 2) {
                continue;
            }

            if (params[0] != Class.class) {
                continue;
            }

            constructor.setAccessible(true);

            if (params[1] == long.class) {
                return constructor.newInstance(
                        managedType,
                        value);
            }

            if (params[1] == Long.class) {
                return constructor.newInstance(
                        managedType,
                        Long.valueOf(value));
            }

            if (params[1] == int.class) {
                return constructor.newInstance(
                        managedType,
                        (int) value);
            }

            if (params[1] == Integer.class) {
                return constructor.newInstance(
                        managedType,
                        Integer.valueOf((int) value));
            }
        }

        throw new AssertionError(
                "No supported numeric object-id constructor for "
                        + objectIdType.getName());
    }

    private static Method findMethod(
            Class<?> type,
            String name,
            int parameterCount) {

        Class<?> current =
                type;

        while (current != null) {

            for (Method method :
                    current.getDeclaredMethods()) {

                if (method.getName().equals(name)
                        && method.getParameterCount()
                                == parameterCount) {

                    method.setAccessible(true);
                    return method;
                }
            }

            current =
                    current.getSuperclass();
        }

        throw new AssertionError(
                "Generated method not found: "
                        + name
                        + "/"
                        + parameterCount);
    }

    private static Method findOneArgumentMethod(
            Class<?> type,
            String name) {

        Class<?> current =
                type;

        while (current != null) {

            for (Method method :
                    current.getDeclaredMethods()) {

                if (method.getName().equals(name)
                        && method.getParameterCount() == 1) {

                    method.setAccessible(true);
                    return method;
                }
            }

            current =
                    current.getSuperclass();
        }

        throw new AssertionError(
                "Generated one-argument method not found: "
                        + name);
    }

    private static Method findTwoArgumentMethod(
            Class<?> type,
            String name) {

        Class<?> current =
                type;

        while (current != null) {

            for (Method method :
                    current.getDeclaredMethods()) {

                if (method.getName().equals(name)
                        && method.getParameterCount() == 2) {

                    method.setAccessible(true);
                    return method;
                }
            }

            current =
                    current.getSuperclass();
        }

        throw new AssertionError(
                "Generated two-argument method not found: "
                        + name);
    }

    private static void setField(
            Object object,
            String name,
            Object value)
            throws Exception {

        Field field =
                findField(
                        object.getClass(),
                        name);

        field.set(
                object,
                value);
    }

    private static void setNumericField(
            Object object,
            String name,
            long value)
            throws Exception {

        Field field =
                findField(
                        object.getClass(),
                        name);

        Class<?> type =
                field.getType();

        if (type == long.class
                || type == Long.class) {

            field.set(
                    object,
                    Long.valueOf(value));

            return;
        }

        if (type == int.class
                || type == Integer.class) {

            field.set(
                    object,
                    Integer.valueOf((int) value));

            return;
        }

        throw new AssertionError(
                "Unsupported numeric field type for "
                        + name
                        + ": "
                        + type.getName());
    }

    private static Object getField(
            Object object,
            String name)
            throws Exception {

        return findField(
                object.getClass(),
                name)
                .get(object);
    }

    private static Field findField(
            Class<?> type,
            String name)
            throws Exception {

        Class<?> current =
                type;

        while (current != null) {

            try {
                Field field =
                        current.getDeclaredField(name);

                field.setAccessible(true);

                return field;
            }
            catch (NoSuchFieldException ignored) {
                current =
                        current.getSuperclass();
            }
        }

        throw new AssertionError(
                "Field not found: "
                        + type.getName()
                        + "."
                        + name);
    }

    private static void assertNumericEquals(
            long expected,
            Object actual,
            String message) {

        assertNotNull(
                actual,
                message + " Actual value is null.");

        assertTrue(
                actual instanceof Number,
                message
                        + " Actual value is not numeric: "
                        + actual);

        assertEquals(
                expected,
                ((Number) actual).longValue(),
                message);
    }

    private static void assertContainsNumeric(
            List<Object> values,
            long expected,
            String message) {

        boolean found =
                false;

        for (Object value : values) {

            if (value instanceof Number
                    && ((Number) value).longValue()
                            == expected) {

                found = true;
                break;
            }
        }

        assertTrue(
                found,
                message
                        + " Values observed: "
                        + values);
    }

    private static final class StateManagerHandler
            implements InvocationHandler {

        private final Object objectId;

        private int fetchObjectIdCalls;

        private StateManagerHandler(
                Object objectId) {

            this.objectId =
                    objectId;
        }

        @Override
        public Object invoke(
                Object proxy,
                Method method,
                Object[] args) {

            String name =
                    method.getName();

            if (method.getDeclaringClass()
                    == Object.class) {

                if ("toString".equals(name)) {
                    return "TMT005-StateManager";
                }

                if ("hashCode".equals(name)) {
                    return System.identityHashCode(
                            proxy);
                }

                if ("equals".equals(name)) {
                    return proxy == args[0];
                }
            }

            if ("fetchObjectId".equals(name)) {

                fetchObjectIdCalls++;

                return objectId;
            }

            return defaultValue(
                    method.getReturnType());
        }
    }

    private static final class FieldConsumerHandler
            implements InvocationHandler {

        private final List<Object> values =
                new ArrayList<>();

        @Override
        public Object invoke(
                Object proxy,
                Method method,
                Object[] args) {

            String name =
                    method.getName();

            if (method.getDeclaringClass()
                    == Object.class) {

                if ("toString".equals(name)) {
                    return "TMT005-FieldConsumer";
                }

                if ("hashCode".equals(name)) {
                    return System.identityHashCode(
                            proxy);
                }

                if ("equals".equals(name)) {
                    return proxy == args[0];
                }
            }

            if (name.startsWith("store")
                    && args != null
                    && args.length >= 2) {

                values.add(
                        args[1]);

                return null;
            }

            return defaultValue(
                    method.getReturnType());
        }
    }

    private static Object defaultValue(
            Class<?> returnType) {

        if (returnType == void.class) {
            return null;
        }

        if (returnType == boolean.class) {
            return false;
        }

        if (returnType == byte.class) {
            return (byte) 0;
        }

        if (returnType == short.class) {
            return (short) 0;
        }

        if (returnType == int.class) {
            return 0;
        }

        if (returnType == long.class) {
            return 0L;
        }

        if (returnType == float.class) {
            return 0.0F;
        }

        if (returnType == double.class) {
            return 0.0D;
        }

        if (returnType == char.class) {
            return '\0';
        }

        return null;
    }

    private static final class SelectedChildFirstLoader
            extends URLClassLoader {

        private final Set<String> childFirstClasses;

        private SelectedChildFirstLoader(
                URL[] urls,
                ClassLoader parent,
                Set<String> childFirstClasses) {

            super(
                    urls,
                    parent);

            this.childFirstClasses =
                    childFirstClasses;
        }

        @Override
        protected Class<?> loadClass(
                String name,
                boolean resolve)
                throws ClassNotFoundException {

            synchronized (
                    getClassLoadingLock(name)) {

                Class<?> loaded =
                        findLoadedClass(name);

                if (loaded == null
                        && childFirstClasses.contains(
                                name)) {

                    try {
                        loaded =
                                findClass(name);
                    }
                    catch (ClassNotFoundException ignored) {
                        // Normal parent delegation below.
                    }
                }

                if (loaded == null) {

                    loaded =
                            super.loadClass(
                                    name,
                                    false);
                }

                if (resolve) {
                    resolveClass(
                            loaded);
                }

                return loaded;
            }
        }
    }
}
