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
import elemental.dom.Element;
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
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Column;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_DESCRIPTION_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Harald Pehl
 */
@Column(Ids.SUBSYSTEM)
public class SubsystemColumn extends FinderColumn<SubsystemMetadata> {

    private static final AddressTemplate SUBSYSTEM_TEMPLATE = AddressTemplate.of("{selected.profile}/subsystem=*");

    @Inject
    public SubsystemColumn(final Finder finder,
            final Dispatcher dispatcher,
            final Places places,
            final StatementContext statementContext,
            final ItemActionFactory itemActionFactory,
            final Subsystems subsystems) {

        super(new Builder<SubsystemMetadata>(finder, Ids.SUBSYSTEM, Names.SUBSYSTEM)

                .itemRenderer(item -> new ItemDisplay<SubsystemMetadata>() {
                    @Override
                    public String getId() {
                        return item.getName();
                    }

                    @Override
                    public Element asElement() {
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
                        if (item.getNextColumn() != null) {
                            return ItemDisplay.super.actions();
                        }
                        PlaceRequest placeRequest = null;
                        if (item.hasCustomImplementation() && item.getToken() != null) {
                            placeRequest = places.selectedProfile(item.getToken()).build();

                        } else if (!item.hasCustomImplementation()) {
                            ResourceAddress address = SUBSYSTEM_TEMPLATE.resolve(statementContext, item.getName());
                            placeRequest = new PlaceRequest.Builder()
                                    .nameToken(NameTokens.GENERIC_SUBSYSTEM)
                                    .with(SubsystemPresenter.ADDRESS_PARAM, address.toString())
                                    .build();
                        }
                        return singletonList(itemActionFactory.view(placeRequest));
                    }
                })

                .onPreview(item -> {
                    if (item.getExternalTextResource() != null) {
                        return new PreviewContent<>(item.getTitle(), item.getExternalTextResource());
                    } else {
                        ResourceAddress address = SUBSYSTEM_TEMPLATE.resolve(statementContext, item.getName());
                        Operation operation = new Operation.Builder(READ_RESOURCE_DESCRIPTION_OPERATION, address)
                                .build();
                        return new ResourceDescriptionPreview(item.getTitle(), dispatcher, operation);
                    }
                })

                .useFirstActionAsBreadcrumbHandler()
                .showCount()
                .withFilter()
                .pinnable()
        );

        ItemsProvider<SubsystemMetadata> itemsProvider = (context, callback) -> {
            ResourceAddress address = SUBSYSTEM_TEMPLATE.resolve(statementContext).getParent();
            Operation operation = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION, address)
                    .param(CHILD_TYPE, SUBSYSTEM).build();
            dispatcher.execute(operation, result -> {

                List<SubsystemMetadata> combined = new ArrayList<>();
                for (ModelNode modelNode : result.asList()) {
                    String name = modelNode.asString();
                    if (subsystems.contains(name)) {
                        combined.add(subsystems.get(name));

                    } else {
                        String title = new LabelBuilder().label(name);
                        SubsystemMetadata subsystem = new SubsystemMetadata(name, title, null, null, null, false);
                        combined.add(subsystem);
                    }
                }
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
                                .filter(metadata -> metadata.getNextColumn() == null)
                                .collect(toList());
                        callback.onSuccess(subsystemsWithTokens);
                    }
                }));
    }
}
