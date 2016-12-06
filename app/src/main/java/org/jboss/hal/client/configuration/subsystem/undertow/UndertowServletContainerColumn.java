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
package org.jboss.hal.client.configuration.subsystem.undertow;

import javax.inject.Inject;

import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.UNDERTOW_SERVLET_CONTAINER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.UNDERTOW_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVLET_CONTAINER;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

/**
 * @author Harald Pehl
 */
@AsyncColumn(Ids.UNDERTOW_SERVLET_CONTAINER)
@Requires(AddressTemplates.UNDERTOW_SERVLET_CONTAINER_ADDRESS)
public class UndertowServletContainerColumn extends FinderColumn<NamedNode> {

    @Inject
    public UndertowServletContainerColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final CrudOperations crud) {
        super(new Builder<NamedNode>(finder, Ids.UNDERTOW_SERVLET_CONTAINER, Names.SERVLET_CONTAINER)

                .columnAction(columnActionFactory.add(Ids.UNDERTOW_SERVLET_CONTAINER_ADD, Names.SERVLET_CONTAINER,
                        UNDERTOW_SERVLET_CONTAINER_TEMPLATE))

                .itemsProvider((context, callback) -> crud.readChildren(UNDERTOW_SUBSYSTEM_TEMPLATE, SERVLET_CONTAINER,
                        children -> callback.onSuccess(asNamedNodes(children))))

                .itemRenderer(item -> new ItemDisplay<NamedNode>() {
                    @Override
                    public String getId() {
                        return Ids.undertowServletContainer(item.getName());
                    }

                    @Override
                    public String getTitle() {
                        return item.getName();
                    }
                })

                .onPreview(UndertowServletContainerPreview::new)
        );
    }
}
