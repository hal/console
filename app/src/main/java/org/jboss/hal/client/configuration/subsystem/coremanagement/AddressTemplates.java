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
package org.jboss.hal.client.configuration.subsystem.coremanagement;

import org.jboss.hal.meta.AddressTemplate;

interface AddressTemplates {

    String CORE_MANAGEMENT_SUBSYSTEM_ADDRESS = "/{selected.profile}/subsystem=core-management";
    String CONFIGURATION_CHANGES_ADDRESS = CORE_MANAGEMENT_SUBSYSTEM_ADDRESS + "/service=configuration-changes";
    String PROCESS_STATE_LISTENER_ADDRESS = CORE_MANAGEMENT_SUBSYSTEM_ADDRESS + "/process-state-listener=*";

    AddressTemplate CORE_MANAGEMENT_SUBSYSTEM_TEMPLATE = AddressTemplate.of(CORE_MANAGEMENT_SUBSYSTEM_ADDRESS);
    AddressTemplate CONFIGURATION_CHANGES_TEMPLATE = AddressTemplate.of(CONFIGURATION_CHANGES_ADDRESS);
    AddressTemplate PROCESS_STATE_LISTENER_TEMPLATE = AddressTemplate.of(PROCESS_STATE_LISTENER_ADDRESS);
}
