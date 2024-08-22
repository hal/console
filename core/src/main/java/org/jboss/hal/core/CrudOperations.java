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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.FormItemValidation;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.meta.processing.SuccessfulMetadataCallback;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import com.google.common.collect.Iterables;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;

import elemental2.promise.Promise;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_ALIASES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE_DEPTH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;

/**
 * Contains generic CRUD methods to add, read, update and remove (singleton) resources. Some methods just execute the underlying
 * DMR operations, other methods also interact with the user by showing (confirmation) dialogs.
 */
@SuppressWarnings("DuplicateStringLiteralInspection")
public class CrudOperations {

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final MetadataProcessor metadataProcessor;
    private final Provider<Progress> progress;
    private final StatementContext statementContext;
    private final Resources resources;
    private final OperationFactory operationFactory;

    @Inject
    public CrudOperations(EventBus eventBus,
            Dispatcher dispatcher,
            MetadataProcessor metadataProcessor,
            @Footer Provider<Progress> progress,
            StatementContext statementContext,
            Resources resources) {
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
     * Opens an add-resource-dialog for the given resource type. The dialog contains fields for all required request properties.
     * When clicking "Add", a new resource is added using the specified address template. After the resource has been added a
     * success message is fired and the specified callback is executed.
     *
     * @param id the id used for the add resource dialog
     * @param type the human readable resource type used in the dialog header and success message
     * @param template the address template which is resolved against the current statement context to get the resource address
     *        for the add operation
     * @param callback the callback executed after the resource has been added
     */
    public void add(String id, String type, AddressTemplate template, AddCallback callback) {
        add(id, type, template, Collections.emptyList(), callback);
    }

    public void add(String id, String type, AddressTemplate template,
            @Nullable FormItemValidation<String> nameItemValidator, AddCallback callback) {
        add(id, type, template, Collections.emptyList(), nameItemValidator, callback);
    }

    /**
     * Opens an add-resource-dialog for the given resource type. The dialog contains fields for all required request properties
     * plus the ones specified by {@code attributes}. When clicking "Add", a new resource is added using the specified address
     * template. After the resource has been added a success message is fired and the specified callback is executed.
     *
     * @param id the id used for the add resource dialog
     * @param type the human readable resource type used in the dialog header and success message
     * @param template the address template which is resolved against the current statement context to get the resource address
     *        for the add operation
     * @param attributes additional attributes which should be part of the add resource dialog
     * @param callback the callback executed after the resource has been added
     */
    public void add(String id, String type, AddressTemplate template, Iterable<String> attributes,
            AddCallback callback) {
        add(id, type, template, attributes, null, callback);
    }

    public void add(String id, String type, AddressTemplate template, Iterable<String> attributes,
            @Nullable FormItemValidation<String> nameItemValidator, AddCallback callback) {
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(Metadata metadata) {
                AddResourceDialog dialog = new AddResourceDialog(id, resources.messages().addResourceTitle(type),
                        metadata, attributes, (name, model) -> add(type, name, template, model, callback));
                if (nameItemValidator != null) {
                    dialog.getForm().<String> getFormItem(NAME).addValidationHandler(nameItemValidator);
                }
                dialog.show();
            }
        });
    }

    // ------------------------------------------------------ (c)reate operation

    /**
     * Executes an add operation using the specified name and payload. After the resource has been added a success message is
     * fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the dialog header and success message
     * @param name the resource name which is part of the add operation
     * @param template the address template which is resolved against the current statement context and the resource name to get
     *        the resource address for the add operation
     * @param payload the optional payload of the add operation (may be null or undefined)
     * @param callback the callback executed after the resource has been added
     */
    public void add(String type, String name, AddressTemplate template, @Nullable ModelNode payload,
            AddCallback callback) {
        add(name, template.resolve(statementContext, name), payload,
                resources.messages().addResourceSuccess(type, name), callback);
    }

    /**
     * Executes an add operation using the specified name and payload. After the resource has been added the specified success
     * message is fired and the specified callback is executed.
     *
     * @param name the resource name which is part of the add operation
     * @param template the address template which is resolved against the current statement context and the resource name to get
     *        the resource address for the add operation
     * @param payload the optional payload of the add operation (may be null or undefined)
     * @param successMessage the success message fired after adding the resource
     * @param callback the callback executed after the resource has been added
     */
    public void add(String name, AddressTemplate template, @Nullable ModelNode payload, SafeHtml successMessage,
            AddCallback callback) {
        add(name, template.resolve(statementContext, name), payload, successMessage, callback);
    }

    /**
     * Executes an add operation using the specified name and payload. After the resource has been added a success message is
     * fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the dialog header and success message
     * @param name the resource name which is part of the add operation
     * @param address the fq address for the add operation
     * @param payload the optional payload of the add operation (may be null or undefined)
     * @param callback the callback executed after the resource has been added
     */
    public void add(String type, String name, ResourceAddress address, @Nullable ModelNode payload,
            AddCallback callback) {
        add(name, address, payload, resources.messages().addResourceSuccess(type, name), callback);
    }

    /**
     * Executes an add operation using the specified name and payload. After the resource has been added the specified success
     * message is fired and the specified callback is executed.
     *
     * @param name the resource name which is part of the add operation
     * @param address the fq address for the add operation
     * @param payload the optional payload of the add operation (may be null or undefined)
     * @param successMessage the success message fired after adding the resource
     * @param callback the callback executed after the resource has been added
     */
    public void add(String name, ResourceAddress address, @Nullable ModelNode payload, SafeHtml successMessage,
            AddCallback callback) {
        Operation.Builder builder = new Operation.Builder(address, ADD);
        if (payload != null && payload.isDefined()) {
            builder.payload(payload);
        }
        dispatcher.execute(builder.build(), result -> {
            MessageEvent.fire(eventBus, Message.success(successMessage));
            callback.execute(name, address);
        }, (__, error) -> MessageEvent.fire(eventBus,
                Message.error(resources.messages().addResourceError(name, error))));
    }

    // ------------------------------------------------------ (c)reate singleton with dialog

    /**
     * Opens an add-resource-dialog for the given singleton resource type. The dialog contains fields for all required request
     * properties. When clicking "Add", a new singleton resource is added using the specified address template. After the
     * singleton resource has been added a success message is fired and the specified callback is executed.
     *
     * @param id the id used for the add resource dialog
     * @param type the human readable resource type used in the dialog header and success message
     * @param template the address template which is resolved against the current statement context to get the singleton
     *        resource address for the add operation
     * @param callback the callback executed after the singleton resource has been added
     */
    public void addSingleton(String id, String type, AddressTemplate template, AddSingletonCallback callback) {
        addSingleton(id, type, template, Collections.emptyList(), callback);
    }

    /**
     * Opens an add-resource-dialog for the given singleton resource type. The dialog contains fields for all required request
     * properties. plus the ones specified by {@code attributes}. When clicking "Add", a new singleton resource is added using
     * the specified address template. After the singleton resource has been added a success message is fired and the specified
     * callback is executed.
     *
     * @param id the id used for the add resource dialog
     * @param type the human readable resource type used in the dialog header and success message
     * @param template the address template which is resolved against the current statement context to get the singleton
     *        resource address for the add operation
     * @param attributes additional attributes which should be part of the add resource dialog
     * @param callback the callback executed after the singleton resource has been added
     */
    public void addSingleton(String id, String type, AddressTemplate template, Iterable<String> attributes,
            AddSingletonCallback callback) {
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(Metadata metadata) {
                boolean hasRequiredAttributes = !metadata.getDescription().requestProperties().required().isEmpty();
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

    /**
     * Opens an add-resource-dialog for the given singleton resource type. The dialog contains fields for all required request
     * properties. plus the ones specified by {@code attributes}. When clicking "Add", a new singleton resource is added using
     * the specified address template. After the singleton resource has been added a success message is fired and the specified
     * callback is executed.
     *
     * @param id the id used for the add resource dialog
     * @param type the human readable resource type used in the dialog header and success message
     * @param metadata the metadata that contains the resource description
     * @param callback the callback executed after the singleton resource has been added
     */
    public void addSingleton(String id, String type, Metadata metadata, AddressTemplate template,
            AddSingletonCallback callback) {
        boolean hasRequiredAttributes = !metadata.getDescription().requestProperties().required().isEmpty();
        if (hasRequiredAttributes) {
            // no unbound name item!
            ModelNodeForm.Builder<ModelNode> builder = new ModelNodeForm.Builder<>(id, metadata)
                    .fromRequestProperties()
                    .requiredOnly();
            AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(type),
                    builder.build(), (name, model) -> addSingleton(type, template, model, callback));
            dialog.show();
        } else {
            addSingleton(type, template, null, callback);
        }
    }

    // ------------------------------------------------------ (c)reate singleton operation

    /**
     * Executes an add operation using the specified template. After the resource has been added a success message is fired and
     * the specified callback is executed.
     *
     * @param type the human readable resource type used in the dialog header and success message
     * @param template the address template which is resolved against the current statement context to get the singleton
     *        resource address for the add operation
     * @param callback the callback executed after the singleton resource has been added
     */
    public void addSingleton(String type, AddressTemplate template, AddSingletonCallback callback) {
        addSingleton(type, template.resolve(statementContext), null, callback);
    }

    /**
     * Executes an add operation using the specified template and payload. After the resource has been added a success message
     * is fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the dialog header and success message
     * @param template the address template which is resolved against the current statement context to get the singleton
     *        resource address for the add operation
     * @param payload the optional payload of the add operation (may be null or undefined)
     * @param callback the callback executed after the singleton resource has been added
     */
    public void addSingleton(String type, AddressTemplate template, @Nullable ModelNode payload,
            AddSingletonCallback callback) {
        addSingleton(type, template.resolve(statementContext), payload, callback);
    }

    /**
     * Executes an add operation using the specified address and payload. After the resource has been added a success message is
     * fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the dialog header and success message
     * @param address the fq address for the add operation
     * @param payload the optional payload of the add operation (may be null or undefined)
     * @param callback the callback executed after the singleton resource has been added
     */
    public void addSingleton(String type, ResourceAddress address, @Nullable ModelNode payload,
            AddSingletonCallback callback) {
        Operation.Builder builder = new Operation.Builder(address, ADD);
        if (payload != null && payload.isDefined()) {
            builder.payload(payload);
        }
        addSingleton(type, builder.build(), callback);
    }

    /**
     * Executes the specified add operation. After the resource has been added a success message is fired and the specified
     * callback is executed.
     *
     * @param operation the add operation with the address and payload
     * @param type the human readable resource type used in the dialog header and success message
     * @param callback the callback executed after the singleton resource has been added
     */
    public void addSingleton(String type, Operation operation, AddSingletonCallback callback) {
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(eventBus, Message.success(resources.messages().addSingleResourceSuccess(type)));
            callback.execute(operation.getAddress());
        }, (__, error) -> MessageEvent.fire(eventBus,
                Message.error(resources.messages().addSingleResourceError(type, error))));
    }

    // ------------------------------------------------------ (r)ead using template

    /**
     * Executes an {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_RESOURCE_OPERATION} on the specified template and
     * passes the result to the specified callback.
     *
     * @param template the address template which is resolved against the current statement context to get the resource address
     *        for the {@code read-resource} operation
     * @param callback the callback which gets the result of the {@code read-resource} operation
     */
    public void read(AddressTemplate template, ReadCallback callback) {
        read(template.resolve(statementContext), callback);
    }

    public Promise<ModelNode> read(AddressTemplate template) {
        return read(template.resolve(statementContext));
    }

    /**
     * Executes an {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_RESOURCE_OPERATION} with the specified depth on the
     * specified template and passes the result to the specified callback.
     *
     * @param template the address template which is resolved against the current statement context to get the resource address
     *        for the {@code read-resource} operation
     * @param depth the depth used for the {@code recursive-depth} parameter
     * @param callback the callback which gets the result of the {@code read-resource} operation
     */
    public void read(AddressTemplate template, int depth, ReadCallback callback) {
        read(template.resolve(statementContext), depth, callback);
    }

    public Promise<ModelNode> read(AddressTemplate template, int depth) {
        return read(template.resolve(statementContext), depth);
    }

    /**
     * Executes a recursive {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_RESOURCE_OPERATION} on the specified
     * template and passes the result to the specified callback.
     *
     * @param template the address template which is resolved against the current statement context to get the resource address
     *        for the {@code read-resource} operation
     * @param callback the callback which gets the result of the {@code read-resource} operation
     */
    public void readRecursive(AddressTemplate template, ReadCallback callback) {
        readRecursive(template.resolve(statementContext), callback);
    }

    public Promise<ModelNode> readRecursive(AddressTemplate template) {
        return readRecursive(template.resolve(statementContext));
    }

    /**
     * Executes an {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_CHILDREN_RESOURCES_OPERATION} on the specified
     * template and passes the result as {@code List<Property>} to the specified callback.
     *
     * @param template the address template which is resolved against the current statement context to get the resource address
     *        for the {@code read-children-resource} operation
     * @param childType the child resource (not human readable, but the actual child resource name!)
     * @param callback the callback which gets the result of the {@code read-children-resource} operation as {@code
     *                  List<Property>}
     */
    public void readChildren(AddressTemplate template, String childType, ReadChildrenCallback callback) {
        readChildren(template.resolve(statementContext), childType, callback);
    }

    public Promise<List<Property>> readChildren(AddressTemplate template, String childType) {
        return readChildren(template.resolve(statementContext), childType);
    }

    /**
     * Executes an {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_CHILDREN_RESOURCES_OPERATION} on the specified
     * template and passes the result as {@code List<Property>} to the specified callback.
     *
     * @param template the address template which is resolved against the current statement context to get the resource address
     *        for the {@code read-children-resource} operation
     * @param childType the child resource (not human readable, but the actual child resource name!)
     * @param depth the depth used for the {@code recursive-depth} parameter
     * @param callback the callback which gets the result of the {@code read-children-resource} operation as {@code
     *                  List<Property>}
     */
    public void readChildren(AddressTemplate template, String childType, int depth, ReadChildrenCallback callback) {
        readChildren(new Operation.Builder(template.resolve(statementContext), READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, childType)
                .param(RECURSIVE_DEPTH, depth)
                .build(),
                callback);
    }

    public Promise<List<Property>> readChildren(AddressTemplate template, String childType, int depth) {
        return readChildren(new Operation.Builder(template.resolve(statementContext), READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, childType)
                .param(RECURSIVE_DEPTH, depth)
                .build());
    }

    // ------------------------------------------------------ (r)ead using address

    /**
     * Executes an {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_RESOURCE_OPERATION} on the specified address and
     * passes the result to the specified callback.
     *
     * @param address the fq address for the {@code read-resource} operation
     * @param callback the callback which gets the result of the {@code read-resource} operation
     */
    public void read(ResourceAddress address, ReadCallback callback) {
        read(new Operation.Builder(address, READ_RESOURCE_OPERATION).param(INCLUDE_ALIASES, true).build(), callback);
    }

    public Promise<ModelNode> read(ResourceAddress address) {
        return read(new Operation.Builder(address, READ_RESOURCE_OPERATION).param(INCLUDE_ALIASES, true).build());
    }

    /**
     * Executes an {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_RESOURCE_OPERATION} with the specified depth on the
     * specified address and passes the result to the specified callback.
     *
     * @param address the fq address for the {@code read-resource} operation
     * @param depth the depth used for the {@code recursive-depth} parameter
     * @param callback the callback which gets the result of the {@code read-resource} operation
     */
    public void read(ResourceAddress address, int depth, ReadCallback callback) {
        read(new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_ALIASES, true)
                .param(RECURSIVE_DEPTH, depth)
                .build(),
                callback);
    }

    public Promise<ModelNode> read(ResourceAddress address, int depth) {
        return read(new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_ALIASES, true)
                .param(RECURSIVE_DEPTH, depth)
                .build());
    }

    /**
     * Executes a recursive {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_RESOURCE_OPERATION} on the specified address
     * and passes the result to the specified callback.
     *
     * @param address the fq address for the {@code read-resource} operation
     * @param callback the callback which gets the result of the {@code read-resource} operation
     */
    public void readRecursive(ResourceAddress address, ReadCallback callback) {
        read(new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_ALIASES, true)
                .param(RECURSIVE, true)
                .build(),
                callback);
    }

    public Promise<ModelNode> readRecursive(ResourceAddress address) {
        return read(new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_ALIASES, true)
                .param(RECURSIVE, true)
                .build());
    }

    /**
     * Executes an {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_CHILDREN_RESOURCES_OPERATION} on the specified
     * address and passes the result as {@code List<Property>} to the specified callback.
     *
     * @param address the fq address for the {@code read-children-resource} operation
     * @param resource the child resource (not human readable, but the actual child resource name!)
     * @param callback the callback which gets the result of the {@code read-children-resource} operation as {@code
     *                 List<Property>}
     */
    public void readChildren(ResourceAddress address, String resource, ReadChildrenCallback callback) {
        readChildren(new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, resource)
                .build(),
                callback);
    }

    public Promise<List<Property>> readChildren(ResourceAddress address, String resource) {
        return readChildren(new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, resource)
                .build());
    }

    /**
     * Executes an {@link org.jboss.hal.dmr.ModelDescriptionConstants#READ_CHILDREN_RESOURCES_OPERATION} on the specified
     * address and passes the result as {@code List<Property>} to the specified callback.
     *
     * @param address the fq address for the {@code read-children-resource} operation
     * @param resource the child resource (not human readable, but the actual child resource name!)
     * @param depth the depth used for the {@code recursive-depth} parameter
     * @param callback the callback which gets the result of the {@code read-children-resource} operation as {@code
     *                 List<Property>}
     */
    public void readChildren(ResourceAddress address, String resource, int depth, ReadChildrenCallback callback) {
        readChildren(new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, resource)
                .param(RECURSIVE_DEPTH, depth)
                .build(),
                callback);
    }

    public Promise<List<Property>> readChildren(ResourceAddress address, String resource, int depth) {
        return readChildren(new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, resource)
                .param(RECURSIVE_DEPTH, depth)
                .build());
    }

    private void read(Operation operation, ReadCallback callback) {
        dispatcher.execute(operation, callback::execute);
    }

    private Promise<ModelNode> read(Operation operation) {
        return dispatcher.execute(operation);
    }

    private void readChildren(Operation operation, ReadChildrenCallback callback) {
        dispatcher.execute(operation, result -> callback.execute(result.asPropertyList()));
    }

    private Promise<List<Property>> readChildren(Operation operation) {
        return dispatcher.execute(operation).then(result -> Promise.resolve(result.asPropertyList()));
    }

    // ------------------------------------------------------ read different child resources

    /**
     * Read multiple different child resources using a composite operation. The steps in the composite result map to the
     * position of the resource in the {@code resources} collection.
     *
     * @param template the address template which is resolved against the current statement context to get the resource address
     *        for the {@code read-children-resource} operation
     * @param resources the child resources (not human readable, but the actual child resource name!)
     * @param callback the callback which gets the composite result
     */
    public void readChildren(AddressTemplate template, Iterable<String> resources, ReadCompositeCallback callback) {
        readChildren(template.resolve(statementContext), resources, callback);
    }

    public Promise<CompositeResult> readChildren(AddressTemplate template, Iterable<String> resources) {
        return readChildren(template.resolve(statementContext), resources);
    }

    /**
     * Read multiple different child resources using a composite operation. The steps in the composite result map to the
     * position of the resource in the {@code resources} collection.
     *
     * @param address the fq address for the {@code read-children-resource} operation
     * @param resources the child resources (not human readable, but the actual child resource name!)
     * @param callback the callback which gets the composite result
     */
    public void readChildren(ResourceAddress address, Iterable<String> resources, ReadCompositeCallback callback) {
        List<Operation> operations = stream(resources.spliterator(), false)
                .map(resource -> new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                        .param(CHILD_TYPE, resource)
                        .build())
                .collect(toList());
        dispatcher.execute(new Composite(operations), callback::execute);
    }

    public Promise<CompositeResult> readChildren(ResourceAddress address, Iterable<String> resources) {
        List<Operation> operations = stream(resources.spliterator(), false)
                .map(resource -> new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                        .param(CHILD_TYPE, resource)
                        .build())
                .collect(toList());
        return dispatcher.execute(new Composite(operations));
    }

    /**
     * Read multiple different child resources using a composite operation. The steps in the composite result map to the
     * position of the resource in the {@code resources} collection.
     *
     * @param address the fq address for the {@code read-children-resource} operation
     * @param resources the child resources (not human readable, but the actual child resource name!)
     * @param depth the depth used for the {@code recursive-depth} parameter
     * @param callback the callback which gets the composite result
     */
    public void readChildren(ResourceAddress address, Iterable<String> resources, int depth,
            ReadCompositeCallback callback) {
        List<Operation> operations = stream(resources.spliterator(), false)
                .map(resource -> new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                        .param(CHILD_TYPE, resource)
                        .param(RECURSIVE_DEPTH, depth)
                        .build())
                .collect(toList());
        dispatcher.execute(new Composite(operations), callback::execute);
    }

    public Promise<CompositeResult> readChildren(ResourceAddress address, Iterable<String> resources, int depth) {
        List<Operation> operations = stream(resources.spliterator(), false)
                .map(resource -> new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                        .param(CHILD_TYPE, resource)
                        .param(RECURSIVE_DEPTH, depth)
                        .build())
                .collect(toList());
        return dispatcher.execute(new Composite(operations));
    }

    // ------------------------------------------------------ (u)pdate using template

    /**
     * Writes the changed values to the specified resource. After the resource has been saved a standard success message is
     * fired and the specified callback is executed.
     * <p>
     * If the change set is empty, a warning message is fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param name the resource name
     * @param template the address template which is resolved against the current statement context and the resource name to get
     *        the resource address for the operation
     * @param changedValues the changed values / payload for the operation
     * @param successCallback the callback executed after the resource has been saved
     */
    public void save(String type, String name, AddressTemplate template, Map<String, Object> changedValues,
            Callback successCallback) {
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(Metadata metadata) {
                ResourceAddress address = template.resolve(statementContext, name);
                Composite operations = operationFactory.fromChangeSet(address, changedValues, metadata);
                save(operations, resources.messages().modifyResourceSuccess(type, name), successCallback);
            }
        });
    }

    /**
     * Writes the changed values to the specified resource. After the resource has been saved the specified success message is
     * fired and the specified callback is executed.
     * <p>
     * If the change set is empty, a warning message is fired and the specified callback is executed.
     *
     * @param name the resource name
     * @param template the address template which is resolved against the current statement context and the resource name to get
     *        the resource address for the operation
     * @param changedValues the changed values / payload for the operation
     * @param successMessage the success message fired after saving the resource
     * @param successCallback the callback executed after the resource has been saved
     */
    public void save(String name, AddressTemplate template, Map<String, Object> changedValues, SafeHtml successMessage,
            Callback successCallback) {
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(Metadata metadata) {
                ResourceAddress address = template.resolve(statementContext, name);
                Composite operations = operationFactory.fromChangeSet(address, changedValues, metadata);
                save(operations, successMessage, successCallback);
            }
        });
    }

    // ------------------------------------------------------ (u)pdate using address

    /**
     * Writes the changed values to the specified resource. After the resource has been saved a standard success message is
     * fired and the specified callback is executed.
     * <p>
     * If the change set is empty, a warning message is fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param name the resource name
     * @param address the fq address for the operation
     * @param changedValues the changed values / payload for the operation
     * @param metadata the metadata of the attributes in the change set
     * @param successCallback the callback executed after the resource has been saved
     */
    public void save(String type, String name, ResourceAddress address, Map<String, Object> changedValues,
            Metadata metadata, Callback successCallback) {
        Composite operations = operationFactory.fromChangeSet(address, changedValues, metadata);
        save(operations, resources.messages().modifyResourceSuccess(type, name), successCallback);
    }

    /**
     * Writes the changed values to the specified resource. After the resource has been saved a standard success message is
     * fired and the specified callback is executed.
     * <p>
     * If the change set is empty, a warning message is fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param name the resource name
     * @param address the fq address for the operation
     * @param changedValues the changed values / payload for the operation
     * @param metadata the metadata of the attributes in the change set
     * @param successCallback the callback executed after the resource has been saved
     * @param errorCallback the callback executed if the save operation fails
     */
    public void save(String type, String name, ResourceAddress address, Map<String, Object> changedValues,
            Metadata metadata, Callback successCallback, Callback errorCallback) {
        Composite operations = operationFactory.fromChangeSet(address, changedValues, metadata);
        save(operations, resources.messages().modifyResourceSuccess(type, name), successCallback, errorCallback);
    }

    /**
     * Writes the changed values to the specified resource. After the resource has been saved the specified success message is
     * fired and the specified callback is executed.
     * <p>
     * If the change set is empty, a warning message is fired and the specified callback is executed.
     *
     * @param address the fq address for the operation
     * @param changedValues the changed values / payload for the operation
     * @param metadata the metadata of the attributes in the change set
     * @param successMessage the success message fired after saving the resource
     * @param successCallback the callback executed after the resource has been saved
     */
    public void save(ResourceAddress address, Map<String, Object> changedValues, Metadata metadata,
            SafeHtml successMessage, Callback successCallback) {
        save(operationFactory.fromChangeSet(address, changedValues, metadata), successMessage, successCallback);
    }

    // ------------------------------------------------------ (u)pdate using operation

    /**
     * Writes the changed values to the specified resource. After the resource has been saved a standard success message is
     * fired and the specified callback is executed.
     * <p>
     * If the composite operation is empty (i.e. there were no changes), a warning message is fired and the specified callback
     * is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param name the resource name
     * @param operations the composite operation to persist the changed values
     * @param successCallback the callback executed after the resource has been saved
     */
    public void save(String type, String name, Composite operations, Callback successCallback) {
        save(operations, resources.messages().modifyResourceSuccess(type, name), successCallback);
    }

    /**
     * Writes the changed values to the specified resource. After the resource has been saved the specified success message is
     * fired and the specified successCallback is executed.
     * <p>
     * If the composite operation is empty (i.e. there were no changes), a warning message is fired and the specified
     * successCallback is executed.
     *
     * @param operations the composite operation to persist the changed values
     * @param successMessage the success message fired after saving the resource
     * @param successCallback the callback executed after the resource has been saved
     */
    public void save(Composite operations, SafeHtml successMessage, Callback successCallback) {
        save(operations, successMessage, successCallback, null);
    }

    /**
     * Writes the changed values to the specified resource. After the resource has been saved the specified success message is
     * fired and the specified successCallback is executed.
     * <p>
     * If the composite operation is empty (i.e. there were no changes), a warning message is fired and the specified
     * successCallback is executed.
     *
     * @param operations the composite operation to persist the changed values
     * @param successMessage the success message fired after saving the resource
     * @param successCallback the callback executed after the resource has been saved
     * @param errorCallback the callback executed if the save operation fails
     */
    public void save(Composite operations, SafeHtml successMessage, Callback successCallback, Callback errorCallback) {
        if (operations.isEmpty()) {
            MessageEvent.fire(eventBus, Message.warning(resources.messages().noChanges()));
            successCallback.execute();
        } else {
            dispatcher.execute(operations, (CompositeResult result) -> {
                MessageEvent.fire(eventBus, Message.success(successMessage));
                successCallback.execute();
            },
                    (operation, error) -> {
                        // call the default dispatcher error callback
                        dispatcher.getDefaultErrorCallback().onError(operation, error);
                        // call the error callback provided by the caller
                        errorCallback.execute();
                    });
        }
    }

    // ------------------------------------------------------ (u)pdate singleton using template

    /**
     * Writes the changed values to the specified singleton resource. After the resource has been saved a success message is
     * fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param template the address template which is resolved against the current statement context to get the resource address
     *        for the operation
     * @param changedValues the changed values / payload for the operation
     * @param successCallback the callback executed after the resource has been saved
     */
    public void saveSingleton(String type, AddressTemplate template, Map<String, Object> changedValues,
            Callback successCallback) {
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(Metadata metadata) {
                saveSingleton(template.resolve(statementContext), changedValues, metadata,
                        resources.messages().modifySingleResourceSuccess(type), successCallback);
            }
        });
    }

    /**
     * Writes the changed values to the specified singleton resource. After the resource has been saved a success message is
     * fired and the specified callback is executed.
     *
     * @param template the address template which is resolved against the current statement context to get the resource address
     *        for the operation
     * @param changedValues the changed values / payload for the operation
     * @param successMessage the success message fired after saving the resource
     * @param successCallback the callback executed after the resource has been saved
     */
    public void saveSingleton(AddressTemplate template, Map<String, Object> changedValues, SafeHtml successMessage,
            Callback successCallback) {
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(Metadata metadata) {
                saveSingleton(template.resolve(statementContext), changedValues, metadata, successMessage, successCallback);
            }
        });
    }

    // ------------------------------------------------------ (u)pdate singleton using address

    /**
     * Writes the changed values to the specified singleton resource. After the resource has been saved a success message is
     * fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param address the fq address for the operation
     * @param changedValues the changed values / payload for the operation
     * @param metadata the metadata of the attributes in the change set
     * @param successCallback the callback executed after the resource has been saved
     */
    public void saveSingleton(String type, ResourceAddress address, Map<String, Object> changedValues,
            Metadata metadata, Callback successCallback) {
        saveSingleton(address, changedValues, metadata, resources.messages().modifySingleResourceSuccess(type),
                successCallback);
    }

    /**
     * Writes the changed values to the specified singleton resource. After the resource has been saved a success message is
     * fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param address the fq address for the operation
     * @param changedValues the changed values / payload for the operation
     * @param metadata the metadata of the attributes in the change set
     * @param successCallback the callback executed after the resource has been saved
     */
    public void saveSingleton(String type, ResourceAddress address, Map<String, Object> changedValues,
            Metadata metadata, Callback successCallback, Callback errorCallback) {
        saveSingleton(address, changedValues, metadata, resources.messages().modifySingleResourceSuccess(type),
                successCallback, errorCallback);
    }

    /**
     * Writes the changed values to the specified singleton resource. After the resource has been saved a success message is
     * fired and the specified callback is executed.
     *
     * @param address the fq address for the operation
     * @param changedValues the changed values / payload for the operation
     * @param metadata the metadata of the attributes in the change set
     * @param successMessage the success message fired after saving the resource
     * @param successCallback the callback executed after the resource has been saved
     */
    public void saveSingleton(ResourceAddress address, Map<String, Object> changedValues, Metadata metadata,
            SafeHtml successMessage, Callback successCallback, Callback errorCallback) {
        save(operationFactory.fromChangeSet(address, changedValues, metadata), successMessage, successCallback, errorCallback);
    }

    /**
     * Writes the changed values to the specified singleton resource. After the resource has been saved a success message is
     * fired and the specified callback is executed.
     *
     * @param address the fq address for the operation
     * @param changedValues the changed values / payload for the operation
     * @param metadata the metadata of the attributes in the change set
     * @param successMessage the success message fired after saving the resource
     * @param successCallback the callback executed after the resource has been saved
     */
    public void saveSingleton(ResourceAddress address, Map<String, Object> changedValues, Metadata metadata,
            SafeHtml successMessage, Callback successCallback) {
        save(operationFactory.fromChangeSet(address, changedValues, metadata), successMessage, successCallback);
    }

    // ------------------------------------------------------ (u) reset using template

    /**
     * Undefines all non required attributes in the specified form. After the resource has been undefined a standard success
     * message is fired and the specified callback is executed.
     * <p>
     * If the form contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param name the resource name
     * @param template the address template which is resolved against the current statement context and the resource name to get
     *        the resource address for the operation
     * @param form the form which should be reset
     * @param metadata the metadata of the attributes
     * @param callback the callback executed after the resource has been undefined
     */
    public <T> void reset(String type, String name, AddressTemplate template, Form<T> form, Metadata metadata,
            Callback callback) {
        Set<String> attributes = stream(form.getBoundFormItems().spliterator(), false)
                .map(FormItem::getName)
                .collect(toSet());
        reset(type, name, template.resolve(statementContext, name), attributes, metadata,
                resources.messages().resetResourceSuccess(type, name), callback);
    }

    /**
     * Undefines all non required attributes in the specified form. After the resource has been undefined the specified success
     * message is fired and the specified callback is executed.
     * <p>
     * If the form contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param name the resource name
     * @param template the address template which is resolved against the current statement context and the resource name to get
     *        the resource address for the operation
     * @param form the form which should be reset
     * @param metadata the metadata of the attributes
     * @param successMessage the success message fired after resetting the resource
     * @param callback the callback executed after the resource has been undefined
     */
    public <T> void reset(String type, String name, AddressTemplate template, Form<T> form, Metadata metadata,
            SafeHtml successMessage, Callback callback) {
        Set<String> attributes = stream(form.getBoundFormItems().spliterator(), false)
                .map(FormItem::getName)
                .collect(toSet());
        reset(type, name, template.resolve(statementContext), attributes, metadata, successMessage, callback);
    }

    /**
     * Undefines all non required attributes in the specified set. After the resource has been undefined a standard success
     * message is fired and the specified callback is executed.
     * <p>
     * If the set contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param name the resource name
     * @param template the address template which is resolved against the current statement context and the resource name to get
     *        the resource address for the operation
     * @param attributes the attributes which should be reset
     * @param metadata the metadata of the attributes
     * @param callback the callback executed after the resource has been undefined
     */
    public void reset(String type, String name, AddressTemplate template, Set<String> attributes, Metadata metadata,
            Callback callback) {
        reset(type, name, template.resolve(statementContext), attributes, metadata,
                resources.messages().resetResourceSuccess(type, name), callback);
    }

    /**
     * Undefines all non required attributes in the specified set. After the resource has been undefined the specified success
     * message is fired and the specified callback is executed.
     * <p>
     * If the set contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param name the resource name
     * @param template the address template which is resolved against the current statement context and the resource name to get
     *        the resource address for the operation
     * @param attributes the attributes which should be reset
     * @param metadata the metadata of the attributes
     * @param successMessage the success message fired after resetting the resource
     * @param callback the callback executed after the resource has been undefined
     */
    public void reset(String type, String name, AddressTemplate template, Set<String> attributes, Metadata metadata,
            SafeHtml successMessage, Callback callback) {
        reset(type, name, template.resolve(statementContext), attributes, metadata, successMessage, callback);
    }

    // ------------------------------------------------------ (u) reset using address

    /**
     * Undefines all non required attributes in the specified form. After the resource has been undefined a standard success
     * message is fired and the specified callback is executed.
     * <p>
     * If the form contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param name the resource name
     * @param address the fq address for the operation
     * @param form the form which should be reset
     * @param metadata the metadata of the attributes
     * @param callback the callback executed after the resource has been undefined
     */
    public <T> void reset(String type, String name, ResourceAddress address, Form<T> form, Metadata metadata,
            Callback callback) {
        Set<String> attributes = stream(form.getBoundFormItems().spliterator(), false)
                .map(FormItem::getName)
                .collect(toSet());
        reset(type, name, address, attributes, metadata, resources.messages().resetResourceSuccess(type, name),
                callback);
    }

    /**
     * Undefines all non required attributes in the specified form. After the resource has been undefined the specified success
     * message is fired and the specified callback is executed.
     * <p>
     * If the form contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param name the resource name
     * @param address the fq address for the operation
     * @param form the from which should be reset
     * @param metadata the metadata of the attributes
     * @param successMessage the success message fired after resetting the resource
     * @param callback the callback executed after the resource has been undefined
     */
    public <T> void reset(String type, String name, ResourceAddress address, Form<T> form, Metadata metadata,
            SafeHtml successMessage, Callback callback) {
        Set<String> attributes = stream(form.getBoundFormItems().spliterator(), false)
                .map(FormItem::getName)
                .collect(toSet());
        reset(type, name, address, attributes, metadata, successMessage, callback);
    }

    /**
     * Undefines all non required attributes in the specified set. After the resource has been undefined a standard success
     * message is fired and the specified callback is executed.
     * <p>
     * If the set contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param name the resource name
     * @param address the fq address for the operation
     * @param attributes the attributes which should be reset
     * @param metadata the metadata of the attributes
     * @param callback the callback executed after the resource has been undefined
     */
    public void reset(String type, String name, ResourceAddress address, Set<String> attributes, Metadata metadata,
            Callback callback) {
        reset(type, name, address, attributes, metadata, resources.messages().resetResourceSuccess(type, name),
                callback);
    }

    /**
     * Undefines all non required attributes in the specified set. After the resource has been undefined the specified success
     * message is fired and the specified callback is executed.
     * <p>
     * If the set contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param name the resource name
     * @param address the fq address for the operation
     * @param attributes the attributes which should be reset
     * @param metadata the metadata of the attributes
     * @param successMessage the success message fired after resetting the resource
     * @param callback the callback executed after the resource has been undefined
     */
    public void reset(String type, String name, ResourceAddress address, Set<String> attributes, Metadata metadata,
            SafeHtml successMessage, Callback callback) {
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
     * Undefines all non required attributes in the specified form. After the singleton resource has been undefined a standard
     * success message is fired and the specified callback is executed.
     * <p>
     * If the set contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param template the address template which is resolved against the current statement context and the resource name to get
     *        the resource address for the operation
     * @param form the form which should be reset
     * @param metadata the metadata of the attributes
     * @param callback the callback executed after the resource has been undefined
     */
    public <T> void resetSingleton(String type, AddressTemplate template, Form<T> form, Metadata metadata,
            Callback callback) {
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
     * @param type the human readable resource type used in the success message
     * @param template the address template which is resolved against the current statement context and the resource name to get
     *        the resource address for the operation
     * @param form the form which should be reset
     * @param metadata the metadata of the attributes
     * @param successMessage the success message fired after resetting the resource
     * @param callback the callback executed after the resource has been undefined
     */
    public <T> void resetSingleton(String type, AddressTemplate template, Form<T> form, Metadata metadata,
            SafeHtml successMessage, Callback callback) {
        Set<String> attributes = stream(form.getBoundFormItems().spliterator(), false)
                .map(FormItem::getName)
                .collect(toSet());
        reset(type, null, template.resolve(statementContext), attributes, metadata, successMessage, callback);
    }

    /**
     * Undefines all non required attributes in the specified set. After the singleton resource has been undefined a standard
     * success message is fired and the specified callback is executed.
     * <p>
     * If the set contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param template the address template which is resolved against the current statement context and the resource name to get
     *        the resource address for the operation
     * @param attributes the attributes which should be reset
     * @param metadata the metadata of the attributes
     * @param callback the callback executed after the resource has been undefined
     */
    public void resetSingleton(String type, AddressTemplate template, Set<String> attributes, Metadata metadata,
            Callback callback) {
        reset(type, null, template.resolve(statementContext), attributes, metadata,
                resources.messages().resetSingletonSuccess(type), callback);
    }

    /**
     * Undefines all non required attributes in the specified set. After the singleton resource has been undefined the specified
     * success message is fired and the specified callback is executed.
     * <p>
     * If the set contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param template the address template which is resolved against the current statement context and the resource name to get
     *        the resource address for the operation
     * @param attributes the attributes which should be reset
     * @param metadata the metadata of the attributes
     * @param successMessage the success message fired after resetting the resource
     * @param callback the callback executed after the resource has been undefined
     */
    public void resetSingleton(String type, AddressTemplate template, Set<String> attributes, Metadata metadata,
            SafeHtml successMessage, Callback callback) {
        reset(type, null, template.resolve(statementContext), attributes, metadata, successMessage, callback);
    }

    // ------------------------------------------------------ (u) reset singleton using address

    /**
     * Undefines all non required attributes in the specified form. After the singleton resource has been undefined a standard
     * success message is fired and the specified callback is executed.
     * <p>
     * If the set contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param address the fq address for the operation
     * @param form the form which should be reset
     * @param metadata the metadata of the attributes
     * @param callback the callback executed after the resource has been undefined
     */
    public <T> void resetSingleton(String type, ResourceAddress address, Form<T> form, Metadata metadata,
            Callback callback) {
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
     * @param type the human readable resource type used in the success message
     * @param address the fq address for the operation
     * @param form the form which should be reset
     * @param metadata the metadata of the attributes
     * @param successMessage the success message fired after resetting the resource
     * @param callback the callback executed after the resource has been undefined
     */
    public <T> void resetSingleton(String type, ResourceAddress address, Form<T> form, Metadata metadata,
            SafeHtml successMessage, Callback callback) {
        Set<String> attributes = stream(form.getBoundFormItems().spliterator(), false)
                .map(FormItem::getName)
                .collect(toSet());
        reset(type, null, address, attributes, metadata, successMessage, callback);
    }

    /**
     * Undefines all non required attributes in the specified set. After the singleton resource has been undefined a standard
     * success message is fired and the specified callback is executed.
     * <p>
     * If the set contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param address the fq address for the operation
     * @param attributes the attributes which should be reset
     * @param metadata the metadata of the attributes
     * @param callback the callback executed after the resource has been undefined
     */
    public void resetSingleton(String type, ResourceAddress address, Set<String> attributes, Metadata metadata,
            Callback callback) {
        reset(type, null, address, attributes, metadata, resources.messages().resetSingletonSuccess(type),
                callback);
    }

    /**
     * Undefines all non required attributes in the specified set. After the singleton resource has been undefined the specified
     * success message is fired and the specified callback is executed.
     * <p>
     * If the set contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param address the fq address for the operation
     * @param attributes the attributes which should be reset
     * @param metadata the metadata of the attributes
     * @param successMessage the success message fired after resetting the resource
     * @param callback the callback executed after the resource has been undefined
     */
    public void resetSingleton(String type, ResourceAddress address, Set<String> attributes, Metadata metadata,
            SafeHtml successMessage, Callback callback) {
        reset(type, null, address, attributes, metadata, successMessage, callback);
    }

    // ------------------------------------------------------ (d)elete using template

    /**
     * Shows a confirmation dialog and removes the resource if confirmed by the user. After the resource has been removed a
     * success message is fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param name the resource name
     * @param template the address template which is resolved against the current statement context and the resource name to get
     *        the resource address for the {@code remove} operation
     * @param callback the callback executed after the resource has been removed
     */
    public void remove(String type, String name, AddressTemplate template, Callback callback) {
        remove(type, name, template.resolve(statementContext, name), callback);
    }

    /**
     * Shows a confirmation dialog and removes the singleton resource if confirmed by the user. After the resource has been
     * removed a success message is fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param template the address template which is resolved against the current statement context to get the resource address
     *        for the {@code remove} operation
     * @param callback the callback executed after the resource has been removed
     */
    public void removeSingleton(String type, AddressTemplate template, Callback callback) {
        remove(type, null, template.resolve(statementContext), callback);
    }

    // ------------------------------------------------------ (d)elete using address

    /**
     * Shows a confirmation dialog and removes the resource if confirmed by the user. After the resource has been removed a
     * success message is fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param name the resource name
     * @param address the fq address for the {@code remove} operation
     * @param callback the callback executed after the resource has been removed
     */
    public void remove(String type, String name, ResourceAddress address, Callback callback) {
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
     * Shows a confirmation dialog and removes the singleton resource if confirmed by the user. After the resource has been
     * removed a success message is fired and the specified callback is executed.
     *
     * @param type the human readable resource type used in the success message
     * @param address the fq address for the {@code remove} operation
     * @param callback the callback executed after the resource has been removed
     */
    public void removeSingleton(String type, ResourceAddress address, Callback callback) {
        remove(type, null, address, callback);
    }

    // ------------------------------------------------------ inner classes

    /**
     * Callback used in {@code add} methods
     */
    @FunctionalInterface
    public interface AddCallback {

        /**
         * Called after the resource has been added.
         *
         * @param name the name of the resource
         * @param address the resource address of the newly added resource
         */
        void execute(@Nullable String name, ResourceAddress address);
    }

    /**
     * Callback used in {@code addSingleton} methods
     */
    @FunctionalInterface
    public interface AddSingletonCallback {

        /**
         * Called after the resource has been added.
         *
         * @param address the resource address of the newly added resource
         */
        void execute(ResourceAddress address);
    }

    @FunctionalInterface
    public interface ReadCallback {

        void execute(ModelNode result);
    }

    @FunctionalInterface
    public interface ReadChildrenCallback {

        void execute(List<Property> children);
    }

    @FunctionalInterface
    public interface ReadCompositeCallback {

        void execute(CompositeResult result);
    }
}
