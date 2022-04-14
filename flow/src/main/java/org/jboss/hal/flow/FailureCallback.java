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

/**
 * A callback for the failed execution a list of {@linkplain Task asynchronous tasks}.
 *
 * @param <C> the type of the {@linkplain FlowContext context} shared between the tasks
 */
@FunctionalInterface
public interface FailureCallback<C extends FlowContext> {

    /**
     * Called when the execution of the {@linkplain Task asynchronous tasks} failed.
     *
     * @param context the context shared between the tasks
     * @param failure the reason why the execution failed
     */
    void failed(C context, String failure);
}
