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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.table.Button;
import org.jboss.hal.ballroom.table.ButtonHandler;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.mbui.form.ComplexAttributeModelNodeForm;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.spi.Callback;

import static java.util.stream.Collectors.toMap;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NILLABLE;

/**
 *  Helper class to create a standard view that contains a table and a form with the attributes. It may also add
 *  complex attributes as tabs.
 *
 * @author Claudio Miranda <claudio@redhat.com>
 */
public class ResourceView {

    private Table<NamedNode> table;
    private Form<NamedNode> form;
    private ElytronPresenter presenter;
    private TableButtonFactory tableButtonFactory;
    private VerticalNavigation navigation;
    private String primaryId;
    private String id;
    private String title;
    private AddressTemplate template;
    private Element elementLayout;
    private CrudOperations.AddCallback tableAddCallback;
    private ButtonHandler<NamedNode> tableAddButtonHandler;
    private Callback tableRemoveCallback;
    private Metadata metadata;
    private Map<String, List<FormItem>> includesComplexAttributes = new HashMap<>();
    private Map<String, ModelNodeForm<NamedNode>> complexAttributeForms = new HashMap<>();
    private ElytronView elytronView;
    private String primaryLevelIcon;

    public ResourceView(final MetadataRegistry metadataRegistry, final TableButtonFactory tableButtonFactory,
            final VerticalNavigation navigation, String primaryId, String id, String title, AddressTemplate template,
            ElytronView elytronView, final CrudOperations.AddCallback tableAddCallback, final Callback tableRemoveCallback) {

        this.metadata = metadataRegistry.lookup(template);
        this.tableButtonFactory = tableButtonFactory;
        this.navigation = navigation;
        this.primaryId = primaryId;
        this.id = id;
        this.title = title;
        this.template = template;
        this.tableRemoveCallback = tableRemoveCallback;
        this.elytronView  = elytronView;

        this.tableAddCallback = tableAddCallback;
    }

