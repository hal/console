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
package org.jboss.hal.client.configuration.subsystem.undertow;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import javax.inject.Inject;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.ListItem;
import org.jboss.hal.ballroom.form.PropertiesItem;
import org.jboss.hal.core.configuration.PathsAutoComplete;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SERVLET_CONTAINER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.ServletContainerSetting.COOKIE;
import static org.jboss.hal.client.configuration.subsystem.undertow.ServletContainerSetting.CRAWLER;
import static org.jboss.hal.client.configuration.subsystem.undertow.ServletContainerSetting.SESSIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.pfIcon;

public class ServletContainerView extends HalViewImpl implements ServletContainerPresenter.MyView {

    private static final ServletContainerSetting[] NAVIGATION_ORDER = new ServletContainerSetting[]{
            ServletContainerSetting.JSP, ServletContainerSetting.WEBSOCKETS, SESSIONS, COOKIE, CRAWLER
    };

    private final Dispatcher dispatcher;
    private final Form<ModelNode> configurationForm;
    private final Form<ModelNode> mimeMappingForm;
    private final PropertiesItem mimeMappingItem;
    private final Form<ModelNode> welcomeFileForm;
    private final ListItem welcomeFileItem;
    private final Map<ServletContainerSetting, Form<ModelNode>> settings;
    private ServletContainerPresenter presenter;

    @Inject
    @SuppressWarnings("ConstantConditions")
    public ServletContainerView(Dispatcher dispatcher,
            MetadataRegistry metadataRegistry,
            Resources resources) {
        this.dispatcher = dispatcher;

        Metadata configurationMetadata = metadataRegistry.lookup(SERVLET_CONTAINER_TEMPLATE);
        configurationForm = new ModelNodeForm.Builder<>(Ids.UNDERTOW_SERVLET_CONTAINER_CONFIGURATION_FORM,
                configurationMetadata)
                .onSave((form, changedValues) -> presenter.saveServletContainer(changedValues))
                .prepareReset(form -> presenter.resetServletContainer(form))
                .build();

        Metadata emptyMetadata = Metadata.empty();

        ModelNode mimeMappingDescription = failSafeGet(configurationMetadata.getDescription(),
                "children/mime-mapping/description"); //NON-NLS
        mimeMappingItem = new PropertiesItem(MIME_MAPPING, Names.MIME_MAPPING);
        mimeMappingForm = new ModelNodeForm.Builder<>(Ids.UNDERTOW_SERVLET_CONTAINER_MIME_MAPPING_FORM, emptyMetadata)
                .unboundFormItem(mimeMappingItem, 0, SafeHtmlUtils.fromString(mimeMappingDescription.asString()))
                .exclude(VALUE)
                .onSave((form, changedValues) -> presenter.saveMimeMapping(mimeMappingItem.getValue()))
                .prepareReset(form -> presenter.resetMimeMapping(form))
                .build();

        ModelNode welcomeFileDescription = failSafeGet(configurationMetadata.getDescription(),
                "children/welcome-file/description"); //NON-NLS
        welcomeFileItem = new ListItem(WELCOME_FILE, Names.WELCOME_FILE);
        welcomeFileForm = new ModelNodeForm.Builder<>(Ids.UNDERTOW_SERVLET_CONTAINER_WELCOME_FILE_FORM, emptyMetadata)
                .unboundFormItem(welcomeFileItem, 0, SafeHtmlUtils.fromString(welcomeFileDescription.asString()))
                .onSave((form, changedValues) -> presenter.saveWelcomeFile(welcomeFileItem.getValue().stream()
                        .collect(toMap(Function.identity(), value -> null))))
                .prepareReset(form -> presenter.resetWelcomeFile(form))
                .build();

        Tabs tabs = new Tabs(Ids.UNDERTOW_SERVLET_CONTAINER_TAB_CONTAINER);
        tabs.add(Ids.UNDERTOW_SERVLET_CONTAINER_CONFIGURATION_TAB, resources.constants().attributes(),
                configurationForm.element());
        tabs.add(Ids.UNDERTOW_SERVLET_CONTAINER_MIME_MAPPING_TAB, Names.MIME_MAPPING, mimeMappingForm.element());
        tabs.add(Ids.UNDERTOW_SERVLET_CONTAINER_WELCOME_FILE_TAB, Names.WELCOME_FILE, welcomeFileForm.element());

        HTMLElement configurationSection = section()
                .add(h(1).textContent(Names.CONFIGURATION))
                .add(p().textContent(configurationMetadata.getDescription().getDescription()))
                .add(tabs)
                .asElement();

        settings = new EnumMap<>(ServletContainerSetting.class);
        Map<ServletContainerSetting, HTMLElement> settingsSections = new EnumMap<>(ServletContainerSetting.class);
        for (ServletContainerSetting setting : ServletContainerSetting.values()) {
            Metadata metadata = metadataRegistry.lookup(SERVLET_CONTAINER_TEMPLATE.append(setting.templateSuffix()));
            Form<ModelNode> form = failSafeFrom(setting, metadata);
            settings.put(setting, form);
            settingsSections.put(setting, section()
                    .add(h(1).textContent(setting.type))
                    .add(p().textContent(metadata.getDescription().getDescription()))
                    .add(form)
                    .asElement());
        }

        VerticalNavigation navigation = new VerticalNavigation();
        navigation.addPrimary(Ids.UNDERTOW_SERVLET_CONTAINER_CONFIGURATION_ITEM, Names.CONFIGURATION,
                pfIcon("settings"), configurationSection);
        for (ServletContainerSetting setting : NAVIGATION_ORDER) {
            navigation.addPrimary(Ids.build(setting.baseId, Ids.ITEM), setting.type, setting.icon,
                    settingsSections.get(setting));
        }

        registerAttachable(navigation, configurationForm, mimeMappingForm, welcomeFileForm);
        settings.values().forEach(s -> registerAttachable(s));

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
    }

