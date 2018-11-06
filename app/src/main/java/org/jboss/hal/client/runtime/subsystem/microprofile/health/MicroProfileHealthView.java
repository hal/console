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
package org.jboss.hal.client.runtime.subsystem.microprofile.health;

import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
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

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.MICROPROFILE_HEALTH;
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

        checkTable = new ModelNodeTable.Builder<>(Ids.build(MICROPROFILE_HEALTH, TABLE), metadata)
                .button(resources.constants().refresh(), table -> presenter.reload())
                .column(Names.NAME, (cell, type, row, meta) -> row.get(NAME).asString())
                .column(Names.STATE, (cell, type, row, meta) -> row.get(STATE).asString())
                .build();

        checkForm = new ModelNodeForm.Builder<>(Ids.build(MICROPROFILE_HEALTH, FORM), metadata)
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
                .add(checkForm)
                .asElement();

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

        String outcome = result.get(OUTCOME).asString();
        Elements.setVisible(outcomeUp.asElement(), UP.equals(outcome));
        Elements.setVisible(outcomeDown.asElement(), DOWN.equals(outcome));
        checkTable.update(result.get(CHECKS).asList());
    }

}
