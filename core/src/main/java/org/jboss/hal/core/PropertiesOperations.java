/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.Sets;
import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
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
import rx.Completable;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.flow.Flow.series;

/**
 * Many resources store properties in form of a sub resource similar to:
 * <pre>
 * {
 *     "top-level-attribute" => undefined,
 *     "property" => {
 *         "foo" => {"value" => "bar"},
 *         "baz" => {"value" => "qux"}
 *     }
 * }
 * </pre>
 * where {@code "property"} is the properties sub resource (PSR). Each property has its onw resource with the name as
 * key and the {@code "value"} string node as value.
 * <p>
 * This class mirrors some of the methods from {@link CrudOperations} to save these resources together with its
 * properties (if modified):
 * <ol>
 * <li>New properties are added as children of the PSR</li>
 * <li>Modified properties are modified in the PSRs</li>
 * <li>Removed properties are removed from the PSR</li>
 * </ol>
 */
public class PropertiesOperations {

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final MetadataProcessor metadataProcessor;
    private final Provider<Progress> progress;
    private final StatementContext statementContext;
    private final Resources resources;
    private final OperationFactory operationFactory;

    @Inject
    public PropertiesOperations(EventBus eventBus,
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


    // ------------------------------------------------------ save methods
    // please add additional save() or saveSingleton() methods from CrudOperations if necessary

    /**
     * Saves the changed values and its properties (if modified) to the specified resource. After the resource has been
     * saved a standard success message is fired and the specified callback is executed.
     * <p>
     * This is the properties-extended version of {@link CrudOperations#save(String, String, AddressTemplate, Map,
     * Callback)}:
     * <ol>
     * <li>New properties are added as children of the PSR</li>
     * <li>Modified properties are modified in the PSRs</li>
     * <li>Removed properties are removed from the PSR</li>
     * </ol>
     *
     * @param type          the human readable resource type used in the success message
     * @param name          the resource name
     * @param template      the address template which is resolved against the current statement context and the
     *                      resource name to get the resource address for the operation
     * @param changedValues the changed values / payload for the operation
     * @param psr           the name of the properties sub resource (PSR) - most often this is "property"
     * @param properties    the properties to save
     * @param callback      the callback executed after saving the resource
     */
    public void saveWithProperties(String type, String name, AddressTemplate template,
            Map<String, Object> changedValues, String psr, Map<String, String> properties,
            Callback callback) {

        changedValues.remove(psr);
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(Metadata metadata) {
                ResourceAddress address = template.resolve(statementContext, name);
                Composite operations = operationFactory.fromChangeSet(address, changedValues, metadata);
                saveInternal(type, name, address, operations, psr, properties, callback);
            }
        });
    }

    /**
     * Saves the changed values and its properties (if modified) to the specified resource. After the resource has been
     * saved a standard success message is fired and the specified callback is executed.
     * <p>
     * This is the properties-extended version of {@link CrudOperations#save(String, String, AddressTemplate, Map,
     * Callback)}:
     * <ol>
     * <li>New properties are added as children of the PSR</li>
     * <li>Modified properties are modified in the PSRs</li>
     * <li>Removed properties are removed from the PSR</li>
     * </ol>
     *
     * @param type          the human readable resource type used in the success message
     * @param name          the resource name
     * @param address       the fq address for the operation
     * @param changedValues the changed values / payload for the operation
     * @param metadata      the metadata for the of the attributes in the change set
     * @param psr           the name of the properties sub resource (PSR) - most often this is "property"
     * @param properties    the properties to save
     * @param callback      the callback executed after saving the resource
     */
    public void saveWithProperties(String type, String name, ResourceAddress address,
            Map<String, Object> changedValues, Metadata metadata, String psr,
            Map<String, String> properties, Callback callback) {

        changedValues.remove(psr);
        Composite operations = operationFactory.fromChangeSet(address, changedValues, metadata);
        saveInternal(type, name, address, operations, psr, properties, callback);
    }

    /**
     * Saves the changed values and its properties (if modified) to the specified resource. After the resource has been
     * saved a standard success message is fired and the specified callback is executed.
     * <p>
     * This is the properties-extended version of {@link CrudOperations#save(String, String, Composite, Callback)}:
     * <ol>
     * <li>New properties are added as children of the PSR</li>
     * <li>Modified properties are modified in the PSRs</li>
     * <li>Removed properties are removed from the PSR</li>
     * </ol>
     *
     * @param type       the human readable resource type used in the success message
     * @param name       the resource name
     * @param address    the fq address for the operation
     * @param operations the composite operation to persist the changed values
     * @param psr        the name of the properties sub resource (PSR) - most often this is "property"
     * @param properties the properties to save
     * @param callback   the callback executed after saving the resource
     */
    public void saveWithProperties(String type, String name, ResourceAddress address,
            Composite operations, String psr, Map<String, String> properties,
            Callback callback) {

        saveInternal(type, name, address, operations, psr, properties, callback);
    }

    /**
     * Saves the changed values and its properties (if modified) to the specified resource. After the resource has been
     * saved a standard success message is fired and the specified callback is executed.
     * <p>
     * This is the properties-extended version of {@link CrudOperations#saveSingleton(String, AddressTemplate, Map,
     * Callback)}:
     * <ol>
     * <li>New properties are added as children of the PSR</li>
     * <li>Modified properties are modified in the PSRs</li>
     * <li>Removed properties are removed from the PSR</li>
     * </ol>
     *
     * @param type          the human readable resource type used in the success message
     * @param address       the fq address for the operation
     * @param changedValues the changed values / payload for the operation
     * @param metadata      the metadata for the of the attributes in the change set
     * @param psr           the name of the properties sub resource (PSR) - most often this is "property"
     * @param properties    the properties to save
     * @param callback      the callback executed after saving the resource
     */
    public void saveSingletonWithProperties(String type, ResourceAddress address,
            Map<String, Object> changedValues, Metadata metadata, String psr,
            Map<String, String> properties, Callback callback) {

        changedValues.remove(psr);
        Composite operations = operationFactory.fromChangeSet(address, changedValues, metadata);
        saveInternal(type, null, address, operations, psr, properties, callback);
    }

    private void saveInternal(String type, String name, ResourceAddress address,
            Composite operations, String psr, Map<String, String> properties, Callback callback) {

        // TODO Check if the steps can be replaced with a composite operation
        series(new FlowContext(progress.get()),
                context -> operations.isEmpty()
                        ? Completable.complete()
                        : dispatcher.execute(operations).toCompletable(),
                new ReadProperties(dispatcher, address, psr),
                new MergeProperties(dispatcher, address, psr, properties))
                .subscribe(new SuccessfulOutcome<FlowContext>(eventBus, resources) {
                    @Override
                    public void onSuccess(FlowContext context) {
                        if (name == null) {
                            MessageEvent.fire(eventBus,
                                    Message.success(resources.messages().modifySingleResourceSuccess(type)));
                        } else {
                            MessageEvent.fire(eventBus,
                                    Message.success(resources.messages().modifyResourceSuccess(type, name)));
                        }
                        callback.execute();
                    }
                });
    }


    private static class ReadProperties implements Task<FlowContext> {

        private final Dispatcher dispatcher;
        private final ResourceAddress address;
        private final String psr;

        private ReadProperties(Dispatcher dispatcher, ResourceAddress address, String psr) {
            this.dispatcher = dispatcher;
            this.address = address;
            this.psr = psr;
        }

        @Override
        public Completable call(FlowContext context) {
            Operation operation = new Operation.Builder(address, READ_CHILDREN_NAMES_OPERATION)
                    .param(CHILD_TYPE, psr)
                    .build();
            return dispatcher.execute(operation)
                    .doOnSuccess(result -> context.push(result.asList().stream()
                            .map(ModelNode::asString)
                            .collect(Collectors.toSet())))
                    .doOnError(failure -> context.push(Collections.emptySet()))
                    .toCompletable();
        }
    }


    private static class MergeProperties implements Task<FlowContext> {

        private final Dispatcher dispatcher;
        private final ResourceAddress address;
        private final String propertiesResource;
        private final Map<String, String> properties;

        public MergeProperties(Dispatcher dispatcher, ResourceAddress address, String propertiesResource,
                Map<String, String> properties) {
            this.dispatcher = dispatcher;
            this.address = address;
            this.propertiesResource = propertiesResource;
            this.properties = properties;
        }

        @Override
        public Completable call(FlowContext context) {
            Set<String> existingProperties = context.pop();
            Set<String> add = Sets.difference(properties.keySet(), existingProperties).immutableCopy();
            Set<String> modify = Sets.intersection(properties.keySet(), existingProperties).immutableCopy();
            Set<String> remove = Sets.difference(existingProperties, properties.keySet()).immutableCopy();

            List<Operation> operations = new ArrayList<>();
            add.stream()
                    .map(property -> {
                        ResourceAddress address = new ResourceAddress(this.address).add(propertiesResource, property);
                        Operation.Builder builder = new Operation.Builder(address, ADD);
                        if (properties.get(property) != null) {
                            builder.param(VALUE, properties.get(property));
                        }
                        return builder.build();
                    })
                    .forEach(operations::add);
            modify.stream()
                    .filter(property -> properties.get(property) != null)
                    .map(property -> {
                        ResourceAddress address = new ResourceAddress(this.address).add(propertiesResource, property);
                        return new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                                .param(NAME, VALUE)
                                .param(VALUE, properties.get(property))
                                .build();
                    })
                    .forEach(operations::add);
            remove.stream()
                    .map(property -> new Operation.Builder(
                            new ResourceAddress(address).add(propertiesResource, property), REMOVE)
                            .build())
                    .forEach(operations::add);

            Composite composite = new Composite(operations);
            return composite.isEmpty()
                    ? Completable.complete()
                    : dispatcher.execute(new Composite(operations)).toCompletable();
        }
    }
}
