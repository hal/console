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
package org.jboss.hal.client.configuration.subsystem.resourceadapter;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.SelectionAwareStatementContext;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.resourceadapter.AddressTemplates.SELECTED_RESOURCE_ADAPTER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESOURCE_ADAPTERS;

/**
 * @author Harald Pehl
 */
public class ResourceAdapterPresenter
        extends MbuiPresenter<ResourceAdapterPresenter.MyView, ResourceAdapterPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.RESOURCE_ADAPTER)
    @Requires(AddressTemplates.RESOURCE_ADAPTER_ADDRESS)
    public interface MyProxy extends ProxyPlace<ResourceAdapterPresenter> {}

    public interface MyView extends MbuiView<ResourceAdapterPresenter> {
        void update(ResourceAdapter resourceAdapter);
    }
    // @formatter:on


    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private String resourceAdapter;

    @Inject
    public ResourceAdapterPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy myProxy,
            final Finder finder,
            final CrudOperations crud,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext) {
        super(eventBus, view, myProxy, finder);
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = new SelectionAwareStatementContext(statementContext, () -> resourceAdapter);
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        resourceAdapter = request.getParameter(NAME, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return SELECTED_RESOURCE_ADAPTER_TEMPLATE.resolve(statementContext);
    }

    String getResourceAdapter() {
        return resourceAdapter;
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.subsystemPath(RESOURCE_ADAPTERS)
                .append(Ids.RESOURCE_ADAPTER, Ids.resourceAdapter(resourceAdapter),
                        Names.RESOURCE_ADAPTER, resourceAdapter);
    }

    @Override
    protected void reload() {
        ResourceAddress address = SELECTED_RESOURCE_ADAPTER_TEMPLATE.resolve(statementContext);
        crud.readRecursive(address, result -> getView().update(new ResourceAdapter(resourceAdapter, result)));
    }
}
