/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.patching.wizard;

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

class RollbackStep extends WizardStep<PatchContext, PatchState> {

    private final Form<ModelNode> form;
    private ModelNode model;

    RollbackStep(Metadata metadata, Resources resources, String host,
            String patchId) {
        super(resources.constants().rollback());

        model = new ModelNode();
        model.get(PATCH_ID).set(patchId);
        String id = Ids.build(Ids.HOST, host, CORE_SERVICE, PATCHING, patchId, ROLLBACK_OPERATION);
        form = new ModelNodeForm.Builder<>(id, metadata)
                .unsorted()
                .build();
        form.getFormItem(PATCH_ID).setEnabled(false);
        // there is no default value of reset-configuration attribute on r-r-d
        // so if the user doesn't set the value in the form, the form will fail to validate
        model.get(RESET_CONFIGURATION).set(false);

        registerAttachable(form);
    }

    @Override
    public HTMLElement element() {
        return form.element();
    }

    @Override
    public void reset(PatchContext context) {
        context.rollbackTo = false;
        context.resetConfiguration = false;
        context.overrideAll = false;
        context.overrideModules = false;
        context.override = new ArrayList<>();
        context.preserve = new ArrayList<>();
        context.patchId = model.get(PATCH_ID).asString();
    }

    @Override
    protected void onShow(PatchContext context) {
        form.edit(model);
    }

    @Override
    protected boolean onNext(PatchContext context) {
        boolean valid = form.save();
        if (valid) {
            context.rollbackTo = form.getFormItem(ROLLBACK_TO).isUndefined() ? false
                    : form.<Boolean> getFormItem(ROLLBACK_TO).getValue();
            context.resetConfiguration = form.getFormItem(RESET_CONFIGURATION).isUndefined() ? false
                    : form.<Boolean> getFormItem(RESET_CONFIGURATION).getValue();
            context.overrideAll = form.getFormItem(OVERRIDE_ALL).isUndefined() ? false
                    : form.<Boolean> getFormItem(OVERRIDE_ALL).getValue();
            context.overrideModules = form.getFormItem(OVERRIDE_MODULE).isUndefined() ? false
                    : form.<Boolean> getFormItem(OVERRIDE_MODULE).getValue();
            context.override = form.getFormItem(OVERRIDE).isUndefined() ? null
                    : form.<List<String>> getFormItem(OVERRIDE).getValue();
            context.preserve = form.getFormItem(PRESERVE).isUndefined() ? null
                    : form.<List<String>> getFormItem(PRESERVE).getValue();
        }
        return valid;
    }

    @Override
    protected boolean onBack(PatchContext context) {
        form.cancel();
        return true;
    }

    @Override
    protected boolean onCancel(PatchContext context) {
        form.cancel();
        return true;
    }
}
