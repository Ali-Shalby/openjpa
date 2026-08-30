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

}
