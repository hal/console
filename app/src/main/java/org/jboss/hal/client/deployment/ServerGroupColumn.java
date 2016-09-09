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
package org.jboss.hal.client.deployment;

import java.util.List;
import javax.inject.Inject;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import elemental.dom.Element;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.runtime.group.ServerGroup;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Requires;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
@AsyncColumn(Ids.DEPLOYMENT_SERVER_GROUP)
@Requires(value = "/server-group=*")
public class ServerGroupColumn extends FinderColumn<ServerGroup> {

    private final Multiset<String> deploymentsPerServerGroup;

    @Inject
    public ServerGroupColumn(final Finder finder,
            final Dispatcher dispatcher,
            final Resources resources) {

        super(new FinderColumn.Builder<ServerGroup>(finder, Ids.DEPLOYMENT_SERVER_GROUP, Names.SERVER_GROUP)
                .pinnable()
                .showCount()
                .withFilter());
        this.deploymentsPerServerGroup = HashMultiset.create();

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
            public Element asElement() {
                return ItemDisplay.withSubtitle(item.getName(),
                        resources.messages().deployments(deploymentsPerServerGroup.count(item.getName())));
            }

            @Override
            public String getFilterData() {
                return Joiner.on(' ')
                        .join(item.getName(), String.valueOf(deploymentsPerServerGroup.count(item.getName())));
            }

            @Override
            public String nextColumn() {
                return Ids.ASSIGNED_DEPLOYMENT;
            }
        });

        setItemsProvider((context, callback) -> {
            Operation serverGroupsOp = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION,
                    ResourceAddress.ROOT)
                    .param(CHILD_TYPE, SERVER_GROUP)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            Operation deploymentsOp = new Operation.Builder(READ_RESOURCE_OPERATION,
                    new ResourceAddress().add(SERVER_GROUP, "*").add(DEPLOYMENT, "*"))
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            dispatcher.execute(new Composite(serverGroupsOp, deploymentsOp), (CompositeResult result) -> {
                List<ServerGroup> serverGroups = result.step(0).get(RESULT).asPropertyList().stream()
                        .map(ServerGroup::new)
                        .sorted(comparing(ServerGroup::getName))
                        .collect(toList());

                deploymentsPerServerGroup.clear();
                result.step(1).get(RESULT).asList().stream()
                        .filter(modelNode -> !modelNode.isFailure() && modelNode.hasDefined(ADDRESS))
                        .forEach(modelNode -> {
                            ResourceAddress serverGroupAddress = new ResourceAddress(modelNode.get(ADDRESS));
                            //noinspection ResultOfMethodCallIgnored
                            deploymentsPerServerGroup.add(serverGroupAddress.firstValue());
                        });

                callback.onSuccess(serverGroups);
            });
        });

        setPreviewCallback(
                item -> new ServerGroupPreview(item, deploymentsPerServerGroup.count(item.getName()), resources));
    }
}
