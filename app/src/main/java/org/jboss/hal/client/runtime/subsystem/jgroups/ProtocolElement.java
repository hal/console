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

import java.util.List;

import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;

public class ProtocolElement implements IsElement<HTMLElement>, Attachable, HasPresenter<ChannelPresenter> {

    private final Table<NamedNode> table;
    private final String resourceId;
    private Form<NamedNode> form;
    private ChannelPresenter presenter;
    private final HTMLElement section;
    private final HTMLElement formContainer;

    ProtocolElement(Metadata metadata, String resourceId) {
        this.resourceId = resourceId;

        table = new ModelNodeTable.Builder<NamedNode>(Ids.build(resourceId, Ids.TABLE), metadata)
                .indexColumn()
                .nameColumn()
                .build();
        form = createForm(metadata);

        section = section()
                .add(h(1).textContent(Names.PROTOCOL))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(table)
                .add(formContainer = div().add(form).element()).element();
    }

    @Override
    public HTMLElement element() {
        return section;
    }

    @Override
    public void attach() {
        table.attach();
        form.attach();

        // we cannot use table.bind since the form will be changing
        table.onSelectionChange(table -> {
            if (table.hasSelection()) {
                adjustAndView(table.selectedRow());
            } else {
                form.clear();
            }
        });
    }

    @Override
    public void detach() {
        form.detach();
        table.detach();
    }

    @Override
    public void setPresenter(ChannelPresenter presenter) {
        this.presenter = presenter;
    }

    void update(List<NamedNode> protocols) {
        table.update(protocols);
        form.clear();
    }

    // protocols have different runtime attributes, we need to replace the form
    private void adjustAndView(NamedNode selectedRow) {
        String selectedProtocol = selectedRow.getName();
        Metadata metadata = presenter.getProtocolMetadata(selectedProtocol);

        form.detach();
        form = createForm(metadata);
        formContainer.replaceChildren(form.element());

        form.attach();
        form.view(selectedRow);
    }

    private Form<NamedNode> createForm(Metadata metadata) {
        return new ModelNodeForm.Builder<NamedNode>(Ids.build(resourceId, Ids.FORM), metadata)
                .readOnly()
                .includeRuntime()
                .build();
    }
}
