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

import elemental.dom.Element;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.core.runtime.server.StandaloneServer;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;

import static java.util.Collections.singletonList;

/**
 * @author Harald Pehl
 */
@Column(Ids.STANDALONE_SERVER)
public class StandaloneServerColumn extends FinderColumn<Server> {

    @Inject
    public StandaloneServerColumn(final Finder finder, final ServerActions serverActions,
            final Resources resources) {
        super(new Builder<Server>(finder, Ids.STANDALONE_SERVER, Names.SERVER)

                .itemsProvider((context, callback) -> callback.onSuccess(singletonList(StandaloneServer.INSTANCE)))

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

                .onPreview(item -> new ServerPreview(serverActions, item, resources))
        );
    }
}
