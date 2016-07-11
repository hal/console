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

import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;

/**
 * @author Harald Pehl
 */
@AsyncColumn(Ids.SOCKET_BINDING)
@Requires(SocketBindingPresenter.ROOT_ADDRESS)
public class SocketBindingColumn extends FinderColumn<Property> {

    @Inject
    public SocketBindingColumn(final Finder finder,
            final Dispatcher dispatcher,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory) {

        super(new FinderColumn.Builder<Property>(finder, Ids.SOCKET_BINDING, Names.SOCKET_BINDING)
                .columnAction(columnActionFactory.add(
                        Ids.SOCKET_BINDING_ADD,
                        Names.SOCKET_BINDING,
                        SocketBindingPresenter.ROOT_TEMPLATE))
                .columnAction(columnActionFactory.refresh(Ids.SOCKET_BINDING_REFRESH))
                .itemsProvider((context, callback) -> {
                    Operation operation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, ResourceAddress.ROOT)
                            .param(CHILD_TYPE, "socket-binding-group").build();
                    dispatcher.execute(operation, result -> { callback.onSuccess(result.asPropertyList()); });
                }));

        setItemRenderer(property -> new ItemDisplay<Property>() {
            @Override
            public String getTitle() {
                return property.getName();
            }

            @Override
            public List<ItemAction<Property>> actions() {
                return asList(
                        itemActionFactory.view(NameTokens.SOCKET_BINDING, NAME, property.getName()),
                        itemActionFactory.remove(Names.SOCKET_BINDING, property.getName(),
                                SocketBindingPresenter.ROOT_TEMPLATE, SocketBindingColumn.this));
            }
        });
    }
}
