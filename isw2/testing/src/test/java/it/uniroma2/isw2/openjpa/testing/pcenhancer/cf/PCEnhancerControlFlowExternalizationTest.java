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

import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.xbean.asm9.Type;
import org.junit.jupiter.api.Test;

/**
 * Coverage-guided test for the detached externalization cluster.
 */
class PCEnhancerControlFlowExternalizationTest {

    /**
     * TCF-002
     *
     * Exercises detached-state externalization for a Serializable entity
     * using a non-transient synthetic detached-state field.
     */
    @Test
    void tcf002SerializableSyntheticDetachedStateGeneratesExternalizationCode() {

        OpenJPAConfigurationImpl configuration =
                newConfiguration();

        try {

            PCEnhancer enhancer =
                    new PCEnhancer(
                            configuration,
                            PCEnhancerControlFlowExternalizationTarget.class);

            ClassMetaData metadata =
                    enhancer.getMetaData();

            assertNotNull(metadata);

            /*
             * Feasibility guard: if this is not SYNTHETIC, the scenario would
             * not reach the externalizeDetached() path selected by the audit.
             */
            assertEquals(
                    ClassMetaData.SYNTHETIC,
                    metadata.getDetachedState());

            assertFalse(
                    configuration
                            .getDetachStateInstance()
                            .isDetachedStateTransient());

            assertEquals(
                    PCEnhancer.ENHANCE_PC,
                    enhancer.run());

            String externalizable =
                    Type.getInternalName(
                            Externalizable.class);

            assertTrue(
                    enhancer
                            .getPCBytecode()
                            .getClassNode()
                            .interfaces
                            .contains(externalizable));

            long readExternalMethods =
                    enhancer
                            .getPCBytecode()
                            .getClassNode()
                            .methods
                            .stream()
                            .filter(method ->
                                    method.name.equals(
                                            "readExternal"))
                            .count();

            long writeExternalMethods =
                    enhancer
                            .getPCBytecode()
                            .getClassNode()
                            .methods
                            .stream()
                            .filter(method ->
                                    method.name.equals(
                                            "writeExternal"))
                            .count();

            assertEquals(
                    1L,
                    readExternalMethods);

            assertEquals(
                    1L,
                    writeExternalMethods);

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

        /*
         * DetachedStateField=true means that the synthetic detached-state
         * field is retained across serialization rather than being transient.
         */
        configuration.setDetachState(
                "fetch-groups(DetachedStateField=true)");

        return configuration;
    }
}
