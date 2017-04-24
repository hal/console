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
package org.jboss.hal.client.configuration.subsystem.undertow;

import java.util.List;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SERVER_TEMPLATE;

/**
 * @author Harald Pehl
 */
class ListenerElement implements IsElement, Attachable, HasPresenter<ServerPresenter> {

    private final Element root;
    private final NamedNodeTable<NamedNode> table;
    private final Form<NamedNode> form;
    private ServerPresenter presenter;

    @SuppressWarnings("ConstantConditions")
    ListenerElement(Listener listenerType, MetadataRegistry metadataRegistry, TableButtonFactory tableButtonFactory) {

        AddressTemplate template = SERVER_TEMPLATE.append(listenerType.resource + "=*");
        Metadata metadata = metadataRegistry.lookup(template);
        Options<NamedNode> options = new NamedNodeTable.Builder<>(metadata)
                .button(tableButtonFactory.add(template, (event, api) -> presenter.addListener(listenerType)))
                .button(tableButtonFactory.remove(template,
                        (event, api) -> presenter.removeListener(listenerType, api.selectedRow().getName())))
                .column(Names.NAME, (cell, type, row, meta) -> row.getName())
                .build();
        table = new NamedNodeTable<>(Ids.build(listenerType.baseId, Ids.TABLE_SUFFIX), metadata, options);

        form = new ModelNodeForm.Builder<NamedNode>(Ids.build(listenerType.baseId, Ids.FORM_SUFFIX), metadata)
                .onSave((form, changedValues) -> presenter.saveListener(listenerType, form.getModel().getName(),
                        changedValues))
                .prepareReset(form -> presenter.resetListener(listenerType, form.getModel().getName(), form))
                .build();

        // @formatter:off
        root = new Elements.Builder()
            .section()
                .h(1).textContent(listenerType.type).end()
                .p().textContent(metadata.getDescription().getDescription()).end()
                .add(table)
                .add(form)
            .end()
        .build();
        // @formatter:on
    }

    @Override
    public Element asElement() {
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
    public void setPresenter(final ServerPresenter presenter) {
        this.presenter = presenter;
    }

    void update(List<NamedNode> items) {
        form.clear();
        table.update(items);
    }
}
