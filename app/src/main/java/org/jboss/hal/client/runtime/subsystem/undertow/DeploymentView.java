/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Scope;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.table;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_DEPLOYMENT_SERVLET_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_DEPLOYMENT_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_DEPLOYMENT_WEBSOCKETS_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.table;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.SESSION;
import static org.jboss.hal.resources.Ids.UNDERTOW_RUNTIME;

public class DeploymentView extends HalViewImpl implements DeploymentPresenter.MyView {

    private final Table<Session> sessionTable;
    private final HTMLElement attributesElement;
    private final HTMLElement attributesTableBody;
    private final Table<NamedNode> servletsTable;
    private final Form<NamedNode> servletsForm;
    private final Table<NamedNode> websocketsTable;
    private final Form<NamedNode> websocketsForm;
    private final VerticalNavigation navigation;
    private DeploymentPresenter presenter;

    @Inject
    public DeploymentView(MetadataRegistry metadataRegistry, Resources resources) {

        // ------------------------------------------------------ sessions

        String id = Ids.build(UNDERTOW, DEPLOYMENT, SESSION);
        sessionTable = new ModelNodeTable.Builder<Session>(id, Metadata.empty())
                .button(resources.constants().reload(), table -> presenter.reload(),
                        Constraint.executable(WEB_DEPLOYMENT_TEMPLATE, LIST_SESSIONS))
                .button(resources.constants().invalidateSession(),
                        table -> presenter.invalidateSession(table.selectedRow()), Scope.SELECTED,
                        Constraint.executable(WEB_DEPLOYMENT_TEMPLATE, INVALIDATE_SESSION))
                .column(SESSION_ID, Names.SESSION_ID, (cell, type, row, meta) -> row.getName())
                .column(CREATION_TIME, resources.constants().creationTime(),
                        (cell, type, row, meta) -> Format.shortDateTime(row.getCreationTime()))
                .column(LAST_ACCESSED_TIME, resources.constants().lastAccessedTime(),
                        (cell, type, row, meta) -> Format.shortDateTime(row.getLastAccessTime()))
                .build();

        attributesElement = div().css(marginTopLarge)
                .add(h(2, resources.constants().attributes()))
                .add(table().css(table, tableStriped, attributes)
                        .add(thead()
                                .add(tr()
                                        .add(th().textContent(resources.constants().name()))
                                        .add(th().textContent(Names.VALUE))))
                        .add(attributesTableBody = tbody().element())).element();
        Elements.setVisible(attributesElement, false);

        HTMLElement sessionSection = section()
                .add(h(1).textContent(Names.SESSIONS))
                .add(sessionTable)
                .add(attributesElement).element();

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
                .add(servletsForm).element();

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
                .add(websocketsForm).element();

        // ------------------------------------------------------ navigation & root

        navigation = new VerticalNavigation();
        navigation.addPrimary(Ids.build(UNDERTOW, DEPLOYMENT, SESSION, Ids.ITEM), Names.SESSIONS,
                pfIcon("users"), sessionSection);
        navigation.addPrimary(Ids.build(UNDERTOW, DEPLOYMENT, SERVLET, Ids.ITEM), Names.SERVLET,
                fontAwesome("code"), servletSection);
        navigation.addPrimary(Ids.build(UNDERTOW, DEPLOYMENT, WEBSOCKET, Ids.ITEM), Names.WEBSOCKETS,
                fontAwesome("exchange"), websocketSection);

        registerAttachable(navigation, sessionTable, servletsTable, servletsForm, websocketsTable, websocketsForm);

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void attach() {
        super.attach();
        sessionTable.onSelectionChange(table -> {
            if (table.hasSelection()) {
                presenter.listSessionAttributes(table.selectedRow());
            } else {
                Elements.setVisible(attributesElement, false);
            }
        });
        servletsTable.bindForm(servletsForm);
        websocketsTable.bindForm(websocketsForm);
    }

    @Override
    public void setPresenter(DeploymentPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void updateSessions(List<Session> sessions) {
        sessionTable.update(sessions);
        Elements.setVisible(attributesElement, sessionTable.hasSelection());
    }

    @Override
    public void updateSessionAttributes(List<Property> attributes) {
        Elements.removeChildrenFrom(attributesTableBody);
        for (Property attribute : attributes) {
            attributesTableBody.appendChild(tr()
                    .add(td().textContent(attribute.getName()))
                    .add(td().textContent(attribute.getValue().asString())).element());
        }
        Elements.setVisible(attributesElement, !attributes.isEmpty());
    }

    @Override
    public void updateServlets(List<NamedNode> model) {
        servletsForm.clear();
        servletsTable.update(model);
    }

    @Override
    public void updateWebsockets(List<NamedNode> model) {
        websocketsForm.clear();
        websocketsTable.update(model);
    }
}
