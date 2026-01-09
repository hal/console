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

import javax.inject.Inject;

import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.configuration.SystemPropertiesPresenter.ROOT_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.BOOT_TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;

public class SystemPropertiesView extends HalViewImpl implements SystemPropertiesPresenter.MyView {

    private final Table<NamedNode> table;
    private final Form<NamedNode> form;
    private SystemPropertiesPresenter presenter;

    @Inject
    public SystemPropertiesView(Environment environment, MetadataRegistry metadataRegistry,
            TableButtonFactory tableButtonFactory, Resources resources) {
        Metadata metadata = metadataRegistry.lookup(ROOT_TEMPLATE);

        ModelNodeForm.Builder<NamedNode> fb = new ModelNodeForm.Builder<>(Ids.SYSTEM_PROPERTY_FORM, metadata);
        fb.include(VALUE);
        if (!environment.isStandalone()) {
            fb.include(BOOT_TIME);
        }
        fb.unsorted()
                .onSave((form, changedValues) -> presenter.save(form, changedValues))
                .prepareReset(form -> presenter.reset(form, metadata))
                .build();
        form = fb.build();

        ModelNodeTable.Builder<NamedNode> tb = new ModelNodeTable.Builder<NamedNode>(Ids.SYSTEM_PROPERTY_TABLE,
                metadata)
                .button(resources.constants().add(), table -> presenter.add(),
                        Constraint.parse("executable(system-property=*:add)"))
                .button(tableButtonFactory.remove(Names.SYSTEM_PROPERTY, ROOT_TEMPLATE,
                        table -> table.selectedRow().getName(),
                        () -> presenter.reload()))
                .nameColumn()
                .column(VALUE);
        if (!environment.isStandalone()) {
            tb.column("boot-time");
        }
        table = tb.build();

        HTMLElement root = row()
                .add(column()
                        .add(h(1).textContent(Names.SYSTEM_PROPERTIES))
                        .add(p().textContent(metadata.getDescription().getDescription()))
                        .add(table)
                        .add(form))
                .element();

        registerAttachable(table);
        registerAttachable(form);
        initElement(root);
    }

    @Override
    public void attach() {
        super.attach();
        table.bindForm(form);
    }

    @Override
    public void setPresenter(SystemPropertiesPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(List<NamedNode> systemProperties) {
        form.clear();
        table.update(systemProperties);
    }
}
