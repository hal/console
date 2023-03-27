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
package org.jboss.hal.client.configuration.subsystem.logging;

import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.ItemMonitor;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.finder.StaticItemColumn;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;

import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import javax.inject.Inject;

import static java.util.Arrays.asList;
import static org.jboss.hal.client.configuration.subsystem.logging.AddressTemplates.ROOT_LOGGER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

@AsyncColumn(Ids.LOGGING_CONFIG_AND_PROFILES)
public class LoggingColumn extends StaticItemColumn {

    @Inject
    public LoggingColumn(Finder finder,
            ItemMonitor itemMonitor,
            StatementContext statementContext,
            Dispatcher dispatcher,
            PlaceManager placeManager,
            Places places,
            Resources resources) {

        super(finder, Ids.LOGGING_CONFIG_AND_PROFILES, Names.LOGGING, asList(

                new StaticItem.Builder(Names.CONFIGURATION)
                        .id(Ids.LOGGING_CONFIGURATION)
                        .action(resources.constants().view(),
                                item -> itemMonitor.monitorPlaceRequestWithPromise(item.getId(), NameTokens.LOGGING_CONFIGURATION)
                                        .then(__ -> {
                                            PlaceRequest placeRequest = places
                                                    .selectedProfile(NameTokens.LOGGING_CONFIGURATION)
                                                    .build();
                                            placeManager.revealPlace(placeRequest);
                                            return null;
                                        }))
/*
                        .action(resources.constants().view(),
                                item -> itemMonitor.monitorPlaceRequest(item.getId(), NameTokens.LOGGING_CONFIGURATION, () -> {
                                    PlaceRequest placeRequest = places
                                            .selectedProfile(NameTokens.LOGGING_CONFIGURATION)
                                            .build();
                                    placeManager.revealPlace(placeRequest);
                                }).execute(item))
*/
                        .onPreview(new LoggingPreview<>(dispatcher, resources, Names.CONFIGURATION,
                                resources.previews().configurationLoggingConfiguration(),
                                () -> new Operation.Builder(ROOT_LOGGER_TEMPLATE.resolve(statementContext),
                                        READ_RESOURCE_OPERATION).build()))
                        .build(),

                new StaticItem.Builder(Names.LOGGING_PROFILES)
                        .nextColumn(Ids.LOGGING_PROFILE)
                        .onPreview(new PreviewContent<>(Names.LOGGING_PROFILES,
                                resources.previews().configurationLoggingProfiles()))
                        .build()));
    }
}
