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
package org.jboss.hal.client.configuration.subsystem.logging;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.logging.AddressTemplates.LOGGING_PROFILE_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.logging.AddressTemplates.LOGGING_PROFILE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.logging.AddressTemplates.LOGGING_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LOGGING_PROFILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

@AsyncColumn(Ids.LOGGING_PROFILE)
@Requires(LOGGING_PROFILE_ADDRESS)
public class LoggingProfileColumn extends FinderColumn<NamedNode> {

    @Inject
    public LoggingProfileColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory,
            final Places places,
            final Dispatcher dispatcher,
            final CrudOperations crud,
            final StatementContext statementContext,
            final Resources resources) {

        super(new FinderColumn.Builder<NamedNode>(finder, Ids.LOGGING_PROFILE, Names.LOGGING_PROFILES)

                .columnAction(columnActionFactory.add(
                        Ids.LOGGING_PROFILE_ADD,
                        Names.LOGGING_PROFILE,
                        LOGGING_PROFILE_TEMPLATE,
                        Ids::loggingProfile))

                .itemsProvider((context, callback) -> crud.readChildren(LOGGING_SUBSYSTEM_TEMPLATE, LOGGING_PROFILE, 1,
                        children -> callback.onSuccess(asNamedNodes(children))))

                .onPreview(item ->
                        new LoggingPreview<>(dispatcher, resources, item.getName(),
                                resources.previews().configurationLoggingProfiles(),
                                () -> new Operation.Builder(LOGGING_PROFILE_TEMPLATE.append("root-logger=ROOT")
                                        .resolve(statementContext, item.getName()), READ_RESOURCE_OPERATION).build()))

                .useFirstActionAsBreadcrumbHandler()
        );

        setItemRenderer(item -> new ItemDisplay<NamedNode>() {
            @Override
            public String getId() {
                return Ids.loggingProfile(item.getName());
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public List<ItemAction<NamedNode>> actions() {
                PlaceRequest placeRequest = places.selectedProfile(NameTokens.LOGGING_PROFILE)
                        .with(NAME, item.getName())
                        .build();
                return Arrays.asList(
                        itemActionFactory.viewAndMonitor(Ids.loggingProfile(item.getName()), placeRequest),
                        itemActionFactory.remove(Names.LOGGING_PROFILE, item.getName(),
                                LOGGING_PROFILE_TEMPLATE, LoggingProfileColumn.this));
            }
        });
    }
}
