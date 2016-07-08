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
package org.jboss.hal.client.configuration;

import java.util.List;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.meta.token.NameTokens.PATH;

/**
 * @author Harald Pehl
 */
public class PathsPresenter extends MbuiPresenter<PathsPresenter.MyView, PathsPresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(PATH)
    @Requires("/path=*")
    public interface MyProxy extends ProxyPlace<PathsPresenter> {}

    public interface MyView extends MbuiView<PathsPresenter> {
        void update(List<NamedNode> paths);
    }
    // @formatter:on


    private final Environment environment;
    private final StatementContext statementContext;
    private final Dispatcher dispatcher;

    @Inject
    public PathsPresenter(final EventBus eventBus,
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
        return new FinderPath()
                .append(Ids.CONFIGURATION, IdBuilder.asId(Names.PATHS), Names.CONFIGURATION, Names.PATHS);
    }

    @Override
    protected void reload() {
        Operation operation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, ResourceAddress.ROOT)
                .param(CHILD_TYPE, "path")
                .build();
        dispatcher.execute(operation, result -> getView().update(asNamedNodes(result.asPropertyList())));

        PathsTypeahead.updateOperation(environment, dispatcher, statementContext);
    }
}
