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
package org.jboss.hal.core.mbui;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.PropertiesOperations;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.core.OperationFactory;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;

/**
 * Holds common classes needed by UI views.
 *
 * @author Harald Pehl
 */
public class MbuiContext {

    private final Dispatcher dispatcher;
    private final CrudOperations crud;
    private final PropertiesOperations po;
    private final Environment environment;
    private final EventBus eventBus;
    private final MetadataRegistry metadataRegistry;
    private final OperationFactory operationFactory;
    private final Provider<Progress> progress;
    private final Resources resources;
    private final StatementContext statementContext;
    private final TableButtonFactory tableButtonFactory;

    @Inject
    public MbuiContext(final CrudOperations crud,
            final PropertiesOperations po,
            final Dispatcher dispatcher,
            final Environment environment,
            final EventBus eventBus,
            final MetadataRegistry metadataRegistry,
            final OperationFactory operationFactory,
            final @Footer Provider<Progress> progress,
            final Resources resources,
            final StatementContext statementContext,
            final TableButtonFactory tableButtonFactory) {
        this.crud = crud;
        this.po = po;
        this.environment = environment;
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.metadataRegistry = metadataRegistry;
        this.operationFactory = operationFactory;
        this.progress = progress;
        this.resources = resources;
        this.statementContext = statementContext;
        this.tableButtonFactory = tableButtonFactory;
    }

    public CrudOperations crud() {
        return crud;
    }

    public PropertiesOperations po() {
        return po;
    }

    public Dispatcher dispatcher() {
        return dispatcher;
    }

    public Environment environment() {
        return environment;
    }

    public EventBus eventBus() {
        return eventBus;
    }

    public MetadataRegistry metadataRegistry() {
        return metadataRegistry;
    }

    public OperationFactory operationFactory() {
        return operationFactory;
    }

    public Provider<Progress> progress() {
        return progress;
    }

    public Resources resources() {
        return resources;
    }

    public StatementContext statementContext() {
        return statementContext;
    }

    public TableButtonFactory tableButtonFactory() {
        return tableButtonFactory;
    }
}
