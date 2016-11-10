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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.OperationFactory;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.meta.processing.SuccessfulMetadataCallback;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Class which contains generic CRUD methods to add, read, update and remove resources. Whenever possible the methods of
 * this class should be reused instead of writing custom CRUD functionality.
 * <p>
 * Currently this class is heavily used in
 * <ul>
 * <li>{@code ColumnActionFactory}</li>
 * <li>{@code ItemActionFactory}</li>
 * <li>{@code ModelBrowser}</li>
 * <li>{@code TableButtonFactory}</li>
 * <li>generated MBUI code</li>
 * <li>custom presenters and columns (where appropriate)</li>
 * </ul>
 *
 * @author Harald Pehl
 */
public class CrudOperations {

    /**
     * Callback used in {@code add} and {@code addSingleton} methods
     */
    @FunctionalInterface
    public interface AddCallback {

        /**
         * Called after the resource has been added.
         *
         * @param name    the name of the resource, {@code null} for {@code addSingleton} methods.
         * @param address the resource address of the newly added resource
         */
        void execute(@Nullable final String name, ResourceAddress address);
    }


    /**
     * Callback used for the {@code save} and {@code remove} methods.
     */
    @FunctionalInterface
    public interface Callback {

        void execute();
    }


    @FunctionalInterface
    public interface ReadCallback {

        void execute(final ModelNode result);
    }


    @FunctionalInterface
    public interface ReadChildrenCallback {

        void execute(final List<Property> children);
    }


    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final MetadataProcessor metadataProcessor;
    private final Provider<Progress> progress;
    private final StatementContext statementContext;
    private final Resources resources;
    private final OperationFactory operationFactory;

    @Inject
    public CrudOperations(final EventBus eventBus,
            final Dispatcher dispatcher,
            final MetadataProcessor metadataProcessor,
            @Footer final Provider<Progress> progress,
            final StatementContext statementContext,
            final Resources resources) {
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.metadataProcessor = metadataProcessor;
        this.progress = progress;
        this.statementContext = statementContext;
        this.resources = resources;
        this.operationFactory = new OperationFactory();
    }


    // ------------------------------------------------------ (c)reate

    /**
     * Opens an add-resource-dialog for the given resource type. The dialog contains fields for all required request
     * properties. When clicking "Add", a new resource is added using the specified address template. After the
     * resource has been added a success message is fired and the specified callback is executed.
     *
     * @param id       the id used for the add resource dialog
     * @param type     the human readable resource type used in the dialog header and success message
     * @param template the address template which is resolved against the current statement context to get the resource
     *                 address for the add operation
     * @param callback the callback executed after adding the resource
     */
    public void add(final String id, final String type, final AddressTemplate template, final AddCallback callback) {
        add(id, type, template, Collections.emptyList(), callback);
    }

