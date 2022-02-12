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
package org.jboss.hal.client.configuration.subsystem.webservice;

import org.jboss.hal.meta.AddressTemplate;

interface AddressTemplates {

    String CONFIG_TYPE = "config-type";
    String CONFIG_NAME = "config-name";
    String HANDLER_CHAIN_TYPE = "handler-chain-type";
    String HANDLER_CHAIN_NAME = "handler-chain-name";

    String WEBSERVICES_ADDRESS = "/{selected.profile}/subsystem=webservices";
    String CLIENT_CONFIG_ADDRESS = WEBSERVICES_ADDRESS + "/client-config=*";
    String ENDPOINT_CONFIG_ADDRESS = WEBSERVICES_ADDRESS + "/endpoint-config=*";

    AddressTemplate WEBSERVICES_TEMPLATE = AddressTemplate.of(WEBSERVICES_ADDRESS);
    AddressTemplate CLIENT_CONFIG_TEMPLATE = AddressTemplate.of(CLIENT_CONFIG_ADDRESS);
    AddressTemplate ENDPOINT_CONFIG_TEMPLATE = AddressTemplate.of(ENDPOINT_CONFIG_ADDRESS);

    // For the nested pre/post-chain-handler and handler resources we use client-config as base resource.
    // This is ok for reading the metadata since both the client-config and endpoint-config (sub)resources
    // are identical in terms of attributes. However we must not use these constants for CRUD operations!
    String HANDLER_CHAIN_ADDRESS = CLIENT_CONFIG_ADDRESS + "/pre-handler-chain=*";
    String HANDLER_ADDRESS = HANDLER_CHAIN_ADDRESS + "/handler=*";

    AddressTemplate HANDLER_CHAIN_TEMPLATE = AddressTemplate.of(HANDLER_CHAIN_ADDRESS);
    AddressTemplate HANDLER_TEMPLATE = AddressTemplate.of(HANDLER_ADDRESS);

    // These templates are used for crud operations on pre/post-chain-handler and handler resources
    AddressTemplate SELECTED_CONFIG_TEMPLATE = AddressTemplate.of(WEBSERVICES_ADDRESS)
            .append(expression(CONFIG_TYPE) + "=*");
    AddressTemplate SELECTED_HANDLER_CHAIN_TEMPLATE = AddressTemplate.of(WEBSERVICES_ADDRESS)
            .append(expression(CONFIG_TYPE) + "=" + expression(CONFIG_NAME))
            .append(expression(HANDLER_CHAIN_TYPE) + "=*");
    AddressTemplate SELECTED_HANDLER_TEMPLATE = AddressTemplate.of(WEBSERVICES_ADDRESS)
            .append(expression(CONFIG_TYPE) + "=" + expression(CONFIG_NAME))
            .append(expression(HANDLER_CHAIN_TYPE) + "=" + expression(HANDLER_CHAIN_NAME))
            .append("handler=*");

    static String expression(String variable) {
        return "{" + variable + "}";
    }
}
