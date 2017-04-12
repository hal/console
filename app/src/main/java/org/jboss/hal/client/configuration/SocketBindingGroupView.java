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
package org.jboss.hal.client.configuration;

import java.util.List;
import javax.annotation.PostConstruct;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static org.jboss.hal.client.configuration.SocketBinding.INBOUND;
import static org.jboss.hal.client.configuration.SocketBindingGroupPresenter.ROOT_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.fontAwesome;

/**
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class SocketBindingGroupView extends MbuiViewImpl<SocketBindingGroupPresenter>
        implements SocketBindingGroupPresenter.MyView {

    public static SocketBindingGroupView create(final MbuiContext mbuiContext) {
        return new Mbui_SocketBindingGroupView(mbuiContext);
    }

    @MbuiElement("socket-binding-group-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("socket-binding-group-configuration-form") Form<NamedNode> configurationForm;
    @MbuiElement("socket-binding-group-outbound-local-table") NamedNodeTable<NamedNode> outboundLocalTable;
    @MbuiElement("socket-binding-group-outbound-local-form") Form<NamedNode> outboundLocalForm;
    @MbuiElement("socket-binding-group-outbound-remote-table") NamedNodeTable<NamedNode> outboundRemoteTable;
    @MbuiElement("socket-binding-group-outbound-remote-form") Form<NamedNode> outboundRemoteForm;

    Pages inboundPages;
    NamedNodeTable<NamedNode> inboundTable;
    Form<NamedNode> inboundForm;
    NamedNodeTable<NamedNode> clientMappingTable;
    Form<NamedNode> clientMappingForm;

    SocketBindingGroupView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @PostConstruct
    @SuppressWarnings("ConstantConditions")
    void init() {
        AddressTemplate inboundTemplate = ROOT_TEMPLATE.append(INBOUND.templateSuffix());
        Metadata inboundMetadata = mbuiContext.metadataRegistry().lookup(inboundTemplate);

        Options<NamedNode> inboundOptions = new NamedNodeTable.Builder<>(inboundMetadata)
                .button(mbuiContext.tableButtonFactory().add(inboundTemplate,
                        (event, api) -> presenter.addSocketBinding(INBOUND)))
                .button(mbuiContext.tableButtonFactory().remove(inboundTemplate,
                        (event, api) -> presenter.removeSocketBinding(INBOUND, api.selectedRow().getName())))
                .column(NAME, (cell, type, row, meta) -> row.getName())
                .column(PORT, (cell, type, row, meta) -> row.get(PORT).asString())
                .column(Names.CLIENT_MAPPINGS, row -> presenter.showClientMappings(row))
                .build();
        inboundTable = new NamedNodeTable<>(Ids.build(INBOUND.baseId, Ids.TABLE_SUFFIX), inboundMetadata,
                inboundOptions);

        inboundForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(INBOUND.baseId, Ids.FORM_SUFFIX), inboundMetadata)
                .include(INTERFACE, PORT, FIXED_PORT, MULTICAST_ADDRESS, MULTICAST_PORT)
                .unsorted()
                .onSave((form, changedValues) -> presenter.saveSocketBinding(INBOUND, form, changedValues))
                .prepareReset(form -> presenter.resetSocketBinding(INBOUND, form))
                .build();

        Element inboundSection = new Elements.Builder()
                .section()
                .h(1).textContent(Names.INBOUND).end()
                .p().textContent(inboundMetadata.getDescription().getDescription()).end()
                .add(inboundTable)
                .add(inboundForm)
                .end()
                .build();

        Metadata clientMappingsMetadata = clientMappingsMetadata(inboundMetadata);

        Options<NamedNode> clientMappingsOptions = new ModelNodeTable.Builder<NamedNode>(clientMappingsMetadata)
                .button(mbuiContext.tableButtonFactory().add(inboundTemplate,
                        (event, api) -> presenter.addClientMapping(clientMappingsMetadata)))
                .button(mbuiContext.tableButtonFactory().remove(inboundTemplate,
                        (event, api) -> presenter.removeClientMapping(api.selectedRow().get(INDEX).asInt(-1))))
                .column(SOURCE_NETWORK)
                .column(Names.DESTINATION, (cell, type, row, meta) -> {
                    String address = row.get(DESTINATION_ADDRESS).asString();
                    if (row.hasDefined(DESTINATION_PORT)) {
                        address += ":" + String.valueOf(row.get(DESTINATION_PORT).asInt());
                    }
                    return address;
                })
                .build();
        clientMappingTable = new NamedNodeTable<>(Ids.SOCKET_BINDING_GROUP_INBOUND_CLIENT_MAPPING_TABLE,
                clientMappingsMetadata, clientMappingsOptions);

        clientMappingForm = new ModelNodeForm.Builder<NamedNode>(Ids.SOCKET_BINDING_GROUP_INBOUND_CLIENT_MAPPING_FORM,
                clientMappingsMetadata)
                .include(SOURCE_NETWORK, DESTINATION_ADDRESS, DESTINATION_PORT)
                .unsorted()
                .onSave((form, changedValues) -> presenter.saveClientMapping(clientMappingsMetadata, form.getModel(),
                        changedValues))
                .build();

        Element clientMappingSection = new Elements.Builder()
                .section()
                .h(1).textContent(Names.CLIENT_MAPPINGS).end()
                .p().textContent(clientMappingsMetadata.getDescription().getDescription()).end()
                .add(clientMappingTable)
                .add(clientMappingForm)
                .end()
                .build();

        String parentId = Ids.build(INBOUND.baseId, Ids.PAGE_SUFFIX);
        inboundPages = new Pages(parentId, inboundSection);
        inboundPages.addPage(parentId, Ids.SOCKET_BINDING_GROUP_INBOUND_CLIENT_MAPPING_PAGE,
                () -> Names.INBOUND + ": " + presenter.inbound, () -> Names.CLIENT_MAPPINGS,
                clientMappingSection);

        navigation.insertPrimary(Ids.build(INBOUND.baseId, Ids.ENTRY_SUFFIX),
                "socket-binding-group-outbound-local-item", //NON-NLS
                Names.INBOUND, fontAwesome("arrow-circle-o-left"), inboundPages);

        registerAttachable(inboundTable, inboundForm, clientMappingTable, clientMappingForm);
    }

    private Metadata clientMappingsMetadata(final Metadata inboundMetadata) {
        ModelNode modelNode = new ModelNode();
        Property clientMappings = inboundMetadata.getDescription().findAttribute(ATTRIBUTES, CLIENT_MAPPINGS);
        if (clientMappings != null) {
            modelNode.get(DESCRIPTION).set(clientMappings.getValue().get(DESCRIPTION));
            modelNode.get(ATTRIBUTES).set(clientMappings.getValue().get(VALUE_TYPE));
        }
        ResourceDescription resourceDescription = new ResourceDescription(modelNode);
        return inboundMetadata.customResourceDescription(resourceDescription);
    }

    @Override
    public void attach() {
        super.attach();
        inboundTable.bindForm(inboundForm);
        clientMappingTable.bindForm(clientMappingForm);
    }

    @Override
    public void reveal() {
        inboundPages.showPage(Ids.build(INBOUND.baseId, Ids.PAGE_SUFFIX));
    }

    @Override
    public void update(final NamedNode socketBindingGroup) {
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
    public void showClientMappings(final List<NamedNode> clientMappings) {
        clientMappingForm.clear();
        clientMappingTable.update(clientMappings);
        inboundPages.showPage(Ids.SOCKET_BINDING_GROUP_INBOUND_CLIENT_MAPPING_PAGE);
    }
}