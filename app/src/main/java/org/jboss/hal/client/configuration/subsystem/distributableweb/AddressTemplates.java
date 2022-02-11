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
package org.jboss.hal.client.configuration.subsystem.distributableweb;

import org.jboss.hal.meta.AddressTemplate;

interface AddressTemplates {

    String DISTRIBUTABLE_WEB_ADDRESS = "/{selected.profile}/subsystem=distributable-web";

    String HOTROD_SESSION_MANAGEMENT = "/hotrod-session-management=*";
    String INFINISPAN_SESSION_MANAGEMENT = "/infinispan-session-management=*";

    AddressTemplate DISTRIBUTABLE_WEB_TEMPLATE = AddressTemplate.of(DISTRIBUTABLE_WEB_ADDRESS);

    AddressTemplate HOTROD_SESSION_TEMPLATE = DISTRIBUTABLE_WEB_TEMPLATE.append(HOTROD_SESSION_MANAGEMENT);
    AddressTemplate INFINISPAN_SESSION_TEMPLATE = DISTRIBUTABLE_WEB_TEMPLATE.append(INFINISPAN_SESSION_MANAGEMENT);
    AddressTemplate AFFINITY_TEMPLATE = AddressTemplate.of("/affinity=*");
}
