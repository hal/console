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
package org.jboss.hal.client.configuration.subsystem.deploymentscanner;

import org.jboss.hal.meta.AddressTemplate;

interface AddressTemplates {

    String DEPLOYMENTSCANNER_SUBSYSTEM_ADDRESS = "/{selected.profile}/subsystem=deployment-scanner";
    String DEPLOYMENTSCANNER_ADDRESS = DEPLOYMENTSCANNER_SUBSYSTEM_ADDRESS + "/scanner=*";

    AddressTemplate DEPLOYMENTSCANNER_SUBSYSTEM_TEMPLATE = AddressTemplate.of(DEPLOYMENTSCANNER_SUBSYSTEM_ADDRESS);
    AddressTemplate DEPLOYMENTSCANNER_TEMPLATE = AddressTemplate.of(DEPLOYMENTSCANNER_ADDRESS);
}
