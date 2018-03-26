/*
 * Copyright 2015-2018 Red Hat, Inc, and individual contributors.
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
package org.jboss.hal.client.management;

import javax.inject.Inject;

import elemental2.dom.HTMLButtonElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Button;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.button;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.management.ManagementInterfacePresenter.HTTP_INTERFACE_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HTTP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SSL_CONTEXT;
import static org.jboss.hal.resources.CSS.pullRight;
import static org.jboss.hal.resources.Ids.DISABLE_SSL;
import static org.jboss.hal.resources.Ids.ENABLE_SSL;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.MANAGEMENT;

public class ManagementInterfaceView extends HalViewImpl implements ManagementInterfacePresenter.MyView {

    private ManagementInterfacePresenter presenter;
    private Form<ModelNode> form;
    private HTMLButtonElement enableSslButton;
    private HTMLButtonElement disableSslButton;

    @Inject
    ManagementInterfaceView(final MetadataRegistry metadataRegistry, final Resources resources) {

        enableSslButton = button().id(ENABLE_SSL)
                .textContent(resources.constants().enableSSL())
                .css(Button.DEFAULT_CSS, pullRight)
                .asElement();
        bind(enableSslButton, click, ev -> presenter.launchEnableSSLWizard());

        disableSslButton = button().id(DISABLE_SSL)
                .textContent(resources.constants().disableSSL())
                .css(Button.DEFAULT_CSS, pullRight)
                .asElement();
        bind(disableSslButton, click, ev -> presenter.disableSSLWizard());

        Metadata metadata = metadataRegistry.lookup(HTTP_INTERFACE_TEMPLATE);

        form = new ModelNodeForm.Builder<>(Ids.build(HTTP, MANAGEMENT, FORM), metadata)
                .onSave((form, changedValues) -> presenter.save(changedValues))
                .prepareReset(form -> presenter.reset(form, metadata))
                .build();

        registerAttachable(form);

        initElement(row()
                .add(column()
                        .add(h(1).textContent(resources.constants().httpInterfaceManagement()).asElement())
                        .add(p().textContent(metadata.getDescription().getDescription()).asElement())
                        .add(enableSslButton)
                        .add(disableSslButton)
                        .add(form)
                        .asElement()));
    }

    @Override
    public void update(ModelNode model) {
        form.view(model);
        boolean isSslEnabled = model.hasDefined(SSL_CONTEXT) && model.get(SSL_CONTEXT).asString() != null;
        toggleSslButton(isSslEnabled);
    }

    private void toggleSslButton(boolean enable) {
        Elements.setVisible(enableSslButton, !enable);
        Elements.setVisible(disableSslButton, enable);
    }

    @Override
    public void setPresenter(ManagementInterfacePresenter presenter) {
        this.presenter = presenter;
    }
}