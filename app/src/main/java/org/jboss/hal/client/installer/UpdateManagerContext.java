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

import com.google.common.base.Strings;

import static java.util.Collections.emptyList;

/** Common context used by update manager wizard */
class UpdateManagerContext {

    UpdateType updateType = UpdateType.ONLINE;

    boolean prepared;
    String revision;
    List<ModelNode> updates;
    String workDir;
    boolean isPropertiesFormBuilt = false;
    ModelNode properties = new ModelNode();
    elemental2.dom.FileList mavenReposForRevert;

    UpdateManagerContext() {
        this(null, emptyList());
    }

    UpdateManagerContext(final String revision, List<ModelNode> updates) {
        this.revision = revision;
        this.updates = updates;
    }

    public boolean isRevert() {
        return !Strings.isNullOrEmpty(revision);
    }

    public boolean hasMavenReposForRevert() {
        return mavenReposForRevert != null && mavenReposForRevert.length > 0;
    }

    void reset() {
        prepared = false;
        revision = "";
        updates = emptyList();
        workDir = "";
        isPropertiesFormBuilt = false;
        properties = new ModelNode();
        mavenReposForRevert = null;
    }
}
