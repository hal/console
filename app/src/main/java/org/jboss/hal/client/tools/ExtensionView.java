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

import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.StaticItem;
import org.jboss.hal.ballroom.table.Scope;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.Strings;
import org.jboss.hal.core.extension.Extension;
import org.jboss.hal.core.extension.ExtensionRegistry;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.tools.ExtensionPresenter.RESOURCES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SCRIPT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATUS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STYLES;

/**
 * @author Harald Pehl
 */
public class ExtensionView extends HalViewImpl implements ExtensionPresenter.MyView {

    private final ExtensionRegistry extensionRegistry;
    private final Resources resources;
    private final Table<Extension> table;
    private final Form<Extension> form;
    private final FormItem<String> statusItem;
    private ExtensionPresenter presenter;

    @Inject
    public ExtensionView(final ExtensionRegistry extensionRegistry, final Resources resources) {
        this.extensionRegistry = extensionRegistry;
        this.resources = resources;
        Metadata metadata = Metadata.staticDescription(RESOURCES.extension());

        table = new ModelNodeTable.Builder<Extension>(Ids.EXTENSION_TABLE, metadata)
                .button(resources.constants().add(), table -> presenter.addExtension())
                .button(resources.constants().remove(), table -> presenter.removeExtension(table.selectedRow()),
                        Scope.SELECTED)
                .column(SCRIPT)
                .column(STATUS, resources.constants().status(),
                        (cell, type, row, meta) -> "<span id=\"" + statusId(row) + "\"></span>") //NON-NLS
                .build();
        registerAttachable(table);

        statusItem = new StaticItem(STATUS, resources.constants().status());
        form = new ModelNodeForm.Builder<Extension>(Ids.EXTENSION_FORM, metadata)
                .readOnly()
                .include(SCRIPT, STYLES)
                .unboundFormItem(statusItem)
                .unsorted()
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
        table.onSelectionChange(table -> {
            if (table.hasSelection()) {
                extensionRegistry.ping(form.getModel(), status -> {
                    if (status >= 200 && status < 400) {
                        statusItem.setValue(resources.messages().extensionAvailable().asString());
                    } else {
                        statusItem.setValue(resources.messages().extensionNotAvailable().asString());
                    }
                });
            } else {
                statusItem.clearValue();
            }
        });
    }

    @Override
    public void update(final List<Extension> extensions) {
        form.clear();
        table.update(extensions, Extension::getScript);
        extensions.forEach(extension ->
                extensionRegistry.ping(extension, status -> {
                    Element element = Browser.getDocument().getElementById(statusId(extension));
                    if (status >= 200 && status < 400) {
                        extension.get(STATUS).set(resources.messages().extensionAvailable().asString());
                        if (element != null) {
                            element.setClassName(Icons.OK);
                        }
                    } else {
                        extension.get(STATUS).set(resources.messages().extensionNotAvailable().asString());
                        if (element != null) {
                            element.setClassName(Icons.ERROR);
                        }
                    }
                }));
    }

    private String statusId(Extension extension) {
        return Ids.build(Ids.EXTENSION, Strings.substringAfterLast(extension.getScript(), "/"), STATUS);
    }
}
