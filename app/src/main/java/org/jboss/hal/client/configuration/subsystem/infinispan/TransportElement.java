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

import java.util.List;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NONE;

/**
 * Element to view and modify the {@code transport=jgroups} singleton of a cache container. Kind of a fail safe form
 * with the difference that we need to take care of {@code transport=none}.
 *
 * @author Harald Pehl
 */
class TransportElement implements IsElement, Attachable, HasPresenter<CacheContainerPresenter> {

    private final EmptyState emptyState;
    private final Form<ModelNode> form;
    private final Element root;
    private CacheContainerPresenter presenter;

    TransportElement(final MetadataRegistry metadataRegistry, final Resources resources) {

        emptyState = new EmptyState.Builder(resources.constants().noTransport())
                .description(resources.messages().noTransport())
                .primaryAction(resources.constants().add(), () -> presenter.addJgroups())
                .build();

        Metadata metadata = metadataRegistry.lookup(AddressTemplates.TRANSPORT_JGROUPS_TEMPLATE);
        form = new ModelNodeForm.Builder<>(Ids.CACHE_CONTAINER_TRANSPORT_FORM,
                metadata)
                .onSave((f, changedValues) -> presenter.saveJgroups(changedValues))
                .build();

        // @formatter:off
        root = new Elements.Builder()
            .div()
                .h(1).textContent(Names.JGROUPS).end()
                .p().textContent(metadata.getDescription().getDescription()).end()
                .add(emptyState)
                .add(form)
            .end()
        .build();
        // @formatter:on

        Elements.setVisible(emptyState.asElement(), false);
        Elements.setVisible(form.asElement(), false);
    }

    @Override
    public Element asElement() {
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
    public void setPresenter(final CacheContainerPresenter presenter) {
        this.presenter = presenter;
    }

    void update(final List<Property> transports) {
        if (transports.isEmpty() || NONE.equals(transports.get(0).getName())) {
            emptyStateMode();
        } else {
            formMode();
            form.view(transports.get(0).getValue());
        }
    }

    private void emptyStateMode() {
        Elements.setVisible(emptyState.asElement(), true);
        Elements.setVisible(form.asElement(), false);
    }

    private void formMode() {
        Elements.setVisible(emptyState.asElement(), false);
        Elements.setVisible(form.asElement(), true);
    }
}
