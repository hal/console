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
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.Message.Level;
import org.jboss.hal.spi.MessageEvent;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
@AsyncColumn(ModelDescriptionConstants.DATA_SOURCE)
public class DataSourceColumn extends FinderColumn<Property> {

    private final MetadataProcessor metadataProcessor;
    private final Provider<Progress> progress;
    private final EventBus eventBus;
    private final StatementContext statementContext;
    private final Environment environment;
    private final Resources resources;
    private final DataSourceTemplates templates;

    @Inject
    public DataSourceColumn(final MetadataProcessor metadataProcessor,
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

        super(new Builder<Property>(finder, ModelDescriptionConstants.DATA_SOURCE, Names.DATASOURCE)
                .onPreview(item -> new PreviewContent(item.getName())));

        this.metadataProcessor = metadataProcessor;
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
            ResourceAddress address = AddressTemplate.of("/{selected.profile}/subsystem=datasources")
                    .resolve(statementContext);
            Operation operation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, address)
                    .param(CHILD_TYPE, resourceType(context.getPath())).build();
            dispatcher.execute(operation, result -> {
                callback.onSuccess(result.asPropertyList());
            });
        });

        setItemRenderer(property -> new ItemDisplay<Property>() {
            @Override
            public String getTitle() {
                return property.getName();
            }

            @Override
            public Level getMarker() {
                return isEnabled(property) ? Level.SUCCESS : Level.INFO;
            }

            @Override
            public String getTooltip() {
                return isEnabled(property) ? resources.constants().enabled() : resources.constants().disabled();
            }

            @Override
            public List<ItemAction<Property>> actions() {
                FinderPath path = finder.getContext().getPath();
                ResourceAddress dataSourceAddress = dataSourceAddress(path, property);

                String profile = environment.isStandalone() ? STANDALONE : statementContext.selectedProfile();
                PlaceRequest viewRequest = new PlaceRequest.Builder()
                        .nameToken(xa(path) ? NameTokens.XA_DATA_SOURCE : NameTokens.DATA_SOURCE)
                        .with(PROFILE, profile)
                        .with(NAME, property.getName()) //NON-NLS
                        .build();

                List<ItemAction<Property>> actions = new ArrayList<>();
                actions.add(itemActionFactory.view(viewRequest));
                actions.add(itemActionFactory.remove(Names.DATASOURCE, property.getName(),
                        DataSourcePresenter.DATA_SOURCE_RESOURCE, DataSourceColumn.this));
                if (isEnabled(property)) {
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
        AddressTemplate template = AddressTemplate.of("/{selected.profile}/subsystem=datasources")
                .append(xa ? "xa-data-source=*" : "data-source=*");
        metadataProcessor.lookup(template, progress.get(), new MetadataProcessor.MetadataCallback() {
            @Override
            public void onError(final Throwable error) {
                MessageEvent.fire(eventBus, Message.error(resources.constants().metadataError(), error.getMessage()));
            }

            @Override
            public void onMetadata(final Metadata metadata) {
                NewDataSourceWizard wizard = new NewDataSourceWizard(environment, metadata, templates,
                        Collections.emptyList(), Collections.emptyList(), resources, xa);
                wizard.show();
            }
        });
    }

    private String resourceType(FinderPath path) {
        return DataSourceTypeColumn.XA.equals(path.last().getValue()) ? XA_DATA_SOURCE : DATA_SOURCE;
    }

    private ResourceAddress dataSourceAddress(FinderPath path, Property property) {
        return xa(path)
                ? DataSourcePresenter.XA_DATA_SOURCE_RESOURCE.resolve(statementContext, property.getName())
                : DataSourcePresenter.DATA_SOURCE_RESOURCE.resolve(statementContext, property.getName());
    }

    private boolean isEnabled(Property datasource) {
        if (!datasource.getValue().has(ModelDescriptionConstants.ENABLED)) {
            throw new IllegalStateException("Datasource " + datasource.getName() + " does not have enabled attribute");
        }
        return datasource.getValue().get(ENABLED).asBoolean();
    }

    private void disable(final Property property, final ResourceAddress address) {
        Window.alert(Names.NYI);
    }

    private void enable(final Property property, final ResourceAddress address) {
        Window.alert(Names.NYI);
    }

    private void testConnection(final Property property, final ResourceAddress address) {
        Window.alert(Names.NYI);
    }
}
