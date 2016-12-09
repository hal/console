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
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.ballroom.table.Button.Scope.SELECTED;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.HOST_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SERVER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.Listener.AJP;
import static org.jboss.hal.client.configuration.subsystem.undertow.Listener.HTTP;
import static org.jboss.hal.client.configuration.subsystem.undertow.Listener.HTTPS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;

/**
 * @author Harald Pehl
 */
public class ServerView extends HalViewImpl implements ServerPresenter.MyView {

    private final Form<ModelNode> configurationForm;
    private final NamedNodeTable<NamedNode> hostTable;
    private final Form<NamedNode> hostForm;
    private final Map<Listener, ListenerElement> listener;
    private final VerticalNavigation navigation;
    private ServerPresenter presenter;

    @Inject
    @SuppressWarnings("ConstantConditions")
    public ServerView(final MetadataRegistry metadataRegistry, final Resources resources) {
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

        Metadata hostMetadata = metadataRegistry.lookup(HOST_TEMPLATE);
        Options<NamedNode> hostOptions = new NamedNodeTable.Builder<>(hostMetadata)
                .button(resources.constants().add(), (event, api) -> presenter.addHost())
                .button(resources.constants().remove(), SELECTED,
                        (event, api) -> presenter.removeHost(api.selectedRow().getName()))
                .column(Names.NAME, (cell, type, row, meta) -> row.getName())
                .build();
        hostTable = new NamedNodeTable<>(Ids.UNDERTOW_HOST_TABLE, hostOptions);

        hostForm = new ModelNodeForm.Builder<NamedNode>(Ids.UNDERTOW_HOST_FORM, hostMetadata)
                .onSave((form, changedValues) -> presenter.saveHost(form.getModel().getName(), changedValues))
                .build();

        // @formatter:off
        Element hostSection = new Elements.Builder()
            .section()
                .h(1).textContent(Names.HOSTS).end()
                .p().textContent(hostMetadata.getDescription().getDescription()).end()
                .add(hostTable)
                .add(hostForm)
            .end()
        .build();
        // @formatter:on

        listener = new EnumMap<>(Listener.class);
        listener.put(AJP, new ListenerElement(AJP, metadataRegistry, resources));
        listener.put(HTTP, new ListenerElement(HTTP, metadataRegistry, resources));
        listener.put(HTTPS, new ListenerElement(HTTPS, metadataRegistry, resources));

        navigation = new VerticalNavigation();
        navigation.addPrimary(Ids.UNDERTOW_SERVER_CONFIGURATION_ENTRY, Names.CONFIGURATION, pfIcon("settings"),
                configurationSection);
        navigation.addPrimary(Ids.UNDERTOW_HOST_ENTRY, Names.HOSTS, pfIcon("enterprise"), hostSection);
        navigation.addPrimary(Ids.UNDERTOW_SERVER_LISTENER_ENTRY, Names.LISTENER, fontAwesome("headphones"));
        navigation.addSecondary(Ids.UNDERTOW_SERVER_LISTENER_ENTRY, Ids.build(AJP.baseId, Ids.ENTRY_SUFFIX),
                AJP.type, listener.get(AJP).asElement());
        navigation.addSecondary(Ids.UNDERTOW_SERVER_LISTENER_ENTRY, Ids.build(HTTP.baseId, Ids.ENTRY_SUFFIX),
                HTTP.type, listener.get(HTTP).asElement());
        navigation.addSecondary(Ids.UNDERTOW_SERVER_LISTENER_ENTRY, Ids.build(HTTPS.baseId, Ids.ENTRY_SUFFIX),
                HTTPS.type, listener.get(HTTPS).asElement());

        registerAttachable(navigation, configurationForm, hostTable, hostForm);
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

    @Override
    public void attach() {
        super.attach();
        hostTable.bindForm(hostForm);
    }

    @Override
    public void setPresenter(final ServerPresenter presenter) {
        this.presenter = presenter;
        this.listener.values().forEach(l -> l.setPresenter(presenter));
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
}
