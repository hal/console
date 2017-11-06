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
package org.jboss.hal.meta;

import java.util.Map;
import java.util.Set;

import org.jboss.hal.dmr.ResourceAddress;
import rx.Single;

public interface Database<T> {

    String PAYLOAD = "payload";

    /** Turns the templates into resource addresses and returns a map for later lookup. */
    Map<ResourceAddress, AddressTemplate> addressLookup(Set<AddressTemplate> templates);

    /** Returns a map with metadata for the specified templates. */
    Single<Map<ResourceAddress, T>> getAll(Set<AddressTemplate> templates);

    /** Returns a map with metadata whose address starts with the specified template */
    Single<Map<ResourceAddress, T>> getRecursive(AddressTemplate template);

    Single<Set<String>> putAll(Map<ResourceAddress, T> metadata);

    /** The type of this database. */
    String type();
}
