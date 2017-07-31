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
import java.util.List;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import elemental2.dom.HTMLElement;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.host.HostActions;
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
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Claudio Miranda
 */
@Column(Ids.PATCHING_DOMAIN)
@Requires(value = "/host=*/core-service=patching")
public class HostPatchesColumn extends FinderColumn<NamedNode> {

    @Inject
    public HostPatchesColumn(final Finder finder,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final ColumnActionFactory columnActionFactory,
            final HostActions hostActions,
            final Resources resources) {

        super(new FinderColumn.Builder<NamedNode>(finder, Ids.PATCHING_DOMAIN, Names.HOSTS)

                .columnAction(columnActionFactory.refresh(Ids.HOST_REFRESH))

                .itemsProvider((context, callback) -> {

                    ResourceAddress hostAddress = new ResourceAddress()
                            .add(HOST, "*");

                    ResourceAddress patchingAddress = new ResourceAddress()
                            .add(HOST, "*")
                            .add(CORE_SERVICE, PATCHING);

                    Operation opHosts = new Operation.Builder(hostAddress, READ_RESOURCE_OPERATION)
                            .param(INCLUDE_RUNTIME, true)
                            .build();

                    Operation opPatches = new Operation.Builder(patchingAddress, READ_RESOURCE_OPERATION)
                            .param(INCLUDE_RUNTIME, true)
                            .param(RECURSIVE, true)
                            .build();
                    Composite composite = new Composite(opHosts, opPatches);

                    dispatcher.execute(composite, (CompositeResult result) -> {

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
                                    hostPatchingResults.forEach(hostPatchingResult -> {
                                        hostPatchingResult.get(ADDRESS).asPropertyList().forEach(pp -> {
                                            if (HOST.equals(pp.getName()) && pp.getValue().asString()
                                                    .equals(hostName)) {
                                                // add the core-service=patching attributes to a sub-resource
                                                // this exists only in memory for HAL purposes to show them in the view
                                                hostNode.get(RESULT).get(CORE_SERVICE_PATCHING)
                                                        .set(hostPatchingResult.get(RESULT));
                                            }
                                        });
                                    });
                                    hostPatchesNode.get(hostName).set(hostNode.get(RESULT));
                                }
                            });
                            NamedNode ac = new NamedNode(hostPatchesNode.asProperty());
                            hostPatches.add(ac);

                        });
                        callback.onSuccess(hostPatches);
                    });

                })


                .onItemSelect(host -> eventBus.fireEvent(new HostSelectionEvent(host.getName())))
                .onPreview(item -> new HostPatchesPreview(hostActions, item, resources))
                .pinnable()
                .showCount()
                .useFirstActionAsBreadcrumbHandler()
                .withFilter()
        );

        setItemRenderer(item -> new ItemDisplay<NamedNode>() {

            Host _host = new Host(new Property(item.getName(), item.asModelNode()));

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
                return Ids.PATCHES_HOST;
            }

        });

    }
}
