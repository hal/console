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

import com.google.web.bindery.event.shared.EventBus;
import elemental.dom.Element;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.ItemMonitor;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActionEvent;
import org.jboss.hal.core.runtime.server.ServerActionEvent.ServerActionHandler;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.core.runtime.server.ServerResultEvent;
import org.jboss.hal.core.runtime.server.ServerResultEvent.ServerResultHandler;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;

import static java.util.Collections.singletonList;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

/**
 * @author Harald Pehl
 */
@Column(Ids.STANDALONE_SERVER)
public class StandaloneServerColumn extends FinderColumn<Server> implements ServerActionHandler, ServerResultHandler {

    private final Finder finder;
    private FinderPath refreshPath;

    @Inject
    public StandaloneServerColumn(final Finder finder, final EventBus eventBus, final Dispatcher dispatcher,
            final ServerActions serverActions, final Places places, final Resources resources) {
        super(new Builder<Server>(finder, Ids.STANDALONE_SERVER, Names.SERVER)

                .itemsProvider((context, callback) -> {
                    Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION, ResourceAddress.root())
                            .param(INCLUDE_RUNTIME, true)
                            .param(ATTRIBUTES_ONLY, true)
                            .build();
                    dispatcher.execute(operation, result -> {
                        Server.STANDALONE.addServerAttributes(result);
                        callback.onSuccess(singletonList(Server.STANDALONE));

                        // Restore pending servers visualization
                        if (serverActions.isPending(Server.STANDALONE)) {
                            ItemMonitor.startProgress(Ids.server(Server.STANDALONE.getName()));
                        }
                    });
                })

                .itemRenderer(item -> new ItemDisplay<Server>() {
                    @Override
                    public String getId() {
                        return Ids.server(item.getName());
                    }

                    @Override
                    public String getTitle() {
                        return item.getName();
                    }

                    @Override
                    public String getTooltip() {
                        if (serverActions.isPending(item)) {
                            return resources.constants().pending();
                        } else if (item.isAdminMode()) {
                            return resources.constants().adminOnly();
                        } else if (item.isStarting()) {
                            return resources.constants().starting();
                        } else if (item.isSuspended()) {
                            return resources.constants().suspended();
                        } else if (item.needsReload()) {
                            return resources.constants().needsReload();
                        } else if (item.needsRestart()) {
                            return resources.constants().needsRestart();
                        } else if (item.isRunning()) {
                            return resources.constants().running();
                        } else if (item.isFailed()) {
                            return resources.constants().failed();
                        } else {
                            return resources.constants().unknownState();
                        }
                    }

                    @Override
                    public Element getIcon() {
                        if (serverActions.isPending(item)) {
                            return Icons.unknown();
                        } else if (item.isAdminMode() || item.isStarting()) {
                            return Icons.disabled();
                        } else if (item.isSuspended()) {
                            return Icons.pause();
                        } else if (item.needsReload() || item.needsRestart()) {
                            return Icons.warning();
                        } else if (item.isRunning()) {
                            return Icons.ok();
                        } else if (item.isFailed()) {
                            return Icons.error();
                        } else {
                            return Icons.unknown();
                        }
                    }

                    @Override
                    public List<ItemAction<Server>> actions() {
                        List<ItemAction<Server>> actions = new ArrayList<>();
                        if (!serverActions.isPending(item)) {
                            // Order is: reload, restart, (resume | suspend)
                            actions.add(new ItemAction<>(resources.constants().reload(), serverActions::reload));
                            actions.add(new ItemAction<>(resources.constants().restart(), serverActions::restart));
                            if (item.isSuspended()) {
                                actions.add(new ItemAction<>(resources.constants().resume(), serverActions::resume));
                            } else {
                                actions.add(new ItemAction<>(resources.constants().suspend(), serverActions::suspend));
                            }
                        }
                        return actions;
                    }

                    @Override
                    public String nextColumn() {
                        return Ids.SERVER_MONITOR;
                    }
                })

                .onPreview(item -> new ServerPreview(serverActions, item, places, resources))
        );

        this.finder = finder;
        eventBus.addHandler(ServerActionEvent.getType(), this);
        eventBus.addHandler(ServerResultEvent.getType(), this);
    }

    @Override
    public void onServerAction(final ServerActionEvent event) {
        if (isVisible()) {
            refreshPath = finder.getContext().getPath().copy();
            ItemMonitor.startProgress(Ids.server(event.getServer().getName()));
            refresh(RESTORE_SELECTION);
        }
    }

    @Override
    public void onServerResult(final ServerResultEvent event) {
        //noinspection Duplicates
        if (isVisible()) {
            Server server = event.getServer();
            String itemId = Ids.server(server.getName());
            ItemMonitor.stopProgress(itemId);

            FinderPath path = refreshPath != null ? refreshPath : finder.getContext().getPath();
            refreshPath = null;
            finder.refresh(path);
        }
    }
}
