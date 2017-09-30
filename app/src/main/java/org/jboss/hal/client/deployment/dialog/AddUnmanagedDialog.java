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
package org.jboss.hal.client.deployment.dialog;

import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.client.configuration.PathsAutoComplete;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public class AddUnmanagedDialog {

    private final AddResourceDialog dialog;

    public AddUnmanagedDialog(Metadata metadata, Resources resources, AddResourceDialog.Callback callback) {
        ModelNode rp = ModelNodeHelper.failSafeGet(metadata.getDescription(),
                String.join("/", OPERATIONS, ADD, REQUEST_PROPERTIES));
        ModelNode vt = ModelNodeHelper.failSafeGet(rp, CONTENT + "/" + VALUE_TYPE);
        ModelNode attributes = new ModelNode();
        attributes.get(RUNTIME_NAME).set(rp.get(RUNTIME_NAME));
        attributes.get(PATH).set(vt.get(PATH));
        attributes.get(RELATIVE_TO).set(vt.get(RELATIVE_TO));
        attributes.get(ARCHIVE).set(vt.get(ARCHIVE));
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
                        payload.get(CONTENT).set(model);
                        callback.onAdd(name, payload);
                    }
                });
    }

    public void show() {
        dialog.show();
    }
}
