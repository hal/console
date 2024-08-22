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
package org.jboss.hal.client.deployment.dialog;

import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.configuration.PathsAutoComplete;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.AttributeCollection;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public class AddUnmanagedDialog {

    private final AddResourceDialog dialog;

    public AddUnmanagedDialog(Metadata metadata, Resources resources, AddResourceDialog.Callback callback) {
        AttributeCollection rp = metadata.getDescription().requestProperties();
        // the "path" attribute requires "archive", but archive may be false, that is a directory deployment
        // but the validation will not let pass, so remove the "requires" and manually set the value if user sets it
        rp.get(CONTENT + "." + PATH).remove(REQUIRES);
        ModelNode attributes = new ModelNode();
        attributes.get(RUNTIME_NAME).set(rp.get(RUNTIME_NAME));
        attributes.get(PATH).set(rp.get(CONTENT + "." + PATH));
        attributes.get(RELATIVE_TO).set(rp.get(CONTENT + "." + RELATIVE_TO));
        attributes.get(ARCHIVE).set(rp.get(CONTENT + "." + ARCHIVE));
        ModelNode description = new ModelNode();
        description.get(ATTRIBUTES).set(attributes);

        Metadata unmanagedMeta = Metadata.staticDescription(new ResourceDescription(description));
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.UNMANAGED_FORM, unmanagedMeta)
                .unboundFormItem(new NameItem(), 0)
                .include(RUNTIME_NAME, PATH, RELATIVE_TO, ARCHIVE)
                .unsorted()
                .addOnly()
                .build();
        form.getFormItem(PATH).setRequired(true);
        form.getFormItem(RELATIVE_TO).registerSuggestHandler(new PathsAutoComplete());
        dialog = new AddResourceDialog(
                resources.messages().addResourceTitle(Names.UNMANAGED_DEPLOYMENT), form,
                (name, model) -> {
                    if (model != null) {
                        // assemble the payload using the provided model node from the form
                        ModelNode payload = new ModelNode();
                        payload.get(RUNTIME_NAME).set(model.get(RUNTIME_NAME));
                        model.remove(RUNTIME_NAME);
                        if (!model.hasDefined(ARCHIVE)) {
                            model.get(ARCHIVE).set(false);
                        }
                        payload.get(CONTENT).set(model);
                        callback.onAdd(name, payload);
                    }
                });
    }

    public Form<ModelNode> getForm() {
        return dialog.getForm();
    }

    public void show() {
        dialog.show();
    }
}
