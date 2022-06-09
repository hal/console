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
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONFIGURATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HA_POLICY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PRIMARY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SECONDARY;

public enum HaPolicy {

    LIVE_ONLY(Ids.MESSAGING_HA_REPLICATION_LIVE_ONLY,
            Names.LIVE_ONLY,
            ModelDescriptionConstants.LIVE_ONLY,
            AddressTemplates.LIVE_ONLY_TEMPLATE),

    REPLICATION_COLOCATED_PRIMARY(Ids.MESSAGING_HA_REPLICATION_COLOCATED_PRIMARY,
            Names.REPLICATION_COLOCATED + Constants.SLASH + Names.PRIMARY,
            ModelDescriptionConstants.REPLICATION_COLOCATED + "/" + CONFIGURATION + Constants.EQUALS + PRIMARY,
            AddressTemplates.REPLICATION_COLOCATED_PRIMARY_TEMPLATE),

    REPLICATION_COLOCATED_SECONDARY(Ids.MESSAGING_HA_REPLICATION_COLOCATED_SECONDARY,
            Names.REPLICATION_COLOCATED + Constants.SLASH + Names.SECONDARY,
            ModelDescriptionConstants.REPLICATION_COLOCATED + "/" + CONFIGURATION + Constants.EQUALS + SECONDARY,
            AddressTemplates.REPLICATION_COLOCATED_SECONDARY_TEMPLATE),

    REPLICATION_COLOCATED(Ids.MESSAGING_HA_REPLICATION_COLOCATED,
            Names.REPLICATION_COLOCATED,
            ModelDescriptionConstants.REPLICATION_COLOCATED,
            AddressTemplates.REPLICATION_COLOCATED_TEMPLATE,
            REPLICATION_COLOCATED_PRIMARY, REPLICATION_COLOCATED_SECONDARY),

    REPLICATION_PRIMARY(Ids.MESSAGING_HA_REPLICATION_PRIMARY,
            Names.REPLICATION_PRIMARY,
            ModelDescriptionConstants.REPLICATION_PRIMARY,
            AddressTemplates.REPLICATION_PRIMARY_TEMPLATE),

    REPLICATION_SECONDARY(Ids.MESSAGING_HA_REPLICATION_SECONDARY,
            Names.REPLICATION_SECONDARY,
            ModelDescriptionConstants.REPLICATION_SECONDARY,
            AddressTemplates.REPLICATION_SECONDARY_TEMPLATE),

    SHARED_STORE_COLOCATED_PRIMARY(Ids.MESSAGING_HA_SHARED_STORE_COLOCATED_PRIMARY,
            Names.SHARED_STORE_COLOCATED + Constants.SLASH + Names.PRIMARY,
            ModelDescriptionConstants.SHARED_STORE_COLOCATED + "/" + CONFIGURATION + Constants.EQUALS + PRIMARY,
            AddressTemplates.SHARED_STORE_COLOCATED_PRIMARY_TEMPLATE),

    SHARED_STORE_COLOCATED_SECONDARY(Ids.MESSAGING_HA_SHARED_STORE_COLOCATED_SECONDARY,
            Names.SHARED_STORE_COLOCATED + Constants.SLASH + Names.SECONDARY,
            ModelDescriptionConstants.SHARED_STORE_COLOCATED + "/" + CONFIGURATION + Constants.EQUALS + SECONDARY,
            AddressTemplates.SHARED_STORE_COLOCATED_SECONDARY_TEMPLATE),

    SHARED_STORE_COLOCATED(Ids.MESSAGING_HA_SHARED_STORE_COLOCATED,
            Names.SHARED_STORE_COLOCATED,
            ModelDescriptionConstants.SHARED_STORE_COLOCATED,
            AddressTemplates.SHARED_STORE_COLOCATED_TEMPLATE,
            SHARED_STORE_COLOCATED_PRIMARY, SHARED_STORE_COLOCATED_SECONDARY),

    SHARED_STORE_PRIMARY(Ids.MESSAGING_HA_SHARED_STORE_PRIMARY,
            Names.SHARED_STORE_PRIMARY,
            ModelDescriptionConstants.SHARED_STORE_PRIMARY,
            AddressTemplates.SHARED_STORE_PRIMARY_TEMPLATE),

    SHARED_STORE_SECONDARY(Ids.MESSAGING_HA_SHARED_STORE_SECONDARY,
            Names.SHARED_STORE_SECONDARY,
            ModelDescriptionConstants.SHARED_STORE_SECONDARY,
            AddressTemplates.SHARED_STORE_SECONDARY_TEMPLATE);

    public static HaPolicy fromResourceName(String resourceName) {
        HaPolicy result = null;
        switch (resourceName) {
            case ModelDescriptionConstants.LIVE_ONLY:
                result = LIVE_ONLY;
                break;
            case ModelDescriptionConstants.REPLICATION_COLOCATED:
                result = REPLICATION_COLOCATED;
                break;
            case ModelDescriptionConstants.REPLICATION_PRIMARY:
                result = REPLICATION_PRIMARY;
                break;
            case ModelDescriptionConstants.REPLICATION_SECONDARY:
                result = REPLICATION_SECONDARY;
                break;
            case ModelDescriptionConstants.SHARED_STORE_COLOCATED:
                result = SHARED_STORE_COLOCATED;
                break;
            case ModelDescriptionConstants.SHARED_STORE_PRIMARY:
                result = SHARED_STORE_PRIMARY;
                break;
            case ModelDescriptionConstants.SHARED_STORE_SECONDARY:
                result = SHARED_STORE_SECONDARY;
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
    final HaPolicy primary;
    final HaPolicy secondary;

    HaPolicy(final String baseId, final String type, final String singleton, final AddressTemplate template) {
        this(baseId, type, singleton, template, null, null);
    }

    HaPolicy(final String baseId, final String type, final String singleton, final AddressTemplate template,
            final HaPolicy primary, final HaPolicy secondary) {
        this.baseId = baseId;
        this.type = type;
        this.singleton = singleton;
        this.template = template;
        this.primary = primary;
        this.secondary = secondary;
    }

    AddressTemplate singleton() {
        return SELECTED_SERVER_TEMPLATE.append(HA_POLICY + Constants.EQUALS + singleton);
    }

    List<HaPolicy> children() {
        if (primary == null && secondary == null) {
            return emptyList();
        }
        return asList(primary, secondary); // oder is important!
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
