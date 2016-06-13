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
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.client.configuration.subsystem.GenericSubsystemPresenter;
import org.jboss.hal.core.ProfileSelectionEvent;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;

/**
 * @author Harald Pehl
 */
@Column(ModelDescriptionConstants.PROFILE)
@Requires(value = "/profile=*", recursive = false)
public class ProfileColumn extends FinderColumn<ModelNode> {

    private static final AddressTemplate PROFILE_TEMPLATE = AddressTemplate.of("/profile=*");

    @Inject
    public ProfileColumn(final Finder finder,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final ColumnActionFactory columnActionFactory,
            final PlaceManager placeManager,
            final StatementContext statementContext) {

        super(new Builder<ModelNode>(finder, ModelDescriptionConstants.PROFILE, Names.PROFILE)
                .columnAction(columnActionFactory.add(
                        IdBuilder.build(ModelDescriptionConstants.PROFILE, "add"),
                        Names.PROFILE,
                        PROFILE_TEMPLATE))
                .columnAction(columnActionFactory.refresh(IdBuilder.build(ModelDescriptionConstants.PROFILE, "refresh")))

                .itemRenderer(modelNode -> new ItemDisplay<ModelNode>() {
                    @Override
                    public String getTitle() {
                        return modelNode.asString();
                    }

                    @Override
                    public String nextColumn() {
                        return ModelDescriptionConstants.SUBSYSTEM;
                    }
                })

                .onItemSelect(modelNode -> eventBus.fireEvent(new ProfileSelectionEvent(modelNode.asString())))

                .itemsProvider((context, callback) -> {
                    Operation operation = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION,
                            ResourceAddress.ROOT)
                            .param(CHILD_TYPE, PROFILE)
                            .build();
                    dispatcher.execute(operation, result -> callback.onSuccess(result.asList()));
                })

                .onPreview(item -> new PreviewContent<>(item.asString()))

                .onBreadcrumbItem((item, context) -> {
                    eventBus.fireEvent(new ProfileSelectionEvent(item.asString()));
                    PlaceRequest current = placeManager.getCurrentPlaceRequest();
                    PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(current.getNameToken());

                    // switch profile in address parameter of generic presenter
                    if (NameTokens.GENERIC_SUBSYSTEM.equals(current.getNameToken())) {
                        String addressParam = current.getParameter(GenericSubsystemPresenter.ADDRESS_PARAM, null);
                        if (addressParam != null) {
                            ResourceAddress currentAddress = AddressTemplate.of(addressParam).resolve(statementContext);
                            ResourceAddress newAddress = new ResourceAddress();
                            for (Property property : currentAddress.asPropertyList()) {
                                if (PROFILE.equals(property.getName())) {
                                    newAddress.add(PROFILE, item.asString());
                                } else {
                                    newAddress.add(property.getName(), property.getValue().asString());
                                }
                            }
                            builder.with(GenericSubsystemPresenter.ADDRESS_PARAM, newAddress.toString());
                        }

                    // switch profile in place request parameter of specific presenter
                    } else {
                        for (String parameter : current.getParameterNames()) {
                            if (PROFILE.equals(parameter)) {
                                builder.with(PROFILE, item.asString());
                            } else {
                                builder.with(parameter, current.getParameter(parameter, ""));
                            }
                        }
                    }
                    placeManager.revealPlace(builder.build());
                }));
    }
}
