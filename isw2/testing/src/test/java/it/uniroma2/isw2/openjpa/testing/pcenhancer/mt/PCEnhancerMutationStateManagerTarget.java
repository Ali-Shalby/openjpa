/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0.
 */

package it.uniroma2.isw2.openjpa.testing.pcenhancer.mt;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class PCEnhancerMutationStateManagerTarget {

    @Id
    private long id;

    private int counter;

    private String label;

    public PCEnhancerMutationStateManagerTarget() {
        id = 101L;
        counter = 202;
        label = "CTOR";
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}