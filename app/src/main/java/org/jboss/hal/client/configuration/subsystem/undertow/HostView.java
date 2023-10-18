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
package org.jboss.hal.client.configuration.subsystem.undertow;

import java.util.EnumMap;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.HOST_TEMPLATE;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.CSS.pfIcon;

public class HostView extends HalViewImpl implements HostPresenter.MyView {

    private final MetadataRegistry metadataRegistry;
    private final Form<ModelNode> configurationForm;
    private final Map<HostSetting, Form<ModelNode>> hostSettingForms;
    private final Form<ModelNode> consoleAccessLogKeysForm;
    private final VerticalNavigation navigation;
    private HostPresenter presenter;

    @Inject
    public HostView(MetadataRegistry metadataRegistry, Resources resources) {
        this.metadataRegistry = metadataRegistry;

        Metadata hostMetadata = metadataRegistry.lookup(HOST_TEMPLATE);
        configurationForm = new ModelNodeForm.Builder<ModelNode>(Ids.UNDERTOW_HOST_ATTRIBUTES_FORM, hostMetadata)
                .onSave((form, changedValues) -> presenter.saveHost(changedValues))
                .prepareReset(form -> presenter.resetHost(form))
                .build();
        HTMLElement configurationSection = section()
                .add(h(1).textContent(Names.CONFIGURATION))
                .add(p().textContent(hostMetadata.getDescription().getDescription()))
                .add(configurationForm).element();

        hostSettingForms = new EnumMap<>(HostSetting.class);
        for (HostSetting setting : HostSetting.values()) {
            hostSettingForms.put(setting, hostSettingForm(setting));
        }

        navigation = new VerticalNavigation();
        navigation.addPrimary(Ids.UNDERTOW_HOST_ATTRIBUTES_ITEM, Names.CONFIGURATION, pfIcon("settings"),
                configurationSection);

        HostSetting setting = HostSetting.ACCESS_LOG;
        Metadata metadata = metadataRegistry.lookup(HOST_TEMPLATE.append(setting.templateSuffix()));
        navigation.addPrimary(Ids.build(setting.baseId, Ids.ITEM), setting.type, setting.icon,
                section()
                        .add(h(1).textContent(setting.type))
                        .add(p().textContent(metadata.getDescription().getDescription()))
                        .add(hostSettingForms.get(setting).element()));

        // console access log is special!
        setting = HostSetting.CONSOLE_ACCESS_LOG;
        metadata = metadataRegistry.lookup(HOST_TEMPLATE.append(setting.templateSuffix()));
        consoleAccessLogKeysForm = consoleAccessLogKeys(metadata);
        Tabs tabs = new Tabs(Ids.UNDERTOW_HOST_CONSOLE_ACCESS_LOG_TAB_CONTAINER);
        tabs.add(Ids.UNDERTOW_HOST_CONSOLE_ACCESS_LOG_ATTRIBUTES_TAB, resources.constants().attributes(),
                hostSettingForms.get(setting).element());
        // TODO Add tab if ready
        // tabs.add(Ids.UNDERTOW_HOST_CONSOLE_ACCESS_LOG_KEYS_TAB, resources.constants().keys(),
        // consoleAccessLogKeysForm.element());
        navigation.addPrimary(Ids.build(setting.baseId, Ids.ITEM), setting.type, setting.icon,
                section()
                        .add(h(1).textContent(setting.type))
                        .add(p().textContent(metadata.getDescription().getDescription()))
                        .add(tabs));

        setting = HostSetting.HTTP_INVOKER;
        metadata = metadataRegistry.lookup(HOST_TEMPLATE.append(setting.templateSuffix()));
        navigation.addPrimary(Ids.build(setting.baseId, Ids.ITEM), setting.type, setting.icon,
                section()
                        .add(h(1).textContent(setting.type))
                        .add(p().textContent(metadata.getDescription().getDescription()))
                        .add(hostSettingForms.get(setting).element()));

        registerAttachable(navigation, configurationForm, consoleAccessLogKeysForm);
        registerAttachables(hostSettingForms.values());

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
    }

    private Form<ModelNode> hostSettingForm(HostSetting hostSetting) {
        Metadata metadata = metadataRegistry.lookup(HOST_TEMPLATE.append(hostSetting.templateSuffix()));
        return new ModelNodeForm.Builder<>(Ids.build(hostSetting.baseId, Ids.FORM), metadata)
                .singleton(() -> presenter.hostSettingOperation(hostSetting),
                        () -> presenter.addHostSetting(hostSetting))
                .onSave((f, changedValues) -> presenter.saveHostSetting(hostSetting, changedValues))
                .prepareReset(f -> presenter.resetHostSetting(hostSetting, f))
                .prepareRemove(f -> presenter.removeHostSetting(hostSetting, f))
                .build();
    }

    private Form<ModelNode> consoleAccessLogKeys(Metadata metadata) {
        // TODO Implement according to analysis document
        return null;
    }

    @Override
    public void setPresenter(final HostPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(final ModelNode payload) {
        configurationForm.view(payload);
        for (HostSetting setting : HostSetting.values()) {
            hostSettingForms.get(setting).view(failSafeGet(payload, setting.path()));
        }
        consoleAccessLogKeysForm.view(failSafeGet(payload, HostSetting.CONSOLE_ACCESS_LOG.path()));
    }
}
