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
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.resources.Names;

import static java.util.stream.Collectors.toList;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public class Channel extends NamedNode {

    Channel(final ModelNode node) {
        super(node);
    }

    String getManifest() {
        ModelNode manifest = get(MANIFEST);
        if (manifest.isDefined()) {
            if (manifest.hasDefined(GAV)) {
                return manifest.get(GAV).asString();
            } else if (manifest.hasDefined(URL)) {
                return manifest.get(URL).asString();
            } else {
                return Names.NOT_AVAILABLE;
            }
        } else {
            return Names.NOT_AVAILABLE;
        }
    }

    String getManifestType() {
        if (isGAV()) {
            return Names.GAV;
        } else if (isURL()) {
            return Names.URL;
        } else {
            return Names.UNKNOWN;
        }
    }

    List<String> getRepositories() {
        return get(REPOSITORIES).asList().stream().map(repo -> repo.get(URL).asString()).collect(toList());
    }

    boolean isGAV() {
        ModelNode modelNode = get(MANIFEST);
        return modelNode.isDefined() && modelNode.hasDefined(GAV);
    }

    boolean isURL() {
        ModelNode modelNode = get(MANIFEST);
        return modelNode.isDefined() && modelNode.hasDefined(URL);
    }
}
