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
package org.jboss.hal.client.runtime.host;

import org.jboss.hal.meta.AddressTemplate;

import static org.jboss.hal.client.runtime.configurationchanges.ConfigurationChangesPresenter.CONFIGURATION_CHANGES_ADDRESS;

interface AddressTemplates {

    String DOMAIN_CONTROLLER = "{domain.controller}";
    String ANY_HOST = "host=*";

    String ELYTRON_ADDRESS = DOMAIN_CONTROLLER + "/subsystem=elytron";
    String HTTP_INTERFACE_ADDRESS = ANY_HOST + "/core-service=management/management-interface=http-interface";
    String HOST_CONNECTION_ADDRESS = "/core-service=management/host-connection=*";
    String HOST_CONFIGURATION_CHANGES_ADDRESS = ANY_HOST + CONFIGURATION_CHANGES_ADDRESS;
    String HOST_MANAGEMENT_OPERATIONS_ADDRESS = ANY_HOST + "/core-service=management/service=management-operations";
    String INTERFACE_ADDRESS = ANY_HOST + "/interface=*";
    String JVM_ADDRESS = ANY_HOST + "/jvm=*";
    String NATIVE_INTERFACE_ADDRESS = ANY_HOST + "/core-service=management/management-interface=native-interface";
    String PATH_ADDRESS = ANY_HOST + "/path=*";
    String SOCKET_BINDING_GROUP_ADDRESS = ANY_HOST + "/socket-binding-group=*";
    String SYSTEM_PROPERTY_ADDRESS = ANY_HOST + "/system-property=*";

    AddressTemplate HTTP_INTERFACE_TEMPLATE = AddressTemplate.of(HTTP_INTERFACE_ADDRESS);
    AddressTemplate NATIVE_INTERFACE_TEMPLATE = AddressTemplate.of(NATIVE_INTERFACE_ADDRESS);
    AddressTemplate ELYTRON_TEMPLATE = AddressTemplate.of(ELYTRON_ADDRESS);
    AddressTemplate HOST_CONNECTION_TEMPLATE = AddressTemplate.of(HOST_CONNECTION_ADDRESS);
}
