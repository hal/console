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

import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ExactlyOneAlternativeValidation;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.form.NotMoreThanOneAlternativeValidation;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;

import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHANNELS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.GAV;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANIFEST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REPOSITORIES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.URL;

class ChannelFormFactory {

    static final String MANIFEST_GAV = MANIFEST + "." + GAV;
    static final String MANIFEST_URL = MANIFEST + "." + URL;

    static Form<ModelNode> channelForm(MetadataRegistry metadataRegistry, Resources resources, boolean add) {
        List<String> manifestAttributes = asList(MANIFEST_GAV, MANIFEST_URL);
        Metadata channelMetadata = metadataRegistry.lookup(INSTALLER_TEMPLATE).forComplexAttribute(CHANNELS);
        ModelNodeForm<ModelNode> form = new ModelNodeForm.Builder<>(Ids.CHANNEL_FORM, channelMetadata)
                .include(NAME, REPOSITORIES)
                .include(manifestAttributes)
                .unsorted()
                .build();
        form.getFormItem(NAME).setEnabled(add);
        // need to set up these manually, since nested properties aren't handled by ModelNodeForm yet.
        form.addFormValidation(
                new ExactlyOneAlternativeValidation<>(manifestAttributes, resources.constants(), resources.messages()));
        form.addFormValidation(new NotMoreThanOneAlternativeValidation<>(manifestAttributes, form,
                resources.constants(), resources.messages()));
        return form;
    }
}
