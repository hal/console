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
package org.jboss.hal.client.configuration.subsystem.messaging;

import java.util.Map;

import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;

import static org.jboss.hal.dmr.ModelDescriptionConstants.EXTERNAL_JMS_QUEUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXTERNAL_JMS_TOPIC;

enum RemoteActiveMQSubResource {

    CONNECTION_FACTORY(Ids.MESSAGING_CONNECTION_FACTORY, Names.CONNECTION_FACTORY,
            ModelDescriptionConstants.CONNECTION_FACTORY, AddressTemplates.CONNECTION_FACTORY_REMOTE_TEMPLATE),

    CONNECTOR(Ids.MESSAGING_CONNECTOR, Names.GENERIC_CONNECTOR,
            ModelDescriptionConstants.CONNECTOR, AddressTemplates.CONNECTOR_REMOTE_TEMPLATE),

    DISCOVERY_GROUP(Ids.MESSAGING_DISCOVERY_GROUP, Names.DISCOVERY_GROUP,
            ModelDescriptionConstants.DISCOVERY_GROUP, AddressTemplates.DISCOVERY_GROUP_REMOTE_TEMPLATE),

    EXTERNAL_QUEUE(EXTERNAL_JMS_QUEUE, Names.EXTERNAL_JMS_QUEUE,
            EXTERNAL_JMS_QUEUE, AddressTemplates.EXTERNAL_JMS_QUEUE_TEMPLATE),

    EXTERNAL_TOPIC(EXTERNAL_JMS_TOPIC, Names.EXTERNAL_JMS_TOPIC,
            EXTERNAL_JMS_TOPIC, AddressTemplates.EXTERNAL_JMS_TOPIC_TEMPLATE),

    HTTP_CONNECTOR(Ids.MESSAGING_HTTP_CONNECTOR, Names.HTTP_CONNECTOR,
            ModelDescriptionConstants.HTTP_CONNECTOR, AddressTemplates.HTTP_CONNECTOR_REMOTE_TEMPLATE),

    IN_VM_CONNECTOR(Ids.MESSAGING_IN_VM_CONNECTOR, Names.IN_VM_CONNECTOR,
            ModelDescriptionConstants.IN_VM_CONNECTOR, AddressTemplates.IN_VM_CONNECTOR_REMOTE_TEMPLATE),

    POOLED_CONNECTION_FACTORY(Ids.MESSAGING_POOLED_CONNECTION_FACTORY, Names.POOLED_CONNECTION_FACTORY,
            ModelDescriptionConstants.POOLED_CONNECTION_FACTORY, AddressTemplates.POOLED_CONNECTION_FACTORY_REMOTE_TEMPLATE),

    REMOTE_CONNECTOR(Ids.MESSAGING_REMOTE_CONNECTOR, Names.REMOTE_CONNECTOR,
            ModelDescriptionConstants.REMOTE_CONNECTOR, AddressTemplates.REMOTE_CONNECTOR_REMOTE_TEMPLATE);

    final String baseId;
    final String type;
    final String resource;
    final AddressTemplate template;

    RemoteActiveMQSubResource(final String baseId, final String type, final String resource, final AddressTemplate template) {
        this.baseId = baseId;
        this.type = type;
        this.resource = resource;
        this.template = template;
    }

    void add(MetadataRegistry metadataRegistry, StatementContext statementContext,
            CrudOperations crud, Resources resources, CrudOperations.AddCallback callback) {
        Metadata metadata = metadataRegistry.lookup(template);
        new AddResourceDialog(Ids.build(baseId, Ids.ADD), resources.messages().addResourceTitle(type), metadata,
                (name, model) -> {
                    ResourceAddress address = template.resolve(statementContext, name);
                    crud.add(type, name, address, model, callback);
                }).show();
    }

    void save(Form<NamedNode> form, Map<String, Object> changedValues, MetadataRegistry metadataRegistry,
            StatementContext statementContext, CrudOperations crud, Callback callback) {
        String name = form.getModel().getName();
        ResourceAddress address = template.resolve(statementContext, name);
        Metadata metadata = metadataRegistry.lookup(template);
        crud.save(type, name, address, changedValues, metadata, callback);
    }

    void reset(Form<NamedNode> form, MetadataRegistry metadataRegistry, StatementContext statementContext,
            CrudOperations crud, Callback callback) {
        String name = form.getModel().getName();
        ResourceAddress address = template.resolve(statementContext, name);
        Metadata metadata = metadataRegistry.lookup(template);
        crud.reset(type, name, address, form, metadata, callback);
    }

    void remove(NamedNode item, StatementContext statementContext, CrudOperations crud, Callback callback) {
        String name = item.getName();
        ResourceAddress address = template.resolve(statementContext, name);
        crud.remove(type, name, address, callback);
    }
}
