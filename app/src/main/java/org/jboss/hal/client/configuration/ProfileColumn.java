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
package org.jboss.hal.client.configuration;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.subsystem.GenericSubsystemPresenter;
import org.jboss.hal.core.configuration.ProfileSelectionEvent;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.Places;
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
import org.jboss.hal.spi.Requires;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;

/**
 * @author Harald Pehl
 */
@Column(Ids.PROFILE)
@Requires(value = "/profile=*", recursive = false)
public class ProfileColumn extends FinderColumn<String> {

    private static final AddressTemplate PROFILE_TEMPLATE = AddressTemplate.of("/profile=*");

    @Inject
    public ProfileColumn(final Finder finder,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final PlaceManager placeManager,
            final Places places,
            final ColumnActionFactory columnActionFactory,
            final StatementContext statementContext) {

        super(new Builder<String>(finder, Ids.PROFILE, Names.PROFILE)
                .columnAction(columnActionFactory.add(
                        Ids.PROFILE_ADD,
                        Names.PROFILE,
                        PROFILE_TEMPLATE))
                .columnAction(columnActionFactory.refresh(Ids.PROFILE_REFRESH))

                .itemsProvider((context, callback) -> {
                    Operation operation = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION,
                            ResourceAddress.ROOT)
                            .param(CHILD_TYPE, PROFILE)
                            .build();
                    dispatcher.execute(operation, result ->
                            callback.onSuccess(result.asList().stream().map(ModelNode::asString).collect(toList())));
                })

                .itemRenderer(profile -> new ItemDisplay<String>() {
                    @Override
                    public String getTitle() {
                        return profile;
                    }

                    @Override
                    public String nextColumn() {
                        return Ids.CONFIGURATION_SUBSYSTEM;
                    }
                })

                .onItemSelect(profile -> eventBus.fireEvent(new ProfileSelectionEvent(profile)))

                .onPreview(PreviewContent::new)

                .onBreadcrumbItem((item, context) -> {
                    PlaceRequest.Builder builder = null;
                    PlaceRequest current = placeManager.getCurrentPlaceRequest();

                    if (NameTokens.GENERIC_SUBSYSTEM.equals(current.getNameToken())) {
                        // switch profile in address parameter of generic presenter
                        builder = new PlaceRequest.Builder().nameToken(current.getNameToken());
                        String addressParam = current.getParameter(GenericSubsystemPresenter.ADDRESS_PARAM, null);
                        if (addressParam != null) {
                            ResourceAddress currentAddress = AddressTemplate.of(addressParam).resolve(statementContext);
                            ResourceAddress newAddress = currentAddress.replaceValue(PROFILE, item);
                            builder.with(GenericSubsystemPresenter.ADDRESS_PARAM, newAddress.toString());
                        }

                    } else {
                        // switch profile in place request parameter of specific presenter
                        builder = places.replaceParameter(current, PROFILE, item);
                    }
                    placeManager.revealPlace(builder.build());
                }));
    }
}
