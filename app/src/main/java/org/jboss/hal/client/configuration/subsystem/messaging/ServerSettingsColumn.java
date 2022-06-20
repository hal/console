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
package org.jboss.hal.client.configuration.subsystem.messaging;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderSegment;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.finder.StaticItemColumn;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.SelectionAwareStatementContext;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import com.google.web.bindery.event.shared.EventBus;

import elemental2.promise.Promise;

import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SELECTED_SERVER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;

@AsyncColumn(Ids.MESSAGING_SERVER_SETTINGS)
public class ServerSettingsColumn
        extends FinderColumn<StaticItem> { // doesn't extend from StaticItemColumn because we need more flexibility

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final Resources resources;

    @Inject
    public ServerSettingsColumn(Finder finder,
            ItemActionFactory itemActionFactory,
            Places places,
            EventBus eventBus,
            Dispatcher dispatcher,
            CrudOperations crud,
            StatementContext statementContext,
            Resources resources) {

        super(new Builder<StaticItem>(finder, Ids.MESSAGING_SERVER_SETTINGS, resources.constants().settings())
                .itemRenderer(StaticItemColumn.StaticItemDisplay::new)
                .onPreview(StaticItem::getPreviewContent)
                .onBreadcrumbItem((item, context) -> {
                    if (item.getId().equals(Ids.MESSAGING_SERVER_HA_POLICY)) {
                        // the first action for HA policy might be 'add'
                        for (ItemAction<StaticItem> itemAction : item.getActions()) {
                            String title = itemAction.getTitle();
                            if (resources.constants().view().equals(title)) {
                                itemAction.getHandler().execute(item);
                                break;
                            }
                        }
                    } else {
                        item.getActions().get(0).getHandler().execute(item);
                    }
                }));

        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.resources = resources;

        setItemsProvider(context -> new Promise<>((resolve, reject) -> {
            List<StaticItem> items = new ArrayList<>();
            FinderSegment<?> segment = context.getPath().findColumn(Ids.MESSAGING_SERVER_CONFIGURATION);
            if (segment != null) {
                String server = segment.getItemTitle();
                StatementContext serverStatementContext = new SelectionAwareStatementContext(statementContext,
                        () -> server);
                ResourceAddress address = SELECTED_SERVER_TEMPLATE.resolve(serverStatementContext);
                HaPolicy.readChildren(crud, address, 1, children -> {

                    items.add(new StaticItem.Builder(Names.DESTINATIONS)
                            .id(Ids.MESSAGING_SERVER_DESTINATION)
                            .action(itemActionFactory.view(
                                    places.selectedProfile(NameTokens.MESSAGING_SERVER_DESTINATION)
                                            .with(SERVER, server)
                                            .build()))
                            .onPreview(new PreviewContent<>(Names.DESTINATIONS,
                                    resources.previews().configurationMessagingDestinations()))
                            .build());
                    items.add(new StaticItem.Builder(Names.CONNECTIONS)
                            .id(Ids.MESSAGING_SERVER_CONNECTION)
                            .action(itemActionFactory.view(
                                    places.selectedProfile(NameTokens.MESSAGING_SERVER_CONNECTION)
                                            .with(SERVER, server)
                                            .build()))
                            .onPreview(new PreviewContent<>(Names.CONNECTIONS,
                                    resources.previews().configurationMessagingConnections()))
                            .build());
                    items.add(new StaticItem.Builder(Names.CLUSTERING)
                            .id(Ids.MESSAGING_SERVER_CLUSTERING)
                            .action(itemActionFactory.view(
                                    places.selectedProfile(NameTokens.MESSAGING_SERVER_CLUSTERING)
                                            .with(SERVER, server)
                                            .build()))
                            .onPreview(new PreviewContent<>(Names.CLUSTERING,
                                    resources.previews().configurationMessagingClustering()))
                            .build());

                    StaticItem.Builder builder = new StaticItem.Builder(Names.HA_POLICY)
                            .id(Ids.MESSAGING_SERVER_HA_POLICY);
                    if (children.isEmpty()) {
                        builder.action(resources.constants().add(), item -> addHaPolicy(serverStatementContext))
                                .action(itemActionFactory.view(
                                        places.selectedProfile(NameTokens.MESSAGING_SERVER_HA_POLICY)
                                                .with(SERVER, server)
                                                .build()))
                                .onPreview(new PreviewContent<>(Names.HA_POLICY,
                                        resources.previews().configurationMessagingHaPolicy()));

                    } else {
                        Property child = children.get(0);
                        HaPolicy haPolicy = HaPolicy.fromResourceName(child.getName());
                        builder.action(itemActionFactory.view(
                                places.selectedProfile(NameTokens.MESSAGING_SERVER_HA_POLICY)
                                        .with(SERVER, server)
                                        .build()))
                                .action(resources.constants().remove(),
                                        item -> removeHaPolicy(serverStatementContext, haPolicy))
                                .onPreview(new HaPolicyPreview(haPolicy, child.getValue()));
                    }
                    items.add(builder.build());

                    resolve.onInvoke(items);
                });

            } else {
                resolve.onInvoke(items);
            }
        }));
    }

    private void addHaPolicy(StatementContext statementContext) {
        new HaPolicyWizard(resources, (wizard, context) -> context.haPolicy.add(dispatcher, statementContext, () -> {
            MessageEvent.fire(eventBus,
                    Message.success(resources.messages().addSingleResourceSuccess(context.haPolicy.type)));
            refresh(RefreshMode.RESTORE_SELECTION);
        })).show();
    }

    private void removeHaPolicy(StatementContext statementContext, HaPolicy haPolicy) {
        haPolicy.remove(dispatcher, statementContext, resources, () -> {
            MessageEvent.fire(eventBus,
                    Message.success(resources.messages().removeSingletonSuccess(haPolicy.type)));
            refresh(RefreshMode.RESTORE_SELECTION);
        });
    }
}
