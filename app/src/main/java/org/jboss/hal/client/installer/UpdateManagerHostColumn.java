/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.installer;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.hal.client.runtime.host.HostDisplay;
import org.jboss.hal.client.runtime.host.HostPreview;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemMonitor;
import org.jboss.hal.core.runtime.TopologyTasks;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.host.HostActionEvent;
import org.jboss.hal.core.runtime.host.HostActions;
import org.jboss.hal.core.runtime.host.HostResultEvent;
import org.jboss.hal.core.runtime.host.HostSelectionEvent;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Footer;

import com.google.web.bindery.event.shared.EventBus;

import elemental2.promise.Promise;

import static java.util.stream.Collectors.toList;

import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CORE_SERVICE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INSTALLER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATCHING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SHUTDOWN;
import static org.jboss.hal.flow.Flow.sequential;

@Column(Ids.UPDATE_MANAGER_DOMAIN)
public class UpdateManagerHostColumn extends FinderColumn<Host> implements HostActionEvent.HostActionHandler,
        HostResultEvent.HostResultHandler {

    static Host namedNodeToHost(NamedNode node) {
        return new Host(new Property(node.getName(), node.asModelNode()));
    }

    static AddressTemplate hostTemplate(NamedNode node) {
        return AddressTemplate.of("/host=" + node.getName());
    }

    @Inject
    public UpdateManagerHostColumn(Finder finder,
            Dispatcher dispatcher,
            Environment environment,
            EventBus eventBus,
            @Footer Provider<Progress> progress,
            ColumnActionFactory columnActionFactory,
            HostActions hostActions,
            Resources resources) {

        super(new FinderColumn.Builder<Host>(finder, Ids.UPDATE_MANAGER_DOMAIN, Names.HOSTS)
                .columnAction(columnActionFactory.refresh(Ids.HOST_REFRESH))
                .itemsProvider(finderContext -> sequential(new FlowContext(progress.get()),
                        TopologyTasks.hosts(environment, dispatcher))
                        .then(flowContext -> {
                            List<Host> hosts = flowContext.get(TopologyTasks.HOSTS);
                            List<Host> alive = hosts.stream()
                                    // alive is not enough here!
                                    .filter(host -> host.isAlive() && !host.isStarting() && host.isRunning())
                                    .collect(toList());

                            // Restore pending visualization
                            hosts.stream()
                                    .filter(item -> hostActions.isPending(namedNodeToHost(item)))
                                    .forEach(item -> ItemMonitor.startProgress(
                                            Ids.host(item.getName())));
                            return Promise.resolve(alive);
                        }))

                .onItemSelect(host -> eventBus.fireEvent(new HostSelectionEvent(host.getName())))
                .onPreview(item -> new HostPreview(dispatcher, hostActions, item, resources))
                .pinnable()
                .showCount()
                .useFirstActionAsBreadcrumbHandler()
                .withFilter()
                .filterDescription(resources.messages().hostPatchesColumnFilterDescription()));

        setItemRenderer(item -> new HostDisplay(item, hostActions, resources) {
            @Override
            public List<ItemAction<Host>> actions() {
                List<ItemAction<Host>> actions = new ArrayList<>();
                if (!hostActions.isPending(item)) {
                    actions.add(new ItemAction.Builder<Host>()
                            .title(resources.constants().restart())
                            .handler(item1 -> hostActions.restart(item))
                            .constraint(Constraint.executable(hostTemplate(item), SHUTDOWN))
                            .build());
                }
                return actions;
            }

            @Override
            public String nextColumn() {
                if (item.get(CORE_SERVICE).keys().contains(PATCHING)) {
                    return Ids.PATCHING;
                } else if (item.get(CORE_SERVICE).keys().contains(INSTALLER)) {
                    return Ids.UPDATE_MANAGER;
                } else {
                    return null;
                }
            }
        });

        eventBus.addHandler(HostActionEvent.getType(), this);
        eventBus.addHandler(HostResultEvent.getType(), this);
    }

    @Override
    public void onHostAction(HostActionEvent event) {
        if (isVisible()) {
            Host host = event.getHost();
            ItemMonitor.startProgress(Ids.host(host.getAddressName()));
            event.getServers().forEach(server -> ItemMonitor.startProgress(server.getId()));
        }
    }

    @Override
    public void onHostResult(HostResultEvent event) {
        if (isVisible()) {
            Host host = event.getHost();
            ItemMonitor.stopProgress(Ids.host(host.getAddressName()));
            event.getServers().forEach(server -> ItemMonitor.stopProgress(server.getId()));
            refresh(RESTORE_SELECTION);
        }
    }
}
