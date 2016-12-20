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
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONNECTOR;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONNECTORS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HTTP_CONNECTOR;
import static org.jboss.hal.dmr.ModelDescriptionConstants.IN_VM_CONNECTOR;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOTE_CONNECTOR;

/**
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral"})
public abstract class ConnectionView extends MbuiViewImpl<ConnectionPresenter>
        implements ConnectionPresenter.MyView {

    public static ConnectionView create(final MbuiContext mbuiContext) {
        return new Mbui_ConnectionView(mbuiContext);
    }

    @MbuiElement("messaging-connection-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("messaging-acceptor-table") NamedNodeTable<NamedNode> acceptorTable;
    @MbuiElement("messaging-acceptor-form") Form<NamedNode> acceptorForm;
    @MbuiElement("messaging-in-vm-acceptor-table") NamedNodeTable<NamedNode> inVmAcceptorTable;
    @MbuiElement("messaging-in-vm-acceptor-form") Form<NamedNode> inVmAcceptorForm;
    @MbuiElement("messaging-http-acceptor-table") NamedNodeTable<NamedNode> httpAcceptorTable;
    @MbuiElement("messaging-http-acceptor-form") Form<NamedNode> httpAcceptorForm;
    @MbuiElement("messaging-remote-acceptor-table") NamedNodeTable<NamedNode> remoteAcceptorTable;
    @MbuiElement("messaging-remote-acceptor-form") Form<NamedNode> remoteAcceptorForm;
    @MbuiElement("messaging-connector-table") NamedNodeTable<NamedNode> connectorTable;
    @MbuiElement("messaging-connector-form") Form<NamedNode> connectorForm;
    @MbuiElement("messaging-in-vm-connector-table") NamedNodeTable<NamedNode> inVmConnectorTable;
    @MbuiElement("messaging-in-vm-connector-form") Form<NamedNode> inVmConnectorForm;
    @MbuiElement("messaging-http-connector-table") NamedNodeTable<NamedNode> httpConnectorTable;
    @MbuiElement("messaging-http-connector-form") Form<NamedNode> httpConnectorForm;
    @MbuiElement("messaging-remote-connector-table") NamedNodeTable<NamedNode> remoteConnectorTable;
    @MbuiElement("messaging-remote-connector-form") Form<NamedNode> remoteConnectorForm;
    @MbuiElement("messaging-connector-service-table") NamedNodeTable<NamedNode> connectorServiceTable;
    @MbuiElement("messaging-connector-service-form") Form<NamedNode> connectorServiceForm;
    @MbuiElement("messaging-connection-factory-table") NamedNodeTable<NamedNode> connectionFactoryTable;
    @MbuiElement("messaging-connection-factory-form") Form<NamedNode> connectionFactoryForm;
    @MbuiElement("messaging-pooled-connection-factory-table") NamedNodeTable<NamedNode> pooledConnectionFactoryTable;
    @MbuiElement("messaging-pooled-connection-factory-form") Form<NamedNode> pooledConnectionFactoryForm;

    ConnectionView(final MbuiContext mbuiContext) {
        super(mbuiContext);
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
        pooledConnectionFactoryForm.getFormItem(CONNECTOR).registerSuggestHandler(
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
        pooledConnectionFactoryForm.clear();
        pooledConnectionFactoryTable.update(pooledConnectionFactories);
    }
}
