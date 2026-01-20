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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.InlineAction;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
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
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.FILTER_REF_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.HANDLER_SUGGESTIONS;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.HOST_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.LOCATION_FILTER_REF_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.LOCATION_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SELECTED_SERVER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SERVER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SERVLET_CONTAINER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.Listener.AJP;
import static org.jboss.hal.client.configuration.subsystem.undertow.Listener.HTTP;
import static org.jboss.hal.client.configuration.subsystem.undertow.Listener.HTTPS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT_HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.FILTER_REF;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HANDLER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LOCATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PRIORITY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVLET_CONTAINER;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;

public class ServerView extends HalViewImpl implements ServerPresenter.MyView {

    private final Dispatcher dispatcher;
    private final MetadataRegistry metadataRegistry;
    private final Form<ModelNode> configurationForm;
    private final Table<NamedNode> hostTable;
    private final Form<NamedNode> hostForm;
    private final Map<HostSetting, Form<ModelNode>> hostSettingForms;
    private final Table<NamedNode> filterRefTable;
    private final Form<NamedNode> filterRefForm;
    private final Table<NamedNode> locationTable;
    private final Form<NamedNode> locationForm;
    private final Table<NamedNode> locationFilterRefTable;
    private final Form<NamedNode> locationFilterRefForm;
    private final Pages hostPages;
    private final Map<Listener, ListenerElement> listener;
    private final VerticalNavigation navigation;
    private ServerPresenter presenter;

