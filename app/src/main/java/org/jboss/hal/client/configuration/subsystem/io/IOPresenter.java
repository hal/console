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
package org.jboss.hal.client.configuration.subsystem.io;

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
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.HasVerticalNavigation;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.io.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE_DEPTH;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

/**
 * @author Claudio Miranda
 */
public class IOPresenter extends MbuiPresenter<IOPresenter.MyView, IOPresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.IO)
    @Requires({IO_SUBSYSTEM_ADDRESS, BUFFER_POOL_ADDRESS, WORKER_ADDRESS})
    public interface MyProxy extends ProxyPlace<IOPresenter> {}

    public interface MyView extends MbuiView<IOPresenter>, HasVerticalNavigation {
        void updateBufferPool(List<NamedNode> items);
        void updateWorkers(List<NamedNode> items);
    }
    // @formatter:on


    private final Environment environment;
    private final StatementContext statementContext;
    private final Dispatcher dispatcher;

    @Inject
    public IOPresenter(final EventBus eventBus,
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
                IO_SUBSYSTEM_TEMPLATE.resolve(statementContext))
                .param(RECURSIVE_DEPTH, 2)
                .build();
        dispatcher.execute(operation, result -> {
            // @formatter:off
            getView().updateBufferPool(asNamedNodes(failSafePropertyList(result, BUFFER_POOL_TEMPLATE.lastKey())));
            getView().updateWorkers(asNamedNodes(failSafePropertyList(result, WORKER_TEMPLATE.lastKey())));
            // @formatter:on
        });

        PathsTypeahead.updateOperation(environment, dispatcher, statementContext);
    }

}
