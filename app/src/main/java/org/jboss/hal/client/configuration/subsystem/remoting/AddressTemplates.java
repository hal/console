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
package org.jboss.hal.client.configuration.subsystem.remoting;

import org.jboss.hal.meta.AddressTemplate;

/**
 * @author Harald Pehl
 */
public interface AddressTemplates {

    String REMOTING_SUBSYSTEM_ADDRESS = "/{selected.profile}/subsystem=remoting";

    String CONNECTOR_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS + "/connector=*";
    String HTTP_CONNECTOR_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS + "/http-connector=*";

    String LOCAL_OUTBOUND_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS + "/local-outbound-connection=*";
    String OUTBOUND_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS + "/outbound-connection=*";
    String REMOTE_OUTBOUND_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS + "/remote-outbound-connection=*";

    AddressTemplate REMOTING_SUBSYSTEM_TEMPLATE = AddressTemplate.of(REMOTING_SUBSYSTEM_ADDRESS);

    AddressTemplate CONNECTOR_TEMPLATE = AddressTemplate.of(CONNECTOR_ADDRESS);
    AddressTemplate HTTP_CONNECTOR_TEMPLATE = AddressTemplate.of(HTTP_CONNECTOR_ADDRESS);

    AddressTemplate LOCAL_OUTBOUND_TEMPLATE = AddressTemplate.of(LOCAL_OUTBOUND_ADDRESS);
    AddressTemplate OUTBOUND_TEMPLATE = AddressTemplate.of(OUTBOUND_ADDRESS);
    AddressTemplate REMOTE_OUTBOUND_TEMPLATE = AddressTemplate.of(REMOTE_OUTBOUND_ADDRESS);
}
