import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ListIteratorWrapperC3RandoopSeed0Regression0 {

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
            System.out.format("%n%s%n", "ListIteratorWrapperC3RandoopSeed0Regression0.test1");
        java.util.ArrayList<java.lang.constant.Constable> constableList0 = new java.util.ArrayList<java.lang.constant.Constable>();
        int int2 = constableList0.indexOf((java.lang.Object) 2);
        java.util.ListIterator<java.lang.constant.Constable> constableItor3 = constableList0.listIterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object> objItor4 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object>((java.util.Iterator<java.lang.constant.Constable>) constableItor3);
        // The following exception was thrown during execution in test generation
        try {
            java.lang.Object obj5 = objItor4.next();
            org.junit.Assert.fail("Expected exception of type java.util.NoSuchElementException; message: null");
        } catch (java.util.NoSuchElementException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + int2 + "' != '" + (-1) + "'", int2 == (-1));
        org.junit.Assert.assertNotNull(constableItor3);
    }

    @Test
    public void test2() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperC3RandoopSeed0Regression0.test2");
        java.util.ArrayList<java.lang.constant.Constable> constableList0 = new java.util.ArrayList<java.lang.constant.Constable>();
        int int2 = constableList0.indexOf((java.lang.Object) 2);
        java.util.ListIterator<java.lang.constant.Constable> constableItor3 = constableList0.listIterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object> objItor4 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object>((java.util.Iterator<java.lang.constant.Constable>) constableItor3);
        // The following exception was thrown during execution in test generation
        try {
            objItor4.remove();
            org.junit.Assert.fail("Expected exception of type java.lang.IllegalStateException; message: null");
        } catch (java.lang.IllegalStateException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + int2 + "' != '" + (-1) + "'", int2 == (-1));
        org.junit.Assert.assertNotNull(constableItor3);
    }

    @Test
    public void test3() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperC3RandoopSeed0Regression0.test3");
        java.util.ArrayList<java.lang.constant.Constable> constableList0 = new java.util.ArrayList<java.lang.constant.Constable>();
        int int2 = constableList0.indexOf((java.lang.Object) 2);
        java.util.ListIterator<java.lang.constant.Constable> constableItor3 = constableList0.listIterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object> objItor4 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object>((java.util.Iterator<java.lang.constant.Constable>) constableItor3);
        java.lang.constant.Constable[] constableArray44 = new java.lang.constant.Constable[] { false, (-1.0d), '#', "hi!", (short) -1, 10.0f, 100, "hi!", 100L, (byte) 10, (byte) 1, 1L, (byte) 1, 100L, (byte) 100, 0.0d, '4', 0.0d, (byte) 10, (byte) 0, 100, (short) -1, 0.0f, 10.0f, (-1L), "", false, (byte) 1, false, 0, 10, (short) 10, ' ', (byte) 10, 10.0d, (byte) 1, '#', (-1), ' ' };
        java.util.ArrayList<java.lang.constant.Constable> constableList45 = new java.util.ArrayList<java.lang.constant.Constable>();
        boolean boolean46 = java.util.Collections.addAll((java.util.Collection<java.lang.constant.Constable>) constableList45, constableArray44);
        java.util.ArrayList<java.lang.constant.Constable> constableList47 = new java.util.ArrayList<java.lang.constant.Constable>();
        boolean boolean48 = constableList45.removeAll((java.util.Collection<java.lang.constant.Constable>) constableList47);
        int int49 = constableList45.size();
        constableList45.ensureCapacity((int) (short) 100);
        boolean boolean53 = constableList45.add((java.lang.constant.Constable) 0.0f);
        objItor4.add((java.lang.Object) constableList45);
        org.junit.Assert.assertTrue("'" + int2 + "' != '" + (-1) + "'", int2 == (-1));
        org.junit.Assert.assertNotNull(constableItor3);
        org.junit.Assert.assertNotNull(constableArray44);
        org.junit.Assert.assertTrue("'" + boolean46 + "' != '" + true + "'", boolean46 == true);
        org.junit.Assert.assertTrue("'" + boolean48 + "' != '" + false + "'", boolean48 == false);
        org.junit.Assert.assertTrue("'" + int49 + "' != '" + 39 + "'", int49 == 39);
        org.junit.Assert.assertTrue("'" + boolean53 + "' != '" + true + "'", boolean53 == true);
    }

    @Test
    public void test4() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperC3RandoopSeed0Regression0.test4");
        java.util.ArrayList<java.lang.constant.Constable> constableList0 = new java.util.ArrayList<java.lang.constant.Constable>();
        int int2 = constableList0.indexOf((java.lang.Object) 2);
        java.util.ListIterator<java.lang.constant.Constable> constableItor3 = constableList0.listIterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object> objItor4 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object>((java.util.Iterator<java.lang.constant.Constable>) constableItor3);
        boolean boolean5 = objItor4.hasPrevious();
        objItor4.reset();
        int int7 = objItor4.nextIndex();
        boolean boolean8 = objItor4.hasPrevious();
        org.junit.Assert.assertTrue("'" + int2 + "' != '" + (-1) + "'", int2 == (-1));
        org.junit.Assert.assertNotNull(constableItor3);
        org.junit.Assert.assertTrue("'" + boolean5 + "' != '" + false + "'", boolean5 == false);
        org.junit.Assert.assertTrue("'" + int7 + "' != '" + 0 + "'", int7 == 0);
        org.junit.Assert.assertTrue("'" + boolean8 + "' != '" + false + "'", boolean8 == false);
    }

    @Test
    public void test5() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperC3RandoopSeed0Regression0.test5");
        java.util.ArrayList<java.lang.constant.Constable> constableList0 = new java.util.ArrayList<java.lang.constant.Constable>();
        int int2 = constableList0.indexOf((java.lang.Object) 2);
        java.util.ListIterator<java.lang.constant.Constable> constableItor3 = constableList0.listIterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object> objItor4 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object>((java.util.Iterator<java.lang.constant.Constable>) constableItor3);
        boolean boolean5 = objItor4.hasPrevious();
        int int6 = objItor4.previousIndex();
        int int7 = objItor4.previousIndex();
        org.junit.Assert.assertTrue("'" + int2 + "' != '" + (-1) + "'", int2 == (-1));
        org.junit.Assert.assertNotNull(constableItor3);
        org.junit.Assert.assertTrue("'" + boolean5 + "' != '" + false + "'", boolean5 == false);
        org.junit.Assert.assertTrue("'" + int6 + "' != '" + (-1) + "'", int6 == (-1));
        org.junit.Assert.assertTrue("'" + int7 + "' != '" + (-1) + "'", int7 == (-1));
    }

    @Test
    public void test6() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperC3RandoopSeed0Regression0.test6");
        java.util.ArrayList<java.lang.constant.Constable> constableList0 = new java.util.ArrayList<java.lang.constant.Constable>();
        int int2 = constableList0.indexOf((java.lang.Object) 2);
        java.util.ListIterator<java.lang.constant.Constable> constableItor3 = constableList0.listIterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object> objItor4 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object>((java.util.Iterator<java.lang.constant.Constable>) constableItor3);
        boolean boolean5 = objItor4.hasPrevious();
        int int6 = objItor4.previousIndex();
        // The following exception was thrown during execution in test generation
        try {
            java.lang.Object obj7 = objItor4.previous();
            org.junit.Assert.fail("Expected exception of type java.util.NoSuchElementException; message: null");
        } catch (java.util.NoSuchElementException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + int2 + "' != '" + (-1) + "'", int2 == (-1));
        org.junit.Assert.assertNotNull(constableItor3);
        org.junit.Assert.assertTrue("'" + boolean5 + "' != '" + false + "'", boolean5 == false);
        org.junit.Assert.assertTrue("'" + int6 + "' != '" + (-1) + "'", int6 == (-1));
    }

    @Test
    public void test7() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperC3RandoopSeed0Regression0.test7");
        java.util.ArrayList<java.lang.constant.Constable> constableList0 = new java.util.ArrayList<java.lang.constant.Constable>();
        int int2 = constableList0.indexOf((java.lang.Object) 2);
        java.util.ListIterator<java.lang.constant.Constable> constableItor3 = constableList0.listIterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object> objItor4 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object>((java.util.Iterator<java.lang.constant.Constable>) constableItor3);
        // The following exception was thrown during execution in test generation
        try {
            java.lang.Object obj5 = objItor4.previous();
            org.junit.Assert.fail("Expected exception of type java.util.NoSuchElementException; message: null");
        } catch (java.util.NoSuchElementException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + int2 + "' != '" + (-1) + "'", int2 == (-1));
        org.junit.Assert.assertNotNull(constableItor3);
    }
}

