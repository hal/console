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
package org.jboss.hal.client.configuration.subsystem.undertow;

import javax.annotation.PostConstruct;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

/**
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral"})
public abstract class HandlerView extends MbuiViewImpl<HandlerPresenter>
        implements HandlerPresenter.MyView {

    public static HandlerView create(final MbuiContext mbuiContext) {
        return new Mbui_HandlerView(mbuiContext);
    }

    @MbuiElement("undertow-handler-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("undertow-file-handler-table") NamedNodeTable<NamedNode> fileHandlerTable;
    @MbuiElement("undertow-file-handler-form") Form<NamedNode> fileHandlerForm;
    @MbuiElement("undertow-reverse-proxy-table") NamedNodeTable<NamedNode> reverseProxyTable;
    @MbuiElement("undertow-reverse-proxy-form") Form<NamedNode> reverseProxyForm;

    HandlerView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @Override
    public void update(final ModelNode modelNode) {
        fileHandlerForm.clear();
        fileHandlerTable.update(asNamedNodes(failSafePropertyList(modelNode, "file")));
        reverseProxyForm.clear();
        reverseProxyTable.update(asNamedNodes(failSafePropertyList(modelNode, "reverse-proxy")));
    }
}
