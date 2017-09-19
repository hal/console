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
import org.jetbrains.annotations.NonNls;

/**
 * @author Heiko Braun
 * @date 9/17/13
 */
public class DispatchException extends RuntimeException {

    static DispatchException statusError(int statusCode, @NonNls String message, Operation operation) {
        return new DispatchException(statusCode, message, operation, null);
    }

    public static DispatchException failedOperation(@NonNls String message, Operation operation) {
        return new DispatchException(500, message, operation, null);
    }

    static DispatchException causedBy(Throwable throwable, Operation operation) {
        return new DispatchException(500, throwable.getMessage(), operation, throwable);
    }


    private final int statusCode;
    private final Operation operation;

    private DispatchException(int statusCode, String message, Operation operation, Throwable throwable) {
        super(message + " Status Code " + statusCode, throwable);
        this.statusCode = statusCode;
        this.operation = operation;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Operation getOperation() {
        return operation;
    }
}
