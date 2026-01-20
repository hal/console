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
import org.jboss.hal.ballroom.table.InlineAction;
import org.jboss.hal.ballroom.table.Table;
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
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.CHANNEL_FORK_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.SELECTED_CHANNEL_FORK_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.ChannelElement.PROTOCOL_ID;

public class ForkElement implements IsElement<HTMLElement>, Attachable, HasPresenter<JGroupsPresenter> {

    protected final Table<NamedNode> table;
    protected final MetadataRegistry metadataRegistry;
    protected final Resources resources;
    protected JGroupsPresenter presenter;
    private HTMLElement section;

    @SuppressWarnings({ "ConstantConditions", "HardCodedStringLiteral" })
    ForkElement(MetadataRegistry metadataRegistry, TableButtonFactory tableButtonFactory,
            Resources resources) {

        this.metadataRegistry = metadataRegistry;
        this.resources = resources;

        Metadata metadata = metadataRegistry.lookup(CHANNEL_FORK_TEMPLATE);

        table = new ModelNodeTable.Builder<NamedNode>(Ids.build(Ids.JGROUPS_CHANNEL_FORK, Ids.TABLE),
                metadata)
                .button(tableButtonFactory.add(CHANNEL_FORK_TEMPLATE,
                        table -> presenter.addResourceDialog(SELECTED_CHANNEL_FORK_TEMPLATE,
                                Ids.JGROUPS_CHANNEL_FORK_ITEM, Names.FORK)))
                .button(tableButtonFactory.remove(CHANNEL_FORK_TEMPLATE,
                        table -> presenter.removeResource(SELECTED_CHANNEL_FORK_TEMPLATE,
                                table.selectedRow().getName(), Names.FORK)))
                .nameColumn()
                .column(new InlineAction<>(Names.PROTOCOL, row -> {
                    presenter.showChannelProtocol(row);
                    presenter.showChannelInnerPage(PROTOCOL_ID);
                }))
                .build();

        section = section()
                .add(h(1).textContent(Names.FORK))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(table).element();
    }

    @Override
    public HTMLElement element() {
        return section;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void attach() {
        table.attach();
    }

    @Override
    public void detach() {
        table.detach();
    }

    @Override
    public void setPresenter(JGroupsPresenter presenter) {
        this.presenter = presenter;
    }

    void update(List<NamedNode> models) {
        table.update(models);
        // disable "remove" button if the table is empty
        table.enableButton(1, !models.isEmpty());
    }
}
