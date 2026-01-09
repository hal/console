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
package org.jboss.hal.client.runtime.subsystem.microprofile.health;

import javax.inject.Inject;

import org.jboss.elemento.Elements;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.MICRO_PROFILE_HEALTH;
import static org.jboss.hal.resources.Ids.TABLE;

public class MicroProfileHealthView extends HalViewImpl implements MicroProfileHealthPresenter.MyView {

    private final Table<ModelNode> checkTable;
    private final Form<ModelNode> checkForm;
    private MicroProfileHealthPresenter presenter;
    private Alert outcomeUp;
    private Alert outcomeDown;

    @Inject
    public MicroProfileHealthView(Resources resources, MicroProfileHealthCheckResource mpCheckResource) {

        this.outcomeUp = new Alert(Icons.OK, resources.messages().microprofileHealthOutcome(UP));
        this.outcomeDown = new Alert(Icons.ERROR, resources.messages().microprofileHealthOutcome(DOWN));

        Metadata metadata = Metadata.staticDescription(mpCheckResource.checksOperationDescription());

        checkTable = new ModelNodeTable.Builder<>(Ids.build(MICRO_PROFILE_HEALTH, TABLE), metadata)
                .button(resources.constants().refresh(), table -> presenter.reload())
                .nameColumn()
                .column(Names.STATUS, (cell, type, row, meta) -> row.get(STATUS).asString())
                .build();

        checkForm = new ModelNodeForm.Builder<>(Ids.build(MICRO_PROFILE_HEALTH, FORM), metadata)
                .includeRuntime()
                .readOnly()
                .unsorted()
                .build();

        HTMLElement section = section()
                .add(h(1).textContent(Names.MICROPROFILE_HEALTH))
                .add(outcomeUp)
                .add(outcomeDown)
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(checkTable)
                .add(checkForm).element();

        registerAttachable(checkTable, checkForm);

        initElement(row()
                .add(column()
                        .addAll(section)));
    }

    @Override
    public void attach() {
        super.attach();
        checkTable.bindForm(checkForm);
    }

    @Override
    public void setPresenter(MicroProfileHealthPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(ModelNode result) {
        checkForm.clear();

        String outcome = result.get(STATUS).asString();
        Elements.setVisible(outcomeUp.element(), UP.equals(outcome));
        Elements.setVisible(outcomeDown.element(), DOWN.equals(outcome));
        checkTable.update(result.get(CHECKS).asList());
    }

}
