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

import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import jsinterop.annotations.JsIgnore;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
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

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Class to create, read, update and delete complex attributes. This class mirrors and delegates to methods from {@link
 * CrudOperations}.
 */
public class ComplexAttributeOperations {

    private final CrudOperations crud;
    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final MetadataProcessor metadataProcessor;
    private final Provider<Progress> progress;
    private final StatementContext statementContext;
    private final Resources resources;
    private final LabelBuilder labelBuilder;

    @Inject
    public ComplexAttributeOperations(final CrudOperations crud,
            final EventBus eventBus,
            final Dispatcher dispatcher,
            final MetadataProcessor metadataProcessor,
            @Footer final Provider<Progress> progress,
            final StatementContext statementContext,
            final Resources resources) {
        this.crud = crud;
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.metadataProcessor = metadataProcessor;
        this.progress = progress;
        this.statementContext = statementContext;
        this.resources = resources;
        this.labelBuilder = new LabelBuilder();
    }


    // ------------------------------------------------------ (c)reate with dialog

    /**
     * Opens an add-resource-dialog for the given complex attribute. The dialog contains fields for all required
     * attributes. When clicking "Add", a new complex attribute is created and written to the specified resource.
     * After the resource has been updated, a success message is fired and the specified callback is executed.
     *
     * @param id               the id used for the add resource dialog
     * @param resource         the resource name
     * @param complexAttribute the name of the complex attribute
     * @param template         the address template which is resolved against the current statement context and the
     *                         resource name to get the resource address for the operation
     * @param callback         the callback executed after the resource has been added
     */
    @JsIgnore
    public void add(final String id, final String resource, final String complexAttribute,
            final AddressTemplate template, final Callback callback) {
        lookupAndAdd(id, complexAttribute, template,
                (name, model) -> add(resource, complexAttribute, template, model, callback));
    }

    /**
     * Opens an add-resource-dialog for the given complex attribute. The dialog contains fields for all required
     * attributes. When clicking "Add", a new model node is created and added to the complex attribute in the specified
     * resource. After the resource has been updated, a success message is fired and the specified callback is executed.
     *
     * @param id               the id used for the add resource dialog
     * @param resource         the resource name
     * @param complexAttribute the name of the complex attribute
     * @param template         the address template which is resolved against the current statement context and the
     *                         resource name to get the resource address for the operation
     * @param callback         the callback executed after the resource has been added
     */
    @JsIgnore
    public void listAdd(final String id, final String resource, final String complexAttribute,
            final AddressTemplate template, final Callback callback) {
        lookupAndAdd(id, complexAttribute, template,
                (name, model) -> listAdd(resource, complexAttribute, template, model, callback));
    }

