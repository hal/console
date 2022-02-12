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
package org.jboss.hal.client.runtime.managementinterface;

import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.Button;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;

import static org.jboss.gwt.elemento.core.Elements.button;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HTTP_INTERFACE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SSL_CONTEXT;
import static org.jboss.hal.resources.CSS.pullRight;
import static org.jboss.hal.resources.Ids.DISABLE_SSL;
import static org.jboss.hal.resources.Ids.ENABLE_SSL;
import static org.jboss.hal.resources.Ids.FORM;

public class HttpManagementInterfaceElement
        implements HasPresenter<HttpManagementInterfacePresenter>, Attachable, IsElement<HTMLElement> {

    private final Form<ModelNode> form;
    private final HTMLButtonElement enableSslButton;
    private final HTMLButtonElement disableSslButton;
    private final HTMLElement root;
    private HttpManagementInterfacePresenter presenter;

    public HttpManagementInterfaceElement(MetadataRegistry metadataRegistry, AddressTemplate template,
            Resources resources) {
        Metadata metadata = metadataRegistry.lookup(template);
        form = new ModelNodeForm.Builder<>(Ids.build(HTTP_INTERFACE, FORM), metadata)
                .onSave((form, changedValues) -> presenter.saveManagementInterface(template, changedValues))
                .prepareReset(form -> presenter.resetManagementInterface(template, form, metadata))
                .unsorted()
                .build();

        enableSslButton = button().id(ENABLE_SSL)
                .textContent(resources.constants().enableSSL())
                .css(Button.DEFAULT_CSS, pullRight)
                .element();
        bind(enableSslButton, click, ev -> presenter.enableSslForManagementInterface());

        disableSslButton = button().id(DISABLE_SSL)
                .textContent(resources.constants().disableSSL())
                .css(Button.DEFAULT_CSS, pullRight)
                .element();
        bind(disableSslButton, click, ev -> presenter.disableSslForManagementInterface());

        root = section()
                .add(div()
                        .add(h(1).textContent(resources.constants().httpManagementInterface()).element())
                        .add(p().textContent(metadata.getDescription().getDescription()).element())
                        .add(enableSslButton)
                        .add(disableSslButton))
                .add(form)
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void attach() {
        form.attach();
    }

    @Override
    public void detach() {
        form.detach();
    }

    @Override
    public void setPresenter(HttpManagementInterfacePresenter presenter) {
        this.presenter = presenter;
    }

    public void update(ModelNode modelNode) {
        form.view(modelNode);
        boolean isSslEnabled = modelNode.hasDefined(SSL_CONTEXT) && modelNode.get(SSL_CONTEXT).asString() != null;
        toggleSslButton(isSslEnabled);
    }

    private void toggleSslButton(boolean enable) {
        Elements.setVisible(enableSslButton, !enable);
        Elements.setVisible(disableSslButton, enable);
    }
}
