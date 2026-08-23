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

import java.io.Externalizable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.xbean.asm9.Type;
import org.apache.xbean.asm9.tree.ClassNode;
import org.junit.jupiter.api.Test;

/**
 * Coverage-guided control-flow test for the standard Java serialization
 * enhancement cluster of PCEnhancer.
 *
 * <p>This scenario was selected from the post-TCF-002 coverage-gap
 * analysis and validated by a dedicated feasibility diagnostic before
 * being admitted to the definitive T_CF suite.</p>
 */
class PCEnhancerControlFlowSerializationTest {

    /**
     * TCF-003
     *
     * Exercises the standard Serializable enhancement path while detached
     * externalization and subclass enhancement are not active.
     *
     * The expected enhancement generates:
     *
     * - serialVersionUID
     * - writeObject(ObjectOutputStream)
     * - readObject(ObjectInputStream)
     *
     * without generating Externalizable support.
     */
    @Test
    void tcf003SerializableTargetGeneratesSerializationSupport() {

        OpenJPAConfigurationImpl configuration =
                newConfiguration();

        try {

            Class<?> target =
                    PCEnhancerControlFlowSerializationTarget.class;

            PCEnhancer enhancer =
                    new PCEnhancer(
                            configuration,
                            target);

            ClassMetaData metadata =
                    enhancer.getMetaData();

            assertNotNull(metadata);

            assertTrue(
                    Serializable.class.isAssignableFrom(
                            target));

            assertFalse(
                    Externalizable.class.isAssignableFrom(
                            target));

            boolean externalizeDetached =
                    ClassMetaData.SYNTHETIC.equals(
                            metadata.getDetachedState())
                            && Serializable.class.isAssignableFrom(
                            target)
                            && !configuration
                            .getDetachStateInstance()
                            .isDetachedStateTransient();

            assertFalse(
                    externalizeDetached);

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

            String writeObjectDescriptor =
                    Type.getMethodDescriptor(
                            Type.VOID_TYPE,
                            Type.getType(
                                    ObjectOutputStream.class));

            String readObjectDescriptor =
                    Type.getMethodDescriptor(
                            Type.VOID_TYPE,
                            Type.getType(
                                    ObjectInputStream.class));

            long serialVersionUidFields =
                    enhanced
                            .fields
                            .stream()
                            .filter(field ->
                                    field.name.equals(
                                            "serialVersionUID"))
                            .count();

            long writeObjectMethods =
                    enhanced
                            .methods
                            .stream()
                            .filter(method ->
                                    method.name.equals(
                                            "writeObject")
                                            && method.desc.equals(
                                            writeObjectDescriptor))
                            .count();

            long readObjectMethods =
                    enhanced
                            .methods
                            .stream()
                            .filter(method ->
                                    method.name.equals(
                                            "readObject")
                                            && method.desc.equals(
                                            readObjectDescriptor))
                            .count();

            long readExternalMethods =
                    enhanced
                            .methods
                            .stream()
                            .filter(method ->
                                    method.name.equals(
                                            "readExternal"))
                            .count();

            long writeExternalMethods =
                    enhanced
                            .methods
                            .stream()
                            .filter(method ->
                                    method.name.equals(
                                            "writeExternal"))
                            .count();

            assertEquals(
                    1L,
                    serialVersionUidFields);

            assertEquals(
                    1L,
                    writeObjectMethods);

            assertEquals(
                    1L,
                    readObjectMethods);

            assertEquals(
                    0L,
                    readExternalMethods);

            assertEquals(
                    0L,
                    writeExternalMethods);

            assertFalse(
                    enhanced
                            .interfaces
                            .contains(
                                    Type.getInternalName(
                                            Externalizable.class)));

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

        configuration.setDetachState(
                "fetch-groups(DetachedStateField=false)");

        return configuration;
    }
}
