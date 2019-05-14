/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.meta.description;

import com.google.gwt.resources.client.TextResource;
import org.jboss.hal.dmr.ModelNode;

public final class StaticResourceDescription {

    private StaticResourceDescription() {
    }

    public static ResourceDescription from(TextResource resource) {
        try {
            return new ResourceDescription(ModelNode.fromBase64(resource.getText()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unable to read static resource description from " + resource.getName());
        }
    }
}
