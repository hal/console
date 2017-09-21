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

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import rx.Completable;

@SuppressWarnings("HardCodedStringLiteral")
public class RxTest {

    @Test
    public void rx() {
        Completable task = task();
        // not mandatory, but it's more natural to do all lazy in rx
        // but at this point the log shows "eager"
        System.out.println(">check point, eager up there ↑");
        System.out.println(">subscribing now to the task…");
        task.subscribe();
        System.out.println(">and again…");
        task.subscribe();
        System.out.println(">not mandatory, but it's not difficult so most of the time RX instances are reusable");
        System.out.println(">BUT, your FlowContext will make it impossibly to reuse, so it's 'fine' to not to reuse");

        System.out.println();
        System.out.println(">My recommendation, make it explicit, for example limiting the number of subscription");
        Completable safeTask = task.compose(new Completable.Transformer() {
            AtomicBoolean consumed = new AtomicBoolean(false);

            @Override
            public Completable call(Completable o) {
                return Completable.complete().doOnSubscribe(s -> {
                    if (consumed.getAndSet(true)) { throw new IllegalStateException("already consumed!"); }
                }).andThen(o); // need to move the o here so the subscription error stops before o evaluation
            }
        });
        System.out.println(">calling for first time our safe task…");
        safeTask.subscribe();
        System.out.println(">calling for second time our safe task…");
        safeTask.subscribe(() -> {
        }, e -> System.out.println("error, it limit subscription to only 1!"));

        System.out.println();
        System.out.println(">Another alternative, which is a bit more natural for the old imperative code");
        System.out.println(
                ">is to actually subscribe to the RX type immediately, this is how Promise or CompletableFuture works");
        System.out.println(">Now, I'm going to define the new task, but you will see how it get executed immediately");
        Completable eagerTask = task.compose(o -> o.toObservable().publish().autoConnect(-1).toCompletable());
        System.out.println(">see ↑, already connected, eagerTask is now a promise");
        System.out.println(">if we subscribe it ends up but it doesn't evaluate the task again");
        eagerTask.subscribe(() -> System.out.println("done"));
        System.out.println(">but it completes only when the original task has completed too");

        System.out.println();
        System.out.println(">Finally, I have used RX.compose(…) on purpose, if you are going to use it, extract the");
        System.out.println(">content of the compose into a method and this is it, you have a new reusable operator");
    }

    private Completable task() {
        System.out.println("eager (on method call)");
        return Completable.fromEmitter(em -> System.out.println("lazy (on each subscription)"));
    }
}