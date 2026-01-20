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
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.POOLED_CONNECTION_FACTORY_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SELECTED_SERVER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.Ids.MESSAGING_SERVER;

@MbuiView
@SuppressWarnings({ "DuplicateStringLiteralInspection", "HardCodedStringLiteral", "WeakerAccess" })
public abstract class ConnectionView extends MbuiViewImpl<ConnectionPresenter>
        implements ConnectionPresenter.MyView {

    public static final String EQ_WILDCARD = "=*";

    public static ConnectionView create(MbuiContext mbuiContext) {
        return new Mbui_ConnectionView(mbuiContext);
    }

    @MbuiElement("messaging-connection-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("messaging-acceptor-table") Table<NamedNode> acceptorTable;
    @MbuiElement("messaging-acceptor-form") Form<NamedNode> acceptorForm;
    @MbuiElement("messaging-in-vm-acceptor-table") Table<NamedNode> inVmAcceptorTable;
    @MbuiElement("messaging-in-vm-acceptor-form") Form<NamedNode> inVmAcceptorForm;
    @MbuiElement("messaging-http-acceptor-table") Table<NamedNode> httpAcceptorTable;
    @MbuiElement("messaging-http-acceptor-form") Form<NamedNode> httpAcceptorForm;
    @MbuiElement("messaging-remote-acceptor-table") Table<NamedNode> remoteAcceptorTable;
    @MbuiElement("messaging-remote-acceptor-form") Form<NamedNode> remoteAcceptorForm;
    @MbuiElement("messaging-connector-table") Table<NamedNode> connectorTable;
    @MbuiElement("messaging-connector-form") Form<NamedNode> connectorForm;
    @MbuiElement("messaging-in-vm-connector-table") Table<NamedNode> inVmConnectorTable;
    @MbuiElement("messaging-in-vm-connector-form") Form<NamedNode> inVmConnectorForm;
    @MbuiElement("messaging-http-connector-table") Table<NamedNode> httpConnectorTable;
    @MbuiElement("messaging-http-connector-form") Form<NamedNode> httpConnectorForm;
    @MbuiElement("messaging-remote-connector-table") Table<NamedNode> remoteConnectorTable;
    @MbuiElement("messaging-remote-connector-form") Form<NamedNode> remoteConnectorForm;
    @MbuiElement("messaging-connector-service-table") Table<NamedNode> connectorServiceTable;
    @MbuiElement("messaging-connector-service-form") Form<NamedNode> connectorServiceForm;
    @MbuiElement("messaging-connection-factory-table") Table<NamedNode> connectionFactoryTable;
    @MbuiElement("messaging-connection-factory-form") Form<NamedNode> connectionFactoryForm;
    private Table<NamedNode> pooledConnectionFactoryTable;
    private Form<NamedNode> pooledConnectionFactoryForm;
    private CredentialReference cr;
    private Form<ModelNode> crForm;

    ConnectionView(MbuiContext mbuiContext) {
        super(mbuiContext);
        cr = new CredentialReference(mbuiContext.eventBus(), mbuiContext.dispatcher(), mbuiContext.ca(),
                mbuiContext.resources());
    }

    @PostConstruct
    void init() {
        Metadata metadata = mbuiContext.metadataRegistry().lookup(POOLED_CONNECTION_FACTORY_TEMPLATE);
        crForm = cr.form(Ids.MESSAGING_SERVER, metadata, CREDENTIAL_REFERENCE, PASSWORD,
                () -> pooledConnectionFactoryForm.<String> getFormItem(PASSWORD).getValue(),
                () -> presenter.pooledConnectionFactoryAddress(
                        pooledConnectionFactoryTable.hasSelection() ? pooledConnectionFactoryTable.selectedRow()
                                .getName() : null),
                () -> presenter.reload());

        pooledConnectionFactoryTable = new ModelNodeTable.Builder<NamedNode>(
                Ids.build(MESSAGING_SERVER, POOLED_CONNECTION_FACTORY, Ids.TABLE), metadata)
                .button(mbuiContext.resources().constants().add(),
                        table -> presenter.addPooledConnectionFactory(ServerSubResource.POOLED_CONNECTION_FACTORY),
                        Constraint.executable(POOLED_CONNECTION_FACTORY_TEMPLATE, ADD))
                .button(mbuiContext.resources().constants().remove(),
                        table -> presenter.remove(ServerSubResource.POOLED_CONNECTION_FACTORY, table.selectedRow()),
                        Scope.SELECTED,
                        Constraint.executable(POOLED_CONNECTION_FACTORY_TEMPLATE, REMOVE))
                .nameColumn()
                .build();

        pooledConnectionFactoryForm = new ModelNodeForm.Builder<NamedNode>(
                Ids.build(Ids.MESSAGING_POOLED_CONNECTION_FACTORY, Ids.FORM), metadata)
                .onSave((form, changedValues) -> presenter
                        .save(ServerSubResource.POOLED_CONNECTION_FACTORY, form, changedValues))
                .prepareReset(form -> presenter.reset(ServerSubResource.POOLED_CONNECTION_FACTORY, form))
                .build();
        pooledConnectionFactoryForm.addFormValidation(
                new CredentialReference.AlternativeValidation<>(PASSWORD, () -> crForm.getModel(),
                        mbuiContext.resources()));

        Tabs tabs = new Tabs(Ids.build(Ids.MESSAGING_SERVER, POOLED_CONNECTION_FACTORY, Ids.TAB_CONTAINER));
        tabs.add(Ids.build(Ids.MESSAGING_SERVER, POOLED_CONNECTION_FACTORY, ATTRIBUTES, Ids.TAB),
                mbuiContext.resources().constants().attributes(), pooledConnectionFactoryForm.element());
        tabs.add(Ids.build(Ids.MESSAGING_SERVER, POOLED_CONNECTION_FACTORY, CREDENTIAL_REFERENCE, Ids.TAB),
                Names.CREDENTIAL_REFERENCE, crForm.element());

        HTMLElement htmlSection = section()
                .add(h(1).textContent(Names.POOLED_CONNECTION_FACTORY))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(pooledConnectionFactoryTable)
                .add(tabs).element();

        registerAttachable(pooledConnectionFactoryTable, pooledConnectionFactoryForm, crForm);

        navigation.insertPrimary(Ids.build(MESSAGING_SERVER, POOLED_CONNECTION_FACTORY, Ids.ITEM), null,
                Names.POOLED_CONNECTION_FACTORY, "pficon pficon-replicator", htmlSection);

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
    public void setPresenter(ConnectionPresenter presenter) {
        super.setPresenter(presenter);

        // register the suggestion handlers here rather than in a @PostConstruct method
        // they need a valid presenter reference!
        List<AddressTemplate> templates = asList(
                SELECTED_SERVER_TEMPLATE.append(CONNECTOR + EQ_WILDCARD),
                SELECTED_SERVER_TEMPLATE.append(IN_VM_CONNECTOR + EQ_WILDCARD),
                SELECTED_SERVER_TEMPLATE.append(HTTP_CONNECTOR + EQ_WILDCARD),
                SELECTED_SERVER_TEMPLATE.append(REMOTE_CONNECTOR + EQ_WILDCARD));

        connectionFactoryForm.getFormItem(CONNECTORS).registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), presenter.statementContext, templates));
        pooledConnectionFactoryForm.getFormItem(CONNECTORS).registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), presenter.statementContext, templates));

        connectionFactoryForm.getFormItem(DISCOVERY_GROUP).registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), presenter.statementContext, asList(
                        SELECTED_SERVER_TEMPLATE.append(JGROUPS_DISCOVERY_GROUP + EQ_WILDCARD),
                        SELECTED_SERVER_TEMPLATE.append(SOCKET_DISCOVERY_GROUP + EQ_WILDCARD))));
        pooledConnectionFactoryForm.getFormItem(DISCOVERY_GROUP).registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), presenter.statementContext, asList(
                        SELECTED_SERVER_TEMPLATE.append(JGROUPS_DISCOVERY_GROUP + EQ_WILDCARD),
                        SELECTED_SERVER_TEMPLATE.append(SOCKET_DISCOVERY_GROUP + EQ_WILDCARD))));
    }

    @Override
    public void updateAcceptor(List<NamedNode> acceptors) {
        acceptorForm.clear();
        acceptorTable.update(acceptors);
        navigation.updateBadge("messaging-acceptor-item", acceptors.size());
    }

    @Override
    public void updateInVmAcceptor(List<NamedNode> inVmAcceptors) {
        inVmAcceptorForm.clear();
        inVmAcceptorTable.update(inVmAcceptors);
        navigation.updateBadge("messaging-in-vm-acceptor-item", inVmAcceptors.size());
    }

    @Override
    public void updateHttpAcceptor(List<NamedNode> httpAcceptors) {
        httpAcceptorForm.clear();
        httpAcceptorTable.update(httpAcceptors);
        navigation.updateBadge("messaging-http-acceptor-item", httpAcceptors.size());
    }

    @Override
    public void updateRemoteAcceptor(List<NamedNode> remoteAcceptors) {
        remoteAcceptorForm.clear();
        remoteAcceptorTable.update(remoteAcceptors);
        navigation.updateBadge("messaging-remote-acceptor-item", remoteAcceptors.size());
    }

    @Override
    public void updateConnector(List<NamedNode> connectors) {
        connectorForm.clear();
        connectorTable.update(connectors);
        navigation.updateBadge("messaging-connector-item", connectors.size());
    }

    @Override
    public void updateInVmConnector(List<NamedNode> inVmConnectors) {
        inVmConnectorForm.clear();
        inVmConnectorTable.update(inVmConnectors);
        navigation.updateBadge("messaging-in-vm-connector-item", inVmConnectors.size());
    }

    @Override
    public void updateHttpConnector(List<NamedNode> httpConnectors) {
        httpConnectorForm.clear();
        httpConnectorTable.update(httpConnectors);
        navigation.updateBadge("messaging-http-connector-item", httpConnectors.size());
    }

    @Override
    public void updateRemoteConnector(List<NamedNode> remoteConnectors) {
        remoteConnectorForm.clear();
        remoteConnectorTable.update(remoteConnectors);
        navigation.updateBadge("messaging-remote-connector-item", remoteConnectors.size());
    }

    @Override
    public void updateConnectorService(List<NamedNode> connectorServices) {
        connectorServiceForm.clear();
        connectorServiceTable.update(connectorServices);
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
}
