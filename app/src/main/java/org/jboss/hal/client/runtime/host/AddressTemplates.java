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
package org.jboss.hal.client.runtime.host;

import org.jboss.hal.meta.AddressTemplate;

interface AddressTemplates {

    String ELYTRON_ADDRESS = "{domain.controller}" + "/subsystem=elytron";
    String HTTP_INTERFACE_ADDRESS = "{selected.host}/core-service=management/management-interface=http-interface";
    String HOST_CONNECTION_ADDRESS = "/core-service=management/host-connection=*";
    String NATIVE_INTERFACE_ADDRESS = "{selected.host}/core-service=management/management-interface=native-interface";

    AddressTemplate ELYTRON_TEMPLATE = AddressTemplate.of(ELYTRON_ADDRESS);
    AddressTemplate HTTP_INTERFACE_TEMPLATE = AddressTemplate.of(HTTP_INTERFACE_ADDRESS);
    AddressTemplate HOST_CONNECTION_TEMPLATE = AddressTemplate.of(HOST_CONNECTION_ADDRESS);
    AddressTemplate NATIVE_INTERFACE_TEMPLATE = AddressTemplate.of(NATIVE_INTERFACE_ADDRESS);
}
