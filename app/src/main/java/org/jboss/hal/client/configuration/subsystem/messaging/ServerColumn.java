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

import javax.inject.Inject;

import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.MESSAGING_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SERVER_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SERVER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

@AsyncColumn(Ids.MESSAGING_SERVER_CONFIGURATION)
@Requires(value = SERVER_ADDRESS, recursive = false)
public class ServerColumn extends FinderColumn<NamedNode> {

    @Inject
    public ServerColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory,
            final CrudOperations crud,
            final PlaceManager placeManager,
            final Places places) {

        super(new FinderColumn.Builder<NamedNode>(finder, Ids.MESSAGING_SERVER_CONFIGURATION, Names.SERVER)

                .columnAction(columnActionFactory.add(Ids.MESSAGING_SERVER_ADD, Names.SERVER, SERVER_TEMPLATE,
                        name -> {
                            //noinspection Convert2MethodRef
                            return Ids.messagingServer(name);
                        }))
                .columnAction(columnActionFactory.refresh(Ids.MESSAGING_SERVER_CONFIGURATION_REFRESH))

                .itemsProvider((context, callback) -> crud.readChildren(MESSAGING_SUBSYSTEM_TEMPLATE, SERVER,
                        children -> callback.onSuccess(asNamedNodes(children))))

                .onBreadcrumbItem((item, context) -> {
                    // replace 'server' request parameter
                    PlaceRequest current = placeManager.getCurrentPlaceRequest();
                    PlaceRequest place = places.replaceParameter(current, SERVER, item.getName()).build();
                    placeManager.revealPlace(place);
                })

                .onPreview(ServerPreview::new)
                .useFirstActionAsBreadcrumbHandler()
                .pinnable()
                .withFilter()
        );

        setItemRenderer(item -> new ItemDisplay<NamedNode>() {
            @Override
            public String getId() {
                return Ids.messagingServer(item.getName());
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public String nextColumn() {
                return Ids.MESSAGING_SERVER_SETTINGS;
            }

            @Override
            public List<ItemAction<NamedNode>> actions() {
                List<ItemAction<NamedNode>> actions = new ArrayList<>();
                actions.add(itemActionFactory.view(
                        places.selectedProfile(NameTokens.MESSAGING_SERVER).with(SERVER, item.getName()).build()));
                actions.add(itemActionFactory.remove(Names.SERVER, item.getName(), SERVER_TEMPLATE, ServerColumn.this));
                return actions;
            }
        });
    }
}
