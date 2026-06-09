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
package org.jboss.hal.client.runtime.subsystem.jgroups;

import javax.inject.Inject;

import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import static org.jboss.hal.client.runtime.subsystem.jgroups.AddressTemplates.CHANNEL_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.jgroups.AddressTemplates.CHANNEL_PROTOCOL_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.jgroups.AddressTemplates.CHANNEL_PROTOCOL_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.jgroups.AddressTemplates.CHANNEL_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.jgroups.AddressTemplates.FORK_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.jgroups.AddressTemplates.FORK_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.jgroups.AddressTemplates.STACK_PROTOCOL_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.JGROUPS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE_DEPTH;

public class ChannelPresenter extends ApplicationFinderPresenter<ChannelPresenter.MyView, ChannelPresenter.MyProxy> {

    private final FinderPathFactory finderPathFactory;
    private final Resources resources;
    private final MetadataRegistry metadataRegistry;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;

    private String selectedChannel;
    private String selectedFork;

    @Inject
    public ChannelPresenter(EventBus eventBus, MyView view, MyProxy myProxy, Finder finder,
            FinderPathFactory finderPathFactory, Resources resources,
            MetadataRegistry metadataRegistry, Dispatcher dispatcher, StatementContext statementContext) {
        super(eventBus, view, myProxy, finder);
        this.finderPathFactory = finderPathFactory;
        this.resources = resources;
        this.metadataRegistry = metadataRegistry;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        selectedChannel = request.getParameter(NAME, null);
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    protected void reload() {
        Operation operation = getReadProtocolsOperation(CHANNEL_TEMPLATE.resolve(statementContext, selectedChannel));
        dispatcher.execute(operation, result -> getView().updateChannel(result));
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.runtimeServerPath()
                .append(Ids.RUNTIME_SUBSYSTEM, JGROUPS, resources.constants().monitor(), Names.CHANNELS)
                .append(Ids.JGROUPS_CHANNEL_RUNTIME, selectedChannel, Names.CHANNEL, selectedChannel);
    }

    public void showFork(String fork) {
        this.selectedFork = fork;
        Operation operation = getReadProtocolsOperation(FORK_TEMPLATE.resolve(statementContext, selectedChannel, selectedFork));
        dispatcher.execute(operation, result -> getView().updateFork(result));
    }

    private Operation getReadProtocolsOperation(ResourceAddress address) {
        return new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE_DEPTH, 1)
                .build();
    }

    public Metadata getProtocolMetadata(String protocol) {
        return metadataRegistry.lookup(CHANNEL_PROTOCOL_TEMPLATE.replaceWildcards(selectedChannel, protocol));
    }

    @ProxyCodeSplit
    @NameToken(NameTokens.JGROUPS_CHANNEL)
    @Requires({ CHANNEL_ADDRESS, CHANNEL_PROTOCOL_ADDRESS, FORK_ADDRESS, STACK_PROTOCOL_ADDRESS })
    public interface MyProxy extends ProxyPlace<ChannelPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<ChannelPresenter> {
        void updateChannel(ModelNode channel);

        void updateFork(ModelNode fork);
    }
}
