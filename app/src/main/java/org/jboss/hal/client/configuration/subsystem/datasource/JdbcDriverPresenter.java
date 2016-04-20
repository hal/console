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
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.OperationFactory;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.JDBC_DRIVER_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DATASOURCES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.JDBC_DRIVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * @author Harald Pehl
 */
public class JdbcDriverPresenter extends
        SubsystemPresenter<JdbcDriverPresenter.MyView, JdbcDriverPresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.JDBC_DRIVER)
    @Requires(JDBC_DRIVER_ADDRESS)
    public interface MyProxy extends ProxyPlace<JdbcDriverPresenter> {}

    public interface MyView extends PatternFlyView, HasPresenter<JdbcDriverPresenter> {
        void update(JdbcDriver driver);
    }
    // @formatter:on


    private final Resources resources;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final OperationFactory operationFactory;
    private String name;

    @Inject
    public JdbcDriverPresenter(final EventBus eventBus,
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
        super.prepareFromRequest(request);
        name = request.getParameter(NAME, null);
    }

    @Override
    protected void onReset() {
        super.onReset();
        loadJdbcDriver();
    }

    @Override
    protected FinderPath finderPath() {
        //noinspection HardCodedStringLiteral
        return FinderPath.subsystemPath(statementContext.selectedProfile(), DATASOURCES)
                .append(Ids.DATA_SOURCE_DRIVER_COLUMN, "jdbc-drivers", Names.DATASOURCES_DRIVERS, Names.JDBC_DRIVERS)
                .append(JDBC_DRIVER, name, Names.JDBC_DRIVER);
    }

    private void loadJdbcDriver() {

    }

    void saveJdbcDriver(final Map<String, Object> changedValues) {

    }
}
