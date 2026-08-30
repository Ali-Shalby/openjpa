import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ListIteratorWrapperC1RandoopRegression0 {

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
            System.out.format("%n%s%n", "ListIteratorWrapperC1RandoopRegression0.test1");
        java.util.ArrayList<java.io.Serializable> serializableList1 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        java.util.ArrayList<java.io.Serializable> serializableList7 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean9 = serializableList7.remove((java.lang.Object) (short) 10);
        java.io.Serializable[] serializableArray20 = new java.io.Serializable[] { (byte) -1, "", (short) 100, (byte) 1, serializableList7, (byte) 0, (-1.0f), 10, 0.0d, (byte) 0, 1.0f, '4', 100.0f, (byte) 100, 'a' };
        java.util.ArrayList<java.io.Serializable> serializableList21 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean22 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList21, serializableArray20);
        java.util.Spliterator<java.io.Serializable> serializableSpliterator23 = serializableList21.spliterator();
        java.util.ArrayList<java.io.Serializable> serializableList25 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        int int27 = serializableList25.lastIndexOf((java.lang.Object) 100.0d);
        boolean boolean28 = serializableList21.addAll((java.util.Collection<java.io.Serializable>) serializableList25);
        java.util.ArrayList<java.io.Serializable> serializableList31 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean33 = serializableList31.remove((java.lang.Object) (short) 10);
        java.util.ArrayList<java.io.Serializable> serializableList43 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        java.util.ArrayList<java.io.Serializable> serializableList53 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean55 = serializableList53.remove((java.lang.Object) (short) 10);
        java.io.Serializable[] serializableArray66 = new java.io.Serializable[] { (byte) -1, "", (short) 100, (byte) 1, serializableList53, (byte) 0, (-1.0f), 10, 0.0d, (byte) 0, 1.0f, '4', 100.0f, (byte) 100, 'a' };
        java.util.ArrayList<java.io.Serializable> serializableList67 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean68 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList67, serializableArray66);
        java.util.Spliterator<java.io.Serializable> serializableSpliterator69 = serializableList67.spliterator();
        java.io.Serializable[] serializableArray78 = new java.io.Serializable[] { (byte) 10, serializableList21, 1.0f, boolean33, (-1.0f), false, (byte) 10, 0.0d, '4', 0.0f, (byte) 10, true, serializableList43, (-1.0d), 1.0f, "hi!", 100, serializableList67, (short) 100, 0.0d, (byte) 0, 0, 100.0d, (byte) 10, true, (byte) 1 };
        java.util.ArrayList<java.io.Serializable> serializableList79 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean80 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList79, serializableArray78);
        java.util.ArrayList<java.io.Serializable> serializableList83 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean85 = serializableList83.remove((java.lang.Object) (short) 10);
        boolean boolean86 = serializableList79.addAll(10, (java.util.Collection<java.io.Serializable>) serializableList83);
        serializableList79.trimToSize();
        java.util.List<java.io.Serializable> serializableList90 = serializableList79.subList((int) (byte) 0, 1);
        java.util.stream.Stream<java.io.Serializable> serializableStream91 = serializableList79.parallelStream();
        java.util.Spliterator<java.io.Serializable> serializableSpliterator92 = serializableList79.spliterator();
        java.lang.Object[] objArray93 = serializableList79.toArray();
        java.util.Iterator<java.io.Serializable> serializableItor94 = serializableList79.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.io.Serializable> serializableItor95 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.io.Serializable>(serializableItor94);
        boolean boolean96 = serializableItor95.hasPrevious();
        // The following exception was thrown during execution in test generation
        try {
            java.io.Serializable serializable97 = serializableItor95.previous();
            org.junit.Assert.fail("Expected exception of type java.util.NoSuchElementException; message: null");
        } catch (java.util.NoSuchElementException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + false + "'", boolean9 == false);
        org.junit.Assert.assertNotNull(serializableArray20);
        org.junit.Assert.assertTrue("'" + boolean22 + "' != '" + true + "'", boolean22 == true);
        org.junit.Assert.assertNotNull(serializableSpliterator23);
        org.junit.Assert.assertTrue("'" + int27 + "' != '" + (-1) + "'", int27 == (-1));
        org.junit.Assert.assertTrue("'" + boolean28 + "' != '" + false + "'", boolean28 == false);
        org.junit.Assert.assertTrue("'" + boolean33 + "' != '" + false + "'", boolean33 == false);
        org.junit.Assert.assertTrue("'" + boolean55 + "' != '" + false + "'", boolean55 == false);
        org.junit.Assert.assertNotNull(serializableArray66);
        org.junit.Assert.assertTrue("'" + boolean68 + "' != '" + true + "'", boolean68 == true);
        org.junit.Assert.assertNotNull(serializableSpliterator69);
        org.junit.Assert.assertNotNull(serializableArray78);
        org.junit.Assert.assertTrue("'" + boolean80 + "' != '" + true + "'", boolean80 == true);
        org.junit.Assert.assertTrue("'" + boolean85 + "' != '" + false + "'", boolean85 == false);
        org.junit.Assert.assertTrue("'" + boolean86 + "' != '" + false + "'", boolean86 == false);
        org.junit.Assert.assertNotNull(serializableList90);
        org.junit.Assert.assertNotNull(serializableStream91);
        org.junit.Assert.assertNotNull(serializableSpliterator92);
        org.junit.Assert.assertNotNull(objArray93);
        org.junit.Assert.assertEquals(java.util.Arrays.deepToString(objArray93), "[10, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 1.0, false, -1.0, false, 10, 0.0, 4, 0.0, 10, true, [], -1.0, 1.0, hi!, 100, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 100, 0.0, 0, 0, 100.0, 10, true, 1]");
        org.junit.Assert.assertEquals(java.util.Arrays.toString(objArray93), "[10, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 1.0, false, -1.0, false, 10, 0.0, 4, 0.0, 10, true, [], -1.0, 1.0, hi!, 100, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 100, 0.0, 0, 0, 100.0, 10, true, 1]");
        org.junit.Assert.assertNotNull(serializableItor94);
        org.junit.Assert.assertTrue("'" + boolean96 + "' != '" + false + "'", boolean96 == false);
    }

    @Test
    public void test2() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperC1RandoopRegression0.test2");
        java.util.ArrayList<java.io.Serializable> serializableList1 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        java.util.ArrayList<java.io.Serializable> serializableList7 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean9 = serializableList7.remove((java.lang.Object) (short) 10);
        java.io.Serializable[] serializableArray20 = new java.io.Serializable[] { (byte) -1, "", (short) 100, (byte) 1, serializableList7, (byte) 0, (-1.0f), 10, 0.0d, (byte) 0, 1.0f, '4', 100.0f, (byte) 100, 'a' };
        java.util.ArrayList<java.io.Serializable> serializableList21 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean22 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList21, serializableArray20);
        java.util.Spliterator<java.io.Serializable> serializableSpliterator23 = serializableList21.spliterator();
        java.util.ArrayList<java.io.Serializable> serializableList25 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        int int27 = serializableList25.lastIndexOf((java.lang.Object) 100.0d);
        boolean boolean28 = serializableList21.addAll((java.util.Collection<java.io.Serializable>) serializableList25);
        java.util.ArrayList<java.io.Serializable> serializableList31 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean33 = serializableList31.remove((java.lang.Object) (short) 10);
        java.util.ArrayList<java.io.Serializable> serializableList43 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        java.util.ArrayList<java.io.Serializable> serializableList53 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean55 = serializableList53.remove((java.lang.Object) (short) 10);
        java.io.Serializable[] serializableArray66 = new java.io.Serializable[] { (byte) -1, "", (short) 100, (byte) 1, serializableList53, (byte) 0, (-1.0f), 10, 0.0d, (byte) 0, 1.0f, '4', 100.0f, (byte) 100, 'a' };
        java.util.ArrayList<java.io.Serializable> serializableList67 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean68 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList67, serializableArray66);
        java.util.Spliterator<java.io.Serializable> serializableSpliterator69 = serializableList67.spliterator();
        java.io.Serializable[] serializableArray78 = new java.io.Serializable[] { (byte) 10, serializableList21, 1.0f, boolean33, (-1.0f), false, (byte) 10, 0.0d, '4', 0.0f, (byte) 10, true, serializableList43, (-1.0d), 1.0f, "hi!", 100, serializableList67, (short) 100, 0.0d, (byte) 0, 0, 100.0d, (byte) 10, true, (byte) 1 };
        java.util.ArrayList<java.io.Serializable> serializableList79 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean80 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList79, serializableArray78);
        java.util.ArrayList<java.io.Serializable> serializableList83 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean85 = serializableList83.remove((java.lang.Object) (short) 10);
        boolean boolean86 = serializableList79.addAll(10, (java.util.Collection<java.io.Serializable>) serializableList83);
        serializableList79.trimToSize();
        java.util.List<java.io.Serializable> serializableList90 = serializableList79.subList((int) (byte) 0, 1);
        java.util.stream.Stream<java.io.Serializable> serializableStream91 = serializableList79.parallelStream();
        java.util.Spliterator<java.io.Serializable> serializableSpliterator92 = serializableList79.spliterator();
        java.lang.Object[] objArray93 = serializableList79.toArray();
        java.util.Iterator<java.io.Serializable> serializableItor94 = serializableList79.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.io.Serializable> serializableItor95 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.io.Serializable>(serializableItor94);
        boolean boolean96 = serializableItor95.hasPrevious();
        java.io.Serializable serializable97 = serializableItor95.next();
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + false + "'", boolean9 == false);
        org.junit.Assert.assertNotNull(serializableArray20);
        org.junit.Assert.assertTrue("'" + boolean22 + "' != '" + true + "'", boolean22 == true);
        org.junit.Assert.assertNotNull(serializableSpliterator23);
        org.junit.Assert.assertTrue("'" + int27 + "' != '" + (-1) + "'", int27 == (-1));
        org.junit.Assert.assertTrue("'" + boolean28 + "' != '" + false + "'", boolean28 == false);
        org.junit.Assert.assertTrue("'" + boolean33 + "' != '" + false + "'", boolean33 == false);
        org.junit.Assert.assertTrue("'" + boolean55 + "' != '" + false + "'", boolean55 == false);
        org.junit.Assert.assertNotNull(serializableArray66);
        org.junit.Assert.assertTrue("'" + boolean68 + "' != '" + true + "'", boolean68 == true);
        org.junit.Assert.assertNotNull(serializableSpliterator69);
        org.junit.Assert.assertNotNull(serializableArray78);
        org.junit.Assert.assertTrue("'" + boolean80 + "' != '" + true + "'", boolean80 == true);
        org.junit.Assert.assertTrue("'" + boolean85 + "' != '" + false + "'", boolean85 == false);
        org.junit.Assert.assertTrue("'" + boolean86 + "' != '" + false + "'", boolean86 == false);
        org.junit.Assert.assertNotNull(serializableList90);
        org.junit.Assert.assertNotNull(serializableStream91);
        org.junit.Assert.assertNotNull(serializableSpliterator92);
        org.junit.Assert.assertNotNull(objArray93);
        org.junit.Assert.assertEquals(java.util.Arrays.deepToString(objArray93), "[10, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 1.0, false, -1.0, false, 10, 0.0, 4, 0.0, 10, true, [], -1.0, 1.0, hi!, 100, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 100, 0.0, 0, 0, 100.0, 10, true, 1]");
        org.junit.Assert.assertEquals(java.util.Arrays.toString(objArray93), "[10, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 1.0, false, -1.0, false, 10, 0.0, 4, 0.0, 10, true, [], -1.0, 1.0, hi!, 100, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 100, 0.0, 0, 0, 100.0, 10, true, 1]");
        org.junit.Assert.assertNotNull(serializableItor94);
        org.junit.Assert.assertTrue("'" + boolean96 + "' != '" + false + "'", boolean96 == false);
        org.junit.Assert.assertEquals("'" + serializable97 + "' != '" + (byte) 10 + "'", serializable97, (byte) 10);
    }

    @Test
    public void test3() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperC1RandoopRegression0.test3");
        java.util.ArrayList<java.io.Serializable> serializableList1 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        java.util.ArrayList<java.io.Serializable> serializableList7 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean9 = serializableList7.remove((java.lang.Object) (short) 10);
        java.io.Serializable[] serializableArray20 = new java.io.Serializable[] { (byte) -1, "", (short) 100, (byte) 1, serializableList7, (byte) 0, (-1.0f), 10, 0.0d, (byte) 0, 1.0f, '4', 100.0f, (byte) 100, 'a' };
        java.util.ArrayList<java.io.Serializable> serializableList21 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean22 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList21, serializableArray20);
        java.util.Spliterator<java.io.Serializable> serializableSpliterator23 = serializableList21.spliterator();
        java.util.ArrayList<java.io.Serializable> serializableList25 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        int int27 = serializableList25.lastIndexOf((java.lang.Object) 100.0d);
        boolean boolean28 = serializableList21.addAll((java.util.Collection<java.io.Serializable>) serializableList25);
        java.util.ArrayList<java.io.Serializable> serializableList31 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean33 = serializableList31.remove((java.lang.Object) (short) 10);
        java.util.ArrayList<java.io.Serializable> serializableList43 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        java.util.ArrayList<java.io.Serializable> serializableList53 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean55 = serializableList53.remove((java.lang.Object) (short) 10);
        java.io.Serializable[] serializableArray66 = new java.io.Serializable[] { (byte) -1, "", (short) 100, (byte) 1, serializableList53, (byte) 0, (-1.0f), 10, 0.0d, (byte) 0, 1.0f, '4', 100.0f, (byte) 100, 'a' };
        java.util.ArrayList<java.io.Serializable> serializableList67 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean68 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList67, serializableArray66);
        java.util.Spliterator<java.io.Serializable> serializableSpliterator69 = serializableList67.spliterator();
        java.io.Serializable[] serializableArray78 = new java.io.Serializable[] { (byte) 10, serializableList21, 1.0f, boolean33, (-1.0f), false, (byte) 10, 0.0d, '4', 0.0f, (byte) 10, true, serializableList43, (-1.0d), 1.0f, "hi!", 100, serializableList67, (short) 100, 0.0d, (byte) 0, 0, 100.0d, (byte) 10, true, (byte) 1 };
        java.util.ArrayList<java.io.Serializable> serializableList79 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean80 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList79, serializableArray78);
        java.util.ArrayList<java.io.Serializable> serializableList83 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean85 = serializableList83.remove((java.lang.Object) (short) 10);
        boolean boolean86 = serializableList79.addAll(10, (java.util.Collection<java.io.Serializable>) serializableList83);
        serializableList79.trimToSize();
        java.util.List<java.io.Serializable> serializableList90 = serializableList79.subList((int) (byte) 0, 1);
        java.util.stream.Stream<java.io.Serializable> serializableStream91 = serializableList79.parallelStream();
        java.util.Spliterator<java.io.Serializable> serializableSpliterator92 = serializableList79.spliterator();
        java.lang.Object[] objArray93 = serializableList79.toArray();
        java.util.Iterator<java.io.Serializable> serializableItor94 = serializableList79.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.io.Serializable> serializableItor95 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.io.Serializable>(serializableItor94);
        boolean boolean96 = serializableItor95.hasPrevious();
        // The following exception was thrown during execution in test generation
        try {
            serializableItor95.add((java.io.Serializable) 'a');
            org.junit.Assert.fail("Expected exception of type java.lang.UnsupportedOperationException; message: ListIteratorWrapper does not support optional operations of ListIterator.");
        } catch (java.lang.UnsupportedOperationException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + false + "'", boolean9 == false);
        org.junit.Assert.assertNotNull(serializableArray20);
        org.junit.Assert.assertTrue("'" + boolean22 + "' != '" + true + "'", boolean22 == true);
        org.junit.Assert.assertNotNull(serializableSpliterator23);
        org.junit.Assert.assertTrue("'" + int27 + "' != '" + (-1) + "'", int27 == (-1));
        org.junit.Assert.assertTrue("'" + boolean28 + "' != '" + false + "'", boolean28 == false);
        org.junit.Assert.assertTrue("'" + boolean33 + "' != '" + false + "'", boolean33 == false);
        org.junit.Assert.assertTrue("'" + boolean55 + "' != '" + false + "'", boolean55 == false);
        org.junit.Assert.assertNotNull(serializableArray66);
        org.junit.Assert.assertTrue("'" + boolean68 + "' != '" + true + "'", boolean68 == true);
        org.junit.Assert.assertNotNull(serializableSpliterator69);
        org.junit.Assert.assertNotNull(serializableArray78);
        org.junit.Assert.assertTrue("'" + boolean80 + "' != '" + true + "'", boolean80 == true);
        org.junit.Assert.assertTrue("'" + boolean85 + "' != '" + false + "'", boolean85 == false);
        org.junit.Assert.assertTrue("'" + boolean86 + "' != '" + false + "'", boolean86 == false);
        org.junit.Assert.assertNotNull(serializableList90);
        org.junit.Assert.assertNotNull(serializableStream91);
        org.junit.Assert.assertNotNull(serializableSpliterator92);
        org.junit.Assert.assertNotNull(objArray93);
        org.junit.Assert.assertEquals(java.util.Arrays.deepToString(objArray93), "[10, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 1.0, false, -1.0, false, 10, 0.0, 4, 0.0, 10, true, [], -1.0, 1.0, hi!, 100, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 100, 0.0, 0, 0, 100.0, 10, true, 1]");
        org.junit.Assert.assertEquals(java.util.Arrays.toString(objArray93), "[10, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 1.0, false, -1.0, false, 10, 0.0, 4, 0.0, 10, true, [], -1.0, 1.0, hi!, 100, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 100, 0.0, 0, 0, 100.0, 10, true, 1]");
        org.junit.Assert.assertNotNull(serializableItor94);
        org.junit.Assert.assertTrue("'" + boolean96 + "' != '" + false + "'", boolean96 == false);
    }

    @Test
    public void test4() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperC1RandoopRegression0.test4");
        java.util.ArrayList<java.io.Serializable> serializableList1 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        java.util.ArrayList<java.io.Serializable> serializableList7 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean9 = serializableList7.remove((java.lang.Object) (short) 10);
        java.io.Serializable[] serializableArray20 = new java.io.Serializable[] { (byte) -1, "", (short) 100, (byte) 1, serializableList7, (byte) 0, (-1.0f), 10, 0.0d, (byte) 0, 1.0f, '4', 100.0f, (byte) 100, 'a' };
        java.util.ArrayList<java.io.Serializable> serializableList21 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean22 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList21, serializableArray20);
        java.util.Spliterator<java.io.Serializable> serializableSpliterator23 = serializableList21.spliterator();
        java.util.ArrayList<java.io.Serializable> serializableList25 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        int int27 = serializableList25.lastIndexOf((java.lang.Object) 100.0d);
        boolean boolean28 = serializableList21.addAll((java.util.Collection<java.io.Serializable>) serializableList25);
        java.util.ArrayList<java.io.Serializable> serializableList31 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean33 = serializableList31.remove((java.lang.Object) (short) 10);
        java.util.ArrayList<java.io.Serializable> serializableList43 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        java.util.ArrayList<java.io.Serializable> serializableList53 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean55 = serializableList53.remove((java.lang.Object) (short) 10);
        java.io.Serializable[] serializableArray66 = new java.io.Serializable[] { (byte) -1, "", (short) 100, (byte) 1, serializableList53, (byte) 0, (-1.0f), 10, 0.0d, (byte) 0, 1.0f, '4', 100.0f, (byte) 100, 'a' };
        java.util.ArrayList<java.io.Serializable> serializableList67 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean68 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList67, serializableArray66);
        java.util.Spliterator<java.io.Serializable> serializableSpliterator69 = serializableList67.spliterator();
        java.io.Serializable[] serializableArray78 = new java.io.Serializable[] { (byte) 10, serializableList21, 1.0f, boolean33, (-1.0f), false, (byte) 10, 0.0d, '4', 0.0f, (byte) 10, true, serializableList43, (-1.0d), 1.0f, "hi!", 100, serializableList67, (short) 100, 0.0d, (byte) 0, 0, 100.0d, (byte) 10, true, (byte) 1 };
        java.util.ArrayList<java.io.Serializable> serializableList79 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean80 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList79, serializableArray78);
        java.util.ArrayList<java.io.Serializable> serializableList83 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean85 = serializableList83.remove((java.lang.Object) (short) 10);
        boolean boolean86 = serializableList79.addAll(10, (java.util.Collection<java.io.Serializable>) serializableList83);
        serializableList79.trimToSize();
        java.util.List<java.io.Serializable> serializableList90 = serializableList79.subList((int) (byte) 0, 1);
        java.util.stream.Stream<java.io.Serializable> serializableStream91 = serializableList79.parallelStream();
        java.util.Spliterator<java.io.Serializable> serializableSpliterator92 = serializableList79.spliterator();
        java.lang.Object[] objArray93 = serializableList79.toArray();
        java.util.Iterator<java.io.Serializable> serializableItor94 = serializableList79.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.io.Serializable> serializableItor95 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.io.Serializable>(serializableItor94);
        serializableItor95.reset();
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + false + "'", boolean9 == false);
        org.junit.Assert.assertNotNull(serializableArray20);
        org.junit.Assert.assertTrue("'" + boolean22 + "' != '" + true + "'", boolean22 == true);
        org.junit.Assert.assertNotNull(serializableSpliterator23);
        org.junit.Assert.assertTrue("'" + int27 + "' != '" + (-1) + "'", int27 == (-1));
        org.junit.Assert.assertTrue("'" + boolean28 + "' != '" + false + "'", boolean28 == false);
        org.junit.Assert.assertTrue("'" + boolean33 + "' != '" + false + "'", boolean33 == false);
        org.junit.Assert.assertTrue("'" + boolean55 + "' != '" + false + "'", boolean55 == false);
        org.junit.Assert.assertNotNull(serializableArray66);
        org.junit.Assert.assertTrue("'" + boolean68 + "' != '" + true + "'", boolean68 == true);
        org.junit.Assert.assertNotNull(serializableSpliterator69);
        org.junit.Assert.assertNotNull(serializableArray78);
        org.junit.Assert.assertTrue("'" + boolean80 + "' != '" + true + "'", boolean80 == true);
        org.junit.Assert.assertTrue("'" + boolean85 + "' != '" + false + "'", boolean85 == false);
        org.junit.Assert.assertTrue("'" + boolean86 + "' != '" + false + "'", boolean86 == false);
        org.junit.Assert.assertNotNull(serializableList90);
        org.junit.Assert.assertNotNull(serializableStream91);
        org.junit.Assert.assertNotNull(serializableSpliterator92);
        org.junit.Assert.assertNotNull(objArray93);
        org.junit.Assert.assertEquals(java.util.Arrays.deepToString(objArray93), "[10, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 1.0, false, -1.0, false, 10, 0.0, 4, 0.0, 10, true, [], -1.0, 1.0, hi!, 100, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 100, 0.0, 0, 0, 100.0, 10, true, 1]");
        org.junit.Assert.assertEquals(java.util.Arrays.toString(objArray93), "[10, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 1.0, false, -1.0, false, 10, 0.0, 4, 0.0, 10, true, [], -1.0, 1.0, hi!, 100, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 100, 0.0, 0, 0, 100.0, 10, true, 1]");
        org.junit.Assert.assertNotNull(serializableItor94);
    }

    @Test
    public void test5() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperC1RandoopRegression0.test5");
        java.util.ArrayList<java.io.Serializable> serializableList1 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        java.util.ArrayList<java.io.Serializable> serializableList7 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean9 = serializableList7.remove((java.lang.Object) (short) 10);
        java.io.Serializable[] serializableArray20 = new java.io.Serializable[] { (byte) -1, "", (short) 100, (byte) 1, serializableList7, (byte) 0, (-1.0f), 10, 0.0d, (byte) 0, 1.0f, '4', 100.0f, (byte) 100, 'a' };
        java.util.ArrayList<java.io.Serializable> serializableList21 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean22 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList21, serializableArray20);
        java.util.Spliterator<java.io.Serializable> serializableSpliterator23 = serializableList21.spliterator();
        java.util.ArrayList<java.io.Serializable> serializableList25 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        int int27 = serializableList25.lastIndexOf((java.lang.Object) 100.0d);
        boolean boolean28 = serializableList21.addAll((java.util.Collection<java.io.Serializable>) serializableList25);
        java.util.ArrayList<java.io.Serializable> serializableList31 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean33 = serializableList31.remove((java.lang.Object) (short) 10);
        java.util.ArrayList<java.io.Serializable> serializableList43 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        java.util.ArrayList<java.io.Serializable> serializableList53 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean55 = serializableList53.remove((java.lang.Object) (short) 10);
        java.io.Serializable[] serializableArray66 = new java.io.Serializable[] { (byte) -1, "", (short) 100, (byte) 1, serializableList53, (byte) 0, (-1.0f), 10, 0.0d, (byte) 0, 1.0f, '4', 100.0f, (byte) 100, 'a' };
        java.util.ArrayList<java.io.Serializable> serializableList67 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean68 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList67, serializableArray66);
        java.util.Spliterator<java.io.Serializable> serializableSpliterator69 = serializableList67.spliterator();
        java.io.Serializable[] serializableArray78 = new java.io.Serializable[] { (byte) 10, serializableList21, 1.0f, boolean33, (-1.0f), false, (byte) 10, 0.0d, '4', 0.0f, (byte) 10, true, serializableList43, (-1.0d), 1.0f, "hi!", 100, serializableList67, (short) 100, 0.0d, (byte) 0, 0, 100.0d, (byte) 10, true, (byte) 1 };
        java.util.ArrayList<java.io.Serializable> serializableList79 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean80 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList79, serializableArray78);
        java.util.ArrayList<java.io.Serializable> serializableList83 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean85 = serializableList83.remove((java.lang.Object) (short) 10);
        boolean boolean86 = serializableList79.addAll(10, (java.util.Collection<java.io.Serializable>) serializableList83);
        serializableList79.trimToSize();
        java.util.List<java.io.Serializable> serializableList90 = serializableList79.subList((int) (byte) 0, 1);
        java.util.stream.Stream<java.io.Serializable> serializableStream91 = serializableList79.parallelStream();
        java.util.Spliterator<java.io.Serializable> serializableSpliterator92 = serializableList79.spliterator();
        java.lang.Object[] objArray93 = serializableList79.toArray();
        java.util.Iterator<java.io.Serializable> serializableItor94 = serializableList79.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.io.Serializable> serializableItor95 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.io.Serializable>(serializableItor94);
        boolean boolean96 = serializableItor95.hasPrevious();
        int int97 = serializableItor95.nextIndex();
        // The following exception was thrown during execution in test generation
        try {
            java.io.Serializable serializable98 = serializableItor95.previous();
            org.junit.Assert.fail("Expected exception of type java.util.NoSuchElementException; message: null");
        } catch (java.util.NoSuchElementException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + false + "'", boolean9 == false);
        org.junit.Assert.assertNotNull(serializableArray20);
        org.junit.Assert.assertTrue("'" + boolean22 + "' != '" + true + "'", boolean22 == true);
        org.junit.Assert.assertNotNull(serializableSpliterator23);
        org.junit.Assert.assertTrue("'" + int27 + "' != '" + (-1) + "'", int27 == (-1));
        org.junit.Assert.assertTrue("'" + boolean28 + "' != '" + false + "'", boolean28 == false);
        org.junit.Assert.assertTrue("'" + boolean33 + "' != '" + false + "'", boolean33 == false);
        org.junit.Assert.assertTrue("'" + boolean55 + "' != '" + false + "'", boolean55 == false);
        org.junit.Assert.assertNotNull(serializableArray66);
        org.junit.Assert.assertTrue("'" + boolean68 + "' != '" + true + "'", boolean68 == true);
        org.junit.Assert.assertNotNull(serializableSpliterator69);
        org.junit.Assert.assertNotNull(serializableArray78);
        org.junit.Assert.assertTrue("'" + boolean80 + "' != '" + true + "'", boolean80 == true);
        org.junit.Assert.assertTrue("'" + boolean85 + "' != '" + false + "'", boolean85 == false);
        org.junit.Assert.assertTrue("'" + boolean86 + "' != '" + false + "'", boolean86 == false);
        org.junit.Assert.assertNotNull(serializableList90);
        org.junit.Assert.assertNotNull(serializableStream91);
        org.junit.Assert.assertNotNull(serializableSpliterator92);
        org.junit.Assert.assertNotNull(objArray93);
        org.junit.Assert.assertEquals(java.util.Arrays.deepToString(objArray93), "[10, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 1.0, false, -1.0, false, 10, 0.0, 4, 0.0, 10, true, [], -1.0, 1.0, hi!, 100, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 100, 0.0, 0, 0, 100.0, 10, true, 1]");
        org.junit.Assert.assertEquals(java.util.Arrays.toString(objArray93), "[10, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 1.0, false, -1.0, false, 10, 0.0, 4, 0.0, 10, true, [], -1.0, 1.0, hi!, 100, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 100, 0.0, 0, 0, 100.0, 10, true, 1]");
        org.junit.Assert.assertNotNull(serializableItor94);
        org.junit.Assert.assertTrue("'" + boolean96 + "' != '" + false + "'", boolean96 == false);
        org.junit.Assert.assertTrue("'" + int97 + "' != '" + 0 + "'", int97 == 0);
    }

    @Test
    public void test6() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperC1RandoopRegression0.test6");
        java.util.ArrayList<java.io.Serializable> serializableList1 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        java.util.ArrayList<java.io.Serializable> serializableList7 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean9 = serializableList7.remove((java.lang.Object) (short) 10);
        java.io.Serializable[] serializableArray20 = new java.io.Serializable[] { (byte) -1, "", (short) 100, (byte) 1, serializableList7, (byte) 0, (-1.0f), 10, 0.0d, (byte) 0, 1.0f, '4', 100.0f, (byte) 100, 'a' };
        java.util.ArrayList<java.io.Serializable> serializableList21 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean22 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList21, serializableArray20);
        java.util.Spliterator<java.io.Serializable> serializableSpliterator23 = serializableList21.spliterator();
        java.util.ArrayList<java.io.Serializable> serializableList25 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        int int27 = serializableList25.lastIndexOf((java.lang.Object) 100.0d);
        boolean boolean28 = serializableList21.addAll((java.util.Collection<java.io.Serializable>) serializableList25);
        java.util.ArrayList<java.io.Serializable> serializableList31 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean33 = serializableList31.remove((java.lang.Object) (short) 10);
        java.util.ArrayList<java.io.Serializable> serializableList43 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        java.util.ArrayList<java.io.Serializable> serializableList53 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean55 = serializableList53.remove((java.lang.Object) (short) 10);
        java.io.Serializable[] serializableArray66 = new java.io.Serializable[] { (byte) -1, "", (short) 100, (byte) 1, serializableList53, (byte) 0, (-1.0f), 10, 0.0d, (byte) 0, 1.0f, '4', 100.0f, (byte) 100, 'a' };
        java.util.ArrayList<java.io.Serializable> serializableList67 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean68 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList67, serializableArray66);
        java.util.Spliterator<java.io.Serializable> serializableSpliterator69 = serializableList67.spliterator();
        java.io.Serializable[] serializableArray78 = new java.io.Serializable[] { (byte) 10, serializableList21, 1.0f, boolean33, (-1.0f), false, (byte) 10, 0.0d, '4', 0.0f, (byte) 10, true, serializableList43, (-1.0d), 1.0f, "hi!", 100, serializableList67, (short) 100, 0.0d, (byte) 0, 0, 100.0d, (byte) 10, true, (byte) 1 };
        java.util.ArrayList<java.io.Serializable> serializableList79 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean80 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList79, serializableArray78);
        java.util.ArrayList<java.io.Serializable> serializableList83 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean85 = serializableList83.remove((java.lang.Object) (short) 10);
        boolean boolean86 = serializableList79.addAll(10, (java.util.Collection<java.io.Serializable>) serializableList83);
        serializableList79.trimToSize();
        java.util.List<java.io.Serializable> serializableList90 = serializableList79.subList((int) (byte) 0, 1);
        java.util.stream.Stream<java.io.Serializable> serializableStream91 = serializableList79.parallelStream();
        java.util.Spliterator<java.io.Serializable> serializableSpliterator92 = serializableList79.spliterator();
        java.lang.Object[] objArray93 = serializableList79.toArray();
        java.util.Iterator<java.io.Serializable> serializableItor94 = serializableList79.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.io.Serializable> serializableItor95 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.io.Serializable>(serializableItor94);
        boolean boolean96 = serializableItor95.hasNext();
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + false + "'", boolean9 == false);
        org.junit.Assert.assertNotNull(serializableArray20);
        org.junit.Assert.assertTrue("'" + boolean22 + "' != '" + true + "'", boolean22 == true);
        org.junit.Assert.assertNotNull(serializableSpliterator23);
        org.junit.Assert.assertTrue("'" + int27 + "' != '" + (-1) + "'", int27 == (-1));
        org.junit.Assert.assertTrue("'" + boolean28 + "' != '" + false + "'", boolean28 == false);
        org.junit.Assert.assertTrue("'" + boolean33 + "' != '" + false + "'", boolean33 == false);
        org.junit.Assert.assertTrue("'" + boolean55 + "' != '" + false + "'", boolean55 == false);
        org.junit.Assert.assertNotNull(serializableArray66);
        org.junit.Assert.assertTrue("'" + boolean68 + "' != '" + true + "'", boolean68 == true);
        org.junit.Assert.assertNotNull(serializableSpliterator69);
        org.junit.Assert.assertNotNull(serializableArray78);
        org.junit.Assert.assertTrue("'" + boolean80 + "' != '" + true + "'", boolean80 == true);
        org.junit.Assert.assertTrue("'" + boolean85 + "' != '" + false + "'", boolean85 == false);
        org.junit.Assert.assertTrue("'" + boolean86 + "' != '" + false + "'", boolean86 == false);
        org.junit.Assert.assertNotNull(serializableList90);
        org.junit.Assert.assertNotNull(serializableStream91);
        org.junit.Assert.assertNotNull(serializableSpliterator92);
        org.junit.Assert.assertNotNull(objArray93);
        org.junit.Assert.assertEquals(java.util.Arrays.deepToString(objArray93), "[10, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 1.0, false, -1.0, false, 10, 0.0, 4, 0.0, 10, true, [], -1.0, 1.0, hi!, 100, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 100, 0.0, 0, 0, 100.0, 10, true, 1]");
        org.junit.Assert.assertEquals(java.util.Arrays.toString(objArray93), "[10, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 1.0, false, -1.0, false, 10, 0.0, 4, 0.0, 10, true, [], -1.0, 1.0, hi!, 100, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 100, 0.0, 0, 0, 100.0, 10, true, 1]");
        org.junit.Assert.assertNotNull(serializableItor94);
        org.junit.Assert.assertTrue("'" + boolean96 + "' != '" + true + "'", boolean96 == true);
    }

    @Test
    public void test7() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperC1RandoopRegression0.test7");
        java.util.ArrayList<java.io.Serializable> serializableList1 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        java.util.ArrayList<java.io.Serializable> serializableList7 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean9 = serializableList7.remove((java.lang.Object) (short) 10);
        java.io.Serializable[] serializableArray20 = new java.io.Serializable[] { (byte) -1, "", (short) 100, (byte) 1, serializableList7, (byte) 0, (-1.0f), 10, 0.0d, (byte) 0, 1.0f, '4', 100.0f, (byte) 100, 'a' };
        java.util.ArrayList<java.io.Serializable> serializableList21 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean22 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList21, serializableArray20);
        java.util.Spliterator<java.io.Serializable> serializableSpliterator23 = serializableList21.spliterator();
        java.util.ArrayList<java.io.Serializable> serializableList25 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        int int27 = serializableList25.lastIndexOf((java.lang.Object) 100.0d);
        boolean boolean28 = serializableList21.addAll((java.util.Collection<java.io.Serializable>) serializableList25);
        java.util.ArrayList<java.io.Serializable> serializableList31 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean33 = serializableList31.remove((java.lang.Object) (short) 10);
        java.util.ArrayList<java.io.Serializable> serializableList43 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        java.util.ArrayList<java.io.Serializable> serializableList53 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean55 = serializableList53.remove((java.lang.Object) (short) 10);
        java.io.Serializable[] serializableArray66 = new java.io.Serializable[] { (byte) -1, "", (short) 100, (byte) 1, serializableList53, (byte) 0, (-1.0f), 10, 0.0d, (byte) 0, 1.0f, '4', 100.0f, (byte) 100, 'a' };
        java.util.ArrayList<java.io.Serializable> serializableList67 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean68 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList67, serializableArray66);
        java.util.Spliterator<java.io.Serializable> serializableSpliterator69 = serializableList67.spliterator();
        java.io.Serializable[] serializableArray78 = new java.io.Serializable[] { (byte) 10, serializableList21, 1.0f, boolean33, (-1.0f), false, (byte) 10, 0.0d, '4', 0.0f, (byte) 10, true, serializableList43, (-1.0d), 1.0f, "hi!", 100, serializableList67, (short) 100, 0.0d, (byte) 0, 0, 100.0d, (byte) 10, true, (byte) 1 };
        java.util.ArrayList<java.io.Serializable> serializableList79 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean80 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList79, serializableArray78);
        java.util.ArrayList<java.io.Serializable> serializableList83 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean85 = serializableList83.remove((java.lang.Object) (short) 10);
        boolean boolean86 = serializableList79.addAll(10, (java.util.Collection<java.io.Serializable>) serializableList83);
        serializableList79.trimToSize();
        java.util.List<java.io.Serializable> serializableList90 = serializableList79.subList((int) (byte) 0, 1);
        java.util.stream.Stream<java.io.Serializable> serializableStream91 = serializableList79.parallelStream();
        java.util.Spliterator<java.io.Serializable> serializableSpliterator92 = serializableList79.spliterator();
        java.lang.Object[] objArray93 = serializableList79.toArray();
        java.util.Iterator<java.io.Serializable> serializableItor94 = serializableList79.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.io.Serializable> serializableItor95 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.io.Serializable>(serializableItor94);
        int int96 = serializableItor95.previousIndex();
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + false + "'", boolean9 == false);
        org.junit.Assert.assertNotNull(serializableArray20);
        org.junit.Assert.assertTrue("'" + boolean22 + "' != '" + true + "'", boolean22 == true);
        org.junit.Assert.assertNotNull(serializableSpliterator23);
        org.junit.Assert.assertTrue("'" + int27 + "' != '" + (-1) + "'", int27 == (-1));
        org.junit.Assert.assertTrue("'" + boolean28 + "' != '" + false + "'", boolean28 == false);
        org.junit.Assert.assertTrue("'" + boolean33 + "' != '" + false + "'", boolean33 == false);
        org.junit.Assert.assertTrue("'" + boolean55 + "' != '" + false + "'", boolean55 == false);
        org.junit.Assert.assertNotNull(serializableArray66);
        org.junit.Assert.assertTrue("'" + boolean68 + "' != '" + true + "'", boolean68 == true);
        org.junit.Assert.assertNotNull(serializableSpliterator69);
        org.junit.Assert.assertNotNull(serializableArray78);
        org.junit.Assert.assertTrue("'" + boolean80 + "' != '" + true + "'", boolean80 == true);
        org.junit.Assert.assertTrue("'" + boolean85 + "' != '" + false + "'", boolean85 == false);
        org.junit.Assert.assertTrue("'" + boolean86 + "' != '" + false + "'", boolean86 == false);
        org.junit.Assert.assertNotNull(serializableList90);
        org.junit.Assert.assertNotNull(serializableStream91);
        org.junit.Assert.assertNotNull(serializableSpliterator92);
        org.junit.Assert.assertNotNull(objArray93);
        org.junit.Assert.assertEquals(java.util.Arrays.deepToString(objArray93), "[10, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 1.0, false, -1.0, false, 10, 0.0, 4, 0.0, 10, true, [], -1.0, 1.0, hi!, 100, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 100, 0.0, 0, 0, 100.0, 10, true, 1]");
        org.junit.Assert.assertEquals(java.util.Arrays.toString(objArray93), "[10, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 1.0, false, -1.0, false, 10, 0.0, 4, 0.0, 10, true, [], -1.0, 1.0, hi!, 100, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 100, 0.0, 0, 0, 100.0, 10, true, 1]");
        org.junit.Assert.assertNotNull(serializableItor94);
        org.junit.Assert.assertTrue("'" + int96 + "' != '" + (-1) + "'", int96 == (-1));
    }

    @Test
    public void test8() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperC1RandoopRegression0.test8");
        java.util.ArrayList<java.io.Serializable> serializableList1 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        java.util.ArrayList<java.io.Serializable> serializableList7 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean9 = serializableList7.remove((java.lang.Object) (short) 10);
        java.io.Serializable[] serializableArray20 = new java.io.Serializable[] { (byte) -1, "", (short) 100, (byte) 1, serializableList7, (byte) 0, (-1.0f), 10, 0.0d, (byte) 0, 1.0f, '4', 100.0f, (byte) 100, 'a' };
        java.util.ArrayList<java.io.Serializable> serializableList21 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean22 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList21, serializableArray20);
        java.util.Spliterator<java.io.Serializable> serializableSpliterator23 = serializableList21.spliterator();
        java.util.ArrayList<java.io.Serializable> serializableList25 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        int int27 = serializableList25.lastIndexOf((java.lang.Object) 100.0d);
        boolean boolean28 = serializableList21.addAll((java.util.Collection<java.io.Serializable>) serializableList25);
        java.util.ArrayList<java.io.Serializable> serializableList31 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean33 = serializableList31.remove((java.lang.Object) (short) 10);
        java.util.ArrayList<java.io.Serializable> serializableList43 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        java.util.ArrayList<java.io.Serializable> serializableList53 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean55 = serializableList53.remove((java.lang.Object) (short) 10);
        java.io.Serializable[] serializableArray66 = new java.io.Serializable[] { (byte) -1, "", (short) 100, (byte) 1, serializableList53, (byte) 0, (-1.0f), 10, 0.0d, (byte) 0, 1.0f, '4', 100.0f, (byte) 100, 'a' };
        java.util.ArrayList<java.io.Serializable> serializableList67 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean68 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList67, serializableArray66);
        java.util.Spliterator<java.io.Serializable> serializableSpliterator69 = serializableList67.spliterator();
        java.io.Serializable[] serializableArray78 = new java.io.Serializable[] { (byte) 10, serializableList21, 1.0f, boolean33, (-1.0f), false, (byte) 10, 0.0d, '4', 0.0f, (byte) 10, true, serializableList43, (-1.0d), 1.0f, "hi!", 100, serializableList67, (short) 100, 0.0d, (byte) 0, 0, 100.0d, (byte) 10, true, (byte) 1 };
        java.util.ArrayList<java.io.Serializable> serializableList79 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean80 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList79, serializableArray78);
        java.util.ArrayList<java.io.Serializable> serializableList83 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean85 = serializableList83.remove((java.lang.Object) (short) 10);
        boolean boolean86 = serializableList79.addAll(10, (java.util.Collection<java.io.Serializable>) serializableList83);
        serializableList79.trimToSize();
        java.util.List<java.io.Serializable> serializableList90 = serializableList79.subList((int) (byte) 0, 1);
        java.util.stream.Stream<java.io.Serializable> serializableStream91 = serializableList79.parallelStream();
        java.util.Spliterator<java.io.Serializable> serializableSpliterator92 = serializableList79.spliterator();
        java.lang.Object[] objArray93 = serializableList79.toArray();
        java.util.Iterator<java.io.Serializable> serializableItor94 = serializableList79.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.io.Serializable> serializableItor95 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.io.Serializable>(serializableItor94);
        // The following exception was thrown during execution in test generation
        try {
            java.io.Serializable serializable96 = serializableItor95.previous();
            org.junit.Assert.fail("Expected exception of type java.util.NoSuchElementException; message: null");
        } catch (java.util.NoSuchElementException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + false + "'", boolean9 == false);
        org.junit.Assert.assertNotNull(serializableArray20);
        org.junit.Assert.assertTrue("'" + boolean22 + "' != '" + true + "'", boolean22 == true);
        org.junit.Assert.assertNotNull(serializableSpliterator23);
        org.junit.Assert.assertTrue("'" + int27 + "' != '" + (-1) + "'", int27 == (-1));
        org.junit.Assert.assertTrue("'" + boolean28 + "' != '" + false + "'", boolean28 == false);
        org.junit.Assert.assertTrue("'" + boolean33 + "' != '" + false + "'", boolean33 == false);
        org.junit.Assert.assertTrue("'" + boolean55 + "' != '" + false + "'", boolean55 == false);
        org.junit.Assert.assertNotNull(serializableArray66);
        org.junit.Assert.assertTrue("'" + boolean68 + "' != '" + true + "'", boolean68 == true);
        org.junit.Assert.assertNotNull(serializableSpliterator69);
        org.junit.Assert.assertNotNull(serializableArray78);
        org.junit.Assert.assertTrue("'" + boolean80 + "' != '" + true + "'", boolean80 == true);
        org.junit.Assert.assertTrue("'" + boolean85 + "' != '" + false + "'", boolean85 == false);
        org.junit.Assert.assertTrue("'" + boolean86 + "' != '" + false + "'", boolean86 == false);
        org.junit.Assert.assertNotNull(serializableList90);
        org.junit.Assert.assertNotNull(serializableStream91);
        org.junit.Assert.assertNotNull(serializableSpliterator92);
        org.junit.Assert.assertNotNull(objArray93);
        org.junit.Assert.assertEquals(java.util.Arrays.deepToString(objArray93), "[10, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 1.0, false, -1.0, false, 10, 0.0, 4, 0.0, 10, true, [], -1.0, 1.0, hi!, 100, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 100, 0.0, 0, 0, 100.0, 10, true, 1]");
        org.junit.Assert.assertEquals(java.util.Arrays.toString(objArray93), "[10, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 1.0, false, -1.0, false, 10, 0.0, 4, 0.0, 10, true, [], -1.0, 1.0, hi!, 100, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 100, 0.0, 0, 0, 100.0, 10, true, 1]");
        org.junit.Assert.assertNotNull(serializableItor94);
    }

    @Test
    public void test9() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ListIteratorWrapperC1RandoopRegression0.test9");
        java.util.ArrayList<java.io.Serializable> serializableList1 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        java.util.ArrayList<java.io.Serializable> serializableList7 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean9 = serializableList7.remove((java.lang.Object) (short) 10);
        java.io.Serializable[] serializableArray20 = new java.io.Serializable[] { (byte) -1, "", (short) 100, (byte) 1, serializableList7, (byte) 0, (-1.0f), 10, 0.0d, (byte) 0, 1.0f, '4', 100.0f, (byte) 100, 'a' };
        java.util.ArrayList<java.io.Serializable> serializableList21 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean22 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList21, serializableArray20);
        java.util.Spliterator<java.io.Serializable> serializableSpliterator23 = serializableList21.spliterator();
        java.util.ArrayList<java.io.Serializable> serializableList25 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        int int27 = serializableList25.lastIndexOf((java.lang.Object) 100.0d);
        boolean boolean28 = serializableList21.addAll((java.util.Collection<java.io.Serializable>) serializableList25);
        java.util.ArrayList<java.io.Serializable> serializableList31 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean33 = serializableList31.remove((java.lang.Object) (short) 10);
        java.util.ArrayList<java.io.Serializable> serializableList43 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        java.util.ArrayList<java.io.Serializable> serializableList53 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean55 = serializableList53.remove((java.lang.Object) (short) 10);
        java.io.Serializable[] serializableArray66 = new java.io.Serializable[] { (byte) -1, "", (short) 100, (byte) 1, serializableList53, (byte) 0, (-1.0f), 10, 0.0d, (byte) 0, 1.0f, '4', 100.0f, (byte) 100, 'a' };
        java.util.ArrayList<java.io.Serializable> serializableList67 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean68 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList67, serializableArray66);
        java.util.Spliterator<java.io.Serializable> serializableSpliterator69 = serializableList67.spliterator();
        java.io.Serializable[] serializableArray78 = new java.io.Serializable[] { (byte) 10, serializableList21, 1.0f, boolean33, (-1.0f), false, (byte) 10, 0.0d, '4', 0.0f, (byte) 10, true, serializableList43, (-1.0d), 1.0f, "hi!", 100, serializableList67, (short) 100, 0.0d, (byte) 0, 0, 100.0d, (byte) 10, true, (byte) 1 };
        java.util.ArrayList<java.io.Serializable> serializableList79 = new java.util.ArrayList<java.io.Serializable>();
        boolean boolean80 = java.util.Collections.addAll((java.util.Collection<java.io.Serializable>) serializableList79, serializableArray78);
        java.util.ArrayList<java.io.Serializable> serializableList83 = new java.util.ArrayList<java.io.Serializable>((int) (byte) 10);
        boolean boolean85 = serializableList83.remove((java.lang.Object) (short) 10);
        boolean boolean86 = serializableList79.addAll(10, (java.util.Collection<java.io.Serializable>) serializableList83);
        serializableList79.trimToSize();
        java.util.List<java.io.Serializable> serializableList90 = serializableList79.subList((int) (byte) 0, 1);
        java.util.stream.Stream<java.io.Serializable> serializableStream91 = serializableList79.parallelStream();
        java.util.Spliterator<java.io.Serializable> serializableSpliterator92 = serializableList79.spliterator();
        java.lang.Object[] objArray93 = serializableList79.toArray();
        java.util.Iterator<java.io.Serializable> serializableItor94 = serializableList79.iterator();
        org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.io.Serializable> serializableItor95 = new org.apache.openjpa.lib.util.collections.ListIteratorWrapper<java.io.Serializable>(serializableItor94);
        boolean boolean96 = serializableItor95.hasPrevious();
        int int97 = serializableItor95.nextIndex();
        serializableItor95.reset();
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + false + "'", boolean9 == false);
        org.junit.Assert.assertNotNull(serializableArray20);
        org.junit.Assert.assertTrue("'" + boolean22 + "' != '" + true + "'", boolean22 == true);
        org.junit.Assert.assertNotNull(serializableSpliterator23);
        org.junit.Assert.assertTrue("'" + int27 + "' != '" + (-1) + "'", int27 == (-1));
        org.junit.Assert.assertTrue("'" + boolean28 + "' != '" + false + "'", boolean28 == false);
        org.junit.Assert.assertTrue("'" + boolean33 + "' != '" + false + "'", boolean33 == false);
        org.junit.Assert.assertTrue("'" + boolean55 + "' != '" + false + "'", boolean55 == false);
        org.junit.Assert.assertNotNull(serializableArray66);
        org.junit.Assert.assertTrue("'" + boolean68 + "' != '" + true + "'", boolean68 == true);
        org.junit.Assert.assertNotNull(serializableSpliterator69);
        org.junit.Assert.assertNotNull(serializableArray78);
        org.junit.Assert.assertTrue("'" + boolean80 + "' != '" + true + "'", boolean80 == true);
        org.junit.Assert.assertTrue("'" + boolean85 + "' != '" + false + "'", boolean85 == false);
        org.junit.Assert.assertTrue("'" + boolean86 + "' != '" + false + "'", boolean86 == false);
        org.junit.Assert.assertNotNull(serializableList90);
        org.junit.Assert.assertNotNull(serializableStream91);
        org.junit.Assert.assertNotNull(serializableSpliterator92);
        org.junit.Assert.assertNotNull(objArray93);
        org.junit.Assert.assertEquals(java.util.Arrays.deepToString(objArray93), "[10, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 1.0, false, -1.0, false, 10, 0.0, 4, 0.0, 10, true, [], -1.0, 1.0, hi!, 100, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 100, 0.0, 0, 0, 100.0, 10, true, 1]");
        org.junit.Assert.assertEquals(java.util.Arrays.toString(objArray93), "[10, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 1.0, false, -1.0, false, 10, 0.0, 4, 0.0, 10, true, [], -1.0, 1.0, hi!, 100, [-1, , 100, 1, [], 0, -1.0, 10, 0.0, 0, 1.0, 4, 100.0, 100, a], 100, 0.0, 0, 0, 100.0, 10, true, 1]");
        org.junit.Assert.assertNotNull(serializableItor94);
        org.junit.Assert.assertTrue("'" + boolean96 + "' != '" + false + "'", boolean96 == false);
        org.junit.Assert.assertTrue("'" + int97 + "' != '" + 0 + "'", int97 == 0);
    }
}

