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

import static org.jboss.hal.meta.SelectionAwareStatementContext.SELECTION_EXPRESSION;

/**
 * @author Harald Pehl
 */
interface AddressTemplates {

    String UNDERTOW_SUBSYSTEM_ADDRESS = "/{selected.profile}/subsystem=undertow";
    String BUFFER_CACHE_ADDRESS = UNDERTOW_SUBSYSTEM_ADDRESS + "/buffer-cache=*";
    String FILTER_ADDRESS = UNDERTOW_SUBSYSTEM_ADDRESS + "/configuration=filter";
    String HANDLER_ADDRESS = UNDERTOW_SUBSYSTEM_ADDRESS + "/configuration=handler";
    String HOST_ADDRESS = UNDERTOW_SUBSYSTEM_ADDRESS + "/server=*/host=*";
    String RESPONSE_HEADER_ADDRESS = FILTER_ADDRESS + "/response-header=*";
    String SERVER_ADDRESS = UNDERTOW_SUBSYSTEM_ADDRESS + "/server=*";
    String SELECTED_SERVER_ADDRESS = UNDERTOW_SUBSYSTEM_ADDRESS + "/server=" + SELECTION_EXPRESSION;
    String SERVLET_CONTAINER_ADDRESS = UNDERTOW_SUBSYSTEM_ADDRESS + "/servlet-container=*";
    String SELECTED_SERVLET_CONTAINER_ADDRESS = UNDERTOW_SUBSYSTEM_ADDRESS + "/servlet-container=" + SELECTION_EXPRESSION;

    AddressTemplate UNDERTOW_SUBSYSTEM_TEMPLATE = AddressTemplate.of(UNDERTOW_SUBSYSTEM_ADDRESS);
    AddressTemplate FILTER_TEMPLATE = AddressTemplate.of(FILTER_ADDRESS);
    AddressTemplate HANDLER_TEMPLATE = AddressTemplate.of(HANDLER_ADDRESS);
    AddressTemplate HOST_TEMPLATE = AddressTemplate.of(HOST_ADDRESS);
    AddressTemplate RESPONSE_HEADER_TEMPLATE = AddressTemplate.of(RESPONSE_HEADER_ADDRESS);
    AddressTemplate SERVER_TEMPLATE = AddressTemplate.of(SERVER_ADDRESS);
    AddressTemplate SELECTED_SERVER_TEMPLATE = AddressTemplate.of(SELECTED_SERVER_ADDRESS);
    AddressTemplate SERVLET_CONTAINER_TEMPLATE = AddressTemplate.of(SERVLET_CONTAINER_ADDRESS);
    AddressTemplate SELECTED_SERVLET_CONTAINER_TEMPLATE = AddressTemplate.of(SELECTED_SERVLET_CONTAINER_ADDRESS);
}
