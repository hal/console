/*
 * Copyright 2015-2018 Red Hat, Inc, and individual contributors.
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
package org.jboss.hal.client.runtime.subsystem.elytron.wizardpassword;

import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static org.jboss.hal.dmr.ModelDescriptionConstants.IDENTITY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SET_PASSWORD;

public class PasswordWizard {

    private final Resources resources;
    private final StatementContext statementContext;
    private Metadata metadata;
    private String selectedRealm;
    private String selectedIdentity;
    private final Dispatcher dispatcher;
    private EventBus eventBus;


    public PasswordWizard(Resources resources, StatementContext statementContext, Dispatcher dispatcher,
            EventBus eventBus, Metadata metadata, String selectedRealm,
            String selectedIdentity) {
        this.resources = resources;
        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.statementContext = statementContext;
        this.metadata = metadata;
        this.selectedRealm = selectedRealm;
        this.selectedIdentity = selectedIdentity;
    }

    public void show() {
        Constants constants = resources.constants();
        Wizard.Builder<PasswordContext, PasswordState> wb = new Wizard.Builder<>(constants.setIdentityPasswordTitle(),
                new PasswordContext());

        wb.addStep(PasswordState.CHOOSE_PASSWORD_TYPE, new ChoosePasswordTypeStep(resources))
                .addStep(PasswordState.CONFIGURATION, new ConfigurePasswordStep(resources, metadata))
                .addStep(PasswordState.REVIEW, new ReviewPasswordStep(resources, metadata))

                .onBack((context, currentState) -> {
                    PasswordState previous = null;
                    switch (currentState) {
                        case CHOOSE_PASSWORD_TYPE:
                            break;
                        case CONFIGURATION:
                            previous = PasswordState.CHOOSE_PASSWORD_TYPE;
                            break;
                        case REVIEW:
                            previous = PasswordState.CONFIGURATION;
                            break;
                        default:
                            break;
                    }
                    return previous;
                })
                .onNext((context, currentState) -> {
                    PasswordState next = null;
                    switch (currentState) {
                        case CHOOSE_PASSWORD_TYPE:
                            next = PasswordState.CONFIGURATION;
                            break;
                        case CONFIGURATION:
                            next = PasswordState.REVIEW;
                            break;
                        case REVIEW:
                            break;
                        default:
                            break;
                    }
                    return next;
                })
                .onFinish((wizard, context) -> {

                    ResourceAddress address = metadata.getTemplate().resolve(statementContext, selectedRealm);
                    Operation operation = new Operation.Builder(address, SET_PASSWORD)
                            .param(IDENTITY, selectedIdentity)
                            .param(context.type.name, context.model)
                            .build();
                    LabelBuilder labelBuilder = new LabelBuilder();
                    String type = labelBuilder.label(metadata.getTemplate().lastName());
                    String resourceName = type + "" + selectedRealm;
                    dispatcher.execute(operation, result -> MessageEvent.fire(eventBus, Message.success(
                            resources.messages().setIdentityPasswordSuccess(selectedIdentity, resourceName))),
                            (operation1, failure) -> MessageEvent.fire(eventBus, Message.error(resources.messages()
                                    .setIdentityPasswordError(selectedIdentity, resourceName, failure))),
                            (operation1, exception) -> MessageEvent.fire(eventBus, Message.error(resources.messages()
                                    .setIdentityPasswordError(selectedIdentity, resourceName, exception.getMessage()))));

                });
        Wizard<PasswordContext, PasswordState> wizard = wb.build();
        wizard.show();
    }

}
