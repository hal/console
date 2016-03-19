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

import com.google.common.collect.Lists;
import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.client.configuration.subsystem.datasource.wizard.NewDataSourceWizard;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message.Level;
import org.jboss.hal.spi.Requires;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Column which is used for both XA and normal data sources.
 *
 * @author Harald Pehl
 */
@AsyncColumn(ModelDescriptionConstants.DATA_SOURCE)
@Requires({DATA_SOURCE_ADDRESS, XA_DATA_SOURCE_ADDRESS, JDBC_DRIVER_ADDRESS})
public class DataSourceColumn extends FinderColumn<DataSource> {

    private final MetadataRegistry metadataRegistry;
    private final Provider<Progress> progress;
    private final EventBus eventBus;
    private final StatementContext statementContext;
    private final Environment environment;
    private final Resources resources;
    private final DataSourceTemplates templates;

    @Inject
    public DataSourceColumn(final MetadataRegistry metadataRegistry,
            @Footer final Provider<Progress> progress,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final StatementContext statementContext,
            final Environment environment,
            final Resources resources,
            final DataSourceTemplates templates,
            final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory) {

        super(new Builder<DataSource>(finder, ModelDescriptionConstants.DATA_SOURCE, Names.DATASOURCE)
                .onPreview(item -> new PreviewContent(item.getName())));
        this.metadataRegistry = metadataRegistry;
        this.progress = progress;
        this.eventBus = eventBus;
        this.statementContext = statementContext;
        this.environment = environment;
        this.resources = resources;
        this.templates = templates;

        addColumnAction(columnActionFactory.add(IdBuilder.build(ModelDescriptionConstants.DATA_SOURCE, "add"),
                column -> launchNewDataSourceWizard(xa(finder.getContext().getPath()))));
        addColumnAction(columnActionFactory.refresh(IdBuilder.build(ModelDescriptionConstants.DATA_SOURCE, "refresh")));

        setItemsProvider((context, callback) -> {
            ResourceAddress address = DATA_SOURCE_SUBSYSTEM_TEMPLATE.resolve(statementContext);
            Operation operation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, address)
                    .param(CHILD_TYPE, resourceType(context.getPath())).build();
            dispatcher.execute(operation, result -> {
                callback.onSuccess(Lists.transform(result.asPropertyList(),
                        input -> new DataSource(input, xa(finder.getContext().getPath()))));
            });
        });

        setItemRenderer(dataSource -> new ItemDisplay<DataSource>() {
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
                FinderPath path = finder.getContext().getPath();
                ResourceAddress dataSourceAddress = dataSourceAddress(path, dataSource);

                String profile = environment.isStandalone() ? STANDALONE : statementContext.selectedProfile();
                PlaceRequest viewRequest = new PlaceRequest.Builder()
                        .nameToken(xa(path) ? NameTokens.XA_DATA_SOURCE : NameTokens.DATA_SOURCE)
                        .with(PROFILE, profile)
                        .with(NAME, dataSource.getName()) //NON-NLS
                        .build();

                List<ItemAction<DataSource>> actions = new ArrayList<>();
                actions.add(itemActionFactory.view(viewRequest));
                actions.add(itemActionFactory.remove(Names.DATASOURCE, dataSource.getName(),
                        DATA_SOURCE_TEMPLATE, DataSourceColumn.this));
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

    private boolean xa(FinderPath path) {
        return DataSourceTypeColumn.XA.equals(path.last().getValue());
    }

    private void launchNewDataSourceWizard(final boolean xa) {
        NewDataSourceWizard wizard = new NewDataSourceWizard(metadataRegistry, environment, resources, templates,
                Collections.emptyList(), Collections.emptyList(), xa);
        wizard.show();
    }

    private String resourceType(FinderPath path) {
        return DataSourceTypeColumn.XA.equals(path.last().getValue()) ? XA_DATA_SOURCE : DATA_SOURCE;
    }

    private ResourceAddress dataSourceAddress(FinderPath path, DataSource dataSource) {
        return xa(path)
                ? XA_DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName())
                : DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName());
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
