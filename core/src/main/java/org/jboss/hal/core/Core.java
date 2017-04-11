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
package org.jboss.hal.core;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;

/**
 * Helper class / singleton to get access to selected dependencies. Please use <em>only</em> if no DI is available!
 *
 * @author Harald Pehl
 */
public class Core {

    @Inject
    public static Core INSTANCE;

    private final Environment environment;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final EventBus eventBus;

    @Inject
    public Core(final Environment environment, final Dispatcher dispatcher, final StatementContext statementContext,
            final EventBus eventBus) {
        this.environment = environment;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.eventBus = eventBus;
    }

    public Environment environment() {
        return environment;
    }

    public Dispatcher dispatcher() {
        return dispatcher;
    }

    public StatementContext statementContext() {
        return statementContext;
    }

    public EventBus eventBus() {
        return eventBus;
    }
}
