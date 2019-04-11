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
package org.jboss.hal.client.runtime.subsystem.messaging;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.table.InlineAction;
import org.jboss.hal.ballroom.table.Scope;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.security.Constraints;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.runtime.subsystem.messaging.AddressTemplates.MESSAGING_SERVER_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.messaging.ServerPresenter.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;

public class ServerView extends HalViewImpl implements MyView {

    private final Pages connectionPages;
    private final Table<ModelNode> connectionTable;
    private final ModelNodeForm<ModelNode> connectionForm;
    private final Table<ModelNode> sessionTable;
    private final ModelNodeForm<ModelNode> sessionForm;
    private final Table<ModelNode> connectionConsumerTable;
    private final ModelNodeForm<ModelNode> connectionConsumerForm;
    private final Table<ModelNode> consumerTable;
    private final ModelNodeForm<ModelNode> consumerForm;
    private final Table<ModelNode> producerTable;
    private final ModelNodeForm<ModelNode> producerForm;
    private final Table<ModelNode> connectorTable;
    private final ModelNodeForm<ModelNode> connectorForm;
    private final GetRolesElement getRolesElement;
    private final Table<ModelNode> roleTable;
    private final ModelNodeForm<ModelNode> roleForm;
    private final Table<ModelNode> transactionTable;
    private final ModelNodeForm<ModelNode> transactionForm;
    private ServerPresenter presenter;

