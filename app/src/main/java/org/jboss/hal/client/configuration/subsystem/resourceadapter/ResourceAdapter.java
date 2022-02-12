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
package org.jboss.hal.client.configuration.subsystem.resourceadapter;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ARCHIVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MODULE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TRANSACTION_SUPPORT;

class ResourceAdapter extends NamedNode {

    ResourceAdapter(final Property property) {
        super(property);
    }

    ResourceAdapter(final String name, final ModelNode node) {
        super(name, node);
    }

    AdapterType getAdapterType() {
        if (hasDefined(ARCHIVE)) {
            return AdapterType.ARCHIVE;
        } else if (hasDefined(MODULE)) {
            return AdapterType.MODULE;
        }
        return AdapterType.UNKNOWN;
    }

    boolean hasTransactionSupport() {
        return hasDefined(TRANSACTION_SUPPORT);
    }

    String getArchive() {
        return hasDefined(ARCHIVE) ? get(ARCHIVE).asString() : null;
    }

    String getModule() {
        return hasDefined(MODULE) ? get(MODULE).asString() : null;
    }

    enum AdapterType {
        ARCHIVE(Names.ARCHIVE.toLowerCase()), MODULE(Names.MODULE.toLowerCase()), UNKNOWN(Names.NOT_AVAILABLE);

        private final String text;

        AdapterType(final String text) {
            this.text = text;
        }

        public String text() {
            return text;
        }
    }
}
