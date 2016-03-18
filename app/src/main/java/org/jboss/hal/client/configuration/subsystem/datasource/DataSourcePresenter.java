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

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.ProfileSelectionEvent;
import org.jboss.hal.core.mvp.ApplicationPresenter;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.PatternFlyView;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.OperationFactory;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.spi.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
public class DataSourcePresenter extends
        ApplicationPresenter<DataSourcePresenter.MyView, DataSourcePresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.DATA_SOURCE)
    @Requires(AddressTemplates.DATA_SOURCE_ADDRESS)
    public interface MyProxy extends ProxyPlace<DataSourcePresenter> {}

    public interface MyView extends PatternFlyView, HasPresenter<DataSourcePresenter> {
        void update(String name, ModelNode datasource);
    }
    // @formatter:on


    private static final Logger logger = LoggerFactory.getLogger(DataSourcePresenter.class);

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final OperationFactory operationFactory;
    private String datasource;

    @Inject
    public DataSourcePresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Dispatcher dispatcher,
            final StatementContext statementContext) {
        super(eventBus, view, proxy);
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
        super.prepareFromRequest(request);
        String profile = request.getParameter(PROFILE, null);
        if (profile != null && !STANDALONE.equals(profile)) {
            getEventBus().fireEvent(new ProfileSelectionEvent(profile));
        }
        datasource = request.getParameter(NAME, null);
        // TODO error handling when profile / datasource is invalid or null
    }

    @Override
    protected void onReset() {
        super.onReset();
        loadDataSource();
    }

    private void loadDataSource() {
        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION,
                AddressTemplates.DATA_SOURCE_TEMPLATE.resolve(statementContext, datasource)).build();
        dispatcher.execute(operation, result -> getView().update(datasource, result));
    }

    void saveDataSource(final Map<String, Object> changedValues) {
        logger.debug("About to save changes for {}: {}", datasource, changedValues); //NON-NLS

        ResourceAddress resourceAddress = AddressTemplates.DATA_SOURCE_TEMPLATE.resolve(statementContext, datasource);
        Composite composite = operationFactory.fromChangeSet(resourceAddress, changedValues);

        dispatcher.execute(composite, (CompositeResult result) -> {
            logger.debug("Datasource {} successfully modified", datasource); //NON-NLS
            loadDataSource();
        });
    }
}
