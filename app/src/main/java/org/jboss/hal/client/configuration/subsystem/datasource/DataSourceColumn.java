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
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.client.configuration.subsystem.datasource.wizard.NewDataSourceWizard;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Message.Level;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.itemText;
import static org.jboss.hal.resources.CSS.subtitle;

/**
 * Column which is used for both XA and normal data sources.
 *
 * @author Harald Pehl
 */
@AsyncColumn(ModelDescriptionConstants.DATA_SOURCE)
@Requires({SELECTED_DATA_SOURCE_ADDRESS, SELECTED_XA_DATA_SOURCE_ADDRESS, JDBC_DRIVER_ADDRESS})
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

        super(new Builder<DataSource>(finder, ModelDescriptionConstants.DATA_SOURCE, Names.DATASOURCE)
                .onPreview(item -> new PreviewContent(item.getName()))
                .useFirstActionAsBreadcrumbHandler());

        this.metadataRegistry = metadataRegistry;
        this.statementContext = statementContext;
        this.environment = environment;
        this.resources = resources;
        this.templates = templates;

        addColumnAction(columnActionFactory.add(IdBuilder.build(ModelDescriptionConstants.DATA_SOURCE, "add"),
                column -> launchNewDataSourceWizard(false)));
        // TODO Add action for XA datasources
        addColumnAction(columnActionFactory.refresh(IdBuilder.build(ModelDescriptionConstants.DATA_SOURCE, "refresh")));

        setItemsProvider((context, callback) -> {
            ResourceAddress dataSourceAddress = DATA_SOURCE_SUBSYSTEM_TEMPLATE.resolve(statementContext);
            Operation dataSourceOperation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, dataSourceAddress)
                    .param(CHILD_TYPE, ModelDescriptionConstants.DATA_SOURCE).build();
            Operation xaDataSourceOperation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION,
                    dataSourceAddress)
                    .param(CHILD_TYPE, ModelDescriptionConstants.XA_DATA_SOURCE).build();
            dispatcher.execute(new Composite(dataSourceOperation, xaDataSourceOperation), (CompositeResult result) -> {
                List<DataSource> combined = new ArrayList<>();
                combined.addAll(Lists.transform(result.step(0).get(RESULT).asPropertyList(),
                        input -> new DataSource(input, false)));
                combined.addAll(Lists.transform(result.step(1).get(RESULT).asPropertyList(),
                        input -> new DataSource(input, true)));
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
                ResourceAddress dataSourceAddress = dataSourceAddress(dataSource);

                String profile = environment.isStandalone() ? STANDALONE : statementContext.selectedProfile();
                PlaceRequest viewRequest = new PlaceRequest.Builder()
                        .nameToken(dataSource.isXa() ? NameTokens.XA_DATA_SOURCE : NameTokens.DATA_SOURCE)
                        .with(PROFILE, profile)
                        .with(NAME, dataSource.getName()) //NON-NLS
                        .build();

                List<ItemAction<DataSource>> actions = new ArrayList<>();
                actions.add(itemActionFactory.view(viewRequest));
                actions.add(itemActionFactory.remove(Names.DATASOURCE, dataSource.getName(),
                        SELECTED_DATA_SOURCE_TEMPLATE, DataSourceColumn.this));
                if (isEnabled(dataSource)) {
                    actions.add(new ItemAction<>(resources.constants().disable(), p -> disable(p, dataSourceAddress)));
                    actions.add(new ItemAction<>(resources.constants().testConnection(),
                            p -> testConnection(p, dataSourceAddress)));
                } else {
                    actions.add(new ItemAction<>(resources.constants().enable(), p -> enable(p, dataSourceAddress)));
                }
                return actions;
            }
        });
    }

    private void launchNewDataSourceWizard(final boolean xa) {
        NewDataSourceWizard wizard = new NewDataSourceWizard(metadataRegistry, environment, resources, templates,
                Collections.emptyList(), Collections.emptyList(), xa);
        wizard.show();
    }

    private ResourceAddress dataSourceAddress(DataSource dataSource) {
        return dataSource.isXa()
                ? SELECTED_XA_DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName())
                : SELECTED_DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName());
    }

    private boolean isEnabled(DataSource datasource) {
        if (!datasource.has(ModelDescriptionConstants.ENABLED)) {
            throw new IllegalStateException("Datasource " + datasource.getName() + " does not have enabled attribute");
        }
        return datasource.get(ENABLED).asBoolean();
    }

    private void disable(final DataSource dataSource, final ResourceAddress address) {
        Window.alert(Names.NYI);
    }

    private void enable(final DataSource dataSource, final ResourceAddress address) {
        Window.alert(Names.NYI);
    }

    private void testConnection(final DataSource dataSource, final ResourceAddress address) {
        Window.alert(Names.NYI);
    }
}
