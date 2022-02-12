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
package org.jboss.hal.meta.processing;

import java.util.Map;

import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import rx.Completable;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

class UpdateDatabaseTask implements Task<LookupContext> {

    private static final Logger logger = LoggerFactory.getLogger(UpdateDatabaseTask.class);

    private final WorkerChannel workerChannel;

    UpdateDatabaseTask(WorkerChannel workerChannel) {
        this.workerChannel = workerChannel;
    }

    @SuppressWarnings("unchecked")
    public Completable call(LookupContext context) {
        if (context.updateDatabase()) {
            Stopwatch watch = Stopwatch.createStarted();
            for (Map.Entry<ResourceAddress, ResourceDescription> entry : context.toResourceDescriptionDatabase
                    .entrySet()) {
                ResourceAddress address = entry.getKey();
                ResourceDescription resourceDescription = entry.getValue();
                workerChannel.postResourceDescription(address, resourceDescription,
                        context.recursive);
            }
            for (Map.Entry<ResourceAddress, SecurityContext> entry : context.toSecurityContextDatabase
                    .entrySet()) {
                ResourceAddress address = entry.getKey();
                SecurityContext securityContext = entry.getValue();
                workerChannel.postSecurityContext(address, securityContext, context.recursive);
            }
            logger.debug(
                    "Posted {} resource descriptions and {} security contexts to the databases in {} ms",
                    context.toResourceDescriptionDatabase.size(), context.toSecurityContextDatabase.size(),
                    watch.stop().elapsed(MILLISECONDS));
        }
        return Completable.complete();
    }
}
