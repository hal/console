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

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static java.util.Arrays.asList;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SELECTED_SERVER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral"})
public abstract class ClusteringView extends MbuiViewImpl<ClusteringPresenter>
        implements ClusteringPresenter.MyView {

    public static ClusteringView create(final MbuiContext mbuiContext) {
        return new Mbui_ClusteringView(mbuiContext);
    }

    @MbuiElement("messaging-clustering-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("messaging-broadcast-group-table") NamedNodeTable<NamedNode> broadcastGroupTable;
    @MbuiElement("messaging-broadcast-group-form") Form<NamedNode> broadcastGroupForm;
    @MbuiElement("messaging-discovery-group-table") NamedNodeTable<NamedNode> discoveryGroupTable;
    @MbuiElement("messaging-discovery-group-form") Form<NamedNode> discoveryGroupForm;
    @MbuiElement("messaging-cluster-connection-table") NamedNodeTable<NamedNode> clusterConnectionTable;
    @MbuiElement("messaging-cluster-connection-form") Form<NamedNode> clusterConnectionForm;
    @MbuiElement("messaging-grouping-handler-table") NamedNodeTable<NamedNode> groupingHandlerTable;
    @MbuiElement("messaging-grouping-handler-form") Form<NamedNode> groupingHandlerForm;
    @MbuiElement("messaging-bridge-table") NamedNodeTable<NamedNode> bridgeTable;
    @MbuiElement("messaging-bridge-form") Form<NamedNode> bridgeForm;

    ClusteringView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @Override
    public void setPresenter(final ClusteringPresenter presenter) {
        super.setPresenter(presenter);

        // register the suggestion handlers here rather than in a @PostConstruct method
        // they need a valid presenter reference!
        List<AddressTemplate> templates = asList(
                SELECTED_SERVER_TEMPLATE.append(CONNECTOR + "=*"),
                SELECTED_SERVER_TEMPLATE.append(IN_VM_CONNECTOR + "=*"),
                SELECTED_SERVER_TEMPLATE.append(HTTP_CONNECTOR + "=*"),
                SELECTED_SERVER_TEMPLATE.append(REMOTE_CONNECTOR + "=*"));

        broadcastGroupForm.getFormItem(CONNECTORS).registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), presenter.statementContext, templates));

        clusterConnectionForm.getFormItem(CONNECTOR_NAME).registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), presenter.statementContext, templates));
        clusterConnectionForm.getFormItem(STATIC_CONNECTORS).registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), presenter.statementContext, templates));
        clusterConnectionForm.getFormItem(DISCOVERY_GROUP).registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), presenter.statementContext,
                        SELECTED_SERVER_TEMPLATE.append(DISCOVERY_GROUP + "=*")));

        bridgeForm.getFormItem(DISCOVERY_GROUP).registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), presenter.statementContext,
                        SELECTED_SERVER_TEMPLATE.append(DISCOVERY_GROUP + "=*")));
        bridgeForm.getFormItem(STATIC_CONNECTORS).registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), presenter.statementContext, templates));
    }

    @Override
    public void updateBroadcastGroup(final List<NamedNode> broadcastGroups) {
        broadcastGroupForm.clear();
        broadcastGroupTable.update(broadcastGroups);
    }

    @Override
    public void updateDiscoveryGroup(final List<NamedNode> discoveryGroups) {
        discoveryGroupForm.clear();
        discoveryGroupTable.update(discoveryGroups);
    }

    @Override
    public void updateClusterConnection(final List<NamedNode> clusterConnections) {
        clusterConnectionForm.clear();
        clusterConnectionTable.update(clusterConnections);
    }

    @Override
    public void updateGroupingHandler(final List<NamedNode> groupingHandlers) {
        groupingHandlerForm.clear();
        groupingHandlerTable.update(groupingHandlers);
    }

    @Override
    public void updateBridge(final List<NamedNode> bridges) {
        bridgeForm.clear();
        bridgeTable.update(bridges);
    }
}
