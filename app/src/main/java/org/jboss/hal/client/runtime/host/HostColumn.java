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
package org.jboss.hal.client.runtime.host;

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
import org.jboss.hal.core.finder.ItemMonitor;
import org.jboss.hal.core.runtime.TopologyFunctions;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.host.HostActionEvent;
import org.jboss.hal.core.runtime.host.HostActionEvent.HostActionHandler;
import org.jboss.hal.core.runtime.host.HostActions;
import org.jboss.hal.core.runtime.host.HostResultEvent;
import org.jboss.hal.core.runtime.host.HostResultEvent.HostResultHandler;
import org.jboss.hal.core.runtime.host.HostSelectionEvent;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.resources.CSS.itemText;
import static org.jboss.hal.resources.CSS.subtitle;

/**
 * @author Harald Pehl
 */
@Column(HOST)
@Requires(value = "/host=*", recursive = false)
public class HostColumn extends FinderColumn<Host> implements HostActionHandler, HostResultHandler {

    @Inject
    public HostColumn(final Finder finder,
            final Environment environment,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final @Footer Provider<Progress> progress,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory,
            final HostActions hostActions,
            final Resources resources) {

        super(new Builder<Host>(finder, HOST, Names.HOST)

                .columnAction(columnActionFactory.refresh(IdBuilder.build(HOST, "refresh")))

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
                                        List<Host> hosts = context.get(TopologyFunctions.HOSTS);
                                        callback.onSuccess(hosts);

                                        // Restore pending visualization
                                        hosts.stream()
                                                .filter(hostActions::isPending)
                                                .forEach(host -> ItemMonitor.startProgress(Host.id(host)));
                                    }
                                },
                                new TopologyFunctions.HostsWithServerConfigs(environment, dispatcher),
                                new TopologyFunctions.HostsStartedServers(environment, dispatcher)))

                // TODO Change the security context (host scoped roles!)
                .onItemSelect(host -> eventBus.fireEvent(new HostSelectionEvent(host.getAddressName())))
                .onPreview(item -> new HostPreview(hostActions, item, resources))
                .pinnable()
                .showCount()
                .useFirstActionAsBreadcrumbHandler()
                .withFilter()
        );

        setItemRenderer(item -> new ItemDisplay<Host>() {
            @Override
            public String getId() {
                return Host.id(item);
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public Element asElement() {
                if (item.isDomainController()) {
                    return new Elements.Builder()
                            .span().css(itemText)
                            .span().textContent(item.getName()).end()
                            .start("small").css(subtitle).textContent(Names.DOMAIN_CONTROLLER).end()
                            .end().build();
                }
                return null;
            }

            @Override
            public String getFilterData() {
                return Joiner.on(' ').join(item.getName(),
                        item.isDomainController() ? "dc" : "hc", //NON-NLS
                        ModelNodeHelper.asAttributeValue(item.getHostState()));
            }

            @Override
            public String getTooltip() {
                if (hostActions.isPending(item)) {
                    return resources.constants().pending();
                } else if (item.isAdminMode()) {
                    return resources.constants().adminOnly();
                } else if (item.isStarting()) {
                    return resources.constants().starting();
                } else if (item.needsReload()) {
                    return resources.constants().needsReload();
                } else if (item.needsRestart()) {
                    return resources.constants().needsRestart();
                } else if (item.isRunning()) {
                    return resources.constants().running();
                } else {
                    return resources.constants().unknownState();
                }
            }

            @Override
            public Element getIcon() {
                if (hostActions.isPending(item)) {
                    return Icons.unknown();
                } else if (item.isAdminMode() || item.isStarting()) {
                    return Icons.disabled();
                } else if (item.needsReload() || item.needsRestart()) {
                    return Icons.warning();
                } else if (item.isRunning()) {
                    return Icons.ok();
                } else {
                    return Icons.unknown();
                }
            }

            @Override
            public String nextColumn() {
                return SERVER;
            }

            @Override
            public List<ItemAction<Host>> actions() {
                PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(NameTokens.HOST_CONFIGURATION)
                        .with(HOST, item.getAddressName()).build();
                List<ItemAction<Host>> actions = new ArrayList<>();
                actions.add(itemActionFactory.viewAndMonitor(Host.id(item), placeRequest));
                if (!hostActions.isPending(item)) {
                    actions.add(new ItemAction<>(resources.constants().reload(), hostActions::reload));
                    actions.add(new ItemAction<>(resources.constants().restart(), hostActions::restart));
                    // TODO Add additional operations like :reload(admin-mode=true), :clean-obsolete-content or :take-snapshot
                }
                return actions;
            }
        });

        eventBus.addHandler(HostActionEvent.getType(), this);
        eventBus.addHandler(HostResultEvent.getType(), this);
    }

    @Override
    public void onHostAction(final HostActionEvent event) {
        if (isVisible()) {
            Host host = event.getHost();
            ItemMonitor.startProgress(Host.id(host));
            event.getServers().forEach(server -> ItemMonitor.startProgress(Server.id(server.getName())));
        }
    }

    @Override
    public void onHostResult(final HostResultEvent event) {
        if (isVisible()) {
            Host host = event.getHost();
            ItemMonitor.stopProgress(Host.id(host));
            event.getServers().forEach(server -> ItemMonitor.stopProgress(Server.id(server.getName())));
            refresh(RESTORE_SELECTION);
        }
    }
}
