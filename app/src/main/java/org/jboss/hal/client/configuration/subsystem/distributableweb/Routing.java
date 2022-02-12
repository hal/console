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
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.client.configuration.subsystem.distributableweb.AddressTemplates.DISTRIBUTABLE_WEB_TEMPLATE;

/** Represents the different routing singletons of the distributable web subsystem. */
public enum Routing {

    LOCAL(Ids.DISTRIBUTABLE_WEB_ROUTING_LOCAL, Names.LOCAL, ModelDescriptionConstants.LOCAL, false), INFINISPAN(
            Ids.DISTRIBUTABLE_WEB_ROUTING_INFINISPAN, Names.INFINISPAN, ModelDescriptionConstants.INFINISPAN, true);

    static Routing fromResource(String resource) {
        if (resource != null) {
            for (Routing routing : Routing.values()) {
                if (routing.resource.equalsIgnoreCase(resource)) {
                    return routing;
                }
            }
        }
        return null;
    }

    final String baseId;
    final String type;
    final String resource;
    final boolean addWithDialog;

    Routing(String baseId, String type, String resource, boolean addWithDialog) {
        this.baseId = baseId;
        this.type = type;
        this.resource = resource;
        this.addWithDialog = addWithDialog;
    }

    AddressTemplate template() {
        return DISTRIBUTABLE_WEB_TEMPLATE.append("routing=" + resource);
    }
}
