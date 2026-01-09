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
package org.jboss.hal.client.configuration.subsystem.undertow;

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
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SERVER_TEMPLATE;

class ListenerElement implements IsElement<HTMLElement>, Attachable, HasPresenter<ServerPresenter> {

    HTMLElement root;
    Table<NamedNode> table;
    Form<NamedNode> form;
    ServerPresenter presenter;

    ListenerElement() {
    }

    @SuppressWarnings("ConstantConditions")
    ListenerElement(Listener listenerType, MetadataRegistry metadataRegistry, TableButtonFactory tableButtonFactory) {

        AddressTemplate template = SERVER_TEMPLATE.append(listenerType.resource + "=*");
        Metadata metadata = metadataRegistry.lookup(template);
        table = new ModelNodeTable.Builder<NamedNode>(Ids.build(listenerType.baseId, Ids.TABLE), metadata)
                .button(tableButtonFactory.add(template, table -> presenter.addListener(listenerType)))
                .button(tableButtonFactory.remove(template,
                        table -> presenter.removeListener(listenerType, table.selectedRow().getName())))
                .nameColumn()
                .build();

        form = new ModelNodeForm.Builder<NamedNode>(Ids.build(listenerType.baseId, Ids.FORM), metadata)
                .onSave((form, changedValues) -> presenter.saveListener(listenerType, form.getModel().getName(),
                        changedValues))
                .prepareReset(form -> presenter.resetListener(listenerType, form.getModel().getName(), form))
                .build();

        root = section()
                .add(h(1).textContent(listenerType.type))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(table)
                .add(form).element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
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
    public void setPresenter(ServerPresenter presenter) {
        this.presenter = presenter;
    }

    void update(List<NamedNode> items) {
        form.clear();
        table.update(items);
    }
}
