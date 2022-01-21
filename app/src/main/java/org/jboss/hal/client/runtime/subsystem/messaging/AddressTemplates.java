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
package org.jboss.hal.client.runtime.subsystem.messaging;

import org.jboss.hal.meta.AddressTemplate;

import static org.jboss.hal.core.deployment.DeploymentResources.DEPLOYMENT_ADDRESS;
import static org.jboss.hal.core.deployment.DeploymentResources.SUBDEPLOYMENT_ADDRESS;

public interface AddressTemplates {

    String MESSAGING_ADDRESS = "/subsystem=messaging-activemq";
    String SERVER_ADDRESS = "/server=*";
    String JMS_BRIDGE_ADDRESS = "/jms-bridge=*";

    String SELECTED_HOST_SELECTED_SERVER_ADDRESS = "/{selected.host}/{selected.server}";
    String MESSAGING_SUBSYSTEM_ADDRESS = SELECTED_HOST_SELECTED_SERVER_ADDRESS + MESSAGING_ADDRESS;
    String MESSAGING_SERVER_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS + SERVER_ADDRESS;
    String MESSAGING_CORE_QUEUE_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS + SERVER_ADDRESS + "/queue=*";
    String MESSAGING_JMS_QUEUE_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS + SERVER_ADDRESS + "/jms-queue=*";
    String MESSAGING_JMS_TOPIC_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS + SERVER_ADDRESS + "/jms-topic=*";

    String MESSAGING_DEPLOYMENT_ADDRESS = DEPLOYMENT_ADDRESS + MESSAGING_ADDRESS + SERVER_ADDRESS;
    String MESSAGING_DEPLOYMENT_JMS_QUEUE_ADDRESS = MESSAGING_DEPLOYMENT_ADDRESS + "/jms-queue=*";
    String MESSAGING_DEPLOYMENT_JMS_TOPIC_ADDRESS = MESSAGING_DEPLOYMENT_ADDRESS + "/jms-topic=*";

    String MESSAGING_SUBDEPLOYMENT_ADDRESS = SUBDEPLOYMENT_ADDRESS + MESSAGING_ADDRESS + SERVER_ADDRESS;

    String MESSAGING_JMS_BRIDGE_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS + JMS_BRIDGE_ADDRESS;

    AddressTemplate SELECTED_HOST_SELECTED_SERVER_TEMPLATE = AddressTemplate.of(SELECTED_HOST_SELECTED_SERVER_ADDRESS);
    AddressTemplate MESSAGING_SUBSYSTEM_TEMPLATE = AddressTemplate.of(MESSAGING_SUBSYSTEM_ADDRESS);
    AddressTemplate MESSAGING_SERVER_TEMPLATE = AddressTemplate.of(MESSAGING_SERVER_ADDRESS);
    AddressTemplate MESSAGING_CORE_QUEUE_TEMPLATE = AddressTemplate.of(MESSAGING_CORE_QUEUE_ADDRESS);
    AddressTemplate MESSAGING_DEPLOYMENT_TEMPLATE = AddressTemplate.of(MESSAGING_DEPLOYMENT_ADDRESS);
    AddressTemplate MESSAGING_SUBDEPLOYMENT_TEMPLATE = AddressTemplate.of(MESSAGING_SUBDEPLOYMENT_ADDRESS);
    AddressTemplate MESSAGING_JMS_BRIDGE_TEMPLATE = AddressTemplate.of(MESSAGING_JMS_BRIDGE_ADDRESS);
}
