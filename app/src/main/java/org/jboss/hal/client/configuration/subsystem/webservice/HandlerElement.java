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
package org.jboss.hal.client.configuration.subsystem.webservice;

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
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.client.configuration.subsystem.webservice.AddressTemplates.HANDLER_TEMPLATE;

/** Element to configure handlers of a handler chain. */
class HandlerElement implements IsElement<HTMLElement>, Attachable, HasPresenter<WebservicePresenter> {

    private final HTMLElement root;
    private final Table<NamedNode> table;
    private final Form<NamedNode> form;
    private WebservicePresenter presenter;

    @SuppressWarnings("ConstantConditions")
    HandlerElement(Config configType, MetadataRegistry metadataRegistry,
            TableButtonFactory tableButtonFactory) {

        String tableId = Ids.build(configType.baseId, "handler", Ids.TABLE);
        Metadata metadata = metadataRegistry.lookup(HANDLER_TEMPLATE);
        table = new ModelNodeTable.Builder<NamedNode>(tableId, metadata)
                .button(tableButtonFactory.add(HANDLER_TEMPLATE, table -> presenter.addHandler()))
                .button(tableButtonFactory.remove(HANDLER_TEMPLATE,
                        table -> presenter.removeHandler(table.selectedRow().getName())))
                .nameColumn()
                .build();

        String formId = Ids.build(configType.baseId, "handler", Ids.FORM);
        form = new ModelNodeForm.Builder<NamedNode>(formId, metadata)
                .onSave((form, changedValues) -> presenter.saveHandler(form.getModel().getName(), changedValues))
                .prepareReset(form -> presenter.resetHandler(form.getModel().getName(), form))
                .build();

        root = section()
                .add(h(1).textContent(Names.HANDLER))
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
    public void setPresenter(WebservicePresenter presenter) {
        this.presenter = presenter;
    }

    void update(List<NamedNode> handlers) {
        form.clear();
        table.update(handlers);
    }
}
