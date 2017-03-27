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
package org.jboss.hal.client.configuration;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static org.jboss.hal.dmr.ModelDescriptionConstants.LOCAL_DESTINATION_OUTBOUND_SOCKET_BINDING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOTE_DESTINATION_OUTBOUND_SOCKET_BINDING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

/**
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class SocketBindingGroupView extends MbuiViewImpl<SocketBindingGroupPresenter>
        implements SocketBindingGroupPresenter.MyView {

    public static SocketBindingGroupView create(final MbuiContext mbuiContext) {
        return new Mbui_SocketBindingGroupView(mbuiContext);
    }

    @MbuiElement("socket-binding-group-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("socket-binding-group-configuration-form") Form<NamedNode> configurationForm;
    @MbuiElement("socket-binding-group-inbound-table") NamedNodeTable<NamedNode> inboundTable;
    @MbuiElement("socket-binding-group-inbound-form") Form<NamedNode> inboundForm;
    @MbuiElement("socket-binding-group-outbound-local-table") NamedNodeTable<NamedNode> outboundLocalTable;
    @MbuiElement("socket-binding-group-outbound-local-form") Form<NamedNode> outboundLocalForm;
    @MbuiElement("socket-binding-group-outbound-remote-table") NamedNodeTable<NamedNode> outboundRemoteTable;
    @MbuiElement("socket-binding-group-outbound-remote-form") Form<NamedNode> outboundRemoteForm;

    SocketBindingGroupView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @Override
    public void update(final NamedNode socketBindingGroup) {
        configurationForm.view(socketBindingGroup);

        inboundForm.clear();
        inboundTable.update(asNamedNodes(failSafePropertyList(socketBindingGroup, SOCKET_BINDING)));

        outboundLocalForm.clear();
        outboundLocalTable.update(asNamedNodes(failSafePropertyList(socketBindingGroup,
                LOCAL_DESTINATION_OUTBOUND_SOCKET_BINDING)));

        outboundRemoteForm.clear();
        outboundRemoteTable.update(asNamedNodes(failSafePropertyList(socketBindingGroup,
                REMOTE_DESTINATION_OUTBOUND_SOCKET_BINDING)));
    }
}