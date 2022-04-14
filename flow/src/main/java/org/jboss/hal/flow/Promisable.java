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

import elemental2.promise.IThenable.ThenOnFulfilledCallbackFn;
import elemental2.promise.IThenable.ThenOnRejectedCallbackFn;
import elemental2.promise.Promise;
import elemental2.promise.Promise.CatchOnRejectedCallbackFn;
import elemental2.promise.Promise.FinallyOnFinallyCallbackFn;

/**
 * An interface to use the promise when executing a list of {@linkplain Task asynchronous tasks}.
 *
 * Most of the methods are taken from the JavaScript object
 * <a href="https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/Promise">Promise</a>.
 *
 * @param <C> the type of the {@linkplain FlowContext context} shared between tasks
 */
public interface Promisable<C extends FlowContext> {

    <V> Promise<V> then(ThenOnFulfilledCallbackFn<? super C, ? extends V> onFulfilled);

    <V> Promise<V> then(
            ThenOnFulfilledCallbackFn<? super C, ? extends V> onFulfilled,
            ThenOnRejectedCallbackFn<? extends V> onRejected);

    <V> Promise<V> catch_(CatchOnRejectedCallbackFn<? extends V> onRejected);

    Promise<C> finally_(FinallyOnFinallyCallbackFn onFinally);

    /**
     * Returns the promise of the asynchronous execution.
     */
    Promise<C> promise();
}
