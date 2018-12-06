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
package org.jboss.hal.client.configuration.subsystem.modcluster;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import elemental2.dom.HTMLElement;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.configuration.subsystem.modcluster.AddressTemplates.MODCLUSTER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.modcluster.AddressTemplates.PROXY_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.modcluster.AddressTemplates.PROXY_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.build;

@AsyncColumn(Ids.MODCLUSTER_PROXY)
@Requires(PROXY_ADDRESS)
public class ProxyColumn extends FinderColumn<NamedNode> {

    @Inject
    protected ProxyColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            ItemActionFactory itemActionFactory,
            EventBus eventBus,
            Dispatcher dispatcher,
            StatementContext statementContext,
            MetadataRegistry metadataRegistry,
            Places places,
            Resources resources) {

        super(new Builder<NamedNode>(finder, Ids.MODCLUSTER_PROXY, Names.PROXY)
                .withFilter()
                .filterDescription(resources.messages().proxyColumnFilterDescription())
                .useFirstActionAsBreadcrumbHandler());

        setItemsProvider((context, callback) -> {
            ResourceAddress address = MODCLUSTER_TEMPLATE.resolve(statementContext);
            Operation op = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                    .param(RECURSIVE, true).build();

            dispatcher.execute(op, result -> {
                List<NamedNode> proxies = result.get(PROXY).asPropertyList().stream()
                        .map(NamedNode::new)
                        .collect(toList());
                callback.onSuccess(proxies);
            });
        });

        addColumnAction(columnActionFactory.add(Ids.MODCLUSTER_PROXY_ADD, Names.PROXY, PROXY_TEMPLATE,
                column -> {
                    String id = build(Ids.MODCLUSTER_PROXY, ADD, FORM);
                    Metadata metadata = metadataRegistry.lookup(PROXY_TEMPLATE);
                    AddResourceDialog dialog = new AddResourceDialog(id,
                            resources.messages().addResourceTitle(Names.PROXY), metadata,
                            (name, modelNode) -> {
                                if (modelNode != null) {
                                    ResourceAddress address = AddressTemplates.PROXY_TEMPLATE
                                            .resolve(statementContext, name);
                                    Operation operation = new Operation.Builder(address, ADD)
                                            .param(PROXY, name)
                                            .payload(modelNode)
                                            .build();
                                    dispatcher.execute(operation, result -> {
                                        MessageEvent.fire(eventBus,
                                                Message.success(resources.messages()
                                                        .addResourceSuccess(Names.PROXY, name)));
                                        column.refresh(Ids.modclusterProxy(name));
                                    });
                                }
                            });
                    dialog.show();
                }));
        addColumnAction(columnActionFactory.refresh(Ids.MODCLUSTER_PROXY_REFRESH));

        setItemRenderer(proxyNode -> new ItemDisplay<NamedNode>() {
            @Override
            public String getId() {
                return Ids.modclusterProxy(proxyNode.getName());
            }

            @Override
            public String getTitle() {
                return proxyNode.getName();
            }

            @Override
            public HTMLElement element() {
                return ItemDisplay.withSubtitle(proxyNode.getName(), proxyNode.get(CONNECTOR).asString());
            }

            @Override
            public String getFilterData() {
                List<String> data = new ArrayList<>();
                data.add(proxyNode.getName());
                data.add(proxyNode.get(CONNECTOR).asString());
                return String.join(" ", data);
            }

            @Override
            public List<ItemAction<NamedNode>> actions() {
                List<ItemAction<NamedNode>> actions = new ArrayList<>();
                actions.add(itemActionFactory.view(places.selectedProfile(NameTokens.MODCLUSTER)
                        .with(NAME, proxyNode.getName()).build()));
                actions.add(itemActionFactory.remove(Names.PROXY, proxyNode.getName(),
                        AddressTemplates.PROXY_TEMPLATE, ProxyColumn.this));
                return actions;
            }
        });

        setPreviewCallback(proxy -> new ProxyPreview(proxy));
    }
}
