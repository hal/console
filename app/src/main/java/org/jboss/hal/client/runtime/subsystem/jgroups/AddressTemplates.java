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
package org.jboss.hal.client.runtime.subsystem.jgroups;

import org.jboss.hal.meta.AddressTemplate;

public interface AddressTemplates {

    String JGROUPS_ADDRESS = "/{selected.host}/{selected.server}/subsystem=jgroups";
    AddressTemplate JGROUPS_TEMPLATE = AddressTemplate.of(JGROUPS_ADDRESS);

    String CHANNEL_ADDRESS = JGROUPS_ADDRESS + "/channel=*";
    AddressTemplate CHANNEL_TEMPLATE = AddressTemplate.of(CHANNEL_ADDRESS);

    String FORK_ADDRESS = CHANNEL_ADDRESS + "/fork=*";
    AddressTemplate FORK_TEMPLATE = AddressTemplate.of(FORK_ADDRESS);

    String CHANNEL_PROTOCOL_ADDRESS = CHANNEL_ADDRESS + "/protocol=*";
    String STACK_PROTOCOL_ADDRESS = JGROUPS_ADDRESS + "/stack=*/protocol=*";
    AddressTemplate CHANNEL_PROTOCOL_TEMPLATE = AddressTemplate.of(CHANNEL_PROTOCOL_ADDRESS);
    AddressTemplate STACK_PROTOCOL_TEMPLATE = AddressTemplate.of(STACK_PROTOCOL_ADDRESS);
}
