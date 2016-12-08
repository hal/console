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
package org.jboss.hal.client.configuration.subsystem.undertow;

import javax.inject.Inject;

import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.finder.StaticItemColumn;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;

import static java.util.Arrays.asList;

/**
 * @author Harald Pehl
 */
@AsyncColumn(Ids.UNDERTOW_SETTINGS)
public class UndertowSettingsColumn
        extends FinderColumn<StaticItem> { // doesn't extend from StaticItemColumn because we need more flexibility

    @Inject
    public UndertowSettingsColumn(final Finder finder,
            final ItemActionFactory itemActionFactory,
            final CrudOperations crud,
            final Resources resources) {

        super(new Builder<StaticItem>(finder, Ids.UNDERTOW_SETTINGS, resources.constants().settings())

                .itemRenderer(StaticItemColumn.StaticItemDisplay::new)

                .initialItems(asList(
                        new StaticItem.Builder(resources.constants().globalSettings())
                                .id(Ids.UNDERTOW_GLOBAL_SETTINGS)
                                .action(itemActionFactory.view(NameTokens.UNDERTOW_CONFIGURATION))
                                .onPreview(new UndertowConfigurationPreview(crud, resources))
                                .build(),
                        new StaticItem.Builder(Names.BUFFER_CACHES)
                                .action(itemActionFactory.view(NameTokens.UNDERTOW_BUFFER_CACHE))
                                .onPreview(new PreviewContent(Names.BUFFER_CACHES,
                                        resources.previews().configurationUndertowBufferCaches()))
                                .build(),
                        new StaticItem.Builder(Names.SERVER)
                                .nextColumn(Ids.UNDERTOW_SERVER)
                                .onPreview(new PreviewContent(Names.SERVER,
                                        resources.previews().configurationUndertowServer()))
                                .build(),
                        new StaticItem.Builder(Names.SERVLET_CONTAINER)
                                .nextColumn(Ids.UNDERTOW_SERVLET_CONTAINER)
                                .onPreview(new PreviewContent(Names.SERVLET_CONTAINER,
                                        resources.previews().configurationUndertowServletContainer()))
                                .build(),
                        new StaticItem.Builder(Names.FILTERS)
                                .action(itemActionFactory.view(NameTokens.UNDERTOW_FILTER))
                                .onPreview(new PreviewContent<>(Names.FILTERS,
                                        resources.previews().configurationUndertowFilters()))
                                .build(),
                        new StaticItem.Builder(Names.HANDLERS)
                                .action(itemActionFactory.view(NameTokens.UNDERTOW_HANDLER))
                                .onPreview(new PreviewContent<>(Names.HANDLERS,
                                        resources.previews().configurationUndertowHandlers()))
                                .build()
                ))

                .breadcrumbItemsProvider((context, callback) -> {
                    // include those items w/o a next column
                    callback.onSuccess(asList(
                            new StaticItem.Builder(resources.constants().globalSettings())
                                    .action(itemActionFactory.view(NameTokens.UNDERTOW_CONFIGURATION))
                                    .onPreview(new UndertowConfigurationPreview(crud, resources))
                                    .build(),
                            new StaticItem.Builder(Names.BUFFER_CACHES)
                                    .action(itemActionFactory.view(NameTokens.UNDERTOW_BUFFER_CACHE))
                                    .onPreview(new PreviewContent(Names.BUFFER_CACHES,
                                            resources.previews().configurationUndertowBufferCaches()))
                                    .build(),
                            new StaticItem.Builder(Names.FILTERS)
                                    .action(itemActionFactory.view(NameTokens.UNDERTOW_FILTER))
                                    .onPreview(new PreviewContent<>(Names.FILTERS,
                                            resources.previews().configurationUndertowFilters()))
                                    .build(),
                            new StaticItem.Builder(Names.HANDLERS)
                                    .action(itemActionFactory.view(NameTokens.UNDERTOW_HANDLER))
                                    .onPreview(new PreviewContent<>(Names.HANDLERS,
                                            resources.previews().configurationUndertowHandlers()))
                                    .build()
                    ));
                })

                .onPreview(StaticItem::getPreviewContent)
                .useFirstActionAsBreadcrumbHandler());
    }
}
