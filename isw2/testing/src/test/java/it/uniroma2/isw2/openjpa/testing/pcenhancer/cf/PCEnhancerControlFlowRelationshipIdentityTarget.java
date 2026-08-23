/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
