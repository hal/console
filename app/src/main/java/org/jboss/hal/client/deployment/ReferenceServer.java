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
package org.jboss.hal.client.deployment;

import org.jboss.hal.dmr.model.ResourceAddress;

import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;

/**
 * The server which is used to load the runtime data of a {@link Deployment}.
 *
 * @author Harald Pehl
 */
public class ReferenceServer {

    @SuppressWarnings("HardCodedStringLiteral")
    public final static ReferenceServer STANDALONE = new ReferenceServer(
            ReferenceServer.class.getName() + ".standalone",
            ReferenceServer.class.getName() + ".server");

    private final String host;
    private final String server;

    public ReferenceServer(final String host, final String server) {
        this.server = server;
        this.host = host;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof ReferenceServer)) { return false; }

        ReferenceServer that = (ReferenceServer) o;
        //noinspection SimplifiableIfStatement
        if (!host.equals(that.host)) { return false; }
        return server.equals(that.server);

    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + server.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return isStandalone() ? "ReferenceServer{STANDALONE}" : "ReferenceServer{" + host + "/" + server + "}";
    }

    public String getHost() {
        return host;
    }

    public String getServer() {
        return server;
    }

    public ResourceAddress getAddress() {
        if (isStandalone()) {
            return ResourceAddress.ROOT;
        }
        return new ResourceAddress().add(HOST, host).add(SERVER, server);
    }

    boolean isStandalone() {return this == STANDALONE;}
}
