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
package org.jboss.hal.client.runtime.subsystem.messaging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.client.runtime.subsystem.messaging.Destination.Type;
import org.jboss.hal.core.SuccessfulOutcome;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.form.OperationFormBuilder;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.runtime.subsystem.messaging.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeBoolean;
import static org.jboss.hal.flow.Flow.series;

public class JmsQueuePresenter extends ApplicationFinderPresenter<JmsQueuePresenter.MyView, JmsQueuePresenter.MyProxy> {

    private static final long MESSAGES_THRESHOLD = 500L;
    private static final String MESSAGES_COUNT = "messagesCount";
    private static final String MESSAGES = "messages";
    @NonNls private static final Logger logger = LoggerFactory.getLogger(JmsQueuePresenter.class);

    private final FinderPathFactory finderPathFactory;
    private final MetadataRegistry metadataRegistry;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Provider<Progress> progress;
    private final Resources resources;
    private final Map<String, Boolean> showAll;
    private String deployment;
    private String subdeployment;
    private String messageServer;
    private String queue;

    @Inject
    public JmsQueuePresenter(EventBus eventBus,
            JmsQueuePresenter.MyView view,
            JmsQueuePresenter.MyProxy myProxy,
            Finder finder,
            FinderPathFactory finderPathFactory,
            MetadataRegistry metadataRegistry,
            Dispatcher dispatcher,
            StatementContext statementContext,
            @Footer Provider<Progress> progress,
            Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.finderPathFactory = finderPathFactory;
        this.metadataRegistry = metadataRegistry;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.progress = progress;
        this.resources = resources;
        this.showAll = new HashMap<>();
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        deployment = request.getParameter(DEPLOYMENT, null);
        subdeployment = request.getParameter(SUBDEPLOYMENT, null);
        messageServer = request.getParameter(Ids.MESSAGING_SERVER, null);
        queue = request.getParameter(NAME, null);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.runtimeServerPath()
                .append(Ids.RUNTIME_SUBSYSTEM, MESSAGING_ACTIVEMQ, resources.constants().monitor(), Names.MESSAGING)
                .append(Ids.MESSAGING_SERVER_RUNTIME, Ids.messagingServer(messageServer), Names.SERVER, messageServer)
                .append(Ids.MESSAGING_SERVER_DESTINATION,
                        Ids.destination(deployment, subdeployment, messageServer, Type.JMS_QUEUE.name(), queue),
                        Names.DESTINATION, queue);
    }

    @Override
    protected void reload() {
        if (showAll()) {
            readAll();

        } else {
            ResourceAddress address = queueAddress();
            Task<FlowContext> count = context -> {
                Operation operation = new Operation.Builder(address, COUNT_MESSAGES).build();
                return dispatcher.execute(operation)
                        .doOnSuccess(result -> context.set(MESSAGES_COUNT, result.asLong()))
                        .toCompletable();
            };
            Task<FlowContext> list = context -> {
                long messages = context.get(MESSAGES_COUNT);
                if (messages > MESSAGES_THRESHOLD) {
                    context.set(MESSAGES, emptyList());
                    return Completable.complete();
                } else {
                    Operation operation = new Operation.Builder(address, LIST_MESSAGES).build();
                    return dispatcher.execute(operation)
                            .doOnSuccess(result -> context.set(MESSAGES,
                                    result.asList().stream().map(JmsMessage::new).collect(toList())))
                            .toCompletable();
                }
            };
            series(new FlowContext(progress.get()), count, list)
                    .subscribe(new SuccessfulOutcome<FlowContext>(getEventBus(), resources) {
                        @Override
                        public void onSuccess(FlowContext context) {
                            long count = context.get(MESSAGES_COUNT);
                            List<JmsMessage> messages = context.get(MESSAGES);
                            if (count > MESSAGES_THRESHOLD) {
                                logger.debug("More than {} messages in queue {}. Skip :list-messages operation.",
                                        MESSAGES_THRESHOLD, queueAddress().toString());
                                getView().showMany(count);
                            } else {
                                getView().showAll(messages);
                            }
                        }
                    });
        }
    }

    void readAllMessages(boolean always) {
        if (always) {
            String id = Ids.destination(deployment, subdeployment, messageServer, Type.JMS_QUEUE.name(), queue);
            showAll.put(id, true);
        }
        readAll();
    }

    private void readAll() {
        ResourceAddress address = queueAddress();
        Operation operation = new Operation.Builder(address, LIST_MESSAGES).build();
        dispatcher.execute(operation, result ->
                getView().showAll(result.asList().stream().map(JmsMessage::new).collect(toList())));
    }

