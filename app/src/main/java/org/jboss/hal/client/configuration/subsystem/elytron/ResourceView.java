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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Splitter;
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
public class ResourceView implements HasPresenter<ElytronPresenter> {

    public static final String HAL_INDEX = "hal_index";

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

        public ResourceView build() {
            return new ResourceView(this);
        }

    }

    private Table<NamedNode> table;
    private Form<NamedNode> form;
    private ElytronPresenter presenter;
    private HTMLElement elementLayout;
    private Metadata metadata;
    private Map<String, List<FormItem>> includesComplexAttributes = new HashMap<>();
    private Map<String, ModelNodeForm<NamedNode>> complexAttributeForms = new HashMap<>();
    private Map<String, ButtonHandler<NamedNode>> includesComplexAttributesPages = new HashMap<>();
    private Map<String, ResourceView> complexAttributesPages = new HashMap<>();
    private Builder builder;
    private String primaryLevelIcon;
    private Pages pages;
    private Map<String, String> pagesId = new HashMap<>();
    private boolean subPage;
    private String selectedParentResource;

    private ResourceView(Builder builder) {
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
    public ResourceView primaryLevel(String icon) {
        primaryLevelIcon = icon;
        return this;
    }

    /**
     * Constructs a ModelNodeForm from the complex attribute and adds it as tab to the detail table. For example, the
     * "credential-reference" attribute of "credential-reference" resource of elytron subsystem.
     *
     * @param complexAttributeName The OBJECT attribute name.
     */
    public ResourceView addComplexAttributeAsTab(String complexAttributeName) {
        includesComplexAttributes.put(complexAttributeName, Collections.emptyList());
        return this;
    }

    public ResourceView markAsSubPage() {
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
    public ResourceView addComplexAttributeAsTab(String complexAttributeName, List<FormItem> unboundFormItems) {
        includesComplexAttributes.put(complexAttributeName, unboundFormItems);
        return this;
    }

    /**
     * @param complexAttributeName The complex attribute name.
     *
     * @return
     */
    // TODO If this is used in a view with the same complexAttributeName
    // TODO an exception is thrown in AutoComplete.detach!
    // See FactoriesView and search for '.addComplexAttributeAsPage("mechanism-configurations")'
    public ResourceView addComplexAttributeAsPage(String complexAttributeName) {
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
    public ResourceView addComplexAttributeAsPage(String complexAttributeName,
            final ButtonHandler<NamedNode> tableAddButtonHandler) {
        includesComplexAttributesPages.put(complexAttributeName, tableAddButtonHandler);
        return this;
    }

    public ResourceView create() {
        this.form = createForm(builder.id, metadata, builder.title);
        ModelNodeTable.Builder<NamedNode> tableBuilder = createTable(Ids.build(builder.primaryId, builder.id), metadata,
                builder.template, builder.title, builder.tableButtonFactory);

        // must initialize tabs instance as it is used in an inner class
        final Tabs tabs = new Tabs();

        if (!includesComplexAttributes.isEmpty()) {
            // adds the main form as the first tab
            tabs.add(Ids.build(builder.id, ATTRIBUTES, Ids.TAB_SUFFIX), "Attributes", form.asElement());
        }

        // create the form to support the complex attribute as tab
        includesComplexAttributes.forEach((attribute, unboundFormItems) -> {
            // check if the attribute is in enhanced syntax attribute1.attribute2
            final String attributeName = attribute.indexOf('.') > -1 ? attribute.substring(attribute.indexOf(".") + 1) : attribute;
            String complexAttributeLabel = new LabelBuilder().label(attributeName);
            String _path = ATTRIBUTES;
            // if is an enhanced syntax, the path to search for attributes must consider the additional attribute
            if (attribute.contains("."))
                _path = ATTRIBUTES + "/" + attribute.substring(0, attribute.indexOf(".")) + "/" + VALUE_TYPE;
            ModelNode attributeDescription = metadata.getDescription().findAttribute(_path, attributeName).getValue();
            ModelNodeForm.Builder<NamedNode> formBuilder = new ComplexAttributeForm(builder.id, metadata,
                    attribute).builder();

            // only adds the "reset" button if the complex attribute is nillable=true
            boolean enableReset = attributeDescription.get(NILLABLE).asBoolean();
            if (enableReset) {
                formBuilder.prepareReset(form -> {
                    if (table.hasSelection()) {
                        String complexAttributeName = attribute;
                        // if on a sub-page, it means there are two complex attributes attribute_1 of type LIST
                        // and attribute_2 that is the complex attribute we want to reset.
                        // this way the complex attribute name will be assembled in the following form:
                        // attribute_1[index].attribute_2
                        // one example of this scenario is jdbc-realm of elytron subsystem, it contains principal-query
                        // as attribute of type LIST and nested bcrypt-mapper of type OBJECT
                        if (subPage)
                            complexAttributeName = builder.id + "[" + table.selectedRow().getName() + "]." + attribute;

                        // if on a sub-page the resource name was set as selectedParentResource
                        String resourceName = subPage ? selectedParentResource : table.selectedRow().getName();
                        this.presenter.resetComplexAttribute(complexAttributeLabel, resourceName, complexAttributeName,
                                metadata, () -> presenter.reload());
                    }
                });
            }

            formBuilder.onSave((form, changedValues) -> {
                String complexAttributeName = attribute;
                // if on a sub-page, it means there are two complex attributes attribute_1 of type LIST
                // and attribute_2 that is the complex attribute we want to reset.
                // this way the complex attribute name will be assembled in the following form:
                // attribute_1[index].attribute_2
                // one example of this scenario is jdbc-realm of elytron subsystem, it contains principal-query
                // as attribute of type LIST and nested bcrypt-mapper of type OBJECT
                if (subPage)
                    complexAttributeName = builder.id + "[" + table.selectedRow().getName() + "]." + attribute;

                // if on a sub-page the resource name was set as selectedParentResource
                String resourceName = subPage ? selectedParentResource : table.selectedRow().getName();

                this.presenter.saveComplexForm(complexAttributeLabel, resourceName, complexAttributeName,
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

        includesComplexAttributesPages.forEach((attribute, tableAddButtonHandler) -> {
            //String attribute = pageBean.attribute;
            ModelNode attributeDescription = metadata.getDescription().findAttribute(ATTRIBUTES, attribute).getValue();
            boolean listType = attributeDescription.get(ModelDescriptionConstants.TYPE).asType().equals(ModelType.LIST);
            if (listType) {
                String complexAttributeLabel = new LabelBuilder().label(attribute);
                tableBuilder.column(complexAttributeLabel, row -> showPage(attribute));
                // this repackaged metadata is used on the Attributes form, so only the nested attributes are used
                Metadata _metadataComplexAttribute = metadata.repackageComplexAttribute(attribute, false, false, true);

                // this repackaged metadata is used to add a new complex object as part of the parent complex attribute
                // of type LIST, this repackaged description must use the operations/add/request-properties path
                Metadata _metadataComplexAttributeReq = metadata.repackageComplexAttribute(attribute, true, false,
                        false);

                if (tableAddButtonHandler == null) {
                    tableAddButtonHandler = table1 ->
                        // lazy load the parent resource name from the map
                        presenter.launchAddDialog( s -> {
                            return complexAttributesPages.get(attribute).selectedParentResource;
                        }, attribute, _metadataComplexAttributeReq, complexAttributeLabel);
                }

                ResourceView subResource = new ResourceView.Builder(builder.tableButtonFactory, builder.id, attribute,
                        complexAttributeLabel, builder.template, builder.elytronView, () -> presenter.reload())
                        .setMetadata(_metadataComplexAttribute)
                        .setTableAddButtonHandler(tableAddButtonHandler)
                        .build()
                        .markAsSubPage();

                // if the attributes list contains any nested attribute of type OBJECT it is added as a tab
                _metadataComplexAttribute.getDescription().get(ATTRIBUTES).asPropertyList().forEach(property -> {
                    String attribute2 = property.getName();
                    boolean objectType = ModelType.OBJECT.equals(property.getValue().get(TYPE).asType());
                    if (objectType) {
                        subResource.addComplexAttributeAsTab(attribute2);
                    }
                });

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
            ResourceView _subResourceView = subResource.create();
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

    public void bindTableToForm() {
        table.bindForm(form);

        // binds each form in the tab to the main table
        table.onSelectionChange(t -> {
            if (t.hasSelection()) {
                NamedNode selectedTableItem = t.selectedRow();

                // bind the table item to the complex attribute forms
                complexAttributeForms.forEach((attribute, complexForm) -> {

                    // check if the attribute is in enhanced syntax attribute1.attribute2
                    List<String>  enhancedAttribute = Splitter.on(".").splitToList(attribute);
                    ModelNode nodeValue;
                    if (attribute.contains(".")) {
                        nodeValue = selectedTableItem.get(enhancedAttribute.get(0)).get(enhancedAttribute.get(1));
                    } else {
                        nodeValue = selectedTableItem.get(attribute);
                    }
                    complexForm.view(new NamedNode(nodeValue));

                    // update unbound form items
                    this.includesComplexAttributes.forEach((complexAttribute, formItems) -> {
                        formItems.forEach(formItem -> {
                            if (selectedTableItem.get(attribute).hasDefined(formItem.getName())) {
                                formItem.setValue(selectedTableItem.get(attribute).get(formItem.getName()));
                            } else {
                                formItem.clearValue();
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
                        List<ModelNode> modelList = selectedTableItem.get(attribute).asList();

                        List<NamedNode> subModel = new ArrayList<>(modelList.size());

                        boolean modelHasNameAttribute = _subResource.metadata.getDescription().get(ATTRIBUTES).hasDefined(NAME);
                        // as the model doesn't contains a "name" attribute we will use an index to identify it
                        // create NamedNode with index based name, as it will be later used to correctly identify
                        // the item in the table, when removing or modifying the attributes.
                        for (int i = 0; i < modelList.size(); i++) {
                            ModelNode node = modelList.get(i);
                            node.get(HAL_INDEX).set(i);
                            String _name = modelHasNameAttribute ? node.get(NAME).asString() : String.valueOf(i);
                            NamedNode nn = new NamedNode(_name, node);
                            subModel.add(nn);
                        }
                        _subResource.table.update(subModel);
                    } else {
                        _subResource.form.clear();
                    }
                });
            } else {
                this.form.clear();
                complexAttributeForms.forEach((attribute, complexForm) -> complexForm.clear());
                complexAttributesPages.forEach((attribute, resourceView) -> {
                    resourceView.table.clear();
                    resourceView.form.clear();
                });
            }
        });

    }

    @Override
    public void setPresenter(final ElytronPresenter presenter) {
        this.presenter = presenter;
        complexAttributesPages.forEach((attribute, resourceView) -> resourceView.setPresenter(presenter));
    }

    private ModelNodeForm<NamedNode> createForm(String id, Metadata metadata, String title) {
        ModelNodeForm<NamedNode> form = new ModelNodeForm.Builder<NamedNode>(Ids.build(id, Ids.FORM_SUFFIX), metadata)
                .onSave((_form, changedValues) -> {
                    if (subPage) {
                        // on a sub page the save form must consider the resource name and the complex attribute, it is
                        // is represented as builder.id
                        presenter.saveFormPage(selectedParentResource, builder.id, metadata, _form.getModel(), changedValues);
                    } else {
                        String name = table.selectedRow().getName();
                        presenter.saveForm(title, name, changedValues, metadata);
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
            // on a sub-page, it is used the list-remove operation on an index of a complex attribute.
            remove = tableButtonFactory.remove(builder.template, _table -> {
                NamedNode selectedModel = _table.selectedRow();
                // the index is the NamedNode name
                int index = selectedModel.remove(HAL_INDEX).asInt();
                presenter.listRemove(title, selectedParentResource, builder.id, index, template);
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
            // on a sub-page there is no need for an attribute named "name", so we inpect all attributes of the
            // complex attribute and add all of them as columns of the table, if they are not LIST or OBJECT
            metadata.getDescription().get(ATTRIBUTES).asPropertyList().forEach(property -> {
                String attribute = property.getName();
                boolean simpleTypes = !ModelType.OBJECT.equals(property.getValue().get(TYPE).asType());
                simpleTypes = simpleTypes && !ModelType.LIST.equals(property.getValue().get(TYPE).asType());
                if (simpleTypes)
                    tableBuilder.column(attribute,
                        (cell, type, row, meta) -> row.hasDefined(attribute) ? row.get(attribute).asString() : "");
            });
        } else {
            tableBuilder.column(NAME, (cell, type, row, meta) -> row.getName());
        }
        return tableBuilder;
    }

}
