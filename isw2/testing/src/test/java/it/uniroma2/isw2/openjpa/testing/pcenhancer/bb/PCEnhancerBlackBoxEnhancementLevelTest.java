/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.openjpa.enhance.FieldConsumer;
import org.apache.openjpa.enhance.FieldSupplier;
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.enhance.PCRegistry;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.StateManager;
import org.apache.openjpa.lib.log.Log;
import org.junit.jupiter.api.Test;

/**
 * Manual black-box tests for the documented enhancement-contract
 * level boundary exposed by PCEnhancer.checkEnhancementLevel(...).
 *
 * Category Partition frames: TBB-029..TBB-030.
 */
class PCEnhancerBlackBoxEnhancementLevelTest {

    @Test
    void tbb029CurrentEnhancementContractVersionIsNotDownLevel() {

        CurrentLevelPersistenceCapable prototype =
                new CurrentLevelPersistenceCapable();

        register(
                CurrentLevelPersistenceCapable.class,
                prototype
        );

        CapturingLog log =
                new CapturingLog();

        boolean result =
                PCEnhancer.checkEnhancementLevel(
                        CurrentLevelPersistenceCapable.class,
                        log
                );

        assertFalse(
                result,
                "A class enhanced at the current contract version "
                        + "must not be reported as down-level"
        );

        assertTrue(
                log.infoMessages().isEmpty(),
                "No down-level message must be logged for the current version"
        );
    }

    @Test
    void tbb030ImmediatelyOlderEnhancementContractVersionIsDownLevel() {

        OlderLevelPersistenceCapable prototype =
                new OlderLevelPersistenceCapable();

        register(
                OlderLevelPersistenceCapable.class,
                prototype
        );

        CapturingLog log =
                new CapturingLog();

        boolean result =
                PCEnhancer.checkEnhancementLevel(
                        OlderLevelPersistenceCapable.class,
                        log
                );

        assertTrue(
                result,
                "A contract version immediately below ENHANCER_VERSION "
                        + "must be reported as down-level"
        );

        assertFalse(
                log.infoMessages().isEmpty(),
                "The down-level condition must be logged"
        );
    }

    private static void register(
            Class<?> type,
            PersistenceCapable prototype) {

        PCRegistry.register(
                type,
                new String[0],
                new Class<?>[0],
                new byte[0],
                null,
                type.getName(),
                prototype
        );

        assertTrue(
                PCRegistry.isRegistered(type),
                "The purpose-built PersistenceCapable fixture must be registered"
        );
    }

