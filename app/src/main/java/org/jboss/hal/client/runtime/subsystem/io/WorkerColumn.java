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
package org.jboss.hal.client.runtime.subsystem.io;

import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Requires;

import static org.jboss.gwt.elemento.core.Elements.small;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.hal.client.runtime.subsystem.io.AddressTemplates.WORKER_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.io.AddressTemplates.WORKER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.resources.CSS.itemText;

@AsyncColumn(Ids.WORKER)
@Requires(WORKER_ADDRESS)
public class WorkerColumn extends FinderColumn<NamedNode> {

    private static final String SLASH = " / ";

    @Inject
    public WorkerColumn(Finder finder,
            Dispatcher dispatcher,
            StatementContext statementContext) {

        super(new FinderColumn.Builder<NamedNode>(finder, Ids.WORKER, Names.WORKER)
                .itemsProvider((context, callback) -> {
                    ResourceAddress address = WORKER_TEMPLATE.getParent().resolve(statementContext);
                    Operation operation = new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                            .param(CHILD_TYPE, WORKER)
                            .param(INCLUDE_RUNTIME, true)
                            .param(RECURSIVE, true)
                            .build();
                    dispatcher.execute(operation, result -> callback.onSuccess(asNamedNodes(result.asPropertyList())));
                })
                .itemRenderer(item -> new ItemDisplay<NamedNode>() {
                    @Override
                    public String getTitle() {
                        return item.getName();
                    }

                    @Override
                    public HTMLElement element() {
                        LabelBuilder labelBuilder = new LabelBuilder();
                        String threadPool = item.get(CORE_POOL_SIZE).asInt() + SLASH +
                                item.get(MAX_POOL_SIZE).asInt() + SLASH +
                                item.get(TASK_MAX_THREADS).asInt();
                        String threadPoolTitle = labelBuilder.label(CORE_POOL_SIZE) + SLASH +
                                labelBuilder.label(MAX_POOL_SIZE) + SLASH +
                                labelBuilder.label(TASK_MAX_THREADS);
                        return span().css(itemText)
                                .add(span().textContent(item.getName()))
                                .add(small().css(CSS.subtitle).title(threadPoolTitle).textContent(threadPool))
                                .get();
                    }
                })
                .onPreview(item -> new WorkerPreview(item, dispatcher, statementContext))
        );
    }
}
