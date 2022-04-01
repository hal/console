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

import javax.inject.Provider;

import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.runtime.TopologyTasks;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Arrays.asList;
import static org.jboss.elemento.Elements.button;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.client.configuration.subsystem.datasource.wizard.DataSourceWizard.addOperation;
import static org.jboss.hal.core.runtime.TopologyTasks.runningServers;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DATASOURCES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DATA_SOURCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TEST_CONNECTION_IN_POOL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.XA_DATA_SOURCE;
import static org.jboss.hal.dmr.ModelNodeHelper.properties;
import static org.jboss.hal.flow.Flow.series;
import static org.jboss.hal.resources.CSS.blankSlatePf;
import static org.jboss.hal.resources.CSS.btn;
import static org.jboss.hal.resources.CSS.btnLg;
import static org.jboss.hal.resources.CSS.btnPrimary;

class TestStep extends WizardStep<Context, State> {

    private static final String WIZARD_TITLE = "wizard-title";
    private static final String WIZARD_TEXT = "wizard-text";
    private static final String WIZARD_ERROR = "wizard-error";

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Environment environment;
    private final Provider<Progress> progress;
    private final Resources resources;
    private final HTMLElement root;

    TestStep(Dispatcher dispatcher,
            StatementContext statementContext,
            Environment environment,
            Provider<Progress> progress,
            Resources resources) {

        super(resources.constants().testConnection());
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.environment = environment;
        this.progress = progress;
        this.resources = resources;

        String testConnection = resources.constants().testConnection();
        SafeHtml description = environment.isStandalone()
                ? resources.messages().testConnectionStandalone(testConnection)
                : resources.messages().testConnectionDomain(testConnection);

        root = div()
                .add(div().innerHtml(description))
                .add(div().css(blankSlatePf)
                        .add(button(resources.constants().testConnection())
                                .id(Ids.DATA_SOURCE_TEST_CONNECTION)
                                .css(btn, btnLg, btnPrimary)
                                .on(click, event -> testConnection())))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    private void testConnection() {
        Context context = wizard().getContext();

        List<Task<FlowContext>> tasks = new ArrayList<>();
        if (!context.isCreated()) {
            // add data source
            tasks.add(flowContext -> dispatcher.execute(addOperation(context, statementContext))
                    .then(__ -> {
                        context.setCreated(true);
                        return Promise.resolve(flowContext);
                    })
                    .catch_(error -> {
                        flowContext.set(WIZARD_TITLE, resources.constants().testConnectionError());
                        flowContext.set(WIZARD_TEXT, resources.messages().dataSourceAddError());
                        return Promise.resolve(flowContext);
                    }));
        }

        // check running server(s)
        tasks.addAll(runningServers(environment, dispatcher,
                properties(PROFILE_NAME, statementContext.selectedProfile())));

        // test connection
        tasks.add(flowContext -> {
            List<Server> servers = flowContext.get(TopologyTasks.SERVERS);
            ResourceAddress address;
            if (!servers.isEmpty()) {
                Server server = servers.get(0);
                address = server.getServerAddress();
            } else if (environment.isStandalone()) {
                address = ResourceAddress.root();
            } else {
                flowContext.set(WIZARD_TITLE, resources.constants().testConnectionError());
                flowContext.set(WIZARD_TEXT, SafeHtmlUtils.fromString(resources.constants().noRunningServers()));
                flowContext.set(WIZARD_ERROR, resources.messages().testConnectionErrorDomain());
                return Promise.resolve(flowContext);
            }
            address.add(SUBSYSTEM, DATASOURCES)
                    .add(context.dataSource.isXa() ? XA_DATA_SOURCE : DATA_SOURCE, context.dataSource.getName());
            Operation operation = new Operation.Builder(address, TEST_CONNECTION_IN_POOL).build();
            return dispatcher.execute(operation)
                    .then(__ -> Promise.resolve(flowContext))
                    .catch_(error -> {
                        flowContext.set(WIZARD_TITLE, resources.constants().testConnectionError());
                        flowContext.set(WIZARD_TEXT, resources.messages().testConnectionError(context.dataSource.getName()));
                        return Promise.resolve(flowContext);
                    });
        });

        series(new FlowContext(progress.get()), tasks)
                .then(flowContext -> {
                    if (flowContext.keys().containsAll(asList(WIZARD_TITLE, WIZARD_TEXT))) {
                        String title = flowContext.get(WIZARD_TITLE);
                        SafeHtml text = flowContext.get(WIZARD_TEXT);
                        String error = flowContext.get(WIZARD_ERROR, resources.constants().unknownError());
                        wizard().showError(title, text, error, false);
                    } else {
                        wizard().showSuccess(resources.constants().testConnectionSuccess(),
                                resources.messages().testConnectionSuccess(context.dataSource.getName()), false);
                    }
                    return null;
                })
                .catch_(error -> {
                    String title = resources.constants().unknownError();
                    SafeHtml text = resources.messages().unknownError();
                    wizard().showError(title, text, String.valueOf(error), false);
                    return null;
                });
    }
}
