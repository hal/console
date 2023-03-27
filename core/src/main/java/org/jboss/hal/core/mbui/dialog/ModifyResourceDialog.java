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

import java.util.Map;

import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.Dialog.Size;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.dmr.ModelNode;

import elemental2.promise.Promise;

public class ModifyResourceDialog {

    private Form<ModelNode> form;
    private Dialog dialog;

    public ModifyResourceDialog(String title, Form<ModelNode> form) {
        this(title, form, null, null);
    }

    /**
     * Uses an existing form for the dialog. If the form has a save callback it's overridden with
     * {@link Callback#onModify(Form, Map)}.
     */
    public ModifyResourceDialog(String title, Form<ModelNode> form, Callback callback) {
        this(title, form, callback, null);
    }

    public ModifyResourceDialog(String title, Form<ModelNode> form, Callback callback,
            org.jboss.hal.spi.Callback closed) {
        if (callback != null) {
            form.setSaveCallback((f, changedValues) -> saveForm(callback, f, changedValues));
        }
        init(title, form, closed);
    }

    private void init(String title, Form<ModelNode> form, org.jboss.hal.spi.Callback closed) {
        this.form = form;
        this.dialog = new Dialog.Builder(title)
                .add(form.element())
                .saveCancel(form::save)
                .size(Size.MEDIUM)
                .closed(closed)
                .build();
        this.dialog.registerAttachable(form);
    }

    private void saveForm(Callback callback, Form<ModelNode> form, Map<String, Object> changedValues) {
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

    public Promise<Map<String, Object>> showWithPromise(ModelNode modelNode) {
        dialog.show();
        form.edit(modelNode);
        return new Promise<Map<String, Object>>(
                (resolve, reject) -> form.setSaveCallback((f, changedValues) -> resolve.onInvoke(changedValues)));
    }

    @FunctionalInterface
    public interface Callback {

        /**
         * Called after the dialog was closed using the primary button.
         */
        void onModify(Form<ModelNode> form, Map<String, Object> changedValues);
    }
}
