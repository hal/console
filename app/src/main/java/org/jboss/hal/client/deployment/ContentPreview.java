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
package org.jboss.hal.client.deployment;

import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.deployment.ContentColumn.serverGroups;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXPLODED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RUNTIME_NAME;

/**
 * @author Harald Pehl
 */
class ContentPreview extends PreviewContent<Content> {

    ContentPreview(final ContentColumn column, final Content content, final Resources resources) {
        super(content.getName());

        if (content.getAssignments().isEmpty()) {
            previewBuilder().add(
                    new Alert(Icons.DISABLED, resources.messages().unassignedContent(content.getName()),
                            resources.constants().assign(), event -> column.assign(content)));

        } else {
            previewBuilder().add(
                    new Alert(Icons.INFO,
                            resources.messages().assignedToDescription(content.getName(), serverGroups(content))));
        }

        PreviewAttributes<Content> attributes = new PreviewAttributes<>(content)
                .append(NAME)
                .append(RUNTIME_NAME)
                .append(MANAGED)
                .append(EXPLODED)
                .end();

        previewBuilder().addAll(attributes);
    }
}
