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
package org.jboss.hal.client.configuration.subsystem.datasource.wizard;

import java.util.ArrayList;
import java.util.List;

import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.core.datasource.DataSource;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONNECTION_URL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PASSWORD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SECURITY_DOMAIN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.USER_NAME;

class ConnectionStep extends WizardStep<Context, State> {

    private final ModelNodeForm<DataSource> form;

    ConnectionStep(Metadata metadata, Resources resources, boolean xa) {
        super(resources.constants().connection());

        List<String> attributes = new ArrayList<>();
        if (!xa) {
            attributes.add(CONNECTION_URL);
        }
        attributes.addAll(asList(USER_NAME, PASSWORD, SECURITY_DOMAIN));
        form = new ModelNodeForm.Builder<DataSource>(Ids.DATA_SOURCE_CONNECTION_FORM, metadata)
                .include(attributes)
                .unsorted()
                .onSave((form, changedValues) -> {
                    changedValues.forEach((k, v) -> wizard().getContext().recordChange(k, v));
                    wizard().getContext().dataSource = form.getModel();
                })
                .build();
        registerAttachable(form);
    }

    @Override
    public HTMLElement element() {
        return form.element();
    }

    @Override
    protected void onShow(Context context) {
        form.edit(context.dataSource);
    }

    @Override
    protected boolean onNext(Context context) {
        return form.save();
    }

    @Override
    protected boolean onBack(Context context) {
        form.cancel();
        return true;
    }

    @Override
    protected boolean onCancel(Context context) {
        form.cancel();
        return true;
    }
}
