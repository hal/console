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

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.hal.core.ComplexAttributeOperations;
import org.jboss.hal.core.mvp.ApplicationPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_ADDRESS;
import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public class ChannelPresenter extends ApplicationPresenter<ChannelPresenter.MyView, ChannelPresenter.MyProxy> {

    private final ComplexAttributeOperations ca;
    private final Dispatcher dispatcher;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private final Resources resources;
    private String channelName;

    @Inject
    public ChannelPresenter(EventBus eventBus,
            ChannelPresenter.MyView view,
            ChannelPresenter.MyProxy proxy,
            ComplexAttributeOperations ca,
            Dispatcher dispatcher,
            MetadataRegistry metadataRegistry,
            StatementContext statementContext,
            Resources resources) {
        super(eventBus, view, proxy);
        this.ca = ca;
        this.dispatcher = dispatcher;
        this.metadataRegistry = metadataRegistry;
        this.statementContext = statementContext;
        this.resources = resources;

    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        channelName = request.getParameter(NAME, null);
    }

    @Override
    protected void onReset() {
        Operation operation = new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), READ_ATTRIBUTE_OPERATION)
                .param(NAME, CHANNELS)
                .build();
        dispatcher.execute(operation, result -> {
            Channel channel = null;
            List<ModelNode> nodes = result.asList();
            for (int i = 0; i < nodes.size(); i++) {
                ModelNode node = nodes.get(i);
                if (channelName.equals(node.get(NAME).asString())) {
                    channel = new Channel(node);
                    channel.get(HAL_INDEX).set(i);
                    break;
                }
            }
            if (channel != null) {
                getView().update(channel);
            } else {
                MessageEvent.fire(getEventBus(), Message.error(resources.messages().noResource()));
            }
        });
    }

    void saveChannel(int index, Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(INSTALLER_TEMPLATE).forComplexAttribute(CHANNELS);
        ca.save(CHANNELS, Names.CHANNEL, index, INSTALLER_TEMPLATE.resolve(statementContext), changedValues, metadata,
                this::onReset);
    }

    // @formatter:off
    @ProxyCodeSplit
    @Requires(INSTALLER_ADDRESS)
    @NameToken(NameTokens.CHANNEL)
    public interface MyProxy extends ProxyPlace<ChannelPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<ChannelPresenter> {
        void update(Channel channel);
    }
    // @formatter:on
}
