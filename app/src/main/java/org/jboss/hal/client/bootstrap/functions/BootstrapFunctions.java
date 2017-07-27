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
package org.jboss.hal.client.bootstrap.functions;

import javax.inject.Inject;

/** Simple wrapper around an ordered array of HAL's bootstrap functions. */
public class BootstrapFunctions {

    private final BootstrapFunction[] functions;

    @Inject
    public BootstrapFunctions(final ReadEnvironment readEnvironment,
            final ReadAuthentication readAuthentication,
            final FindDomainController findDomainController,
            final RegisterStaticCapabilities registerStaticCapabilities,
            final LoadSettings loadSettings,
            final ReadExtensions readExtensions) {
        this.functions = new BootstrapFunction[]{
                readEnvironment,
                readAuthentication,
                findDomainController,
                registerStaticCapabilities,
                loadSettings,
                readExtensions
        };
    }

    public BootstrapFunction[] functions() {
        return functions;
    }
}
