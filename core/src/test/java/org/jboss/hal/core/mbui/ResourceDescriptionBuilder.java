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
package org.jboss.hal.core.mbui;

import java.util.Map;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.description.ResourceDescription;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public class ResourceDescriptionBuilder {

    public ResourceDescription empty() {
        return new ResourceDescription(new ModelNode());
    }

    public ResourceDescription attributes(String... attributes) {
        ModelNode list = new ModelNode();
        if (attributes != null) {
            for (String attribute : attributes) {
                list.get(attribute).set(new ModelNode());
            }
        }
        return new ResourceDescription(new ModelNode().set(ATTRIBUTES, list));
    }

    public ResourceDescription storage(Map<String, String> storageAttributes) {
        ModelNode list = new ModelNode();
        if (storageAttributes != null) {
            for (Map.Entry<String, String> entry : storageAttributes.entrySet()) {
                list.get(entry.getKey()).set(new ModelNode().set(STORAGE, entry.getValue()));
            }
        }
        return new ResourceDescription(new ModelNode().set(ATTRIBUTES, list));
    }

    public ResourceDescription requestProperties(Map<String, Boolean> requestProperties) {
        ModelNode list = new ModelNode();
        if (requestProperties != null) {
            for (Map.Entry<String, Boolean> entry : requestProperties.entrySet()) {
                list.get(entry.getKey()).set(new ModelNode().set(REQUIRED, entry.getValue()));
            }
        }
        return new ResourceDescription(new ModelNode().set(OPERATIONS,
                new ModelNode().set(ADD, new ModelNode().set(REQUEST_PROPERTIES, list))));
    }
}
