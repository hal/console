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

import java.util.function.Predicate;

/**
 * An interface to control the {@linkplain Flow#while_(FlowContext, Task, Predicate) repeated} execution of an {@linkplain Task
 * asynchronous task}.
 *
 * @param <C> the type of the {@linkplain FlowContext context} shared between tasks
 */
public interface While<C extends FlowContext> extends Promisable<C>, Subscription<C> {

    /**
     * By default, the execution of {@linkplain Task tasks} fails fast.
     */
    boolean DEFAULT_FAIL_FAST = true;

    /**
     * By default, the interval between the iterations is 1 second.
     */
    long DEFAULT_INTERVAL = 1_000;

    /**
     * By default, the timeout for the while loop is 10 seconds.
     */
    long DEFAULT_TIMEOUT = 10_000;

    /**
     * The error timeout.
     */
    String TIMEOUT_ERROR = "org.jboss.hal.flow.timeout";

    /**
     * Whether the execution of {@linkplain Task tasks} should fail fast or fail last.
     */
    While<C> failFast(boolean failFast);

    /**
     * The interval in milliseconds between the iterations.
     */
    While<C> interval(long interval);

    /**
     * The timeout in milliseconds for the while loop.
     */
    While<C> timeout(long timeout);
}
