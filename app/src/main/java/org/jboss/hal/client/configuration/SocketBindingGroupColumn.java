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
package org.jboss.hal.client.configuration;

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
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING_GROUP;

@AsyncColumn(Ids.SOCKET_BINDING_GROUP)
@Requires(SocketBindingGroupPresenter.ROOT_ADDRESS)
public class SocketBindingGroupColumn extends FinderColumn<NamedNode> {

    @Inject
    public SocketBindingGroupColumn(final Finder finder,
            final Places places,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory,
            final CrudOperations crud) {

        super(new FinderColumn.Builder<NamedNode>(finder, Ids.SOCKET_BINDING_GROUP, Names.SOCKET_BINDING_GROUP)
                .columnAction(columnActionFactory.add(
                        Ids.SOCKET_BINDING_GROUP_ADD,
                        Names.SOCKET_BINDING_GROUP,
                        SocketBindingGroupPresenter.ROOT_TEMPLATE))
                .columnAction(columnActionFactory.refresh(Ids.SOCKET_BINDING_GROUP_REFRESH))
                .itemsProvider((context, callback) -> crud.readChildren(ResourceAddress.root(), SOCKET_BINDING_GROUP, 1,
                        result -> callback.onSuccess(ModelNodeHelper.asNamedNodes(result))))
                .useFirstActionAsBreadcrumbHandler()
                .onPreview((socketBinding) -> new SocketBindingGroupPreview(socketBinding, places))
        );

        setItemRenderer(item -> new ItemDisplay<NamedNode>() {
            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public List<ItemAction<NamedNode>> actions() {
                return asList(
                        itemActionFactory.view(NameTokens.SOCKET_BINDING_GROUP, NAME, item.getName()),
                        itemActionFactory.remove(Names.SOCKET_BINDING_GROUP, item.getName(),
                                SocketBindingGroupPresenter.ROOT_TEMPLATE, SocketBindingGroupColumn.this));
            }
        });
    }
}
