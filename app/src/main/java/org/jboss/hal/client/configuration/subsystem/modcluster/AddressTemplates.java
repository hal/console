/*
 * Copyright 2015-2018 Red Hat, Inc, and individual contributors.
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
package org.jboss.hal.client.configuration.subsystem.modcluster;

import org.jboss.hal.meta.AddressTemplate;

public interface AddressTemplates {

    String MODCLUSTER_ADDRESS = "/{selected.profile}/subsystem=modcluster";
    String PROXY_ADDRESS = "/{selected.profile}/subsystem=modcluster/proxy=*";
    String DYNAMIC_LOAD_PROVIDER_ADDRESS = "/{selected.profile}/subsystem=modcluster/proxy=*/dynamic-load-provider=configuration";
    String CUSTOM_LOAD_METRIC_ADDRESS = "/{selected.profile}/subsystem=modcluster/proxy=*/dynamic-load-provider=configuration/custom-load-metric=*";
    String LOAD_METRIC_ADDRESS = "/{selected.profile}/subsystem=modcluster/proxy=*/dynamic-load-provider=configuration/load-metric=*";

    AddressTemplate MODCLUSTER_TEMPLATE = AddressTemplate.of(MODCLUSTER_ADDRESS);
    AddressTemplate PROXY_TEMPLATE = AddressTemplate.of(PROXY_ADDRESS);
    AddressTemplate DYNAMIC_LOAD_PROVIDER_TEMPLATE = AddressTemplate.of(DYNAMIC_LOAD_PROVIDER_ADDRESS);
    AddressTemplate CUSTOM_LOAD_METRIC_TEMPLATE = AddressTemplate.of(CUSTOM_LOAD_METRIC_ADDRESS);
    AddressTemplate LOAD_METRIC_TEMPLATE = AddressTemplate.of(LOAD_METRIC_ADDRESS);

}
