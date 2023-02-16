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
package org.jboss.hal.client.configuration.subsystem.undertow;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderSegment;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.SelectionAwareStatementContext;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Requires;

import elemental2.promise.Promise;

import static java.util.Collections.singletonList;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.HOST_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SELECTED_HOST_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SELECTED_SERVER_TEMPLATE;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.CLEAR_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT_WEB_MODULE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

@AsyncColumn(Ids.UNDERTOW_HOST)
@Requires(AddressTemplates.HOST_ADDRESS)
public class HostColumn extends FinderColumn<NamedNode> {

    private final CrudOperations crud;
    private final StatementContext statementContext;
    private final MetadataRegistry metadataRegistry;
    private final Resources resources;

    @Inject
    public HostColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            ItemActionFactory itemActionFactory,
            Places places,
            CrudOperations crud,
            StatementContext statementContext,
            MetadataRegistry metadataRegistry,
            Resources resources) {

        super(new FinderColumn.Builder<NamedNode>(finder, Ids.UNDERTOW_HOST, Names.HOST)
                .onPreview(HostPreview::new)
                .useFirstActionAsBreadcrumbHandler()
                .pinnable()
                .withFilter());
        this.crud = crud;
        this.statementContext = statementContext;
        this.metadataRegistry = metadataRegistry;
        this.resources = resources;

        addColumnAction(columnActionFactory.add(Ids.UNDERTOW_HOST_ADD, Names.HOST, SELECTED_HOST_TEMPLATE, this::addHost));
        addColumnAction(columnActionFactory.refresh(Ids.UNDERTOW_HOST_REFRESH));

        setItemsProvider(context -> {
            ResourceAddress address = SELECTED_SERVER_TEMPLATE.resolve(selectionAwareStatementContext());
            return crud.readChildren(address, HOST).then(children -> Promise.resolve(asNamedNodes(children)));
        });

        setItemRenderer(item -> new ItemDisplay<NamedNode>() {
            @Override
            public String getId() {
                return Ids.undertowHost(item.getName());
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public List<ItemAction<NamedNode>> actions() {
                List<ItemAction<NamedNode>> actions = new ArrayList<>();
                actions.add(itemActionFactory.view(
                        places.selectedProfile(NameTokens.UNDERTOW_HOST)
                                .with(SERVER, findServer())
                                .with(HOST, item.getName())
                                .build()));
                actions.add(new ItemAction.Builder<NamedNode>()
                        .title(resources.constants().remove())
                        .handler(item -> {
                            ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(HOST + "=" + item.getName()).resolve(
                                    selectionAwareStatementContext());
                            crud.remove(Names.HOST, item.getName(), address, () -> refresh(CLEAR_SELECTION));
                        })
                        .constraint(Constraint.executable(HOST_TEMPLATE, REMOVE))
                        .build());
                return actions;
            }
        });
    }

    private void addHost(final FinderColumn<NamedNode> column) {
        Metadata metadata = metadataRegistry.lookup(HOST_TEMPLATE);
        AddResourceDialog dialog = new AddResourceDialog(Ids.UNDERTOW_HOST_ADD,
                resources.messages().addResourceTitle(Names.HOST), metadata, singletonList(DEFAULT_WEB_MODULE),
                (name, model) -> {
                    ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(HOST + "=" + name)
                            .resolve(selectionAwareStatementContext());
                    crud.add(Names.HOST, name, address, model, (n, a) -> column.refresh(Ids.undertowHost(name)));
                });
        dialog.show();
    }

    private StatementContext selectionAwareStatementContext() {
        String server = findServer();
        if (server != null) {
            return new SelectionAwareStatementContext(statementContext, () -> server);
        }
        return statementContext;
    }

    private String findServer() {
        FinderSegment<?> segment = getFinder().getContext().getPath().findColumn(Ids.UNDERTOW_SERVER);
        if (segment != null) {
            return Ids.extractUndertowServer(segment.getItemId());
        }
        return null;
    }
}
