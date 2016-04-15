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
package org.jboss.hal.client.configuration.subsystem.datasource;

import org.jboss.hal.meta.AddressTemplate;

/**
 * @author Harald Pehl
 */
public interface AddressTemplates {

    String ANY_DATA_SOURCE_ADDRESS = "/{any.profile}/subsystem=datasources/data-source=*";
    String ANY_XA_DATA_SOURCE_ADDRESS = "/{any.profile}/subsystem=datasources/xa-data-source=*";
    String SELECTED_DATA_SOURCE_ADDRESS = "/{selected.profile}/subsystem=datasources/data-source=*";
    String SELECTED_XA_DATA_SOURCE_ADDRESS = "/{selected.profile}/subsystem=datasources/xa-data-source=*";
    String JDBC_DRIVER_ADDRESS = "/{any.profile}/subsystem=datasources/data-source=*";

    AddressTemplate ANY_DATA_SOURCE_TEMPLATE = AddressTemplate.of(ANY_DATA_SOURCE_ADDRESS);
    AddressTemplate SELECTED_DATA_SOURCE_TEMPLATE = AddressTemplate.of(SELECTED_DATA_SOURCE_ADDRESS);
    AddressTemplate SELECTED_XA_DATA_SOURCE_TEMPLATE = AddressTemplate.of(SELECTED_XA_DATA_SOURCE_ADDRESS);
    AddressTemplate JDBC_DRIVER_TEMPLATE = AddressTemplate.of(JDBC_DRIVER_ADDRESS);

    AddressTemplate DATA_SOURCE_SUBSYSTEM_TEMPLATE = AddressTemplate.of("/{selected.profile}/subsystem=datasources");
}
