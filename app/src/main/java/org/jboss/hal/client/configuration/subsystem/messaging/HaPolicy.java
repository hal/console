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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SELECTED_SERVER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public enum HaPolicy {

    LIVE_ONLY(Ids.MESSAGING_HA_REPLICATION_LIVE_ONLY,
            Names.LIVE_ONLY,
            ModelDescriptionConstants.LIVE_ONLY,
            AddressTemplates.LIVE_ONLY_TEMPLATE),

    REPLICATION_COLOCATED_MASTER(Ids.MESSAGING_HA_REPLICATION_COLOCATED_MASTER,
            Names.REPLICATION_COLOCATED + Constants.SLASH + Names.MASTER,
            ModelDescriptionConstants.REPLICATION_COLOCATED + "/" + CONFIGURATION + Constants.EQUALS + MASTER,
            AddressTemplates.REPLICATION_COLOCATED_MASTER_TEMPLATE),

    REPLICATION_COLOCATED_SLAVE(Ids.MESSAGING_HA_REPLICATION_COLOCATED_SLAVE,
            Names.REPLICATION_COLOCATED + Constants.SLASH + Names.SLAVE,
            ModelDescriptionConstants.REPLICATION_COLOCATED + "/" + CONFIGURATION + Constants.EQUALS + SLAVE,
            AddressTemplates.REPLICATION_COLOCATED_SLAVE_TEMPLATE),

    REPLICATION_COLOCATED(Ids.MESSAGING_HA_REPLICATION_COLOCATED,
            Names.REPLICATION_COLOCATED,
            ModelDescriptionConstants.REPLICATION_COLOCATED,
            AddressTemplates.REPLICATION_COLOCATED_TEMPLATE,
            REPLICATION_COLOCATED_MASTER, REPLICATION_COLOCATED_SLAVE),

    REPLICATION_MASTER(Ids.MESSAGING_HA_REPLICATION_MASTER,
            Names.REPLICATION_MASTER,
            ModelDescriptionConstants.REPLICATION_MASTER,
            AddressTemplates.REPLICATION_MASTER_TEMPLATE),

    REPLICATION_SLAVE(Ids.MESSAGING_HA_REPLICATION_SLAVE,
            Names.REPLICATION_SLAVE,
            ModelDescriptionConstants.REPLICATION_SLAVE,
            AddressTemplates.REPLICATION_SLAVE_TEMPLATE),

    SHARED_STORE_COLOCATED_MASTER(Ids.MESSAGING_HA_SHARED_STORE_COLOCATED_MASTER,
            Names.SHARED_STORE_COLOCATED + Constants.SLASH + Names.MASTER,
            ModelDescriptionConstants.SHARED_STORE_COLOCATED + "/" + CONFIGURATION + Constants.EQUALS + MASTER,
            AddressTemplates.SHARED_STORE_COLOCATED_MASTER_TEMPLATE),

    SHARED_STORE_COLOCATED_SLAVE(Ids.MESSAGING_HA_SHARED_STORE_COLOCATED_SLAVE,
            Names.SHARED_STORE_COLOCATED + Constants.SLASH + Names.SLAVE,
            ModelDescriptionConstants.SHARED_STORE_COLOCATED + "/" + CONFIGURATION + Constants.EQUALS + SLAVE,
            AddressTemplates.SHARED_STORE_COLOCATED_SLAVE_TEMPLATE),

    SHARED_STORE_COLOCATED(Ids.MESSAGING_HA_SHARED_STORE_COLOCATED,
            Names.SHARED_STORE_COLOCATED,
            ModelDescriptionConstants.SHARED_STORE_COLOCATED,
            AddressTemplates.SHARED_STORE_COLOCATED_TEMPLATE,
            SHARED_STORE_COLOCATED_MASTER, SHARED_STORE_COLOCATED_SLAVE),

    SHARED_STORE_MASTER(Ids.MESSAGING_HA_SHARED_STORE_MASTER,
            Names.SHARED_STORE_MASTER,
            ModelDescriptionConstants.SHARED_STORE_MASTER,
            AddressTemplates.SHARED_STORE_MASTER_TEMPLATE),

    SHARED_STORE_SLAVE(Ids.MESSAGING_HA_SHARED_STORE_SLAVE,
            Names.SHARED_STORE_SLAVE,
            ModelDescriptionConstants.SHARED_STORE_SLAVE,
            AddressTemplates.SHARED_STORE_SLAVE_TEMPLATE);

    public static HaPolicy fromResourceName(String resourceName) {
        HaPolicy result = null;
        switch (resourceName) {
            case ModelDescriptionConstants.LIVE_ONLY:
                result = LIVE_ONLY;
                break;
            case ModelDescriptionConstants.REPLICATION_COLOCATED:
                result = REPLICATION_COLOCATED;
                break;
            case ModelDescriptionConstants.REPLICATION_MASTER:
                result = REPLICATION_MASTER;
                break;
            case ModelDescriptionConstants.REPLICATION_SLAVE:
                result = REPLICATION_SLAVE;
                break;
            case ModelDescriptionConstants.SHARED_STORE_COLOCATED:
                result = SHARED_STORE_COLOCATED;
                break;
            case ModelDescriptionConstants.SHARED_STORE_MASTER:
                result = SHARED_STORE_MASTER;
                break;
            case ModelDescriptionConstants.SHARED_STORE_SLAVE:
                result = SHARED_STORE_SLAVE;
                break;
            default:
                break;
        }
        return result;
    }

    final String baseId;
    final String type;
    final String singleton;
    final AddressTemplate template;
    final HaPolicy master;
    final HaPolicy slave;

    HaPolicy(final String baseId, final String type, final String singleton, final AddressTemplate template) {
        this(baseId, type, singleton, template, null, null);
    }

    HaPolicy(final String baseId, final String type, final String singleton, final AddressTemplate template,
            final HaPolicy master, final HaPolicy slave) {
        this.baseId = baseId;
        this.type = type;
        this.singleton = singleton;
        this.template = template;
        this.master = master;
        this.slave = slave;
    }

    AddressTemplate singleton() {
        return SELECTED_SERVER_TEMPLATE.append(HA_POLICY + Constants.EQUALS + singleton);
    }

    List<HaPolicy> children() {
        if (master == null && slave == null) {
            return emptyList();
        }
        return asList(master, slave); // oder is important!
    }

    /**
     * Adds a HA policy. The statement context must be able to resolve the selected server!
     */
    void add(Dispatcher dispatcher, StatementContext statementContext, Callback callback) {
        List<Operation> operations = new ArrayList<>();
        operations.add(new Operation.Builder(singleton().resolve(statementContext), ADD).build());
        if (!children().isEmpty()) {
            children().forEach(child -> operations.add(
                    new Operation.Builder(child.singleton().resolve(statementContext), ADD).build()));
        }
        dispatcher.execute(new Composite(operations), (CompositeResult result) -> callback.execute());
    }

    /**
     * Saves a HA policy. The statement context must be able to resolve the selected server!
     */
    void save(Map<String, Object> changedValues, MetadataRegistry metadataRegistry, StatementContext statementContext,
            CrudOperations crud, Callback callback) {
        ResourceAddress address = singleton().resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(template);
        crud.saveSingleton(type, address, changedValues, metadata, callback);
    }

    /**
     * Resets a HA policy. The statement context must be able to resolve the selected server!
     */
    void reset(Form<ModelNode> form, MetadataRegistry metadataRegistry, StatementContext statementContext,
            CrudOperations crud, Callback callback) {
        ResourceAddress address = singleton().resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(template);
        crud.resetSingleton(type, address, form, metadata, callback);
    }

    /**
     * Removes the HA policy. The statement context must be able to resolve the selected server!
     */
    void remove(Dispatcher dispatcher, StatementContext statementContext, Resources resources, Callback callback) {
        DialogFactory.showConfirmation(
                resources.messages().removeConfirmationTitle(type),
                resources.messages().removeConfirmationQuestion(type),
                () -> {
                    List<Operation> operations = new ArrayList<>();
                    if (children().isEmpty()) {
                        children().forEach(child -> operations
                                .add(new Operation.Builder(child.singleton().resolve(statementContext), REMOVE).build()));
                    }
                    operations.add(new Operation.Builder(singleton().resolve(statementContext), REMOVE).build());
                    dispatcher.execute(new Composite(operations), (CompositeResult result) -> callback.execute());
                });
    }

    private static class Constants {

        private static final String SLASH = " / ";
        private static final String EQUALS = "=";
    }
}
