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
package org.jboss.hal.dmr.dispatch;

import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.JsonType;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.Names.UNKNOWN;

/**
 * @author Harald Pehl
 */
public class UploadPayloadProcessor implements PayloadProcessor {

    private static final String FAILURE = "failure";

    @Override
    public ModelNode processPayload(final HttpMethod method, final String contentType, final String payload) {
        ModelNode node;
        if (contentType.startsWith(Dispatcher.APPLICATION_DMR_ENCODED)) {
            node = ModelNode.fromBase64(payload);

        } else if (contentType.startsWith(Dispatcher.APPLICATION_JSON)) {
            node = new ModelNode();

            JsonObject jsonResponse = Json.parse(payload);
            String jsonOutcome = jsonResponse.getString(OUTCOME);
            node.get(OUTCOME).set(jsonOutcome);

            if (SUCCESS.equals(jsonOutcome)) {
                if (jsonResponse.hasKey(RESULT)) {
                    node.get(RESULT).set(jsonResponse.get(RESULT).asString());
                } else {
                    node.get(RESULT).set(new ModelNode());
                }
                // TODO What about "response-headers"?
            } else {
                String failure = extractFailure(jsonResponse);
                node.get(FAILURE_DESCRIPTION).set(failure);
            }

        } else {
            node = new ModelNode();
            node.get(OUTCOME).set(FAILED);
            node.get(FAILURE_DESCRIPTION).set(PARSE_ERROR + contentType);
        }
        return node;
    }

    private String extractFailure(final JsonObject jsonResponse) {
        String failure = UNKNOWN;
        JsonType type = jsonResponse.get(FAILURE_DESCRIPTION).getType();
        if (type == JsonType.STRING) {
            failure = jsonResponse.getString(FAILURE_DESCRIPTION);
        } else if (type == JsonType.OBJECT) {
            JsonObject failureObject = jsonResponse.getObject(FAILURE_DESCRIPTION);
            for (String key : failureObject.keys()) {
                if (key.contains(FAILURE) && failureObject.getString(key) != null) {
                    failure = failureObject.getString(key);
                    break;
                }
            }
        }
        return failure;
    }
}
