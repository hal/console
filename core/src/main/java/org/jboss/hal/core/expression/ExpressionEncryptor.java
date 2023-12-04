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
package org.jboss.hal.core.expression;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.jboss.hal.ballroom.form.AbstractFormItem;
import org.jboss.hal.ballroom.form.EncryptExpressionEvent;
import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import com.google.web.bindery.event.shared.EventBus;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CREATE_EXPRESSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT_RESOLVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ENCRYPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXPRESSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESOLVERS;



public class ExpressionEncryptor implements EncryptExpressionEvent.EncryptExpressionHandler {

    private static final ResourceAddress ELYTRON_ADDRESS = ResourceAddress.from("subsystem=elytron");
    private static final ResourceAddress EXPRESSION_ADDRESS = ResourceAddress.from("subsystem=elytron/expression=encryption");

    private final EventBus eventBus;
    private final Environment environment;
    private final Dispatcher dispatcher;
    private final Resources resources;

    private AbstractFormItem formItem;

    @Inject
    public ExpressionEncryptor(final EventBus eventBus,
            final Environment environment,
            final Dispatcher dispatcher,
            final Resources resources) {
        this.eventBus = eventBus;
        this.environment = environment;
        this.dispatcher = dispatcher;
        this.resources = resources;

        eventBus.addHandler(EncryptExpressionEvent.getType(), this);
    }

    @Override
    public void onEncryptExpression(final EncryptExpressionEvent event) {
        this.formItem = event.getFormItem();
        if (!environment.isStandalone()) {
            showEmptyDialog();
            return;
        }
        Operation operation = new Operation.Builder(ELYTRON_ADDRESS, READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, EXPRESSION)
                .build();
        dispatcher.execute(operation,
                (result) -> {
                    if (!result.hasDefined(ENCRYPTION)) {
                        showEmptyDialog();
                        return;
                    }

                    List<String> resolverNames = result.get(ENCRYPTION).get(RESOLVERS)
                            .asList().stream().map(r -> r.get(NAME).asString())
                            .collect(Collectors.toList());
                    boolean hasDefaultResolver = result.hasDefined(DEFAULT_RESOLVER);
                    showDialog(resolverNames, hasDefaultResolver);
                },
                (op1, error) -> showEmptyDialog());
    }

    private void showDialog(List<String> resolverNames, boolean hasDefaultResolver) {
        new EncryptionDialog(this, resources, resolverNames, hasDefaultResolver).show();
    }

    private void showEmptyDialog() {
        new EncryptionDialog(this, resources).show();
    }

    void saveEncryption(ModelNode payload) {

        Operation operation = new Operation.Builder(EXPRESSION_ADDRESS, CREATE_EXPRESSION)
                .payload(payload)
                .build();
        dispatcher.execute(operation,
                (result) -> {
                    formItem.setExpressionValue(result.get(EXPRESSION).asString());
                    formItem.setModified(true);
                },
                (op1, error) -> MessageEvent.fire(eventBus,
                        Message.error(resources.messages().expressionEncryptionError(error))));

    }
}
