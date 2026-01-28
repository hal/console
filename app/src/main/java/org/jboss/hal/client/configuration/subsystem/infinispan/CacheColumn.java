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
import javax.inject.Provider;

import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.ColumnAction;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderSegment;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.ResourceCheck;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.CACHE_CONTAINER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.DISTRIBUTED_CACHE_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.INVALIDATION_CACHE_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.LOCAL_CACHE_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.REPLICATED_CACHE_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.SCATTERED_CACHE_ADDRESS;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.CLEAR_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CACHE_CONTAINER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.flow.Flow.sequential;
import static org.jboss.hal.resources.CSS.pfIcon;

@AsyncColumn(Ids.CACHE)
@Requires(value = { DISTRIBUTED_CACHE_ADDRESS,
        INVALIDATION_CACHE_ADDRESS,
        LOCAL_CACHE_ADDRESS,
        REPLICATED_CACHE_ADDRESS,
        SCATTERED_CACHE_ADDRESS }, recursive = false)
public class CacheColumn extends FinderColumn<Cache> {

    private static String findCacheContainer(FinderPath path) {
        FinderSegment<?> segment = path.findColumn(Ids.CACHE_CONTAINER);
        if (segment != null) {
            return Ids.extractCacheContainer(segment.getItemId());
        }
        return null;
    }

    private static final String JGROUPS_ADDITION_STATUS = "jgrupsAdditionStatus";

    private final CrudOperations crud;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private final Resources resources;
    private final Dispatcher dispatcher;
    private final Provider<Progress> progress;
    private final EventBus eventBus;

