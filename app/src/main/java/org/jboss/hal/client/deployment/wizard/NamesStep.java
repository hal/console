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
package org.jboss.hal.client.deployment.wizard;

import elemental.dom.Element;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RUNTIME_NAME;

/**
 * @author Harald Pehl
 */
public class NamesStep extends WizardStep<UploadContext, UploadState> {

    private final NameItem nameItem;
    private final Form<ModelNode> form;
    private final Environment environment;

    public NamesStep(final Environment environment, final Metadata metadata, final Resources resources) {
        super(Ids.UPLOAD_NAMES_STEP, resources.constants().specifyNames());
        this.environment = environment;

        nameItem = new NameItem();
        ModelNodeForm.Builder<ModelNode> builder = new ModelNodeForm.Builder<>(Ids.UPLOAD_NAMES_FORM, metadata)
                .unboundFormItem(nameItem, 0)
                .addFromRequestProperties()
                .unsorted()
                .include(RUNTIME_NAME);
        if (environment.isStandalone()) {
            builder.include(ENABLED);
        }
        form = builder.build();
        registerAttachable(form);
    }

    @Override
    public Element asElement() {
        return form.asElement();
    }

    @Override
    public void reset(final UploadContext context) {
        context.name = "";
        context.runtimeName = "";
        context.enabled = true;
    }

    @Override
    protected void onShow(final UploadContext context) {
        String filename = context.file.getName();

        form.add(new ModelNode());
        nameItem.setValue(filename);
        nameItem.setUndefined(false);
        form.getFormItem(RUNTIME_NAME).setValue(filename);
        if (environment.isStandalone()) {
            form.getFormItem(ENABLED).setValue(context.enabled);
        }
    }

    @Override
    protected boolean onNext(final UploadContext context) {
        boolean valid = form.save();
        if (valid) {
            context.name = nameItem.getValue();
            context.runtimeName = form.<String>getFormItem(RUNTIME_NAME).getValue();
        }
        return valid;
    }
}
