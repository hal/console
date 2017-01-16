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
package org.jboss.hal.client.configuration.subsystem.infinispan;

import org.jboss.hal.meta.AddressTemplate;

/**
 * @author Harald Pehl
 */
interface AddressTemplates {

    String INFINISPAN_SUBSYSTEM_ADDRESS = "/{selected.profile}/subsystem=infinispan";
    String CACHE_CONTAINER_ADDRESS = INFINISPAN_SUBSYSTEM_ADDRESS + "/cache-container=*";

    AddressTemplate INFINISPAN_SUBSYSTEM_TEMPLATE = AddressTemplate.of(INFINISPAN_SUBSYSTEM_ADDRESS);
    AddressTemplate CACHE_CONTAINER_TEMPLATE = AddressTemplate.of(CACHE_CONTAINER_ADDRESS);
}
