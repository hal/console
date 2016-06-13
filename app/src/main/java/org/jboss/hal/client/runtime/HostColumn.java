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
package org.jboss.hal.client.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import com.google.common.base.Joiner;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.core.HostSelectionEvent;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Requires;

import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.jboss.hal.client.runtime.ServerConfigStatus.STARTED;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.itemText;
import static org.jboss.hal.resources.CSS.subtitle;
import static org.jboss.hal.resources.CSS.withProgress;

/**
 * @author Harald Pehl
 */
@Column(HOST)
@Requires(value = "/host=*", recursive = false)
public class HostColumn extends FinderColumn<Host> {

    @Inject
    public HostColumn(final Finder finder,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final StatementContext statementContext,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory,
            final HostActions hostActions,
            final Resources resources) {

        super(new Builder<Host>(finder, HOST, Names.HOSTS)

                .columnAction(columnActionFactory.refresh(IdBuilder.build(HOST, "refresh")))

                .itemsProvider((context, callback) -> {
                    // Read the hosts and the running server in a composite operation.
                    // The running servers are not needed by the column itself, however they're used by the
                    // host actions like reload or restart to wait until the host and all of its running servers
                    // are up and running again.
                    Operation hosts = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION,
                            ResourceAddress.ROOT)
                            .param(CHILD_TYPE, HOST)
                            .param(INCLUDE_RUNTIME, true)
                            .build();
                    Operation runningServers = new Operation.Builder(QUERY,
                            AddressTemplate.of("/host=*/server-config=*").resolve(statementContext))
                            .param(SELECT, new ModelNode().add(NAME))
                            .param(WHERE, new ModelNode().set(STATUS, STARTED.name()))
                            .build();
                    dispatcher.execute(new Composite(hosts, runningServers),
                            (CompositeResult result) -> {

                                // read the hosts from step 0
                                Map<String, Host> hostsByName = result.step(0).get(RESULT).asPropertyList().stream()
                                        .map(Host::new)
                                        .collect(toMap(NamedNode::getName, identity()));

                                /*
                                 * read the addresses of the running servers from step 1, the address looks like
                                 * "address" => [
                                 *     ("host" => "foo"),
                                 *     ("server-config" => "bar")
                                 * ]
                                 */
                                Map<String, List<ResourceAddress>> runningServersByHost = result.step(1).get(RESULT)
                                        .asList().stream()
                                        .map(modelNode -> new ResourceAddress(modelNode.get(ADDRESS)))
                                        .collect(groupingBy(address -> address.getParent().lastValue()));

                                // pick the server name from the address and add it to the host
                                runningServersByHost.forEach((hostName, runningServerAddresses) -> {
                                    Host host = hostsByName.get(hostName);
                                    if (host != null) {
                                        runningServerAddresses
                                                .forEach(address -> host.addRunningServer(address.lastValue()));
                                    }
                                });

                                // return the host instances as ordered list
                                callback.onSuccess(hostsByName.values().stream().sorted(comparing(Host::getName))
                                        .collect(toList()));
                            });
                })

                .onItemSelect(host -> eventBus.fireEvent(new HostSelectionEvent(host.getName())))
                .pinnable()
                .showCount()
                .useFirstActionAsBreadcrumbHandler()
                .withFilter()
        );

        setItemRenderer(item -> new ItemDisplay<Host>() {
            @Override
            public String getId() {
                return Host.id(item.getName());
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
                return Joiner.on(' ').join(item.getName(), item.isDomainController() ? "dc" : "hc"); //NON-NLS
            }

            @Override
            public String getTooltip() {
                if (item.isAdminMode()) {
                    return resources.constants().adminOnly();
                } else if (item.isSuspending()) {
                    return resources.constants().suspending();
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
                if (item.isAdminMode()) {
                    return Icons.disabled();
                } else if (item.isSuspending() || item.needsReload() || item.needsRestart()) {
                    return Icons.warning();
                } else if (item.isRunning()) {
                    return Icons.ok();
                } else {
                    return Icons.error();
                }
            }

            @Override
            public String nextColumn() {
                return SERVER;
            }

            @Override
            public List<ItemAction<Host>> actions() {
                PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(NameTokens.HOST_CONFIGURATION)
                        .with(HOST, item.getName()).build();
                List<ItemAction<Host>> actions = new ArrayList<>();
                actions.add(itemActionFactory.view(placeRequest));
                actions.add(new ItemAction<>(resources.constants().reload(),
                        itm -> hostActions.reload(itm, item.isDomainController(),
                                () -> {
                                    if (!itm.isDomainController()) {
                                        startProgress(itm);
                                    }
                                },
                                () -> {
                                    if (!itm.isDomainController()) {
                                        endProgress(itm);
                                    }
                                    refresh(RESTORE_SELECTION);
                                })));
                actions.add(new ItemAction<>(resources.constants().restart(),
                        itm -> hostActions.restart(itm, item.isDomainController(),
                                () -> {
                                    if (!itm.isDomainController()) {
                                        startProgress(itm);
                                    }
                                },
                                () -> {
                                    if (!itm.isDomainController()) {
                                        endProgress(itm);
                                    }
                                    refresh(RESTORE_SELECTION);
                                })));
                // TODO Add additional operations like :reload(admin-mode=true), :clean-obsolete-content or :take-snapshot
                return actions;
            }
        });

        setPreviewCallback(item -> new HostPreview(this, hostActions, item, resources));
    }

    void startProgress(Host host) {
        Element element = Browser.getDocument().getElementById(Host.id(host.getName()));
        if (element != null) {
            element.getClassList().add(withProgress);
        }
    }

    void endProgress(Host host) {
        Element element = Browser.getDocument().getElementById(Host.id(host.getName()));
        if (element != null) {
            element.getClassList().remove(withProgress);
        }
    }
}
