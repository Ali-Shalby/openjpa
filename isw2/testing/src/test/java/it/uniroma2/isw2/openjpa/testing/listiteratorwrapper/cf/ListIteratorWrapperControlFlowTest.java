/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0.
 */

package it.uniroma2.isw2.openjpa.testing.listiteratorwrapper.cf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.openjpa.lib.util.collections.ListIteratorWrapper;
import org.junit.jupiter.api.Test;

class ListIteratorWrapperControlFlowTest {

    /*
     * TCF-001
     *
     * Structural target:
     * iterator instanceof ListIterator == true
     * in navigation/index/reset methods.
     */
    @Test
    void tcf001ListIteratorNavigationIndicesAndResetBranches() {
        ArrayList<String> values =
                new ArrayList<>(List.of("A", "B", "C"));

        ListIteratorWrapper<String> wrapper =
                new ListIteratorWrapper<>(values.listIterator());

        assertTrue(wrapper.hasNext());
        assertFalse(wrapper.hasPrevious());
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());

        assertEquals("A", wrapper.next());

        assertTrue(wrapper.hasPrevious());
        assertEquals(1, wrapper.nextIndex());
        assertEquals(0, wrapper.previousIndex());

        assertEquals("A", wrapper.previous());

        assertEquals("A", wrapper.next());
        assertEquals("B", wrapper.next());

        wrapper.reset();

        assertFalse(wrapper.hasPrevious());
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
        assertEquals("A", wrapper.next());
    }

    /*
     * TCF-002
     *
     * Structural target:
     * iterator instanceof ListIterator == true
     * in add(), set() and remove().
     */
    @Test
    void tcf002ListIteratorOptionalOperationsDelegateToWrappedIterator() {
        ArrayList<String> values =
                new ArrayList<>(List.of("A", "C"));

        ListIteratorWrapper<String> wrapper =
                new ListIteratorWrapper<>(values.listIterator());

        wrapper.add("B");

        assertEquals(List.of("B", "A", "C"), values);

        assertEquals("A", wrapper.next());

        wrapper.set("X");

        assertEquals(List.of("B", "X", "C"), values);

        wrapper.remove();

        assertEquals(List.of("B", "C"), values);
    }

    /*
     * TCF-003
     *
     * Structural target in remove():
     * plain Iterator;
     * currentIndex == wrappedIteratorIndex;
     * removeState == true;
     * successful removal path.
     */
    @Test
    void tcf003PlainIteratorRemoveImmediatelyAfterNext() {
        ArrayList<String> values =
                new ArrayList<>(List.of("A", "B", "C"));

        ListIteratorWrapper<String> wrapper =
                new ListIteratorWrapper<>(values.iterator());

        assertEquals("A", wrapper.next());

        wrapper.remove();

        assertEquals(List.of("B", "C"), values);
        assertEquals("B", wrapper.next());
    }

    /*
     * TCF-004
     *
     * Structural target in remove():
     * plain Iterator;
     * currentIndex != wrappedIteratorIndex;
     * removeState remains valid after one previous();
     * successful cached-element removal path.
     */
    @Test
    void tcf004PlainIteratorRemoveAfterOneBackwardStep() {
        ArrayList<String> values =
                new ArrayList<>(List.of("A", "B", "C"));

        ListIteratorWrapper<String> wrapper =
                new ListIteratorWrapper<>(values.iterator());

        assertEquals("A", wrapper.next());
        assertEquals("B", wrapper.next());
        assertEquals("C", wrapper.next());

        assertEquals("C", wrapper.previous());

        wrapper.remove();

        assertEquals(List.of("A", "B"), values);
        assertFalse(wrapper.hasNext());
    }

    /*
     * TCF-005
     *
     * Structural target in remove():
     * plain Iterator;
     * currentIndex != wrappedIteratorIndex;
     * removeState == false after moving farther backward;
     * IllegalStateException path.
     */
    @Test
    void tcf005PlainIteratorRemoveRejectedAfterTwoBackwardSteps() {
        ArrayList<String> values =
                new ArrayList<>(List.of("A", "B", "C"));

        ListIteratorWrapper<String> wrapper =
                new ListIteratorWrapper<>(values.iterator());

        assertEquals("A", wrapper.next());
        assertEquals("B", wrapper.next());
        assertEquals("C", wrapper.next());

        assertEquals("C", wrapper.previous());
        assertEquals("B", wrapper.previous());

        assertThrows(
                IllegalStateException.class,
                wrapper::remove
        );

        assertEquals(List.of("A", "B", "C"), values);
    }
}