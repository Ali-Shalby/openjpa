/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.enhance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.util.asm.BytecodeWriter;
import org.apache.openjpa.util.asm.ClassNodeTracker;
import org.apache.openjpa.util.asm.EnhancementProject;
import org.apache.xbean.asm9.tree.ClassNode;
import org.junit.jupiter.api.Test;

class PCEnhancerLLMTest {

    /*
     * Fixture condivisa.
     *
     * I relativi metodi non sono test case aggiuntivi.
     */
    static class AccessFixture {

        private String value;
        private Object objectValue;
        private int number;

        private static String staticValue;

        String getValue() {
            return value;
        }

        String getCastedValue() {
            return (String) objectValue;
        }

        int getLiteral() {
            return 42;
        }

        static String getStaticValue() {
            return staticValue;
        }

        void setValue(String value) {
            this.value = value;
        }

        void setIncrementedNumber(int number) {
            this.number = number + 1;
        }

        static void setStaticValue(String value) {
            staticValue = value;
        }
    }

    interface AccessFixtureInterface {

        String getValue();
    }

    static class ManagedType {
    }

    static class InterfaceImplementation {
    }

    /*
     * Costruisce un PCEnhancer senza richiedere metadata reali.
     *
     * Il costruttore production PCEnhancer(MetaDataRepository,
     * ClassNodeTracker, ClassMetaData) conserva i riferimenti forniti e
     * richiede soltanto il Log ottenuto dalla configurazione del repository.
     */
    private static PCEnhancer newEnhancer() {
        EnhancementProject project = new EnhancementProject();
        ClassNodeTracker tracker = project.loadClass(ManagedType.class);

        MetaDataRepository repository = mock(MetaDataRepository.class);
        OpenJPAConfiguration configuration = mock(OpenJPAConfiguration.class);
        Log log = mock(Log.class);
        ClassMetaData metadata = mock(ClassMetaData.class);

        when(repository.getConfiguration()).thenReturn(configuration);
        when(configuration.getLog(OpenJPAConfiguration.LOG_ENHANCE))
                .thenReturn(log);

        return new PCEnhancer(repository, tracker, metadata);
    }

    private static ClassNode accessFixtureClassNode() {
        return new EnhancementProject()
                .loadClass(AccessFixture.class)
                .getClassNode();
    }

    private static Method method(
            Class<?> declaringClass,
            String name,
            Class<?>... parameterTypes) throws NoSuchMethodException {

        return declaringClass.getDeclaredMethod(name, parameterTypes);
    }

    // TLLM-001
    @Test
    void tllm001_enhanceNoneHasExpectedValue() {
        assertEquals(0, PCEnhancer.ENHANCE_NONE);
    }

    // TLLM-002
    @Test
    void tllm002_enhanceAwareHasExpectedValue() {
        assertEquals(2, PCEnhancer.ENHANCE_AWARE);
    }

    // TLLM-003
    @Test
    void tllm003_enhanceInterfaceHasExpectedValue() {
        assertEquals(4, PCEnhancer.ENHANCE_INTERFACE);
    }

    // TLLM-004
    @Test
    void tllm004_enhancePcHasExpectedValue() {
        assertEquals(8, PCEnhancer.ENHANCE_PC);
    }

    // TLLM-005
    @Test
    void tllm005_generatedMemberPrefixIsPc() {
        assertEquals("pc", PCEnhancer.PRE);
    }

    // TLLM-006
    @Test
    void tllm006_recognizesValidPcSubclassName() {
        String className =
                "org.apache.openjpa.enhance.example$Entity$pcsubclass";

        assertTrue(PCEnhancer.isPCSubclassName(className));
    }

    // TLLM-007
    @Test
    void tllm007_rejectsPcSubclassSuffixOutsideEnhancePackage() {
        assertFalse(PCEnhancer.isPCSubclassName(
                "example.Entity$pcsubclass"));
    }

    // TLLM-008
    @Test
    void tllm008_rejectsEnhancePackageNameWithoutPcSubclassSuffix() {
        assertFalse(PCEnhancer.isPCSubclassName(
                "org.apache.openjpa.enhance.example$Entity"));
    }

    // TLLM-009
    @Test
    void tllm009_convertsPcSubclassNameToManagedTypeName() {
        String subclassName =
                "org.apache.openjpa.enhance.example$Entity$pcsubclass";

        assertEquals(
                "example.Entity",
                PCEnhancer.toManagedTypeName(subclassName));
    }

    // TLLM-010
    @Test
    void tllm010_preservesOrdinaryManagedTypeName() {
        assertEquals(
                "example.Entity",
                PCEnhancer.toManagedTypeName("example.Entity"));
    }

    // TLLM-011
    @Test
    void tllm011_addDefaultConstructorIsEnabledInitially() {
        PCEnhancer enhancer = newEnhancer();

        assertTrue(enhancer.getAddDefaultConstructor());
    }

    // TLLM-012
    @Test
    void tllm012_addDefaultConstructorCanBeDisabled() {
        PCEnhancer enhancer = newEnhancer();

        enhancer.setAddDefaultConstructor(false);

        assertFalse(enhancer.getAddDefaultConstructor());
    }

    // TLLM-013
    @Test
    void tllm013_redefineIsDisabledInitially() {
        PCEnhancer enhancer = newEnhancer();

        assertFalse(enhancer.getRedefine());
    }

    // TLLM-014
    @Test
    void tllm014_redefineCanBeEnabled() {
        PCEnhancer enhancer = newEnhancer();

        enhancer.setRedefine(true);

        assertTrue(enhancer.getRedefine());
    }

    // TLLM-015
    @Test
    void tllm015_createSubclassIsDisabledInitially() {
        PCEnhancer enhancer = newEnhancer();

        assertFalse(enhancer.getCreateSubclass());
    }

