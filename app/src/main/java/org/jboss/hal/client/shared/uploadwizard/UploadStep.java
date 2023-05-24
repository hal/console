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
package org.jboss.hal.client.shared.uploadwizard;

import org.jboss.hal.ballroom.wizard.WizardStep;

import com.google.gwt.safehtml.shared.SafeHtml;

import elemental2.dom.HTMLElement;

public abstract class UploadStep<C extends UploadContext, S extends Enum<S>> extends WizardStep<C, S> {

    private final UploadElement uploadElement;

    public UploadStep(String title, SafeHtml onError) {
        super(title);
        this.uploadElement = new UploadElement(true, onError);
    }

    @Override
    public HTMLElement element() {
        return uploadElement.element();
    }

    @Override
    public void reset(C context) {
        context.file = null;
    }

    @Override
    protected void onShow(C context) {
        uploadElement.reset();
    }

    @Override
    protected boolean onNext(C context) {
        if (uploadElement.validate()) {
            context.file = uploadElement.getFiles().item(0);
            return true;
        }
        return false;
    }
}
