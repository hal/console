/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.resourceadapter;

import org.jboss.hal.meta.AddressTemplate;

import static org.jboss.hal.meta.SelectionAwareStatementContext.SELECTION_EXPRESSION;

interface AddressTemplates {

    String RESOURCE_ADAPTER_SUBSYSTEM_ADDRESS = "/{selected.profile}/subsystem=resource-adapters";
    String RESOURCE_ADAPTER_ADDRESS = RESOURCE_ADAPTER_SUBSYSTEM_ADDRESS + "/resource-adapter=*";
    String SELECTED_RESOURCE_ADAPTER_ADDRESS = RESOURCE_ADAPTER_SUBSYSTEM_ADDRESS + "/resource-adapter=" + SELECTION_EXPRESSION;

    String CONNECTION_DEFINITIONS_ADDRESS = RESOURCE_ADAPTER_ADDRESS + "/connection-definitions=*";
    String SELECTED_CONNECTION_DEFINITIONS_ADDRESS = SELECTED_RESOURCE_ADAPTER_ADDRESS + "/connection-definitions=*";

    String ADMIN_OBJECTS_ADDRESS = RESOURCE_ADAPTER_ADDRESS + "/admin-objects=*";
    String SELECTED_ADMIN_OBJECTS_ADDRESS = SELECTED_RESOURCE_ADAPTER_ADDRESS + "/admin-objects=*";

    AddressTemplate RESOURCE_ADAPTER_SUBSYSTEM_TEMPLATE = AddressTemplate.of(RESOURCE_ADAPTER_SUBSYSTEM_ADDRESS);
    AddressTemplate RESOURCE_ADAPTER_TEMPLATE = AddressTemplate.of(RESOURCE_ADAPTER_ADDRESS);
    AddressTemplate SELECTED_RESOURCE_ADAPTER_TEMPLATE = AddressTemplate.of(SELECTED_RESOURCE_ADAPTER_ADDRESS);
    AddressTemplate CONNECTION_DEFINITIONS_TEMPLATE = AddressTemplate.of(CONNECTION_DEFINITIONS_ADDRESS);
    AddressTemplate SELECTED_CONNECTION_DEFINITIONS_TEMPLATE = AddressTemplate
            .of(SELECTED_CONNECTION_DEFINITIONS_ADDRESS);
    AddressTemplate ADMIN_OBJECTS_TEMPLATE = AddressTemplate.of(ADMIN_OBJECTS_ADDRESS);
    AddressTemplate SELECTED_ADMIN_OBJECTS_TEMPLATE = AddressTemplate.of(SELECTED_ADMIN_OBJECTS_ADDRESS);
}
