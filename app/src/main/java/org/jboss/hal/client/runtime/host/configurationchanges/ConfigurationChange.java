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
package org.jboss.hal.client.runtime.host.configurationchanges;

import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public class ConfigurationChange extends NamedNode {

    private Boolean composite;

    public ConfigurationChange(ModelNode model) {
        super(model.hasDefined(DOMAIN_UUID) ? model.get(DOMAIN_UUID).asString() : model.get(OPERATION_DATE).asString(), model);

        model.get(OPERATIONS).asList().forEach(nestedNode -> {
            composite = nestedNode.get(OPERATION).asString().equals(COMPOSITE);
        });
        int length = get(OPERATIONS).asString().length();
        get(HAL_LENGTH).set(length);
    }

    String getOperationDate() {
        return get(OPERATION_DATE).asString();
    }

    int getOperationsLength() {
        return get(HAL_LENGTH).asInt();
    }

    String getAccessMechanism() {
        return get(ACCESS_MECHANISM).asString();
    }

    String getRemoteAddress() {
        return get(REMOTE_ADDRESS).asString();
    }

    boolean isSuccess() {
        return get(OUTCOME).asString().equals(SUCCESS);
    }

    String getOutcome() {
        return get(OUTCOME).asString();
    }

    boolean isComposite() {
        return composite;
    }

    List<ModelNode> changes() {
        List<ModelNode> changes;
        if (composite) {
            changes = get(OPERATIONS).asList().get(0).get(STEPS).asList();
        } else {
            changes = get(OPERATIONS).asList();
        }

        return changes;
    }

}