    /**
     * Minimal PersistenceCapable implementation used only to control the
     * enhancement-contract version returned to PCEnhancer.
     *
     * Methods unrelated to F10 return neutral values or perform no action.
     */
    private abstract static class VersionedPersistenceCapable
            implements PersistenceCapable {

        private final int enhancementContractVersion;

        VersionedPersistenceCapable(
                int enhancementContractVersion) {

            this.enhancementContractVersion =
                    enhancementContractVersion;
        }

        @Override
        public int pcGetEnhancementContractVersion() {
            return enhancementContractVersion;
        }

        @Override
        public Object pcGetGenericContext() {
            return null;
        }

        @Override
        public StateManager pcGetStateManager() {
            return null;
        }

        @Override
        public void pcReplaceStateManager(
                StateManager sm) {
            // Not relevant to F10.
        }

        @Override
        public void pcProvideField(
                int fieldIndex) {
            // Not relevant to F10.
        }

        @Override
        public void pcProvideFields(
                int[] fieldIndices) {
            // Not relevant to F10.
        }

        @Override
        public void pcReplaceField(
                int fieldIndex) {
            // Not relevant to F10.
        }

        @Override
        public void pcReplaceFields(
                int[] fieldIndices) {
            // Not relevant to F10.
        }

        @Override
        public void pcCopyFields(
                Object fromObject,
                int[] fields) {
            // Not relevant to F10.
        }

        @Override
        public void pcDirty(
                String fieldName) {
            // Not relevant to F10.
        }

        @Override
        public Object pcFetchObjectId() {
            return null;
        }

        @Override
        public Object pcGetVersion() {
            return null;
        }

        @Override
        public boolean pcIsDirty() {
            return false;
        }

        @Override
        public boolean pcIsTransactional() {
            return false;
        }

        @Override
        public boolean pcIsPersistent() {
            return false;
        }

        @Override
        public boolean pcIsNew() {
            return false;
        }

        @Override
        public boolean pcIsDeleted() {
            return false;
        }

        @Override
        public Boolean pcIsDetached() {
            return null;
        }

        @Override
        public PersistenceCapable pcNewInstance(
                StateManager sm,
                boolean clear) {

            /*
             * PCRegistry.newInstance(...) is used by
             * PCEnhancer.checkEnhancementLevel(...).
             *
             * Returning this prototype is sufficient because F10
             * observes only pcGetEnhancementContractVersion().
             */
            return this;
        }

        @Override
        public PersistenceCapable pcNewInstance(
                StateManager sm,
                Object objectId,
                boolean clear) {

            return this;
        }

        @Override
        public Object pcNewObjectIdInstance() {
            return null;
        }

        @Override
        public Object pcNewObjectIdInstance(
                Object object) {
            return null;
        }

        @Override
        public void pcCopyKeyFieldsToObjectId(
                Object objectId) {
            // Not relevant to F10.
        }

        @Override
        public void pcCopyKeyFieldsToObjectId(
                FieldSupplier supplier,
                Object objectId) {
            // Not relevant to F10.
        }

        @Override
        public void pcCopyKeyFieldsFromObjectId(
                FieldConsumer consumer,
                Object objectId) {
            // Not relevant to F10.
        }

        @Override
        public Object pcGetDetachedState() {
            return null;
        }

        @Override
        public void pcSetDetachedState(
                Object state) {
            // Not relevant to F10.
        }
    }

    /**
     * Exact boundary value:
     *
     * version == PCEnhancer.ENHANCER_VERSION
     */
    private static final class CurrentLevelPersistenceCapable
            extends VersionedPersistenceCapable {

        CurrentLevelPersistenceCapable() {
            super(
                    PCEnhancer.ENHANCER_VERSION
            );
        }
    }

    /**
     * Immediately lower boundary value:
     *
     * version == PCEnhancer.ENHANCER_VERSION - 1
     */
    private static final class OlderLevelPersistenceCapable
            extends VersionedPersistenceCapable {

        OlderLevelPersistenceCapable() {
            super(
                    PCEnhancer.ENHANCER_VERSION - 1
            );
        }
    }

    /**
     * Minimal Log implementation used to observe whether
     * checkEnhancementLevel emits the documented informational message.
     */
    private static final class CapturingLog
            implements Log {

        private final List<String> infoMessages =
                new ArrayList<>();

        List<String> infoMessages() {
            return infoMessages;
        }

        @Override
        public boolean isTraceEnabled() {
            return false;
        }

        @Override
        public boolean isInfoEnabled() {
            return true;
        }

        @Override
        public boolean isWarnEnabled() {
            return true;
        }

        @Override
        public boolean isErrorEnabled() {
            return true;
        }

        @Override
        public boolean isFatalEnabled() {
            return true;
        }

        @Override
        public void trace(Object message) {
            // Not relevant to F10.
        }

        @Override
        public void trace(
                Object message,
                Throwable throwable) {
            // Not relevant to F10.
        }

        @Override
        public void info(Object message) {
            infoMessages.add(
                    String.valueOf(message)
            );
        }

        @Override
        public void info(
                Object message,
                Throwable throwable) {

            info(message);
        }

        @Override
        public void warn(Object message) {
            // Not relevant to F10.
        }

        @Override
        public void warn(
                Object message,
                Throwable throwable) {
            // Not relevant to F10.
        }

        @Override
        public void error(Object message) {
            // Not relevant to F10.
        }

        @Override
        public void error(
                Object message,
                Throwable throwable) {
            // Not relevant to F10.
        }

        @Override
        public void fatal(Object message) {
            // Not relevant to F10.
        }

        @Override
        public void fatal(
                Object message,
                Throwable throwable) {
            // Not relevant to F10.
        }
    }
}
