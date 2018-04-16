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
package org.jboss.hal.client.runtime.subsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental2.dom.HTMLElement;
import org.jboss.hal.client.runtime.server.ServerRuntimePreview;
import org.jboss.hal.client.runtime.subsystem.batch.BatchPreview;
import org.jboss.hal.client.runtime.subsystem.ejb.ThreadPoolPreview;
import org.jboss.hal.client.runtime.subsystem.transaction.TransactionsPreview;
import org.jboss.hal.client.runtime.subsystem.undertow.UndertowPreview;
import org.jboss.hal.client.runtime.subsystem.webservice.WebservicesPreview;
import org.jboss.hal.config.Version;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.ItemsProvider;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.subsystem.SubsystemMetadata;
import org.jboss.hal.core.subsystem.Subsystems;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.ManagementModel;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;

import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_HOST;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_SERVER;

@AsyncColumn(Ids.RUNTIME_SUBSYSTEM)
public class SubsystemColumn extends FinderColumn<SubsystemMetadata> {

    private final Map<String, PreviewContent<SubsystemMetadata>> customPreviews;

    @Inject
    public SubsystemColumn(Finder finder,
            Dispatcher dispatcher,
            Places places,
            StatementContext statementContext,
            ItemActionFactory itemActionFactory,
            Subsystems subsystems,
            Resources resources) {
        super(new Builder<SubsystemMetadata>(finder, Ids.RUNTIME_SUBSYSTEM, resources.constants().monitor())
                .useFirstActionAsBreadcrumbHandler());

        customPreviews = new HashMap<>();
        customPreviews.put(Ids.SERVER_RUNTIME_STATUS,
                new ServerRuntimePreview(dispatcher, statementContext, resources));
        customPreviews.put(BATCH_JBERET, new BatchPreview(dispatcher, statementContext, resources));
        customPreviews.put(EJB3, new ThreadPoolPreview(dispatcher, statementContext, resources));
        customPreviews.put(TRANSACTIONS, new TransactionsPreview(dispatcher, statementContext, resources));
        customPreviews.put(UNDERTOW, new UndertowPreview(dispatcher, statementContext, resources));
        customPreviews.put(WEBSERVICES, new WebservicesPreview(dispatcher, statementContext, resources));

        ItemsProvider<SubsystemMetadata> itemsProvider = (context, callback) -> {
            ResourceAddress address = AddressTemplate.of(SELECTED_HOST, SELECTED_SERVER)
                    .resolve(statementContext);
            Operation subsystemsOp = new Operation.Builder(address, READ_CHILDREN_NAMES_OPERATION)
                    .param(CHILD_TYPE, SUBSYSTEM)
                    .build();
            Operation versionOp = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                    .param(ATTRIBUTES_ONLY, true)
                    .build();
            dispatcher.execute(new Composite(subsystemsOp, versionOp), (CompositeResult result) -> {
                Map<String, SubsystemMetadata> existingSubsystems = result.step(0).get(RESULT).asList().stream()
                        .map(ModelNode::asString)
                        .filter(subsystems::containsRuntime)
                        .map(subsystems::getRuntime)
                        .collect(toMap(SubsystemMetadata::getName, identity()));

                // remove logging if not supported
                Version serverVersion = ManagementModel.parseVersion(result.step(1).get(RESULT));
                if (!ManagementModel.supportsListLogFiles(serverVersion)) {
                    existingSubsystems.remove(LOGGING);
                }

                // add server runtime as first item
                List<SubsystemMetadata> items = new ArrayList<>();
                items.add(new SubsystemMetadata.Builder(Ids.SERVER_RUNTIME_STATUS, resources.constants().status())
                        .token(NameTokens.SERVER_RUNTIME)
                        .build());
                items.addAll(existingSubsystems.values().stream()
                        .sorted(comparing(SubsystemMetadata::getTitle))
                        .collect(toList()));
                callback.onSuccess(items);
            });
        };
        setItemsProvider(itemsProvider);

        setBreadcrumbItemsProvider(
                (context, callback) -> itemsProvider.get(context, new AsyncCallback<List<SubsystemMetadata>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(List<SubsystemMetadata> result) {
                        // only subsystems w/o next columns will show up in the breadcrumb dropdown
                        List<SubsystemMetadata> subsystemsWithTokens = result.stream()
                                .filter(metadata -> metadata.getToken() != null)
                                .collect(toList());
                        callback.onSuccess(subsystemsWithTokens);
                    }
                }));

        setItemRenderer(item -> new ItemDisplay<SubsystemMetadata>() {
            @Override
            public String getId() {
                return item.getName();
            }

            @Override
            public HTMLElement asElement() {
                return item.getSubtitle() != null ? ItemDisplay
                        .withSubtitle(item.getTitle(), item.getSubtitle()) : null;
            }

            @Override
            public String getTitle() {
                return item.getTitle();
            }

            @Override
            public String nextColumn() {
                return item.getNextColumn();
            }

            @Override
            public List<ItemAction<SubsystemMetadata>> actions() {
                PlaceRequest placeRequest = null;
                if (item.getToken() != null) {
                    placeRequest = places.selectedServer(item.getToken()).build();
                }

                if (placeRequest == null) {
                    return ItemDisplay.super.actions();
                } else {
                    return singletonList(itemActionFactory.view(placeRequest));
                }
            }
        });

        setPreviewCallback(item -> {
            if (customPreviews.containsKey(item.getName())) {
                return customPreviews.get(item.getName());
            } else if (item.getExternalTextResource() != null) {
                return new PreviewContent<>(item.getTitle(), item.getExternalTextResource());
            } else {
                return null;
            }
        });
    }
}
