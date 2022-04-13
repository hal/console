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
package org.jboss.hal.client.configuration.subsystem.infinispan;

import org.jboss.elemento.Elements;
import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.button;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.REMOTE_CACHE_CONTAINER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.infinispan.NearCache.INVALIDATION;
import static org.jboss.hal.client.configuration.subsystem.infinispan.NearCache.NONE;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.CSS.btn;
import static org.jboss.hal.resources.CSS.btnDefault;
import static org.jboss.hal.resources.CSS.marginTopLarge;

class NearCacheElement implements IsElement<HTMLElement>, Attachable, HasPresenter<RemoteCacheContainerPresenter> {

    private final HTMLElement undefinedElement;
    private final HTMLElement noneElement;
    private final Form<ModelNode> invalidationForm;
    private final HTMLElement invalidationElement;
    private final HTMLElement root;
    private RemoteCacheContainerPresenter presenter;

    NearCacheElement(MetadataRegistry metadataRegistry, Resources resources) {
        undefinedElement = div()
                .add(p().css(marginTopLarge)
                        .innerHtml(resources.messages().nearCacheUndefined()))
                .element();

        noneElement = div()
                .add(p().css(marginTopLarge)
                        .innerHtml(resources.messages().nearCacheNone()))
                .add(button(resources.constants().switchNearCache())
                        .css(btn, btnDefault)
                        .on(click, event -> presenter.switchNearCache(INVALIDATION)))
                .element();

        String id = Ids.build(INVALIDATION.baseId, Ids.FORM);
        Metadata metadata = metadataRegistry.lookup(REMOTE_CACHE_CONTAINER_TEMPLATE.append("near-cache=invalidation"));
        invalidationForm = new ModelNodeForm.Builder<>(id, metadata)
                .onSave((form, changedValues) -> presenter.saveInvalidationNearCache(changedValues))
                .prepareReset(form -> presenter.resetInvalidationNearCache(form))
                .build();

        invalidationElement = div()
                .add(p().css(marginTopLarge)
                        .innerHtml(resources.messages().nearCacheInvalidation()))
                .add(button(resources.constants().switchNearCache())
                        .css(btn, btnDefault)
                        .on(click, event -> presenter.switchNearCache(NONE)))
                .add(invalidationForm).element();

        root = section()
                .add(undefinedElement)
                .add(noneElement)
                .add(invalidationElement).element();

        Elements.setVisible(undefinedElement, false);
        Elements.setVisible(noneElement, false);
        Elements.setVisible(invalidationElement, false);
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void attach() {
        invalidationForm.attach();
    }

    @Override
    public void detach() {
        invalidationForm.detach();
    }

    @Override
    public void setPresenter(RemoteCacheContainerPresenter presenter) {
        this.presenter = presenter;
    }

    void update(CacheContainer cacheContainer) {
        ModelNode noneNode = failSafeGet(cacheContainer, NONE.path());
        ModelNode invalidationNode = failSafeGet(cacheContainer, INVALIDATION.path());

        if (noneNode.isDefined()) {
            switchNearCache(NONE);
        } else if (invalidationNode.isDefined()) {
            invalidationForm.view(invalidationNode);
            switchNearCache(INVALIDATION);
        } else {
            switchNearCache(null);
        }
    }

    private void switchNearCache(NearCache nearCache) {
        Elements.setVisible(undefinedElement, nearCache == null);
        Elements.setVisible(noneElement, nearCache == NONE);
        Elements.setVisible(invalidationElement, nearCache == INVALIDATION);
    }
}
