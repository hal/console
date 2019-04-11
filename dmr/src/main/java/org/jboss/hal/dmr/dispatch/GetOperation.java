/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.dmr.dispatch;

import java.util.HashMap;
import java.util.Map;

import org.jboss.hal.dmr.ModelDescriptionConstants;

@SuppressWarnings("HardCodedStringLiteral")
enum GetOperation {
    /*
     *  It is essential that the GET requests exposed over the HTTP interface are for read only
     *  operations that do not modify the domain model or update anything server side.
     */
    READ_RESOURCE_OPERATION(ModelDescriptionConstants.READ_RESOURCE_OPERATION, "resource"),
    READ_ATTRIBUTE_OPERATION(ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION, "attribute"),
    READ_RESOURCE_DESCRIPTION_OPERATION(ModelDescriptionConstants.READ_RESOURCE_DESCRIPTION_OPERATION,
            "resource-description"),
    READ_CONTENT(ModelDescriptionConstants.READ_CONTENT, "read-content");

    private static Map<String, GetOperation> lookup = new HashMap<>();

    static {
        for (GetOperation getOperation : GetOperation.values()) {
            lookup.put(getOperation.dmrOperation, getOperation);
        }
    }

    static boolean isSupported(String operation) {
        return lookup.containsKey(operation);
    }

    static GetOperation get(String operation) {
        return lookup.get(operation);
    }

    private String dmrOperation;
    private String httpGetOperation;

    GetOperation(String dmrOperation, String httpGetOperation) {
        this.dmrOperation = dmrOperation;
        this.httpGetOperation = httpGetOperation;
    }

    String httpGetOperation() {
        return httpGetOperation;
    }
}