    @Inject
    @SuppressWarnings({ "ConstantConditions", "HardCodedStringLiteral" })
    public ServerView(Dispatcher dispatcher, MetadataRegistry metadataRegistry,
            TableButtonFactory tableButtonFactory, Resources resources) {
        this.dispatcher = dispatcher;
        this.metadataRegistry = metadataRegistry;

        // ------------------------------------------------------ server configuration

        Metadata configurationMetadata = metadataRegistry.lookup(SERVER_TEMPLATE);
        configurationForm = new ModelNodeForm.Builder<>(Ids.UNDERTOW_SERVER_CONFIGURATION_FORM, configurationMetadata)
                .onSave((form, changedValues) -> presenter.saveServer(changedValues))
                .prepareReset(form -> presenter.resetServer(form))
                .build();

        HTMLElement configurationSection = section()
                .add(h(1).textContent(Names.CONFIGURATION))
                .add(p().textContent(configurationMetadata.getDescription().getDescription()))
                .add(configurationForm).element();

        // ------------------------------------------------------ hosts

        List<InlineAction<NamedNode>> inlineActions = new ArrayList<>();
        inlineActions.add(new InlineAction<>(Names.FILTERS, row -> presenter.showFilterRef(row)));
        inlineActions.add(new InlineAction<>(Names.LOCAL_CACHE, row -> presenter.showLocation(row)));

        Metadata hostMetadata = metadataRegistry.lookup(HOST_TEMPLATE);
        hostTable = new ModelNodeTable.Builder<NamedNode>(Ids.UNDERTOW_HOST_TABLE, hostMetadata)
                .button(tableButtonFactory.add(HOST_TEMPLATE, table -> presenter.addHost()))
                .button(tableButtonFactory.remove(HOST_TEMPLATE,
                        table -> presenter.removeHost(table.selectedRow().getName())))
                .nameColumn()
                .column(inlineActions, "15em")
                .build();

        hostForm = new ModelNodeForm.Builder<NamedNode>(Ids.UNDERTOW_HOST_ATTRIBUTES_FORM, hostMetadata)
                .onSave((form, changedValues) -> presenter.saveHost(form.getModel().getName(), changedValues))
                .prepareReset(form -> presenter.resetHost(form.getModel().getName(), form))
                .build();

        hostSettingForms = new EnumMap<>(HostSetting.class);
        for (HostSetting setting : HostSetting.values()) {
            // Skip console access log until HAL-1600 has been implemented
            if (setting == HostSetting.CONSOLE_ACCESS_LOG) {
                continue;
            }
            hostSettingForms.put(setting, hostSetting(setting));
        }

        Tabs tabs = new Tabs(Ids.UNDERTOW_HOST_ATTRIBUTES_TAB_CONTAINER);
        tabs.add(Ids.UNDERTOW_HOST_ATTRIBUTES_TAB, resources.constants().attributes(), hostForm.element());
        for (HostSetting setting : HostSetting.values()) {
            // Skip console access log until HAL-1600 has been implemented
            if (setting == HostSetting.CONSOLE_ACCESS_LOG) {
                continue;
            }
            tabs.add(Ids.build(setting.baseId, Ids.TAB), setting.type,
                    hostSettingForms.get(setting).element());
        }

        HTMLElement hostSection = section()
                .add(h(1).textContent(Names.HOSTS))
                .add(p().textContent(hostMetadata.getDescription().getDescription()))
                .add(hostTable)
                .add(tabs).element();

        // ------------------------------------------------------ host filter refs

        Metadata filterRefMetadata = metadataRegistry.lookup(FILTER_REF_TEMPLATE);
        filterRefTable = new ModelNodeTable.Builder<NamedNode>(Ids.UNDERTOW_HOST_FILTER_REF_TABLE, filterRefMetadata)
                .button(tableButtonFactory.add(FILTER_REF_TEMPLATE, table -> presenter.addFilterRef()))
                .button(tableButtonFactory.remove(FILTER_REF_TEMPLATE,
                        table -> presenter.removeFilterRef(table.selectedRow().getName())))
                .column(FILTER_REF, resources.constants().filter(), (cell, type, row, meta) -> row.getName())
                .column(PRIORITY)
                .build();

        filterRefForm = new ModelNodeForm.Builder<NamedNode>(Ids.UNDERTOW_HOST_FILTER_REF_FORM, filterRefMetadata)
                .onSave((form, changedValues) -> presenter.saveFilterRef(form, changedValues))
                .prepareReset(form -> presenter.resetFilterRef(form))
                .build();

        HTMLElement filterRefSection = section()
                .add(h(1).textContent(Names.FILTERS))
                .add(p().textContent(filterRefMetadata.getDescription().getDescription()))
                .add(filterRefTable)
                .add(filterRefForm).element();

        // ------------------------------------------------------ host locations

        Metadata locationMetadata = metadataRegistry.lookup(LOCATION_TEMPLATE);
        locationTable = new ModelNodeTable.Builder<NamedNode>(Ids.UNDERTOW_HOST_LOCATION_TABLE, locationMetadata)
                .button(tableButtonFactory.add(LOCATION_TEMPLATE, table -> presenter.addLocation()))
                .button(tableButtonFactory.remove(LOCATION_TEMPLATE,
                        table -> presenter.removeLocation(table.selectedRow().getName())))
                .column(LOCATION, Names.LOCATION, (cell, type, row, meta) -> row.getName())
                .column(HANDLER)
                .column(new InlineAction<>(Names.FILTERS, row -> presenter.showLocationFilterRef(row)))
                .build();

        locationForm = new ModelNodeForm.Builder<NamedNode>(Ids.UNDERTOW_HOST_LOCATION_FORM, locationMetadata)
                .onSave((form, changedValues) -> presenter.saveLocation(form, changedValues))
                .prepareReset(form -> presenter.resetLocation(form))
                .build();

        HTMLElement locationSection = section()
                .add(h(1).textContent(Names.LOCATIONS))
                .add(p().textContent(locationMetadata.getDescription().getDescription()))
                .add(locationTable)
                .add(locationForm).element();

        // ------------------------------------------------------ host location filter refs

        Metadata locationFilterRefMetadata = metadataRegistry.lookup(LOCATION_FILTER_REF_TEMPLATE);
        locationFilterRefTable = new ModelNodeTable.Builder<NamedNode>(Ids.UNDERTOW_HOST_LOCATION_FILTER_REF_TABLE,
                locationFilterRefMetadata)
                .button(tableButtonFactory.add(LOCATION_FILTER_REF_TEMPLATE,
                        table -> presenter.addLocationFilterRef()))
                .button(tableButtonFactory.remove(LOCATION_FILTER_REF_TEMPLATE,
                        table -> presenter.removeLocationFilterRef(table.selectedRow().getName())))
                .column(FILTER_REF, resources.constants().filter(), (cell, type, row, meta) -> row.getName())
                .column(PRIORITY)
                .build();

        locationFilterRefForm = new ModelNodeForm.Builder<NamedNode>(Ids.UNDERTOW_HOST_LOCATION_FILTER_REF_FORM,
                locationFilterRefMetadata)
                .onSave((form, changedValues) -> presenter.saveLocationFilterRef(form, changedValues))
                .prepareReset(form -> presenter.resetLocationFilterRef(form))
                .build();

        HTMLElement locationFilterRefSection = section()
                .add(h(1).textContent(Names.FILTERS))
                .add(p().textContent(locationFilterRefMetadata.getDescription().getDescription()))
                .add(locationFilterRefTable)
                .add(locationFilterRefForm).element();

        // ------------------------------------------------------ pages, listener and navigation

        hostPages = new Pages(Ids.UNDERTOW_HOST_PAGES, Ids.UNDERTOW_HOST_MAIN_PAGE, hostSection);
        hostPages.addPage(Ids.UNDERTOW_HOST_MAIN_PAGE, Ids.UNDERTOW_HOST_FILTER_REF_PAGE,
                () -> presenter.hostSegment(), () -> Names.FILTERS, filterRefSection);
        hostPages.addPage(Ids.UNDERTOW_HOST_MAIN_PAGE, Ids.UNDERTOW_HOST_LOCATION_PAGE,
                () -> presenter.hostSegment(), () -> Names.LOCATIONS, locationSection);
        hostPages.addPage(Ids.UNDERTOW_HOST_LOCATION_PAGE, Ids.UNDERTOW_HOST_LOCATION_FILTER_REF_PAGE,
                () -> presenter.locationSegment(), () -> Names.FILTERS, locationFilterRefSection);

        listener = new EnumMap<>(Listener.class);
        listener.put(AJP, new ListenerElement(AJP, metadataRegistry, tableButtonFactory));
        listener.put(HTTP, new ListenerElement(HTTP, metadataRegistry, tableButtonFactory));
        listener.put(HTTPS, new HttpsListenerElement(resources, metadataRegistry, tableButtonFactory));

        navigation = new VerticalNavigation();
        navigation.addPrimary(Ids.UNDERTOW_SERVER_CONFIGURATION_ITEM, Names.CONFIGURATION, pfIcon("settings"),
                configurationSection);
        navigation.addPrimary(Ids.UNDERTOW_HOST_ITEM, Names.HOSTS, pfIcon("enterprise"), hostPages);
        navigation.addPrimary(Ids.UNDERTOW_SERVER_LISTENER_ITEM, Names.LISTENER, fontAwesome("headphones"));
        navigation.addSecondary(Ids.UNDERTOW_SERVER_LISTENER_ITEM, Ids.build(AJP.baseId, Ids.ITEM),
                AJP.type, listener.get(AJP).element());
        navigation.addSecondary(Ids.UNDERTOW_SERVER_LISTENER_ITEM, Ids.build(HTTP.baseId, Ids.ITEM),
                HTTP.type, listener.get(HTTP).element());
        navigation.addSecondary(Ids.UNDERTOW_SERVER_LISTENER_ITEM, Ids.build(HTTPS.baseId, Ids.ITEM),
                HTTPS.type, listener.get(HTTPS).element());

        registerAttachable(navigation,
                configurationForm,
                hostTable, hostForm,
                filterRefTable, filterRefForm,
                locationTable, locationForm,
                locationFilterRefTable, locationFilterRefForm);
        registerAttachables(hostSettingForms.values());
        listener.values().forEach(element -> registerAttachable(element));

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
    }