    @Inject
    public ServerView(MetadataRegistry metadataRegistry, Resources resources) {

        // ------------------------------------------------------ connections

        Constraint cfa = Constraint.executable(MESSAGING_SERVER_TEMPLATE, CLOSE_CONNECTIONS_FOR_ADDRESS);
        Constraint cc = Constraint.executable(MESSAGING_SERVER_TEMPLATE, CLOSE_CONSUMER_CONNECTIONS_FOR_ADDRESS);
        Constraint cfu = Constraint.executable(MESSAGING_SERVER_TEMPLATE, CLOSE_CONNECTIONS_FOR_USER);
        Constraints constraints = Constraints.and(cfa, cc, cfu);

        List<InlineAction<ModelNode>> inlineActions = new ArrayList<>();
        inlineActions.add(new InlineAction<>(Names.SESSIONS, row -> presenter.showSessions(row)));
        inlineActions.add(new InlineAction<>(Names.CONSUMERS, row -> presenter.showConnectionConsumers(row)));

        connectionTable = new ModelNodeTable.Builder<>(Ids.MESSAGING_SERVER_CONNECTION_TABLE, CONNECTION_METADATA)
                .button("Close Connections", t -> presenter.openCloseConnectionsDialog(), constraints)
                .columns(asList(CONNECTION_ID, CLIENT_ADDRESS, SESSION_COUNT))
                .column(inlineActions)
                .build();
        connectionForm = new ModelNodeForm.Builder<>(Ids.MESSAGING_SERVER_CONNECTION_FORM, CONNECTION_METADATA)
                .include(CONNECTION_ID, CLIENT_ADDRESS, CREATION_TIME, IMPLEMENTATION, SESSION_COUNT)
                .unsorted()
                .readOnly()
                .build();
        HTMLElement connectionSection = section()
                .add(h(1).textContent(Names.CONNECTIONS))
                .add(connectionTable)
                .add(connectionForm)
                .get();

        // ------------------------------------------------------ connection / sessions

        sessionTable = new ModelNodeTable.Builder<>(Ids.MESSAGING_SERVER_SESSION_TABLE, SESSION_METADATA)
                .columns(asList(SESSION_ID, CONSUMER_COUNT))
                .build();
        sessionForm = new ModelNodeForm.Builder<>(Ids.MESSAGING_SERVER_SESSION_FORM, SESSION_METADATA)
                .include(SESSION_ID, CREATION_TIME, SESSION_COUNT)
                .unsorted()
                .readOnly()
                .build();
        HTMLElement sessionSection = section()
                .add(h(1).textContent(Names.SESSIONS))
                .add(sessionTable)
                .add(sessionForm)
                .get();

        // ------------------------------------------------------ connection / consumers

        connectionConsumerTable = new ModelNodeTable.Builder<>(Ids.MESSAGING_SERVER_CONNECTION_CONSUMER_TABLE,
                CONSUMER_METADATA)
                .columns(asList(SESSION_ID, DESTINATION_NAME))
                .build();
        connectionConsumerForm = new ModelNodeForm.Builder<>(Ids.MESSAGING_SERVER_CONNECTION_CONSUMER_FORM,
                CONSUMER_METADATA)
                .include(asList(CONSUMER_ID, CONNECTION_ID, SESSION_ID, QUEUE_NAME, DESTINATION_NAME, DESTINATION_TYPE,
                        CREATION_TIME, DELIVERING_COUNT, DURABLE, BROWSE_ONLY))
                .unsorted()
                .readOnly()
                .build();
        HTMLElement connectionConsumerSection = section()
                .add(h(1).textContent(Names.CONSUMERS))
                .add(connectionConsumerTable)
                .add(connectionConsumerForm)
                .get();

        // ------------------------------------------------------ all consumers

        consumerTable = new ModelNodeTable.Builder<>(Ids.MESSAGING_SERVER_CONSUMER_TABLE, CONSUMER_METADATA)
                .columns(asList(CONNECTION_ID, SESSION_ID, DESTINATION_NAME))
                .build();
        consumerForm = new ModelNodeForm.Builder<>(Ids.MESSAGING_SERVER_CONSUMER_FORM, CONSUMER_METADATA)
                .include(asList(CONSUMER_ID, CONNECTION_ID, SESSION_ID, QUEUE_NAME, DESTINATION_NAME, DESTINATION_TYPE,
                        CREATION_TIME, DELIVERING_COUNT, DURABLE, BROWSE_ONLY))
                .unsorted()
                .readOnly()
                .build();
        HTMLElement consumerSection = section()
                .add(h(1).textContent(Names.CONSUMERS))
                .add(consumerTable)
                .add(consumerForm)
                .get();

        // ------------------------------------------------------ producers

        producerTable = new ModelNodeTable.Builder<>(Ids.MESSAGING_SERVER_PRODUCER_TABLE, PRODUCER_METADATA)
                .columns(asList(CONNECTION_ID, SESSION_ID, DESTINATION))
                .build();
        producerForm = new ModelNodeForm.Builder<>(Ids.MESSAGING_SERVER_PRODUCER_FORM, PRODUCER_METADATA)
                .include(CONNECTION_ID, SESSION_ID, DESTINATION, LAST_UUID_SENT, MSG_SENT)
                .unsorted()
                .readOnly()
                .build();
        HTMLElement producerSection = section()
                .add(h(1).textContent(Names.PRODUCERS))
                .add(producerTable)
                .add(producerForm)
                .get();

        // ------------------------------------------------------ connectors

        connectorTable = new ModelNodeTable.Builder<>(Ids.MESSAGING_SERVER_CONNECTOR_TABLE, CONNECTOR_METADATA)
                .columns(asList(NAME, FACTORY_CLASS_NAME))
                .build();
        connectorForm = new ModelNodeForm.Builder<>(Ids.MESSAGING_SERVER_CONNECTOR_FORM, CONNECTOR_METADATA)
                .include(NAME, FACTORY_CLASS_NAME, PARAMS, EXTRA_PROPS)
                .unsorted()
                .readOnly()
                .build();
        HTMLElement connectorSection = section()
                .add(h(1).textContent(Names.CONNECTORS))
                .add(connectorTable)
                .add(connectorForm)
                .get();

        // ------------------------------------------------------ roles

        Metadata roleMetadata = getRolesReplyMetadata(metadataRegistry.lookup(MESSAGING_SERVER_TEMPLATE));
        getRolesElement = new GetRolesElement(resources);
        roleTable = new ModelNodeTable.Builder<>(Ids.MESSAGING_SERVER_ROLE_TABLE, roleMetadata)
                .column(NAME)
                .build();
        roleForm = new ModelNodeForm.Builder<>(Ids.MESSAGING_SERVER_ROLE_FORM, roleMetadata)
                .include(NAME, SEND, CONSUME, CREATE_DURABLE_QUEUE, DELETE_DURABLE_QUEUE,
                        CREATE_NON_DURABLE_QUEUE, DELETE_NON_DURABLE_QUEUE,
                        MANAGE, BROWSE, CREATE_ADDRESS, DELETE_ADDRESS)
                .unsorted()
                .readOnly()
                .build();
        HTMLElement roleSection = section()
                .add(h(1).textContent(resources.constants().roles()))
                .add(getRolesElement)
                .add(roleTable)
                .add(roleForm)
                .get();

        // ------------------------------------------------------ transaction

        transactionTable = new ModelNodeTable.Builder<>(Ids.MESSAGING_SERVER_TRANSACTION_TABLE, TRANSACTION_METADATA)
                .button(resources.constants().rollback(), table -> presenter.rollbackTransaction(table.selectedRow()),
                        Scope.SELECTED, Constraint.executable(MESSAGING_SERVER_TEMPLATE, ROLLBACK_PREPARED_TRANSACTION))
                .button(resources.constants().commit(), table -> presenter.commitTransaction(table.selectedRow()),
                        Scope.SELECTED, Constraint.executable(MESSAGING_SERVER_TEMPLATE, COMMIT_PREPARED_TRANSACTION))
                .column(TRANSACTION_ID)
                .build();
        transactionForm = new ModelNodeForm.Builder<>(Ids.MESSAGING_SERVER_TRANSACTION_FORM, TRANSACTION_METADATA)
                .include(TRANSACTION_ID)
                .unsorted()
                .readOnly()
                .build();
        HTMLElement transactionSection = section()
                .add(h(1).textContent(resources.constants().transactions()))
                .add(transactionTable)
                .add(transactionForm)
                .get();

        // ------------------------------------------------------ pages and navigation

        connectionPages = new Pages(Ids.MESSAGING_SERVER_CONNECTION_PAGES, Ids.MESSAGING_SERVER_CONNECTION_PAGE,
                connectionSection);
        connectionPages.addPage(Ids.MESSAGING_SERVER_CONNECTION_PAGE, Ids.MESSAGING_SERVER_SESSION_PAGE,
                () -> presenter.connectionSegment(), () -> Names.SESSIONS, sessionSection);
        connectionPages.addPage(Ids.MESSAGING_SERVER_CONNECTION_PAGE, Ids.MESSAGING_SERVER_CONNECTION_CONSUMER_PAGE,
                () -> presenter.connectionSegment(), () -> Names.CONSUMERS, connectionConsumerSection);

        VerticalNavigation navigation = new VerticalNavigation();
        navigation.addPrimary(Ids.MESSAGING_SERVER_CONNECTION_ITEM, Names.CONNECTIONS,
                fontAwesome("exchange"), connectionPages);
        navigation.addPrimary(Ids.MESSAGING_SERVER_CONSUMER_ITEM, Names.CONSUMERS,
                fontAwesome("download"), consumerSection);
        navigation.addPrimary(Ids.MESSAGING_SERVER_PRODUCER_ITEM, Names.PRODUCERS,
                fontAwesome("upload"), producerSection);
        navigation.addPrimary(Ids.MESSAGING_SERVER_CONNECTOR_ITEM, Names.CONNECTORS,
                pfIcon("plugged"), connectorSection);
        navigation.addPrimary(Ids.MESSAGING_SERVER_ROLE_ITEM, resources.constants().roles(),
                pfIcon("users"), roleSection);
        navigation.addPrimary(Ids.MESSAGING_SERVER_TRANSACTION_ITEM, resources.constants().transactions(),
                fontAwesome("handshake-o"), transactionSection);

        registerAttachable(navigation,
                connectionTable, connectionForm,
                sessionTable, sessionForm,
                connectionConsumerTable, connectionConsumerForm,
                consumerTable, consumerForm,
                producerTable, producerForm,
                connectorTable, connectorForm,
                roleTable, roleForm,
                transactionTable, transactionForm);
        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
    }

