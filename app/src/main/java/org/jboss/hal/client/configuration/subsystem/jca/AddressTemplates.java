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
package org.jboss.hal.client.configuration.subsystem.jca;

import org.jboss.hal.meta.AddressTemplate;

interface AddressTemplates {

    String JCA_ADDRESS = "/{selected.profile}/subsystem=jca";
    String ARCHIVE_VALIDATION_ADDRESS = JCA_ADDRESS + "/archive-validation=archive-validation";
    String BEAN_VALIDATION_ADDRESS = JCA_ADDRESS + "/bean-validation=bean-validation";
    String BOOTSTRAP_CONTEXT_ADDRESS = JCA_ADDRESS + "/bootstrap-context=*";
    String CCM_ADDRESS = JCA_ADDRESS + "/cached-connection-manager=cached-connection-manager";
    String DISTRIBUTED_WORKMANAGER_ADDRESS = JCA_ADDRESS + "/distributed-workmanager=*";
    String DISTRIBUTED_WORKMANAGER_LRT_ADDRESS = DISTRIBUTED_WORKMANAGER_ADDRESS + "/long-running-threads=*";
    String DISTRIBUTED_WORKMANAGER_SRT_ADDRESS = DISTRIBUTED_WORKMANAGER_ADDRESS + "/short-running-threads=*";
    String TRACER_ADDRESS = JCA_ADDRESS + "/tracer=tracer";
    String WORKMANAGER_ADDRESS = JCA_ADDRESS + "/workmanager=*";
    String WORKMANAGER_LRT_ADDRESS = WORKMANAGER_ADDRESS + "/long-running-threads=*";
    String WORKMANAGER_SRT_ADDRESS = WORKMANAGER_ADDRESS + "/short-running-threads=*";

    AddressTemplate JCA_TEMPLATE = AddressTemplate.of(JCA_ADDRESS);
    AddressTemplate ARCHIVE_VALIDATION_TEMPLATE = AddressTemplate.of(ARCHIVE_VALIDATION_ADDRESS);
    AddressTemplate BEAN_VALIDATION_TEMPLATE = AddressTemplate.of(BEAN_VALIDATION_ADDRESS);
    AddressTemplate BOOTSTRAP_CONTEXT_TEMPLATE = AddressTemplate.of(BOOTSTRAP_CONTEXT_ADDRESS);
    AddressTemplate CCM_TEMPLATE = AddressTemplate.of(CCM_ADDRESS);
    AddressTemplate DISTRIBUTED_WORKMANAGER_TEMPLATE = AddressTemplate.of(DISTRIBUTED_WORKMANAGER_ADDRESS);
    AddressTemplate DISTRIBUTED_WORKMANAGER_LRT_TEMPLATE = AddressTemplate.of(DISTRIBUTED_WORKMANAGER_LRT_ADDRESS);
    AddressTemplate DISTRIBUTED_WORKMANAGER_SRT_TEMPLATE = AddressTemplate.of(DISTRIBUTED_WORKMANAGER_SRT_ADDRESS);
    AddressTemplate TRACER_TEMPLATE = AddressTemplate.of(TRACER_ADDRESS);
    AddressTemplate WORKMANAGER_TEMPLATE = AddressTemplate.of(WORKMANAGER_ADDRESS);
    AddressTemplate WORKMANAGER_LRT_TEMPLATE = AddressTemplate.of(WORKMANAGER_LRT_ADDRESS);
    AddressTemplate WORKMANAGER_SRT_TEMPLATE = AddressTemplate.of(WORKMANAGER_SRT_ADDRESS);
}
