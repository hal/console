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
package org.jboss.hal.meta.processing;

import java.util.HashMap;
import java.util.Map;

import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;

/**
 * @author Harald Pehl
 */
class RrdResult {

    final Map<ResourceAddress, ResourceDescription> resourceDescriptions;
    final Map<ResourceAddress, SecurityContext> securityContexts;

    RrdResult() {
        resourceDescriptions = new HashMap<>();
        securityContexts = new HashMap<>();
    }

    boolean containsResourceDescription(ResourceAddress address) {
        return resourceDescriptions.containsKey(address);
    }

    void addResourceDescription(ResourceDescription resourceDescription) {
        if (!resourceDescriptions.containsKey(resourceDescription.getAddress())) {
            resourceDescriptions.put(resourceDescription.getAddress(), resourceDescription);
        }
    }

    boolean containsSecurityContext(ResourceAddress address) {
        return securityContexts.containsKey(address);
    }

    void addSecurityContext(SecurityContext securityContext) {
        if (!securityContexts.containsKey(securityContext.getAddress())) {
            securityContexts.put(securityContext.getAddress(), securityContext);
        }
    }
}
