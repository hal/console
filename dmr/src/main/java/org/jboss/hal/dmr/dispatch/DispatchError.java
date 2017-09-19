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

import org.jboss.hal.dmr.Operation;

public class DispatchError extends Exception {
    private final Operation operation;
    private final int statusCode;

    DispatchError(Operation operation, String message, int statusCode) {
        super(message + " Status Code " + statusCode);
        this.operation = operation;
        this.statusCode = statusCode;
    }

    public Operation getOperation() {
        return operation;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
