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
import java.util.Map;

import com.google.common.collect.Maps;
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.autocomplete.StaticAutoComplete;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.core.datasource.JdbcDriver;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

class DriverStep extends WizardStep<Context, State> {

    private final ModelNodeForm<JdbcDriver> form;

    DriverStep(List<JdbcDriver> drivers, Metadata metadata, Resources resources) {

        super(resources.constants().jdbcDriver());
        Map<String, JdbcDriver> driversByName = Maps.uniqueIndex(drivers, JdbcDriver::getName);
        this.form = new ModelNodeForm.Builder<JdbcDriver>(Ids.DATA_SOURCE_DRIVER_FORM, adjustMetadata(metadata))
                .include(DRIVER_NAME, DRIVER_MODULE_NAME, DRIVER_CLASS_NAME)
                .unsorted()
                .onSave((form, changedValues) -> wizard().getContext().driver = form.getModel())
                .build();

        if (!driversByName.isEmpty()) {
            form.getFormItem(DRIVER_NAME)
                    .registerSuggestHandler(new StaticAutoComplete(new ArrayList<>(driversByName.keySet())));
            form.getFormItem(DRIVER_NAME)
                    .addValidationHandler(value ->
                            driversByName.keySet().contains(value) ? ValidationResult.OK : ValidationResult.invalid(
                                    "Invalid driver name")
                    );
        }
        registerAttachable(form);
    }

    private Metadata adjustMetadata(Metadata metadata) {
        ModelNode newAttributes = new ModelNode();
        for (Property property : metadata.getDescription().get(ATTRIBUTES).asPropertyList()) {
            ModelNode value = property.getValue().clone();
            value.get(ACCESS_TYPE).set(READ_WRITE);
            value.get(NILLABLE).set(!DRIVER_NAME.equals(property.getName()));
            newAttributes.get(property.getName()).set(value);
        }

        metadata.getDescription().remove(ATTRIBUTES);
        metadata.getDescription().get(ATTRIBUTES).set(newAttributes);
        return metadata;
    }

    @Override
    public HTMLElement asElement() {
        return form.asElement();
    }

    @Override
    protected void onShow(Context context) {
        form.edit(context.driver);
    }

    @Override
    protected boolean onNext(Context context) {
        boolean valid = form.save();
        if (valid) {
            JdbcDriver driver = form.getModel();
            context.dataSource.setDriver(driver);
            if (context.isCreated()) {
                context.recordChange(DRIVER_NAME, driver.getName());
                context.recordChange(DRIVER_CLASS, driver.get(DRIVER_CLASS_NAME).asString());
            }
        }
        return valid;
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
