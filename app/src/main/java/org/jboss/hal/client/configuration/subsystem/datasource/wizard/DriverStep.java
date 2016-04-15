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
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.client.configuration.subsystem.datasource.JdbcDriver;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * @author Harald Pehl
 */
class DriverStep extends WizardStep<Context, State> {

    private final Tabs tabs;
    private final ModelNodeForm<JdbcDriver> form;

    DriverStep(final NewDataSourceWizard wizard, final List<JdbcDriver> drivers,
            final Metadata metadata, final Resources resources) {
        super(wizard, resources.constants().jdbcDriver());

        form = new ModelNodeForm.Builder<JdbcDriver>(IdBuilder.build(id(), "driver", "step"), metadata)
                .unboundFormItem(new NameItem(), 0)
                .requiredOnly()
                .onSave((form, changedValues) -> wizard.getContext().driver = form.getModel())
                .build();
        Element nyi = new Elements.Builder().p().textContent(Names.NYI).end().build();

        tabs = new Tabs();
        tabs.add(IdBuilder.build(id(), "driver", "step", "specify"), resources.constants().specifyDriver(),
                form.asElement());
        tabs.add(IdBuilder.build(id(), "driver", "step", "detected"), resources.constants().detectedDriver(),
                nyi);
    }

    @Override
    public Element asElement() {
        return tabs.asElement();
    }

    @Override
    protected void onShow(final Context context) {
        // name is unbound so we have to bind it manually
        FormItem<Object> nameItem = form.getFormItem(NAME);
        nameItem.setValue(context.driver.getName());
        nameItem.setUndefined(false);

        form.edit(context.driver);
    }

    @Override
    protected boolean onNext(final Context context) {
        return form.save();
    }
}
