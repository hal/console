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
package org.jboss.hal.client.homepage;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.ballroom.Popover.Placement;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.accesscontrol.AccessControl;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.mvp.TopLevelPresenter;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import rx.Single;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public class HomepagePresenter extends TopLevelPresenter<HomepagePresenter.MyView, HomepagePresenter.MyProxy> {

    private final AccessControl accessControl;
    private final Environment environment;
    private final PlaceManager placeManager;
    private final Places places;
    private final Resources resources;
    private Single<String[]> domain;
    private Tour tour;

    @Inject
    public HomepagePresenter(EventBus eventBus, MyView view, MyProxy proxy, AccessControl accessControl,
            Dispatcher dispatcher, Environment environment, PlaceManager placeManager, Places places,
            Resources resources) {
        super(eventBus, view, proxy);
        this.accessControl = accessControl;
        this.environment = environment;
        this.placeManager = placeManager;
        this.places = places;
        this.resources = resources;

        // define DRM operations (w/o executing them)
        if (environment.isStandalone()) {
            domain = Single.just(new String[2]);
        } else {
            // read domain values:
            // domain[0]: first profile
            // domain[1]: first server group
            List<Operation> operations = new ArrayList<>();
            operations.add(new Operation.Builder(ResourceAddress.root(), READ_CHILDREN_NAMES_OPERATION)
                    .param(CHILD_TYPE, PROFILE)
                    .build());
            operations.add(new Operation.Builder(ResourceAddress.root(), READ_CHILDREN_NAMES_OPERATION)
                    .param(CHILD_TYPE, SERVER_GROUP)
                    .build());
            domain = dispatcher.execute(new Composite(operations)).map(result -> {
                String[] values = new String[operations.size()];
                for (int i = 0; i < operations.size(); i++) {
                    List<ModelNode> nodes = result.step(i).get(RESULT).asList();
                    if (!nodes.isEmpty()) {
                        values[i] = nodes.get(0).asString();
                    }
                }
                return values;
            });
        }
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    void launchGuidedTour() {
        if (tour == null) {
            // execute the DRM operations and use the values for domain mode
            domain.subscribe(values -> {
                tour = new Tour(getEventBus(), placeManager, resources);

                // place requests used for both standalone and domain mode
                PlaceRequest homepage = new PlaceRequest.Builder().nameToken(NameTokens.HOMEPAGE).build();
                PlaceRequest deployments = new PlaceRequest.Builder().nameToken(NameTokens.DEPLOYMENTS).build();
                PlaceRequest runtime = new PlaceRequest.Builder().nameToken(NameTokens.RUNTIME).build();

                if (environment.isStandalone()) {
                    // place requests for standalone mode
                    PlaceRequest configuration = places.finderPlace(NameTokens.CONFIGURATION,
                            new FinderPath().append(Ids.CONFIGURATION, Ids.asId(Names.SUBSYSTEMS))).build();
                    String serverId = Ids.hostServer(Server.STANDALONE.getHost(),
                            environment.getInstanceInfo().serverName());
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

                } else {
                    // place requests for domain mode
                    PlaceRequest deploymentsContentRepository = places.finderPlace(NameTokens.DEPLOYMENTS,
                            new FinderPath().append(Ids.DEPLOYMENT_BROWSE_BY,
                                    Ids.asId(resources.constants().contentRepository()))).build();
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
                    if (values[0] != null) {
                        PlaceRequest profileSubsystems = places.finderPlace(NameTokens.CONFIGURATION,
                                new FinderPath().append(Ids.CONFIGURATION, Ids.asId(Names.PROFILES))
                                        .append(Ids.PROFILE, values[0])).build();
                        tour.addStep(profileSubsystems, Ids.CONFIGURATION_SUBSYSTEM, Names.CONFIGURATION,
                                resources.messages().tourDomainConfigurationSubsystem(), Placement.RIGHT);
                    }
                    tour.addStep(runtime, Ids.DOMAIN_BROWSE_BY, Names.RUNTIME,
                            resources.messages().tourDomainRuntimeBrowseBy(), Placement.RIGHT);
                    tour.addStep(serverGroups, Ids.SERVER_GROUP, Names.RUNTIME,
                            resources.messages().tourDomainRuntimeServerGroup(), Placement.RIGHT);
                    tour.addStep(serverGroups, Ids.SERVER_GROUP_ADD, Names.RUNTIME,
                            resources.messages().tourDomainRuntimeServerGroupsAdd(), Placement.BOTTOM);
                    if (values[1] != null) {
                        PlaceRequest firstServerGroup = places.finderPlace(NameTokens.RUNTIME,
                                new FinderPath().append(Ids.DOMAIN_BROWSE_BY, Ids.asId(Names.SERVER_GROUPS))
                                        .append(Ids.SERVER_GROUP, Ids.serverGroup(values[1]))).build();
                        tour.addStep(firstServerGroup, Ids.SERVER, Names.RUNTIME,
                                resources.messages().tourDomainRuntimeServer(), Placement.RIGHT);
                        tour.addStep(firstServerGroup, Ids.SERVER_ADD, Names.RUNTIME,
                                resources.messages().tourDomainRuntimeServerAdd(), Placement.BOTTOM);
                    }
                }

                // Steps for access control apply to both standalone and domain mode
                if (accessControl.isSuperUserOrAdministrator() && !accessControl.isSingleSignOn()) {
                    PlaceRequest browseBy = new PlaceRequest.Builder().nameToken(NameTokens.ACCESS_CONTROL).build();
                    PlaceRequest users = places.finderPlace(NameTokens.ACCESS_CONTROL,
                            new FinderPath().append(Ids.ACCESS_CONTROL_BROWSE_BY,
                                    Ids.asId(resources.constants().users()))).build();
                    PlaceRequest roles = places.finderPlace(NameTokens.ACCESS_CONTROL,
                            new FinderPath().append(Ids.ACCESS_CONTROL_BROWSE_BY,
                                    Ids.asId(resources.constants().roles()))).build();
                    tour.addStep(browseBy, Ids.ACCESS_CONTROL_BROWSE_BY, Names.ACCESS_CONTROL,
                            resources.messages().tourAccessControl(), Placement.RIGHT);
                    tour.addStep(users, Ids.USER, Names.ACCESS_CONTROL,
                            resources.messages().tourAccessControlUsers(), Placement.RIGHT);
                    tour.addStep(roles, Ids.ROLE, Names.ACCESS_CONTROL,
                            resources.messages().tourAccessControlRoles(), Placement.RIGHT);
                }
                tour.start();
            });

        } else {
            if (!tour.running()) {
                tour.start();
            }
        }
    }


    // @formatter:off
    @ProxyStandard
    @NameToken(NameTokens.HOMEPAGE)
    public interface MyProxy extends ProxyPlace<HomepagePresenter> {
    }

    public interface MyView extends HalView, HasPresenter<HomepagePresenter> {
    }
    // @formatter:on
}
