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
package org.jboss.hal.client.configuration.subsystem.security;

import org.jboss.hal.meta.AddressTemplate;

import static org.jboss.hal.meta.SelectionAwareStatementContext.SELECTION_EXPRESSION;

interface AddressTemplates {

    String SECURITY_SUBSYSTEM_ADDRESS = "/{selected.profile}/subsystem=security";
    String SECURITY_DOMAIN_ADDRESS = SECURITY_SUBSYSTEM_ADDRESS + "/security-domain=*";
    String SELECTED_SECURITY_DOMAIN_ADDRESS = SECURITY_SUBSYSTEM_ADDRESS + "/security-domain=" + SELECTION_EXPRESSION;

    AddressTemplate SECURITY_SUBSYSTEM_TEMPLATE = AddressTemplate.of(SECURITY_SUBSYSTEM_ADDRESS);
    AddressTemplate SECURITY_DOMAIN_TEMPLATE = AddressTemplate.of(SECURITY_DOMAIN_ADDRESS);
    AddressTemplate SELECTED_SECURITY_DOMAIN_TEMPLATE = AddressTemplate.of(SELECTED_SECURITY_DOMAIN_ADDRESS);
}
