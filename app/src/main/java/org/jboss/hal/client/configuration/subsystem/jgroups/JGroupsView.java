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

import javax.inject.Inject;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.JGROUPS_TEMPLATE;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;

/** This view is not MBUI generated because there are deep neste objects as relay, transport, protocol into stacks. */
public class JGroupsView extends HalViewImpl implements JGroupsPresenter.MyView {

    private final Form<ModelNode> jgroupsForm;
    private final StackElement stackConfig;
    private final ChannelElement channelConfig;
    private JGroupsPresenter presenter;

    @Inject
    public JGroupsView(final MetadataRegistry metadataRegistry, final TableButtonFactory tableButtonFactory,
            final Resources resources) {

        Metadata metadata = metadataRegistry.lookup(JGROUPS_TEMPLATE);
        jgroupsForm = new ModelNodeForm.Builder<>(Ids.JGROUPS_FORM, metadata)
                .onSave((form, changedValues) -> presenter.saveSingleton(JGROUPS_TEMPLATE, metadata, changedValues,
                        resources.messages().modifySingleResourceSuccess(Names.JGROUPS)))
                .prepareReset(form -> presenter.resetSingleton(JGROUPS_TEMPLATE, Names.JGROUPS, form, metadata))
                .build();

        HTMLElement jgroupsSection = section()
                .add(h(1).textContent(Names.JGROUPS))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(jgroupsForm).element();

        stackConfig = new StackElement(metadataRegistry, tableButtonFactory, resources);
        channelConfig = new ChannelElement(metadataRegistry, tableButtonFactory, resources);

        VerticalNavigation navigation = new VerticalNavigation();
        // main settings
        navigation.addPrimary(Ids.JGROUPS_ITEM, Names.CONFIGURATION, pfIcon("settings"), jgroupsSection);

        navigation.addPrimary(Ids.JGROUPS_STACK_ITEM, Names.STACK, fontAwesome("laptop"), stackConfig);
        navigation.addPrimary(Ids.JGROUPS_CHANNEL_ITEM, Names.CHANNEL, fontAwesome("pficon pficon-service"),
                channelConfig);

        registerAttachables(asList(navigation, jgroupsForm, stackConfig, channelConfig));

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
    }

    @Override
    public void setPresenter(final JGroupsPresenter presenter) {
        this.presenter = presenter;
        stackConfig.setPresenter(presenter);
        channelConfig.setPresenter(presenter);
    }

    @Override
    public void update(final ModelNode payload) {
        jgroupsForm.view(payload);
        stackConfig.update(asNamedNodes(failSafePropertyList(payload, ModelDescriptionConstants.STACK)));
        channelConfig.update(asNamedNodes(failSafePropertyList(payload, ModelDescriptionConstants.CHANNEL)));
    }

    @Override
    public void updateProtocols(final List<NamedNode> model) {
        stackConfig.updateProtocol(model);
    }

    @Override
    public void updateTransports(final List<NamedNode> model) {
        stackConfig.updateTransport(model);
    }

    @Override
    public void updateRemoteSite(List<NamedNode> model) {
        stackConfig.updateRemoteSite(model);
    }

    @Override
    public void updateRelays(final List<NamedNode> model) {
        stackConfig.updateRelays(model);
    }

    @Override
    public void updateForks(final List<NamedNode> model) {
        channelConfig.updateForks(model);
    }

    public void showStackInnerPage(String id) {
        stackConfig.showInnerPage(id);
    }

    public void showChannelInnerPage(String id) {
        channelConfig.showInnerPage(id);
    }

    public void updateChannelProtocols(List<NamedNode> model) {
        channelConfig.updateProtocol(model);
    }

    public void attach() {
        super.attach();
        presenter.processStackSingletons();
    }

}
