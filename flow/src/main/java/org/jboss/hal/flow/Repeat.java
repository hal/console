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
 * An interface to control the {@linkplain Flow#repeat(FlowContext, Task) repeated} execution of an {@linkplain Task
 * asynchronous task}.
 * <p>
 * You can customize the conditions how often and how long the task is executed using the methods in this interface, before
 * calling any of the methods from {@link Promisable} or {@link Subscription}.
 *
 * @param <C> the type of the {@linkplain FlowContext context} shared between tasks
 */
public interface Repeat<C extends FlowContext> extends Promisable<C>, Subscription<C> {

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
     * By default, the iterations are infinite.
     */
    int DEFAULT_ITERATIONS = -1;

    /**
     * The error message in case of a timeout.
     */
    String TIMEOUT_ERROR = "org.jboss.hal.flow.timeout";

    /**
     * The task is executed as long as the given predicate evaluates to {@code true}. Defaults to a precondition which always
     * returns {@code true}.
     */
    Repeat<C> while_(Predicate<C> predicate);

    /**
     * Whether the execution of {@linkplain Task tasks} should fail fast or fail last. Defaults to
     * {@value Repeat#DEFAULT_FAIL_FAST}.
     */
    Repeat<C> failFast(boolean failFast);

    /**
     * The interval in milliseconds between the iterations. Defaults to {@value Repeat#DEFAULT_INTERVAL} milliseconds.
     */
    Repeat<C> interval(long interval);

    /**
     * The timeout in milliseconds for the while loop. Defaults to {@value Repeat#DEFAULT_TIMEOUT} milliseconds.
     */
    Repeat<C> timeout(long timeout);

    /**
     * The maximal iterations of the loop. Defaults to an infinite loop ({@value Repeat#DEFAULT_ITERATIONS}).
     */
    Repeat<C> iterations(int iterations);
}
