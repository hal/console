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
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.button;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.gwt.elemento.core.EventType.click;
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
                .get();

        noneElement = div()
                .add(p().css(marginTopLarge)
                        .innerHtml(resources.messages().nearCacheNone()))
                .add(button(resources.constants().switchNearCache())
                        .css(btn, btnDefault)
                        .on(click, event -> presenter.switchNearCache(INVALIDATION)))
                .get();

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
                .add(invalidationForm)
                .get();

        root = section()
                .add(undefinedElement)
                .add(noneElement)
                .add(invalidationElement)
                .get();

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
