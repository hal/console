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
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.capabilitiy.Capabilities;

/**
 * Registers core capabilities from https://github.com/wildfly/wildfly-capabilities and other well-known capabilities.
 *
 * @author Harald Pehl
 */
public class RegisterCoreCapabilities implements BootstrapFunction {

    private final Capabilities capabilities;

    @Inject
    public RegisterCoreCapabilities(Capabilities capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    @SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
    public void execute(final Control<FunctionContext> control) {
        logStart();

        // Selected capabilities from https://github.com/wildfly/wildfly-capabilities
        capabilities.register("org.wildfly.data-source", true,
                AddressTemplate.of("/{selected.profile}/subsystem=datasources/data-source=*"));
        capabilities.register("org.wildfly.io.buffer-pool", true,
                AddressTemplate.of("/{selected.profile}/subsystem=io/buffer-pool=*"));
        capabilities.register("org.wildfly.io.worker", true,
                AddressTemplate.of("/{selected.profile}/subsystem=io/worker=*"));
        capabilities.register("org.wildfly.remoting.connector", true,
                AddressTemplate.of("/{selected.profile}/subsystem=remoting/connector=*"));
        capabilities.register("org.wildfly.remoting.outbound-connection", true,
                AddressTemplate.of("/{selected.profile}/subsystem=remoting/local-outbound-connection=*"));
        capabilities.register("org.wildfly.remoting.outbound-connection", true,
                AddressTemplate.of("/{selected.profile}/subsystem=remoting/remote-outbound-connection=*"));

        // Well-known capabilities not (yet) on https://github.com/wildfly/wildfly-capabilities
        capabilities.register("org.wildfly.network.interface", true, AddressTemplate.of("/interface=*"));
        capabilities.register("org.wildfly.domain.profile", true, AddressTemplate.of("/profile=*"));
        capabilities.register("org.wildfly.domain.server-group", true, AddressTemplate.of("/server-group=*"));
        capabilities.register("org.wildfly.domain.socket-binding-group", true,
                AddressTemplate.of("/socket-binding-group=*"));

        // Lists local and remote outbound socket bindings of all socket binding groups
        capabilities.register("org.wildfly.network.outbound-socket-binding", true,
                AddressTemplate.of("/socket-binding-group=*/local-destination-outbound-socket-binding=*"),
                AddressTemplate.of("/socket-binding-group=*/remote-destination-outbound-socket-binding=*"));

        // Lists all socket bindings of all socket binding groups
        capabilities.register("org.wildfly.network.socket-binding", true,
                AddressTemplate.of("/socket-binding-group=*/socket-binding=*"));

        logDone();
        control.proceed();
    }

    @Override
    public String name() {
        return "Bootstrap[RegisterCoreCapabilities]";
    }
}
