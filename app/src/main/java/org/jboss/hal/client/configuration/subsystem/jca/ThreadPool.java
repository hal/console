/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.jca;

import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;

import static org.jboss.hal.resources.Names.LONG_RUNNING;
import static org.jboss.hal.resources.Names.SHORT_RUNNING;

/** Model node for {@code short-running-threads} and {@code long-running-threads} resources. */
class ThreadPool extends NamedNode {

    private final boolean longRunning;

    ThreadPool(final Property property, final boolean longRunning) {
        super(property);
        this.longRunning = longRunning;
    }

    boolean isLongRunning() {
        return longRunning;
    }

    String getRunningMode() {
        return longRunning ? LONG_RUNNING : SHORT_RUNNING;
    }

    String id() {
        return getName() + "-" + getRunningMode();
    }
}
