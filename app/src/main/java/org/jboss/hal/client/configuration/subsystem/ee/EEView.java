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

import com.google.gwt.core.client.Scheduler;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptions;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityFramework;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.ballroom.table.Api.RefreshMode.RESET;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.Ids.*;

/**
 * @author Claudio Miranda
 */
public class EEView extends PatternFlyViewImpl implements EEPresenter.MyView {

    private static final String HEADER_ELEMENT = "headerElement";

    private final Map<String, ModelNodeForm<ModelNode>> forms = new HashMap<>(2);
    private final VerticalNavigation navigation;
    private final DataTable<ModelNode> globalModulesTable;
    private EEPresenter presenter;

    @Inject
    public EEView(SecurityFramework securityFramework,
            ResourceDescriptions descriptions,
            Capabilities capabilities,
            final Resources resources) {

        navigation = new VerticalNavigation();

        // ============================================
        // attributes - deployments
        SecurityContext securityContext = securityFramework.lookup(AddressTemplates.EE_SUBSYSTEM_TEMPLATE);
        ResourceDescription eeDescription = descriptions.lookup(AddressTemplates.EE_SUBSYSTEM_TEMPLATE);
        Metadata metadata = new Metadata(securityContext, eeDescription, capabilities);

        ModelNodeForm<ModelNode> eeAttributesForm = new ModelNodeForm.Builder<>(EE_ATTRIBUTES_FORM, metadata)
                .include("annotation-property-replacement",
                        "ear-subdeployments-isolated",
                        "jboss-descriptor-property-replacement",
                        "spec-descriptor-property-replacement")
                .onSave((form1, changedValues1) -> presenter.saveAttributes(changedValues1))
                .build();

        forms.put(EE_ATTRIBUTES_FORM, eeAttributesForm);
        navigation.add(EE_ATTRIBUTES_ENTRY, Names.DEPLOYMENTS, fontAwesome("archive"), eeAttributesForm.asElement());
        registerAttachable(eeAttributesForm);

        // ============================================
        // global modules
        Metadata globalModulesMetadata = EEPresenter
                .globalModulesMetadata(securityFramework, descriptions, capabilities);

        Options<ModelNode> options = new ModelNodeTable.Builder<>(globalModulesMetadata)
                .columns(NAME, "slot", "annotations", "services", "meta-inf")
                .button(resources.constants().add(), (event, api) -> presenter.launchAddDialogGlobalModule())
                .button(resources.constants().remove(), (event, api) -> presenter.removeGlobalModule(api.selectedRow()))
                .build();

        globalModulesTable = new ModelNodeTable<>(Ids.EE_GLOBAL_MODULES_TABLE, options);
        // Register as 'IsElement' instead of 'Element' since the data table's root element changes after it has been
        // attached to the DOM. If registered as 'IsElement' the navigation will take care of this when showing and
        // hiding the table
        navigation.add(EE_GLOBAL_MODULES_ENTRY, Names.GLOBAL_MODULES, fontAwesome("cube"), globalModulesTable);
        registerAttachable(globalModulesTable);

        // ============================================
        // service=default-bindings
        ResourceDescription defaultBindingsResource = descriptions
                .lookup(AddressTemplates.SERVICE_DEFAULT_BINDINGS_TEMPLATE);
        SecurityContext securityContextDefBindings = securityFramework
                .lookup(AddressTemplates.SERVICE_DEFAULT_BINDINGS_TEMPLATE);
        Metadata defaultBindingsMetadata = new Metadata(securityContextDefBindings, defaultBindingsResource,
                capabilities);

        ModelNodeForm<ModelNode> defaultBindingsForm = new ModelNodeForm.Builder<>(EE_DEFAULT_BINDINGS_FORM,
                defaultBindingsMetadata)
                .include("context-service",
                        "datasource",
                        "jms-connection-factory",
                        "managed-executor-service",
                        "managed-scheduled-executor-service",
                        "managed-thread-factory")
                .onSave((form, changedValues) -> presenter.saveDefaultBindings(changedValues))
                .build();

        forms.put(EE_DEFAULT_BINDINGS_FORM, defaultBindingsForm);
        navigation.add(EE_DEFAULT_BINDINGS_ENTRY, resources.constants().defaultBindings(), fontAwesome("link"),
                defaultBindingsForm.asElement());
        registerAttachable(defaultBindingsForm);

        // ============================================
        // services
        Tabs serviceTabs = new Tabs();
        Element nyi = Browser.getDocument().createElement("p");
        nyi.setTextContent(Names.NYI);
        serviceTabs.add(IdBuilder.build(EE, "service", "context-service"), "Context Service",
                ((Element) nyi.cloneNode(true)));
        serviceTabs.add(IdBuilder.build(EE, "service", "executor"), "Executor",
                ((Element) nyi.cloneNode(true)));
        serviceTabs.add(IdBuilder.build(EE, "service", "scheduled-executor"), "Scheduled Executor",
                ((Element) nyi.cloneNode(true)));
        serviceTabs.add(IdBuilder.build(EE, "service", "thread-factories"), "Thread Factories",
                ((Element) nyi.cloneNode(true)));
        navigation.add(IdBuilder.build(EE, "services", "entry"), "Services", fontAwesome("cogs"), serviceTabs);

        // ============================================
        // main layout
        Element info = new Elements.Builder().p().textContent(eeDescription
                .getDescription() + " It provides the ability to specify a list of modules that should be made available to all deployments.")
                .end().build();

        // @formatter:off
        LayoutBuilder layoutBuilder = new LayoutBuilder()
            .row()
                .column()
                    .header(Names.EE).rememberAs(HEADER_ELEMENT).end()
                    .add(info)
                    .addAll(navigation.panes())
                .end()
            .end();
        // @formatter:on

        Element root = layoutBuilder.build();
        initElement(root);
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
    public void reset() {
        Scheduler.get().scheduleDeferred(() -> navigation.show(EE_ATTRIBUTES_ENTRY));
    }

    @Override
    public void update(final ModelNode eeData) {
        // update the attributes - deployments tab
        Form<ModelNode> formDeployments = forms.get(EE_ATTRIBUTES_FORM);
        formDeployments.view(eeData);

        // update the global modules tab
        globalModulesTable.api().clear();
        if (eeData.hasDefined(GLOBAL_MODULES)) {
            List<ModelNode> llp = eeData.get(GLOBAL_MODULES).asList();
            globalModulesTable.api().add(llp).refresh(RESET);
        }

        // update the default-bindings tab
        if (eeData.hasDefined(SERVICE)) {
            ModelNode defaultBindings = eeData.get(SERVICE).get(DEFAULT_BINDINGS);
            Form<ModelNode> formDefaulBindings = forms.get(EE_DEFAULT_BINDINGS_FORM);
            formDefaulBindings.view(defaultBindings);
        }
    }
}
