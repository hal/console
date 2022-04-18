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
package org.jboss.hal.flow;

import static elemental2.dom.DomGlobal.console;

/**
 * An interface to subscribe to the result of the execution of a list of {@linkplain Task asynchronous tasks}.
 *
 * @param <C> the type of the {@linkplain FlowContext context} shared between tasks
 */
public interface Subscription<C extends FlowContext> {

    /**
     * Subscribes to the successful termination of the {@linkplain Task tasks}. The failed termination is handled by a default
     * implementation that logs the context and the failure to the browser console.
     *
     * @param onSuccess the callback for successful termination
     */
    default void subscribe(SuccessCallback<C> onSuccess) {
        subscribe(onSuccess, ((context, failure) -> console.error("Default failure callback for flow subscription. " +
                "Context: '" + context + "', failure: '" + failure + "'")));
    }

    /**
     * Subscribes to the termination of the {@linkplain Task tasks}.
     *
     * @param onSuccess the callback for successful termination
     * @param onFailure the callback for failed termination
     */
    void subscribe(SuccessCallback<C> onSuccess, FailureCallback<C> onFailure);
}
