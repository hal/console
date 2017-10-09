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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.CACHE_CONTAINER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TRANSPORT;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;

/**
 * Implementation note: Not based on MBUI XML due to special cache container singleton resources.
 *
 * TODO This view generates a huge DOM tree (about 14k lines of HTML). Think about ways to optimize this.
 */
public class CacheContainerView extends HalViewImpl
        implements CacheContainerPresenter.MyView {

    private final Form<ModelNode> configurationForm;
    private final Map<Cache, CacheElement> caches;
    private final Map<ThreadPool, ThreadPoolElement> threadPools;
    private final TransportElement transport;
    private final VerticalNavigation navigation;
    private CacheContainerPresenter presenter;

    @Inject
    public CacheContainerView(final MetadataRegistry metadataRegistry, final TableButtonFactory tableButtonFactory,
            final Resources resources) {
        Metadata metadata = metadataRegistry.lookup(CACHE_CONTAINER_TEMPLATE);
        configurationForm = new ModelNodeForm.Builder<>(Ids.CACHE_CONTAINER_FORM, metadata)
                .onSave((form, changedValues) -> presenter.saveCacheContainer(changedValues))
                .prepareReset(form -> presenter.resetCacheContainer(form))
                .build();

        caches = new HashMap<>();
        for (Cache cache : Cache.values()) {
            caches.put(cache, new CacheElement(cache, metadataRegistry, tableButtonFactory, resources));
        }

        threadPools = new HashMap<>();
        for (ThreadPool threadPool : ThreadPool.values()) {
            threadPools.put(threadPool, new ThreadPoolElement(threadPool, metadataRegistry));
        }

        transport = new TransportElement(metadataRegistry, resources);

        navigation = new VerticalNavigation();
        HTMLElement section = section()
                .add(h(1).textContent(Names.CONFIGURATION))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(configurationForm)
                .asElement();
        navigation.addPrimary(Ids.CACHE_CONTAINER_ENTRY, Names.CONFIGURATION, pfIcon("settings"), section);

        caches.forEach((cache, cacheElement) ->
                navigation.addPrimary(Ids.build(cache.baseId, Ids.ENTRY_SUFFIX), cache.type, cache.icon,
                        cacheElement.asElement()));

        navigation.addPrimary(Ids.CACHE_CONTAINER_THREAD_POOLS_ENTRY, Names.THREAD_POOLS, pfIcon("resource-pool"));
        threadPools.forEach((threadPool, threadPoolElement) ->
                navigation.addSecondary(Ids.CACHE_CONTAINER_THREAD_POOLS_ENTRY,
                        Ids.build(threadPool.baseId, Ids.ENTRY_SUFFIX), threadPool.type,
                        threadPoolElement.asElement()));

        navigation.addPrimary(Ids.CACHE_CONTAINER_TRANSPORT_ENTRY, Names.TRANSPORT, fontAwesome("road"),
                transport.asElement());

        registerAttachable(navigation);
        registerAttachable(configurationForm);
        caches.values().forEach(cacheElement -> registerAttachable(cacheElement));
        threadPools.values().forEach(threadPoolElement -> registerAttachable(threadPoolElement));
        registerAttachable(transport);

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
    }

    @Override
    public void setPresenter(final CacheContainerPresenter presenter) {
        this.presenter = presenter;
        caches.values().forEach(cacheElement -> cacheElement.setPresenter(presenter));
        threadPools.values().forEach(threadPoolElement -> threadPoolElement.setPresenter(presenter));
        transport.setPresenter(presenter);
    }

    @Override
    public void update(final CacheContainer cacheContainer, boolean jgroups) {
        configurationForm.view(cacheContainer);
        caches.forEach((cache, cacheElement) -> {
            List<NamedNode> caches = asNamedNodes(failSafePropertyList(cacheContainer, cache.resource()));
            cacheElement.update(caches);
        });
        threadPools.forEach((threadPool, threadPoolElement) -> {
            ModelNode modelNode = failSafeGet(cacheContainer, threadPool.path());
            threadPoolElement.update(modelNode);
        });

        navigation.setVisible(Ids.CACHE_CONTAINER_TRANSPORT_ENTRY, jgroups);
        if (jgroups) {
            List<Property> transports = failSafePropertyList(cacheContainer, TRANSPORT);
            transport.update(transports);
        }
    }

    @Override
    public void updateCacheBackups(final Cache cache, final List<NamedNode> backups) {
        caches.get(cache).updateBackups(backups);
    }

    @Override
    public void updateCacheStore(final Cache cache, final List<Property> stores) {
        caches.get(cache).updateStore(stores);
    }
}
