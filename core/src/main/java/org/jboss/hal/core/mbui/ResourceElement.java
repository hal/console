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
package org.jboss.hal.core.mbui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.Iterables;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Column;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.ComplexAttributeOperations;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;
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

/**
 * Master detail element for a resource with support for n complex attributes of type {@code OBJECT} and one
 * complex attribute of type {@code LIST}.
 *
 * <p>This class provides the following features:</p>
 * <ul>
 * <li>Table for the main resource with add and remove buttons.</li>
 * <li>Form with all simple configuration attributes of the main resource.</li>
 * <li>A tab for each complex attribute of type {@code OBJECT}. The tab contains a form to add, save, reset and remove
 * the complex attribute.</li>
 * <li>If there's a complex attribute of type {@code LIST}, the table contains a link to a sub-page. The sub-page
 * contains a table and a form to CRUD the elements of the complex attribute. Only simple attributes in the complex
 * attribute of type {@code LIST} are supported.</li>
 * <li>All CRUD actions are delegated to {@link CrudOperations} and {@link ComplexAttributeOperations}</li>
 * </ul>
 *
 * <p>Multiple complex attributes of type {@code LIST} or nested complex attributes are not supported.</p>
 */
public class ResourceElement implements IsElement<HTMLElement>, Attachable {

    private static class ComplexObjectAttribute {

        final String name;
        final Supplier<Operation> ping;

        private ComplexObjectAttribute(String name, Supplier<Operation> ping) {
            this.name = name;
            this.ping = ping;
        }
    }


    public static class Builder {

        private final String baseId;
        private final String resource;
        private final Metadata metadata;
        private final CrudOperations crud;
        private final ComplexAttributeOperations ca;
        private final TableButtonFactory tableButtonFactory;
        private final Resources resources;
        private final ModelNodeTable.Builder<NamedNode> tableBuilder;
        private String type;
        private List<ComplexObjectAttribute> coAttributes; // co = complex object
        private String clAttribute; // cl = complex list
        private final List<String> clColumns;
        private final List<String> clAddAttributes;
        private Function<ModelNode, String> clIdentifier;
        private Callback callback;

        public Builder(String baseId, String resource, Metadata metadata, MbuiContext mbuiContext) {
            this(baseId, resource, metadata, mbuiContext.crud(), mbuiContext.ca(), mbuiContext.tableButtonFactory(),
                    mbuiContext.resources());
        }

        public Builder(String baseId, String resource, Metadata metadata, CrudOperations crud,
                ComplexAttributeOperations ca, TableButtonFactory tableButtonFactory, Resources resources) {
            this.baseId = baseId;
            this.resource = resource;
            this.metadata = metadata;
            this.crud = crud;
            this.ca = ca;
            this.tableButtonFactory = tableButtonFactory;
            this.resources = resources;

            this.tableBuilder = new ModelNodeTable.Builder<>(Ids.build(baseId, Ids.TABLE_SUFFIX), metadata);
            this.type = new LabelBuilder().label(resource);
            this.coAttributes = new ArrayList<>();
            this.clAttribute = null;
            this.clColumns = new ArrayList<>();
            this.clAddAttributes = new ArrayList<>();
            this.clIdentifier = null;
            this.callback = () -> alert(Names.NYI);
        }

        /**
         * Overrides the human readable type of the main resource which is built by default using the {@link
         * LabelBuilder}.
         */
        public Builder type(String type) {
            this.type = type;
            return this;
        }

        /** Columns for the main table. */
        public Builder columns(@NonNls String first, @NonNls String... rest) {
            tableBuilder.columns(first, rest);
            return this;
        }

        public Builder column(String name, Column.RenderCallback<NamedNode, ?> render) {
            tableBuilder.column(name, render);
            return this;
        }

        /**
         * Adds a complex attribute of type {@code OBJECT}. The operation checks whether the resource contains the
         * complex attribute.
         */
        public Builder addComplexObjectAttribute(String name, Supplier<Operation> ping) {
            coAttributes.add(new ComplexObjectAttribute(name, ping));
            return this;
        }

        /**
         * Sets the complex attribute of type {@code LIST}.
         *
         * @param name   The name of the complex attribute
         * @param column The column for the table on the sub-page
         */
        public Builder setComplexListAttribute(String name, String column) {
            return setComplexListAttribute(name, singletonList(column), Collections.emptyList(), null);
        }

        /**
         * Sets the complex attribute of type {@code LIST}.
         *
         * @param name          The name of the complex attribute
         * @param column        The column for the table on the sub-page
         * @param addAttributes attributes for the add resource dialog
         */
        public Builder setComplexListAttribute(String name, String column, Iterable<String> addAttributes) {
            return setComplexListAttribute(name, singletonList(column), addAttributes, null);
        }

