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

import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.core.ProfileSelectionEvent;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Column;

import javax.inject.Inject;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
@Column(Ids.PROFILE_COLUMN)
public class ProfileColumn extends FinderColumn<Property> {

    private static final AddressTemplate PROFILE_TEMPLATE = AddressTemplate.of("/profile=*");

    @Inject
    public ProfileColumn(final Finder finder,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final ColumnActionFactory columnActionFactory) {

        super(new Builder<Property>(finder, Ids.PROFILE_COLUMN, Names.PROFILES)
                .columnAction(columnActionFactory.add(
                        IdBuilder.build(Ids.PROFILE_COLUMN, "add"),
                        Names.PROFILE,
                        PROFILE_TEMPLATE))
                .columnAction(columnActionFactory.refresh(IdBuilder.build(Ids.PROFILE_COLUMN, "refresh")))

                .itemRenderer(property -> new ItemDisplay<Property>() {
                    @Override
                    public String getTitle() {
                        return property.getName();
                    }

                    @Override
                    public String nextColumn() {
                        return Ids.SUBSYSTEM_COLUMN;
                    }
                })

                .onItemSelect(property -> eventBus.fireEvent(new ProfileSelectionEvent(property.getName())))

                .itemsProvider((context, callback) -> {
                    Operation operation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION,
                            ResourceAddress.ROOT)
                            .param(CHILD_TYPE, PROFILE)
                            .build();
                    dispatcher.execute(operation, result -> callback.onSuccess(result.asPropertyList()));
                })

                .onPreview(item -> new PreviewContent(item.getName())));
    }
}
