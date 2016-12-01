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
package org.jboss.hal.client.configuration.subsystem.remoting;

import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;

import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.PropertiesItem;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.form.FailSafeForm;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static java.util.stream.Collectors.toMap;
import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROPERTY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.marginTopLarge;

/**
 * This view is half generated, half hand written. The navigation, tables and outbound forms are generated using MBUI
 * XML, whereas the connector forms are created in this view. This is because these forms contain nested singletons
 * which may be null.
 *
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings({"WeakerAccess", "DuplicateStringLiteralInspection"})
public class RemotingView extends MbuiViewImpl<RemotingPresenter> implements RemotingPresenter.MyView {

    public static RemotingView create(final MbuiContext mbuiContext) {
        return new Mbui_RemotingView(mbuiContext);
    }

    @MbuiElement("remoting-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("remoting-endpoint-configuration") Form<ModelNode> endpointConfigurationForm;

    @MbuiElement("remoting-connector-table") NamedNodeTable<NamedNode> connectorTable;
    @MbuiElement("remoting-http-connector-table") NamedNodeTable<NamedNode> httpConnectorTable;

    @MbuiElement("remoting-local-outbound-table") NamedNodeTable<NamedNode> localOutboundTable;
    @MbuiElement("remoting-local-outbound-form") Form<NamedNode> localOutboundForm;
    @MbuiElement("remoting-outbound-table") NamedNodeTable<NamedNode> outboundTable;
    @MbuiElement("remoting-outbound-form") Form<NamedNode> outboundForm;
    @MbuiElement("remoting-remote-outbound-table") NamedNodeTable<NamedNode> remoteOutboundTable;
    @MbuiElement("remoting-remote-outbound-form") Form<NamedNode> remoteOutboundForm;

    private Form<NamedNode> connectorForm;
    private FailSafeForm<ModelNode> connectorSecurityForm;
    private FailSafeForm<ModelNode> connectorSecurityPolicyForm;

    private Form<NamedNode> httpConnectorForm;
    private FailSafeForm<ModelNode> httpConnectorSecurityForm;
    private FailSafeForm<ModelNode> httpConnectorSecurityPolicyForm;

    RemotingView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @PostConstruct
    void init() {
        // remote connector
        Metadata connectorMetadata = mbuiContext.metadataRegistry().lookup(CONNECTOR_TEMPLATE);
        connectorForm = new ModelNodeForm.Builder<NamedNode>(Ids.REMOTING_CONNECTOR_FORM, connectorMetadata)
                .unboundFormItem(new PropertiesItem(PROPERTY), 1)
                .onSave((form, changedValues) -> presenter.saveConnector(form, changedValues))
                .build();
        registerAttachable(connectorForm);

        // remote connector security
        Metadata connectorSecurityMetadata = mbuiContext.metadataRegistry().lookup(CONNECTOR_SECURITY_TEMPLATE);
        Form<ModelNode> csf = new ModelNodeForm.Builder<>(Ids.REMOTING_CONNECTOR_SECURITY_FORM,
                connectorSecurityMetadata)
                .unboundFormItem(new PropertiesItem(PROPERTY), 2)
                .onSave((form, changedValues) -> presenter.saveConnectorSecurity(form, changedValues))
                .build();
        connectorSecurityForm = new FailSafeForm<>(mbuiContext.dispatcher(),
                () -> new Operation.Builder(READ_RESOURCE_OPERATION,
                        SELECTED_CONNECTOR_SECURITY_TEMPLATE.resolve(presenter.selectedConnectorContext))
                        .build(),
                csf, () -> presenter.createConnectorSecurity());
        connectorSecurityForm.asElement().getClassList().add(marginTopLarge);
        registerAttachable(connectorSecurityForm);

        // remote connector security policy
        Metadata connectorSecurityPolicyMetadata = mbuiContext.metadataRegistry()
                .lookup(CONNECTOR_SECURITY_POLICY_TEMPLATE);
        Form<ModelNode> cspf = new ModelNodeForm.Builder<>(Ids.REMOTING_CONNECTOR_SECURITY_POLICY_FORM,
                connectorSecurityPolicyMetadata)
                .onSave((form, changedValues) -> presenter.saveConnectorSecurityPolicy(changedValues))
                .build();
        connectorSecurityPolicyForm = new FailSafeForm<>(mbuiContext.dispatcher(),
                () -> new Operation.Builder(READ_RESOURCE_OPERATION, SELECTED_CONNECTOR_SECURITY_POLICY_TEMPLATE
                        .resolve(presenter.selectedHttpConnectorContext))
                        .build(),
                cspf, () -> presenter.createConnectorSecurityPolicy());
        connectorSecurityPolicyForm.asElement().getClassList().add(marginTopLarge);
        registerAttachable(connectorSecurityPolicyForm);

        // remote connector tabs
        Tabs connectorTabs = new Tabs();
        connectorTabs.add(Ids.REMOTING_CONNECTOR_TAB, mbuiContext.resources().constants().attributes(),
                connectorForm.asElement());
        connectorTabs.add(Ids.REMOTING_CONNECTOR_SECURITY_TAB, mbuiContext.resources().constants().security(),
                connectorSecurityForm.asElement());
        connectorTabs.add(Ids.REMOTING_CONNECTOR_SECURITY_POLICY_TAB, mbuiContext.resources().constants().policy(),
                connectorSecurityPolicyForm.asElement());
        connectorTable.asElement().getParentElement().appendChild(connectorTabs.asElement());

        // http connector
        Metadata httpConnectorMetadata = mbuiContext.metadataRegistry().lookup(HTTP_CONNECTOR_TEMPLATE);
        httpConnectorForm = new ModelNodeForm.Builder<NamedNode>(Ids.REMOTING_HTTP_CONNECTOR_FORM,
                httpConnectorMetadata)
                .unboundFormItem(new PropertiesItem(PROPERTY), 2)
                .onSave((form, changedValues) -> presenter.saveHttpConnector(form, changedValues))
                .build();
        registerAttachable(httpConnectorForm);

        // http connector security
        Metadata httpConnectorSecurityMetadata = mbuiContext.metadataRegistry()
                .lookup(HTTP_CONNECTOR_SECURITY_TEMPLATE);
        Form<ModelNode> hcsf = new ModelNodeForm.Builder<>(Ids.REMOTING_HTTP_CONNECTOR_SECURITY_FORM,
                httpConnectorSecurityMetadata)
                .unboundFormItem(new PropertiesItem(PROPERTY), 2)
                .onSave((form, changedValues) -> presenter.saveHttpConnectorSecurity(form, changedValues))
                .build();
        httpConnectorSecurityForm = new FailSafeForm<>(mbuiContext.dispatcher(),
                () -> new Operation.Builder(READ_RESOURCE_OPERATION, SELECTED_HTTP_CONNECTOR_SECURITY_TEMPLATE
                        .resolve(presenter.selectedHttpConnectorContext))
                        .build(),
                hcsf, () -> presenter.createHttpConnectorSecurity());
        httpConnectorSecurityForm.asElement().getClassList().add(marginTopLarge);
        registerAttachable(httpConnectorSecurityForm);

        // http connector security policy
        Metadata httpConnectorSecurityPolicyMetadata = mbuiContext.metadataRegistry()
                .lookup(HTTP_CONNECTOR_SECURITY_POLICY_TEMPLATE);
        Form<ModelNode> hcspf = new ModelNodeForm.Builder<>(Ids.REMOTING_HTTP_CONNECTOR_SECURITY_POLICY_FORM,
                httpConnectorSecurityPolicyMetadata)
                .onSave((form, changedValues) -> presenter.saveHttpConnectorSecurityPolicy(changedValues))
                .build();
        httpConnectorSecurityPolicyForm = new FailSafeForm<>(mbuiContext.dispatcher(),
                () -> new Operation.Builder(READ_RESOURCE_OPERATION, SELECTED_HTTP_CONNECTOR_SECURITY_POLICY_TEMPLATE
                        .resolve(presenter.selectedHttpConnectorContext))
                        .build(),
                hcspf, () -> presenter.createHttpConnectorSecurityPolicy());
        httpConnectorSecurityPolicyForm.asElement().getClassList().add(marginTopLarge);
        registerAttachable(httpConnectorSecurityPolicyForm);

        // http connector tabs
        Tabs httpConnectorTabs = new Tabs();
        httpConnectorTabs.add(Ids.REMOTING_HTTP_CONNECTOR_TAB, mbuiContext.resources().constants().attributes(),
                httpConnectorForm.asElement());
        httpConnectorTabs.add(Ids.REMOTING_HTTP_CONNECTOR_SECURITY_TAB, mbuiContext.resources().constants().security(),
                httpConnectorSecurityForm.asElement());
        httpConnectorTabs
                .add(Ids.REMOTING_HTTP_CONNECTOR_SECURITY_POLICY_TAB, mbuiContext.resources().constants().policy(),
                        httpConnectorSecurityPolicyForm.asElement());
        httpConnectorTable.asElement().getParentElement().appendChild(httpConnectorTabs.asElement());
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void attach() {
        super.attach();
        connectorTable.api().onSelectionChange(
                api -> presenter.selectConnector(api.hasSelection() ? api.selectedRow().getName() : null));
        httpConnectorTable.api().onSelectionChange(
                api -> presenter.selectHttpConnector(api.hasSelection() ? api.selectedRow().getName() : null));

        localOutboundTable.api().onSelectionChange(
                api -> presenter.selectLocalOutbound(api.hasSelection() ? api.selectedRow().getName() : null));
        outboundTable.api().onSelectionChange(
                api -> presenter.selectOutbound(api.hasSelection() ? api.selectedRow().getName() : null));
        remoteOutboundTable.api().onSelectionChange(
                api -> presenter.selectRemoteOutbound(api.hasSelection() ? api.selectedRow().getName() : null));
    }

    @Override
    public void update(final ModelNode payload) {
        endpointConfigurationForm.view(failSafeGet(payload, "configuration/endpoint")); //NON-NLS

        connectorForm.clear();
        connectorForm.getFormItem(PROPERTY).clearValue();
        connectorSecurityForm.clear();
        connectorSecurityForm.getFormItem(PROPERTY).clearValue();
        connectorSecurityPolicyForm.clear();
        connectorTable.update(asNamedNodes(failSafePropertyList(payload, CONNECTOR_TEMPLATE.lastKey())));

        httpConnectorForm.clear();
        httpConnectorForm.getFormItem(PROPERTY).clearValue();
        httpConnectorSecurityForm.clear();
        httpConnectorSecurityForm.getFormItem(PROPERTY).clearValue();
        httpConnectorSecurityPolicyForm.clear();
        httpConnectorTable.update(asNamedNodes(failSafePropertyList(payload, HTTP_CONNECTOR_TEMPLATE.lastKey())));

        localOutboundForm.clear();
        localOutboundForm.getFormItem(PROPERTY).clearValue();
        localOutboundTable.update(asNamedNodes(failSafePropertyList(payload, LOCAL_OUTBOUND_TEMPLATE.lastKey())));

        outboundForm.clear();
        outboundForm.getFormItem(PROPERTY).clearValue();
        outboundTable.update(asNamedNodes(failSafePropertyList(payload, OUTBOUND_TEMPLATE.lastKey())));

        remoteOutboundForm.clear();
        remoteOutboundForm.getFormItem(PROPERTY).clearValue();
        remoteOutboundTable.update(asNamedNodes(failSafePropertyList(payload, REMOTE_OUTBOUND_TEMPLATE.lastKey())));
    }

    @Override
    public void updateConnector(final NamedNode connector) {
        updateGenericConnector(connector, connectorForm, connectorSecurityForm, connectorSecurityPolicyForm);
    }

    @Override
    public void updateHttpConnector(final NamedNode httpConnector) {
        updateGenericConnector(httpConnector, httpConnectorForm, httpConnectorSecurityForm,
                httpConnectorSecurityPolicyForm);
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private void updateGenericConnector(NamedNode node, Form<NamedNode> form, FailSafeForm<ModelNode> securityForm,
            FailSafeForm<ModelNode> policyForm) {

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
    public void updateLocalOutbound(@Nullable final NamedNode localOutbound) {
        updateGenericOutbound(localOutbound, localOutboundForm);
    }

    @Override
    public void updateOutbound(@Nullable final NamedNode outbound) {
        updateGenericOutbound(outbound, outboundForm);
    }

    @Override
    public void updateRemoteOutbound(@Nullable final NamedNode remoteOutbound) {
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
