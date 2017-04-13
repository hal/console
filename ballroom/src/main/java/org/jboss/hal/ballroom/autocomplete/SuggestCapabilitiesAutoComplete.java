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
package org.jboss.hal.ballroom.autocomplete;

import java.util.List;

import elemental.js.util.JsArrayOf;
import org.jboss.hal.ballroom.JsHelper;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPENDENT_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUGGEST_CAPABILITIES;
import static org.jboss.hal.meta.StatementContext.Tuple.DOMAIN_CONTROLLER;

/**
 * @author Harald Pehl
 */
public class SuggestCapabilitiesAutoComplete extends AutoComplete {

    private static final AddressTemplate CAPABILITY_REGISTRY =
            AddressTemplate.of(DOMAIN_CONTROLLER, "core-service=capability-registry");
    private static final String ERROR_MESSAGE = "Unable to read capability suggestions for {} from {}: {}";

    public SuggestCapabilitiesAutoComplete(final Dispatcher dispatcher, final StatementContext statementContext,
            final String capability, final AddressTemplate template) {

        Operation operation = new Operation.Builder(SUGGEST_CAPABILITIES, CAPABILITY_REGISTRY.resolve(statementContext))
                .param(NAME, capability)
                .param(DEPENDENT_ADDRESS, template.resolve(statementContext))
                .build();

        Options options = new OptionsBuilder<String>(
                (query, response) -> dispatcher.execute(operation,
                        result -> {
                            if (result.isDefined()) {
                                List<String> items = result.asList().stream()
                                        .map(ModelNode::asString)
                                        .filter(value -> SHOW_ALL_VALUE.equals(query) ||
                                                value.toLowerCase().contains(query.toLowerCase()))
                                        .collect(toList());
                                response.response(JsHelper.asJsArray(items));
                            } else {
                                response.response(JsArrayOf.create());
                            }
                        },
                        (op, failure) -> {
                            logger.error(ERROR_MESSAGE, capability, template, failure);
                            response.response(JsArrayOf.create());
                        },
                        (op, exception) -> {
                            logger.error(ERROR_MESSAGE, capability, template, exception.getMessage());
                            response.response(JsArrayOf.create());
                        }))
                .build();

        init(options);
    }
}
