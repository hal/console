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

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Api.RefreshMode;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.CONNECTOR_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.HTTP_CONNECTOR_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.LOCAL_OUTBOUND_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.OUTBOUND_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.REMOTE_OUTBOUND_TEMPLATE;
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
    @MbuiElement("remoting-connector-form") Form<NamedNode> connectorForm;
    @MbuiElement("remoting-http-connector-table") DataTable<NamedNode> httpConnectorTable;
    @MbuiElement("remoting-http-connector-form") Form<NamedNode> httpConnectorForm;

    @MbuiElement("remoting-local-outbound-table") DataTable<NamedNode> localOutboundTable;
    @MbuiElement("remoting-local-outbound-form") Form<NamedNode> localOutboundForm;
    @MbuiElement("remoting-outbound-table") DataTable<NamedNode> outboundTable;
    @MbuiElement("remoting-outbound-form") Form<NamedNode> outboundForm;
    @MbuiElement("remoting-remote-outbound-table") DataTable<NamedNode> remoteOutboundTable;
    @MbuiElement("remoting-remote-outbound-form") Form<NamedNode> remoteOutboundForm;

    RemotingView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public void update(final ModelNode payload) {
        endpointConfigurationForm.view(failSafeGet(payload, "configuration/endpoint"));

        connectorTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, CONNECTOR_TEMPLATE.lastKey())))
                .refresh(RefreshMode.RESET);
        connectorForm.clear();
        httpConnectorTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, HTTP_CONNECTOR_TEMPLATE.lastKey())))
                .refresh(RefreshMode.RESET);
        httpConnectorForm.clear();

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
