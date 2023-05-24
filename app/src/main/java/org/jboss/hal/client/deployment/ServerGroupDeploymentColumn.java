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
package org.jboss.hal.client.deployment;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.hal.client.deployment.DeploymentTasks.LoadDeploymentsFromRunningServer;
import org.jboss.hal.client.deployment.DeploymentTasks.ReadServerGroupDeployments;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.deployment.Deployment.Status;
import org.jboss.hal.core.deployment.ServerGroupDeployment;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.ItemsProvider;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.SecurityContextRegistry;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.stream.Collectors.toList;

import static org.jboss.hal.client.deployment.AbstractDeploymentColumn.DEPLOYMENT_ADDRESS;
import static org.jboss.hal.client.deployment.AbstractDeploymentColumn.SELECTED_SERVER_GROUP_DEPLOYMENT_ADDRESS;
import static org.jboss.hal.core.deployment.Deployment.Status.OK;
import static org.jboss.hal.core.runtime.TopologyTasks.runningServers;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DISABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;
import static org.jboss.hal.dmr.ModelNodeHelper.properties;
import static org.jboss.hal.flow.Flow.sequential;

/** The deployments of a server group. */
@AsyncColumn(Ids.SERVER_GROUP_DEPLOYMENT)
@Requires(value = { DEPLOYMENT_ADDRESS, SELECTED_SERVER_GROUP_DEPLOYMENT_ADDRESS }, recursive = false)
public class ServerGroupDeploymentColumn extends AbstractDeploymentColumn<ServerGroupDeployment> {

    @Inject
    public ServerGroupDeploymentColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            ItemActionFactory itemActionFactory,
            Environment environment,
            EventBus eventBus,
            Dispatcher dispatcher,
            Places places,
            CrudOperations crud,
            ServerActions serverActions,
            StatementContext statementContext,
            MetadataRegistry metadataRegistry,
            SecurityContextRegistry securityContextRegistry,
            @Footer Provider<Progress> progress,
            Resources resources) {

        super(new FinderColumn.Builder<ServerGroupDeployment>(finder, Ids.SERVER_GROUP_DEPLOYMENT, Names.DEPLOYMENT)
                .useFirstActionAsBreadcrumbHandler()
                .pinnable()
                .showCount()
                .withFilter(),
                columnActionFactory, crud, dispatcher, environment, eventBus, itemActionFactory,
                metadataRegistry, securityContextRegistry, progress, resources, statementContext);

        ItemsProvider<ServerGroupDeployment> itemsProvider = finderContext -> {
            List<Task<FlowContext>> tasks = new ArrayList<>();
            tasks.add(new ReadServerGroupDeployments(environment, dispatcher, statementContext.selectedServerGroup()));
            tasks.addAll(runningServers(environment, dispatcher,
                    properties(SERVER_GROUP, statementContext.selectedServerGroup())));
            tasks.add(new LoadDeploymentsFromRunningServer(environment, dispatcher));
            return sequential(new FlowContext(progress.get()), tasks)
                    .then(flowContext -> {
                        List<ServerGroupDeployment> serverGroupDeployments = flowContext
                                .get(DeploymentTasks.SERVER_GROUP_DEPLOYMENTS);
                        return Promise.resolve(serverGroupDeployments);
                    });
        };
        setItemsProvider(itemsProvider);

        // reuse the items provider to filter breadcrumb items
        setBreadcrumbItemsProvider(context -> itemsProvider.items(context)
                .then(result -> {
                    // only running deployments w/ a reference server will show up in the breadcrumb dropdown
                    List<ServerGroupDeployment> deploymentsOnServer = result.stream()
                            .filter(ServerGroupDeployment::runningWithReferenceServer)
                            .collect(toList());
                    return Promise.resolve(deploymentsOnServer);

                }));

        setItemRenderer(item -> new ItemDisplay<ServerGroupDeployment>() {
            @Override
            public String getId() {
                return Ids.serverGroupDeployment(statementContext.selectedServerGroup(), item.getName());
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public String getTooltip() {
                if (item.getDeployment() != null) {
                    if (item.getDeployment().getStatus() == Status.FAILED) {
                        return resources.constants().failed();
                    } else if (item.getDeployment().getStatus() == Status.STOPPED) {
                        return resources.constants().stopped();
                    } else if (item.getDeployment().getStatus() == OK) {
                        return resources.constants().activeLower();
                    } else {
                        return resources.constants().unknownState();
                    }
                } else {
                    return item.isEnabled() ? resources.constants().enabled()
                            : resources.constants()
                                    .disabled();
                }
            }

            @Override
            public HTMLElement getIcon() {
                if (item.getDeployment() != null) {
                    if (item.getDeployment().getStatus() == Status.FAILED) {
                        return Icons.error();
                    } else if (item.getDeployment().getStatus() == Status.STOPPED) {
                        return Icons.stopped();
                    } else if (item.getDeployment().getStatus() == OK) {
                        return Icons.ok();
                    } else {
                        return Icons.unknown();
                    }
                } else {
                    return item.isEnabled() ? Icons.ok() : Icons.disabled();
                }
            }

            @Override
            public String getFilterData() {
                return item.getName() + " " + (item.isEnabled() ? ENABLED : DISABLED);
            }

            @Override
            public List<ItemAction<ServerGroupDeployment>> actions() {
                return getItemActions(item);
            }
        });

        setPreviewCallback(item -> new ServerGroupDeploymentPreview(this, item, places, resources, serverActions,
                environment));
    }

    @Override
    ColumnProps getColumnProps() {
        return SERVER_GROUP_COLUMN;
    }
}
