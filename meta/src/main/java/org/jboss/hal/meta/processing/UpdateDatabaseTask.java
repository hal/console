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
package org.jboss.hal.meta.processing;

import java.util.Map;

import com.google.common.base.Stopwatch;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

class UpdateDatabaseTask implements Task<LookupContext> {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(UpdateDatabaseTask.class);

    private final WorkerChannel workerChannel;

    UpdateDatabaseTask(WorkerChannel workerChannel) {
        this.workerChannel = workerChannel;
    }

    @Override
    public Completable call(LookupContext context) {
        Stopwatch rdWatch = Stopwatch.createStarted();
        for (Map.Entry<ResourceAddress, ResourceDescription> entry : context.toResourceDescriptionRegistry.entrySet()) {
            workerChannel.postResourceDescription(entry.getKey(), entry.getValue());
        }
        logger.debug("Posted {} resource descriptions to the worker in {} ms",
                context.toResourceDescriptionRegistry.size(), rdWatch.stop().elapsed(MILLISECONDS));

        Stopwatch scWatch = Stopwatch.createStarted();
        for (Map.Entry<ResourceAddress, SecurityContext> entry : context.toSecurityContextDatabase.entrySet()) {
            workerChannel.postSecurityContext(entry.getKey(), entry.getValue());
        }
        logger.debug("Posted {} security contexts to the worker in {} ms",
                context.toSecurityContextRegistry.size(), scWatch.stop().elapsed(MILLISECONDS));

        return Completable.complete();
    }
}
