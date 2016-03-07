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
package org.jboss.hal.core.finder;

import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.CLEAR_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;

/**
 * Convenience method for common item actions.
 *
 * @author Harald Pehl
 */
public class ItemActionFactory {

    private final StatementContext statementContext;
    private final Dispatcher dispatcher;
    private final PlaceManager placeManager;
    private final Resources resources;

    @Inject
    public ItemActionFactory(StatementContext statementContext,
            Dispatcher dispatcher,
            PlaceManager placeManager,
            Resources resources) {
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.placeManager = placeManager;
        this.resources = resources;
    }

    public <T> ItemAction<T> view(String nameToken, String... parameter) {
        PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(nameToken);
        if (parameter != null && parameter.length > 1) {
            if (parameter.length % 2 != 0) {
                throw new IllegalArgumentException(
                        "Parameter in ItemActionFactory.action('" + nameToken + "') must be key/value pairs");
            }
            Map<String, String> map = new HashMap<>();
            for (int i = 0; i < parameter.length; i += 2) {
                map.put(parameter[i], parameter[i + 1]);
            }
            builder.with(map);
        }
        return view(builder.build());
    }

    public <T> ItemAction<T> view(PlaceRequest placeRequest) {
        return new ItemAction<>(resources.constants().view(), item -> placeManager.revealPlace(placeRequest));
    }

    /**
     * Creates a 'remove' action which removes the specified resource from the given address. The address can contain a
     * wildcard which is replace by the resource name. The action wil bring up a confirmation dialog. If confirmed the
     * resource is removed and {@link FinderColumn#refresh(FinderColumn.RefreshMode)} is called.
     */
    public <T> ItemAction<T> remove(String type, String name, AddressTemplate addressTemplate, FinderColumn<T> column) {
        return new ItemAction<>(resources.constants().remove(), item -> {
            Dialog dialog = DialogFactory.confirmation(resources.messages().removeResourceConfirmationTitle(type),
                    resources.messages().removeResourceConfirmationQuestion(name),
                    () -> {
                        ResourceAddress address = addressTemplate.resolve(statementContext, name);
                        Operation operation = new Operation.Builder(REMOVE, address).build();
                        dispatcher.execute(operation, result -> column.refresh(CLEAR_SELECTION));
                        return true;
                    });
            dialog.show();
        });
    }
}
