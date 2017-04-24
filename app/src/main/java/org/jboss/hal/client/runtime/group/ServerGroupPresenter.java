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
package org.jboss.hal.client.runtime.group;

import java.util.List;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.core.runtime.group.ServerGroup;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

/**
 * @author Harald Pehl
 */
public class ServerGroupPresenter
        extends MbuiPresenter<ServerGroupPresenter.MyView, ServerGroupPresenter.MyProxy>
        implements SupportsExpertMode {

    static final String SERVER_GROUP_ADDRESS = "/{selected.group}";
    static final String JVM_ADDRESS = SERVER_GROUP_ADDRESS + "/jvm=*";
    static final String SYSTEM_PROPERTY_ADDRESS = SERVER_GROUP_ADDRESS + "/system-property=*";


    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.SERVER_GROUP_CONFIGURATION)
    @Requires(value = {SERVER_GROUP_ADDRESS, JVM_ADDRESS, SYSTEM_PROPERTY_ADDRESS},
            recursive = false)
    public interface MyProxy extends ProxyPlace<ServerGroupPresenter> {}

    public interface MyView extends MbuiView<ServerGroupPresenter> {
        void updateServerGroup(ServerGroup serverGroup);
        void updateJvms(List<NamedNode> interfaces);
        void updateSystemProperties(List<NamedNode> interfaces);
    }
    // @formatter:on


    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Dispatcher dispatcher;

    @Inject
    public ServerGroupPresenter(final EventBus eventBus,
            final ServerGroupPresenter.MyView view,
            final ServerGroupPresenter.MyProxy proxy,
            final Finder finder,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            final Dispatcher dispatcher) {
        super(eventBus, view, proxy, finder);
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return AddressTemplate.of(SERVER_GROUP_ADDRESS).resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.runtimeServerGroupPath();
    }

    @Override
    protected void reload() {
        ResourceAddress serverGroupAddress = AddressTemplate.of(SERVER_GROUP_ADDRESS).resolve(statementContext);
        Operation serverGroupOp = new Operation.Builder(serverGroupAddress, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation jvmsOp = new Operation.Builder(serverGroupAddress, READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, JVM)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation systemPropertiesOp = new Operation.Builder(serverGroupAddress, READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, SYSTEM_PROPERTY)
                .param(INCLUDE_RUNTIME, true)
                .build();

        dispatcher.execute(
                new Composite(serverGroupOp, jvmsOp, systemPropertiesOp),
                (CompositeResult result) -> {
                    getView().updateServerGroup(
                            new ServerGroup(statementContext.selectedServerGroup(), result.step(0).get(RESULT)));
                    getView().updateJvms(asNamedNodes(result.step(1).get(RESULT).asPropertyList()));
                    getView().updateSystemProperties(asNamedNodes(result.step(2).get(RESULT).asPropertyList()));
                });
    }
}

