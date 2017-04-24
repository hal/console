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
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.User;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;

/**
 * Helper class / singleton to get access to selected dependencies. Please use <em>only</em> if no DI is available!
 * <p>
 * Besides that this class serves as an entry point for the JS API.
 *
 * @author Harald Pehl
 */
@JsType
public class Core {

    @Inject
    @JsIgnore
    public static Core INSTANCE;

    private final CrudOperations crud;
    private final Dispatcher dispatcher;
    private final Environment environment;
    private final EventBus eventBus;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private final User user;

    @Inject
    @JsIgnore
    public Core(final CrudOperations crud,
            final Dispatcher dispatcher,
            final Environment environment,
            final EventBus eventBus,
            final MetadataRegistry metadataRegistry,
            final StatementContext statementContext,
            final User user) {
        this.crud = crud;
        this.dispatcher = dispatcher;
        this.environment = environment;
        this.eventBus = eventBus;
        this.metadataRegistry = metadataRegistry;
        this.statementContext = statementContext;
        this.user = user;
    }

    @JsProperty(name = "crud")
    public CrudOperations crud() {
        return crud;
    }

    @JsProperty(name = "dispatcher")
    public Dispatcher dispatcher() {
        return dispatcher;
    }

    @JsProperty(name = "environment")
    public Environment environment() {
        return environment;
    }

    @JsIgnore
    public EventBus eventBus() {
        return eventBus;
    }

    @JsProperty(name = "metadataRegistry")
    public MetadataRegistry metadataRegistry() {
        return metadataRegistry;
    }

    @JsProperty(name = "statementContext")
    public StatementContext statementContext() {
        return statementContext;
    }


    // ------------------------------------------------------ JS methods

    @JsMethod(name = "getInstance")
    public static Core jsGetInstance() {
        return INSTANCE;
    }

    @JsMethod(name = "operation")
    public Operation.Builder jsOperation(final String address, final String name) {
        AddressTemplate template = AddressTemplate.of(address);
        return new Operation.Builder(template.resolve(statementContext), name);
    }
}
