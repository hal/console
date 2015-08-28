/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.core.dispatch;

import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.JsonType;
import org.jboss.hal.core.dispatch.Dispatcher.HttpMethod;
import org.jboss.hal.dmr.ModelNode;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
class UploadPayloadProcessor implements Dispatcher.PayloadProcessor {

    @Override
    public ModelNode processPayload(final HttpMethod method, final String payload) {
        JsonObject jsonResponse = Json.parse(payload);
        String jsonOutcome = jsonResponse.getString(OUTCOME);

        ModelNode modelNode = new ModelNode();
        modelNode.get(OUTCOME).set(jsonOutcome);

        if (SUCCESS.equals(jsonOutcome)) {
            if (jsonResponse.hasKey(RESULT)) {
                modelNode.get(RESULT).set(jsonResponse.get(RESULT).asString());
            } else {
                modelNode.get(RESULT).set(new ModelNode());
            }
        } else {
            String failure = extractFailure(jsonResponse);
            modelNode.get(FAILURE_DESCRIPTION).set(failure);
        }
        return modelNode;
    }

    private String extractFailure(final JsonObject jsonResponse) {
        String failure = "unknown";
        JsonType type = jsonResponse.get(FAILURE_DESCRIPTION).getType();
        if (type == JsonType.STRING) {
            failure = jsonResponse.getString(FAILURE_DESCRIPTION);
        } else if (type == JsonType.OBJECT) {
            JsonObject failureObject = jsonResponse.getObject(FAILURE_DESCRIPTION);
            for (String key : failureObject.keys()) {
                if (key.contains("failure") && failureObject.getString(key) != null) {
                    failure = failureObject.getString(key);
                    break;
                }
            }
        }
        return failure;
    }
}
