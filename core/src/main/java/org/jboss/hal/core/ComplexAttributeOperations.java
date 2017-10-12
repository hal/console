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
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.Iterables;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import jsinterop.annotations.JsIgnore;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
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

    @Inject
    public ComplexAttributeOperations(CrudOperations crud,
            EventBus eventBus,
            Dispatcher dispatcher,
            MetadataProcessor metadataProcessor,
            @Footer Provider<Progress> progress,
            StatementContext statementContext,
            Resources resources) {
        this.crud = crud;
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.metadataProcessor = metadataProcessor;
        this.progress = progress;
        this.statementContext = statementContext;
        this.resources = resources;
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
     * @param type             the human readable name of the complex attribute
     * @param template         the address template which is resolved against the current statement context and the
     *                         resource name to get the resource address for the operation
     * @param callback         the callback executed after the resource has been added
     */
    @JsIgnore
    public void add(String id, String resource, String complexAttribute, String type, AddressTemplate template,
            Callback callback) {
        lookupAndAdd(id, complexAttribute, type, template, emptyList(),
                (name, model) -> add(resource, complexAttribute, type, template, model, callback));
    }

    /**
     * Opens an add-resource-dialog for the given complex attribute. The dialog contains fields for all required
     * attributes. When clicking "Add", a new complex attribute is created and written to the specified resource.
     * After the resource has been updated, a success message is fired and the specified callback is executed.
     *
     * @param id               the id used for the add resource dialog
     * @param resource         the resource name
     * @param complexAttribute the name of the complex attribute
     * @param type             the human readable name of the complex attribute
     * @param template         the address template which is resolved against the current statement context and the
     *                         resource name to get the resource address for the operation
     * @param attributes       additional attributes which should be part of the add resource dialog
     * @param callback         the callback executed after the resource has been added
     */
    @JsIgnore
    public void add(String id, String resource, String complexAttribute, String type, AddressTemplate template,
            Iterable<String> attributes, Callback callback) {
        lookupAndAdd(id, complexAttribute, type, template, attributes,
                (name, model) -> add(resource, complexAttribute, type, template, model, callback));
    }

    /**
     * Opens an add-resource-dialog for the given complex attribute. The dialog contains fields for all required
     * attributes. When clicking "Add", a new model node is created and added to the complex attribute in the specified
     * resource. After the resource has been updated, a success message is fired and the specified callback is executed.
     *
     * @param id               the id used for the add resource dialog
     * @param resource         the resource name
     * @param complexAttribute the name of the complex attribute
     * @param type             the human readable name of the complex attribute
     * @param template         the address template which is resolved against the current statement context and the
     *                         resource name to get the resource address for the operation
     * @param callback         the callback executed after the resource has been added
     */
    @JsIgnore
    public void listAdd(String id, String resource, String complexAttribute, String type, AddressTemplate template,
            Callback callback) {
        lookupAndAdd(id, complexAttribute, type, template, emptyList(),
                (name, model) -> listAdd(resource, complexAttribute, type, template, model, callback));
    }

    /**
     * Opens an add-resource-dialog for the given complex attribute. If the resource contains required attributes, they
     * are displayed, otherwise all attributes are displayed.
     * When clicking "Add", a new model node is created and added to the complex attribute in the specified
     * resource. After the resource has been updated, a success message is fired and the specified callback is
     * executed.
     *
     * @param id               the id used for the add resource dialog
     * @param resource         the resource name
     * @param complexAttribute the name of the complex attribute
     * @param type             the human readable name of the complex attribute
     * @param template         the address template which is resolved against the current statement context and the
     *                         resource name to get the resource address for the operation
     * @param attributes       additional attributes which should be part of the add resource dialog
     * @param callback         the callback executed after the resource has been added
     */
    @JsIgnore
    public void listAdd(String id, String resource, String complexAttribute, String type, AddressTemplate template,
            Iterable<String> attributes, Callback callback) {
        lookupAndAdd(id, complexAttribute, type, template, attributes,
                (name, model) -> listAdd(resource, complexAttribute, type, template, model, callback));
    }

    private void lookupAndAdd(String id, String complexAttribute, String type, AddressTemplate template,
            Iterable<String> attributes, AddResourceDialog.Callback callback) {
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(Metadata metadata) {
                Metadata caMetadata = metadata.forComplexAttribute(complexAttribute);
                boolean requiredAttributes = !caMetadata.getDescription()
                        .getRequiredAttributes(ATTRIBUTE_GROUP)
                        .isEmpty();
                ModelNodeForm.Builder<ModelNode> builder = new ModelNodeForm.Builder<>(id, caMetadata)
                        .addOnly();
                if (requiredAttributes) {
                    builder.requiredOnly();
                }
                if (!Iterables.isEmpty(attributes)) {
                    builder.include(attributes).unsorted();
                }
                AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(type),
                        builder.build(), callback);
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
     * @param type             the human readable name of the complex attribute
     * @param template         the address template which is resolved against the current statement context and the
     *                         resource name to get the resource address for the operation
     * @param payload          the optional payload for the complex attribute (may be null or undefined)
     * @param callback         the callback executed after the resource has been added
     */
    @JsIgnore
    public void add(String resource, String complexAttribute, String type, AddressTemplate template,
            @Nullable ModelNode payload, Callback callback) {
        ResourceAddress address = template.resolve(statementContext, resource);
        add(complexAttribute, type, address, payload, callback);
    }

    /**
     * Writes the payload to the complex attribute in the specified resource. After the resource has been updated,
     * a success message is fired and the specified callback is executed.
     *
     * @param complexAttribute the name of the complex attribute
     * @param type             the human readable name of the complex attribute
     * @param address          the fq address for the operation
     * @param payload          the optional payload for the complex attribute (may be null or undefined)
     * @param callback         the callback executed after the resource has been added
     */
    @JsIgnore
    public void add(String complexAttribute, String type, ResourceAddress address, @Nullable ModelNode payload,
            Callback callback) {
        Operation operation = new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, complexAttribute)
                .param(VALUE, payload == null ? new ModelNode().addEmptyObject() : payload)
                .build();
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(eventBus, Message.success(resources.messages().addSingleResourceSuccess(type)));
            callback.execute();
        });
    }

    /**
     * Adds the payload to the complex attribute in the specified resource. After the resource has been updated,
     * a success message is fired and the specified callback is executed.
     *
     * @param resource         the resource name
     * @param complexAttribute the name of the complex attribute
     * @param type             the human readable name of the complex attribute
     * @param template         the address template which is resolved against the current statement context and the
     *                         resource name to get the resource address for the operation
     * @param payload          the optional payload for the complex attribute (may be null or undefined)
     * @param callback         the callback executed after the resource has been added
     */
    @JsIgnore
    public void listAdd(String resource, String complexAttribute, String type, AddressTemplate template,
            @Nullable ModelNode payload, Callback callback) {
        ResourceAddress address = template.resolve(statementContext, resource);
        Operation operation = new Operation.Builder(address, LIST_ADD_OPERATION)
                .param(NAME, complexAttribute)
                .param(VALUE, payload == null ? new ModelNode().addEmptyObject() : payload)
                .build();
        dispatcher.execute(operation, result -> {
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
     * @param type             the human readable name of the complex attribute
     * @param template         the address template which is resolved against the current statement context and the
     *                         resource name to get the resource address for the operation
     * @param changedValues    the changed values / payload for the operation
     * @param callback         the callback executed after the resource has been saved
     */
    @JsIgnore
    public void save(String resource, String complexAttribute, String type, AddressTemplate template,
            Map<String, Object> changedValues, Callback callback) {
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(Metadata metadata) {
                ResourceAddress address = template.resolve(statementContext, resource);
                Metadata caMetadata = metadata.forComplexAttribute(complexAttribute);
                save(complexAttribute, type, address, changedValues, caMetadata, callback);
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
     * @param type             the human readable name of the complex attribute
     * @param index            the index for the list-type complex attribute
     * @param template         the address template which is resolved against the current statement context and the
     *                         resource name to get the resource address for the operation
     * @param changedValues    the changed values / payload for the operation
     * @param callback         the callback executed after the resource has been saved
     */
    @JsIgnore
    public void save(String resource, String complexAttribute, String type, int index, AddressTemplate template,
            Map<String, Object> changedValues, Callback callback) {
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(Metadata metadata) {
                ResourceAddress address = template.resolve(statementContext, resource);
                Metadata caMetadata = metadata.forComplexAttribute(complexAttribute);
                save(complexAttribute, type, index, address, changedValues, caMetadata, callback);
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
     * @param type             the human readable name of the complex attribute
     * @param address          the fq address for the operation
     * @param changedValues    the changed values / payload for the operation
     * @param metadata         the metadata for the complex attribute
     * @param callback         the callback executed after the resource has been saved
     */
    @JsIgnore
    public void save(String complexAttribute, String type, ResourceAddress address, Map<String, Object> changedValues,
            Metadata metadata, Callback callback) {
        Composite operations = operationFactory(complexAttribute).fromChangeSet(address, changedValues, metadata);
        crud.save(operations, resources.messages().modifySingleResourceSuccess(type), callback);
    }

    /**
     * Writes the changed values to the list-type complex attribute. After the complex attribute has been saved a
     * standard success message is fired and the specified callback is executed.
     * <p>
     * If the change set is empty, a warning message is fired and the specified callback is executed.
     *
     * @param complexAttribute the name of the complex attribute
     * @param type             the human readable name of the complex attribute
     * @param index            the index for the list-type complex attribute
     * @param address          the fq address for the operation
     * @param changedValues    the changed values / payload for the operation
     * @param metadata         the metadata for the complex attribute
     * @param callback         the callback executed after the resource has been saved
     */
    @JsIgnore
    public void save(String complexAttribute, String type, int index, ResourceAddress address,
            Map<String, Object> changedValues, Metadata metadata, Callback callback) {
        Composite operations = operationFactory(complexAttribute, index).fromChangeSet(address, changedValues,
                metadata);
        crud.save(operations, resources.messages().modifySingleResourceSuccess(type), callback);
    }


    // ------------------------------------------------------ (u) reset using template

    /**
     * Undefines all non required attributes in the specified form. After the attributes in the complex attribute have
     * been undefined a standard success message is fired and the specified callback is executed.
     * <p>
     * If the form contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param resource         the resource name
     * @param complexAttribute the name of the complex attribute
     * @param type             the human readable name of the complex attribute
     * @param template         the address template which is resolved against the current statement context and the
     *                         resource name to get the resource address for the operation
     * @param form             the form which should be reset
     * @param callback         the callback executed after the resource has been saved
     */
    @JsIgnore
    public <T> void reset(String resource, String complexAttribute, String type, AddressTemplate template, Form<T> form,
            Callback callback) {
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(Metadata metadata) {
                Metadata caMetadata = metadata.forComplexAttribute(complexAttribute);
                reset(resource, complexAttribute, type, template, caMetadata, form, callback);
            }
        });
    }

    /**
     * Undefines all non required attributes in the specified form. After the attributes in the complex attribute have
     * been undefined a standard success message is fired and the specified callback is executed.
     * <p>
     * If the form contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param resource         the resource name
     * @param complexAttribute the name of the complex attribute
     * @param type             the human readable name of the complex attribute
     * @param template         the address template which is resolved against the current statement context and the
     *                         resource name to get the resource address for the operation
     * @param form             the form which should be reset
     * @param callback         the callback executed after the resource has been saved
     */
    @JsIgnore
    public <T> void reset(String resource, String complexAttribute, String type, AddressTemplate template,
            Metadata metadata, Form<T> form, Callback callback) {
        Set<String> attributes = stream(form.getBoundFormItems().spliterator(), false)
                .map(FormItem::getName)
                .collect(toSet());

        ResourceAddress address = template.resolve(statementContext, resource);
        Composite composite = operationFactory(complexAttribute).resetResource(address, attributes, metadata);
        reset(type, composite, callback);
    }

    /**
     * Undefines all non required attributes in the specified form. After the attributes in the complex attribute have
     * been undefined a standard success message is fired and the specified callback is executed.
     * <p>
     * If the form contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param complexAttribute the name of the complex attribute
     * @param type             the human readable name of the complex attribute
     * @param address          the fq address for the operation
     * @param form             the form which should be reset
     * @param callback         the callback executed after the resource has been saved
     */
    @JsIgnore
    public <T> void reset(String complexAttribute, String type, ResourceAddress address, Metadata metadata,
            Form<T> form, Callback callback) {
        Set<String> attributes = stream(form.getBoundFormItems().spliterator(), false)
                .map(FormItem::getName)
                .collect(toSet());

        Composite composite = operationFactory(complexAttribute).resetResource(address, attributes, metadata);
        reset(type, composite, callback);
    }

    private void reset(String type, Composite composite, Callback callback) {
        if (composite.isEmpty()) {
            MessageEvent.fire(eventBus, Message.warning(resources.messages().noReset()));
            callback.execute();
        } else {
            DialogFactory.showConfirmation(
                    resources.messages().resetConfirmationTitle(type),
                    resources.messages().resetSingletonConfirmationQuestion(),
                    () -> dispatcher.execute(composite, (CompositeResult result) -> {
                        MessageEvent.fire(eventBus,
                                Message.success(resources.messages().resetSingletonSuccess(type)));
                        callback.execute();
                    }));
        }
    }


    // ------------------------------------------------------ (d)elete using template

    /**
     * Undefines the complex attribute. After the attribute has been undefined a standard success message is fired and
     * the specified callback is executed.
     *
     * @param resource         the resource name
     * @param complexAttribute the name of the complex attribute
     * @param type             the human readable name of the complex attribute
     * @param template         the address template which is resolved against the current statement context and the
     *                         resource name to get the resource address for the operation
     * @param callback         the callback executed after the complex attribute has been undefined
     */
    @JsIgnore
    public void remove(String resource, String complexAttribute, String type, AddressTemplate template,
            Callback callback) {
        ResourceAddress address = template.resolve(statementContext, resource);
        remove(complexAttribute, type, address, callback);
    }

    /**
     * Undefines the complex attribute. After the attribute has been undefined a standard success message is fired and
     * the specified callback is executed.
     *
     * @param complexAttribute the name of the complex attribute
     * @param type             the human readable name of the complex attribute
     * @param address          the fq address for the operation
     * @param callback         the callback executed after the complex attribute has been undefined
     */
    @JsIgnore
    public void remove(String complexAttribute, String type, ResourceAddress address, Callback callback) {
        Operation operation = new Operation.Builder(address, UNDEFINE_ATTRIBUTE_OPERATION)
                .param(NAME, complexAttribute)
                .build();
        SafeHtml question = resources.messages().removeSingletonConfirmationQuestion();
        DialogFactory.showConfirmation(
                resources.messages().removeConfirmationTitle(type), question,
                () -> dispatcher.execute(operation, result -> {
                    MessageEvent.fire(eventBus,
                            Message.success(resources.messages().removeSingletonResourceSuccess(type)));
                    callback.execute();
                }));
    }

    /**
     * Undefines the complex attribute at the specified index. After the attribute has been undefined a standard success
     * message is fired and the specified callback is executed.
     *
     * @param resource         the resource name
     * @param complexAttribute the name of the complex attribute
     * @param type             the human readable name of the complex attribute
     * @param index            the index for the list-type complex attribute
     * @param template         the address template which is resolved against the current statement context and the
     *                         resource name to get the resource address for the operation
     * @param callback         the callback executed after the complex attribute has been undefined
     */
    @JsIgnore
    public void remove(String resource, String complexAttribute, String type, int index, AddressTemplate template,
            Callback callback) {
        ResourceAddress address = template.resolve(statementContext, resource);
        Operation operation = new Operation.Builder(address, LIST_REMOVE_OPERATION)
                .param(NAME, complexAttribute)
                .param(INDEX, index)
                .build();
        SafeHtml question = resources.messages().removeSingletonConfirmationQuestion();
        DialogFactory.showConfirmation(
                resources.messages().removeConfirmationTitle(type), question,
                () -> dispatcher.execute(operation, result -> {
                    MessageEvent.fire(eventBus,
                            Message.success(resources.messages().removeSingletonResourceSuccess(type)));
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
