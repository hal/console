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
package org.jboss.hal.client.runtime.subsystem.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.runtime.subsystem.transaction.AddressTemplates.TRANSACTIONS_LOGSTORE_RUNTIME_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.webservice.AddressTemplates.WEBSERVICES_DEPLOYMENT_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.webservice.AddressTemplates.WEBSERVICES_DEPLOYMENT_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.webservice.AddressTemplates.WEBSERVICES_SUBDEPLOYMENT_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.webservice.AddressTemplates.WEBSERVICES_SUBDEPLOYMENT_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

public class WebservicesPresenter
        extends ApplicationFinderPresenter<WebservicesPresenter.MyView, WebservicesPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @Requires({WEBSERVICES_DEPLOYMENT_ADDRESS, WEBSERVICES_SUBDEPLOYMENT_ADDRESS})
    @NameToken(NameTokens.WEBSERVICES_RUNTIME)
    public interface MyProxy extends ProxyPlace<WebservicesPresenter> {}

    public interface MyView extends HalView, HasPresenter<WebservicesPresenter> {
        void update(List<NamedNode> model);
    }
    // @formatter:on

    private final Dispatcher dispatcher;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;

    @Inject
    public WebservicesPresenter(
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
    public ResourceAddress resourceAddress() {
        return TRANSACTIONS_LOGSTORE_RUNTIME_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.runtimeServerPath()
                .append(Ids.RUNTIME_SUBSYSTEM, WEBSERVICES, resources.constants().monitor(), Names.WEBSERVICES);
    }

    @Override
    protected void reload() {
        ResourceAddress addressDeploy = WEBSERVICES_DEPLOYMENT_TEMPLATE.resolve(statementContext);
        Operation opDeploy = new Operation.Builder(addressDeploy, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE, true)
                .build();
        ResourceAddress addressSubdeploy = WEBSERVICES_SUBDEPLOYMENT_TEMPLATE.resolve(statementContext);
        Operation opSubDeploy = new Operation.Builder(addressSubdeploy, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE, true)
                .build();
        dispatcher.execute(new Composite(opDeploy, opSubDeploy), (CompositeResult result) -> {

            List<NamedNode> endpoints = new ArrayList<>();
            result.step(0).get(RESULT).asList().forEach(r -> {
                ResourceAddress _address = new ResourceAddress(r.get(ADDRESS));
                String deploymentName = deploymentName(_address);
                List<Property> props = r.get(RESULT).get(ENDPOINT).asPropertyList();
                props.forEach(prop -> {
                    prop.getValue().get(HAL_ADDRESS).set(_address);
                    prop.getValue().get(HAL_DEPLOYMENT).set(deploymentName);
                });
                endpoints.addAll(asNamedNodes(props));
            });
            result.step(1).get(RESULT).asList().forEach(r -> {
                ResourceAddress _address = new ResourceAddress(r.get(ADDRESS));
                String deploymentName = deploymentName(_address);
                List<Property> props = r.get(RESULT).get(ENDPOINT).asPropertyList();
                props.forEach(prop -> {
                    prop.getValue().get(HAL_ADDRESS).set(_address);
                    prop.getValue().get(HAL_DEPLOYMENT).set(deploymentName);
                });
                endpoints.addAll(asNamedNodes(props));
            });
            getView().update(endpoints);
        });
    }

    StatementContext getStatementContext() {
        return statementContext;
    }

    private String deploymentName(ResourceAddress address) {
        String deployment = null;
        String subdeployment = null;
        for (ModelNode segment: address.asList()) {
            if (segment.hasDefined(DEPLOYMENT)) {
                deployment = segment.get(DEPLOYMENT).asString();
            }
            if (segment.hasDefined(SUBDEPLOYMENT)) {
                subdeployment = segment.get(SUBDEPLOYMENT).asString();
            }
        }
        return subdeployment != null ? deployment + "/" + subdeployment : deployment;

    }
}
