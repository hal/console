/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.flow;

import java.util.Collection;

import io.reactivex.Observable;
import io.reactivex.Single;

import static java.util.Arrays.asList;

/** Collection of static methods to execute (async) tasks in order. Uses RxGWT for orchestration. */
public interface Flow {

    /** Executes multiple tasks in order. */
    @SafeVarargs
    static <C extends FlowContext> Single<C> series(C context, Task<C>... task) {
        return series(context, asList(task));
    }

    /** Executes multiple tasks in order. */
    static <C extends FlowContext> Single<C> series(C context, Collection<? extends Task<C>> tasks) {
        return Observable.fromIterable(tasks)
                .concatMap(task -> task.apply(context).toSingleDefault(context).toObservable())
                .doOnSubscribe(s -> context.progress.reset(tasks.size()))
                .doOnNext(c -> c.progress.tick())
                .doOnTerminate(context.progress::finish)
                .last(context);
    }
}
