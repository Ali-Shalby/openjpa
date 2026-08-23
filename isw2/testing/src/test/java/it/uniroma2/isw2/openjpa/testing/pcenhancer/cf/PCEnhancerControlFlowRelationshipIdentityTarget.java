/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0.
 */

package it.uniroma2.isw2.openjpa.testing.pcenhancer.cf;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToOne;

/**
 * Controlled target for the relationship-valued primary-key scenario.
 *
 * <p>The composite application identity contains a persistent relationship
 * as one of its primary-key attributes. The corresponding IdClass attribute
 * uses the identifier type of the referenced entity.</p>
 */
@Entity
@IdClass(
        PCEnhancerControlFlowRelationshipIdentityTarget.RelationshipIdentityId.class
)
public class PCEnhancerControlFlowRelationshipIdentityTarget {

    @Id
    @ManyToOne(optional = false)
    private PCEnhancerControlFlowRelationshipIdentityParentTarget parent;

    @Id
    private long sequenceId;

    private String value;

    public PCEnhancerControlFlowRelationshipIdentityTarget() {
    }

    public static final class RelationshipIdentityId
            implements Serializable {

        private long parent;

        private long sequenceId;

        public RelationshipIdentityId() {
        }

        public RelationshipIdentityId(
                long parent,
                long sequenceId) {

            this.parent = parent;
            this.sequenceId = sequenceId;
        }

        @Override
        public boolean equals(Object other) {

            if (this == other) {
                return true;
            }

            if (!(other instanceof RelationshipIdentityId that)) {
                return false;
            }

            return parent == that.parent
                    && sequenceId == that.sequenceId;
        }

        @Override
        public int hashCode() {

            return Objects.hash(
                    parent,
                    sequenceId);
        }
    }
}
