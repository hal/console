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
package org.jboss.hal.dmr.dispatch;

import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;
import rx.Observable;
import rx.Single;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/** Executes a DMR operation until a specific condition is met or a timeout occurs. */
public interface TimeoutHandler {

    int INTERVAL = 500;
    Logger logger = LoggerFactory.getLogger(TimeoutHandler.class);

    /** Executes the operation until it successfully returns. */
    static Completable repeatUntilTimeout(Dispatcher dispatcher, int timeout, Operation operation) {
        return operation instanceof Composite
                ? TimeoutHandler.repeatCompositeUntil(dispatcher, timeout, (Composite) operation, null)
                : TimeoutHandler.repeatOperationUntil(dispatcher, timeout, operation, null);
    }

    /**
     * Executes the operation until the operation successfully returns and the precondition is met. The precondition
     * receives the result of the operation.
     */
    @SuppressWarnings("HardCodedStringLiteral")
    static Completable repeatOperationUntil(Dispatcher dispatcher, int timeout, Operation operation,
            @Nullable Predicate<ModelNode> until) {
        Single<ModelNode> execution = Single.fromEmitter(em -> dispatcher.execute(operation, em::onSuccess,
                (op, fail) -> em.onError(new RuntimeException("Dispatcher failure: " + fail)),
                (op, ex) -> em.onError(new RuntimeException("Dispatcher exception: " + ex, ex))));
        if (until == null) {
            until = r -> !r.isFailure(); // default: until success
        }

        return Observable
                .interval(INTERVAL, MILLISECONDS) // execute a operation each INTERVAL millis
                .flatMapSingle(n -> execution, false, 1)
                .takeUntil(until::test) // until succeeded
                .toCompletable().timeout(timeout, SECONDS) // wait succeeded or stop after timeout seconds
                .doOnError(e -> {
                    String msg = "Operation " + operation.asCli() + " ran into ";
                    if (e instanceof TimeoutException) {
                        logger.warn(msg + "a timeout after " + timeout + " seconds");
                    } else {
                        logger.error(msg + "an error", e);
                    }
                });
    }

    /**
     * Executes the composite operation until the operation successfully returns and the precondition is met.
     * The precondition receives the composite result of the operation.
     */
    @SuppressWarnings("HardCodedStringLiteral")
    static Completable repeatCompositeUntil(Dispatcher dispatcher, int timeout, Composite composite,
            @Nullable Predicate<CompositeResult> until) {
        Single<CompositeResult> execution = Single.fromEmitter(em -> dispatcher.execute(composite, em::onSuccess,
                (op, fail) -> em.onError(new RuntimeException("Dispatcher failure: " + fail)),
                (op, ex) -> em.onError(new RuntimeException("Dispatcher exception: " + ex, ex))));
        if (until == null) {
            until = r -> r.stream().noneMatch(ModelNode::isFailure); // default: until success
        }

        return Observable
                .interval(INTERVAL, MILLISECONDS) // execute a operation each INTERVAL millis
                .flatMapSingle(n -> execution, false, 1)
                .takeUntil(until::test) // until succeeded
                .toCompletable().timeout(timeout, SECONDS) // wait succeeded or stop after timeout seconds
                .doOnError(e -> {
                    String msg = "Composite operation " + composite.asCli() + " ran into ";
                    if (e instanceof TimeoutException) {
                        logger.warn(msg + "a timeout after " + timeout + " seconds");
                    } else {
                        logger.error(msg + "an error", e);
                    }
                });
    }
}
