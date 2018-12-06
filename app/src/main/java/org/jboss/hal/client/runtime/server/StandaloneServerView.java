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
package org.jboss.hal.client.runtime.server;

import javax.inject.Inject;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Button;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.runtime.server.StandaloneServerPresenter.HTTP_INTERFACE_TEMPLATE;
import static org.jboss.hal.client.runtime.server.StandaloneServerPresenter.ROOT_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.resources.CSS.pullRight;
import static org.jboss.hal.resources.Ids.*;

public class StandaloneServerView extends HalViewImpl implements StandaloneServerPresenter.MyView {

    private Form<ModelNode> attributesForm;
    private Form<ModelNode> httpInterfaceForm;
    private HTMLButtonElement enableSslButton;
    private HTMLButtonElement disableSslButton;
    private StandaloneServerPresenter presenter;

    @Inject
    StandaloneServerView(MetadataRegistry metadataRegistry, Resources resources) {
        VerticalNavigation navigation = new VerticalNavigation();
        registerAttachable(navigation);

        Metadata metadata = metadataRegistry.lookup(ROOT_TEMPLATE);
        String attributesFormId = Ids.build(Ids.STANDALONE_SERVER_COLUMN, ATTRIBUTES, FORM);
        attributesForm = new ModelNodeForm.Builder<>(attributesFormId, metadata)
                .includeRuntime()
                .onSave((form, changedValues) -> presenter.save(resources.constants().standaloneServer(), ROOT_TEMPLATE,
                        changedValues))
                .prepareReset(form -> presenter.reset(resources.constants().standaloneServer(), ROOT_TEMPLATE, form,
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
                .add(h(1).textContent(resources.constants().attributes()).get())
                .add(p().textContent(metadata.getDescription().getDescription()).get())
                .add(attributesForm)
                .get();
        String attributesItemId = Ids.build(ATTRIBUTES, ITEM);
        navigation.addPrimary(attributesItemId, resources.constants().configuration(), pfIcon("settings"),
                attributesElement);

        String httpTitle = resources.constants().httpManagementInterface();
        Metadata httpMetadata = metadataRegistry.lookup(HTTP_INTERFACE_TEMPLATE);
        String httpId = Ids.build(HTTP_INTERFACE, FORM);
        httpInterfaceForm = new ModelNodeForm.Builder<>(httpId, httpMetadata)
                .onSave((form, changedValues) -> presenter.save(httpTitle, HTTP_INTERFACE_TEMPLATE, changedValues))
                .prepareReset(form -> presenter.reset(httpTitle, HTTP_INTERFACE_TEMPLATE, form, httpMetadata))
                .unsorted()
                .build();

        enableSslButton = button().id(ENABLE_SSL)
                .textContent(resources.constants().enableSSL())
                .css(Button.DEFAULT_CSS, pullRight)
                .get();
        bind(enableSslButton, click, ev -> presenter.launchEnableSSLWizard());

        disableSslButton = button().id(DISABLE_SSL)
                .textContent(resources.constants().disableSSL())
                .css(Button.DEFAULT_CSS, pullRight)
                .get();
        bind(disableSslButton, click, ev -> presenter.disableSSLWizard());

        HTMLElement httpMgmtItemElement = section()
                .add(div()
                        .add(h(1).textContent(httpTitle).get())
                        .add(p().textContent(httpMetadata.getDescription().getDescription()).get())
                        .add(enableSslButton)
                        .add(disableSslButton))
                .add(httpInterfaceForm)
                .get();
        registerAttachable(httpInterfaceForm);

        navigation.addPrimary(HTTP_INTERFACE_ITEM, resources.constants().httpManagementInterface(),
                pfIcon("virtual-machine"), httpMgmtItemElement);

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
    }

    @Override
    public void updateAttributes(ModelNode attributes) {
        attributesForm.view(attributes);
    }

    @Override
    public void updateHttpInterface(ModelNode model) {
        httpInterfaceForm.view(model);
        boolean isSslEnabled = model.hasDefined(SSL_CONTEXT) && model.get(SSL_CONTEXT).asString() != null;
        toggleSslButton(isSslEnabled);
    }

    private void toggleSslButton(boolean enable) {
        Elements.setVisible(enableSslButton, !enable);
        Elements.setVisible(disableSslButton, enable);
    }

    @Override
    public void setPresenter(StandaloneServerPresenter presenter) {
        this.presenter = presenter;
    }

}
