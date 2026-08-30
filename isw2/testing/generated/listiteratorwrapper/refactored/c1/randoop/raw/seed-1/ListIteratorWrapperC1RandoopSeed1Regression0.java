import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ListIteratorWrapperC1RandoopSeed1Regression0 {

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
    public void test1() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperC1RandoopSeed1Regression0.test1");
        java.lang.String[] strArray1 = new java.lang.String[] { "" };
        java.util.ArrayList<java.lang.String> strList2 = new java.util.ArrayList<java.lang.String>();
        boolean boolean3 = java.util.Collections.addAll((java.util.Collection<java.lang.String>) strList2, strArray1);
        java.util.ArrayList<java.lang.String> strList5 = new java.util.ArrayList<java.lang.String>((int) (short) 100);
        boolean boolean6 = strList2.containsAll((java.util.Collection<java.lang.String>) strList5);
        java.util.Iterator<java.lang.String> strItor7 = strList2.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.String> strItor8 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.String>(strItor7);
        // The following exception was thrown during execution in test generation
        try {
            strItor8.remove();
            org.junit.Assert.fail("Expected exception of type java.lang.IllegalStateException; message: Cannot remove element at index -1.");
        } catch (java.lang.IllegalStateException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray1);
        org.junit.Assert.assertArrayEquals(strArray1, new java.lang.String[] { "" });
        org.junit.Assert.assertTrue("'" + boolean3 + "' != '" + true + "'", boolean3 == true);
        org.junit.Assert.assertTrue("'" + boolean6 + "' != '" + true + "'", boolean6 == true);
        org.junit.Assert.assertNotNull(strItor7);
    }

    @Test
    public void test2() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperC1RandoopSeed1Regression0.test2");
        java.lang.String[] strArray1 = new java.lang.String[] { "" };
        java.util.ArrayList<java.lang.String> strList2 = new java.util.ArrayList<java.lang.String>();
        boolean boolean3 = java.util.Collections.addAll((java.util.Collection<java.lang.String>) strList2, strArray1);
        java.util.ArrayList<java.lang.String> strList5 = new java.util.ArrayList<java.lang.String>((int) (short) 100);
        boolean boolean6 = strList2.containsAll((java.util.Collection<java.lang.String>) strList5);
        java.util.Iterator<java.lang.String> strItor7 = strList2.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.String> strItor8 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.String>(strItor7);
        java.lang.Class<?> wildcardClass9 = strItor8.getClass();
        org.junit.Assert.assertNotNull(strArray1);
        org.junit.Assert.assertArrayEquals(strArray1, new java.lang.String[] { "" });
        org.junit.Assert.assertTrue("'" + boolean3 + "' != '" + true + "'", boolean3 == true);
        org.junit.Assert.assertTrue("'" + boolean6 + "' != '" + true + "'", boolean6 == true);
        org.junit.Assert.assertNotNull(strItor7);
        org.junit.Assert.assertNotNull(wildcardClass9);
    }

    @Test
    public void test3() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperC1RandoopSeed1Regression0.test3");
        java.lang.String[] strArray1 = new java.lang.String[] { "" };
        java.util.ArrayList<java.lang.String> strList2 = new java.util.ArrayList<java.lang.String>();
        boolean boolean3 = java.util.Collections.addAll((java.util.Collection<java.lang.String>) strList2, strArray1);
        java.util.ArrayList<java.lang.String> strList5 = new java.util.ArrayList<java.lang.String>((int) (short) 100);
        boolean boolean6 = strList2.containsAll((java.util.Collection<java.lang.String>) strList5);
        java.util.Iterator<java.lang.String> strItor7 = strList2.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Comparable<java.lang.String>> strComparableItor8 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Comparable<java.lang.String>>(strItor7);
        org.junit.Assert.assertNotNull(strArray1);
        org.junit.Assert.assertArrayEquals(strArray1, new java.lang.String[] { "" });
        org.junit.Assert.assertTrue("'" + boolean3 + "' != '" + true + "'", boolean3 == true);
        org.junit.Assert.assertTrue("'" + boolean6 + "' != '" + true + "'", boolean6 == true);
        org.junit.Assert.assertNotNull(strItor7);
    }

    @Test
    public void test4() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperC1RandoopSeed1Regression0.test4");
        java.lang.String[] strArray1 = new java.lang.String[] { "hi!" };
        java.util.ArrayList<java.lang.String> strList2 = new java.util.ArrayList<java.lang.String>();
        boolean boolean3 = java.util.Collections.addAll((java.util.Collection<java.lang.String>) strList2, strArray1);
        java.lang.String str4 = strList2.toString();
        java.lang.String str5 = strList2.toString();
        java.lang.String[] strArray7 = new java.lang.String[] { "" };
        java.util.ArrayList<java.lang.String> strList8 = new java.util.ArrayList<java.lang.String>();
        boolean boolean9 = java.util.Collections.addAll((java.util.Collection<java.lang.String>) strList8, strArray7);
        java.util.ArrayList<java.lang.String> strList11 = new java.util.ArrayList<java.lang.String>((int) (short) 100);
        boolean boolean12 = strList8.containsAll((java.util.Collection<java.lang.String>) strList11);
        java.util.Iterator<java.lang.String> strItor13 = strList8.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.String> strItor14 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.String>(strItor13);
        boolean boolean15 = strList2.contains((java.lang.Object) strItor14);
        // The following exception was thrown during execution in test generation
        try {
            strItor14.set("[hi!]");
            org.junit.Assert.fail("Expected exception of type java.lang.UnsupportedOperationException; message: ListIteratorWrapper does not support optional operations of ListIterator.");
        } catch (java.lang.UnsupportedOperationException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray1);
        org.junit.Assert.assertArrayEquals(strArray1, new java.lang.String[] { "hi!" });
        org.junit.Assert.assertTrue("'" + boolean3 + "' != '" + true + "'", boolean3 == true);
        org.junit.Assert.assertEquals("'" + str4 + "' != '" + "[hi!]" + "'", str4, "[hi!]");
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "[hi!]" + "'", str5, "[hi!]");
        org.junit.Assert.assertNotNull(strArray7);
        org.junit.Assert.assertArrayEquals(strArray7, new java.lang.String[] { "" });
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + true + "'", boolean9 == true);
        org.junit.Assert.assertTrue("'" + boolean12 + "' != '" + true + "'", boolean12 == true);
        org.junit.Assert.assertNotNull(strItor13);
        org.junit.Assert.assertTrue("'" + boolean15 + "' != '" + false + "'", boolean15 == false);
    }

    @Test
    public void test5() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperC1RandoopSeed1Regression0.test5");
        java.lang.String[] strArray1 = new java.lang.String[] { "hi!" };
        java.util.ArrayList<java.lang.String> strList2 = new java.util.ArrayList<java.lang.String>();
        boolean boolean3 = java.util.Collections.addAll((java.util.Collection<java.lang.String>) strList2, strArray1);
        java.lang.String str4 = strList2.toString();
        java.lang.String str5 = strList2.toString();
        java.lang.String[] strArray7 = new java.lang.String[] { "" };
        java.util.ArrayList<java.lang.String> strList8 = new java.util.ArrayList<java.lang.String>();
        boolean boolean9 = java.util.Collections.addAll((java.util.Collection<java.lang.String>) strList8, strArray7);
        java.util.ArrayList<java.lang.String> strList11 = new java.util.ArrayList<java.lang.String>((int) (short) 100);
        boolean boolean12 = strList8.containsAll((java.util.Collection<java.lang.String>) strList11);
        java.util.Iterator<java.lang.String> strItor13 = strList8.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.String> strItor14 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.String>(strItor13);
        boolean boolean15 = strList2.contains((java.lang.Object) strItor14);
        boolean boolean16 = strItor14.hasPrevious();
        org.junit.Assert.assertNotNull(strArray1);
        org.junit.Assert.assertArrayEquals(strArray1, new java.lang.String[] { "hi!" });
        org.junit.Assert.assertTrue("'" + boolean3 + "' != '" + true + "'", boolean3 == true);
        org.junit.Assert.assertEquals("'" + str4 + "' != '" + "[hi!]" + "'", str4, "[hi!]");
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "[hi!]" + "'", str5, "[hi!]");
        org.junit.Assert.assertNotNull(strArray7);
        org.junit.Assert.assertArrayEquals(strArray7, new java.lang.String[] { "" });
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + true + "'", boolean9 == true);
        org.junit.Assert.assertTrue("'" + boolean12 + "' != '" + true + "'", boolean12 == true);
        org.junit.Assert.assertNotNull(strItor13);
        org.junit.Assert.assertTrue("'" + boolean15 + "' != '" + false + "'", boolean15 == false);
        org.junit.Assert.assertTrue("'" + boolean16 + "' != '" + false + "'", boolean16 == false);
    }

    @Test
    public void test6() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperC1RandoopSeed1Regression0.test6");
        java.lang.String[] strArray1 = new java.lang.String[] { "hi!" };
        java.util.ArrayList<java.lang.String> strList2 = new java.util.ArrayList<java.lang.String>();
        boolean boolean3 = java.util.Collections.addAll((java.util.Collection<java.lang.String>) strList2, strArray1);
        java.lang.String str4 = strList2.toString();
        java.lang.String str5 = strList2.toString();
        java.lang.String[] strArray7 = new java.lang.String[] { "" };
        java.util.ArrayList<java.lang.String> strList8 = new java.util.ArrayList<java.lang.String>();
        boolean boolean9 = java.util.Collections.addAll((java.util.Collection<java.lang.String>) strList8, strArray7);
        java.util.ArrayList<java.lang.String> strList11 = new java.util.ArrayList<java.lang.String>((int) (short) 100);
        boolean boolean12 = strList8.containsAll((java.util.Collection<java.lang.String>) strList11);
        java.util.Iterator<java.lang.String> strItor13 = strList8.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.String> strItor14 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.String>(strItor13);
        boolean boolean15 = strList2.contains((java.lang.Object) strItor14);
        // The following exception was thrown during execution in test generation
        try {
            strItor14.add("hi!");
            org.junit.Assert.fail("Expected exception of type java.lang.UnsupportedOperationException; message: ListIteratorWrapper does not support optional operations of ListIterator.");
        } catch (java.lang.UnsupportedOperationException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray1);
        org.junit.Assert.assertArrayEquals(strArray1, new java.lang.String[] { "hi!" });
        org.junit.Assert.assertTrue("'" + boolean3 + "' != '" + true + "'", boolean3 == true);
        org.junit.Assert.assertEquals("'" + str4 + "' != '" + "[hi!]" + "'", str4, "[hi!]");
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "[hi!]" + "'", str5, "[hi!]");
        org.junit.Assert.assertNotNull(strArray7);
        org.junit.Assert.assertArrayEquals(strArray7, new java.lang.String[] { "" });
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + true + "'", boolean9 == true);
        org.junit.Assert.assertTrue("'" + boolean12 + "' != '" + true + "'", boolean12 == true);
        org.junit.Assert.assertNotNull(strItor13);
        org.junit.Assert.assertTrue("'" + boolean15 + "' != '" + false + "'", boolean15 == false);
    }

    @Test
    public void test7() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperC1RandoopSeed1Regression0.test7");
        java.lang.String[] strArray3 = new java.lang.String[] { "hi!", "", "[hi!]" };
        java.util.ArrayList<java.lang.String> strList4 = new java.util.ArrayList<java.lang.String>();
        boolean boolean5 = java.util.Collections.addAll((java.util.Collection<java.lang.String>) strList4, strArray3);
        java.util.ArrayList<java.lang.String> strList7 = new java.util.ArrayList<java.lang.String>((int) (short) 100);
        int int9 = strList7.indexOf((java.lang.Object) false);
        java.lang.String[] strArray11 = new java.lang.String[] { "" };
        java.util.ArrayList<java.lang.String> strList12 = new java.util.ArrayList<java.lang.String>();
        boolean boolean13 = java.util.Collections.addAll((java.util.Collection<java.lang.String>) strList12, strArray11);
        java.util.ArrayList<java.lang.String> strList15 = new java.util.ArrayList<java.lang.String>((int) (short) 100);
        boolean boolean16 = strList12.containsAll((java.util.Collection<java.lang.String>) strList15);
        boolean boolean17 = strList7.addAll((java.util.Collection<java.lang.String>) strList12);
        boolean boolean18 = strList4.retainAll((java.util.Collection<java.lang.String>) strList7);
        strList7.ensureCapacity((int) (byte) 1);
        java.lang.Object obj21 = strList7.clone();
        java.util.ArrayList<java.lang.String> strList23 = new java.util.ArrayList<java.lang.String>((int) (short) 100);
        java.util.ListIterator<java.lang.String> strItor24 = strList23.listIterator();
        boolean boolean25 = strList7.containsAll((java.util.Collection<java.lang.String>) strList23);
        java.lang.String[] strArray27 = new java.lang.String[] { "hi!" };
        java.util.ArrayList<java.lang.String> strList28 = new java.util.ArrayList<java.lang.String>();
        boolean boolean29 = java.util.Collections.addAll((java.util.Collection<java.lang.String>) strList28, strArray27);
        java.lang.String str30 = strList28.toString();
        java.lang.String str31 = strList28.toString();
        java.lang.String[] strArray33 = new java.lang.String[] { "" };
        java.util.ArrayList<java.lang.String> strList34 = new java.util.ArrayList<java.lang.String>();
        boolean boolean35 = java.util.Collections.addAll((java.util.Collection<java.lang.String>) strList34, strArray33);
        java.util.ArrayList<java.lang.String> strList37 = new java.util.ArrayList<java.lang.String>((int) (short) 100);
        boolean boolean38 = strList34.containsAll((java.util.Collection<java.lang.String>) strList37);
        java.util.Iterator<java.lang.String> strItor39 = strList34.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.String> strItor40 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.String>(strItor39);
        boolean boolean41 = strList28.contains((java.lang.Object) strItor40);
        boolean boolean42 = strList23.addAll((java.util.Collection<java.lang.String>) strList28);
        // The following exception was thrown during execution in test generation
        try {
            java.util.ListIterator<java.lang.String> strItor44 = strList23.listIterator((int) '4');
            org.junit.Assert.fail("Expected exception of type java.lang.IndexOutOfBoundsException; message: Index: 52, Size: 1");
        } catch (java.lang.IndexOutOfBoundsException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray3);
        org.junit.Assert.assertArrayEquals(strArray3, new java.lang.String[] { "hi!", "", "[hi!]" });
        org.junit.Assert.assertTrue("'" + boolean5 + "' != '" + true + "'", boolean5 == true);
        org.junit.Assert.assertTrue("'" + int9 + "' != '" + (-1) + "'", int9 == (-1));
        org.junit.Assert.assertNotNull(strArray11);
        org.junit.Assert.assertArrayEquals(strArray11, new java.lang.String[] { "" });
        org.junit.Assert.assertTrue("'" + boolean13 + "' != '" + true + "'", boolean13 == true);
        org.junit.Assert.assertTrue("'" + boolean16 + "' != '" + true + "'", boolean16 == true);
        org.junit.Assert.assertTrue("'" + boolean17 + "' != '" + true + "'", boolean17 == true);
        org.junit.Assert.assertTrue("'" + boolean18 + "' != '" + true + "'", boolean18 == true);
        org.junit.Assert.assertNotNull(obj21);
        org.junit.Assert.assertEquals(obj21.toString(), "[]");
        org.junit.Assert.assertEquals(java.lang.String.valueOf(obj21), "[]");
        org.junit.Assert.assertEquals(java.util.Objects.toString(obj21), "[]");
        org.junit.Assert.assertNotNull(strItor24);
        org.junit.Assert.assertTrue("'" + boolean25 + "' != '" + true + "'", boolean25 == true);
        org.junit.Assert.assertNotNull(strArray27);
        org.junit.Assert.assertArrayEquals(strArray27, new java.lang.String[] { "hi!" });
        org.junit.Assert.assertTrue("'" + boolean29 + "' != '" + true + "'", boolean29 == true);
        org.junit.Assert.assertEquals("'" + str30 + "' != '" + "[hi!]" + "'", str30, "[hi!]");
        org.junit.Assert.assertEquals("'" + str31 + "' != '" + "[hi!]" + "'", str31, "[hi!]");
        org.junit.Assert.assertNotNull(strArray33);
        org.junit.Assert.assertArrayEquals(strArray33, new java.lang.String[] { "" });
        org.junit.Assert.assertTrue("'" + boolean35 + "' != '" + true + "'", boolean35 == true);
        org.junit.Assert.assertTrue("'" + boolean38 + "' != '" + true + "'", boolean38 == true);
        org.junit.Assert.assertNotNull(strItor39);
        org.junit.Assert.assertTrue("'" + boolean41 + "' != '" + false + "'", boolean41 == false);
        org.junit.Assert.assertTrue("'" + boolean42 + "' != '" + true + "'", boolean42 == true);
    }

    @Test
    public void test8() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperC1RandoopSeed1Regression0.test8");
        java.lang.String[] strArray1 = new java.lang.String[] { "hi!" };
        java.util.ArrayList<java.lang.String> strList2 = new java.util.ArrayList<java.lang.String>();
        boolean boolean3 = java.util.Collections.addAll((java.util.Collection<java.lang.String>) strList2, strArray1);
        java.lang.String str4 = strList2.toString();
        java.lang.String str5 = strList2.toString();
        java.lang.String[] strArray7 = new java.lang.String[] { "" };
        java.util.ArrayList<java.lang.String> strList8 = new java.util.ArrayList<java.lang.String>();
        boolean boolean9 = java.util.Collections.addAll((java.util.Collection<java.lang.String>) strList8, strArray7);
        java.util.ArrayList<java.lang.String> strList11 = new java.util.ArrayList<java.lang.String>((int) (short) 100);
        boolean boolean12 = strList8.containsAll((java.util.Collection<java.lang.String>) strList11);
        java.util.Iterator<java.lang.String> strItor13 = strList8.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.String> strItor14 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.String>(strItor13);
        boolean boolean15 = strList2.contains((java.lang.Object) strItor14);
        strItor14.reset();
        int int17 = strItor14.previousIndex();
        org.junit.Assert.assertNotNull(strArray1);
        org.junit.Assert.assertArrayEquals(strArray1, new java.lang.String[] { "hi!" });
        org.junit.Assert.assertTrue("'" + boolean3 + "' != '" + true + "'", boolean3 == true);
        org.junit.Assert.assertEquals("'" + str4 + "' != '" + "[hi!]" + "'", str4, "[hi!]");
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "[hi!]" + "'", str5, "[hi!]");
        org.junit.Assert.assertNotNull(strArray7);
        org.junit.Assert.assertArrayEquals(strArray7, new java.lang.String[] { "" });
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + true + "'", boolean9 == true);
        org.junit.Assert.assertTrue("'" + boolean12 + "' != '" + true + "'", boolean12 == true);
        org.junit.Assert.assertNotNull(strItor13);
        org.junit.Assert.assertTrue("'" + boolean15 + "' != '" + false + "'", boolean15 == false);
        org.junit.Assert.assertTrue("'" + int17 + "' != '" + (-1) + "'", int17 == (-1));
    }
}

