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

import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.CHANNEL_FORK_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.SELECTED_CHANNEL_FORK_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.ChannelElement.PROTOCOL_ID;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * @author Claudio Miranda <claudio@redhat.com>
 */
public class ForkElement implements IsElement, Attachable, HasPresenter<JGroupsPresenter> {

    protected final Table<NamedNode> table;
    protected final MetadataRegistry metadataRegistry;
    protected final Resources resources;
    protected JGroupsPresenter presenter;
    private Element section;

    @SuppressWarnings({"ConstantConditions", "HardCodedStringLiteral"})
    ForkElement(final MetadataRegistry metadataRegistry, final TableButtonFactory tableButtonFactory,
            final Resources resources) {

        this.metadataRegistry = metadataRegistry;
        this.resources = resources;

        Metadata metadata = metadataRegistry.lookup(CHANNEL_FORK_TEMPLATE);

        table = new ModelNodeTable.Builder<NamedNode>(Ids.build(Ids.JGROUPS_CHANNEL_FORK_ENTRY, Ids.TABLE_SUFFIX),
                metadata)
                .button(tableButtonFactory.add(CHANNEL_FORK_TEMPLATE,
                        table -> presenter.addResourceDialog(SELECTED_CHANNEL_FORK_TEMPLATE,
                                Ids.JGROUPS_CHANNEL_FORK_ENTRY, Names.FORK)))
                .button(tableButtonFactory.remove(CHANNEL_FORK_TEMPLATE,
                        table -> presenter.removeResource(SELECTED_CHANNEL_FORK_TEMPLATE,
                                table.selectedRow().getName(), Names.FORK)))
                .column(NAME, (cell, t, row, meta) -> row.getName())
                .column("Protocols", row -> {
                    presenter.showChannelProtocol(row);
                    presenter.showChannelInnerPage(PROTOCOL_ID);
                })
                .build();

        // @formatter:off
        section = new Elements.Builder()
            .section()
                .h(1).textContent(Names.FORK).end()
                .p().textContent(metadata.getDescription().getDescription()).end()
                .add(table)
            .end()
        .build();
        // @formatter:on
    }

    @Override
    public Element asElement() {
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
    public void setPresenter(final JGroupsPresenter presenter) {
        this.presenter = presenter;
    }

    void update(List<NamedNode> models) {
        table.update(models);
    }

}
