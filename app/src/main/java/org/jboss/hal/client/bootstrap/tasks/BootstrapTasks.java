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
package org.jboss.hal.client.bootstrap.tasks;

import javax.inject.Inject;

/** Simple wrapper around an ordered array of bootstrap tasks. */
public class BootstrapTasks {

    private final BootstrapTask[] tasks;

    // Don't change the order unless you know what you're doing!
    @Inject
    public BootstrapTasks(ReadEnvironment readEnvironment,
            ReadAuthentication readAuthentication,
            ReadHostNames readHostNames,
            FindDomainController findDomainController,
            RegisterStaticCapabilities registerStaticCapabilities,
            ReadExtensions readExtensions,
            LoadSettings loadSettings,
            SetTitle setTitle,
            StartAnalytics startAnalytics) {
        this.tasks = new BootstrapTask[] {
                readEnvironment,
                readAuthentication,
                readHostNames,
                findDomainController,
                registerStaticCapabilities,
                readExtensions,
                loadSettings,
                setTitle,
                startAnalytics,
        };
    }

    public BootstrapTask[] tasks() {
        return tasks;
    }
}
