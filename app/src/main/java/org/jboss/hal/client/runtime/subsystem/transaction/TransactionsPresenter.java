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
package org.jboss.hal.client.runtime.subsystem.transaction;

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

import static org.jboss.hal.client.runtime.subsystem.transaction.AddressTemplates.LOGSTORE_RUNTIME_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.transaction.AddressTemplates.TRANSACTIONS_LOGSTORE_RUNTIME_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.transaction.AddressTemplates.TRANSACTION_RUNTIME_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

public class TransactionsPresenter
        extends ApplicationFinderPresenter<TransactionsPresenter.MyView, TransactionsPresenter.MyProxy>
        implements SupportsExpertMode {

    private final Dispatcher dispatcher;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;

    @Inject
    public TransactionsPresenter(
            EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            Dispatcher dispatcher,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext,
            Resources resources) {
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
                .append(Ids.RUNTIME_SUBSYSTEM, TRANSACTIONS, resources.constants().monitor(), Names.TRANSACTION);
    }

    @Override
    protected void reload() {
        ResourceAddress address = TRANSACTIONS_LOGSTORE_RUNTIME_TEMPLATE.resolve(statementContext);
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE, true)
                .build();
        dispatcher.execute(operation, result -> {
            getView().update(asNamedNodes(result.asPropertyList()));
        });
    }

    void probe() {
        ResourceAddress address = LOGSTORE_RUNTIME_TEMPLATE.resolve(statementContext);
        Operation operation = new Operation.Builder(address, PROBE_OPERATION)
                .build();
        dispatcher.execute(operation, result -> {
            reload();
        });
    }

    StatementContext getStatementContext() {
        return statementContext;
    }


    // @formatter:off
    @ProxyCodeSplit
    @Requires(TRANSACTION_RUNTIME_ADDRESS)
    @NameToken(NameTokens.TRANSACTIONS_RUNTIME)
    public interface MyProxy extends ProxyPlace<TransactionsPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<TransactionsPresenter> {
        void update(List<NamedNode> model);
    }
    // @formatter:on
}
