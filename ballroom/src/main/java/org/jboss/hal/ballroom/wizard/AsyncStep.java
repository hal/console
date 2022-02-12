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
package org.jboss.hal.ballroom.wizard;

/**
 * Mixin interface for wizard steps which need an asynchronous implementation for the cancel, back and next workflow.
 */
public interface AsyncStep<C> {

    /**
     * Called when this step is canceled.
     * <p>
     * The default implementation calls {@link WorkflowCallback#proceed()}.
     *
     * @param context the current context
     * @param callback call {@link WorkflowCallback#proceed()} if we can cancel, do nothing otherwise
     */
    default void onCancel(C context, WorkflowCallback callback) {
        callback.proceed();
    }

    /**
     * Called before the previous step is shown. The method is called no matter if there's a previous step!
     * <p>
     * The default implementation calls {@link WorkflowCallback#proceed()}.
     *
     * @param context the current context
     * @param callback call {@link WorkflowCallback#proceed()} if we can navigate to the previous step, do nothing otherwise
     */
    default void onBack(C context, WorkflowCallback callback) {
        callback.proceed();
    }

    /**
     * Called before the next step is shown. The method is called no matter if there's a next step!
     * <p>
     * The default implementation calls {@link WorkflowCallback#proceed()}.
     *
     * @param context the current context
     * @param callback call {@link WorkflowCallback#proceed()} if we can navigate to the next step, do nothing otherwise
     */
    default void onNext(C context, WorkflowCallback callback) {
        callback.proceed();
    }
}
