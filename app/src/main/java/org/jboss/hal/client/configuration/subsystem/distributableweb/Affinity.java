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
package org.jboss.hal.client.configuration.subsystem.distributableweb;

import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

/** Represents the different affinity singletons of a specific session management resource. */
enum Affinity {

    UNDEFINED(ModelDescriptionConstants.UNDEFINED, ModelDescriptionConstants.UNDEFINED, "null"), // represents undefined
                                                                                                 // affinity, not an actual type
                                                                                                 // in the subsystem

    LOCAL(Ids.AFFINITY_LOCAL, Names.LOCAL, ModelDescriptionConstants.LOCAL), NONE(Ids.AFFINITY_NONE, Names.NONE,
            ModelDescriptionConstants.NONE), PRIMARY_OWNER(Ids.AFFINITY_PRIMARY_OWNER, Names.PRIMARY_OWNER,
                    ModelDescriptionConstants.PRIMARY_OWNER), RANKED(Ids.AFFINITY_RANKED, Names.RANKED,
                            ModelDescriptionConstants.RANKED);

    static Affinity fromResource(String resource) {
        if (resource != null) {
            for (Affinity affinity : Affinity.values()) {
                if (affinity.resource.equalsIgnoreCase(resource)) {
                    return affinity;
                }
            }
        }
        return null;
    }

    final String baseId;
    final String type;
    final String resource;

    Affinity(String baseId, String type, String resource) {
        this.baseId = baseId;
        this.type = type;
        this.resource = resource;
    }
}
