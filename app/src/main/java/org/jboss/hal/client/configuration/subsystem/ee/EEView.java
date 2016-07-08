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
package org.jboss.hal.client.configuration.subsystem.ee;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Button;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.ballroom.table.Api.RefreshMode.RESET;
import static org.jboss.hal.client.configuration.subsystem.ee.AddressTemplates.CONTEXT_SERVICE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.ee.AddressTemplates.MANAGED_EXECUTOR_SCHEDULED_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.ee.AddressTemplates.MANAGED_EXECUTOR_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.ee.AddressTemplates.MANAGED_THREAD_FACTORY_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.Ids.*;

/**
 * @author Claudio Miranda
 */
public class EEView extends PatternFlyViewImpl implements EEPresenter.MyView {

    // local names
    private static final String DEFAULT_BINDINGS_NAME = "Default Bindings";
    private static final String CONTEXT_SERVICE_NAME = "Context Service";
    private static final String MANAGED_EXECUTOR_NAME = "Executor";
    private static final String MANAGED_EXECUTOR_SCHEDULED_NAME = "Scheduled Executor";
    private static final String SERVICES_NAME = "Services";
    private static final String MANAGED_THREAD_FACTORY_NAME = "Thread Factories";

    private final MetadataRegistry metadataRegistry;
    private final VerticalNavigation navigation;
    private final TableButtonFactory tableButtonFactory;
    private final Map<String, ModelNodeForm> forms;
    private final DataTable<ModelNode> globalModulesTable;
    private final Map<String, ModelNodeTable> tables;

    private EEPresenter presenter;

