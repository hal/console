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
package org.jboss.hal.client.configuration.subsystem.messaging;

import java.util.List;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

/**
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings("DuplicateStringLiteralInspection")
public abstract class DestinationView extends MbuiViewImpl<DestinationPresenter>
        implements DestinationPresenter.MyView {

    public static DestinationView create(final MbuiContext mbuiContext) {
        return new Mbui_DestinationView(mbuiContext);
    }

    @MbuiElement("messaging-destination-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("messaging-core-queue-table") NamedNodeTable<NamedNode> coreQueueTable;
    @MbuiElement("messaging-core-queue-form") Form<NamedNode> coreQueueForm;
    @MbuiElement("messaging-jms-queue-table") NamedNodeTable<NamedNode> jmsQueueTable;
    @MbuiElement("messaging-jms-queue-form") Form<NamedNode> jmsQueueForm;
    @MbuiElement("messaging-jms-topic-table") NamedNodeTable<NamedNode> jmsTopicTable;
    @MbuiElement("messaging-jms-topic-form") Form<NamedNode> jmsTopicForm;
    @MbuiElement("messaging-address-setting-table") NamedNodeTable<NamedNode> addressSettingTable;
    @MbuiElement("messaging-address-setting-form") Form<NamedNode> addressSettingForm;
    @MbuiElement("messaging-divert-table") NamedNodeTable<NamedNode> divertTable;
    @MbuiElement("messaging-divert-form") Form<NamedNode> divertForm;

    DestinationView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @Override
    public void updateCoreQueues(final List<NamedNode> coreQueues) {
        coreQueueForm.clear();
        coreQueueTable.update(coreQueues);
    }

    @Override
    public void updateJmsQueues(final List<NamedNode> jmsQueues) {
        jmsQueueForm.clear();
        jmsQueueTable.update(jmsQueues);
    }

    @Override
    public void updateJmsTopics(final List<NamedNode> jmsTopics) {
        jmsTopicForm.clear();
        jmsTopicTable.update(jmsTopics);
    }

    @Override
    public void updateSecuritySettings(final List<NamedNode> securitySettings) {

    }

    @Override
    public void updateAddressSettings(final List<NamedNode> addressSettings) {
        addressSettingForm.clear();
        addressSettingTable.update(addressSettings);
    }

    @Override
    public void updateDiverts(final List<NamedNode> diverts) {
        divertForm.clear();
        divertTable.update(diverts);
    }
}
