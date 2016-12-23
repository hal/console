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
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.ColumnBuilder;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.core.mbui.form.FailSafeForm;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.ballroom.table.Button.Scope.SELECTED;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.*;
import static org.jboss.hal.client.configuration.subsystem.undertow.Listener.AJP;
import static org.jboss.hal.client.configuration.subsystem.undertow.Listener.HTTP;
import static org.jboss.hal.client.configuration.subsystem.undertow.Listener.HTTPS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.columnAction;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;

/**
 * @author Harald Pehl
 */
public class ServerView extends HalViewImpl implements ServerPresenter.MyView {

    private final Dispatcher dispatcher;
    private final MetadataRegistry metadataRegistry;
    private final Form<ModelNode> configurationForm;
    private final NamedNodeTable<NamedNode> hostTable;
    private final Form<NamedNode> hostForm;
    private final FailSafeForm<ModelNode> accessLogForm;
    private final FailSafeForm<ModelNode> singleSignOnForm;
    private final NamedNodeTable<NamedNode> filterRefTable;
    private final Form<NamedNode> filterRefForm;
    private final NamedNodeTable<NamedNode> locationTable;
    private final Form<NamedNode> locationForm;
    private final NamedNodeTable<NamedNode> locationFilterRefTable;
    private final Form<NamedNode> locationFilterRefForm;
    private final Pages hostPages;
    private final Map<Listener, ListenerElement> listener;
    private final VerticalNavigation navigation;
    private ServerPresenter presenter;

