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
package org.jboss.hal.client.runtime.subsystem.jgroups;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.table.InlineAction;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.runtime.subsystem.jgroups.AddressTemplates.FORK_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.jgroups.AddressTemplates.STACK_PROTOCOL_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.FORK;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROTOCOL;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.fontAwesome;

public class ChannelView extends HalViewImpl implements ChannelPresenter.MyView {

    private final String MAIN_PAGE_ID = Ids.build(Ids.JGROUPS_PROTOCOL, Ids.PAGE);
    private final String FORK_ID = Ids.build(Ids.JGROUPS_CHANNEL_RUNTIME, Ids.JGROUPS_CHANNEL_FORK);
    private final String FORK_PAGE_ID = Ids.build(Ids.JGROUPS_CHANNEL_FORK_PROTOCOL, Ids.PAGE);

    private ChannelPresenter presenter;

    private final ProtocolElement channelProtocols;
    private final ProtocolElement forkProtocols;
    private final Table<NamedNode> forkTable;
    private final Pages innerPages;

    private String selectedFork;

    @Inject
    public ChannelView(MetadataRegistry metadataRegistry) {
        List<InlineAction<NamedNode>> inlineActions = new ArrayList<>();
        inlineActions.add(new InlineAction<>(Names.PROTOCOL, row -> {
            selectedFork = row.getName();
            presenter.showFork(selectedFork);
        }));

        Metadata metadata = metadataRegistry.lookup(FORK_TEMPLATE);
        forkTable = new ModelNodeTable.Builder<NamedNode>(Ids.build(FORK_ID, Ids.TABLE), metadata)
                .nameColumn()
                .column(inlineActions)
                .build();

        HTMLElement forkSection = section()
                .add(h(1).textContent(Names.FORK))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(forkTable).element();

        // this is a hack, channel protocols are runtime-only but we need "generic protocol" metadata for the table
        Metadata protocolMetadata = metadataRegistry.lookup(STACK_PROTOCOL_TEMPLATE);
        channelProtocols = new ProtocolElement(protocolMetadata,
                Ids.build(Ids.JGROUPS_CHANNEL_RUNTIME, Ids.JGROUPS_PROTOCOL));

        VerticalNavigation navigation = new VerticalNavigation();
        navigation.addPrimary(Ids.JGROUPS_CHANNEL_ITEM, "Protocols", fontAwesome("list"), channelProtocols);

        forkProtocols = new ProtocolElement(protocolMetadata,
                Ids.build(Ids.JGROUPS_CHANNEL_RUNTIME, Ids.JGROUPS_CHANNEL_FORK_PROTOCOL));

        innerPages = new Pages(Ids.build(FORK_ID, Ids.PAGES), MAIN_PAGE_ID, forkSection);
        innerPages.addPage(MAIN_PAGE_ID, FORK_PAGE_ID,
                () -> Names.FORK + ": " + selectedFork,
                () -> Names.PROTOCOL, forkProtocols);

        navigation.addPrimary(Ids.JGROUPS_CHANNEL_FORK_ITEM, Names.FORK, fontAwesome("code-fork"), innerPages);

        registerAttachable(navigation, forkTable, channelProtocols, forkProtocols);

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
    }

    @Override
    public void updateChannel(ModelNode channel) {
        channelProtocols.update(asNamedNodes(failSafePropertyList(channel, PROTOCOL)));
        forkTable.update(asNamedNodes(failSafePropertyList(channel, FORK)));
    }

    @Override
    public void updateFork(ModelNode fork) {
        forkProtocols.update(asNamedNodes(failSafePropertyList(fork, PROTOCOL)));
        innerPages.showPage(FORK_PAGE_ID);
    }

    @Override
    public void setPresenter(ChannelPresenter presenter) {
        this.presenter = presenter;
        channelProtocols.setPresenter(presenter);
        forkProtocols.setPresenter(presenter);
    }
}
