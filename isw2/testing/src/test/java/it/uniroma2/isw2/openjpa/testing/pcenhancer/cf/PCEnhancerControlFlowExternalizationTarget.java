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
import jakarta.persistence.Transient;

import org.apache.openjpa.persistence.DetachedState;

/**
 * Controlled target for the T_CF externalization scenario.
 *
 * <p>This is test input, not an additional experimental subject.</p>
 */
@Entity
@DetachedState
public class PCEnhancerControlFlowExternalizationTarget
        implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private long id;

    private String name;

    /*
     * Unmanaged fields deliberately retained in the class bytecode so that
     * PCEnhancer must handle them during detached externalization.
     */
    @Transient
    private int localCounter;

    @Transient
    private String localNote;

    public PCEnhancerControlFlowExternalizationTarget() {
    }
}
