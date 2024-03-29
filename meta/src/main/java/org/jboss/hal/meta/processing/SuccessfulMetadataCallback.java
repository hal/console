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

import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import com.google.web.bindery.event.shared.EventBus;

public abstract class SuccessfulMetadataCallback implements MetadataProcessor.MetadataCallback {

    private final EventBus eventBus;
    private final Resources resources;

    protected SuccessfulMetadataCallback(EventBus eventBus, Resources resources) {
        this.eventBus = eventBus;
        this.resources = resources;
    }

    @Override
    public void onError(Throwable error) {
        MessageEvent.fire(eventBus, Message.error(resources.messages().metadataError(), error.getMessage()));
    }
}
