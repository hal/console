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
package org.jboss.hal.client.installer;

import org.jboss.hal.meta.AddressTemplate;

import static org.jboss.hal.meta.StatementContext.Expression.SELECTED_HOST;

public interface AddressTemplates {

    String INSTALLER_ADDRESS = "/core-service=installer";

    AddressTemplate ROOT_TEMPLATE = AddressTemplate.of(SELECTED_HOST);
    AddressTemplate INSTALLER_TEMPLATE = AddressTemplate.of(SELECTED_HOST, INSTALLER_ADDRESS);
}
