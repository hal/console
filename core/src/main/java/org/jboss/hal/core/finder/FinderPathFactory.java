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
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.subsystem.SubsystemMetadata;
import org.jboss.hal.core.subsystem.Subsystems;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;

/**
 * Factory to build common finder path instances for configuration and runtime presenters.
 *
 * @author Harald Pehl
 */
public class FinderPathFactory {

    private final Environment environment;
    private final StatementContext statementContext;
    private final Finder finder;
    private final Subsystems subsystems;
    private final Resources resources;

    @Inject
    public FinderPathFactory(final Environment environment,
            final StatementContext statementContext,
            final Finder finder,
            final Subsystems subsystems,
            final Resources resources) {
        this.environment = environment;
        this.statementContext = statementContext;
        this.finder = finder;
        this.subsystems = subsystems;
        this.resources = resources;
    }


    // ------------------------------------------------------ configuration

    /**
     * Create a finder path for the specified subsystem. Includes the selected profile when running in domain mode.
     */
    public FinderPath subsystemPath(String subsystem) {
        FinderPath path = new FinderPath();

        if (environment.isStandalone()) {
            path.append(Ids.CONFIGURATION, Ids.asId(Names.SUBSYSTEMS), Names.CONFIGURATION, Names.SUBSYSTEMS);
        } else {
            String profile = statementContext.selectedProfile();
            path.append(Ids.CONFIGURATION, Ids.asId(Names.PROFILES), Names.CONFIGURATION, Names.PROFILES)
                    .append(Ids.PROFILE, profile, Names.PROFILES, profile);

        }
        path.append(Ids.SUBSYSTEM, subsystem,
                Names.SUBSYSTEM, subsystemTitle(subsystem, subsystems.get(subsystem)));
        return path;
    }


    // ------------------------------------------------------ deployment

    public FinderPath content(final String content) {
        if (environment.isStandalone()) {
            // in standalone content and deployment are the same thing
            return deployment(content);
        } else {
            return new FinderPath()
                    .append(Ids.DEPLOYMENT_BROWSE_BY, Ids.asId(resources.constants().contentRepository()),
                            resources.constants().browseBy(), resources.constants().contentRepository())
                    .append(Ids.CONTENT, Ids.content(content),
                            resources.constants().content(), content);
        }
    }

    public FinderPath deployment(String deployment) {
        if (environment.isStandalone()) {
            return new FinderPath().append(Ids.DEPLOYMENT, Ids.deployment(deployment),
                    Names.DEPLOYMENT, deployment);
        } else {
            String serverGroup = statementContext.selectedServerGroup();
            return new FinderPath()
                    .append(Ids.DEPLOYMENT_BROWSE_BY, Ids.asId(Names.SERVER_GROUPS),
                            resources.constants().browseBy(), Names.SERVER_GROUPS)
                    .append(Ids.DEPLOYMENT_SERVER_GROUP, Ids.serverGroup(serverGroup),
                            Names.SERVER_GROUP, serverGroup)
                    .append(Ids.SERVER_GROUP_DEPLOYMENT, Ids.serverGroupDeployment(serverGroup, deployment),
                            Names.DEPLOYMENT, deployment);
        }
    }


    // ------------------------------------------------------ runtime

    /**
     * Creates a finder path for the selected host.
     */
    public FinderPath runtimeHostPath() {
        return runtimeHostPath(statementContext.selectedHost());
    }

    /**
     * Creates a finder path for the specified host.
     */
    public FinderPath runtimeHostPath(String host) {
        return new FinderPath()
                .append(Ids.DOMAIN_BROWSE_BY, Ids.asId(Names.HOSTS), resources.constants().browseBy(), Names.HOSTS)
                .append(Ids.HOST, Ids.host(host), Names.HOST, host);
    }

    /**
     * Creates a finder path for the selected server group.
     */
    public FinderPath runtimeServerGroupPath() {
        return runtimeServerGroupPath(statementContext.selectedServerGroup());
    }

    /**
     * Creates a finder path for the specified server group.
     */
    public FinderPath runtimeServerGroupPath(String serverGroup) {
        return new FinderPath()
                .append(Ids.DOMAIN_BROWSE_BY, Ids.asId(Names.SERVER_GROUPS),
                        resources.constants().browseBy(), Names.SERVER_GROUPS)
                .append(SERVER_GROUP, Ids.serverGroup(serverGroup), Names.SERVER_GROUP, serverGroup);
    }

    /**
     * Creates a finder path for the selected server. Adds the selected host / server group when running domain mode.
     */
    public FinderPath runtimeServerPath() {
        if (environment.isStandalone()) {
            return new FinderPath().append(Ids.STANDALONE_SERVER, Ids.server(Server.STANDALONE.getName()),
                    Names.SERVER, Names.STANDALONE_SERVER);
        } else {
            String server = statementContext.selectedServer();
            FinderPath path = browseByServerGroups() ? runtimeServerGroupPath() : runtimeHostPath();
            return path.append(Ids.SERVER, Ids.server(server), Names.SERVER, server);
        }
    }


    // ------------------------------------------------------ helpers

    private boolean browseByServerGroups() {
        if (!finder.getContext().getPath().isEmpty()) {
            FinderSegment firstSegment = finder.getContext().getPath().iterator().next();
            return firstSegment.getItemId().equals(Ids.asId(Names.SERVER_GROUPS));
        }
        return false;
    }

    private String subsystemTitle(String subsystem, SubsystemMetadata subsystemMetadata) {
        return subsystemMetadata != null ? subsystemMetadata.getTitle() : new LabelBuilder().label(subsystem);
    }
}
