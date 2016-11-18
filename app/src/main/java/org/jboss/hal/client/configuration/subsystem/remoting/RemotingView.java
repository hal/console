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
import org.jboss.hal.ballroom.form.PropertiesItem;
import org.jboss.hal.ballroom.table.Api.RefreshMode;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.form.FailSafeForm;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.form.SubResourceProperties;
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
 * This view is half generated, half hand written. The navigation and tables are generated using MBUI XML,
 * whereas the forms are created in this view. This is because the forms contain properties items which map to
 * sub resources and nested singletons which may be null.
 *
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings("WeakerAccess")
public class RemotingView extends MbuiViewImpl<RemotingPresenter> implements RemotingPresenter.MyView {

    // ------------------------------------------------------ initialization

    public static RemotingView create(final MbuiContext mbuiContext) {
        return new Mbui_RemotingView(mbuiContext);
    }

    @MbuiElement("remoting-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("remoting-endpoint-configuration") Form<ModelNode> endpointConfigurationForm;

    @MbuiElement("remoting-connector-table") DataTable<NamedNode> connectorTable;
    @MbuiElement("remoting-http-connector-table") DataTable<NamedNode> httpConnectorTable;

    @MbuiElement("remoting-local-outbound-table") DataTable<NamedNode> localOutboundTable;
    @MbuiElement("remoting-outbound-table") DataTable<NamedNode> outboundTable;
    @MbuiElement("remoting-remote-outbound-table") DataTable<NamedNode> remoteOutboundTable;

    private Form<NamedNode> connectorForm;
    private SubResourceProperties.FormItem connectorProperties;
    private FailSafeForm<ModelNode> connectorSecurityForm;
    private SubResourceProperties.FormItem connectorSecurityProperties;
    private FailSafeForm<ModelNode> connectorSecurityPolicyForm;

    private Form<NamedNode> httpConnectorForm;
    private SubResourceProperties.FormItem httpConnectorProperties;
    private FailSafeForm<ModelNode> httpConnectorSecurityForm;
    private SubResourceProperties.FormItem httpConnectorSecurityProperties;
    private FailSafeForm<ModelNode> httpConnectorSecurityPolicyForm;

    private Form<NamedNode> localOutboundForm;
    private SubResourceProperties.FormItem localOutboundProperties;
    private Form<NamedNode> outboundForm;
    private SubResourceProperties.FormItem outboundProperties;
    private Form<NamedNode> remoteOutboundForm;
    private SubResourceProperties.FormItem remoteOutboundProperties;

    RemotingView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @PostConstruct
    void init() {
        // remote connector
        Metadata connectorMetadata = mbuiContext.metadataRegistry().lookup(CONNECTOR_TEMPLATE);
        connectorProperties = new SubResourceProperties.FormItem(PROPERTY);
        connectorForm = new ModelNodeForm.Builder<NamedNode>(Ids.REMOTING_CONNECTOR_FORM, connectorMetadata)
                .unboundFormItem(connectorProperties, 1)
                .onSave((form, changedValues) -> presenter.saveConnector(form.getModel().getName(), changedValues,
                        connectorProperties.isModified(), connectorProperties.getValue()))
                .build();
        registerAttachable(connectorForm);

        // remote connector security
        Metadata connectorSecurityMetadata = mbuiContext.metadataRegistry().lookup(CONNECTOR_SECURITY_TEMPLATE);
        connectorSecurityProperties = new SubResourceProperties.FormItem(PROPERTY);
        Form<ModelNode> csf = new ModelNodeForm.Builder<>(Ids.REMOTING_CONNECTOR_SECURITY_FORM,
                connectorSecurityMetadata)
                .unboundFormItem(connectorSecurityProperties, 2)
                .onSave((form, changedValues) -> presenter.saveConnectorSecurity(changedValues,
                        connectorSecurityProperties.isModified(), connectorSecurityProperties.getValue()))
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
        httpConnectorProperties = new SubResourceProperties.FormItem(PROPERTY);
        httpConnectorForm = new ModelNodeForm.Builder<NamedNode>(Ids.REMOTING_HTTP_CONNECTOR_FORM,
                httpConnectorMetadata)
                .unboundFormItem(httpConnectorProperties, 2)
                .onSave((form, changedValues) -> presenter.saveHttpConnector(form.getModel().getName(), changedValues,
                        httpConnectorProperties.isModified(), httpConnectorProperties.getValue()))
                .build();
        registerAttachable(httpConnectorForm);

        // http connector security
        Metadata httpConnectorSecurityMetadata = mbuiContext.metadataRegistry()
                .lookup(HTTP_CONNECTOR_SECURITY_TEMPLATE);
        httpConnectorSecurityProperties = new SubResourceProperties.FormItem(PROPERTY);
        Form<ModelNode> hcsf = new ModelNodeForm.Builder<>(Ids.REMOTING_HTTP_CONNECTOR_SECURITY_FORM,
                httpConnectorSecurityMetadata)
                .unboundFormItem(httpConnectorSecurityProperties, 2)
                .onSave((form, changedValues) -> presenter.saveHttpConnectorSecurity(changedValues,
                        httpConnectorSecurityProperties.isModified(), httpConnectorSecurityProperties.getValue()))
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

        // local outbound connection
        Metadata localOutboundMetadata = mbuiContext.metadataRegistry().lookup(LOCAL_OUTBOUND_TEMPLATE);
        localOutboundProperties = new SubResourceProperties.FormItem(PROPERTY);
        localOutboundForm = new ModelNodeForm.Builder<NamedNode>(Ids.REMOTING_LOCAL_OUTBOUND_FORM,
                localOutboundMetadata)
                .unboundFormItem(localOutboundProperties)
                .onSave((form, changedValues) -> presenter.saveLocalOutbound(form.getModel().getName(), changedValues,
                        localOutboundProperties.isModified(), localOutboundProperties.getValue()))
                .build();
        registerAttachable(localOutboundForm);
        localOutboundTable.asElement().getParentElement().appendChild(localOutboundForm.asElement());

        // outbound connection
        Metadata outboundMetadata = mbuiContext.metadataRegistry().lookup(OUTBOUND_TEMPLATE);
        outboundProperties = new SubResourceProperties.FormItem(PROPERTY);
        outboundForm = new ModelNodeForm.Builder<NamedNode>(Ids.REMOTING_OUTBOUND_FORM, outboundMetadata)
                .unboundFormItem(outboundProperties, 0)
                .onSave((form, changedValues) -> presenter.saveOutbound(form.getModel().getName(), changedValues,
                        outboundProperties.isModified(), outboundProperties.getValue()))
                .build();
        registerAttachable(outboundForm);
        outboundTable.asElement().getParentElement().appendChild(outboundForm.asElement());

        // remote outbound connection
        Metadata remoteOutboundMetadata = mbuiContext.metadataRegistry().lookup(REMOTE_OUTBOUND_TEMPLATE);
        remoteOutboundProperties = new SubResourceProperties.FormItem(PROPERTY);
        remoteOutboundForm = new ModelNodeForm.Builder<NamedNode>(Ids.REMOTING_REMOTE_OUTBOUND_FORM,
                remoteOutboundMetadata)
                .unboundFormItem(remoteOutboundProperties, 1)
                .onSave((form, changedValues) -> presenter.saveRemoteOutbound(form.getModel().getName(), changedValues,
                        remoteOutboundProperties.isModified(), remoteOutboundProperties.getValue()))
                .build();
        registerAttachable(remoteOutboundForm);
        remoteOutboundTable.asElement().getParentElement().appendChild(remoteOutboundForm.asElement());
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

        connectorTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, CONNECTOR_TEMPLATE.lastKey())))
                .refresh(RefreshMode.RESET);
        connectorForm.clear();
        connectorProperties.clearValue();
        connectorSecurityForm.clear();
        connectorSecurityProperties.clearValue();
        connectorSecurityPolicyForm.clear();

        httpConnectorTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, HTTP_CONNECTOR_TEMPLATE.lastKey())))
                .refresh(RefreshMode.RESET);
        httpConnectorForm.clear();
        httpConnectorProperties.clearValue();
        httpConnectorSecurityForm.clear();
        httpConnectorSecurityProperties.clearValue();
        httpConnectorSecurityPolicyForm.clear();

        localOutboundTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, LOCAL_OUTBOUND_TEMPLATE.lastKey())))
                .refresh(RefreshMode.RESET);
        localOutboundForm.clear();
        localOutboundProperties.clearValue();

        outboundTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, OUTBOUND_TEMPLATE.lastKey())))
                .refresh(RefreshMode.RESET);
        outboundForm.clear();
        outboundProperties.clearValue();

        remoteOutboundTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, REMOTE_OUTBOUND_TEMPLATE.lastKey())))
                .refresh(RefreshMode.RESET);
        remoteOutboundForm.clear();
        remoteOutboundProperties.clearValue();
    }

    @Override
    public void updateConnector(final NamedNode connector) {
        updateGenericConnector(connector, connectorForm, connectorProperties,
                connectorSecurityForm, connectorSecurityProperties, connectorSecurityPolicyForm);
    }

    @Override
    public void updateHttpConnector(final NamedNode httpConnector) {
        updateGenericConnector(httpConnector, httpConnectorForm, httpConnectorProperties,
                httpConnectorSecurityForm, httpConnectorSecurityProperties, httpConnectorSecurityPolicyForm);
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private void updateGenericConnector(NamedNode node, Form<NamedNode> form, PropertiesItem properties,
            FailSafeForm<ModelNode> securityForm, PropertiesItem securityProperties,
            FailSafeForm<ModelNode> policyForm) {

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
        updateGenericOutbound(localOutbound, localOutboundForm, localOutboundProperties);
    }

    @Override
    public void updateOutbound(@Nullable final NamedNode outbound) {
        updateGenericOutbound(outbound, outboundForm, outboundProperties);
    }

    @Override
    public void updateRemoteOutbound(@Nullable final NamedNode remoteOutbound) {
        updateGenericOutbound(remoteOutbound, remoteOutboundForm, remoteOutboundProperties);
    }

    private void updateGenericOutbound(NamedNode node, Form<NamedNode> form, PropertiesItem properties) {
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
