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
import java.util.Set;

import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;

class LookupContext extends FlowContext {

    final boolean recursive;
    final LookupResult lookupResult;
    final Map<ResourceAddress, ResourceDescription> toResourceDescriptionRegistry;
    final Map<ResourceAddress, ResourceDescription> toResourceDescriptionDatabase;
    final Map<ResourceAddress, SecurityContext> toSecurityContextRegistry;
    final Map<ResourceAddress, SecurityContext> toSecurityContextDatabase;

    // for unit testing only!
    LookupContext(LookupResult lookupResult) {
        super(Progress.NOOP);
        this.recursive = false;
        this.lookupResult = lookupResult;
        this.toResourceDescriptionRegistry = new HashMap<>();
        this.toResourceDescriptionDatabase = new HashMap<>();
        this.toSecurityContextRegistry = new HashMap<>();
        this.toSecurityContextDatabase = new HashMap<>();
    }

    LookupContext(Progress progress, Set<AddressTemplate> template, boolean recursive) {
        super(progress);
        this.recursive = recursive;
        this.lookupResult = new LookupResult(template, recursive);
        this.toResourceDescriptionRegistry = new HashMap<>();
        this.toResourceDescriptionDatabase = new HashMap<>();
        this.toSecurityContextRegistry = new HashMap<>();
        this.toSecurityContextDatabase = new HashMap<>();
    }

    boolean updateDatabase() {
        return !toResourceDescriptionDatabase.isEmpty() || !toSecurityContextDatabase.isEmpty();
    }

    boolean updateRegistry() {
        return !toResourceDescriptionRegistry.isEmpty() || !toSecurityContextRegistry.isEmpty();
    }
}
