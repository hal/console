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
package org.jboss.hal.client.configuration.subsystem.resourceadapter;

import java.util.Collections;
import java.util.Map;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.table.Api;
import org.jboss.hal.core.OperationFactory;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.SelectionAwareStatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static java.util.stream.Collectors.toMap;
import static org.jboss.hal.client.configuration.subsystem.resourceadapter.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

/**
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings({"WeakerAccess", "HardCodedStringLiteral", "UnusedParameters", "DuplicateStringLiteralInspection"})
public abstract class ResourceAdapterView extends MbuiViewImpl<ResourceAdapterPresenter>
        implements ResourceAdapterPresenter.MyView {

    public static ResourceAdapterView create(final MbuiContext mbuiContext) {
        return new Mbui_ResourceAdapterView(mbuiContext);
    }

    final SelectionAwareStatementContext selectionAwareStatementContext;
    @MbuiElement("resource-adapter-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("resource-adapter-configuration-form") Form<ModelNode> configurationForm;
    @MbuiElement("resource-adapter-connection-definition-table") NamedNodeTable<NamedNode> connectionDefinitionsTable;
    @MbuiElement("resource-adapter-connection-definition-form") Form<NamedNode> connectionDefinitionsForm;
    @MbuiElement("resource-adapter-admin-object-table") NamedNodeTable<NamedNode> adminObjectsTable;
    @MbuiElement("resource-adapter-admin-object-form") Form<NamedNode> adminObjectsForm;

    public ResourceAdapterView(final MbuiContext mbuiContext) {
        super(mbuiContext);
        selectionAwareStatementContext = new SelectionAwareStatementContext(mbuiContext.statementContext(),
                () -> presenter.getResourceAdapter());
    }

    @Override
    public void attach() {
        super.attach();
        connectionDefinitionsTable.api().onSelectionChange(api -> updateProperties(api, connectionDefinitionsForm));
        adminObjectsTable.api().onSelectionChange(api -> updateProperties(api, adminObjectsForm));
    }

    void saveConfiguration(Form<ModelNode> form, Map<String, Object> changedValues) {
        changedValues.remove(CONFIG_PROPERTIES);
        changedValues.remove(WM_SECURITY_MAPPING_GROUPS);
        changedValues.remove(WM_SECURITY_MAPPING_USERS);

        ResourceAddress address = SELECTED_RESOURCE_ADAPTER_TEMPLATE.resolve(selectionAwareStatementContext);
        OperationFactory operationFactory = new OperationFactory();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(RESOURCE_ADAPTER_TEMPLATE);
        Composite operations = operationFactory.fromChangeSet(address, changedValues, metadata);

        Operation groupMappings = mappingsOperation(address, form, WM_SECURITY_MAPPING_GROUPS);
        if (groupMappings != null) {
            operations.add(groupMappings);
        }
        Operation userMappings = mappingsOperation(address, form, WM_SECURITY_MAPPING_USERS);
        if (userMappings != null) {
            operations.add(userMappings);
        }

        mbuiContext.po().saveWithProperties(Names.RESOURCE_ADAPTER, presenter.getResourceAdapter(), address,
                operations, CONFIG_PROPERTIES, form.<Map<String, String>>getFormItem(CONFIG_PROPERTIES).getValue(),
                () -> presenter.reload());
    }

    private Operation mappingsOperation(ResourceAddress address, Form<ModelNode> form, String attribute) {
        Operation operation = null;
        FormItem<Map<String, String>> formItem = form.getFormItem(attribute);
        if (formItem != null && formItem.isModified()) {
            if (formItem.getValue().isEmpty()) {
                operation = new Operation.Builder(UNDEFINE_ATTRIBUTE_OPERATION, address)
                        .param(NAME, attribute)
                        .build();
            } else {
                ModelNode mappings = new ModelNode();
                formItem.getValue().forEach((key, value) -> {
                    ModelNode mapping = new ModelNode();
                    mapping.get(FROM).set(key);
                    mapping.get(TO).set(value);
                    mappings.add(mapping);
                });
                operation = new Operation.Builder(WRITE_ATTRIBUTE_OPERATION, address)
                        .param(NAME, attribute)
                        .param(VALUE, mappings)
                        .build();
            }
        }
        return operation;
    }

    void addConnectionDefinition() {
        Metadata metadata = mbuiContext.metadataRegistry().lookup(CONNECTION_DEFINITIONS_TEMPLATE);
        AddResourceDialog dialog = new AddResourceDialog(Ids.RESOURCE_ADAPTER_CONNECTION_DEFINITION_ADD,
                Names.CONNECTION_DEFINITION, metadata, (name, modelNode) -> {
            ResourceAddress address = SELECTED_CONNECTION_DEFINITIONS_TEMPLATE
                    .resolve(selectionAwareStatementContext, name);
            mbuiContext.crud().add(Names.CONNECTION_DEFINITION, name, address, modelNode, (n, a) -> presenter.reload());
        });
        dialog.show();
    }

    void saveConnectionDefinition(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        ResourceAddress address = SELECTED_CONNECTION_DEFINITIONS_TEMPLATE.resolve(selectionAwareStatementContext,
                name);
        Metadata metadata = mbuiContext.metadataRegistry().lookup(CONNECTION_DEFINITIONS_TEMPLATE);
        Map<String, String> properties = form.getFormItem(CONFIG_PROPERTIES) != null
                ? form.<Map<String, String>>getFormItem(CONFIG_PROPERTIES).getValue()
                : Collections.emptyMap();
        mbuiContext.po().saveWithProperties(Names.CONNECTION_DEFINITION, name, address, changedValues, metadata,
                CONFIG_PROPERTIES, properties, () -> presenter.reload());
    }

    void removeConnectionDefinition(Api<NamedNode> api) {
        //noinspection ConstantConditions
        String name = api.selectedRow().getName();
        mbuiContext.crud().remove(Names.CONNECTION_DEFINITION, name,
                SELECTED_CONNECTION_DEFINITIONS_TEMPLATE.resolve(selectionAwareStatementContext, name),
                () -> presenter.reload());
    }

    void addAdminObject() {
        Metadata metadata = mbuiContext.metadataRegistry().lookup(ADMIN_OBJECTS_TEMPLATE);
        AddResourceDialog dialog = new AddResourceDialog(Ids.RESOURCE_ADAPTER_ADMIN_OBJECT_ADD,
                Names.ADMIN_OBJECT, metadata, (name, modelNode) -> {
            ResourceAddress address = SELECTED_ADMIN_OBJECTS_TEMPLATE.resolve(selectionAwareStatementContext, name);
            mbuiContext.crud().add(Names.ADMIN_OBJECT, name, address, modelNode, (n, a) -> presenter.reload());
        });
        dialog.show();
    }

    void saveAdminObject(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        ResourceAddress address = SELECTED_ADMIN_OBJECTS_TEMPLATE.resolve(selectionAwareStatementContext, name);
        Metadata metadata = mbuiContext.metadataRegistry().lookup(ADMIN_OBJECTS_TEMPLATE);
        mbuiContext.po().saveWithProperties(Names.ADMIN_OBJECT, name, address, changedValues, metadata,
                CONFIG_PROPERTIES, form.<Map<String, String>>getFormItem(CONFIG_PROPERTIES).getValue(),
                () -> presenter.reload());
    }

    void removeAdminObject(Api<NamedNode> api) {
        //noinspection ConstantConditions
        String name = api.selectedRow().getName();
        mbuiContext.crud().remove(Names.ADMIN_OBJECT, name,
                SELECTED_ADMIN_OBJECTS_TEMPLATE.resolve(selectionAwareStatementContext, name),
                () -> presenter.reload());
    }

    @Override
    public void update(final ResourceAdapter resourceAdapter) {
        configurationForm.view(resourceAdapter);
        Map<String, String> p = failSafePropertyList(resourceAdapter, CONFIG_PROPERTIES).stream()
                .collect(toMap(Property::getName, property -> property.getValue().get(VALUE).asString()));
        configurationForm.getFormItem(CONFIG_PROPERTIES).setValue(p);
        updateMappings(resourceAdapter, WM_SECURITY_MAPPING_GROUPS);
        updateMappings(resourceAdapter, WM_SECURITY_MAPPING_USERS);

        connectionDefinitionsForm.clear();
        connectionDefinitionsForm.getFormItem(CONFIG_PROPERTIES).clearValue();
        adminObjectsForm.clear();
        adminObjectsForm.getFormItem(CONFIG_PROPERTIES).clearValue();

        connectionDefinitionsTable.update(asNamedNodes(failSafePropertyList(resourceAdapter, CONNECTION_DEFINITIONS)));
        adminObjectsTable.update(asNamedNodes(failSafePropertyList(resourceAdapter, ADMIN_OBJECTS)));
    }

    private void updateProperties(Api<NamedNode> api, Form<NamedNode> form) {
        FormItem<Map<String, String>> formItem = form.getFormItem(CONFIG_PROPERTIES);
        if (!api.hasSelection()) {
            formItem.clearValue();
        } else {
            Map<String, String> properties = failSafePropertyList(api.selectedRow(), CONFIG_PROPERTIES).stream()
                    .collect(toMap(Property::getName, property -> property.getValue().get(VALUE).asString()));
            formItem.setValue(properties);
        }
    }

    private void updateMappings(ResourceAdapter resourceAdapter, String attribute) {
        FormItem<Map<String, String>> formItem = configurationForm.getFormItem(attribute);
        if (!resourceAdapter.hasDefined(attribute)) {
            formItem.clearValue();
        } else {
            Map<String, String> mappings = resourceAdapter.get(attribute).asList().stream()
                    .collect(toMap(node -> node.get(FROM).asString(), node -> node.get(TO).asString()));
            formItem.setValue(mappings);
        }
    }
}
