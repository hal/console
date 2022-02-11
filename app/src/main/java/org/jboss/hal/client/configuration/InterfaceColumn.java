/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INET_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INTERFACE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

@Column(Ids.INTERFACE)
@Requires(InterfacePresenter.ROOT_ADDRESS)
public class InterfaceColumn extends FinderColumn<NamedNode> {

    @Inject
    public InterfaceColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            ItemActionFactory itemActionFactory,
            Places places,
            Dispatcher dispatcher,
            CrudOperations crud) {

        super(new Builder<NamedNode>(finder, Ids.INTERFACE, Names.INTERFACE)
                .itemsProvider((context, callback) -> crud.readChildren(ResourceAddress.root(), INTERFACE,
                        result -> callback.onSuccess(asNamedNodes(result))))
                .useFirstActionAsBreadcrumbHandler()
                .onPreview(item -> new InterfacePreview(item, dispatcher, places)));

        setItemRenderer(item -> new ItemDisplay<NamedNode>() {
            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public List<ItemAction<NamedNode>> actions() {
                return asList(
                        itemActionFactory.view(NameTokens.INTERFACE, NAME, item.getName()),
                        itemActionFactory.remove(Names.INTERFACE, item.getName(), InterfacePresenter.ROOT_TEMPLATE,
                                InterfaceColumn.this));
            }
        });
        addColumnAction(columnActionFactory.add(
                Ids.INTERFACE_ADD,
                Names.INTERFACE,
                InterfacePresenter.ROOT_TEMPLATE,
                singletonList(INET_ADDRESS),
                this::createUniqueValidation));
        addColumnAction(columnActionFactory.refresh(Ids.INTERFACE_REFRESH));
    }
}
