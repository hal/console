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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.listview.ItemAction;
import org.jboss.hal.ballroom.listview.ItemDisplay;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jetbrains.annotations.NonNls;

import static org.jboss.hal.client.runtime.subsystem.messaging.AddressTemplates.MESSAGING_QUEUE_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

class JmsMessageDisplay implements ItemDisplay<JmsMessage> {

    private final JmsMessage message;
    private final JmsQueuePresenter presenter;
    private final Resources resources;

    JmsMessageDisplay(JmsMessage message, JmsQueuePresenter presenter, Resources resources) {
        this.message = message;
        this.presenter = presenter;
        this.resources = resources;
    }

    @Override
    public String getTitle() {
        return message.getName();
    }

    @Override
    public SafeHtml getDescriptionHtml() {
        Date timestamp = message.getTimestamp();
        Date expiration = message.getExpiration();
        if (timestamp != null || expiration != null) {
            @NonNls SafeHtmlBuilder builder = new SafeHtmlBuilder();
            builder.appendHtmlConstant("<p>");
            if (timestamp != null) {
                builder.appendEscaped(JMS_TIMESTAMP + ": ").appendEscaped(Format.mediumDateTime(timestamp))
                .appendHtmlConstant("<br/>");
            }
            if (expiration != null) {
                builder.appendEscaped(JMS_EXPIRATION + ": ").appendEscaped(Format.mediumDateTime(
                        expiration));
            }
            return builder.toSafeHtml();
        }
        return null;
    }

    @Override
    public SafeHtml getAdditionalInfoHtml() {
        @NonNls SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.appendHtmlConstant("<p>")
                .appendEscaped(JMS_PRIORITY + ": " + message.get(JMS_PRIORITY).asInt())
                .appendHtmlConstant("</br/>")
                .appendEscaped(JMS_DELIVERY_MODE + ": " + message.get(JMS_DELIVERY_MODE).asString())
                .appendHtmlConstant("</p>");
        return builder.toSafeHtml();
    }

    @Override
    public List<ItemAction<JmsMessage>> actions() {
        List<ItemAction<JmsMessage>> actions = new ArrayList<>();
        actions.add(new ItemAction<>(Ids.JMS_MESSAGE_CHANGE_PRIORITY,
                resources.constants().changePriority(),
                Constraint.executable(MESSAGING_QUEUE_TEMPLATE, CHANGE_MESSAGE_PRIORITY),
                item -> presenter.changePriority(Collections.singletonList(item))));
        actions.add(new ItemAction<>(Ids.JMS_MESSAGE_EXPIRE,
                resources.constants().expire(),
                Constraint.executable(MESSAGING_QUEUE_TEMPLATE, EXPIRE_MESSAGE),
                item -> presenter.expire(Collections.singletonList(item))));
        actions.add(new ItemAction<>(Ids.JMS_MESSAGE_MOVE,
                resources.constants().move(),
                Constraint.executable(MESSAGING_QUEUE_TEMPLATE, MOVE_MESSAGE),
                item -> presenter.move(Collections.singletonList(item))));
        actions.add(new ItemAction<>(Ids.JMS_MESSAGE_SEND_TO_DEAD_LETTER,
                resources.constants().sendToDeadLetter(),
                Constraint.executable(MESSAGING_QUEUE_TEMPLATE, SEND_MESSAGE_TO_DEAD_LETTER_ADDRESS),
                item -> presenter.sendToDeadLetter(Collections.singletonList(item))));
        actions.add(new ItemAction<>(Ids.JMS_MESSAGE_REMOVE,
                resources.constants().remove(),
                Constraint.executable(MESSAGING_QUEUE_TEMPLATE, REMOVE_MESSAGE),
                item -> presenter.remove(Collections.singletonList(item))));
        return actions;
    }
}
