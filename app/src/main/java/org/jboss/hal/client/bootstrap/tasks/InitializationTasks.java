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
package org.jboss.hal.client.bootstrap.tasks;

import javax.inject.Inject;

/** Simple wrapper around an ordered array of initialization tasks. */
public class InitializationTasks {

    private final InitializedTask[] tasks;

    @Inject
    public InitializationTasks(CheckForUpdate checkForUpdate,
            CheckTargetVersion checkTargetVersion,
            PollingTasks pollingTasks) {
        this.tasks = new InitializedTask[] {
                checkForUpdate,
                checkTargetVersion,
                pollingTasks
        };
    }

    public InitializedTask[] tasks() {
        return tasks;
    }
}
