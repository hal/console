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
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.RELAY_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.SELECTED_RELAY_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.StackElement.REMOTE_SITE_ID;

public class RelayElement implements IsElement<HTMLElement>, Attachable, HasPresenter<JGroupsPresenter> {

    private final Table<NamedNode> table;
    private final Form<NamedNode> form;
    private JGroupsPresenter presenter;
    private HTMLElement section;

    @SuppressWarnings({ "ConstantConditions", "HardCodedStringLiteral" })
    RelayElement(MetadataRegistry metadataRegistry, TableButtonFactory tableButtonFactory,
            Resources resources) {

        Metadata metadata = metadataRegistry.lookup(RELAY_TEMPLATE);

        table = new ModelNodeTable.Builder<NamedNode>(Ids.build(Ids.JGROUPS_RELAY, Ids.TABLE), metadata)
                .button(tableButtonFactory.add(RELAY_TEMPLATE, table -> presenter.addRelay()))
                .button(tableButtonFactory.remove(RELAY_TEMPLATE,
                        table -> presenter.removeResource(SELECTED_RELAY_TEMPLATE, table.selectedRow().getName(),
                                Names.RELAY)))
                .nameColumn()
                .column(new InlineAction<>(Names.REMOTE_SITE, row -> {
                    presenter.showRemoteSites(row);
                    presenter.showStackInnerPage(REMOTE_SITE_ID);
                }))
                .build();
        form = new ModelNodeForm.Builder<NamedNode>(Ids.build(Ids.JGROUPS_RELAY, Ids.FORM), metadata)
                .onSave((form, changedValues) -> presenter.saveSingleton(SELECTED_RELAY_TEMPLATE, metadata,
                        changedValues, resources.messages().modifySingleResourceSuccess(Names.RELAY)))
                .prepareReset(form -> presenter.resetSingleton(SELECTED_RELAY_TEMPLATE, Names.RELAY, form, metadata))
                .build();

        section = section()
                .add(h(1).textContent(Names.RELAY))
                .add(p().textContent(
                        metadata.getDescription().getDescription() + ". " + resources.constants().jgroupsRelayAlias()))
                .add(table)
                .add(form).element();
    }

    @Override
    public HTMLElement element() {
        return section;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void attach() {
        table.attach();
        form.attach();
        table.bindForm(form);
    }

    @Override
    public void detach() {
        form.detach();
        table.detach();
    }

    @Override
    public void setPresenter(JGroupsPresenter presenter) {
        this.presenter = presenter;
    }

    void update(List<NamedNode> models) {
        table.update(models);
        table.enableButton(0, models.isEmpty());
        table.enableButton(1, !models.isEmpty());
        form.clear();
    }
}
