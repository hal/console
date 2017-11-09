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

import javax.inject.Inject;

import com.google.common.base.Stopwatch;
import elemental2.dom.Worker;
import jsinterop.annotations.JsType;
import org.jboss.hal.db.Document;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptionDatabase;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextDatabase;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.meta.Database.PAYLOAD;
import static org.jboss.hal.resources.UIConstants.OBJECT;

public class WorkerChannel {

    private static final String WORKER_JS = "js/worker.js";
    @NonNls private static final Logger logger = LoggerFactory.getLogger(WorkerChannel.class);

    private final ResourceDescriptionDatabase resourceDescriptionDatabase;
    private final SecurityContextDatabase securityContextDatabase;
    private final Worker worker;

    @Inject
    public WorkerChannel(ResourceDescriptionDatabase resourceDescriptionDatabase,
            SecurityContextDatabase securityContextDatabase) {
        this.resourceDescriptionDatabase = resourceDescriptionDatabase;
        this.securityContextDatabase = securityContextDatabase;
        this.worker = new Worker(WORKER_JS);
    }

    public void postResourceDescription(ResourceAddress address, ResourceDescription resourceDescription) {
        if (worker != null) {
            Stopwatch watch = Stopwatch.createStarted();
            Document document = resourceDescriptionDatabase.asDocument(address, resourceDescription);
            UpdateMessage message = new UpdateMessage();
            message.database = resourceDescriptionDatabase.name();
            message.document = document;
            worker.postMessage(message);
            watch.stop();
            logger.debug("Posted resource description: {} bytes in {} ms", document.getAny(PAYLOAD).asString().length(),
                    watch.elapsed(MILLISECONDS));
        }
    }

    public void postSecurityContext(ResourceAddress address, SecurityContext securityContext) {
        if (worker != null) {
            UpdateMessage message = new UpdateMessage();
            message.database = securityContextDatabase.name();
            message.document = securityContextDatabase.asDocument(address, securityContext);
            worker.postMessage(message);
        }
    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    private static class UpdateMessage {

        String database;
        Document document;
    }
}