    /**
     * Opens an add-resource-dialog for the given resource type. The dialog contains fields for all required request
     * properties plus the ones specified by {@code attributes}. When clicking "Add", a new resource is added using the
     * specified address template. After the resource has been added a success message is fired and the specified
     * callback is executed.
     *
     * @param id         the id used for the add resource dialog
     * @param type       the human readable resource type used in the dialog header and success message
     * @param template   the address template which is resolved against the current statement context to get the
     *                   resource address for the add operation
     * @param attributes additional attributes which should be part of the add resource dialog
     * @param callback   the callback executed after adding the resource
     */
    public void add(final String id, final String type, final AddressTemplate template,
            final Iterable<String> attributes, final AddCallback callback) {
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(final Metadata metadata) {
                AddResourceDialog dialog = new AddResourceDialog(id, resources.messages().addResourceTitle(type),
                        metadata, attributes, (name, model) -> add(type, name, template, model, callback));
                dialog.show();
            }
        });
    }

    /**
     * Executes an add operation using the specified name and payload. After the resource has been added a success
     * message is fired and the specified callback is executed.
     *
     * @param type     the human readable resource type used in the dialog header and success message
     * @param name     the resource name which is part of the add operation
     * @param template the address template which is resolved against the current statement context and the resource
     *                 name to get the resource address for the add operation
     * @param payload  the payload of the add operation
     * @param callback the callback executed after adding the resource
     */
    public void add(final String type, final String name, final AddressTemplate template, final ModelNode payload,
            final AddCallback callback) {
        add(type, name, template.resolve(statementContext, name), payload, callback);
    }

    /**
     * Executes an add operation using the specified name and payload. After the resource has been added a success
     * message is fired and the specified callback is executed.
     *
     * @param type     the human readable resource type used in the dialog header and success message
     * @param name     the resource name which is part of the add operation
     * @param address  the fq address for the add operation
     * @param payload  the payload of the add operation
     * @param callback the callback executed after adding the resource
     */
    public void add(final String type, final String name, final ResourceAddress address, final ModelNode payload,
            final AddCallback callback) {
        Operation operation = new Operation.Builder(ADD, address)
                .payload(payload)
                .build();
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(eventBus, Message.success(resources.messages().addResourceSuccess(type, name)));
            callback.execute(name, address);
        });
    }

    /**
     * Opens an add-resource-dialog for the given singleton resource type. The dialog contains fields for all required
     * request properties. When clicking "Add", a new singleton resource is added using the specified address template.
     * After the singleton resource has been added a success message is fired and the specified callback is executed.
     *
     * @param id       the id used for the add resource dialog
     * @param type     the human readable resource type used in the dialog header and success message
     * @param template the address template which is resolved against the current statement context to get the
     *                 singleton resource address for the add operation
     * @param callback the callback executed after adding the singleton resource
     */
    public void addSingleton(final String id, final String type, final AddressTemplate template,
            final AddCallback callback) {
        addSingleton(id, type, template, Collections.emptyList(), callback);
    }

    /**
     * Opens an add-resource-dialog for the given singleton resource type. The dialog contains fields for all required
     * request properties. plus the ones specified by {@code attributes}. When clicking "Add", a new singleton resource
     * is added using the specified address template. After the singleton resource has been added a success message is
     * fired and the specified callback is executed.
     *
     * @param id         the id used for the add resource dialog
     * @param type       the human readable resource type used in the dialog header and success message
     * @param template   the address template which is resolved against the current statement context to get the
     *                   singleton resource address for the add operation
     * @param attributes additional attributes which should be part of the add resource dialog
     * @param callback   the callback executed after adding the singleton resource
     */
    public void addSingleton(final String id, final String type, final AddressTemplate template,
            final Iterable<String> attributes, final AddCallback callback) {
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(final Metadata metadata) {
                AddResourceDialog dialog = new AddResourceDialog(id, resources.messages().addResourceTitle(type),
                        metadata, attributes, (name, model) -> addSingleton(type, template, model, callback));
                dialog.show();
            }
        });
    }

    /**
     * Executes an add operation using the specified template. After the resource has been added a success message is
     * fired and the specified callback is executed.
     *
     * @param type     the human readable resource type used in the dialog header and success message
     * @param template the address template which is resolved against the current statement context to get the
     *                 singleton resource address for the add operation
     * @param callback the callback executed after adding the singleton resource
     */
    public void addSingleton(final String type, final AddressTemplate template, final AddCallback callback) {
        addSingleton(type, template.resolve(statementContext), null, callback);
    }

    /**
     * Executes an add operation using the specified template. After the resource has been added a success message is
     * fired and the specified callback is executed.
     *
     * @param type     the human readable resource type used in the dialog header and success message
     * @param address  the fq address for the add operation
     * @param callback the callback executed after adding the singleton resource
     */
    public void addSingleton(final String type, final ResourceAddress address, final AddCallback callback) {
        addSingleton(type, address, null, callback);
    }

    /**
     * Executes an add operation using the specified template and payload. After the resource has been added a success
     * message is fired and the specified callback is executed.
     *
     * @param type     the human readable resource type used in the dialog header and success message
     * @param template the address template which is resolved against the current statement context to get the
     *                 singleton resource address for the add operation
     * @param payload  the payload of the add operation
     * @param callback the callback executed after adding the singleton resource
     */
    public void addSingleton(final String type, final AddressTemplate template, final ModelNode payload,
            final AddCallback callback) {
        addSingleton(type, template.resolve(statementContext), payload, callback);
    }

    /**
     * Executes an add operation using the specified template and payload. After the resource has been added a success
     * message is fired and the specified callback is executed.
     *
     * @param type     the human readable resource type used in the dialog header and success message
     * @param address  the fq address for the add operation
     * @param payload  the payload of the add operation
     * @param callback the callback executed after adding the singleton resource
     */
    public void addSingleton(final String type, final ResourceAddress address, final ModelNode payload,
            final AddCallback callback) {
        Operation.Builder builder = new Operation.Builder(ADD, address);
        if (payload != null && payload.isDefined()) {
            builder.payload(payload);
        }
        dispatcher.execute(builder.build(), result -> {
            MessageEvent.fire(eventBus, Message.success(resources.messages().addSingleResourceSuccess(type)));
            callback.execute(null, address);
        });
    }


    // ------------------------------------------------------ (r)ead

    /**
     * Executes an {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_RESOURCE_OPERATION} on the specified
     * template and passes the result to the specified callback.
     *
     * @param template the address template which is resolved against the current statement context to get the
     *                 resource address for the {@code read-resource} operation
     * @param callback the callback which gets the result of the {@code read-resource} operation
     */
    public void read(final AddressTemplate template, final ReadCallback callback) {
        read(template.resolve(statementContext), callback);
    }

    /**
     * Executes an {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_RESOURCE_OPERATION} on the specified
     * template and passes the result to the specified callback.
     *
     * @param address  the fq address for the {@code read-resource} operation
     * @param callback the callback which gets the result of the {@code read-resource} operation
     */
    public void read(final ResourceAddress address, final ReadCallback callback) {
        read(new Operation.Builder(READ_RESOURCE_OPERATION, address).param(INCLUDE_ALIASES, true).build(), callback);
    }

    /**
     * Executes an {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_RESOURCE_OPERATION} with the specified depth
     * on the specified template and passes the result to the specified callback.
     *
     * @param template the address template which is resolved against the current statement context to get the
     *                 resource address for the {@code read-resource} operation
     * @param depth    the depth used for the {@code recursive-depth} parameter
     * @param callback the callback which gets the result of the {@code read-resource} operation
     */
    public void read(final AddressTemplate template, final int depth, final ReadCallback callback) {
        read(template.resolve(statementContext), depth, callback);
    }

    /**
     * Executes an {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_RESOURCE_OPERATION} with the specified depth
     * on the specified template and passes the result to the specified callback.
     *
     * @param address  the fq address for the {@code read-resource} operation
     * @param depth    the depth used for the {@code recursive-depth} parameter
     * @param callback the callback which gets the result of the {@code read-resource} operation
     */
    public void read(final ResourceAddress address, final int depth, final ReadCallback callback) {
        read(new Operation.Builder(READ_RESOURCE_OPERATION, address)
                        .param(INCLUDE_ALIASES, true)
                        .param(RECURSIVE_DEPTH, depth)
                        .build(),
                callback);
    }

    /**
     * Executes a recursive {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_RESOURCE_OPERATION} on the
     * specified template and passes the result to the specified callback.
     *
     * @param template the address template which is resolved against the current statement context to get the
     *                 resource address for the {@code read-resource} operation
     * @param callback the callback which gets the result of the {@code read-resource} operation
     */
    public void readRecursive(final AddressTemplate template, final ReadCallback callback) {
        readRecursive(template.resolve(statementContext), callback);
    }

    /**
     * Executes a recursive {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_RESOURCE_OPERATION} on the
     * specified template and passes the result to the specified callback.
     *
     * @param address  the fq address for the {@code read-resource} operation
     * @param callback the callback which gets the result of the {@code read-resource} operation
     */
    public void readRecursive(final ResourceAddress address, final ReadCallback callback) {
        read(new Operation.Builder(READ_RESOURCE_OPERATION, address)
                        .param(INCLUDE_ALIASES, true)
                        .param(RECURSIVE, true)
                        .build(),
                callback);
    }

    private void read(final Operation operation, final ReadCallback callback) {
        dispatcher.execute(operation, callback::execute);
    }

    /**
     * Executes an {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_CHILDREN_RESOURCES_OPERATION} on the
     * specified template and passes the result as {@code List<Property>} to the specified callback.
     *
     * @param template the address template which is resolved against the current statement context to get the
     *                 resource address for the {@code read-children-resource} operation
     * @param resource the child resource (not human readable, but the actual child resource name!)
     * @param callback the callback which gets the result of the {@code read-children-resource} operation as {@code
     *                 List<Property>}
     */
    public void readChildren(final AddressTemplate template, final String resource,
            final ReadChildrenCallback callback) {
        readChildren(template.resolve(statementContext), resource, callback);
    }

    /**
     * Executes an {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_CHILDREN_RESOURCES_OPERATION} on the
     * specified template and passes the result as {@code List<Property>} to the specified callback.
     *
     * @param address  the fq address for the {@code read-children-resource} operation
     * @param resource the child resource (not human readable, but the actual child resource name!)
     * @param callback the callback which gets the result of the {@code read-children-resource} operation as {@code
     *                 List<Property>}
     */
    public void readChildren(final ResourceAddress address, final String resource,
            final ReadChildrenCallback callback) {
        readChildren(new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, address)
                        .param(CHILD_TYPE, resource)
                        .build(),
                callback);
    }

    private void readChildren(final Operation operation, final ReadChildrenCallback callback) {
        dispatcher.execute(operation, result -> callback.execute(result.asPropertyList()));
    }


    // ------------------------------------------------------ (u)pdate

    /**
     * Write the changed values to the specified resource. After the resource resource has been saved a standard
     * success message is fired and the specified callback is executed.
     *
     * @param type          the human readable resource type used in the success message
     * @param name          the resource name
     * @param template      the address template which is resolved against the current statement context and the
     *                      resource name to get the resource address for the operation
     * @param changedValues the changed values / payload for the operation
     * @param callback      the callback executed after saving the resource
     */
    public void save(final String type, final String name, final AddressTemplate template,
            final Map<String, Object> changedValues, final Callback callback) {
        save(template.resolve(statementContext, name), changedValues,
                resources.messages().modifyResourceSuccess(type, name), callback);
    }

    /**
     * Write the changed values to the specified resource. After the resource resource has been saved the specified
     * success message is fired and the specified callback is executed.
     *
     * @param name           the resource name
     * @param template       the address template which is resolved against the current statement context and the
     *                       resource name to get the resource address for the operation
     * @param changedValues  the changed values / payload for the operation
     * @param successMessage the success message fired after saving the resource
     * @param callback       the callback executed after saving the resource
     */
    public void save(final String name, final AddressTemplate template, final Map<String, Object> changedValues,
            final SafeHtml successMessage, final Callback callback) {
        save(template.resolve(statementContext, name), changedValues, successMessage, callback);
    }

    /**
     * Write the changed values to the specified resource. After the resource resource has been saved a standard
     * success message is fired and the specified callback is executed.
     *
     * @param type          the human readable resource type used in the success message
     * @param name          the resource name
     * @param address       the fq address for the operation
     * @param changedValues the changed values / payload for the operation
     * @param callback      the callback executed after saving the resource
     */
    public void save(final String type, final String name, final ResourceAddress address,
            final Map<String, Object> changedValues, final Callback callback) {
        save(address, changedValues, resources.messages().modifyResourceSuccess(type, name), callback);
    }

    /**
     * Write the changed values to the specified resource. After the resource resource has been saved the specified
     * success message is fired and the specified callback is executed.
     *
     * @param address        the fq address for the operation
     * @param changedValues  the changed values / payload for the operation
     * @param successMessage the success message fired after saving the resource
     * @param callback       the callback executed after saving the resource
     */
    public void save(final ResourceAddress address, final Map<String, Object> changedValues,
            final SafeHtml successMessage, final Callback callback) {
        Composite operation = operationFactory.fromChangeSet(address, changedValues);
        dispatcher.execute(operation, (CompositeResult result) -> {
            MessageEvent.fire(eventBus, Message.success(successMessage));
            callback.execute();
        });
    }

    /**
     * Write the changed values to the specified singleton resource. After the resource resource has been saved a
     * success message is fired and the specified callback is executed.
     *
     * @param type          the human readable resource type used in the success message
     * @param template      the address template which is resolved against the current statement context to get the
     *                      resource address for the operation
     * @param changedValues the changed values / payload for the operation
     * @param callback      the callback executed after saving the singleton resource
     */
    public void saveSingleton(final String type, final AddressTemplate template,
            final Map<String, Object> changedValues, final Callback callback) {
        saveSingleton(template.resolve(statementContext), changedValues,
                resources.messages().modifySingleResourceSuccess(type), callback);
    }

    /**
     * Write the changed values to the specified singleton resource. After the resource resource has been saved a
     * success message is fired and the specified callback is executed.
     *
     * @param template       the address template which is resolved against the current statement context to get the
     *                       resource address for the operation
     * @param changedValues  the changed values / payload for the operation
     * @param successMessage the success message fired after saving the resource
     * @param callback       the callback executed after saving the singleton resource
     */
    public void saveSingleton(final AddressTemplate template, final Map<String, Object> changedValues,
            final SafeHtml successMessage, final Callback callback) {
        saveSingleton(template.resolve(statementContext), changedValues, successMessage, callback);
    }

    /**
     * Write the changed values to the specified singleton resource. After the resource resource has been saved a
     * success message is fired and the specified callback is executed.
     *
     * @param type          the human readable resource type used in the success message
     * @param address       the fq address for the operation
     * @param changedValues the changed values / payload for the operation
     * @param callback      the callback executed after saving the singleton resource
     */
    public void saveSingleton(final String type, final ResourceAddress address, final Map<String, Object> changedValues,
            final Callback callback) {
        saveSingleton(address, changedValues, resources.messages().modifySingleResourceSuccess(type), callback);
    }

    /**
     * Write the changed values to the specified singleton resource. After the resource resource has been saved a
     * success message is fired and the specified callback is executed.
     *
     * @param address        the fq address for the operation
     * @param changedValues  the changed values / payload for the operation
     * @param successMessage the success message fired after saving the resource
     * @param callback       the callback executed after saving the singleton resource
     */
    public void saveSingleton(final ResourceAddress address, final Map<String, Object> changedValues,
            final SafeHtml successMessage, final Callback callback) {
        Composite operation = operationFactory.fromChangeSet(address, changedValues);
        dispatcher.execute(operation, (CompositeResult result) -> {
            MessageEvent.fire(eventBus, Message.success(successMessage));
            callback.execute();
        });
    }


    // ------------------------------------------------------ (d)elete

    /**
     * Shows a confirmation dialog and removes the resource if confirmed by the user. After the resource has been
     * removed a success message is fired and the specified callback is executed.
     *
     * @param type     the human readable resource type used in the success message
     * @param name     the resource name
     * @param template the address template which is resolved against the current statement context and the resource
     *                 name to get the resource address for the {@code remove} operation
     * @param callback the callback executed after removing the resource
     */
    public void remove(final String type, final String name, final AddressTemplate template, final Callback callback) {
        remove(type, name, template.resolve(statementContext, name), callback);
    }

    /**
     * Shows a confirmation dialog and removes the resource if confirmed by the user. After the resource has been
     * removed a success message is fired and the specified callback is executed.
     *
     * @param type     the human readable resource type used in the success message
     * @param name     the resource name
     * @param address  the fq address for the {@code remove} operation
     * @param callback the callback executed after removing the resource
     */
    public void remove(final String type, final String name, final ResourceAddress address, final Callback callback) {
        DialogFactory.showConfirmation(
                resources.messages().removeResourceConfirmationTitle(type),
                resources.messages().removeResourceConfirmationQuestion(name),
                () -> {
                    Operation operation = new Operation.Builder(REMOVE, address)
                            .build();
                    dispatcher.execute(operation, result -> {
                        MessageEvent.fire(eventBus, Message.success(
                                resources.messages().removeResourceSuccess(type, name)));
                        callback.execute();
                    });
                });
    }
}
