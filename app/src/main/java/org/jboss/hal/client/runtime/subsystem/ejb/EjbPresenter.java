/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.runtime.subsystem.ejb;

import javax.inject.Inject;

import org.jboss.hal.client.runtime.subsystem.ejb.EjbNode.Type;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import static org.jboss.hal.client.runtime.subsystem.ejb.AddressTemplates.EJB3_DEPLOYMENT_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.ejb.AddressTemplates.EJB3_DEPLOYMENT_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.ejb.AddressTemplates.EJB3_SUBDEPLOYMENT_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.meta.token.NameTokens.EJB3_RUNTIME;

public class EjbPresenter extends ApplicationFinderPresenter<EjbPresenter.MyView, EjbPresenter.MyProxy> implements
        SupportsExpertMode {

    private final FinderPathFactory finderPathFactory;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private String deployment;
    private String subdeployment;
    private Type type;
    private String ejb;

    @Inject
    public EjbPresenter(EventBus eventBus,
            EjbPresenter.MyView view,
            EjbPresenter.MyProxy myProxy,
            Finder finder,
            FinderPathFactory finderPathFactory,
            Dispatcher dispatcher,
            StatementContext statementContext,
            Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.finderPathFactory = finderPathFactory;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resources = resources;
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        deployment = request.getParameter(DEPLOYMENT, null);
        subdeployment = request.getParameter(SUBDEPLOYMENT, null);
        String typeParameter = request.getParameter(TYPE, null);
        type = typeParameter != null ? Type.valueOf(typeParameter.toUpperCase()) : null;
        ejb = request.getParameter(NAME, null);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.runtimeServerPath()
                .append(Ids.RUNTIME_SUBSYSTEM, EJB3, resources.constants().monitor(), Names.EJB3)
                .append(Ids.EJB3, Ids.ejb3(deployment, subdeployment, type.name().toLowerCase(), ejb), Names.EJB3, ejb);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return ejbAddress();
    }

    @Override
    protected void reload() {
        ResourceAddress address = ejbAddress();
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE, true)
                .build();
        dispatcher.execute(operation, result -> getView().update(new EjbNode(address, result)));
    }

    private ResourceAddress ejbAddress() {
        ResourceAddress address;
        if (subdeployment == null) {
            address = EJB3_DEPLOYMENT_TEMPLATE
                    .append(type.resource + "=*")
                    .resolve(statementContext, deployment, ejb);
        } else {
            address = EJB3_SUBDEPLOYMENT_TEMPLATE
                    .append(type.resource + "=*")
                    .resolve(statementContext, deployment, subdeployment, ejb);
        }
        return address;
    }

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(EJB3_RUNTIME)
    @Requires(EJB3_DEPLOYMENT_ADDRESS)
    public interface MyProxy extends ProxyPlace<EjbPresenter> {
    }

    public interface MyView extends HalView {
        void update(EjbNode ejb);
    }
    // @formatter:on
}
