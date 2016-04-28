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
package org.jboss.hal.ballroom.typeahead;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;

import static org.jboss.hal.ballroom.typeahead.Typeahead.WHITESPACE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Provides typeahead instances based on one or multiple resource addresses. If multiple addresses are given or if
 * an address contains multiple wildcards, the suggestions will be grouped.
 *
 * @author Harald Pehl
 */
public class TypeaheadProvider {

    public Typeahead from(ResourceAddress address) {
        return from(Collections.singletonList(address));
    }

    public Typeahead from(Iterable<ResourceAddress> addresses) {
        if (Iterables.isEmpty(addresses)) {
            throw new IllegalStateException("No resource address given in typeahead provider!");
        }

        boolean singleAddress = Iterables.size(addresses) == 1;
        //noinspection Guava
        List<Operation> operations = FluentIterable.from(addresses)
                .transform((address) -> operation(address, singleAddress)).toList();
        Operation operation;
        ResultProcessor resultProcessor;
        Identifier identifier;
        DataTokenizer dataTokenizer;
        Display display;
        Templates.SuggestionTemplate suggestionTemplate;

        if (singleAddress) {
            operation = operations.get(0);
            int wildcards = countWildcards(operation.getAddress());
            if (wildcards == 0) {
                resultProcessor = new NamesResultProcessor();
                identifier = data -> data.getString(NAME);
                dataTokenizer = data -> data.getString(NAME).split(WHITESPACE);
                display = data -> data.getString(NAME);
                suggestionTemplate = null;

            } else {
                resultProcessor = new NestedResultProcessor(operation);
                identifier = new NestedIdentifier();
                dataTokenizer = new NestedTokenizer();
                display = data -> data.getString(NAME);
                suggestionTemplate = new NestedSuggestionTemplate();
            }

        } else {
            operation = new Composite(operations);
            resultProcessor = new NestedResultProcessor(operation);
            identifier = new NestedIdentifier();
            dataTokenizer = new NestedTokenizer();
            display = data -> data.getString(NAME);
            suggestionTemplate = new NestedSuggestionTemplate();
        }

        return new Typeahead.OperationBuilder(operation, resultProcessor, identifier)
                .dataTokenizer(dataTokenizer)
                .display(display)
                .suggestion(suggestionTemplate)
                .build();
    }

    private Operation operation(ResourceAddress address, boolean singleAddress) {
        Operation operation;
        int wildcards = countWildcards(address);

        if (singleAddress &&
                (wildcards == 0 || (wildcards == 1 && "*".equals(address.lastValue())))) {
            ResourceAddress parent = address.getParent();
            String childName = address.lastName();
            operation = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION, parent)
                    .param(CHILD_TYPE, childName).build();

        } else {
            // The address is something like /foo=*/bar=*
            // Would be nice if we could use
            // /foo=*:read-children-names(child-type=bar)
            // but it returns an empty list, so we're using
            // /foo=*/bar=*:read-resource
            // which makes parsing the response more complicated
            operation = new Operation.Builder(READ_RESOURCE_OPERATION, address)
                    .param(ATTRIBUTES_ONLY, true)
                    .param(INCLUDE_ALIASES, false)
                    .param(INCLUDE_DEFAULTS, false)
                    .param(INCLUDE_RUNTIME, false)
                    .param(PROXIES, false)
                    .build();
        }

        return operation;
    }

    private int countWildcards(ResourceAddress address) {
        int wildcards = 0;
        if (address.isDefined()) {
            for (Property property : address.asPropertyList()) {
                if ("*".equals(property.getValue().asString())) {
                    wildcards++;
                }
            }
        }
        return wildcards;
    }
}
