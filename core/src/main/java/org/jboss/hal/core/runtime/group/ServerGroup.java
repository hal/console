/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.core.runtime.group;

import java.util.List;
import java.util.Map;

import org.jboss.hal.core.runtime.HasServersNode;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;

import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;

public class ServerGroup extends HasServersNode {

    public static void addServers(List<ServerGroup> serverGroups, List<Server> servers) {
        if (serverGroups != null && servers != null) {
            Map<String, List<Server>> serversByServerGroup = servers.stream()
                    .collect(groupingBy(Server::getServerGroup));
            for (ServerGroup serverGroup : serverGroups) {
                List<Server> serversOfServerGroup = serversByServerGroup
                        .getOrDefault(serverGroup.getName(), emptyList());
                serversOfServerGroup.forEach(serverGroup::addServer);
            }
        }
    }

    public ServerGroup(final String name, final ModelNode node) {
        super(name, node);
    }

    public ServerGroup(final Property property) {
        super(property);
    }

    public String getProfile() {
        return get(PROFILE).asString();
    }

    public ResourceAddress getAddress() {
        return new ResourceAddress().add(SERVER_GROUP, getName());
    }
}
