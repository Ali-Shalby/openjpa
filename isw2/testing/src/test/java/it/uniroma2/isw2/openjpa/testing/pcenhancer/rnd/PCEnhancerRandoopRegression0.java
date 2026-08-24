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

package it.uniroma2.isw2.openjpa.testing.pcenhancer.rnd;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PCEnhancerRandoopRegression0 {

    public static boolean debug = false;

    public void assertBooleanArrayEquals(boolean[] expectedArray, boolean[] actualArray) {
        if (expectedArray.length != actualArray.length) {
            throw new AssertionError("Array lengths differ: " + expectedArray.length + " != " + actualArray.length);
        }
        for (int i = 0; i < expectedArray.length; i++) {
            if (expectedArray[i] != actualArray[i]) {
                throw new AssertionError("Arrays differ at index " + i + ": " + expectedArray[i] + " != " + actualArray[i]);
            }
        }
    }

    @Test
    public void test01() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test01");
        org.apache.openjpa.conf.OpenJPAConfiguration openJPAConfiguration0 = null;
        java.lang.String[] strArray1 = new java.lang.String[] {};
        org.apache.openjpa.enhance.PCEnhancer.Flags flags2 = null;
        org.apache.openjpa.meta.MetaDataRepository metaDataRepository3 = null;
        org.apache.openjpa.util.asm.BytecodeWriter bytecodeWriter4 = null;
        java.lang.ClassLoader classLoader5 = null;
        // The following exception was thrown during execution in test generation
        try {
            boolean boolean6 = org.apache.openjpa.enhance.PCEnhancer.run(openJPAConfiguration0, strArray1, flags2, metaDataRepository3, bytecodeWriter4, classLoader5);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.conf.OpenJPAConfiguration.getClassResolverInstance()\" because \"conf\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray1);
        org.junit.Assert.assertArrayEquals(strArray1, new java.lang.String[] {});
    }

    @Test
    public void test02() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test02");
        org.apache.openjpa.conf.OpenJPAConfiguration openJPAConfiguration0 = null;
        org.apache.openjpa.meta.ClassMetaData classMetaData1 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.openjpa.enhance.PCEnhancer pCEnhancer2 = new org.apache.openjpa.enhance.PCEnhancer(openJPAConfiguration0, classMetaData1);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.meta.ClassMetaData.getDescribedType()\" because \"meta\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test03() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test03");
        java.lang.String str1 = org.apache.openjpa.enhance.PCEnhancer.toManagedTypeName("");
        org.junit.Assert.assertEquals("'" + str1 + "' != '" + "" + "'", str1, "");
    }

    @Test
    public void test04() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test04");
        org.apache.openjpa.conf.OpenJPAConfiguration openJPAConfiguration0 = null;
        java.lang.String[] strArray1 = null;
        org.apache.openjpa.enhance.PCEnhancer.Flags flags2 = null;
        org.apache.openjpa.meta.MetaDataRepository metaDataRepository3 = null;
        org.apache.openjpa.util.asm.BytecodeWriter bytecodeWriter4 = null;
        java.lang.ClassLoader classLoader5 = null;
        // The following exception was thrown during execution in test generation
        try {
            boolean boolean6 = org.apache.openjpa.enhance.PCEnhancer.run(openJPAConfiguration0, strArray1, flags2, metaDataRepository3, bytecodeWriter4, classLoader5);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.conf.OpenJPAConfiguration.getClassResolverInstance()\" because \"conf\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test05() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test05");
        boolean boolean1 = org.apache.openjpa.enhance.PCEnhancer.isPCSubclassName("hi!");
        org.junit.Assert.assertTrue("'" + boolean1 + "' != '" + false + "'", boolean1 == false);
    }

    @Test
    public void test06() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test06");
        int int0 = org.apache.openjpa.enhance.PCEnhancer.ENHANCE_AWARE;
        org.junit.Assert.assertTrue("'" + int0 + "' != '" + 2 + "'", int0 == 2);
    }

    @Test
    public void test07() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test07");
        int int0 = org.apache.openjpa.enhance.PCEnhancer.ENHANCE_NONE;
        org.junit.Assert.assertTrue("'" + int0 + "' != '" + 0 + "'", int0 == 0);
    }

    @Test
    public void test08() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test08");
        org.apache.openjpa.conf.OpenJPAConfiguration openJPAConfiguration0 = null;
        java.lang.String[] strArray3 = new java.lang.String[] { "hi!", "" };
        org.apache.openjpa.lib.util.Options options4 = null;
        // The following exception was thrown during execution in test generation
        try {
            boolean boolean5 = org.apache.openjpa.enhance.PCEnhancer.run(openJPAConfiguration0, strArray3, options4);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.lib.util.Options.removeProperty(String, String, String)\" because \"opts\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray3);
        org.junit.Assert.assertArrayEquals(strArray3, new java.lang.String[] { "hi!", "" });
    }

    @Test
    public void test09() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test09");
        java.lang.String[] strArray3 = new java.lang.String[] { "hi!", "", "hi!" };
        org.apache.openjpa.lib.util.Options options4 = null;
        // The following exception was thrown during execution in test generation
        try {
            boolean boolean5 = org.apache.openjpa.enhance.PCEnhancer.run(strArray3, options4);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.lib.util.Options.containsKey(Object)\" because \"opts\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray3);
        org.junit.Assert.assertArrayEquals(strArray3, new java.lang.String[] { "hi!", "", "hi!" });
    }

    @Test
    public void test10() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test10");
        org.apache.openjpa.meta.MetaDataRepository metaDataRepository0 = null;
        org.apache.openjpa.util.asm.ClassNodeTracker classNodeTracker1 = null;
        org.apache.openjpa.meta.ClassMetaData classMetaData2 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.openjpa.enhance.PCEnhancer pCEnhancer3 = new org.apache.openjpa.enhance.PCEnhancer(metaDataRepository0, classNodeTracker1, classMetaData2);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.util.asm.ClassNodeTracker.getProject()\" because \"type\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test11() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test11");
        java.lang.String str1 = org.apache.openjpa.enhance.PCEnhancer.toManagedTypeName("hi!");
        org.junit.Assert.assertEquals("'" + str1 + "' != '" + "hi!" + "'", str1, "hi!");
    }

    @Test
    public void test12() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test12");
        int int0 = org.apache.openjpa.enhance.PCEnhancer.ENHANCE_PC;
        org.junit.Assert.assertTrue("'" + int0 + "' != '" + 8 + "'", int0 == 8);
    }

    @Test
    public void test13() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test13");
        java.lang.String str0 = org.apache.openjpa.enhance.PCEnhancer.PRE;
        org.junit.Assert.assertEquals("'" + str0 + "' != '" + "pc" + "'", str0, "pc");
    }

    @Test
    public void test14() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test14");
        org.apache.openjpa.conf.OpenJPAConfiguration openJPAConfiguration0 = null;
        org.apache.openjpa.util.asm.ClassNodeTracker classNodeTracker1 = null;
        org.apache.openjpa.meta.MetaDataRepository metaDataRepository2 = null;
        java.lang.ClassLoader classLoader3 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.openjpa.enhance.PCEnhancer pCEnhancer4 = new org.apache.openjpa.enhance.PCEnhancer(openJPAConfiguration0, classNodeTracker1, metaDataRepository2, classLoader3);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.util.asm.ClassNodeTracker.getProject()\" because \"type\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test15() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test15");
        boolean boolean1 = org.apache.openjpa.enhance.PCEnhancer.isPCSubclassName("pc");
        org.junit.Assert.assertTrue("'" + boolean1 + "' != '" + false + "'", boolean1 == false);
    }

    @Test
    public void test16() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test16");
        org.apache.openjpa.conf.OpenJPAConfiguration openJPAConfiguration0 = null;
        org.apache.openjpa.util.asm.ClassNodeTracker classNodeTracker1 = null;
        org.apache.openjpa.meta.MetaDataRepository metaDataRepository2 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.openjpa.enhance.PCEnhancer pCEnhancer3 = new org.apache.openjpa.enhance.PCEnhancer(openJPAConfiguration0, classNodeTracker1, metaDataRepository2);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.util.asm.ClassNodeTracker.getProject()\" because \"type\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test17() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test17");
        int int0 = org.apache.openjpa.enhance.PCEnhancer.ENHANCE_INTERFACE;
        org.junit.Assert.assertTrue("'" + int0 + "' != '" + 4 + "'", int0 == 4);
    }

    @Test
    public void test18() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test18");
        java.lang.String str1 = org.apache.openjpa.enhance.PCEnhancer.toManagedTypeName("pc");
        org.junit.Assert.assertEquals("'" + str1 + "' != '" + "pc" + "'", str1, "pc");
    }

    @Test
    public void test19() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test19");
        boolean boolean1 = org.apache.openjpa.enhance.PCEnhancer.isPCSubclassName("");
        org.junit.Assert.assertTrue("'" + boolean1 + "' != '" + false + "'", boolean1 == false);
    }

    @Test
    public void test20() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test20");
        java.lang.String[] strArray2 = new java.lang.String[] { "hi!", "pc" };
        org.apache.openjpa.lib.util.Options options3 = null;
        // The following exception was thrown during execution in test generation
        try {
            boolean boolean4 = org.apache.openjpa.enhance.PCEnhancer.run(strArray2, options3);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.lib.util.Options.containsKey(Object)\" because \"opts\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray2);
        org.junit.Assert.assertArrayEquals(strArray2, new java.lang.String[] { "hi!", "pc" });
    }

    @Test
    public void test21() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test21");
        org.apache.openjpa.conf.OpenJPAConfiguration openJPAConfiguration0 = null;
        java.lang.String[] strArray4 = new java.lang.String[] { "hi!", "", "" };
        org.apache.openjpa.lib.util.Options options5 = null;
        // The following exception was thrown during execution in test generation
        try {
            boolean boolean6 = org.apache.openjpa.enhance.PCEnhancer.run(openJPAConfiguration0, strArray4, options5);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.lib.util.Options.removeProperty(String, String, String)\" because \"opts\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray4);
        org.junit.Assert.assertArrayEquals(strArray4, new java.lang.String[] { "hi!", "", "" });
    }

    @Test
    public void test22() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test22");
        org.apache.openjpa.conf.OpenJPAConfiguration openJPAConfiguration0 = null;
        java.lang.String[] strArray3 = new java.lang.String[] { "hi!", "pc" };
        org.apache.openjpa.enhance.PCEnhancer.Flags flags4 = new org.apache.openjpa.enhance.PCEnhancer.Flags();
        boolean boolean5 = flags4.tmpClassLoader;
        java.io.File file6 = flags4.directory;
        java.io.File file7 = null;
        flags4.directory = file7;
        org.apache.openjpa.meta.MetaDataRepository metaDataRepository9 = null;
        org.apache.openjpa.util.asm.BytecodeWriter bytecodeWriter10 = null;
        java.lang.ClassLoader classLoader11 = null;
        // The following exception was thrown during execution in test generation
        try {
            boolean boolean12 = org.apache.openjpa.enhance.PCEnhancer.run(openJPAConfiguration0, strArray3, flags4, metaDataRepository9, bytecodeWriter10, classLoader11);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.conf.OpenJPAConfiguration.getClassResolverInstance()\" because \"conf\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray3);
        org.junit.Assert.assertArrayEquals(strArray3, new java.lang.String[] { "hi!", "pc" });
        org.junit.Assert.assertTrue("'" + boolean5 + "' != '" + true + "'", boolean5 == true);
        org.junit.Assert.assertNull(file6);
    }

    @Test
    public void test23() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test23");
        org.apache.openjpa.conf.OpenJPAConfiguration openJPAConfiguration0 = null;
        java.lang.String[] strArray7 = new java.lang.String[] { "", "", "hi!", "pc", "hi!", "" };
        org.apache.openjpa.lib.util.Options options8 = null;
        // The following exception was thrown during execution in test generation
        try {
            boolean boolean9 = org.apache.openjpa.enhance.PCEnhancer.run(openJPAConfiguration0, strArray7, options8);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.lib.util.Options.removeProperty(String, String, String)\" because \"opts\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray7);
        org.junit.Assert.assertArrayEquals(strArray7, new java.lang.String[] { "", "", "hi!", "pc", "hi!", "" });
    }

    @Test
    public void test24() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test24");
        int int0 = org.apache.openjpa.enhance.PCEnhancer.ENHANCER_VERSION;
        org.junit.Assert.assertTrue("'" + int0 + "' != '" + 255933283 + "'", int0 == 255933283);
    }

    @Test
    public void test25() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test25");
        org.apache.openjpa.conf.OpenJPAConfiguration openJPAConfiguration0 = null;
        java.lang.String[] strArray1 = null;
        org.apache.openjpa.enhance.PCEnhancer.Flags flags2 = new org.apache.openjpa.enhance.PCEnhancer.Flags();
        boolean boolean3 = flags2.tmpClassLoader;
        boolean boolean4 = flags2.enforcePropertyRestrictions;
        boolean boolean5 = flags2.enforcePropertyRestrictions;
        org.apache.openjpa.meta.MetaDataRepository metaDataRepository6 = null;
        org.apache.openjpa.util.asm.BytecodeWriter bytecodeWriter7 = null;
        java.lang.ClassLoader classLoader8 = null;
        // The following exception was thrown during execution in test generation
        try {
            boolean boolean9 = org.apache.openjpa.enhance.PCEnhancer.run(openJPAConfiguration0, strArray1, flags2, metaDataRepository6, bytecodeWriter7, classLoader8);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.conf.OpenJPAConfiguration.getClassResolverInstance()\" because \"conf\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean3 + "' != '" + true + "'", boolean3 == true);
        org.junit.Assert.assertTrue("'" + boolean4 + "' != '" + false + "'", boolean4 == false);
        org.junit.Assert.assertTrue("'" + boolean5 + "' != '" + false + "'", boolean5 == false);
    }

    @Test
    public void test26() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test26");
        java.lang.String str0 = org.apache.openjpa.enhance.PCEnhancer.ISDETACHEDSTATEDEFINITIVE;
        org.junit.Assert.assertEquals("'" + str0 + "' != '" + "pcisDetachedStateDefinitive" + "'", str0, "pcisDetachedStateDefinitive");
    }

    @Test
    public void test27() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test27");
        org.apache.openjpa.conf.OpenJPAConfiguration openJPAConfiguration0 = null;
        java.lang.String[] strArray1 = new java.lang.String[] {};
        org.apache.openjpa.enhance.PCEnhancer.Flags flags2 = new org.apache.openjpa.enhance.PCEnhancer.Flags();
        boolean boolean3 = flags2.tmpClassLoader;
        boolean boolean4 = flags2.enforcePropertyRestrictions;
        java.io.File file5 = flags2.directory;
        org.apache.openjpa.meta.MetaDataRepository metaDataRepository6 = null;
        org.apache.openjpa.util.asm.BytecodeWriter bytecodeWriter7 = null;
        java.lang.ClassLoader classLoader8 = null;
        // The following exception was thrown during execution in test generation
        try {
            boolean boolean9 = org.apache.openjpa.enhance.PCEnhancer.run(openJPAConfiguration0, strArray1, flags2, metaDataRepository6, bytecodeWriter7, classLoader8);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.conf.OpenJPAConfiguration.getClassResolverInstance()\" because \"conf\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray1);
        org.junit.Assert.assertArrayEquals(strArray1, new java.lang.String[] {});
        org.junit.Assert.assertTrue("'" + boolean3 + "' != '" + true + "'", boolean3 == true);
        org.junit.Assert.assertTrue("'" + boolean4 + "' != '" + false + "'", boolean4 == false);
        org.junit.Assert.assertNull(file5);
    }

    @Test
    public void test28() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test28");
        org.apache.openjpa.conf.OpenJPAConfiguration openJPAConfiguration0 = null;
        java.lang.String[] strArray4 = new java.lang.String[] { "pc", "hi!", "pcisDetachedStateDefinitive" };
        org.apache.openjpa.enhance.PCEnhancer.Flags flags5 = new org.apache.openjpa.enhance.PCEnhancer.Flags();
        boolean boolean6 = flags5.tmpClassLoader;
        boolean boolean7 = flags5.enforcePropertyRestrictions;
        flags5.tmpClassLoader = false;
        org.apache.openjpa.meta.MetaDataRepository metaDataRepository10 = null;
        org.apache.openjpa.util.asm.BytecodeWriter bytecodeWriter11 = null;
        java.lang.ClassLoader classLoader12 = null;
        // The following exception was thrown during execution in test generation
        try {
            boolean boolean13 = org.apache.openjpa.enhance.PCEnhancer.run(openJPAConfiguration0, strArray4, flags5, metaDataRepository10, bytecodeWriter11, classLoader12);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.conf.OpenJPAConfiguration.getClassResolverInstance()\" because \"conf\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray4);
        org.junit.Assert.assertArrayEquals(strArray4, new java.lang.String[] { "pc", "hi!", "pcisDetachedStateDefinitive" });
        org.junit.Assert.assertTrue("'" + boolean6 + "' != '" + true + "'", boolean6 == true);
        org.junit.Assert.assertTrue("'" + boolean7 + "' != '" + false + "'", boolean7 == false);
    }

    @Test
    public void test29() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test29");
        java.lang.String[] strArray6 = new java.lang.String[] { "hi!", "pc", "pcisDetachedStateDefinitive", "", "pcisDetachedStateDefinitive", "pcisDetachedStateDefinitive" };
        org.apache.openjpa.lib.util.Options options7 = null;
        // The following exception was thrown during execution in test generation
        try {
            boolean boolean8 = org.apache.openjpa.enhance.PCEnhancer.run(strArray6, options7);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.lib.util.Options.containsKey(Object)\" because \"opts\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray6);
        org.junit.Assert.assertArrayEquals(strArray6, new java.lang.String[] { "hi!", "pc", "pcisDetachedStateDefinitive", "", "pcisDetachedStateDefinitive", "pcisDetachedStateDefinitive" });
    }

    @Test
    public void test30() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "PCEnhancerRandoopRegression0.test30");
        java.lang.String[] strArray5 = new java.lang.String[] { "pcisDetachedStateDefinitive", "", "", "pc", "pcisDetachedStateDefinitive" };
        org.apache.openjpa.lib.util.Options options6 = null;
        // The following exception was thrown during execution in test generation
        try {
            boolean boolean7 = org.apache.openjpa.enhance.PCEnhancer.run(strArray5, options6);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.lib.util.Options.containsKey(Object)\" because \"opts\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray5);
        org.junit.Assert.assertArrayEquals(strArray5, new java.lang.String[] { "pcisDetachedStateDefinitive", "", "", "pc", "pcisDetachedStateDefinitive" });
    }
}
