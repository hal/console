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
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.FailSafeForm;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;

/**
 * Element to manage the {@linkplain ThreadPool thread pool} singletons of a cache container. The element contains a
 * fail safe form to update the thread pool resource.
 *
 * @author Harald Pehl
 */
class ThreadPoolElement implements IsElement, Attachable, HasPresenter<CacheContainerPresenter> {

    private final FailSafeForm<ModelNode> fsf;
    private final Element root;
    private CacheContainerPresenter presenter;

    ThreadPoolElement(ThreadPool threadPool, Dispatcher dispatcher, MetadataRegistry metadataRegistry) {
        Metadata metadata = metadataRegistry.lookup(threadPool.template());
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(threadPool.baseId, Ids.FORM_SUFFIX), metadata)
                .onSave((f, changedValues) -> presenter.saveThreadPool(threadPool, changedValues))
                .build();
        fsf = new FailSafeForm<>(dispatcher, () -> presenter.readThreadPool(threadPool), form,
                () -> presenter.addThreadPool(threadPool));

        // @formatter:off
        root = new Elements.Builder()
            .section()
                .h(1).textContent(threadPool.type).end()
                .p().textContent(metadata.getDescription().getDescription()).end()
                .add(fsf)
            .end()
        .build();
        // @formatter:on
    }

    @Override
    public Element asElement() {
        return root;
    }

    @Override
    public void attach() {
        fsf.attach();
    }

    @Override
    public void detach() {
        fsf.detach();
    }

    @Override
    public void setPresenter(final CacheContainerPresenter presenter) {
        this.presenter = presenter;
    }

    void update(ModelNode modelNode) {
        fsf.view(modelNode);
    }
}
