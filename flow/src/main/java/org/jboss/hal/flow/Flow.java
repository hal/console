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

import static org.jboss.hal.flow.FlowSequence.Mode.PARALLEL;
import static org.jboss.hal.flow.FlowSequence.Mode.SEQUENTIAL;

/**
 * An interface to execute a list of {@linkplain Task asynchronous tasks} in {@linkplain #parallel(FlowContext, List) parallel},
 * in {@linkplain #sequential(FlowContext, List) sequence} or {@linkplain #while_(FlowContext, Task, Predicate) while} a
 * {@linkplain Predicate predicate} evaluates to {@code true}.
 * <p>
 * The {@linkplain Task tasks} share a {@linkplain FlowContext context} that can be used to store data in a map or on a stack.
 *
 * @param <C> the type of the {@linkplain FlowContext context} shared between tasks
 */
public interface Flow<C extends FlowContext> {

    /**
     * Executes a list of {@linkplain Task asynchronous tasks} in parallel (all at once).
     *
     * @param context the context shared between tasks
     * @param tasks the list of tasks to execute in parallel
     * @param <C> the type of the shared context
     * @return an interface to control whether the execution of the tasks should fail fast or fail last
     */
    static <C extends FlowContext> Sequence<C> parallel(C context, List<Task<C>> tasks) {
        return new FlowSequence<>(PARALLEL, context, tasks);
    }

    /**
     * Executes a list of {@linkplain Task asynchronous tasks} in sequence (one after the other).
     *
     * @param context the context shared between tasks
     * @param tasks the list of tasks to execute in order
     * @param <C> the type of the shared context
     * @return an interface to control whether the execution of the tasks should fail fast or fail last
     */
    static <C extends FlowContext> Sequence<C> sequential(C context, List<Task<C>> tasks) {
        return new FlowSequence<>(SEQUENTIAL, context, tasks);
    }

    /**
     * Executes the given {@linkplain Task task} as long as the given {@linkplain Predicate predicate} evaluates to
     * {@code true}.
     *
     * @param context the context shared between the iterations
     * @param task the task to execute while the predicate evaluates to {@code true}
     * @param until the predicate used to decide whether to continue or break the loop
     * @param <C> the type of the shared context
     * @return an interface to control the interval, timeout and fail fast behaviour
     */
    static <C extends FlowContext> While<C> while_(C context, Task<C> task, Predicate<C> until) {
        return new FlowWhile<>(context, task, until);
    }
}
