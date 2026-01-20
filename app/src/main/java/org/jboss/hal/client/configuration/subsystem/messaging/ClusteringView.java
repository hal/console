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

import javax.annotation.PostConstruct;

import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Scope;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.elytron.CredentialReference;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.BRIDGE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SELECTED_SERVER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.Ids.MESSAGING_SERVER;

@MbuiView
@SuppressWarnings({ "DuplicateStringLiteralInspection", "HardCodedStringLiteral", "unused", "WeakerAccess" })
public abstract class ClusteringView extends MbuiViewImpl<ClusteringPresenter>
        implements ClusteringPresenter.MyView {

    private static final String EQ_WILDCARD = "=*";

    public static ClusteringView create(MbuiContext mbuiContext) {
        return new Mbui_ClusteringView(mbuiContext);
    }

    @MbuiElement("messaging-clustering-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("messaging-jgroups-broadcast-group-table") Table<NamedNode> jgroupsBroadcastGroupTable;
    @MbuiElement("messaging-socket-broadcast-group-table") Table<NamedNode> socketBroadcastGroupTable;
    @MbuiElement("messaging-jgroups-broadcast-group-form") Form<NamedNode> jgroupsBroadcastGroupForm;
    @MbuiElement("messaging-socket-broadcast-group-form") Form<NamedNode> socketBroadcastGroupForm;
    @MbuiElement("messaging-jgroups-discovery-group-table") Table<NamedNode> jgroupsDiscoveryGroupTable;
    @MbuiElement("messaging-socket-discovery-group-table") Table<NamedNode> socketDiscoveryGroupTable;
    @MbuiElement("messaging-jgroups-discovery-group-form") Form<NamedNode> jgroupsDiscoveryGroupForm;
    @MbuiElement("messaging-socket-discovery-group-form") Form<NamedNode> socketDiscoveryGroupForm;
    @MbuiElement("messaging-cluster-connection-table") Table<NamedNode> clusterConnectionTable;
    @MbuiElement("messaging-cluster-connection-form") Form<NamedNode> clusterConnectionForm;
    @MbuiElement("messaging-grouping-handler-table") Table<NamedNode> groupingHandlerTable;
    @MbuiElement("messaging-grouping-handler-form") Form<NamedNode> groupingHandlerForm;
    private Table<NamedNode> bridgeTable;
    private Form<NamedNode> bridgeForm;
    private CredentialReference cr;
    private Form<ModelNode> crForm;

    ClusteringView(MbuiContext mbuiContext) {
        super(mbuiContext);
        cr = new CredentialReference(mbuiContext.eventBus(), mbuiContext.dispatcher(), mbuiContext.ca(),
                mbuiContext.resources());
    }

    @PostConstruct
    void init() {
        Metadata metadata = mbuiContext.metadataRegistry().lookup(BRIDGE_TEMPLATE);
        crForm = cr.form(MESSAGING_SERVER, metadata, CREDENTIAL_REFERENCE, PASSWORD,
                () -> bridgeForm.<String> getFormItem(PASSWORD).getValue(),
                () -> presenter.bridgeAddress(bridgeTable.hasSelection() ? bridgeTable.selectedRow().getName() : null),
                () -> presenter.reload());

        bridgeTable = new ModelNodeTable.Builder<NamedNode>(Ids.build(MESSAGING_SERVER, BRIDGE, Ids.TABLE), metadata)
                .button(mbuiContext.resources().constants().add(),
                        table -> presenter.addBridge(ServerSubResource.BRIDGE),
                        Constraint.executable(BRIDGE_TEMPLATE, ADD))
                .button(mbuiContext.resources().constants().remove(),
                        table -> presenter.remove(ServerSubResource.BRIDGE, table.selectedRow()), Scope.SELECTED,
                        Constraint.executable(BRIDGE_TEMPLATE, REMOVE))
                .nameColumn()
                .build();

        bridgeForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(Ids.MESSAGING_BRIDGE, Ids.FORM), metadata)
                .onSave((form, changedValues) -> presenter.save(ServerSubResource.BRIDGE, form, changedValues))
                .prepareReset(form -> presenter.reset(ServerSubResource.BRIDGE, form))
                .build();
        bridgeForm.addFormValidation(
                new CredentialReference.AlternativeValidation<>(PASSWORD, () -> crForm.getModel(),
                        mbuiContext.resources()));

        Tabs tabs = new Tabs(Ids.build(MESSAGING_SERVER, BRIDGE, Ids.TAB_CONTAINER));
        tabs.add(Ids.build(MESSAGING_SERVER, BRIDGE, ATTRIBUTES, Ids.TAB),
                mbuiContext.resources().constants().attributes(), bridgeForm.element());
        tabs.add(Ids.build(MESSAGING_SERVER, BRIDGE, CREDENTIAL_REFERENCE, Ids.TAB),
                Names.CREDENTIAL_REFERENCE, crForm.element());

        HTMLElement bridgeSection = section()
                .add(h(1).textContent(Names.BRIDGE))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(bridgeTable)
                .add(tabs).element();

        registerAttachable(bridgeTable, bridgeForm, crForm);

        navigation.insertPrimary(Ids.build(MESSAGING_SERVER, BRIDGE, Ids.ITEM), null, Names.BRIDGE, "fa fa-road",
                bridgeSection);
    }

    @Override
    public void attach() {
        super.attach();

        bridgeTable.bindForm(bridgeForm);
        bridgeTable.onSelectionChange(t -> {
            if (t.hasSelection()) {
                crForm.view(failSafeGet(t.selectedRow(), CREDENTIAL_REFERENCE));
            } else {
                crForm.clear();
            }
        });
    }

    @Override
    public void setPresenter(ClusteringPresenter presenter) {
        super.setPresenter(presenter);

        // register the suggestion handlers here rather than in a @PostConstruct method
        // they need a valid presenter reference!
        List<AddressTemplate> templates = asList(
                SELECTED_SERVER_TEMPLATE.append(CONNECTOR + EQ_WILDCARD),
                SELECTED_SERVER_TEMPLATE.append(IN_VM_CONNECTOR + EQ_WILDCARD),
                SELECTED_SERVER_TEMPLATE.append(HTTP_CONNECTOR + EQ_WILDCARD),
                SELECTED_SERVER_TEMPLATE.append(REMOTE_CONNECTOR + EQ_WILDCARD));

        jgroupsBroadcastGroupForm.getFormItem(CONNECTORS).registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), presenter.statementContext, templates));
        socketBroadcastGroupForm.getFormItem(CONNECTORS).registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), presenter.statementContext, templates));

        clusterConnectionForm.getFormItem(CONNECTOR_NAME).registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), presenter.statementContext, templates));
        clusterConnectionForm.getFormItem(STATIC_CONNECTORS).registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), presenter.statementContext, templates));
        clusterConnectionForm.getFormItem(DISCOVERY_GROUP).registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), presenter.statementContext, asList(
                        SELECTED_SERVER_TEMPLATE.append(JGROUPS_DISCOVERY_GROUP + EQ_WILDCARD),
                        SELECTED_SERVER_TEMPLATE.append(SOCKET_DISCOVERY_GROUP + EQ_WILDCARD))));

        bridgeForm.getFormItem(DISCOVERY_GROUP).registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), presenter.statementContext, asList(
                        SELECTED_SERVER_TEMPLATE.append(JGROUPS_DISCOVERY_GROUP + EQ_WILDCARD),
                        SELECTED_SERVER_TEMPLATE.append(SOCKET_DISCOVERY_GROUP + EQ_WILDCARD))));
        bridgeForm.getFormItem(STATIC_CONNECTORS).registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), presenter.statementContext, templates));
    }

    @Override
    public void updateJGroupsBroadcastGroup(List<NamedNode> broadcastGroups) {
        jgroupsBroadcastGroupForm.clear();
        jgroupsBroadcastGroupTable.update(broadcastGroups);
    }

    @Override
    public void updateJGroupsDiscoveryGroup(List<NamedNode> discoveryGroups) {
        jgroupsDiscoveryGroupForm.clear();
        jgroupsDiscoveryGroupTable.update(discoveryGroups);
    }

    @Override
    public void updateSocketBroadcastGroup(List<NamedNode> broadcastGroups) {
        socketBroadcastGroupForm.clear();
        socketBroadcastGroupTable.update(broadcastGroups);
    }

    @Override
    public void updateSocketDiscoveryGroup(List<NamedNode> discoveryGroups) {
        socketDiscoveryGroupForm.clear();
        socketDiscoveryGroupTable.update(discoveryGroups);
    }

    @Override
    public void updateClusterConnection(List<NamedNode> clusterConnections) {
        clusterConnectionForm.clear();
        clusterConnectionTable.update(clusterConnections);
    }

    @Override
    public void updateGroupingHandler(List<NamedNode> groupingHandlers) {
        groupingHandlerForm.clear();
        groupingHandlerTable.update(groupingHandlers);
    }

    @Override
    public void updateBridge(List<NamedNode> bridges) {
        crForm.clear();
        bridgeForm.clear();
        bridgeTable.update(bridges);
    }
}