    private boolean showAll() {
        String id = Ids.destination(deployment, subdeployment, messageServer, Type.JMS_QUEUE.name(), queue);
        return showAll.getOrDefault(id, false);
    }

    void changePriority(List<JmsMessage> messages) {
        if (messages.isEmpty()) {
            noMessagesSelected();
        } else {
            Metadata metadata = metadataRegistry.lookup(MESSAGING_QUEUE_TEMPLATE);
            Form<ModelNode> form = new OperationFormBuilder<>(Ids.JMS_MESSAGE_CHANGE_PRIORITY_FORM, metadata,
                    CHANGE_MESSAGE_PRIORITY)
                    .build();

            Dialog dialog = new Dialog.Builder(resources.constants().changePriority())
                    .add(form.asElement())
                    .cancel()
                    .primary(resources.constants().ok(), () -> {
                        boolean valid = form.save();
                        if (valid) {
                            Operation operation;
                            int priority = form.getModel().get(NEW_PRIORITY).asInt();
                            if (messages.size() == 1) {
                                operation = new Operation.Builder(queueAddress(), CHANGE_MESSAGE_PRIORITY)
                                        .param(MESSAGE_ID, messages.get(0).getMessageId())
                                        .param(NEW_PRIORITY, priority)
                                        .build();
                            } else {
                                operation = new Operation.Builder(queueAddress(), CHANGE_MESSAGES_PRIORITY)
                                        .param(FILTER, filter(messages))
                                        .param(NEW_PRIORITY, priority)
                                        .build();
                            }
                            dispatcher.execute(operation, result -> {
                                reload();
                                MessageEvent.fire(getEventBus(),
                                        Message.success(resources.messages().changePrioritySuccess(priority)));
                            });
                        }
                        return valid;
                    })
                    .build();
            dialog.registerAttachable(form);
            dialog.show();

            ModelNode model = new ModelNode();
            form.edit(model);
            FormItem<Number> messageId = form.getFormItem(MESSAGE_ID);
            messageId.setValue(42L);
            Elements.setVisible(messageId.asElement(Form.State.EDITING), false);
            FormItem<Number> priorityItem = form.getFormItem(NEW_PRIORITY);
            if (messages.size() == 1) {
                priorityItem.setValue(messages.get(0).get(JMS_PRIORITY).asLong());
            }
            priorityItem.setFocus(true);
        }
    }

    void expire(List<JmsMessage> messages) {
        if (messages.isEmpty()) {
            noMessagesSelected();
        } else {
            SafeHtml question = messages.size() == 1
                    ? resources.messages().expireMessageQuestion()
                    : resources.messages().expireMessagesQuestion();
            DialogFactory.showConfirmation(resources.constants().expire(), question, () -> {
                Operation operation;
                if (messages.size() == 1) {
                    operation = new Operation.Builder(queueAddress(), EXPIRE_MESSAGE)
                            .param(MESSAGE_ID, messages.get(0).getMessageId())
                            .build();
                } else {
                    operation = new Operation.Builder(queueAddress(), EXPIRE_MESSAGES)
                            .param(FILTER, filter(messages))
                            .build();
                }
                dispatcher.execute(operation, result -> {
                    reload();
                    MessageEvent.fire(getEventBus(),
                            Message.success(resources.messages().expireMessageSuccess()));
                });
            });
        }
    }

    void move(List<JmsMessage> messages) {
        if (messages.isEmpty()) {
            noMessagesSelected();
        } else {
            Metadata metadata = metadataRegistry.lookup(MESSAGING_QUEUE_TEMPLATE);
            Form<ModelNode> form = new OperationFormBuilder<>(Ids.JMS_MESSAGE_MOVE_FORM, metadata, MOVE_MESSAGE)
                    .build();

            Dialog dialog = new Dialog.Builder(resources.constants().move())
                    .add(form.asElement())
                    .cancel()
                    .primary(resources.constants().ok(), () -> {
                        boolean valid = form.save();
                        if (valid) {
                            Operation operation;
                            String destination = form.getModel().get(OTHER_QUEUE_NAME).asString();
                            boolean rejectDuplicates = failSafeBoolean(form.getModel(), REJECT_DUPLICATES);
                            if (messages.size() == 1) {
                                operation = new Operation.Builder(queueAddress(), CHANGE_MESSAGE_PRIORITY)
                                        .param(MESSAGE_ID, messages.get(0).getMessageId())
                                        .param(OTHER_QUEUE_NAME, destination)
                                        .param(REJECT_DUPLICATES, rejectDuplicates)
                                        .build();
                            } else {
                                operation = new Operation.Builder(queueAddress(), CHANGE_MESSAGES_PRIORITY)
                                        .param(FILTER, filter(messages))
                                        .param(OTHER_QUEUE_NAME, destination)
                                        .param(REJECT_DUPLICATES, rejectDuplicates)
                                        .build();
                            }
                            dispatcher.execute(operation, result -> {
                                reload();
                                MessageEvent.fire(getEventBus(),
                                        Message.success(resources.messages().moveMessageSuccess(destination)));
                            });
                        }
                        return valid;
                    })
                    .build();
            dialog.registerAttachable(form);
            dialog.show();

            ModelNode model = new ModelNode();
            form.edit(model);
            FormItem<Number> messageId = form.getFormItem(MESSAGE_ID);
            messageId.setValue(42L);
            Elements.setVisible(messageId.asElement(Form.State.EDITING), false);
            form.getFormItem(OTHER_QUEUE_NAME).setFocus(true);
        }
    }

