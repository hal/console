/*
 * Copyright 2015-2018 Red Hat, Inc, and individual contributors.
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
package org.jboss.hal.client.runtime.sslwizard;

import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.wizard.AsyncStep;
import org.jboss.hal.ballroom.wizard.WorkflowCallback;
import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public class ReviewStep extends AbstractConfiguration implements AsyncStep<EnableSSLContext> {

    private Dispatcher dispatcher;
    private StatementContext statementContext;
    private HTMLElement section;
    private Environment environment;

    ReviewStep(Dispatcher dispatcher, StatementContext statementContext, Resources resources, Environment environment) {
        super(resources.constants().review(), environment, false);
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;

        section = section()
                .add(p().textContent(resources.messages().enableSSLConfirmationDescription()))
                .add(form)
                .asElement();
        this.environment = environment;
    }

    @Override
    protected void onShow(final EnableSSLContext context) {
        super.onShow(context);
        form.view(context.model);
    }

    @Override
    public void onNext(EnableSSLContext context, WorkflowCallback callback) {
        if (environment.isStandalone()) {
            // read the port attribute associated to the socket-binding-group/socket-binding
            // the user had chosen
            Operation readSbgOp = new Operation.Builder(ResourceAddress.root(), READ_CHILDREN_NAMES_OPERATION)
                    .param(CHILD_TYPE, SOCKET_BINDING_GROUP)
                    .build();
            dispatcher.execute(readSbgOp, result -> {
                String sbg = result.asList().get(0).asString();
                String httpsBinding = context.model.get(SECURE_SOCKET_BINDING).asString();
                ResourceAddress address = SOCKET_BINDING_GROUP_TEMPLATE.resolve(statementContext, sbg, httpsBinding);
                Operation readPort = new Operation.Builder(address, READ_ATTRIBUTE_OPERATION)
                        .param(NAME, PORT)
                        .param(RESOLVE_EXPRESSIONS, true)
                        .build();
                dispatcher.execute(readPort, portResult -> {
                    context.securePort = portResult.asInt();
                    callback.proceed();
                });
            });
        } else {
            callback.proceed();
        }
    }

    @Override
    public HTMLElement asElement() {
        return section;
    }
}
