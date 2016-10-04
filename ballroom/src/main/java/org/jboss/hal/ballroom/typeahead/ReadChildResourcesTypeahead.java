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

import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.jboss.hal.ballroom.typeahead.Templates.SuggestionTemplate;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * A typeahead implementation which uses the specified address templates to read the child resource names.
 *
 * @author Harald Pehl
 */
public class ReadChildResourcesTypeahead extends Typeahead {

    public ReadChildResourcesTypeahead(final AddressTemplate template, final StatementContext statementContext) {
        this(singleton(template), statementContext);
    }

    public ReadChildResourcesTypeahead(final Iterable<AddressTemplate> templates, final StatementContext statementContext) {
        verifyTemplates(templates);

        ResultProcessor resultProcessor;
        Identifier identifier;
        DataTokenizer dataTokenizer;
        Display display;
        SuggestionTemplate suggestionTemplate;

        int numberOfTemplates = Iterables.size(templates);
        if (numberOfTemplates == 1) {
            AddressTemplate template = templates.iterator().next();
            int wildcards = Iterables.size(Splitter.on('*').split(template.toString())) - 1;
            if (wildcards == 0 || (wildcards == 1 && "*".equals(template.lastValue()))) {
                resultProcessor = new NamesResultProcessor();
                identifier = data -> data.getString(NAME);
                dataTokenizer = data -> data.getString(NAME).split(WHITESPACE);
                display = data -> data.getString(NAME);
                suggestionTemplate = null;

            } else {
                resultProcessor = new NestedResultProcessor(false);
                identifier = new NestedIdentifier();
                dataTokenizer = new NestedTokenizer();
                display = data -> data.getString(NAME);
                suggestionTemplate = new NestedSuggestionTemplate();
            }

        } else {
            resultProcessor = new NestedResultProcessor(true);
            identifier = new NestedIdentifier();
            dataTokenizer = new NestedTokenizer();
            display = data -> data.getString(NAME);
            suggestionTemplate = new NestedSuggestionTemplate();
        }

        options = initOptions();
        bloodhound = initBloodhound(identifier, dataTokenizer, () -> {
                    List<Operation> operations = stream(templates.spliterator(), false)
                            .map(template -> template.resolve(statementContext))
                            .map(address -> operation(address, numberOfTemplates))
                            .collect(toList());

                    return operations.size() == 1 ? operations.get(0) : new Composite(operations);
                }, resultProcessor
        );
        dataset = initDataset(display, suggestionTemplate);
    }

    private void verifyTemplates(final Iterable<AddressTemplate> templates) {
        if (Iterables.isEmpty(templates)) {
            throw new IllegalArgumentException(
                    "Templates must not be empty in Typeahead(List<AddressTemplate>, StatementContext)");
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
}
