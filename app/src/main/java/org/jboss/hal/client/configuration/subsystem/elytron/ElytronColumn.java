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
package org.jboss.hal.client.configuration.subsystem.elytron;

import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.core.CrudOperations;
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
import org.jboss.hal.spi.AsyncColumn;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * Holds the top level items to configure the elytron subsystem.
 */
@AsyncColumn(Ids.ELYTRON)
public class ElytronColumn
        extends FinderColumn<StaticItem> { // doesn't extend from StaticItemColumn because we need more flexibility

    @Inject
    public ElytronColumn(final Finder finder,
            final ItemActionFactory itemActionFactory,
            final CrudOperations crud,
            final Places places,
            final Resources resources) {

        super(new Builder<StaticItem>(finder, Ids.ELYTRON, resources.constants().settings())
                .itemRenderer(StaticItemColumn.StaticItemDisplay::new)
                .onPreview(StaticItem::getPreviewContent)
                .useFirstActionAsBreadcrumbHandler()
                .withFilter());

        List<StaticItem> items = asList(
                new StaticItem.Builder(resources.constants().globalSettings())
                        .id(Ids.ELYTRON)
                        .action(itemActionFactory.viewAndMonitor(Ids.ELYTRON,
                                places.selectedProfile(NameTokens.ELYTRON).build()))
                        .onPreview(new ElytronSubsystemPreview(crud, resources))
                        .keywords("global", "settings")
                        .build(),

                new StaticItem.Builder(Names.FACTORIES_TRANSFORMERS)
                        .action(itemActionFactory.viewAndMonitor(Ids.asId(Names.FACTORIES_TRANSFORMERS),
                                places.selectedProfile(NameTokens.ELYTRON_FACTORIES_TRANSFORMERS).build()))
                        .onPreview(new PreviewContent(Names.FACTORIES_TRANSFORMERS,
                                resources.previews().configurationElytronFactories()))
                        .keywords("factory", "transformer", "http", "sasl", "kerberos", "principal")
                        .build(),

                new StaticItem.Builder(Names.MAPPERS_DECODERS)
                        .action(itemActionFactory.viewAndMonitor(Ids.asId(Names.MAPPERS_DECODERS),
                                places.selectedProfile(NameTokens.ELYTRON_MAPPERS_DECODERS).build()))
                        .onPreview(new PreviewContent(Names.MAPPERS_DECODERS,
                                resources.previews().configurationElytronMappersDecoders()))
                        .keywords("mapper", "decoder", "role", "permission", "principal")
                        .build(),

                new StaticItem.Builder(Names.OTHER_SETTINGS)
                        .action(itemActionFactory.viewAndMonitor(Ids.asId(Names.OTHER_SETTINGS),
                                places.selectedProfile(NameTokens.ELYTRON_OTHER).build()))
                        .onPreview(new PreviewContent(Names.OTHER_SETTINGS,
                                resources.previews().configurationElytronOtherSettings()))
                        .keywords("store", "ssl", "authentication", "ldap")
                        .build(),

                new StaticItem.Builder(Names.SECURITY_REALMS)
                        .action(itemActionFactory.viewAndMonitor(Ids.asId(Names.SECURITY_REALMS),
                                places.selectedProfile(NameTokens.ELYTRON_SECURITY_REALMS).build()))
                        .onPreview(new PreviewContent(Names.SECURITY_REALMS,
                                resources.previews().configurationElytronSecurityRealms()))
                        .keywords("realm")
                        .build()


        );
        setItemsProvider((context, callback) -> callback.onSuccess(items));
        setBreadcrumbItemsProvider(
                (context, callback) -> callback.onSuccess(
                        items.stream().filter(item -> item.getNextColumn() == null).collect(toList())));
    }
}