    @Inject
    public CacheColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            ItemActionFactory itemActionFactory,
            CrudOperations crud,
            Places places,
            MetadataRegistry metadataRegistry,
            StatementContext statementContext,
            Resources resources,
            Dispatcher dispatcher,
            @Footer Provider<Progress> progress,
            EventBus eventBus) {

        super(new Builder<Cache>(finder, Ids.CACHE, Names.CACHE)
                .itemsProvider(context -> new Promise<>((resolve, reject) -> {
                    String cacheContainer = findCacheContainer(context.getPath());
                    if (cacheContainer != null) {
                        CacheType[] cacheTypes = CacheType.values();
                        ResourceAddress address = CACHE_CONTAINER_TEMPLATE.resolve(statementContext, cacheContainer);
                        List<String> children = stream(cacheTypes).map(CacheType::resource).collect(toList());
                        crud.readChildren(address, children, 1, result -> {
                            List<Cache> caches = new ArrayList<>();
                            for (int i = 0; i < result.size(); i++) {
                                List<Property> properties = result.step(i).get(RESULT).asPropertyList();
                                for (Property property : properties) {
                                    caches.add(new Cache(property.getName(), cacheTypes[i], property.getValue()));
                                }
                            }
                            // Collections.sort(caches, (c1, c2) -> c1.getName().compareTo(c2.getName()));
                            caches.sort(comparing(NamedNode::getName));
                            resolve.onInvoke(caches);
                        });
                    } else {
                        resolve.onInvoke(emptyList());
                    }
                }))
                .onPreview(CachePreview::new)
                .pinnable()
                .showCount()
                .useFirstActionAsBreadcrumbHandler()
                .withFilter());
        this.crud = crud;
        this.metadataRegistry = metadataRegistry;
        this.statementContext = statementContext;
        this.resources = resources;
        this.dispatcher = dispatcher;
        this.progress = progress;
        this.eventBus = eventBus;

        List<ColumnAction<Cache>> addActions = new ArrayList<>();
        for (CacheType cacheType : CacheType.values()) {
            addActions.add(new ColumnAction.Builder<Cache>(Ids.build(cacheType.baseId, Ids.ADD))
                    .title(resources.messages().addResourceTitle(cacheType.type))
                    .handler(column -> addCache(cacheType))
                    .constraint(Constraint.executable(cacheType.template, ADD))
                    .build());
        }
        addColumnActions(Ids.CACHE_ADD_ACTIONS, pfIcon(UIConstants.ADD_CIRCLE_O), resources.constants().add(), addActions);
        addColumnAction(columnActionFactory.refresh(Ids.CACHE_REFRESH));

        setItemRenderer(item -> new ItemDisplay<Cache>() {
            @Override
            public String getId() {
                return Ids.build(item.type().baseId, item.getName());
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public HTMLElement element() {
                return ItemDisplay.withSubtitle(item.getName(), item.type().type);
            }

            @Override
            public String getTooltip() {
                return item.type().type;
            }

            @Override
            public HTMLElement getIcon() {
                return Icons.custom(item.type().icon);
            }

            @Override
            public String getFilterData() {
                return item.getName() + " " + item.type().type;
            }

            @Override
            public List<ItemAction<Cache>> actions() {
                List<ItemAction<Cache>> actions = new ArrayList<>();
                String cacheContainer = findCacheContainer(getFinder().getContext().getPath());
                if (cacheContainer != null) {
                    actions.add(itemActionFactory.viewAndMonitor(Ids.build(item.type().baseId, item.getName()),
                            places.selectedProfile(item.type().nameToken)
                                    .with(CACHE_CONTAINER, cacheContainer)
                                    .with(NAME, item.getName())
                                    .build()));
                    actions.add(new ItemAction.Builder<Cache>()
                            .title(resources.constants().remove())
                            .handler(item -> {
                                ResourceAddress address = item.type().template.resolve(statementContext,
                                        cacheContainer, item.getName());
                                crud.remove(item.type().type, item.getName(), address, () -> refresh(CLEAR_SELECTION));
                            })
                            .constraint(Constraint.executable(item.type().template, REMOVE))
                            .build());
                }
                return actions;
            }
        });
    }

    private void addCache(CacheType cacheType) {
        Metadata metadata = metadataRegistry.lookup(cacheType.template);

        AddResourceDialog dialog = new AddResourceDialog(Ids.build(cacheType.baseId, Ids.ADD),
                resources.messages().addResourceTitle(cacheType.type), metadata,
                (name, model) -> {
                    String cacheContainer = findCacheContainer(getFinder().getContext().getPath());
                    ResourceAddress address = cacheType.template.resolve(statementContext, cacheContainer, name);

                    if (cacheType.equals(CacheType.LOCAL)) {
                        crud.add(cacheType.type, name, address, model,
                                (n, a) -> refresh(Ids.build(cacheType.baseId, name)));
                    } else {
                        ResourceAddress jgroupsAddress = AddressTemplates.TRANSPORT_JGROUPS_TEMPLATE.resolve(statementContext,
                                cacheContainer);
                        ResourceCheck check = new ResourceCheck(dispatcher, jgroupsAddress);
                        Task<FlowContext> add = context -> {
                            Operation addJgroups = new Operation.Builder(jgroupsAddress, ADD).build();

                            int status = context.pop();
                            if (status == 200) {
                                context.set(JGROUPS_ADDITION_STATUS, false);
                                return Promise.resolve(context);
                            } else {
                                context.set(JGROUPS_ADDITION_STATUS, true);
                                return dispatcher.execute(addJgroups).then(__ -> Promise.resolve(context));
                            }
                        };

                        List<Task<FlowContext>> tasks = asList(check, add);
                        sequential(new FlowContext(progress.get()), tasks)
                                .then(context -> {
                                    if (context.get(JGROUPS_ADDITION_STATUS).equals(true)) {
                                        MessageEvent.fire(eventBus, Message.success(resources.messages()
                                                .addResourceSuccess(Names.TRANSPORT, Names.JGROUPS)));
                                    }
                                    crud.add(cacheType.type, name, address, model,
                                            (n, a) -> refresh(Ids.build(cacheType.baseId, name)));
                                    return null;
                                })
                                .catch_(error -> {
                                    MessageEvent.fire(eventBus, Message.error(
                                            resources.messages().addResourceError(cacheType.type, name),
                                            String.valueOf(error)));
                                    return null;
                                });
                    }

                });
        dialog.getForm().<String> getFormItem(NAME).addValidationHandler(createUniqueValidation());
        dialog.show();
    }
}
