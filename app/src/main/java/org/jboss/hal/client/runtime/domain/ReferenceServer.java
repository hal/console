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
package org.jboss.hal.client.runtime.domain;

/**
 * Keeps information about the host/server names for resources which are only valid if they have a relation to a running
 * server on an active host.
 *
 * @author Harald Pehl
 */
public class ReferenceServer {

    private final String host;
    private final String server;

    public ReferenceServer(final String host, final String server) {
        this.host = host;
        this.server = server;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof ReferenceServer)) { return false; }

        ReferenceServer that = (ReferenceServer) o;

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
        return host + "/" + server;
    }

    public String getHost() {
        return host;
    }

    public String getServer() {
        return server;
    }
}
