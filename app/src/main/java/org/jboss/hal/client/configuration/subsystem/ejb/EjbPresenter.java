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
package org.jboss.hal.client.configuration.subsystem.ejb;

import java.util.List;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.HasVerticalNavigation;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVICE;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

/**
 * @author Claudio Miranda
 */
public class EjbPresenter
        extends MbuiPresenter<EjbPresenter.MyView, EjbPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.EJB3)
    @Requires({EJB_SUBSYSTEM_ADDRESS})
    public interface MyProxy extends ProxyPlace<EjbPresenter> {}

    public interface MyView extends MbuiView<EjbPresenter>, HasVerticalNavigation {
        void updateConfiguration(ModelNode conf);
        void updateThreadPool(List<NamedNode> items);
        void updateRemotingProfile(List<NamedNode> items);
        
        void updateBeanPool(List<NamedNode> items);
        
        void updateCache(List<NamedNode> items);
        void updatePassivation(List<NamedNode> items);
        
        void updateServiceAsync(ModelNode node);
        void updateServiceIiop(ModelNode node);
        void updateServiceRemote(ModelNode node);
        void updateServiceTimer(ModelNode node);
        
        void updateMdbDeliveryGroup(List<NamedNode> items);
        
        void updateApplicationSecurityDomain(List<NamedNode> items);
    }
    // @formatter:on

    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Dispatcher dispatcher;

    @Inject
    public EjbPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Finder finder,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            final Dispatcher dispatcher) {
        super(eventBus, view, proxy, finder);
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return EJB_SUBSYSTEM_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.subsystemPath(ModelDescriptionConstants.EJB3);
    }

    @Override
    protected void reload() {
        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION,
                EJB_SUBSYSTEM_TEMPLATE.resolve(statementContext))
                .param(RECURSIVE, true)
                .build();
        dispatcher.execute(operation, result -> {
            // @formatter:off
            getView().updateConfiguration(result);

            getView().updateThreadPool(asNamedNodes(failSafePropertyList(result, THREAD_POOL_TEMPLATE.lastKey())));
            getView().updateRemotingProfile(asNamedNodes(failSafePropertyList(result, REMOTING_PROFILE_TEMPLATE.lastKey())));

            getView().updateBeanPool(asNamedNodes(failSafePropertyList(result, BEAN_POOL_TEMPLATE.lastKey())));

            getView().updateCache(asNamedNodes(failSafePropertyList(result, CACHE_TEMPLATE.lastKey())));
            getView().updatePassivation(asNamedNodes(failSafePropertyList(result, PASSIVATION_TEMPLATE.lastKey())));

            getView().updateServiceAsync(result.get(SERVICE).get(SERVICE_ASYNC_TEMPLATE.lastValue()));
            getView().updateServiceIiop(result.get(SERVICE).get(SERVICE_IIOP_TEMPLATE.lastValue()));
            getView().updateServiceRemote(result.get(SERVICE).get(SERVICE_REMOTE_TEMPLATE.lastValue()));
            getView().updateServiceTimer(result.get(SERVICE).get(SERVICE_TIMER_TEMPLATE.lastValue()));

            getView().updateMdbDeliveryGroup(asNamedNodes(failSafePropertyList(result, MDB_DELIVERY_GROUP_TEMPLATE.lastKey())));

            getView().updateApplicationSecurityDomain(asNamedNodes(failSafePropertyList(result, APP_SEC_DOMAIN_TEMPLATE.lastKey())));
            // @formatter:on
        });
    }
}
