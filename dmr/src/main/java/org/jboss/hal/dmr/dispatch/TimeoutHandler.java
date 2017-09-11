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

import java.util.function.Predicate;

import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.flow.Outcome;
import org.jboss.hal.flow.Progress;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.hal.flow.Flow.interval;

/** Executes a DMR operation until a specific condition is met or a timeout occurs. */
public class TimeoutHandler {

    public interface Callback {

        /**
         * Operation was successful within the specified timeout.
         */
        void onSuccess();

        /**
         * Operation ran into a timeout.
         */
        void onTimeout();
    }


    private static class TimeoutContext {

        final long start;
        boolean conditionSatisfied;

        TimeoutContext() {
            this.start = System.currentTimeMillis();
            this.conditionSatisfied = false;
            logger.debug("Start timeout handler @ {}", start); //NON-NLS
        }
    }


    private static final int INTERVAL = 500;
    @NonNls private static final Logger logger = LoggerFactory.getLogger(TimeoutHandler.class);

    private final Dispatcher dispatcher;
    private final int timeout; // in seconds

    public TimeoutHandler(Dispatcher dispatcher, int timeout) {
        this.dispatcher = dispatcher;
        this.timeout = timeout;
    }

    /**
     * Executes the operation until it successfully returns.
     */
    public void execute(Operation operation, Callback callback) {
        if (operation instanceof Composite) {
            execute((Composite) operation, (Predicate<CompositeResult>) null, callback);
        } else {
            execute(operation, null, callback);
        }
    }

    /**
     * Executes the operation until the operation successfully returns and the precondition is met. The precondition
     * receives the result of the operation.
     */
    public void execute(Operation operation, Predicate<ModelNode> predicate, Callback callback) {
        interval(Progress.NOOP, new TimeoutContext(), INTERVAL,
                context -> timeout(context) || context.conditionSatisfied,
                (context, control) -> dispatcher.execute(operation,
                        result -> {
                            context.conditionSatisfied = predicate == null || predicate.test(result);
                            control.proceed();
                        },
                        (op, failure) -> control.proceed(),
                        (op, exception) -> control.proceed()))
                .subscribe(new Outcome<TimeoutContext>() {
                    @Override
                    public void onError(TimeoutContext context, Throwable error) {
                        logger.error("Operation {} ran into an error: {}", operation.asCli());
                        callback.onTimeout();
                    }

                    @Override
                    public void onSuccess(TimeoutContext context) {
                        if (timeout(context)) {
                            logger.warn("Operation {} ran into a timeout after {} seconds", operation.asCli(), timeout);
                            callback.onTimeout();
                        } else {
                            callback.onSuccess();
                        }
                    }
                });
    }

    /**
     * Executes the composite operation until the operation successfully returns and the precondition is met.
     * The precondition receives the composite result of the operation.
     */
    public void execute(Composite composite, Predicate<CompositeResult> predicate, Callback callback) {
        interval(Progress.NOOP, new TimeoutContext(), INTERVAL,
                context -> timeout(context) || context.conditionSatisfied,
                (context, control) -> dispatcher.execute(composite,
                        (CompositeResult result) -> {
                            if (predicate != null) {
                                context.conditionSatisfied = predicate.test(result);
                            } else {
                                context.conditionSatisfied = result.stream()
                                        .map(stepResult -> !stepResult.isFailure())
                                        .allMatch(flag -> true);
                            }
                            control.proceed();
                        },
                        (op, failure) -> control.proceed(),
                        (op, exception) -> control.proceed()))
                .subscribe(new Outcome<TimeoutContext>() {
                    @Override
                    public void onError(TimeoutContext context, Throwable error) {
                        logger.error("Composite operation {} ran into an error", composite.asCli());
                        callback.onTimeout();
                    }

                    @Override
                    public void onSuccess(TimeoutContext context) {
                        if (timeout(context)) {
                            logger.warn("Composite operation {} ran into a timeout after {} seconds", composite.asCli(),
                                    timeout);
                            callback.onTimeout();
                        } else {
                            callback.onSuccess();
                        }
                    }
                });
    }

    private boolean timeout(TimeoutContext timeoutContext) {
        long elapsed = (System.currentTimeMillis() - timeoutContext.start) / 1000;
        logger.debug("Checking elapsed > timeout ({} > {})", elapsed, timeout);
        return elapsed > timeout;
    }
}
