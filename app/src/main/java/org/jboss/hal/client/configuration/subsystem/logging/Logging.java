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
package org.jboss.hal.client.configuration.subsystem.logging;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.dmr.ModelDescriptionConstants.FILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELATIVE_TO;

/** Common code used by both the general logging configuration and the logging profile configuration. */
final class Logging {

    /** Helper method to build the filename using the nested ({@code relative-to/path}) attributes. */
    static String getFilename(NamedNode node) {
        String filename = Names.NOT_AVAILABLE;
        if (node.hasDefined(FILE) && node.get(FILE).hasDefined(PATH)) {
            ModelNode file = node.get(FILE);
            if (file.hasDefined(RELATIVE_TO)) {
                filename = file.get(RELATIVE_TO).asString() + "/" + file.get(PATH).asString();
            } else {
                filename = file.get(PATH).asString();
            }
        }
        return filename;
    }

    private Logging() {
    }
}
