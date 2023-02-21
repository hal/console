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

import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.resources.Names;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.GAV;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANIFEST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REPOSITORIES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.URL;

public class Channel extends NamedNode {

    public Channel(final ModelNode node) {
        super(node);
    }

    public String getManifest() {
        ModelNode modelNode = get(MANIFEST);
        if (modelNode.isDefined()) {
            if (modelNode.hasDefined(GAV)) {
                return modelNode.get(GAV).asString();
            } else if (modelNode.hasDefined(URL)) {
                return modelNode.get(URL).asString();
            } else {
                return Names.NOT_AVAILABLE;
            }
        } else {
            return Names.NOT_AVAILABLE;
        }
    }

    public String getManifestType() {
        if (isGAV()) {
            return Names.GAV;
        } else if (isURL()) {
            return Names.URL;
        } else {
            return Names.UNKNOWN;
        }
    }

    public List<String> getRepositories() {
        return get(REPOSITORIES).asList().stream().map(repo -> repo.get(URL).asString()).collect(toList());
    }

    public boolean isGAV() {
        ModelNode modelNode = get(MANIFEST);
        return modelNode.isDefined() && modelNode.hasDefined(GAV);
    }

    public boolean isURL() {
        ModelNode modelNode = get(MANIFEST);
        return modelNode.isDefined() && modelNode.hasDefined(URL);
    }
}
