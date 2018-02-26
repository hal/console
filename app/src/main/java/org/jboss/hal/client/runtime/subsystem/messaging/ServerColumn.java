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
package org.jboss.hal.client.runtime.subsystem.messaging;

import javax.inject.Inject;

import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.AsyncColumn;

import static org.jboss.hal.client.runtime.subsystem.messaging.AddressTemplates.MESSAGING_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

@AsyncColumn(Ids.MESSAGING_SERVER_RUNTIME)
public class ServerColumn extends FinderColumn<NamedNode> {

    @Inject
    public ServerColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            PlaceManager placeManager,
            Places places,
            Dispatcher dispatcher,
            StatementContext statementContext) {

        super(new FinderColumn.Builder<NamedNode>(finder, Ids.MESSAGING_SERVER_RUNTIME, Names.SERVER)
                .columnAction(columnActionFactory.refresh(Ids.MESSAGING_SERVER_RUNTIME_REFRESH))
                .itemsProvider((context, callback) -> {
                    ResourceAddress address = MESSAGING_SUBSYSTEM_TEMPLATE.resolve(statementContext);
                    Operation operation = new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                            .param(CHILD_TYPE, SERVER)
                            .param(INCLUDE_RUNTIME, true)
                            .build();
                    dispatcher.execute(operation, result -> callback.onSuccess(asNamedNodes(result.asPropertyList())));
                })
                .onBreadcrumbItem((item, context) -> {
                    // try to replace Ids.MESSAGING_SERVER request parameter
                    PlaceRequest current = placeManager.getCurrentPlaceRequest();
                    PlaceRequest place = places.replaceParameter(current, Ids.MESSAGING_SERVER, item.getName()).build();
                    placeManager.revealPlace(place);
                })
                .itemRenderer(item -> new ItemDisplay<NamedNode>() {
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
                        return Ids.MESSAGING_SERVER_DESTINATION_RUNTIME;
                    }
                })
                .onPreview(ServerPreview::new)
        );
    }
}
