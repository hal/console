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
package org.jboss.hal.client.runtime.subsystem.jgroups;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.client.runtime.BrowseByColumn;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Requires;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.promise.Promise;

import static org.jboss.hal.client.runtime.subsystem.jgroups.AddressTemplates.CHANNEL_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.jgroups.AddressTemplates.JGROUPS_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.jgroups.AddressTemplates.JGROUPS_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHANNEL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

@AsyncColumn(Ids.JGROUPS_CHANNEL_RUNTIME)
@Requires(JGROUPS_ADDRESS)
public class ChannelColumn extends FinderColumn<NamedNode> {

    @Inject
    public ChannelColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            Dispatcher dispatcher,
            MetadataRegistry metadataRegistry,
            StatementContext statementContext,
            ItemActionFactory itemActionFactory) {

        super(new Builder<NamedNode>(finder, Ids.JGROUPS_CHANNEL_RUNTIME, Names.CHANNELS)

                .columnAction(columnActionFactory.refresh(Ids.build(Ids.JGROUPS_CHANNEL_RUNTIME, Ids.REFRESH)))

                .itemsProvider(context -> {
                    ResourceAddress ra = JGROUPS_TEMPLATE.resolve(statementContext);
                    Operation op = new Operation.Builder(ra, READ_CHILDREN_RESOURCES_OPERATION)
                            .param(CHILD_TYPE, CHANNEL)
                            .param(INCLUDE_RUNTIME, true)
                            .build();
                    return dispatcher.execute(op)
                            .then(result -> Promise.resolve(asNamedNodes(result.asPropertyList())));
                })

                .itemRenderer(item -> new ItemDisplay<NamedNode>() {
                    @Override
                    public String getId() {
                        return Ids.build(CHANNEL, item.getName());
                    }

                    @Override
                    public String getTitle() {
                        return item.getName();
                    }

                    @Override
                    public String getFilterData() {
                        return item.getName();
                    }

                    @Override
                    public List<ItemAction<NamedNode>> actions() {
                        // if the service is not started there is nothing to show
                        if (!item.hasDefined(ADDRESS)) {
                            return Collections.emptyList();
                        }
                        PlaceRequest.Builder builder = new PlaceRequest.Builder()
                                .nameToken(NameTokens.JGROUPS_CHANNEL);
                        if (BrowseByColumn.browseByServerGroups(finder.getContext())) {
                            builder.with(SERVER_GROUP, statementContext.selectedServerGroup());
                        } else {
                            builder.with(HOST, statementContext.selectedHost());
                        }
                        builder.with(SERVER, statementContext.selectedServer())
                                .with(NAME, item.getName());
                        return List.of(itemActionFactory.view(builder.build()));
                    }
                })

                .onPreview(item -> new ChannelPreview(item, metadataRegistry.lookup(CHANNEL_TEMPLATE)))
                .useFirstActionAsBreadcrumbHandler()
                .withFilter()
                .showCount()
                .pinnable());
    }
}
