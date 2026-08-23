/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.uniroma2.isw2.openjpa.testing.pcenhancer.bb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.openjpa.enhance.PCEnhancer;
import org.junit.jupiter.api.Test;

/**
 * Black-box tests for the public PCEnhancer subclass-name operations.
 *
 * <p>Category Partition frames:
 * TBB-001..TBB-004 and TBB-006..TBB-007.</p>
 */
class PCEnhancerBlackBoxNamingTest {

    private static final String MANAGED_TYPE_NAME = "com.example.Customer";

    /*
     * The functional category "generated persistence-capable subclass name"
     * was frozen before source inspection.
     *
     * Minimal inspection of the OpenJPA 4.1.1 C0 implementation was used only
     * to instantiate a concrete value for that already-defined category.
     */
    private static final String GENERATED_SUBCLASS_NAME =
            "org.apache.openjpa.enhance.com$example$Customer$pcsubclass";

    @Test
    void tbb001GeneratedSubclassNameIsRecognized() {
        assertTrue(PCEnhancer.isPCSubclassName(GENERATED_SUBCLASS_NAME));
    }

    @Test
    void tbb002OrdinaryClassNameIsNotRecognized() {
        assertFalse(PCEnhancer.isPCSubclassName(MANAGED_TYPE_NAME));
    }

    @Test
    void tbb003GeneratedSubclassNameConvertsToManagedTypeName() {
        assertEquals(
                MANAGED_TYPE_NAME,
                PCEnhancer.toManagedTypeName(GENERATED_SUBCLASS_NAME));
    }

    @Test
    void tbb004OrdinaryClassNameRemainsUnchanged() {
        assertEquals(
                MANAGED_TYPE_NAME,
                PCEnhancer.toManagedTypeName(MANAGED_TYPE_NAME));
    }

    @Test
    void tbb006EmptyNameIsNotRecognizedAsGeneratedSubclass() {
        assertFalse(PCEnhancer.isPCSubclassName(""));
    }

    @Test
    void tbb007EmptyNameRemainsUnchangedOnConversion() {
        assertEquals("", PCEnhancer.toManagedTypeName(""));
    }
}
