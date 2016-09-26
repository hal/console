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

import java.util.List;
import javax.inject.Inject;

import com.google.common.collect.Lists;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.semver.Version;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.finder.StaticItemColumn;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.ManagementModel;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

/**
 * @author Harald Pehl
 */
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
            List<StaticItem> items = Lists.newArrayList(

                    new StaticItem.Builder(resources.constants().status())
                            .action(itemActionFactory.view(places.selectedServer(NameTokens.SERVER_STATUS).build()))
                            .onPreview(
                                    new ServerStatusPreview(environment, dispatcher, statementContext, resources))
                            .build(),

                    new StaticItem.Builder(Names.DATASOURCES)
                            .nextColumn(Ids.DATA_SOURCE_RUNTIME)
                            .onPreview(new PreviewContent(Names.DATASOURCES,
                                    resources.previews().runtimeDatasources()))
                            .build(),

                    new StaticItem.Builder(Names.JPA)
                            .nextColumn(Ids.JPA_RUNTIME)
                            .onPreview(new PreviewContent(Names.JPA, resources.previews().runtimeJpa()))
                            .build(),

                    new StaticItem.Builder(Names.JNDI)
                            .action(itemActionFactory.view(NameTokens.JNDI))
                            .onPreview(new PreviewContent(Names.JNDI, resources.previews().runtimeJndi()))
                            .build());

            ResourceAddress address = AddressTemplate.of("/{selected.host}/{selected.server}")
                    .resolve(statementContext);
            Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION, address)
                    .param(ATTRIBUTES_ONLY, true)
                    .build();
            dispatcher.execute(operation, result -> {
                Version serverVersion = ManagementModel.parseVersion(result);
                if (ManagementModel.supportsListLogFiles(serverVersion)) {
                    items.add(new StaticItem.Builder(resources.constants().logFiles())
                            .nextColumn(Ids.LOG_FILE)
                            .onPreview(new PreviewContent(resources.constants().logFiles(),
                                    resources.previews().runtimeLogFiles()))
                            .build());
                }
                callback.onSuccess(items);
            });
        });
    }
}