    private Form<ModelNode> failSafeFrom(ServletContainerSetting settingType, Metadata metadata) {
        return new ModelNodeForm.Builder<>(Ids.build(settingType.baseId, Ids.FORM), metadata)
                .singleton(() -> presenter.pingSettings(settingType), () -> presenter.addSettingsSingleton(settingType))
                .onSave((f, changedValues) -> presenter.saveSettings(settingType, changedValues))
                .prepareReset(f -> presenter.resetSettings(settingType, f))
                .prepareRemove(f -> presenter.removeSettings(settingType, f))
                .build();
    }

    @Override
    public void attach() {
        super.attach();
        settings.get(ServletContainerSetting.WEBSOCKETS).getFormItem(BUFFER_POOL).registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, presenter.getStatementContext(),
                        AddressTemplate.of("/{selected.profile}/subsystem=io/buffer-pool=*")));
        settings.get(ServletContainerSetting.WEBSOCKETS).getFormItem(WORKER).registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, presenter.getStatementContext(),
                        AddressTemplate.of("/{selected.profile}/subsystem=io/worker=*")));
        settings.get(SESSIONS).getFormItem(RELATIVE_TO).registerSuggestHandler(new PathsAutoComplete());

    }

    @Override
    public void setPresenter(ServletContainerPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(ModelNode payload) {
        configurationForm.view(payload);
        mimeMappingForm.view(new ModelNode());
        mimeMappingItem.setValue(failSafePropertyList(payload, MIME_MAPPING).stream()
                .collect(toMap(Property::getName, property -> property.getValue().get(VALUE).asString())));
        welcomeFileForm.view(new ModelNode());
        welcomeFileItem.setValue(failSafePropertyList(payload, WELCOME_FILE).stream()
                .map(Property::getName)
                .collect(toList()));
        settings.forEach((settingType, form) -> form.view(failSafeGet(payload, settingType.path())));
    }
}
