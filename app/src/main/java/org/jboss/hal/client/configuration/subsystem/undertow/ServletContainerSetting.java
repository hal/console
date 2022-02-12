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
package org.jboss.hal.client.configuration.subsystem.undertow;

import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CRAWLER_SESSION_MANAGEMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PERSISTENT_SESSIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SESSION_COOKIE;
import static org.jboss.hal.resources.CSS.fontAwesome;

enum ServletContainerSetting {

    COOKIE(Ids.UNDERTOW_SERVLET_CONTAINER_COOKIE, Names.COOKIES, SESSION_COOKIE, fontAwesome("birthday-cake")), CRAWLER(
            Ids.UNDERTOW_SERVLET_CONTAINER_CRAWLER, Names.CRAWLER, CRAWLER_SESSION_MANAGEMENT,
            fontAwesome("search")), JSP(Ids.UNDERTOW_SERVLET_CONTAINER_JSP, Names.JSP, ModelDescriptionConstants.JSP,
                    fontAwesome("code")), SESSIONS(Ids.UNDERTOW_SERVLET_CONTAINER_SESSION, Names.SESSIONS, PERSISTENT_SESSIONS,
                            fontAwesome("id-card-o")), WEBSOCKETS(Ids.UNDERTOW_SERVLET_CONTAINER_WEBSOCKET, Names.WEBSOCKETS,
                                    ModelDescriptionConstants.WEBSOCKETS,
                                    fontAwesome("exchange"));

    final String baseId;
    final String type;
    final String resource;
    final String icon;

    ServletContainerSetting(String baseId, String type, String resource, String icon) {
        this.baseId = baseId;
        this.type = type;
        this.resource = resource;
        this.icon = icon;
    }

    String templateSuffix() {
        return "setting=" + resource; // NON-NLS
    }

    String path() {
        return "setting/" + resource; // NON-NLS
    }
}
