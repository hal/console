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
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.datasource.DataSource;
import org.jboss.hal.core.runtime.TopologyFunctions;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.DATA_SOURCE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.XA_DATA_SOURCE_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.blankSlatePf;
import static org.jboss.hal.resources.CSS.btn;
import static org.jboss.hal.resources.CSS.btnLg;
import static org.jboss.hal.resources.CSS.btnPrimary;

/**
 * @author Harald Pehl
 */
class TestStep extends WizardStep<Context, State> {

    private static final String WIZARD_TITLE = "wizard-title";
    private static final String WIZARD_TEXT = "wizard-text";
    private static final String WIZARD_ERROR = "wizard-error";

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Environment environment;
    private final Provider<Progress> progress;
    private final Resources resources;
    private final Element root;

    TestStep(final Dispatcher dispatcher,
            final StatementContext statementContext,
            final Environment environment,
            final Provider<Progress> progress,
            final Resources resources) {

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
        // @formatter:off
        root = new Elements.Builder()
            .div()
                .div().innerHtml(description).end()
                .div().css(blankSlatePf)
                    .button(resources.constants().testConnection()).css(btn, btnLg, btnPrimary)
                        .on(click, event -> testConnection())
                    .end()
                .end()
            .end()
        .build();
        // @formatter:on
    }

    @Override
    public Element asElement() {
        return root;
    }

    private void testConnection() {
        Context context = wizard().getContext();
        DataSource dataSource = context.getDataSource();

        List<Function<FunctionContext>> functions = new ArrayList<>();
        if (!context.isCreated()) {
            // add data source
            functions.add(control -> {
                ResourceAddress address = dataSource.isXa()
                        ? XA_DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName())
                        : DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName());
                Operation operation = new Operation.Builder(address, ADD).payload(dataSource).build();
                dispatcher.executeInFunction(control, operation,
                        result -> {
                            context.setCreated(true);
                            control.proceed();
                        },
                        (op, failure) -> {
                            control.getContext().set(WIZARD_TITLE, resources.constants().testConnectionError());
                            control.getContext().set(WIZARD_TEXT, resources.messages().dataSourceAddError());
                            control.getContext().set(WIZARD_ERROR, failure);
                            control.abort();
                        });
            });
        }

        // check running server(s)
        functions.add(new TopologyFunctions.RunningServersQuery(
                environment, dispatcher, new ModelNode().set(PROFILE_NAME, statementContext.selectedProfile())));

        // test connection
        functions.add(control -> {
            List<Server> servers = control.getContext().get(TopologyFunctions.RUNNING_SERVERS);
            if (!servers.isEmpty()) {
                Server server = servers.get(0);
                ResourceAddress address = server.getServerAddress().add(SUBSYSTEM, DATASOURCES)
                        .add(DATA_SOURCE, dataSource.getName());
                Operation operation = new Operation.Builder(address, TEST_CONNECTION_IN_POOL).build();
                dispatcher.executeInFunction(control, operation,
                        result -> control.proceed(),
                        (op, failure) -> {
                            control.getContext().set(WIZARD_TITLE, resources.constants().testConnectionError());
                            control.getContext()
                                    .set(WIZARD_TEXT, resources.messages().testConnectionError(dataSource.getName()));
                            control.getContext().set(WIZARD_ERROR, failure);
                            control.abort();
                        });

            } else {
                control.getContext().set(WIZARD_TITLE, resources.constants().testConnectionError());
                control.getContext()
                        .set(WIZARD_TEXT, SafeHtmlUtils.fromString(resources.constants().noRunningServers()));
                control.abort();
            }
        });

        Outcome<FunctionContext> outcome = new Outcome<FunctionContext>() {
            @Override
            public void onFailure(final FunctionContext context) {
                String title = context.get(WIZARD_TITLE);
                SafeHtml text = context.get(WIZARD_TEXT);
                String error = context.get(WIZARD_ERROR);
                wizard().showError(title, text, error, false);
            }

            @Override
            public void onSuccess(final FunctionContext context) {
                wizard().showSuccess(resources.constants().testConnectionSuccess(),
                        resources.messages().testConnectionSuccess(dataSource.getName()), false);
            }
        };
        new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(), outcome,
                functions.toArray(new Function[functions.size()]));
    }
}
