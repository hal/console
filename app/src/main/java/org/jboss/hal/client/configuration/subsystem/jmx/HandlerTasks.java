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
package org.jboss.hal.client.configuration.subsystem.jmx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import org.jboss.hal.core.OperationFactory;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import io.reactivex.Completable;

import static org.jboss.hal.client.configuration.subsystem.jmx.AddressTemplates.AUDIT_LOG_HANDLER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jmx.AddressTemplates.AUDIT_LOG_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

class HandlerTasks {

    static class SaveAuditLog implements Task<FlowContext> {

        private final Dispatcher dispatcher;
        private final StatementContext statementContext;
        private final Map<String, Object> changedValues;
        private final Metadata metadata;

        SaveAuditLog(Dispatcher dispatcher, StatementContext statementContext, Map<String, Object> changedValues,
                Metadata metadata) {
            this.dispatcher = dispatcher;
            this.statementContext = statementContext;
            this.changedValues = changedValues;
            this.metadata = metadata;
        }

        @Override
        public Completable apply(FlowContext context) {
            OperationFactory operationFactory = new OperationFactory();
            Composite operation = operationFactory
                    .fromChangeSet(AUDIT_LOG_TEMPLATE.resolve(statementContext), changedValues, metadata);
            return operation.isEmpty()
                    ? Completable.complete()
                    : dispatcher.execute(operation).toCompletable();
        }
    }


    static class ReadHandlers implements Task<FlowContext> {

        private final Dispatcher dispatcher;
        private final StatementContext statementContext;

        ReadHandlers(Dispatcher dispatcher, StatementContext statementContext) {
            this.dispatcher = dispatcher;
            this.statementContext = statementContext;
        }

        @Override
        public Completable apply(FlowContext context) {
            Operation operation = new Operation.Builder(AUDIT_LOG_TEMPLATE.resolve(statementContext),
                    READ_CHILDREN_NAMES_OPERATION)
                    .param(CHILD_TYPE, HANDLER)
                    .build();
            return dispatcher.execute(operation)
                    .doOnSuccess(result -> context.push(result.asList().stream()
                            .map(ModelNode::asString)
                            .collect(Collectors.toSet())))
                    .doOnError(failure -> context.push(Collections.emptySet()))
                    .toCompletable();
        }
    }


    static class MergeHandler implements Task<FlowContext> {

        private final Dispatcher dispatcher;
        private final StatementContext statementContext;
        private final Set<String> newHandlers;

        MergeHandler(Dispatcher dispatcher, StatementContext statementContext, Set<String> newHandlers) {
            this.dispatcher = dispatcher;
            this.statementContext = statementContext;
            this.newHandlers = newHandlers;
        }

        @Override
        public Completable apply(FlowContext context) {
            Set<String> existingHandlers = context.pop();
            Set<String> add = Sets.difference(newHandlers, existingHandlers).immutableCopy();
            Set<String> remove = Sets.difference(existingHandlers, newHandlers).immutableCopy();

            List<Operation> operations = new ArrayList<>();
            add.stream()
                    .map(handler -> new Operation.Builder(AUDIT_LOG_HANDLER_TEMPLATE.resolve(statementContext, handler),
                            ADD
                    ).build())
                    .forEach(operations::add);
            remove.stream()
                    .map(handler -> new Operation.Builder(AUDIT_LOG_HANDLER_TEMPLATE.resolve(statementContext, handler),
                            REMOVE
                    ).build())
                    .forEach(operations::add);
            Composite composite = new Composite(operations);
            return composite.isEmpty()
                    ? Completable.complete()
                    : dispatcher.execute(new Composite(operations)).toCompletable();
        }
    }
}
