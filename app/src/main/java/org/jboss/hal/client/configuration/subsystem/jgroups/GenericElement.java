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
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;

public class GenericElement implements IsElement<HTMLElement>, Attachable, HasPresenter<JGroupsPresenter> {

    protected final Table<NamedNode> table;
    protected final Resources resources;
    private final Form<NamedNode> form;
    protected JGroupsPresenter presenter;
    private HTMLElement section;

    @SuppressWarnings({ "ConstantConditions", "HardCodedStringLiteral" })
    GenericElement(Metadata metadata, TableButtonFactory tableButtonFactory,
            Resources resources,
            AddressTemplate template, String name, String resourceId) {
        this.resources = resources;

        table = new ModelNodeTable.Builder<NamedNode>(Ids.build(resourceId, Ids.TABLE), metadata)
                .button(tableButtonFactory.add(template, table -> presenter.addResourceDialog(template,
                        Ids.build(resourceId, Ids.ADD, Ids.FORM), name)))
                .button(tableButtonFactory.remove(template,
                        table -> presenter.removeResource(template, table.selectedRow().getName(), name)))
                .nameColumn()
                .build();
        form = new ModelNodeForm.Builder<NamedNode>(Ids.build(resourceId, Ids.FORM), metadata)
                .onSave((form, changedValues) -> presenter
                        .saveResource(template, table.selectedRow().getName(), changedValues, metadata,
                                resources.messages().modifySingleResourceSuccess(name)))
                .prepareReset(form -> presenter.resetResource(template, table.selectedRow().getName(), name, form,
                        metadata))
                .build();

        section = section()
                .add(h(1).textContent(name))
                .add(p().textContent(metadata.getDescription().getDescription()))
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
        form.clear();
        table.enableButton(1, !models.isEmpty());
    }

}
