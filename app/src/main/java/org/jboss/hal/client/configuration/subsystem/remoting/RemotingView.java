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
package org.jboss.hal.client.configuration.subsystem.remoting;

import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;

import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.PropertiesItem;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static java.util.stream.Collectors.toMap;
import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.CONNECTOR_SECURITY_POLICY_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.CONNECTOR_SECURITY_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.CONNECTOR_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.HTTP_CONNECTOR_SECURITY_POLICY_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.HTTP_CONNECTOR_SECURITY_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.HTTP_CONNECTOR_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.LOCAL_OUTBOUND_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.OUTBOUND_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.REMOTE_OUTBOUND_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.SELECTED_CONNECTOR_SECURITY_POLICY_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.SELECTED_CONNECTOR_SECURITY_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.SELECTED_HTTP_CONNECTOR_SECURITY_POLICY_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.SELECTED_HTTP_CONNECTOR_SECURITY_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROPERTY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

/**
 * This view is half generated, half hand written. The navigation, tables and outbound forms are generated using MBUI XML,
 * whereas the connector forms are created in this view. This is because these forms contain nested singletons which may be
 * null.
 */
@MbuiView
@SuppressWarnings({ "WeakerAccess", "DuplicateStringLiteralInspection", "unused" })
public abstract class RemotingView extends MbuiViewImpl<RemotingPresenter> implements RemotingPresenter.MyView {

    public static RemotingView create(MbuiContext mbuiContext) {
        return new Mbui_RemotingView(mbuiContext);
    }

