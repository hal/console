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
package org.jboss.hal.ballroom.autocomplete;

import java.util.List;

import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.js.JsonObject;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_ALIASES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_DEFAULTS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROXIES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

public class ReadChildrenAutoComplete extends AutoComplete {

    private static final String ERROR_MESSAGE = "Unable to read child resource suggestions for {}: {}";

    public ReadChildrenAutoComplete(Dispatcher dispatcher, StatementContext statementContext,
            AddressTemplate template) {
        this(dispatcher, statementContext, singleton(template));
    }

    public ReadChildrenAutoComplete(Dispatcher dispatcher, StatementContext statementContext,
            Iterable<AddressTemplate> templates) {
        verifyTemplates(templates);

        ResultProcessor resultProcessor;
        ItemRenderer<JsonObject> itemRenderer;
        int numberOfTemplates = Iterables.size(templates);

        if (numberOfTemplates == 1) {
            AddressTemplate template = templates.iterator().next();
            int wildcards = Iterables.size(Splitter.on('*').split(template.toString())) - 1;
            if (wildcards == 0 || (wildcards == 1 && "*".equals(template.lastValue()))) {
                resultProcessor = new NamesResultProcessor();
                itemRenderer = new StringRenderer<>(result -> result.get(NAME).asString());

            } else {
                resultProcessor = new SingleReadChildrenProcessor();
                itemRenderer = new ReadChildrenRenderer();
            }

        } else {
            resultProcessor = new CompositeReadChildrenProcessor();
            itemRenderer = new ReadChildrenRenderer();
        }

        Options options = new OptionsBuilder<JsonObject>((query, response) -> {
            List<Operation> operations = stream(templates.spliterator(), false)
                    .map(template -> template.resolve(statementContext))
                    .map(address -> operation(address, numberOfTemplates))
                    .collect(toList());
            if (operations.size() == 1) {
                dispatcher.execute(operations.get(0),
                        result -> response.response(resultProcessor.process(query, result)),
                        (operation, error) -> {
                            logger.error(ERROR_MESSAGE, templates, error);
                            response.response(new JsonObject[0]);

                        });
            } else {
                dispatcher.execute(new Composite(operations),
                        (CompositeResult result) -> response.response(resultProcessor.process(query, result)),
                        (operation, error) -> {
                            logger.error(ERROR_MESSAGE, templates, error);
                            response.response(new JsonObject[0]);

                        });
            }
        }).renderItem(itemRenderer).build();
        init(options);
    }

    private void verifyTemplates(Iterable<AddressTemplate> templates) {
        if (Iterables.isEmpty(templates)) {
            throw new IllegalArgumentException("Templates must not be empty in ReadChildrenAutoComplete");
        }
    }

    private Operation operation(ResourceAddress address, int numberOfTemplates) {
        Operation operation;

        int wildcards = 0;
        if (address.isDefined()) {
            for (Property property : address.asPropertyList()) {
                if ("*".equals(property.getValue().asString())) {
                    wildcards++;
                }
            }
        }

        if (numberOfTemplates == 1 &&
                (wildcards == 0 || (wildcards == 1 && "*".equals(address.lastValue())))) {
            ResourceAddress parent = address.getParent();
            String childName = address.lastName();
            operation = new Operation.Builder(parent, READ_CHILDREN_NAMES_OPERATION, true)
                    .param(CHILD_TYPE, childName).build();

        } else {
            // The address is something like /foo=*/bar=*
            // Would be nice if we could use
            // /foo=*:read-children-names(child-type=bar)
            // but it returns an empty list, so we're using
            // /foo=*/bar=*:read-resource
            // which makes parsing the response more complicated
            operation = new Operation.Builder(address, READ_RESOURCE_OPERATION, true)
                    .param(ATTRIBUTES_ONLY, true)
                    .param(INCLUDE_ALIASES, false)
                    .param(INCLUDE_DEFAULTS, false)
                    .param(INCLUDE_RUNTIME, false)
                    .param(PROXIES, false)
                    .build();
        }

        return operation;
    }
}
