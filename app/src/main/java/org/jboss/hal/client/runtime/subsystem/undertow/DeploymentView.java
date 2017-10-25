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
package org.jboss.hal.client.runtime.subsystem.undertow;

import java.util.List;

import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_DEPLOYMENT_SERVLET_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_DEPLOYMENT_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_DEPLOYMENT_WEBSOCKETS_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.UNDERTOW_RUNTIME;

public class DeploymentView extends HalViewImpl implements DeploymentPresenter.MyView {

    private final Table<NamedNode> servletsTable;
    private final Form<NamedNode> servletsForm;
    private final Table<NamedNode> websocketsTable;
    private final Form<NamedNode> websocketsForm;
    private final VerticalNavigation navigation;
    private DeploymentPresenter presenter;

    @Inject
    @SuppressWarnings({"ConstantConditions", "HardCodedStringLiteral"})
    public DeploymentView(final MetadataRegistry metadataRegistry, final Resources resources) {

        // ------------------------------------------------------ servlets

        Metadata servletMetadata = metadataRegistry.lookup(WEB_DEPLOYMENT_SERVLET_TEMPLATE);

        servletsTable = new ModelNodeTable.Builder<NamedNode>(Ids.build(UNDERTOW_RUNTIME, DEPLOYMENT, SERVLET,
                Ids.TABLE), servletMetadata)
                .button(resources.constants().reload(), table -> presenter.reload(),
                        Constraint.executable(WEB_DEPLOYMENT_TEMPLATE, READ_RESOURCE_OPERATION))
                .column(Names.SERVLET, (cell, type, row, meta) -> row.getName())
                .build();

        servletsForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(Ids.build(UNDERTOW_RUNTIME, DEPLOYMENT, SERVLET,
                FORM)), servletMetadata)
                .includeRuntime()
                .readOnly()
                .build();

        HTMLElement servletSection = section()
                .add(h(1).textContent(Names.SERVLET))
                .add(p().textContent(servletMetadata.getDescription().getDescription()))
                .add(servletsTable)
                .add(servletsForm)
                .asElement();

        // ------------------------------------------------------ websockets

        Metadata websocketMetadata = metadataRegistry.lookup(WEB_DEPLOYMENT_WEBSOCKETS_TEMPLATE);

        websocketsTable = new ModelNodeTable.Builder<NamedNode>(Ids.build(UNDERTOW_RUNTIME, DEPLOYMENT, WEBSOCKET,
                Ids.TABLE), websocketMetadata)
                .button(resources.constants().reload(), table -> presenter.reload(),
                        Constraint.executable(WEB_DEPLOYMENT_TEMPLATE, READ_RESOURCE_OPERATION))
                .column(Names.WEBSOCKET, (cell, type, row, meta) -> row.getName())
                .build();

        websocketsForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(UNDERTOW_RUNTIME, DEPLOYMENT, WEBSOCKET, FORM),
                websocketMetadata)
                .includeRuntime()
                .readOnly()
                .build();

        HTMLElement websocketSection = section()
                .add(h(1).textContent(Names.WEBSOCKETS))
                .add(p().textContent(websocketMetadata.getDescription().getDescription()))
                .add(websocketsTable)
                .add(websocketsForm)
                .asElement();

        navigation = new VerticalNavigation();
        navigation.addPrimary(Ids.build(UNDERTOW, DEPLOYMENT, SERVLET, Ids.ITEM), Names.SERVLET, pfIcon("enterprise"),
                servletSection);
        navigation.addPrimary(Ids.build(UNDERTOW, DEPLOYMENT, WEBSOCKET, Ids.ITEM), Names.WEBSOCKETS, pfIcon("replicator"),
                websocketSection);

        registerAttachable(navigation, servletsTable, servletsForm, websocketsTable, websocketsForm);

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void attach() {
        super.attach();
        servletsTable.bindForm(servletsForm);
        websocketsTable.bindForm(websocketsForm);
    }

    @Override
    public void setPresenter(final DeploymentPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void updateServlets(final List<NamedNode> model) {
        servletsForm.clear();
        servletsTable.update(model);
    }

    @Override
    public void updateWebsockets(final List<NamedNode> model) {
        websocketsForm.clear();
        websocketsTable.update(model);
    }
}
