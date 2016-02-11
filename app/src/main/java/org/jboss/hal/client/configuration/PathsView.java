/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.client.configuration;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.layout.LayoutBuilder;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptions;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityFramework;
import org.jboss.hal.resources.Resources;

import javax.inject.Inject;
import java.util.List;

import static org.jboss.hal.ballroom.table.Api.RefreshMode.RESET;
import static org.jboss.hal.ballroom.table.Button.Scope.SELECTED_SINGLE;
import static org.jboss.hal.resources.Ids.PATHS_FORM;
import static org.jboss.hal.resources.Ids.PATHS_TABLE;
import static org.jboss.hal.resources.Names.NAME;
import static org.jboss.hal.resources.Names.PATHS;

/**
 * @author Harald Pehl
 */
public class PathsView extends PatternFlyViewImpl implements PathsPresenter.MyView {

    private final DataTable<ModelNode> table;
    private final ModelNodeForm<ModelNode> form;
//    private final Dialog dialog;
    private PathsPresenter presenter;

    @Inject
    public PathsView(ResourceDescriptions descriptions,
            SecurityFramework securityFramework,
            Resources resources) {

        ResourceDescription description = descriptions.lookup(PathsPresenter.ROOT_TEMPLATE);
        SecurityContext securityContext = securityFramework.lookup(PathsPresenter.ROOT_TEMPLATE);

//        new Dialog.Builder(resources.messages())

        Element info = new Elements.Builder().p().innerText(description.getDescription()).end().build();
        Options<ModelNode> options = new ModelNodeTable.Builder<>(description)
                .column(ModelDescriptionConstants.NAME, NAME, (cell, type, row, meta) -> row.get(
                        ModelDescriptionConstants.NAME).asString())
                .button(resources.constants().add(), (event, api) -> {
                    // dialog.open();
                })
                .button(resources.constants().remove(), SELECTED_SINGLE, (event, api) -> {
                    ModelNode selectedRow = api.selectedRow();
                    if (selectedRow != null) {
                        presenter.removePath(selectedRow.get(ModelDescriptionConstants.NAME).asString());
                    }
                })
                .build();
        table = new ModelNodeTable<>(PATHS_TABLE, securityContext, options);

        form = new ModelNodeForm.Builder<>(PATHS_FORM, securityContext, description)
                .include("path", "read-only", "relative-to")
                .unsorted()
                .onSave((form, changedValues) -> {
                    ModelNode selectedRow = table.api().selectedRow();
                    if (selectedRow != null) {
                        presenter.savePath(selectedRow.get(ModelDescriptionConstants.NAME).asString(), changedValues);
                    }
                })
                .build();

        // @formatter:off
        Element element = new LayoutBuilder()
            .startRow()
                .header(PATHS)
                .add(info)
                .add(table.asElement(), form.asElement())
            .endRow()
        .build();
        // @formatter:on

        registerAttachable(table, form);
        initWidget(Elements.asWidget(element));

    }

    @Override
    public void attach() {
        super.attach();
        table.api().bindForm(form);
    }

    @Override
    public void setPresenter(final PathsPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(final List<ModelNode> paths) {
        table.api().add(paths).refresh(RESET);
    }
}
