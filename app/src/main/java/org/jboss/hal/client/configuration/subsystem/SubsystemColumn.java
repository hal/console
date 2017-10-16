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
package org.jboss.hal.client.configuration.subsystem;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.ItemsProvider;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.ResourceDescriptionPreview;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.subsystem.SubsystemMetadata;
import org.jboss.hal.core.subsystem.Subsystems;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;

import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_DESCRIPTION_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_PROFILE;

@Column(Ids.CONFIGURATION_SUBSYSTEM)
public class SubsystemColumn extends FinderColumn<SubsystemMetadata> {

    private static final AddressTemplate SUBSYSTEM_TEMPLATE = AddressTemplate.of(SELECTED_PROFILE, "subsystem=*");

    @Inject
    public SubsystemColumn(Finder finder,
            Dispatcher dispatcher,
            Places places,
            StatementContext statementContext,
            ItemActionFactory itemActionFactory,
            Subsystems subsystems,
            Resources resources) {

        super(new Builder<SubsystemMetadata>(finder, Ids.CONFIGURATION_SUBSYSTEM, Names.SUBSYSTEM)

                .itemRenderer(item -> new ItemDisplay<SubsystemMetadata>() {
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
                    public String getFilterData() {
                        return item.getSubtitle() != null ? item.getTitle() + " " + item.getSubtitle() : item
                                .getTitle();
                    }

                    @Override
                    public String nextColumn() {
                        return item.getNextColumn();
                    }

                    @Override
                    public List<ItemAction<SubsystemMetadata>> actions() {
                        PlaceRequest placeRequest = null;
                        if (item.isGeneric()) {
                            ResourceAddress address = SUBSYSTEM_TEMPLATE.resolve(statementContext, item.getName());
                            placeRequest = places.genericSubsystem(address);
                        } else if (item.getToken() != null) {
                            placeRequest = places.selectedProfile(item.getToken()).build();
                        }

                        if (placeRequest == null) {
                            return ItemDisplay.super.actions();
                        } else {
                            return singletonList(itemActionFactory.view(placeRequest));
                        }
                    }
                })

                .onPreview(item -> {
                    if (item.getExternalTextResource() != null) {
                        return new PreviewContent<>(item.getTitle(), item.getExternalTextResource());
                    } else {
                        ResourceAddress address = SUBSYSTEM_TEMPLATE.resolve(statementContext, item.getName());
                        Operation operation = new Operation.Builder(address, READ_RESOURCE_DESCRIPTION_OPERATION)
                                .build();
                        return new ResourceDescriptionPreview(item.getTitle(), dispatcher, operation);
                    }
                })

                .useFirstActionAsBreadcrumbHandler()
                .showCount()
                .withFilter()
                .pinnable()
                .filterDescription(resources.messages().susbsystemFilterDescription())
        );

        ItemsProvider<SubsystemMetadata> itemsProvider = (context, callback) -> {
            ResourceAddress address = SUBSYSTEM_TEMPLATE.resolve(statementContext).getParent();
            Operation operation = new Operation.Builder(address, READ_CHILDREN_NAMES_OPERATION)
                    .param(CHILD_TYPE, SUBSYSTEM).build();
            dispatcher.execute(operation, result -> {

                List<SubsystemMetadata> combined = new ArrayList<>();
                for (ModelNode modelNode : result.asList()) {
                    String name = modelNode.asString();
                    if (subsystems.containsConfiguration(name)) {
                        combined.add(subsystems.getConfiguration(name));

                    } else {
                        String title = new LabelBuilder().label(name);
                        combined.add(new SubsystemMetadata.Builder(name, title).generic().build());
                    }
                }
                combined.sort(comparing(SubsystemMetadata::getTitle));
                callback.onSuccess(combined);
            });
        };
        setItemsProvider(itemsProvider);

        // reuse the items provider to filter breadcrumb items
        setBreadcrumbItemsProvider((context, callback) ->
                itemsProvider.get(context, new AsyncCallback<List<SubsystemMetadata>>() {
                    @Override
                    public void onFailure(final Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(final List<SubsystemMetadata> result) {
                        // only subsystems w/o next columns will show up in the breadcrumb dropdown
                        List<SubsystemMetadata> subsystemsWithTokens = result.stream()
                                .filter(metadata -> metadata.getToken() != null || metadata.isGeneric())
                                .collect(toList());
                        callback.onSuccess(subsystemsWithTokens);
                    }
                }));
    }
}
