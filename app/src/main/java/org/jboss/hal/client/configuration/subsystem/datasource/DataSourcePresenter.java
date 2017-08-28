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
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.datasource.DataSource;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.DATA_SOURCE_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.DATA_SOURCE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.XA_DATA_SOURCE_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.XA_DATA_SOURCE_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DATASOURCES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/** Presenter which is used for both XA and normal data sources. */
public class DataSourcePresenter
        extends ApplicationFinderPresenter<DataSourcePresenter.MyView, DataSourcePresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.DATA_SOURCE_CONFIGURATION)
    @Requires({DATA_SOURCE_ADDRESS, XA_DATA_SOURCE_ADDRESS})
    public interface MyProxy extends ProxyPlace<DataSourcePresenter> {}

    public interface MyView extends HalView, HasPresenter<DataSourcePresenter> {
        void clear(boolean xa);
        void update(DataSource dataSource);
    }
    // @formatter:on


    static final String XA_PARAM = "xa";

    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private String name;
    private boolean xa;

    @Inject
    public DataSourcePresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Finder finder,
            final CrudOperations crud,
            final FinderPathFactory finderPathFactory,
            final MetadataRegistry metadataRegistry,
            final StatementContext statementContext) {
        super(eventBus, view, proxy, finder);
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.metadataRegistry = metadataRegistry;
        this.statementContext = statementContext;
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
        xa = Boolean.valueOf(request.getParameter(XA_PARAM, String.valueOf(false)));
    }

    @Override
    public ResourceAddress resourceAddress() {
        return xa
                ? XA_DATA_SOURCE_TEMPLATE.resolve(statementContext, name)
                : DATA_SOURCE_TEMPLATE.resolve(statementContext, name);
    }

    @Override
    protected void onReset() {
        super.onReset();
        reload();
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(DATASOURCES)
                .append(Ids.DATA_SOURCE_DRIVER, DATASOURCES, Names.DATASOURCES_DRIVERS, Names.DATASOURCES)
                .append(Ids.DATA_SOURCE_CONFIGURATION, Ids.dataSourceConfiguration(name, xa), Names.DATASOURCE, name);
    }

    @Override
    protected void reload() {
        crud.read(resourceAddress(), result -> getView().update(new DataSource(name, result, xa)));
    }

    void saveDataSource(final Map<String, Object> changedValues) {
        crud.save(type(), name, resourceAddress(), changedValues, metadata(), this::reload);
    }

    void resetDataSource(final Form<DataSource> form) {
        crud.reset(type(), name, resourceAddress(), form, metadata(), this::reload);
    }

    private String type() {
        return xa ? Names.DATASOURCE : Names.XA_DATASOURCE;
    }

    private Metadata metadata() {
        return metadataRegistry.lookup(xa ? XA_DATA_SOURCE_TEMPLATE : DATA_SOURCE_TEMPLATE);
    }
}
