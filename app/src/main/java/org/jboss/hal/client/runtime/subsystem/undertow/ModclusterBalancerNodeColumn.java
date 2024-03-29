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
package org.jboss.hal.client.runtime.subsystem.undertow;

import javax.inject.Inject;

import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderSegment;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.AsyncColumn;

import elemental2.promise.Promise;

import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.MODCLUSTER_BALANCER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.BALANCER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MODCLUSTER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NODE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDERTOW;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

@AsyncColumn(Ids.UNDERTOW_RUNTIME_MODCLUSTER_BALANCER_NODE)
public class ModclusterBalancerNodeColumn extends FinderColumn<NamedNode> {

    @Inject
    public ModclusterBalancerNodeColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            Dispatcher dispatcher,
            StatementContext statementContext) {

        super(new Builder<NamedNode>(finder, Ids.UNDERTOW_RUNTIME_MODCLUSTER_BALANCER_NODE, Names.NODE)
                .columnAction(columnActionFactory.refresh(Ids.UNDERTOW_MODCLUSTER_BALANCER_NODE_REFRESH))
                .itemsProvider(context -> {
                    String modcluster = "";
                    String balancer = "";

                    FinderSegment<?> modclusterSegment = context.getPath().findColumn(Ids.UNDERTOW_RUNTIME_MODCLUSTER);
                    if (modclusterSegment != null) {
                        modcluster = Ids.extractUndertowModcluster(modclusterSegment.getItemId());
                    }

                    FinderSegment<?> balancerSegment = context.getPath().findColumn(Ids.UNDERTOW_RUNTIME_MODCLUSTER_BALANCER);
                    if (balancerSegment != null) {
                        balancer = Ids.extractUndertowModclusterBalancer(balancerSegment.getItemId());
                    }

                    ResourceAddress address = MODCLUSTER_BALANCER_TEMPLATE.resolve(statementContext, modcluster,
                            balancer);
                    Operation operation = new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                            .param(CHILD_TYPE, NODE)
                            .param(INCLUDE_RUNTIME, true)
                            .build();
                    return dispatcher.execute(operation)
                            .then(result -> Promise.resolve(asNamedNodes(result.asPropertyList())));
                })
                .itemRenderer(item -> new ItemDisplay<NamedNode>() {
                    @Override
                    public String getId() {
                        return Ids.build(UNDERTOW, MODCLUSTER, BALANCER, NODE, item.getName());
                    }

                    @Override
                    public String getTitle() {
                        return item.getName();
                    }

                    /*
                     * @Override public String nextColumn() { return Ids.UNDERTOW_RUNTIME_MODCLUSTER_BALANCER_NODE_CONTEXT; }
                     */
                })
                .onPreview(ModclusterBalancerNodePreview::new));
    }
}
