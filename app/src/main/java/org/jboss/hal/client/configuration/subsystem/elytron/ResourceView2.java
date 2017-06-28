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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.table.Button;
import org.jboss.hal.ballroom.table.ButtonHandler;
import org.jboss.hal.ballroom.table.ColumnAction;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.mbui.form.ComplexAttributeForm;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.spi.Callback;

import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Helper class to create a standard view that contains a table and a form with the attributes. It may also add
 * complex attributes as tabs.
 *
 * @author Claudio Miranda <claudio@redhat.com>
 */
public class ResourceView2 implements HasPresenter<ElytronPresenter> {

    public static class Builder {

        // required atributes
        private AddressTemplate template;
        private Callback tableRemoveCallback;
        private TableButtonFactory tableButtonFactory;
        private String primaryId;
        private String id;
        private String title;
        private ElytronView elytronView;

        // optional attributes
        private MetadataRegistry metadataRegistry;
        private CrudOperations.AddCallback tableAddCallback;
        private ButtonHandler<NamedNode> tableAddButtonHandler;
        private VerticalNavigation navigation;
        private Metadata metadata;

        public Builder(final TableButtonFactory tableButtonFactory, String primaryId, String id, String title,
                AddressTemplate template, ElytronView elytronView, final Callback tableRemoveCallback) {

            this.tableButtonFactory = tableButtonFactory;
            this.primaryId = primaryId;
            this.id = id;
            this.title = title;
            this.template = template;
            this.elytronView = elytronView;
            this.tableRemoveCallback = tableRemoveCallback;
        }

        public Builder setMetadataRegistry(final MetadataRegistry metadataRegistry) {
            this.metadataRegistry = metadataRegistry;
            return this;
        }

        public Builder setTableAddCallback(final CrudOperations.AddCallback tableAddCallback) {
            this.tableAddCallback = tableAddCallback;
            return this;
        }

        public Builder setTableAddButtonHandler(final ButtonHandler<NamedNode> tableAddButtonHandler) {
            this.tableAddButtonHandler = tableAddButtonHandler;
            return this;
        }

        public Builder setTemplate(final AddressTemplate template) {
            this.template = template;
            return this;
        }

        public Builder setNavigation(final VerticalNavigation navigation) {
            this.navigation = navigation;
            return this;
        }

        public Builder setMetadata(final Metadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder setColumnAction(final ColumnAction<NamedNode> columnAction) {
            return this;
        }

        public ResourceView2 build() {
            return new ResourceView2(this);
        }

    }


    private static class PageBean {

        private String attribute;
        private ButtonHandler<NamedNode> tableAddButtonHandler;

        public PageBean(final String attribute, final ButtonHandler<NamedNode> tableAddButtonHandler) {
            this.attribute = attribute;
            this.tableAddButtonHandler = tableAddButtonHandler;
        }
    }


    private Table<NamedNode> table;
    private Form<NamedNode> form;
    private ElytronPresenter presenter;
    private HTMLElement elementLayout;
    private Metadata metadata;
    private Map<String, List<FormItem>> includesComplexAttributes = new HashMap<>();
    private Map<String, ModelNodeForm<NamedNode>> complexAttributeForms = new HashMap<>();
    private List<PageBean> includesComplexAttributesPages = new ArrayList<>();
    private Map<String, ResourceView2> complexAttributesPages = new HashMap<>();
    private Builder builder;
    private String primaryLevelIcon;
    private Pages pages;
    private Map<String, String> pagesId = new HashMap<>();
    private boolean subPage;
    private String selectedParentResource;

    private ResourceView2(Builder builder) {
        this.builder = builder;

        if (builder.metadataRegistry != null) {
            this.metadata = builder.metadataRegistry.lookup(builder.template);
        } else {
            this.metadata = builder.metadata;
        }

    }

    /**
     * Defines this resource to be added as primary level for the navigation object.
     *
     * @param icon The string of the icon, example: <pre>"fa fa-file-o"</pre>
     */
    public ResourceView2 primaryLevel(String icon) {
        primaryLevelIcon = icon;
        return this;
    }

    /**
     * Constructs a ModelNodeForm from the complex attribute and adds it as tab to the detail table. For example, the
     * "credential-reference" attribute of "credential-reference" resource of elytron subsystem.
     *
     * @param complexAttributeName The OBJECT attribute name.
     */
    public ResourceView2 addComplexAttributeAsTab(String complexAttributeName) {
        includesComplexAttributes.put(complexAttributeName, Collections.emptyList());
        return this;
    }

