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
package org.jboss.hal.client.runtime.subsystem.undertow;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderSegment;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static java.util.stream.StreamSupport.stream;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.AJP_LISTENER_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_SERVER_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_SERVER_TEMPLATE;
import static org.jboss.hal.core.Strings.substringAfterLast;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

@AsyncColumn(Ids.UNDERTOW_RUNTIME_LISTENER)
public class ListenerColumn extends FinderColumn<NamedNode> {

    static final String HAL_LISTENER_TYPE = "hal-listener-type";
    static final String HAL_WEB_SERVER = "hal-web-server";

    private Dispatcher dispatcher;
    private Resources resources;
    private EventBus eventBus;
    private StatementContext statementContext;

    @Inject
    public ListenerColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final Dispatcher dispatcher,
            final Resources resources,
            final EventBus eventBus,
            final StatementContext statementContext) {

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

                            List<Property> ajpProps = result.step(0).get(RESULT).asPropertyList();
                            List<Property> httpProps = result.step(1).get(RESULT).asPropertyList();
                            List<Property> httpsProps = result.step(2).get(RESULT).asPropertyList();
                            // add the listener type and undertow server to the result, because the preview pane
                            // contains a link to refresh the values, that it call a :read-resource operation
                            // and the listener-type and undertow server is part of the resource address.
                            ajpProps.forEach(p -> {
                                p.getValue().get(HAL_LISTENER_TYPE).set(AJP_LISTENER);
                                p.getValue().get(HAL_WEB_SERVER).set(server);
                            });
                            httpProps.forEach(p -> {
                                p.getValue().get(HAL_LISTENER_TYPE).set(HTTP_LISTENER);
                                p.getValue().get(HAL_WEB_SERVER).set(server);
                            });
                            httpsProps.forEach(p -> {
                                p.getValue().get(HAL_LISTENER_TYPE).set(HTTPS_LISTENER);
                                p.getValue().get(HAL_WEB_SERVER).set(server);
                            });

                            List<NamedNode> listeners = new ArrayList<>();
                            listeners.addAll(asNamedNodes(ajpProps));
                            listeners.addAll(asNamedNodes(httpProps));
                            listeners.addAll(asNamedNodes(httpsProps));

                            callback.onSuccess(listeners);
                        });
                    }

                })
                .onPreview(server -> new ListenerPreview(dispatcher, statementContext, resources, server)));
        this.dispatcher = dispatcher;
        this.resources = resources;
        this.eventBus = eventBus;
        this.statementContext = statementContext;

        setItemRenderer(item -> new ItemDisplay<NamedNode>() {
            @Override
            public String getId() {
                return Ids.webListener(item.getName());
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public List<ItemAction<NamedNode>> actions() {
                List<ItemAction<NamedNode>> actions = new ArrayList<>();
                actions.add(new ItemAction.Builder<NamedNode>()
                        .title(resources.constants().reset())
                        .constraint(Constraint.executable(AJP_LISTENER_TEMPLATE, RESET_STATISTICS_OPERATION))
                        .handler(item1 -> resetStatistics(item))
                        .build());
                return actions;
            }
        });
    }

    private void resetStatistics(final NamedNode item) {

        DialogFactory.showConfirmation(resources.messages().resetStatisticsTitle(),
                resources.messages().resetStatisticsQuestion(item.getName()), () -> {

                    String listenerType = item.asModelNode().get(HAL_LISTENER_TYPE).asString();
                    String webserver = item.asModelNode().get(HAL_WEB_SERVER).asString();
                    ResourceAddress address = AddressTemplate
                            .of(WEB_SERVER_ADDRESS + "/" + listenerType + "=" + item.getName())
                            .resolve(statementContext, webserver);
                    Operation operation = new Operation.Builder(address, RESET_STATISTICS_OPERATION).build();
                    dispatcher.execute(operation, result -> {
                        refresh(RESTORE_SELECTION);
                        MessageEvent.fire(eventBus,
                                Message.success(resources.messages().resetStatisticsSuccess(item.getName())));
                    });
                });
    }


}
