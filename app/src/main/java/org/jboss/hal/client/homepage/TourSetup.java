/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.homepage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jboss.hal.ballroom.Popover.Placement;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.accesscontrol.AccessControl;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

class TourSetup {

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final Environment environment;
    private final PlaceManager placeManager;
    private final Places places;
    private final AccessControl accessControl;
    private final Resources resources;

    TourSetup(EventBus eventBus, Dispatcher dispatcher, Environment environment, PlaceManager placeManager,
            Places places, AccessControl accessControl, Resources resources) {
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.environment = environment;
        this.placeManager = placeManager;
        this.places = places;
        this.accessControl = accessControl;
        this.resources = resources;
    }

    void initTour(Consumer<Tour> consumer) {
        Tour tour = new Tour(eventBus, placeManager, resources);

        // place requests used for both standalone and domain mode
        PlaceRequest homepage = new PlaceRequest.Builder().nameToken(NameTokens.HOMEPAGE).build();
        PlaceRequest deployments = new PlaceRequest.Builder().nameToken(NameTokens.DEPLOYMENTS).build();
        PlaceRequest runtime = new PlaceRequest.Builder().nameToken(NameTokens.RUNTIME).build();

        if (environment.isStandalone()) {
            standaloneSteps(tour, homepage, deployments, runtime);
            commonSteps(tour);
            consumer.accept(tour);

        } else {
            // read domain values: first profile, first server group
            List<Operation> operations = new ArrayList<>();
            operations.add(new Operation.Builder(ResourceAddress.root(), READ_CHILDREN_NAMES_OPERATION)
                    .param(CHILD_TYPE, PROFILE)
                    .build());
            operations.add(new Operation.Builder(ResourceAddress.root(), READ_CHILDREN_NAMES_OPERATION)
                    .param(CHILD_TYPE, SERVER_GROUP)
                    .build());
            dispatcher.execute(new Composite(operations), (CompositeResult result) -> {
                List<ModelNode> profiles = result.step(0).get(RESULT).asList();
                String profile = profiles.isEmpty() ? null : profiles.get(0).asString();
                List<ModelNode> serverGroups = result.step(1).get(RESULT).asList();
                String serverGroup = serverGroups.isEmpty() ? null : serverGroups.get(0).asString();

                domainSteps(tour, homepage, deployments, runtime, profile, serverGroup);
                commonSteps(tour);
                consumer.accept(tour);
            });
        }
    }

    private void standaloneSteps(Tour tour, PlaceRequest homepage, PlaceRequest deployments, PlaceRequest runtime) {
        // place requests for standalone mode
        PlaceRequest configuration = places.finderPlace(NameTokens.CONFIGURATION,
                new FinderPath().append(Ids.CONFIGURATION, Ids.asId(Names.SUBSYSTEMS))).build();
        String serverId = Ids.hostServer(Server.STANDALONE.getHost(), environment.getName());
        PlaceRequest monitor = places.finderPlace(NameTokens.RUNTIME,
                new FinderPath().append(Ids.STANDALONE_SERVER_COLUMN, serverId)).build();

        // steps for standalone mode
        tour.addStep(homepage, Ids.HOMEPAGE_DEPLOYMENTS_MODULE, Names.HOMEPAGE,
                resources.messages().tourStandaloneHomeDeployments(), Placement.RIGHT);
        tour.addStep(homepage, Ids.HOMEPAGE_CONFIGURATION_MODULE, Names.HOMEPAGE,
                resources.messages().tourStandaloneHomeConfiguration(), Placement.LEFT);
        tour.addStep(homepage, Ids.HOMEPAGE_RUNTIME_MODULE, Names.HOMEPAGE,
                resources.messages().tourStandaloneHomeRuntime(), Placement.RIGHT);
        tour.addStep(deployments, Ids.DEPLOYMENT, Names.DEPLOYMENT,
                resources.messages().tourStandaloneDeployment(), Placement.RIGHT);
        tour.addStep(deployments, Ids.DEPLOYMENT_ADD_ACTIONS, Names.DEPLOYMENT,
                resources.messages().tourStandaloneDeploymentAddActions(), Placement.BOTTOM);
        tour.addStep(configuration, Ids.CONFIGURATION_SUBSYSTEM, Names.CONFIGURATION,
                resources.messages().tourStandaloneConfigurationSubsystem(), Placement.RIGHT);
        tour.addStep(runtime, Ids.STANDALONE_SERVER_COLUMN, Names.RUNTIME,
                resources.messages().tourStandaloneRuntimeServer(), Placement.RIGHT);
        tour.addStep(monitor, Ids.RUNTIME_SUBSYSTEM, Names.RUNTIME,
                resources.messages().tourStandaloneRuntimeSubsystem(), Placement.RIGHT);

    }

