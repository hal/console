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
package org.jboss.hal.client.runtime.subsystem.resourceadapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderSegment;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.ItemsProvider;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Requires;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import static org.jboss.hal.client.runtime.subsystem.resourceadapter.AddressTemplates.ADMIN_OBJECT_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.resourceadapter.AddressTemplates.CONNECTION_DEFINITION_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.resourceadapter.AddressTemplates.RESOURCE_ADAPTER_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.resourceadapter.StatisticsResource.ResourceType.ADMIN_OBJECT;
import static org.jboss.hal.client.runtime.subsystem.resourceadapter.StatisticsResource.ResourceType.CONNECTION_DEFINITION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADMIN_OBJECTS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONNECTION_DEFINITIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE_DEPTH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.meta.StatementContext.Expression.SELECTED_HOST;
import static org.jboss.hal.meta.StatementContext.Expression.SELECTED_SERVER;

@AsyncColumn(Ids.RESOURCE_ADAPTER_CHILD_RUNTIME)
@Requires({ ADMIN_OBJECT_ADDRESS, CONNECTION_DEFINITION_ADDRESS })
public class ChildResourceColumn extends FinderColumn<StatisticsResource> {

    private Server server;

    @Inject
    public ChildResourceColumn(ServerActions serverActions,
            Dispatcher dispatcher,
            StatementContext statementContext,
            Environment environment,
            Resources resources,
            Finder finder,
            ItemActionFactory itemActionFactory,
            Places places) {

        super(new Builder<StatisticsResource>(finder, Ids.RESOURCE_ADAPTER_CHILD_RUNTIME, Names.RESOURCE_ADAPTER + " Child")
                .withFilter()
                .filterDescription(resources.messages().filterBy("name, type"))
                .useFirstActionAsBreadcrumbHandler());

        ItemsProvider<StatisticsResource> itemsProvider = context -> {
            // extract server name from the finder path
            FinderSegment<?> segment = context.getPath().findColumn(Ids.RESOURCE_ADAPTER_RUNTIME);

            if (segment != null) {
                String raName = segment.getItemTitle();
                List<Operation> operations = new ArrayList<>();

                ResourceAddress resourceAdapterAddress = RESOURCE_ADAPTER_TEMPLATE.resolve(statementContext, raName);
                operations.add(new Operation.Builder(resourceAdapterAddress, READ_CHILDREN_RESOURCES_OPERATION)
                        .param(CHILD_TYPE, ADMIN_OBJECTS)
                        .param(INCLUDE_RUNTIME, true)
                        .param(RECURSIVE_DEPTH, 2)
                        .build());

                operations.add(new Operation.Builder(resourceAdapterAddress, READ_CHILDREN_RESOURCES_OPERATION)
                        .param(CHILD_TYPE, CONNECTION_DEFINITIONS)
                        .param(INCLUDE_RUNTIME, true)
                        .param(RECURSIVE_DEPTH, 2)
                        .build());

                if (!environment.isStandalone()) {
                    ResourceAddress serverAddress = AddressTemplate.of(SELECTED_HOST, SELECTED_SERVER)
                            .resolve(statementContext);
                    operations.add(new Operation.Builder(serverAddress, READ_RESOURCE_OPERATION)
                            .param(INCLUDE_RUNTIME, true)
                            .param(ATTRIBUTES_ONLY, true)
                            .build());
                }
                return dispatcher.execute(new Composite(operations)).then(result -> {
                    server = environment.isStandalone()
                            ? Server.STANDALONE
                            : new Server(statementContext.selectedHost(), result.step(2).get(RESULT));
                    List<StatisticsResource> combined = new ArrayList<>();
                    combined.addAll(result.step(0).get(RESULT).asPropertyList().stream()
                            .map(ao -> new StatisticsResource(raName, ADMIN_OBJECT, ao)).collect(toList()));
                    combined.addAll(result.step(1).get(RESULT).asPropertyList().stream()
                            .map(cd -> new StatisticsResource(raName, CONNECTION_DEFINITION, cd)).collect(toList()));
                    combined.sort(comparing(NamedNode::getName));
                    return Promise.resolve(combined);
                });
            }
            return Promise.resolve(Collections.emptyList());
        };
        setItemsProvider(itemsProvider);

        // reuse the items provider to filter breadcrumb items
        setBreadcrumbItemsProvider(context -> itemsProvider.items(context)
                .then(result -> Promise.resolve(result.stream()
                        .filter(StatisticsResource::isStatisticsEnabled)
                        .collect(toList()))));

        setItemRenderer(raChild -> new ItemDisplay<StatisticsResource>() {
            @Override
            public String getId() {
                return Ids.resourceAdapterChildRuntime(raChild.getParentName(), raChild.getName());
            }

            @Override
            public HTMLElement element() {
                String subtitle = raChild.getResourceType().name().toLowerCase().replace('_', '-');
                return ItemDisplay.withSubtitle(raChild.getName(), subtitle);
            }

            @Override
            public String getTitle() {
                return raChild.getName();
            }

            @Override
            public HTMLElement getIcon() {
                return raChild.isStatisticsEnabled() ? Icons.ok() : Icons.unknown();
            }

            @Override
            public String getTooltip() {
                return raChild.isStatisticsEnabled() ? resources.constants().enabled()
                        : resources.constants().statisticsDisabled();
            }

            @Override
            public String getFilterData() {
                // noinspection HardCodedStringLiteral
                return getTitle() + " " + raChild.getResourceType().name().toLowerCase().replace('_', '-');
            }

            @Override
            @SuppressWarnings("HardCodedStringLiteral")
            public List<ItemAction<StatisticsResource>> actions() {
                List<ItemAction<StatisticsResource>> actions = new ArrayList<>();

                PlaceRequest placeRequest = places.selectedServer(NameTokens.RESOURCE_ADAPTER_CHILD_RUNTIME)
                        .with("parent", raChild.getParentName())
                        .with(NAME, raChild.getName())
                        .with(TYPE, raChild.getResourceType().name())
                        .build();
                actions.add(itemActionFactory.view(placeRequest));

                return actions;
            }
        });

        setPreviewCallback(child -> new ChildResourcePreview(server, child, environment, dispatcher, statementContext,
                serverActions, resources));
    }

}
