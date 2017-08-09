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

import java.util.List;
import javax.inject.Inject;

import org.jboss.hal.ballroom.dataprovider.DataProvider;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.Toolbar;
import org.jboss.hal.ballroom.Toolbar.Attribute;
import org.jboss.hal.core.mbui.listview.ModelNodeListView;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static java.util.Comparator.comparing;
import static org.jboss.hal.client.runtime.subsystem.messaging.AddressTemplates.MESSAGING_QUEUE_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;


public class JmsQueueView extends HalViewImpl implements JmsQueuePresenter.MyView {

    private static final String TOO_MANY_MESSAGES = "tooManyMessages";

    private final Resources resources;
    private final DataProvider<JmsMessage> dataProvider;
    private final EmptyState tooManyMessages;
    private final ModelNodeListView<JmsMessage> listView;
    private JmsQueuePresenter presenter;

    @Inject
    public JmsQueueView(MetadataRegistry metadataRegistry, Resources resources) {
        this.resources = resources;

        dataProvider = new DataProvider<>(JmsMessage::getName, true);
        Metadata metadata = metadataRegistry.lookup(MESSAGING_QUEUE_TEMPLATE);
        tooManyMessages = new EmptyState.Builder(resources.constants().manyMessages())
                .icon(Icons.WARNING)
                .primaryAction(resources.constants().allMessagesAlways(), () -> presenter.readAllMessages(true))
                .secondaryAction(resources.constants().allMessagesOnce(), () -> presenter.readAllMessages(false))
                .build();
        listView = new ModelNodeListView.Builder<>(Ids.JMS_MESSAGE_LIST, metadata, dataProvider,
                item -> new JmsMessageDisplay(item, presenter, resources))

                .toolbarAttribute(new Attribute<>(JMS_MESSAGE_ID, JMS_MESSAGE_ID,
                        (model, filter) -> model.getMessageId().contains(filter),
                        comparing(JmsMessage::getMessageId)))
                .toolbarAttribute(new Attribute<>(JMS_TIMESTAMP, JMS_TIMESTAMP,
                        comparing(JmsMessage::getTimestamp)))
                .toolbarAttribute(new Attribute<>(JMS_EXPIRATION, JMS_EXPIRATION,
                        comparing(JmsMessage::getExpiration)))
                .toolbarAttribute(new Attribute<>(JMS_PRIORITY, JMS_PRIORITY,
                        (model, filter) -> model.hasDefined(JMS_PRIORITY) &&
                                model.get(JMS_PRIORITY).asString().equals(filter),
                        comparing(JmsMessage::getPriority)))
                .toolbarAttribute(new Attribute<>(JMS_DELIVERY_MODE, JMS_DELIVERY_MODE,
                        (model, filter) -> model.hasDefined(JMS_DELIVERY_MODE) &&
                                model.get(JMS_DELIVERY_MODE).asString().contains(filter),
                        comparing(JmsMessage::getDeliveryMode)))

                .toolbarAction(new Toolbar.Action(Ids.JMS_MESSAGE_LIST_REFRESH, resources.constants().refresh(),
                        this::refresh))
                .toolbarAction(new Toolbar.Action(Ids.JMS_MESSAGE_LIST_CLEAR_SELECTION,
                        resources.constants().clearSelection(), this::clearSelection))
                .toolbarAction(new Toolbar.Action(Ids.JMS_MESSAGE_LIST_SELECT_ALL,
                        resources.constants().selectAll(), this::selectAll))
                .toolbarAction(new Toolbar.Action(Ids.JMS_MESSAGE_LIST_CHANGE_PRIORITY,
                        resources.constants().changePriority(),
                        Constraint.executable(MESSAGING_QUEUE_TEMPLATE, CHANGE_MESSAGES_PRIORITY),
                        this::changePriority))
                .toolbarAction(new Toolbar.Action(Ids.JMS_MESSAGE_LIST_EXPIRE,
                        resources.constants().expire(),
                        Constraint.executable(MESSAGING_QUEUE_TEMPLATE, EXPIRE_MESSAGES),
                        this::expire))
                .toolbarAction(new Toolbar.Action(Ids.JMS_MESSAGE_LIST_MOVE, resources.constants().move(),
                        Constraint.executable(MESSAGING_QUEUE_TEMPLATE, MOVE_MESSAGES),
                        this::move))
                .toolbarAction(new Toolbar.Action(Ids.JMS_MESSAGE_LIST_SEND_TO_DEAD_LETTER,
                        resources.constants().sendToDeadLetter(),
                        Constraint.executable(MESSAGING_QUEUE_TEMPLATE, SEND_MESSAGES_TO_DEAD_LETTER_ADDRESS),
                        this::sendToDeadLetter))
                .toolbarAction(new Toolbar.Action(Ids.JMS_MESSAGE_LIST_REMOVE, resources.constants().remove(),
                        Constraint.executable(MESSAGING_QUEUE_TEMPLATE, REMOVE_MESSAGES),
                        this::remove))

                .emptyState(TOO_MANY_MESSAGES, tooManyMessages)
                .multiselect(true)
                .build();

        registerAttachable(listView);
        initElements(listView);
    }

    @Override
    public void setPresenter(JmsQueuePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showMany(long count) {
        tooManyMessages.setDescription(resources.messages().manyMessages(count));
        listView.showEmptyState(TOO_MANY_MESSAGES);
    }

    @Override
    public void showAll(List<JmsMessage> messages) {
        dataProvider.update(messages);
    }

    private void refresh() {
        if (presenter != null) {
            presenter.reload();
        }
    }

    private void clearSelection() {
        dataProvider.clearVisibleSelection();
    }

    private void selectAll() {
        dataProvider.selectVisible();
    }

    private void changePriority() {
        if (presenter != null) {
            presenter.changePriority(dataProvider.getSelection());
        }
    }

    private void expire() {
        if (presenter != null) {
            presenter.expire(dataProvider.getSelection());
        }
    }

    private void move() {
        if (presenter != null) {
            presenter.move(dataProvider.getSelection());
        }
    }

    private void sendToDeadLetter() {
        if (presenter != null) {
            presenter.sendToDeadLetter(dataProvider.getSelection());
        }
    }

    private void remove() {
        if (presenter != null) {
            presenter.remove(dataProvider.getSelection());
        }
    }
}
