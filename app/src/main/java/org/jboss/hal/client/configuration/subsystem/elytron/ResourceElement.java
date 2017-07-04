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
package org.jboss.hal.client.configuration.subsystem.elytron;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jetbrains.annotations.NonNls;

import static elemental2.dom.DomGlobal.alert;
import static java.util.Collections.singletonList;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HAL_INDEX;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeList;
import static org.jboss.hal.dmr.ModelNodeHelper.storeIndex;

class ResourceElement implements IsElement<HTMLElement>, Attachable, HasPresenter<ResourceWithComplexAttribute> {

    public static class Builder {

        private final String baseId;
        private final String resource;
        private final String title;
        private final List<String> columns;
        private final Metadata metadata;
        private final TableButtonFactory tableButtonFactory;
        private final Resources resources;
        private List<String> coAttributes; // co = complex object
        private String clAttribute; // cl = complex list
        private final List<String> clColumns;
        private Function<ModelNode, String> clIdentifier;

        public Builder(String baseId, String resource, String title, Metadata metadata,
                TableButtonFactory tableButtonFactory, Resources resources) {
            this.baseId = baseId;
            this.resource = resource;
            this.title = title;
            this.columns = new ArrayList<>();
            this.metadata = metadata;
            this.tableButtonFactory = tableButtonFactory;
            this.resources = resources;
            this.coAttributes = new ArrayList<>();
            this.clAttribute = null;
            this.clColumns = new ArrayList<>();
            this.clIdentifier = null;
        }

        public Builder columns(@NonNls String first, @NonNls String... rest) {
            columns.addAll(Lists.asList(first, rest));
            return this;
        }

        public Builder addComplexObjectAttribute(String name) {
            coAttributes.add(name);
            return this;
        }

        public Builder setComplexListAttribute(String name, String column) {
            return setComplexListAttribute(name, singletonList(column), null);
        }

        public Builder setComplexListAttribute(String name, Iterable<String> columns) {
            return setComplexListAttribute(name, columns, null);
        }

        public Builder setComplexListAttribute(String name, Iterable<String> columns,
                Function<ModelNode, String> identifier) {
            clAttribute = name;
            Iterables.addAll(clColumns, columns);
            clIdentifier = identifier;
            return this;
        }
    }


    private final Builder builder;
    private final Table<NamedNode> table;
    private final Form<NamedNode> form;
    private final Map<String, Form<ModelNode>> coForms;
    private final Pages pages;
    private final Table<ModelNode> clTable;
    private final Form<ModelNode> clForm;
    private final HTMLElement root;

    private String selectedResource;
    private int clIndex;
    private ResourceWithComplexAttribute presenter;

