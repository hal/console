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

import java.util.HashSet;
import java.util.Set;

import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.capabilitiy.Capability;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;

/**
 * @author Harald Pehl
 */
class RrdResult {

    final ResourceAddress address;
    ResourceDescription resourceDescription;
    SecurityContext securityContext;
    Set<Capability> capabilities;

    RrdResult(final ResourceAddress address) {
        this.address = address;
        this.capabilities = new HashSet<>();
    }

    boolean isDefined() {
        return resourceDescription != null || securityContext != null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof RrdResult)) { return false; }

        RrdResult rrdResult = (RrdResult) o;

        return address.equals(rrdResult.address);

    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }
}
