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
package org.jboss.hal.client.runtime.subsystem.ejb;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental2.dom.HTMLElement;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;

import static org.jboss.hal.client.runtime.subsystem.ejb.AddressTemplates.EJB3_DEPLOYMENT_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.ejb.AddressTemplates.EJB3_SUBDEPLOYMENT_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

@AsyncColumn(Ids.EJB3)
public class EjbColumn extends FinderColumn<EjbNode> {

    @Inject
    public EjbColumn(Finder finder,
            FinderPathFactory finderPathFactory,
            ItemActionFactory itemActionFactory,
            Places places,
            Dispatcher dispatcher,
            StatementContext statementContext,
            Resources resources) {
        super(new Builder<EjbNode>(finder, Ids.EJB3, Names.EJB3)

                .itemsProvider((context, callback) -> {
                    List<Operation> operations = new ArrayList<>();
                    AddressTemplate[] templates = new AddressTemplate[]{
                            EJB3_DEPLOYMENT_TEMPLATE,
                            EJB3_SUBDEPLOYMENT_TEMPLATE
                    };
                    for (EjbNode.Type type : EjbNode.Type.values()) {
                        for (AddressTemplate template : templates) {
                            ResourceAddress deploymentAddress = template
                                    .append(type.resource + "=*")
                                    .resolve(statementContext);
                            operations.add(new Operation.Builder(deploymentAddress, READ_RESOURCE_OPERATION)
                                    .param(INCLUDE_RUNTIME, true)
                                    .param(RECURSIVE, true)
                                    .build());
                        }
                    }
                    dispatcher.execute(new Composite(operations), (CompositeResult result) -> {
                        List<EjbNode> ejbs = new ArrayList<>();
                        for (ModelNode step : result) {
                            if (!step.isFailure()) {
                                for (ModelNode node : step.get(RESULT).asList()) {
                                    ResourceAddress address = new ResourceAddress(node.get(ADDRESS));
                                    ejbs.add(new EjbNode(address, node.get(RESULT)));
                                }
                            }
                        }
                        ejbs.sort(Comparator.comparing(NamedNode::getName));
                        callback.onSuccess(ejbs);
                    });
                })

                .itemRenderer(item -> new ItemDisplay<EjbNode>() {
                    @Override
                    public String getId() {
                        return Ids.ejb3(item.getDeployment(), item.getSubdeployment(), item.type.name(),
                                item.getName());
                    }

                    @Override
                    public String getTitle() {
                        return item.getName();
                    }

                    @Override
                    public HTMLElement asElement() {
                        return ItemDisplay.withSubtitle(item.getName(), item.type.type);
                    }

                    @Override
                    public String getFilterData() {
                        return item.getName() + " " + item.type.type;
                    }

                    @Override
                    public List<ItemAction<EjbNode>> actions() {
                        List<ItemAction<EjbNode>> actions = new ArrayList<>();
                        PlaceRequest.Builder builder = places.selectedServer(NameTokens.EJB3_RUNTIME)
                                .with(DEPLOYMENT, item.getDeployment());
                        if (item.getSubdeployment() != null) {
                            builder.with(SUBDEPLOYMENT, item.getSubdeployment());
                        }
                        PlaceRequest placeRequest = builder
                                .with(TYPE, item.type.name().toLowerCase())
                                .with(NAME, item.getName())
                                .build();
                        actions.add(itemActionFactory.view(placeRequest));
                        return actions;
                    }
                })

                .onPreview(item -> new EjbPreview(item, finderPathFactory, places, dispatcher, resources))

                .useFirstActionAsBreadcrumbHandler()
                .withFilter()
                .showCount()
                .pinnable());
    }
}
