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

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.deployment.StandaloneDeploymentColumn.DEPLOYMENT_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
public class StandaloneDeploymentPresenter extends
        ApplicationFinderPresenter<StandaloneDeploymentPresenter.MyView, StandaloneDeploymentPresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.DEPLOYMENT)
    @Requires(value = DEPLOYMENT_ADDRESS, recursive = false)
    public interface MyProxy extends ProxyPlace<StandaloneDeploymentPresenter> {}

    public interface MyView extends HalView, HasPresenter<StandaloneDeploymentPresenter> {
        void reset();
        void update(ModelNode browseContentResult, Deployment deployment);
    }
    // @formatter:on

    private final FinderPathFactory finderPathFactory;
    private final Dispatcher dispatcher;
    private final Provider<Progress> progress;
    private final Resources resources;
    private String deployment;

    @Inject
    public StandaloneDeploymentPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Finder finder,
            final FinderPathFactory finderPathFactory,
            final Dispatcher dispatcher,
            @Footer final Provider<Progress> progress,
            final Resources resources) {
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
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        deployment = request.getParameter(DEPLOYMENT, null);
    }

    public FinderPath finderPath() {
        return finderPathFactory.deployment(deployment);
    }

    @Override
    protected void reload() {
        ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, deployment);
        Operation browseContent = new Operation.Builder(BROWSE_CONTENT, address).build();
        Operation readDeployment = new Operation.Builder(READ_RESOURCE_OPERATION, address)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Composite composite = new Composite(browseContent, readDeployment);
        dispatcher.execute(composite, (CompositeResult result) -> {
            ModelNode readContentResult = result.step(0).get(RESULT);
            Deployment d = new Deployment(Server.STANDALONE, result.step(1).get(RESULT));
            getView().reset();
            getView().update(readContentResult, d);
        });
    }

    void enable(final String deployment) {
        ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, deployment);
        progress.get().reset();
        progress.get().tick();
        Operation operation = new Operation.Builder(DEPLOY, address).build();
        dispatcher.execute(operation, result -> {
            progress.get().finish();
            reload();
            MessageEvent
                    .fire(getEventBus(), Message.success(resources.messages().deploymentEnabledSuccess(deployment)));
        });
    }
}
