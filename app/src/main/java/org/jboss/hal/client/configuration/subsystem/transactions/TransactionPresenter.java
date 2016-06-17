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
package org.jboss.hal.client.configuration.subsystem.transactions;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.client.configuration.PathsTypeahead;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.HasVerticalNavigation;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.transactions.AddressTemplates.TRANSACTIONS_SUBSYSTEM_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.transactions.AddressTemplates.TRANSACTIONS_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE_DEPTH;

/**
 * @author Claudio Miranda
 */
public class TransactionPresenter extends MbuiPresenter<TransactionPresenter.MyView, TransactionPresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.TRANSACTIONS)
    @Requires({TRANSACTIONS_SUBSYSTEM_ADDRESS})
    public interface MyProxy extends ProxyPlace<TransactionPresenter> {}

    public interface MyView extends MbuiView<TransactionPresenter>, HasVerticalNavigation {
        void updateConfiguration(ModelNode conf);
    }
    // @formatter:on


    private final Environment environment;
    private final StatementContext statementContext;
    private final Dispatcher dispatcher;

    @Inject
    public TransactionPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Finder finder,
            final Environment environment,
            final StatementContext statementContext,
            final Dispatcher dispatcher) {
        super(eventBus, view, proxy, finder);
        this.environment = environment;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    protected FinderPath finderPath() {
        return FinderPath
            .subsystemPath(statementContext.selectedProfile(), ModelDescriptionConstants.IO);
    }

    @Override
    protected void reload() {
        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION,
                TRANSACTIONS_SUBSYSTEM_TEMPLATE.resolve(statementContext))
                .param(RECURSIVE_DEPTH, 1)
                .build();
        dispatcher.execute(operation, result -> {
            // @formatter:off
            getView().updateConfiguration(result);
            // @formatter:on
        });

        PathsTypeahead.updateOperation(environment, dispatcher, statementContext);
    }

}
