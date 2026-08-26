/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0.
 */
package it.uniroma2.isw2.openjpa.testing.listiteratorwrapper.bb;

import java.util.ArrayList;
import java.util.List;

import org.apache.openjpa.lib.util.collections.ListIteratorWrapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ListIteratorWrapperBlackBoxOptionalOperationsTest {

    @Test
    void tbb011AddIsUnsupportedForPlainIterator() {
        ListIteratorWrapper<String> wrapper =
                new ListIteratorWrapper<>(
                        new ArrayList<>(List.of("A", "B", "C")).iterator()
                );

        assertThrows(
                UnsupportedOperationException.class,
                () -> wrapper.add("X")
        );
    }

    @Test
    void tbb012SetIsUnsupportedForPlainIterator() {
        ListIteratorWrapper<String> wrapper =
                new ListIteratorWrapper<>(
                        new ArrayList<>(List.of("A", "B", "C")).iterator()
                );

        assertThrows(
                UnsupportedOperationException.class,
                () -> wrapper.set("X")
        );
    }
}