    @Inject
    public EEView(final MetadataRegistry metadataRegistry,
            final TableButtonFactory tableButtonFactory,
            final Resources resources) {
        this.metadataRegistry = metadataRegistry;
        this.tableButtonFactory = tableButtonFactory;

        this.navigation = new VerticalNavigation();
        this.forms = new HashMap<>();
        this.tables = new HashMap<>(4);

        // ============================================
        // attributes - deployments
        Metadata eeMetadata = metadataRegistry.lookup(AddressTemplates.EE_SUBSYSTEM_TEMPLATE);

        ModelNodeForm<ModelNode> eeAttributesForm = new ModelNodeForm.Builder<>(EE_ATTRIBUTES_FORM, eeMetadata)
                .onSave((f, changedValues) -> presenter.save(AddressTemplates.EE_SUBSYSTEM_TEMPLATE, changedValues,
                        resources.constants().deploymentAttributes()))
                .build();
        forms.put(EE_ATTRIBUTES_FORM, eeAttributesForm);
        registerAttachable(eeAttributesForm);

        Element navigationElement = new Elements.Builder()
                .div()
                .h(1).textContent(Names.DEPLOYMENTS).end()
                .p().textContent(eeMetadata.getDescription().getDescription()).end()
                .add(eeAttributesForm.asElement())
                .end()
                .build();
        navigation.addPrimary(EE_ATTRIBUTES_ENTRY, Names.DEPLOYMENTS, fontAwesome("archive"), navigationElement);

        // ============================================
        // global modules
        Metadata globalModulesMetadata = EEPresenter.globalModulesMetadata(metadataRegistry);

        Options<ModelNode> options = new ModelNodeTable.Builder<>(globalModulesMetadata)
                .columns(NAME, "slot", "annotations", "services", "meta-inf")
                .button(resources.constants().add(), (event, api) -> presenter.launchAddDialogGlobalModule())
                .button(resources.constants().remove(), Button.Scope.SELECTED,
                        (event, api) -> presenter.removeGlobalModule(api.selectedRow()))
                .build();
        globalModulesTable = new ModelNodeTable<>(Ids.EE_GLOBAL_MODULES_TABLE, options);
        registerAttachable(globalModulesTable);

        navigationElement = new Elements.Builder()
                .div()
                .h(1).textContent(Names.GLOBAL_MODULES).end()
                .p().textContent(globalModulesMetadata.getDescription().getDescription()).end()
                .add(globalModulesTable.asElement())
                .end()
                .build();
        navigation.addPrimary(EE_GLOBAL_MODULES_ENTRY, Names.GLOBAL_MODULES, fontAwesome("cube"), navigationElement);

        // ============================================
        // service=default-bindings
        Metadata defaultBindingsMetadata = metadataRegistry.lookup(AddressTemplates.SERVICE_DEFAULT_BINDINGS_TEMPLATE);

        ModelNodeForm<ModelNode> defaultBindingsForm = new ModelNodeForm.Builder<>(EE_DEFAULT_BINDINGS_FORM,
                defaultBindingsMetadata)
                .onSave((form, changedValues) -> presenter.save(AddressTemplates.SERVICE_DEFAULT_BINDINGS_TEMPLATE,
                        changedValues, DEFAULT_BINDINGS_NAME))
                .build();
        forms.put(EE_DEFAULT_BINDINGS_FORM, defaultBindingsForm);
        registerAttachable(defaultBindingsForm);

        navigationElement = new Elements.Builder()
                .div()
                .h(1).textContent(DEFAULT_BINDINGS_NAME).end()
                .p().textContent(defaultBindingsMetadata.getDescription().getDescription()).end()
                .add(defaultBindingsForm.asElement())
                .end()
                .build();
        navigation.addPrimary(EE_DEFAULT_BINDINGS_ENTRY, DEFAULT_BINDINGS_NAME, fontAwesome("link"),
                navigationElement);

        // ============================================
        // services
        navigation.addPrimary(EE_SERVICES_ENTRY, SERVICES_NAME, fontAwesome("cogs"));

        navigation.addSecondary(EE_SERVICES_ENTRY, EE_CONTEXT_SERVICE, CONTEXT_SERVICE_NAME,
                buildServicePanel(EE_CONTEXT_SERVICE, CONTEXT_SERVICE_TEMPLATE, CONTEXT_SERVICE_NAME));
        navigation.addSecondary(EE_SERVICES_ENTRY, EE_MANAGED_EXECUTOR, MANAGED_EXECUTOR_NAME,
                buildServicePanel(EE_MANAGED_EXECUTOR, MANAGED_EXECUTOR_TEMPLATE, MANAGED_EXECUTOR_NAME));
        navigation.addSecondary(EE_SERVICES_ENTRY, EE_MANAGED_EXECUTOR_SCHEDULED, MANAGED_EXECUTOR_SCHEDULED_NAME,
                buildServicePanel(EE_MANAGED_EXECUTOR_SCHEDULED, MANAGED_EXECUTOR_SCHEDULED_TEMPLATE,
                        MANAGED_EXECUTOR_SCHEDULED_NAME));
        navigation.addSecondary(EE_SERVICES_ENTRY, EE_MANAGED_THREAD_FACTORY, MANAGED_THREAD_FACTORY_NAME,
                buildServicePanel(EE_MANAGED_THREAD_FACTORY, MANAGED_THREAD_FACTORY_TEMPLATE,
                        MANAGED_THREAD_FACTORY_NAME));

        // ============================================
        // main layout
        // @formatter:off
        LayoutBuilder layoutBuilder = new LayoutBuilder()
            .row()
                .column()
                    .addAll(navigation.panes())
                .end()
            .end();
        // @formatter:on

        Element root = layoutBuilder.build();
        initElement(root);
    }

    @Override
    public void attach() {
        super.attach();
        bindFormToTable(ModelDescriptionConstants.CONTEXT_SERVICE);
        bindFormToTable(MANAGED_EXECUTOR_SERVICE);
        bindFormToTable(MANAGED_SCHEDULED_EXECUTOR_SERVICE);
        bindFormToTable(MANAGED_THREAD_FACTORY);
    }

    @SuppressWarnings("unchecked")
    private void bindFormToTable(String formName) {
        DataTable<ModelNode> table = tables.get(formName);
        table.api().bindForm(forms.get(formName));
    }

