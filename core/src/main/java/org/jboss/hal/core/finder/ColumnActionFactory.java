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

import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.form.FormItemValidation;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;

/** Provides methods to create common column actions. */
public class ColumnActionFactory {

    private final CrudOperations crud;
    private final Resources resources;

    @Inject
    public ColumnActionFactory(CrudOperations crud, Resources resources) {
        this.crud = crud;
        this.resources = resources;
    }

    /**
     * Returns a column action which opens an add-resource-dialog for the given resource type. The dialog contains
     * fields for all required request properties.
     * <p>
     * When clicking "Add", a new resource is added using the specified address template and the newly added resource
     * is selected using the name as identifier in {@link FinderColumn#refresh(String)}.
     */
    public <T> ColumnAction<T> add(String id, String type, AddressTemplate template) {
        return add(id, type, template, Collections.emptyList(), Ids::asId, null);
    }

    public <T> ColumnAction<T> add(String id, String type, AddressTemplate template,
            Function<String, String> identifier) {
        return add(id, type, template, Collections.emptyList(), identifier, null);
    }

    /**
     * Returns a column action which opens an add-resource-dialog for the given resource type. The dialog contains
     * fields for all required request properties plus the ones specified by {@code attributes}.
     * <p>
     * When clicking "Add", a new resource is added using the specified address template and the newly added resource
     * is selected using the name as identifier in {@link FinderColumn#refresh(String)}.
     */
    public <T> ColumnAction<T> add(String id, String type, AddressTemplate template, Iterable<String> attributes) {
        return add(id, type, template, attributes, Ids::asId, null);
    }

    public <T> ColumnAction<T> add(String id, String type, AddressTemplate template, Iterable<String> attributes,
            Supplier<FormItemValidation<String>> createValidator) {
        return add(id, type, template, attributes, Ids::asId, createValidator);
    }

    public <T> ColumnAction<T> add(String id, String type, AddressTemplate template,
            Function<String, String> identifier, Supplier<FormItemValidation<String>> createValidator) {
        return add(id, type, template, Collections.emptyList(), identifier, createValidator);
    }

    public <T> ColumnAction<T> add(String id, String type, AddressTemplate template, Iterable<String> attributes,
            Function<String, String> identifier, Supplier<FormItemValidation<String>> createValidator) {
        //noinspection Convert2Lambda
        return add(id, type, template, new ColumnActionHandler<T>() {
            @Override
            public void execute(FinderColumn<T> column) {
                FormItemValidation<String> validator = createValidator != null ? createValidator.get() : null;

                crud.add(id, type, template, attributes, validator, (name, address) -> {
                    if (name != null) {
                        column.refresh(identifier.apply(name));
                    }
                });
            }
        });
    }

    public <T> ColumnAction<T> add(String id, String type, AddressTemplate template, ColumnActionHandler<T> handler) {
        return add(id, type, template, pfIcon("add-circle-o"), handler);
    }

    public <T> ColumnAction<T> add(String id, String type, AddressTemplate template, String iconCss,
            ColumnActionHandler<T> handler) {
        ColumnAction.Builder<T> builder = new ColumnAction.Builder<T>(id)
                .element(addButton(resources.messages().addResourceTitle(type), iconCss))
                .handler(handler);
        if (template != null) {
            builder.constraint(Constraint.executable(template, ADD));
        }
        return builder.build();
    }

    public HTMLElement addButton(String type) {
        return addButton(resources.messages().addResourceTitle(type), pfIcon("add-circle-o"));
    }

    public HTMLElement addButton(String title, String iconCss) {
        return span().css(iconCss)
                .title(title)
                .data(UIConstants.TOGGLE, UIConstants.TOOLTIP)
                .data(UIConstants.PLACEMENT, "bottom")
                .get();
    }

    public <T> ColumnAction<T> refresh(String id) {
        return refresh(id, column -> column.refresh(RESTORE_SELECTION));
    }

    public <T> ColumnAction<T> refresh(String id, ColumnActionHandler<T> handler) {
        HTMLElement element = span()
                .css(fontAwesome(CSS.refresh))
                .title(resources.constants().refresh())
                .data(UIConstants.TOGGLE, UIConstants.TOOLTIP)
                .data(UIConstants.PLACEMENT, "bottom")
                .get();
        return new ColumnAction.Builder<T>(id)
                .element(element)
                .handler(handler)
                .build();
    }
}
