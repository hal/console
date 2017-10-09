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
package org.jboss.hal.client.configuration.subsystem.messaging;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.JMS_BRIDGE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

@AsyncColumn(Ids.JMS_BRIDGE)
@Requires(AddressTemplates.JMS_BRIDGE_ADDRESS)
public class JmsBridgeColumn extends FinderColumn<NamedNode> {

    @SuppressWarnings("HardCodedStringLiteral")
    static <T> void registerSuggestionHandler(Dispatcher dispatcher, StatementContext statementContext, Form<T> form) {
        form.getFormItem("source-connection-factory").registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, statementContext, asList(
                        CONNECTION_FACTORY_TEMPLATE, POOLED_CONNECTION_FACTORY_TEMPLATE)));
        form.getFormItem("target-connection-factory").registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, statementContext, asList(
                        CONNECTION_FACTORY_TEMPLATE, POOLED_CONNECTION_FACTORY_TEMPLATE)));
        form.getFormItem("source-destination").registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, statementContext, asList(
                        JMS_QUEUE_TEMPLATE, JMS_TOPIC_TEMPLATE)));
        form.getFormItem("target-destination").registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, statementContext, asList(
                        JMS_QUEUE_TEMPLATE, JMS_TOPIC_TEMPLATE)));
    }

    @Inject
    public JmsBridgeColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory,
            final MetadataRegistry metadataRegistry,
            final Dispatcher dispatcher,
            final CrudOperations crud,
            final StatementContext statementContext,
            final Places places,
            final Resources resources) {

        super(new FinderColumn.Builder<NamedNode>(finder, Ids.JMS_BRIDGE, Names.JMS_BRIDGE)
                .itemsProvider((context, callback) -> crud.readChildren(MESSAGING_SUBSYSTEM_TEMPLATE, JMS_BRIDGE,
                        children -> callback.onSuccess(asNamedNodes(children))))
                .onPreview(JmsBridgePreview::new)
                .useFirstActionAsBreadcrumbHandler()
                .withFilter());

        addColumnAction(columnActionFactory.add(Ids.JMS_BRIDGE_ADD, Names.JMS_BRIDGE, JMS_BRIDGE_TEMPLATE,
                column -> {
                    Metadata metadata = metadataRegistry.lookup(JMS_BRIDGE_TEMPLATE);
                    Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.JMS_BRIDGE_ADD, metadata)
                            .unboundFormItem(new NameItem(), 0)
                            .fromRequestProperties()
                            .requiredOnly()
                            .build();
                    registerSuggestionHandler(dispatcher, statementContext, form);
                    new AddResourceDialog(resources.messages().addResourceTitle(Names.JMS_BRIDGE),
                            form, (name, model) -> crud.add(Names.JMS_BRIDGE, name, JMS_BRIDGE_TEMPLATE, model,
                            (n, a) -> {
                                refresh(Ids.jmsBridge(n));
                            })).show();
                }));
        addColumnAction(columnActionFactory.refresh(Ids.JMS_BRIDGE_REFRESH));

        setItemRenderer(item -> new ItemDisplay<NamedNode>() {
            @Override
            public String getId() {
                return Ids.jmsBridge(item.getName());
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public List<ItemAction<NamedNode>> actions() {
                List<ItemAction<NamedNode>> actions = new ArrayList<>();
                actions.add(itemActionFactory.view(
                        places.selectedProfile(NameTokens.JMS_BRIDGE).with(NAME, item.getName()).build()));
                actions.add(itemActionFactory.remove(Names.JMS_BRIDGE, item.getName(), JMS_BRIDGE_TEMPLATE,
                        JmsBridgeColumn.this));
                return actions;
            }
        });
    }
}
