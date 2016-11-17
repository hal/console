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
package org.jboss.hal.core.mbui.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.google.gwt.core.client.GWT;
import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.ballroom.form.PropertiesItem;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Constants;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;

/**
 * Umbrella class for managing properties which are laid out as sub resources:
 * <pre>
 * {
 *     "top-level-attribute" => undefined,
 *     "property" => {
 *         "foo" => {"value" => "bar"},
 *         "baz" => {"value" => "qux"}
 *     }
 * }
 * </pre>
 *
 * @author Harald Pehl
 */
public class SubResourceProperties {

    private static final Constants CONSTANTS = GWT.create(Constants.class);


    public static class FormItem extends PropertiesItem {

        /**
         * Creates a new properties item labeled {@link Constants#properties()}
         *
         * @param resource the name of the properties sub resource - normally "property"
         */
        public FormItem(final String resource) {
            super(resource, CONSTANTS.properties());
        }
    }


    /**
     * Function which reads the properties from the resource containing the property sub resource. The property names
     * are pushed as set onto the function context stack.
     */
    public static class ReadProperties implements Function<FunctionContext> {

        private final Dispatcher dispatcher;
        private final StatementContext statementContext;
        private final AddressTemplate template;
        private final String resource;

        /**
         * Creates a new instance to read the properties names.
         *
         * @param dispatcher       the dispatcher
         * @param statementContext the statement context to resolve the address template
         * @param template         the address template of the resource containing the property sub resource
         * @param resource         the name of the properties sub resource - normally "property"
         */
        public ReadProperties(final Dispatcher dispatcher, final StatementContext statementContext,
                final AddressTemplate template, final String resource) {
            this.dispatcher = dispatcher;
            this.statementContext = statementContext;
            this.template = template;
            this.resource = resource;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            Operation operation = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION,
                    template.resolve(statementContext))
                    .param(CHILD_TYPE, resource)
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


    /**
     * Function to save properties. Properties which are in the specified map, but not in the management
     * model are added, properties which are in the management model but not in the map are removed.
     */
    public static class MergeProperties implements Function<FunctionContext> {

        private final Dispatcher dispatcher;
        private final StatementContext statementContext;
        private final AddressTemplate template;
        private final Map<String, String> properties;

        /**
         * @param dispatcher       the dispatcher
         * @param statementContext the statement context to resolve the address template
         * @param template         the address template of the resource containing the property sub resource
         * @param resource         the name of the properties sub resource - normally "property"
         * @param properties       the new properties which are merged into the existing properties in the model
         */
        public MergeProperties(final Dispatcher dispatcher, final StatementContext statementContext,
                final AddressTemplate template, final String resource, final Map<String, String> properties) {
            this.dispatcher = dispatcher;
            this.statementContext = statementContext;
            this.template = template.append(resource + "=*");
            this.properties = properties;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            Set<String> existingProperties = control.getContext().pop();
            Set<String> add = Sets.difference(properties.keySet(), existingProperties).immutableCopy();
            Set<String> remove = Sets.difference(existingProperties, properties.keySet()).immutableCopy();

            List<Operation> operations = new ArrayList<>();
            add.stream()
                    .map(property -> new Operation.Builder(ADD, template.resolve(statementContext, property))
                            .param(VALUE, properties.get(property))
                            .build())
                    .forEach(operations::add);
            remove.stream()
                    .map(property -> new Operation.Builder(REMOVE, template.resolve(statementContext, property))
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
}
