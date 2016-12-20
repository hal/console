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

import java.util.Map;

import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Callback;

import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SELECTED_SERVER_TEMPLATE;

/**
 * @author Harald Pehl
 */
enum ServerSubResource {

    ACCEPTOR(Ids.MESSAGING_ACCEPTOR, Names.GENERIC_ACCEPTOR,
            ModelDescriptionConstants.ACCEPTOR, AddressTemplates.ACCEPTOR_TEMPLATE),
    ADDRESS_SETTING(Ids.MESSAGING_ADDRESS_SETTING, Names.ADDRESS_SETTING,
            ModelDescriptionConstants.ADDRESS_SETTING, AddressTemplates.ADDRESS_SETTING_TEMPLATE),
    BRIDGE(Ids.MESSAGING_BRIDGE, Names.BRIDGE,
            ModelDescriptionConstants.BRIDGE, AddressTemplates.BRIDGE_TEMPLATE),
    BROADCAST_GROUP(Ids.MESSAGING_BROADCAST_GROUP, Names.BROADCAST_GROUP,
            ModelDescriptionConstants.BROADCAST_GROUP, AddressTemplates.BROADCAST_GROUP_TEMPLATE),
    CLUSTER_CONNECTION(Ids.MESSAGING_CLUSTER_CONNECTION, Names.CLUSTER_CONNECTION,
            ModelDescriptionConstants.CLUSTER_CONNECTION, AddressTemplates.CLUSTER_CONNECTION_TEMPLATE),
    CONNECTION_FACTORY(Ids.MESSAGING_CONNECTION_FACTORY, Names.CONNECTION_FACTORY,
            ModelDescriptionConstants.CONNECTION_FACTORY, AddressTemplates.CONNECTION_FACTORY_TEMPLATE),
    CONNECTOR(Ids.MESSAGING_CONNECTOR, Names.GENERIC_CONNECTOR,
            ModelDescriptionConstants.CONNECTOR, AddressTemplates.CONNECTOR_TEMPLATE),
    CONNECTOR_SERVICE(Ids.MESSAGING_CONNECTOR_SERVICE, Names.CONNECTOR_SERVICE,
            ModelDescriptionConstants.CONNECTOR_SERVICE, AddressTemplates.CONNECTOR_SERVICE_TEMPLATE),
    CORE_QUEUE(Ids.MESSAGING_CORE_QUEUE, Names.CORE_QUEUE,
            ModelDescriptionConstants.QUEUE, AddressTemplates.CORE_QUEUE_TEMPLATE),
    DISCOVERY_GROUP(Ids.MESSAGING_DISCOVERY_GROUP, Names.DISCOVERY_GROUP,
            ModelDescriptionConstants.DISCOVERY_GROUP, AddressTemplates.DISCOVERY_GROUP_TEMPLATE),
    DIVERT(Ids.MESSAGING_DIVERT, Names.DIVERT,
            ModelDescriptionConstants.DIVERT, AddressTemplates.DIVERT_TEMPLATE),
    GROUPING_HANDLER(Ids.MESSAGING_GROUPING_HANDLER, Names.GROUPING_HANDLER,
            ModelDescriptionConstants.GROUPING_HANDLER, AddressTemplates.GROUPING_HANDLER_TEMPLATE),
    HTTP_ACCEPTOR(Ids.MESSAGING_HTTP_ACCEPTOR, Names.HTTP_ACCEPTOR,
            ModelDescriptionConstants.HTTP_ACCEPTOR, AddressTemplates.HTTP_ACCEPTOR_TEMPLATE),
    HTTP_CONNECTOR(Ids.MESSAGING_HTTP_CONNECTOR, Names.HTTP_CONNECTOR,
            ModelDescriptionConstants.HTTP_CONNECTOR, AddressTemplates.HTTP_CONNECTOR_TEMPLATE),
    IN_VM_ACCEPTOR(Ids.MESSAGING_IN_VM_ACCEPTOR, Names.IN_VM_ACCEPTOR,
            ModelDescriptionConstants.IN_VM_ACCEPTOR, AddressTemplates.IN_VM_ACCEPTOR_TEMPLATE),
    IN_VM_CONNECTOR(Ids.MESSAGING_IN_VM_CONNECTOR, Names.IN_VM_CONNECTOR,
            ModelDescriptionConstants.IN_VM_CONNECTOR, AddressTemplates.IN_VM_CONNECTOR_TEMPLATE),
    JMS_QUEUE(Ids.MESSAGING_JMS_QUEUE, Names.JMS_QUEUE,
            ModelDescriptionConstants.JMS_QUEUE, AddressTemplates.JMS_QUEUE_TEMPLATE),
    JMS_TOPIC(Ids.MESSAGING_JMS_TOPIC, Names.JMS_TOPIC,
            ModelDescriptionConstants.JMS_TOPIC, AddressTemplates.JMS_TOPIC_TEMPLATE),
    POOLED_CONNECTION_FACTORY(Ids.MESSAGING_POOLED_CONNECTION_FACTORY, Names.POOLED_CONNECTION_FACTORY,
            ModelDescriptionConstants.POOLED_CONNECTION_FACTORY, AddressTemplates.POOLED_CONNECTION_FACTORY_TEMPLATE),
    REMOTE_ACCEPTOR(Ids.MESSAGING_REMOTE_ACCEPTOR, Names.REMOTE_ACCEPTOR,
            ModelDescriptionConstants.REMOTE_ACCEPTOR, AddressTemplates.REMOTE_ACCEPTOR_TEMPLATE),
    REMOTE_CONNECTOR(Ids.MESSAGING_REMOTE_CONNECTOR, Names.REMOTE_CONNECTOR,
            ModelDescriptionConstants.REMOTE_CONNECTOR, AddressTemplates.REMOTE_CONNECTOR_TEMPLATE),;

    final String baseId;
    final String type;
    final String resource;
    final AddressTemplate template;

    ServerSubResource(final String baseId, final String type, final String resource, final AddressTemplate template) {
        this.baseId = baseId;
        this.type = type;
        this.resource = resource;
        this.template = template;
    }

    void add(MetadataRegistry metadataRegistry, StatementContext statementContext,
            CrudOperations crud, CrudOperations.AddCallback callback) {
        Metadata metadata = metadataRegistry.lookup(template);
        new AddResourceDialog(Ids.build(baseId, Ids.ADD_SUFFIX), type, metadata, (name, model) -> {
            ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(resource + "=" + name)
                    .resolve(statementContext);
            crud.add(type, name, address, model, callback);
        }).show();
    }

    void save(Form<NamedNode> form, Map<String, Object> changedValues, StatementContext statementContext,
            CrudOperations crud, Callback callback) {
        String name = form.getModel().getName();
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(resource + "=" + name).resolve(statementContext);
        crud.save(type, name, address, changedValues, callback);
    }

    void remove(NamedNode item, StatementContext statementContext, CrudOperations crud, Callback callback) {
        String name = item.getName();
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(type + "=" + name).resolve(statementContext);
        crud.remove(type, name, address, callback);
    }
}
