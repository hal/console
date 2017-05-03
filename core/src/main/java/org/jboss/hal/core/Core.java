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
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.extension.ExtensionRegistry;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.resources.Ids;
import org.jetbrains.annotations.NonNls;

/**
 * Helper class / singleton to get access to selected dependencies. Please use <em>only</em> if no DI is available!
 * <p>
 * Entry point for the HAL JavaScript API.
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
    private final Endpoints endpoints;
    private final Environment environment;
    private final EventBus eventBus;
    private final ExtensionRegistry extensionRegistry;
    private final MetadataProcessor metadataProcessor;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private final TableButtonFactory tableButtonFactory;

    @Inject
    @JsIgnore
    public Core(final CrudOperations crud,
            final Dispatcher dispatcher,
            final Endpoints endpoints,
            final Environment environment,
            final EventBus eventBus,
            final ExtensionRegistry extensionRegistry,
            final MetadataProcessor metadataProcessor,
            final MetadataRegistry metadataRegistry,
            final StatementContext statementContext,
            final TableButtonFactory tableButtonFactory) {
        this.crud = crud;
        this.dispatcher = dispatcher;
        this.endpoints = endpoints;
        this.environment = environment;
        this.eventBus = eventBus;
        this.extensionRegistry = extensionRegistry;
        this.metadataProcessor = metadataProcessor;
        this.metadataRegistry = metadataRegistry;
        this.statementContext = statementContext;
        this.tableButtonFactory = tableButtonFactory;
    }

    @JsProperty(name = "crud")
    public CrudOperations crud() {
        return crud;
    }

    @JsProperty(name = "dispatcher")
    public Dispatcher dispatcher() {
        return dispatcher;
    }

    @JsProperty(name = "endpoints")
    public Endpoints endpoints() {
        return endpoints;
    }

    @JsProperty(name = "environment")
    public Environment environment() {
        return environment;
    }

    @JsIgnore
    public EventBus eventBus() {
        return eventBus;
    }

    @JsProperty(name = "extensionRegistry")
    public ExtensionRegistry extensionRegistry() {
        return extensionRegistry;
    }

    @JsProperty(name = "metadataProcessor")
    public MetadataProcessor metadataProcessor() {
        return metadataProcessor;
    }

    @JsProperty(name = "metadataRegistry")
    public MetadataRegistry metadataRegistry() {
        return metadataRegistry;
    }

    @JsProperty(name = "statementContext")
    public StatementContext statementContext() {
        return statementContext;
    }

    @JsIgnore
    public TableButtonFactory tableButtonFactory() {
        return tableButtonFactory;
    }


    // ------------------------------------------------------ JS methods

    @JsMethod(name = "getInstance")
    public static Core jsGetInstance() {
        return INSTANCE;
    }

    @JsMethod(name = "operation")
    public Operation.Builder jsOperation(final Object address, final String name) {
        ResourceAddress ra;
        if (address instanceof AddressTemplate) {
            ra = ((AddressTemplate) address).resolve(statementContext());
        } else if (address instanceof ResourceAddress) {
            ra = (ResourceAddress) address;
        } else if (address instanceof String) {
            ra = AddressTemplate.of(((String) address)).resolve(statementContext());
        } else {
            throw new IllegalArgumentException("Illegal 1st argument: Use Core.operation((AddressTemplate|ResourceAddress|String) address, String name)");
        }
        return new Operation.Builder(ra, name);
    }

    @JsMethod(name = "form")
    public ModelNodeForm.Builder<ModelNode> jsForm(final Object meta) {
        return new ModelNodeForm.Builder<>(Ids.build(Ids.uniqueId(), Ids.FORM_SUFFIX), jsMetadata("form", meta));
    }

    @JsMethod(name = "namedForm")
    public ModelNodeForm.Builder<NamedNode> jsNamedForm(final Object meta) {
        return new ModelNodeForm.Builder<>(Ids.build(Ids.uniqueId(), Ids.FORM_SUFFIX), jsMetadata("namedForm", meta));
    }

    @JsMethod(name = "table")
    public ModelNodeTable.Builder<ModelNode> jsTable(final Object meta) {
        return new ModelNodeTable.Builder<>(Ids.build(Ids.uniqueId(), Ids.TAB_SUFFIX), jsMetadata("table", meta));
    }

    @JsMethod(name = "namedTable")
    public ModelNodeTable.Builder<NamedNode> jsNamedTable(final Object meta) {
        return new ModelNodeTable.Builder<>(Ids.build(Ids.uniqueId(), Ids.FORM_SUFFIX), jsMetadata("namedTable", meta));
    }

    private Metadata jsMetadata(@NonNls String method, Object meta) {
        if (meta instanceof String) {
            AddressTemplate t = AddressTemplate.of(((String) meta));
            return metadataRegistry.lookup(t);
        } else if (meta instanceof AddressTemplate) {
            return metadataRegistry.lookup(((AddressTemplate) meta));
        } else if (meta instanceof Metadata) {
            return (Metadata) meta;
        } else {
            throw new IllegalArgumentException("Use Core." + method + "(String|AddressTemplate|Metadata)");
        }
    }
}
