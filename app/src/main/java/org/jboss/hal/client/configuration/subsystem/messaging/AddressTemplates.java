/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

interface AddressTemplates {

    String MESSAGING_SUBSYSTEM_ADDRESS = "/{selected.profile}/subsystem=messaging-activemq";
    AddressTemplate MESSAGING_SUBSYSTEM_TEMPLATE = AddressTemplate.of(MESSAGING_SUBSYSTEM_ADDRESS);
    AddressTemplate SOCKET_BINDING_TEMPLATE = AddressTemplate.of("/socket-binding-group=*/socket-binding=*");

    String CONNECTOR_REMOTE_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS + "/connector=*";
    String IN_VM_CONNECTOR_REMOTE_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS + "/in-vm-connector=*";
    String HTTP_CONNECTOR_REMOTE_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS + "/http-connector=*";
    String REMOTE_CONNECTOR_REMOTE_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS + "/remote-connector=*";
    String DISCOVERY_GROUP_REMOTE_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS + "/discovery-group=*";
    String CONNECTION_FACTORY_REMOTE_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS + "/connection-factory=*";
    String POOLED_CONNECTION_FACTORY_REMOTE_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS + "/pooled-connection-factory=*";
    String EXTERNAL_JMS_QUEUE_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS + "/external-jms-queue=*";
    String EXTERNAL_JMS_TOPIC_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS + "/external-jms-topic=*";
    AddressTemplate CONNECTOR_REMOTE_TEMPLATE = AddressTemplate.of(CONNECTOR_REMOTE_ADDRESS);
    AddressTemplate IN_VM_CONNECTOR_REMOTE_TEMPLATE = AddressTemplate.of(IN_VM_CONNECTOR_REMOTE_ADDRESS);
    AddressTemplate HTTP_CONNECTOR_REMOTE_TEMPLATE = AddressTemplate.of(HTTP_CONNECTOR_REMOTE_ADDRESS);
    AddressTemplate REMOTE_CONNECTOR_REMOTE_TEMPLATE = AddressTemplate.of(REMOTE_CONNECTOR_REMOTE_ADDRESS);
    AddressTemplate DISCOVERY_GROUP_REMOTE_TEMPLATE = AddressTemplate.of(DISCOVERY_GROUP_REMOTE_ADDRESS);
    AddressTemplate CONNECTION_FACTORY_REMOTE_TEMPLATE = AddressTemplate.of(CONNECTION_FACTORY_REMOTE_ADDRESS);
    AddressTemplate POOLED_CONNECTION_FACTORY_REMOTE_TEMPLATE = AddressTemplate.of(POOLED_CONNECTION_FACTORY_REMOTE_ADDRESS);
    AddressTemplate EXTERNAL_JMS_QUEUE_TEMPLATE = AddressTemplate.of(EXTERNAL_JMS_QUEUE_ADDRESS);
    AddressTemplate EXTERNAL_JMS_TOPIC_TEMPLATE = AddressTemplate.of(EXTERNAL_JMS_TOPIC_ADDRESS);

    String SERVER_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS + "/server=*";
    String SELECTED_SERVER_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS + "/server=" + SELECTION_EXPRESSION;
    AddressTemplate SERVER_TEMPLATE = AddressTemplate.of(SERVER_ADDRESS);
    AddressTemplate SELECTED_SERVER_TEMPLATE = AddressTemplate.of(SELECTED_SERVER_ADDRESS);

    // journal directory
    String BINDING_DIRECTORY_ADDRESS = SERVER_ADDRESS + "/path=bindings-directory";
    String PAGING_DIRECTORY_ADDRESS = SERVER_ADDRESS + "/path=paging-directory";
    String LARGE_MESSAGES_DIRECTORY_ADDRESS = SERVER_ADDRESS + "/path=large-messages-directory";
    String JOURNAL_DIRECTORY_ADDRESS = SERVER_ADDRESS + "/path=journal-directory";
    AddressTemplate BINDING_DIRECTORY_TEMPLATE = AddressTemplate.of(BINDING_DIRECTORY_ADDRESS);
    AddressTemplate PAGING_DIRECTORY_TEMPLATE = AddressTemplate.of(PAGING_DIRECTORY_ADDRESS);
    AddressTemplate LARGE_MESSAGES_DIRECTORY_TEMPLATE = AddressTemplate.of(LARGE_MESSAGES_DIRECTORY_ADDRESS);
    AddressTemplate JOURNAL_DIRECTORY_TEMPLATE = AddressTemplate.of(JOURNAL_DIRECTORY_ADDRESS);

    AddressTemplate SELECTED_BINDING_DIRECTORY_TEMPLATE = AddressTemplate.of(SELECTED_SERVER_ADDRESS + "/path=bindings-directory");
    AddressTemplate SELECTED_PAGING_DIRECTORY_TEMPLATE = AddressTemplate.of(SELECTED_SERVER_ADDRESS + "/path=paging-directory");
    AddressTemplate SELECTED_LARGE_MESSAGES_DIRECTORY_TEMPLATE = AddressTemplate.of(SELECTED_SERVER_ADDRESS + "/path=large-messages-directory");
    AddressTemplate SELECTED_JOURNAL_DIRECTORY_TEMPLATE = AddressTemplate.of(SELECTED_SERVER_ADDRESS + "/path=journal-directory");

