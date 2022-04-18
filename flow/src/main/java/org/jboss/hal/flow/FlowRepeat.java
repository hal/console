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

import elemental2.promise.IThenable;
import elemental2.promise.IThenable.ThenOnFulfilledCallbackFn;
import elemental2.promise.Promise;
import elemental2.promise.Promise.CatchOnRejectedCallbackFn;
import elemental2.promise.Promise.FinallyOnFinallyCallbackFn;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.RejectCallbackFn;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.ResolveCallbackFn;

import static elemental2.dom.DomGlobal.clearInterval;
import static elemental2.dom.DomGlobal.clearTimeout;
import static elemental2.dom.DomGlobal.setInterval;
import static elemental2.dom.DomGlobal.setTimeout;

class FlowRepeat<C extends FlowContext> implements Repeat<C> {

    private final C context;
    private final Task<C> task;
    private Predicate<C> predicate;
    private boolean failFast;
    private long interval;
    private long timeout;
    private int iterations;
    private int index;
    private String lastFailure;
    private double timeoutHandle;
    private double intervalHandle;

    FlowRepeat(final C context, final Task<C> task) {
        this.context = context;
        this.context.progress.reset();
        this.task = task;
        this.predicate = __ -> true;
        this.failFast = DEFAULT_FAIL_FAST;
        this.interval = DEFAULT_INTERVAL;
        this.timeout = DEFAULT_TIMEOUT;
        this.iterations = DEFAULT_ITERATIONS;
        this.index = 0;
        this.lastFailure = null;
        this.timeoutHandle = 0;
        this.intervalHandle = 0;
    }

    @Override
    public Repeat<C> while_(final Predicate<C> predicate) {
        this.predicate = predicate;
        return this;
    }

    @Override
    public Repeat<C> failFast(final boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    @Override
    public Repeat<C> interval(final long interval) {
        this.interval = interval;
        return this;
    }

    @Override
    public Repeat<C> timeout(final long timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public Repeat<C> iterations(final int iterations) {
        this.iterations = iterations;
        return this;
    }

    @Override
    public <V> Promise<V> then(final ThenOnFulfilledCallbackFn<? super C, ? extends V> onFulfilled) {
        return run().then(onFulfilled);
    }

    @Override
    public <V> Promise<V> then(final ThenOnFulfilledCallbackFn<? super C, ? extends V> onFulfilled,
            final IThenable.ThenOnRejectedCallbackFn<? extends V> onRejected) {
        return run().then(onFulfilled, onRejected);
    }

    @Override
    public <V> Promise<V> catch_(final CatchOnRejectedCallbackFn<? extends V> onRejected) {
        return run().catch_(onRejected);
    }

    @Override
    public Promise<C> finally_(final FinallyOnFinallyCallbackFn onFinally) {
        return run().finally_(onFinally);
    }

    @Override
    public Promise<C> promise() {
        return run();
    }

    @Override
    public void subscribe(final SuccessCallback<C> onSuccess, final FailureCallback<C> onFailure) {
        run().then(c -> {
            onSuccess.success(c);
            return null;
        })
                .catch_(error -> {
                    onFailure.failed(context, String.valueOf(error));
                    return null;
                });
    }

    private Promise<C> run() {
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
                            index++;
                            context.progress.tick();
                            if (areWeDone(context)) {
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
        }, interval);
    }

    private boolean areWeDone(C context) {
        if (iterations > 0) {
            return index == iterations || !predicate.test(context);
        } else {
            return !predicate.test(context);
        }
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
