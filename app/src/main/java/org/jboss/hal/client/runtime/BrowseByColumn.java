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
package org.jboss.hal.client.runtime;

import java.util.Arrays;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderContext;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.FinderSegment;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.finder.StaticItemColumn;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.group.ServerGroupActions;
import org.jboss.hal.core.runtime.host.HostActions;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.security.SecurityContextRegistry;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;

import static org.jboss.hal.client.runtime.BrowseByColumn.ANY_HOST;
import static org.jboss.hal.client.runtime.BrowseByColumn.SERVER_CONFIG_ADDRESS;
import static org.jboss.hal.client.runtime.BrowseByColumn.SERVER_GROUP_ADDRESS;

@Column(Ids.DOMAIN_BROWSE_BY)
@Requires(value = { ANY_HOST, SERVER_GROUP_ADDRESS, SERVER_CONFIG_ADDRESS }, recursive = false)
public class BrowseByColumn extends StaticItemColumn {

    // necessary for the constraints in topology preview
    static final String ANY_HOST = "/host=*";
    static final String SERVER_GROUP_ADDRESS = "/server-group=*";
    static final String SERVER_CONFIG_ADDRESS = ANY_HOST + "/server-config=*";

    public static boolean browseByHosts(FinderContext context) {
        for (FinderSegment<?> segment : context.getPath()) {
            if (Ids.DOMAIN_BROWSE_BY.equals(segment.getColumnId())) {
                return Objects.equals(Ids.asId(Names.HOSTS), segment.getItemId());
            }
        }
        return false;
    }

    public static boolean browseByServerGroups(FinderContext context) {
        for (FinderSegment<?> segment : context.getPath()) {
            if (Ids.DOMAIN_BROWSE_BY.equals(segment.getColumnId())) {
                return Objects.equals(Ids.asId(Names.SERVER_GROUPS), segment.getItemId());
            }
        }
        return false;
    }

    @Inject
    public BrowseByColumn(Finder finder,
            Environment environment,
            SecurityContextRegistry securityContextRegistry,
            @Footer Provider<Progress> progress,
            EventBus eventBus,
            ItemActionFactory itemActionFactory,
            Dispatcher dispatcher,
            Places places,
            FinderPathFactory finderPathFactory,
            HostActions hostActions,
            ServerGroupActions serverGroupActions,
            ServerActions serverActions,
            Resources resources) {
        super(finder, Ids.DOMAIN_BROWSE_BY, resources.constants().browseBy(),
                Arrays.asList(
                        new StaticItem.Builder(Names.TOPOLOGY)
                                .onPreview(new TopologyPreview(securityContextRegistry, environment,
                                        dispatcher, progress, eventBus, places, finderPathFactory, hostActions,
                                        serverGroupActions, serverActions, resources))
                                .build(),
                        new StaticItem.Builder(Names.HOSTS)
                                .nextColumn(Ids.HOST)
                                .onPreview(new PreviewContent<>(Names.HOSTS, resources.previews().runtimeHosts()))
                                .build(),
                        new StaticItem.Builder(Names.SERVER_GROUPS)
                                .nextColumn(Ids.SERVER_GROUP)
                                .onPreview(new PreviewContent<>(Names.SERVER_GROUPS,
                                        resources.previews().runtimeServerGroups()))
                                .build(),
                        new StaticItem.Builder(Names.MANAGEMENT_OPERATIONS)
                                .id(Ids.MANAGEMENT_OPERATIONS)
                                .onPreview(new PreviewContent<>(Names.MANAGEMENT_OPERATIONS,
                                        resources.previews().runtimeManagementOperations()))
                                .action(itemActionFactory.view(NameTokens.MANAGEMENT_OPERATIONS))
                                .build()));
    }
}
