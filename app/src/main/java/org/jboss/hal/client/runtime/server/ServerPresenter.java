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
package org.jboss.hal.client.runtime.server;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.SuccessfulOutcome;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Requires;
import io.reactivex.Completable;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.flow.Flow.series;

public class ServerPresenter
        extends MbuiPresenter<ServerPresenter.MyView, ServerPresenter.MyProxy>
        implements SupportsExpertMode {

    static final String SERVER_CONFIG_ADDRESS = "/{selected.host}/{selected.server-config}";
    static final String INTERFACE_ADDRESS = SERVER_CONFIG_ADDRESS + "/interface=*";
    static final String JVM_ADDRESS = SERVER_CONFIG_ADDRESS + "/jvm=*";
    static final String PATH_ADDRESS = SERVER_CONFIG_ADDRESS + "/path=*";
    static final String SYSTEM_PROPERTY_ADDRESS = SERVER_CONFIG_ADDRESS + "/system-property=*";
    static final String SERVER_RUNTIME_ADDRESS = "/{selected.host}/{selected.server}/core-service=platform-mbean/type=runtime";
    static final AddressTemplate SERVER_RUNTIME_TEMPLATE = AddressTemplate.of(SERVER_RUNTIME_ADDRESS);
    private static final String SERVER_KEY = "server";
    private static final String SERVER_CONFIG_KEY = "server-config";
    private static final String SERVER_RUNTIME_KEY = "server-runtime";


    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.SERVER_CONFIGURATION)
    @Requires(value = {SERVER_CONFIG_ADDRESS,
            INTERFACE_ADDRESS,
            JVM_ADDRESS,
            PATH_ADDRESS,
            SYSTEM_PROPERTY_ADDRESS},
            recursive = false)
    public interface MyProxy extends ProxyPlace<ServerPresenter> {}

    public interface MyView extends MbuiView<ServerPresenter> {
        void updateServer(Server server);
        void updateInterfaces(List<NamedNode> interfaces);
        void updateJvms(List<NamedNode> interfaces);
        void updatePaths(List<NamedNode> interfaces);
        void updateSystemProperties(List<NamedNode> interfaces);
        void updateRuntime(ModelNode modelNode);
    }
    // @formatter:on


    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Dispatcher dispatcher;
    private final ServerActions serverActions;
    private final Provider<Progress> progress;
    private final Resources resources;

    @Inject
    public ServerPresenter(EventBus eventBus,
            ServerPresenter.MyView view,
            ServerPresenter.MyProxy proxy,
            Finder finder,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext,
            Dispatcher dispatcher,
            ServerActions serverActions,
            @Footer Provider<Progress> progress,
            Resources resources) {
        super(eventBus, view, proxy, finder);
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.serverActions = serverActions;
        this.progress = progress;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return AddressTemplate.of(SERVER_CONFIG_ADDRESS).resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.runtimeServerPath();
    }

    @Override
    protected void reload() {
        Task<FlowContext> serverConfigFn = context -> {
            ResourceAddress serverAddress = AddressTemplate.of(SERVER_CONFIG_ADDRESS).resolve(statementContext);
            Operation serverOp = new Operation.Builder(serverAddress, READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            Operation interfacesOp = new Operation.Builder(serverAddress, READ_CHILDREN_RESOURCES_OPERATION)
                    .param(CHILD_TYPE, INTERFACE)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            Operation jvmsOp = new Operation.Builder(serverAddress, READ_CHILDREN_RESOURCES_OPERATION)
                    .param(CHILD_TYPE, JVM)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            Operation pathsOp = new Operation.Builder(serverAddress, READ_CHILDREN_RESOURCES_OPERATION)
                    .param(CHILD_TYPE, PATH)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            Operation systemPropertiesOp = new Operation.Builder(serverAddress, READ_CHILDREN_RESOURCES_OPERATION)
                    .param(CHILD_TYPE, SYSTEM_PROPERTY)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            return dispatcher.execute(new Composite(serverOp, interfacesOp, jvmsOp, pathsOp, systemPropertiesOp))
                    .doOnSuccess((CompositeResult result) -> {
                        Server server = new Server(statementContext.selectedHost(), result.step(0).get(RESULT));
                        context.set(SERVER_KEY, server);
                        context.set(SERVER_CONFIG_KEY, result);
                    })
                    .toCompletable();
        };
        Task<FlowContext> serverRuntimeFn = context -> {
            Server server = context.get(SERVER_KEY);
            if (!serverActions.isPending(server) && server.isRunning()) {
                ResourceAddress address = SERVER_RUNTIME_TEMPLATE.resolve(statementContext);
                Operation serverRuntimeOp = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                        .param(INCLUDE_RUNTIME, true)
                        .build();
                return dispatcher.execute(serverRuntimeOp)
                        .doOnSuccess(result -> context.set(SERVER_RUNTIME_KEY, result))
                        .toCompletable();
            } else {
                return Completable.complete();
            }
        };

        series(new FlowContext(progress.get()), serverConfigFn, serverRuntimeFn)
                .subscribe(new SuccessfulOutcome<FlowContext>(getEventBus(), resources) {
                    @Override
                    public void onSuccess(FlowContext context) {
                        Server server = context.get(SERVER_KEY);
                        getView().updateServer(server);

                        CompositeResult serverConfig = context.get(SERVER_CONFIG_KEY);
                        getView().updateInterfaces(asNamedNodes(serverConfig.step(1).get(RESULT).asPropertyList()));
                        getView().updateJvms(asNamedNodes(serverConfig.step(2).get(RESULT).asPropertyList()));
                        getView().updatePaths(asNamedNodes(serverConfig.step(3).get(RESULT).asPropertyList()));
                        getView().updateSystemProperties(
                                asNamedNodes(serverConfig.step(4).get(RESULT).asPropertyList()));

                        if (context.get(SERVER_RUNTIME_KEY) != null) {
                            ModelNode serverRuntime = context.get(SERVER_RUNTIME_KEY);
                            getView().updateRuntime(serverRuntime);
                        } else {
                            getView().updateRuntime(null);
                        }
                    }
                });
    }
}
