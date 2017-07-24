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
package org.jboss.hal.client.deployment;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.client.deployment.DeploymentFunctions.LoadDeploymentsFromRunningServer;
import org.jboss.hal.client.deployment.DeploymentFunctions.ReadServerGroupDeployments;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.deployment.ServerGroupDeployment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.TopologyFunctions.RunningServersQuery;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.deployment.ContentColumn.CONTENT_ADDRESS;
import static org.jboss.hal.client.deployment.ServerGroupDeploymentColumn.SERVER_GROUP_DEPLOYMENT_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;

/**
 * @author Harald Pehl
 */
public class ServerGroupDeploymentPresenter extends
        ApplicationFinderPresenter<ServerGroupDeploymentPresenter.MyView, ServerGroupDeploymentPresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.SERVER_GROUP_DEPLOYMENT)
    @Requires(value = {CONTENT_ADDRESS, SERVER_GROUP_DEPLOYMENT_ADDRESS}, recursive = false)
    public interface MyProxy extends ProxyPlace<ServerGroupDeploymentPresenter> {}

    public interface MyView extends HalView, HasPresenter<ServerGroupDeploymentPresenter> {
        void update(String serverGroup, ServerGroupDeployment serverGroupDeployment);
    }
    // @formatter:on


    private final Environment environment;
    private final FinderPathFactory finderPathFactory;
    private final Dispatcher dispatcher;
    private final PlaceManager placeManager;
    private final Places places;
    private final Provider<Progress> progress;
    private final Resources resources;
    private String serverGroup;
    private String deployment;

    @Inject
    public ServerGroupDeploymentPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Environment environment,
            final Finder finder,
            final FinderPathFactory finderPathFactory,
            final Dispatcher dispatcher,
            final PlaceManager placeManager,
            final Places places,
            @Footer final Provider<Progress> progress,
            final Resources resources) {
        super(eventBus, view, proxy, finder);
        this.environment = environment;
        this.finderPathFactory = finderPathFactory;
        this.dispatcher = dispatcher;
        this.placeManager = placeManager;
        this.places = places;
        this.progress = progress;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        serverGroup = request.getParameter(SERVER_GROUP, null);
        deployment = request.getParameter(DEPLOYMENT, null);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.deployment(deployment);
    }

    @Override
    protected void reload() {
        Function[] functions = new Function[]{
                new ReadServerGroupDeployments(environment, dispatcher, serverGroup, deployment),
                new RunningServersQuery(environment, dispatcher, new ModelNode().set(SERVER_GROUP, serverGroup)),
                new LoadDeploymentsFromRunningServer(environment, dispatcher)
        };

        new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(),
                new Outcome<FunctionContext>() {
                    @Override
                    public void onFailure(final FunctionContext context) {
                        MessageEvent.fire(getEventBus(),
                                Message.error(resources.messages().deploymentReadError(deployment)));
                    }

                    @Override
                    public void onSuccess(final FunctionContext context) {
                        List<ServerGroupDeployment> serverGroupDeployments = context
                                .get(DeploymentFunctions.SERVER_GROUP_DEPLOYMENTS);
                        if (!serverGroupDeployments.isEmpty()) {
                            ServerGroupDeployment serverGroupDeployment = serverGroupDeployments.get(0);
                            getView().update(serverGroup, serverGroupDeployment);
                        } else {
                            MessageEvent.fire(getEventBus(),
                                    Message.error(resources.messages().deploymentReadError(deployment)));
                        }
                    }
                }, functions);
    }

    void goToServerGroup() {
        PlaceRequest serverGroupPlaceRequest = places.finderPlace(NameTokens.RUNTIME, new FinderPath()
                .append(Ids.DOMAIN_BROWSE_BY, Ids.asId(Names.SERVER_GROUPS))
                .append(Ids.SERVER_GROUP, Ids.serverGroup(serverGroup)))
                .build();
        placeManager.revealPlace(serverGroupPlaceRequest);
    }

    void enable(final String deployment) {
        ResourceAddress address = new ResourceAddress().add(SERVER_GROUP, serverGroup).add(DEPLOYMENT, deployment);
        progress.get().reset();
        progress.get().tick();
        Operation operation = new Operation.Builder(address, DEPLOY).build();
        dispatcher.execute(operation, result -> {
            progress.get().finish();
            reload();
            MessageEvent
                    .fire(getEventBus(), Message.success(resources.messages().deploymentEnabledSuccess(deployment)));
        });
    }
}
