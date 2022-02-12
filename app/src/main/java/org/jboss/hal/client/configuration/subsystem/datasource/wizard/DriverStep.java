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
import java.util.List;
import java.util.Map;

import org.jboss.hal.ballroom.autocomplete.StaticAutoComplete;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.client.configuration.subsystem.datasource.DataSourceTemplate;
import org.jboss.hal.core.datasource.JdbcDriver;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import com.google.common.collect.Maps;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ACCESS_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DRIVER_CLASS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DRIVER_CLASS_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DRIVER_MODULE_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DRIVER_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DRIVER_XA_DATASOURCE_CLASS_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NILLABLE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_WRITE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.XA_DATASOURCE_CLASS;

class DriverStep extends WizardStep<Context, State> {

    private final ModelNodeForm<JdbcDriver> form;
    private final Map<String, JdbcDriver> driversByName;
    private boolean firstTime = false;

    DriverStep(List<JdbcDriver> drivers, Metadata metadata, Resources resources, boolean xa) {

        super(Names.JDBC_DRIVER);
        driversByName = Maps.uniqueIndex(drivers, JdbcDriver::getName);
        this.form = new ModelNodeForm.Builder<JdbcDriver>(Ids.DATA_SOURCE_DRIVER_FORM, adjustMetadata(metadata))
                .include(DRIVER_NAME, DRIVER_MODULE_NAME, xa ? DRIVER_XA_DATASOURCE_CLASS_NAME : DRIVER_CLASS_NAME)
                .unsorted()
                .onSave((form, changedValues) -> wizard().getContext().driver = form.getModel())
                .build();

        if (!driversByName.isEmpty()) {
            form.getFormItem(DRIVER_NAME)
                    .registerSuggestHandler(new StaticAutoComplete(new ArrayList<>(driversByName.keySet())));
            form.getFormItem(DRIVER_NAME)
                    .addValidationHandler(value -> driversByName.keySet().contains(value) ? ValidationResult.OK
                            : ValidationResult.invalid(
                                    "Invalid driver name"));
            if (xa) {
                // assign a value change handler that modfies the xa-datasource-class using jdbcDriver conf or template
                form.getFormItem(DRIVER_NAME)
                        .addValueChangeHandler(event -> assignFromJdbcDriverOrTemplate(String.valueOf(event.getValue()),
                                DRIVER_XA_DATASOURCE_CLASS_NAME));
                firstTime = true;
            }
        }
        registerAttachable(form);
    }

    private Metadata adjustMetadata(Metadata metadata) {
        ModelNode newAttributes = new ModelNode();
        for (Property property : metadata.getDescription().get(ATTRIBUTES).asPropertyList()) {
            ModelNode value = property.getValue().clone();
            value.get(ACCESS_TYPE).set(READ_WRITE);
            value.get(NILLABLE).set(
                    !DRIVER_NAME.equals(property.getName()) && !DRIVER_XA_DATASOURCE_CLASS_NAME.equals(property.getName()));
            newAttributes.get(property.getName()).set(value);
        }

        metadata.getDescription().remove(ATTRIBUTES);
        metadata.getDescription().get(ATTRIBUTES).set(newAttributes);
        return metadata;
    }

    @Override
    public HTMLElement element() {
        return form.element();
    }

    @SuppressWarnings("unchecked")
    private void assignFromJdbcDriverOrTemplate(String driverName, String propName) {
        FormItem formItem = form.getFormItem(propName);
        if (formItem != null) {
            JdbcDriver driver = driversByName.get(driverName);
            DataSourceTemplate template = this.wizard().getContext().template;
            if (driver != null && driver.hasDefined(propName)
                    && !driver.get(propName).asString().isEmpty()) {
                // assign the value inside the driver
                formItem.setModified(true);
                formItem.setValue(driver.get(propName).asString());
            } else if (template != null && template.getDriver() != null
                    && template.getDriver().hasDefined(propName)
                    && !template.getDriver().get(propName).asString().isEmpty()) {
                // assign the value in the template
                formItem.setModified(true);
                formItem.setValue(this.wizard().getContext().template.getDriver().get(propName).asString());
            }
            // let the current value at it is now
        }
    }

    @Override
    protected void onShow(Context context) {
        form.edit(context.driver);
        if (firstTime) {
            firstTime = false;
            assignFromJdbcDriverOrTemplate(context.driver.getName(), DRIVER_XA_DATASOURCE_CLASS_NAME);
        }
    }

    @Override
    protected boolean onNext(Context context) {
        boolean valid = form.save();
        if (valid) {
            JdbcDriver driver = form.getModel();
            context.dataSource.setDriver(driver);
            if (context.isCreated()) {
                context.recordChange(DRIVER_NAME, driver.getName());
                if (context.isXa()) {
                    context.recordChange(XA_DATASOURCE_CLASS, driver.get(DRIVER_XA_DATASOURCE_CLASS_NAME).asString());
                } else {
                    context.recordChange(DRIVER_CLASS, driver.get(DRIVER_CLASS_NAME).asString());
                }
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