        /**
         * Sets the complex attribute of type {@code LIST}.
         *
         * @param name       The name of the complex attribute
         * @param column     The column for the table on the sub-page
         * @param identifier function to maintain the selection after updating the table entries
         */
        public Builder setComplexListAttribute(String name, String column, Function<ModelNode, String> identifier) {
            return setComplexListAttribute(name, singletonList(column), Collections.emptyList(), identifier);
        }

        /**
         * Sets the complex attribute of type {@code LIST}.
         *
         * @param name          The name of the complex attribute
         * @param column        The column for the table on the sub-page
         * @param addAttributes attributes for the add resource dialog
         * @param identifier    function to maintain the selection after updating the table entries
         */
        public Builder setComplexListAttribute(String name, String column, Iterable<String> addAttributes,
                Function<ModelNode, String> identifier) {
            return setComplexListAttribute(name, singletonList(column), addAttributes, identifier);
        }

        /**
         * Sets the complex attribute of type {@code LIST}.
         *
         * @param name    The name of the complex attribute
         * @param columns The columns for the table on the sub-page
         */
        public Builder setComplexListAttribute(String name, Iterable<String> columns) {
            return setComplexListAttribute(name, columns, Collections.emptyList(), null);
        }

        /**
         * Sets the complex attribute of type {@code LIST}.
         *
         * @param name          The name of the complex attribute
         * @param columns       The columns for the table on the sub-page
         * @param addAttributes attributes for the add resource dialog
         */
        public Builder setComplexListAttribute(String name, Iterable<String> columns, Iterable<String> addAttributes) {
            return setComplexListAttribute(name, columns, addAttributes, null);
        }

        /**
         * Sets the complex attribute of type {@code LIST}.
         *
         * @param name       The name of the complex attribute
         * @param columns    The columns for the table on the sub-page
         * @param identifier function to maintain the selection after updating the table entries
         */
        public Builder setComplexListAttribute(String name, Iterable<String> columns,
                Function<ModelNode, String> identifier) {
            return setComplexListAttribute(name, columns, Collections.emptyList(), identifier);
        }

        /**
         * Sets the complex attribute of type {@code LIST}.
         *
         * @param name          The name of the complex attribute
         * @param columns       The columns for the table on the sub-page
         * @param addAttributes attributes for the add resource dialog
         * @param identifier    function to maintain the selection after updating the table entries
         */
        public Builder setComplexListAttribute(String name, Iterable<String> columns,
                Iterable<String> addAttributes, Function<ModelNode, String> identifier) {
            clAttribute = name;
            Iterables.addAll(clColumns, columns);
            Iterables.addAll(clAddAttributes, addAttributes);
            clIdentifier = identifier;
            return this;
        }

        /** Defines the callback which is used after all CRUD actions. */
        public Builder onCrud(Callback callback) {
            this.callback = callback;
            return this;
        }