    @Override
    public void setPresenter(final EEPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public VerticalNavigation getVerticalNavigation() {
        return navigation;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(final ModelNode eeData) {
        // update the attributes - deployments tab
        Form<ModelNode> formDeployments = forms.get(EE_ATTRIBUTES_FORM);
        formDeployments.view(eeData);

        // update the global modules tab
        globalModulesTable.api().clear();
        if (eeData.hasDefined(GLOBAL_MODULES)) {
            List<ModelNode> globalModulesList = eeData.get(GLOBAL_MODULES).asList();
            globalModulesTable.api().add(globalModulesList).refresh(RESET);
        }

        // update the default-bindings tab
        if (eeData.hasDefined(SERVICE)) {
            ModelNode defaultBindings = eeData.get(SERVICE).get(ModelDescriptionConstants.DEFAULT_BINDINGS);
            Form<ModelNode> formDefaultBindings = forms.get(EE_DEFAULT_BINDINGS_FORM);
            formDefaultBindings.view(defaultBindings);
        }
        // update the context-service table
        update(eeData, ModelDescriptionConstants.CONTEXT_SERVICE, EE_CONTEXT_SERVICE);

        // update the managed-executor-service table
        update(eeData, MANAGED_EXECUTOR_SERVICE, EE_MANAGED_EXECUTOR);

        // update the managed-scheduled-executor-service table
        update(eeData, MANAGED_SCHEDULED_EXECUTOR_SERVICE, EE_MANAGED_EXECUTOR_SCHEDULED);

        // update the managed-thread-factory table
        update(eeData, MANAGED_THREAD_FACTORY, EE_MANAGED_THREAD_FACTORY);

    }

    @SuppressWarnings("unchecked")
    private void update(final ModelNode eeData, String resourceType, String navigationId) {
        if (eeData.hasDefined(resourceType)) {
            List<NamedNode> models = asNamedNodes(eeData.get(resourceType).asPropertyList());
            Form form = forms.get(resourceType);
            DataTable<NamedNode> table = tables.get(resourceType);
            table.api().clear().add(models).refresh(RESET);
            form.clear();
            navigation.updateBadge(navigationId, models.size());
        }
    }

    @SuppressWarnings("ConstantConditions")
    private Element buildServicePanel(String baseId, AddressTemplate template, String type) {

        Metadata metadata = metadataRegistry.lookup(template);

        Options<NamedNode> options = new ModelNodeTable.Builder<NamedNode>(metadata)
                .column(NAME, (cell, t, row, meta) -> row.getName())

                .button(tableButtonFactory.add(
                        IdBuilder.build(baseId, Ids.ADD_SUFFIX), type,
                        template,
                        () -> presenter.loadEESubsystem()))

                .button(tableButtonFactory.remove(
                        type,
                        template, (api) -> api.selectedRow().getName(),
                        () -> presenter.loadEESubsystem()))

                .build();

        ModelNodeTable<NamedNode> table = new ModelNodeTable<>(IdBuilder.build(baseId, Ids.TABLE_SUFFIX), options);
        registerAttachable(table);
        tables.put(template.lastKey(), table);

        ModelNodeForm<NamedNode> form = new ModelNodeForm.Builder<NamedNode>(IdBuilder.build(baseId, Ids.FORM_SUFFIX),
                metadata)
                .onSave((f, changedValues) -> {
                    AddressTemplate fullyQualified = template.replaceWildcards(table.api().selectedRow().getName());
                    presenter.save(fullyQualified, changedValues, template.lastKey());
                })
                .build();

        forms.put(template.lastKey(), form);
        registerAttachable(form);

        return new Elements.Builder()
                .div()
                .h(1).textContent(type).end()
                .p().textContent(metadata.getDescription().getDescription()).end()
                .add(table.asElement())
                .add(form.asElement())
                .end()
                .build();
    }
}
