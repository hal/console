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
package org.jboss.hal.client.configuration.subsystem.messaging;

import org.jboss.hal.meta.AddressTemplate;

import static org.jboss.hal.meta.SelectionAwareStatementContext.SELECTION_EXPRESSION;

/**
 * @author Harald Pehl
 */
interface AddressTemplates {

    String MESSAGING_SUBSYSTEM_ADDRESS = "/{selected.profile}/subsystem=messaging-activemq";

    String SERVER_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS + "/server=*";
    String SELECTED_SERVER_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS + "/server=" + SELECTION_EXPRESSION;

    String CORE_QUEUE_ADDRESS = SERVER_ADDRESS + "/queue=*";
    String JMS_QUEUE_ADDRESS = SERVER_ADDRESS + "/jms-queue=*";
    String JMS_TOPIC_ADDRESS = SERVER_ADDRESS + "/jms-topic=*";
    String SECURITY_SETTING_ADDRESS = SERVER_ADDRESS + "/security-setting=*";
    String ROLE_ADDRESS = SECURITY_SETTING_ADDRESS + "/role=*";
    String ADDRESS_SETTING_ADDRESS = SERVER_ADDRESS + "/address-setting=*";
    String DIVERT_ADDRESS = SERVER_ADDRESS + "/divert=*";

    String JMS_BRIDGE_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS + "/jms-bridge=*";
    String SELECTED_JMS_BRIDGE_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS + "/jms-bridge=" + SELECTION_EXPRESSION;

    AddressTemplate MESSAGING_SUBSYSTEM_TEMPLATE = AddressTemplate.of(MESSAGING_SUBSYSTEM_ADDRESS);

    AddressTemplate SERVER_TEMPLATE = AddressTemplate.of(SERVER_ADDRESS);
    AddressTemplate SELECTED_SERVER_TEMPLATE = AddressTemplate.of(SELECTED_SERVER_ADDRESS);

    AddressTemplate CORE_QUEUE_TEMPLATE = AddressTemplate.of(CORE_QUEUE_ADDRESS);
    AddressTemplate JMS_QUEUE_TEMPLATE = AddressTemplate.of(JMS_QUEUE_ADDRESS);
    AddressTemplate JMS_TOPIC_TEMPLATE = AddressTemplate.of(JMS_TOPIC_ADDRESS);
    AddressTemplate SECURITY_SETTING_TEMPLATE = AddressTemplate.of(SECURITY_SETTING_ADDRESS);
    AddressTemplate ROLE_TEMPLATE = AddressTemplate.of(ROLE_ADDRESS);
    AddressTemplate ADDRESS_SETTING_TEMPLATE = AddressTemplate.of(ADDRESS_SETTING_ADDRESS);
    AddressTemplate DIVERT_TEMPLATE = AddressTemplate.of(DIVERT_ADDRESS);

    AddressTemplate JMS_BRIDGE_TEMPLATE = AddressTemplate.of(JMS_BRIDGE_ADDRESS);
    AddressTemplate SELECTED_JMS_BRIDGE_TEMPLATE = AddressTemplate.of(SELECTED_JMS_BRIDGE_ADDRESS);
}
