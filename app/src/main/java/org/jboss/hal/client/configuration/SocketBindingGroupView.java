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
package org.jboss.hal.client.configuration;

import java.util.List;

import javax.annotation.PostConstruct;

import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.InlineAction;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.client.configuration.SocketBinding.INBOUND;
import static org.jboss.hal.client.configuration.SocketBindingGroupPresenter.ROOT_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.fontAwesome;

@MbuiView
@SuppressWarnings({ "unused", "WeakerAccess", "DuplicateStringLiteralInspection" })
public abstract class SocketBindingGroupView extends MbuiViewImpl<SocketBindingGroupPresenter>
        implements SocketBindingGroupPresenter.MyView {

    public static SocketBindingGroupView create(MbuiContext mbuiContext) {
        return new Mbui_SocketBindingGroupView(mbuiContext);
    }

    @MbuiElement("socket-binding-group-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("socket-binding-group-configuration-form") Form<NamedNode> configurationForm;
    @MbuiElement("socket-binding-group-outbound-local-table") Table<NamedNode> outboundLocalTable;
    @MbuiElement("socket-binding-group-outbound-local-form") Form<NamedNode> outboundLocalForm;
    @MbuiElement("socket-binding-group-outbound-remote-table") Table<NamedNode> outboundRemoteTable;
    @MbuiElement("socket-binding-group-outbound-remote-form") Form<NamedNode> outboundRemoteForm;

    Pages inboundPages;
    Table<NamedNode> inboundTable;
    Form<NamedNode> inboundForm;
    Table<NamedNode> clientMappingTable;
    Form<NamedNode> clientMappingForm;

    SocketBindingGroupView(MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @PostConstruct
    @SuppressWarnings("ConstantConditions")
    void init() {
        AddressTemplate inboundTemplate = ROOT_TEMPLATE.append(INBOUND.templateSuffix());
        Metadata inboundMetadata = mbuiContext.metadataRegistry().lookup(inboundTemplate);

        inboundTable = new ModelNodeTable.Builder<NamedNode>(Ids.build(INBOUND.baseId, Ids.TABLE),
                inboundMetadata)
                .button(mbuiContext.tableButtonFactory().add(inboundTemplate,
                        table -> presenter.addSocketBinding(INBOUND)))
                .button(mbuiContext.tableButtonFactory().remove(inboundTemplate,
                        table -> presenter.removeSocketBinding(INBOUND, table.selectedRow().getName())))
                .nameColumn()
                .column(PORT, (cell, type, row, meta) -> row.get(PORT).asString())
                .column(new InlineAction<>(Names.CLIENT_MAPPINGS, row -> presenter.showClientMappings(row)))
                .build();

        inboundForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(INBOUND.baseId, Ids.FORM), inboundMetadata)
                .include(INTERFACE, PORT, FIXED_PORT, MULTICAST_ADDRESS, MULTICAST_PORT)
                .unsorted()
                .onSave((form, changedValues) -> presenter.saveSocketBinding(INBOUND, form, changedValues))
                .prepareReset(form -> presenter.resetSocketBinding(INBOUND, form))
                .build();

        HTMLElement inboundSection = section()
                .add(h(1).textContent(Names.INBOUND))
                .add(p().textContent(inboundMetadata.getDescription().getDescription()))
                .add(inboundTable)
                .add(inboundForm).element();

        Metadata clientMappingsMetadata = inboundMetadata.forComplexAttribute(CLIENT_MAPPINGS);

        clientMappingTable = new ModelNodeTable.Builder<NamedNode>(
                Ids.SOCKET_BINDING_GROUP_INBOUND_CLIENT_MAPPING_TABLE, clientMappingsMetadata)
                .button(mbuiContext.tableButtonFactory().add(inboundTemplate,
                        table -> presenter.addClientMapping(clientMappingsMetadata)))
                .button(mbuiContext.tableButtonFactory().remove(inboundTemplate,
                        table -> presenter.removeClientMapping(table.selectedRow().get(INDEX).asInt(-1))))
                .column(SOURCE_NETWORK)
                .column(Names.DESTINATION, (cell, type, row, meta) -> {
                    String address = row.get(DESTINATION_ADDRESS).asString();
                    if (row.hasDefined(DESTINATION_PORT)) {
                        address += ":" + row.get(DESTINATION_PORT).asInt();
                    }
                    return address;
                })
                .build();

        clientMappingForm = new ModelNodeForm.Builder<NamedNode>(Ids.SOCKET_BINDING_GROUP_INBOUND_CLIENT_MAPPING_FORM,
                clientMappingsMetadata)
                .include(SOURCE_NETWORK, DESTINATION_ADDRESS, DESTINATION_PORT)
                .unsorted()
                .onSave((form, changedValues) -> presenter.saveClientMapping(clientMappingsMetadata, form.getModel(),
                        changedValues))
                .build();

        HTMLElement clientMappingSection = section()
                .add(h(1).textContent(Names.CLIENT_MAPPINGS))
                .add(p().textContent(clientMappingsMetadata.getDescription().getDescription()))
                .add(clientMappingTable)
                .add(clientMappingForm).element();

        String id = Ids.build(INBOUND.baseId, Ids.PAGES);
        String parentId = Ids.build(INBOUND.baseId, Ids.PAGE);
        inboundPages = new Pages(id, parentId, inboundSection);
        inboundPages.addPage(parentId, Ids.SOCKET_BINDING_GROUP_INBOUND_CLIENT_MAPPING_PAGE,
                () -> Names.INBOUND + ": " + presenter.inbound, () -> Names.CLIENT_MAPPINGS,
                clientMappingSection);

        navigation.insertPrimary(Ids.build(INBOUND.baseId, Ids.ITEM),
                "socket-binding-group-outbound-local-item", // NON-NLS
                Names.INBOUND, fontAwesome("arrow-circle-o-left"), inboundPages);

        registerAttachable(inboundTable, inboundForm, clientMappingTable, clientMappingForm);
    }

    @Override
    public void attach() {
        super.attach();
        inboundTable.bindForm(inboundForm);
        clientMappingTable.bindForm(clientMappingForm);
    }

    @Override
    public void reveal() {
        inboundPages.showPage(Ids.build(INBOUND.baseId, Ids.PAGE));
    }

    @Override
    public void update(NamedNode socketBindingGroup) {
        configurationForm.view(socketBindingGroup);

        inboundForm.clear();
        inboundTable.update(asNamedNodes(failSafePropertyList(socketBindingGroup, SOCKET_BINDING)));

        outboundLocalForm.clear();
        outboundLocalTable.update(asNamedNodes(failSafePropertyList(socketBindingGroup,
                LOCAL_DESTINATION_OUTBOUND_SOCKET_BINDING)));

        outboundRemoteForm.clear();
        outboundRemoteTable.update(asNamedNodes(failSafePropertyList(socketBindingGroup,
                REMOTE_DESTINATION_OUTBOUND_SOCKET_BINDING)));
    }

    @Override
    public void showClientMappings(List<NamedNode> clientMappings) {
        clientMappingForm.clear();
        clientMappingTable.update(clientMappings);
        inboundPages.showPage(Ids.SOCKET_BINDING_GROUP_INBOUND_CLIENT_MAPPING_PAGE);
    }
}