    public ResourceView2 markAsSubPage() {
        subPage = true;
        return this;
    }

    /**
     * Constructs a ModelNodeForm from the complex attribute and adds it as tab to the detail table. For example, the
     * "credential-reference" attribute of "credential-reference" resource of elytron subsystem. Also, the user may
     * add a list of unbound form items, that requires custom handling.
     *
     * @param complexAttributeName The OBJECT attribute name.
     * @param unboundFormItems     The list of custom form items.
     */
    public ResourceView2 addComplexAttributeAsTab(String complexAttributeName, List<FormItem> unboundFormItems) {
        includesComplexAttributes.put(complexAttributeName, unboundFormItems);
        return this;
    }

    /**
     * @param complexAttributeName The complex attribute name.
     *
     * @return
     */
    public ResourceView2 addComplexAttributeAsPage(String complexAttributeName) {
        addComplexAttributeAsPage(complexAttributeName, null);
        return this;
    }

    /**
     * @param complexAttributeName  The complex attribute name.
     * @param tableAddButtonHandler Call to save the add payload or the launch of a custom dialog for the nested
     *                              resource.
     *
     * @return
     */
    public ResourceView2 addComplexAttributeAsPage(String complexAttributeName,
            final ButtonHandler<NamedNode> tableAddButtonHandler) {
        includesComplexAttributesPages.add(new PageBean(complexAttributeName, tableAddButtonHandler));
        return this;
    }

