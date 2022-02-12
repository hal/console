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
package org.jboss.hal.client.deployment;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.hal.core.deployment.Deployment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import static org.jboss.hal.client.deployment.StandaloneDeploymentColumn.DEPLOYMENT_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

public class StandaloneDeploymentPresenter extends
        ApplicationFinderPresenter<StandaloneDeploymentPresenter.MyView, StandaloneDeploymentPresenter.MyProxy> {

    private final FinderPathFactory finderPathFactory;
    private final Dispatcher dispatcher;
    private final Provider<Progress> progress;
    private final Resources resources;
    private String deployment;

    @Inject
    public StandaloneDeploymentPresenter(EventBus eventBus,
            MyView view,
            MyProxy proxy,
            Finder finder,
            FinderPathFactory finderPathFactory,
            Dispatcher dispatcher,
            @Footer Provider<Progress> progress,
            Resources resources) {
        super(eventBus, view, proxy, finder);
        this.finderPathFactory = finderPathFactory;
        this.dispatcher = dispatcher;
        this.progress = progress;
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
        deployment = request.getParameter(DEPLOYMENT, null);
    }

    public FinderPath finderPath() {
        return finderPathFactory.deployment(deployment);
    }

    @Override
    protected void reload() {
        reload(0);
    }

    private void reload(int tab) {
        ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, deployment);
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .build();
        dispatcher.execute(operation, result -> {
            getView().reset();
            getView().update(new Deployment(Server.STANDALONE, result), tab);
        });
    }

    void enable(String deployment) {
        progress.get().reset();
        progress.get().tick();
        ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, deployment);
        Operation operation = new Operation.Builder(address, DEPLOY).build();
        dispatcher.execute(operation, result -> {
            progress.get().finish();
            MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().deploymentEnabledSuccess(deployment)));
            reload(1);
        });
    }

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.DEPLOYMENT)
    @Requires(value = DEPLOYMENT_ADDRESS, recursive = false)
    public interface MyProxy extends ProxyPlace<StandaloneDeploymentPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<StandaloneDeploymentPresenter> {
        void reset();

        void update(Deployment deployment, int tab);
    }
    // @formatter:on
}
