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
package org.jboss.hal.client.runtime.subsystem.elytron;

import java.util.List;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.finder.StaticItemColumn;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

@Column(Ids.ELYTRON_RUNTIME)
public class ElytronColumn extends FinderColumn<StaticItem> {

    @Inject
    public ElytronColumn(Finder finder, Resources resources, ItemActionFactory itemActionFactory, Places places) {

        super(new Builder<StaticItem>(finder, Ids.ELYTRON_RUNTIME, Names.SECURITY)

                .itemRenderer(StaticItemColumn.StaticItemDisplay::new)
                .onPreview(StaticItem::getPreviewContent)
                .useFirstActionAsBreadcrumbHandler());

        Supplier<List<StaticItem>> itemsSupplier = () -> asList(
                new StaticItem.Builder(Names.SECURITY_REALMS)
                        .onPreview(new PreviewContent<>(Names.SECURITY_REALMS, resources.previews().runtimeElytronSecurityRealms()))
                        .action(itemActionFactory.viewAndMonitor(Ids.ELYTRON_SECURITY_REALMS,
                                places.selectedServer(NameTokens.ELYTRON_RUNTIME_SECURITY_REALMS).build()))
                        .build(),
                new StaticItem.Builder(Names.STORES)
                        .onPreview(new PreviewContent<>(Names.STORES, resources.previews().runtimeElytronStores()))
                        .action(itemActionFactory.viewAndMonitor(Ids.ELYTRON_STORES,
                                places.selectedServer(NameTokens.ELYTRON_RUNTIME_STORES).build()))
                        .build(),
                new StaticItem.Builder(Names.SSL)
                        .onPreview(new PreviewContent<>(Names.SSL, resources.previews().runtimeElytronSSL()))
                        .action(itemActionFactory.viewAndMonitor(Ids.ELYTRON_SSL,
                                places.selectedServer(NameTokens.ELYTRON_RUNTIME_SSL).build()))
                        .build()

        );
        setItemsProvider((context, callback) -> callback.onSuccess(itemsSupplier.get()));
        setBreadcrumbItemsProvider(
                (context, callback) -> callback.onSuccess(
                        itemsSupplier.get().stream().filter(item -> item.getNextColumn() == null).collect(toList())));

    }
}
