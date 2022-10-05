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
package org.jboss.hal.client.configuration.subsystem.infinispan;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.ColumnAction;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.CACHE_CONTAINER_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.CACHE_CONTAINER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.INFINISPAN_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.REMOTE_CACHE_CONTAINER_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.REMOTE_CACHE_CONTAINER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.REMOTE_CLUSTER_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.REMOTE_CLUSTER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CACHE_CONTAINER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT_CACHE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT_REMOTE_CLUSTER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OPERATIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOTE_CACHE_CONTAINER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDINGS;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;

@AsyncColumn(Ids.CACHE_CONTAINER)
@Requires(value = { CACHE_CONTAINER_ADDRESS, REMOTE_CACHE_CONTAINER_ADDRESS, REMOTE_CLUSTER_ADDRESS }, recursive = false)
public class CacheContainerColumn extends FinderColumn<CacheContainer> {

    private final Dispatcher dispatcher;
    private final CrudOperations crud;
    private final EventBus eventBus;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private final Resources resources;

    @Inject
    public CacheContainerColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            ItemActionFactory itemActionFactory,
            Dispatcher dispatcher,
            CrudOperations crud,
            Places places,
            EventBus eventBus,
            MetadataRegistry metadataRegistry,
            StatementContext statementContext,
            Resources resources) {

        super(new Builder<CacheContainer>(finder, Ids.CACHE_CONTAINER, Names.CACHE_CONTAINER)

                .itemsProvider(context -> crud
                        .readChildren(INFINISPAN_SUBSYSTEM_TEMPLATE, asList(CACHE_CONTAINER, REMOTE_CACHE_CONTAINER))
                        .then(result -> {
                            List<CacheContainer> cc = new ArrayList<>();
                            for (Property property : result.step(0).get(RESULT).asPropertyList()) {
                                cc.add(new CacheContainer(property.getName(), false, property.getValue()));
                            }
                            for (Property property : result.step(1).get(RESULT).asPropertyList()) {
                                cc.add(new CacheContainer(property.getName(), true, property.getValue()));
                            }
                            cc.sort(comparing(NamedNode::getName));
                            return Promise.resolve(cc);
                        }))

                .onPreview(CacheContainerPreview::new)
                .useFirstActionAsBreadcrumbHandler()
                .withFilter()
                .showCount());

        this.dispatcher = dispatcher;
        this.crud = crud;
        this.eventBus = eventBus;
        this.metadataRegistry = metadataRegistry;
        this.statementContext = statementContext;
        this.resources = resources;

        List<ColumnAction<CacheContainer>> addActions = new ArrayList<>();
        addActions.add(new ColumnAction.Builder<CacheContainer>(Ids.CACHE_CONTAINER_ADD)
                .title(resources.messages().addResourceTitle(Names.CACHE_CONTAINER))
                .handler(column -> addCacheContainer())
                .constraint(Constraint.executable(CACHE_CONTAINER_TEMPLATE, ADD))
                .build());
        addActions.add(new ColumnAction.Builder<CacheContainer>(Ids.REMOTE_CACHE_CONTAINER_ADD)
                .title(resources.messages().addResourceTitle(Names.REMOTE_CACHE_CONTAINER))
                .handler(column -> addRemoteCacheContainer())
                .constraint(Constraint.executable(REMOTE_CACHE_CONTAINER_TEMPLATE, ADD))
                .build());
        addColumnActions(Ids.CACHE_CONTAINER_ADD_ACTIONS, pfIcon(UIConstants.ADD_CIRCLE_O), resources.constants().add(),
                addActions);
        addColumnAction(columnActionFactory.refresh(Ids.CACHE_CONTAINER_REFRESH));

        setItemRenderer(item -> new ItemDisplay<CacheContainer>() {
            @Override
            public String getId() {
                return item.isRemote() ? Ids.remoteCacheContainer(item.getName()) : Ids.cacheContainer(item.getName());
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public HTMLElement getIcon() {
                return item.isRemote() ? Icons.custom(fontAwesome("cloud")) : Icons.custom(pfIcon("memory"));
            }

            @Override
            public HTMLElement element() {
                if (item.isRemote()) {
                    return item.hasDefined(DEFAULT_REMOTE_CLUSTER) ? ItemDisplay.withSubtitle(item.getName(),
                            item.get(DEFAULT_REMOTE_CLUSTER).asString()) : null;
                } else {
                    return item.hasDefined(DEFAULT_CACHE) ? ItemDisplay.withSubtitle(item.getName(),
                            item.get(DEFAULT_CACHE).asString()) : null;
                }
            }

            @Override
            public String getTooltip() {
                return item.isRemote() ? Names.REMOTE_CACHE_CONTAINER : Names.CACHE_CONTAINER;
            }

            @Override
            public String getFilterData() {
                String name = item.getName();
                return item.isRemote() ? name + " remote" : name;
            }

            @Override
            public List<ItemAction<CacheContainer>> actions() {
                if (item.isRemote()) {
                    return asList(itemActionFactory.viewAndMonitor(Ids.remoteCacheContainer(item.getName()),
                            places.selectedProfile(NameTokens.REMOTE_CACHE_CONTAINER)
                                    .with(NAME, item.getName())
                                    .build()),
                            itemActionFactory.remove(Names.REMOTE_CACHE_CONTAINER, item.getName(),
                                    REMOTE_CACHE_CONTAINER_TEMPLATE, CacheContainerColumn.this));
                } else {
                    return asList(
                            itemActionFactory.viewAndMonitor(Ids.cacheContainer(item.getName()),
                                    places.selectedProfile(NameTokens.CACHE_CONTAINER)
                                            .with(NAME, item.getName())
                                            .build()),
                            itemActionFactory.remove(Names.CACHE_CONTAINER, item.getName(),
                                    CACHE_CONTAINER_TEMPLATE, CacheContainerColumn.this));
                }
            }

            @Override
            public String nextColumn() {
                return item.isRemote() ? null : Ids.CACHE;
            }
        });
    }

    private void addCacheContainer() {
        crud.add(Ids.CACHE_CONTAINER_ADD, Names.CACHE_CONTAINER, CACHE_CONTAINER_TEMPLATE,
                createUniqueValidationFromFilteredItems(item -> !item.isRemote()),
                (name, address) -> refresh(Ids.cacheContainer(name)));
    }

    private void addRemoteCacheContainer() {
        Metadata rccMetadata = metadataRegistry.lookup(REMOTE_CACHE_CONTAINER_TEMPLATE);
        Metadata rcMetadata = metadataRegistry.lookup(REMOTE_CLUSTER_TEMPLATE);

        // add nested 'socket-bindings' attribute from 'remote-cluster' resource to top level metadata
        String path = OPERATIONS + "/" + ADD + "/" + REQUEST_PROPERTIES;
        Property socketBindingsDescription = rcMetadata.getDescription().findAttribute(path, SOCKET_BINDINGS);
        failSafeGet(rccMetadata.getDescription(), path)
                .get(SOCKET_BINDINGS)
                .set(socketBindingsDescription.getValue());
        ModelNode socketBindingsPermissions = failSafeGet(rcMetadata.getSecurityContext(),
                ATTRIBUTES + "/" + SOCKET_BINDINGS);
        failSafeGet(rccMetadata.getSecurityContext(), ATTRIBUTES)
                .get(SOCKET_BINDINGS)
                .set(socketBindingsPermissions);

        AddResourceDialog dialog = new AddResourceDialog(Ids.REMOTE_CACHE_CONTAINER_FORM,
                resources.messages().addResourceTitle(Names.REMOTE_CACHE_CONTAINER), rccMetadata,
                asList(DEFAULT_REMOTE_CLUSTER, SOCKET_BINDINGS), (name, model) -> {
                    String rcName = model.get(DEFAULT_REMOTE_CLUSTER).asString();
                    ModelNode socketBindings = model.remove(SOCKET_BINDINGS);
                    ResourceAddress rccAddress = REMOTE_CACHE_CONTAINER_TEMPLATE.resolve(statementContext, name);
                    ResourceAddress rcAddress = REMOTE_CLUSTER_TEMPLATE.resolve(statementContext, name, rcName);
                    List<Operation> operations = asList(new Operation.Builder(rccAddress, ADD).payload(model).build(),
                            new Operation.Builder(rcAddress, ADD).param(SOCKET_BINDINGS, socketBindings).build());
                    dispatcher.execute(new Composite(operations), (CompositeResult result) -> {
                        MessageEvent.fire(eventBus,
                                Message.success(resources.messages().addResourceSuccess(Names.REMOTE_CACHE_CONTAINER, name)));
                        refresh(Ids.remoteCacheContainer(name));
                    });
                });
        dialog.getForm().<String> getFormItem(NAME).addValidationHandler(
                createUniqueValidationFromFilteredItems(CacheContainer::isRemote));
        dialog.show();
    }
}
