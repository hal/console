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
package org.jboss.hal.client.update;

import java.util.Date;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.NamedNode;

import static org.jboss.hal.dmr.ModelDescriptionConstants.HASH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TIMESTAMP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;

public class HistoryItem extends NamedNode {

    public enum HistoryType {
        INSTALL, UPDATE, ROLLBACK, UNDEFINED
    }

    public HistoryItem(final ModelNode node) {
        super(node.get(HASH).asString(), node);
    }

    public Date getTimestamp() {
        return ModelNodeHelper.failSafeDate(this, TIMESTAMP);
    }

    public HistoryType getHistoryKind() {
        return ModelNodeHelper.asEnumValue(this, TYPE, value -> HistoryType.valueOf(value.toUpperCase()),
                HistoryType.UNDEFINED);
    }

    public boolean isInstall() {
        return getHistoryKind() == HistoryType.INSTALL;
    }

    public boolean isUpdate() {
        return getHistoryKind() == HistoryType.UPDATE;
    }

    public boolean isRollback() {
        return getHistoryKind() == HistoryType.ROLLBACK;
    }
}
