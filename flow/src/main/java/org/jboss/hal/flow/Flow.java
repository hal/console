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

import java.util.List;
import java.util.function.Predicate;

import elemental2.promise.Promise;

import static org.jboss.hal.flow.FlowExecutor.Mode.PARALLEL;
import static org.jboss.hal.flow.FlowExecutor.Mode.SEQUENTIAL;

public interface Flow {

    static <C extends FlowContext> Promise<C> parallel(C context, List<Task<C>> tasks) {
        return parallel(context, tasks, true);
    }

    static <C extends FlowContext> Promise<C> parallel(C context, List<Task<C>> tasks, boolean failFast) {
        return new FlowExecutor<>(PARALLEL, context, tasks, failFast).execute();
    }

    static <C extends FlowContext> Promise<C> series(C context, List<Task<C>> tasks) {
        return series(context, tasks, true);
    }

    static <C extends FlowContext> Promise<C> series(C context, List<Task<C>> tasks, boolean failFast) {
        return new FlowExecutor<>(SEQUENTIAL, context, tasks, failFast).execute();
    }

    static <C extends FlowContext> Promise<C> repeat(C context, Task<C> task, Predicate<C> until, int timeout) {
        return new FlowLoop<>(context, task, until, timeout, false).execute();
    }

    static <C extends FlowContext> Promise<C> repeat(C context, Task<C> task, Predicate<C> until, int timeout,
            boolean failFast) {
        return new FlowLoop<>(context, task, until, timeout, failFast).execute();
    }
}
