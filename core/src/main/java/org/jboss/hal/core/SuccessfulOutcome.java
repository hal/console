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
package org.jboss.hal.core;

import org.jboss.hal.flow.Outcome;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import com.google.web.bindery.event.shared.EventBus;

/** An outcome implementation which has a default implementation for the {@link Outcome#onError(Throwable)} method. */
public abstract class SuccessfulOutcome<C> extends Outcome<C> {

    private final EventBus eventBus;
    private final Resources resources;

    protected SuccessfulOutcome(EventBus eventBus, Resources resources) {
        this.eventBus = eventBus;
        this.resources = resources;
    }

    /** Emits a error message using the {@link org.jboss.hal.resources.Messages#lastOperationFailed()} error message. */
    @Override
    public void onError(C context, Throwable throwable) {
        MessageEvent.fire(eventBus, Message.error(resources.messages().lastOperationFailed(), throwable.getMessage()));
    }
}
