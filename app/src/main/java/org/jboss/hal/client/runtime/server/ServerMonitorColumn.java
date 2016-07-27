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

import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.finder.StaticItemColumn;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;

import static java.util.Arrays.asList;

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
            final Resources resources) {
        super(finder, Ids.SERVER_MONITOR, resources.constants().monitor(), asList(

                new StaticItem.Builder(resources.constants().status())
                        .onPreview(new ServerStatusPreview(environment, dispatcher, statementContext, resources))
                        .build(),

                new StaticItem.Builder(Names.DATASOURCES)
                        .onPreview(new PreviewContent(Names.DATASOURCES, resources.previews().runtimeDatasources()))
                        .nextColumn(Ids.DATA_SOURCE_RUNTIME)
                        .build(),

                new StaticItem.Builder(Names.JPA)
                        .onPreview(new PreviewContent(Names.JPA, resources.previews().runtimeJpa()))
                        .nextColumn(Ids.JPA_RUNTIME)
                        .build(),

                new StaticItem.Builder(resources.constants().logFiles())
                        .onPreview(new PreviewContent(resources.constants().logFiles(),
                                resources.previews().runtimeLogFiles()))
                        .nextColumn(Ids.LOG_FILE)
                        .build()
        ));
    }
}
