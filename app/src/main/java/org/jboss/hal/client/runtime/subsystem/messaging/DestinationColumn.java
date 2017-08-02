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
package org.jboss.hal.client.runtime.subsystem.messaging;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;

import static org.jboss.hal.client.runtime.subsystem.messaging.AddressTemplates.MESSAGING_DEPLOYMENT_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.messaging.AddressTemplates.MESSAGING_SERVER_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.messaging.Destination.Type.DEPLOYMENT_RESOURCES;
import static org.jboss.hal.client.runtime.subsystem.messaging.Destination.Type.SUBSYSTEM_RESOURCES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.resources.CSS.fontAwesome;

@AsyncColumn(Ids.MESSAGING_SERVER_DESTINATION)
public class DestinationColumn extends FinderColumn<Destination> {

    @Inject
    public DestinationColumn(Finder finder,
            FinderPathFactory finderPathFactory,
            Places places,
            Dispatcher dispatcher,
            StatementContext statementContext,
            Resources resources) {

        super(new Builder<Destination>(finder, Ids.MESSAGING_SERVER_DESTINATION, Names.DESTINATION)
                .itemsProvider((context, callback) -> {
                    List<Operation> operations = new ArrayList<>();
                    for (Destination.Type type : SUBSYSTEM_RESOURCES) {
                        ResourceAddress address = MESSAGING_SERVER_TEMPLATE.append(type.resource + "=*")
                                .resolve(statementContext);
                        operations.add(new Operation.Builder(address, READ_RESOURCE_OPERATION)
                                .param(INCLUDE_RUNTIME, true)
                                .build());
                    }
                    for (Destination.Type type : DEPLOYMENT_RESOURCES) {
                        ResourceAddress address = MESSAGING_DEPLOYMENT_TEMPLATE.append(type.resource + "=*")
                                .resolve(statementContext);
                        operations.add(new Operation.Builder(address, READ_RESOURCE_OPERATION)
                                .param(INCLUDE_RUNTIME, true)
                                .build());
                    }
                    dispatcher.execute(new Composite(operations), (CompositeResult result) -> {
                        List<Destination> destinations = new ArrayList<>();
                        for (ModelNode step : result) {
                            if (!step.isFailure()) {
                                for (ModelNode node : step.get(RESULT).asList()) {
                                    ResourceAddress address = new ResourceAddress(node.get(ADDRESS));
                                    destinations.add(new Destination(address, node.get(RESULT)));
                                }
                            }
                        }
                        destinations.sort(Comparator.comparing(NamedNode::getName));
                        callback.onSuccess(destinations);
                    });
                })
                .itemRenderer(item -> new ItemDisplay<Destination>() {
                    @Override
                    public String getId() {
                        return Ids.destination(item.getDeployment(), item.getSubdeployment(), item.type.name(),
                                item.getName());
                    }

                    @Override
                    public String getTitle() {
                        return item.getName();
                    }

                    @Override
                    public HTMLElement asElement() {
                        if (item.fromDeployment()) {
                            return ItemDisplay.withSubtitle(item.getName(), item.getPath());
                        }
                        return null;
                    }

                    @Override
                    public String getTooltip() {
                        return item.type.type;
                    }

                    @Override
                    public HTMLElement getIcon() {
                        switch (item.type) {
                            case JMS_QUEUE:
                                return Icons.custom(fontAwesome("long-arrow-right"));
                            case JMS_TOPIC:
                                return Icons.custom(fontAwesome("arrows"));
                            case QUEUE:
                                return Icons.custom(fontAwesome("cog"));
                            case UNDEFINED:
                                return Icons.unknown();
                            default:
                                return Icons.unknown();
                        }
                    }

                    @Override
                    public String getFilterData() {
                        return item.getName() + " " + item.type.type +
                                (item.fromDeployment() ? " " + Names.DEPLOYMENT : "");
                    }
                })
                .onPreview(item -> new DestinationPreview(item, finderPathFactory, places, dispatcher, statementContext,
                        resources))
                .pinnable()
                .showCount()
                .withFilter()
        );
    }
}
