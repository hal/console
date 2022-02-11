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
package org.jboss.hal.client.runtime.subsystem.datasource;

import javax.inject.Inject;

import org.jboss.hal.core.datasource.DataSource;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
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

import static org.jboss.hal.client.runtime.subsystem.datasource.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.meta.AddressTemplate.OPTIONAL;
import static org.jboss.hal.meta.token.NameTokens.DATA_SOURCE_RUNTIME;

public class DataSourcePresenter
        extends ApplicationFinderPresenter<DataSourcePresenter.MyView, DataSourcePresenter.MyProxy> {

    static final String XA_PARAM = "xa";

    private final FinderPathFactory finderPathFactory;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private String name;
    private boolean xa;

    @Inject
    public DataSourcePresenter(EventBus eventBus,
            MyView view,
            MyProxy myProxy,
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
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        name = request.getParameter(NAME, null);
        xa = Boolean.valueOf(request.getParameter(XA_PARAM, String.valueOf(false)));
        getView().setup();
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.runtimeServerPath()
                .append(Ids.RUNTIME_SUBSYSTEM, DATASOURCES, resources.constants().monitor(), Names.DATASOURCES)
                .append(Ids.DATA_SOURCE_RUNTIME, Ids.dataSourceRuntime(name, xa), Names.DATASOURCE, name);
    }

    @Override
    protected void reload() {
        ResourceAddress address = xa ? XA_DATA_SOURCE_TEMPLATE.resolve(statementContext, name)
                : DATA_SOURCE_TEMPLATE
                        .resolve(statementContext, name);
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE, true)
                .build();
        dispatcher.execute(operation, result -> getView().update(new DataSource(name, result, xa)));
    }

    String getDataSource() {
        return name;
    }

    boolean isXa() {
        return xa;
    }

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(DATA_SOURCE_RUNTIME)
    @Requires({ DATA_SOURCE_ADDRESS, XA_DATA_SOURCE_ADDRESS,
            OPTIONAL + DATA_SOURCE_POOL_ADDRESS,
            OPTIONAL + DATA_SOURCE_JDBC_ADDRESS,
            OPTIONAL + XA_DATA_SOURCE_POOL_ADDRESS,
            OPTIONAL + XA_DATA_SOURCE_JDBC_ADDRESS })
    public interface MyProxy extends ProxyPlace<DataSourcePresenter> {
    }

    public interface MyView extends HalView, HasPresenter<DataSourcePresenter> {
        void setup();

        void update(DataSource dataSource);
    }
    // @formatter:on
}
