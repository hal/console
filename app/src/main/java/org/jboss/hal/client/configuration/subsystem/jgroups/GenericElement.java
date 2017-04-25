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
package org.jboss.hal.client.configuration.subsystem.jgroups;

import java.util.List;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
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

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * @author Claudio Miranda <claudio@redhat.com>
 */
public class GenericElement implements IsElement, Attachable, HasPresenter<JGroupsPresenter> {

    protected final Table<NamedNode> table;
    protected final Resources resources;
    private final Form<NamedNode> form;
    protected JGroupsPresenter presenter;
    private Element section;

    @SuppressWarnings({"ConstantConditions", "HardCodedStringLiteral"})
    GenericElement(final Metadata metadata, final TableButtonFactory tableButtonFactory,
            final Resources resources,
            AddressTemplate template, String name, String resourceId) {
        this.resources = resources;

        table = new ModelNodeTable.Builder<NamedNode>(Ids.build(resourceId, Ids.TABLE_SUFFIX), metadata)
                .button(tableButtonFactory.add(template, (event, table) -> presenter.addResourceDialog(template,
                        Ids.build(resourceId, Ids.ADD_SUFFIX, Ids.FORM_SUFFIX), name)))
                .button(tableButtonFactory.remove(template,
                        (event, table) -> presenter.removeResource(template, table.selectedRow().getName(), name)))
                .column(NAME, (cell, t, row, meta) -> row.getName())
                .build();
        form = new ModelNodeForm.Builder<NamedNode>(Ids.build(resourceId, Ids.FORM_SUFFIX), metadata)
                .onSave((form, changedValues) -> presenter
                        .saveResource(template, table.selectedRow().getName(), changedValues, metadata,
                                resources.messages().modifySingleResourceSuccess(name)))
                .prepareReset(form -> presenter.resetResource(template, table.selectedRow().getName(), name, form,
                        metadata))
                .build();

        // @formatter:off
        section = new Elements.Builder()
            .section()
                .h(1).textContent(name).end()
                .p().textContent(metadata.getDescription().getDescription()).end()
                .add(table)
                .add(form)
            .end()
        .build();
        // @formatter:on
    }

    @Override
    public Element asElement() {
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
    public void setPresenter(final JGroupsPresenter presenter) {
        this.presenter = presenter;
    }

    void update(List<NamedNode> models) {
        table.update(models);
        form.clear();
    }

}
