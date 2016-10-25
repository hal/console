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
import javax.inject.Inject;

import elemental.dom.Element;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.LayoutBuilder;
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
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.configuration.subsystem.jca.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;

/**
 * @author Harald Pehl
 */
public class JcaView extends HalViewImpl implements JcaPresenter.MyView {

    private static final String WORKMANAGER = "workmanager";

    private final Form<ModelNode> ccmForm;
    private final Form<ModelNode> avForm;
    private final Form<ModelNode> bvForm;
    private final FailSafeModelNodeForm<ModelNode> failSafeTracerForm;
    private final DataTable<NamedNode> bcTable;
    private final Form<NamedNode> bcForm;
    private JcaPresenter presenter;

    @Inject
    @SuppressWarnings("ConstantConditions")
    public JcaView(final Dispatcher dispatcher,
            final StatementContext statementContext,
            final MetadataRegistry metadataRegistry,
            final TableButtonFactory tableButtonFactory,
            final Resources resources) {

        LabelBuilder labelBuilder = new LabelBuilder();
        VerticalNavigation navigation = new VerticalNavigation();

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
        Element commonConfigLayout = new LayoutBuilder()
            .row()
                .column()
                    .h(1).textContent(resources.constants().commonConfiguration()).end()
                    .p().textContent(resources.constants().jcaCommonConfiguration()).end()
                    .add(tabs)
                .end()
            .end()
        .build();
        // @formatter:on

        navigation.addPrimary(Ids.JCA_COMMON_CONFIGURATION_ENTRY, resources.constants().commonConfiguration(),
                pfIcon("settings"), commonConfigLayout);


        // ------------------------------------------------------ tracer

        String tracerType = labelBuilder.label(TRACER_TEMPLATE.lastKey());
        Metadata tracerMetadata = metadataRegistry.lookup(TRACER_TEMPLATE);
        Form<ModelNode> tracerForm = new ModelNodeForm.Builder<>(Ids.JCA_TRACER_FORM, tracerMetadata)
                .onSave((form, changedValues) -> presenter.saveSingleton(TRACER_TEMPLATE, changedValues,
                        resources.messages().modifySingleResourceSuccess(tracerType)))
                .build();
        failSafeTracerForm = new FailSafeModelNodeForm<>(dispatcher, () -> presenter.addTracer(), tracerForm,
                () -> presenter.lookupTracerOp());

        // @formatter:off
        Element tracerLayout = new LayoutBuilder()
            .row()
                .column()
                    .h(1).textContent(tracerType).end()
                    .p().textContent(tracerMetadata.getDescription().getDescription()).end()
                    .add(failSafeTracerForm)
                .end()
            .end()
        .build();
        // @formatter:on

        navigation.addPrimary(Ids.JCA_TRACER_ENTRY, tracerType, fontAwesome("bug"), tracerLayout);


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
                (name, model) -> presenter.addBootstrapContext(name, model));

        Options<NamedNode> bcTableOptions = new ModelNodeTable.Builder<NamedNode>(bcMetadata)
                .button(resources.constants().add(), (event, api) -> {
                    bcAddDialog.show();
                })
                .button(tableButtonFactory.remove(bcType, BOOTSTRAP_CONTEXT_TEMPLATE,
                        api -> api.selectedRow().getName(), () -> presenter.load()))
                .column(NAME)
                .build();
        bcTable = new ModelNodeTable<>(Ids.JCA_BOOTSTRAP_CONTEXT_TABLE, bcTableOptions);

        bcForm = new ModelNodeForm.Builder<NamedNode>(Ids.JCA_BOOTSTRAP_CONTEXT_FORM,
                bcMetadata)
                .onSave((form, changedValues) -> {
                    String bcName = bcTable.api().selectedRow().getName();
                    presenter.saveResource(BOOTSTRAP_CONTEXT_TEMPLATE, bcName, changedValues,
                            resources.messages().modifyResourceSuccess(bcType, bcName));
                })
                .build();
        bcForm.getFormItem(WORKMANAGER)
                .registerSuggestHandler(new ReadChildrenAutoComplete(dispatcher, statementContext,
                        Arrays.asList(WORKMANAGER_TEMPLATE, DISTRIBUTED_WORKMANAGER_TEMPLATE)));

        // @formatter:off
        Element bcLayout = new LayoutBuilder()
            .row()
                .column()
                    .h(1).textContent(bcType).end()
                    .p().textContent(bcMetadata.getDescription().getDescription()).end()
                    .add(bcTable)
                    .add(bcForm)
                .end()
            .end()
        .build();
        // @formatter:on

        navigation.addPrimary(Ids.JCA_BOOTSTRAP_CONTEXT_ENTRY, bcType, fontAwesome("play"), bcLayout);


        // ------------------------------------------------------ main layout

        registerAttachable(ccmForm, avForm, bvForm, failSafeTracerForm, bcTable, bcForm, navigation);

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
    }

    @Override
    public void attach() {
        super.attach();
        bcTable.api().bindForm(bcForm);
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
                .add(asNamedNodes(failSafePropertyList(payload, "bootstrap-context")))
                .refresh(RefreshMode.RESET);
    }
}
