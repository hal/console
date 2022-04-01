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

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Provider;

import org.jboss.hal.ballroom.autocomplete.StaticAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.PropertiesItem;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.client.configuration.subsystem.datasource.JdbcDriverTasks.JdbcDriverOutcome;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.client.configuration.subsystem.datasource.JdbcDriverTasks.jdbcDriverProperties;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DRIVER_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.XA_DATASOURCE_CLASS;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.flow.Flow.series;

class PropertiesStep extends WizardStep<Context, State> {

    private final ModelNode dummy;
    private final Form<ModelNode> form;
    private final PropertiesItem propertiesItem;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Environment environment;
    private final Provider<Progress> progress;
    private final Resources resources;
    private final StaticAutoComplete propsAutoComplete;

    PropertiesStep(Dispatcher dispatcher, StatementContext statementContext, Environment environment,
            Provider<Progress> progress, Metadata metadata,
            Resources resources) {
        super(resources.constants().xaProperties());
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.environment = environment;
        this.progress = progress;
        this.resources = resources;

        dummy = new ModelNode().setEmptyObject();
        propertiesItem = new PropertiesItem(VALUE);
        propertiesItem.setRequired(true);
        ModelNode propertiesDescription = failSafeGet(metadata.getDescription(),
                "attributes/value/description"); // NON-NLS
        form = new ModelNodeForm.Builder<>(Ids.DATA_SOURCE_PROPERTIES_FORM, Metadata.empty())
                .unboundFormItem(propertiesItem, 0, SafeHtmlUtils.fromString(propertiesDescription.asString()))
                .build();
        propsAutoComplete = new StaticAutoComplete(Collections.emptyList());
        propertiesItem.registerSuggestHandler(propsAutoComplete);
        registerAttachable(form);
    }

    @Override
    public HTMLElement element() {
        return form.element();
    }

    @Override
    protected void onShow(Context context) {
        propertiesItem.setValue(context.xaProperties);
        propertiesItem.setUndefined(false);
        propertiesItem.setEnabled(!context.isCreated()); // can only be changed if DS was not already created
        String dsClassname = context.dataSource.hasDefined(XA_DATASOURCE_CLASS)
                ? context.dataSource.get(XA_DATASOURCE_CLASS).asString()
                : null;
        readJdbcDriverProperties(true, dsClassname, context.dataSource.get(DRIVER_NAME).asString(),
                propsAutoComplete::update);
        form.edit(dummy);
    }

    private void readJdbcDriverProperties(boolean isXa, String dsClassname, String driverName,
            Consumer<List<String>> callback) {
        List<Task<FlowContext>> tasks = jdbcDriverProperties(environment, dispatcher, statementContext, driverName,
                resources);

        series(new FlowContext(progress.get()), tasks)
                .then(context -> new JdbcDriverOutcome(dsClassname, isXa, callback).onInvoke(context));
    }

    @Override
    protected boolean onNext(Context context) {
        boolean valid = form.save();
        if (valid) {
            context.xaProperties.clear();
            context.xaProperties.putAll(propertiesItem.getValue());
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
