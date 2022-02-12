/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.infinispan;

import org.jboss.hal.meta.AddressTemplate;

import static org.jboss.hal.meta.SelectionAwareStatementContext.SELECTION_EXPRESSION;

interface AddressTemplates {

    // ------------------------------------------------------ address

    String INFINISPAN_SUBSYSTEM_ADDRESS = "/{selected.profile}/subsystem=infinispan";

    // cache container and sub resources
    String CACHE_CONTAINER_ADDRESS = INFINISPAN_SUBSYSTEM_ADDRESS + "/cache-container=*";
    String SELECTED_CACHE_CONTAINER_ADDRESS = INFINISPAN_SUBSYSTEM_ADDRESS + "/cache-container=" + SELECTION_EXPRESSION;
    String THREAD_POOL_ASYNC_OPERATIONS = CACHE_CONTAINER_ADDRESS + "/thread-pool=async-operations";
    String THREAD_POOL_EXPIRATION = CACHE_CONTAINER_ADDRESS + "/thread-pool=expiration";
    String THREAD_POOL_LISTENER = CACHE_CONTAINER_ADDRESS + "/thread-pool=listener";
    String THREAD_POOL_PERSISTENCE = CACHE_CONTAINER_ADDRESS + "/thread-pool=persistence";
    String THREAD_POOL_REMOTE_COMMAND = CACHE_CONTAINER_ADDRESS + "/thread-pool=remote-command";
    String THREAD_POOL_SITE_TRANSFER = CACHE_CONTAINER_ADDRESS + "/thread-pool=state-transfer";
    String THREAD_POOL_TRANSPORT = CACHE_CONTAINER_ADDRESS + "/thread-pool=transport";
    String TRANSPORT_JGROUPS_ADDRESS = CACHE_CONTAINER_ADDRESS + "/transport=jgroups";

    // remote cache container and sub resources
    String REMOTE_CACHE_CONTAINER_ADDRESS = INFINISPAN_SUBSYSTEM_ADDRESS + "/remote-cache-container=*";
    String SELECTED_REMOTE_CACHE_CONTAINER_ADDRESS = INFINISPAN_SUBSYSTEM_ADDRESS + "/remote-cache-container="
            + SELECTION_EXPRESSION;
    String REMOTE_CLUSTER_ADDRESS = REMOTE_CACHE_CONTAINER_ADDRESS + "/remote-cluster=*";
    String COMPONENT_CONNECTION_POOL_ADDRESS = REMOTE_CACHE_CONTAINER_ADDRESS + "/component=connection-pool";
    String COMPONENT_SECURITY_ADDRESS = REMOTE_CACHE_CONTAINER_ADDRESS + "/component=security";
    String THREAD_POOL_ASYNC_ADDRESS = REMOTE_CACHE_CONTAINER_ADDRESS + "/thread-pool=async";

    // cache types
    String DISTRIBUTED_CACHE_ADDRESS = CACHE_CONTAINER_ADDRESS + "/distributed-cache=*";
    String INVALIDATION_CACHE_ADDRESS = CACHE_CONTAINER_ADDRESS + "/invalidation-cache=*";
    String LOCAL_CACHE_ADDRESS = CACHE_CONTAINER_ADDRESS + "/local-cache=*";
    String REPLICATED_CACHE_ADDRESS = CACHE_CONTAINER_ADDRESS + "/replicated-cache=*";
    String SCATTERED_CACHE_ADDRESS = CACHE_CONTAINER_ADDRESS + "/scattered-cache=*";

    // ------------------------------------------------------ template

    AddressTemplate INFINISPAN_SUBSYSTEM_TEMPLATE = AddressTemplate.of(INFINISPAN_SUBSYSTEM_ADDRESS);

    // cache container and sub resources
    AddressTemplate CACHE_CONTAINER_TEMPLATE = AddressTemplate.of(CACHE_CONTAINER_ADDRESS);
    AddressTemplate SELECTED_CACHE_CONTAINER_TEMPLATE = AddressTemplate.of(SELECTED_CACHE_CONTAINER_ADDRESS);
    AddressTemplate TRANSPORT_JGROUPS_TEMPLATE = AddressTemplate.of(TRANSPORT_JGROUPS_ADDRESS);

    // remote cache container and sub resources
    AddressTemplate REMOTE_CACHE_CONTAINER_TEMPLATE = AddressTemplate.of(REMOTE_CACHE_CONTAINER_ADDRESS);
    AddressTemplate SELECTED_REMOTE_CACHE_CONTAINER_TEMPLATE = AddressTemplate.of(
            SELECTED_REMOTE_CACHE_CONTAINER_ADDRESS);
    AddressTemplate REMOTE_CLUSTER_TEMPLATE = AddressTemplate.of(REMOTE_CLUSTER_ADDRESS);
    AddressTemplate COMPONENT_CONNECTION_POOL_TEMPLATE = AddressTemplate.of(COMPONENT_CONNECTION_POOL_ADDRESS);
    AddressTemplate COMPONENT_SECURITY_TEMPLATE = AddressTemplate.of(COMPONENT_SECURITY_ADDRESS);
    AddressTemplate THREAD_POOL_ASYNC_TEMPLATE = AddressTemplate.of(THREAD_POOL_ASYNC_ADDRESS);

    // cache types
    AddressTemplate DISTRIBUTED_CACHE_TEMPLATE = AddressTemplate.of(DISTRIBUTED_CACHE_ADDRESS);
    AddressTemplate INVALIDATION_CACHE_TEMPLATE = AddressTemplate.of(INVALIDATION_CACHE_ADDRESS);
    AddressTemplate LOCAL_CACHE_TEMPLATE = AddressTemplate.of(LOCAL_CACHE_ADDRESS);
    AddressTemplate REPLICATED_CACHE_TEMPLATE = AddressTemplate.of(REPLICATED_CACHE_ADDRESS);
    AddressTemplate SCATTERED_CACHE_TEMPLATE = AddressTemplate.of(SCATTERED_CACHE_ADDRESS);

}
