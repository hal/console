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

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;

/**
 * Element to manage the {@linkplain ThreadPool thread pool} singletons of a cache container. The element contains a
 * fail safe form to update the thread pool resource.
 *
 * @author Harald Pehl
 */
class ThreadPoolElement implements IsElement<HTMLElement>, Attachable, HasPresenter<CacheContainerPresenter> {

    private final Form<ModelNode> form;
    private final HTMLElement root;
    private CacheContainerPresenter presenter;

    ThreadPoolElement(ThreadPool threadPool, MetadataRegistry metadataRegistry) {
        Metadata metadata = metadataRegistry.lookup(threadPool.template());
        form = new ModelNodeForm.Builder<>(Ids.build(threadPool.baseId, Ids.FORM_SUFFIX), metadata)
                .singleton(() -> presenter.readThreadPool(threadPool), () -> presenter.addThreadPool(threadPool))
                .onSave((f, changedValues) -> presenter.saveThreadPool(threadPool, changedValues))
                .prepareReset(f -> presenter.resetThreadPool(threadPool, f))
                .prepareRemove(f -> presenter.removeThreadPool(threadPool, f))
                .build();

        root = section()
                .add(h(1).textContent(threadPool.type))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(form)
                .asElement();
    }

    @Override
    public HTMLElement asElement() {
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

    void update(ModelNode modelNode) {
        form.view(modelNode);
    }
}
