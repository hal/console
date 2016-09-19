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
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

/**
 * @author Harald Pehl
 */
public class UploadContentStep extends WizardStep<ContentContext, ContentState> {

    private final UploadElement uploadElement;

    public UploadContentStep(final Resources resources) {
        super(Ids.CONTENT_ADD_MANAGED_UPLOAD_STEP, resources.constants().uploadContent());
        this.uploadElement = new UploadElement(resources.messages().noContent());
    }

    @Override
    public Element asElement() {
        return uploadElement.asElement();
    }

    @Override
    public void reset(final ContentContext context) {
        context.file = null;
    }

    @Override
    protected void onShow(final ContentContext context) {
        uploadElement.reset();
    }

    @Override
    protected boolean onNext(final ContentContext context) {
        if (uploadElement.validate()) {
            context.file = uploadElement.getFiles().item(0);
            return true;
        }
        return false;
    }
}
