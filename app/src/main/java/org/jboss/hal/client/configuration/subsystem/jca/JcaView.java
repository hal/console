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
package org.jboss.hal.client.configuration.subsystem.jca;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Api.RefreshMode;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.FailSafeModelNodeForm;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.configuration.subsystem.jca.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.resources.Names.THREAD_POOLS;

/**
 * Implementation note: Not based on MBUI XML due to special handling of long and short running thread pools for
 * (distributed) work manager (only one long and short running thread pool allowed per (distributed) work manager).
 *
 * @author Harald Pehl
 */
public class JcaView extends HalViewImpl implements JcaPresenter.MyView {

    private final LabelBuilder labelBuilder;
    private final Map<AddressTemplate, Pages> pages;
    private final Form<ModelNode> ccmForm;
    private final Form<ModelNode> avForm;
    private final Form<ModelNode> bvForm;
    private final FailSafeModelNodeForm<ModelNode> failSafeTracerForm;
    private final DataTable<NamedNode> bcTable;
    private final Form<NamedNode> bcForm;
    private final ModelNodeTable<NamedNode> wmTable;
    private final ThreadPoolsEditor wmTpEditor;
    private final ModelNodeTable<NamedNode> dwmTable;
    private final Form<NamedNode> dwmForm;
    private final ThreadPoolsEditor dwmTpEditor;

    private JcaPresenter presenter;

