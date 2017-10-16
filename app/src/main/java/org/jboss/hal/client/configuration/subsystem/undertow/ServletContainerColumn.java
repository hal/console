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

import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SERVLET_CONTAINER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.UNDERTOW_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVLET_CONTAINER;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

@AsyncColumn(Ids.UNDERTOW_SERVLET_CONTAINER)
@Requires(AddressTemplates.SERVLET_CONTAINER_ADDRESS)
public class ServletContainerColumn extends FinderColumn<NamedNode> {

    @Inject
    public ServletContainerColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory,
            final Places places,
            final CrudOperations crud) {
        super(new Builder<NamedNode>(finder, Ids.UNDERTOW_SERVLET_CONTAINER, Names.SERVLET_CONTAINER)

                .columnAction(columnActionFactory.add(Ids.UNDERTOW_SERVLET_CONTAINER_ADD, Names.SERVLET_CONTAINER,
                        SERVLET_CONTAINER_TEMPLATE, Ids::undertowServletContainer))
                .columnAction(columnActionFactory.refresh(Ids.UNDERTOW_SERVLET_CONTAINER_REFRESH))

                .itemsProvider(
                        (context, callback) -> crud.readChildren(UNDERTOW_SUBSYSTEM_TEMPLATE, SERVLET_CONTAINER, 2,
                                children -> callback.onSuccess(asNamedNodes(children))))

                .onPreview(ServletContainerPreview::new)
                .useFirstActionAsBreadcrumbHandler()
                .pinnable()
                .withFilter()
        );

        setItemRenderer(item -> new ItemDisplay<NamedNode>() {
            @Override
            public String getId() {
                return Ids.undertowServletContainer(item.getName());
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public List<ItemAction<NamedNode>> actions() {
                List<ItemAction<NamedNode>> actions = new ArrayList<>();
                actions.add(itemActionFactory.view(places.selectedProfile(NameTokens.UNDERTOW_SERVLET_CONTAINER)
                        .with(NAME, item.getName())
                        .build()));
                actions.add(
                        itemActionFactory.remove(Names.SERVLET_CONTAINER, item.getName(), SERVLET_CONTAINER_TEMPLATE,
                                ServletContainerColumn.this));
                return actions;
            }
        });
    }
}