    // destinations
    String CORE_QUEUE_ADDRESS = SERVER_ADDRESS + "/queue=*";
    String JMS_QUEUE_ADDRESS = SERVER_ADDRESS + "/jms-queue=*";
    String JMS_TOPIC_ADDRESS = SERVER_ADDRESS + "/jms-topic=*";
    String SECURITY_SETTING_ADDRESS = SERVER_ADDRESS + "/security-setting=*";
    String ROLE_ADDRESS = SECURITY_SETTING_ADDRESS + "/role=*";
    String ADDRESS_SETTING_ADDRESS = SERVER_ADDRESS + "/address-setting=*";
    String DIVERT_ADDRESS = SERVER_ADDRESS + "/divert=*";
    AddressTemplate CORE_QUEUE_TEMPLATE = AddressTemplate.of(CORE_QUEUE_ADDRESS);
    AddressTemplate JMS_QUEUE_TEMPLATE = AddressTemplate.of(JMS_QUEUE_ADDRESS);
    AddressTemplate JMS_TOPIC_TEMPLATE = AddressTemplate.of(JMS_TOPIC_ADDRESS);
    AddressTemplate ROLE_TEMPLATE = AddressTemplate.of(ROLE_ADDRESS);
    AddressTemplate ADDRESS_SETTING_TEMPLATE = AddressTemplate.of(ADDRESS_SETTING_ADDRESS);
    AddressTemplate DIVERT_TEMPLATE = AddressTemplate.of(DIVERT_ADDRESS);

    //connections
    String ACCEPTOR_ADDRESS = SERVER_ADDRESS + "/acceptor=*";
    String IN_VM_ACCEPTOR_ADDRESS = SERVER_ADDRESS + "/in-vm-acceptor=*";
    String HTTP_ACCEPTOR_ADDRESS = SERVER_ADDRESS + "/http-acceptor=*";
    String REMOTE_ACCEPTOR_ADDRESS = SERVER_ADDRESS + "/remote-acceptor=*";
    String CONNECTOR_ADDRESS = SERVER_ADDRESS + "/connector=*";
    String IN_VM_CONNECTOR_ADDRESS = SERVER_ADDRESS + "/in-vm-connector=*";
    String HTTP_CONNECTOR_ADDRESS = SERVER_ADDRESS + "/http-connector=*";
    String REMOTE_CONNECTOR_ADDRESS = SERVER_ADDRESS + "/remote-connector=*";
    String CONNECTOR_SERVICE_ADDRESS = SERVER_ADDRESS + "/connector-service=*";
    String CONNECTION_FACTORY_ADDRESS = SERVER_ADDRESS + "/connection-factory=*";
    String POOLED_CONNECTION_FACTORY_ADDRESS = SERVER_ADDRESS + "/pooled-connection-factory=*";
    AddressTemplate ACCEPTOR_TEMPLATE = AddressTemplate.of(ACCEPTOR_ADDRESS);
    AddressTemplate IN_VM_ACCEPTOR_TEMPLATE = AddressTemplate.of(IN_VM_ACCEPTOR_ADDRESS);
    AddressTemplate HTTP_ACCEPTOR_TEMPLATE = AddressTemplate.of(HTTP_ACCEPTOR_ADDRESS);
    AddressTemplate REMOTE_ACCEPTOR_TEMPLATE = AddressTemplate.of(REMOTE_ACCEPTOR_ADDRESS);
    AddressTemplate CONNECTOR_TEMPLATE = AddressTemplate.of(CONNECTOR_ADDRESS);
    AddressTemplate IN_VM_CONNECTOR_TEMPLATE = AddressTemplate.of(IN_VM_CONNECTOR_ADDRESS);
    AddressTemplate HTTP_CONNECTOR_TEMPLATE = AddressTemplate.of(HTTP_CONNECTOR_ADDRESS);
    AddressTemplate REMOTE_CONNECTOR_TEMPLATE = AddressTemplate.of(REMOTE_CONNECTOR_ADDRESS);
    AddressTemplate CONNECTOR_SERVICE_TEMPLATE = AddressTemplate.of(CONNECTOR_SERVICE_ADDRESS);
    AddressTemplate CONNECTION_FACTORY_TEMPLATE = AddressTemplate.of(CONNECTION_FACTORY_ADDRESS);
    AddressTemplate POOLED_CONNECTION_FACTORY_TEMPLATE = AddressTemplate.of(POOLED_CONNECTION_FACTORY_ADDRESS);
    AddressTemplate SELECTED_POOLED_CONNECTION_FACTORY_TEMPLATE = AddressTemplate.of(SELECTED_SERVER_ADDRESS)
            .append("pooled-connection-factory=*");

