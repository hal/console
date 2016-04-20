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

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.Tabs;
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
import org.jboss.hal.resources.*;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jboss.hal.ballroom.table.Api.RefreshMode.RESET;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.Ids.*;

/**
 * @author Claudio Miranda
 */
public class EEView extends PatternFlyViewImpl implements EEPresenter.MyView {

    private static final String HEADER_ELEMENT = "headerElement";

    private Element header;
    private EEPresenter presenter;
    private final Resources resources;
    private Map<String, ModelNodeForm<ModelNode>> forms = new HashMap<>(2);
    private DataTable<ModelNode> globalModulesTable;

    @Inject
    public EEView(SecurityFramework securityFramework, 
                  ResourceDescriptions descriptions,
                  Capabilities capabilities, final Resources resources) {

        Tabs tabs = new Tabs();
        this.resources = resources;
        
        // ============================================
        // attributes - deployments tab
        SecurityContext securityContext = securityFramework.lookup(AddressTemplates.EE_SUBSYSTEM_TEMPLATE);
        ResourceDescription descriptionEESbsystem = descriptions.lookup(AddressTemplates.EE_SUBSYSTEM_TEMPLATE);
        Metadata metadata = new Metadata(securityContext, descriptionEESbsystem, capabilities);

        Element info = new Elements.Builder().p().textContent(descriptionEESbsystem.getDescription() + " It provides the ability to specify a list of modules that should be made available to all deployments.").end().build();

        ModelNodeForm<ModelNode> currentForm;
        Form.SaveCallback<ModelNode> saveCallback = (form, changedValues) -> presenter.saveAttributes(changedValues);

        currentForm = new ModelNodeForm.Builder<>(EE_ATTRIBUTES_FORM, metadata)
                .include("annotation-property-replacement", 
                         "ear-subdeployments-isolated", 
                         "jboss-descriptor-property-replacement",
                         "spec-descriptor-property-replacement")
                .onSave(saveCallback)
                .build();
        forms.put(EE_ATTRIBUTES_FORM, currentForm);
        tabs.add(EE_ATTRIBUTES_TAB, "Deployments", currentForm.asElement());
        registerAttachable(currentForm);

        
        // ============================================
        // global modules tab

        Options<ModelNode> options = new ModelNodeTable.Builder<>(metadata)

                .column(NAME, "Name", (cell, type, row, meta) -> row.get(NAME).asString())
                .column("slot", "Slot", (cell, type, row, meta) -> retrieveValue(descriptionEESbsystem, row, "slot"))
                .column("annotations", "Annotations", (cell, type, row, meta) -> retrieveValue(descriptionEESbsystem, row, "annotations"))
                .column("services", "Services", (cell, type, row, meta) -> retrieveValue(descriptionEESbsystem, row, "services"))
                .column("meta-inf", "Meta-inf", (cell, type, row, meta) -> retrieveValue(descriptionEESbsystem, row, "meta-inf"))
                .button(resources.constants().add(), (event, api) -> presenter.launchAddDialogGlobalModule())
                .button(resources.constants().remove(), (event, api) -> {
                    presenter.removeGlobalModule(api);
                })
                .build();
        globalModulesTable = new ModelNodeTable<>(Ids.EE_GLOBAL_MODULES_TABLE, options);
        tabs.add(EE_GLOBAL_MODULES_TAB, resources.constants().globalModules(), globalModulesTable.asElement()); //NON-NLS

        registerAttachable(globalModulesTable);
        
        // ============================================
        // service=default-bindings tab
        ResourceDescription defaulBindingsResource = descriptions.lookup(AddressTemplates.SERVICE_DEFAULT_BINDINGS_TEMPLATE);
        SecurityContext securityContextDefBindings = securityFramework.lookup(AddressTemplates.SERVICE_DEFAULT_BINDINGS_TEMPLATE);
        Metadata defaultBindingsMetadata = new Metadata(securityContextDefBindings, defaulBindingsResource, capabilities);
        saveCallback = (form, changedValues) -> presenter.saveDefaultBindings(changedValues);

        ModelNodeForm<ModelNode> defaultBindingsForm = new ModelNodeForm.Builder<>(EE_DEFAULT_BINDINGS_FORM, defaultBindingsMetadata)
                .include("context-service", 
                         "datasource", 
                         "jms-connection-factory", 
                         "managed-executor-service", 
                         "managed-scheduled-executor-service", 
                         "managed-thread-factory")
                .onSave(saveCallback)
                .build();
        registerAttachable(defaultBindingsForm);
        forms.put(EE_DEFAULT_BINDINGS_FORM, defaultBindingsForm);
        tabs.add(EE_DEFAULT_BINDINGS_TAB, resources.constants().defaultBindings(), defaultBindingsForm.asElement()); //NON-NLS

        // @formatter:off
        Element tabElement = tabs.asElement();
        LayoutBuilder layoutBuilder = new LayoutBuilder()
            .row()
                .column()
                    .header(Names.EE).rememberAs(HEADER_ELEMENT).end()
                    .add(info)
                    .add(tabElement)
                .end()
            .end();
        // @formatter:on
        
        Element root = layoutBuilder.build();
        header = layoutBuilder.referenceFor(HEADER_ELEMENT);
        initElement(root);
    }

    @Override
    public void setPresenter(final EEPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void attach() {
        super.attach();
    }

    private String retrieveValue(ResourceDescription descriptionEESbsystem, ModelNode node, String attrName) {
        String val = "";
        if (node.hasDefined(attrName)) 
            val = node.get(attrName).asString();
        else {
            val = descriptionEESbsystem.get(ATTRIBUTES)
                    .get(GLOBAL_MODULES)
                    .get(VALUE_TYPE)
                    .get(attrName)
                    .get(DEFAULT).asString();
        }
        return val;
    }

    @Override
    public void update(final ModelNode eeData) {
        // update the attributes - deployments tab
        Form<ModelNode> formDeployments = forms.get(EE_ATTRIBUTES_FORM);
        formDeployments.view(eeData);
        
        // update the global modules tab
        List<ModelNode> llp = eeData.get(GLOBAL_MODULES).asList();
        globalModulesTable.api().clear().add(llp).refresh(RESET);
        
        // update the default-bindings tab
        if (eeData.hasDefined(SERVICE)) {
            ModelNode defaultBindings = eeData.get(SERVICE).get(DEFAULT_BINDINGS);
            Form<ModelNode> formDefaulBindings = forms.get(EE_DEFAULT_BINDINGS_FORM);
            formDefaulBindings.view(defaultBindings);
        }
    }
}
