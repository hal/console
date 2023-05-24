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
package org.jboss.hal.client.patching;

import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.dmr.ModelNode;

import static java.util.Arrays.asList;

import static org.jboss.hal.dmr.ModelDescriptionConstants.APPLIED_AT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATCH_ID;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;

class PatchesPreview extends PreviewContent<ModelNode> {

    PatchesPreview(final ModelNode patchNode) {
        super(patchNode.get(PATCH_ID).asString());

        PreviewAttributes<ModelNode> attributes = new PreviewAttributes<>(patchNode,
                asList(PATCH_ID, TYPE, APPLIED_AT));
        previewBuilder().addAll(attributes);
    }
}
