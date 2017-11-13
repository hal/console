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

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.GET;

public class DmrPayloadProcessor implements PayloadProcessor {

    @Override
    public ModelNode processPayload(final HttpMethod method, final String contentType, final String payload) {
        ModelNode node;
        if (contentType.startsWith(Dispatcher.APPLICATION_DMR_ENCODED)) {
            try {
                node = ModelNode.fromBase64(payload);
                if (method == GET && !node.isFailure()) {
                    // For GET request the response is purely the model nodes result. The outcome
                    // is not send as part of the response but expressed with the HTTP status code.
                    // In order to not break existing code, we repackage the payload into a
                    // new model node with an "outcome" and "result" key.
                    // TODO What about response headers?
                    ModelNode repackaged = new ModelNode();
                    repackaged.get(OUTCOME).set(SUCCESS);
                    repackaged.get(RESULT).set(node);
                    node = repackaged;
                }
            } catch (Throwable e) {
                ModelNode err = new ModelNode();
                err.get(OUTCOME).set(FAILED);
                err.get(FAILURE_DESCRIPTION)
                        .set("Failed to decode response: " + e.getClass().getName() + ": " + e.getMessage()); //NON-NLS
                node = err;
            }

        } else {
            node = new ModelNode();
            node.get(OUTCOME).set(FAILED);
            node.get(FAILURE_DESCRIPTION).set(PARSE_ERROR + contentType); //NON-NLS
        }
        return node;
    }
}
