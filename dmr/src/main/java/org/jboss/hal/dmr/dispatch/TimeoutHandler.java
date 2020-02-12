/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.dmr.dispatch;

import java.util.function.Predicate;

import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;
import rx.Observable;
import rx.Single;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.joining;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/** Executes a DMR operation until a specific condition is met or a timeout occurs. */
public class TimeoutHandler {

    private static int INTERVAL = 500;
    private static Logger logger = LoggerFactory.getLogger(TimeoutHandler.class);

    /** Executes the operation until it successfully returns. */
    public static Completable repeatUntilTimeout(Dispatcher dispatcher, int timeout, Operation operation) {
        return operation instanceof Composite
                ? TimeoutHandler.repeatCompositeUntil(dispatcher, timeout, (Composite) operation, null)
                : TimeoutHandler.repeatOperationUntil(dispatcher, timeout, operation, null);
    }

    /**
     * Executes the operation until the operation successfully returns and the precondition is met. The precondition
     * receives the result of the operation.
     */
    @SuppressWarnings("HardCodedStringLiteral")
    public static Completable repeatOperationUntil(Dispatcher dispatcher, int timeout, Operation operation,
            Predicate<ModelNode> until) {
        logger.debug("Repeat {} using {} seconds timeout", operation.asCli(), timeout);

        Single<ModelNode> execution = Single.fromEmitter(em -> dispatcher.execute(operation, em::onSuccess,
                (op, fail) -> em.onSuccess(operationFailure("Dispatcher failure: " + fail)),
                (op, ex) -> em.onSuccess(operationFailure("Dispatcher exception: " + ex.getMessage()))));
        if (until == null) {
            until = r -> !r.isFailure(); // default: until success
        }

        return Observable
                .interval(INTERVAL, MILLISECONDS) // execute a operation each INTERVAL millis
                .doOnEach(n -> logger.debug("#{}: execute {}", n.getValue(), operation.asCli()))
                .flatMapSingle(n -> execution, false, 1)
                .takeUntil(until::test) // until succeeded
                .toCompletable().timeout(timeout, SECONDS); // wait succeeded or stop after timeout seconds
    }

    /**
     * Executes the composite operation until the operation successfully returns and the precondition is met. The
     * precondition receives the composite result of the operation.
     */
    @SuppressWarnings("HardCodedStringLiteral")
    public static Completable repeatCompositeUntil(Dispatcher dispatcher, int timeout, Composite composite,
            Predicate<CompositeResult> until) {
        logger.debug("Repeat {} using {} seconds as timeout", composite, timeout);

        Single<CompositeResult> execution = Single.fromEmitter(em -> dispatcher.execute(composite, em::onSuccess,
                (op, fail) -> em.onSuccess(compositeFailure("Dispatcher failure: " + fail)),
                (op, ex) -> em.onSuccess(compositeFailure("Dispatcher exception: " + ex.getMessage()))));
        if (until == null) {
            until = r -> r.stream().noneMatch(ModelNode::isFailure); // default: until success
        }

        return Observable
                .interval(INTERVAL, MILLISECONDS) // execute a operation each INTERVAL millis
                .doOnEach(n -> logger.debug("#{}: execute {}", n.getValue(), composite))
                .flatMapSingle(n -> execution, false, 1)
                .takeUntil(until::test) // until succeeded
                .toCompletable().timeout(timeout, SECONDS); // wait succeeded or stop after timeout seconds
    }

    private static ModelNode operationFailure(String reason) {
        ModelNode node = new ModelNode();
        node.get(OUTCOME).set(FAILED);
        node.get(FAILURE_DESCRIPTION).set(reason);
        return node;
    }

    private static CompositeResult compositeFailure(String reason) {
        ModelNode step1 = new ModelNode();
        step1.get(OUTCOME).set(FAILED);
        step1.get(FAILURE_DESCRIPTION).set(reason);
        ModelNode steps = new ModelNode();
        steps.get("step-1").set(step1);
        return new CompositeResult(steps);
    }

    private TimeoutHandler() {
    }
}
