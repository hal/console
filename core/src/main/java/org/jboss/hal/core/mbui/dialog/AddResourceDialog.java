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
package org.jboss.hal.core.mbui.dialog;

import java.util.Map;

import com.google.gwt.core.client.GWT;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.Dialog.Size;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Constants;
import org.jetbrains.annotations.Nullable;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * @author Harald Pehl
 */
public class AddResourceDialog {

    @FunctionalInterface
    public interface Callback {

        /**
         * Called after the dialog was closed using the primary button.
         *
         * @param name  The name of the resource to add. {@code null} if the dialog's form does not contain a
         *              name item (i.e. when adding a singleton resource)
         * @param model The model of the related form. {@code null} if the related resource description and thus
         *              the form does not contain attributes / form items.
         */
        void onAdd(@Nullable final String name, @Nullable final ModelNode model);
    }


    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private Form<ModelNode> form;
    private Dialog dialog;

    /**
     * Creates an add resource dialog with a form which contains an unbound name item plus all request properties from
     * the add operation. Clicking on the add button will call the specified callback.
     */
    public AddResourceDialog(final String id, final String title, final Metadata metadata, final Callback callback) {

        ModelNodeForm.Builder<ModelNode> formBuilder = new ModelNodeForm.Builder<>(id, metadata)
                .addFromRequestProperties()
                .unboundFormItem(new NameItem(), 0)
                .onSave((f, changedValues) -> saveForm(callback, changedValues, form.getModel()));

        init(title, formBuilder.build());
    }

    /**
     * Uses an existing form for the dialog. If the form has a save callback it's overridden with {@link
     * Callback#onAdd(String, ModelNode)}.
     */
    public AddResourceDialog(final String title, final Form<ModelNode> form, final Callback callback) {
        form.setSaveCallback((f, changedValues) -> saveForm(callback, changedValues, form.getModel()));
        init(title, form);
    }

    private void init(final String title, final Form<ModelNode> form) {
        this.form = form;
        this.dialog = new Dialog.Builder(title)
                .add(form.asElement())
                .primary(CONSTANTS.add(), form::save)
                .secondary(CONSTANTS.cancel(), () -> true)
                .size(Size.MEDIUM)
                .closeOnEsc(true)
                .build();
        this.dialog.registerAttachable(form);
    }

    private void saveForm(final Callback callback, final Map<String, Object> changedValues,
            final ModelNode model) {
        String name = String.valueOf(changedValues.remove(NAME));
        callback.onAdd(name, model);
    }

    public void show() {
        // First call dialog.show() (which attaches everything), then call form.add()
        dialog.show();
        form.add(new ModelNode());
    }
}
