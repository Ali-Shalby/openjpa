/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package it.uniroma2.isw2.openjpa.testing.pcenhancer.cf;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

/**
 * Controlled input class used by the T_CF application-identity tests.
 *
 * <p>This class is not a test and is not an experimental subject.
 * It is a target supplied to PCEnhancer in order to exercise the
 * application-identity enhancement control flow.</p>
 */
@Entity
@IdClass(PCEnhancerControlFlowApplicationIdentityTarget.ApplicationIdentityId.class)
public class PCEnhancerControlFlowApplicationIdentityTarget {

    @Id
    private int tenantId;

    @Id
    private long sequenceId;

    @Id
    private String externalCode;

    private String payload;

    /**
     * Required no-argument constructor.
     */
    public PCEnhancerControlFlowApplicationIdentityTarget() {
    }

    /**
     * IdClass associated with the controlled target.
     */
    public static class ApplicationIdentityId implements Serializable {

        private static final long serialVersionUID = 1L;

        public int tenantId;
        public long sequenceId;
        public String externalCode;

        /**
         * Required no-argument constructor for the IdClass.
         */
        public ApplicationIdentityId() {
        }

        @Override
        public boolean equals(Object object) {

            if (this == object) {
                return true;
            }

            if (!(object instanceof ApplicationIdentityId other)) {
                return false;
            }

            return tenantId == other.tenantId
                    && sequenceId == other.sequenceId
                    && Objects.equals(
                    externalCode,
                    other.externalCode);
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
