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
package org.jboss.hal.client.patching;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import elemental2.dom.HTMLElement;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.ItemMonitor;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.host.HostActionEvent;
import org.jboss.hal.core.runtime.host.HostActions;
import org.jboss.hal.core.runtime.host.HostResultEvent;
import org.jboss.hal.core.runtime.host.HostSelectionEvent;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Outcome;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Requires;
import rx.Completable;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.flow.Flow.series;

@Column(Ids.PATCHING_DOMAIN)
@Requires(value = "/host=*/core-service=patching")
public class HostPatchesColumn extends FinderColumn<NamedNode> implements HostActionEvent.HostActionHandler,
        HostResultEvent.HostResultHandler {

    public static class AvailableHosts implements Task<FlowContext> {

        private Dispatcher dispatcher;

        AvailableHosts(Dispatcher dispatcher) {
            this.dispatcher = dispatcher;
        }

        @Override
        public Completable call(FlowContext context) {
            ResourceAddress hostAddress = new ResourceAddress()
                    .add(HOST, "*");
            Operation opHosts = new Operation.Builder(hostAddress, READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .build();

            ResourceAddress patchingAddress = new ResourceAddress()
                    .add(HOST, "*")
                    .add(CORE_SERVICE, PATCHING);
            Operation opPatches = new Operation.Builder(patchingAddress, READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .param(RECURSIVE, true)
                    .build();

            return dispatcher.execute(new Composite(opHosts, opPatches)).doOnSuccess((CompositeResult result) -> {
                ModelNode availableHosts = result.step(0).get(RESULT);
                List<ModelNode> hostPatchingResults = result.step(1).get(RESULT).asList();

                // this namednode list stores all hosts (and its attributes) and core-service=patching attributes
                // the tweaked hostPatches is necessary as it is important to show hosts and core-service=patching information.
                List<NamedNode> hostPatches = new ArrayList<>();
                availableHosts.asList().forEach(hostNode -> {
                    ModelNode hostPatchesNode = new ModelNode();
                    hostNode.get(ADDRESS).asPropertyList().forEach(p -> {
                        if (HOST.equals(p.getName())) {
                            String hostName = p.getValue().asString();

                            // as we navigate on each host, retrieve the patching attributes from hostPatchingResults
                            hostPatchingResults.forEach(hostPatchingResult -> hostPatchingResult.get(ADDRESS)
                                    .asPropertyList()
                                    .forEach(pp -> {
                                        if (HOST.equals(pp.getName()) && pp.getValue().asString().equals(hostName)) {
                                            // add the core-service=patching attributes to a sub-resource
                                            // this exists only in memory for HAL purposes to show them in the view
                                            hostNode.get(RESULT).get(CORE_SERVICE_PATCHING)
                                                    .set(hostPatchingResult.get(RESULT));
                                        }
                                    }));
                            hostPatchesNode.get(hostName).set(hostNode.get(RESULT));
                        }
                    });
                    NamedNode ac = new NamedNode(hostPatchesNode.asProperty());
                    hostPatches.add(ac);
                });
                List<NamedNode> sortedHosts = orderedHostWithDomainControllerAsFirstElement(hostPatches);
                context.set(HOSTS, sortedHosts);
            }).toCompletable();
        }
    }

    static Host namedNodeToHost(NamedNode node) {
        return new Host(new Property(node.getName(), node.asModelNode()));
    }

    static AddressTemplate hostTemplate(NamedNode node) {
        return AddressTemplate.of("/host=" + node.getName());
    }


    @Inject
    public HostPatchesColumn(Finder finder,
            Dispatcher dispatcher,
            EventBus eventBus,
            @Footer Provider<Progress> progress,
            ColumnActionFactory columnActionFactory,
            HostActions hostActions,
            Resources resources) {

        super(new FinderColumn.Builder<NamedNode>(finder, Ids.PATCHING_DOMAIN, Names.HOSTS)

                .columnAction(columnActionFactory.refresh(Ids.HOST_REFRESH))

                .itemsProvider((context, callback) -> series(new FlowContext(progress.get()),
                        new AvailableHosts(dispatcher))
                        .subscribe(new Outcome<FlowContext>() {
                            @Override
                            public void onError(FlowContext context, Throwable error) {
                                callback.onFailure(error);
                            }

                            @Override
                            public void onSuccess(FlowContext context) {
                                List<NamedNode> hosts = context.get(HOSTS);
                                callback.onSuccess(hosts);

                                // Restore pending visualization
                                hosts.stream()
                                        .filter(item -> hostActions.isPending(namedNodeToHost(item)))
                                        .forEach(item -> ItemMonitor.startProgress(Ids.host(item.getName())));
                            }
                        }))

                .onItemSelect(host -> eventBus.fireEvent(new HostSelectionEvent(host.getName())))
                .onPreview(item -> new HostPatchesPreview(hostActions, item, resources))
                .pinnable()
                .showCount()
                .useFirstActionAsBreadcrumbHandler()
                .withFilter()
        );

        setItemRenderer((NamedNode item) -> new ItemDisplay<NamedNode>() {

            Host _host = namedNodeToHost(item);

            @Override
            public String getId() {
                return Ids.host(_host.getAddressName());
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public HTMLElement asElement() {
                return _host.isDomainController() ? ItemDisplay
                        .withSubtitle(item.getName(), Names.DOMAIN_CONTROLLER) : null;
            }

            @Override
            public String getFilterData() {
                return String.join(" ", item.getName(),
                        _host.isDomainController() ? "dc" : "hc", //NON-NLS
                        ModelNodeHelper.asAttributeValue(_host.getHostState()));
            }

            @Override
            public String getTooltip() {
                if (hostActions.isPending(_host)) {
                    return resources.constants().pending();
                } else if (_host.isAdminMode()) {
                    return resources.constants().adminOnly();
                } else if (_host.isStarting()) {
                    return resources.constants().starting();
                } else if (_host.needsReload()) {
                    return resources.constants().needsReload();
                } else if (_host.needsRestart()) {
                    return resources.constants().needsRestart();
                } else if (_host.isRunning()) {
                    return resources.constants().running();
                } else {
                    return resources.constants().unknownState();
                }
            }

            @Override
            public HTMLElement getIcon() {
                if (hostActions.isPending(_host)) {
                    return Icons.unknown();
                } else if (_host.isAdminMode() || _host.isStarting()) {
                    return Icons.disabled();
                } else if (_host.needsReload() || _host.needsRestart()) {
                    return Icons.warning();
                } else if (_host.isRunning()) {
                    return Icons.ok();
                } else {
                    return Icons.unknown();
                }
            }

            @Override
            public String nextColumn() {
                return Ids.PATCHING;
            }

            @Override
            public List<ItemAction<NamedNode>> actions() {
                List<ItemAction<NamedNode>> actions = new ArrayList<>();
                if (!hostActions.isPending(_host)) {
                    actions.add(new ItemAction.Builder<NamedNode>()
                            .title(resources.constants().restart())
                            .handler(item1 -> hostActions.restart(_host))
                            .constraint(Constraint.executable(hostTemplate(item), SHUTDOWN))
                            .build());
                }
                return actions;
            }

        });

        eventBus.addHandler(HostActionEvent.getType(), this);
        eventBus.addHandler(HostResultEvent.getType(), this);
    }

    private static List<NamedNode> orderedHostWithDomainControllerAsFirstElement(List<NamedNode> hostNodes) {
        // first collect all hosts, sort them by name and finally
        // remove the domain controller to add it as first element
        List<NamedNode> hosts = hostNodes.stream()
                .sorted(comparing(NamedNode::getName))
                .collect(toList());
        NamedNode domainController = null;
        for (Iterator<NamedNode> iterator = hosts.iterator(); iterator.hasNext() && domainController == null; ) {
            NamedNode host = iterator.next();
            if (host.get(MASTER).asBoolean()) {
                domainController = host;
                iterator.remove();
            }
        }
        if (domainController != null) {
            hosts.add(0, domainController);
        }
        return hosts;
    }


    @Override
    public void onHostAction(final HostActionEvent event) {
        if (isVisible()) {
            Host host = event.getHost();
            ItemMonitor.startProgress(Ids.host(host.getAddressName()));
            event.getServers().forEach(server -> ItemMonitor.startProgress(server.getId()));
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public void onHostResult(final HostResultEvent event) {
        if (isVisible()) {
            Host host = event.getHost();
            ItemMonitor.stopProgress(Ids.host(host.getAddressName()));
            event.getServers().forEach(server -> ItemMonitor.stopProgress(server.getId()));
            refresh(RESTORE_SELECTION);
        }
    }
}