    // TLLM-016
    @Test
    void tllm016_createSubclassCanBeEnabled() {
        PCEnhancer enhancer = newEnhancer();

        enhancer.setCreateSubclass(true);

        assertTrue(enhancer.getCreateSubclass());
    }

    // TLLM-017
    @Test
    void tllm017_propertyRestrictionEnforcementIsDisabledInitially() {
        PCEnhancer enhancer = newEnhancer();

        assertFalse(enhancer.getEnforcePropertyRestrictions());
    }

    // TLLM-018
    @Test
    void tllm018_propertyRestrictionEnforcementCanBeEnabled() {
        PCEnhancer enhancer = newEnhancer();

        enhancer.setEnforcePropertyRestrictions(true);

        assertTrue(enhancer.getEnforcePropertyRestrictions());
    }

    // TLLM-019
    @Test
    void tllm019_directorySetterStoresTheSameFileInstance() {
        PCEnhancer enhancer = newEnhancer();
        File directory = new File("target/tllm-enhanced");

        enhancer.setDirectory(directory);

        assertSame(directory, enhancer.getDirectory());
    }

    // TLLM-020
    @Test
    void tllm020_bytecodeWriterSetterStoresTheSameWriterInstance() {
        PCEnhancer enhancer = newEnhancer();
        BytecodeWriter writer = mock(BytecodeWriter.class);

        enhancer.setBytecodeWriter(writer);

        assertSame(writer, enhancer.getBytecodeWriter());
    }

    // TLLM-021
    @Test
    void tllm021_getReturnedFieldFindsDirectlyReturnedField()
            throws Exception {

        ClassNode classNode = accessFixtureClassNode();
        Method getter = method(AccessFixture.class, "getValue");

        Field result = PCEnhancer.getReturnedField(classNode, getter);

        assertEquals(
                AccessFixture.class.getDeclaredField("value"),
                result);
    }

    // TLLM-022
    @Test
    void tllm022_getReturnedFieldSkipsCheckcast()
            throws Exception {

        ClassNode classNode = accessFixtureClassNode();
        Method getter = method(AccessFixture.class, "getCastedValue");

        Field result = PCEnhancer.getReturnedField(classNode, getter);

        assertEquals(
                AccessFixture.class.getDeclaredField("objectValue"),
                result);
    }

    // TLLM-023
    @Test
    void tllm023_getReturnedFieldRejectsLiteralReturn()
            throws Exception {

        ClassNode classNode = accessFixtureClassNode();
        Method getter = method(AccessFixture.class, "getLiteral");

        Field result = PCEnhancer.getReturnedField(classNode, getter);

        assertNull(result);
    }

    // TLLM-024
    @Test
    void tllm024_getReturnedFieldRejectsStaticGetter()
            throws Exception {

        ClassNode classNode = accessFixtureClassNode();
        Method getter = method(AccessFixture.class, "getStaticValue");

        Field result = PCEnhancer.getReturnedField(classNode, getter);

        assertNull(result);
    }

    // TLLM-025
    @Test
    void tllm025_getReturnedFieldRejectsInterfaceMethod()
            throws Exception {

        ClassNode classNode = new EnhancementProject()
                .loadClass(AccessFixtureInterface.class)
                .getClassNode();

        Method getter = method(
                AccessFixtureInterface.class,
                "getValue");

        Field result = PCEnhancer.getReturnedField(classNode, getter);

        assertNull(result);
    }

    // TLLM-026
    @Test
    void tllm026_getAssignedFieldFindsDirectSetterAssignment()
            throws Exception {

        ClassNode classNode = accessFixtureClassNode();
        Method setter = method(
                AccessFixture.class,
                "setValue",
                String.class);

        Field result = PCEnhancer.getAssignedField(classNode, setter);

        assertEquals(
                AccessFixture.class.getDeclaredField("value"),
                result);
    }

    // TLLM-027
    @Test
    void tllm027_getAssignedFieldRejectsTransformedArgument()
            throws Exception {

        ClassNode classNode = accessFixtureClassNode();
        Method setter = method(
                AccessFixture.class,
                "setIncrementedNumber",
                int.class);

        Field result = PCEnhancer.getAssignedField(classNode, setter);

        assertNull(result);
    }

    // TLLM-028
    @Test
    void tllm028_getAssignedFieldRejectsStaticSetter()
            throws Exception {

        ClassNode classNode = accessFixtureClassNode();
        Method setter = method(
                AccessFixture.class,
                "setStaticValue",
                String.class);

        Field result = PCEnhancer.getAssignedField(classNode, setter);

        assertNull(result);
    }

    // TLLM-029
    @Test
    void tllm029_getTypeUsesInterfaceImplementationWhenPresent() {
        PCEnhancer enhancer = newEnhancer();
        ClassMetaData metadata = mock(ClassMetaData.class);

        org.mockito.Mockito.doReturn(InterfaceImplementation.class)
                .when(metadata).getInterfaceImpl();
        org.mockito.Mockito.doReturn(ManagedType.class)
                .when(metadata).getDescribedType();

        Class<?> result = enhancer.getType(metadata);

        assertSame(InterfaceImplementation.class, result);
    }

    // TLLM-030
    @Test
    void tllm030_getTypeFallsBackToDescribedType() {
        PCEnhancer enhancer = newEnhancer();
        ClassMetaData metadata = mock(ClassMetaData.class);

        org.mockito.Mockito.doReturn(null)
                .when(metadata).getInterfaceImpl();
        org.mockito.Mockito.doReturn(ManagedType.class)
                .when(metadata).getDescribedType();

        Class<?> result = enhancer.getType(metadata);

        assertSame(ManagedType.class, result);
    }

}
