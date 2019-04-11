/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.microprofile;

import org.jboss.hal.meta.AddressTemplate;

interface AddressTemplates {

    String MICRO_PROFILE_CONFIG_ADDRESS = "/{selected.profile}/subsystem=microprofile-config-smallrye";
    String CONFIG_SOURCE_ADDRESS = MICRO_PROFILE_CONFIG_ADDRESS + "/config-source=*";
    String MICRO_PROFILE_METRICS_ADDRESS = "/{selected.profile}/subsystem=microprofile-metrics-smallrye";

    AddressTemplate MICRO_PROFILE_CONFIG_TEMPLATE = AddressTemplate.of(MICRO_PROFILE_CONFIG_ADDRESS);
    AddressTemplate CONFIG_SOURCE_TEMPLATE = AddressTemplate.of(CONFIG_SOURCE_ADDRESS);
    AddressTemplate MICRO_PROFILE_METRICS_TEMPLATE = AddressTemplate.of(MICRO_PROFILE_METRICS_ADDRESS);
}
