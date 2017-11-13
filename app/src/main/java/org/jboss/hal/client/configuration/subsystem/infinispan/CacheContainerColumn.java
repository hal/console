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

import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.CACHE_CONTAINER_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.CACHE_CONTAINER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.INFINISPAN_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CACHE_CONTAINER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT_CACHE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

@AsyncColumn(Ids.CACHE_CONTAINER)
@Requires(value = {CACHE_CONTAINER_ADDRESS}, recursive = false)
public class CacheContainerColumn extends FinderColumn<CacheContainer> {

    @Inject
    public CacheContainerColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory,
            final CrudOperations crud,
            final Places places) {

        super(new Builder<CacheContainer>(finder, Ids.CACHE_CONTAINER, Names.CACHE_CONTAINER)

                .columnAction(columnActionFactory.add(Ids.CACHE_CONTAINER_ADD, Names.CACHE_CONTAINER,
                        CACHE_CONTAINER_TEMPLATE, name -> {
                            //noinspection Convert2MethodRef
                            return Ids.cacheContainer(name);
                        }))
                .columnAction(columnActionFactory.refresh(Ids.CACHE_CONTAINER_REFRESH))

                .itemsProvider((context, callback) ->
                        crud.readChildren(INFINISPAN_SUBSYSTEM_TEMPLATE, CACHE_CONTAINER, children ->
                                callback.onSuccess(children.stream().map(CacheContainer::new).collect(toList()))))

                .onPreview(CacheContainerPreview::new)
                .useFirstActionAsBreadcrumbHandler()
                .withFilter()
                .showCount()
        );

        setItemRenderer(item -> new ItemDisplay<CacheContainer>() {
            @Override
            public String getId() {
                return Ids.cacheContainer(item.getName());
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public HTMLElement asElement() {
                return item.hasDefined(DEFAULT_CACHE) ? ItemDisplay.withSubtitle(item.getName(),
                        item.get(DEFAULT_CACHE).asString()) : null;
            }

            @Override
            public List<ItemAction<CacheContainer>> actions() {
                return asList(
                        itemActionFactory.viewAndMonitor(Ids.cacheContainer(item.getName()),
                                places.selectedProfile(NameTokens.CACHE_CONTAINER).with(NAME, item.getName()).build()),
                        itemActionFactory.remove(Names.CACHE_CONTAINER, item.getName(),
                                CACHE_CONTAINER_TEMPLATE, CacheContainerColumn.this)
                );
            }
        });
    }
}
