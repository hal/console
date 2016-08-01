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
package org.jboss.hal.client.runtime.subsystem.jpa;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationPresenter;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.HasVerticalNavigation;
import org.jboss.hal.core.mvp.PatternFlyView;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.runtime.subsystem.jpa.AddressTemplates.JPA_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.jpa.AddressTemplates.JPA_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE;
import static org.jboss.hal.meta.token.NameTokens.JPA_RUNTIME;

/**
 * @author Harald Pehl
 */

public class JpaPresenter extends ApplicationPresenter<JpaPresenter.MyView, JpaPresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(JPA_RUNTIME)
    @Requires(JPA_ADDRESS)
    public interface MyProxy extends ProxyPlace<JpaPresenter> {}

    public interface MyView extends PatternFlyView, HasPresenter<JpaPresenter>, HasVerticalNavigation {
        void update(JpaStatistic statistic);
    }
    // @formatter:on


    private final FinderPathFactory finderPathFactory;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private String resourceName;
    private String deployment;
    private String persistenceUnit;

    @Inject
    public JpaPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy myProxy,
            final Finder finder,
            final FinderPathFactory finderPathFactory,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.finderPathFactory = finderPathFactory;
        this.dispatcher = dispatcher;
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
        persistenceUnit = request.getParameter(NAME, null);
        deployment = request.getParameter(DEPLOYMENT, null);
        resourceName = deployment + "#" + persistenceUnit;
    }

    @Override
    protected FinderPath finderPath() {
        return finderPathFactory.runtimeServerPath()
                .append(Ids.SERVER_MONITOR, Ids.asId(Names.JPA), resources.constants().monitor(), Names.JPA)
                .append(Ids.JPA_RUNTIME, Ids.jpaStatistic(deployment, persistenceUnit), Names.JPA, persistenceUnit);
    }

    @Override
    protected void onReset() {
        super.onReset();
        load();
    }

    void load() {
        ResourceAddress address = JPA_TEMPLATE.resolve(statementContext, deployment, resourceName);
        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION, address)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE, true)
                .build();
        dispatcher.execute(operation, result -> getView().update(new JpaStatistic(address, result)));
    }
}
