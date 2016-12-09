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
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.OperationFactory;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.dmr.model.SuccessfulOutcome;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

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
 *
 * @author Harald Pehl
 */
public class PropertiesOperations {

    private static class ReadProperties implements Function<FunctionContext> {

        private final Dispatcher dispatcher;
        private final ResourceAddress address;
        private final String psr;

        private ReadProperties(final Dispatcher dispatcher, ResourceAddress address, final String psr) {
            this.dispatcher = dispatcher;
            this.address = address;
            this.psr = psr;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            Operation operation = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION, address)
                    .param(CHILD_TYPE, psr)
                    .build();
            //noinspection Duplicates
            dispatcher.executeInFunction(control, operation,
                    result -> {
                        control.getContext().push(result.asList().stream()
                                .map(ModelNode::asString)
                                .collect(Collectors.toSet()));
                        control.proceed();
                    },
                    (op, failure) -> {
                        control.getContext().push(Collections.emptySet());
                        control.proceed();
                    });
        }
    }


    private static class MergeProperties implements Function<FunctionContext> {

        private final Dispatcher dispatcher;
        private final ResourceAddress address;
        private final String propertiesResource;
        private final Map<String, String> properties;

        public MergeProperties(final Dispatcher dispatcher, final ResourceAddress address,
                final String propertiesResource, final Map<String, String> properties) {
            this.dispatcher = dispatcher;
            this.address = address;
            this.propertiesResource = propertiesResource;
            this.properties = properties;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            Set<String> existingProperties = control.getContext().pop();
            Set<String> add = Sets.difference(properties.keySet(), existingProperties).immutableCopy();
            Set<String> modify = Sets.intersection(properties.keySet(), existingProperties).immutableCopy();
            Set<String> remove = Sets.difference(existingProperties, properties.keySet()).immutableCopy();

            List<Operation> operations = new ArrayList<>();
            add.stream()
                    .map(property -> {
                        ResourceAddress address = new ResourceAddress(this.address).add(propertiesResource, property);
                        Operation.Builder builder = new Operation.Builder(ADD, address);
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
                        return new Operation.Builder(WRITE_ATTRIBUTE_OPERATION, address)
                                .param(NAME, VALUE)
                                .param(VALUE, properties.get(property))
                                .build();
                    })
                    .forEach(operations::add);
            remove.stream()
                    .map(property -> new Operation.Builder(REMOVE,
                            new ResourceAddress(address).add(propertiesResource, property))
                            .build())
                    .forEach(operations::add);

            Composite composite = new Composite(operations);
            if (composite.isEmpty()) {
                control.proceed();
            } else {
                dispatcher.executeInFunction(control, new Composite(operations),
                        (CompositeResult result) -> control.proceed());
            }
        }
    }


    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final Provider<Progress> progress;
    private final StatementContext statementContext;
    private final Resources resources;
    private final CrudOperations crud;
    private final OperationFactory operationFactory;

    @Inject
    public PropertiesOperations(final EventBus eventBus,
            final Dispatcher dispatcher,
            @Footer final Provider<Progress> progress,
            final StatementContext statementContext,
            final Resources resources,
            final CrudOperations crud) {
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.progress = progress;
        this.statementContext = statementContext;
        this.resources = resources;
        this.crud = crud;
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
    public void saveWithProperties(final String type, final String name, final AddressTemplate template,
            final Map<String, Object> changedValues, final String psr, final Map<String, String> properties,
            final Callback callback) {

        changedValues.remove(psr);
        if (properties.isEmpty()) {
            crud.save(type, name, template, changedValues, callback);
        } else {
            ResourceAddress address = template.resolve(statementContext, name);
            Composite operations = operationFactory.fromChangeSet(address, changedValues);
            saveInternal(type, name, address, operations, psr, properties, callback);
        }
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
     * @param psr           the name of the properties sub resource (PSR) - most often this is "property"
     * @param properties    the properties to save
     * @param callback      the callback executed after saving the resource
     */
    public void saveWithProperties(final String type, final String name, final ResourceAddress address,
            final Map<String, Object> changedValues, final String psr, final Map<String, String> properties,
            final Callback callback) {

        changedValues.remove(psr);
        if (properties.isEmpty()) {
            crud.save(type, name, address, changedValues, callback);
        } else {
            Composite operations = operationFactory.fromChangeSet(address, changedValues);
            saveInternal(type, name, address, operations, psr, properties, callback);
        }
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
    public void saveWithProperties(final String type, final String name, final ResourceAddress address,
            final Composite operations, final String psr, final Map<String, String> properties,
            final Callback callback) {

        if (properties.isEmpty()) {
            crud.save(type, name, operations, callback);
        } else {
            saveInternal(type, name, address, operations, psr, properties, callback);
        }
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
     * @param psr           the name of the properties sub resource (PSR) - most often this is "property"
     * @param properties    the properties to save
     * @param callback      the callback executed after saving the resource
     */
    public void saveSingletonWithProperties(final String type, final ResourceAddress address,
            final Map<String, Object> changedValues, final String psr, final Map<String, String> properties,
            final Callback callback) {

        changedValues.remove(psr);

        if (properties.isEmpty()) {
            crud.saveSingleton(type, address, changedValues, callback);
        } else {
            Composite operations = operationFactory.fromChangeSet(address, changedValues);
            saveInternal(type, null, address, operations, psr, properties, callback);
        }
    }

    private void saveInternal(String type, String name, ResourceAddress address,
            Composite operations, String psr, Map<String, String> properties, Callback callback) {

        // TODO Check if the functions can be replaced with a composite operation
        Function[] functions = new Function[]{
                (Function<FunctionContext>) control -> {
                    if (operations.isEmpty()) {
                        control.proceed();
                    } else {
                        dispatcher.executeInFunction(control, operations,
                                (CompositeResult result) -> control.proceed());
                    }
                },
                new ReadProperties(dispatcher, address, psr),
                new MergeProperties(dispatcher, address, psr, properties)
        };

        new Async<FunctionContext>(progress.get())
                .waterfall(new FunctionContext(), new SuccessfulOutcome(eventBus, resources) {
                    @Override
                    public void onSuccess(final FunctionContext context) {
                        if (name == null) {
                            MessageEvent.fire(eventBus,
                                    Message.success(resources.messages().modifySingleResourceSuccess(type)));
                        } else {
                            MessageEvent.fire(eventBus,
                                    Message.success(resources.messages().modifyResourceSuccess(type, name)));
                        }
                        callback.execute();
                    }
                }, functions);
    }
}
