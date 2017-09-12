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
import javax.inject.Provider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.runtime.TopologyTasks;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Outcome;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.button;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.client.configuration.subsystem.datasource.wizard.DataSourceWizard.addOperation;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.flow.Flow.series;
import static org.jboss.hal.resources.CSS.blankSlatePf;
import static org.jboss.hal.resources.CSS.btn;
import static org.jboss.hal.resources.CSS.btnLg;
import static org.jboss.hal.resources.CSS.btnPrimary;

class TestStep extends WizardStep<Context, State> {

    private static final String WIZARD_TITLE = "wizard-title";
    private static final String WIZARD_TEXT = "wizard-text";

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
                        .add(button(resources.constants().testConnection()).css(btn, btnLg, btnPrimary)
                                .on(click, event -> testConnection())))
                .asElement();
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    private void testConnection() {
        Context context = wizard().getContext();

        List<Task<FlowContext>> tasks = new ArrayList<>();
        if (!context.isCreated()) {
            // add data source
            tasks.add((flowContext, control) -> dispatcher.executeInFlow(control, addOperation(context, statementContext),
                    (CompositeResult result) -> {
                        context.setCreated(true);
                        control.proceed();
                    },
                    (op, failure) -> {
                        flowContext.set(WIZARD_TITLE, resources.constants().testConnectionError());
                        flowContext.set(WIZARD_TEXT, resources.messages().dataSourceAddError());
                        control.abort(failure);
                    }));
        }

        // check running server(s)
        tasks.add(new TopologyTasks.RunningServersQuery(
                environment, dispatcher, new ModelNode().set(PROFILE_NAME, statementContext.selectedProfile())));

        // test connection
        tasks.add((flowContext, control) -> {
            List<Server> servers = flowContext.get(TopologyTasks.RUNNING_SERVERS);
            if (!servers.isEmpty()) {
                Server server = servers.get(0);
                ResourceAddress address = server.getServerAddress().add(SUBSYSTEM, DATASOURCES)
                        .add(DATA_SOURCE, context.dataSource.getName());
                Operation operation = new Operation.Builder(address, TEST_CONNECTION_IN_POOL).build();
                dispatcher.executeInFlow(control, operation,
                        result -> control.proceed(),
                        (op, failure) -> {
                            flowContext.set(WIZARD_TITLE, resources.constants().testConnectionError());
                            flowContext.set(WIZARD_TEXT,
                                    resources.messages().testConnectionError(context.dataSource.getName()));
                            control.abort(failure);
                        });

            } else {
                flowContext.set(WIZARD_TITLE, resources.constants().testConnectionError());
                flowContext.set(WIZARD_TEXT,
                        SafeHtmlUtils.fromString(resources.constants().noRunningServers()));
                control.abort("no running servers"); //NON-NLS
            }
        });

        series(progress.get(), new FlowContext(), tasks)
                .subscribe(new Outcome<FlowContext>() {
                    @Override
                    public void onError(FlowContext flowContext, Throwable error) {
                        String title = flowContext.get(WIZARD_TITLE);
                        SafeHtml text = flowContext.get(WIZARD_TEXT);
                        wizard().showError(title, text, error.getMessage(), false);
                    }

                    @Override
                    public void onSuccess(FlowContext flowContext) {
                        wizard().showSuccess(resources.constants().testConnectionSuccess(),
                                resources.messages().testConnectionSuccess(context.dataSource.getName()), false);
                    }
                });
    }
}
