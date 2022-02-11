/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.runtime.subsystem.messaging;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.core.Json;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import com.google.common.collect.ImmutableMap;
import com.google.gwt.core.client.GWT;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import static org.jboss.hal.ballroom.Format.mediumDateTime;
import static org.jboss.hal.client.runtime.subsystem.messaging.AddressTemplates.MESSAGING_SERVER_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.messaging.AddressTemplates.MESSAGING_SERVER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public class ServerPresenter extends ApplicationFinderPresenter<ServerPresenter.MyView, ServerPresenter.MyProxy>
        implements SupportsExpertMode {

    private static final ServerResources RESOURCES = GWT.create(ServerResources.class);
    static final Metadata CONNECTION_METADATA = Metadata.staticDescription(ServerPresenter.RESOURCES.connection());
    static final Metadata CONNECTOR_METADATA = Metadata.staticDescription(ServerPresenter.RESOURCES.connector());
    static final Metadata CONSUMER_METADATA = Metadata.staticDescription(ServerPresenter.RESOURCES.consumer());
    static final Metadata PRODUCER_METADATA = Metadata.staticDescription(ServerPresenter.RESOURCES.producer());
    static final Metadata SESSION_METADATA = Metadata.staticDescription(RESOURCES.session());
    static final Metadata TRANSACTION_METADATA = Metadata.staticDescription(RESOURCES.transaction());

    private static final ImmutableMap<String, String> CONSUMER_MAPPING = new ImmutableMap.Builder<String, String>()
            .put("consumerID", CONSUMER_ID)
            .put("connectionID", CONNECTION_ID)
            .put("sessionID", SESSION_ID)
            .put("queueName", QUEUE_NAME)
            .put("browseOnly", BROWSE_ONLY)
            .put("creationTime", CREATION_TIMESTAMP)
            .put("deliveringCount", DELIVERING_COUNT)
            .put("durable", DURABLE)
            .put("destinationType", DESTINATION_TYPE)
            .put("destinationName", DESTINATION_NAME)
            .build();

    private final FinderPathFactory finderPathFactory;
    private final Dispatcher dispatcher;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private final Resources resources;
    private String messagingServer;
    private String connectionId;

    @Inject
    public ServerPresenter(EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            FinderPathFactory finderPathFactory,
            Dispatcher dispatcher,
            MetadataRegistry metadataRegistry,
            StatementContext statementContext,
            Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.finderPathFactory = finderPathFactory;
        this.dispatcher = dispatcher;
        this.metadataRegistry = metadataRegistry;
        this.statementContext = statementContext;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        messagingServer = request.getParameter(Ids.MESSAGING_SERVER, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return MESSAGING_SERVER_TEMPLATE.resolve(statementContext, messagingServer);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.runtimeServerPath()
                .append(Ids.RUNTIME_SUBSYSTEM, MESSAGING_ACTIVEMQ, resources.constants().monitor(), Names.MESSAGING)
                .append(Ids.MESSAGING_SERVER_RUNTIME, Ids.messagingServer(messagingServer), Names.SERVER,
                        messagingServer);
    }

    @Override
    protected void reload() {
        Operation connectionsOp = new Operation.Builder(resourceAddress(), LIST_CONNECTIONS_AS_JSON).build();
        Operation consumersOp = new Operation.Builder(resourceAddress(), LIST_ALL_CONSUMERS_AS_JSON).build();
        Operation producersOp = new Operation.Builder(resourceAddress(), LIST_PRODUCERS_INFO_AS_JSON).build();
        Operation connectorsOp = new Operation.Builder(resourceAddress(), GET_CONNECTORS_AS_JSON).build();
        Operation transactionsOp = new Operation.Builder(resourceAddress(),
                LIST_PREPARED_TRANSACTION_DETAILS_AS_JSON).build();
        Composite composite = new Composite(connectionsOp, consumersOp, producersOp, connectorsOp, transactionsOp);
        dispatcher.execute(composite, (CompositeResult result) -> {
            // connections
            String json = result.step(0).get(RESULT).asString();
            List<ModelNode> connections = Json.parseArray(json, CONNECTION_METADATA, ImmutableMap.of(
                    "connectionID", CONNECTION_ID,
                    "clientAddress", CLIENT_ADDRESS,
                    "creationTime", CREATION_TIMESTAMP,
                    "implementation", IMPLEMENTATION,
                    "sessionCount", SESSION_COUNT));
            setCreationTime(connections);
            getView().updateConnections(connections);

            // consumers
            json = result.step(1).get(RESULT).asString();
            List<ModelNode> consumers = Json.parseArray(json, CONSUMER_METADATA, CONSUMER_MAPPING);
            setCreationTime(consumers);
            getView().updateConsumers(consumers);

            // producers
            json = result.step(2).get(RESULT).asString();
            List<ModelNode> producers = Json.parseArray(json, PRODUCER_METADATA, ImmutableMap.of(
                    "connectionID", CONNECTION_ID,
                    "sessionID", SESSION_ID,
                    "destination", DESTINATION,
                    "lastUUIDSent", LAST_UUID_SENT,
                    "msgSent", MSG_SENT));
            getView().updateProducers(producers);

            // connectors
            json = result.step(3).get(RESULT).asString();
            List<ModelNode> connectors = Json.parseArray(json, CONNECTOR_METADATA, ImmutableMap.of(
                    "name", NAME,
                    "factoryClassName", FACTORY_CLASS_NAME,
                    "params", PARAMS,
                    "extraProps", EXTRA_PROPS));
            getView().updateConnectors(connectors);

            json = result.step(4).get(RESULT).asString();
            List<ModelNode> transactions = Json.parseArray(json, TRANSACTION_METADATA, ImmutableMap.of(
                    "transactionID", TRANSACTION_ID));
            getView().updateTransactions(transactions);
        });
    }

    // ------------------------------------------------------ connection

    void openCloseConnectionsDialog() {
        Metadata metadata = metadataRegistry.lookup(MESSAGING_SERVER_TEMPLATE);
        CloseConnectionsDialog dialog = new CloseConnectionsDialog(metadata, resources);
        dialog.setPresenter(this);
        dialog.show();
    }

    void closeConnections(String operation, ModelNode payload) {
        Operation op = new Operation.Builder(resourceAddress(), operation)
                .payload(payload)
                .build();
        dispatcher.execute(op, result -> {
            if (result.asBoolean()) {
                MessageEvent.fire(getEventBus(), Message.success(resources.messages().closeConnectionsSuccess()));
            } else {
                MessageEvent.fire(getEventBus(), Message.success(resources.messages().noMatchingConnections()));
            }
        });
    }

    private void selectConnection(String connectionId) {
        this.connectionId = connectionId;
    }

    String connectionSegment() {
        return connectionId != null ? Names.CONNECTION + ": " + connectionId : Names.NOT_AVAILABLE;
    }

    // ------------------------------------------------------ connection / sessions

    void showSessions(ModelNode connection) {
        selectConnection(connection.get(CONNECTION_ID).asString());
        Operation operation = new Operation.Builder(resourceAddress(), LIST_SESSIONS_AS_JSON)
                .param(CONNECTION_ID, connectionId)
                .build();
        dispatcher.execute(operation, result -> {
            List<ModelNode> sessions = Json.parseArray(result.asString(), SESSION_METADATA, ImmutableMap.of(
                    "sessionID", SESSION_ID,
                    "creationTime", CREATION_TIMESTAMP,
                    "consumerCount", CONSUMER_COUNT));
            setCreationTime(sessions);
            getView().updateSessions(sessions);
        });
    }

    // ------------------------------------------------------ connection / consumers

    void showConnectionConsumers(ModelNode connection) {
        selectConnection(connection.get(CONNECTION_ID).asString());
        Operation operation = new Operation.Builder(resourceAddress(), LIST_CONSUMERS_AS_JSON)
                .param(CONNECTION_ID, connectionId)
                .build();
        dispatcher.execute(operation, result -> {
            List<ModelNode> consumers = Json.parseArray(result.asString(), CONSUMER_METADATA, CONSUMER_MAPPING);
            setCreationTime(consumers);
            getView().updateConnectionConsumers(consumers);
        });
    }

    // ------------------------------------------------------ role

    void getRoles(String addressMatch) {
        Operation operation = new Operation.Builder(resourceAddress(), GET_ROLES)
                .param(ADDRESS_MATCH, addressMatch)
                .build();
        dispatcher.execute(operation, result -> getView().updateRoles(result.asList()));
    }

    // ------------------------------------------------------ transaction

    void rollbackTransaction(ModelNode transaction) {
        if (transaction != null) {
            String transactionId = transaction.get(TRANSACTION_ID).asString();
            Operation operation = new Operation.Builder(resourceAddress(), ROLLBACK_PREPARED_TRANSACTION)
                    .param(TRANSACTION_ID, transactionId)
                    .build();
            dispatcher.execute(operation, result -> MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().rollbackTransactionSuccess(transactionId))));
        }
    }

    void commitTransaction(ModelNode transaction) {
        if (transaction != null) {
            String transactionId = transaction.get(TRANSACTION_ID).asString();
            Operation operation = new Operation.Builder(resourceAddress(), COMMIT_PREPARED_TRANSACTION)
                    .param(TRANSACTION_ID, transactionId)
                    .build();
            dispatcher.execute(operation, result -> MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().commitTransactionSuccess(transactionId))));
        }
    }

    // ------------------------------------------------------ helper methods

    private void setCreationTime(List<ModelNode> nodes) {
        for (ModelNode node : nodes) {
            if (node.hasDefined(CREATION_TIMESTAMP)) {
                long ts = node.get(CREATION_TIMESTAMP).asLong();
                node.get(CREATION_TIME).set(mediumDateTime(new Date(ts)));
            }
        }
    }

    // @formatter:off
    @ProxyCodeSplit
    @Requires(MESSAGING_SERVER_ADDRESS)
    @NameToken(NameTokens.MESSAGING_SERVER_RUNTIME)
    public interface MyProxy extends ProxyPlace<ServerPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<ServerPresenter> {
        void updateConnections(List<ModelNode> connections);

        void updateRoles(List<ModelNode> roles);

        void updateConnectionConsumers(List<ModelNode> consumers);

        void updateConsumers(List<ModelNode> consumers);

        void updateProducers(List<ModelNode> producers);

        void updateConnectors(List<ModelNode> connectors);

        void updateSessions(List<ModelNode> sessions);

        void updateTransactions(List<ModelNode> transactions);
    }
    // @formatter:on
}
