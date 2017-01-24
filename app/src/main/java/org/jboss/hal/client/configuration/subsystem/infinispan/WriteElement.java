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
package org.jboss.hal.client.configuration.subsystem.infinispan;

import elemental.dom.Element;
import elemental.html.InputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.InputType;
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

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.client.configuration.subsystem.infinispan.Write.BEHIND;
import static org.jboss.hal.client.configuration.subsystem.infinispan.Write.THROUGH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STORE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.CSS.btn;
import static org.jboss.hal.resources.CSS.btnDefault;
import static org.jboss.hal.resources.CSS.marginTopLarge;

/**
 * @author Harald Pehl
 */
class WriteElement implements IsElement, Attachable, HasPresenter<CacheContainerPresenter> {

    private static final String BEHIND_RADIO = "behindRadio";

    private final EmptyState emptyState;
    private final Element throughElement;
    private final Element behindElement;
    private final Form<ModelNode> behindForm;
    private final Element root;
    private CacheContainerPresenter presenter;

    WriteElement(final Cache cache, final Store store, final MetadataRegistry metadataRegistry,
            final Resources resources) {

        String radioName = Ids.build(cache.baseId, store.baseId, WRITE, "radio");
        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .div().css(CSS.radio)
                .label()
                    .input(InputType.radio).rememberAs(BEHIND_RADIO)
                        .attr(UIConstants.NAME, radioName)
                        .attr(UIConstants.VALUE, BEHIND.resource)
                    .span().textContent(BEHIND.type).end()
                .end()
            .end()
            .div().css(CSS.radio)
                .label()
                    .input(InputType.radio)
                        .attr(UIConstants.NAME, radioName)
                        .attr(UIConstants.VALUE, THROUGH.resource)
                        .attr(UIConstants.CHECKED, UIConstants.TRUE)
                    .span().textContent(THROUGH.type).end()
                .end()
            .end();
        // @formatter:on
        InputElement behindRadio = builder.referenceFor(BEHIND_RADIO);

        emptyState = new EmptyState.Builder(resources.constants().noWrite())
                .description(resources.messages().noWrite())
                .addAll(builder.elements())
                .primaryAction(resources.constants().add(), () -> {
                    Write write = behindRadio.isChecked() ? BEHIND : THROUGH;
                    presenter.addWrite(write);
                })
                .build();

        // @formatter:off
        throughElement = new Elements.Builder()
            .div()
                .p().css(marginTopLarge)
                    .innerHtml(resources.messages().writeBehaviour(THROUGH.type, BEHIND.type))
                .end()
                .button().css(btn, btnDefault).on(click, event -> presenter.switchWrite(THROUGH, BEHIND))
                    .textContent(resources.constants().switchBehaviour())
                .end()
            .end()
        .build();
        // @formatter:on

        String id = Ids.build(cache.baseId, store.baseId, BEHIND.baseId, Ids.FORM_SUFFIX);
        Metadata metadata = metadataRegistry.lookup(cache.template
                .append(STORE + "=" + store.resource)
                .append(WRITE + "=" + BEHIND.resource));
        behindForm = new ModelNodeForm.Builder<>(id, metadata)
                .onSave((f, changedValues) -> presenter.saveWrite(BEHIND, changedValues))
                .build();

        // @formatter:off
        behindElement = new Elements.Builder()
            .div()
                .p().css(marginTopLarge)
                    .innerHtml(resources.messages().writeBehaviour(BEHIND.type, THROUGH.type))
                .end()
                .button().css(btn, btnDefault).on(click, event -> presenter.switchWrite(BEHIND, THROUGH))
                    .textContent(resources.constants().switchBehaviour())
                .end()
                .add(behindForm.asElement())
            .end()
        .build();
        // @formatter:on

        root = new Elements.Builder()
                .section()
                .add(emptyState)
                .add(throughElement)
                .add(behindElement)
                .end()
                .build();

        Elements.setVisible(emptyState.asElement(), false);
        Elements.setVisible(throughElement, false);
        Elements.setVisible(behindElement, false);
    }

    @Override
    public Element asElement() {
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
    public void setPresenter(final CacheContainerPresenter presenter) {
        this.presenter = presenter;
    }

    void update(final ModelNode storeNode) {
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
        Elements.setVisible(emptyState.asElement(), write == null);
        Elements.setVisible(throughElement, write == THROUGH);
        Elements.setVisible(behindElement, write == BEHIND);
    }
}
