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
package org.jboss.hal.client.configuration.subsystem.datasource;

import org.jboss.hal.meta.AddressTemplate;

import static org.jboss.hal.meta.StatementContext.Expression.SELECTED_PROFILE;

public interface AddressTemplates {

    String DATA_SOURCE_ADDRESS = "/{selected.profile}/subsystem=datasources/data-source=*";
    String XA_DATA_SOURCE_ADDRESS = "/{selected.profile}/subsystem=datasources/xa-data-source=*";
    String XA_DATA_SOURCE_PROPERTIES_ADDRESS = XA_DATA_SOURCE_ADDRESS + "/xa-datasource-properties=*";
    String JDBC_DRIVER_ADDRESS = "/{selected.profile}/subsystem=datasources/jdbc-driver=*";

    AddressTemplate DATA_SOURCE_TEMPLATE = AddressTemplate.of(DATA_SOURCE_ADDRESS);
    AddressTemplate XA_DATA_SOURCE_TEMPLATE = AddressTemplate.of(XA_DATA_SOURCE_ADDRESS);
    AddressTemplate XA_DATA_SOURCE_PROPERTIES_TEMPLATE = AddressTemplate.of(XA_DATA_SOURCE_PROPERTIES_ADDRESS);
    AddressTemplate JDBC_DRIVER_TEMPLATE = AddressTemplate.of(JDBC_DRIVER_ADDRESS);
    AddressTemplate DATA_SOURCE_SUBSYSTEM_TEMPLATE = AddressTemplate.of(SELECTED_PROFILE, "subsystem=datasources");
}
