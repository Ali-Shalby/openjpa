package it.uniroma2.isw2.openjpa.testing.listiteratorwrapper.rnd.raw;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ListIteratorWrapperRandoopRegression0 {

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
            System.out.format("%n%s%n", "ListIteratorWrapperRandoopRegression0.test1");
        java.util.ArrayList<java.lang.Object> objList0 = new java.util.ArrayList<java.lang.Object>();
        boolean boolean2 = objList0.contains((java.lang.Object) (short) 10);
        java.util.ArrayList<java.lang.Object> objList4 = new java.util.ArrayList<java.lang.Object>(10);
        java.util.Iterator<java.lang.Object> objItor5 = objList4.iterator();
        java.util.ArrayList<java.lang.Object> objList6 = new java.util.ArrayList<java.lang.Object>((java.util.Collection<java.lang.Object>) objList4);
        boolean boolean7 = objList0.containsAll((java.util.Collection<java.lang.Object>) objList6);
        objList6.ensureCapacity(100);
        java.util.ArrayList<java.lang.Object> objList10 = new java.util.ArrayList<java.lang.Object>();
        java.util.ArrayList<java.lang.Object> objList12 = new java.util.ArrayList<java.lang.Object>();
        java.util.ArrayList<java.lang.Object> objList22 = new java.util.ArrayList<java.lang.Object>();
        java.util.ArrayList<java.lang.Object> objList31 = new java.util.ArrayList<java.lang.Object>();
        java.util.ArrayList<java.lang.Object> objList33 = new java.util.ArrayList<java.lang.Object>();
        boolean boolean35 = objList33.contains((java.lang.Object) (byte) 0);
        java.util.ArrayList<java.lang.Object> objList39 = new java.util.ArrayList<java.lang.Object>();
        boolean boolean41 = objList39.contains((java.lang.Object) (byte) 0);
        java.lang.Object[] objArray51 = new java.lang.Object[] { (-1), objList12, 10, 0L, 1L, (short) 100, 1L, 1.0f, 0L, (byte) 0, 100.0f, objList22, (short) 0, (short) 1, 0, (-1.0f), '#', 100.0d, 1.0f, false, objList31, 0.0d, boolean35, (byte) 100, 100, 10.0f, objList39, 10.0d, (byte) 0, 'a', 1.0d, (short) 1, (short) 0, 10.0d, 100L, ' ' };
        java.util.ArrayList<java.lang.Object> objList52 = new java.util.ArrayList<java.lang.Object>();
        boolean boolean53 = java.util.Collections.addAll((java.util.Collection<java.lang.Object>) objList52, objArray51);
        boolean boolean54 = objList10.addAll((java.util.Collection<java.lang.Object>) objList52);
        java.util.ArrayList<java.lang.Object> objList55 = new java.util.ArrayList<java.lang.Object>();
        objList55.addFirst((java.lang.Object) 10.0f);
        boolean boolean58 = objList52.contains((java.lang.Object) 10.0f);
        java.util.ListIterator<java.lang.Object> objItor60 = objList52.listIterator((int) (byte) 0);
        objList6.addLast((java.lang.Object) objItor60);
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object> objItor62 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object>((java.util.Iterator<java.lang.Object>) objItor60);
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
        org.junit.Assert.assertNotNull(objItor5);
        org.junit.Assert.assertTrue("'" + boolean7 + "' != '" + true + "'", boolean7 == true);
        org.junit.Assert.assertTrue("'" + boolean35 + "' != '" + false + "'", boolean35 == false);
        org.junit.Assert.assertTrue("'" + boolean41 + "' != '" + false + "'", boolean41 == false);
        org.junit.Assert.assertNotNull(objArray51);
        org.junit.Assert.assertEquals(java.util.Arrays.deepToString(objArray51), "[-1, [], 10, 0, 1, 100, 1, 1.0, 0, 0, 100.0, [], 0, 1, 0, -1.0, #, 100.0, 1.0, false, [], 0.0, false, 100, 100, 10.0, [], 10.0, 0, a, 1.0, 1, 0, 10.0, 100,  ]");
        org.junit.Assert.assertEquals(java.util.Arrays.toString(objArray51), "[-1, [], 10, 0, 1, 100, 1, 1.0, 0, 0, 100.0, [], 0, 1, 0, -1.0, #, 100.0, 1.0, false, [], 0.0, false, 100, 100, 10.0, [], 10.0, 0, a, 1.0, 1, 0, 10.0, 100,  ]");
        org.junit.Assert.assertTrue("'" + boolean53 + "' != '" + true + "'", boolean53 == true);
        org.junit.Assert.assertTrue("'" + boolean54 + "' != '" + true + "'", boolean54 == true);
        org.junit.Assert.assertTrue("'" + boolean58 + "' != '" + true + "'", boolean58 == true);
        org.junit.Assert.assertNotNull(objItor60);
    }

    @Test
    public void test2() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperRandoopRegression0.test2");
        java.util.ArrayList<java.lang.Object> objList0 = new java.util.ArrayList<java.lang.Object>();
        boolean boolean2 = objList0.contains((java.lang.Object) (short) 10);
        java.util.ArrayList<java.lang.Object> objList4 = new java.util.ArrayList<java.lang.Object>(10);
        java.util.Iterator<java.lang.Object> objItor5 = objList4.iterator();
        java.util.ArrayList<java.lang.Object> objList6 = new java.util.ArrayList<java.lang.Object>((java.util.Collection<java.lang.Object>) objList4);
        boolean boolean7 = objList0.containsAll((java.util.Collection<java.lang.Object>) objList6);
        boolean boolean8 = objList6.isEmpty();
        java.util.Iterator<java.lang.Object> objItor9 = objList6.iterator();
        java.util.Iterator<java.lang.Object> objItor10 = objList6.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object> objItor11 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object>(objItor10);
        objItor11.reset();
        java.util.ArrayList<java.lang.Object> objList13 = new java.util.ArrayList<java.lang.Object>();
        java.util.ArrayList<java.lang.Object> objList15 = new java.util.ArrayList<java.lang.Object>();
        java.util.ArrayList<java.lang.Object> objList25 = new java.util.ArrayList<java.lang.Object>();
        java.util.ArrayList<java.lang.Object> objList34 = new java.util.ArrayList<java.lang.Object>();
        java.util.ArrayList<java.lang.Object> objList36 = new java.util.ArrayList<java.lang.Object>();
        boolean boolean38 = objList36.contains((java.lang.Object) (byte) 0);
        java.util.ArrayList<java.lang.Object> objList42 = new java.util.ArrayList<java.lang.Object>();
        boolean boolean44 = objList42.contains((java.lang.Object) (byte) 0);
        java.lang.Object[] objArray54 = new java.lang.Object[] { (-1), objList15, 10, 0L, 1L, (short) 100, 1L, 1.0f, 0L, (byte) 0, 100.0f, objList25, (short) 0, (short) 1, 0, (-1.0f), '#', 100.0d, 1.0f, false, objList34, 0.0d, boolean38, (byte) 100, 100, 10.0f, objList42, 10.0d, (byte) 0, 'a', 1.0d, (short) 1, (short) 0, 10.0d, 100L, ' ' };
        java.util.ArrayList<java.lang.Object> objList55 = new java.util.ArrayList<java.lang.Object>();
        boolean boolean56 = java.util.Collections.addAll((java.util.Collection<java.lang.Object>) objList55, objArray54);
        boolean boolean57 = objList13.addAll((java.util.Collection<java.lang.Object>) objList55);
        boolean boolean59 = objList13.equals((java.lang.Object) "");
        java.util.Spliterator<java.lang.Object> objSpliterator60 = objList13.spliterator();
        java.util.stream.Stream<java.lang.Object> objStream61 = objList13.stream();
        // The following exception was thrown during execution in test generation
        try {
            objItor11.add((java.lang.Object) objStream61);
            org.junit.Assert.fail("Expected exception of type java.lang.UnsupportedOperationException; message: ListIteratorWrapper does not support optional operations of ListIterator.");
        } catch (java.lang.UnsupportedOperationException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
        org.junit.Assert.assertNotNull(objItor5);
        org.junit.Assert.assertTrue("'" + boolean7 + "' != '" + true + "'", boolean7 == true);
        org.junit.Assert.assertTrue("'" + boolean8 + "' != '" + true + "'", boolean8 == true);
        org.junit.Assert.assertNotNull(objItor9);
        org.junit.Assert.assertNotNull(objItor10);
        org.junit.Assert.assertTrue("'" + boolean38 + "' != '" + false + "'", boolean38 == false);
        org.junit.Assert.assertTrue("'" + boolean44 + "' != '" + false + "'", boolean44 == false);
        org.junit.Assert.assertNotNull(objArray54);
        org.junit.Assert.assertEquals(java.util.Arrays.deepToString(objArray54), "[-1, [], 10, 0, 1, 100, 1, 1.0, 0, 0, 100.0, [], 0, 1, 0, -1.0, #, 100.0, 1.0, false, [], 0.0, false, 100, 100, 10.0, [], 10.0, 0, a, 1.0, 1, 0, 10.0, 100,  ]");
        org.junit.Assert.assertEquals(java.util.Arrays.toString(objArray54), "[-1, [], 10, 0, 1, 100, 1, 1.0, 0, 0, 100.0, [], 0, 1, 0, -1.0, #, 100.0, 1.0, false, [], 0.0, false, 100, 100, 10.0, [], 10.0, 0, a, 1.0, 1, 0, 10.0, 100,  ]");
        org.junit.Assert.assertTrue("'" + boolean56 + "' != '" + true + "'", boolean56 == true);
        org.junit.Assert.assertTrue("'" + boolean57 + "' != '" + true + "'", boolean57 == true);
        org.junit.Assert.assertTrue("'" + boolean59 + "' != '" + false + "'", boolean59 == false);
        org.junit.Assert.assertNotNull(objSpliterator60);
        org.junit.Assert.assertNotNull(objStream61);
    }

    @Test
    public void test3() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperRandoopRegression0.test3");
        java.util.ArrayList<java.lang.Object> objList0 = new java.util.ArrayList<java.lang.Object>();
        boolean boolean2 = objList0.contains((java.lang.Object) (short) 10);
        java.util.ArrayList<java.lang.Object> objList4 = new java.util.ArrayList<java.lang.Object>(10);
        java.util.Iterator<java.lang.Object> objItor5 = objList4.iterator();
        java.util.ArrayList<java.lang.Object> objList6 = new java.util.ArrayList<java.lang.Object>((java.util.Collection<java.lang.Object>) objList4);
        boolean boolean7 = objList0.containsAll((java.util.Collection<java.lang.Object>) objList6);
        boolean boolean8 = objList6.isEmpty();
        java.util.Iterator<java.lang.Object> objItor9 = objList6.iterator();
        java.util.Iterator<java.lang.Object> objItor10 = objList6.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object> objItor11 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object>(objItor10);
        objItor11.reset();
        java.util.ArrayList<java.lang.Object> objList13 = new java.util.ArrayList<java.lang.Object>();
        java.util.ArrayList<java.lang.Object> objList15 = new java.util.ArrayList<java.lang.Object>();
        java.util.ArrayList<java.lang.Object> objList25 = new java.util.ArrayList<java.lang.Object>();
        java.util.ArrayList<java.lang.Object> objList34 = new java.util.ArrayList<java.lang.Object>();
        java.util.ArrayList<java.lang.Object> objList36 = new java.util.ArrayList<java.lang.Object>();
        boolean boolean38 = objList36.contains((java.lang.Object) (byte) 0);
        java.util.ArrayList<java.lang.Object> objList42 = new java.util.ArrayList<java.lang.Object>();
        boolean boolean44 = objList42.contains((java.lang.Object) (byte) 0);
        java.lang.Object[] objArray54 = new java.lang.Object[] { (-1), objList15, 10, 0L, 1L, (short) 100, 1L, 1.0f, 0L, (byte) 0, 100.0f, objList25, (short) 0, (short) 1, 0, (-1.0f), '#', 100.0d, 1.0f, false, objList34, 0.0d, boolean38, (byte) 100, 100, 10.0f, objList42, 10.0d, (byte) 0, 'a', 1.0d, (short) 1, (short) 0, 10.0d, 100L, ' ' };
        java.util.ArrayList<java.lang.Object> objList55 = new java.util.ArrayList<java.lang.Object>();
        boolean boolean56 = java.util.Collections.addAll((java.util.Collection<java.lang.Object>) objList55, objArray54);
        boolean boolean57 = objList13.addAll((java.util.Collection<java.lang.Object>) objList55);
        // The following exception was thrown during execution in test generation
        try {
            objItor11.set((java.lang.Object) objList55);
            org.junit.Assert.fail("Expected exception of type java.lang.UnsupportedOperationException; message: ListIteratorWrapper does not support optional operations of ListIterator.");
        } catch (java.lang.UnsupportedOperationException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
        org.junit.Assert.assertNotNull(objItor5);
        org.junit.Assert.assertTrue("'" + boolean7 + "' != '" + true + "'", boolean7 == true);
        org.junit.Assert.assertTrue("'" + boolean8 + "' != '" + true + "'", boolean8 == true);
        org.junit.Assert.assertNotNull(objItor9);
        org.junit.Assert.assertNotNull(objItor10);
        org.junit.Assert.assertTrue("'" + boolean38 + "' != '" + false + "'", boolean38 == false);
        org.junit.Assert.assertTrue("'" + boolean44 + "' != '" + false + "'", boolean44 == false);
        org.junit.Assert.assertNotNull(objArray54);
        org.junit.Assert.assertEquals(java.util.Arrays.deepToString(objArray54), "[-1, [], 10, 0, 1, 100, 1, 1.0, 0, 0, 100.0, [], 0, 1, 0, -1.0, #, 100.0, 1.0, false, [], 0.0, false, 100, 100, 10.0, [], 10.0, 0, a, 1.0, 1, 0, 10.0, 100,  ]");
        org.junit.Assert.assertEquals(java.util.Arrays.toString(objArray54), "[-1, [], 10, 0, 1, 100, 1, 1.0, 0, 0, 100.0, [], 0, 1, 0, -1.0, #, 100.0, 1.0, false, [], 0.0, false, 100, 100, 10.0, [], 10.0, 0, a, 1.0, 1, 0, 10.0, 100,  ]");
        org.junit.Assert.assertTrue("'" + boolean56 + "' != '" + true + "'", boolean56 == true);
        org.junit.Assert.assertTrue("'" + boolean57 + "' != '" + true + "'", boolean57 == true);
    }

    @Test
    public void test4() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperRandoopRegression0.test4");
        java.util.ArrayList<java.lang.Object> objList0 = new java.util.ArrayList<java.lang.Object>();
        boolean boolean2 = objList0.contains((java.lang.Object) (short) 10);
        java.util.ArrayList<java.lang.Object> objList4 = new java.util.ArrayList<java.lang.Object>(10);
        java.util.Iterator<java.lang.Object> objItor5 = objList4.iterator();
        java.util.ArrayList<java.lang.Object> objList6 = new java.util.ArrayList<java.lang.Object>((java.util.Collection<java.lang.Object>) objList4);
        boolean boolean7 = objList0.containsAll((java.util.Collection<java.lang.Object>) objList6);
        boolean boolean8 = objList6.isEmpty();
        java.util.Iterator<java.lang.Object> objItor9 = objList6.iterator();
        java.util.Iterator<java.lang.Object> objItor10 = objList6.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object> objItor11 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object>(objItor10);
        boolean boolean12 = objItor11.hasNext();
        // The following exception was thrown during execution in test generation
        try {
            java.lang.Object obj13 = objItor11.previous();
            org.junit.Assert.fail("Expected exception of type java.util.NoSuchElementException; message: null");
        } catch (java.util.NoSuchElementException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
        org.junit.Assert.assertNotNull(objItor5);
        org.junit.Assert.assertTrue("'" + boolean7 + "' != '" + true + "'", boolean7 == true);
        org.junit.Assert.assertTrue("'" + boolean8 + "' != '" + true + "'", boolean8 == true);
        org.junit.Assert.assertNotNull(objItor9);
        org.junit.Assert.assertNotNull(objItor10);
        org.junit.Assert.assertTrue("'" + boolean12 + "' != '" + false + "'", boolean12 == false);
    }

    @Test
    public void test5() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperRandoopRegression0.test5");
        java.util.ArrayList<java.lang.Object> objList0 = new java.util.ArrayList<java.lang.Object>();
        boolean boolean2 = objList0.contains((java.lang.Object) (short) 10);
        java.util.ArrayList<java.lang.Object> objList4 = new java.util.ArrayList<java.lang.Object>(10);
        java.util.Iterator<java.lang.Object> objItor5 = objList4.iterator();
        java.util.ArrayList<java.lang.Object> objList6 = new java.util.ArrayList<java.lang.Object>((java.util.Collection<java.lang.Object>) objList4);
        boolean boolean7 = objList0.containsAll((java.util.Collection<java.lang.Object>) objList6);
        boolean boolean8 = objList6.isEmpty();
        java.util.Iterator<java.lang.Object> objItor9 = objList6.iterator();
        java.util.Iterator<java.lang.Object> objItor10 = objList6.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object> objItor11 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object>(objItor10);
        boolean boolean12 = objItor11.hasNext();
        // The following exception was thrown during execution in test generation
        try {
            objItor11.remove();
            org.junit.Assert.fail("Expected exception of type java.lang.IllegalStateException; message: Cannot remove element at index -1.");
        } catch (java.lang.IllegalStateException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
        org.junit.Assert.assertNotNull(objItor5);
        org.junit.Assert.assertTrue("'" + boolean7 + "' != '" + true + "'", boolean7 == true);
        org.junit.Assert.assertTrue("'" + boolean8 + "' != '" + true + "'", boolean8 == true);
        org.junit.Assert.assertNotNull(objItor9);
        org.junit.Assert.assertNotNull(objItor10);
        org.junit.Assert.assertTrue("'" + boolean12 + "' != '" + false + "'", boolean12 == false);
    }

    @Test
    public void test6() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperRandoopRegression0.test6");
        java.util.ArrayList<java.lang.Object> objList0 = new java.util.ArrayList<java.lang.Object>();
        boolean boolean2 = objList0.contains((java.lang.Object) (short) 10);
        java.util.ArrayList<java.lang.Object> objList4 = new java.util.ArrayList<java.lang.Object>(10);
        java.util.Iterator<java.lang.Object> objItor5 = objList4.iterator();
        java.util.ArrayList<java.lang.Object> objList6 = new java.util.ArrayList<java.lang.Object>((java.util.Collection<java.lang.Object>) objList4);
        boolean boolean7 = objList0.containsAll((java.util.Collection<java.lang.Object>) objList6);
        boolean boolean8 = objList6.isEmpty();
        java.util.Iterator<java.lang.Object> objItor9 = objList6.iterator();
        java.util.Iterator<java.lang.Object> objItor10 = objList6.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object> objItor11 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object>(objItor10);
        // The following exception was thrown during execution in test generation
        try {
            objItor11.remove();
            org.junit.Assert.fail("Expected exception of type java.lang.IllegalStateException; message: Cannot remove element at index -1.");
        } catch (java.lang.IllegalStateException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
        org.junit.Assert.assertNotNull(objItor5);
        org.junit.Assert.assertTrue("'" + boolean7 + "' != '" + true + "'", boolean7 == true);
        org.junit.Assert.assertTrue("'" + boolean8 + "' != '" + true + "'", boolean8 == true);
        org.junit.Assert.assertNotNull(objItor9);
        org.junit.Assert.assertNotNull(objItor10);
    }

    @Test
    public void test7() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperRandoopRegression0.test7");
        java.util.ArrayList<java.lang.Object> objList0 = new java.util.ArrayList<java.lang.Object>();
        boolean boolean2 = objList0.contains((java.lang.Object) (short) 10);
        java.util.ArrayList<java.lang.Object> objList4 = new java.util.ArrayList<java.lang.Object>(10);
        java.util.Iterator<java.lang.Object> objItor5 = objList4.iterator();
        java.util.ArrayList<java.lang.Object> objList6 = new java.util.ArrayList<java.lang.Object>((java.util.Collection<java.lang.Object>) objList4);
        boolean boolean7 = objList0.containsAll((java.util.Collection<java.lang.Object>) objList6);
        boolean boolean8 = objList6.isEmpty();
        java.util.Iterator<java.lang.Object> objItor9 = objList6.iterator();
        java.util.Iterator<java.lang.Object> objItor10 = objList6.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object> objItor11 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object>(objItor10);
        objItor11.reset();
        // The following exception was thrown during execution in test generation
        try {
            java.lang.Object obj13 = objItor11.next();
            org.junit.Assert.fail("Expected exception of type java.util.NoSuchElementException; message: null");
        } catch (java.util.NoSuchElementException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
        org.junit.Assert.assertNotNull(objItor5);
        org.junit.Assert.assertTrue("'" + boolean7 + "' != '" + true + "'", boolean7 == true);
        org.junit.Assert.assertTrue("'" + boolean8 + "' != '" + true + "'", boolean8 == true);
        org.junit.Assert.assertNotNull(objItor9);
        org.junit.Assert.assertNotNull(objItor10);
    }

    @Test
    public void test8() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperRandoopRegression0.test8");
        java.util.ArrayList<java.lang.Object> objList0 = new java.util.ArrayList<java.lang.Object>();
        boolean boolean2 = objList0.contains((java.lang.Object) (short) 10);
        java.util.ArrayList<java.lang.Object> objList4 = new java.util.ArrayList<java.lang.Object>(10);
        java.util.Iterator<java.lang.Object> objItor5 = objList4.iterator();
        java.util.ArrayList<java.lang.Object> objList6 = new java.util.ArrayList<java.lang.Object>((java.util.Collection<java.lang.Object>) objList4);
        boolean boolean7 = objList0.containsAll((java.util.Collection<java.lang.Object>) objList6);
        boolean boolean8 = objList6.isEmpty();
        java.util.Iterator<java.lang.Object> objItor9 = objList6.iterator();
        java.util.Iterator<java.lang.Object> objItor10 = objList6.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object> objItor11 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object>(objItor10);
        // The following exception was thrown during execution in test generation
        try {
            java.lang.Object obj12 = objItor11.previous();
            org.junit.Assert.fail("Expected exception of type java.util.NoSuchElementException; message: null");
        } catch (java.util.NoSuchElementException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
        org.junit.Assert.assertNotNull(objItor5);
        org.junit.Assert.assertTrue("'" + boolean7 + "' != '" + true + "'", boolean7 == true);
        org.junit.Assert.assertTrue("'" + boolean8 + "' != '" + true + "'", boolean8 == true);
        org.junit.Assert.assertNotNull(objItor9);
        org.junit.Assert.assertNotNull(objItor10);
    }

    @Test
    public void test9() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperRandoopRegression0.test9");
        java.util.ArrayList<java.lang.Object> objList0 = new java.util.ArrayList<java.lang.Object>();
        boolean boolean2 = objList0.contains((java.lang.Object) (short) 10);
        java.util.ArrayList<java.lang.Object> objList4 = new java.util.ArrayList<java.lang.Object>(10);
        java.util.Iterator<java.lang.Object> objItor5 = objList4.iterator();
        java.util.ArrayList<java.lang.Object> objList6 = new java.util.ArrayList<java.lang.Object>((java.util.Collection<java.lang.Object>) objList4);
        boolean boolean7 = objList0.containsAll((java.util.Collection<java.lang.Object>) objList6);
        boolean boolean8 = objList6.isEmpty();
        java.util.Iterator<java.lang.Object> objItor9 = objList6.iterator();
        java.util.Iterator<java.lang.Object> objItor10 = objList6.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object> objItor11 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.lang.Object>(objItor10);
        int int12 = objItor11.previousIndex();
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
        org.junit.Assert.assertNotNull(objItor5);
        org.junit.Assert.assertTrue("'" + boolean7 + "' != '" + true + "'", boolean7 == true);
        org.junit.Assert.assertTrue("'" + boolean8 + "' != '" + true + "'", boolean8 == true);
        org.junit.Assert.assertNotNull(objItor9);
        org.junit.Assert.assertNotNull(objItor10);
        org.junit.Assert.assertTrue("'" + int12 + "' != '" + (-1) + "'", int12 == (-1));
    }
}