    // clustering
    String BROADCAST_GROUP_ADDRESS = SERVER_ADDRESS + "/broadcast-group=*";
    String DISCOVERY_GROUP_ADDRESS = SERVER_ADDRESS + "/discovery-group=*";
    String CLUSTER_CONNECTION_ADDRESS = SERVER_ADDRESS + "/cluster-connection=*";
    String GROUPING_HANDLER_ADDRESS = SERVER_ADDRESS + "/grouping-handler=*";
    String BRIDGE_ADDRESS = SERVER_ADDRESS + "/bridge=*";
    AddressTemplate BROADCAST_GROUP_TEMPLATE = AddressTemplate.of(BROADCAST_GROUP_ADDRESS);
    AddressTemplate DISCOVERY_GROUP_TEMPLATE = AddressTemplate.of(DISCOVERY_GROUP_ADDRESS);
    AddressTemplate CLUSTER_CONNECTION_TEMPLATE = AddressTemplate.of(CLUSTER_CONNECTION_ADDRESS);
    AddressTemplate GROUPING_HANDLER_TEMPLATE = AddressTemplate.of(GROUPING_HANDLER_ADDRESS);
    AddressTemplate BRIDGE_TEMPLATE = AddressTemplate.of(BRIDGE_ADDRESS);
    AddressTemplate SELECTED_BRIDGE_TEMPLATE = AddressTemplate.of(SELECTED_SERVER_ADDRESS).append("bridge=*");

    // ha-* resources
    String LIVE_ONLY_ADDRESS = SERVER_ADDRESS + "/ha-policy=live-only";
    String REPLICATION_COLOCATED_ADDRESS = SERVER_ADDRESS + "/ha-policy=replication-colocated";
    String REPLICATION_COLOCATED_MASTER_ADDRESS = REPLICATION_COLOCATED_ADDRESS + "/configuration=master";
    String REPLICATION_COLOCATED_SLAVE_ADDRESS = REPLICATION_COLOCATED_ADDRESS + "/configuration=slave";
    String REPLICATION_MASTER_ADDRESS = SERVER_ADDRESS + "/ha-policy=replication-master";
    String REPLICATION_SLAVE_ADDRESS = SERVER_ADDRESS + "/ha-policy=replication-slave";
    String SHARED_STORE_COLOCATED_ADDRESS = SERVER_ADDRESS + "/ha-policy=shared-store-colocated";
    String SHARED_STORE_COLOCATED_MASTER_ADDRESS = SHARED_STORE_COLOCATED_ADDRESS + "/configuration=master";
    String SHARED_STORE_COLOCATED_SLAVE_ADDRESS = SHARED_STORE_COLOCATED_ADDRESS + "/configuration=slave";
    String SHARED_STORE_MASTER_ADDRESS = SERVER_ADDRESS + "/ha-policy=shared-store-master";
    String SHARED_STORE_SLAVE_ADDRESS = SERVER_ADDRESS + "/ha-policy=shared-store-slave";
    AddressTemplate LIVE_ONLY_TEMPLATE = AddressTemplate.of(LIVE_ONLY_ADDRESS);
    AddressTemplate REPLICATION_COLOCATED_TEMPLATE = AddressTemplate.of(REPLICATION_COLOCATED_ADDRESS);
    AddressTemplate REPLICATION_COLOCATED_MASTER_TEMPLATE = AddressTemplate.of(REPLICATION_COLOCATED_MASTER_ADDRESS);
    AddressTemplate REPLICATION_COLOCATED_SLAVE_TEMPLATE = AddressTemplate.of(REPLICATION_COLOCATED_SLAVE_ADDRESS);
    AddressTemplate REPLICATION_MASTER_TEMPLATE = AddressTemplate.of(REPLICATION_MASTER_ADDRESS);
    AddressTemplate REPLICATION_SLAVE_TEMPLATE = AddressTemplate.of(REPLICATION_SLAVE_ADDRESS);
    AddressTemplate SHARED_STORE_COLOCATED_TEMPLATE = AddressTemplate.of(SHARED_STORE_COLOCATED_ADDRESS);
    AddressTemplate SHARED_STORE_COLOCATED_MASTER_TEMPLATE = AddressTemplate.of(SHARED_STORE_COLOCATED_MASTER_ADDRESS);
    AddressTemplate SHARED_STORE_COLOCATED_SLAVE_TEMPLATE = AddressTemplate.of(SHARED_STORE_COLOCATED_SLAVE_ADDRESS);
    AddressTemplate SHARED_STORE_MASTER_TEMPLATE = AddressTemplate.of(SHARED_STORE_MASTER_ADDRESS);
    AddressTemplate SHARED_STORE_SLAVE_TEMPLATE = AddressTemplate.of(SHARED_STORE_SLAVE_ADDRESS);

    // jms-bridge
    String JMS_BRIDGE_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS + "/jms-bridge=*";
    String SELECTED_JMS_BRIDGE_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS + "/jms-bridge=" + SELECTION_EXPRESSION;
    AddressTemplate JMS_BRIDGE_TEMPLATE = AddressTemplate.of(JMS_BRIDGE_ADDRESS);
    AddressTemplate SELECTED_JMS_BRIDGE_TEMPLATE = AddressTemplate.of(SELECTED_JMS_BRIDGE_ADDRESS);
}
