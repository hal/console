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
package org.jboss.hal.client.configuration.subsystem.jca;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.InlineAction;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.configuration.subsystem.jca.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.resources.Names.THREAD_POOLS;

/**
 * Implementation note: Not based on MBUI XML due to special handling of long and short running thread pools for (distributed)
 * work manager (only one long and short running thread pool allowed per (distributed) work manager).
 */
public class JcaView extends HalViewImpl implements JcaPresenter.MyView {

    private final LabelBuilder labelBuilder;
    private final Map<AddressTemplate, Pages> pages;
    private final Form<ModelNode> ccmForm;
    private final Form<ModelNode> avForm;
    private final Form<ModelNode> bvForm;
    private final Form<ModelNode> tracerForm;
    private final Table<NamedNode> bcTable;
    private final Form<NamedNode> bcForm;
    private final Table<NamedNode> wmTable;
    private final ThreadPoolsEditor wmTpEditor;
    private final Table<NamedNode> dwmTable;
    private final Form<NamedNode> dwmForm;
    private final ThreadPoolsEditor dwmTpEditor;

    private JcaPresenter presenter;
    private String selectedWorkmanager;

    @Inject
    @SuppressWarnings("ConstantConditions")
    public JcaView(Dispatcher dispatcher,
            StatementContext statementContext,
            MetadataRegistry metadataRegistry,
            TableButtonFactory tableButtonFactory,
            Resources resources) {

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
                .prepareReset(f -> presenter.resetSingleton(ccmType, CCM_TEMPLATE, f, ccmMetadata))
                .build();

        String avType = labelBuilder.label(ARCHIVE_VALIDATION_TEMPLATE.lastValue());
        Metadata avMetadata = metadataRegistry.lookup(ARCHIVE_VALIDATION_TEMPLATE);
        avForm = new ModelNodeForm.Builder<>(Ids.JCA_ARCHIVE_VALIDATION_FORM, avMetadata)
                .onSave((form, changedValues) -> presenter
                        .saveSingleton(ARCHIVE_VALIDATION_TEMPLATE, changedValues,
                                resources.messages().modifySingleResourceSuccess(avType)))
                .prepareReset(f -> presenter.resetSingleton(avType, ARCHIVE_VALIDATION_TEMPLATE, f, avMetadata))
                .build();

        String bvType = labelBuilder.label(BEAN_VALIDATION_TEMPLATE.lastValue());
        Metadata bvMetadata = metadataRegistry.lookup(BEAN_VALIDATION_TEMPLATE);
        bvForm = new ModelNodeForm.Builder<>(Ids.JCA_BEAN_VALIDATION_FORM, bvMetadata)
                .onSave((form, changedValues) -> presenter
                        .saveSingleton(BEAN_VALIDATION_TEMPLATE, changedValues,
                                resources.messages().modifySingleResourceSuccess(bvType)))
                .prepareReset(f -> presenter.resetSingleton(bvType, BEAN_VALIDATION_TEMPLATE, f, bvMetadata))
                .build();

        Tabs tabs = new Tabs(Ids.JCA_TAB_CONTAINER);
        tabs.add(Ids.JCA_CCM_TAB, ccmType, ccmForm.element());
        tabs.add(Ids.JCA_ARCHIVE_VALIDATION_TAB, avType, avForm.element());
        tabs.add(Ids.JCA_BEAN_VALIDATION_TAB, bvType, bvForm.element());

        HTMLElement configLayout = div()
                .add(h(1).textContent(Names.CONFIGURATION))
                .add(p().textContent(resources.constants().jcaConfiguration()))
                .add(tabs).element();

        navigation.addPrimary(Ids.JCA_CONFIGURATION_ITEM, Names.CONFIGURATION, pfIcon("settings"), configLayout);
        registerAttachable(ccmForm, avForm, bvForm);

        // ------------------------------------------------------ tracer

        String tracerType = labelBuilder.label(TRACER_TEMPLATE.lastName());
        Metadata tracerMetadata = metadataRegistry.lookup(TRACER_TEMPLATE);
        tracerForm = new ModelNodeForm.Builder<>(Ids.JCA_TRACER_FORM, tracerMetadata)
                .singleton(() -> new Operation.Builder(TRACER_TEMPLATE.resolve(statementContext),
                        READ_RESOURCE_OPERATION).build(),
                        () -> presenter.addTracer())
                .onSave((form, changedValues) -> presenter.saveSingleton(TRACER_TEMPLATE, changedValues,
                        resources.messages().modifySingleResourceSuccess(tracerType)))
                .prepareReset(f -> presenter.resetSingleton(tracerType, TRACER_TEMPLATE, f, tracerMetadata))
                .prepareRemove(f -> presenter.removeSingleton(tracerType, TRACER_TEMPLATE, f))
                .build();

        HTMLElement tracerLayout = div()
                .add(h(1).textContent(tracerType))
                .add(p().textContent(tracerMetadata.getDescription().getDescription()))
                .add(tracerForm).element();

        navigation.addPrimary(Ids.JCA_TRACER_ITEM, tracerType, fontAwesome("bug"), tracerLayout);
        registerAttachable(tracerForm);

        // ------------------------------------------------------ bootstrap context (bc)

        String bcType = labelBuilder.label(BOOTSTRAP_CONTEXT_TEMPLATE.lastName());
        Metadata bcMetadata = metadataRegistry.lookup(BOOTSTRAP_CONTEXT_TEMPLATE);

        Form<ModelNode> bcAddForm = new ModelNodeForm.Builder<>(Ids.JCA_BOOTSTRAP_CONTEXT_ADD, bcMetadata)
                .fromRequestProperties()
                .requiredOnly()
                .build();
        bcAddForm.getFormItem(WORKMANAGER).registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, statementContext,
                        Arrays.asList(WORKMANAGER_TEMPLATE, DISTRIBUTED_WORKMANAGER_TEMPLATE)));
        AddResourceDialog bcAddDialog = new AddResourceDialog(resources.messages().addResourceTitle(bcType), bcAddForm,
                (name, model) -> presenter.add(bcType, name, BOOTSTRAP_CONTEXT_TEMPLATE, model));

        bcTable = new ModelNodeTable.Builder<NamedNode>(Ids.JCA_BOOTSTRAP_CONTEXT_TABLE, bcMetadata)
                .button(tableButtonFactory.add(BOOTSTRAP_CONTEXT_TEMPLATE, table -> bcAddDialog.show()))
                .button(tableButtonFactory.remove(bcType, BOOTSTRAP_CONTEXT_TEMPLATE,
                        api -> api.selectedRow().getName(), () -> presenter.reload()))
                .column(NAME)
                .build();

        bcForm = new ModelNodeForm.Builder<NamedNode>(Ids.JCA_BOOTSTRAP_CONTEXT_FORM, bcMetadata)
                .onSave((form, changedValues) -> {
                    String bcName = form.getModel().getName();
                    presenter.saveResource(BOOTSTRAP_CONTEXT_TEMPLATE, bcName, changedValues,
                            resources.messages().modifyResourceSuccess(bcType, bcName));
                })
                .prepareReset(form -> {
                    String bcName = form.getModel().getName();
                    presenter.resetResource(BOOTSTRAP_CONTEXT_TEMPLATE, bcType, bcName, form, bcMetadata);
                })
                .build();
        bcForm.getFormItem(WORKMANAGER)
                .registerSuggestHandler(new ReadChildrenAutoComplete(dispatcher, statementContext,
                        Arrays.asList(WORKMANAGER_TEMPLATE, DISTRIBUTED_WORKMANAGER_TEMPLATE)));

        HTMLElement bcLayout = div()
                .add(h(1).textContent(bcType))
                .add(p().textContent(bcMetadata.getDescription().getDescription()))
                .add(bcTable)
                .add(bcForm).element();

        navigation.addPrimary(Ids.JCA_BOOTSTRAP_CONTEXT_ITEM, bcType, fontAwesome("play"), bcLayout);
        registerAttachable(bcTable, bcForm);

        // ------------------------------------------------------ workmanager

        String wmType = labelBuilder.label(WORKMANAGER_TEMPLATE.lastName());
        Metadata wmMetadata = metadataRegistry.lookup(WORKMANAGER_TEMPLATE);

        Form<ModelNode> wmAddForm = new ModelNodeForm.Builder<>(Ids.JCA_WORKMANAGER_ADD, wmMetadata)
                .fromRequestProperties()
                .include(NAME, ELYTRON_ENABLED)
                .unsorted()
                .build();
        AddResourceDialog wmAddDialog = new AddResourceDialog(resources.messages().addResourceTitle(wmType), wmAddForm,
                (name, model) -> presenter.add(wmType, name, WORKMANAGER_TEMPLATE, model));

        wmTable = new ModelNodeTable.Builder<NamedNode>(Ids.JCA_WORKMANAGER_TABLE, wmMetadata)
                .button(tableButtonFactory.add(WORKMANAGER_TEMPLATE, table -> wmAddDialog.show()))
                .button(tableButtonFactory.remove(wmType, WORKMANAGER_TEMPLATE, api -> api.selectedRow().getName(),
                        () -> presenter.reload()))
                .column(NAME)
                .column(ELYTRON_ENABLED)
                .column(new InlineAction<>(THREAD_POOLS,
                        row -> presenter.loadThreadPools(WORKMANAGER_TEMPLATE, row.getName())))
                .build();

        HTMLElement wmLayout = div()
                .add(h(1).textContent(wmType))
                .add(p().textContent(wmMetadata.getDescription().getDescription()))
                .add(wmTable).element();

        wmTpEditor = new ThreadPoolsEditor(Ids.JCA_WORKMANAGER, metadataRegistry, tableButtonFactory, resources);
        registerAttachable(wmTpEditor);

        Pages wmPages = new Pages(Ids.JCA_WORKMANAGER_PAGES, Ids.JCA_WORKMANAGER_PAGE, wmLayout);
        wmPages.addPage(Ids.JCA_WORKMANAGER_PAGE, Ids.JCA_THREAD_POOL_PAGE,
                () -> labelBuilder.label(wmType) + ": " + selectedWorkmanager, () -> Names.THREAD_POOLS,
                wmTpEditor.element());
        pages.put(WORKMANAGER_TEMPLATE, wmPages);

        navigation.addPrimary(Ids.JCA_WORKMANAGER_ITEM, wmType, fontAwesome("cog"), wmPages);
        registerAttachable(wmTable);

        // ------------------------------------------------------ distributed workmanager

        String dwmType = labelBuilder.label(DISTRIBUTED_WORKMANAGER_TEMPLATE.lastName());
        Metadata dwmMetadata = metadataRegistry.lookup(DISTRIBUTED_WORKMANAGER_TEMPLATE);
        Metadata srtMetadata = metadataRegistry.lookup(WORKMANAGER_SRT_TEMPLATE);

        // short-running-thread is required for a distributed workmanager
        Property maxThreadsDesc = srtMetadata.getDescription().findAttribute(ATTRIBUTES, MAX_THREADS);
        Property queueLengthDesc = srtMetadata.getDescription().findAttribute(ATTRIBUTES, QUEUE_LENGTH);
        ModelNode addOpDwm = dwmMetadata.getDescription().get(OPERATIONS).get(ADD).get(REQUEST_PROPERTIES);
        addOpDwm.get(MAX_THREADS).set(maxThreadsDesc.getValue());
        addOpDwm.get(QUEUE_LENGTH).set(queueLengthDesc.getValue());
        dwmMetadata.makeWritable(MAX_THREADS);
        dwmMetadata.makeWritable(QUEUE_LENGTH);

        Form<ModelNode> dwmAddForm = new ModelNodeForm.Builder<>(Ids.JCA_DISTRIBUTED_WORKMANAGER_ADD, dwmMetadata)
                .include(NAME, ELYTRON_ENABLED)
                .unsorted()
                .fromRequestProperties()
                .build();
        AddResourceDialog dwmAddDialog = new AddResourceDialog(resources.messages().addResourceTitle(dwmType),
                dwmAddForm, (name, model) -> presenter.addDistributedWorkManager(dwmType, name, model));

        dwmTable = new ModelNodeTable.Builder<NamedNode>(Ids.JCA_DISTRIBUTED_WORKMANAGER_TABLE, dwmMetadata)
                .button(tableButtonFactory.add(DISTRIBUTED_WORKMANAGER_TEMPLATE, table -> dwmAddDialog.show()))
                .button(tableButtonFactory.remove(dwmType, DISTRIBUTED_WORKMANAGER_TEMPLATE,
                        api -> api.selectedRow().getName(), () -> presenter.reload()))
                .column(NAME)
                .column(ELYTRON_ENABLED)
                .column(POLICY)
                .column(SELECTOR)
                .column(new InlineAction<>(Names.THREAD_POOLS,
                        row -> presenter.loadThreadPools(DISTRIBUTED_WORKMANAGER_TEMPLATE, row.getName())))
                .build();

        dwmForm = new ModelNodeForm.Builder<NamedNode>(Ids.JCA_DISTRIBUTED_WORKMANAGER_FORM, dwmMetadata)
                .onSave((form, changedValues) -> {
                    String dwmName = form.getModel().getName();
                    presenter.saveResource(DISTRIBUTED_WORKMANAGER_TEMPLATE, dwmName, changedValues,
                            resources.messages().modifyResourceSuccess(dwmType, dwmName));
                })
                .prepareReset(form -> {
                    String dwmName = form.getModel().getName();
                    presenter.resetResource(DISTRIBUTED_WORKMANAGER_TEMPLATE, dwmType, dwmName, form, dwmMetadata);
                })
                .build();

        HTMLElement dwmLayout = div()
                .add(h(1).textContent(dwmType))
                .add(p().textContent(dwmMetadata.getDescription().getDescription()))
                .add(dwmTable)
                .add(dwmForm).element();

        dwmTpEditor = new ThreadPoolsEditor(Ids.JCA_DISTRIBUTED_WORKMANAGER, metadataRegistry, tableButtonFactory,
                resources);
        registerAttachable(dwmTpEditor);

        Pages dwmPages = new Pages(Ids.JCA_DISTRIBUTED_WORKMANAGER_PAGES, Ids.JCA_DISTRIBUTED_WORKMANAGER_PAGE,
                dwmLayout);
        dwmPages.addPage(Ids.JCA_DISTRIBUTED_WORKMANAGER_PAGE, Ids.JCA_THREAD_POOL_PAGE,
                () -> labelBuilder.label(dwmType) + ": " + selectedWorkmanager, () -> THREAD_POOLS,
                dwmTpEditor.element());
        pages.put(DISTRIBUTED_WORKMANAGER_TEMPLATE, dwmPages);

        navigation.addPrimary(Ids.JCA_DISTRIBUTED_WORKMANAGER_ITEM, dwmType, fontAwesome("cogs"), dwmPages);
        registerAttachable(dwmTable, dwmForm);

        // ------------------------------------------------------ main layout

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
    }

    @Override
    public void setPresenter(JcaPresenter presenter) {
        this.presenter = presenter;
        wmTpEditor.setPresenter(presenter);
        dwmTpEditor.setPresenter(presenter);
    }

    @Override
    public void attach() {
        super.attach();
        bcTable.bindForm(bcForm);
        dwmTable.bindForm(dwmForm);
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public void update(ModelNode payload) {
        ccmForm.view(failSafeGet(payload, "cached-connection-manager/cached-connection-manager"));
        avForm.view(failSafeGet(payload, "archive-validation/archive-validation"));
        bvForm.view(failSafeGet(payload, "bean-validation/bean-validation"));

        tracerForm.view(failSafeGet(payload, "tracer/tracer"));

        bcForm.clear();
        dwmForm.clear();

        bcTable.update(asNamedNodes(failSafePropertyList(payload, BOOTSTRAP_CONTEXT_TEMPLATE.lastName())));
        wmTable.update(asNamedNodes(failSafePropertyList(payload, WORKMANAGER_TEMPLATE.lastName())));
        dwmTable.update(asNamedNodes(failSafePropertyList(payload, DISTRIBUTED_WORKMANAGER_TEMPLATE.lastName())));
    }

    @Override
    public void updateThreadPools(AddressTemplate workmanagerTemplate, String workmanager,
            List<Property> lrt, List<Property> srt) {
        selectedWorkmanager = workmanager;
        Pages pages = this.pages.get(workmanagerTemplate);
        if (pages != null) {
            pages.showPage(Ids.JCA_THREAD_POOL_PAGE);
        }
        if (WORKMANAGER.equals(workmanagerTemplate.lastName())) {
            wmTpEditor.update(workmanagerTemplate, workmanager, lrt, srt);
        } else {
            dwmTpEditor.update(workmanagerTemplate, workmanager, lrt, srt);
        }
    }
}
