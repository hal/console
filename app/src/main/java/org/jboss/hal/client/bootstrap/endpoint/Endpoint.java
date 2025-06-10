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
package org.jboss.hal.client.bootstrap.endpoint;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;

import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PORT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SCHEME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.URL;

class Endpoint extends NamedNode {

    private static final String SELECTED = "selected";

    Endpoint() {
        super("", new ModelNode());
    }

    Endpoint(ModelNode endpoint) {
        super(endpoint);
    }

    public boolean isSelected() {
        return get(SELECTED).asBoolean();
    }

    public void setSelected(boolean selected) {
        get(SELECTED).set(selected);
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
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
