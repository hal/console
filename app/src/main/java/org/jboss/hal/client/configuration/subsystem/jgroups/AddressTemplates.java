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
package org.jboss.hal.client.configuration.subsystem.jgroups;

import org.jboss.hal.meta.AddressTemplate;

interface AddressTemplates {

    String JGROUPS_ADDRESS = "/{selected.profile}/subsystem=jgroups";
    String STACK_ADDRESS = JGROUPS_ADDRESS + "/stack=*";
    String RELAY_ADDRESS = JGROUPS_ADDRESS + "/stack=*/relay=relay.RELAY2";
    String SELECTED_RELAY_ADDRESS = JGROUPS_ADDRESS + "/stack={selected.stack}/relay=relay.RELAY2";
    String REMOTE_SITE_ADDRESS = JGROUPS_ADDRESS + "/stack=*/relay=relay.RELAY2/remote-site=*";
    String SELECTED_REMOTE_SITE_ADDRESS = JGROUPS_ADDRESS + "/stack={selected.stack}/relay=relay.RELAY2/remote-site=*";
    String PROTOCOL_ADDRESS = JGROUPS_ADDRESS + "/stack=*/protocol=*";
    String SELECTED_PROTOCOL_ADDRESS = JGROUPS_ADDRESS + "/stack={selected.stack}/protocol=*";
    String TRANSPORT_THREAD_POOL_DEFAULT_ADDRESS = JGROUPS_ADDRESS + "/stack=*/transport=*/thread-pool=default";
    String TRANSPORT_THREAD_POOL_INTERNAL_ADDRESS = JGROUPS_ADDRESS + "/stack=*/transport=*/thread-pool=internal";
    String TRANSPORT_THREAD_POOL_OOB_ADDRESS = JGROUPS_ADDRESS + "/stack=*/transport=*/thread-pool=oob";
    String TRANSPORT_THREAD_POOL_TIMER_ADDRESS = JGROUPS_ADDRESS + "/stack=*/transport=*/thread-pool=timer";
    String SELECTED_TRANSPORT_THREAD_POOL_ADDRESS = JGROUPS_ADDRESS + "/stack={selected.stack}/transport=*/thread-pool=*";
    String TRANSPORT_ADDRESS = JGROUPS_ADDRESS + "/stack=*/transport=*";
    String SELECTED_TRANSPORT_ADDRESS = JGROUPS_ADDRESS + "/stack={selected.stack}/transport=*";
    String CHANNEL_ADDRESS = JGROUPS_ADDRESS + "/channel=*";
    String CHANNEL_FORK_ADDRESS = JGROUPS_ADDRESS + "/channel=*/fork=*";
    String CHANNEL_FORK_PROTOCOL_ADDRESS = JGROUPS_ADDRESS + "/channel=*/fork=*/protocol=*";
    String SELECTED_CHANNEL_FORK_ADDRESS = JGROUPS_ADDRESS + "/channel={selected.channel}/fork=*";
    String SELECTED_CHANNEL_FORK_PROTOCOL_ADDRESS = JGROUPS_ADDRESS
            + "/channel={selected.channel}/fork={selected.fork}/protocol=*";
    String AUTH_TOKEN_ADDRESS = JGROUPS_ADDRESS + "/stack=*/protocol=AUTH/token=*";
    String SELECTED_AUTH_TOKEN_ADDRESS = JGROUPS_ADDRESS + "/stack={selected.stack}/protocol=AUTH/token=*";

    AddressTemplate JGROUPS_TEMPLATE = AddressTemplate.of(JGROUPS_ADDRESS);
    AddressTemplate STACK_TEMPLATE = AddressTemplate.of(STACK_ADDRESS);
    AddressTemplate RELAY_TEMPLATE = AddressTemplate.of(RELAY_ADDRESS);
    AddressTemplate SELECTED_RELAY_TEMPLATE = AddressTemplate.of(SELECTED_RELAY_ADDRESS);
    AddressTemplate SELECTED_PROTOCOL_TEMPLATE = AddressTemplate.of(SELECTED_PROTOCOL_ADDRESS);
    AddressTemplate PROTOCOL_TEMPLATE = AddressTemplate.of(PROTOCOL_ADDRESS);
    AddressTemplate TRANSPORT_TEMPLATE = AddressTemplate.of(TRANSPORT_ADDRESS);
    AddressTemplate SELECTED_TRANSPORT_TEMPLATE = AddressTemplate.of(SELECTED_TRANSPORT_ADDRESS);
    AddressTemplate SELECTED_TRANSPORT_THREAD_POOL_TEMPLATE = AddressTemplate.of(
            SELECTED_TRANSPORT_THREAD_POOL_ADDRESS);
    AddressTemplate TRANSPORT_THREAD_POOL_DEFAULT_TEMPLATE = AddressTemplate.of(TRANSPORT_THREAD_POOL_DEFAULT_ADDRESS);
    AddressTemplate TRANSPORT_THREAD_POOL_INTERNAL_TEMPLATE = AddressTemplate.of(
            TRANSPORT_THREAD_POOL_INTERNAL_ADDRESS);
    AddressTemplate TRANSPORT_THREAD_POOL_OOB_TEMPLATE = AddressTemplate.of(TRANSPORT_THREAD_POOL_OOB_ADDRESS);
    AddressTemplate TRANSPORT_THREAD_POOL_TIMER_TEMPLATE = AddressTemplate.of(TRANSPORT_THREAD_POOL_TIMER_ADDRESS);
    AddressTemplate REMOTE_SITE_TEMPLATE = AddressTemplate.of(REMOTE_SITE_ADDRESS);
    AddressTemplate SELECTED_REMOTE_SITE_TEMPLATE = AddressTemplate.of(SELECTED_REMOTE_SITE_ADDRESS);
    AddressTemplate CHANNEL_TEMPLATE = AddressTemplate.of(CHANNEL_ADDRESS);
    AddressTemplate CHANNEL_FORK_TEMPLATE = AddressTemplate.of(CHANNEL_FORK_ADDRESS);
    AddressTemplate SELECTED_CHANNEL_FORK_TEMPLATE = AddressTemplate.of(SELECTED_CHANNEL_FORK_ADDRESS);
    AddressTemplate CHANNEL_FORK_PROTOCOL_TEMPLATE = AddressTemplate.of(CHANNEL_FORK_PROTOCOL_ADDRESS);
    AddressTemplate SELECTED_CHANNEL_FORK_PROTOCOL_TEMPLATE = AddressTemplate.of(
            SELECTED_CHANNEL_FORK_PROTOCOL_ADDRESS);
    AddressTemplate AUTH_TOKEN_TEMPLATE = AddressTemplate.of(AUTH_TOKEN_ADDRESS);
    AddressTemplate SELECTED_AUTH_TOKEN_TEMPLATE = AddressTemplate.of(SELECTED_AUTH_TOKEN_ADDRESS);

}
