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
package org.jboss.hal.client.configuration.subsystem.batch;

import java.util.List;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.client.configuration.PathsTypeahead;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.HasVerticalNavigation;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.batch.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

/**
 * @author Claudio Miranda
 */
public class BatchPresenter extends MbuiPresenter<BatchPresenter.MyView, BatchPresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.BATCH_CONFIGURATION)
    @Requires({BATCH_SUBSYSTEM_ADDRESS, IN_MEMORY_JOB_REPO_ADDRESS, JDBC_JOB_REPO_ADDRESS, THREAD_FACTORY_ADDRESS, THREAD_POOL_ADDRESS})
    public interface MyProxy extends ProxyPlace<BatchPresenter> {}

    public interface MyView extends MbuiView<BatchPresenter>, HasVerticalNavigation {
        void updateConfiguration(ModelNode conf);
        void updateInMemoryJobRepository(List<NamedNode> items);
        void updateJdbcJobRepository(List<NamedNode> items);
        void updateThreadFactory(List<NamedNode> items);
        void updateThreadPool(List<NamedNode> items);
    }
    // @formatter:on

    private final FinderPathFactory finderPathFactory;
    private final Environment environment;
    private final StatementContext statementContext;
    private final Dispatcher dispatcher;

    @Inject
    public BatchPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Finder finder,
            final FinderPathFactory finderPathFactory,
            final Environment environment,
            final StatementContext statementContext,
            final Dispatcher dispatcher) {
        super(eventBus, view, proxy, finder);
        this.finderPathFactory = finderPathFactory;
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
        return finderPathFactory.subsystemPath(ModelDescriptionConstants.BATCH_JBERET);
    }

    @Override
    protected void reload() {
        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION,
                BATCH_SUBSYSTEM_TEMPLATE.resolve(statementContext))
                .param(RECURSIVE, true)
                .build();
        dispatcher.execute(operation, result -> {
            // @formatter:off
            getView().updateConfiguration(result);
            getView().updateInMemoryJobRepository(asNamedNodes(failSafePropertyList(result, IN_MEMORY_JOB_REPO_TEMPLATE.lastKey())));
            getView().updateJdbcJobRepository(asNamedNodes(failSafePropertyList(result, JDBC_JOB_REPO_TEMPLATE.lastKey())));
            getView().updateThreadFactory(asNamedNodes(failSafePropertyList(result, THREAD_FACTORY_TEMPLATE.lastKey())));
            getView().updateThreadPool(asNamedNodes(failSafePropertyList(result, THREAD_POOL_TEMPLATE.lastKey())));
            // @formatter:on
        });

        PathsTypeahead.updateOperation(environment, dispatcher, statementContext);
    }
}