        public ResourceElement build() {
            return new ResourceElement(this);
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

    private ResourceElement(Builder builder) {
        this.builder = builder;
        this.selectedResource = null;
        this.clIndex = -1;

        HTMLElement section;
        LabelBuilder labelBuilder = new LabelBuilder();

        // main table and form
        builder.tableBuilder
                .button(builder.tableButtonFactory.add(builder.metadata.getTemplate(),
                        table -> builder.crud.add(Ids.build(builder.baseId, Ids.ADD_SUFFIX), builder.type,
                                builder.metadata.getTemplate(), (name, address) -> builder.callback.execute())))
                .button(builder.tableButtonFactory.remove(builder.metadata.getTemplate(),
                        table -> builder.crud.remove(builder.type, table.selectedRow().getName(),
                                builder.metadata.getTemplate(), builder.callback)));
        if (builder.clAttribute != null) {
            builder.tableBuilder.column(labelBuilder.label(builder.clAttribute), this::showComplexList);
        }
        table = builder.tableBuilder.build();

        form = new ModelNodeForm.Builder<NamedNode>(Ids.build(builder.baseId, Ids.FORM_SUFFIX), builder.metadata)
                .onSave((f, changedValues) -> builder.crud.save(builder.type, f.getModel().getName(),
                        builder.metadata.getTemplate(), changedValues, builder.callback))
                .build();

        // complex attributes of type OBJECT
        coForms = new HashMap<>();
        if (!builder.coAttributes.isEmpty()) {
            Tabs tabs = new Tabs();
            tabs.add(Ids.build(builder.baseId, ATTRIBUTES, Ids.TAB_SUFFIX), builder.resources.constants().attributes(),
                    form.asElement());

            for (ComplexObjectAttribute complexAttribute : builder.coAttributes) {
                Callback callback;
                String type = labelBuilder.label(complexAttribute.name);
                Metadata metadata = builder.metadata.forComplexAttribute(complexAttribute.name);
                List<Property> requiredAttributes = metadata.getDescription().getRequiredAttributes(ATTRIBUTES);

                if (requiredAttributes.isEmpty()) {
                    callback = () -> builder.ca.add(selectedResource, complexAttribute.name, type,
                            metadata.getTemplate(), null, builder.callback);
                } else {
                    callback = () -> builder.ca.add(Ids.build(builder.baseId, complexAttribute.name, Ids.ADD_SUFFIX),
                            selectedResource, complexAttribute.name, type, metadata.getTemplate(), builder.callback);
                }

                Form<ModelNode> form = new ModelNodeForm.Builder<>(
                        Ids.build(builder.baseId, complexAttribute.name, Ids.FORM_SUFFIX), metadata)
                        .singleton(complexAttribute.ping, callback)
                        .onSave((f, changedValues) -> alert(Names.NYI))
                        .prepareReset(f -> builder.ca.reset(selectedResource, complexAttribute.name, type,
                                metadata.getTemplate(), f, new Form.FinishReset<ModelNode>(f) {
                                    @Override
                                    public void afterReset(Form<ModelNode> form) {
                                        builder.callback.execute();
                                    }
                                }))
                        .prepareRemove(f -> builder.ca.remove(selectedResource, complexAttribute.name, type,
                                metadata.getTemplate(), new Form.FinishRemove<ModelNode>(f) {
                                    @Override
                                    public void afterRemove(Form<ModelNode> form) {
                                        builder.callback.execute();
                                    }
                                }))
                        .build();

                tabs.add(Ids.build(builder.baseId, complexAttribute.name, Ids.TAB_SUFFIX), type, form.asElement());
                coForms.put(complexAttribute.name, form);
            }

            section = section()
                    .add(h(1).textContent(builder.type))
                    .add(p().textContent(builder.metadata.getDescription().getDescription()))
                    .addAll(table, tabs)
                    .asElement();

        } else {
            section = section()
                    .add(h(1).textContent(builder.type))
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
                    .button(builder.tableButtonFactory.add(metadata.getTemplate(),
                            table -> builder.ca.listAdd(Ids.build(builder.baseId, builder.clAttribute, Ids.ADD_SUFFIX),
                                    selectedResource, builder.clAttribute, labelBuilder.label(builder.clAttribute),
                                    metadata.getTemplate(), builder.clAddAttributes, builder.callback)))
                    .button(builder.tableButtonFactory.remove(metadata.getTemplate(),
                            table -> builder.ca.remove(selectedResource, builder.clAttribute,
                                    labelBuilder.label(builder.clAttribute), clIndex, metadata.getTemplate(),
                                    builder.callback)))
                    .columns(builder.clColumns)
                    .build();
            clForm = new ModelNodeForm.Builder<>(Ids.build(builder.baseId, builder.clAttribute, Ids.FORM_SUFFIX),
                    metadata)
                    .onSave((f, changedValues) -> builder.ca.save(selectedResource, builder.clAttribute,
                            labelBuilder.label(builder.clAttribute), clIndex, metadata.getTemplate(), changedValues,
                            builder.callback))
                    .build();
            HTMLElement clSection = section()
                    .add(h(1).textContent(labelBuilder.label(builder.clAttribute)))
                    .add(p().textContent(metadata.getDescription().getDescription()))
                    .addAll(clTable, clForm)
                    .asElement();

            pages.addPage(mainPageId(), complexListPageId(),
                    () -> builder.type + ": " + selectedResource,
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
                selectedResource = table.selectedRow().getName();
                coForms.forEach((complexAttribute, form) ->
                        form.view(failSafeGet(table.selectedRow(), complexAttribute)));
            } else {
                selectedResource = null;
                for (Form<ModelNode> form : coForms.values()) {
                    form.clear();
                }
            }
        });
        if (Iterables.isEmpty(form.getFormItems())) {
            Elements.setVisible(form.asElement(), false);
        }

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

    public void update(List<NamedNode> nodes) {
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

        if (clTable != null && clForm != null && pages != null) {
            clForm.clear();
            if (builder.clIdentifier != null) {
                clTable.update(clNodes, builder.clIdentifier);
            } else {
                clTable.update(clNodes);
            }
            pages.showPage(complexListPageId());
        }
    }

    private String mainPageId() {
        return Ids.build(builder.baseId, builder.resource, Ids.PAGE_SUFFIX);
    }

    private String complexListPageId() {
        return Ids.build(builder.baseId, builder.clAttribute, Ids.PAGE_SUFFIX);
    }
}
