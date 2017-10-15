/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.flow;

/** Interface to reflect progress of a flow. */
public interface Progress {

    void reset();

    default void reset(int max) {
        reset(max, null);
    }

    void reset(int max, String label);

    default void tick() {
        tick(null);
    }

    void tick(String label);

    void finish();

    Progress NOOP = new Progress() {

        @Override
        public void reset() {
        }

        @Override
        public void reset(int max, String label) {
        }

        @Override
        public void tick(String label) {
        }

        @Override
        public void finish() {
        }
    };
}