    private void lookupAndAdd(final String id, final String complexAttribute, final AddressTemplate template,
            final AddResourceDialog.Callback callback) {
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(final Metadata metadata) {
                String type = labelBuilder.label(complexAttribute);
                Metadata caMetadata = metadata.forComplexAttribute(complexAttribute);
                Form<ModelNode> form = new ModelNodeForm.Builder<>(id, caMetadata)
                        .addOnly()
                        .requiredOnly()
                        .build();
                AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(type), form,
                        callback);
                dialog.show();
            }
        });
    }


    // ------------------------------------------------------ (c)reate operation

    /**
     * Writes the payload to the complex attribute in the specified resource. After the resource has been updated,
     * a success message is fired and the specified callback is executed.
     *
     * @param resource         the resource name
     * @param complexAttribute the name of the complex attribute
     * @param template         the address template which is resolved against the current statement context and the
     *                         resource name to get the resource address for the operation
     * @param payload          the optional payload for the complex attribute (may be null or undefined)
     * @param callback         the callback executed after the resource has been added
     */
    @JsIgnore
    public void add(final String resource, final String complexAttribute, final AddressTemplate template,
            @Nullable final ModelNode payload, final Callback callback) {
        ResourceAddress address = template.resolve(statementContext, resource);
        Operation operation = new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, complexAttribute)
                .param(VALUE, payload)
                .build();
        add(complexAttribute, operation, callback);
    }

    /**
     * Adds the payload to the complex attribute in the specified resource. After the resource has been updated,
     * a success message is fired and the specified callback is executed.
     *
     * @param resource         the resource name
     * @param complexAttribute the name of the complex attribute
     * @param template         the address template which is resolved against the current statement context and the
     *                         resource name to get the resource address for the operation
     * @param payload          the optional payload for the complex attribute (may be null or undefined)
     * @param callback         the callback executed after the resource has been added
     */
    @JsIgnore
    public void listAdd(final String resource, final String complexAttribute, final AddressTemplate template,
            @Nullable final ModelNode payload, final Callback callback) {
        ResourceAddress address = template.resolve(statementContext, resource);
        Operation operation = new Operation.Builder(address, LIST_ADD_OPERATION)
                .param(NAME, complexAttribute)
                .param(VALUE, payload)
                .build();
        add(complexAttribute, operation, callback);
    }

    private void add(final String complexAttribute, final Operation operation, final Callback callback) {
        dispatcher.execute(operation, result -> {
            String type = labelBuilder.label(complexAttribute);
            MessageEvent.fire(eventBus, Message.success(resources.messages().addSingleResourceSuccess(type)));
            callback.execute();
        });
    }


    // ------------------------------------------------------ (u)pdate using template

    /**
     * Writes the changed values to the complex attribute. After the complex attribute has been saved a standard success
     * message is fired and the specified callback is executed.
     * <p>
     * If the change set is empty, a warning message is fired and the specified callback is executed.
     *
     * @param resource         the resource name
     * @param complexAttribute the name of the complex attribute
     * @param template         the address template which is resolved against the current statement context and the
     *                         resource name to get the resource address for the operation
     * @param changedValues    the changed values / payload for the operation
     * @param callback         the callback executed after the resource has been saved
     */
    @JsIgnore
    public void save(final String resource, final String complexAttribute, final AddressTemplate template,
            final Map<String, Object> changedValues, final Callback callback) {
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(final Metadata metadata) {
                ResourceAddress address = template.resolve(statementContext, resource);
                Metadata caMetadata = metadata.forComplexAttribute(complexAttribute);
                save(complexAttribute, address, changedValues, caMetadata, callback);
            }
        });
    }

    /**
     * Writes the changed values to the list-type complex attribute. After the complex attribute has been saved a
     * standard success message is fired and the specified callback is executed.
     * <p>
     * If the change set is empty, a warning message is fired and the specified callback is executed.
     *
     * @param resource         the resource name
     * @param complexAttribute the name of the complex attribute
     * @param index            the index for the list-type complex attribute
     * @param template         the address template which is resolved against the current statement context and the
     *                         resource name to get the resource address for the operation
     * @param changedValues    the changed values / payload for the operation
     * @param callback         the callback executed after the resource has been saved
     */
    @JsIgnore
    public void save(final String resource, final String complexAttribute, final int index,
            final AddressTemplate template, final Map<String, Object> changedValues, final Callback callback) {
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(final Metadata metadata) {
                ResourceAddress address = template.resolve(statementContext, resource);
                Metadata caMetadata = metadata.forComplexAttribute(complexAttribute);
                save(complexAttribute, index, address, changedValues, caMetadata, callback);
            }
        });
    }


    // ------------------------------------------------------ (u)pdate using address

    /**
     * Writes the changed values to the complex attribute. After the complex attribute has been saved a standard success
     * message is fired and the specified callback is executed.
     * <p>
     * If the change set is empty, a warning message is fired and the specified callback is executed.
     *
     * @param complexAttribute the name of the complex attribute
     * @param address          the fq address for the operation
     * @param changedValues    the changed values / payload for the operation
     * @param metadata         the metadata for the complex attribute
     * @param callback         the callback executed after the resource has been saved
     */
    @JsIgnore
    public void save(final String complexAttribute, final ResourceAddress address,
            final Map<String, Object> changedValues, final Metadata metadata, final Callback callback) {
        Composite operations = operationFactory(complexAttribute).fromChangeSet(address, changedValues, metadata);
        crud.save(operations, resources.messages().modifySingleResourceSuccess(labelBuilder.label(complexAttribute)),
                callback);
    }

    /**
     * Writes the changed values to the list-type complex attribute. After the complex attribute has been saved a
     * standard success message is fired and the specified callback is executed.
     * <p>
     * If the change set is empty, a warning message is fired and the specified callback is executed.
     *
     * @param complexAttribute the name of the complex attribute
     * @param index            the index for the list-type complex attribute
     * @param address          the fq address for the operation
     * @param changedValues    the changed values / payload for the operation
     * @param metadata         the metadata for the complex attribute
     * @param callback         the callback executed after the resource has been saved
     */
    @JsIgnore
    public void save(final String complexAttribute, final int index, final ResourceAddress address,
            final Map<String, Object> changedValues, final Metadata metadata, final Callback callback) {
        Composite operations = operationFactory(complexAttribute, index).fromChangeSet(address, changedValues,
                metadata);
        crud.save(operations, resources.messages().modifySingleResourceSuccess(labelBuilder.label(complexAttribute)),
                callback);
    }


    // ------------------------------------------------------ (d)elete using template

    /**
     * Undefines the complex attribute. After the attribute has been undefined a standard success message is fired and
     * the specified callback is executed.
     *
     * @param resource         the resource name
     * @param complexAttribute the name of the complex attribute
     * @param template         the address template which is resolved against the current statement context and the
     *                         resource name to get the resource address for the operation
     * @param callback         the callback executed after the complex attribute has been undefined
     */
    @JsIgnore
    public void remove(final String resource, final String complexAttribute, final AddressTemplate template,
            final Callback callback) {
        String type = labelBuilder.label(complexAttribute);
        ResourceAddress address = template.resolve(statementContext, resource);
        Operation operation = new Operation.Builder(address, UNDEFINE_ATTRIBUTE_OPERATION)
                .param(NAME, complexAttribute)
                .build();
        SafeHtml question = resources.messages().removeConfirmationQuestion(type);
        DialogFactory.showConfirmation(
                resources.messages().removeConfirmationTitle(type), question,
                () -> dispatcher.execute(operation, result -> {
                    MessageEvent.fire(eventBus,
                            Message.success(resources.messages().resetComplexAttributeSuccess(type)));
                    callback.execute();
                }));
    }

    /**
     * Undefines the complex attribute at the specified index. After the attribute has been undefined a standard success
     * message is fired and the specified callback is executed.
     *
     * @param resource         the resource name
     * @param complexAttribute the name of the complex attribute
     * @param index            the index for the list-type complex attribute
     * @param template         the address template which is resolved against the current statement context and the
     *                         resource name to get the resource address for the operation
     * @param callback         the callback executed after the complex attribute has been undefined
     */
    @JsIgnore
    public void remove(final String resource, final String complexAttribute, final int index,
            final AddressTemplate template, final Callback callback) {
        String type = labelBuilder.label(complexAttribute);
        ResourceAddress address = template.resolve(statementContext, resource);
        Operation operation = new Operation.Builder(address, LIST_REMOVE_OPERATION)
                .param(NAME, complexAttribute)
                .param(INDEX, index)
                .build();
        SafeHtml question = resources.messages().removeConfirmationQuestion(type);
        DialogFactory.showConfirmation(
                resources.messages().removeConfirmationTitle(type), question,
                () -> dispatcher.execute(operation, result -> {
                    MessageEvent.fire(eventBus,
                            Message.success(resources.messages().resetComplexAttributeSuccess(type)));
                    callback.execute();
                }));
    }


    // ------------------------------------------------------ helper methods

    private OperationFactory operationFactory(String complexAttribute) {
        return new OperationFactory(name -> complexAttribute + "." + name);
    }

    private OperationFactory operationFactory(String complexAttribute, int index) {
        return new OperationFactory(name -> complexAttribute + "[" + index + "]." + name);
    }
}
