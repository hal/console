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

import java.util.Map;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Api;
import org.jboss.hal.ballroom.table.Api.RefreshMode;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.PropertiesOperations;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.meta.SelectionAwareStatementContext;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;
import org.omg.CORBA.CurrentOperations;

import static java.util.stream.Collectors.toMap;
import static org.jboss.hal.client.configuration.subsystem.resourceadapter.AddressTemplates.SELECTED_RESOURCE_ADAPTER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONFIG_PROPERTIES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

/**
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings({"WeakerAccess", "HardCodedStringLiteral", "UnusedParameters"})
public abstract class ResourceAdapterView extends MbuiViewImpl<ResourceAdapterPresenter>
        implements ResourceAdapterPresenter.MyView {

    public static ResourceAdapterView create(final MbuiContext mbuiContext, final CrudOperations crud,
            final PropertiesOperations propertiesOperations) {
        return new Mbui_ResourceAdapterView(mbuiContext, crud, propertiesOperations);
    }

    abstract CrudOperations crud();
    abstract PropertiesOperations propertiesOperations();

    final SelectionAwareStatementContext selectionAwareStatementContext;
    @MbuiElement("resource-adapter-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("resource-adapter-configuration-form") Form<ModelNode> configurationForm;
    @MbuiElement("resource-adapter-connection-definition-table") DataTable<NamedNode> connectionDefinitionsTable;
    @MbuiElement("resource-adapter-connection-definition-form") Form<NamedNode> connectionDefinitionsForm;
    @MbuiElement("resource-adapter-admin-object-table") DataTable<NamedNode> adminObjectsTable;
    @MbuiElement("resource-adapter-admin-object-form") Form<NamedNode> adminObjectsForm;

    public ResourceAdapterView(final MbuiContext mbuiContext) {
        super(mbuiContext);
        selectionAwareStatementContext = new SelectionAwareStatementContext(mbuiContext.statementContext(),
                () -> presenter.getResourceAdapter());
    }

    void saveConfiguration(Form<ModelNode> form, Map<String, Object> changedValues) {
        propertiesOperations().saveWithProperties(Names.RESOURCE_ADAPTER, presenter.getResourceAdapter(),
                SELECTED_RESOURCE_ADAPTER_TEMPLATE.resolve(selectionAwareStatementContext), form, changedValues,
                "config-properties", () -> presenter.reload());
    }

    void addConnectionDefinition() {
    }

    void saveConnectionDefinition(Form<NamedNode> form, Map<String, Object> changedValues) {

    }

    void removeConnectionDefinition(Api<NamedNode> api) {

    }

    void addAdminObject() {

    }

    void saveAdminObject(Form<NamedNode> form, Map<String, Object> changedValues) {

    }

    void removeAdminObject(Api<NamedNode> api) {

    }

    @Override
    public void update(final ResourceAdapter resourceAdapter) {
        configurationForm.view(resourceAdapter);
        Map<String, String> p = failSafePropertyList(resourceAdapter, CONFIG_PROPERTIES).stream()
                .collect(toMap(Property::getName, property -> property.getValue().get(VALUE).asString()));
        configurationForm.getFormItem(CONFIG_PROPERTIES).setValue(p);

        connectionDefinitionsTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(resourceAdapter, "connection-definitions")))
                .refresh(RefreshMode.RESET);
        connectionDefinitionsForm.clear();
        connectionDefinitionsForm.getFormItem(CONFIG_PROPERTIES).clearValue();

        adminObjectsTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(resourceAdapter, "admin-objects")))
                .refresh(RefreshMode.RESET);
        adminObjectsForm.clear();
        adminObjectsForm.getFormItem(CONFIG_PROPERTIES).clearValue();
    }

}
