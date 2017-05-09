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
package org.jboss.hal.client.tools;

import java.util.List;
import javax.inject.Inject;

import elemental.dom.Element;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Scope;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.extension.ExtensionRegistry;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.tools.ExtensionPresenter.RESOURCES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * @author Harald Pehl
 */
public class ExtensionView extends HalViewImpl implements ExtensionPresenter.MyView {

    private final Table<NamedNode> table;
    private final Form<NamedNode> form;
    private ExtensionPresenter presenter;

    @Inject
    public ExtensionView(final ExtensionRegistry extensionRegistry, final Resources resources) {
        Metadata metadata = Metadata.staticDescription(RESOURCES.extension());

        table = new ModelNodeTable.Builder<NamedNode>(Ids.EXTENSION_TABLE, metadata)
                .button(resources.constants().add(), table -> presenter.addExtension())
                .button(resources.constants().remove(), table -> presenter.removeExtension(table.selectedRow()),
                        Scope.SELECTED)
                .button(resources.constants().register(), table -> presenter.registerExtension(table.selectedRow()),
                        Scope.SELECTED)
                .columns(NAME, "script")
                .column("active", resources.constants().active(), (cell, type, row, meta) -> {
                    //noinspection HardCodedStringLiteral
                    return extensionRegistry.isRegistered(row.getName())
                            ? "<span class=\"fa fa-check\"></span>"
                            : "<span class=\"pficon pficon-close\"></span>";
                })
                .build();
        registerAttachable(table);

        form = new ModelNodeForm.Builder<NamedNode>(Ids.EXTENSION_FORM, metadata)
                .include(NAME, DESCRIPTION, "script", "styles")
                .unsorted()
                .onSave((form, changedValues) -> presenter.saveExtension(form.getModel().getName(), changedValues))
                .build();
        registerAttachable(form);

        // @formatter:off
        Element root = new LayoutBuilder()
            .row()
                .column()
                    .header(Names.EXTENSIONS).end()
                    .p().textContent(resources.constants().extensionDescription()).end()
                    .add(table)
                    .add(form)
                .end()
            .end()
        .build();
        // @formatter:on

        initElement(root);
    }

    @Override
    public void setPresenter(final ExtensionPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void attach() {
        super.attach();
        table.bindForm(form);
    }

    @Override
    public void update(final List<NamedNode> extensions) {
        form.clear();
        table.update(extensions);
    }
}
