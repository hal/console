/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.client.configuration;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.AsyncColumn;

import javax.inject.Inject;
import java.util.List;

import static java.util.Arrays.asList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;

@AsyncColumn(value = Ids.INTERFACE_COLUMN)
public class InterfaceColumn extends FinderColumn<Property> {

    @Inject
    public InterfaceColumn(final Finder finder,
            final Dispatcher dispatcher,
            final ItemActionFactory itemActionFactory) {

        super(new Builder<Property>(finder, Ids.INTERFACE_COLUMN, Names.INTERFACE)
                .itemsProvider((context, callback) -> {
                    Operation operation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, ResourceAddress.ROOT)
                            .param(CHILD_TYPE, ModelDescriptionConstants.INTERFACE).build();
                    dispatcher.execute(operation, result -> { callback.onSuccess(result.asPropertyList()); });
                }));

        setItemRenderer(property -> new ItemDisplay<Property>() {
            @Override
            public String getTitle() {
                return new LabelBuilder().label(property);
            }

            @Override
            public List<ItemAction<Property>> actions() {
                return asList(
                        itemActionFactory.view(NameTokens.INTERFACE, NAME, property.getName()),
                        itemActionFactory.remove(property.getName(), Names.INTERFACE, InterfacePresenter.ROOT_TEMPLATE,
                                InterfaceColumn.this));
            }
        });
    }
}
