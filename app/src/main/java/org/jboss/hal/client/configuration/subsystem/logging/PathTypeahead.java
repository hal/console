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
package org.jboss.hal.client.configuration.subsystem.logging;

import javax.inject.Provider;

import org.jboss.hal.ballroom.typeahead.NamesResultProcessor;
import org.jboss.hal.ballroom.typeahead.Typeahead;
import org.jboss.hal.dmr.model.Operation;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * @author Harald Pehl
 */
class PathTypeahead extends Typeahead {

    protected PathTypeahead(final Provider<Operation> operation) {
        super(data -> data.getString(NAME), data -> data.getString(NAME).split(WHITESPACE), new NamesResultProcessor(),
                data -> data.getString(NAME), null, operation);
    }
}
