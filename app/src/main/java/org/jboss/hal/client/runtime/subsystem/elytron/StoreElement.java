/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.runtime.subsystem.elytron;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.PreTextItem;
import org.jboss.hal.ballroom.table.Button;
import org.jboss.hal.ballroom.table.InlineAction;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALIAS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CERTIFICATE_DETAILS;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.PAGE;
import static org.jboss.hal.resources.Ids.PAGES;
import static org.jboss.hal.resources.Ids.TABLE;

public class StoreElement implements IsElement<HTMLElement>, Attachable {

    private final Table<NamedNode> table;
    private final Form<NamedNode> form;
    private final Table<ModelNode> aliasesTable;
    private Form<ModelNode> formAlias;
    private final Pages pages;
    private StoresPresenter presenter;
    private HTMLElement root;
    private Builder builder;
    private String selectedResource;
    private PreTextItem aliasDetails;
    private Map<String, String> aliasDetailsMapping = new HashMap<>();

    private StoreElement(Builder builder) {
        this.builder = builder;

        ModelNodeTable.Builder<NamedNode> tableBuilder = new ModelNodeTable.Builder<>(id(TABLE), builder.metadata);
        builder.buttonsHandler.forEach(tableBuilder::button);

        table = tableBuilder
                .nameColumn()
                .column(new InlineAction<>(builder.resources.constants().aliases(),
                        row -> {
                            selectedResource = row.getName();
                            showAliases(builder.metadata.getTemplate(), row.getName());
                        }))
                .build();

        form = new ModelNodeForm.Builder<NamedNode>(id(FORM), builder.metadata)
                .readOnly()
                .includeRuntime()
                .build();

        HTMLElement mainSection = section()
                .add(h(1).textContent(builder.title))
                .add(p().textContent(builder.metadata.getDescription().getDescription()))
                .add(table)
                .add(form).element();

        ModelNodeTable.Builder<ModelNode> aliasTableBuilder = new ModelNodeTable.Builder<>(id(ALIAS, TABLE),
                builder.metadata);
        builder.aliasButtonsHandler.forEach(aliasTableBuilder::button);

        aliasesTable = aliasTableBuilder
                .column(ALIAS, (cell, t, row, meta) -> row.asString())
                .build();

        aliasDetails = new PreTextItem(CERTIFICATE_DETAILS);
        aliasDetails.setEnabled(false);
        formAlias = new ModelNodeForm.Builder<>(id(ALIAS, FORM), Metadata.empty())
                .readOnly()
                .unboundFormItem(aliasDetails)
                .build();

        HTMLElement aliasesSection = section()
                .add(h(1).textContent(builder.resources.constants().aliases()))
                .add(aliasesTable)
                .add(formAlias).element();

        pages = new Pages(id(PAGES), id(PAGE), mainSection);
        pages.addPage(id(PAGE), id(ALIAS, PAGE),
                () -> builder.title + ": " + selectedResource,
                () -> builder.resources.constants().aliases(), aliasesSection);

        root = section()
                .add(pages).element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void attach() {
        table.attach();
        form.attach();
        aliasesTable.attach();
        formAlias.attach();

        table.bindForm(form);

        aliasesTable.onSelectionChange(table1 -> {
            if (table1.hasSelection()) {
                String alias = table1.selectedRow().asString();
                String value = aliasDetailsMapping.get(alias);
                if (value != null) {
                    aliasDetails.setValue(value);
                } else {
                    aliasDetails.clearValue();
                }
            }
        });
    }

    @Override
    public void detach() {
        table.detach();
        form.detach();
        aliasesTable.detach();
        formAlias.detach();
    }

    public void setPresenter(StoresPresenter presenter) {
        this.presenter = presenter;
    }

    private String id(String... id) {
        return Ids.build(builder.baseId, id);
    }

    public String getSelectedResource() {
        return selectedResource;
    }

    public void update(List<NamedNode> items) {
        form.clear();
        table.update(items);
        aliasDetailsMapping.clear();
    }

    // -------------- aliases operations

    private void showAliases(AddressTemplate template, String resource) {
        presenter.readAliases(template, resource, aliases -> aliasesTable.update(aliases));
        pages.showPage(id(ALIAS, PAGE));
    }

    public void updateAliases(List<ModelNode> items) {
        aliasesTable.update(items);
        formAlias.clear();
    }

    public Table<ModelNode> getAliasesTable() {
        return aliasesTable;
    }

    void updateAliasDetails(ModelNode details) {
        String value = details.toString();
        aliasDetails.setValue(value);
        aliasDetailsMapping.put(details.get(ALIAS).asString(), value);
    }

    static class Builder {

        private String baseId;
        private String title;
        private Resources resources;
        private Metadata metadata;
        private List<Button<NamedNode>> buttonsHandler;
        private List<Button<ModelNode>> aliasButtonsHandler;

        public Builder(String baseId, String title, Resources resources, Metadata metadata) {
            this.baseId = baseId;
            this.title = title;
            this.resources = resources;
            this.metadata = metadata;
            buttonsHandler = new ArrayList<>();
            aliasButtonsHandler = new ArrayList<>();
        }

        Builder addButtonHandler(Button<NamedNode> button) {
            buttonsHandler.add(button);
            return this;
        }

        Builder addAliasButtonHandler(Button<ModelNode> button) {
            aliasButtonsHandler.add(button);
            return this;
        }

        StoreElement build() {
            return new StoreElement(this);
        }
    }
}
