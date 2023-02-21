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

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.finder.PreviewContent;

import static org.jboss.hal.dmr.ModelDescriptionConstants.MANIFEST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REPOSITORIES;

public class ChannelPreview extends PreviewContent<Channel> {

    public ChannelPreview(Channel channel) {
        super(channel.getName());

        LabelBuilder labelBuilder = new LabelBuilder();
        PreviewAttributes<Channel> attributes = new PreviewAttributes<>(channel);
        attributes.append(model -> new PreviewAttribute(labelBuilder.label(REPOSITORIES),
                String.join(", ", channel.getRepositories())));
        attributes.append(model -> new PreviewAttribute(labelBuilder.label(MANIFEST),
                channel.getManifestType() + ": " + channel.getManifest()));
        previewBuilder().addAll(attributes);
    }
}
