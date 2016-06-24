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

import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Outcome;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.StreamSupport.stream;
import static org.jboss.hal.dmr.dispatch.Dispatcher.NOOP_EXCEPTIONAL_CALLBACK;
import static org.jboss.hal.dmr.dispatch.Dispatcher.NOOP_FAILED_CALLBACK;

/**
 * Executes a DMR operation until a specific condition is met or a timeout occurs.
 *
 * @author Harald Pehl
 */
public class TimeoutHandler {

    public interface Callback {

        /**
         * Operation was successful within the specified timeout
         */
        void onSuccess();

        /**
         * Operation ran into a timeout
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


    private static final int PERIOD = 500;
    private static final Logger logger = LoggerFactory.getLogger(TimeoutHandler.class);

    private final Dispatcher dispatcher;
    private final int timeout; // in seconds

    public TimeoutHandler(final Dispatcher dispatcher, final int timeout) {
        this.dispatcher = dispatcher;
        this.timeout = timeout;
    }

    /**
     * Executes the operation until it successfully returns.
     */
    public void execute(final Operation operation, final Callback callback) {
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
    public void execute(final Operation operation, final Predicate<ModelNode> predicate, final Callback callback) {
        new Async<TimeoutContext>().whilst(new TimeoutContext(),
                (context) -> !timeout(context) && !context.conditionSatisfied,
                new Outcome<TimeoutContext>() {
                    @Override
                    public void onFailure(final TimeoutContext context) {
                        logger.error("Operation {} ran into an error: {}", operation.asCli()); //NON-NLS
                        callback.onTimeout();
                    }

                    @Override
                    public void onSuccess(final TimeoutContext context) {
                        if (timeout(context)) {
                            //noinspection HardCodedStringLiteral
                            logger.warn("Operation {} ran into a timeout after {} seconds", operation.asCli(), timeout);
                            callback.onTimeout();
                        } else {
                            callback.onSuccess();
                        }
                    }
                },
                control -> dispatcher.execute(operation,
                        result -> control.getContext().conditionSatisfied = predicate == null || predicate.test(result),
                        NOOP_FAILED_CALLBACK,
                        NOOP_EXCEPTIONAL_CALLBACK),
                PERIOD);
    }

    /**
     * Executes the composite operation until the operation successfully returns and the precondition is met.
     * The precondition receives the composite result of the operation.
     */
    public void execute(final Composite composite, final Predicate<CompositeResult> predicate,
            final Callback callback) {
        new Async<TimeoutContext>().whilst(new TimeoutContext(),
                (context) -> !timeout(context) && !context.conditionSatisfied,
                new Outcome<TimeoutContext>() {
                    @Override
                    public void onFailure(final TimeoutContext context) {
                        logger.error("Composite operation {} ran into an error", composite.asCli()); //NON-NLS
                        callback.onTimeout();
                    }

                    @Override
                    public void onSuccess(final TimeoutContext context) {
                        if (timeout(context)) {
                            //noinspection HardCodedStringLiteral
                            logger.warn("Composite operation {} ran into a timeout after {} seconds", composite.asCli(),
                                    timeout);
                            callback.onTimeout();
                        } else {
                            callback.onSuccess();
                        }
                    }
                },
                control -> dispatcher.execute(composite,
                        (CompositeResult result) -> {
                            if (predicate != null) {
                                control.getContext().conditionSatisfied = predicate.test(result);
                            } else {
                                control.getContext().conditionSatisfied = stream(result.spliterator(), false)
                                        .map(stepResult -> !stepResult.isFailure())
                                        .allMatch(flag -> true);
                            }
                        }, NOOP_FAILED_CALLBACK, NOOP_EXCEPTIONAL_CALLBACK),
                PERIOD);
    }

    private boolean timeout(TimeoutContext timeoutContext) {
        long elapsed = (System.currentTimeMillis() - timeoutContext.start) / 1000;
        logger.debug("Checking elapsed > timeout ({} > {})", elapsed, timeout); //NON-NLS
        return elapsed > timeout;
    }
}
