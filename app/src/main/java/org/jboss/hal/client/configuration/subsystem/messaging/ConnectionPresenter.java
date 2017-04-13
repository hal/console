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

import java.util.List;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SELECTED_SERVER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SERVER_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

/**
 * @author Harald Pehl
 */
public class ConnectionPresenter
        extends ServerSettingsPresenter<ConnectionPresenter.MyView, ConnectionPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    // TODO Replace with
    // TODO {ACCEPTOR_ADDRESS, IN_VM_ACCEPTOR_ADDRESS, REMOTE_ACCEPTOR_ADDRESS,
    // TODO  CONNECTOR_ADDRESS, IN_VM_CONNECTOR_ADDRESS, REMOTE_CONNECTOR_ADDRESS,
    // TODO  CONNECTOR_SERVICE_ADDRESS, CONNECTION_FACTORY_ADDRESS, POOLED_CONNECTION_FACTORY_ADDRESS}
    // TODO once WFCORE-2022 is resolved
    @Requires(SERVER_ADDRESS)
    @NameToken(NameTokens.MESSAGING_SERVER_CONNECTION)
    public interface MyProxy extends ProxyPlace<ConnectionPresenter> {}

    public interface MyView extends MbuiView<ConnectionPresenter> {
        void updateAcceptor(List<NamedNode> acceptors);
        void updateInVmAcceptor(List<NamedNode> inVmAcceptors);
        void updateHttpAcceptor(List<NamedNode> httpAcceptors);
        void updateRemoteAcceptor(List<NamedNode> remoteAcceptors);
        void updateConnector(List<NamedNode> connectors);
        void updateInVmConnector(List<NamedNode> inVmConnectors);
        void updateHttpConnector(List<NamedNode> httpConnectors);
        void updateRemoteConnector(List<NamedNode> remoteConnectors);
        void updateConnectorService(List<NamedNode> connectorServices);
        void updateConnectionFactory(List<NamedNode> connectionFactories);
        void updatePooledConnectionFactory(List<NamedNode> pooledConnectionFactories);
    }
    // @formatter:on


    private final Dispatcher dispatcher;

    @Inject
    public ConnectionPresenter(
            final EventBus eventBus,
            final ConnectionPresenter.MyView view,
            final ConnectionPresenter.MyProxy myProxy,
            final Finder finder,
            final MetadataRegistry metadataRegistry,
            final Dispatcher dispatcher,
            final CrudOperations crud,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            final Resources resources) {
        super(eventBus, view, myProxy, finder, crud, metadataRegistry, finderPathFactory, statementContext, resources);
        this.dispatcher = dispatcher;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.subsystemPath(MESSAGING_ACTIVEMQ)
                .append(Ids.MESSAGING_CATEGORY, Ids.asId(Names.SERVER),
                        resources.constants().category(), Names.SERVER)
                .append(Ids.MESSAGING_SERVER, Ids.messagingServer(serverName),
                        Names.SERVER, serverName)
                .append(Ids.MESSAGING_SERVER_SETTINGS, Ids.MESSAGING_SERVER_CONNECTION,
                        resources.constants().settings(), Names.CONNECTIONS);
    }

    @Override
    protected void reload() {
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.resolve(statementContext);
        crud.readChildren(address, asList(ACCEPTOR, IN_VM_ACCEPTOR, HTTP_ACCEPTOR, REMOTE_ACCEPTOR,
                CONNECTOR, IN_VM_CONNECTOR, HTTP_CONNECTOR, REMOTE_CONNECTOR,
                CONNECTOR_SERVICE, CONNECTION_FACTORY, POOLED_CONNECTION_FACTORY),
                result -> {
                    getView().updateAcceptor(asNamedNodes(result.step(0).get(RESULT).asPropertyList()));
                    getView().updateInVmAcceptor(asNamedNodes(result.step(1).get(RESULT).asPropertyList()));
                    getView().updateHttpAcceptor(asNamedNodes(result.step(2).get(RESULT).asPropertyList()));
                    getView().updateRemoteAcceptor(asNamedNodes(result.step(3).get(RESULT).asPropertyList()));
                    getView().updateConnector(asNamedNodes(result.step(4).get(RESULT).asPropertyList()));
                    getView().updateInVmConnector(asNamedNodes(result.step(5).get(RESULT).asPropertyList()));
                    getView().updateHttpConnector(asNamedNodes(result.step(6).get(RESULT).asPropertyList()));
                    getView().updateRemoteConnector(asNamedNodes(result.step(7).get(RESULT).asPropertyList()));
                    getView().updateConnectorService(asNamedNodes(result.step(8).get(RESULT).asPropertyList()));
                    getView().updateConnectionFactory(asNamedNodes(result.step(9).get(RESULT).asPropertyList()));
                    getView().updatePooledConnectionFactory(asNamedNodes(result.step(10).get(RESULT).asPropertyList()));
                });
    }

    void addHttp(ServerSubResource ssr) {
        Metadata metadata = metadataRegistry.lookup(ssr.template);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(ssr.baseId, Ids.ADD_SUFFIX), metadata)
                .unboundFormItem(new NameItem(), 0)
                .fromRequestProperties()
                .requiredOnly()
                .build();
        form.getFormItem(HTTP_LISTENER).registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, statementContext,
                        AddressTemplate.of("/{selected.profile}/subsystem=undertow/server=*/http-listener=*")));

        new AddResourceDialog(ssr.type, form, (name, model) -> {
            ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(ssr.resource + "=" + name)
                    .resolve(statementContext);
            crud.add(ssr.type, name, address, model, (n, a) -> reload());
        }).show();
    }

    void addRemote(ServerSubResource ssr) {
        Metadata metadata = metadataRegistry.lookup(ssr.template);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(ssr.baseId, Ids.ADD_SUFFIX), metadata)
                .unboundFormItem(new NameItem(), 0)
                .fromRequestProperties()
                .requiredOnly()
                .build();
        form.getFormItem(SOCKET_BINDING).registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, statementContext,
                        AddressTemplate.of("/socket-binding-group=*/socket-binding=*")));

        new AddResourceDialog(ssr.type, form, (name, model) -> {
            ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(ssr.resource + "=" + name)
                    .resolve(statementContext);
            crud.add(ssr.type, name, address, model, (n, a) -> reload());
        }).show();
    }
}
