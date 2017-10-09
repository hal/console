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
package org.jboss.hal.client.configuration.subsystem.undertow;

import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.dmr.ModelDescriptionConstants.AJP_LISTENER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HTTPS_LISTENER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HTTP_LISTENER;

enum Listener {

    AJP(Ids.UNDERTOW_SERVER_AJP_LISTENER, Names.AJP_LISTENER, AJP_LISTENER),
    HTTP(Ids.UNDERTOW_SERVER_HTTP_LISTENER, Names.HTTP_LISTENER, HTTP_LISTENER),
    HTTPS(Ids.UNDERTOW_SERVER_HTTPS_LISTENER, Names.HTTPS_LISTENER, HTTPS_LISTENER);

    final String baseId;
    final String type;
    final String resource;

    Listener(final String baseId, final String type, final String resource) {
        this.baseId = baseId;
        this.type = type;
        this.resource = resource;
    }
}
