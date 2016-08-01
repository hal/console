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
package org.jboss.hal.client.bootstrap.functions;

import java.util.List;
import javax.inject.Inject;

import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;

import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.QUERY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SELECT;

/**
 * Reads the domain controller. Only executed in domain mode. Depends on {@link ReadEnvironment}.
 *
 * @author Harald Pehl
 */
public class FindDomainController implements BootstrapFunction {

    private final Dispatcher dispatcher;
    private final Environment environment;

    @Inject
    public FindDomainController(final Dispatcher dispatcher,
            final Environment environment) {
        this.dispatcher = dispatcher;
        this.environment = environment;
    }

    @Override
    public void execute(final Control<FunctionContext> control) {
        logStart();
        if (environment.isStandalone()) {
            logDone();
            control.proceed();

        } else {
            Operation operation = new Operation.Builder(QUERY, new ResourceAddress().add(HOST, "*"))
                    .param(SELECT, new ModelNode().add(NAME))
                    .param(QUERY, new ModelNode().set("master", true))
                    .build();
            dispatcher.execute(operation, result -> {
                List<ModelNode> nodes = result.asList();
                if (!nodes.isEmpty()) {
                    if (!nodes.get(0).get(RESULT).isFailure()) {
                        String dc = nodes.get(0).get(RESULT).get(NAME).asString();
                        environment.setDomainController(dc);
                    }
                }
                logDone();
                control.proceed();
            });
        }
    }

    @Override
    public String name() {
        return "Bootstrap[FindDomainController]";
    }
}
