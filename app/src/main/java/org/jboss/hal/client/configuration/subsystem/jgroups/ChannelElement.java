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
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Button;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.CHANNEL_FORK_PROTOCOL_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.CHANNEL_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.SELECTED_CHANNEL_FORK_PROTOCOL_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * Element to configure the fork resource
 *
 * @author Claudio Miranda
 */
class ChannelElement implements IsElement, Attachable, HasPresenter<JGroupsPresenter> {

    static final String PROTOCOL_ID = Ids.build(Ids.JGROUPS_CHANNEL_FORK_PROTOCOL, Ids.PAGE_SUFFIX);
    private static final String CHANNEL_ID = Ids.build(Ids.JGROUPS_CHANNEL_CONFIG, Ids.PAGE_SUFFIX);
    private static final String FORK_ID = Ids.build(Ids.JGROUPS_RELAY, Ids.PAGE_SUFFIX);

    private final Pages innerPages;
    private final NamedNodeTable<NamedNode> table;
    private final Form<NamedNode> form;
    private JGroupsPresenter presenter;
    private String selectedChannel;

    private final ForkElement forkElement;
    private final GenericElement protocolElement;

    @SuppressWarnings({"ConstantConditions", "HardCodedStringLiteral"})
    ChannelElement(final MetadataRegistry metadataRegistry, final Resources resources) {

        Metadata metadata = metadataRegistry.lookup(CHANNEL_TEMPLATE);
        Options<NamedNode> options = new ModelNodeTable.Builder<NamedNode>(metadata)
                .button(resources.constants().add(), (event, api) -> presenter.addResourceDialog(CHANNEL_TEMPLATE,
                        Ids.build(Ids.JGROUPS_CHANNEL_CONFIG, Ids.ADD_SUFFIX), Names.CHANNEL))
                .button(resources.constants().remove(), Button.Scope.SELECTED,
                        (event, api) -> presenter
                                .removeResource(CHANNEL_TEMPLATE, api.selectedRow().getName(), Names.CHANNEL))
                .column(NAME, (cell, t, row, meta) -> row.getName())
                .column("Forks", row -> {
                    selectedChannel = row.getName();
                    presenter.showForks(row);
                    presenter.showChannelInnerPage(FORK_ID);
                })
                .build();
        table = new NamedNodeTable<>(Ids.build(Ids.JGROUPS_CHANNEL_CONFIG, Ids.TABLE_SUFFIX), options);
        form = new ModelNodeForm.Builder<NamedNode>(Ids.build(Ids.JGROUPS_CHANNEL_CONFIG, Ids.FORM_SUFFIX), metadata)
                .onSave((form, changedValues) -> presenter
                        .saveResource(CHANNEL_TEMPLATE, table.api().selectedRow().getName(), changedValues, metadata,
                                resources.messages().modifySingleResourceSuccess(Names.CHANNEL)))
                .prepareReset(form -> presenter.resetResource(CHANNEL_TEMPLATE, Names.CHANNEL,
                        table.api().selectedRow().getName(), form, metadata))
                .build();

        // @formatter:off
        Element section = new Elements.Builder()
            .section()
                .h(1).textContent(Names.CHANNEL).end()
                .p().textContent(metadata.getDescription().getDescription()).end()
                .add(table)
                .add(form)
            .end()
        .build();
        // @formatter:on

        forkElement = new ForkElement(metadataRegistry, resources);
        protocolElement = new GenericElement(metadataRegistry.lookup(CHANNEL_FORK_PROTOCOL_TEMPLATE), resources,
                SELECTED_CHANNEL_FORK_PROTOCOL_TEMPLATE, Names.PROTOCOL, Ids.JGROUPS_CHANNEL_FORK_PROTOCOL);

        innerPages = new Pages(CHANNEL_ID, section);
        // Fork page
        innerPages.addPage(CHANNEL_ID, FORK_ID,
                () -> Names.CHANNEL + ": " + selectedChannel,
                () -> Names.FORK,
                forkElement);
        // Fork / Protocol
        innerPages.addPage(FORK_ID, PROTOCOL_ID,
                () -> Names.FORK + ": " + presenter.getCurrentFork(),
                () -> Names.PROTOCOL,
                protocolElement);

    }

    @Override
    public Element asElement() {
        return innerPages.asElement();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void attach() {
        table.attach();
        form.attach();
        table.bindForm(form);
        forkElement.attach();
        protocolElement.attach();
    }

    @Override
    public void detach() {
        table.detach();
        forkElement.detach();
        form.detach();
        protocolElement.detach();
    }

    @Override
    public void setPresenter(final JGroupsPresenter presenter) {
        this.presenter = presenter;
        forkElement.setPresenter(presenter);
        protocolElement.setPresenter(presenter);
    }

    void update(List<NamedNode> models) {
        table.update(models);
        form.clear();
    }

    void updateProtocol(List<NamedNode> node) {
        protocolElement.update(node);
    }

    void updateForks(final List<NamedNode> model) {
        forkElement.update(model);
    }

    void showInnerPage(final String id) {
        innerPages.showPage(id);
    }

}
