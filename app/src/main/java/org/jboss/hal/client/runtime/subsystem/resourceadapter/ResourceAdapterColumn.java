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
import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.ItemsProvider;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.stream.Collectors.toList;

import static org.jboss.hal.client.runtime.subsystem.resourceadapter.AddressTemplates.*;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESOURCE_ADAPTER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;
import static org.jboss.hal.meta.StatementContext.Expression.SELECTED_HOST;
import static org.jboss.hal.meta.StatementContext.Expression.SELECTED_SERVER;

@AsyncColumn(Ids.RESOURCE_ADAPTER_RUNTIME)
@Requires({ RESOURCE_ADAPTER_ADDRESS })
public class ResourceAdapterColumn extends FinderColumn<StatisticsResource> {

    private final Dispatcher dispatcher;
    private final EventBus eventBus;
    private final StatementContext statementContext;
    private final Environment environment;
    private final Resources resources;
    private final Finder finder;
    private Server server;

    @Inject
    public ResourceAdapterColumn(Dispatcher dispatcher,
            EventBus eventBus,
            StatementContext statementContext,
            Environment environment,
            Resources resources,
            Finder finder,
            ItemActionFactory itemActionFactory,
            Places places) {

        super(new Builder<StatisticsResource>(finder, Ids.RESOURCE_ADAPTER_RUNTIME, Names.RESOURCE_ADAPTER)
                .withFilter()
                .useFirstActionAsBreadcrumbHandler());

        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.statementContext = statementContext;
        this.environment = environment;
        this.resources = resources;
        this.finder = finder;

        ItemsProvider<StatisticsResource> itemsProvider = context -> {
            List<Operation> operations = new ArrayList<>();

            ResourceAddress raSubsystemAddress = RESOURCE_ADAPTER_SUBSYSTEM_TEMPLATE.resolve(statementContext);
            operations.add(new Operation.Builder(raSubsystemAddress, READ_CHILDREN_RESOURCES_OPERATION)
                    .param(CHILD_TYPE, RESOURCE_ADAPTER)
                    .param(INCLUDE_RUNTIME, true)
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
                        : new Server(statementContext.selectedHost(), result.step(1).get(RESULT));
                return Promise.resolve(result.step(0).get(RESULT).asPropertyList().stream()
                        .map(StatisticsResource::new)
                        .collect(toList()));
            });
        };
        setItemsProvider(itemsProvider);

        // reuse the items provider to filter breadcrumb items
        setBreadcrumbItemsProvider(context -> itemsProvider.items(context)
                .then(result -> Promise.resolve(result.stream()
                        .filter(ra -> ra.get(STATISTICS_ENABLED).asBoolean())
                        .collect(toList()))));

        setItemRenderer(ra -> new ItemDisplay<StatisticsResource>() {
            @Override
            public String getId() {
                return Ids.resourceAdapterRuntime(ra.getName());
            }

            @Override
            public String getTitle() {
                return ra.getName();
            }

            @Override
            public HTMLElement getIcon() {
                if (!ra.get(STATISTICS_ENABLED).asBoolean()) {
                    return Icons.unknown();
                } else {
                    return Icons.ok();
                }
            }

            @Override
            public String getTooltip() {
                if (!ra.get(STATISTICS_ENABLED).asBoolean()) {
                    return resources.constants().statisticsDisabled();
                } else {
                    return resources.constants().enabled();
                }
            }

            @Override
            public String getFilterData() {
                return getTitle();
            }

            @Override
            public String nextColumn() {
                return Ids.RESOURCE_ADAPTER_CHILD_RUNTIME;
            }

            @Override
            @SuppressWarnings("HardCodedStringLiteral")
            public List<ItemAction<StatisticsResource>> actions() {
                List<ItemAction<StatisticsResource>> actions = new ArrayList<>();

                PlaceRequest placeRequest = places.selectedServer(NameTokens.RESOURCE_ADAPTER_RUNTIME)
                        .with(NAME, ra.getName())
                        .with(TYPE, ra.getResourceType().name())
                        .build();
                actions.add(itemActionFactory.view(placeRequest));
                actions.add(new ItemAction.Builder<StatisticsResource>().title("Activate")
                        .handler(item -> activate(item))
                        .constraint(Constraint.executable(RESOURCE_ADAPTER_TEMPLATE, "activate"))
                        .build());

                return actions;
            }
        });

        setPreviewCallback(item -> new ResourceAdapterPreview(this, item, resources));
    }

    private void activate(StatisticsResource resourceAdapter) {
        Operation operation = new Operation.Builder(
                RESOURCE_ADAPTER_TEMPLATE.resolve(statementContext, resourceAdapter.getName()), "activate").build();
        dispatcher.execute(operation,
                result -> {
                    refresh(RESTORE_SELECTION);
                    MessageEvent.fire(eventBus, Message.success(resources.messages().activationSuccess()));
                });
    }

    private ResourceAddress resourceAdapterConfigurationAddress(StatisticsResource resourceAdapter) {
        if (environment.isStandalone()) {
            return AddressTemplate.of(RESOURCE_ADAPTER_CONFIGURATION_ADDRESS).resolve(statementContext,
                    resourceAdapter.getName());
        } else {
            String profile = server.get(PROFILE_NAME).asString();
            return AddressTemplate.of("/profile=*" + RESOURCE_ADAPTER_CONFIGURATION_ADDRESS)
                    .resolve(statementContext, profile, resourceAdapter.getName());
        }
    }

    void enableStatistics(StatisticsResource resourceAdapter) {

        ResourceAddress address = resourceAdapterConfigurationAddress(resourceAdapter);
        Operation operation = new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION).param(NAME, STATISTICS_ENABLED)
                .param(VALUE, true).build();
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(eventBus,
                    Message.success(resources.messages().statisticsEnabled(resourceAdapter.getName())));
            finder.refresh();
        });
    }
}
