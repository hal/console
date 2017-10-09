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
package org.jboss.hal.client.configuration.subsystem.webservice;

import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.resources.Names;

/** Enum struct for the pre and post handler chains of a webservices configuration. */
enum HandlerChain {

    PRE_HANDLER_CHAIN(Names.PRE_HANDLER_CHAIN, ModelDescriptionConstants.PRE_HANDLER_CHAIN),
    POST_HANDLER_CHAIN(Names.POST_HANDLER_CHAIN, ModelDescriptionConstants.POST_HANDLER_CHAIN),;

    final String type;
    final String resource;

    HandlerChain(final String type, final String resource) {
        this.type = type;
        this.resource = resource;
    }
}
