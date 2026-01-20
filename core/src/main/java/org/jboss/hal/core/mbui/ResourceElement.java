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
package org.jboss.hal.core.mbui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jboss.elemento.Elements;
import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItemProvider;
import org.jboss.hal.ballroom.form.FormValidation;
import org.jboss.hal.ballroom.table.Column;
import org.jboss.hal.ballroom.table.InlineAction;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.ComplexAttributeOperations;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Callback;

import com.google.common.collect.Iterables;

import elemental2.dom.HTMLElement;

import static java.util.Collections.singletonList;

import static elemental2.dom.DomGlobal.alert;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeBoolean;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeList;
import static org.jboss.hal.dmr.ModelNodeHelper.storeIndex;

/**
 * Master detail element for a resource with support for n complex attributes of type {@code OBJECT} and one complex attribute
 * of type {@code LIST}.
 *
 * <p>
 * This class provides the following features:
 * </p>
 * <ul>
 * <li>Table for the main resource with add and remove buttons.</li>
 * <li>Form with all simple configuration attributes of the main resource.</li>
 * <li>A tab for each complex attribute of type {@code OBJECT}. The tab contains a form to add, save, reset and remove the
 * complex attribute (remove is only available if the complex attribute is not required).</li>
 * <li>If there's a complex attribute of type {@code LIST}, the table contains a link to a sub-page. The sub-page contains a
 * table and a form to CRUD the elements of the complex attribute. Only simple attributes in the complex attribute of type
 * {@code LIST} are supported.</li>
 * <li>All CRUD actions are delegated to {@link CrudOperations} and {@link ComplexAttributeOperations}</li>
 * </ul>
 *
 * <p>
 * Multiple complex attributes of type {@code LIST} or nested complex attributes are not supported.
 * </p>
 */
public class ResourceElement implements IsElement<HTMLElement>, Attachable {

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

