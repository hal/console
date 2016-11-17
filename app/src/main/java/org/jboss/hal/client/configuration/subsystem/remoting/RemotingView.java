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
import javax.annotation.PostConstruct;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.PropertiesItem;
import org.jboss.hal.ballroom.table.Api;
import org.jboss.hal.ballroom.table.Api.RefreshMode;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.form.SubResourceProperties;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static java.util.stream.Collectors.toMap;
import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROPERTY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

/**
 * TODO Add support for (http-)connector properties, security, security-properties and policy resources.
 * Replace the generated forms with hand-written tabs with forms / fail-safe-forms.
 * So this becomes a hybrid-view (half generated, half custom impl.)
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
    @MbuiElement("remoting-local-outbound-form") Form<NamedNode> localOutboundForm;
    @MbuiElement("remoting-outbound-table") DataTable<NamedNode> outboundTable;
    @MbuiElement("remoting-outbound-form") Form<NamedNode> outboundForm;
    @MbuiElement("remoting-remote-outbound-table") DataTable<NamedNode> remoteOutboundTable;
    @MbuiElement("remoting-remote-outbound-form") Form<NamedNode> remoteOutboundForm;

    private ModelNode payload;
    private Form<NamedNode> connectorForm;
    private SubResourceProperties.FormItem connectorProperties;
    private Form<NamedNode> httpConnectorForm;
    private SubResourceProperties.FormItem httpConnectorProperties;

    RemotingView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @PostConstruct
    void init() {
        Metadata connectorMetadata = mbuiContext.metadataRegistry().lookup(CONNECTOR_TEMPLATE);
        connectorProperties = new SubResourceProperties.FormItem(PROPERTY);
        connectorForm = new ModelNodeForm.Builder<NamedNode>(Ids.REMOTING_CONNECTOR_FORM, connectorMetadata)
                .unboundFormItem(connectorProperties, 2)
                .onSave((form, changedValues) -> presenter.saveConnector(form.getModel().getName(), changedValues,
                        connectorProperties.isModified(), connectorProperties.getValue()))
                .build();
        registerAttachable(connectorForm);
        connectorTable.asElement().getParentElement().appendChild(connectorForm.asElement());

        Metadata httpConnectorMetadata = mbuiContext.metadataRegistry().lookup(HTTP_CONNECTOR_TEMPLATE);
        httpConnectorProperties = new SubResourceProperties.FormItem(PROPERTY);
        httpConnectorForm = new ModelNodeForm.Builder<NamedNode>(Ids.REMOTING_HTTP_CONNECTOR_FORM,
                httpConnectorMetadata)
                .unboundFormItem(httpConnectorProperties, 2)
                .onSave((form, changedValues) -> presenter.saveHttpConnector(form.getModel().getName(), changedValues,
                        httpConnectorProperties.isModified(), httpConnectorProperties.getValue()))
                .build();
        registerAttachable(httpConnectorForm);
        httpConnectorTable.asElement().getParentElement().appendChild(httpConnectorForm.asElement());
    }

    @Override
    public void attach() {
        super.attach();
        connectorTable.api().onSelectionChange(api -> updateForm(api, connectorForm,
                CONNECTOR_TEMPLATE.lastKey(), connectorProperties));
        httpConnectorTable.api().onSelectionChange(api -> updateForm(api, httpConnectorForm,
                HTTP_CONNECTOR_TEMPLATE.lastKey(), httpConnectorProperties));
    }

    private void updateForm(Api<NamedNode> api, Form<NamedNode> form, String resource, PropertiesItem propertiesItem) {
        if (api.hasSelection()) {
            //noinspection ConstantConditions
            String path = resource + "/" + api.selectedRow().getName() + "/" + PROPERTY;
            form.view(api.selectedRow());
            Map<String, String> properties = failSafePropertyList(payload, path).stream()
                    .collect(toMap(Property::getName, property -> property.getValue().get(VALUE).asString()));
            propertiesItem.setValue(properties);

        } else {
            form.clear();
            propertiesItem.clearValue();
        }
    }

    @Override
    public void update(final ModelNode payload) {
        this.payload = payload;

        endpointConfigurationForm.view(failSafeGet(payload, "configuration/endpoint")); //NON-NLS

        connectorTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, CONNECTOR_TEMPLATE.lastKey())))
                .refresh(RefreshMode.RESET);
        connectorForm.clear();
        connectorProperties.clearValue();
        httpConnectorTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, HTTP_CONNECTOR_TEMPLATE.lastKey())))
                .refresh(RefreshMode.RESET);
        httpConnectorForm.clear();
        httpConnectorProperties.clearValue();

        localOutboundTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, LOCAL_OUTBOUND_TEMPLATE.lastKey())))
                .refresh(RefreshMode.RESET);
        localOutboundForm.clear();
        outboundTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, OUTBOUND_TEMPLATE.lastKey())))
                .refresh(RefreshMode.RESET);
        outboundForm.clear();
        remoteOutboundTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, REMOTE_OUTBOUND_TEMPLATE.lastKey())))
                .refresh(RefreshMode.RESET);
        remoteOutboundForm.clear();
    }
}
