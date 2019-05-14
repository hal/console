/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.undertow;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

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

import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SERVER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.UNDERTOW_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

@AsyncColumn(Ids.UNDERTOW_SERVER)
@Requires(AddressTemplates.SERVER_ADDRESS)
public class ServerColumn extends FinderColumn<NamedNode> {

    @Inject
    public ServerColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory,
            final Places places,
            final CrudOperations crud) {

        super(new FinderColumn.Builder<NamedNode>(finder, Ids.UNDERTOW_SERVER, Names.SERVER)

                .itemsProvider((context, callback) -> crud.readChildren(UNDERTOW_SUBSYSTEM_TEMPLATE, SERVER,
                        children -> callback.onSuccess(asNamedNodes(children))))

                .onPreview(ServerPreview::new)
                .useFirstActionAsBreadcrumbHandler()
                .pinnable()
                .withFilter()
        );

        addColumnAction(columnActionFactory.add(Ids.UNDERTOW_SERVER_ADD, Names.SERVER, SERVER_TEMPLATE,
                Ids::undertowServer, this::createUniqueValidation));
        addColumnAction(columnActionFactory.refresh(Ids.UNDERTOW_SERVER_REFRESH));

        setItemRenderer(item -> new ItemDisplay<NamedNode>() {
            @Override
            public String getId() {
                return Ids.undertowServer(item.getName());
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public List<ItemAction<NamedNode>> actions() {
                List<ItemAction<NamedNode>> actions = new ArrayList<>();
                actions.add(itemActionFactory.view(
                        places.selectedProfile(NameTokens.UNDERTOW_SERVER).with(NAME, item.getName()).build()));
                actions.add(itemActionFactory.remove(Names.SERVER, item.getName(), SERVER_TEMPLATE,
                        ServerColumn.this));
                return actions;
            }
        });
    }
}
