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
import javax.annotation.PostConstruct;

import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Scope;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.subsystem.elytron.CredentialReference;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.BRIDGE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SELECTED_SERVER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.Ids.ENTRY_SUFFIX;
import static org.jboss.hal.resources.Ids.MESSAGING_SERVER;
import static org.jboss.hal.resources.Ids.TABLE_SUFFIX;

/**
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral", "unused", "WeakerAccess"})
public abstract class ClusteringView extends MbuiViewImpl<ClusteringPresenter>
        implements ClusteringPresenter.MyView {

    public static ClusteringView create(final MbuiContext mbuiContext) {
        return new Mbui_ClusteringView(mbuiContext);
    }

    @MbuiElement("messaging-clustering-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("messaging-broadcast-group-table") Table<NamedNode> broadcastGroupTable;
    @MbuiElement("messaging-broadcast-group-form") Form<NamedNode> broadcastGroupForm;
    @MbuiElement("messaging-discovery-group-table") Table<NamedNode> discoveryGroupTable;
    @MbuiElement("messaging-discovery-group-form") Form<NamedNode> discoveryGroupForm;
    @MbuiElement("messaging-cluster-connection-table") Table<NamedNode> clusterConnectionTable;
    @MbuiElement("messaging-cluster-connection-form") Form<NamedNode> clusterConnectionForm;
    @MbuiElement("messaging-grouping-handler-table") Table<NamedNode> groupingHandlerTable;
    @MbuiElement("messaging-grouping-handler-form") Form<NamedNode> groupingHandlerForm;
    //@MbuiElement("messaging-bridge-table") Table<NamedNode> bridgeTable;
    //@MbuiElement("messaging-bridge-form") Form<NamedNode> bridgeForm;
    private Table<NamedNode> bridgeTable;
    private Form<NamedNode> bridgeForm;
    private CredentialReference cr;
    private Form<ModelNode> crForm;

    ClusteringView(final MbuiContext mbuiContext) {
        super(mbuiContext);
        cr = new CredentialReference(mbuiContext.eventBus(), mbuiContext.dispatcher(), mbuiContext.ca(),
                mbuiContext.resources());
    }

    @PostConstruct
    void init() {

        Metadata metadata = mbuiContext.metadataRegistry().lookup(BRIDGE_TEMPLATE);
        crForm = cr.form(MESSAGING_SERVER, metadata, CREDENTIAL_REFERENCE, PASSWORD,
                () -> bridgeForm.<String>getFormItem(PASSWORD).getValue(),
                () -> presenter.resourceAddress(),
                () -> presenter.reload());

        bridgeTable = new ModelNodeTable.Builder<NamedNode>(Ids.build(MESSAGING_SERVER, BRIDGE, TABLE_SUFFIX), metadata)
                .button(mbuiContext.resources().constants().add(),
                        table -> presenter.addBridge(ServerSubResource.BRIDGE),
                        Constraint.executable(BRIDGE_TEMPLATE, ADD))
                .button(mbuiContext.resources().constants().remove(),
                        table -> presenter.remove(ServerSubResource.BRIDGE, table.selectedRow()), Scope.SELECTED,
                        Constraint.executable(BRIDGE_TEMPLATE, REMOVE))
                .column(NAME, (cell, type, row, meta) -> row.getName())
                .build();

        bridgeForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(Ids.MESSAGING_BRIDGE, Ids.FORM_SUFFIX), metadata)
                .onSave((form, changedValues) -> presenter.save(ServerSubResource.BRIDGE, form, changedValues))
                .prepareReset(form -> presenter.reset(ServerSubResource.BRIDGE, form))
                .build();

        Tabs tabs = new Tabs();
        tabs.add(Ids.build(MESSAGING_SERVER, BRIDGE, ATTRIBUTES, Ids.TAB_SUFFIX),
                mbuiContext.resources().constants().attributes(), bridgeForm.asElement());
        tabs.add(Ids.build(MESSAGING_SERVER, BRIDGE, CREDENTIAL_REFERENCE, Ids.TAB_SUFFIX),
                Names.CREDENTIAL_REFERENCE, crForm.asElement());

        HTMLElement bridgeSection = section()
                .add(h(1).textContent(Names.BRIDGE))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(bridgeTable)
                .add(tabs)
                .asElement();

        registerAttachable(bridgeTable, bridgeForm, crForm);

        navigation.insertPrimary(Ids.build(MESSAGING_SERVER, BRIDGE, ENTRY_SUFFIX), null, Names.BRIDGE, "fa fa-road", bridgeSection);
    }

    @Override
    public void attach() {
        super.attach();

        bridgeTable.bindForm(bridgeForm);
        bridgeTable.onSelectionChange(t -> {
            if (t.hasSelection()) {
                crForm.view(failSafeGet(t.selectedRow(), CREDENTIAL_REFERENCE));
            }
        });
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
        crForm.clear();
        bridgeForm.clear();
        bridgeTable.update(bridges);
    }
}
