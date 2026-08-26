/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0.
 */
package it.uniroma2.isw2.openjpa.testing.listiteratorwrapper.bb;

import org.apache.openjpa.lib.util.collections.ListIteratorWrapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ListIteratorWrapperBlackBoxConstructionTest {

    @Test
    void tbb001NullIteratorIsRejected() {
        assertThrows(
                NullPointerException.class,
                () -> new ListIteratorWrapper<String>(null)
        );
    }
}