    private Form<ModelNode> hostSetting(HostSetting hostSetting) {
        Metadata metadata = metadataRegistry.lookup(HOST_TEMPLATE.append(hostSetting.templateSuffix()));
        return new ModelNodeForm.Builder<>(Ids.build(hostSetting.baseId, Ids.FORM), metadata)
                .singleton(() -> presenter.hostSettingOperation(hostSetting),
                        () -> presenter.addHostSetting(hostSetting))
                .onSave((f, changedValues) -> presenter.saveHostSetting(hostSetting, changedValues))
                .prepareReset(f -> presenter.resetHostSetting(hostSetting, f))
                .prepareRemove(f -> presenter.removeHostSetting(hostSetting, f))
                .build();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void attach() {
        super.attach();
        hostTable.bindForm(hostForm);
        hostTable.onSelectionChange(t -> {
            if (t.hasSelection()) {
                presenter.selectHost(t.selectedRow().getName());
                for (HostSetting setting : HostSetting.values()) {
                    // Skip console access log until HAL-1600 has been implemented
                    if (setting == HostSetting.CONSOLE_ACCESS_LOG) {
                        continue;
                    }
                    hostSettingForms.get(setting).view(failSafeGet(t.selectedRow(), setting.path()));
                }
            } else {
                presenter.selectHost(null);
                hostSettingForms.values().forEach(Form::clear);
            }
        });

        filterRefTable.bindForm(filterRefForm);
        locationTable.bindForm(locationForm);
        locationFilterRefTable.bindForm(locationFilterRefForm);
    }

    @Override
    public void setPresenter(ServerPresenter presenter) {
        this.presenter = presenter;
        this.listener.values().forEach(l -> l.setPresenter(presenter));

        // register suggest handlers here; they need a valid presenter reference (which is n/a in constructor)
        configurationForm.getFormItem(DEFAULT_HOST).registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, presenter.getStatementContext(),
                        SELECTED_SERVER_TEMPLATE.append(HOST + "=*")));
        configurationForm.getFormItem(SERVLET_CONTAINER).registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, presenter.getStatementContext(), SERVLET_CONTAINER_TEMPLATE));
        locationForm.getFormItem(HANDLER).registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, presenter.getStatementContext(), HANDLER_SUGGESTIONS));
    }

    @Override
    public void update(ModelNode payload) {
        configurationForm.view(payload);

        hostForm.clear();
        hostTable.update(asNamedNodes(failSafePropertyList(payload, HOST)));

        listener.forEach((l, e) -> {
            List<NamedNode> items = asNamedNodes(failSafePropertyList(payload, l.resource));
            e.update(items);
            navigation.updateBadge(Ids.build(l.baseId, Ids.ITEM), items.size());
        });
    }

    @Override
    public void updateFilterRef(List<NamedNode> filters, boolean showPage) {
        filterRefForm.clear();
        filterRefTable.update(filters);
        if (showPage) {
            hostPages.showPage(Ids.UNDERTOW_HOST_FILTER_REF_PAGE);
        }
    }

    @Override
    public void updateLocation(List<NamedNode> locations, boolean showPage) {
        locationForm.clear();
        locationTable.update(locations);
        if (showPage) {
            hostPages.showPage(Ids.UNDERTOW_HOST_LOCATION_PAGE);
        }
    }

    @Override
    public void updateLocationFilterRef(List<NamedNode> filters, boolean showPage) {
        locationFilterRefForm.clear();
        locationFilterRefTable.update(filters);
        if (showPage) {
            hostPages.showPage(Ids.UNDERTOW_HOST_LOCATION_FILTER_REF_PAGE);
        }
    }
}
