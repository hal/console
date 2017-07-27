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

import static org.jboss.hal.meta.SelectionAwareStatementContext.SELECTION_EXPRESSION;

interface AddressTemplates {

    String INFINISPAN_SUBSYSTEM_ADDRESS = "/{selected.profile}/subsystem=infinispan";

    String CACHE_CONTAINER_ADDRESS = INFINISPAN_SUBSYSTEM_ADDRESS + "/cache-container=*";
    String SELECTED_CACHE_CONTAINER_ADDRESS = INFINISPAN_SUBSYSTEM_ADDRESS + "/cache-container=" + SELECTION_EXPRESSION;

    String DISTRIBUTED_CACHE_ADDRESS = CACHE_CONTAINER_ADDRESS + "/distributed-cache=*";
    String INVALIDATION_CACHE_ADDRESS = CACHE_CONTAINER_ADDRESS + "/invalidation-cache=*";
    String LOCAL_CACHE_ADDRESS = CACHE_CONTAINER_ADDRESS + "/local-cache=*";
    String REPLICATED_CACHE_ADDRESS = CACHE_CONTAINER_ADDRESS + "/replicated-cache=*";

    String TRANSPORT_JGROUPS_ADDRESS = CACHE_CONTAINER_ADDRESS + "/transport=jgroups";

    AddressTemplate INFINISPAN_SUBSYSTEM_TEMPLATE = AddressTemplate.of(INFINISPAN_SUBSYSTEM_ADDRESS);

    AddressTemplate CACHE_CONTAINER_TEMPLATE = AddressTemplate.of(CACHE_CONTAINER_ADDRESS);
    AddressTemplate SELECTED_CACHE_CONTAINER_TEMPLATE = AddressTemplate.of(SELECTED_CACHE_CONTAINER_ADDRESS);

    AddressTemplate DISTRIBUTED_CACHE_TEMPLATE = AddressTemplate.of(DISTRIBUTED_CACHE_ADDRESS);
    AddressTemplate INVALIDATION_CACHE_TEMPLATE = AddressTemplate.of(INVALIDATION_CACHE_ADDRESS);
    AddressTemplate LOCAL_CACHE_TEMPLATE = AddressTemplate.of(LOCAL_CACHE_ADDRESS);
    AddressTemplate REPLICATED_CACHE_TEMPLATE = AddressTemplate.of(REPLICATED_CACHE_ADDRESS);

    AddressTemplate TRANSPORT_JGROUPS_TEMPLATE = AddressTemplate.of(TRANSPORT_JGROUPS_ADDRESS);
}
