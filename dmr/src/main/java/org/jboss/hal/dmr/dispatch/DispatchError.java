/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.dmr.dispatch;

import org.jboss.hal.dmr.Operation;

/** Exception caused by a communication error or another exception not directly related to the DMR operation. */
public class DispatchError extends RuntimeException {

    private final int statusCode;
    private final Operation operation;

    public DispatchError(Throwable throwable, Operation operation) {
        super(throwable);
        this.statusCode = 500;
        this.operation = operation;
    }

    public DispatchError(int statusCode, String message, Operation operation) {
        super(message);
        this.statusCode = statusCode;
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "DispatchError(" + statusCode + ": " + getMessage() + ")";
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Operation getOperation() {
        return operation;
    }
}
