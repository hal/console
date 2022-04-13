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

import java.util.Iterator;
import java.util.List;

import elemental2.promise.Promise;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.RejectCallbackFn;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.ResolveCallbackFn;

class FlowExecutor<C extends FlowContext> {

    enum Mode {
        PARALLEL, SEQUENTIAL
    }

    private final Mode mode;
    private final C context;
    private final List<Task<C>> tasks;
    private final Iterator<Task<C>> iterator;
    private final boolean failFast;

    FlowExecutor(final Mode mode, final C context, final List<Task<C>> tasks, final boolean failFast) {
        this.mode = mode;
        this.context = context;
        this.context.progress.reset(tasks.size());
        this.tasks = tasks;
        this.iterator = tasks.iterator();
        this.failFast = failFast;
    }

    Promise<C> execute() {
        if (tasks.isEmpty()) {
            return Promise.resolve(context);
        } else {
            switch (mode) {
                case PARALLEL:
                    return parallel();
                case SEQUENTIAL:
                    return sequential();
                default:
                    throw new IllegalStateException("Unexpected flow execution mode: " + mode);
            }
        }
    }

    // ------------------------------------------------------ parallel

    @SuppressWarnings("unchecked")
    Promise<C> parallel() {
        Promise<C>[] promises = tasks.stream()
                .map(task -> task.apply(context).then(c -> {
                    context.progress.tick();
                    return Promise.resolve(c);
                }))
                .toArray(Promise[]::new);
        if (failFast) {
            return FlowPromise.all(promises)
                    .then(all -> {
                        context.progress.finish();
                        return Promise.resolve(context);
                    });
        } else {
            return FlowPromise.allSettled(promises)
                    .then(all -> {
                        context.progress.finish();
                        return Promise.resolve(context);
                    });
        }
    }

    // ------------------------------------------------------ sequential

    Promise<C> sequential() {
        return new Promise<>(this::next)
                .then(c -> {
                    context.progress.finish();
                    return Promise.resolve(context);
                })
                .catch_(Promise::reject);
    }

    private void next(ResolveCallbackFn<C> resolve, RejectCallbackFn reject) {
        iterator.next().apply(context)
                .then(c -> {
                    if (iterator.hasNext()) {
                        context.progress.tick();
                        next(resolve, reject);
                    } else {
                        resolve.onInvoke(c);
                    }
                    return null;
                })
                .catch_(error -> {
                    if (failFast) {
                        reject.onInvoke(error);
                    } else {
                        if (iterator.hasNext()) {
                            context.progress.tick();
                            next(resolve, reject);
                        } else {
                            resolve.onInvoke(context);
                        }
                    }
                    return null;
                });

    }
}
