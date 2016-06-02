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

import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.OperationFactory;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;

/**
 * Holds common classes needed by generated MBUI views.
 *
 * @author Harald Pehl
 */
public class MbuiContext {

    private final EventBus eventBus;
    private final MetadataRegistry metadataRegistry;
    private final Dispatcher dispatcher;
    private final OperationFactory operationFactory;
    private final TableButtonFactory tableButtonFactory;
    private final Resources resources;
    private StatementContext statementContext;

    @Inject
    public MbuiContext(final EventBus eventBus, final MetadataRegistry metadataRegistry,
            final Dispatcher dispatcher, final OperationFactory operationFactory,
            final TableButtonFactory tableButtonFactory, final Resources resources,
            final StatementContext statementContext) {
        this.eventBus = eventBus;
        this.metadataRegistry = metadataRegistry;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.operationFactory = operationFactory;
        this.tableButtonFactory = tableButtonFactory;
        this.resources = resources;
        this.statementContext = statementContext;
    }

    public EventBus eventBus() {
        return eventBus;
    }

    public MetadataRegistry metadataRegistry() {
        return metadataRegistry;
    }

    public StatementContext statementContext() {
        return statementContext;
    }

    public Dispatcher dispatcher() {
        return dispatcher;
    }

    public OperationFactory operationFactory() {
        return operationFactory;
    }

    public TableButtonFactory tableButtonFactory() {
        return tableButtonFactory;
    }

    public Resources resources() {
        return resources;
    }

    public MbuiContext updateStatementContext(final StatementContext statementContext) {
        this.statementContext = statementContext;
        return this;
    }
}
