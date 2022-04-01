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

import elemental2.promise.Promise;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.RejectCallbackFn;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.ResolveCallbackFn;

import static elemental2.dom.DomGlobal.clearInterval;
import static elemental2.dom.DomGlobal.clearTimeout;
import static elemental2.dom.DomGlobal.setInterval;
import static elemental2.dom.DomGlobal.setTimeout;

class FlowLoop<C extends FlowContext> {

    private static final String TIMEOUT_ERROR = "timeout";
    private static final int INTERVAL = 500;

    private final int timeout;
    private final C context;
    private final Task<C> task;
    private final Predicate<C> predicate;
    private final boolean failFast;
    private String lastFailure;
    private double timeoutHandle;
    private double intervalHandle;

    FlowLoop(final C context, final Task<C> task, final Predicate<C> predicate, final int timeout,
            final boolean failFast) {
        this.timeout = timeout;
        this.context = context;
        this.context.progress.reset();
        this.task = task;
        this.predicate = predicate;
        this.failFast = failFast;
        this.lastFailure = null;
        this.timeoutHandle = 0;
        this.intervalHandle = 0;
    }

    Promise<C> execute() {
        return new Promise<>((resolve, reject) -> {
            timeoutHandle = setTimeout(__ -> cancel(reject, TIMEOUT_ERROR), timeout);
            if (!predicate.test(context)) {
                finish(resolve, context);
            }
            until(resolve, reject);
        });
    }

    private void until(ResolveCallbackFn<C> resolve, RejectCallbackFn reject) {
        intervalHandle = setInterval(__ -> {
            if (failFast && lastFailure != null) {
                cancel(reject, lastFailure);
            } else {
                task.apply(context)
                        .then(c -> {
                            context.progress.tick();
                            if (!predicate.test(c)) {
                                finish(resolve, c);
                            }
                            return null;
                        })
                        .catch_(error -> {
                            lastFailure = String.valueOf(error);
                            if (failFast) {
                                cancel(reject, lastFailure);
                            }
                            return null;
                        });
            }
        }, INTERVAL);
    }

    private void finish(ResolveCallbackFn<C> resolve, C context) {
        cleanup();
        context.progress.finish();
        resolve.onInvoke(context);
    }

    private void cancel(RejectCallbackFn reject, String reason) {
        cleanup();
        reject.onInvoke(reason);
    }

    private void cleanup() {
        clearInterval(intervalHandle);
        clearTimeout(timeoutHandle);
    }
}
