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
package org.jboss.hal.client.runtime.group;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.base.Joiner;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.runtime.SuspendState;
import org.jboss.hal.core.runtime.TopologyFunctions;
import org.jboss.hal.core.runtime.group.ServerGroup;
import org.jboss.hal.core.runtime.group.ServerGroupActions;
import org.jboss.hal.core.runtime.group.ServerGroupSelectionEvent;
import org.jboss.hal.core.runtime.server.ServerConfigStatus;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;
import static org.jboss.hal.resources.CSS.itemText;
import static org.jboss.hal.resources.CSS.subtitle;

/**
 * @author Harald Pehl
 */
@Column(SERVER_GROUP)
@Requires(value = "/server-group=*", recursive = false)
public class ServerGroupColumn extends FinderColumn<ServerGroup> {

    @Inject
    public ServerGroupColumn(final Finder finder,
            final Environment environment,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final @Footer Provider<Progress> progress,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory,
            final ServerGroupActions serverGroupActions,
            final Resources resources) {

        super(new Builder<ServerGroup>(finder, SERVER_GROUP, Names.SERVER_GROUP)

                .columnAction(columnActionFactory.add(IdBuilder.build(SERVER_GROUP, "add"), Names.SERVER_GROUP,
                        AddressTemplate.of("/server-group=*")))
                .columnAction(columnActionFactory.refresh(IdBuilder.build(SERVER_GROUP, "refresh")))

                .itemsProvider((context, callback) ->
                        new Async<FunctionContext>(progress.get()).waterfall(
                                new FunctionContext(),
                                new Outcome<FunctionContext>() {
                                    @Override
                                    public void onFailure(final FunctionContext context) {
                                        callback.onFailure(context.getError());
                                    }

                                    @Override
                                    public void onSuccess(final FunctionContext context) {
                                        List<ServerGroup> serverGroups = context.get(TopologyFunctions.SERVER_GROUPS);
                                        callback.onSuccess(serverGroups);
                                    }
                                },
                                new TopologyFunctions.ServerGroupsWithServerConfigs(environment, dispatcher),
                                new TopologyFunctions.ServerGroupsStartedServers(environment, dispatcher)))

                .onPreview(ServerGroupPreview::new)
                // TODO Change the security context (server group scoped roles!)
                .onItemSelect(serverGroup -> eventBus.fireEvent(new ServerGroupSelectionEvent(serverGroup.getName())))
                .pinnable()
                .showCount()
                .useFirstActionAsBreadcrumbHandler()
                .withFilter()
        );

        setItemRenderer(item -> new ItemDisplay<ServerGroup>() {
            @Override
            public String getId() {
                return ServerGroup.id(item.getName());
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public Element asElement() {
                return new Elements.Builder()
                        .span().css(itemText)
                        .span().textContent(item.getName()).end()
                        .start("small").css(subtitle).textContent(item.getProfile()).end()
                        .end().build();
            }

            @Override
            public String getFilterData() {
                return Joiner.on(' ').join(item.getName(), item.getProfile());
            }

            @Override
            public String nextColumn() {
                return SERVER;
            }

            @Override
            public List<ItemAction<ServerGroup>> actions() {
                PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(NameTokens.SERVER_GROUP_CONFIGURATION)
                        .with(SERVER_GROUP, item.getName()).build();
                List<ItemAction<ServerGroup>> actions = new ArrayList<>();
                actions.add(itemActionFactory.viewAndMonitor(ServerGroup.id(item.getName()), placeRequest));

                // Order is: reload, restart, suspend, resume, stop, start
                if (item.hasServers(ServerConfigStatus.STARTED)) {
                    actions.add(new ItemAction<>(resources.constants().reload(), serverGroupActions::reload));
                    actions.add(new ItemAction<>(resources.constants().restart(), serverGroupActions::restart));
                }
                if (item.getServers(ServerConfigStatus.STARTED).size() - item.getServers(SuspendState.SUSPENDED)
                        .size() > 0) {
                    actions.add(new ItemAction<>(resources.constants().suspend(), serverGroupActions::suspend));
                }
                if (item.hasServers(SuspendState.SUSPENDED)) {
                    actions.add(new ItemAction<>(resources.constants().resume(), serverGroupActions::resume));
                }
                if (item.hasServers(ServerConfigStatus.STARTED)) {
                    actions.add(new ItemAction<>(resources.constants().stop(), serverGroupActions::stop));
                }
                if (item.hasServers(ServerConfigStatus.STOPPED, ServerConfigStatus.DISABLED,
                        ServerConfigStatus.FAILED)) {
                    actions.add(new ItemAction<>(resources.constants().start(), serverGroupActions::start));
                }
                return actions;
            }
        });
    }
}
