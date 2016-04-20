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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.Window;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.client.configuration.subsystem.datasource.wizard.NewDataSourceWizard;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Message.Level;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.itemText;
import static org.jboss.hal.resources.CSS.subtitle;

/**
 * Column which is used for both XA and normal data sources.
 *
 * @author Harald Pehl
 */
@AsyncColumn(DATA_SOURCE)
@Requires({DATA_SOURCE_ADDRESS, XA_DATA_SOURCE_ADDRESS, JDBC_DRIVER_ADDRESS})
public class DataSourceColumn extends FinderColumn<DataSource> {

    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private final Environment environment;
    private final Resources resources;
    private final DataSourceTemplates templates;

    @Inject
    public DataSourceColumn(final MetadataRegistry metadataRegistry,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final Environment environment,
            final Resources resources,
            final DataSourceTemplates templates,
            final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory) {

        super(new Builder<DataSource>(finder, DATA_SOURCE, Names.DATASOURCE)
                .useFirstActionAsBreadcrumbHandler());

        this.metadataRegistry = metadataRegistry;
        this.statementContext = statementContext;
        this.environment = environment;
        this.resources = resources;
        this.templates = templates;

        addColumnAction(columnActionFactory.add(IdBuilder.build(DATA_SOURCE, "add"), Names.DATASOURCE,
                column -> launchNewDataSourceWizard(false)));
        addColumnAction(columnActionFactory.add(IdBuilder.build(XA_DATA_SOURCE, "add"), Names.XA_DATASOURCE,
                fontAwesome("credit-card"), column -> launchNewDataSourceWizard(true)));
        addColumnAction(columnActionFactory.refresh(IdBuilder.build(DATA_SOURCE, "refresh")));

        setItemsProvider((context, callback) -> {
            ResourceAddress dataSourceAddress = DATA_SOURCE_SUBSYSTEM_TEMPLATE.resolve(statementContext);
            Operation dataSourceOperation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, dataSourceAddress)
                    .param(CHILD_TYPE, DATA_SOURCE).build();
            Operation xaDataSourceOperation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION,
                    dataSourceAddress)
                    .param(CHILD_TYPE, XA_DATA_SOURCE).build();
            dispatcher.execute(new Composite(dataSourceOperation, xaDataSourceOperation), (CompositeResult result) -> {
                List<DataSource> combined = new ArrayList<>();
                combined.addAll(Lists.transform(result.step(0).get(RESULT).asPropertyList(),
                        property -> new DataSource(property, false)));
                combined.addAll(Lists.transform(result.step(1).get(RESULT).asPropertyList(),
                        property -> new DataSource(property, true)));
                callback.onSuccess(combined);
            });
        });

        setItemRenderer(dataSource -> new ItemDisplay<DataSource>() {
            @Override
            public String getId() {
                return IdBuilder.build(dataSource.getName(), (dataSource.isXa() ? "xa" : "non-xa"));
            }

            @Override
            public Element asElement() {
                //noinspection HardCodedStringLiteral
                return dataSource.isXa()
                        ? new Elements.Builder()
                        .span().css(itemText)
                        .span().textContent(dataSource.getName()).end()
                        .start("small").css(subtitle).textContent(Names.XA_DATASOURCE).end()
                        .end().build()
                        : null;
            }

            @Override
            public String getTitle() {
                return dataSource.getName();
            }

            @Override
            public Level getMarker() {
                return isEnabled(dataSource) ? Level.SUCCESS : Level.INFO;
            }

            @Override
            public String getTooltip() {
                return isEnabled(dataSource) ? resources.constants().enabled() : resources.constants().disabled();
            }

            @Override
            public List<ItemAction<DataSource>> actions() {
                String profile = environment.isStandalone() ? STANDALONE : statementContext.selectedProfile();
                PlaceRequest.Builder builder = new PlaceRequest.Builder()
                        .nameToken(NameTokens.DATA_SOURCE)
                        .with(PROFILE, profile)
                        .with(NAME, dataSource.getName());
                if (dataSource.isXa()) {
                    builder.with(DataSourcePresenter.XA_PARAM, String.valueOf(true));
                }

                List<ItemAction<DataSource>> actions = new ArrayList<>();
                actions.add(itemActionFactory.view(builder.build()));
                actions.add(itemActionFactory.remove(Names.DATASOURCE, dataSource.getName(),
                        DATA_SOURCE_TEMPLATE, DataSourceColumn.this));
                if (isEnabled(dataSource)) {
                    actions.add(new ItemAction<>(resources.constants().disable(), ds -> disable(ds)));
                    actions.add(new ItemAction<>(resources.constants().testConnection(), ds -> testConnection(ds)));
                } else {
                    actions.add(new ItemAction<>(resources.constants().enable(), ds -> enable(ds)));
                }
                return actions;
            }
        });

        setPreviewCallback(item -> new DataSourcePreview(this, item, resources));
    }

    private void launchNewDataSourceWizard(final boolean xa) {
        NewDataSourceWizard wizard = new NewDataSourceWizard(metadataRegistry, environment, resources, templates,
                Collections.emptyList(), Collections.emptyList(), xa);
        wizard.show();
    }

    private ResourceAddress dataSourceAddress(DataSource dataSource) {
        return dataSource.isXa()
                ? XA_DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName())
                : DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName());
    }

    private boolean isEnabled(DataSource datasource) {
        return datasource.hasDefined(ENABLED) && datasource.get(ENABLED).asBoolean();
    }

    void disable(final DataSource dataSource) {
        ResourceAddress dataSourceAddress = dataSourceAddress(dataSource);
        Window.alert(Names.NYI);
    }

    void enable(final DataSource dataSource) {
        ResourceAddress dataSourceAddress = dataSourceAddress(dataSource);
        Window.alert(Names.NYI);
    }

    private void testConnection(final DataSource dataSource) {
        ResourceAddress dataSourceAddress = dataSourceAddress(dataSource);
        Window.alert(Names.NYI);
    }
}