    @Override
    public void setPresenter(ServerPresenter presenter) {
        this.presenter = presenter;
        getRolesElement.setPresenter(presenter);
    }

    @Override
    public void attach() {
        super.attach();
        connectionTable.bindForm(connectionForm);
        sessionTable.bindForm(sessionForm);
        connectionConsumerTable.bindForm(connectionConsumerForm);
        consumerTable.bindForm(consumerForm);
        producerTable.bindForm(producerForm);
        connectorTable.bindForm(connectorForm);
        getRolesElement.clear();
        roleTable.bindForm(roleForm);
        transactionTable.bindForm(transactionForm);
    }

    @Override
    public void updateConnections(List<ModelNode> connections) {
        connectionForm.clear();
        connectionTable.update(connections);
    }

    @Override
    public void updateSessions(List<ModelNode> sessions) {
        sessionForm.clear();
        sessionTable.update(sessions);
        connectionPages.showPage(Ids.MESSAGING_SERVER_SESSION_PAGE);
    }

    @Override
    public void updateConnectionConsumers(List<ModelNode> consumers) {
        connectionConsumerForm.clear();
        connectionConsumerTable.update(consumers);
        connectionPages.showPage(Ids.MESSAGING_SERVER_CONNECTION_CONSUMER_PAGE);
    }

    @Override
    public void updateConsumers(List<ModelNode> consumers) {
        consumerForm.clear();
        consumerTable.update(consumers);
    }

    @Override
    public void updateProducers(List<ModelNode> producers) {
        producerForm.clear();
        producerTable.update(producers);
    }

    @Override
    public void updateConnectors(List<ModelNode> connectors) {
        connectorForm.clear();
        connectorTable.update(connectors);
    }

    @Override
    public void updateRoles(List<ModelNode> roles) {
        roleForm.clear();
        roleTable.update(roles);
    }

    @Override
    public void updateTransactions(List<ModelNode> transactions) {
        transactionForm.clear();
        transactionTable.update(transactions);
    }

    private Metadata getRolesReplyMetadata(Metadata metadata) {
        ModelNode payload = new ModelNode();
        ResourceDescription description = metadata.getDescription();
        payload.get(DESCRIPTION).set(failSafeGet(description,
                String.join("/", OPERATIONS, GET_ROLES, DESCRIPTION)));
        payload.get(ATTRIBUTES).set(failSafeGet(description,
                String.join("/", OPERATIONS, GET_ROLES, REPLY_PROPERTIES, VALUE_TYPE)));
        return new Metadata(metadata.getTemplate(), () -> SecurityContext.READ_ONLY, new ResourceDescription(payload),
                metadata.getCapabilities());
    }
}
