/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0.
 */

package it.uniroma2.isw2.openjpa.testing.pcenhancer.cf;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Controlled target for the T_CF serialization scenario.
 *
 * <p>This class is test input and is not an additional experimental subject.</p>
 */
@Entity
public class PCEnhancerControlFlowSerializationTarget
        implements Serializable {

    @Id
    private long id;

    private String name;

    public PCEnhancerControlFlowSerializationTarget() {
    }
}
