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
package org.jboss.hal.core.runtime.group;

import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerConfigStatus;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.resources.IdBuilder;

import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;

/**
 * @author Harald Pehl
 */
public class ServerGroup extends NamedNode {

    public static String id(final String name) {
        return IdBuilder.build(SERVER_GROUP, name);
    }

    private final Multimap<ServerConfigStatus, Server> serversByState;

    public ServerGroup(final ModelNode node) {
        super(node);
        this.serversByState = ArrayListMultimap.create();
    }

    public ServerGroup(final Property property) {
        super(property);
        this.serversByState = ArrayListMultimap.create();
    }

    public String getProfile() {
        return get(PROFILE).asString();
    }

    public boolean hasServers(ServerConfigStatus status) {
        return serversByState.containsKey(status);
    }

    public void addServer(ServerConfigStatus status, Server server) {
        serversByState.put(status, server);
    }

    public List<Server> getServers(ServerConfigStatus status) {
        return Lists.newArrayList(serversByState.get(status));
    }

    public ResourceAddress getAddress() {
        return new ResourceAddress().add(SERVER_GROUP, getName());
    }
}
