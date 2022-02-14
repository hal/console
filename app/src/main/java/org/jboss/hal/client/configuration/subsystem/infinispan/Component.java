/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.infinispan;

import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.dmr.ModelDescriptionConstants.COMPONENT;

/** Represents the different component singletons of a specific cache resource. */
enum Component {

    EXPIRATION(Ids.CACHE_COMPONENT_EXPIRATION, Names.EXPIRATION, ModelDescriptionConstants.EXPIRATION), LOCKING(
            Ids.CACHE_COMPONENT_LOCKING, Names.LOCKING, ModelDescriptionConstants.LOCKING), PARTITION_HANDLING(
                    Ids.CACHE_COMPONENT_PARTITION_HANDLING, Names.PARTITION_HANDLING,
                    ModelDescriptionConstants.PARTITION_HANDLING), STATE_TRANSFER(Ids.CACHE_COMPONENT_STATE_TRANSFER,
                            Names.STATE_TRANSFER, ModelDescriptionConstants.STATE_TRANSFER), TRANSACTION(
                                    Ids.CACHE_COMPONENT_TRANSACTION, Names.TRANSACTION, ModelDescriptionConstants.TRANSACTION);

    final String baseId;
    final String type;
    final String resource;

    Component(String baseId, String type, String resource) {
        this.baseId = baseId;
        this.type = type;
        this.resource = resource;
    }

    String path() {
        return COMPONENT + "/" + resource;
    }
}
