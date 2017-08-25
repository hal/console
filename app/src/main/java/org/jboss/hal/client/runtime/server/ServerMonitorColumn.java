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
package org.jboss.hal.client.runtime.server;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import org.jboss.hal.client.runtime.subsystem.batch.BatchPreview;
import org.jboss.hal.client.runtime.subsystem.ejb.ThreadPoolPreview;
import org.jboss.hal.client.runtime.subsystem.web.WebPreview;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.semver.Version;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.finder.StaticItemColumn;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.ManagementModel;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;

import static java.util.Comparator.comparing;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_HOST;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_SERVER;

@AsyncColumn(Ids.SERVER_MONITOR)
public class ServerMonitorColumn extends StaticItemColumn {

    @Inject
    public ServerMonitorColumn(final Finder finder,
            final Environment environment,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final ItemActionFactory itemActionFactory,
            final Places places,
            final Resources resources) {
        super(finder, Ids.SERVER_MONITOR, resources.constants().monitor(), (context, callback) -> {

            ResourceAddress address = AddressTemplate.of(SELECTED_HOST, SELECTED_SERVER)
                    .resolve(statementContext);
            Operation operation1 = new Operation.Builder(address, READ_CHILDREN_NAMES_OPERATION)
                    .param(CHILD_TYPE, SUBSYSTEM)
                    .build();
            Operation operation2 = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                    .param(ATTRIBUTES_ONLY, true)
                    .build();
            dispatcher.execute(new Composite(operation1, operation2), (CompositeResult result) -> {

                List<StaticItem> items = new ArrayList<>();
                ModelNode subsystemsNode = result.step(0).get(RESULT);

                for (ModelNode subsystemNode : subsystemsNode.asList()) {
                    String subsystem = subsystemNode.asString();

                    switch (subsystem) {
                        case BATCH_JBERET:
                            items.add(new StaticItem.Builder(Names.BATCH)
                                    .nextColumn(Ids.JOB)
                                    .onPreview(new BatchPreview(dispatcher, statementContext, resources))
                                    .build());
                            break;

                        case DATASOURCES:
                            items.add(new StaticItem.Builder(Names.DATASOURCES)
                                    .nextColumn(Ids.DATA_SOURCE_RUNTIME)
                                    .onPreview(new PreviewContent(Names.DATASOURCES,
                                            resources.previews().runtimeDatasources()))
                                    .build());
                            break;


                        case EJB3:
                            items.add(new StaticItem.Builder(Names.EJB3)
                                    .nextColumn(Ids.EJB3)
                                    .onPreview(new ThreadPoolPreview(dispatcher, statementContext, resources))
                                    .build());
                            break;

                        case IO:
                            items.add(new StaticItem.Builder(Names.IO)
                                    .nextColumn(Ids.WORKER)
                                    .onPreview(new PreviewContent(Names.WORKER, resources.previews().runtimeWorker()))
                                    .build());
                            break;

                        case NAMING:
                            items.add(new StaticItem.Builder(Names.JNDI)
                                    .action(itemActionFactory.view(places.selectedServer(NameTokens.JNDI).build()))
                                    .onPreview(new PreviewContent(Names.JNDI, resources.previews().runtimeJndi()))
                                    .build());
                            break;

                        case JPA:
                            items.add(new StaticItem.Builder(Names.JPA)
                                    .nextColumn(Ids.JPA_RUNTIME)
                                    .onPreview(new PreviewContent(Names.JPA, resources.previews().runtimeJpa()))
                                    .build());
                            break;

                        case MESSAGING_ACTIVEMQ:
                            items.add(new StaticItem.Builder(Names.MESSAGING)
                                    .subtitle(Names.ACTIVE_MQ)
                                    .nextColumn(Ids.MESSAGING_SERVER_RUNTIME)
                                    .onPreview(
                                            new PreviewContent(Names.SERVER,
                                                    resources.previews().runtimeMessagingServer()))
                                    .build());
                            break;

                        case UNDERTOW:
                            items.add(new StaticItem.Builder(Names.WEB)
                                    .subtitle(Names.UNDERTOW)
                                    .nextColumn(Ids.UNDERTOW_RUNTIME)
                                    .onPreview(new WebPreview(dispatcher, statementContext, resources))
                                    .build());
                            break;
                    }
                }

                ModelNode attrsNode = result.step(1).get(RESULT);
                Version serverVersion = ManagementModel.parseVersion(attrsNode);
                if (ManagementModel.supportsListLogFiles(serverVersion)) {
                    items.add(new StaticItem.Builder(resources.constants().logFiles())
                            .nextColumn(Ids.LOG_FILE)
                            .onPreview(new PreviewContent(resources.constants().logFiles(),
                                    resources.previews().runtimeLogFiles()))
                            .build());
                }
                items.sort(comparing(StaticItem::getTitle));
                List<StaticItem> statusFirst = new ArrayList<>();
                statusFirst.add(new StaticItem.Builder(resources.constants().status())
                        .action(itemActionFactory.view(places.selectedServer(NameTokens.SERVER_STATUS).build()))
                        .onPreview(new ServerStatusPreview(environment, dispatcher, statementContext, resources))
                        .build());
                statusFirst.addAll(items);
                callback.onSuccess(statusFirst);
            });
        });
    }
}
