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
package org.jboss.hal.core;

import java.util.Map;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.OperationFactory;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
public class CrudOperations {

    @FunctionalInterface
    public interface Callback {

        void execute();
    }


    @FunctionalInterface
    public interface ReadCallback {

        void execute(ModelNode result);
    }


    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final OperationFactory operationFactory;

    @Inject
    public CrudOperations(final EventBus eventBus, final Dispatcher dispatcher, final StatementContext statementContext,
            final Resources resources) {
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resources = resources;
        this.operationFactory = new OperationFactory();
    }


    // ------------------------------------------------------ (c)reate

    public void add(String type, String name, AddressTemplate template, ModelNode payload, Callback callback) {
        Operation operation = new Operation.Builder(ADD, template.resolve(statementContext, name))
                .payload(payload)
                .build();
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(eventBus, Message.success(resources.messages().addResourceSuccess(type, name)));
            callback.execute();
        });
    }

    public void addSingleton(String type, AddressTemplate template, Callback callback) {
        addSingleton(type, template, null, callback);
    }

    public void addSingleton(String type, AddressTemplate template, ModelNode payload, Callback callback) {
        Operation.Builder builder = new Operation.Builder(ADD, template.resolve(statementContext));
        if (payload != null) {
            builder.payload(payload);
        }
        dispatcher.execute(builder.build(), result -> {
            MessageEvent.fire(eventBus, Message.success(resources.messages().addSingleResourceSuccess(type)));
            callback.execute();
        });
    }


    // ------------------------------------------------------ (r)ead

    public void read(AddressTemplate template, ReadCallback callback) {
        read(new Operation.Builder(READ_RESOURCE_OPERATION, template.resolve(statementContext))
                        .param(INCLUDE_ALIASES, true)
                        .build(),
                callback);
    }

    public void read(AddressTemplate template, int depth, ReadCallback callback) {
        read(new Operation.Builder(READ_RESOURCE_OPERATION, template.resolve(statementContext))
                        .param(INCLUDE_ALIASES, true)
                        .param(RECURSIVE_DEPTH, depth)
                        .build(),
                callback);
    }

    public void readRecursive(AddressTemplate template, ReadCallback callback) {
        read(new Operation.Builder(READ_RESOURCE_OPERATION, template.resolve(statementContext))
                        .param(INCLUDE_ALIASES, true)
                        .param(RECURSIVE, true)
                        .build(),
                callback);
    }

    private void read(Operation operation, ReadCallback callback) {
        dispatcher.execute(operation, callback::execute);
    }


    // ------------------------------------------------------ (u)pdate

    public void save(String type, String name, AddressTemplate template, Map<String, Object> changedValues,
            Callback callback) {
        Composite operation = operationFactory.fromChangeSet(template.resolve(statementContext, name), changedValues);
        dispatcher.execute(operation, (CompositeResult result) -> {
            MessageEvent.fire(eventBus, Message.success(resources.messages().modifyResourceSuccess(type, name)));
            callback.execute();
        });
    }

    public void saveSingleton(String type, AddressTemplate template, Map<String, Object> changedValues,
            Callback callback) {
        Composite operation = operationFactory.fromChangeSet(template.resolve(statementContext), changedValues);
        dispatcher.execute(operation, (CompositeResult result) -> {
            MessageEvent.fire(eventBus, Message.success(resources.messages().modifySingleResourceSuccess(type)));
            callback.execute();
        });
    }


    // ------------------------------------------------------ (d)elete

    /**
     * Shows a confirmation dialog and removes the resource if confirmed by the user.
     */
    public void remove(String type, String name, AddressTemplate template, Callback callback) {
        remove(type, name, template.resolve(statementContext, name), callback);
    }

    /**
     * Shows a confirmation dialog and removes the resource if verified by the user.
     */
    public void remove(String type, String name, ResourceAddress address, Callback callback) {
        DialogFactory.showConfirmation(
                resources.messages().removeResourceConfirmationTitle(type),
                resources.messages().removeResourceConfirmationQuestion(name),
                () -> {
                    Operation operation = new Operation.Builder(REMOVE, address).build();
                    dispatcher.execute(operation, result -> {
                        MessageEvent.fire(eventBus, Message.success(
                                resources.messages().removeResourceSuccess(type, name)));
                        callback.execute();
                    });
                });
    }
}