    private ResourceElement(Builder builder) {
        this.builder = builder;
        this.selectedResource = null;
        this.clIndex = -1;

        HTMLElement section;
        LabelBuilder labelBuilder = new LabelBuilder();

        // main table and form
        ModelNodeTable.Builder<NamedNode> tableBuilder = new ModelNodeTable.Builder<NamedNode>(
                Ids.build(builder.baseId, Ids.TABLE_SUFFIX), builder.metadata)
                .button(builder.tableButtonFactory.add(builder.metadata.getTemplate(), table -> alert(Names.NYI)))
                .button(builder.tableButtonFactory.remove(builder.metadata.getTemplate(), table -> alert(Names.NYI)))
                .columns(builder.columns);
        if (builder.clAttribute != null) {
            tableBuilder.column(labelBuilder.label(builder.clAttribute), this::showComplexList);
        }
        table = tableBuilder.build();

        form = new ModelNodeForm.Builder<NamedNode>(Ids.build(builder.baseId, Ids.FORM_SUFFIX), builder.metadata)
                .onSave((f, changedValues) -> alert(Names.NYI))
                .build();

        // complex attributes of type OBJECT
        coForms = new HashMap<>();
        if (!builder.coAttributes.isEmpty()) {
            Tabs tabs = new Tabs();
            tabs.add(Ids.build(builder.baseId, ATTRIBUTES, Ids.TAB_SUFFIX), builder.resources.constants().attributes(),
                    form.asElement());

            for (String complexAttribute : builder.coAttributes) {
                Metadata metadata = builder.metadata.forComplexAttribute(complexAttribute);
                Form<ModelNode> form = new ModelNodeForm.Builder<>(
                        Ids.build(builder.baseId, complexAttribute, Ids.FORM_SUFFIX), metadata)
                        .onSave((f, changedValues) -> alert(Names.NYI))
                        .build();
                tabs.add(Ids.build(builder.baseId, complexAttribute, Ids.TAB_SUFFIX),
                        labelBuilder.label(complexAttribute), form.asElement());
                coForms.put(complexAttribute, form);
            }

            section = section()
                    .add(h(1).textContent(builder.title))
                    .add(p().textContent(builder.metadata.getDescription().getDescription()))
                    .addAll(table, tabs)
                    .asElement();

        } else {
            section = section()
                    .add(h(1).textContent(builder.title))
                    .add(p().textContent(builder.metadata.getDescription().getDescription()))
                    .addAll(table, form)
                    .asElement();
        }

        // complex attributes of type LIST
        if (builder.clAttribute != null) {
            pages = new Pages(mainPageId(), section);

            Metadata metadata = builder.metadata.forComplexAttribute(builder.clAttribute);
            clTable = new ModelNodeTable.Builder<>(Ids.build(builder.baseId, builder.clAttribute, Ids.TABLE_SUFFIX),
                    metadata)
                    .button(builder.tableButtonFactory.add(metadata.getTemplate(), table -> alert(Names.NYI)))
                    .button(builder.tableButtonFactory.remove(metadata.getTemplate(), table -> alert(Names.NYI)))
                    .columns(builder.clColumns)
                    .build();
            clForm = new ModelNodeForm.Builder<>(Ids.build(builder.baseId, builder.clAttribute, Ids.FORM_SUFFIX),
                    metadata)
                    .onSave((f, changedValues) -> alert(Names.NYI))
                    .build();
            HTMLElement clSection = section()
                    .add(h(1).textContent(labelBuilder.label(builder.clAttribute)))
                    .add(p().textContent(metadata.getDescription().getDescription()))
                    .addAll(clTable, clForm)
                    .asElement();

            pages.addPage(mainPageId(), complexListPageId(),
                    () -> builder.title + ": " + selectedResource,
                    () -> labelBuilder.label(builder.clAttribute),
                    clSection);
            root = pages.asElement();

        } else {
            pages = null;
            clTable = null;
            clForm = null;
            root = section;
        }
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    @Override
    public void attach() {
        table.attach();
        form.attach();
        table.bindForm(form);
        table.onSelectionChange(table -> {
            if (table.hasSelection()) {
                coForms.forEach((complexAttribute, form) ->
                        form.view(failSafeGet(table.selectedRow(), complexAttribute)));
            } else {
                selectedResource = null;
                for (Form<ModelNode> form : coForms.values()) {
                    form.clear();
                }
            }
        });

        if (clTable != null && clForm != null) {
            clTable.attach();
            clForm.attach();
            clTable.bindForm(clForm);
            clTable.onSelectionChange(table -> {
                if (table.hasSelection()) {
                    clIndex = table.selectedRow().get(HAL_INDEX).asInt();
                } else {
                    clIndex = -1;
                }
            });
        }
    }

    @Override
    public void setPresenter(ResourceWithComplexAttribute presenter) {
        this.presenter = presenter;
    }

    void update(List<NamedNode> nodes) {
        form.clear();
        table.update(nodes);

        if (pages != null) {
            if (complexListPageId().equals(pages.getCurrentId())) {
                nodes.stream()
                        .filter(node -> selectedResource.equals(node.getName()))
                        .findFirst()
                        .ifPresent(this::showComplexList);
            }
        }
    }

    private void showComplexList(NamedNode node) {
        selectedResource = node.getName();
        List<ModelNode> clNodes = failSafeList(node, builder.clAttribute);
        storeIndex(clNodes);

        if (clTable != null && clForm != null) {
            clForm.clear();
            if (builder.clIdentifier != null) {
                clTable.update(clNodes, builder.clIdentifier);
            } else {
                clTable.update(clNodes);
            }
        }
        pages.showPage(complexListPageId());
    }

    private String mainPageId() {
        return Ids.build(builder.baseId, builder.resource, Ids.PAGE_SUFFIX);
    }

    private String complexListPageId() {
        return Ids.build(builder.baseId, builder.clAttribute, Ids.PAGE_SUFFIX);
    }
}
