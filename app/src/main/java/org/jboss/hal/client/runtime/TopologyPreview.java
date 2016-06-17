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
package org.jboss.hal.client.runtime;

import elemental.client.Browser;
import elemental.dom.Element;
import elemental.dom.NodeList;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static elemental.css.CSSStyleDeclaration.Unit.PX;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
class TopologyPreview extends PreviewContent<StaticItem> {

    private static final String TOPOLOGY_TABLE = "topology-table";
    private final Dispatcher dispatcher;

    TopologyPreview(final Dispatcher dispatcher, final Resources resources) {
        super(Names.TOPOLOGY, resources.previews().topology());
        this.dispatcher = dispatcher;

        previewBuilder().table().rememberAs(TOPOLOGY_TABLE).end();
        Element refresh = Browser.getDocument().getElementById(Ids.TOPOLOGY_REFRESH);
        refresh.setOnclick(event -> update(null));
    }

    @Override
    public void update(final StaticItem item) {
        Operation hostOp = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, ResourceAddress.ROOT)
                .param(CHILD_TYPE, HOST)
                .build();
        Operation serverGroupOp = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, ResourceAddress.ROOT)
                .param(CHILD_TYPE, SERVER_GROUP)
                .build();
        Operation serverOp = new Operation.Builder(QUERY, new ResourceAddress().add(HOST, "*").add(SERVER_CONFIG, "*"))
                .param(SELECT, new ModelNode().add(GROUP).add(SOCKET_BINDING_PORT_OFFSET).add(STATUS))
                .build();
        dispatcher.execute(new Composite(hostOp, serverGroupOp, serverOp), (CompositeResult result) -> {
            Browser.getWindow().setOnresize(event -> adjustTdHeight());
            adjustTdHeight();
        });
    }

    private void adjustTdHeight() {
        NodeList servers = Browser.getDocument().querySelectorAll("." + CSS.topology + " ." + CSS.servers);
        Elements.elements(servers).forEach(element ->
                element.getStyle().setHeight(element.getParentElement().getOffsetHeight(), PX));
    }
}
