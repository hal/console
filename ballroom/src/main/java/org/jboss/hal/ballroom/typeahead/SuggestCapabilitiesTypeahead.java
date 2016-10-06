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

import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.meta.StatementContext.Tuple.DOMAIN_CONTROLLER;

/**
 * A typeahead implementation which is backed by the {@code :suggest-capabilities} operation. Uses the address given at
 * construction time as {@code dependent-address} parameter.
 *
 * @author Harald Pehl
 */
public class SuggestCapabilitiesTypeahead extends Typeahead {

    private static final AddressTemplate CAPABILITY_REGISTRY =
            AddressTemplate.of(DOMAIN_CONTROLLER, "core-service=capability-registry");

    private final Operation operation;

    public SuggestCapabilitiesTypeahead(final StatementContext statementContext, final String capability,
            final AddressTemplate template) {
        //noinspection HardCodedStringLiteral
        operation = new Operation.Builder("suggest-capabilities", CAPABILITY_REGISTRY.resolve(statementContext))
                .param(NAME, capability)
                .param("dependent-address", template.resolve(statementContext))
                .build();

        options = initOptions();
        bloodhound = initBloodhound(data -> data.getString(NAME), data -> data.getString(NAME).split(WHITESPACE),
                () -> operation, new NamesResultProcessor());
        dataset = initDataset(data -> data.getString(NAME), null);
    }
}
