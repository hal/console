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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.ROLE_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ROLE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SECURITY_SETTING;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.fontAwesome;

@MbuiView
@SuppressWarnings("DuplicateStringLiteralInspection")
public abstract class DestinationView extends MbuiViewImpl<DestinationPresenter>
        implements DestinationPresenter.MyView {

    public static DestinationView create(final MbuiContext mbuiContext) {
        return new Mbui_DestinationView(mbuiContext);
    }

    @MbuiElement("messaging-destination-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("messaging-core-queue-table") Table<NamedNode> coreQueueTable;
    @MbuiElement("messaging-core-queue-form") Form<NamedNode> coreQueueForm;
    @MbuiElement("messaging-jms-queue-table") Table<NamedNode> jmsQueueTable;
    @MbuiElement("messaging-jms-queue-form") Form<NamedNode> jmsQueueForm;
    @MbuiElement("messaging-jms-topic-table") Table<NamedNode> jmsTopicTable;
    @MbuiElement("messaging-jms-topic-form") Form<NamedNode> jmsTopicForm;
    @MbuiElement("messaging-address-setting-table") Table<NamedNode> addressSettingTable;
    @MbuiElement("messaging-address-setting-form") Form<NamedNode> addressSettingForm;
    @MbuiElement("messaging-divert-table") Table<NamedNode> divertTable;
    @MbuiElement("messaging-divert-form") Form<NamedNode> divertForm;

    private Table<NamedNode> roleTable;
    private Form<NamedNode> roleForm;

    DestinationView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @PostConstruct
    void init() {
        Metadata roleMetadata = mbuiContext.metadataRegistry().lookup(ROLE_TEMPLATE);
        roleTable = new ModelNodeTable.Builder<NamedNode>(Ids.MESSAGING_SECURITY_SETTING_ROLE_TABLE, roleMetadata)
                .button(mbuiContext.tableButtonFactory().add(ROLE_TEMPLATE,
                        table -> presenter.addSecuritySettingRole()))
                .button(mbuiContext.tableButtonFactory().remove(ROLE_TEMPLATE,
                        table -> presenter.removeSecuritySettingRole(table.selectedRow())))
                .column(SECURITY_SETTING, mbuiContext.resources().constants().pattern(),
                        (cell, type, row, meta) -> row.get(SECURITY_SETTING).asString())
                .column(ROLE, mbuiContext.resources().constants().role(),
                        (cell, type, row, meta) -> row.getName())
                .build();

        roleForm = new ModelNodeForm.Builder<NamedNode>(Ids.MESSAGING_SECURITY_SETTING_ROLE_FORM, roleMetadata)
                .onSave((form, changedValues) -> presenter.saveSecuritySettingRole(form, changedValues))
                .prepareReset(form -> presenter.resetSecuritySettingRole(form))
                .build();

        registerAttachable(roleTable, roleForm);

        HTMLElement roleSection = section()
                .add(h(1).textContent(Names.SECURITY_SETTING))
                .add(p().textContent(roleMetadata.getDescription().getDescription()))
                .add(roleTable)
                .add(roleForm)
                .asElement();

        //noinspection HardCodedStringLiteral
        navigation.insertPrimary(Ids.MESSAGING_SECURITY_SETTING_ROLE_ENTRY, "messaging-address-setting-entry",
                Names.SECURITY_SETTING, fontAwesome("lock"), roleSection);
    }

    @Override
    public void attach() {
        super.attach();
        roleTable.onSelectionChange(t -> {
            if (t.hasSelection()) {
                //noinspection ConstantConditions
                presenter.selectSecuritySetting(t.selectedRow().get(SECURITY_SETTING).asString());
                roleForm.view(t.selectedRow());
            } else {
                presenter.selectSecuritySetting(null);
                roleForm.clear();
            }
        });
    }

    @Override
    public void updateCoreQueue(final List<NamedNode> coreQueues) {
        coreQueueForm.clear();
        coreQueueTable.update(coreQueues);
    }

    @Override
    public void updateJmsQueue(final List<NamedNode> jmsQueues) {
        jmsQueueForm.clear();
        jmsQueueTable.update(jmsQueues);
    }

    @Override
    public void updateJmsTopic(final List<NamedNode> jmsTopics) {
        jmsTopicForm.clear();
        jmsTopicTable.update(jmsTopics);
    }

    @Override
    public void updateSecuritySetting(final List<NamedNode> securitySettings) {
        // Extract the roles and store the parent security setting name as artificial value
        List<NamedNode> roles = new ArrayList<>();
        securitySettings.forEach(securitySetting -> {
            List<NamedNode> currentRoles = asNamedNodes(failSafePropertyList(securitySetting, ROLE));
            currentRoles.forEach(role -> {
                role.get(SECURITY_SETTING).set(securitySetting.getName());
                roles.add(role);
            });
        });

        roleForm.clear();
        roleTable.update(roles);
    }

    @Override
    public void updateAddressSetting(final List<NamedNode> addressSettings) {
        addressSettingForm.clear();
        addressSettingTable.update(addressSettings);
    }

    @Override
    public void updateDivert(final List<NamedNode> diverts) {
        divertForm.clear();
        divertTable.update(diverts);
    }
}
