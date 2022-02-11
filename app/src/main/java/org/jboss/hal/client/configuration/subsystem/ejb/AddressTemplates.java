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
package org.jboss.hal.client.configuration.subsystem.ejb;

import org.jboss.hal.meta.AddressTemplate;

interface AddressTemplates {

    // Container
    String EJB_SUBSYSTEM_ADDRESS = "/{selected.profile}/subsystem=ejb3";
    String THREAD_POOL_ADDRESS = EJB_SUBSYSTEM_ADDRESS + "/thread-pool=*";
    String REMOTING_PROFILE_ADDRESS = EJB_SUBSYSTEM_ADDRESS + "/remoting-profile=*";

    // bean pools
    String BEAN_POOL_ADDRESS = EJB_SUBSYSTEM_ADDRESS + "/strict-max-bean-instance-pool=*";

    // state management
    String CACHE_ADDRESS = EJB_SUBSYSTEM_ADDRESS + "/cache=*";
    String PASSIVATION_ADDRESS = EJB_SUBSYSTEM_ADDRESS + "/passivation-store=*";

    // Services
    String SERVICE_ASYNC_ADDRESS = EJB_SUBSYSTEM_ADDRESS + "/service=async";
    String SERVICE_IDENTITY_ADDRESS = EJB_SUBSYSTEM_ADDRESS + "/service=identity";
    String SERVICE_IIOP_ADDRESS = EJB_SUBSYSTEM_ADDRESS + "/service=iiop";
    String SERVICE_REMOTE_ADDRESS = EJB_SUBSYSTEM_ADDRESS + "/service=remote";
    String SERVICE_TIMER_ADDRESS = EJB_SUBSYSTEM_ADDRESS + "/service=timer-service";

    String MDB_DELIVERY_GROUP_ADDRESS = EJB_SUBSYSTEM_ADDRESS + "/mdb-delivery-group=*";

    String APP_SEC_DOMAIN_ADDRESS = EJB_SUBSYSTEM_ADDRESS + "/application-security-domain=*";

    AddressTemplate EJB_SUBSYSTEM_TEMPLATE = AddressTemplate.of(EJB_SUBSYSTEM_ADDRESS);
    AddressTemplate THREAD_POOL_TEMPLATE = AddressTemplate.of(THREAD_POOL_ADDRESS);
    AddressTemplate REMOTING_PROFILE_TEMPLATE = AddressTemplate.of(REMOTING_PROFILE_ADDRESS);

    AddressTemplate BEAN_POOL_TEMPLATE = AddressTemplate.of(BEAN_POOL_ADDRESS);

    AddressTemplate CACHE_TEMPLATE = AddressTemplate.of(CACHE_ADDRESS);
    AddressTemplate PASSIVATION_TEMPLATE = AddressTemplate.of(PASSIVATION_ADDRESS);

    AddressTemplate SERVICE_ASYNC_TEMPLATE = AddressTemplate.of(SERVICE_ASYNC_ADDRESS);
    AddressTemplate SERVICE_IDENTITY_TEMPLATE = AddressTemplate.of(SERVICE_IDENTITY_ADDRESS);
    AddressTemplate SERVICE_IIOP_TEMPLATE = AddressTemplate.of(SERVICE_IIOP_ADDRESS);
    AddressTemplate SERVICE_REMOTE_TEMPLATE = AddressTemplate.of(SERVICE_REMOTE_ADDRESS);
    AddressTemplate SERVICE_TIMER_TEMPLATE = AddressTemplate.of(SERVICE_TIMER_ADDRESS);

    AddressTemplate MDB_DELIVERY_GROUP_TEMPLATE = AddressTemplate.of(MDB_DELIVERY_GROUP_ADDRESS);

    AddressTemplate APP_SEC_DOMAIN_TEMPLATE = AddressTemplate.of(APP_SEC_DOMAIN_ADDRESS);
}
