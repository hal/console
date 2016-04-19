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
package org.jboss.hal.client.configuration.subsystem.datasource;

import java.util.Map;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.PatternFlyView;
import org.jboss.hal.core.mvp.SubsystemPresenter;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.OperationFactory;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.DATA_SOURCE_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.XA_DATA_SOURCE_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DATA_SOURCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

/**
 * Presenter which is used for both XA and normal data sources.
 * @author Harald Pehl
 */
public class DataSourcePresenter extends
        SubsystemPresenter<DataSourcePresenter.MyView, DataSourcePresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.DATA_SOURCE)
    @Requires({DATA_SOURCE_ADDRESS, XA_DATA_SOURCE_ADDRESS})
    public interface MyProxy extends ProxyPlace<DataSourcePresenter> {}

    public interface MyView extends PatternFlyView, HasPresenter<DataSourcePresenter> {
        void update(String name, ModelNode datasource);
    }
    // @formatter:on


    static final String XA_PARAM = "xa";

    private final Resources resources;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final OperationFactory operationFactory;
    private String datasource;

    @Inject
    public DataSourcePresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Finder finder,
            final Resources resources,
            final Dispatcher dispatcher,
            final StatementContext statementContext) {
        super(eventBus, view, proxy, finder);
        this.resources = resources;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.operationFactory = new OperationFactory();
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        // TODO error handling when datasource is invalid or null
        datasource = request.getParameter(NAME, null);
    }

    @Override
    protected void onReset() {
        super.onReset();
        loadDataSource();
    }

    @Override
    protected FinderPath finderPath() {
        return FinderPath.subsystemPath(statementContext.selectedProfile(), ModelDescriptionConstants.DATASOURCES)
                .append(DATA_SOURCE, datasource, Names.DATASOURCE);
    }

    private void loadDataSource() {
        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION,
                AddressTemplates.DATA_SOURCE_TEMPLATE.resolve(statementContext, datasource)).build();
        dispatcher.execute(operation, result -> getView().update(datasource, result));
    }

    void saveDataSource(final Map<String, Object> changedValues) {
        ResourceAddress resourceAddress = AddressTemplates.DATA_SOURCE_TEMPLATE.resolve(statementContext, datasource);
        Composite composite = operationFactory.fromChangeSet(resourceAddress, changedValues);

        dispatcher.execute(composite, (CompositeResult result) -> {
            MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().modifyResourceSuccess(Names.DATASOURCE, datasource)));
            loadDataSource();
        });
    }
}