    @SuppressWarnings("unchecked")
    private ResourceElement(Builder builder) {
        this.builder = builder;
        this.selectedResource = null;
        this.clIndex = -1;

        HTMLElement section;
        LabelBuilder labelBuilder = new LabelBuilder();

        // main table and form
        if (builder.onAdd != null) {
            builder.tableBuilder.button(builder.mbuiContext.tableButtonFactory().add(builder.metadata.getTemplate(),
                    table -> builder.onAdd.execute()));
        } else {
            builder.tableBuilder.button(builder.mbuiContext.tableButtonFactory().add(builder.metadata.getTemplate(),
                    table -> builder.mbuiContext.crud().add(Ids.build(builder.baseId, Ids.ADD), builder.type,
                            builder.metadata.getTemplate(), (name, address) -> builder.crudCallback.execute())));
        }
        builder.tableBuilder.button(builder.mbuiContext.tableButtonFactory().remove(builder.metadata.getTemplate(),
                table -> builder.mbuiContext.crud().remove(builder.type, table.selectedRow().getName(),
                        builder.metadata.getTemplate(), builder.crudCallback)));
        if (builder.clAttribute != null) {
            builder.tableBuilder.column(
                    new InlineAction<>(labelBuilder.label(builder.clAttribute), this::showComplexList));
        }
        table = builder.tableBuilder.build();

        Set<String> excludeComplexAttributes = new HashSet<>();
        builder.metadata.getDescription().attributes().forEach(prop -> {
            String name = prop.getName();
            int pos = name.indexOf('.');
            if (pos != -1) {
                String parentName = name.substring(0, pos);
                if (builder.coAttributes.containsKey(parentName) ||
                        builder.coAttributeForms.containsKey(parentName)) {
                    excludeComplexAttributes.add(name.substring(0, pos) + ".*");
                }
            }
        });

        ModelNodeForm.Builder formBuilder = new ModelNodeForm.Builder<NamedNode>(Ids.build(builder.baseId, Ids.FORM),
                builder.metadata)
                .exclude(excludeComplexAttributes)
                .prepareReset(f -> builder.mbuiContext.crud()
                        .reset(builder.type, f.getModel().getName(), builder.metadata.getTemplate(), f,
                                builder.metadata, builder.crudCallback))
                .onSave((f, changedValues) -> builder.mbuiContext.crud().save(builder.type, f.getModel().getName(),
                        builder.metadata.getTemplate(), changedValues, builder.crudCallback));
        builder.customFormItems.forEach(formBuilder::customFormItem);

        form = formBuilder.build();

        // complex attributes of type OBJECT
        coForms = new HashMap<>();
        if (!builder.coAttributes.isEmpty() || !builder.coAttributeForms.isEmpty()) {
            Tabs tabs = new Tabs(Ids.build(builder.baseId, Ids.TAB_CONTAINER));
            tabs.add(Ids.build(builder.baseId, ATTRIBUTES, Ids.TAB),
                    builder.mbuiContext.resources().constants().attributes(), form.element());

            for (String complexAttribute : builder.coAttributes.keySet()) {
                // is the complex attribute *itself* required?
                boolean requiredComplexAttribute = false;
                ModelNode attribute = builder.metadata.getDescription().attributes().get(complexAttribute);
                if (attribute.isDefined()) {
                    requiredComplexAttribute = failSafeBoolean(attribute, REQUIRED);
                }

                Callback callback;
                String type = labelBuilder.label(complexAttribute);
                Metadata metadata = builder.metadata.forComplexAttribute(complexAttribute);

                // does the complex attribute *contain* required attributes?
                List<Property> requiredAttributes = metadata.getDescription().attributes().required();
                if (requiredAttributes.isEmpty()) {
                    callback = () -> builder.mbuiContext.ca().add(selectedResource, complexAttribute, type,
                            metadata.getTemplate(), null, builder.crudCallback);
                } else {
                    callback = () -> builder.mbuiContext.ca()
                            .add(Ids.build(builder.baseId, complexAttribute, Ids.ADD),
                                    selectedResource, complexAttribute, type, metadata.getTemplate(),
                                    builder.crudCallback);
                }

                Supplier<Operation> ping = () -> {
                    Operation op = null;
                    if (selectedResource != null) {
                        ResourceAddress address = builder.metadata.getTemplate()
                                .resolve(builder.mbuiContext.statementContext(), selectedResource);
                        op = new Operation.Builder(address, READ_ATTRIBUTE_OPERATION)
                                .param(NAME, complexAttribute)
                                .build();
                    }
                    return op;
                };
                ModelNodeForm.Builder<ModelNode> coFormBuilder = new ModelNodeForm.Builder<>(
                        Ids.build(builder.baseId, complexAttribute, Ids.FORM), metadata)
                        .singleton(ping, callback)
                        .onSave((f, changedValues) -> builder.mbuiContext.ca()
                                .save(selectedResource, complexAttribute, type,
                                        metadata.getTemplate(), changedValues, builder.crudCallback))
                        .prepareReset(f -> builder.mbuiContext.ca().reset(selectedResource, complexAttribute, type,
                                metadata.getTemplate(), f, new Form.FinishReset<ModelNode>(f) {
                                    @Override
                                    public void afterReset(Form<ModelNode> form) {
                                        builder.crudCallback.execute();
                                    }
                                }));
                if (!requiredComplexAttribute) {
                    coFormBuilder.prepareRemove(
                            f -> builder.mbuiContext.ca().remove(selectedResource, complexAttribute, type,
                                    metadata.getTemplate(), new Form.FinishRemove<ModelNode>(f) {
                                        @Override
                                        public void afterRemove(Form<ModelNode> form) {
                                            builder.crudCallback.execute();
                                        }
                                    }));
                }
                Form<ModelNode> form = coFormBuilder.build();
                FormValidation<ModelNode> coFormValidation = builder.coAttributes.get(complexAttribute);
                if (coFormValidation != null) {
                    form.addFormValidation(coFormValidation);
                }

                tabs.add(Ids.build(builder.baseId, complexAttribute, Ids.TAB), type, form.element());
                coForms.put(complexAttribute, form);
            }

            for (String complexAttribute : builder.coAttributeForms.keySet()) {
                Form<ModelNode> form = builder.coAttributeForms.get(complexAttribute);
                String type = labelBuilder.label(complexAttribute);
                tabs.add(Ids.build(builder.baseId, complexAttribute, Ids.TAB), type, form.element());
                coForms.put(complexAttribute, form);
            }

            section = section()
                    .add(h(1).textContent(builder.type))
                    .add(p().textContent(builder.metadata.getDescription().getDescription()))
                    .addAll(table, tabs).element();

        } else {
            section = section()
                    .add(h(1).textContent(builder.type))
                    .add(p().textContent(builder.metadata.getDescription().getDescription()))
                    .addAll(table, form).element();
        }

        // complex attributes of type LIST
        if (builder.clAttribute != null) {
            pages = new Pages(Ids.build(builder.baseId, Ids.PAGES), mainPageId(), section);

            Metadata metadata = builder.metadata.forComplexAttribute(builder.clAttribute);
            clTable = new ModelNodeTable.Builder<>(Ids.build(builder.baseId, builder.clAttribute, Ids.TABLE), metadata)
                    .button(builder.mbuiContext.tableButtonFactory().add(metadata.getTemplate(),
                            table -> builder.mbuiContext.ca()
                                    .listAdd(Ids.build(builder.baseId, builder.clAttribute, Ids.ADD),
                                            selectedResource, builder.clAttribute,
                                            labelBuilder.label(builder.clAttribute),
                                            metadata.getTemplate(), builder.clAddAttributes, builder.crudCallback)))
                    .button(builder.mbuiContext.tableButtonFactory().remove(metadata.getTemplate(),
                            table -> builder.mbuiContext.ca().remove(selectedResource, builder.clAttribute,
                                    labelBuilder.label(builder.clAttribute), clIndex, metadata.getTemplate(),
                                    builder.crudCallback)))
                    .columns(builder.clColumns)
                    .build();
            clForm = new ModelNodeForm.Builder<>(Ids.build(builder.baseId, builder.clAttribute, Ids.FORM), metadata)
                    .onSave((f, changedValues) -> builder.mbuiContext.ca().save(selectedResource, builder.clAttribute,
                            labelBuilder.label(builder.clAttribute), clIndex, metadata.getTemplate(), changedValues,
                            builder.crudCallback))
                    .build();
            HTMLElement clSection = section()
                    .add(h(1).textContent(labelBuilder.label(builder.clAttribute)))
                    .add(p().textContent(metadata.getDescription().getDescription()))
                    .addAll(clTable, clForm).element();

            pages.addPage(mainPageId(), complexListPageId(),
                    () -> builder.type + ": " + selectedResource,
                    () -> labelBuilder.label(builder.clAttribute),
                    clSection);
            root = pages.element();

        } else {
            pages = null;
            clTable = null;
            clForm = null;
            root = section;
        }
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void attach() {
        table.attach();
        table.enableButton(1, false);
        form.attach();
        table.bindForm(form);
        table.onSelectionChange(table -> {
            if (table.hasSelection()) {
                selectedResource = table.selectedRow().getName();
                coForms.forEach((complexAttribute, form) -> form.view(failSafeGet(table.selectedRow(), complexAttribute)));
            } else {
                selectedResource = null;
                for (Form<ModelNode> form : coForms.values()) {
                    form.clear();
                }
            }
            table.enableButton(1, table.hasSelection());
        });
        coForms.forEach((s, form1) -> form1.attach());
        if (Iterables.isEmpty(form.getFormItems())) {
            Elements.setVisible(form.element(), false);
        }

        if (clTable != null && clForm != null) {
            clTable.attach();
            clForm.attach();
            clTable.bindForm(clForm);
            clTable.onSelectionChange(table -> {
                clTable.enableButton(1, clTable.hasSelection());
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
        for (Form<ModelNode> form : coForms.values()) {
            form.clear();
        }
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
            clTable.enableButton(1, clTable.hasSelection());
            pages.showPage(complexListPageId());
        }
    }

    private String mainPageId() {
        return Ids.build(builder.baseId, builder.resource, Ids.PAGE);
    }

    private String complexListPageId() {
        return Ids.build(builder.baseId, builder.clAttribute, Ids.PAGE);
    }

    public Form<NamedNode> getForm() {
        return form;
    }

    public Table<NamedNode> getTable() {
        return table;
    }

    /** @return The form that represents the complex attribute of type LIST */
    public Form<ModelNode> getFormComplexList() {
        return clForm;
    }

    @SuppressWarnings("unused")
    public static class Builder {

        private final String baseId;
        private final String resource;
        private final Metadata metadata;
        private final MbuiContext mbuiContext;
        private final ModelNodeTable.Builder<NamedNode> tableBuilder;
        private String type;
        private Map<String, FormItemProvider> customFormItems = new HashMap<>();
        private Map<String, FormValidation> coAttributes; // co = complex object
        private Map<String, Form<ModelNode>> coAttributeForms; // co = complex object
        private String clAttribute; // cl = complex list
        private final List<String> clColumns;
        private final List<String> clAddAttributes;
        private Function<ModelNode, String> clIdentifier;
        private Callback onAdd;
        private Callback crudCallback;

        public Builder(String baseId, String resource, Metadata metadata, MbuiContext mbuiContext) {
            this.baseId = baseId;
            this.resource = resource;
            this.metadata = metadata;
            this.mbuiContext = mbuiContext;

            this.tableBuilder = new ModelNodeTable.Builder<>(Ids.build(baseId, Ids.TABLE), metadata);
            this.type = new LabelBuilder().label(resource);
            this.coAttributes = new HashMap<>();
            this.coAttributeForms = new HashMap<>();
            this.clAttribute = null;
            this.clColumns = new ArrayList<>();
            this.clAddAttributes = new ArrayList<>();
            this.clIdentifier = null;
            this.onAdd = null;
            this.crudCallback = () -> alert(Names.NYI);
        }

        /**
         * Overrides the human readable type of the main resource which is built by default using the {@link LabelBuilder}.
         */
        public Builder type(String type) {
            this.type = type;
            return this;
        }

        /** Columns for the main table. */
        public Builder columns(String first, String... rest) {
            tableBuilder.columns(first, rest);
            return this;
        }

        public Builder column(String name, Column.RenderCallback<NamedNode, String> render) {
            tableBuilder.column(name, render);
            return this;
        }

        public Builder nameColumn() {
            tableBuilder.nameColumn();
            return this;
        }

        /**
         * Adds a complex attribute of type {@code OBJECT}. The operation checks whether the resource contains the complex
         * attribute.
         */
        public Builder addComplexObjectAttribute(String name) {
            coAttributes.put(name, null);
            return this;
        }

        /**
         * Adds a custom form for a complex attribute of type {@code OBJECT}
         */
        public Builder addComplexObjectAttributeForm(String name, Form<ModelNode> form) {
            coAttributeForms.put(name, form);
            return this;
        }

        /**
         * Sets a custom form item
         */
        public Builder customFormItem(String attribute, FormItemProvider formItemProvider) {
            customFormItems.put(attribute, formItemProvider);
            return this;
        }

        /**
         * Adds a complex attribute of type {@code OBJECT}. The operation checks whether the resource contains the complex
         * attribute. Also adds a form validation for the specific complex attribute form.
         */
        public Builder addComplexObjectAttribute(String name, FormValidation formValidation) {
            coAttributes.put(name, formValidation);
            return this;
        }

        /**
         * Sets the complex attribute of type {@code LIST}.
         *
         * @param name The name of the complex attribute
         * @param column The column for the table on the sub-page
         */
        public Builder setComplexListAttribute(String name, String column) {
            return setComplexListAttribute(name, singletonList(column), Collections.emptyList(), null);
        }

        /**
         * Sets the complex attribute of type {@code LIST}.
         *
         * @param name The name of the complex attribute
         * @param column The column for the table on the sub-page
         * @param addAttributes attributes for the add resource dialog
         */
        public Builder setComplexListAttribute(String name, String column, Iterable<String> addAttributes) {
            return setComplexListAttribute(name, singletonList(column), addAttributes, null);
        }

        /**
         * Sets the complex attribute of type {@code LIST}.
         *
         * @param name The name of the complex attribute
         * @param column The column for the table on the sub-page
         * @param identifier function to maintain the selection after updating the table entries
         */
        public Builder setComplexListAttribute(String name, String column, Function<ModelNode, String> identifier) {
            return setComplexListAttribute(name, singletonList(column), Collections.emptyList(), identifier);
        }

        /**
         * Sets the complex attribute of type {@code LIST}.
         *
         * @param name The name of the complex attribute
         * @param column The column for the table on the sub-page
         * @param addAttributes attributes for the add resource dialog
         * @param identifier function to maintain the selection after updating the table entries
         */
        public Builder setComplexListAttribute(String name, String column, Iterable<String> addAttributes,
                Function<ModelNode, String> identifier) {
            return setComplexListAttribute(name, singletonList(column), addAttributes, identifier);
        }

        /**
         * Sets the complex attribute of type {@code LIST}.
         *
         * @param name The name of the complex attribute
         * @param columns The columns for the table on the sub-page
         */
        public Builder setComplexListAttribute(String name, Iterable<String> columns) {
            return setComplexListAttribute(name, columns, Collections.emptyList(), null);
        }

        /**
         * Sets the complex attribute of type {@code LIST}.
         *
         * @param name The name of the complex attribute
         * @param columns The columns for the table on the sub-page
         * @param addAttributes attributes for the add resource dialog
         */
        public Builder setComplexListAttribute(String name, Iterable<String> columns, Iterable<String> addAttributes) {
            return setComplexListAttribute(name, columns, addAttributes, null);
        }

        /**
         * Sets the complex attribute of type {@code LIST}.
         *
         * @param name The name of the complex attribute
         * @param columns The columns for the table on the sub-page
         * @param identifier function to maintain the selection after updating the table entries
         */
        public Builder setComplexListAttribute(String name, Iterable<String> columns,
                Function<ModelNode, String> identifier) {
            return setComplexListAttribute(name, columns, Collections.emptyList(), identifier);
        }

        /**
         * Sets the complex attribute of type {@code LIST}.
         *
         * @param name The name of the complex attribute
         * @param columns The columns for the table on the sub-page
         * @param addAttributes attributes for the add resource dialog
         * @param identifier function to maintain the selection after updating the table entries
         */
        public Builder setComplexListAttribute(String name, Iterable<String> columns,
                Iterable<String> addAttributes, Function<ModelNode, String> identifier) {
            clAttribute = name;
            Iterables.addAll(clColumns, columns);
            Iterables.addAll(clAddAttributes, addAttributes);
            clIdentifier = identifier;
            return this;
        }

        /**
         * Defines the callback which is executed to add the main resource. If not specified
         * {@link CrudOperations#add(String, String, AddressTemplate, CrudOperations.AddCallback)} is used (which opens an
         * {@linkplain org.jboss.hal.core.mbui.dialog.AddResourceDialog add-resource-dialog} with all required properties of the
         * main resource).
         * <p>
         * Use this method if you need to customize the add-resource-dialog somehow, e.g. if there's a required complex
         * attribute which has to be specified).
         */
        public Builder onAdd(Callback callback) {
            this.onAdd = callback;
            return this;
        }

        /** Defines the callback which is used after all CRUD actions. */
        public Builder onCrud(Callback callback) {
            this.crudCallback = callback;
            return this;
        }

        public ResourceElement build() {
            return new ResourceElement(this);
        }
    }
}
