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

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationPresenter;
import org.jboss.hal.core.mvp.PatternFlyView;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.token.NameTokens;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;

/**
 * @author Harald Pehl
 */
public class DeploymentDetailPresenter extends ApplicationPresenter<DeploymentDetailPresenter.MyView, DeploymentDetailPresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.DEPLOYMENT_DETAIL)
    public interface MyProxy extends ProxyPlace<DeploymentDetailPresenter> {}

    public interface MyView extends PatternFlyView {
        void setRoot(ResourceAddress root);
    }
    // @formatter:on

    private final Environment environment;
    private final FinderPathFactory finderPathFactory;
    private ResourceAddress address;

    @Inject
    public DeploymentDetailPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Environment environment,
            final Finder finder,
            final FinderPathFactory finderPathFactory) {
        super(eventBus, view, proxy, finder);
        this.environment = environment;
        this.finderPathFactory = finderPathFactory;
        this.address = ResourceAddress.ROOT;
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        if (!environment.isStandalone()) {
            String serverGroup = request.getParameter(SERVER_GROUP, null);
            String deployment = request.getParameter(DEPLOYMENT, null);
            address = new ResourceAddress().add(SERVER_GROUP, serverGroup).add(DEPLOYMENT, deployment);
        } else {
            String deployment = request.getParameter(DEPLOYMENT, null);
            address = new ResourceAddress().add(DEPLOYMENT, deployment);
        }
    }

    @Override
    protected void onReset() {
        super.onReset();
        getView().setRoot(address);
    }

    @Override
    protected FinderPath finderPath() {
        return finderPathFactory.deployment(address.lastValue());
    }
}
