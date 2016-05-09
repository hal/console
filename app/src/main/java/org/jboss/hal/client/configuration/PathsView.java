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

import java.util.List;
import javax.inject.Inject;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

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
    public PathsView(MetadataRegistry metadataRegistry,
            TableButtonFactory tableButtonFactory,
            Resources resources) {

        Metadata metadata = metadataRegistry.lookup(PathsPresenter.ROOT_TEMPLATE);
        Element info = new Elements.Builder().p().textContent(metadata.getDescription().getDescription()).end().build();

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
                        PathsPresenter.ROOT_TEMPLATE, (api) -> api.selectedRow().getName(),
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
                    .header(PATHS).end()
                    .add(info)
                    .add(table.asElement())
                    .add(form.asElement())
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
