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
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.CHANNEL_FORK_PROTOCOL_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.CHANNEL_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.SELECTED_CHANNEL_FORK_PROTOCOL_TEMPLATE;

/** Element to configure the fork resource */
class ChannelElement implements IsElement<HTMLElement>, Attachable, HasPresenter<JGroupsPresenter> {

    static final String PROTOCOL_ID = Ids.build(Ids.JGROUPS_CHANNEL_FORK_PROTOCOL, Ids.PAGE);
    private static final String CHANNEL_ID = Ids.build(Ids.JGROUPS_CHANNEL_CONFIG, Ids.PAGE);
    private static final String FORK_ID = Ids.build(Ids.JGROUPS_RELAY, Ids.PAGE);

    private final Pages innerPages;
    private final Table<NamedNode> table;
    private final Form<NamedNode> form;
    private JGroupsPresenter presenter;
    private String selectedChannel;

    private final ForkElement forkElement;
    private final GenericElement protocolElement;

    @SuppressWarnings({ "ConstantConditions", "HardCodedStringLiteral" })
    ChannelElement(MetadataRegistry metadataRegistry, TableButtonFactory tableButtonFactory,
            Resources resources) {

        Metadata metadata = metadataRegistry.lookup(CHANNEL_TEMPLATE);
        table = new ModelNodeTable.Builder<NamedNode>(Ids.build(Ids.JGROUPS_CHANNEL_CONFIG, Ids.TABLE), metadata)
                .button(tableButtonFactory.add(CHANNEL_TEMPLATE,
                        table -> presenter.addResourceDialog(CHANNEL_TEMPLATE,
                                Ids.build(Ids.JGROUPS_CHANNEL_CONFIG, Ids.ADD), Names.CHANNEL)))
                .button(tableButtonFactory.remove(CHANNEL_TEMPLATE,
                        table -> presenter.removeResource(CHANNEL_TEMPLATE, table.selectedRow().getName(),
                                Names.CHANNEL)))
                .nameColumn()
                .column(new InlineAction<>(Names.FORK, row -> {
                    selectedChannel = row.getName();
                    presenter.showForks(row);
                    presenter.showChannelInnerPage(FORK_ID);
                }))
                .build();
        form = new ModelNodeForm.Builder<NamedNode>(Ids.build(Ids.JGROUPS_CHANNEL_CONFIG, Ids.FORM), metadata)
                .onSave((form, changedValues) -> presenter
                        .saveResource(CHANNEL_TEMPLATE, table.selectedRow().getName(), changedValues, metadata,
                                resources.messages().modifySingleResourceSuccess(Names.CHANNEL)))
                .prepareReset(form -> presenter.resetResource(CHANNEL_TEMPLATE, table.selectedRow().getName(),
                        Names.CHANNEL, form, metadata))
                .build();

        HTMLElement section = section()
                .add(h(1).textContent(Names.CHANNEL))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(table)
                .add(form).element();

        forkElement = new ForkElement(metadataRegistry, tableButtonFactory, resources);
        protocolElement = new GenericElement(metadataRegistry.lookup(CHANNEL_FORK_PROTOCOL_TEMPLATE),
                tableButtonFactory, resources, SELECTED_CHANNEL_FORK_PROTOCOL_TEMPLATE, Names.PROTOCOL,
                Ids.JGROUPS_CHANNEL_FORK_PROTOCOL);

        innerPages = new Pages(Ids.build(Ids.JGROUPS_CHANNEL_CONFIG, Ids.PAGES), CHANNEL_ID, section);
        // Fork page
        innerPages.addPage(CHANNEL_ID, FORK_ID,
                () -> Names.CHANNEL + ": " + selectedChannel,
                () -> Names.FORK,
                forkElement);
        // Fork / Protocol
        innerPages.addPage(FORK_ID, PROTOCOL_ID,
                () -> Names.FORK + ": " + presenter.getSelectedFork(),
                () -> Names.PROTOCOL,
                protocolElement);

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
    public void setPresenter(JGroupsPresenter presenter) {
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

    void updateForks(List<NamedNode> model) {
        forkElement.update(model);
    }

    void showInnerPage(String id) {
        innerPages.showPage(id);
    }

}
