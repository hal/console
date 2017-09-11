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
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import rx.Observable;
import rx.Single;

import static java.util.Arrays.asList;

/** Flow control based on RxGWT */
public class Flow {

    public static <C> Single<C> single(Progress progress, C context, Step<C> step) {
        return fromControl(context, step)
                .doOnSubscribe(progress::reset)
                .doOnSuccess(n -> progress.finish())
                .doOnError(e -> progress.finish());
    }

    @SafeVarargs
    public static <C> Single<C> series(Progress progress, C context, Step<C>... step) {
        return series(progress, context, asList(step));
    }

    public static <C> Single<C> series(Progress progress, C context, Collection<? extends Step<C>> steps) {
        assert !steps.isEmpty();
        return Observable.from(steps)
                .doOnSubscribe(() -> progress.reset(steps.size()))
                .concatMap(f -> fromControl(context, f).toObservable())
                .doOnNext(n -> progress.tick())
                .doOnTerminate(progress::finish)
                .doOnError(e -> progress.finish())
                .last().toSingle();
    }

    public static <C> Single<C> interval(Progress progress, C context, int interval, Predicate<C> until,
            Step<C> step) {
        return Observable.interval(interval, TimeUnit.MILLISECONDS)
                .doOnSubscribe(progress::reset)
                .flatMapSingle(n -> fromControl(context, step))
                .doOnNext(n -> progress.tick())
                .doOnTerminate(progress::finish)
                .doOnError(e -> progress.finish())
                .takeUntil(until::test)
                .last().toSingle();
    }

    private static <C> Single<C> fromControl(C context, Step<C> producer) {
        return Single.fromEmitter(emitter -> producer.execute(new Control<C>() {
            @Override public void proceed() { emitter.onSuccess(context); }
            @Override public void abort(String error) { emitter.onError(new FlowException(error, context)); }
            @Override public C getContext() { return context; }
        }));
    }
}