/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0.
 */

package it.uniroma2.isw2.openjpa.testing.pcenhancer.cf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.xbean.asm9.Opcodes;
import org.apache.xbean.asm9.Type;
import org.apache.xbean.asm9.tree.AbstractInsnNode;
import org.apache.xbean.asm9.tree.ClassNode;
import org.apache.xbean.asm9.tree.MethodInsnNode;
import org.apache.xbean.asm9.tree.MethodNode;
import org.apache.xbean.asm9.tree.TypeInsnNode;
import org.junit.jupiter.api.Test;

/**
 * Coverage-guided control-flow test for optimized application-identity
 * copying through an IdClass constructor.
 *
 * <p>The scenario was selected from the post-TCF-003 coverage-gap analysis.
 * A dedicated feasibility preflight confirmed execution of both
 * optimizeIdCopy and getIdClassConstructorParmOrder.</p>
 */
class PCEnhancerControlFlowOptimizedIdentityTest {

    /**
     * TCF-005
     *
     * Exercises the optimized IdClass-copy path when:
     *
     * - OptimizeIdCopy is enabled;
     * - all primary-key attributes are non-PC values;
     * - IdClass fields are private;
     * - no public setters are available;
     * - a compatible public IdClass constructor is available.
     */
    @Test
    void tcf005OptimizedIdClassConstructorIsUsed() {

        OpenJPAConfigurationImpl configuration =
                newConfiguration();

        try {

            assertTrue(
                    enableOptimizeIdCopy(
                            configuration));

            assertTrue(
                    configuration.getOptimizeIdCopy());

            Class<?> target =
                    PCEnhancerControlFlowOptimizedIdentityTarget.class;

            Class<?> idClass =
                    PCEnhancerControlFlowOptimizedIdentityTarget
                            .OptimizedIdentityId.class;

            PCEnhancer enhancer =
                    new PCEnhancer(
                            configuration,
                            target);

            ClassMetaData metadata =
                    enhancer.getMetaData();

            assertNotNull(metadata);

            assertEquals(
                    ClassMetaData.ID_APPLICATION,
                    metadata.getIdentityType());

            assertFalse(
                    metadata.isOpenJPAIdentity());

            assertEquals(
                    idClass,
                    metadata.getObjectIdType());

            FieldMetaData[] primaryKeyFields =
                    metadata.getPrimaryKeyFields();

            assertEquals(
                    3,
                    primaryKeyFields.length);

            for (FieldMetaData field : primaryKeyFields) {

                assertFalse(
                        field.getDeclaredTypeCode()
                                == JavaTypes.PC);
            }

            assertFalse(
                    enhancer.getCreateSubclass());

            assertFalse(
                    enhancer.getRedefine());

            assertEquals(
                    PCEnhancer.ENHANCE_PC,
                    enhancer.run());

            ClassNode enhanced =
                    enhancer
                            .getPCBytecode()
                            .getClassNode();

            long copyToMethods =
                    enhanced
                            .methods
                            .stream()
                            .filter(method ->
                                    method.name.equals(
                                            "pcCopyKeyFieldsToObjectId"))
                            .count();

            long copyFromMethods =
                    enhanced
                            .methods
                            .stream()
                            .filter(method ->
                                    method.name.equals(
                                            "pcCopyKeyFieldsFromObjectId"))
                            .count();

            assertEquals(
                    2L,
                    copyToMethods);

            assertEquals(
                    2L,
                    copyFromMethods);

            String idInternalName =
                    Type.getInternalName(
                            idClass);

            String optimizedConstructorDescriptor =
                    Type.getMethodDescriptor(
                            Type.VOID_TYPE,
                            Type.getType(String.class),
                            Type.LONG_TYPE,
                            Type.INT_TYPE);

            int idClassNewInstructions =
                    0;

            int optimizedConstructorCalls =
                    0;

            for (MethodNode method : enhanced.methods) {

                if (!method.name.equals(
                        "pcCopyKeyFieldsToObjectId")) {
                    continue;
                }

                for (AbstractInsnNode instruction
                        : method.instructions) {

                    if (instruction
                            instanceof TypeInsnNode typeInsn
                            && typeInsn.getOpcode()
                            == Opcodes.NEW
                            && typeInsn.desc.equals(
                            idInternalName)) {

                        idClassNewInstructions++;
                    }

                    if (instruction
                            instanceof MethodInsnNode methodInsn
                            && methodInsn.getOpcode()
                            == Opcodes.INVOKESPECIAL
                            && methodInsn.owner.equals(
                            idInternalName)
                            && methodInsn.name.equals(
                            "<init>")
                            && methodInsn.desc.equals(
                            optimizedConstructorDescriptor)) {

                        optimizedConstructorCalls++;
                    }
                }
            }

            assertEquals(
                    2,
                    idClassNewInstructions);

            assertEquals(
                    2,
                    optimizedConstructorCalls);

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

    /**
     * Uses the same configuration mechanism already validated by the
     * TCF-005 feasibility preflight without assuming a concrete setter
     * parameter type.
     */
    private static boolean enableOptimizeIdCopy(
            OpenJPAConfigurationImpl configuration) {

        Method setter =
                Arrays.stream(
                                configuration
                                        .getClass()
                                        .getMethods())
                        .filter(method ->
                                method.getName()
                                        .equals(
                                                "setOptimizeIdCopy"))
                        .filter(method ->
                                method.getParameterCount()
                                        == 1)
                        .findFirst()
                        .orElse(null);

        if (setter == null) {
            return false;
        }

        try {

            Class<?> parameter =
                    setter.getParameterTypes()[0];

            if (parameter == boolean.class
                    || parameter == Boolean.class) {

                setter.invoke(
                        configuration,
                        true);

                return true;
            }

            if (parameter == String.class) {

                setter.invoke(
                        configuration,
                        "true");

                return true;
            }

            return false;

        } catch (ReflectiveOperationException exception) {

            throw new IllegalStateException(
                    "Cannot enable OptimizeIdCopy.",
                    exception);
        }
    }
}
