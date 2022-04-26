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
package org.jboss.hal.core.runtime;

import java.util.function.Predicate;

import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.Flow;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.FlowStatus;
import org.jboss.hal.flow.Progress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import elemental2.promise.Promise;

import static org.jboss.hal.flow.FlowStatus.SUCCESS;

/** Executes a DMR operation until a specific condition is met or a timeout occurs. */
public class TimeoutHandler {

    private static final Logger logger = LoggerFactory.getLogger(TimeoutHandler.class);

    /** Executes the operation until it successfully returns. */
    public static Promise<FlowStatus> repeatUntilTimeout(final Dispatcher dispatcher, final Operation operation,
            final int timeout) {
        // repeat until there are failures
        return repeatOperationUntil(dispatcher, operation, modelNode -> !modelNode.isFailure(), timeout);
    }

    /** Executes the operation until the predicate no longer is true. */
    public static Promise<FlowStatus> repeatOperationUntil(final Dispatcher dispatcher, final Operation operation,
            final Predicate<ModelNode> until, final int timeout) {
        logger.debug("Repeat {} while the predicate evaluates to true with {} seconds timeout", operation.asCli(),
                timeout);
        // repeat until the predicate returns true
        return Flow.repeat(new FlowContext(),
                c -> dispatcher.execute(operation).then(result -> c.resolve(!until.test(result)))) // until = !while
                .while_(c -> c.pop(true))
                .failFast(false)
                .timeout(timeout * 1000L)
                .then(__ -> Promise.resolve(SUCCESS), error -> Promise.resolve(FlowStatus.fromError(error)));
    }

    /** Executes the composite operation until the operation successfully returns. */
    public static Promise<FlowStatus> repeatCompositeUntil(final Dispatcher dispatcher, final Composite composite,
            final int timeout) {
        // repeat until there are failures
        return repeatCompositeUntil(dispatcher, composite, modelNodes -> !modelNodes.isFailure(), timeout);
    }

    /** Executes the composite operation until the predicate no longer is true. */
    public static Promise<FlowStatus> repeatCompositeUntil(final Dispatcher dispatcher, final Composite composite,
            final Predicate<CompositeResult> until, final int timeout) {
        logger.debug("Repeat {} while the predicate evaluates to true with {} seconds timeout", composite.asCli(),
                timeout);
        // repeat until the predicate returns true
        return Flow.repeat(new FlowContext(Progress.NOOP),
                c -> dispatcher.execute(composite).then(cr -> c.resolve(!until.test(cr)))) // until = !while
                .while_(c -> c.pop(true))
                .failFast(false)
                .timeout(timeout * 1000L)
                .then(__ -> Promise.resolve(SUCCESS), error -> Promise.resolve(FlowStatus.fromError(error)));
    }

    private TimeoutHandler() {
    }
}
