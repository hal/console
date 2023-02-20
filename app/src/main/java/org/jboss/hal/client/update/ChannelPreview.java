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
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.resources.Names;

import static java.util.stream.Collectors.joining;

public class ChannelPreview extends PreviewContent<NamedNode> {

    public ChannelPreview(NamedNode channel) {
        super(channel.getName());

        LabelBuilder labelBuilder = new LabelBuilder();
        PreviewAttributes<NamedNode> attributes = new PreviewAttributes<>(channel);
        attributes.append(model -> {
            String repositories = model.get("repositories").asList().stream().map(repo -> repo.get("url").asString())
                    .collect(joining(", "));
            return new PreviewAttribute(labelBuilder.label("repositories"), repositories);
        });
        attributes.append(model -> {
            String manifestValue;
            ModelNode manifest = model.get("manifest");
            if (manifest.hasDefined("gav")) {
                manifestValue = manifest.get("gav").asString();
            } else if (manifest.hasDefined("url")) {
                manifestValue = manifest.get("url").asString();
            } else {
                manifestValue = Names.NOT_AVAILABLE;
            }
            return new PreviewAttribute(labelBuilder.label("manifest"), manifestValue);
        });
        previewBuilder().addAll(attributes);
    }
}
