/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.runtime.subsystem.resourceadapter;

import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import static org.jboss.hal.client.runtime.subsystem.resourceadapter.AddressTemplates.EXTENDED_STATISTICS;
import static org.jboss.hal.client.runtime.subsystem.resourceadapter.AddressTemplates.POOL_STATISTICS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESOURCE_ADAPTERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;

public abstract class BasePresenter<V extends BasePresenter.MyView<?>, P extends BasePresenter.MyProxy<?>>
        extends ApplicationFinderPresenter<V, P> {

    private final FinderPathFactory finderPathFactory;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private String resourceName;
    private String parentName;
    private StatisticsResource.ResourceType type;

    public BasePresenter(EventBus eventBus,
            V view,
            P myProxy,
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
        parentName = request.getParameter("parent", null);
        resourceName = request.getParameter(NAME, null);
        type = StatisticsResource.ResourceType.valueOf(request.getParameter(TYPE, null));
        getView().setup();
    }

    @Override
    public FinderPath finderPath() {
        FinderPath path = finderPathFactory.runtimeServerPath()
                .append(Ids.RUNTIME_SUBSYSTEM, RESOURCE_ADAPTERS, resources.constants().monitor(),
                        Names.RESOURCE_ADAPTERS);

        String raName = type == StatisticsResource.ResourceType.RESOURCE_ADAPTER ? resourceName : parentName;
        path.append(Ids.RESOURCE_ADAPTER_RUNTIME, Ids.resourceAdapterRuntime(raName), Names.RESOURCE_ADAPTER, raName);

        if (type != StatisticsResource.ResourceType.RESOURCE_ADAPTER) {
            path.append(Ids.RESOURCE_ADAPTER_CHILD_RUNTIME, Ids.resourceAdapterChildRuntime(parentName, resourceName),
                    Names.RESOURCE_ADAPTER + " Child", resourceName);
        }
        return path;
    }

    @Override
    protected void reload() {
        ResourceAddress address = getResourceAddress().resolve(statementContext);
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE, true)
                .build();
        dispatcher.execute(operation,
                result -> getView().update(new StatisticsResource(parentName, resourceName, type, result)));
    }

    public StatisticsResource.ResourceType getType() {
        return type;
    }

    public AddressTemplate getResourceAddress() {
        AddressTemplate template = type.getTemplate();
        return type == StatisticsResource.ResourceType.RESOURCE_ADAPTER
                ? template.replaceWildcards(resourceName)
                : template.replaceWildcards(parentName, resourceName);
    }

    public AddressTemplate getExtendedStatsAddress() {
        return getResourceAddress().append(EXTENDED_STATISTICS);
    }

    public AddressTemplate getPoolStatsAddress() {
        return getResourceAddress().append(POOL_STATISTICS);
    }

    public interface MyProxy<P extends BasePresenter<?, ?>> extends ProxyPlace<P> {
    }

    public interface MyView<P extends BasePresenter<?, ?>> extends HalView, HasPresenter<P> {
        void setup();

        void update(StatisticsResource resource);
    }
}
