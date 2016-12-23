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
import java.util.Arrays;
import java.util.List;

import elemental.dom.Element;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.core.datasource.DataSource;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
class ReviewStep extends WizardStep<Context, State> {

    private final ModelNodeForm<DataSource> form;

    ReviewStep(final Metadata metadata, final Resources resources, final boolean xa) {
        super(Ids.DATA_SOURCE_REVIEW_STEP, resources.constants().review());

        List<String> attributes = new ArrayList<>();
        attributes.add(JNDI_NAME);
        if (!xa) {
            attributes.add(CONNECTION_URL);
        }
        attributes.addAll(Arrays.asList(DRIVER_NAME, "user-name", PASSWORD)); //NON-NLS

        form = new ModelNodeForm.Builder<DataSource>(Ids.DATA_SOURCE_REVIEW_FORM, metadata)
                .unboundFormItem(new NameItem(), 0)
                .include(attributes)
                .unsorted()
                .viewOnly()
                .build();
    }

    @Override
    public Element asElement() {
        return form.asElement();
    }

    @Override
    protected void onShow(final Context context) {
        FormItem<String> nameItem = form.getFormItem(NAME);
        nameItem.setValue(context.dataSource.getName());
        form.view(context.dataSource);
    }
}
