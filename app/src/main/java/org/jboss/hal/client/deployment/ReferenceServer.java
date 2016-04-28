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
package org.jboss.hal.client.deployment;

import org.jboss.hal.dmr.model.ResourceAddress;

import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;

/**
 * The server which is used to load the runtime data of a {@link Deployment}.
 * TODO Replace with Server
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
