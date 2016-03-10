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
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.ballroom.layout.LayoutBuilder;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptions;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityFramework;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import javax.inject.Inject;
import java.util.List;

import static org.jboss.hal.ballroom.table.Api.RefreshMode.RESET;
import static org.jboss.hal.resources.Names.PATHS;

/**
 * @author Harald Pehl
 */
public class PathsView extends PatternFlyViewImpl implements PathsPresenter.MyView {

    private DataTable<NamedNode> table;
    private ModelNodeForm<NamedNode> form;
    private PathsPresenter presenter;

    @Inject
    public PathsView(SecurityFramework securityFramework,
            ResourceDescriptions descriptions,
            Capabilities capabilities,
            TableButtonFactory tableButtonFactory,
            Resources resources) {

        SecurityContext securityContext = securityFramework.lookup(PathsPresenter.ROOT_TEMPLATE);
        ResourceDescription description = descriptions.lookup(PathsPresenter.ROOT_TEMPLATE);
        Metadata metadata = new Metadata(securityContext, description, capabilities);

        Element info = new Elements.Builder().p().textContent(description.getDescription()).end().build();

        //noinspection ConstantConditions
        Options<NamedNode> options = new ModelNodeTable.Builder<NamedNode>(metadata)

                .column(ModelDescriptionConstants.NAME, resources.constants().name(),
                        (cell, type, row, meta) -> row.get(ModelDescriptionConstants.NAME).asString())

                .button(tableButtonFactory.add(
                        IdBuilder.build(Ids.PATHS_TABLE, "add"),
                        Names.PATH,
                        PathsPresenter.ROOT_TEMPLATE,
                        () -> presenter.loadPaths()))

                .button(tableButtonFactory.remove(
                        Names.PATH,
                        () -> table.api().selectedRow().getName(),
                        PathsPresenter.ROOT_TEMPLATE,
                        () -> presenter.loadPaths()))

                .build();
        table = new ModelNodeTable<>(Ids.PATHS_TABLE, options);

        form = new ModelNodeForm.Builder<NamedNode>(Ids.PATHS_FORM, metadata)
                .include("path", "read-only", "relative-to")
                .unsorted()
                .onSave((form, changedValues) -> {
                    NamedNode selectedRow = table.api().selectedRow();
                    if (selectedRow != null) {
                        presenter.savePath(selectedRow.getName(), changedValues);
                    }
                })
                .build();

        // @formatter:off
        Element element = new LayoutBuilder()
            .row()
                .column()
                    .header(PATHS)
                    .add(info, table.asElement(), form.asElement())
                .end()
            .end()
        .build();
        // @formatter:on

        registerAttachable(table, form);
        initElement(element);

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
    public void update(final List<NamedNode> paths) {
        table.api().clear().add(paths).refresh(RESET);
        form.clear(); // TODO restore selection, set selection to newly created item or clear selection after remove
    }
}
