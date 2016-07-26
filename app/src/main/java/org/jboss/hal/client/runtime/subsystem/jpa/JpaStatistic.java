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
package org.jboss.hal.client.runtime.subsystem.jpa;

import org.jboss.hal.core.Strings;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;

/**
 * @author Harald Pehl
 */
public class JpaStatistic extends NamedNode {

    private final ResourceAddress address;

    public JpaStatistic(final ResourceAddress address, final ModelNode node) {
        super(Strings.substringAfterLast(node.get("hibernate-persistence-unit").asString(), "#"), node);
        this.address = address;
    }

    public boolean isStatisticsEnabled() {
        return hasDefined(STATISTICS_ENABLED) && get(STATISTICS_ENABLED).asBoolean();
    }

    public String getDeployment() {
        return address.asPropertyList().stream()
                .filter(property -> DEPLOYMENT.equals(property.getName()))
                .findFirst()
                .map((property) -> property.getValue().asString())
                .orElse(Names.NOT_AVAILABLE);
    }

    public ResourceAddress getAddress() {
        return address;
    }
}
