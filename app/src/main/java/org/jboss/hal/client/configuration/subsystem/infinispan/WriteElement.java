/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.infinispan;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.gwt.elemento.core.InputType.radio;
import static org.jboss.hal.client.configuration.subsystem.infinispan.Write.BEHIND;
import static org.jboss.hal.client.configuration.subsystem.infinispan.Write.THROUGH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STORE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.CSS.btn;
import static org.jboss.hal.resources.CSS.btnDefault;
import static org.jboss.hal.resources.CSS.marginTopLarge;

class WriteElement implements IsElement<HTMLElement>, Attachable, HasPresenter<CachePresenter<?, ?>> {

    private final EmptyState emptyState;
    private final HTMLElement throughElement;
    private final HTMLElement behindElement;
    private final Form<ModelNode> behindForm;
    private final HTMLElement root;
    private CachePresenter<?, ?> presenter;

    WriteElement(CacheType cacheType, Store store, MetadataRegistry metadataRegistry, Resources resources) {
        HTMLInputElement behindRadio;
        String radioName = Ids.build(cacheType.baseId, store.baseId, WRITE, "radio");
        Iterable<HTMLElement> elements = collect()
                .add(div().css(CSS.radio)
                        .add(label()
                                .add(behindRadio = input(radio)
                                        .attr(UIConstants.NAME, radioName)
                                        .attr(UIConstants.VALUE, BEHIND.resource)
                                        .get())
                                .add(span().textContent(BEHIND.type))))
                .add(div().css(CSS.radio)
                        .add(label()
                                .add(input(radio)
                                        .attr(UIConstants.NAME, radioName)
                                        .attr(UIConstants.VALUE, THROUGH.resource)
                                        .attr(UIConstants.CHECKED, UIConstants.TRUE))
                                .add(span().textContent(THROUGH.type))))
                .get();

        emptyState = new EmptyState.Builder(Ids.build(cacheType.baseId, store.baseId, WRITE, Ids.EMPTY),
                resources.constants().noWrite())
                .description(resources.messages().noWrite())
                .addAll(elements)
                .primaryAction(resources.constants().add(), () -> {
                    Write write = behindRadio.checked ? BEHIND : THROUGH;
                    presenter.addWrite(write);
                })
                .build();
        emptyState.element().classList.add(marginTopLarge);

        throughElement = div()
                .add(p().css(marginTopLarge)
                        .innerHtml(resources.messages().writeBehaviour(THROUGH.type, BEHIND.type)))
                .add(button(resources.constants().switchBehaviour())
                        .css(btn, btnDefault)
                        .on(click, event -> presenter.switchWrite(THROUGH, BEHIND)))
                .get();

        String id = Ids.build(cacheType.baseId, store.baseId, BEHIND.baseId, Ids.FORM);
        Metadata metadata = metadataRegistry.lookup(cacheType.template
                .append(STORE + "=" + store.resource)
                .append(WRITE + "=" + BEHIND.resource));
        behindForm = new ModelNodeForm.Builder<>(id, metadata)
                .onSave((f, changedValues) -> presenter.saveWrite(BEHIND, changedValues))
                .prepareReset(f -> presenter.resetWrite(BEHIND, f))
                .build();

        behindElement = div()
                .add(p().css(marginTopLarge)
                        .innerHtml(resources.messages().writeBehaviour(BEHIND.type, THROUGH.type)))
                .add(button(resources.constants().switchBehaviour())
                        .css(btn, btnDefault)
                        .on(click, event -> presenter.switchWrite(BEHIND, THROUGH)))
                .add(behindForm)
                .get();

        root = section()
                .add(emptyState)
                .add(throughElement)
                .add(behindElement)
                .get();

        Elements.setVisible(emptyState.element(), false);
        Elements.setVisible(throughElement, false);
        Elements.setVisible(behindElement, false);
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void attach() {
        behindForm.attach();
    }

    @Override
    public void detach() {
        behindForm.detach();
    }

    @Override
    public void setPresenter(CachePresenter<?, ?> presenter) {
        this.presenter = presenter;
    }

    void update(ModelNode storeNode) {
        ModelNode behindNode = failSafeGet(storeNode, BEHIND.path());
        ModelNode throughNode = failSafeGet(storeNode, THROUGH.path());

        if (behindNode.isDefined()) {
            behindForm.view(behindNode);
            switchBehaviour(BEHIND);
        } else if (throughNode.isDefined()) {
            switchBehaviour(THROUGH);
        } else {
            switchBehaviour(null);
        }
    }

    private void switchBehaviour(Write write) {
        Elements.setVisible(emptyState.element(), write == null);
        Elements.setVisible(throughElement, write == THROUGH);
        Elements.setVisible(behindElement, write == BEHIND);
    }
}
