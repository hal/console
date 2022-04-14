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
package org.jboss.hal.dmr.dispatch;

import java.util.function.Predicate;

import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.flow.Flow;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import elemental2.promise.Promise;

/** Executes a DMR operation until a specific condition is met or a timeout occurs. */
public class TimeoutHandler {

    private static final String PREDICATE = "org.jboss.hal.dmr.dispatch.TimeoutHandler.predicate";
    private static final Logger logger = LoggerFactory.getLogger(TimeoutHandler.class);

    /** Executes the operation until it successfully returns. */
    public static Promise<Void> repeatUntilTimeout(final Dispatcher dispatcher, final Operation operation,
            final int timeout) {
        // repeat until there are failures
        return repeatOperationUntil(dispatcher, operation, modelNode -> !modelNode.isFailure(), timeout);
    }

    /** Executes the operation until the predicate no longer is true. */
    public static Promise<Void> repeatOperationUntil(final Dispatcher dispatcher, final Operation operation,
            final Predicate<ModelNode> until, final int timeout) {
        logger.debug("Repeat {} while the predicate evaluates to true with {} seconds timeout", operation.asCli(),
                timeout);
        // repeat until the predicate returns true
        return Flow.while_(new FlowContext(Progress.NOOP),
                context -> dispatcher.execute(operation)
                        .then(node -> Promise.resolve(context.set(PREDICATE, !until.test(node)))), // until = !while
                context -> context.get(PREDICATE, true))
                .failFast(false)
                .timeout(timeout * 1000L)
                .then(__ -> Promise.resolve((Void) null));
    }

    /** Executes the composite operation until the operation successfully returns. */
    public static Promise<Void> repeatCompositeUntil(final Dispatcher dispatcher, final Composite composite,
            final int timeout) {
        // repeat until there are failures
        return repeatCompositeUntil(dispatcher, composite, modelNodes -> !modelNodes.isFailure(), timeout);
    }

    /** Executes the composite operation until the predicate no longer is true. */
    public static Promise<Void> repeatCompositeUntil(final Dispatcher dispatcher, final Composite composite,
            final Predicate<CompositeResult> until, final int timeout) {
        logger.debug("Repeat {} while the predicate evaluates to true with {} seconds timeout", composite.asCli(),
                timeout);
        // repeat until the predicate returns true
        return Flow.while_(new FlowContext(Progress.NOOP),
                context -> dispatcher.execute(composite)
                        .then(cr -> Promise.resolve(context.set(PREDICATE, !until.test(cr)))), // until = !while
                context -> context.get(PREDICATE, true))
                .failFast(false)
                .timeout(timeout * 1000L)
                .then(__ -> Promise.resolve((Void) null));
    }

    private TimeoutHandler() {
    }
}
