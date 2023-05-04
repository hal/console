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
package org.jboss.hal.client.configuration.subsystem.datasource.wizard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.PropertiesItem;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.core.datasource.DataSource;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

class ReviewStep extends WizardStep<Context, State> {

    private final ModelNodeForm<DataSource> form;

    ReviewStep(Metadata metadata, Resources resources, boolean xa) {
        super(resources.constants().review());

        List<String> attributes = new ArrayList<>();
        attributes.add(JNDI_NAME);
        attributes.add(DRIVER_NAME);
        if (!xa) {
            attributes.add(DRIVER_CLASS);
        } else {
            attributes.add(XA_DATASOURCE_CLASS);
        }
        if (!xa) {
            attributes.add(CONNECTION_URL);
        }
        attributes.addAll(Arrays.asList(USER_NAME, PASSWORD, AUTHENTICATION_CONTEXT)); // NON-NLS

        ModelNodeForm.Builder builder = new ModelNodeForm.Builder<DataSource>(Ids.DATA_SOURCE_REVIEW_FORM, metadata)
                .unboundFormItem(new NameItem(), 0)
                .unboundFormItem(new TextBoxItem(CREDENTIAL_REFERENCE))
                .include(attributes)
                .unsorted()
                .readOnly();
        if (xa) {
            builder.unboundFormItem(new PropertiesItem(XA_DATASOURCE_PROPERTIES));
        }

        form = builder.build();
    }

    @Override
    public HTMLElement element() {
        return form.element();
    }

    @Override
    protected void onShow(Context context) {
        FormItem<String> nameItem = form.getFormItem(NAME);
        nameItem.setValue(context.dataSource.getName());
        ModelNode credRef = context.dataSource.has(CREDENTIAL_REFERENCE) ? context.dataSource.get(CREDENTIAL_REFERENCE)
                : new ModelNode();
        form.<String> getFormItem(CREDENTIAL_REFERENCE).setValue(credRef.isDefined() ? credRef.asString() : "");
        if (context.isXa()) {
            form.getFormItem(XA_DATASOURCE_PROPERTIES).setValue(context.xaProperties);
        }
        form.view(context.dataSource);
    }
}
