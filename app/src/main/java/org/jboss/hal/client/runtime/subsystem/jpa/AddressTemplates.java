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
package org.jboss.hal.client.runtime.subsystem.jpa;

import org.jboss.hal.meta.AddressTemplate;

/**
 * @author Harald Pehl
 */
public interface AddressTemplates {

    String JPA_ADDRESS = "/{selected.host}/{selected.server}/deployment=*/subsystem=jpa/hibernate-persistence-unit=*";
    String ENTITY_ADDRESS = JPA_ADDRESS + "/entity=*";
    String ENTITY_CACHE_ADDRESS = JPA_ADDRESS + "/entity-cache=*";
    String QUERY_CACHE_ADDRESS = JPA_ADDRESS + "/query-cache=*";
    String COLLECTION_ADDRESS = JPA_ADDRESS + "/collection=*";

    AddressTemplate JPA_TEMPLATE = AddressTemplate.of(JPA_ADDRESS);
    AddressTemplate ENTITY_TEMPLATE = AddressTemplate.of(ENTITY_ADDRESS);
    AddressTemplate ENTITY_CACHE_TEMPLATE = AddressTemplate.of(ENTITY_CACHE_ADDRESS);
    AddressTemplate QUERY_CACHE_TEMPLATE = AddressTemplate.of(QUERY_CACHE_ADDRESS);
    AddressTemplate COLLECTION_TEMPLATE = AddressTemplate.of(COLLECTION_ADDRESS);
}
