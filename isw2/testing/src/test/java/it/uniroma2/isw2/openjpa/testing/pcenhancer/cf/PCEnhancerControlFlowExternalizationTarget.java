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
