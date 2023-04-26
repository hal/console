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

import java.util.List;

import org.jboss.hal.dmr.ModelNode;

/** Common context used by all installer wizards */
public class InstallerContext {

    public boolean prepared;
    public final UpdateItem updateItem;
    public final List<ModelNode> updates;

    public InstallerContext(final List<ModelNode> updates) {
        this(updates, null);
    }

    public InstallerContext(final List<ModelNode> updates, final UpdateItem updateItem) {
        this.updateItem = updateItem;
        this.updates = updates;
    }
}