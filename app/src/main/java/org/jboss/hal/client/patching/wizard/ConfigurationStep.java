/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.patching.wizard;

import java.util.ArrayList;
import java.util.List;

import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.dmr.ModelDescriptionConstants.OVERRIDE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OVERRIDE_ALL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OVERRIDE_MODULE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PRESERVE;

public class ConfigurationStep extends WizardStep<PatchContext, PatchState> {

    private final Form<ModelNode> form;

    ConfigurationStep(Metadata metadata, Resources resources) {
        super(resources.messages().configurePatchTitle());

        form = new ModelNodeForm.Builder<>(Ids.PATCH_UPLOAD_NAMES_FORM, metadata)
                .exclude("input-stream-index")
                .unsorted()
                .build();
        registerAttachable(form);
    }

    @Override
    public HTMLElement element() {
        return form.element();
    }

    @Override
    public void reset(PatchContext context) {
        context.overrideAll = false;
        context.overrideModules = false;
        context.override = new ArrayList<>();
        context.preserve = new ArrayList<>();
    }

    @Override
    protected void onShow(PatchContext context) {
        form.edit(new ModelNode());
    }

    @Override
    protected boolean onNext(PatchContext context) {
        boolean valid = form.save();
        if (valid) {
            context.overrideAll = form.getFormItem(OVERRIDE_ALL).isUndefined() ? false
                    : form.<Boolean>getFormItem(OVERRIDE_ALL).getValue();
            context.overrideModules = form.getFormItem(OVERRIDE_MODULE).isUndefined() ? false
                    : form.<Boolean>getFormItem(OVERRIDE_MODULE).getValue();
            context.override = form.getFormItem(OVERRIDE).isUndefined() ? null
                    : form.<List<String>>getFormItem(OVERRIDE).getValue();
            context.preserve = form.getFormItem(PRESERVE).isUndefined() ? null
                    : form.<List<String>>getFormItem(PRESERVE).getValue();
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
