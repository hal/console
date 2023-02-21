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
package org.jboss.hal.client.update;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.core.finder.ColumnAction;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.web.bindery.event.shared.EventBus;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.update.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHANNELS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;
import static org.jboss.hal.resources.CSS.fontAwesome;

@Column(Ids.UPDATE_CHANNEL)
public class ChannelColumn extends FinderColumn<Channel> {

    private final EventBus eventBus;

    @Inject
    public ChannelColumn(final Finder finder,
            final EventBus eventBus,
            final ColumnActionFactory columnActionFactory,
            final StatementContext statementContext,
            final Dispatcher dispatcher,
            final Resources resources) {
        super(new Builder<Channel>(finder, Ids.UPDATE_CHANNEL, Names.CHANNELS)
                .onPreview(ChannelPreview::new)
                .showCount()
                .withFilter());
        this.eventBus = eventBus;

        setItemsProvider(context -> {
            ResourceAddress address = INSTALLER_TEMPLATE.resolve(statementContext);
            Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            return dispatcher.execute(operation).then(result -> {
                // noinspection Convert2MethodRef
                List<Channel> channels = result.get(CHANNELS).asList().stream().map(node -> new Channel(node))
                        .collect(toList());
                return Promise.resolve(channels);
            });
        });

        setItemRenderer(item -> new ItemDisplay<Channel>() {
            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public HTMLElement getIcon() {
                if (item.isGAV()) {
                    return Icons.custom(fontAwesome("diamond"));
                } else if (item.isURL()) {
                    return Icons.custom(fontAwesome("external-link"));
                } else {
                    return Icons.unknown();
                }
            }

            @Override
            public HTMLElement element() {
                return ItemDisplay.withSubtitle(item.getName(), Names.MANIFEST + ": " + item.getManifestType());
            }

            @Override
            public List<ItemAction<Channel>> actions() {
                List<ItemAction<Channel>> actions = new ArrayList<>();
                actions.add(new ItemAction.Builder<Channel>()
                        .title(resources.constants().remove())
                        .handler(item1 -> remove(item1))
                        .constraint(Constraint.executable(INSTALLER_TEMPLATE, WRITE_ATTRIBUTE_OPERATION))
                        .build());
                return actions;
            }
        });

        addColumnAction(new ColumnAction.Builder<Channel>(Ids.UPDATE_CHANNEL_ADD)
                .element(columnActionFactory.addButton(Names.CHANNEL))
                .handler(column -> add())
                .constraint(Constraint.executable(INSTALLER_TEMPLATE, WRITE_ATTRIBUTE_OPERATION))
                .build());
        addColumnAction(columnActionFactory.refresh(Ids.UPDATE_CHANNEL_REFRESH));
    }

    private void add() {
        MessageEvent.fire(eventBus, Message.error(SafeHtmlUtils.fromSafeConstant(Names.NYI)));
    }

    private void remove(Channel channel) {
        MessageEvent.fire(eventBus, Message.error(SafeHtmlUtils.fromSafeConstant(Names.NYI)));
    }
}