    @Inject
    @SuppressWarnings("ConstantConditions")
    public JcaView(final Dispatcher dispatcher,
            final StatementContext statementContext,
            final MetadataRegistry metadataRegistry,
            final TableButtonFactory tableButtonFactory,
            final Resources resources) {

        labelBuilder = new LabelBuilder();
        pages = new HashMap<>();
        VerticalNavigation navigation = new VerticalNavigation();
        registerAttachable(navigation);

        // ------------------------------------------------------ common config (ccm, av, bv)

        String ccmType = labelBuilder.label(CCM_TEMPLATE.lastValue());
        Metadata ccmMetadata = metadataRegistry.lookup(CCM_TEMPLATE);
        ccmForm = new ModelNodeForm.Builder<>(Ids.JCA_CCM_FORM, ccmMetadata)
                .onSave((form, changedValues) -> presenter
                        .saveSingleton(CCM_TEMPLATE, changedValues,
                                resources.messages().modifySingleResourceSuccess(ccmType)))
                .build();

        String avType = labelBuilder.label(ARCHIVE_VALIDATION_TEMPLATE.lastValue());
        Metadata avMetadata = metadataRegistry.lookup(ARCHIVE_VALIDATION_TEMPLATE);
        avForm = new ModelNodeForm.Builder<>(Ids.JCA_ARCHIVE_VALIDATION_FORM, avMetadata)
                .onSave((form, changedValues) -> presenter
                        .saveSingleton(ARCHIVE_VALIDATION_TEMPLATE, changedValues,
                                resources.messages().modifySingleResourceSuccess(avType)))
                .build();

        String bvType = labelBuilder.label(BEAN_VALIDATION_TEMPLATE.lastValue());
        Metadata bvMetadata = metadataRegistry.lookup(BEAN_VALIDATION_TEMPLATE);
        bvForm = new ModelNodeForm.Builder<>(Ids.JCA_BEAN_VALIDATION_FORM, bvMetadata)
                .onSave((form, changedValues) -> presenter
                        .saveSingleton(BEAN_VALIDATION_TEMPLATE, changedValues,
                                resources.messages().modifySingleResourceSuccess(bvType)))
                .build();

        Tabs tabs = new Tabs();
        tabs.add(Ids.JCA_CCM_TAB, ccmType, ccmForm.asElement());
        tabs.add(Ids.JCA_ARCHIVE_VALIDATION_TAB, avType, avForm.asElement());
        tabs.add(Ids.JCA_BEAN_VALIDATION_TAB, bvType, bvForm.asElement());

        // @formatter:off
        Element configLayout = new Elements.Builder()
            .div()
                .h(1).textContent(Names.CONFIGURATION).end()
                .p().textContent(resources.constants().jcaConfiguration()).end()
                .add(tabs)
            .end()
        .build();
        // @formatter:on

        navigation.addPrimary(Ids.JCA_CONFIGURATION_ENTRY, Names.CONFIGURATION, pfIcon("settings"), configLayout);
        registerAttachable(ccmForm, avForm, bvForm);


        // ------------------------------------------------------ tracer

        String tracerType = labelBuilder.label(TRACER_TEMPLATE.lastKey());
        Metadata tracerMetadata = metadataRegistry.lookup(TRACER_TEMPLATE);
        Form<ModelNode> tracerForm = new ModelNodeForm.Builder<>(Ids.JCA_TRACER_FORM, tracerMetadata)
                .onSave((form, changedValues) -> presenter.saveSingleton(TRACER_TEMPLATE, changedValues,
                        resources.messages().modifySingleResourceSuccess(tracerType)))
                .build();
        failSafeTracerForm = new FailSafeModelNodeForm<>(dispatcher,
                () -> new Operation.Builder(READ_RESOURCE_OPERATION, TRACER_TEMPLATE.resolve(statementContext)).build(),
                tracerForm, () -> presenter.addTracer());

        // @formatter:off
        Element tracerLayout = new Elements.Builder()
            .div()
                .h(1).textContent(tracerType).end()
                .p().textContent(tracerMetadata.getDescription().getDescription()).end()
                .add(failSafeTracerForm)
            .end()
        .build();
        // @formatter:on

        navigation.addPrimary(Ids.JCA_TRACER_ENTRY, tracerType, fontAwesome("bug"), tracerLayout);
        registerAttachable(failSafeTracerForm);


        // ------------------------------------------------------ bootstrap context (bc)

        String bcType = labelBuilder.label(BOOTSTRAP_CONTEXT_TEMPLATE.lastKey());
        Metadata bcMetadata = metadataRegistry.lookup(BOOTSTRAP_CONTEXT_TEMPLATE);

        Form<ModelNode> bcAddForm = new ModelNodeForm.Builder<>(Ids.JCA_BOOTSTRAP_CONTEXT_ADD, bcMetadata)
                .addFromRequestProperties()
                .requiredOnly()
                .build();
        bcAddForm.getFormItem(WORKMANAGER).registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, statementContext,
                        Arrays.asList(WORKMANAGER_TEMPLATE, DISTRIBUTED_WORKMANAGER_TEMPLATE)));
        AddResourceDialog bcAddDialog = new AddResourceDialog(resources.messages().addResourceTitle(bcType), bcAddForm,
                (name, model) -> presenter.add(bcType, name, BOOTSTRAP_CONTEXT_TEMPLATE, model));

        Options<NamedNode> bcTableOptions = new ModelNodeTable.Builder<NamedNode>(bcMetadata)
                .button(resources.constants().add(), (event, api) -> bcAddDialog.show())
                .button(tableButtonFactory.remove(bcType, BOOTSTRAP_CONTEXT_TEMPLATE,
                        api -> api.selectedRow().getName(), () -> presenter.load()))
                .column(NAME)
                .build();
        bcTable = new ModelNodeTable<>(Ids.JCA_BOOTSTRAP_CONTEXT_TABLE, bcTableOptions);

        bcForm = new ModelNodeForm.Builder<NamedNode>(Ids.JCA_BOOTSTRAP_CONTEXT_FORM,
                bcMetadata)
                .onSave((form, changedValues) -> {
                    String bcName = form.getModel().getName();
                    presenter.saveResource(BOOTSTRAP_CONTEXT_TEMPLATE, bcName, changedValues,
                            resources.messages().modifyResourceSuccess(bcType, bcName));
                })
                .build();
        bcForm.getFormItem(WORKMANAGER)
                .registerSuggestHandler(new ReadChildrenAutoComplete(dispatcher, statementContext,
                        Arrays.asList(WORKMANAGER_TEMPLATE, DISTRIBUTED_WORKMANAGER_TEMPLATE)));

        // @formatter:off
        Element bcLayout = new Elements.Builder()
            .div()
                .h(1).textContent(bcType).end()
                .p().textContent(bcMetadata.getDescription().getDescription()).end()
                .add(bcTable)
                .add(bcForm)
            .end()
        .build();
        // @formatter:on

        navigation.addPrimary(Ids.JCA_BOOTSTRAP_CONTEXT_ENTRY, bcType, fontAwesome("play"), bcLayout);
        registerAttachable(bcTable, bcForm);

        // ------------------------------------------------------ workmanager

        String wmType = labelBuilder.label(WORKMANAGER_TEMPLATE.lastKey());
        Metadata wmMetadata = metadataRegistry.lookup(WORKMANAGER_TEMPLATE);

        Form<ModelNode> wmAddForm = new ModelNodeForm.Builder<>(Ids.JCA_WORKMANAGER_ADD, wmMetadata)
                .addFromRequestProperties()
                .requiredOnly()
                .build();
        AddResourceDialog wmAddDialog = new AddResourceDialog(resources.messages().addResourceTitle(wmType), wmAddForm,
                (name, model) -> presenter.add(wmType, name, WORKMANAGER_TEMPLATE, model));

        Options<NamedNode> wmOptions = new ModelNodeTable.Builder<NamedNode>(wmMetadata)
                .button(resources.constants().add(), (event, api) -> wmAddDialog.show())
                .button(tableButtonFactory.remove(wmType, WORKMANAGER_TEMPLATE, api -> api.selectedRow().getName(),
                        () -> presenter.load()))
                .column(NAME)
                .column(THREAD_POOLS, row -> presenter.loadThreadPools(WORKMANAGER_TEMPLATE, row.getName()))
                .build();
        wmTable = new ModelNodeTable<>(Ids.JCA_WORKMANAGER_TABLE, wmOptions);

        // @formatter:off
        Element wmLayout = new Elements.Builder()
            .div()
                .h(1).textContent(wmType).end()
                .p().textContent(wmMetadata.getDescription().getDescription()).end()
                .add(wmTable)
            .end()
        .build();
        // @formatter:on

        wmTpEditor = new ThreadPoolsEditor(Ids.JCA_WORKMANAGER, metadataRegistry, resources);
        registerAttachable(wmTpEditor);

        Pages wmPages = new Pages(wmType, wmLayout)
                .addPage(Ids.JCA_THREAD_POOL_PAGE, THREAD_POOLS, wmTpEditor.asElement());
        pages.put(WORKMANAGER_TEMPLATE, wmPages);

        navigation.addPrimary(Ids.JCA_WORKMANAGER_ENTRY, wmType, fontAwesome("cog"), wmPages);
        registerAttachable(wmTable);

        // ------------------------------------------------------ distributed workmanager

        String dwmType = labelBuilder.label(DISTRIBUTED_WORKMANAGER_TEMPLATE.lastKey());
        Metadata dwmMetadata = metadataRegistry.lookup(DISTRIBUTED_WORKMANAGER_TEMPLATE);

        Form<ModelNode> dwmAddForm = new ModelNodeForm.Builder<>(Ids.JCA_DISTRIBUTED_WORKMANAGER_ADD, wmMetadata)
                .addFromRequestProperties()
                .requiredOnly()
                .build();
        AddResourceDialog dwmAddDialog = new AddResourceDialog(resources.messages().addResourceTitle(dwmType),
                dwmAddForm, (name, model) -> presenter.add(dwmType, name, DISTRIBUTED_WORKMANAGER_TEMPLATE, model));

        Options<NamedNode> dwmOptions = new ModelNodeTable.Builder<NamedNode>(dwmMetadata)
                .button(resources.constants().add(), (event, api) -> dwmAddDialog.show())
                .button(tableButtonFactory.remove(dwmType, DISTRIBUTED_WORKMANAGER_TEMPLATE,
                        api -> api.selectedRow().getName(), () -> presenter.load()))
                .column(NAME)
                .column(POLICY)
                .column(SELECTOR)
                .column(THREAD_POOLS, row -> presenter.loadThreadPools(DISTRIBUTED_WORKMANAGER_TEMPLATE, row.getName()))
                .build();
        dwmTable = new ModelNodeTable<>(Ids.JCA_DISTRIBUTED_WORKMANAGER_TABLE, dwmOptions);

        dwmForm = new ModelNodeForm.Builder<NamedNode>(
                Ids.JCA_DISTRIBUTED_WORKMANAGER_FORM, dwmMetadata)
                .onSave((form, changedValues) -> {
                    String dwmName = form.getModel().getName();
                    presenter.saveResource(DISTRIBUTED_WORKMANAGER_TEMPLATE, dwmName, changedValues,
                            resources.messages().modifyResourceSuccess(dwmType, dwmName));
                })
                .build();

        // @formatter:off
        Element dwmLayout = new Elements.Builder()
            .div()
                .h(1).textContent(dwmType).end()
                .p().textContent(dwmMetadata.getDescription().getDescription()).end()
                .add(dwmTable)
                .add(dwmForm)
            .end()
        .build();
        // @formatter:on

        dwmTpEditor = new ThreadPoolsEditor(Ids.JCA_DISTRIBUTED_WORKMANAGER, metadataRegistry, resources);
        registerAttachable(dwmTpEditor);

        Pages dwmPages = new Pages(dwmType, dwmLayout)
                .addPage(Ids.JCA_THREAD_POOL_PAGE, THREAD_POOLS, dwmTpEditor.asElement());
        pages.put(DISTRIBUTED_WORKMANAGER_TEMPLATE, dwmPages);

        navigation.addPrimary(Ids.JCA_DISTRIBUTED_WORKMANAGER_ENTRY, dwmType, fontAwesome("cogs"), dwmPages);
        registerAttachable(dwmTable, dwmForm);

        // ------------------------------------------------------ main layout

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
    public void setPresenter(final JcaPresenter presenter) {
        this.presenter = presenter;
        wmTpEditor.setPresenter(presenter);
        dwmTpEditor.setPresenter(presenter);
    }

    @Override
    public void attach() {
        super.attach();
        bcTable.api().bindForm(bcForm);
        dwmTable.api().bindForm(dwmForm);
    }

    @Override
    public void reveal() {
        pages.values().forEach(Pages::showMain);
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public void update(final ModelNode payload) {
        ccmForm.view(failSafeGet(payload, "cached-connection-manager/cached-connection-manager"));
        avForm.view(failSafeGet(payload, "archive-validation/archive-validation"));
        bvForm.view(failSafeGet(payload, "bean-validation/bean-validation"));

        failSafeTracerForm.view(failSafeGet(payload, "tracer/tracer"));

        bcTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, BOOTSTRAP_CONTEXT_TEMPLATE.lastKey())))
                .refresh(RefreshMode.RESET);

        wmTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, WORKMANAGER_TEMPLATE.lastKey())))
                .refresh(RefreshMode.RESET);

        dwmTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, DISTRIBUTED_WORKMANAGER_TEMPLATE.lastKey())))
                .refresh(RefreshMode.RESET);
    }

    @Override
    public void updateThreadPools(final AddressTemplate workmanagerTemplate, final String workmanager,
            final List<Property> lrt, final List<Property> srt) {
        Pages pages = this.pages.get(workmanagerTemplate);
        if (pages != null) {
            pages.showPage(Ids.JCA_THREAD_POOL_PAGE);
            pages.updateBreadcrumb(Ids.JCA_THREAD_POOL_PAGE,
                    labelBuilder.label(workmanagerTemplate.lastKey()) + ": " + workmanager);
        }
        if (WORKMANAGER.equals(workmanagerTemplate.lastKey())) {
            wmTpEditor.update(workmanagerTemplate, workmanager, lrt, srt);
        } else {
            dwmTpEditor.update(workmanagerTemplate, workmanager, lrt, srt);
        }
    }
}
