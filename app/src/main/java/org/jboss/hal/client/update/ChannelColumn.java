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
package org.jboss.hal.client.update;

import java.util.List;

import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Column;

import elemental2.promise.Promise;
import javax.inject.Inject;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.update.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHANNELS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

@Column(Ids.UPDATE_CHANNEL)
public class ChannelColumn extends FinderColumn<NamedNode> {

    @Inject
    public ChannelColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final StatementContext statementContext,
            final Dispatcher dispatcher) {
        super(new Builder<NamedNode>(finder, Ids.UPDATE_CHANNEL, Names.CHANNELS)
                .columnAction(columnActionFactory.refresh(Ids.UPDATE_CHANNEL_REFRESH))
                .onPreview(ChannelPreview::new)
                .showCount()
                .withFilter());

        setItemsProvider(context -> {
            ResourceAddress address = INSTALLER_TEMPLATE.resolve(statementContext);
            Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            return dispatcher.execute(operation).then(result -> {
                List<NamedNode> channels = result.get(CHANNELS).asList().stream().map(NamedNode::new).collect(toList());
                return Promise.resolve(channels);
            });
        });

        setItemRenderer(item -> new ItemDisplay<NamedNode>() {
            @Override
            public String getTitle() {
                return item.getName();
            }
        });
    }
}
