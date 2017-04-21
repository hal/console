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
package org.jboss.hal.client.configuration.subsystem.jgroups;

import java.util.List;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.table.ColumnBuilder;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.resources.CSS.columnAction;

/**
 * Element to configure the stack resource
 *
 * @author Claudio Miranda
 */
class StackElement implements IsElement, Attachable, HasPresenter<JGroupsPresenter> {

    static final String REMOTE_SITE_ID = Ids.build(Ids.JGROUPS_REMOTE_SITE, Ids.PAGE_SUFFIX);
    private static final String STACK_ID = Ids.build(Ids.JGROUPS_STACK_CONFIG, Ids.PAGE_SUFFIX);
    private static final String RELAY_ID = Ids.build(Ids.JGROUPS_RELAY, Ids.PAGE_SUFFIX);
    private static final String PROTOCOL_ID = Ids.build(Ids.JGROUPS_PROTOCOL, Ids.PAGE_SUFFIX);
    private static final String TRANSPORT_ID = Ids.build(Ids.JGROUPS_TRANSPORT, Ids.PAGE_SUFFIX);

    private final Pages innerPages;
    private final NamedNodeTable<NamedNode> table;
    private JGroupsPresenter presenter;
    private String selectedStack;

    private final RelayElement relayElement;
    private final GenericElement remoteSiteElement;
    private final GenericElement protocolElement;
    private final TransportElement transportElement;

    @SuppressWarnings({"ConstantConditions", "HardCodedStringLiteral"})
    StackElement(final MetadataRegistry metadataRegistry, final TableButtonFactory tableButtonFactory,
            final Resources resources) {

        Metadata metadata = metadataRegistry.lookup(STACK_TEMPLATE);
        Options<NamedNode> options = new ModelNodeTable.Builder<NamedNode>(metadata)
                .button(tableButtonFactory.add(STACK_TEMPLATE, (event, api) -> presenter.addStack()))
                //presenter.addResourceDialog(STACK_TEMPLATE, Ids.build(Ids.JGROUPS_STACK_CONFIG, Ids.ADD_SUFFIX),
                //        Names.STACK))
                .button(tableButtonFactory.remove(STACK_TEMPLATE,
                        (event, api) -> presenter.removeResource(STACK_TEMPLATE, api.selectedRow().getName(),
                                Names.STACK)))
                .column(NAME, (cell, t, row, meta) -> row.getName())
                .column(columnActions -> new ColumnBuilder<NamedNode>(Ids.JGROUPS_STACK_COLUMN,
                        "Action",
                        (cell, t, row, meta) -> {
                            String id1 = Ids.uniqueId();
                            String id2 = Ids.uniqueId();
                            String id3 = Ids.uniqueId();
                            columnActions.add(id1, row1 -> {
                                selectedStack = row1.getName();
                                presenter.showRelays(row1);
                                presenter.showStackInnerPage(RELAY_ID);
                            });
                            columnActions.add(id2, row2 -> {
                                selectedStack = row2.getName();
                                presenter.showProtocols(row2);
                                presenter.showStackInnerPage(PROTOCOL_ID);
                            });
                            columnActions.add(id3, row3 -> {
                                selectedStack = row3.getName();
                                presenter.showTransports(row3);
                                presenter.showStackInnerPage(TRANSPORT_ID);
                            });

                            return "<a id=\"" + id1 + "\" class=\"" + columnAction + "\">Relays</a> / " +
                                    "<a id=\"" + id2 + "\" class=\"" + columnAction + "\">Protocol</a> / " +
                                    "<a id=\"" + id3 + "\" class=\"" + columnAction + "\">Transport</a>";
                        })
                        .orderable(false)
                        .searchable(false)
                        .width("18em")
                        .build())
                .build();
        table = new NamedNodeTable<>(Ids.build(Ids.JGROUPS_STACK_CONFIG, Ids.TABLE_SUFFIX), metadata, options);

        // @formatter:off
        Element section = new Elements.Builder()
            .section()
                .h(1).textContent(Names.STACK).end()
                .p().textContent(metadata.getDescription().getDescription()).end()
                .add(table)
            .end()
        .build();
        // @formatter:on

        relayElement = new RelayElement(metadataRegistry, tableButtonFactory, resources);
        remoteSiteElement = new GenericElement(metadataRegistry.lookup(REMOTE_SITE_TEMPLATE), tableButtonFactory,
                resources, SELECTED_REMOTE_SITE_TEMPLATE, Names.REMOTE_SITE, Ids.JGROUPS_REMOTE_SITE);
        protocolElement = new GenericElement(metadataRegistry.lookup(PROTOCOL_TEMPLATE), tableButtonFactory,
                resources, SELECTED_PROTOCOL_TEMPLATE, Names.PROTOCOL, Ids.JGROUPS_PROTOCOL);
        transportElement = new TransportElement(metadataRegistry, tableButtonFactory,
                metadataRegistry.lookup(TRANSPORT_TEMPLATE), resources, SELECTED_TRANSPORT_TEMPLATE, Names.TRANSPORT,
                Ids.JGROUPS_TRANSPORT);

        innerPages = new Pages(STACK_ID, section);
        // Relay page
        innerPages.addPage(STACK_ID, RELAY_ID,
                () -> Names.STACK + ": " + selectedStack,
                () -> Names.RELAY,
                relayElement);
        // relay -> remote site page
        innerPages.addPage(RELAY_ID, REMOTE_SITE_ID,
                () -> Names.RELAY + ": relay.RELAY2: ",
                () -> Names.REMOTE_SITE,
                remoteSiteElement);
        // protocol page
        innerPages.addPage(STACK_ID, PROTOCOL_ID,
                () -> Names.STACK + ": " + selectedStack,
                () -> Names.PROTOCOL,
                protocolElement);
        // transport page
        innerPages.addPage(STACK_ID, TRANSPORT_ID,
                () -> Names.STACK + ": " + selectedStack,
                () -> Names.TRANSPORT,
                transportElement);
    }

    @Override
    public Element asElement() {
        return innerPages.asElement();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void attach() {
        table.attach();
        relayElement.attach();
        remoteSiteElement.attach();
        protocolElement.attach();
        transportElement.attach();
    }

    @Override
    public void detach() {
        relayElement.detach();
        table.detach();
        remoteSiteElement.detach();
        protocolElement.detach();
        transportElement.detach();
    }

    @Override
    public void setPresenter(final JGroupsPresenter presenter) {
        this.presenter = presenter;
        relayElement.setPresenter(presenter);
        remoteSiteElement.setPresenter(presenter);
        protocolElement.setPresenter(presenter);
        transportElement.setPresenter(presenter);
    }

    void update(List<NamedNode> models) {
        table.update(models);
    }

    void updateRelays(List<NamedNode> node) {
        relayElement.update(node);
    }

    void updateProtocol(List<NamedNode> node) {
        protocolElement.update(node);
    }

    void updateRemoteSite(final List<NamedNode> model) {
        remoteSiteElement.update(model);
    }

    void updateTransport(final List<NamedNode> model) {
        transportElement.update(model);
    }

    void showInnerPage(final String id) {
        innerPages.showPage(id);
    }
}
