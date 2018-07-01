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
package org.jboss.hal.client.configuration.subsystem.infinispan;

import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.client.configuration.subsystem.infinispan.Table.BINARY;
import static org.jboss.hal.client.configuration.subsystem.infinispan.Table.STRING;

/** Represents the different store singletons of a specific cache resource. */
public enum Store {

    BINARY_JDBC(Ids.CACHE_STORE_BINARY_JDBC, Names.BINARY_JDBC, ModelDescriptionConstants.BINARY_JDBC, true, BINARY),
    CUSTOM(Ids.CACHE_STORE_CUSTOM, Names.CUSTOM, ModelDescriptionConstants.CUSTOM, true),
    FILE(Ids.CACHE_STORE_FILE, Names.FILE, ModelDescriptionConstants.FILE, false),
    HOTROD(Ids.CACHE_STORE_HOTROd, Names.HOTROD, ModelDescriptionConstants.HOTROD, true),
    JDBC(Ids.CACHE_STORE_JDBC, Names.JDBC, ModelDescriptionConstants.JDBC, true, STRING),
    MIXED_JDBC(Ids.CACHE_STORE_MIXED_JDBC, Names.MIXED_JDBC, ModelDescriptionConstants.MIXED_JDBC, true,
            BINARY, STRING);

    static Store fromResource(String resource) {
        if (resource != null) {
            // STORE is an alias for custom
            if (resource.equalsIgnoreCase("STORE")) { //NON-NLS
                return CUSTOM;
            } else {
                for (Store store : Store.values()) {
                    if (store.resource.equalsIgnoreCase(resource)) {
                        return store;
                    }
                }
            }
        }
        return null;
    }

    final String baseId;
    final String type;
    final String resource;
    final boolean addWithDialog;
    final Table[] tables;

    Store(String baseId, String type, String resource, boolean addWithDialog, Table... tables) {
        this.baseId = baseId;
        this.type = type;
        this.resource = resource;
        this.addWithDialog = addWithDialog;
        this.tables = tables;
    }
}
