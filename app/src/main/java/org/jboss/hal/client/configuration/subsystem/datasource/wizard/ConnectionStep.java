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

import elemental.dom.Element;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.client.configuration.subsystem.datasource.DataSource;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONNECTION_URL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PASSWORD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SECURITY_DOMAIN;

/**
 * @author Harald Pehl
 */
class ConnectionStep extends WizardStep<Context, State> {

    private final ModelNodeForm<DataSource> form;

    ConnectionStep(final NewDataSourceWizard wizard, final Metadata metadata, final Resources resources,
            final boolean xa) {
        super(wizard, resources.constants().connection());

        List<String> attributes = new ArrayList<>();
        if (!xa) {
            attributes.add(CONNECTION_URL);
        }
        attributes.addAll(asList("user-name", PASSWORD, SECURITY_DOMAIN)); //NON-NLS
        form = new ModelNodeForm.Builder<DataSource>(Ids.build(id(), "connection", "step"), metadata)
                .include(attributes)
                .unsorted()
                .onSave((form, changedValues) -> wizard.getContext().dataSource = form.getModel())
                .build();
    }

    @Override
    public Element asElement() {
        return form.asElement();
    }

    @Override
    protected void onShow(final Context context) {
        form.edit(context.dataSource);
    }

    @Override
    protected boolean onNext(final Context context) {
        return form.save();
    }
}
