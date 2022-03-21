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

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import static java.util.Arrays.asList;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

public class RemoteActiveMQPresenter
        extends MbuiPresenter<RemoteActiveMQPresenter.MyView, RemoteActiveMQPresenter.MyProxy>
        implements SupportsExpertMode {

    private MetadataRegistry metadataRegistry;
    private final Dispatcher dispatcher;
    private CrudOperations crud;
    private FinderPathFactory finderPathFactory;
    protected final StatementContext statementContext;
    private Resources resources;

    @Inject
    public RemoteActiveMQPresenter(
            EventBus eventBus,
            RemoteActiveMQPresenter.MyView view,
            RemoteActiveMQPresenter.MyProxy myProxy,
            Finder finder,
            MetadataRegistry metadataRegistry,
            Dispatcher dispatcher,
            CrudOperations crud,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext,
            Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.metadataRegistry = metadataRegistry;
        this.dispatcher = dispatcher;
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return MESSAGING_SUBSYSTEM_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(MESSAGING_ACTIVEMQ)
                .append(Ids.MESSAGING_CATEGORY, Ids.MESSAGING_REMOTE_ACTIVEMQ,
                        resources.constants().category(), Names.CONNECTIONS);
    }

    @Override
    protected void reload() {
        ResourceAddress address = MESSAGING_SUBSYSTEM_TEMPLATE.resolve(statementContext);
        crud.readChildren(address, asList(CONNECTOR, IN_VM_CONNECTOR, HTTP_CONNECTOR, REMOTE_CONNECTOR, JGROUPS_DISCOVERY_GROUP,
                SOCKET_DISCOVERY_GROUP, CONNECTION_FACTORY, POOLED_CONNECTION_FACTORY, EXTERNAL_JMS_QUEUE,
                EXTERNAL_JMS_TOPIC),
                result -> {
                    int i = 0;
                    getView().updateConnector(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateInVmConnector(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateHttpConnector(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateRemoteConnector(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateJGroupsDiscoveryGroup(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateSocketDiscoveryGroup(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateConnectionFactory(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updatePooledConnectionFactory(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateExternalQueue(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateExternalTopic(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                });
    }

    void addHttpConnector() {
        Metadata metadata = metadataRegistry.lookup(HTTP_CONNECTOR_REMOTE_TEMPLATE);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(HTTP_CONNECTOR, Ids.ADD), metadata)
                .unboundFormItem(new NameItem(), 0)
                .fromRequestProperties()
                .requiredOnly()
                .build();
        form.getFormItem(SOCKET_BINDING).registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, statementContext,
                        AddressTemplate.of("/socket-binding-group=*/socket-binding=*")));

        new AddResourceDialog(Names.HTTP_CONNECTOR, form, (name, model) -> {
            crud.add(Names.HTTP_CONNECTOR, name, HTTP_CONNECTOR_REMOTE_TEMPLATE, model, (n, a) -> reload());
        }).show();
    }

    void addRemoteConnector() {
        Metadata metadata = metadataRegistry.lookup(REMOTE_CONNECTOR_REMOTE_TEMPLATE);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(REMOTE_CONNECTOR, Ids.ADD), metadata)
                .unboundFormItem(new NameItem(), 0)
                .fromRequestProperties()
                .requiredOnly()
                .build();
        form.getFormItem(SOCKET_BINDING).registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, statementContext,
                        AddressTemplate.of("/socket-binding-group=*/socket-binding=*")));

        new AddResourceDialog(Names.REMOTE_CONNECTOR, form, (name, model) -> {
            crud.add(Names.REMOTE_CONNECTOR, name, REMOTE_CONNECTOR_REMOTE_TEMPLATE, model, (n, a) -> reload());
        }).show();
    }

    void addConnectionFactory(RemoteActiveMQSubResource ssr) {
        Metadata metadata = metadataRegistry.lookup(ssr.template);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(ssr.baseId, Ids.ADD), metadata)
                .unboundFormItem(new NameItem(), 0)
                .fromRequestProperties()
                .include(ENTRIES, DISCOVERY_GROUP, CONNECTORS)
                .unsorted()
                .build();

        List<AddressTemplate> connectors = asList(CONNECTOR_REMOTE_TEMPLATE, IN_VM_CONNECTOR_REMOTE_TEMPLATE,
                HTTP_CONNECTOR_REMOTE_TEMPLATE, REMOTE_CONNECTOR_REMOTE_TEMPLATE);

        form.getFormItem(CONNECTORS).registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, statementContext, connectors));
        form.getFormItem(DISCOVERY_GROUP).registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, statementContext, asList(JGROUPS_DISCOVERY_GROUP_REMOTE_TEMPLATE,
                        SOCKET_DISCOVERY_GROUP_REMOTE_TEMPLATE)));

        new AddResourceDialog(resources.messages().addResourceTitle(ssr.type), form, (name, model) -> {
            ResourceAddress address = ssr.template.resolve(statementContext, name);
            crud.add(ssr.type, name, address, model, (n, a) -> reload());
        }).show();
    }

    ResourceAddress pooledConnectionFactoryAddress(String resource) {
        return resource != null ? POOLED_CONNECTION_FACTORY_REMOTE_TEMPLATE.resolve(statementContext, resource) : null;
    }

    void add(RemoteActiveMQSubResource ssr) {
        ssr.add(metadataRegistry, statementContext, crud, resources, (n, a) -> reload());
    }

    void save(RemoteActiveMQSubResource ssr, Form<NamedNode> form, Map<String, Object> changedValues) {
        ssr.save(form, changedValues, metadataRegistry, statementContext, crud, this::reload);
    }

    void reset(RemoteActiveMQSubResource ssr, Form<NamedNode> form) {
        ssr.reset(form, metadataRegistry, statementContext, crud, new Form.FinishReset<NamedNode>(form) {
            @Override
            public void afterReset(final Form<NamedNode> form) {
                reload();
            }
        });
    }

    void remove(RemoteActiveMQSubResource ssr, NamedNode item) {
        ssr.remove(item, statementContext, crud, this::reload);
    }

    // @formatter:off
    @ProxyCodeSplit
    @Requires({ CONNECTOR_REMOTE_ADDRESS, IN_VM_CONNECTOR_REMOTE_ADDRESS, HTTP_CONNECTOR_REMOTE_ADDRESS,
            REMOTE_CONNECTOR_REMOTE_ADDRESS, JGROUPS_DISCOVERY_GROUP_REMOTE_ADDRESS, SOCKET_DISCOVERY_GROUP_REMOTE_ADDRESS,
            CONNECTION_FACTORY_REMOTE_ADDRESS, POOLED_CONNECTION_FACTORY_REMOTE_ADDRESS, EXTERNAL_JMS_QUEUE_ADDRESS,
            EXTERNAL_JMS_TOPIC_ADDRESS
    })
    @NameToken(NameTokens.MESSAGING_REMOTE_ACTIVEMQ)
    public interface MyProxy extends ProxyPlace<RemoteActiveMQPresenter> {
    }

    public interface MyView extends MbuiView<RemoteActiveMQPresenter> {
        void updateConnector(List<NamedNode> connectors);

        void updateInVmConnector(List<NamedNode> inVmConnectors);

        void updateHttpConnector(List<NamedNode> httpConnectors);

        void updateRemoteConnector(List<NamedNode> remoteConnectors);

        void updateSocketDiscoveryGroup(List<NamedNode> connectorServices);

        void updateJGroupsDiscoveryGroup(List<NamedNode> connectorServices);

        void updateConnectionFactory(List<NamedNode> connectionFactories);

        void updatePooledConnectionFactory(List<NamedNode> pooledConnectionFactories);

        void updateExternalQueue(List<NamedNode> nodes);

        void updateExternalTopic(List<NamedNode> nodes);
    }
    // @formatter:on
}
