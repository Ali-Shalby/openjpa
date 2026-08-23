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

/**
 * Controlled fixture for the optimized IdClass-copy control-flow scenario.
 *
 * <p>The IdClass deliberately uses private fields, exposes no public
 * setters and provides a public constructor whose parameter order differs
 * from the metadata field order. This allows PCEnhancer to analyze the
 * constructor and use it for optimized identity copying.</p>
 */
@Entity
@IdClass(
        PCEnhancerControlFlowOptimizedIdentityTarget.OptimizedIdentityId.class
)
public class PCEnhancerControlFlowOptimizedIdentityTarget {

    @Id
    private int tenantId;

    @Id
    private long sequenceId;

    @Id
    private String externalCode;

    private String value;

    public PCEnhancerControlFlowOptimizedIdentityTarget() {
    }

    public static final class OptimizedIdentityId
            implements Serializable {

        private int tenantId;

        private long sequenceId;

        private String externalCode;

        public OptimizedIdentityId() {
        }

        /*
         * Deliberately different from the entity metadata order:
         *
         * metadata:
         * tenantId, sequenceId, externalCode
         *
         * constructor:
         * externalCode, sequenceId, tenantId
         */
        public OptimizedIdentityId(
                String externalCode,
                long sequenceId,
                int tenantId) {

            this.externalCode = externalCode;
            this.sequenceId = sequenceId;
            this.tenantId = tenantId;
        }

        @Override
        public boolean equals(Object other) {

            if (this == other) {
                return true;
            }

            if (!(other instanceof OptimizedIdentityId that)) {
                return false;
            }

            return tenantId == that.tenantId
                    && sequenceId == that.sequenceId
                    && Objects.equals(
                    externalCode,
                    that.externalCode);
        }

        @Override
        public int hashCode() {

            return Objects.hash(
                    tenantId,
                    sequenceId,
                    externalCode);
        }
    }
}
