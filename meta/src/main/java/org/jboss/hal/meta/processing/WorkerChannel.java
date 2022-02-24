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

import javax.inject.Inject;

import org.jboss.hal.db.Document;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.js.Browser;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptionDatabase;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextDatabase;

import elemental2.dom.Worker;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HAL_RECURSIVE;
import static org.jboss.hal.resources.UIConstants.OBJECT;

public class WorkerChannel {

    // provided by app/src/web/script/index.js
    @JsType(isNative = true, namespace = GLOBAL, name = "window")
    static class WorkerProvider {

        @JsProperty static Worker metadataChannel;
    }

    private final ResourceDescriptionDatabase resourceDescriptionDatabase;
    private final SecurityContextDatabase securityContextDatabase;
    private final Worker worker;

    @Inject
    public WorkerChannel(ResourceDescriptionDatabase resourceDescriptionDatabase,
            SecurityContextDatabase securityContextDatabase) {
        this.resourceDescriptionDatabase = resourceDescriptionDatabase;
        this.securityContextDatabase = securityContextDatabase;
        this.worker = Browser.isIE() ? null : WorkerProvider.metadataChannel;
    }

    void postResourceDescription(ResourceAddress address, ResourceDescription resourceDescription, boolean recursive) {
        if (worker != null) {
            resourceDescription.get(HAL_RECURSIVE).set(recursive);
            UpdateMessage message = new UpdateMessage();
            message.database = resourceDescriptionDatabase.name();
            message.document = resourceDescriptionDatabase.asDocument(address, resourceDescription);
            worker.postMessage(message);
        }
    }

    void postSecurityContext(ResourceAddress address, SecurityContext securityContext, boolean recursive) {
        if (worker != null) {
            securityContext.get(HAL_RECURSIVE).set(recursive);
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
