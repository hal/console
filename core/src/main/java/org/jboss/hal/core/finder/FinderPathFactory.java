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
package org.jboss.hal.core.finder;

import javax.inject.Inject;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.core.runtime.group.ServerGroup;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.subsystem.SubsystemMetadata;
import org.jboss.hal.core.subsystem.Subsystems;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Factory to build finder path instances for configuration and runtime presenters.
 *
 * @author Harald Pehl
 */
public class FinderPathFactory {

    private final Subsystems subsystems;
    private final StatementContext statementContext;
    private final Finder finder;
    private final Resources resources;

    @Inject
    public FinderPathFactory(final Subsystems subsystems,
            final StatementContext statementContext,
            final Finder finder,
            final Resources resources) {
        this.subsystems = subsystems;
        this.statementContext = statementContext;
        this.finder = finder;
        this.resources = resources;
    }


    // ------------------------------------------------------ configuration

    public FinderPath configurationSubsystemPath(String subsystem) {
        FinderPath path = new FinderPath();
        String profile = statementContext.selectedProfile();

        if (profile == null) {
            path.append(CONFIGURATION, Names.SUBSYSTEMS.toLowerCase(), Names.CONFIGURATION, Names.SUBSYSTEMS);
        } else {
            path.append(CONFIGURATION, Names.PROFILES.toLowerCase(), Names.CONFIGURATION, Names.PROFILES)
                    .append(PROFILE, profile, Names.PROFILES);
        }
        path.append(Ids.CONFIGURATION_SUBSYSTEM_COLUMN, subsystem, Names.SUBSYSTEM, subsystemTitle(subsystem));
        return path;
    }


    // ------------------------------------------------------ runtime

    public FinderPath runtimeHostPath() {
        String host = statementContext.selectedHost();
        return new FinderPath()
                .append(Ids.DOMAIN_BROWSE_BY_COLUMN, IdBuilder.asId(Names.HOSTS),
                        resources.constants().browseBy(), Names.HOSTS)
                .append(HOST, Host.id(host), Names.HOST, host);
    }

    public FinderPath runtimeServerGroupPath() {
        String serverGroup = statementContext.selectedServerGroup();
        return new FinderPath()
                .append(Ids.DOMAIN_BROWSE_BY_COLUMN, IdBuilder.asId(Names.SERVER_GROUPS),
                        resources.constants().browseBy(), Names.SERVER_GROUPS)
                .append(SERVER_GROUP, ServerGroup.id(serverGroup), Names.SERVER_GROUP, serverGroup);
    }

    public FinderPath runtimeServerPath() {
        String server = statementContext.selectedServer();
        FinderPath path = browseByHosts() ? runtimeHostPath() : runtimeServerGroupPath();
        return path.append(SERVER, Server.id(server), Names.SERVER, server);
    }

    public FinderPath runtimeSubsystemPath(String subsystem) {
        return runtimeServerPath()
                .append(Ids.RUNTIME_SUBSYSTEM_COLUMN, subsystem, Names.SUBSYSTEM, subsystemTitle(subsystem));
    }


    // ------------------------------------------------------ helpers

    private boolean browseByHosts() {
        if (!finder.getContext().getPath().isEmpty()) {
            FinderSegment firstSegment = finder.getContext().getPath().iterator().next();
            return firstSegment.getValue().equals(IdBuilder.asId(Names.HOSTS));
        }
        return false;
    }

    private boolean browseByServerGroups() {
        if (!finder.getContext().getPath().isEmpty()) {
            FinderSegment firstSegment = finder.getContext().getPath().iterator().next();
            return firstSegment.getValue().equals(IdBuilder.asId(Names.SERVER_GROUPS));
        }
        return false;
    }

    private String subsystemTitle(String subsystem) {
        SubsystemMetadata subsystemMetadata = subsystems.getConfigurationSubsystem(subsystem);
        return subsystemMetadata != null ? subsystemMetadata.getTitle() : new LabelBuilder().label(subsystem);
    }
}
