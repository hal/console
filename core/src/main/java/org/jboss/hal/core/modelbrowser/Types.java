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
package org.jboss.hal.core.modelbrowser;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;

final class Types {

    private Types() {
    }

    static String formatType(ModelNode hasType) {
        StringBuilder builder = new StringBuilder();
        if (hasType.hasDefined(TYPE)) {
            builder.append(hasType.get(TYPE).asString());
            if (hasType.hasDefined(VALUE_TYPE)) {
                ModelNode node = hasType.get(VALUE_TYPE);
                if (ModelType.TYPE.equals(node.getType())) {
                    builder.append("<").append(node.asString()).append(">");
                }
            }
        } else {
            builder.append(Names.NOT_AVAILABLE);
        }
        return builder.toString();
    }
}
