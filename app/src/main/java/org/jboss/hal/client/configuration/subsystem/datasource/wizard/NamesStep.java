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

import elemental.dom.Element;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.client.configuration.subsystem.datasource.DataSource;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Resources;

import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * @author Harald Pehl
 */
class NamesStep extends WizardStep<Context, State> {

    private final ModelNodeForm<DataSource> form;

    NamesStep(final NewDataSourceWizard wizard, final List<DataSource> existingDataSources,
            final Metadata metadata, final Resources resources) {
        super(wizard, resources.constants().attributes());

        form = new ModelNodeForm.Builder<DataSource>(IdBuilder.build(id(), "names", "step"), metadata)
                .unboundFormItem(new NameItem(), 0)
                .include("jndi-name")
                .onSave((form, changedValues) -> wizard.getContext().dataSource = form.getModel())
                .build();
    }

    @Override
    public Element asElement() {
        return form.asElement();
    }

    @Override
    protected void onShow(final Context context) {
        // name is unbound so we have to bind it manually
        FormItem<Object> nameItem = form.getFormItem(NAME);
        nameItem.setValue(context.dataSource.getName());
        nameItem.setUndefined(false);

        form.edit(context.dataSource);
    }

    @Override
    protected boolean onNext(final Context context) {
        return form.save();
    }
}
