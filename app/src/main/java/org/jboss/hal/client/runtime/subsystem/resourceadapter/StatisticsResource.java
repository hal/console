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
package org.jboss.hal.client.runtime.subsystem.resourceadapter;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.AddressTemplate;

import static org.jboss.hal.client.runtime.subsystem.resourceadapter.AddressTemplates.ADMIN_OBJECT_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.resourceadapter.AddressTemplates.CONN_DEF_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.resourceadapter.AddressTemplates.RESOURCE_ADAPTER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;

/**
 * Wrapper for easier handling of "statistics" resources ("statistics" is the only runtime resource)
 *
 * "statistics=extended" is a child of "resource-adapter=*" and its children "admin-objects=*" and "connection-definitions=*"
 * (conditionally present in all)
 *
 * "statistics=pool" is a child of "connection-definitions=*" (always present)
 */

public class StatisticsResource extends NamedNode {

    private static final String EXTENDED = "extended";
    private static final String EXTENDED_STATS = STATISTICS + "/" + EXTENDED;
    private static final String POOL_STATS = STATISTICS + "/pool";

    public static final String EXT_STATS_AVAILABLE = "extended-statistics-available";

    private final String parentName;
    private final ResourceType resourceType;

    public StatisticsResource(final Property property) {
        this(null, ResourceType.RESOURCE_ADAPTER, property);
    }

    public StatisticsResource(String parentName, ResourceType resourceType, final Property property) {
        this(parentName, property.getName(), resourceType, property.getValue());
    }

    public StatisticsResource(String parentName, String name, ResourceType resourceType, ModelNode modelNode) {
        super(name, modelNode);
        this.parentName = parentName;
        this.resourceType = resourceType;
    }

    public enum ResourceType {
        RESOURCE_ADAPTER(RESOURCE_ADAPTER_TEMPLATE), ADMIN_OBJECT(ADMIN_OBJECT_TEMPLATE), CONNECTION_DEFINITION(
                CONN_DEF_TEMPLATE);

        private final AddressTemplate template;

        ResourceType(AddressTemplate template) {
            this.template = template;
        }

        public AddressTemplate getTemplate() {
            return template;
        }
    }

    public String getParentName() {
        return parentName;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public boolean hasExtendedStats() {
        return has(STATISTICS) && get(STATISTICS).has(EXTENDED);
    }

    public ModelNode getExtendedStats() {
        return failSafeGet(this, EXTENDED_STATS);
    }

    public ModelNode getPoolStats() {
        return failSafeGet(this, POOL_STATS);
    }

    public boolean isStatisticsEnabled() {
        boolean isStatisticsEnabled = getExtendedStats().get(STATISTICS_ENABLED).asBoolean(false);
        if (resourceType == ResourceType.CONNECTION_DEFINITION) {
            isStatisticsEnabled = isStatisticsEnabled || getPoolStats().get(STATISTICS_ENABLED).asBoolean(false);
        }
        return isStatisticsEnabled;
    }
}
