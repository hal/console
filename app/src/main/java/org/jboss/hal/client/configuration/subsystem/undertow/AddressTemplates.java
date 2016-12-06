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
package org.jboss.hal.client.configuration.subsystem.undertow;

import org.jboss.hal.meta.AddressTemplate;

/**
 * @author Harald Pehl
 */
interface AddressTemplates {

    String UNDERTOW_SUBSYSTEM_ADDRESS = "/{selected.profile}/subsystem=undertow";
    String BUFFER_CACHE_ADDRESS = UNDERTOW_SUBSYSTEM_ADDRESS + "/buffer-cache=*";
    String UNDERTOW_FILTER_ADDRESS = UNDERTOW_SUBSYSTEM_ADDRESS + "/configuration=filter";
    String UNDERTOW_HANDLER_ADDRESS = UNDERTOW_SUBSYSTEM_ADDRESS + "/configuration=handler";
    String UNDERTOW_SERVER_ADDRESS = UNDERTOW_SUBSYSTEM_ADDRESS + "/server=*";
    String UNDERTOW_SERVLET_CONTAINER_ADDRESS = UNDERTOW_SUBSYSTEM_ADDRESS + "/servlet-container=*";

    AddressTemplate UNDERTOW_SUBSYSTEM_TEMPLATE = AddressTemplate.of(UNDERTOW_SUBSYSTEM_ADDRESS);
    AddressTemplate BUFFER_CACHE_TEMPLATE = AddressTemplate.of(BUFFER_CACHE_ADDRESS);
    AddressTemplate UNDERTOW_FILTER_TEMPLATE = AddressTemplate.of(UNDERTOW_FILTER_ADDRESS);
    AddressTemplate UNDERTOW_HANDLER_TEMPLATE = AddressTemplate.of(UNDERTOW_HANDLER_ADDRESS);
    AddressTemplate UNDERTOW_SERVER_TEMPLATE = AddressTemplate.of(UNDERTOW_SERVER_ADDRESS);
    AddressTemplate UNDERTOW_SERVLET_CONTAINER_TEMPLATE = AddressTemplate.of(UNDERTOW_SERVLET_CONTAINER_ADDRESS);
}
