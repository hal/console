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
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationPresenter;
import org.jboss.hal.core.mvp.PatternFlyView;
import org.jboss.hal.core.runtime.TopologyFunctions;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.spi.Footer;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;

/**
 * @author Harald Pehl
 */
public class DeploymentDetailPresenter
        extends ApplicationPresenter<DeploymentDetailPresenter.MyView, DeploymentDetailPresenter.MyProxy> {

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
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Provider<Progress> progress;
    private String deployment;
    private ResourceAddress address;

    @Inject
    public DeploymentDetailPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Environment environment,
            final Finder finder,
            final FinderPathFactory finderPathFactory,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            @Footer final Provider<Progress> progress) {
        super(eventBus, view, proxy, finder);
        this.environment = environment;
        this.finderPathFactory = finderPathFactory;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.progress = progress;
        this.address = ResourceAddress.ROOT;
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        if (!environment.isStandalone()) {
            String serverGroup = request.getParameter(SERVER_GROUP, null);
            deployment = request.getParameter(DEPLOYMENT, null);
            address = new ResourceAddress().add(SERVER_GROUP, serverGroup).add(DEPLOYMENT, deployment);
        } else {
            deployment = request.getParameter(DEPLOYMENT, null);
            address = new ResourceAddress().add(DEPLOYMENT, deployment);
        }
    }

    @Override
    protected void onReset() {
        super.onReset();
        if (environment.isStandalone()) {
            getView().setRoot(address);

        } else {
            new Async<FunctionContext>(progress.get()).single(new FunctionContext(),
                    new Outcome<FunctionContext>() {
                        @Override
                        public void onFailure(final FunctionContext context) {
                            getView().setRoot(address);
                        }

                        @Override
                        public void onSuccess(final FunctionContext context) {
                            List<Server> servers = context.get(TopologyFunctions.RUNNING_SERVERS);
                            if (!servers.isEmpty()) {
                                address = servers.get(0).getServerAddress()
                                        .add(DEPLOYMENT, deployment);
                            }
                            getView().setRoot(address);
                        }
                    },
                    new TopologyFunctions.RunningServersQuery(environment, dispatcher,
                            new ModelNode().set(SERVER_GROUP, statementContext.selectedServerGroup())));
        }
    }

    @Override
    protected FinderPath finderPath() {
        return finderPathFactory.deployment(address.lastValue());
    }
}
