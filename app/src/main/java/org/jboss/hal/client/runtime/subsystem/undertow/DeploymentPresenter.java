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
package org.jboss.hal.client.runtime.subsystem.undertow;

import java.util.List;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_DEPLOYMENT_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_DEPLOYMENT_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_SUBDEPLOYMENT_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_SUBDEPLOYMENT_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

public class DeploymentPresenter
        extends ApplicationFinderPresenter<DeploymentPresenter.MyView, DeploymentPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @Requires({WEB_DEPLOYMENT_ADDRESS, WEB_SUBDEPLOYMENT_ADDRESS})
    @NameToken(NameTokens.UNDERTOW_RUNTIME_DEPLOYMENT_VIEW)
    public interface MyProxy extends ProxyPlace<DeploymentPresenter> {}

    public interface MyView extends HalView, HasPresenter<DeploymentPresenter> {
        void updateServlets(List<NamedNode> model);
        void updateWebsockets(List<NamedNode> model);
    }
    // @formatter:on

    private final Dispatcher dispatcher;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;
    private String deploymentName;
    private String subdeploymentName;

    @Inject
    public DeploymentPresenter(
            final EventBus eventBus,
            final MyView view,
            final MyProxy myProxy,
            final Finder finder,
            final Dispatcher dispatcher,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            final Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.dispatcher = dispatcher;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
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
        deploymentName = request.getParameter(DEPLOYMENT, null);
        subdeploymentName = request.getParameter(SUBDEPLOYMENT, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return WEB_DEPLOYMENT_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        String itemPath = subdeploymentName == null ? deploymentName : deploymentName + "/" + subdeploymentName;
        return finderPathFactory.runtimeServerPath()
                .append(Ids.RUNTIME_SUBSYSTEM, UNDERTOW, resources.constants().monitor(), Names.WEB)
                .append(Ids.UNDERTOW_RUNTIME, Ids.asId(Names.DEPLOYMENT), Names.WEB, Names.DEPLOYMENT)
                .append(Ids.UNDERTOW_RUNTIME_DEPLOYMENT, Ids.asId(itemPath), Names.DEPLOYMENT, itemPath);
    }

    @Override
    protected void reload() {
        ResourceAddress address;
        if (subdeploymentName == null) {
            address = WEB_DEPLOYMENT_TEMPLATE.resolve(statementContext, deploymentName);
        } else {
            address = WEB_SUBDEPLOYMENT_TEMPLATE.resolve(statementContext, deploymentName, subdeploymentName);
        }
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE, true)
                .build();
        dispatcher.execute(operation, result -> {
            getView().updateServlets(asNamedNodes(failSafePropertyList(result, SERVLET)));
            getView().updateWebsockets(asNamedNodes(failSafePropertyList(result, WEBSOCKET)));
        });
    }

    StatementContext getStatementContext() {
        return statementContext;
    }
}
