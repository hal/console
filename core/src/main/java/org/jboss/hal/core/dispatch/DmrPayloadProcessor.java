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

import org.jboss.hal.core.dispatch.Dispatcher.HttpMethod;
import org.jboss.hal.dmr.ModelNode;

import static org.jboss.hal.core.dispatch.Dispatcher.HttpMethod.GET;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
class DmrPayloadProcessor implements PayloadProcessor {

    @Override
    public ModelNode processPayload(final HttpMethod method, final String payload) {
        ModelNode response;
        try {
            response = ModelNode.fromBase64(payload);
            if (method == GET) {
                // For GET request the response is purely the model nodes result. The outcome
                // is not send as part of the response but expressed with the HTTP status code.
                // In order to not break existing code, we repackage the payload into a
                // new model node with an "outcome" and "result" key.
                ModelNode repackaged = new ModelNode();
                repackaged.get(OUTCOME).set(SUCCESS);
                repackaged.get(RESULT).set(response);
                response = repackaged;
            }
        } catch (Throwable e) {
            ModelNode err = new ModelNode();
            err.get(OUTCOME).set(FAILED);
            err.get(FAILURE_DESCRIPTION)
                    .set("Failed to decode response: " + e.getClass().getName() + ": " + e.getMessage());
            response = err;
        }
        return response;
    }
}
