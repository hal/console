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
package org.jboss.hal.client.configuration.subsystem.jgroups;

import java.util.ArrayList;
import java.util.List;

import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.InlineAction;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/** Element to configure the stack resource */
class StackElement implements IsElement<HTMLElement>, Attachable, HasPresenter<JGroupsPresenter> {

    static final String REMOTE_SITE_ID = Ids.build(Ids.JGROUPS_REMOTE_SITE, Ids.PAGE);
    private static final String STACK_ID = Ids.build(Ids.JGROUPS_STACK_CONFIG, Ids.PAGE);
    private static final String RELAY_ID = Ids.build(Ids.JGROUPS_RELAY, Ids.PAGE);
    private static final String PROTOCOL_ID = Ids.build(Ids.JGROUPS_PROTOCOL, Ids.PAGE);
    private static final String TRANSPORT_ID = Ids.build(Ids.JGROUPS_TRANSPORT, Ids.PAGE);

    private final Pages innerPages;
    private final Table<NamedNode> table;
    private final Form<NamedNode> form;
    private JGroupsPresenter presenter;
    private String selectedStack;

    private final RelayElement relayElement;
    private final GenericElement remoteSiteElement;
    private final ProtocolElement protocolElement;
    private final TransportElement transportElement;

    @SuppressWarnings({ "ConstantConditions", "HardCodedStringLiteral" })
    StackElement(MetadataRegistry metadataRegistry, TableButtonFactory tableButtonFactory,
            Resources resources) {

        List<InlineAction<NamedNode>> inlineActions = new ArrayList<>();
        inlineActions.add(new InlineAction<>(Names.RELAY, row -> {
            selectedStack = row.getName();
            presenter.showRelays(row);
            presenter.showStackInnerPage(RELAY_ID);
        }));
        inlineActions.add(new InlineAction<>(Names.PROTOCOL, row -> {
            selectedStack = row.getName();
            presenter.showProtocols(row);
            presenter.showStackInnerPage(PROTOCOL_ID);
        }));
        inlineActions.add(new InlineAction<>(Names.TRANSPORT, row -> {
            selectedStack = row.getName();
            presenter.showTransports(row);
            presenter.showStackInnerPage(TRANSPORT_ID);
        }));

        Metadata metadata = metadataRegistry.lookup(STACK_TEMPLATE);
        table = new ModelNodeTable.Builder<NamedNode>(Ids.build(Ids.JGROUPS_STACK_CONFIG, Ids.TABLE), metadata)
                .button(tableButtonFactory.add(STACK_TEMPLATE, table -> presenter.addStack()))
                .button(tableButtonFactory.remove(STACK_TEMPLATE,
                        table -> presenter.removeResource(STACK_TEMPLATE, table.selectedRow().getName(),
                                Names.STACK)))
                .column(NAME, (cell, t, row, meta) -> row.getName())
                .column(inlineActions)
                .build();

        form = new ModelNodeForm.Builder<NamedNode>(Ids.build(Ids.JGROUPS_STACK_CONFIG, Ids.FORM), metadata)
                .onSave((form, changedValues) -> presenter
                        .saveResource(STACK_TEMPLATE, table.selectedRow().getName(), changedValues, metadata,
                                resources.messages().modifySingleResourceSuccess(Names.STACK)))
                .prepareReset(form -> presenter.resetResource(STACK_TEMPLATE, table.selectedRow().getName(),
                        Names.STACK, form, metadata))
                .build();

        HTMLElement section = section()
                .add(h(1).textContent(Names.STACK))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(table)
                .add(form).element();

        relayElement = new RelayElement(metadataRegistry, tableButtonFactory, resources);
        remoteSiteElement = new GenericElement(metadataRegistry.lookup(REMOTE_SITE_TEMPLATE), tableButtonFactory,
                resources, SELECTED_REMOTE_SITE_TEMPLATE, Names.REMOTE_SITE, Ids.JGROUPS_REMOTE_SITE);
        protocolElement = new ProtocolElement(metadataRegistry.lookup(PROTOCOL_TEMPLATE), tableButtonFactory,
                resources, SELECTED_PROTOCOL_TEMPLATE, Names.PROTOCOL, Ids.JGROUPS_PROTOCOL);
        transportElement = new TransportElement(metadataRegistry, resources);

        innerPages = new Pages(Ids.build(Ids.JGROUPS_STACK_CONFIG, Ids.PAGES), STACK_ID, section);
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
    public HTMLElement element() {
        return innerPages.element();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void attach() {
        table.attach();
        form.attach();
        table.bindForm(form);
        relayElement.attach();
        remoteSiteElement.attach();
        protocolElement.attach();
        transportElement.attach();
    }

    @Override
    public void detach() {
        relayElement.detach();
        table.detach();
        form.detach();
        remoteSiteElement.detach();
        protocolElement.detach();
        transportElement.detach();
    }

    @Override
    public void setPresenter(JGroupsPresenter presenter) {
        this.presenter = presenter;
        relayElement.setPresenter(presenter);
        remoteSiteElement.setPresenter(presenter);
        protocolElement.setPresenter(presenter);
        transportElement.setPresenter(presenter);
    }

    void update(List<NamedNode> models) {
        table.update(models);
        form.clear();
    }

    void updateRelays(List<NamedNode> node) {
        relayElement.update(node);
    }

    void updateProtocol(List<NamedNode> node) {
        protocolElement.update(node);
    }

    void updateRemoteSite(List<NamedNode> model) {
        remoteSiteElement.update(model);
    }

    void updateTransport(List<NamedNode> model) {
        transportElement.update(model);
    }

    void showInnerPage(String id) {
        innerPages.showPage(id);
    }
}