    public ResourceView(final MetadataRegistry metadataRegistry, final TableButtonFactory tableButtonFactory,
            final VerticalNavigation navigation, String primaryId, String id, String title, AddressTemplate template,
            ElytronView elytronView, final ButtonHandler<NamedNode> tableAddButtonHandler, final Callback tableRemoveCallback) {

        this.elytronView = elytronView;
        this.metadata = metadataRegistry.lookup(template);
        this.tableButtonFactory = tableButtonFactory;
        this.navigation = navigation;
        this.primaryId = primaryId;
        this.id = id;
        this.title = title;
        this.template = template;
        this.tableRemoveCallback = tableRemoveCallback;

        this.tableAddButtonHandler = tableAddButtonHandler;
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
     *
     * Constructs a ModelNodeForm from the complex attribute and adds it as tab to the detail table. For example, the
     * "credential-reference" attribute of "credential-reference" resource of elytron subsystem.
     *
     * @param complexAttributeName The OBJECT attribute name.
     */
    public ResourceView addComplexAttributeAsTab(String complexAttributeName) {
        includesComplexAttributes.put(complexAttributeName, Collections.EMPTY_LIST);
        return this;
    }

    /**
     *
     * Constructs a ModelNodeForm from the complex attribute and adds it as tab to the detail table. For example, the
     * "credential-reference" attribute of "credential-reference" resource of elytron subsystem. Also, the user may
     * add a list of unbound form items, that requires custom handling.
     *
     * @param complexAttributeName The OBJECT attribute name.
     * @param unboundFormItems The list of custom form items.
     */
    public ResourceView addComplexAttributeAsTab(String complexAttributeName, List<FormItem> unboundFormItems) {
        includesComplexAttributes.put(complexAttributeName, unboundFormItems);
        return this;
    }

    public ResourceView build() {
        this.form = createForm(id, metadata, title, template);
        this.table = createTable(id, metadata, template, title, tableButtonFactory);

        // tabs
        final Tabs tabs = new Tabs();

        elytronView.registerComponents(table, form);

        if (!includesComplexAttributes.isEmpty()) {
            // adds the main form as the first tab
            tabs.add(Ids.build(id, ATTRIBUTES, Ids.TAB_SUFFIX), "Attributes", form.asElement());
        }

        includesComplexAttributes.forEach((attribute, unboundFormItems) -> {
            String complexAttributeLabel = new LabelBuilder().label(attribute);
            ModelNodeForm.Builder<NamedNode> formBuilder = new ComplexAttributeModelNodeForm(id, metadata, attribute).builder();

            // only adds the "reset" button if the complex attribute is nillable=true
            boolean enableReset = metadata.getDescription().findAttribute(ATTRIBUTES, attribute).getValue().get(NILLABLE).asBoolean();
            if (enableReset) {
                Set<String> attributeToReset = new HashSet<>();
                attributeToReset.add(attribute);
                formBuilder.prepareReset(form -> this.presenter
                        .resetComplexAttribute(title, table.selectedRow().getName(), template, attributeToReset,
                                metadata, () -> presenter.reload()));
            }

            formBuilder.onSave((form, changedValues) -> {
                this.presenter.saveComplexForm(complexAttributeLabel, table.selectedRow().getName(), attribute, template,
                        form.getUpdatedModel(), metadata);
            });

            unboundFormItems.forEach(formItem -> formBuilder.unboundFormItem(formItem));

            ModelNodeForm<NamedNode> formComplexAttribute = formBuilder.build();

            elytronView.registerComponents(formComplexAttribute);

            complexAttributeForms.put(attribute, formComplexAttribute);

            tabs.add(Ids.build(id, attribute, Ids.TAB_SUFFIX), complexAttributeLabel, formComplexAttribute.asElement());

        });


        if (!includesComplexAttributes.isEmpty()) {
            elementLayout = createElementLayout(title, metadata, table, tabs);
        } else {
            elementLayout = createElementLayout(title, metadata, this.table, this.form);
        }

        if (primaryLevelIcon != null)
            navigation.addPrimary(Ids.build(id, "item"), title, primaryLevelIcon, elementLayout);
        else
            navigation.addSecondary(primaryId, Ids.build(id, "item"), title, elementLayout);

        return this;
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

    public Element getElementLayout() {
        return elementLayout;
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
        complexAttributeForms.forEach((attribute, complexForm) -> {
            table.onSelectionChange(t -> {

                NamedNode selectedResource = t.selectedRow();
                if (table.hasSelection() && selectedResource != null) {
                    complexForm.view(new NamedNode(selectedResource.get(attribute)));

                    // update unbound form items
                    this.includesComplexAttributes.forEach((complexAttribute, formItems) -> {
                        formItems.forEach(formItem -> {

                            if (selectedResource.get(attribute).hasDefined(formItem.getName())) {
                                // this special handling is necessary as the CustomPropertiesItem may provide
                                // a different key,value attribute names.
                                if (formItem instanceof CustomPropertiesItem) {
                                    CustomPropertiesItem cp = (CustomPropertiesItem) formItem;
                                    Map<String, String> mappings = selectedResource.get(attribute)
                                            .get(formItem.getName())
                                            .asList().stream()
                                            .collect(toMap(node -> node.get(cp.getKeyName()).asString(),
                                                    node -> node.get(cp.getValueName()).asString().replaceAll("\\[|\"|\\]", "")));
                                    formItem.setValue(mappings);
                                } else {
                                    formItem.clearValue();
                                }

                            }
                        });
                    });
                } else {
                    complexForm.clear();
                }
            });
        });
    }

    public void setPresenter(final ElytronPresenter presenter) {
        this.presenter = presenter;
    }

    private ModelNodeForm<NamedNode> createForm(String id, Metadata metadata, String title, AddressTemplate template) {
        ModelNodeForm<NamedNode> form = new ModelNodeForm.Builder<NamedNode>(Ids.build(id, Ids.FORM_SUFFIX),
                metadata)
                .onSave((_form, changedValues) -> {
                    String name = _form.getModel().getName();
                    presenter.saveForm(title, name, template, changedValues, metadata);
                })
                .build();
        return form;
    }

    private Element createElementLayout(String title, final Metadata metadata, Table<NamedNode> table,
            Form<NamedNode> form) {
        Element elem = new Elements.Builder()
                .div()
                .h(1).textContent(title).end()
                .p().textContent(metadata.getDescription().getDescription()).end()
                .add(table)
                .add(form)
                .end()
                .build();

        return elem;
    }

    private Element createElementLayout(String title, final Metadata metadata, Table<NamedNode> table,
            Tabs tabs) {
        Element elem = new Elements.Builder()
                .div()
                .h(1).textContent(title).end()
                .p().textContent(metadata.getDescription().getDescription()).end()
                .add(table)
                .add(tabs)
                .end()
                .build();

        return elem;
    }

    private Table<NamedNode> createTable(String id, Metadata metadata, AddressTemplate template, String title,
            TableButtonFactory tableButtonFactory) {

        Button<NamedNode> add = null;
        if (tableAddButtonHandler != null)
            add = tableButtonFactory.add(template, tableAddButtonHandler);
        else if (tableAddCallback != null)
            add = tableButtonFactory.add(Ids.build(id, Ids.TABLE_SUFFIX, Ids.ADD_SUFFIX), title, template, tableAddCallback);


        Button<NamedNode> remove = tableButtonFactory.remove(title, template, _table -> _table.selectedRow().getName(),
                tableRemoveCallback);

        Table<NamedNode> table = new ModelNodeTable.Builder<NamedNode>(Ids.build(id, Ids.TABLE_SUFFIX), metadata)
                .button(add)
                .button(remove)
                .column(NAME, (cell, type, row, meta) -> row.getName())
                .build();
        return table;
    }

}
