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

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.web.bindery.event.shared.EventBus;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.jboss.hal.ballroom.dialog.Dialog;
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
import org.jboss.hal.spi.EsParam;
import org.jboss.hal.spi.EsReturn;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.Message.Level;
import org.jboss.hal.spi.MessageEvent;
import org.jetbrains.annotations.NonNls;

/** Provides access to all important classes. Acts as an entry point for the JavaScript API. */
@JsType
@SuppressWarnings("DuplicateStringLiteralInspection")
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

    /**
     * @return crud operations
     */
    @JsProperty(name = "crud")
    public CrudOperations crud() {
        return crud;
    }

    /**
     * @return dispatcher
     */
    @JsProperty(name = "dispatcher")
    public Dispatcher dispatcher() {
        return dispatcher;
    }

    /**
     * @return endpoints
     */
    @JsProperty(name = "endpoints")
    public Endpoints endpoints() {
        return endpoints;
    }

    /**
     * @return environment
     */
    @JsProperty(name = "environment")
    public Environment environment() {
        return environment;
    }

    @JsIgnore
    public EventBus eventBus() {
        return eventBus;
    }

    /**
     * @return extension registry
     */
    @JsProperty(name = "extensionRegistry")
    public ExtensionRegistry extensionRegistry() {
        return extensionRegistry;
    }

    /**
     * @return metadata processor
     */
    @JsProperty(name = "metadataProcessor")
    public MetadataProcessor metadataProcessor() {
        return metadataProcessor;
    }

    /**
     * @return metadata registry
     */
    @JsProperty(name = "metadataRegistry")
    public MetadataRegistry metadataRegistry() {
        return metadataRegistry;
    }

    /**
     * @return statement context
     */
    @JsProperty(name = "statementContext")
    public StatementContext statementContext() {
        return statementContext;
    }

    @JsIgnore
    public TableButtonFactory tableButtonFactory() {
        return tableButtonFactory;
    }


    // ------------------------------------------------------ JS methods (static, inner classes, a-z)

    /**
     * @return the singleton core instance.
     */
    @JsMethod(name = "getInstance")
    public static Core jsGetInstance() {
        return INSTANCE;
    }

    /**
     * Creates and returns a dialog builder using the specified title.
     *
     * @param title The dialog title.
     *
     * @return a builder to create dialogs
     */
    @JsMethod(name = "dialog")
    @EsReturn("DialogBuilder")
    public Dialog.Builder jsDialog(String title) {
        return new Dialog.Builder(title);
    }

    /**
     * Shows an error message.
     *
     * @param message The error message.
     */
    @JsMethod(name = "error")
    public void jsError(String message) {
        jsMessage(Level.ERROR, message);
    }

    /**
     * Returns a new form builder for a {@link ModelNode}.
     *
     * @param meta The metadata for the form.
     *
     * @return the form builder
     */
    @JsMethod(name = "form")
    @EsReturn("FormBuilder")
    public ModelNodeForm.Builder<ModelNode> jsForm(@EsParam("Metadata|AddressTemplate|string") Object meta) {
        return new ModelNodeForm.Builder<>(Ids.build(Ids.uniqueId(), Ids.FORM), jsMetadata("form", meta));
    }

    /**
     * Shows an info message.
     *
     * @param message The info message.
     */
    @JsMethod(name = "info")
    public void jsInfo(String message) {
        jsMessage(Level.INFO, message);
    }

    /**
     * Returns a new form builder for a {@link NamedNode}.
     *
     * @param meta The metadata for the form.
     *
     * @return the form builder
     */
    @JsMethod(name = "namedForm")
    @EsReturn("FormBuilder")
    public ModelNodeForm.Builder<NamedNode> jsNamedForm(@EsParam("Metadata|AddressTemplate|string") Object meta) {
        return new ModelNodeForm.Builder<>(Ids.build(Ids.uniqueId(), Ids.FORM), jsMetadata("namedForm", meta));
    }

    /**
     * Returns a new table builder for a {@link NamedNode}.
     *
     * @param meta The metadata for the table.
     *
     * @return the table builder
     */
    @JsMethod(name = "namedTable")
    @EsReturn("TableBuilder")
    public ModelNodeTable.Builder<NamedNode> jsNamedTable(@EsParam("Metadata|AddressTemplate|string") Object meta) {
        return new ModelNodeTable.Builder<>(Ids.build(Ids.uniqueId(), Ids.FORM), jsMetadata("namedTable", meta));
    }

    /**
     * Returns a new operation builder.
     *
     * @param address The address.
     * @param name    The operation name.
     *
     * @return the operation builder
     */
    @JsMethod(name = "operation")
    @EsReturn("OperationBuilder")
    public Operation.Builder jsOperation(@EsParam("AddressTemplate|ResourceAddress|string") Object address,
            String name) {
        ResourceAddress ra;
        if (address instanceof AddressTemplate) {
            ra = ((AddressTemplate) address).resolve(statementContext());
        } else if (address instanceof ResourceAddress) {
            ra = (ResourceAddress) address;
        } else if (address instanceof String) {
            ra = AddressTemplate.of(((String) address)).resolve(statementContext());
        } else {
            throw new IllegalArgumentException(
                    "Illegal 1st argument: Use Core.operation((AddressTemplate|ResourceAddress|string), string)");
        }
        return new Operation.Builder(ra, name);
    }

    /**
     * Shows a success message.
     *
     * @param message The success message.
     */
    @JsMethod(name = "success")
    public void jsSuccess(String message) {
        jsMessage(Level.SUCCESS, message);
    }

    /**
     * Returns a new table builder for a {@link ModelNode}.
     *
     * @param meta The metadata for the table.
     *
     * @return the table builder
     */
    @JsMethod(name = "table")
    @EsReturn("TableBuilder")
    public ModelNodeTable.Builder<ModelNode> jsTable(final Object meta) {
        return new ModelNodeTable.Builder<>(Ids.build(Ids.uniqueId(), Ids.TAB), jsMetadata("table", meta));
    }

    /**
     * Shows a warning message.
     *
     * @param message The warning message.
     */
    @JsMethod(name = "warning")
    public void jsWarning(String message) {
        jsMessage(Level.WARNING, message);
    }

    private void jsMessage(Level level, String message) {
        SafeHtml safeMessage = SafeHtmlUtils.fromSafeConstant(message);
        switch (level) {
            case ERROR:
                MessageEvent.fire(eventBus, Message.error(safeMessage));
                break;
            case WARNING:
                MessageEvent.fire(eventBus, Message.warning(safeMessage));
                break;
            case INFO:
                MessageEvent.fire(eventBus, Message.info(safeMessage));
                break;
            case SUCCESS:
                MessageEvent.fire(eventBus, Message.success(safeMessage));
                break;
            default:
                break;
        }
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
            throw new IllegalArgumentException("Use Core." + method + "(string|AddressTemplate|Metadata)");
        }
    }
}
