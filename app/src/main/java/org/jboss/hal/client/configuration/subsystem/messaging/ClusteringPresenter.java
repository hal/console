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
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.ResourceAddress;
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
public class ClusteringPresenter
        extends ServerSettingsPresenter<ClusteringPresenter.MyView, ClusteringPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    // TODO Replace with
    // TODO value = {BROADCAST_GROUP_ADDRESS, DISCOVERY_GROUP_ADDRESS,
    // TODO         CLUSTER_CONNECTION_ADDRESS, GROUPING_HANDLER_ADDRESS,
    // TODO         BRIDGE_ADDRESS}
    // TODO once WFCORE-2022 is resolved
    @Requires(value = SERVER_ADDRESS)
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


    private final Resources resources;

    @Inject
    public ClusteringPresenter(
            final EventBus eventBus,
            final ClusteringPresenter.MyView view,
            final ClusteringPresenter.MyProxy myProxy,
            final Finder finder,
            final MetadataRegistry metadataRegistry,
            final CrudOperations crud,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            final Resources resources) {
        super(eventBus, view, myProxy, finder, crud, metadataRegistry, finderPathFactory, statementContext);
        this.resources = resources;
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

    // TODO Add suggestion handlers for add dialogs
}
