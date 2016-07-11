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

import javax.inject.Inject;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.subsystem.GenericSubsystemPresenter;
import org.jboss.hal.core.subsystem.GenericSubsystemColumn;
import org.jboss.hal.core.subsystem.Subsystems;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.spi.Column;

/**
 * @author Harald Pehl
 */
@Column(Ids.RUNTIME_SUBSYSTEM)
public class SubsystemColumn extends GenericSubsystemColumn {

    private static final AddressTemplate SUBSYSTEM_TEMPLATE = AddressTemplate
            .of("{selected.host}/{selected.server}/subsystem=*");

    @Inject
    public SubsystemColumn(final Finder finder,
            final Dispatcher dispatcher,
            final Places places,
            final StatementContext statementContext,
            final ItemActionFactory itemActionFactory,
            final Subsystems subsystems) {

        super(finder, Ids.RUNTIME_SUBSYSTEM, dispatcher, statementContext, itemActionFactory,
                subsystems.getRuntimeSubsystems(), SUBSYSTEM_TEMPLATE,
                metadata -> {
                    PlaceRequest placeRequest = null;
                    if (metadata.hasCustomImplementation() && metadata.getToken() != null) {
                        placeRequest = places.selectedServer(metadata.getToken()).build();

                    } else if (!metadata.hasCustomImplementation()) {
                        ResourceAddress address = SUBSYSTEM_TEMPLATE.resolve(statementContext, metadata.getName());
                        placeRequest = new PlaceRequest.Builder()
                                .nameToken(NameTokens.GENERIC_SUBSYSTEM)
                                .with(GenericSubsystemPresenter.ADDRESS_PARAM, address.toString())
                                .build();
                    }
                    return placeRequest;
                });
    }
}
