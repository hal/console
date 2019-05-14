/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.core.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;

import static java.util.stream.Collectors.toList;

public abstract class HasServersNode extends NamedNode {

    private final List<Server> servers;

    public HasServersNode(String name, ModelNode node) {
        super(name, node);
        this.servers = new ArrayList<>();
    }

    public HasServersNode(Property property) {
        super(property);
        this.servers = new ArrayList<>();
    }

    public boolean hasServers() {
        return !servers.isEmpty();
    }

    public boolean hasServers(Predicate<Server> predicate) {
        return servers.stream().anyMatch(predicate);
    }

    public void addServer(Server server) {
        servers.add(server);
    }

    public List<Server> getServers() {
        return servers;
    }

    public List<Server> getServers(Predicate<Server> predicate) {
        return servers.stream().filter(predicate).collect(toList());
    }
}
