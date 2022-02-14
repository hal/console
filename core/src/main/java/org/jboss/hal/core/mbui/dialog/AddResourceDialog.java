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
package org.jboss.hal.core.mbui.dialog;

import java.util.Collections;

import javax.annotation.Nullable;

import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.Dialog.Size;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Constants;

import com.google.common.collect.Iterables;
import com.google.gwt.core.client.GWT;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

public class AddResourceDialog {

    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private FormItem<String> nameItem;
    private Form<ModelNode> form;
    private Dialog dialog;

    /**
     * Creates an add resource dialog with a form which contains an unbound name item plus all request properties from the add
     * operation. Clicking on the add button will call the specified callback.
     */
    public AddResourceDialog(String id, String title, Metadata metadata, Callback callback) {
        this(id, title, metadata, Collections.emptyList(), callback);
    }

    public AddResourceDialog(String id, String title, Metadata metadata, Iterable<String> attributes,
            Callback callback) {
        nameItem = new NameItem();
        ModelNodeForm.Builder<ModelNode> formBuilder = new ModelNodeForm.Builder<>(id, metadata)
                .unboundFormItem(nameItem, 0)
                .fromRequestProperties()
                .requiredOnly()
                .onSave((f, changedValues) -> saveForm(callback, form.getModel()));

        if (!Iterables.isEmpty(attributes)) {
            formBuilder.include(attributes).unsorted();
        }

        init(title, formBuilder.build());
    }

    /**
     * Uses an existing form for the dialog. If the form has a save callback it's overridden with
     * {@link Callback#onAdd(String, ModelNode)}.
     */
    public AddResourceDialog(String title, Form<ModelNode> form, Callback callback) {
        nameItem = form.getFormItem(NAME);
        form.setSaveCallback((f, changedValues) -> saveForm(callback, form.getModel()));
        init(title, form);
    }

    private void init(String title, Form<ModelNode> form) {
        this.form = form;
        this.dialog = new Dialog.Builder(title)
                .add(form.element())
                .primary(CONSTANTS.add(), form::save)
                .size(Size.MEDIUM)
                .cancel()
                .build();
        this.dialog.registerAttachable(form);
    }

    private void saveForm(Callback callback, ModelNode model) {
        String name = nameItem != null ? nameItem.getValue() : null;
        callback.onAdd(name, model);
    }

    public Form<ModelNode> getForm() {
        return form;
    }

    public void show() {
        // First call dialog.show() (which attaches everything), then call form.edit()
        dialog.show();
        form.edit(new ModelNode());
    }

    @FunctionalInterface
    public interface Callback {

        /**
         * Called after the dialog was closed using the primary button.
         *
         * @param name The name of the resource to add. {@code null} if the dialog's form does not contain a name item (i.e.
         *        when adding a singleton resource)
         * @param model The model of the related form. {@code null} if the related resource description and thus the form does
         *        not contain attributes / form items.
         */
        void onAdd(@Nullable String name, @Nullable ModelNode model);
    }
}