    @MbuiElement("remoting-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("remoting-configuration") Form<ModelNode> configurationForm;

    @MbuiElement("remoting-connector-table") Table<NamedNode> connectorTable;
    @MbuiElement("remoting-http-connector-table") Table<NamedNode> httpConnectorTable;

    @MbuiElement("remoting-local-outbound-table") Table<NamedNode> localOutboundTable;
    @MbuiElement("remoting-local-outbound-form") Form<NamedNode> localOutboundForm;
    @MbuiElement("remoting-outbound-table") Table<NamedNode> outboundTable;
    @MbuiElement("remoting-outbound-form") Form<NamedNode> outboundForm;
    @MbuiElement("remoting-remote-outbound-table") Table<NamedNode> remoteOutboundTable;
    @MbuiElement("remoting-remote-outbound-form") Form<NamedNode> remoteOutboundForm;

    private Form<NamedNode> connectorForm;
    private Form<ModelNode> connectorSecurityForm;
    private Form<ModelNode> connectorSecurityPolicyForm;

    private Form<NamedNode> httpConnectorForm;
    private Form<ModelNode> httpConnectorSecurityForm;
    private Form<ModelNode> httpConnectorSecurityPolicyForm;

    RemotingView(MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @PostConstruct
    void init() {
        // remote connector
        Metadata connectorMetadata = mbuiContext.metadataRegistry().lookup(CONNECTOR_TEMPLATE);
        connectorForm = new ModelNodeForm.Builder<NamedNode>(Ids.REMOTING_CONNECTOR_FORM, connectorMetadata)
                .unboundFormItem(new PropertiesItem(PROPERTY), 1)
                .onSave((form, changedValues) -> presenter.saveConnector(form, changedValues))
                .prepareReset(form -> presenter.resetConnector(form))
                .build();
        registerAttachable(connectorForm);

        // remote connector security
        Metadata connectorSecurityMetadata = mbuiContext.metadataRegistry().lookup(CONNECTOR_SECURITY_TEMPLATE);
        connectorSecurityForm = new ModelNodeForm.Builder<>(Ids.REMOTING_CONNECTOR_SECURITY_FORM,
                connectorSecurityMetadata)
                .unboundFormItem(new PropertiesItem(PROPERTY), 2)
                .singleton(
                        () -> new Operation.Builder(
                                SELECTED_CONNECTOR_SECURITY_TEMPLATE.resolve(presenter.selectedConnectorContext),
                                READ_RESOURCE_OPERATION).build(),
                        () -> presenter.createConnectorSecurity())
                .onSave((form, changedValues) -> presenter.saveConnectorSecurity(form, changedValues))
                .prepareReset(form -> presenter.resetConnectorSecurity(form))
                .prepareRemove(form -> presenter.removeConnectorSecurity(form))
                .build();
        registerAttachable(connectorSecurityForm);

        // remote connector security policy
        Metadata connectorSecurityPolicyMetadata = mbuiContext.metadataRegistry()
                .lookup(CONNECTOR_SECURITY_POLICY_TEMPLATE);
        connectorSecurityPolicyForm = new ModelNodeForm.Builder<>(Ids.REMOTING_CONNECTOR_SECURITY_POLICY_FORM,
                connectorSecurityPolicyMetadata)
                .singleton(
                        () -> new Operation.Builder(SELECTED_CONNECTOR_SECURITY_POLICY_TEMPLATE
                                .resolve(presenter.selectedConnectorContext), READ_RESOURCE_OPERATION)
                                .build(),
                        () -> presenter.createConnectorSecurityPolicy())
                .onSave((form, changedValues) -> presenter.saveConnectorSecurityPolicy(changedValues))
                .prepareReset(form -> presenter.resetConnectorSecurityPolicy(form))
                .prepareRemove(form -> presenter.removeConnectorSecurityPolicy(form))
                .build();
        registerAttachable(connectorSecurityPolicyForm);

        // remote connector tabs
        Tabs connectorTabs = new Tabs(Ids.REMOTING_CONNECTOR_TAB_CONTAINER);
        connectorTabs.add(Ids.REMOTING_CONNECTOR_TAB, mbuiContext.resources().constants().attributes(),
                connectorForm.element());
        connectorTabs.add(Ids.REMOTING_CONNECTOR_SECURITY_TAB, Names.SECURITY,
                connectorSecurityForm.element());
        connectorTabs.add(Ids.REMOTING_CONNECTOR_SECURITY_POLICY_TAB, Names.POLICY,
                connectorSecurityPolicyForm.element());
        connectorTable.element().parentNode.appendChild(connectorTabs.element());

        // http connector
        Metadata httpConnectorMetadata = mbuiContext.metadataRegistry().lookup(HTTP_CONNECTOR_TEMPLATE);
        httpConnectorForm = new ModelNodeForm.Builder<NamedNode>(Ids.REMOTING_HTTP_CONNECTOR_FORM,
                httpConnectorMetadata)
                .unboundFormItem(new PropertiesItem(PROPERTY), 2)
                .onSave((form, changedValues) -> presenter.saveHttpConnector(form, changedValues))
                .prepareReset(form -> presenter.resetHttpConnector(form))
                .build();
        registerAttachable(httpConnectorForm);

        // http connector security
        Metadata httpConnectorSecurityMetadata = mbuiContext.metadataRegistry()
                .lookup(HTTP_CONNECTOR_SECURITY_TEMPLATE);
        httpConnectorSecurityForm = new ModelNodeForm.Builder<>(Ids.REMOTING_HTTP_CONNECTOR_SECURITY_FORM,
                httpConnectorSecurityMetadata)
                .unboundFormItem(new PropertiesItem(PROPERTY), 2)
                .singleton(
                        () -> new Operation.Builder(SELECTED_HTTP_CONNECTOR_SECURITY_TEMPLATE
                                .resolve(presenter.selectedHttpConnectorContext), READ_RESOURCE_OPERATION)
                                .build(),
                        () -> presenter.createHttpConnectorSecurity())
                .onSave((form, changedValues) -> presenter.saveHttpConnectorSecurity(form, changedValues))
                .prepareReset(form -> presenter.resetHttpConnectorSecurity(form))
                .prepareRemove(form -> presenter.removeHttpConnectorSecurity(form))
                .build();
        registerAttachable(httpConnectorSecurityForm);

        // http connector security policy
        Metadata httpConnectorSecurityPolicyMetadata = mbuiContext.metadataRegistry()
                .lookup(HTTP_CONNECTOR_SECURITY_POLICY_TEMPLATE);
        httpConnectorSecurityPolicyForm = new ModelNodeForm.Builder<>(Ids.REMOTING_HTTP_CONNECTOR_SECURITY_POLICY_FORM,
                httpConnectorSecurityPolicyMetadata)
                .singleton(
                        () -> new Operation.Builder(SELECTED_HTTP_CONNECTOR_SECURITY_POLICY_TEMPLATE
                                .resolve(presenter.selectedHttpConnectorContext), READ_RESOURCE_OPERATION)
                                .build(),
                        () -> presenter.createHttpConnectorSecurityPolicy())
                .onSave((form, changedValues) -> presenter.saveHttpConnectorSecurityPolicy(changedValues))
                .prepareReset(form -> presenter.resetHttpConnectorSecurityPolicy(form))
                .prepareRemove(form -> presenter.removeHttpConnectorSecurityPolicy(form))
                .build();
        registerAttachable(httpConnectorSecurityPolicyForm);

        // http connector tabs
        Tabs httpConnectorTabs = new Tabs(Ids.REMOTING_HTTP_CONNECTOR_TAB_CONTAINER);
        httpConnectorTabs.add(Ids.REMOTING_HTTP_CONNECTOR_TAB, mbuiContext.resources().constants().attributes(),
                httpConnectorForm.element());
        httpConnectorTabs.add(Ids.REMOTING_HTTP_CONNECTOR_SECURITY_TAB, Names.SECURITY,
                httpConnectorSecurityForm.element());
        httpConnectorTabs
                .add(Ids.REMOTING_HTTP_CONNECTOR_SECURITY_POLICY_TAB, Names.POLICY,
                        httpConnectorSecurityPolicyForm.element());
        httpConnectorTable.element().parentNode.appendChild(httpConnectorTabs.element());
    }

    @Override
    public void attach() {
        super.attach();
        connectorTable.onSelectionChange(
                t -> presenter.selectConnector(t.hasSelection() ? t.selectedRow().getName() : null));
        httpConnectorTable.onSelectionChange(
                t -> presenter.selectHttpConnector(t.hasSelection() ? t.selectedRow().getName() : null));

        localOutboundTable.onSelectionChange(
                t -> presenter.selectLocalOutbound(t.hasSelection() ? t.selectedRow().getName() : null));
        outboundTable.onSelectionChange(
                t -> presenter.selectOutbound(t.hasSelection() ? t.selectedRow().getName() : null));
        remoteOutboundTable.onSelectionChange(
                t -> presenter.selectRemoteOutbound(t.hasSelection() ? t.selectedRow().getName() : null));
    }

    @Override
    public void update(ModelNode payload) {
        configurationForm.view(payload); // NON-NLS

        connectorForm.clear();
        connectorForm.getFormItem(PROPERTY).clearValue();
        connectorSecurityForm.clear();
        connectorSecurityForm.getFormItem(PROPERTY).clearValue();
        connectorSecurityPolicyForm.clear();
        connectorTable.update(asNamedNodes(failSafePropertyList(payload, CONNECTOR_TEMPLATE.lastName())));

        httpConnectorForm.clear();
        httpConnectorForm.getFormItem(PROPERTY).clearValue();
        httpConnectorSecurityForm.clear();
        httpConnectorSecurityForm.getFormItem(PROPERTY).clearValue();
        httpConnectorSecurityPolicyForm.clear();
        httpConnectorTable.update(asNamedNodes(failSafePropertyList(payload, HTTP_CONNECTOR_TEMPLATE.lastName())));

        localOutboundForm.clear();
        localOutboundForm.getFormItem(PROPERTY).clearValue();
        localOutboundTable.update(asNamedNodes(failSafePropertyList(payload, LOCAL_OUTBOUND_TEMPLATE.lastName())));

        outboundForm.clear();
        outboundForm.getFormItem(PROPERTY).clearValue();
        outboundTable.update(asNamedNodes(failSafePropertyList(payload, OUTBOUND_TEMPLATE.lastName())));

        remoteOutboundForm.clear();
        remoteOutboundForm.getFormItem(PROPERTY).clearValue();
        remoteOutboundTable.update(asNamedNodes(failSafePropertyList(payload, REMOTE_OUTBOUND_TEMPLATE.lastName())));
    }

    @Override
    public void updateConnector(NamedNode connector) {
        updateGenericConnector(connector, connectorForm, connectorSecurityForm, connectorSecurityPolicyForm);
    }

    @Override
    public void updateHttpConnector(NamedNode httpConnector) {
        updateGenericConnector(httpConnector, httpConnectorForm, httpConnectorSecurityForm,
                httpConnectorSecurityPolicyForm);
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private void updateGenericConnector(NamedNode node, Form<NamedNode> form,
            Form<ModelNode> securityForm, Form<ModelNode> policyForm) {

        FormItem<Object> properties = form.getFormItem(PROPERTY);
        FormItem<Object> securityProperties = securityForm.getFormItem(PROPERTY);

        if (node != null) {
            Map<String, String> p = failSafePropertyList(node, PROPERTY).stream()
                    .collect(toMap(Property::getName, property -> property.getValue().get(VALUE).asString()));
            ModelNode security = failSafeGet(node, "security/sasl");
            Map<String, String> sp = failSafePropertyList(node, "security/sasl/" + PROPERTY).stream()
                    .collect(toMap(Property::getName, property -> property.getValue().get(VALUE).asString()));
            ModelNode policy = failSafeGet(node, "security/sasl/sasl-policy/policy");

            form.view(node);
            properties.setValue(p);
            securityForm.view(security);
            securityProperties.setValue(sp);
            policyForm.view(policy);

        } else {
            form.clear();
            properties.clearValue();
            securityForm.clear();
            securityProperties.clearValue();
            policyForm.clear();
        }
    }

    @Override
    public void updateLocalOutbound(@Nullable NamedNode localOutbound) {
        updateGenericOutbound(localOutbound, localOutboundForm);
    }

    @Override
    public void updateOutbound(@Nullable NamedNode outbound) {
        updateGenericOutbound(outbound, outboundForm);
    }

    @Override
    public void updateRemoteOutbound(@Nullable NamedNode remoteOutbound) {
        updateGenericOutbound(remoteOutbound, remoteOutboundForm);
    }

    private void updateGenericOutbound(NamedNode node, Form<NamedNode> form) {
        FormItem<Object> properties = form.getFormItem(PROPERTY);
        if (node != null) {
            Map<String, String> p = failSafePropertyList(node, PROPERTY).stream()
                    .collect(toMap(Property::getName, property -> property.getValue().get(VALUE).asString()));
            form.view(node);
            properties.setValue(p);

        } else {
            form.clear();
            properties.clearValue();
        }
    }
}
