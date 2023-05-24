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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import com.google.common.base.Strings;
import com.google.web.bindery.event.shared.EventBus;

import elemental2.promise.Promise;

import static java.util.stream.Collectors.toList;

import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.client.installer.ChannelFormFactory.MANIFEST_GAV;
import static org.jboss.hal.client.installer.ChannelFormFactory.MANIFEST_URL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHANNELS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHANNEL_REMOVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.GAV;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANIFEST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REPOSITORIES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.URL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;

/**
 * The channels list in /core-service=installer is a runtime attribute. It cannot be saved like configuration attributes
 * using @{@link org.jboss.hal.core.ComplexAttributeOperations} which uses a composite operation containing write-attribute
 * operations to modify only changes of nested attributes.
 * <p>
 * Instead, this class modifies the complete list of channels as one write-attribute operation.
 * <p>
 * <strong>Important:</strong> This class assumes that the channel name is unique and uses the name to identify a channel.
 */
class ChannelOperations {

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;

    ChannelOperations(EventBus eventBus,
            Dispatcher dispatcher,
            StatementContext statementContext,
            Resources resources) {
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resources = resources;
    }

    void addChannel(final Channel channel, final Callback callback) {
        readChannels()
                .then(channels -> {
                    channels.add(channel);
                    return writeChannels(channels);
                })
                .then(result -> {
                    MessageEvent.fire(eventBus, Message.success(resources.messages().addResourceSuccess(Names.CHANNEL,
                            channel.getName())));
                    return null;
                })
                .finally_(callback::execute);
    }

    void saveChannel(final String name, final Map<String, Object> changedValues, final Callback callback) {
        readChannels()
                .then(channels -> {
                    Channel existingChannel = findChannel(name, channels);
                    if (existingChannel != null) {
                        Channel modifiedChannel = mergeChannel(existingChannel, changedValues);
                        List<Channel> updatedChannels = insertChannel(channels, modifiedChannel);
                        return writeChannels(updatedChannels);
                    } else {
                        return Promise.reject(resources.constants().noResource());
                    }
                })
                .then(result -> {
                    MessageEvent.fire(eventBus, Message.success(resources.messages().modifyResourceSuccess(Names.CHANNEL,
                            name)));
                    return null;
                })
                .catch_(failure -> {
                    MessageEvent.fire(eventBus,
                            Message.error(resources.messages().lastOperationFailed(), String.valueOf(failure)));
                    return null;
                })
                .finally_(callback::execute);
    }

    void removeChannel(final String name, final Callback callback) {
        Operation operation = new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), CHANNEL_REMOVE)
                .param(NAME, name)
                .build();
        dispatcher.execute(operation)
                .then(result -> {
                    MessageEvent.fire(eventBus, Message.success(resources.messages().removeResourceSuccess(Names.CHANNEL,
                            name)));
                    return null;
                })
                .catch_(failure -> {
                    MessageEvent.fire(eventBus,
                            Message.error(resources.messages().lastOperationFailed(), String.valueOf(failure)));
                    return null;
                })
                .finally_(callback::execute);
    }

    private Promise<List<Channel>> readChannels() {
        return dispatcher.execute(readOperation())
                .then(result -> Promise.resolve(result.asList().stream().map(Channel::new).collect(toList())));
    }

    private Operation readOperation() {
        return new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), READ_ATTRIBUTE_OPERATION)
                .param(NAME, CHANNELS)
                .build();
    }

    private Promise<ModelNode> writeChannels(final List<Channel> channels) {
        return dispatcher.execute(writeOperation(channels));
    }

    private Operation writeOperation(final List<Channel> channels) {
        ModelNode value = new ModelNode();
        for (Channel channel : channels) {
            value.add(channel);
        }
        return new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, CHANNELS)
                .param(VALUE, value)
                .build();
    }

    private Channel findChannel(final String name, final List<Channel> channels) {
        for (Channel channel : channels) {
            if (name.equals(channel.getName())) {
                return channel;
            }
        }
        return null;
    }

    private Channel mergeChannel(final Channel channel, final Map<String, Object> changedValues) {
        ModelNode modelNode = new ModelNode();
        modelNode.get(NAME).set(channel.getName());
        Channel updatedChannel = new Channel(modelNode);

        if (changedValues.containsKey(REPOSITORIES)) {
            ModelNode repositories = (ModelNode) changedValues.get(REPOSITORIES);
            updatedChannel.get(REPOSITORIES).set(repositories);
        } else {
            updatedChannel.get(REPOSITORIES).set(channel.get(REPOSITORIES));
        }
        if (changedValues.containsKey(MANIFEST_GAV) || changedValues.containsKey(MANIFEST_URL)) {
            updatedChannel.remove(MANIFEST);
            String gav = (String) changedValues.get(MANIFEST_GAV);
            String url = (String) changedValues.get(MANIFEST_URL);
            if (!Strings.isNullOrEmpty(gav)) {
                updatedChannel.get(MANIFEST).get(GAV).set(gav);
            } else if (!Strings.isNullOrEmpty(url)) {
                updatedChannel.get(MANIFEST).get(URL).set(url);
            }
        } else {
            updatedChannel.get(MANIFEST).set(channel.get(MANIFEST));
        }
        return updatedChannel;
    }

    private List<Channel> insertChannel(final List<Channel> channels, final Channel channel) {
        List<Channel> updatedChannels = new ArrayList<>();
        for (Channel c : channels) {
            if (channel.getName().equals(c.getName())) {
                updatedChannels.add(channel);
            } else {
                updatedChannels.add(c);
            }
        }
        return updatedChannels;
    }
}
