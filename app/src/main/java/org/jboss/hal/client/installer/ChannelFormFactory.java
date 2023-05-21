package org.jboss.hal.client.installer;

import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ExactlyOneAlternativeValidation;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.form.NotMoreThanOneAlternativeValidation;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import java.util.List;

import static java.util.Arrays.asList;
import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

class ChannelFormFactory {

    private static final String MANIFEST_GAV = MANIFEST + "." + GAV;
    private static final String MANIFEST_URL = MANIFEST + "." + URL;

    static Form<ModelNode> channelForm(MetadataRegistry metadataRegistry, Resources resources) {
        List<String> manifestAttributes = asList(MANIFEST_GAV, MANIFEST_URL);
        Metadata channelMetadata = metadataRegistry.lookup(INSTALLER_TEMPLATE).forComplexAttribute(CHANNELS);
        ModelNodeForm<ModelNode> form = new ModelNodeForm.Builder<>(Ids.CHANNEL_FORM, channelMetadata)
                .include(NAME, REPOSITORIES)
                .include(manifestAttributes)
                .unsorted()
                .build();
        form.getFormItem(NAME).setRequired(true);
        // need to set up these manually, since nested properties aren't handled by ModelNodeForm yet.
        form.addFormValidation(
                new ExactlyOneAlternativeValidation<>(manifestAttributes, resources.constants(), resources.messages()));
        form.addFormValidation(new NotMoreThanOneAlternativeValidation<>(manifestAttributes, form,
                resources.constants(), resources.messages()));
        return form;
    }
}
