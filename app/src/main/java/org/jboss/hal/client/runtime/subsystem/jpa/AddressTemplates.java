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
package org.jboss.hal.client.runtime.subsystem.jpa;

import org.jboss.hal.meta.AddressTemplate;

import static org.jboss.hal.core.deployment.DeploymentResources.DEPLOYMENT_ADDRESS;
import static org.jboss.hal.core.deployment.DeploymentResources.SUBDEPLOYMENT_ADDRESS;

interface AddressTemplates {

    String JPA_ADDRESS = "/subsystem=jpa";
    String HPU_ADDRESS = JPA_ADDRESS + "/hibernate-persistence-unit=*";

    String JPA_DEPLOYMENT_ADDRESS = DEPLOYMENT_ADDRESS + JPA_ADDRESS;
    String HPU_DEPLOYMENT_ADDRESS = DEPLOYMENT_ADDRESS + HPU_ADDRESS;
    String ENTITY_DEPLOYMENT_ADDRESS = HPU_DEPLOYMENT_ADDRESS + "/entity=*";
    String ENTITY_CACHE_DEPLOYMENT_ADDRESS = HPU_DEPLOYMENT_ADDRESS + "/entity-cache=*";
    String QUERY_CACHE_DEPLOYMENT_ADDRESS = HPU_DEPLOYMENT_ADDRESS + "/query-cache=*";
    String COLLECTION_DEPLOYMENT_ADDRESS = HPU_DEPLOYMENT_ADDRESS + "/collection=*";

    String HPU_SUBDEPLOYMENT_ADDRESS = SUBDEPLOYMENT_ADDRESS + HPU_ADDRESS;

    AddressTemplate JPA_DEPLOYMENT_TEMPLATE = AddressTemplate.of(HPU_DEPLOYMENT_ADDRESS);
    AddressTemplate ENTITY_DEPLOYMENT_TEMPLATE = AddressTemplate.of(ENTITY_DEPLOYMENT_ADDRESS);
    AddressTemplate ENTITY_CACHE_DEPLOYMENT_TEMPLATE = AddressTemplate.of(ENTITY_CACHE_DEPLOYMENT_ADDRESS);
    AddressTemplate QUERY_CACHE_DEPLOYMENT_TEMPLATE = AddressTemplate.of(QUERY_CACHE_DEPLOYMENT_ADDRESS);
    AddressTemplate COLLECTION_DEPLOYMENT_TEMPLATE = AddressTemplate.of(COLLECTION_DEPLOYMENT_ADDRESS);

    AddressTemplate HPU_SUBDEPLOYMENT_TEMPLATE = AddressTemplate.of(HPU_SUBDEPLOYMENT_ADDRESS);
}
