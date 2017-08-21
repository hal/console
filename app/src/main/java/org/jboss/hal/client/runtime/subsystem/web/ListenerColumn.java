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
package org.jboss.hal.client.runtime.subsystem.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import javax.inject.Inject;

import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderSegment;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.AsyncColumn;

import static java.util.stream.StreamSupport.stream;
import static org.jboss.hal.client.runtime.subsystem.web.AddressTemplates.WEB_SERVER_TEMPLATE;
import static org.jboss.hal.core.Strings.substringAfterLast;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

@AsyncColumn(Ids.UNDERTOW_RUNTIME_LISTENER)
public class ListenerColumn extends FinderColumn<NamedNode> {

    static Logger _log = Logger.getLogger("org.jboss");


    @Inject
    public ListenerColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            PlaceManager placeManager,
            Places places,
            Dispatcher dispatcher,
            StatementContext statementContext) {

        super(new Builder<NamedNode>(finder, Ids.UNDERTOW_RUNTIME_LISTENER, Names.LISTENER)
                .columnAction(columnActionFactory.refresh(Ids.UNDERTOW_LISTENER_REFRESH))
                .itemsProvider((context, callback) -> {

                    // extract server name from the finder path
                    Optional<String> optional = stream(context.getPath().spliterator(), false)
                            .filter(segment -> Ids.UNDERTOW_RUNTIME_SERVER.equals(segment.getColumnId()))
                            .findAny()
                            .map(FinderSegment::getItemId);
                    if (optional.isPresent()) {
                        // Extract the server name from the item id "undertow-server-<server name>"
                        String server = substringAfterLast(optional.get(), Ids.UNDERTOW_SERVER + "-");
                        ResourceAddress address = WEB_SERVER_TEMPLATE.resolve(statementContext, server);

                        Operation opAjp = new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                                .param(CHILD_TYPE, AJP_LISTENER)
                                .param(INCLUDE_RUNTIME, true)
                                .build();
                        Operation opHttp = new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                                .param(CHILD_TYPE, HTTP_LISTENER)
                                .param(INCLUDE_RUNTIME, true)
                                .build();
                        Operation opHttps = new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                                .param(CHILD_TYPE, HTTPS_LISTENER)
                                .param(INCLUDE_RUNTIME, true)
                                .build();

                        dispatcher.execute(new Composite(opAjp, opHttp, opHttps), (CompositeResult result) -> {

                            List<NamedNode> listeners = new ArrayList<>();
                            listeners.addAll(asNamedNodes(result.step(0).get(RESULT).asPropertyList()));
                            listeners.addAll(asNamedNodes(result.step(1).get(RESULT).asPropertyList()));
                            listeners.addAll(asNamedNodes(result.step(2).get(RESULT).asPropertyList()));

                            callback.onSuccess(listeners);
                        });
                    }

                })
                .onBreadcrumbItem((item, context) -> {
                    PlaceRequest current = placeManager.getCurrentPlaceRequest();
                    PlaceRequest place = places.replaceParameter(current, Ids.UNDERTOW_RUNTIME_LISTENER, item.getName()).build();
                    placeManager.revealPlace(place);
                })
                .itemRenderer(item -> new ItemDisplay<NamedNode>() {
                    @Override
                    public String getId() {
                        return Ids.webListener(item.getName());
                    }

                    @Override
                    public String getTitle() {
                        return item.getName();
                    }

                })
                .onPreview(ListenerPreview::new)
        );
    }
}
