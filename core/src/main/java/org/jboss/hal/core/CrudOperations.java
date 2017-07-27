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
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.Iterables;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import jsinterop.base.JsPropertyMapOfAny;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.JsCallback;
import org.jboss.hal.ballroom.JsHelper;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.meta.processing.SuccessfulMetadataCallback;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;
import org.jboss.hal.spi.EsParam;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Contains generic CRUD methods to add, read, update and remove (singleton) resources. Some methods just execute the
 * underlying DMR operations, other methods also interact with the user by showing (confirmation) dialogs.
 */
@JsType
@SuppressWarnings("DuplicateStringLiteralInspection")
public class CrudOperations {

    /**
     * Callback used in {@code add} methods
     */
    @JsFunction
    @FunctionalInterface
    public interface AddCallback {

        /**
         * Called after the resource has been added.
         *
         * @param name    the name of the resource
         * @param address the resource address of the newly added resource
         */
        void execute(@Nullable final String name, final ResourceAddress address);
    }


    /**
     * Callback used in {@code addSingleton} methods
     */
    @JsFunction
    @FunctionalInterface
    public interface AddSingletonCallback {

        /**
         * Called after the resource has been added.
         *
         * @param address the resource address of the newly added resource
         */
        void execute(final ResourceAddress address);
    }


    @JsFunction
    @FunctionalInterface
    public interface ReadCallback {

        void execute(final ModelNode result);
    }


    @FunctionalInterface
    public interface ReadChildrenCallback {

        void execute(final List<Property> children);
    }


    @JsFunction
    @FunctionalInterface
    public interface ReadCompositeCallback {

        void execute(final CompositeResult result);
    }


    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final MetadataProcessor metadataProcessor;
    private final Provider<Progress> progress;
    private final StatementContext statementContext;
    private final Resources resources;
    private final OperationFactory operationFactory;

    @Inject
    @JsIgnore
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


    // ------------------------------------------------------ (c)reate with dialog

    /**
     * Opens an add-resource-dialog for the given resource type. The dialog contains fields for all required request
     * properties. When clicking "Add", a new resource is added using the specified address template. After the
     * resource has been added a success message is fired and the specified callback is executed.
     *
     * @param id       the id used for the add resource dialog
     * @param type     the human readable resource type used in the dialog header and success message
     * @param template the address template which is resolved against the current statement context to get the resource
     *                 address for the add operation
     * @param callback the callback executed after the resource has been added
     */
    @JsIgnore
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
     * @param callback   the callback executed after the resource has been added
     */
    @JsIgnore
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


    // ------------------------------------------------------ (c)reate operation

    /**
     * Executes an add operation using the specified name and payload. After the resource has been added a success
     * message is fired and the specified callback is executed.
     *
     * @param type     the human readable resource type used in the dialog header and success message
     * @param name     the resource name which is part of the add operation
     * @param template the address template which is resolved against the current statement context and the resource
     *                 name to get the resource address for the add operation
     * @param payload  the optional payload of the add operation (may be null or undefined)
     * @param callback the callback executed after the resource has been added
     */
    @JsIgnore
    public void add(final String type, final String name, final AddressTemplate template,
            @Nullable final ModelNode payload, final AddCallback callback) {
        add(name, template.resolve(statementContext, name), payload,
                resources.messages().addResourceSuccess(type, name), callback);
    }

    /**
     * Executes an add operation using the specified name and payload. After the resource has been added the specified
     * success message is fired and the specified callback is executed.
     *
     * @param name           the resource name which is part of the add operation
     * @param template       the address template which is resolved against the current statement context and the
     *                       resource name to get the resource address for the add operation
     * @param payload        the optional payload of the add operation (may be null or undefined)
     * @param successMessage the success message fired after adding the resource
     * @param callback       the callback executed after the resource has been added
     */
    @JsIgnore
    public void add(final String name, final AddressTemplate template, @Nullable final ModelNode payload,
            final SafeHtml successMessage, final AddCallback callback) {
        add(name, template.resolve(statementContext, name), payload, successMessage, callback);
    }

    /**
     * Executes an add operation using the specified name and payload. After the resource has been added a success
     * message is fired and the specified callback is executed.
     *
     * @param type     the human readable resource type used in the dialog header and success message
     * @param name     the resource name which is part of the add operation
     * @param address  the fq address for the add operation
     * @param payload  the optional payload of the add operation (may be null or undefined)
     * @param callback the callback executed after the resource has been added
     */
    @JsIgnore
    public void add(final String type, final String name, final ResourceAddress address,
            @Nullable final ModelNode payload, final AddCallback callback) {
        add(name, address, payload, resources.messages().addResourceSuccess(type, name), callback);
    }

    /**
     * Executes an add operation using the specified name and payload. After the resource has been added the specified
     * success message is fired and the specified callback is executed.
     *
     * @param name           the resource name which is part of the add operation
     * @param address        the fq address for the add operation
     * @param payload        the optional payload of the add operation (may be null or undefined)
     * @param successMessage the success message fired after adding the resource
     * @param callback       the callback executed after the resource has been added
     */
    @JsIgnore
    public void add(final String name, final ResourceAddress address, @Nullable final ModelNode payload,
            final SafeHtml successMessage, final AddCallback callback) {
        Operation.Builder builder = new Operation.Builder(address, ADD);
        if (payload != null && payload.isDefined()) {
            builder.payload(payload);
        }
        dispatcher.execute(builder.build(), result -> {
            MessageEvent.fire(eventBus, Message.success(successMessage));
            callback.execute(name, address);
        });
    }


    // ------------------------------------------------------ (c)reate singleton with dialog

