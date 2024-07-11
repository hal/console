/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.installer;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.NamedNode;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

class UpdateItem extends NamedNode {

    enum UpdateType {
        INSTALL, UPDATE, ROLLBACK, CONFIG_CHANGE, UNDEFINED
    }

    UpdateItem(final ModelNode node) {
        super(node.get(HASH).asString(), node);
    }

    List<String> getVersions() {
        if (this.has("channel-versions")) {
            return this.get("channel-versions").asList().stream().map(ModelNode::asString).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    Date getTimestamp() {
        return ModelNodeHelper.failSafeDate(this, TIMESTAMP);
    }

    UpdateType getUpdateKind() {
        return ModelNodeHelper.asEnumValue(this, TYPE, value -> UpdateType.valueOf(value.toUpperCase()),
                UpdateType.UNDEFINED);
    }
}
