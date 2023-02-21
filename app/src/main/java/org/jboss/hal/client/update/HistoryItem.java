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

import static org.jboss.hal.dmr.ModelDescriptionConstants.DATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.KIND;

public class HistoryItem extends NamedNode {

    public enum Kind {
        INSTALL, UPDATE, ROLLBACK, UNDEFINED
    }

    public HistoryItem(final String name, final ModelNode node) {
        super(name, node);
    }

    public Date getDate() {
        return ModelNodeHelper.failSafeDate(this, DATE);
    }

    public Kind getKind() {
        return ModelNodeHelper.asEnumValue(this, KIND, value -> Kind.valueOf(value.toUpperCase()),
                Kind.UNDEFINED);
    }

    public boolean isInstall() {
        return getKind() == Kind.INSTALL;
    }

    public boolean isUpdate() {
        return getKind() == Kind.UPDATE;
    }

    public boolean isRollback() {
        return getKind() == Kind.ROLLBACK;
    }
}