    @Inject
    @SuppressWarnings({"ConstantConditions", "HardCodedStringLiteral"})
    public ServerView(final Dispatcher dispatcher, final MetadataRegistry metadataRegistry, final Resources resources) {
        this.dispatcher = dispatcher;
        this.metadataRegistry = metadataRegistry;

        // ------------------------------------------------------ server configuration

        Metadata configurationMetadata = metadataRegistry.lookup(SERVER_TEMPLATE);
        configurationForm = new ModelNodeForm.Builder<>(Ids.UNDERTOW_SERVER_CONFIGURATION_FORM, configurationMetadata)
                .onSave((form, changedValues) -> presenter.saveServer(changedValues))
                .build();

        // @formatter:off
        Element configurationSection = new Elements.Builder()
            .section()
                .h(1).textContent(Names.CONFIGURATION).end()
                .p().textContent(configurationMetadata.getDescription().getDescription()).end()
                .add(configurationForm)
            .end()
        .build();
        // @formatter:on

        // ------------------------------------------------------ hosts

        Metadata hostMetadata = metadataRegistry.lookup(HOST_TEMPLATE);
        Options<NamedNode> hostOptions = new NamedNodeTable.Builder<>(hostMetadata)
                .button(resources.constants().add(), (event, api) -> presenter.addHost())
                .button(resources.constants().remove(), SELECTED,
                        (event, api) -> presenter.removeHost(api.selectedRow().getName()))
                .column(Names.NAME, (cell, type, row, meta) -> row.getName())
                .column(columnActions -> new ColumnBuilder<NamedNode>(Ids.UNDERTOW_HOST_ACTION_COLUMN,
                        resources.constants().references(),
                        (cell, t, row, meta) -> {
                            String id1 = Ids.uniqueId();
                            String id2 = Ids.uniqueId();
                            columnActions.add(id1, row1 -> presenter.showFilterRef(row1));
                            columnActions.add(id2, row2 -> presenter.showLocation(row2));
                            return "<a id=\"" + id1 + "\" class=\"" + columnAction + "\">" + Names.FILTERS + "</a> / " +
                                    "<a id=\"" + id2 + "\" class=\"" + columnAction + "\">" + Names.LOCATIONS + "</a>";
                        })
                        .orderable(false)
                        .searchable(false)
                        .width("13em")
                        .build())
                .build();
        hostTable = new NamedNodeTable<>(Ids.UNDERTOW_HOST_TABLE, hostOptions);

        hostForm = new ModelNodeForm.Builder<NamedNode>(Ids.UNDERTOW_HOST_ATTRIBUTES_FORM, hostMetadata)
                .onSave((form, changedValues) -> presenter.saveHost(form.getModel().getName(), changedValues))
                .build();

        accessLogForm = hostSetting(HostSetting.ACCESS_LOG);
        singleSignOnForm = hostSetting(HostSetting.SINGLE_SIGN_ON);

        Tabs tabs = new Tabs();
        tabs.add(Ids.UNDERTOW_HOST_ATTRIBUTES_TAB, resources.constants().attributes(), hostForm.asElement());
        tabs.add(Ids.build(HostSetting.ACCESS_LOG.baseId, Ids.TAB_SUFFIX), HostSetting.ACCESS_LOG.type,
                accessLogForm.asElement());
        tabs.add(Ids.build(HostSetting.SINGLE_SIGN_ON.baseId, Ids.TAB_SUFFIX), HostSetting.SINGLE_SIGN_ON.type,
                singleSignOnForm.asElement());

        // @formatter:off
        Element hostSection = new Elements.Builder()
            .section()
                .h(1).textContent(Names.HOSTS).end()
                .p().textContent(hostMetadata.getDescription().getDescription()).end()
                .add(hostTable)
                .add(tabs)
            .end()
        .build();
        // @formatter:on

        // ------------------------------------------------------ host filter refs

        Metadata filterRefMetadata = metadataRegistry.lookup(FILTER_REF_TEMPLATE);
        Options<NamedNode> filterRefOptions = new NamedNodeTable.Builder<>(filterRefMetadata)
                .button(resources.constants().add(), (event, api) -> presenter.addFilterRef())
                .button(resources.constants().remove(), SELECTED,
                        (event, api) -> presenter.removeFilterRef(api.selectedRow().getName()))
                .column(FILTER_REF, Names.FILTER, (cell, type, row, meta) -> row.getName())
                .column(PRIORITY)
                .build();
        filterRefTable = new NamedNodeTable<>(Ids.UNDERTOW_HOST_FILTER_REF_TABLE, filterRefOptions);

        filterRefForm = new ModelNodeForm.Builder<NamedNode>(Ids.UNDERTOW_HOST_FILTER_REF_FORM, filterRefMetadata)
                .onSave((form, changedValues) -> presenter.saveFilterRef(form, changedValues))
                .build();

        // @formatter:off
        Element filterRefSection = new Elements.Builder()
            .section()
                .h(1).textContent(Names.FILTERS).end()
                .p().textContent(filterRefMetadata.getDescription().getDescription()).end()
                .add(filterRefTable)
                .add(filterRefForm)
            .end()
        .build();
        // @formatter:on

        // ------------------------------------------------------ host locations

        Metadata locationMetadata = metadataRegistry.lookup(LOCATION_TEMPLATE);
        Options<NamedNode> locationOptions = new NamedNodeTable.Builder<>(locationMetadata)
                .button(resources.constants().add(), (event, api) -> presenter.addLocation())
                .button(resources.constants().remove(), SELECTED,
                        (event, api) -> presenter.removeLocation(api.selectedRow().getName()))
                .column(LOCATION, Names.LOCATION, (cell, type, row, meta) -> row.getName())
                .column(HANDLER)
                .column(Names.FILTERS, row -> presenter.showLocationFilterRef(row))
                .build();
        locationTable = new NamedNodeTable<>(Ids.UNDERTOW_HOST_LOCATION_TABLE, locationOptions);

        locationForm = new ModelNodeForm.Builder<NamedNode>(Ids.UNDERTOW_HOST_LOCATION_FORM, locationMetadata)
                .onSave((form, changedValues) -> presenter.saveLocation(form, changedValues))
                .build();

        // @formatter:off
        Element locationSection = new Elements.Builder()
            .section()
                .h(1).textContent(Names.LOCATIONS).end()
                .p().textContent(locationMetadata.getDescription().getDescription()).end()
                .add(locationTable)
                .add(locationForm)
            .end()
        .build();
        // @formatter:on

        // ------------------------------------------------------ host location filter refs

        Metadata locationFilterRefMetadata = metadataRegistry.lookup(LOCATION_FILTER_REF_TEMPLATE);
        Options<NamedNode> locationFilterRefOptions = new NamedNodeTable.Builder<>(locationFilterRefMetadata)
                .button(resources.constants().add(), (event, api) -> presenter.addLocationFilterRef())
                .button(resources.constants().remove(), SELECTED,
                        (event, api) -> presenter.removeLocationFilterRef(api.selectedRow().getName()))
                .column(FILTER_REF, Names.FILTER, (cell, type, row, meta) -> row.getName())
                .column(PRIORITY)
                .build();
        locationFilterRefTable = new NamedNodeTable<>(Ids.UNDERTOW_HOST_LOCATION_FILTER_REF_TABLE,
                locationFilterRefOptions);

        locationFilterRefForm = new ModelNodeForm.Builder<NamedNode>(Ids.UNDERTOW_HOST_LOCATION_FILTER_REF_FORM,
                locationFilterRefMetadata)
                .onSave((form, changedValues) -> presenter.saveLocationFilterRef(form, changedValues))
                .build();

        // @formatter:off
        Element locationFilterRefSection = new Elements.Builder()
            .section()
                .h(1).textContent(Names.FILTERS).end()
                .p().textContent(locationFilterRefMetadata.getDescription().getDescription()).end()
                .add(locationFilterRefTable)
                .add(locationFilterRefForm)
            .end()
        .build();
        // @formatter:on

        // ------------------------------------------------------ pages, listener and navigation

        hostPages = new Pages(Ids.UNDERTOW_HOST_MAIN_PAGE, hostSection);
        hostPages.addPage(Ids.UNDERTOW_HOST_MAIN_PAGE, Ids.UNDERTOW_HOST_FILTER_REF_PAGE,
                () -> presenter.hostSegment(), () -> Names.FILTERS, filterRefSection);
        hostPages.addPage(Ids.UNDERTOW_HOST_MAIN_PAGE, Ids.UNDERTOW_HOST_LOCATION_PAGE,
                () -> presenter.hostSegment(), () -> Names.LOCATIONS, locationSection);
        hostPages.addPage(Ids.UNDERTOW_HOST_LOCATION_PAGE, Ids.UNDERTOW_HOST_LOCATION_FILTER_REF_PAGE,
                () -> presenter.locationSegment(), () -> Names.FILTERS, locationFilterRefSection);

        listener = new EnumMap<>(Listener.class);
        listener.put(AJP, new ListenerElement(AJP, metadataRegistry, resources));
        listener.put(HTTP, new ListenerElement(HTTP, metadataRegistry, resources));
        listener.put(HTTPS, new ListenerElement(HTTPS, metadataRegistry, resources));

        navigation = new VerticalNavigation();
        navigation.addPrimary(Ids.UNDERTOW_SERVER_CONFIGURATION_ENTRY, Names.CONFIGURATION, pfIcon("settings"),
                configurationSection);
        navigation.addPrimary(Ids.UNDERTOW_HOST_ENTRY, Names.HOSTS, pfIcon("enterprise"), hostPages);
        navigation.addPrimary(Ids.UNDERTOW_SERVER_LISTENER_ENTRY, Names.LISTENER, fontAwesome("headphones"));
        navigation.addSecondary(Ids.UNDERTOW_SERVER_LISTENER_ENTRY, Ids.build(AJP.baseId, Ids.ENTRY_SUFFIX),
                AJP.type, listener.get(AJP).asElement());
        navigation.addSecondary(Ids.UNDERTOW_SERVER_LISTENER_ENTRY, Ids.build(HTTP.baseId, Ids.ENTRY_SUFFIX),
                HTTP.type, listener.get(HTTP).asElement());
        navigation.addSecondary(Ids.UNDERTOW_SERVER_LISTENER_ENTRY, Ids.build(HTTPS.baseId, Ids.ENTRY_SUFFIX),
                HTTPS.type, listener.get(HTTPS).asElement());

        registerAttachable(navigation,
                configurationForm,
                hostTable, hostForm,
                filterRefTable, filterRefForm,
                locationTable, locationForm,
                locationFilterRefTable, locationFilterRefForm,
                accessLogForm, singleSignOnForm);
        listener.values().forEach(element -> registerAttachable(element));

        LayoutBuilder layoutBuilder = new LayoutBuilder()
                .row()
                .column()
                .addAll(navigation.panes())
                .end()
                .end();
        Element root = layoutBuilder.build();
        initElement(root);
    }

