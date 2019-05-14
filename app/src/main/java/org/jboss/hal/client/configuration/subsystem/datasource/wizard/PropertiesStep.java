/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.datasource.wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.inject.Provider;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.autocomplete.StaticAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.PropertiesItem;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.runtime.TopologyTasks;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.FlowException;
import org.jboss.hal.flow.Outcome;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.flow.Flow.series;

class PropertiesStep extends WizardStep<Context, State> {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(DataSourceWizard.class);

    private final ModelNode dummy;
    private final Form<ModelNode> form;
    private final PropertiesItem propertiesItem;
    private Dispatcher dispatcher;
    private StatementContext statementContext;
    private Environment environment;
    private Provider<Progress> progress;
    private Resources resources;
    private StaticAutoComplete propsAutoComplete;

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
                "attributes/value/description"); //NON-NLS
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
        String dsClassname = context.dataSource.hasDefined(XA_DATASOURCE_CLASS) ?
                context.dataSource.get(XA_DATASOURCE_CLASS).asString()
                : null;
        readJdbcDriverProperties(true, dsClassname, context.dataSource.get(DRIVER_NAME).asString(),
                propsAutoComplete::update);
        form.edit(dummy);
    }

    private void readJdbcDriverProperties(boolean isXa, String dsClassname, String driverName,
            Consumer<List<String>> callback) {
        List<Task<FlowContext>> tasks = new ArrayList<>();

        // check running server(s)
        if (!environment.isStandalone()) {
            tasks.add(new TopologyTasks.RunningServersQuery(environment, dispatcher, environment.isStandalone()
                    ? null : new ModelNode().set(PROFILE_NAME, statementContext.selectedProfile())));
        }

        // read jdbc-driver datasource properties
        tasks.add(flowContext -> {
            ResourceAddress address;
            if (environment.isStandalone()) {
                address = Server.STANDALONE.getServerAddress();
            } else {
                List<Server> servers = flowContext.get(TopologyTasks.RUNNING_SERVERS);
                if (!servers.isEmpty()) {
                    Server server = servers.get(0);
                    address = server.getServerAddress();
                } else {
                    String message = resources.messages()
                            .readDatasourcePropertiesErrorDomain(statementContext.selectedProfile());
                    return Completable.error(new FlowException(message, flowContext));
                }
            }
            address.add(SUBSYSTEM, DATASOURCES).add(JDBC_DRIVER, driverName);
            Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            return dispatcher.execute(operation)
                    .doOnSuccess(result -> flowContext.set(RESULT, result))
                    .toCompletable();
        });

        series(new FlowContext(progress.get()), tasks)
                .subscribe(new Outcome<FlowContext>() {
                    @Override
                    public void onError(FlowContext flowContext, Throwable error) {
                        logger.warn("Failed to read jdbc-driver. Cause: {}", error.getMessage());
                    }

                    @Override
                    public void onSuccess(FlowContext flowContext) {
                        ModelNode result = flowContext.get(RESULT);
                        List<String> properties = Collections.emptyList();
                        String datasourceClassname;
                        if (dsClassname == null) {
                            String attribute = isXa ? DRIVER_XA_DATASOURCE_CLASS_NAME : DRIVER_DATASOURCE_CLASS_NAME;
                            datasourceClassname = result.get(attribute).asString();
                        } else {
                            datasourceClassname = dsClassname;
                        }
                        if (result.hasDefined(DATASOURCE_CLASS_INFO)) {
                            properties = result.get(DATASOURCE_CLASS_INFO).asList().stream()
                                    .filter(node -> datasourceClassname.equals(node.asProperty().getName()))
                                    .flatMap(node -> node.asProperty().getValue().asPropertyList().stream())
                                    .map(Property::getName)
                                    .collect(Collectors.toList());
                        }
                        callback.accept(properties);
                    }
                });
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
