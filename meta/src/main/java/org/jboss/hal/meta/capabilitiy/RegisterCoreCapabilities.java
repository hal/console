/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
