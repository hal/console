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
import org.jboss.hal.core.elytron.CredentialReference;
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
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.POOLED_CONNECTION_FACTORY_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SELECTED_SERVER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.Ids.ENTRY_SUFFIX;
import static org.jboss.hal.resources.Ids.MESSAGING_SERVER;
import static org.jboss.hal.resources.Ids.TABLE_SUFFIX;

@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral", "WeakerAccess"})
public abstract class ConnectionView extends MbuiViewImpl<ConnectionPresenter>
        implements ConnectionPresenter.MyView {

    public static ConnectionView create(final MbuiContext mbuiContext) {
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

    ConnectionView(final MbuiContext mbuiContext) {
        super(mbuiContext);
        cr = new CredentialReference(mbuiContext.eventBus(), mbuiContext.dispatcher(), mbuiContext.ca(),
                mbuiContext.resources());
    }

    @PostConstruct
    void init() {

        Metadata metadata = mbuiContext.metadataRegistry().lookup(POOLED_CONNECTION_FACTORY_TEMPLATE);
        crForm = cr.form(Ids.MESSAGING_SERVER, metadata, CREDENTIAL_REFERENCE, PASSWORD,
                () -> pooledConnectionFactoryForm.<String>getFormItem(PASSWORD).getValue(),
                () -> presenter.pooledConnectionFactoryAddress(
                        pooledConnectionFactoryTable.hasSelection() ? pooledConnectionFactoryTable.selectedRow()
                                .getName() : null),
                () -> presenter.reload());

        pooledConnectionFactoryTable = new ModelNodeTable.Builder<NamedNode>(
                Ids.build(MESSAGING_SERVER, POOLED_CONNECTION_FACTORY, TABLE_SUFFIX), metadata)
                .button(mbuiContext.resources().constants().add(),
                        table -> presenter.addPooledConnectionFactory(ServerSubResource.POOLED_CONNECTION_FACTORY),
                        Constraint.executable(POOLED_CONNECTION_FACTORY_TEMPLATE, ADD))
                .button(mbuiContext.resources().constants().remove(),
                        table -> presenter.remove(ServerSubResource.POOLED_CONNECTION_FACTORY, table.selectedRow()),
                        Scope.SELECTED,
                        Constraint.executable(POOLED_CONNECTION_FACTORY_TEMPLATE, REMOVE))
                .column(NAME, (cell, type, row, meta) -> row.getName())
                .build();

        pooledConnectionFactoryForm = new ModelNodeForm.Builder<NamedNode>(
                Ids.build(Ids.MESSAGING_POOLED_CONNECTION_FACTORY, Ids.FORM_SUFFIX), metadata)
                .onSave((form, changedValues) -> presenter
                        .save(ServerSubResource.POOLED_CONNECTION_FACTORY, form, changedValues))
                .prepareReset(form -> presenter.reset(ServerSubResource.POOLED_CONNECTION_FACTORY, form))
                .build();

        Tabs tabs = new Tabs();
        tabs.add(Ids.build(Ids.MESSAGING_SERVER, POOLED_CONNECTION_FACTORY, ATTRIBUTES, Ids.TAB_SUFFIX),
                mbuiContext.resources().constants().attributes(), pooledConnectionFactoryForm.asElement());
        tabs.add(Ids.build(Ids.MESSAGING_SERVER, POOLED_CONNECTION_FACTORY, CREDENTIAL_REFERENCE, Ids.TAB_SUFFIX),
                Names.CREDENTIAL_REFERENCE, crForm.asElement());

        HTMLElement htmlSection = section()
                .add(h(1).textContent(Names.POOLED_CONNECTION_FACTORY))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(pooledConnectionFactoryTable)
                .add(tabs)
                .asElement();

        registerAttachable(pooledConnectionFactoryTable, pooledConnectionFactoryForm, crForm);

        navigation.insertPrimary(Ids.build(MESSAGING_SERVER, POOLED_CONNECTION_FACTORY, ENTRY_SUFFIX), null,
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
    public void setPresenter(final ConnectionPresenter presenter) {
        super.setPresenter(presenter);

        // register the suggestion handlers here rather than in a @PostConstruct method
        // they need a valid presenter reference!
        List<AddressTemplate> templates = asList(
                SELECTED_SERVER_TEMPLATE.append(CONNECTOR + "=*"),
                SELECTED_SERVER_TEMPLATE.append(IN_VM_CONNECTOR + "=*"),
                SELECTED_SERVER_TEMPLATE.append(HTTP_CONNECTOR + "=*"),
                SELECTED_SERVER_TEMPLATE.append(REMOTE_CONNECTOR + "=*"));

        connectionFactoryForm.getFormItem(CONNECTORS).registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), presenter.statementContext, templates));
        pooledConnectionFactoryForm.getFormItem(CONNECTORS).registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), presenter.statementContext, templates));
    }

    @Override
    public void updateAcceptor(final List<NamedNode> acceptors) {
        acceptorForm.clear();
        acceptorTable.update(acceptors);
        navigation.updateBadge("messaging-acceptor-entry", acceptors.size());
    }

    @Override
    public void updateInVmAcceptor(final List<NamedNode> inVmAcceptors) {
        inVmAcceptorForm.clear();
        inVmAcceptorTable.update(inVmAcceptors);
        navigation.updateBadge("messaging-in-vm-acceptor-entry", inVmAcceptors.size());
    }

    @Override
    public void updateHttpAcceptor(final List<NamedNode> httpAcceptors) {
        httpAcceptorForm.clear();
        httpAcceptorTable.update(httpAcceptors);
        navigation.updateBadge("messaging-http-acceptor-entry", httpAcceptors.size());
    }

    @Override
    public void updateRemoteAcceptor(final List<NamedNode> remoteAcceptors) {
        remoteAcceptorForm.clear();
        remoteAcceptorTable.update(remoteAcceptors);
        navigation.updateBadge("messaging-remote-acceptor-entry", remoteAcceptors.size());
    }

    @Override
    public void updateConnector(final List<NamedNode> connectors) {
        connectorForm.clear();
        connectorTable.update(connectors);
        navigation.updateBadge("messaging-connector-entry", connectors.size());
    }

    @Override
    public void updateInVmConnector(final List<NamedNode> inVmConnectors) {
        inVmConnectorForm.clear();
        inVmConnectorTable.update(inVmConnectors);
        navigation.updateBadge("messaging-in-vm-connector-entry", inVmConnectors.size());
    }

    @Override
    public void updateHttpConnector(final List<NamedNode> httpConnectors) {
        httpConnectorForm.clear();
        httpConnectorTable.update(httpConnectors);
        navigation.updateBadge("messaging-http-connector-entry", httpConnectors.size());
    }

    @Override
    public void updateRemoteConnector(final List<NamedNode> remoteConnectors) {
        remoteConnectorForm.clear();
        remoteConnectorTable.update(remoteConnectors);
        navigation.updateBadge("messaging-remote-connector-entry", remoteConnectors.size());
    }

    @Override
    public void updateConnectorService(final List<NamedNode> connectorServices) {
        connectorServiceForm.clear();
        connectorServiceTable.update(connectorServices);
    }

    @Override
    public void updateConnectionFactory(final List<NamedNode> connectionFactories) {
        connectionFactoryForm.clear();
        connectionFactoryTable.update(connectionFactories);
    }

    @Override
    public void updatePooledConnectionFactory(final List<NamedNode> pooledConnectionFactories) {
        crForm.clear();
        pooledConnectionFactoryForm.clear();
        pooledConnectionFactoryTable.update(pooledConnectionFactories);
    }
}