    private FailSafeForm<ModelNode> hostSetting(final HostSetting hostSetting) {
        Metadata metadata = metadataRegistry.lookup(HOST_TEMPLATE.append(hostSetting.templateSuffix()));
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(hostSetting.baseId, Ids.FORM_SUFFIX), metadata)
                .onSave((f, changedValues) -> presenter.saveHostSetting(hostSetting, changedValues))
                .build();
        return new FailSafeForm<>(dispatcher, () -> presenter.hostSettingOperation(hostSetting), form,
                () -> presenter.addHostSetting(hostSetting));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void attach() {
        super.attach();
        hostTable.bindForm(hostForm);
        hostTable.api().onSelectionChange(api -> {
            if (api.hasSelection()) {
                presenter.selectHost(api.selectedRow().getName());
                accessLogForm.view(failSafeGet(api.selectedRow(), HostSetting.ACCESS_LOG.path()));
                singleSignOnForm.view(failSafeGet(api.selectedRow(), HostSetting.SINGLE_SIGN_ON.path()));
            } else {
                presenter.selectHost(null);
                accessLogForm.clear();
                singleSignOnForm.clear();
            }
        });

        filterRefTable.bindForm(filterRefForm);
        locationTable.bindForm(locationForm);
        locationFilterRefTable.bindForm(locationFilterRefForm);
    }

    @Override
    public void setPresenter(final ServerPresenter presenter) {
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
    public void update(final ModelNode payload) {
        configurationForm.view(payload);

        hostForm.clear();
        hostTable.update(asNamedNodes(failSafePropertyList(payload, HOST)));

        listener.forEach((l, e) -> {
            List<NamedNode> items = asNamedNodes(failSafePropertyList(payload, l.resource));
            e.update(items);
            navigation.updateBadge(Ids.build(l.baseId, Ids.ENTRY_SUFFIX), items.size());
        });
    }

    @Override
    public void updateFilterRef(final List<NamedNode> filters) {
        filterRefForm.clear();
        filterRefTable.update(filters);
        hostPages.showPage(Ids.UNDERTOW_HOST_FILTER_REF_PAGE);
    }

    @Override
    public void updateLocation(final List<NamedNode> locations) {
        locationForm.clear();
        locationTable.update(locations);
        hostPages.showPage(Ids.UNDERTOW_HOST_LOCATION_PAGE);
    }

    @Override
    public void updateLocationFilterRef(final List<NamedNode> filters) {
        locationFilterRefForm.clear();
        locationFilterRefTable.update(filters);
        hostPages.showPage(Ids.UNDERTOW_HOST_LOCATION_FILTER_REF_PAGE);
    }
}