    /**
     * Opens an add-resource-dialog for the given singleton resource type. The dialog contains fields for all required
     * request properties. When clicking "Add", a new singleton resource is added using the specified address template.
     * After the singleton resource has been added a success message is fired and the specified callback is executed.
     *
     * @param id       the id used for the add resource dialog
     * @param type     the human readable resource type used in the dialog header and success message
     * @param template the address template which is resolved against the current statement context to get the
     *                 singleton resource address for the add operation
     * @param callback the callback executed after the singleton resource has been added
     */
    @JsIgnore
    public void addSingleton(final String id, final String type, final AddressTemplate template,
            final AddSingletonCallback callback) {
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
     * @param callback   the callback executed after the singleton resource has been added
     */
    @JsIgnore
    public void addSingleton(final String id, final String type, final AddressTemplate template,
            final Iterable<String> attributes, final AddSingletonCallback callback) {
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(final Metadata metadata) {
                boolean hasRequiredAttributes = !metadata.getDescription()
                        .getRequiredAttributes(OPERATIONS + "/" + ADD + "/" + REQUEST_PROPERTIES).isEmpty();
                if (hasRequiredAttributes || !Iterables.isEmpty(attributes)) {
                    // no unbound name item!
                    ModelNodeForm.Builder<ModelNode> builder = new ModelNodeForm.Builder<>(id, metadata)
                            .fromRequestProperties()
                            .requiredOnly();
                    if (!Iterables.isEmpty(attributes)) {
                        builder.include(attributes).unsorted();
                    }
                    AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(type),
                            builder.build(), (name, model) -> addSingleton(type, template, model, callback));
                    dialog.show();
                } else {
                    addSingleton(type, template, null, callback);
                }
            }
        });
    }


    // ------------------------------------------------------ (c)reate singleton operation

    /**
     * Executes an add operation using the specified template. After the resource has been added a success message is
     * fired and the specified callback is executed.
     *
     * @param type     the human readable resource type used in the dialog header and success message
     * @param template the address template which is resolved against the current statement context to get the
     *                 singleton resource address for the add operation
     * @param callback the callback executed after the singleton resource has been added
     */
    @JsIgnore
    public void addSingleton(final String type, final AddressTemplate template, final AddSingletonCallback callback) {
        addSingleton(type, template.resolve(statementContext), null, callback);
    }

    /**
     * Executes an add operation using the specified template and payload. After the resource has been added a success
     * message is fired and the specified callback is executed.
     *
     * @param type     the human readable resource type used in the dialog header and success message
     * @param template the address template which is resolved against the current statement context to get the
     *                 singleton resource address for the add operation
     * @param payload  the optional payload of the add operation (may be null or undefined)
     * @param callback the callback executed after the singleton resource has been added
     */
    @JsIgnore
    public void addSingleton(final String type, final AddressTemplate template, @Nullable final ModelNode payload,
            final AddSingletonCallback callback) {
        addSingleton(type, template.resolve(statementContext), payload, callback);
    }

    /**
     * Executes an add operation using the specified address and payload. After the resource has been added a success
     * message is fired and the specified callback is executed.
     *
     * @param type     the human readable resource type used in the dialog header and success message
     * @param address  the fq address for the add operation
     * @param payload  the optional payload of the add operation (may be null or undefined)
     * @param callback the callback executed after the singleton resource has been added
     */
    @JsIgnore
    public void addSingleton(final String type, final ResourceAddress address, @Nullable final ModelNode payload,
            final AddSingletonCallback callback) {
        Operation.Builder builder = new Operation.Builder(address, ADD);
        if (payload != null && payload.isDefined()) {
            builder.payload(payload);
        }
        addSingleton(type, builder.build(), callback);
    }

    /**
     * Executes the specified add operation. After the resource has been added a success message is fired and the
     * specified callback is executed.
     *
     * @param operation the add operation with the address and payload
     * @param type      the human readable resource type used in the dialog header and success message
     * @param callback  the callback executed after the singleton resource has been added
     */
    @JsIgnore
    public void addSingleton(final String type, final Operation operation, final AddSingletonCallback callback) {
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(eventBus, Message.success(resources.messages().addSingleResourceSuccess(type)));
            callback.execute(operation.getAddress());
        });
    }


    // ------------------------------------------------------ (r)ead using template

    /**
     * Executes an {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_RESOURCE_OPERATION} on the specified
     * template and passes the result to the specified callback.
     *
     * @param template the address template which is resolved against the current statement context to get the
     *                 resource address for the {@code read-resource} operation
     * @param callback the callback which gets the result of the {@code read-resource} operation
     */
    @JsIgnore
    public void read(final AddressTemplate template, final ReadCallback callback) {
        read(template.resolve(statementContext), callback);
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
    @JsIgnore
    public void read(final AddressTemplate template, final int depth, final ReadCallback callback) {
        read(template.resolve(statementContext), depth, callback);
    }

    /**
     * Executes a recursive {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_RESOURCE_OPERATION} on the
     * specified template and passes the result to the specified callback.
     *
     * @param template the address template which is resolved against the current statement context to get the
     *                 resource address for the {@code read-resource} operation
     * @param callback the callback which gets the result of the {@code read-resource} operation
     */
    @JsIgnore
    public void readRecursive(final AddressTemplate template, final ReadCallback callback) {
        readRecursive(template.resolve(statementContext), callback);
    }

    /**
     * Executes an {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_CHILDREN_RESOURCES_OPERATION} on the
     * specified template and passes the result as {@code List<Property>} to the specified callback.
     *
     * @param template  the address template which is resolved against the current statement context to get the
     *                  resource address for the {@code read-children-resource} operation
     * @param childType the child resource (not human readable, but the actual child resource name!)
     * @param callback  the callback which gets the result of the {@code read-children-resource} operation as {@code
     *                  List<Property>}
     */
    @JsIgnore
    public void readChildren(final AddressTemplate template, final String childType,
            final ReadChildrenCallback callback) {
        readChildren(template.resolve(statementContext), childType, callback);
    }

    /**
     * Executes an {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_CHILDREN_RESOURCES_OPERATION} on the
     * specified template and passes the result as {@code List<Property>} to the specified callback.
     *
     * @param template  the address template which is resolved against the current statement context to get the
     *                  resource address for the {@code read-children-resource} operation
     * @param childType the child resource (not human readable, but the actual child resource name!)
     * @param depth     the depth used for the {@code recursive-depth} parameter
     * @param callback  the callback which gets the result of the {@code read-children-resource} operation as {@code
     *                  List<Property>}
     */
    @JsIgnore
    public void readChildren(final AddressTemplate template, final String childType, final int depth,
            final ReadChildrenCallback callback) {
        readChildren(new Operation.Builder(template.resolve(statementContext), READ_CHILDREN_RESOURCES_OPERATION)
                        .param(CHILD_TYPE, childType)
                        .param(RECURSIVE_DEPTH, depth)
                        .build(),
                callback);
    }


    // ------------------------------------------------------ (r)ead using address

    /**
     * Executes an {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_RESOURCE_OPERATION} on the specified
     * address and passes the result to the specified callback.
     *
     * @param address  the fq address for the {@code read-resource} operation
     * @param callback the callback which gets the result of the {@code read-resource} operation
     */
    @JsIgnore
    public void read(final ResourceAddress address, final ReadCallback callback) {
        read(new Operation.Builder(address, READ_RESOURCE_OPERATION).param(INCLUDE_ALIASES, true).build(), callback);
    }

    /**
     * Executes an {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_RESOURCE_OPERATION} with the specified depth
     * on the specified address and passes the result to the specified callback.
     *
     * @param address  the fq address for the {@code read-resource} operation
     * @param depth    the depth used for the {@code recursive-depth} parameter
     * @param callback the callback which gets the result of the {@code read-resource} operation
     */
    @JsIgnore
    public void read(final ResourceAddress address, final int depth, final ReadCallback callback) {
        read(new Operation.Builder(address, READ_RESOURCE_OPERATION)
                        .param(INCLUDE_ALIASES, true)
                        .param(RECURSIVE_DEPTH, depth)
                        .build(),
                callback);
    }

    /**
     * Executes a recursive {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_RESOURCE_OPERATION} on the
     * specified address and passes the result to the specified callback.
     *
     * @param address  the fq address for the {@code read-resource} operation
     * @param callback the callback which gets the result of the {@code read-resource} operation
     */
    @JsIgnore
    public void readRecursive(final ResourceAddress address, final ReadCallback callback) {
        read(new Operation.Builder(address, READ_RESOURCE_OPERATION)
                        .param(INCLUDE_ALIASES, true)
                        .param(RECURSIVE, true)
                        .build(),
                callback);
    }

    /**
     * Executes an {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_CHILDREN_RESOURCES_OPERATION} on the
     * specified address and passes the result as {@code List<Property>} to the specified callback.
     *
     * @param address  the fq address for the {@code read-children-resource} operation
     * @param resource the child resource (not human readable, but the actual child resource name!)
     * @param callback the callback which gets the result of the {@code read-children-resource} operation as {@code
     *                 List<Property>}
     */
    @JsIgnore
    public void readChildren(final ResourceAddress address, final String resource,
            final ReadChildrenCallback callback) {
        readChildren(new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                        .param(CHILD_TYPE, resource)
                        .build(),
                callback);
    }

    /**
     * Executes an {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_CHILDREN_RESOURCES_OPERATION} on the
     * specified address and passes the result as {@code List<Property>} to the specified callback.
     *
     * @param address  the fq address for the {@code read-children-resource} operation
     * @param resource the child resource (not human readable, but the actual child resource name!)
     * @param depth    the depth used for the {@code recursive-depth} parameter
     * @param callback the callback which gets the result of the {@code read-children-resource} operation as {@code
     *                 List<Property>}
     */
    @JsIgnore
    public void readChildren(final ResourceAddress address, final String resource, final int depth,
            final ReadChildrenCallback callback) {
        readChildren(new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                        .param(CHILD_TYPE, resource)
                        .param(RECURSIVE_DEPTH, depth)
                        .build(),
                callback);
    }

    private void read(final Operation operation, final ReadCallback callback) {
        dispatcher.execute(operation, callback::execute);
    }

    private void readChildren(final Operation operation, final ReadChildrenCallback callback) {
        dispatcher.execute(operation, result -> callback.execute(result.asPropertyList()));
    }


    // ------------------------------------------------------ read different child resources

    /**
     * Read multiple different child resources using a composite operation. The steps in the composite result map to the
     * position of the resource in the {@code resources} collection.
     *
     * @param address   the fq address for the {@code read-children-resource} operation
     * @param resources the child resources (not human readable, but the actual child resource name!)
     * @param callback  the callback which gets the composite result
     */
    @JsIgnore
    public void readChildren(final ResourceAddress address, final Iterable<String> resources,
            final ReadCompositeCallback callback) {
        List<Operation> operations = stream(resources.spliterator(), false)
                .map(resource -> new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                        .param(CHILD_TYPE, resource)
                        .build())
                .collect(toList());
        dispatcher.execute(new Composite(operations), callback::execute);
    }

    /**
     * Read multiple different child resources using a composite operation. The steps in the composite result map to the
     * position of the resource in the {@code resources} collection.
     *
     * @param address   the fq address for the {@code read-children-resource} operation
     * @param resources the child resources (not human readable, but the actual child resource name!)
     * @param depth     the depth used for the {@code recursive-depth} parameter
     * @param callback  the callback which gets the composite result
     */
    @JsIgnore
    public void readChildren(final ResourceAddress address, final Iterable<String> resources, final int depth,
            final ReadCompositeCallback callback) {
        List<Operation> operations = stream(resources.spliterator(), false)
                .map(resource -> new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                        .param(CHILD_TYPE, resource)
                        .param(RECURSIVE_DEPTH, depth)
                        .build())
                .collect(toList());
        dispatcher.execute(new Composite(operations), callback::execute);
    }


    // ------------------------------------------------------ (u)pdate using template

    /**
     * Writes the changed values to the specified resource. After the resource has been saved a standard success message
     * is fired and the specified callback is executed.
     * <p>
     * If the change set is empty, a warning message is fired and the specified callback is executed.
     *
     * @param type          the human readable resource type used in the success message
     * @param name          the resource name
     * @param template      the address template which is resolved against the current statement context and the
     *                      resource name to get the resource address for the operation
     * @param changedValues the changed values / payload for the operation
     * @param callback      the callback executed after the resource has been saved
     */
    @JsIgnore
    public void save(final String type, final String name, final AddressTemplate template,
            final Map<String, Object> changedValues, final Callback callback) {
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(final Metadata metadata) {
                ResourceAddress address = template.resolve(statementContext, name);
                Composite operations = operationFactory.fromChangeSet(address, changedValues, metadata);
                save(operations, resources.messages().modifyResourceSuccess(type, name), callback);
            }
        });
    }

    /**
     * Writes the changed values to the specified resource. After the resource has been saved the specified success
     * message is fired and the specified callback is executed.
     * <p>
     * If the change set is empty, a warning message is fired and the specified callback is executed.
     *
     * @param name           the resource name
     * @param template       the address template which is resolved against the current statement context and the
     *                       resource name to get the resource address for the operation
     * @param changedValues  the changed values / payload for the operation
     * @param successMessage the success message fired after saving the resource
     * @param callback       the callback executed after the resource has been saved
     */
    @JsIgnore
    public void save(final String name, final AddressTemplate template, final Map<String, Object> changedValues,
            final SafeHtml successMessage, final Callback callback) {
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(final Metadata metadata) {
                ResourceAddress address = template.resolve(statementContext, name);
                Composite operations = operationFactory.fromChangeSet(address, changedValues, metadata);
                save(operations, successMessage, callback);
            }
        });
    }


    // ------------------------------------------------------ (u)pdate using address

    /**
     * Writes the changed values to the specified resource. After the resource has been saved a standard success message
     * is fired and the specified callback is executed.
     * <p>
     * If the change set is empty, a warning message is fired and the specified callback is executed.
     *
     * @param type          the human readable resource type used in the success message
     * @param name          the resource name
     * @param address       the fq address for the operation
     * @param changedValues the changed values / payload for the operation
     * @param metadata      the metadata of the attributes in the change set
     * @param callback      the callback executed after the resource has been saved
     */
    @JsIgnore
    public void save(final String type, final String name, final ResourceAddress address,
            final Map<String, Object> changedValues, final Metadata metadata, final Callback callback) {
        Composite operations = operationFactory.fromChangeSet(address, changedValues, metadata);
        save(operations, resources.messages().modifyResourceSuccess(type, name), callback);
    }

    /**
     * Writes the changed values to the specified resource. After the resource has been saved the specified success
     * message is fired and the specified callback is executed.
     * <p>
     * If the change set is empty, a warning message is fired and the specified callback is executed.
     *
     * @param address        the fq address for the operation
     * @param changedValues  the changed values / payload for the operation
     * @param metadata       the metadata of the attributes in the change set
     * @param successMessage the success message fired after saving the resource
     * @param callback       the callback executed after the resource has been saved
     */
    @JsIgnore
    public void save(final ResourceAddress address, final Map<String, Object> changedValues,
            final Metadata metadata, final SafeHtml successMessage, final Callback callback) {
        save(operationFactory.fromChangeSet(address, changedValues, metadata), successMessage, callback);
    }


    // ------------------------------------------------------ (u)pdate using operation

    /**
     * Writes the changed values to the specified resource. After the resource has been saved a standard success message
     * is fired and the specified callback is executed.
     * <p>
     * If the composite operation is empty (i.e. there were no changes), a warning message is fired and the specified
     * callback is executed.
     *
     * @param type       the human readable resource type used in the success message
     * @param name       the resource name
     * @param operations the composite operation to persist the changed values
     * @param callback   the callback executed after the resource has been saved
     */
    @JsIgnore
    public void save(final String type, final String name, final Composite operations, final Callback callback) {
        save(operations, resources.messages().modifyResourceSuccess(type, name), callback);
    }

    /**
     * Writes the changed values to the specified resource. After the resource has been saved the specified success
     * message is fired and the specified callback is executed.
     * <p>
     * If the composite operation is empty (i.e. there were no changes), a warning message is fired and the specified
     * callback is executed.
     *
     * @param operations     the composite operation to persist the changed values
     * @param successMessage the success message fired after saving the resource
     * @param callback       the callback executed after the resource has been saved
     */
    @JsIgnore
    public void save(final Composite operations, final SafeHtml successMessage, final Callback callback) {
        if (operations.isEmpty()) {
            MessageEvent.fire(eventBus, Message.warning(resources.messages().noChanges()));
            callback.execute();
        } else {
            dispatcher.execute(operations, (CompositeResult result) -> {
                MessageEvent.fire(eventBus, Message.success(successMessage));
                callback.execute();
            });
        }
    }


    // ------------------------------------------------------ (u)pdate singleton using template

    /**
     * Writes the changed values to the specified singleton resource. After the resource has been saved a success
     * message is fired and the specified callback is executed.
     *
     * @param type          the human readable resource type used in the success message
     * @param template      the address template which is resolved against the current statement context to get the
     *                      resource address for the operation
     * @param changedValues the changed values / payload for the operation
     * @param callback      the callback executed after the resource has been saved
     */
    @JsIgnore
    public void saveSingleton(final String type, final AddressTemplate template,
            final Map<String, Object> changedValues, final Callback callback) {
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(final Metadata metadata) {
                saveSingleton(template.resolve(statementContext), changedValues, metadata,
                        resources.messages().modifySingleResourceSuccess(type), callback);
            }
        });
    }

    /**
     * Writes the changed values to the specified singleton resource. After the resource has been saved a success
     * message is fired and the specified callback is executed.
     *
     * @param template       the address template which is resolved against the current statement context to get the
     *                       resource address for the operation
     * @param changedValues  the changed values / payload for the operation
     * @param successMessage the success message fired after saving the resource
     * @param callback       the callback executed after the resource has been saved
     */
    @JsIgnore
    public void saveSingleton(final AddressTemplate template, final Map<String, Object> changedValues,
            final SafeHtml successMessage, final Callback callback) {
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(final Metadata metadata) {
                saveSingleton(template.resolve(statementContext), changedValues, metadata, successMessage, callback);
            }
        });
    }


    // ------------------------------------------------------ (u)pdate singleton using address

    /**
     * Writes the changed values to the specified singleton resource. After the resource has been saved a success
     * message is fired and the specified callback is executed.
     *
     * @param type          the human readable resource type used in the success message
     * @param address       the fq address for the operation
     * @param changedValues the changed values / payload for the operation
     * @param metadata      the metadata of the attributes in the change set
     * @param callback      the callback executed after the resource has been saved
     */
    @JsIgnore
    public void saveSingleton(final String type, final ResourceAddress address, final Map<String, Object> changedValues,
            final Metadata metadata, final Callback callback) {
        saveSingleton(address, changedValues, metadata, resources.messages().modifySingleResourceSuccess(type),
                callback);
    }

    /**
     * Writes the changed values to the specified singleton resource. After the resource has been saved a success
     * message is fired and the specified callback is executed.
     *
     * @param address        the fq address for the operation
     * @param changedValues  the changed values / payload for the operation
     * @param metadata       the metadata of the attributes in the change set
     * @param successMessage the success message fired after saving the resource
     * @param callback       the callback executed after the resource has been saved
     */
    @JsIgnore
    public void saveSingleton(final ResourceAddress address, final Map<String, Object> changedValues,
            final Metadata metadata, final SafeHtml successMessage, final Callback callback) {
        save(operationFactory.fromChangeSet(address, changedValues, metadata), successMessage, callback);
    }


    // ------------------------------------------------------ (u) reset using template

    /**
     * Undefines all non required attributes in the specified form. After the resource has been undefined a standard
     * success message is fired and the specified callback is executed.
     * <p>
     * If the form contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type     the human readable resource type used in the success message
     * @param name     the resource name
     * @param template the address template which is resolved against the current statement context and the
     *                 resource name to get the resource address for the operation
     * @param form     the form which should be reset
     * @param metadata the metadata of the attributes
     * @param callback the callback executed after the resource has been undefined
     */
    @JsIgnore
    public <T> void reset(final String type, final String name, final AddressTemplate template,
            final Form<T> form, final Metadata metadata, final Callback callback) {
        Set<String> attributes = stream(form.getBoundFormItems().spliterator(), false)
                .map(FormItem::getName)
                .collect(toSet());
        reset(type, name, template.resolve(statementContext), attributes, metadata,
                resources.messages().resetResourceSuccess(type, name), callback);
    }

    /**
     * Undefines all non required attributes in the specified form. After the resource has been undefined the specified
     * success message is fired and the specified callback is executed.
     * <p>
     * If the form contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type           the human readable resource type used in the success message
     * @param name           the resource name
     * @param template       the address template which is resolved against the current statement context and the
     *                       resource name to get the resource address for the operation
     * @param form           the form which should be reset
     * @param metadata       the metadata of the attributes
     * @param successMessage the success message fired after resetting the resource
     * @param callback       the callback executed after the resource has been undefined
     */
    @JsIgnore
    public <T> void reset(final String type, final String name, final AddressTemplate template,
            final Form<T> form, final Metadata metadata, final SafeHtml successMessage, final Callback callback) {
        Set<String> attributes = stream(form.getBoundFormItems().spliterator(), false)
                .map(FormItem::getName)
                .collect(toSet());
        reset(type, name, template.resolve(statementContext), attributes, metadata, successMessage, callback);
    }

    /**
     * Undefines all non required attributes in the specified set. After the resource has been undefined a standard
     * success message is fired and the specified callback is executed.
     * <p>
     * If the set contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type       the human readable resource type used in the success message
     * @param name       the resource name
     * @param template   the address template which is resolved against the current statement context and the
     *                   resource name to get the resource address for the operation
     * @param attributes the attributes which should be reset
     * @param metadata   the metadata of the attributes
     * @param callback   the callback executed after the resource has been undefined
     */
    @JsIgnore
    public void reset(final String type, final String name, final AddressTemplate template,
            final Set<String> attributes, final Metadata metadata, final Callback callback) {
        reset(type, name, template.resolve(statementContext), attributes, metadata,
                resources.messages().resetResourceSuccess(type, name), callback);
    }

    /**
     * Undefines all non required attributes in the specified set. After the resource has been undefined the specified
     * success message is fired and the specified callback is executed.
     * <p>
     * If the set contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type           the human readable resource type used in the success message
     * @param name           the resource name
     * @param template       the address template which is resolved against the current statement context and the
     *                       resource name to get the resource address for the operation
     * @param attributes     the attributes which should be reset
     * @param metadata       the metadata of the attributes
     * @param successMessage the success message fired after resetting the resource
     * @param callback       the callback executed after the resource has been undefined
     */
    @JsIgnore
    public void reset(final String type, final String name, final AddressTemplate template,
            final Set<String> attributes, final Metadata metadata, final SafeHtml successMessage,
            final Callback callback) {
        reset(type, name, template.resolve(statementContext), attributes, metadata, successMessage, callback);
    }


    // ------------------------------------------------------ (u) reset using address

    /**
     * Undefines all non required attributes in the specified form. After the resource has been undefined a standard
     * success message is fired and the specified callback is executed.
     * <p>
     * If the form contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type     the human readable resource type used in the success message
     * @param name     the resource name
     * @param address  the fq address for the operation
     * @param form     the form which should be reset
     * @param metadata the metadata of the attributes
     * @param callback the callback executed after the resource has been undefined
     */
    @JsIgnore
    public <T> void reset(final String type, final String name, final ResourceAddress address,
            final Form<T> form, final Metadata metadata, final Callback callback) {
        Set<String> attributes = stream(form.getBoundFormItems().spliterator(), false)
                .map(FormItem::getName)
                .collect(toSet());
        reset(type, name, address, attributes, metadata, resources.messages().resetResourceSuccess(type, name),
                callback);
    }

    /**
     * Undefines all non required attributes in the specified form. After the resource has been undefined the specified
     * success message is fired and the specified callback is executed.
     * <p>
     * If the form contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type           the human readable resource type used in the success message
     * @param name           the resource name
     * @param address        the fq address for the operation
     * @param form           the from which should be reset
     * @param metadata       the metadata of the attributes
     * @param successMessage the success message fired after resetting the resource
     * @param callback       the callback executed after the resource has been undefined
     */
    @JsIgnore
    public <T> void reset(final String type, final String name, final ResourceAddress address,
            final Form<T> form, final Metadata metadata, final SafeHtml successMessage, final Callback callback) {
        Set<String> attributes = stream(form.getBoundFormItems().spliterator(), false)
                .map(FormItem::getName)
                .collect(toSet());
        reset(type, name, address, attributes, metadata, successMessage, callback);
    }

    /**
     * Undefines all non required attributes in the specified set. After the resource has been undefined a standard
     * success message is fired and the specified callback is executed.
     * <p>
     * If the set contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type       the human readable resource type used in the success message
     * @param name       the resource name
     * @param address    the fq address for the operation
     * @param attributes the attributes which should be reset
     * @param metadata   the metadata of the attributes
     * @param callback   the callback executed after the resource has been undefined
     */
    @JsIgnore
    public void reset(final String type, final String name, final ResourceAddress address,
            final Set<String> attributes, final Metadata metadata, final Callback callback) {
        reset(type, name, address, attributes, metadata, resources.messages().resetResourceSuccess(type, name),
                callback);
    }

    /**
     * Undefines all non required attributes in the specified set. After the resource has been undefined the specified
     * success message is fired and the specified callback is executed.
     * <p>
     * If the set contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type           the human readable resource type used in the success message
     * @param name           the resource name
     * @param address        the fq address for the operation
     * @param attributes     the attributes which should be reset
     * @param metadata       the metadata of the attributes
     * @param successMessage the success message fired after resetting the resource
     * @param callback       the callback executed after the resource has been undefined
     */
    @JsIgnore
    public void reset(final String type, final String name, final ResourceAddress address,
            final Set<String> attributes, final Metadata metadata, final SafeHtml successMessage,
            final Callback callback) {
        Composite composite = operationFactory.resetResource(address, attributes, metadata);
        if (composite.isEmpty()) {
            MessageEvent.fire(eventBus, Message.warning(resources.messages().noReset()));
            callback.execute();
        } else {
            SafeHtml question = name == null
                    ? resources.messages().resetSingletonConfirmationQuestion()
                    : resources.messages().resetConfirmationQuestion(name);
            DialogFactory.showConfirmation(
                    resources.messages().resetConfirmationTitle(type), question,
                    () -> dispatcher.execute(composite, (CompositeResult result) -> {
                        MessageEvent.fire(eventBus, Message.success(successMessage));
                        callback.execute();
                    }));
        }
    }


    // ------------------------------------------------------ (u) reset singleton using template

    /**
     * Undefines all non required attributes in the specified form. After the singleton resource has been undefined a
     * standard success message is fired and the specified callback is executed.
     * <p>
     * If the set contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type     the human readable resource type used in the success message
     * @param template the address template which is resolved against the current statement context and the
     *                 resource name to get the resource address for the operation
     * @param form     the form which should be reset
     * @param metadata the metadata of the attributes
     * @param callback the callback executed after the resource has been undefined
     */
    @JsIgnore
    public <T> void resetSingleton(final String type, final AddressTemplate template,
            final Form<T> form, final Metadata metadata, final Callback callback) {
        Set<String> attributes = stream(form.getBoundFormItems().spliterator(), false)
                .map(FormItem::getName)
                .collect(toSet());
        reset(type, null, template.resolve(statementContext), attributes, metadata,
                resources.messages().resetSingletonSuccess(type), callback);
    }

    /**
     * Undefines all non required attributes in the specified form. After the singleton resource has been undefined the
     * specified success message is fired and the specified callback is executed.
     * <p>
     * If the set contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type           the human readable resource type used in the success message
     * @param template       the address template which is resolved against the current statement context and the
     *                       resource name to get the resource address for the operation
     * @param form           the form which should be reset
     * @param metadata       the metadata of the attributes
     * @param successMessage the success message fired after resetting the resource
     * @param callback       the callback executed after the resource has been undefined
     */
    @JsIgnore
    public <T> void resetSingleton(final String type, final AddressTemplate template,
            final Form<T> form, final Metadata metadata, final SafeHtml successMessage, final Callback callback) {
        Set<String> attributes = stream(form.getBoundFormItems().spliterator(), false)
                .map(FormItem::getName)
                .collect(toSet());
        reset(type, null, template.resolve(statementContext), attributes, metadata, successMessage, callback);
    }

    /**
     * Undefines all non required attributes in the specified set. After the singleton resource has been undefined a
     * standard success message is fired and the specified callback is executed.
     * <p>
     * If the set contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type       the human readable resource type used in the success message
     * @param template   the address template which is resolved against the current statement context and the
     *                   resource name to get the resource address for the operation
     * @param attributes the attributes which should be reset
     * @param metadata   the metadata of the attributes
     * @param callback   the callback executed after the resource has been undefined
     */
    @JsIgnore
    public void resetSingleton(final String type, final AddressTemplate template,
            final Set<String> attributes, final Metadata metadata, final Callback callback) {
        reset(type, null, template.resolve(statementContext), attributes, metadata,
                resources.messages().resetSingletonSuccess(type), callback);
    }

    /**
     * Undefines all non required attributes in the specified set. After the singleton resource has been undefined the
     * specified success message is fired and the specified callback is executed.
     * <p>
     * If the set contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type           the human readable resource type used in the success message
     * @param template       the address template which is resolved against the current statement context and the
     *                       resource name to get the resource address for the operation
     * @param attributes     the attributes which should be reset
     * @param metadata       the metadata of the attributes
     * @param successMessage the success message fired after resetting the resource
     * @param callback       the callback executed after the resource has been undefined
     */
    @JsIgnore
    public void resetSingleton(final String type, final AddressTemplate template,
            final Set<String> attributes, final Metadata metadata, final SafeHtml successMessage,
            final Callback callback) {
        reset(type, null, template.resolve(statementContext), attributes, metadata, successMessage, callback);
    }


    // ------------------------------------------------------ (u) reset singleton using address

    /**
     * Undefines all non required attributes in the specified form. After the singleton resource has been undefined a
     * standard success message is fired and the specified callback is executed.
     * <p>
     * If the set contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type     the human readable resource type used in the success message
     * @param address  the fq address for the operation
     * @param form     the form which should be reset
     * @param metadata the metadata of the attributes
     * @param callback the callback executed after the resource has been undefined
     */
    @JsIgnore
    public <T> void resetSingleton(final String type, final ResourceAddress address,
            final Form<T> form, final Metadata metadata, final Callback callback) {
        Set<String> attributes = stream(form.getBoundFormItems().spliterator(), false)
                .map(FormItem::getName)
                .collect(toSet());
        reset(type, null, address, attributes, metadata, resources.messages().resetSingletonSuccess(type),
                callback);
    }

    /**
     * Undefines all non required attributes in the specified form. After the singleton resource has been undefined the
     * specified success message is fired and the specified callback is executed.
     * <p>
     * If the set contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type           the human readable resource type used in the success message
     * @param address        the fq address for the operation
     * @param form           the form which should be reset
     * @param metadata       the metadata of the attributes
     * @param successMessage the success message fired after resetting the resource
     * @param callback       the callback executed after the resource has been undefined
     */
    @JsIgnore
    public <T> void resetSingleton(final String type, final ResourceAddress address,
            final Form<T> form, final Metadata metadata, final SafeHtml successMessage, final Callback callback) {
        Set<String> attributes = stream(form.getBoundFormItems().spliterator(), false)
                .map(FormItem::getName)
                .collect(toSet());
        reset(type, null, address, attributes, metadata, successMessage, callback);
    }

    /**
     * Undefines all non required attributes in the specified set. After the singleton  resource has been undefined a
     * standard success message is fired and the specified callback is executed.
     * <p>
     * If the set contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type       the human readable resource type used in the success message
     * @param address    the fq address for the operation
     * @param attributes the attributes which should be reset
     * @param metadata   the metadata of the attributes
     * @param callback   the callback executed after the resource has been undefined
     */
    @JsIgnore
    public void resetSingleton(final String type, final ResourceAddress address,
            final Set<String> attributes, final Metadata metadata, final Callback callback) {
        reset(type, null, address, attributes, metadata, resources.messages().resetSingletonSuccess(type),
                callback);
    }

    /**
     * Undefines all non required attributes in the specified set. After the singleton  resource has been undefined the
     * specified success message is fired and the specified callback is executed.
     * <p>
     * If the set contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type           the human readable resource type used in the success message
     * @param address        the fq address for the operation
     * @param attributes     the attributes which should be reset
     * @param metadata       the metadata of the attributes
     * @param successMessage the success message fired after resetting the resource
     * @param callback       the callback executed after the resource has been undefined
     */
    @JsIgnore
    public void resetSingleton(final String type, final ResourceAddress address,
            final Set<String> attributes, final Metadata metadata, final SafeHtml successMessage,
            final Callback callback) {
        reset(type, null, address, attributes, metadata, successMessage, callback);
    }


    // ------------------------------------------------------ (d)elete using template

    /**
     * Shows a confirmation dialog and removes the resource if confirmed by the user. After the resource has been
     * removed a success message is fired and the specified callback is executed.
     *
     * @param type     the human readable resource type used in the success message
     * @param name     the resource name
     * @param template the address template which is resolved against the current statement context and the resource
     *                 name to get the resource address for the {@code remove} operation
     * @param callback the callback executed after the resource has been removed
     */
    @JsIgnore
    public void remove(final String type, final String name, final AddressTemplate template, final Callback callback) {
        remove(type, name, template.resolve(statementContext, name), callback);
    }

    /**
     * Shows a confirmation dialog and removes the singleton resource if confirmed by the user. After the resource has
     * been removed a success message is fired and the specified callback is executed.
     *
     * @param type     the human readable resource type used in the success message
     * @param template the address template which is resolved against the current statement context to get the resource
     *                 address for the {@code remove} operation
     * @param callback the callback executed after the resource has been removed
     */
    @JsIgnore
    public void removeSingleton(final String type, final AddressTemplate template, final Callback callback) {
        remove(type, null, template.resolve(statementContext), callback);
    }


    // ------------------------------------------------------ (d)elete using address

    /**
     * Shows a confirmation dialog and removes the resource if confirmed by the user. After the resource has been
     * removed a success message is fired and the specified callback is executed.
     *
     * @param type     the human readable resource type used in the success message
     * @param name     the resource name
     * @param address  the fq address for the {@code remove} operation
     * @param callback the callback executed after the resource has been removed
     */
    @JsIgnore
    public void remove(final String type, final String name, final ResourceAddress address, final Callback callback) {
        String title = resources.messages().removeConfirmationTitle(type);
        SafeHtml question = name == null
                ? resources.messages().removeSingletonConfirmationQuestion()
                : resources.messages().removeConfirmationQuestion(name);
        SafeHtml success = name == null
                ? resources.messages().removeSingletonResourceSuccess(type)
                : resources.messages().removeResourceSuccess(type, name);

        DialogFactory.showConfirmation(title, question, () -> {
            Operation operation = new Operation.Builder(address, REMOVE).build();
            dispatcher.execute(operation, result -> {
                MessageEvent.fire(eventBus, Message.success(success));
                callback.execute();
            });
        });
    }

    /**
     * Shows a confirmation dialog and removes the singleton resource if confirmed by the user. After the resource has
     * been removed a success message is fired and the specified callback is executed.
     *
     * @param type     the human readable resource type used in the success message
     * @param address  the fq address for the {@code remove} operation
     * @param callback the callback executed after the resource has been removed
     */
    @JsIgnore
    public void removeSingleton(final String type, final ResourceAddress address, final Callback callback) {
        remove(type, null, address, callback);
    }


    // ------------------------------------------------------ JS methods


    @JsFunction
    public interface JsReadChildrenCallback {

        void execute(Property[] children);
    }

    /**
     * Opens an add-resource dialog for the given resource type. The dialog contains fields for all required request
     * properties. When clicking "Add", a new resource is added using the specified address. After the resource has been
     * added a success message is displayed and the callback is executed.
     *
     * @param type       The human readable resource type used in the dialog header and success message.
     * @param address    The address for the add operation. Must end in <code>&lt;resource type&gt;=*</code>.
     * @param attributes Additional attributes (besides the required attributes) which should be part of the
     *                   add-resource dialog. May be null or empty.
     * @param callback   The callback executed after the resource has been added.
     */
    @JsMethod(name = "addDialog")
    public void jsAddDialog(String type,
            @EsParam("AddressTemplate|ResourceAddress|string") Object address,
            @EsParam("string[]") String[] attributes,
            @EsParam("function(name: string, address: ResourceAddress)") AddCallback callback) {

        String id = Ids.build(type, Ids.ADD_SUFFIX, Ids.uniqueId());
        if (address instanceof AddressTemplate) {
            if (attributes != null) {
                add(id, type, ((AddressTemplate) address), asList(attributes), callback);
            } else {
                add(id, type, ((AddressTemplate) address), callback);
            }
        } else if (address instanceof ResourceAddress) {
            AddressTemplate template = AddressTemplate.of(((ResourceAddress) address));
            if (attributes != null) {
                add(id, type, template, asList(attributes), callback);
            } else {
                add(id, type, template, callback);
            }
        } else if (address instanceof String) {
            if (attributes != null) {
                add(id, type, (AddressTemplate.of((String) address)), asList(attributes), callback);
            } else {
                add(id, type, (AddressTemplate.of((String) address)), callback);
            }
        } else {
            throw new IllegalArgumentException(
                    "Illegal 2nd argument: Use CrudOperations.addDialog(string, (AddressTemplate|ResourceAddress|String), string[], function(ResourceAddress, string))");
        }
    }

    /**
     * Executes an add operation using the specified name and payload. After the resource has been added a success
     * message is displayed and the callback is executed.
     *
     * @param type     The human readable resource type used in the dialog header and success message.
     * @param name     The resource name which is part of the add operation.
     * @param address  The address for the add operation. Must end in <code>&lt;resource type&gt;=*</code>.
     * @param payload  The optional payload of the add operation (may be null or undefined).
     * @param callback The callback executed after the resource has been added.
     */
    @JsMethod(name = "add")
    public void jsAdd(String type, String name,
            @EsParam("AddressTemplate|ResourceAddress|string") Object address, ModelNode payload,
            @EsParam("function(name: string, address: ResourceAddress)") AddCallback callback) {

        if (address instanceof AddressTemplate) {
            add(type, name, ((AddressTemplate) address).resolve(statementContext), payload, callback);
        } else if (address instanceof ResourceAddress) {
            add(type, name, ((ResourceAddress) address), payload, callback);
        } else if (address instanceof String) {
            add(type, name, AddressTemplate.of(((String) address)).resolve(statementContext), payload, callback);
        } else {
            throw new IllegalArgumentException(
                    "Illegal 3rd argument: Use CrudOperations.add(string, string, (AddressTemplate|ResourceAddress|string), ModelNode, function(ResourceAddress, string))");
        }
    }

    /**
     * Opens an add-resource dialog for the given singleton resource type. The dialog contains fields for all required
     * request properties. When clicking "Add", a new singleton resource is added using the specified address template.
     * After the singleton resource has been added a success message is displayed and the callback is executed.
     *
     * @param type       The human readable resource type used in the dialog header and success message.
     * @param address    The address for the add operation. Must end in <code>&lt;resource type&gt;=&lt;resource
     *                   name&gt;</code>.
     * @param attributes Additional attributes (besides the required attributes) which should be part of the
     *                   add-resource dialog. May be null or empty.
     * @param callback   The callback executed after the singleton resource has been added.
     */
    @JsMethod(name = "addSingletonDialog")
    public void jsAddSingletonDialog(String type, Object address,
            @EsParam("string[]") String[] attributes,
            @EsParam("function(address: ResourceAddress)") AddSingletonCallback callback) {

        String id = Ids.build(type, Ids.ADD_SUFFIX, Ids.uniqueId());
        if (address instanceof AddressTemplate) {
            if (attributes != null) {
                addSingleton(id, type, ((AddressTemplate) address), asList(attributes), callback);
            } else {
                addSingleton(id, type, ((AddressTemplate) address), callback);
            }
        } else if (address instanceof ResourceAddress) {
            AddressTemplate template = AddressTemplate.of(((ResourceAddress) address));
            if (attributes != null) {
                addSingleton(id, type, template, asList(attributes), callback);
            } else {
                addSingleton(id, type, template, callback);
            }
        } else if (address instanceof String) {
            if (attributes != null) {
                addSingleton(id, type, (AddressTemplate.of((String) address)), asList(attributes), callback);
            } else {
                addSingleton(id, type, (AddressTemplate.of((String) address)), callback);
            }
        } else {
            throw new IllegalArgumentException(
                    "Illegal 2nd argument: Use CrudOperations.addSingletonDialog(string, (AddressTemplate|ResourceAddress|string), string[], function(ResourceAddress, string))");
        }
    }

    /**
     * Executes an add operation using the specified payload. After the resource has been added a success message is
     * displayed and the callback is executed.
     *
     * @param type     The human readable resource type used in the dialog header and success message.
     * @param address  The address for the add operation. Must end in <code>&lt;resource type&gt;=&lt;resource
     *                 name&gt;</code>.
     * @param payload  The optional payload of the add operation (may be null or undefined).
     * @param callback The callback executed after the singleton resource has been added.
     */
    @JsMethod(name = "addSingleton")
    public void jsAddSingleton(String type,
            @EsParam("AddressTemplate|ResourceAddress|string") Object address, final ModelNode payload,
            @EsParam("function(address: ResourceAddress)") AddSingletonCallback callback) {

        if (address instanceof AddressTemplate) {
            addSingleton(type, ((AddressTemplate) address).resolve(statementContext), payload, callback);
        } else if (address instanceof ResourceAddress) {
            addSingleton(type, ((ResourceAddress) address), payload, callback);
        } else if (address instanceof String) {
            addSingleton(type, AddressTemplate.of(((String) address)).resolve(statementContext), payload, callback);
        } else {
            throw new IllegalArgumentException(
                    "Illegal 2nd argument: Use CrudOperations.addSingleton(string, (AddressTemplate|ResourceAddress|string), ModelNode, function(ResourceAddress, string))");
        }
    }

    /**
     * Executes a read-resource operation on the specified address and passes the result to the callback.
     *
     * @param address  The address for the read-resource operation operation.
     * @param callback The callback which gets the result of the read-resource operation.
     */
    @JsMethod(name = "read")
    public void jsRead(@EsParam("AddressTemplate|ResourceAddress|string") Object address,
            @EsParam("function(result: ModelNode)") ReadCallback callback) {

        if (address instanceof AddressTemplate) {
            read((AddressTemplate) address, callback);
        } else if (address instanceof ResourceAddress) {
            read((ResourceAddress) address, callback);
        } else if (address instanceof String) {
            read(AddressTemplate.of((String) address), callback);
        } else {
            throw new IllegalArgumentException(
                    "Illegal 1st argument: Use CrudOperations.read((AddressTemplate|ResourceAddress|string), function(ModelNode))");
        }
    }

    /**
     * Executes a recursive read-resource operation on the specified address and passes the result to the callback.
     *
     * @param address  The address for the read-resource operation operation.
     * @param callback The callback which gets the result of the read-resource operation.
     */
    @JsMethod(name = "readRecursive")
    public void jsReadRecursive(@EsParam("AddressTemplate|ResourceAddress|string") Object address,
            @EsParam("function(result: ModelNode)") ReadCallback callback) {

        if (address instanceof AddressTemplate) {
            readRecursive((AddressTemplate) address, callback);
        } else if (address instanceof ResourceAddress) {
            readRecursive((ResourceAddress) address, callback);
        } else if (address instanceof String) {
            readRecursive(AddressTemplate.of((String) address), callback);
        } else {
            throw new IllegalArgumentException(
                    "Illegal 1st argument: Use CrudOperations.readRecursive((AddressTemplate|ResourceAddress|string), function(ModelNode))");
        }
    }

    /**
     * Executes a read-children-resources operation on the specified address and passes the result as {@link Property}
     * array to the callback.
     *
     * @param address   The address for the read-children-resources operation.
     * @param childType The child resource type.
     * @param callback  The callback which gets the result of the read-children-resources operation.
     */
    @JsMethod(name = "readChildren")
    public void jsReadChildren(@EsParam("AddressTemplate|ResourceAddress|string") Object address, String childType,
            @EsParam("function(children: Property[])") JsReadChildrenCallback callback) {

        ReadChildrenCallback rcc = children -> callback.execute(children.toArray(new Property[children.size()]));
        if (address instanceof AddressTemplate) {
            readChildren((AddressTemplate) address, childType, rcc);
        } else if (address instanceof ResourceAddress) {
            readChildren((ResourceAddress) address, childType, rcc);
        } else if (address instanceof String) {
            readChildren(AddressTemplate.of((String) address), childType, rcc);
        } else {
            throw new IllegalArgumentException(
                    "Illegal 1st argument: Use CrudOperations.readChildren((AddressTemplate|ResourceAddress|string), string, function(Property[]))");
        }
    }

    /**
     * Writes the changed values to the specified resource. After the resource has been saved a standard success message
     * is displayed and the callback is executed. If the change set is empty, a warning message is displayed and the
     * callback is executed.
     *
     * @param type      The human readable resource type used in the success message.
     * @param name      The resource name.
     * @param address   The address for the operation.
     * @param changeSet A key-value map containing the changes to the resource.
     * @param callback  The callback executed after the resource has been saved.
     */
    @JsMethod(name = "save")
    public void jsSave(String type, String name, @EsParam("AddressTemplate|ResourceAddress|string") Object address,
            @EsParam("{key: string, value: object}") JsPropertyMapOfAny changeSet,
            @EsParam("function()") JsCallback callback) {

        Callback c = callback::execute;
        if (address instanceof AddressTemplate) {
            save(type, name, ((AddressTemplate) address), JsHelper.asMap(changeSet), c);
        } else if (address instanceof ResourceAddress) {
            AddressTemplate template = AddressTemplate.of(((ResourceAddress) address));
            save(type, name, template, JsHelper.asMap(changeSet), c);
        } else if (address instanceof String) {
            save(type, name, AddressTemplate.of(((String) address)), JsHelper.asMap(changeSet), c);
        } else {
            throw new IllegalArgumentException(
                    "Illegal 3rd argument: Use CrudOperations.save(string, string, (AddressTemplate|ResourceAddress|string), {\"key\": <value>}, function())");
        }
    }

    /**
     * Writes the changed values to the specified singleton resource. After the resource has been saved a standard
     * success message is displayed and the callback is executed. If the change set is empty, a warning message is
     * displayed and the callback is executed.
     *
     * @param type      The human readable resource type used in the success message.
     * @param address   The address for the operation.
     * @param changeSet A key-value map containing the changes to the resource.
     * @param callback  The callback executed after the singleton resource has been saved.
     */
    @JsMethod(name = "saveSingleton")
    public void jsSaveSingleton(String type, @EsParam("AddressTemplate|ResourceAddress|string") Object address,
            @EsParam("{key: string, value: object}") JsPropertyMapOfAny changeSet,
            @EsParam("function()") JsCallback callback) {

        Callback c = callback::execute;
        if (address instanceof AddressTemplate) {
            saveSingleton(type, ((AddressTemplate) address), JsHelper.asMap(changeSet), c);
        } else if (address instanceof String) {
            saveSingleton(type, AddressTemplate.of(((String) address)), JsHelper.asMap(changeSet), c);
        } else {
            throw new IllegalArgumentException(
                    "Illegal 2nd argument: Use CrudOperations.save(string, (AddressTemplate|ResourceAddress|string), {\"key\": <value>}, function())");
        }
    }

    /**
     * Shows a confirmation dialog and removes the resource if confirmed by the user. After the resource has been
     * removed a success message is displayed and the callback is executed.
     *
     * @param type     The human readable resource type used in the success message.
     * @param name     The resource name.
     * @param address  The address for the operation.
     * @param callback The callback executed after the resource has been removed.
     */
    @JsMethod(name = "remove")
    public void jsRemove(String type, String name, @EsParam("AddressTemplate|ResourceAddress|string") Object address,
            @EsParam("function()") JsCallback callback) {

        Callback c = callback::execute;
        if (address instanceof AddressTemplate) {
            remove(type, name, ((AddressTemplate) address), c);
        } else if (address instanceof ResourceAddress) {
            remove(type, name, ((ResourceAddress) address), c);
        } else if (address instanceof String) {
            remove(type, name, AddressTemplate.of(((String) address)), c);
        } else {
            throw new IllegalArgumentException(
                    "Illegal 3rd argument: Use CrudOperations.remove(string, string, (AddressTemplate|ResourceAddress|string), function())");
        }
    }

    /**
     * Shows a confirmation dialog and removes the singleton resource if confirmed by the user. After the resource has
     * been removed a success message is displayed and the callback is executed.
     *
     * @param type     The human readable resource type used in the success message.
     * @param address  The address for the operation.
     * @param callback The callback executed after the resource has been removed.
     */
    @JsMethod(name = "removeSingleton")
    public void jsRemoveSingleton(String type, @EsParam("AddressTemplate|ResourceAddress|string") Object address,
            @EsParam("function()") JsCallback callback) {

        Callback c = callback::execute;
        if (address instanceof AddressTemplate) {
            removeSingleton(type, ((AddressTemplate) address), c);
        } else if (address instanceof ResourceAddress) {
            removeSingleton(type, ((ResourceAddress) address), c);
        } else if (address instanceof String) {
            removeSingleton(type, AddressTemplate.of(((String) address)), c);
        } else {
            throw new IllegalArgumentException(
                    "Illegal 2nd argument: Use CrudOperations.removeSingleton(string, (AddressTemplate|ResourceAddress|string), function())");
        }
    }
}

