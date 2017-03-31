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
package org.jboss.hal.core.finder;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.CLEAR_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;

/**
 * Convenience methods for common item actions.
 *
 * @author Harald Pehl
 */
public class ItemActionFactory {

    private final CrudOperations crud;
    private final ItemMonitor itemMonitor;
    private final PlaceManager placeManager;
    private final Resources resources;

    @Inject
    public ItemActionFactory(CrudOperations crud,
            ItemMonitor itemMonitor,
            PlaceManager placeManager,
            Resources resources) {
        this.crud = crud;
        this.itemMonitor = itemMonitor;
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
        return placeRequest(resources.constants().view(), placeRequest);
    }

    public <T> ItemAction<T> placeRequest(String title, PlaceRequest placeRequest) {
        return placeRequest(title, placeRequest, null);
    }

    public <T> ItemAction<T> placeRequest(String title, PlaceRequest placeRequest, Constraint constraint) {
        ItemAction.Builder<T> builder = new ItemAction.Builder<T>()
                .title(title)
                .handler(item -> placeManager.revealPlace(placeRequest));
        if (constraint != null) {
            builder.constraint(constraint);
        }
        return builder.build();
    }

    public <T> ItemAction<T> viewAndMonitor(String itemId, PlaceRequest placeRequest) {
        return new ItemAction.Builder<T>().title(resources.constants().view())
                .handler(itemMonitor.monitorPlaceRequest(itemId, placeRequest.getNameToken(),
                        () -> placeManager.revealPlace(placeRequest)))
                .build();
    }

    /**
     * Wraps the specified handler inside a confirmation dialog. The action is executed upon confirmation.
     */
    public <T> ItemAction<T> remove(String type, String name, AddressTemplate template, ItemActionHandler<T> handler) {
        return remove(type, name, template, template, handler);
    }

    public <T> ItemAction<T> remove(String type, String name, AddressTemplate template, AddressTemplate constraint,
            ItemActionHandler<T> handler) {
        ItemAction.Builder<T> builder = new ItemAction.Builder<T>()
                .title(resources.constants().remove())
                .handler(item -> DialogFactory.showConfirmation(
                        resources.messages().removeConfirmationTitle(type),
                        resources.messages().removeConfirmationQuestion(name),
                        () -> handler.execute(item)));
        if (template != null) {
            builder.constraint(Constraint.executable(constraint, REMOVE));
        }
        return builder.build();
    }

    /**
     * Creates a 'remove' action which removes the specified resource from the given template. The template can contain
     * a wildcard which is replaced by the resource name. The action wil bring up a confirmation dialog. If confirmed
     * the resource is removed and {@link FinderColumn#refresh(FinderColumn.RefreshMode)} is called.
     */
    public <T> ItemAction<T> remove(String type, String name, AddressTemplate template, FinderColumn<T> column) {
        return remove(type, name, template, template, column);

    }

    public <T> ItemAction<T> remove(String type, String name, AddressTemplate template, AddressTemplate constraint,
            FinderColumn<T> column) {
        return new ItemAction.Builder<T>()
                .title(resources.constants().remove())
                .handler(item -> crud.remove(type, name, template, () -> column.refresh(CLEAR_SELECTION)))
                .constraint(Constraint.executable(constraint, REMOVE))
                .build();
    }
}
