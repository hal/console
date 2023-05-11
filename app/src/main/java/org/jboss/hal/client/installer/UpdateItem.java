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

import java.util.Date;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.NamedNode;

import static org.jboss.hal.dmr.ModelDescriptionConstants.HASH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TIMESTAMP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;

class UpdateItem extends NamedNode {

    enum UpdateType {
        INSTALL, UPDATE, ROLLBACK, UNDEFINED
    }

    UpdateItem(final ModelNode node) {
        super(node.get(HASH).asString(), node);
    }

    Date getTimestamp() {
        return ModelNodeHelper.failSafeDate(this, TIMESTAMP);
    }

    UpdateType getUpdateKind() {
        return ModelNodeHelper.asEnumValue(this, TYPE, value -> UpdateType.valueOf(value.toUpperCase()),
                UpdateType.UNDEFINED);
    }

    boolean isInstall() {
        return getUpdateKind() == UpdateType.INSTALL;
    }

    boolean isUpdate() {
        return getUpdateKind() == UpdateType.UPDATE;
    }

    boolean isRollback() {
        return getUpdateKind() == UpdateType.ROLLBACK;
    }
}
