/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.core;

import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;

import com.google.web.bindery.event.shared.EventBus;

/** Provides access to all important classes. Acts as an entry point for the JavaScript API. */
@SuppressWarnings("DuplicateStringLiteralInspection")
public class Core {

    @Inject public static Core INSTANCE;

    private final Dispatcher dispatcher;
    private final Environment environment;
    private final EventBus eventBus;
    private final StatementContext statementContext;
    private final TableButtonFactory tableButtonFactory;

    @Inject
    public Core(Dispatcher dispatcher,
            Environment environment,
            EventBus eventBus,
            StatementContext statementContext,
            TableButtonFactory tableButtonFactory) {
        this.dispatcher = dispatcher;
        this.environment = environment;
        this.eventBus = eventBus;
        this.statementContext = statementContext;
        this.tableButtonFactory = tableButtonFactory;
    }

    /**
     * @return dispatcher
     */
    public Dispatcher dispatcher() {
        return dispatcher;
    }

    /**
     * @return environment
     */
    public Environment environment() {
        return environment;
    }

    public EventBus eventBus() {
        return eventBus;
    }

    /**
     * @return statement context
     */
    public StatementContext statementContext() {
        return statementContext;
    }

    public TableButtonFactory tableButtonFactory() {
        return tableButtonFactory;
    }
}
