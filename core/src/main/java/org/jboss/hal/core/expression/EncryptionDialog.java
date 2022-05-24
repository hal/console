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
package org.jboss.hal.core.expression;

import java.util.List;

import org.jboss.hal.ballroom.autocomplete.StaticAutoComplete;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Resources;

import com.google.gwt.core.client.GWT;

import static org.jboss.elemento.Elements.p;

public class EncryptionDialog {

    private static final ExpressionResources RESOURCES = GWT.create(ExpressionResources.class);

    private final ExpressionEncryptor expressionEncryptor;

    private final Form<ModelNode> form;
    private final Dialog dialog;

    private final boolean empty;

    public EncryptionDialog(ExpressionEncryptor expressionEncryptor, Resources resources) {
        this(expressionEncryptor, resources, null, false);
    }

    public EncryptionDialog(ExpressionEncryptor expressionEncryptor, Resources resources, List<String> resolverNames,
            boolean hasDefaultResolver) {
        this.expressionEncryptor = expressionEncryptor;

        Metadata metadata = Metadata.staticDescription(RESOURCES.expressionEncryption());
        form = new ModelNodeForm.Builder<>("encrypt-expression-form", metadata)
                .addOnly()
                .onSave((f, changedValues) -> encrypt(f.getModel()))
                .build();

        Dialog.Builder dialogBuilder = new Dialog.Builder(resources.constants().resolveExpression());
        empty = resolverNames == null;
        if (!empty) {

            StaticAutoComplete resolverAutoComplete = new StaticAutoComplete(resolverNames);
            form.getFormItem("resolver").registerSuggestHandler(resolverAutoComplete);

            if (!hasDefaultResolver) {
                // make resolver explicitly required if default-resolver is not set in the encryption model
                form.getFormItem("resolver").setRequired(true);
            }

            dialogBuilder.add(form.element())
                    .primary(resources.constants().save(), () -> form.save())
                    .secondary(resources.constants().close(), () -> true);
        } else {
            dialogBuilder.add(p().innerHtml(resources.messages().expressionEncryptionUnavailable()).element())
                    .primary(resources.constants().close(), () -> true);
        }

        dialog = dialogBuilder.size(Dialog.Size.MEDIUM)
                .build();
        dialog.registerAttachable(form);

    }

    public void show() {
        dialog.show();
        if (!empty) {
            form.edit(new ModelNode());
        }
    }

    private void encrypt(ModelNode modelNode) {
        expressionEncryptor.saveEncryption(modelNode);
    }
}
