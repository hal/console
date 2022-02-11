/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.core.mbui.table;

import java.util.Collections;
import java.util.function.Function;

import javax.inject.Inject;

import org.jboss.hal.ballroom.table.Button;
import org.jboss.hal.ballroom.table.ButtonHandler;
import org.jboss.hal.ballroom.table.Scope;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.CrudOperations.AddCallback;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;

public class TableButtonFactory {

    private final CrudOperations crud;
    private final Resources resources;

    @Inject
    public TableButtonFactory(final CrudOperations crud, final Resources resources) {
        this.crud = crud;
        this.resources = resources;
    }

    public <T extends ModelNode> Button<T> add(AddressTemplate template, ButtonHandler<T> handler) {
        return new Button<>(resources.constants().add(), handler, Constraint.executable(template, ADD));
    }

    public <T extends ModelNode> Button<T> add(String id, String type, AddressTemplate template, AddCallback callback) {
        return add(id, type, template, Collections.emptyList(), callback);
    }

    public <T extends ModelNode> Button<T> add(String id, String type, AddressTemplate template,
            Iterable<String> attributes, AddCallback callback) {
        return new Button<>(resources.constants().add(), table -> crud.add(id, type, template, attributes, callback),
                Constraint.executable(template, ADD));
    }

    public <T extends ModelNode> Button<T> remove(AddressTemplate template, ButtonHandler<T> handler) {
        return new Button<>(resources.constants().remove(), handler, Constraint.executable(template, REMOVE));
    }

    public <T> Button<T> remove(String type, AddressTemplate template, Callback callback) {
        return remove(type, template, null, callback);
    }

    public <T> Button<T> remove(String type, AddressTemplate template, Function<Table<T>, String> nameFunction,
            Callback callback) {
        return new Button<>(resources.constants().remove(), null,
                table -> crud.remove(type, nameFunction != null ? nameFunction.apply(table) : null, template, callback),
                Scope.SELECTED_SINGLE,
                Constraint.executable(template, REMOVE));
    }
}
