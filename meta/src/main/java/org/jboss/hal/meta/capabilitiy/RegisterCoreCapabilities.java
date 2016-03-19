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
package org.jboss.hal.meta.capabilitiy;

import org.jboss.hal.meta.AddressTemplate;

import javax.inject.Inject;

/**
 * Registers core capabilities from https://github.com/wildfly/wildfly-capabilities and other well-known capabilities.
 *
 * @author Harald Pehl
 */
public class RegisterCoreCapabilities {

    @Inject
    @SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
    public RegisterCoreCapabilities(Capabilities capabilities) {
        // Keep the capabilities of each section in alphabetical order

        // Selected capabilities from https://github.com/wildfly/wildfly-capabilities
        // @formatter:off
        capabilities.add("org.wildfly.data-source", AddressTemplate.of("/{selected.profile}/subsystem=datasources/data-source=*"));
        capabilities.add("org.wildfly.io.buffer-pool", AddressTemplate.of("/{selected.profile}/subsystem=io/buffer-pool=*"));
        capabilities.add("org.wildfly.io.worker", AddressTemplate.of("/{selected.profile}/subsystem=io/worker=*"));
        capabilities.add("org.wildfly.network.outbound-socket-binding", AddressTemplate.of("/socket-binding-group=*/local-destination-outbound-socket-binding=*"));
        capabilities.add("org.wildfly.network.outbound-socket-binding", AddressTemplate.of("/socket-binding-group=*/remote-destination-outbound-socket-binding=*"));
        capabilities.add("org.wildfly.network.socket-binding", AddressTemplate.of("/socket-binding-group=*/socket-binding=*"));
        capabilities.add("org.wildfly.remoting.connector", AddressTemplate.of("/{selected.profile}/subsystem=remoting/connector=*"));
        capabilities.add("org.wildfly.remoting.outbound-connection", AddressTemplate.of("/{selected.profile}/subsystem=remoting/local-outbound-connection=*"));
        capabilities.add("org.wildfly.remoting.outbound-connection", AddressTemplate.of("/{selected.profile}/subsystem=remoting/remote-outbound-connection=*"));
        // @formatter:on

        // Well-known capabilities not (yet) on https://github.com/wildfly/wildfly-capabilities
        // @formatter:off
        capabilities.add("org.wildfly.network.interface", AddressTemplate.of("/interface=*"));
        capabilities.add("org.wildfly.domain.profile", AddressTemplate.of("/profile=*"));
        capabilities.add("org.wildfly.domain.server-group", AddressTemplate.of("/server-group=*"));
        capabilities.add("org.wildfly.domain.socket-binding-group", AddressTemplate.of("/socket-binding-group=*"));
        // @formatter:on
    }
}
