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

import javax.inject.Inject;

import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.ManagementModel;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.capabilitiy.Capability;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CAPABILITY_REGISTRY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CORE_SERVICE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

/**
 * Reads the capabilities from the capability registry. Depends on {@link FindDomainController}.
 *
 * @author Harald Pehl
 */
public class ReadCapabilities implements BootstrapFunction {

    private static final String POSSIBLE_CAPABILITIES = "possible-capabilities";

    private final Environment environment;
    private final Dispatcher dispatcher;
    private final Capabilities capabilities;

    @Inject
    public ReadCapabilities(final Environment environment,
            final Dispatcher dispatcher,
            final Capabilities capabilities) {
        this.environment = environment;
        this.dispatcher = dispatcher;
        this.capabilities = capabilities;
    }

    @Override
    public void execute(final Control<FunctionContext> control) {
        logStart();

        if (!ManagementModel.supportsCapabilitiesRegistry(environment.getManagementVersion())) {
            logger.warn("Skip {}: Capabilities registry is not supported for management model version {}!", name(),
                    environment.getManagementVersion());
            // TODO Register some well-known capabilities as fall back
            logDone();
            control.proceed();

        } else {
            ResourceAddress capabilityRegistry = new ResourceAddress();
            if (!environment.isStandalone()) {
                capabilityRegistry.add(HOST, environment.getDomainController());
            }
            capabilityRegistry.add(CORE_SERVICE, CAPABILITY_REGISTRY);
            Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION, capabilityRegistry).build();
            dispatcher.execute(operation, result -> {
                if (result.hasDefined(POSSIBLE_CAPABILITIES)) {
                    for (ModelNode capabilityNode : result.get(POSSIBLE_CAPABILITIES).asList()) {
                        Capability capability = new Capability(capabilityNode);
                        capabilities.register(capability);
                        logger.debug("Registered {}", capability);
                    }
                }
                logDone();
                control.proceed();
            });
        }
    }

    @Override
    public String name() {
        return "Bootstrap[ReadCapabilities]";
    }
}
