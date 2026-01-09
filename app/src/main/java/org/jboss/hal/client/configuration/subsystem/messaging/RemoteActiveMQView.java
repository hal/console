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
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.resources.Ids.MESSAGING_REMOTE_ACTIVEMQ;

@MbuiView
public abstract class RemoteActiveMQView extends MbuiViewImpl<RemoteActiveMQPresenter>
        implements RemoteActiveMQPresenter.MyView {

    public static RemoteActiveMQView create(MbuiContext mbuiContext) {
        return new Mbui_RemoteActiveMQView(mbuiContext);
    }

    @MbuiElement("msg-remote-connection-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("msg-remote-connector-table") Table<NamedNode> connectorTable;
    @MbuiElement("msg-remote-connector-form") Form<NamedNode> connectorForm;
    @MbuiElement("msg-remote-in-vm-connector-table") Table<NamedNode> inVmConnectorTable;
    @MbuiElement("msg-remote-in-vm-connector-form") Form<NamedNode> inVmConnectorForm;
    @MbuiElement("msg-remote-http-connector-table") Table<NamedNode> httpConnectorTable;
    @MbuiElement("msg-remote-http-connector-form") Form<NamedNode> httpConnectorForm;
    @MbuiElement("msg-remote-remote-connector-table") Table<NamedNode> remoteConnectorTable;
    @MbuiElement("msg-remote-remote-connector-form") Form<NamedNode> remoteConnectorForm;
    @MbuiElement("msg-remote-socket-discovery-group-table") Table<NamedNode> socketDiscoveryGroupTable;
    @MbuiElement("msg-remote-jgroups-discovery-group-table") Table<NamedNode> jgroupsDiscoveryGroupTable;
    @MbuiElement("msg-remote-socket-discovery-group-form") Form<NamedNode> socketDiscoveryGroupForm;
    @MbuiElement("msg-remote-jgroups-discovery-group-form") Form<NamedNode> jgroupsDiscoveryGroupForm;
    @MbuiElement("msg-remote-connection-factory-table") Table<NamedNode> connectionFactoryTable;
    @MbuiElement("msg-remote-connection-factory-form") Form<NamedNode> connectionFactoryForm;
    @MbuiElement("msg-remote-external-queue-table") Table<NamedNode> externalQueueTable;
    @MbuiElement("msg-remote-external-queue-form") Form<NamedNode> externalQueueForm;
    @MbuiElement("msg-remote-external-topic-table") Table<NamedNode> externalTopicTable;
    @MbuiElement("msg-remote-external-topic-form") Form<NamedNode> externalTopicForm;
    private Table<NamedNode> pooledConnectionFactoryTable;
    private Form<NamedNode> pooledConnectionFactoryForm;
    private CredentialReference cr;
    private Form<ModelNode> crForm;

    RemoteActiveMQView(MbuiContext mbuiContext) {
        super(mbuiContext);
        cr = new CredentialReference(mbuiContext.eventBus(), mbuiContext.dispatcher(), mbuiContext.ca(),
                mbuiContext.resources());
    }

    @PostConstruct
    void init() {
        Metadata metadata = mbuiContext.metadataRegistry().lookup(POOLED_CONNECTION_FACTORY_REMOTE_TEMPLATE);
        crForm = cr.form(Ids.MESSAGING_REMOTE_ACTIVEMQ, metadata, CREDENTIAL_REFERENCE, PASSWORD,
                () -> pooledConnectionFactoryForm.<String> getFormItem(PASSWORD).getValue(),
                () -> presenter.pooledConnectionFactoryAddress(
                        pooledConnectionFactoryTable.hasSelection() ? pooledConnectionFactoryTable.selectedRow()
                                .getName() : null),
                () -> presenter.reload());

        pooledConnectionFactoryTable = new ModelNodeTable.Builder<NamedNode>(
                Ids.build(MESSAGING_REMOTE_ACTIVEMQ, POOLED_CONNECTION_FACTORY, Ids.TABLE), metadata)
                .button(mbuiContext.resources().constants().add(),
                        table -> presenter.addConnectionFactory(RemoteActiveMQSubResource.POOLED_CONNECTION_FACTORY),
                        Constraint.executable(POOLED_CONNECTION_FACTORY_TEMPLATE, ADD))
                .button(mbuiContext.resources().constants().remove(),
                        table -> presenter.remove(RemoteActiveMQSubResource.POOLED_CONNECTION_FACTORY,
                                table.selectedRow()),
                        Scope.SELECTED,
                        Constraint.executable(POOLED_CONNECTION_FACTORY_TEMPLATE, REMOVE))
                .nameColumn()
                .build();

        pooledConnectionFactoryForm = new ModelNodeForm.Builder<NamedNode>(
                Ids.build(Ids.MESSAGING_POOLED_CONNECTION_FACTORY, Ids.FORM), metadata)
                .onSave((form, changedValues) -> presenter
                        .save(RemoteActiveMQSubResource.POOLED_CONNECTION_FACTORY, form, changedValues))
                .prepareReset(form -> presenter.reset(RemoteActiveMQSubResource.POOLED_CONNECTION_FACTORY, form))
                .build();
        pooledConnectionFactoryForm.addFormValidation(
                new CredentialReference.AlternativeValidation<>(PASSWORD, () -> crForm.getModel(),
                        mbuiContext.resources()));

        Tabs tabs = new Tabs(Ids.build(Ids.MESSAGING_REMOTE_ACTIVEMQ, POOLED_CONNECTION_FACTORY, Ids.TAB_CONTAINER));
        tabs.add(Ids.build(Ids.MESSAGING_REMOTE_ACTIVEMQ, POOLED_CONNECTION_FACTORY, ATTRIBUTES, Ids.TAB),
                mbuiContext.resources().constants().attributes(), pooledConnectionFactoryForm.element());
        tabs.add(Ids.build(Ids.MESSAGING_REMOTE_ACTIVEMQ, POOLED_CONNECTION_FACTORY, CREDENTIAL_REFERENCE, Ids.TAB),
                Names.CREDENTIAL_REFERENCE, crForm.element());

        HTMLElement htmlSection = section()
                .add(h(1).textContent(Names.POOLED_CONNECTION_FACTORY))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(pooledConnectionFactoryTable)
                .add(tabs).element();

        registerAttachable(pooledConnectionFactoryTable, pooledConnectionFactoryForm, crForm);

        String primId = Ids.build(MESSAGING_REMOTE_ACTIVEMQ, POOLED_CONNECTION_FACTORY, Ids.ITEM);
        navigation.insertPrimary(primId, "msg-remote-external-queue-item", Names.POOLED_CONNECTION_FACTORY,
                pfIcon("replicator"), htmlSection);

    }

    @Override
    public void attach() {
        super.attach();

        pooledConnectionFactoryTable.bindForm(pooledConnectionFactoryForm);
        pooledConnectionFactoryTable.onSelectionChange(t -> {
            if (t.hasSelection()) {
                crForm.view(failSafeGet(t.selectedRow(), CREDENTIAL_REFERENCE));
            }
        });
    }

    @Override
    public void setPresenter(RemoteActiveMQPresenter presenter) {
        super.setPresenter(presenter);

        // register the suggestion handlers here rather than in a @PostConstruct method
        // they need a valid presenter reference!
        List<AddressTemplate> connectors = asList(CONNECTOR_REMOTE_TEMPLATE, IN_VM_CONNECTOR_REMOTE_TEMPLATE,
                HTTP_CONNECTOR_REMOTE_TEMPLATE, REMOTE_CONNECTOR_REMOTE_TEMPLATE);

        connectionFactoryForm.getFormItem(CONNECTORS).registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), presenter.statementContext, connectors));
        connectionFactoryForm.getFormItem(DISCOVERY_GROUP).registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), presenter.statementContext,
                        asList(JGROUPS_DISCOVERY_GROUP_REMOTE_TEMPLATE, SOCKET_DISCOVERY_GROUP_REMOTE_TEMPLATE)));

        pooledConnectionFactoryForm.getFormItem(CONNECTORS).registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), presenter.statementContext, connectors));
        pooledConnectionFactoryForm.getFormItem(DISCOVERY_GROUP).registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), presenter.statementContext,
                        asList(JGROUPS_DISCOVERY_GROUP_REMOTE_TEMPLATE, SOCKET_DISCOVERY_GROUP_REMOTE_TEMPLATE)));

        connectorForm.getFormItem(SOCKET_BINDING).registerSuggestHandler(new ReadChildrenAutoComplete(
                mbuiContext.dispatcher(), statementContext(), SOCKET_BINDING_TEMPLATE));

        socketDiscoveryGroupForm.getFormItem(SOCKET_BINDING).registerSuggestHandler(new ReadChildrenAutoComplete(
                mbuiContext.dispatcher(), statementContext(), SOCKET_BINDING_TEMPLATE));

        remoteConnectorForm.getFormItem(SOCKET_BINDING).registerSuggestHandler(new ReadChildrenAutoComplete(
                mbuiContext.dispatcher(), statementContext(), SOCKET_BINDING_TEMPLATE));

        httpConnectorForm.getFormItem(SOCKET_BINDING).registerSuggestHandler(new ReadChildrenAutoComplete(
                mbuiContext.dispatcher(), statementContext(), SOCKET_BINDING_TEMPLATE));
    }

    @Override
    public void updateConnector(List<NamedNode> connectors) {
        connectorForm.clear();
        connectorTable.update(connectors);
        navigation.updateBadge("msg-remote-connector-item", connectors.size());
    }

    @Override
    public void updateInVmConnector(List<NamedNode> inVmConnectors) {
        inVmConnectorForm.clear();
        inVmConnectorTable.update(inVmConnectors);
        navigation.updateBadge("msg-remote-in-vm-connector-item", inVmConnectors.size());
    }

    @Override
    public void updateHttpConnector(List<NamedNode> httpConnectors) {
        httpConnectorForm.clear();
        httpConnectorTable.update(httpConnectors);
        navigation.updateBadge("msg-remote-http-connector-item", httpConnectors.size());
    }

    @Override
    public void updateRemoteConnector(List<NamedNode> remoteConnectors) {
        remoteConnectorForm.clear();
        remoteConnectorTable.update(remoteConnectors);
        navigation.updateBadge("msg-remote-remote-connector-item", remoteConnectors.size());
    }

    @Override
    public void updateSocketDiscoveryGroup(List<NamedNode> nodes) {
        socketDiscoveryGroupForm.clear();
        socketDiscoveryGroupTable.update(nodes);
    }

    @Override
    public void updateJGroupsDiscoveryGroup(List<NamedNode> nodes) {
        jgroupsDiscoveryGroupForm.clear();
        jgroupsDiscoveryGroupTable.update(nodes);
    }

    @Override
    public void updateConnectionFactory(List<NamedNode> connectionFactories) {
        connectionFactoryForm.clear();
        connectionFactoryTable.update(connectionFactories);
    }

    @Override
    public void updatePooledConnectionFactory(List<NamedNode> pooledConnectionFactories) {
        crForm.clear();
        pooledConnectionFactoryForm.clear();
        pooledConnectionFactoryTable.update(pooledConnectionFactories);
    }

    @Override
    public void updateExternalQueue(List<NamedNode> nodes) {
        externalQueueForm.clear();
        externalQueueTable.update(nodes);
    }

    @Override
    public void updateExternalTopic(List<NamedNode> nodes) {
        externalTopicForm.clear();
        externalTopicTable.update(nodes);
    }
}
