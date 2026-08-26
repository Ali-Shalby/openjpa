/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0.
 */
package it.uniroma2.isw2.openjpa.testing.listiteratorwrapper.bb;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.openjpa.lib.util.collections.ListIteratorWrapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListIteratorWrapperBlackBoxNavigationTest {

    @Test
    void tbb002EmptyIteratorExposesInitialBoundaries() {
        ListIteratorWrapper<String> wrapper =
                new ListIteratorWrapper<>(new ArrayList<String>().iterator());

        assertFalse(wrapper.hasNext());
        assertFalse(wrapper.hasPrevious());
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
    }

    @Test
    void tbb003NextPastEndIsRejected() {
        ListIteratorWrapper<String> wrapper =
                new ListIteratorWrapper<>(new ArrayList<String>().iterator());

        assertThrows(NoSuchElementException.class, wrapper::next);
    }

    @Test
    void tbb004PreviousBeforeBeginningIsRejected() {
        ListIteratorWrapper<String> wrapper =
                new ListIteratorWrapper<>(
                        new ArrayList<>(List.of("A")).iterator()
                );

        assertThrows(NoSuchElementException.class, wrapper::previous);
    }

    @Test
    void tbb005SingleElementSupportsForwardAndBackwardNavigation() {
        ListIteratorWrapper<String> wrapper =
                new ListIteratorWrapper<>(
                        new ArrayList<>(List.of("A")).iterator()
                );

        assertEquals("A", wrapper.next());
        assertEquals(1, wrapper.nextIndex());
        assertEquals(0, wrapper.previousIndex());

        assertEquals("A", wrapper.previous());
        assertEquals(0, wrapper.nextIndex());
        assertEquals(-1, wrapper.previousIndex());
    }

    @Test
    void tbb006MultipleElementsAreTraversedForwardInOrder() {
        ListIteratorWrapper<String> wrapper =
                new ListIteratorWrapper<>(
                        new ArrayList<>(List.of("A", "B", "C")).iterator()
                );

        assertEquals("A", wrapper.next());
        assertEquals("B", wrapper.next());
        assertEquals("C", wrapper.next());
        assertFalse(wrapper.hasNext());
    }

    @Test
    void tbb007MultipleElementsAreTraversedBackwardInReverseOrder() {
        ListIteratorWrapper<String> wrapper =
                new ListIteratorWrapper<>(
                        new ArrayList<>(List.of("A", "B", "C")).iterator()
                );

        wrapper.next();
        wrapper.next();
        wrapper.next();

        assertEquals("C", wrapper.previous());
        assertEquals("B", wrapper.previous());
        assertEquals("A", wrapper.previous());
        assertFalse(wrapper.hasPrevious());
    }

    @Test
    void tbb008AlternatingNavigationReturnsTheSameElement() {
        ListIteratorWrapper<String> wrapper =
                new ListIteratorWrapper<>(
                        new ArrayList<>(List.of("A", "B", "C")).iterator()
                );

        assertEquals("A", wrapper.next());
        assertEquals("A", wrapper.previous());
        assertEquals("A", wrapper.next());
    }
}
