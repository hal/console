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

import org.jboss.hal.meta.AddressTemplate;

interface AddressTemplates {

    String EXTENDED_STATISTICS = "/statistics=extended";
    String POOL_STATISTICS = "/statistics=pool";

    String RESOURCE_ADAPTER_SUBSYSTEM = "/subsystem=resource-adapters";
    String RESOURCE_ADAPTER_SUBSYSTEM_ADDRESS = "/{selected.host}/{selected.server}" + RESOURCE_ADAPTER_SUBSYSTEM;
    String RESOURCE_ADAPTER_ADDRESS = RESOURCE_ADAPTER_SUBSYSTEM_ADDRESS + "/resource-adapter=*";
    String RESOURCE_ADAPTER_CONFIGURATION_ADDRESS = RESOURCE_ADAPTER_SUBSYSTEM + "/resource-adapter=*";
    String CONNECTION_DEFINITION_ADDRESS = RESOURCE_ADAPTER_ADDRESS + "/connection-definitions=*";
    String ADMIN_OBJECT_ADDRESS = RESOURCE_ADAPTER_ADDRESS + "/admin-objects=*";

    String RESOURCE_ADAPTER_STATS_ADDRESS = AddressTemplate.OPTIONAL + "/" + RESOURCE_ADAPTER_ADDRESS + EXTENDED_STATISTICS;
    String ADMIN_OBJECT_STATS_ADDRESS = AddressTemplate.OPTIONAL + "/" + ADMIN_OBJECT_ADDRESS + EXTENDED_STATISTICS;
    String CONN_DEF_EXT_STATS_ADDRESS = AddressTemplate.OPTIONAL + "/" + CONNECTION_DEFINITION_ADDRESS + EXTENDED_STATISTICS;
    String CONN_DEF_POOL_STATS_ADDRESS = CONNECTION_DEFINITION_ADDRESS + POOL_STATISTICS;

    AddressTemplate RESOURCE_ADAPTER_SUBSYSTEM_TEMPLATE = AddressTemplate.of(RESOURCE_ADAPTER_SUBSYSTEM_ADDRESS);
    AddressTemplate RESOURCE_ADAPTER_TEMPLATE = AddressTemplate.of(RESOURCE_ADAPTER_ADDRESS);

    AddressTemplate ADMIN_OBJECT_TEMPLATE = AddressTemplate.of(ADMIN_OBJECT_ADDRESS);
    AddressTemplate CONN_DEF_TEMPLATE = AddressTemplate.of(CONNECTION_DEFINITION_ADDRESS);
}
