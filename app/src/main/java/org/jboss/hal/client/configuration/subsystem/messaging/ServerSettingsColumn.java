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
package org.jboss.hal.client.configuration.subsystem.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;

import org.jboss.hal.core.Strings;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderSegment;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.finder.StaticItemColumn;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;

import static java.util.stream.StreamSupport.stream;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;

/**
 * @author Harald Pehl
 */
@AsyncColumn(Ids.MESSAGING_SERVER_SETTINGS)
public class ServerSettingsColumn
        extends FinderColumn<StaticItem> { // doesn't extend from StaticItemColumn because we need more flexibility

    @Inject
    public ServerSettingsColumn(final Finder finder,
            final ItemActionFactory itemActionFactory,
            final Places places,
            final Resources resources) {

        super(new Builder<StaticItem>(finder, Ids.MESSAGING_SERVER_SETTINGS, resources.constants().settings())

                .itemsProvider((context, callback) -> {
                    List<StaticItem> items = new ArrayList<>();
                    Optional<String> optional = stream(context.getPath().spliterator(), false)
                            .filter(segment -> Ids.MESSAGING_SERVER.equals(segment.getColumnId()))
                            .findAny()
                            .map(FinderSegment::getItemId);
                    if (optional.isPresent()) {
                        // Extract the server name from the item id "messaging-server-<server name>"
                        String server = Strings.substringAfterLast(optional.get(), Ids.MESSAGING_SERVER + "-");
                        items.add(new StaticItem.Builder(Names.DESTINATIONS)
                                .id(Ids.MESSAGING_SERVER_DESTINATION)
                                .action(itemActionFactory.view(
                                        places.selectedProfile(NameTokens.MESSAGING_SERVER_DESTINATION)
                                                .with(SERVER, server)
                                                .build()))
                                .onPreview(new PreviewContent(Names.DESTINATIONS,
                                        resources.previews().configurationMessagingDestinations()))
                                .build());
                        items.add(new StaticItem.Builder(Names.CONNECTIONS)
                                .id(Ids.MESSAGING_SERVER_CONNECTION)
                                .action(itemActionFactory.view(
                                        places.selectedProfile(NameTokens.MESSAGING_SERVER_CONNECTION)
                                                .with(SERVER, server)
                                                .build()))
                                .onPreview(new PreviewContent(Names.CONNECTIONS,
                                        resources.previews().configurationMessagingConnections()))
                                .build());
                        items.add(new StaticItem.Builder(Names.CLUSTERING)
                                .id(Ids.MESSAGING_SERVER_CLUSTERING)
                                .action(itemActionFactory.view(
                                        places.selectedProfile(NameTokens.MESSAGING_SERVER_CLUSTERING)
                                                .with(SERVER, server)
                                                .build()))
                                .onPreview(new PreviewContent(Names.CLUSTERING,
                                        resources.previews().configurationMessagingClustering()))
                                .build());
                        items.add(new StaticItem.Builder(Names.HA_POLICY)
                                .id(Ids.MESSAGING_SERVER_HA_POLICY)
                                .action(itemActionFactory.view(
                                        places.selectedProfile(NameTokens.MESSAGING_SERVER_HA_POLICY)
                                                .with(SERVER, server)
                                                .build()))
                                .onPreview(new PreviewContent(Names.HA_POLICY,
                                        resources.previews().configurationMessagingHaPolicy()))
                                .build());
                    }
                    callback.onSuccess(items);
                })

                .itemRenderer(StaticItemColumn.StaticItemDisplay::new)
                .useFirstActionAsBreadcrumbHandler()
                .onPreview(StaticItem::getPreviewContent)
        );
    }
}