    private void domainSteps(Tour tour, PlaceRequest homepage, PlaceRequest deployments, PlaceRequest runtime,
            String profile, String serverGroup) {
        // place requests for domain mode
        PlaceRequest deploymentsContentRepository = places.finderPlace(NameTokens.DEPLOYMENTS,
                new FinderPath().append(Ids.DEPLOYMENT_BROWSE_BY,
                        Ids.asId(resources.constants().contentRepository())))
                .build();
        PlaceRequest profiles = places.finderPlace(NameTokens.CONFIGURATION,
                new FinderPath().append(Ids.CONFIGURATION, Ids.asId(Names.PROFILES))).build();
        PlaceRequest serverGroups = places.finderPlace(NameTokens.RUNTIME,
                new FinderPath().append(Ids.DOMAIN_BROWSE_BY, Ids.asId(Names.SERVER_GROUPS))).build();

        // steps for domain mode
        tour.addStep(homepage, Ids.HOMEPAGE_DEPLOYMENTS_MODULE, Names.HOMEPAGE,
                resources.messages().tourDomainHomeDeployments(), Placement.RIGHT);
        tour.addStep(homepage, Ids.HOMEPAGE_CONFIGURATION_MODULE, Names.HOMEPAGE,
                resources.messages().tourDomainHomeConfiguration(), Placement.LEFT);
        tour.addStep(homepage, Ids.HOMEPAGE_RUNTIME_MODULE, Names.HOMEPAGE,
                resources.messages().tourDomainHomeRuntime(), Placement.RIGHT);
        tour.addStep(deployments, Ids.DEPLOYMENT_BROWSE_BY, Names.DEPLOYMENT,
                resources.messages().tourDomainDeploymentsBrowseBy(), Placement.RIGHT);
        tour.addStep(deploymentsContentRepository, Ids.CONTENT_ADD_ACTIONS, Names.DEPLOYMENT,
                resources.messages().tourDomainDeploymentsAddActions(), Placement.BOTTOM);
        tour.addStep(profiles, Ids.PROFILE, Names.CONFIGURATION,
                resources.messages().tourDomainConfiguration(), Placement.RIGHT);
        if (profile != null) {
            PlaceRequest profileSubsystems = places.finderPlace(NameTokens.CONFIGURATION,
                    new FinderPath().append(Ids.CONFIGURATION, Ids.asId(Names.PROFILES))
                            .append(Ids.PROFILE, profile))
                    .build();
            tour.addStep(profileSubsystems, Ids.CONFIGURATION_SUBSYSTEM, Names.CONFIGURATION,
                    resources.messages().tourDomainConfigurationSubsystem(), Placement.RIGHT);
        }
        tour.addStep(runtime, Ids.DOMAIN_BROWSE_BY, Names.RUNTIME,
                resources.messages().tourDomainRuntimeBrowseBy(), Placement.RIGHT);
        tour.addStep(serverGroups, Ids.SERVER_GROUP, Names.RUNTIME,
                resources.messages().tourDomainRuntimeServerGroup(), Placement.RIGHT);
        tour.addStep(serverGroups, Ids.SERVER_GROUP_ADD, Names.RUNTIME,
                resources.messages().tourDomainRuntimeServerGroupsAdd(), Placement.BOTTOM);
        if (serverGroup != null) {
            PlaceRequest firstServerGroup = places.finderPlace(NameTokens.RUNTIME,
                    new FinderPath().append(Ids.DOMAIN_BROWSE_BY, Ids.asId(Names.SERVER_GROUPS))
                            .append(Ids.SERVER_GROUP, Ids.serverGroup(serverGroup)))
                    .build();
            tour.addStep(firstServerGroup, Ids.SERVER, Names.RUNTIME,
                    resources.messages().tourDomainRuntimeServer(), Placement.RIGHT);
            tour.addStep(firstServerGroup, Ids.SERVER_ADD, Names.RUNTIME,
                    resources.messages().tourDomainRuntimeServerAdd(), Placement.BOTTOM);
        }
    }

    private void commonSteps(Tour tour) {
        // steps for access control apply to both standalone and domain mode
        if (accessControl.isSuperUserOrAdministrator() && !accessControl.isSingleSignOn()) {
            PlaceRequest browseBy = new PlaceRequest.Builder().nameToken(NameTokens.ACCESS_CONTROL).build();
            PlaceRequest users = places.finderPlace(NameTokens.ACCESS_CONTROL,
                    new FinderPath().append(Ids.ACCESS_CONTROL_BROWSE_BY,
                            Ids.ACCESS_CONTROL_BROWSE_BY_USERS))
                    .build();
            PlaceRequest roles = places.finderPlace(NameTokens.ACCESS_CONTROL,
                    new FinderPath().append(Ids.ACCESS_CONTROL_BROWSE_BY,
                            Ids.ACCESS_CONTROL_BROWSE_BY_ROLES))
                    .build();
            tour.addStep(browseBy, Ids.ACCESS_CONTROL_BROWSE_BY, Names.ACCESS_CONTROL,
                    resources.messages().tourAccessControl(), Placement.RIGHT);
            tour.addStep(users, Ids.USER, Names.ACCESS_CONTROL,
                    resources.messages().tourAccessControlUsers(), Placement.RIGHT);
            tour.addStep(roles, Ids.ROLE, Names.ACCESS_CONTROL,
                    resources.messages().tourAccessControlRoles(), Placement.RIGHT);
        }
    }
}
