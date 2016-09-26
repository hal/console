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
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.Constants;

/**
 * @author Harald Pehl
 */
public class ModifyResourceDialog {

    @FunctionalInterface
    public interface Callback {

        /**
         * Called after the dialog was closed using the primary button.
         */
        void onModify(Form<ModelNode> form, Map<String, Object> changedValues);
    }


    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private Form<ModelNode> form;
    private Dialog dialog;

    /**
     * Uses an existing form for the dialog. If the form has a save callback it's overridden with {@link
     * Callback#onModify(Form, Map)}.
     */
    public ModifyResourceDialog(final String title, final Form<ModelNode> form, final Callback callback) {
        form.setSaveCallback((f, changedValues) -> saveForm(callback, f, changedValues));
        init(title, form);
    }

    private void init(final String title, final Form<ModelNode> form) {
        this.form = form;
        this.dialog = new Dialog.Builder(title)
                .add(form.asElement())
                .saveCancel(form::save)
                .size(Size.MEDIUM)
                .closeOnEsc(true)
                .build();
        this.dialog.registerAttachable(form);
    }

    private void saveForm(final Callback callback, final Form<ModelNode> form,
            final Map<String, Object> changedValues) {
        callback.onModify(form, changedValues);
    }

    public Form<ModelNode> getForm() {
        return form;
    }

    public void show(ModelNode modelNode) {
        // First call dialog.show() (which attaches everything), then call form.edit()
        dialog.show();
        form.edit(modelNode);
    }
}
