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
package org.jboss.hal.client.configuration.subsystem.infinispan;

import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.dmr.ModelDescriptionConstants.MEMORY;

/** Represents the different memory singletons of a specific cache resource. */
enum Memory {

    BINARY(Ids.CACHE_MEMORY_BINARY, Names.BINARY, ModelDescriptionConstants.BINARY),
    OBJECT(Ids.CACHE_MEMORY_OBJECT, Names.OBJECT, ModelDescriptionConstants.OBJECT),
    OFF_HEAP(Ids.CACHE_MEMORY_OFF_HEAP, Names.OFF_HEAP, ModelDescriptionConstants.OFF_HEAP);

    static Memory fromResource(String resource) {
        if (resource != null) {
            for (Memory memory : Memory.values()) {
                if (memory.resource.equalsIgnoreCase(resource)) {
                    return memory;
                }
            }
        }
        return null;
    }

    final String baseId;
    final String type;
    final String resource;

    Memory(String baseId, String type, String resource) {
        this.baseId = baseId;
        this.type = type;
        this.resource = resource;
    }

    String path() {
        return MEMORY + "/" + resource;
    }
}
