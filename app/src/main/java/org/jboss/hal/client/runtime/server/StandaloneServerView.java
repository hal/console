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
package org.jboss.hal.client.runtime.server;

import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.client.runtime.managementinterface.ConstantHeadersElement;
import org.jboss.hal.client.runtime.managementinterface.HttpManagementInterfaceElement;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.runtime.server.StandaloneServerPresenter.HTTP_INTERFACE_TEMPLATE;
import static org.jboss.hal.client.runtime.server.StandaloneServerPresenter.ROOT_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONSTANT_HEADERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAMESPACES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SCHEMA_LOCATIONS;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeList;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.resources.Ids.CONSTANT_HEADERS_ITEM;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.HTTP_INTERFACE_ITEM;
import static org.jboss.hal.resources.Ids.ITEM;

public class StandaloneServerView extends HalViewImpl implements StandaloneServerPresenter.MyView {

    private Form<ModelNode> attributesForm;
    private HttpManagementInterfaceElement httpManagementInterfaceElement;
    private ConstantHeadersElement constantHeadersElement;
    private StandaloneServerPresenter presenter;

    @Inject
    StandaloneServerView(MetadataRegistry metadataRegistry, Resources resources) {
        VerticalNavigation navigation = new VerticalNavigation();
        registerAttachable(navigation);

        Metadata metadata = metadataRegistry.lookup(ROOT_TEMPLATE);
        String attributesFormId = Ids.build(Ids.STANDALONE_SERVER_COLUMN, Ids.ATTRIBUTES, FORM);
        attributesForm = new ModelNodeForm.Builder<>(attributesFormId, metadata)
                .includeRuntime()
                .onSave((form, changedValues) -> presenter.save(Names.STANDALONE_SERVER, ROOT_TEMPLATE,
                        changedValues))
                .prepareReset(form -> presenter.reset(Names.STANDALONE_SERVER, ROOT_TEMPLATE, form,
                        metadata))
                .unsorted()
                .build();
        // makes no sense to mask name attribute
        attributesForm.getFormItem(NAME).unmask();
        // the following attributes are read-only, set as non required to let user save the form
        attributesForm.getFormItem(NAMESPACES).setRequired(false);
        attributesForm.getFormItem(SCHEMA_LOCATIONS).setRequired(false);
        registerAttachable(attributesForm);

        HTMLElement attributesElement = section()
                .add(h(1).textContent(resources.constants().attributes()).element())
                .add(p().textContent(metadata.getDescription().getDescription()).element())
                .add(attributesForm).element();
        String attributesItemId = Ids.build(Ids.ATTRIBUTES, ITEM);
        navigation.addPrimary(attributesItemId, resources.constants().configuration(), pfIcon("settings"),
                attributesElement);

        httpManagementInterfaceElement = new HttpManagementInterfaceElement(metadataRegistry, HTTP_INTERFACE_TEMPLATE,
                resources);
        registerAttachable(httpManagementInterfaceElement);
        navigation.addPrimary(HTTP_INTERFACE_ITEM, resources.constants().httpManagementInterface(),
                pfIcon("virtual-machine"), httpManagementInterfaceElement);

        constantHeadersElement = new ConstantHeadersElement(metadataRegistry, HTTP_INTERFACE_TEMPLATE, resources);
        registerAttachable(constantHeadersElement);
        navigation.addPrimary(CONSTANT_HEADERS_ITEM, new LabelBuilder().label(CONSTANT_HEADERS), fontAwesome("bars"),
                constantHeadersElement);

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
    }

    @Override
    public void updateAttributes(ModelNode attributes) {
        attributesForm.view(attributes);
    }

    @Override
    public void updateHttpInterface(ModelNode model, int pathIndex) {
        httpManagementInterfaceElement.update(model);
        List<ModelNode> constantHeaders = failSafeList(model, CONSTANT_HEADERS);
        constantHeadersElement.update(constantHeaders);
        if (pathIndex >= 0 && pathIndex < constantHeaders.size()) {
            constantHeadersElement.showHeaders(constantHeaders.get(pathIndex));
        }
    }

    @Override
    public void setPresenter(StandaloneServerPresenter presenter) {
        this.presenter = presenter;
        httpManagementInterfaceElement.setPresenter(presenter);
        constantHeadersElement.setPresenter(presenter);
    }
}
