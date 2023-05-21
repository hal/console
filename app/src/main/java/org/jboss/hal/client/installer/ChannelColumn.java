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
package org.jboss.hal.client.installer;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.web.bindery.event.shared.EventBus;
import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.finder.*;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_ADDRESS;
import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.fontAwesome;

@Column(Ids.UPDATE_MANAGER_CHANNEL)
@Requires(INSTALLER_ADDRESS)
public class ChannelColumn extends FinderColumn<Channel> {

    private final EventBus eventBus;
    private final StatementContext statementContext;
    private final MetadataRegistry metadataRegistry;
    private final Dispatcher dispatcher;
    private final Resources resources;

    @Inject
    public ChannelColumn(final Finder finder,
                         final EventBus eventBus,
                         final ColumnActionFactory columnActionFactory,
                         final ItemActionFactory itemActionFactory,
                         final StatementContext statementContext,
                         final MetadataRegistry metadataRegistry,
                         final Dispatcher dispatcher,
                         final Resources resources) {
        super(new Builder<Channel>(finder, Ids.UPDATE_MANAGER_CHANNEL, Names.CHANNELS)
                .onPreview(ChannelPreview::new)
                .showCount()
                .withFilter());
        this.eventBus = eventBus;
        this.statementContext = statementContext;
        this.metadataRegistry = metadataRegistry;
        this.dispatcher = dispatcher;
        this.resources = resources;

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
            public String getId() {
                return Ids.asId(item.getName());
            }

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
                actions.add(itemActionFactory.view(NameTokens.CHANNEL, NAME, item.getName()));
                actions.add(new ItemAction.Builder<Channel>()
                        .title("Unsubscribe")
                        .handler(itm -> remove(itm))
                        .constraint(Constraint.executable(INSTALLER_TEMPLATE, WRITE_ATTRIBUTE_OPERATION))
                        .build());
                return actions;
            }
        });

        addColumnAction(new ColumnAction.Builder<Channel>(Ids.UPDATE_MANAGER_CHANNEL_ADD)
                .element(columnActionFactory.addButton(Names.CHANNEL))
                .handler(column -> add())
                .constraint(Constraint.executable(INSTALLER_TEMPLATE, WRITE_ATTRIBUTE_OPERATION))
                .build());
        addColumnAction(columnActionFactory.refresh(Ids.UPDATE_MANAGER_CHANNEL_REFRESH));
    }

    private void add() {
        Form<ModelNode> channelForm = ChannelFormFactory.channelForm(metadataRegistry, resources);
        AddResourceDialog dialog = new AddResourceDialog("Add channel", channelForm, (name, modelNode) -> {
            Operation operation = new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), LIST_ADD_OPERATION)
                    .param(NAME, CHANNELS)
                    .param(VALUE, modelNode)
                    .build();
            dispatcher.execute(operation, result -> {
                MessageEvent.fire(eventBus, Message.success(resources.messages().addResourceSuccess(Names.CHANNEL, name)));
                //noinspection DataFlowIssue
                refresh(Ids.asId(name));
            });
        });
        dialog.show();
    }

    private void remove(Channel channel) {
        DialogFactory.showConfirmation("Unsubscribe channel", new SafeHtmlBuilder()
                .appendHtmlConstant("Are you sure you want to unsubscribe from channel <b>" + channel.getName() + "</b>?")
                .toSafeHtml(), () -> {
            Operation operation = new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), CHANNEL_REMOVE)
                    .param(NAME, channel.getName())
                    .build();
            dispatcher.execute(operation, result -> {
                MessageEvent
                        .fire(eventBus,
                                Message.success(new SafeHtmlBuilder().appendHtmlConstant(
                                                "Successfully unsubscribed from channel <b>" + channel.getName() + "</b>.")
                                        .toSafeHtml()));
                refresh(RefreshMode.CLEAR_SELECTION);
            });
        });
    }
}
