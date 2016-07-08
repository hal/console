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

import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemsProvider;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.subsystem.GenericSubsystemPresenter;
import org.jboss.hal.core.subsystem.SubsystemDisplay;
import org.jboss.hal.core.subsystem.SubsystemMetadata;
import org.jboss.hal.core.subsystem.SubsystemPreviewCallback;
import org.jboss.hal.core.subsystem.SubsystemProvider;
import org.jboss.hal.core.subsystem.Subsystems;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;

import static java.util.stream.Collectors.toList;

/**
 * @author Harald Pehl
 */
@Column(Ids.CONFIGURATION_SUBSYSTEM)
public class SubsystemColumn extends FinderColumn<SubsystemMetadata> {

    private static final AddressTemplate SUBSYSTEM_TEMPLATE = AddressTemplate.of("{selected.profile}/subsystem=*");

    private final Places places;
    private final StatementContext statementContext;

    @Inject
    public SubsystemColumn(final Finder finder,
            final Dispatcher dispatcher,
            final Places places,
            final StatementContext statementContext,
            final ItemActionFactory itemActionFactory,
            final Subsystems subsystems,
            final Resources resources) {

        super(new Builder<SubsystemMetadata>(finder, Ids.CONFIGURATION_SUBSYSTEM, Names.SUBSYSTEM)

                .onPreview(new SubsystemPreviewCallback(dispatcher, statementContext, SUBSYSTEM_TEMPLATE, resources))
                .useFirstActionAsBreadcrumbHandler()
                .showCount()
                .withFilter()
                .pinnable());

        this.places = places;
        this.statementContext = statementContext;

        ItemsProvider<SubsystemMetadata> itemsProvider = new SubsystemProvider(dispatcher, statementContext,
                AddressTemplate.of("/{selected.profile}"), subsystems);
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

        setItemRenderer(item -> new SubsystemDisplay(item,
                Collections.singletonList(itemActionFactory.view(subsystemPlaceRequest(item)))));
    }

    private PlaceRequest subsystemPlaceRequest(SubsystemMetadata metadata) {
        PlaceRequest placeRequest = null;
        if (metadata.hasCustomImplementation() && metadata.getToken() != null) {
            placeRequest = places.selectedProfile(metadata.getToken()).build();

        } else if (!metadata.hasCustomImplementation()) {
            ResourceAddress address = SUBSYSTEM_TEMPLATE.resolve(statementContext, metadata.getName());
            placeRequest = new PlaceRequest.Builder()
                    .nameToken(NameTokens.GENERIC_SUBSYSTEM)
                    .with(GenericSubsystemPresenter.ADDRESS_PARAM, address.toString())
                    .build();
        }
        return placeRequest;
    }
}
