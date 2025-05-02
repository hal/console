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
package org.jboss.hal.core.runtime.server;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.resources.Ids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.hal.dmr.ModelDescriptionConstants.BOUND_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.BOUND_PORT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PORT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SCHEME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.URL;

public class ServerUrl extends ModelNode {

    private static final Logger logger = LoggerFactory.getLogger(ServerUrl.class);
    private static final String CUSTOM = "custom";

    static ServerUrl fromPayload(String payload) {
        if (payload != null) {
            try {
                ModelNode modelNode = ModelNode.fromBase64(payload);
                return new ServerUrl(modelNode);
            } catch (Throwable t) {
                logger.error("Unable to read server URL from local storage using key '{}': {}",
                        Ids.ENDPOINT_STORAGE, t.getMessage());
            }
        }
        return null;
    }

    static ServerUrl fromManagementModel(Property property) {
        int port = -1;
        String scheme = property.getName();
        String host = property.getValue().get(BOUND_ADDRESS).asString();
        if (property.getValue().hasDefined(BOUND_PORT)) {
            port = property.getValue().get(BOUND_PORT).asInt();
        }
        return new ServerUrl(scheme, host, port);
    }

    private ServerUrl(ModelNode modelNode) {
        set(modelNode);
    }

    private ServerUrl(String scheme, String host, int port) {
        get(SCHEME).set(scheme);
        get(HOST).set(host);
        get(PORT).set(port);
        get(CUSTOM).set(false);
    }

    public String getUrl() {
        if (hasDefined(URL)) {
            return get(URL).asString();
        } else {
            StringBuilder url = new StringBuilder();
            url.append(get(SCHEME).asString()).append("://").append(get(HOST).asString());
            ModelNode port = get(PORT);
            if (!defaultPort()) {
                url.append(":").append(port);
            }
            return url.toString();
        }
    }

    void makeCustom() {
        get(CUSTOM).set(true);
    }

    boolean isCustom() {
        return hasDefined(CUSTOM) && get(CUSTOM).asBoolean();
    }

    private boolean defaultPort() {
        ModelNode port = get(PORT);
        if (port.isDefined()) {
            ModelNode scheme = get(SCHEME);
            if (scheme.isDefined()) {
                if ("https".equals(scheme.asString())) {
                    return port.asInt() == 443;
                } else if ("http".equals(scheme.asString())) {
                    return port.asInt() == 80;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
}
