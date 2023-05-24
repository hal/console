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

import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.group.ServerGroup;
import org.jboss.hal.core.runtime.group.ServerGroupSelectionEvent;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;

@AsyncColumn(Ids.DEPLOYMENT_SERVER_GROUP)
@Requires("/server-group=*")
public class ServerGroupColumn extends FinderColumn<ServerGroup> {

    @Inject
    public ServerGroupColumn(Finder finder,
            Dispatcher dispatcher,
            EventBus eventBus,
            PlaceManager placeManager,
            Places places,
            Resources resources) {

        super(new FinderColumn.Builder<ServerGroup>(finder, Ids.DEPLOYMENT_SERVER_GROUP, Names.SERVER_GROUP)

                // TODO Change the security context (server group scoped roles!)
                .onItemSelect(serverGroup -> eventBus.fireEvent(new ServerGroupSelectionEvent(serverGroup.getName())))

                .onBreadcrumbItem((item, context) -> {
                    // switch server group in place request parameter of specific presenter
                    PlaceRequest current = placeManager.getCurrentPlaceRequest();
                    PlaceRequest update = places.replaceParameter(current, SERVER_GROUP, item.getName()).build();
                    placeManager.revealPlace(update);
                })

                .pinnable()
                .withFilter()
                .filterDescription(resources.messages().serverGroupColumnFilterDescription()));

        setItemRenderer(item -> new ItemDisplay<ServerGroup>() {
            @Override
            public String getId() {
                return Ids.serverGroup(item.getName());
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public HTMLElement element() {
                return ItemDisplay.withSubtitle(item.getName(), item.getProfile());
            }

            @Override
            public String getFilterData() {
                return String.join(" ", item.getName(), item.getProfile());
            }

            @Override
            public String nextColumn() {
                return Ids.SERVER_GROUP_DEPLOYMENT;
            }
        });

        setItemsProvider(context -> {
            Operation serverGroupsOp = new Operation.Builder(ResourceAddress.root(), READ_CHILDREN_RESOURCES_OPERATION)
                    .param(CHILD_TYPE, SERVER_GROUP)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            Operation deploymentsOp = new Operation.Builder(
                    new ResourceAddress().add(SERVER_GROUP, "*").add(DEPLOYMENT, "*"), READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            return dispatcher.execute(new Composite(serverGroupsOp, deploymentsOp)).then(result -> {
                List<ServerGroup> serverGroups = result.step(0).get(RESULT).asPropertyList().stream()
                        .map(ServerGroup::new)
                        .sorted(comparing(ServerGroup::getName))
                        .collect(toList());
                return Promise.resolve(serverGroups);
            });
        });

        setPreviewCallback(item -> new ServerGroupPreview(item, resources));
    }
}