    public ResourceView2 create() {
        this.form = createForm(builder.id, metadata, builder.title, builder.template);
        ModelNodeTable.Builder<NamedNode> tableBuilder = createTable(builder.id, metadata, builder.template,
                builder.title, builder.tableButtonFactory);

        // must initialize tabs instance as it is used in an inner class
        final Tabs tabs = new Tabs();

        if (!includesComplexAttributes.isEmpty()) {
            // adds the main form as the first tab
            tabs.add(Ids.build(builder.id, ATTRIBUTES, Ids.TAB_SUFFIX), "Attributes", form.asElement());
        }

        // create the form to support the complex attribute as tab
        includesComplexAttributes.forEach((attribute, unboundFormItems) -> {
            String complexAttributeLabel = new LabelBuilder().label(attribute);
            ModelNode attributeDescription = metadata.getDescription().findAttribute(ATTRIBUTES, attribute).getValue();
            ModelNodeForm.Builder<NamedNode> formBuilder = new ComplexAttributeForm(builder.id, metadata,
                    attribute).builder();

            // only adds the "reset" button if the complex attribute is nillable=true
            boolean enableReset = attributeDescription.get(NILLABLE).asBoolean();
            if (enableReset) {
                Set<String> attributeToReset = new HashSet<>();
                attributeToReset.add(attribute);
                formBuilder.prepareReset(form -> this.presenter
                        .resetComplexAttribute(complexAttributeLabel, table.selectedRow().getName(), builder.template,
                                attributeToReset,
                                metadata, () -> presenter.reload()));
            }

            formBuilder.onSave((form, changedValues) -> {
                this.presenter.saveComplexForm(complexAttributeLabel, table.selectedRow().getName(), attribute,
                        builder.template,
                        form.getUpdatedModel(), metadata);
            });

            unboundFormItems.forEach(formItem -> formBuilder.unboundFormItem(formItem));

            ModelNodeForm<NamedNode> formComplexAttribute = formBuilder.build();

            builder.elytronView.registerComponents(formComplexAttribute);

            complexAttributeForms.put(attribute, formComplexAttribute);

            tabs.add(Ids.build(builder.id, attribute, Ids.TAB_SUFFIX), complexAttributeLabel,
                    formComplexAttribute.asElement());

        });


        // create the page to support the list of complex attribute
        includesComplexAttributesPages.forEach(pageBean -> {
            String attribute = pageBean.attribute;
            ModelNode attributeDescription = metadata.getDescription().findAttribute(ATTRIBUTES, attribute).getValue();
            boolean listType = attributeDescription.get(ModelDescriptionConstants.TYPE).asType().equals(ModelType.LIST);
            if (listType) {
                String complexAttributeLabel = new LabelBuilder().label(attribute);
                tableBuilder.column(complexAttributeLabel, row -> showPage(attribute));
                Metadata _metadataComplexAttribute = metadata.repackageComplexAttribute(attribute, false, false, true);
                Metadata _metadataComplexAttributeReq = metadata.repackageComplexAttribute(attribute, true, false,
                        false);

                if (pageBean.tableAddButtonHandler == null) {
                    pageBean.tableAddButtonHandler = table1 ->
                            presenter.launchAddDialog(builder.template, s -> {
                                return complexAttributesPages.get(attribute).selectedParentResource;
                            }, pageBean.attribute, _metadataComplexAttributeReq, complexAttributeLabel);
                }

                ResourceView2 subResource = new ResourceView2.Builder(builder.tableButtonFactory, builder.id, attribute,
                        complexAttributeLabel, builder.template, builder.elytronView, () -> presenter.reload())
                        .setMetadata(_metadataComplexAttribute)
                        .setTableAddButtonHandler(pageBean.tableAddButtonHandler)
                        .build()
                        .markAsSubPage();

                complexAttributesPages.put(attribute, subResource);
            }

        });
        this.table = tableBuilder.build();
        builder.elytronView.registerComponents(table, form);


        if (!includesComplexAttributes.isEmpty()) {
            elementLayout = createElementLayout(builder.title, metadata, table, tabs);
        } else {
            elementLayout = createElementLayout(builder.title, metadata, this.table, this.form);
        }

        // must initialize pages instance as it is used in an inner class
        String idPage = Ids.build(builder.id, Ids.PAGE_SUFFIX);

        pages = new Pages(idPage, elementLayout);
        complexAttributesPages.forEach((attribute, subResource) -> {
            String complexAttributeLabel = new LabelBuilder().label(attribute);
            ResourceView2 _subResourceView = subResource.create();
            String idSubPage = Ids.build(attribute, Ids.PAGE_SUFFIX);
            pagesId.put(attribute, idSubPage);
            pages.addPage(idPage, idSubPage,
                    () -> builder.title + ": " + table.selectedRow().getName(), () -> complexAttributeLabel,
                    _subResourceView.elementLayout);
        });

        if (builder.navigation != null) {
            if (primaryLevelIcon != null) {
                if (complexAttributesPages.isEmpty()) {
                    builder.navigation
                            .addPrimary(Ids.build(builder.id, ITEM), builder.title, primaryLevelIcon, elementLayout);
                } else {
                    builder.navigation.addPrimary(Ids.build(builder.id, ITEM), builder.title, primaryLevelIcon, pages);
                }
            } else if (complexAttributesPages.isEmpty()) {
                builder.navigation
                        .addSecondary(builder.primaryId, Ids.build(builder.id, ITEM), builder.title, elementLayout);
            } else {
                builder.navigation
                        .addSecondary(builder.primaryId, Ids.build(builder.id, ITEM), builder.title, pages.asElement());
            }
        }
        return this;
    }

    public void update(final List<NamedNode> model) {
        this.form.clear();
        this.table.update(model);
    }

    public Table<NamedNode> getTable() {
        return table;
    }

