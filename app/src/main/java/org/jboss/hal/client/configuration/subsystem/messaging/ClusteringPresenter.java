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

import static java.util.Arrays.asList;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

public class ClusteringPresenter
        extends ServerSettingsPresenter<ClusteringPresenter.MyView, ClusteringPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @Requires({BRIDGE_ADDRESS,
            BROADCAST_GROUP_ADDRESS,
            CLUSTER_CONNECTION_ADDRESS,
            DISCOVERY_GROUP_ADDRESS,
            GROUPING_HANDLER_ADDRESS})
    @NameToken(NameTokens.MESSAGING_SERVER_CLUSTERING)
    public interface MyProxy extends ProxyPlace<ClusteringPresenter> {}

    public interface MyView extends MbuiView<ClusteringPresenter> {
        void updateBroadcastGroup(List<NamedNode> broadcastGroups);
        void updateDiscoveryGroup(List<NamedNode> discoveryGroups);
        void updateClusterConnection(List<NamedNode> clusterConnections);
        void updateGroupingHandler(List<NamedNode> groupingHandlers);
        void updateBridge(List<NamedNode> bridges);
    }
    // @formatter:on


    private final Dispatcher dispatcher;

    @Inject
    public ClusteringPresenter(
            final EventBus eventBus,
            final ClusteringPresenter.MyView view,
            final ClusteringPresenter.MyProxy myProxy,
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
        return finderPathFactory.configurationSubsystemPath(MESSAGING_ACTIVEMQ)
                .append(Ids.MESSAGING_CATEGORY, Ids.asId(Names.SERVER),
                        resources.constants().category(), Names.SERVER)
                .append(Ids.MESSAGING_SERVER_CONFIGURATION, Ids.messagingServer(serverName),
                        Names.SERVER, serverName)
                .append(Ids.MESSAGING_SERVER_SETTINGS, Ids.MESSAGING_SERVER_CLUSTERING,
                        resources.constants().settings(), Names.CLUSTERING);
    }

    @Override
    protected void reload() {
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.resolve(statementContext);
        crud.readChildren(address, asList(BROADCAST_GROUP, DISCOVERY_GROUP,
                CLUSTER_CONNECTION, GROUPING_HANDLER, BRIDGE),
                result -> {
                    getView().updateBroadcastGroup(asNamedNodes(result.step(0).get(RESULT).asPropertyList()));
                    getView().updateDiscoveryGroup(asNamedNodes(result.step(1).get(RESULT).asPropertyList()));
                    getView().updateClusterConnection(asNamedNodes(result.step(2).get(RESULT).asPropertyList()));
                    getView().updateGroupingHandler(asNamedNodes(result.step(3).get(RESULT).asPropertyList()));
                    getView().updateBridge(asNamedNodes(result.step(4).get(RESULT).asPropertyList()));
                });
    }

    void addClusterConnection(ServerSubResource ssr) {
        Metadata metadata = metadataRegistry.lookup(ssr.template);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(ssr.baseId, Ids.ADD_SUFFIX), metadata)
                .unboundFormItem(new NameItem(), 0)
                .fromRequestProperties()
                .requiredOnly()
                .build();

        List<AddressTemplate> templates = asList(
                SELECTED_SERVER_TEMPLATE.append(CONNECTOR + "=*"),
                SELECTED_SERVER_TEMPLATE.append(IN_VM_CONNECTOR + "=*"),
                SELECTED_SERVER_TEMPLATE.append(HTTP_CONNECTOR + "=*"),
                SELECTED_SERVER_TEMPLATE.append(REMOTE_CONNECTOR + "=*"));
        form.getFormItem(CONNECTOR_NAME).registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, statementContext, templates));

        new AddResourceDialog(resources.messages().addResourceTitle(ssr.type), form, (name, model) -> {
            ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(ssr.resource + "=" + name)
                    .resolve(statementContext);
            crud.add(ssr.type, name, address, model, (n, a) -> reload());
        }).show();
    }

    void addBridge(ServerSubResource ssr) {
        Metadata metadata = metadataRegistry.lookup(ssr.template);
        NameItem nameItem = new NameItem();
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(ssr.baseId, Ids.ADD_SUFFIX), metadata)
                .unboundFormItem(nameItem, 0)
                .fromRequestProperties()
                .include(QUEUE_NAME, DISCOVERY_GROUP, STATIC_CONNECTORS)
                .unsorted()
                .build();

        List<AddressTemplate> templates = asList(
                SELECTED_SERVER_TEMPLATE.append(CONNECTOR + "=*"),
                SELECTED_SERVER_TEMPLATE.append(IN_VM_CONNECTOR + "=*"),
                SELECTED_SERVER_TEMPLATE.append(HTTP_CONNECTOR + "=*"),
                SELECTED_SERVER_TEMPLATE.append(REMOTE_CONNECTOR + "=*"));
        form.getFormItem(DISCOVERY_GROUP).registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, statementContext,
                        SELECTED_SERVER_TEMPLATE.append(DISCOVERY_GROUP + "=*")));
        form.getFormItem(STATIC_CONNECTORS).registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, statementContext, templates));

        new AddResourceDialog(resources.messages().addResourceTitle(ssr.type), form, (name, model) -> {
            name = nameItem.getValue();
            ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(ssr.resource + "=" + name)
                    .resolve(statementContext);
            crud.add(ssr.type, name, address, model, (n, a) -> reload());
        }).show();
    }

    ResourceAddress bridgeAddress(final String bridgeName) {
        return bridgeName != null ? SELECTED_BRIDGE_TEMPLATE.resolve(statementContext, bridgeName) : null;
    }
}
