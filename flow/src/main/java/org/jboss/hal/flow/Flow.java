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

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import rx.Observable;
import rx.Single;

/**
 * Collection of static methods to execute async operations in order or until a condition is met. Uses RxGWT to
 * orchestrate the async operations.
 */
public interface Flow {

    /** Executes a single step. Useful if you already have a step implementation which you want to re-use. */
    static <C> Single<C> single(Progress progress, C context, Step<C> step) {
        return step.call(context)
                .doOnSubscribe(progress::reset)
                .doOnSuccess(n -> progress.finish())
                .doOnError(e -> progress.finish());
    }

    /** Executes multiple steps in order. */
    @SafeVarargs
    static <C> Single<C> series(Progress progress, C context, Step<C>... step) {
        return series(progress, context, asList(step));
    }

    /** Executes multiple steps in order. */
    static <C> Single<C> series(Progress progress, C context, Collection<? extends Step<C>> steps) {
        assert !steps.isEmpty();
        return Observable.from(steps)
                .flatMapSingle(f -> f.call(context), false, 1)
                .doOnSubscribe(() -> progress.reset(steps.size()))
                .doOnNext(n -> progress.tick())
                .doOnTerminate(progress::finish)
                .lastOrDefault(context).toSingle();
    }

    /** Executes a steps until a condition is met. */
    static <C> Single<C> interval(Progress progress, C context, int interval, Predicate<C> until, Step<C> step) {
        return Observable.interval(interval, TimeUnit.MILLISECONDS)
                .flatMapSingle(n -> step.call(context))
                .takeUntil(until::test)
                .doOnSubscribe(progress::reset)
                .doOnNext(n -> progress.tick())
                .doOnTerminate(progress::finish)
                .lastOrDefault(context).toSingle();
    }
}
