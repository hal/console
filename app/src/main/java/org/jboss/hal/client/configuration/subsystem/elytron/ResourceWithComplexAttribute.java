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
package org.jboss.hal.client.configuration.subsystem.elytron;

import java.util.Map;

import org.jboss.hal.core.ComplexAttributeOperations;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.CrudOperations.AddCallback;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.spi.Callback;

public interface ResourceWithComplexAttribute {

    CrudOperations crud();

    ComplexAttributeOperations ca();

    default void add(String id, String type, AddressTemplate template, AddCallback callback) {
        crud().add(id, type, template, callback);
    }

    default void save(String type, String name, AddressTemplate template, Map<String, Object> changedValues,
            Callback callback) {
        crud().save(type, name, template, changedValues, callback);
    }

    default void remove(String type, String name, AddressTemplate template, Callback callback) {
        crud().remove(type, name, template, callback);
    }

    default void addComplexObject(String id, String resource, String complexAttribute, String type,
            AddressTemplate template, Callback callback) {

    }
}
