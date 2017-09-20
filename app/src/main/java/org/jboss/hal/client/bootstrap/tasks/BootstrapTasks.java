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
package org.jboss.hal.client.bootstrap.tasks;

import javax.inject.Inject;

/** Simple wrapper around an ordered array of HAL's bootstrap tasks. */
public class BootstrapTasks {

    private final BootstrapTask[] functions;

    @Inject
    public BootstrapTasks( ReadEnvironment readEnvironment,
             ReadAuthentication readAuthentication,
             FindDomainController findDomainController,
             RegisterStaticCapabilities registerStaticCapabilities,
             LoadSettings loadSettings,
             ReadExtensions readExtensions) {
        this.functions = new BootstrapTask[]{
                readEnvironment,
                readAuthentication,
                findDomainController,
                registerStaticCapabilities,
                loadSettings,
                readExtensions
        };
    }

    public BootstrapTask[] functions() {
        return functions;
    }
}
