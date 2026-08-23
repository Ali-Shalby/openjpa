/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0.
 */

package it.uniroma2.isw2.openjpa.testing.pcenhancer.cf;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Controlled persistent target referenced by the TCF-004 candidate.
 *
 * <p>This class is a fixture, not a test subject.</p>
 */
@Entity
public class PCEnhancerControlFlowRelationshipIdentityParentTarget {

    @Id
    private long id;

    private String label;

    public PCEnhancerControlFlowRelationshipIdentityParentTarget() {
    }
}