    public Form<NamedNode> getForm() {
        return form;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void showPage(String attribute) {
        if (table.hasSelection()) { pages.showPage(pagesId.get(attribute)); }
    }

    public Collection<ModelNodeForm<NamedNode>> getComplexAttributeForms() {
        return complexAttributeForms.values();
    }

    public ModelNodeForm<NamedNode> getComplexAttributeForm(String complexAttributeName) {
        return complexAttributeForms.get(complexAttributeName);
    }

    public void bindTableToForm() {
        table.bindForm(form);

        // binds each form in the tab to the main table
        table.onSelectionChange(t -> {
            if (t.hasSelection()) {
                NamedNode selectedTableItem = t.selectedRow();

                // bind the table item to the complex attribute forms
                complexAttributeForms.forEach((attribute, complexForm) -> {

                    complexForm.view(new NamedNode(selectedTableItem.get(attribute)));

                    // update unbound form items
                    this.includesComplexAttributes.forEach((complexAttribute, formItems) -> {
                        formItems.forEach(formItem -> {

                            if (selectedTableItem.get(attribute).hasDefined(formItem.getName())) {
                                // this special handling is necessary as the NewItemAttributesItem may provide
                                // a different key,value attribute names.
                                if (formItem instanceof NewItemAttributesItem) {
                                    NewItemAttributesItem niaItem = (NewItemAttributesItem) formItem;
                                    ModelNode niaNode = selectedTableItem.get(attribute).get(formItem.getName());

                                    niaItem.setValue(niaNode);
                                } else {
                                    formItem.clearValue();
                                }
                            }
                        });
                    });
                });

                // bind the table item to the complex attribute tables
                complexAttributesPages.forEach((attribute, _subResource) -> {
                    _subResource.table.clear();
                    _subResource.selectedParentResource = selectedTableItem.getName();
                    if (selectedTableItem.hasDefined(attribute)) {
                        _subResource.bindTableToForm();
                        List<ModelNode> modelNodes = selectedTableItem.get(attribute).asList();
                        List<NamedNode> aa = modelNodes.stream().map(NamedNode::new).collect(toList());
                        _subResource.getTable().update(aa);
                    }
                });
            } else {
                this.form.clear();
                complexAttributeForms.forEach((attribute, complexForm) -> complexForm.clear());
                complexAttributesPages.forEach((attribute, resourceView) -> resourceView.table.clear());
            }
        });

    }

    @Override
    public void setPresenter(final ElytronPresenter presenter) {
        this.presenter = presenter;
        complexAttributesPages.forEach((attribute, resourceView) -> resourceView.setPresenter(presenter));
    }

    private ModelNodeForm<NamedNode> createForm(String id, Metadata metadata, String title, AddressTemplate template) {
        ModelNodeForm<NamedNode> form = new ModelNodeForm.Builder<NamedNode>(Ids.build(id, Ids.FORM_SUFFIX),
                metadata)
                .onSave((_form, changedValues) -> {
                    if (subPage) {
                        // TODO use write-attribute with index based on complex attribute
                        // write-attribute(name=match-rules[idx],value{})
                        //presenter.listAdd(title, selectedParentResource, id, template, changedValues, metadata);
                    } else {
                        String name = table.selectedRow().getName();
                        presenter.saveForm(title, name, template, changedValues, metadata);
                    }
                })
                .build();
        return form;
    }

    private HTMLElement createElementLayout(String title, Metadata metadata, Table<NamedNode> table,
            Form<NamedNode> form) {
        return div()
                .add(h(1).textContent(title))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(table)
                .add(form)
                .asElement();
    }

    private HTMLElement createElementLayout(String title, Metadata metadata, Table<NamedNode> table, Tabs tabs) {
        return div()
                .add(h(1).textContent(title))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(table)
                .add(tabs)
                .asElement();
    }

    private ModelNodeTable.Builder<NamedNode> createTable(String id, Metadata metadata, AddressTemplate template,
            String title, TableButtonFactory tableButtonFactory) {

        Button<NamedNode> add = null;
        if (builder.tableAddButtonHandler != null) {
            add = tableButtonFactory.add(template, builder.tableAddButtonHandler);
        } else if (builder.tableAddCallback != null) {
            add = tableButtonFactory
                    .add(Ids.build(id, Ids.TABLE_SUFFIX, Ids.ADD_SUFFIX), title, template, builder.tableAddCallback);
        }

        Button<NamedNode> remove;
        if (subPage) {
            remove = tableButtonFactory.remove(builder.template, _table -> {

                String name = _table.selectedRow().getName();
                // if the resource doesn't contain a NAME attribute, use the model node value to display
                if (UNDEFINED.equals(name.substring(0, UNDEFINED.length()))) {
                    name = _table.selectedRow().asModelNode().asString();
                }

                // TODO: implement the table.api.selectedRowIndex()
                int index = 0;
                presenter.listRemove(title, selectedParentResource, id, index, template);

            });
        } else {
            remove = tableButtonFactory.remove(title, template, _table -> _table.selectedRow().getName(),
                    builder.tableRemoveCallback);

        }

        ModelNodeTable.Builder<NamedNode> tableBuilder = new ModelNodeTable.Builder<NamedNode>(
                Ids.build(id, Ids.TABLE_SUFFIX), metadata)
                .button(add)
                .button(remove);

        if (subPage) {
            metadata.getDescription().get(ATTRIBUTES).asPropertyList().forEach(property -> {
                String attribute = property.getName();
                tableBuilder.column(attribute,
                        (cell, type, row, meta) -> row.hasDefined(attribute) ? row.get(attribute).asString() : "");
            });
        } else {
            tableBuilder.column(NAME, (cell, type, row, meta) -> row.getName());
        }

        return tableBuilder;
    }

}
