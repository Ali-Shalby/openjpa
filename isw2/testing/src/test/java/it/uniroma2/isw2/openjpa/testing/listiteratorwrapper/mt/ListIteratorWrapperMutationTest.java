/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0.
 */

package it.uniroma2.isw2.openjpa.testing.listiteratorwrapper.mt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.openjpa.lib.util.collections.ListIteratorWrapper;
import org.junit.jupiter.api.Test;

class ListIteratorWrapperMutationTest {

    /*
     * TMT-001
     *
     * Mutation targets:
     *
     * - hasNext():
     *   surviving removal of the currentIndex == wrappedIteratorIndex
     *   condition;
     *
     * - hasNext():
     *   previously uncovered false-return mutation on the cached
     *   navigation path;
     *
     * - hasPrevious():
     *   surviving removal of the currentIndex > 0 comparison.
     *
     * The test deliberately observes both frontier and cached states
     * of a wrapper backed by a plain Iterator.
     */
    @Test
    void tmt001NavigationPredicatesRemainCorrectAcrossCachedAndFrontierStates() {

        ArrayList<String> values =
                new ArrayList<>(List.of("A", "B"));

        ListIteratorWrapper<String> wrapper =
                new ListIteratorWrapper<>(values.iterator());

        assertEquals("A", wrapper.next());

        // currentIndex > 0:
        // hasPrevious() must be true.
        assertTrue(wrapper.hasPrevious());

        assertEquals("B", wrapper.next());

        // Move backward into the internally cached portion.
        assertEquals("B", wrapper.previous());

        // currentIndex < wrappedIteratorIndex:
        // the cached element must still be available.
        assertTrue(wrapper.hasNext());

        assertEquals("B", wrapper.next());

        // At the frontier, with the wrapped iterator exhausted,
        // hasNext() must be false.
        assertFalse(wrapper.hasNext());
    }

    /*
     * TMT-002
     *
     * Mutation target:
     *
     * remove():
     * surviving removal of the
     * wrappedIteratorIndex - currentIndex > 1
     * comparison.
     *
     * After complete consumption:
     *
     *   wrappedIteratorIndex = 3
     *   removeState          = true
     *
     * reset() moves currentIndex back to 0 while preserving the
     * underlying iterator position. Therefore remove() must reject
     * the operation because the wrapper cursor is more than one
     * element behind the wrapped iterator cursor.
     */
    @Test
    void tmt002RemoveIsRejectedAfterResetFromConsumedIterator() {

        ArrayList<String> values =
                new ArrayList<>(List.of("A", "B", "C"));

        ListIteratorWrapper<String> wrapper =
                new ListIteratorWrapper<>(values.iterator());

        assertEquals("A", wrapper.next());
        assertEquals("B", wrapper.next());
        assertEquals("C", wrapper.next());

        wrapper.reset();

        assertThrows(
                IllegalStateException.class,
                wrapper::remove
        );

        // The rejected removal must not alter the underlying data.
        assertEquals(List.of("A", "B", "C"), values);
    }
}