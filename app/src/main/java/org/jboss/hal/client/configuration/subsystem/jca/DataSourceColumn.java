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
package org.jboss.hal.client.configuration.subsystem.jca;

import com.google.gwt.user.client.Window;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;

/**
 * @author Harald Pehl
 */
@AsyncColumn(Ids.DATA_SOURCE_COLUMN)
public class DataSourceColumn extends FinderColumn<Property> {

    @Inject
    public DataSourceColumn(final Finder finder,
            final Dispatcher dispatcher,
            final PlaceManager placeManager,
            final StatementContext statementContext,
            final Resources resources) {

        super(new Builder<Property>(finder, Ids.DATA_SOURCE_COLUMN, "Datasource")
                .itemsProvider((context, callback) -> {
                    ResourceAddress address = AddressTemplate.of("/{selected.profile}/subsystem=datasources")
                            .resolve(statementContext);
                    // TODO Remove dirty hack which relies on the *generated* id of the last column
                    String childType = "xa".equals(context.getPath().last().getValue())
                            ? "xa-data-source" : "data-source";
                    Operation operation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, address)
                            .param(CHILD_TYPE, childType).build();
                    dispatcher.execute(operation, result -> {
                        callback.onSuccess(result.asPropertyList());
                    });
                })
                .onPreview(item -> new PreviewContent(item.getName())));

        setItemRenderer(item -> new ItemDisplay<Property>() {
            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public List<ItemAction<Property>> actions() {
                String profile = statementContext.selectedProfile() != null ? statementContext
                        .selectedProfile() : "standalone";
                PlaceRequest view = new PlaceRequest.Builder()
                        .nameToken(NameTokens.DATASOURCE)
                        .with(Names.PROFILE, profile)
                        .with("datasource", item.getName()) //NON-NLS
                        .build();

                return Arrays.asList(
                        new ItemAction<>(resources.constants().view(),
                                item -> placeManager.revealPlace(view)),
                        new ItemAction<>(resources.constants().remove(), item -> Window.alert(Names.NYI)),
                        new ItemAction<>(resources.constants().testConnection(),
                                item -> Window.alert(Names.NYI))
                );
            }
        });
    }
}
