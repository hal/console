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

import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.JMS_BRIDGE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.MESSAGING_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.JMS_BRIDGE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

/**
 * @author Harald Pehl
 */
@AsyncColumn(Ids.JMS_BRIDGE)
@Requires(AddressTemplates.JMS_BRIDGE_ADDRESS)
public class JmsBridgeColumn extends FinderColumn<NamedNode> {

    @Inject
    public JmsBridgeColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory,
            final CrudOperations crud,
            final Places places) {

        super(new FinderColumn.Builder<NamedNode>(finder, Ids.JMS_BRIDGE, Names.JMS_BRIDGE)

                .columnAction(columnActionFactory.add(Ids.JMS_BRIDGE_ADD, Names.JMS_BRIDGE, JMS_BRIDGE_TEMPLATE,
                        Ids::jmsBridge))
                .columnAction(columnActionFactory.refresh(Ids.JMS_BRIDGE_REFRESH))

                .itemsProvider((context, callback) -> crud.readChildren(MESSAGING_SUBSYSTEM_TEMPLATE, JMS_BRIDGE,
                        children -> callback.onSuccess(asNamedNodes(children))))

                .onPreview(JmsBridgePreview::new)
                .useFirstActionAsBreadcrumbHandler()
                .withFilter()
        );

        setItemRenderer(item -> new ItemDisplay<NamedNode>() {
            @Override
            public String getId() {
                return Ids.messagingServer(item.getName());
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