    void sendToDeadLetter(List<JmsMessage> messages) {
        if (messages.isEmpty()) {
            noMessagesSelected();
        } else {
            SafeHtml question = messages.size() == 1
                    ? resources.messages().sendMessageToDeadLetterQuestion()
                    : resources.messages().sendMessagesToDeadLetterQuestion();
            DialogFactory.showConfirmation(resources.constants().sendToDeadLetter(), question, () -> {
                Operation operation;
                if (messages.size() == 1) {
                    operation = new Operation.Builder(queueAddress(), SEND_MESSAGE_TO_DEAD_LETTER_ADDRESS)
                            .param(MESSAGE_ID, messages.get(0).getMessageId())
                            .build();
                } else {
                    operation = new Operation.Builder(queueAddress(), SEND_MESSAGES_TO_DEAD_LETTER_ADDRESS)
                            .param(FILTER, filter(messages))
                            .build();
                }
                dispatcher.execute(operation, result -> {
                    reload();
                    MessageEvent.fire(getEventBus(),
                            Message.success(resources.messages().sendMessageToDeadLetterSuccess()));
                });
            });
        }
    }

    void remove(List<JmsMessage> messages) {
        if (messages.isEmpty()) {
            noMessagesSelected();
        } else {
            SafeHtml question = messages.size() == 1
                    ? resources.messages().removeMessageQuestion()
                    : resources.messages().removeMessagesQuestion();
            DialogFactory.showConfirmation(resources.constants().remove(), question, () -> {
                Operation operation;
                if (messages.size() == 1) {
                    operation = new Operation.Builder(queueAddress(), REMOVE_MESSAGE)
                            .param(MESSAGE_ID, messages.get(0).getMessageId())
                            .build();
                } else {
                    operation = new Operation.Builder(queueAddress(), REMOVE_MESSAGES)
                            .param(FILTER, filter(messages))
                            .build();
                }
                dispatcher.execute(operation, result -> {
                    reload();
                    MessageEvent.fire(getEventBus(),
                            Message.success(resources.messages().removeMessageSuccess()));
                });
            });
        }
    }

    private void noMessagesSelected() {
        MessageEvent.fire(getEventBus(), Message.warning(resources.messages().noMessagesSelected()));
    }

    private String filter(List<JmsMessage> messages) {
        return messages.stream()
                .map(message -> JMS_MESSAGE_ID + "='" + message.getMessageId() + "'")
                .collect(joining(" OR ")); //NON-NLS
    }

    private ResourceAddress queueAddress() {
        ResourceAddress address;
        if (deployment != null || subdeployment != null) {
            if (subdeployment == null) {
                address = MESSAGING_DEPLOYMENT_TEMPLATE
                        .append("jms-queue=*")
                        .resolve(statementContext, deployment, messageServer, queue);
            } else {
                address = MESSAGING_SUBDEPLOYMENT_TEMPLATE
                        .append("jms-queue=*")
                        .resolve(statementContext, deployment, subdeployment, messageServer, queue);
            }

        } else {
            address = MESSAGING_SERVER_TEMPLATE
                    .append("jms-queue=*")
                    .resolve(statementContext, messageServer, queue);
        }
        return address;
    }


    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.JMS_QUEUE)
    @Requires(MESSAGING_QUEUE_ADDRESS)
    public interface MyProxy extends ProxyPlace<JmsQueuePresenter> {
    }

    public interface MyView extends HalView, HasPresenter<JmsQueuePresenter> {
        void showMany(long count);
        void showAll(List<JmsMessage> messages);
    }
    // @formatter:on
}
