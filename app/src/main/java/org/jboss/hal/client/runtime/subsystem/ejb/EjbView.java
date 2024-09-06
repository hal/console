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
package org.jboss.hal.client.runtime.subsystem.ejb;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.jboss.elemento.Elements;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.runtime.subsystem.ejb.AddressTemplates.ejbDeploymentTemplate;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NEXT_TIMEOUT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TIMERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TIME_REMAINING;

public class EjbView extends HalViewImpl implements EjbPresenter.MyView {

    private final HTMLElement header;
    private final HTMLElement lead;
    private final Map<EjbNode.Type, Form<EjbNode>> forms;
    private final Table<ModelNode> timersTable;
    private final Table<ModelNode> methodsTable;
    private final Table<ModelNode> removeMethodsTable;
    private final Form<ModelNode> scheduleForm;
    private final Tabs tabs;

    @Inject
    public EjbView(MetadataRegistry metadataRegistry) {
        forms = new HashMap<>();
        for (EjbNode.Type type : EjbNode.Type.values()) {
            Form<EjbNode> form = ejbForm(type, metadataRegistry);
            Elements.setVisible(form.element(), false);
            forms.put(type, form);
        }

        /**
         * "methods" is present in every bean type "timers" is present in every type except STATEFUL "remove-methods" is present
         * only in STATEFUL the metadata are the same regardless of type
         */
        Metadata methodsMetadata = metadataRegistry.lookup(ejbDeploymentTemplate(EjbNode.Type.MDB))
                .forComplexAttribute("methods");
        Metadata removeMethodsMetadata = metadataRegistry.lookup(ejbDeploymentTemplate(EjbNode.Type.STATEFUL))
                .forComplexAttribute("remove-methods");
        Metadata timersMetadata = metadataRegistry.lookup(ejbDeploymentTemplate(EjbNode.Type.MDB))
                .forComplexAttribute("timers");
        Metadata scheduleMetadata = timersMetadata.forComplexAttribute("schedule");

        // methods come in a property list, we need to add an identifier to the table
        methodsMetadata.getDescription().attributes().add(new Property(NAME, new ModelNode().set(NAME)));

        methodsTable = new ModelNodeTable.Builder<>("ejb-bean-methods-table", methodsMetadata)
                .columns("name", "execution-time", "invocations", "wait-time")
                .build();

        removeMethodsTable = new ModelNodeTable.Builder<>("ejb-bean-remove-methods-table", removeMethodsMetadata)
                .columns("bean-method", "retain-if-exception")
                .build();

        timersTable = new ModelNodeTable.Builder<>("ejb-bean-timers-table", timersMetadata)
                .columns("info", "time-remaining", "next-timeout", "calendar-timer", "persistent")
                .build();

        HTMLElement scheduleHeader = h(4).element();
        scheduleHeader.textContent = "Schedule";

        scheduleForm = new ModelNodeForm.Builder<>("host-system-property-form", scheduleMetadata)
                .readOnly()
                .build();

        tabs = new Tabs(Ids.build("ejb", Ids.TAB_CONTAINER));
        tabs.add(Ids.build("ejb-attributes", Ids.TAB), "Attributes",
                forms.values().stream().map(Form::element).collect(Collectors.toList()));
        tabs.add(Ids.build("ejb-methods", Ids.TAB), "Methods", methodsTable.element());
        tabs.add(Ids.build("ejb-remove-methods", Ids.TAB), "Remove Methods", removeMethodsTable.element());
        tabs.add(Ids.build("ejb-timers", Ids.TAB), "Timers", timersTable.element(), scheduleHeader, scheduleForm.element());

        registerAttachable(methodsTable, removeMethodsTable, timersTable, scheduleForm);

        initElement(row()
                .add(column()
                        .add(header = h(1).element())
                        .add(lead = p().css(CSS.lead).element())
                        .add(tabs)));
    }

    private Form<EjbNode> ejbForm(EjbNode.Type type, MetadataRegistry metadataRegistry) {
        Metadata metadata = metadataRegistry.lookup(ejbDeploymentTemplate(type));
        String id = Ids.build(Ids.EJB3_DEPLOYMENT, type.name(), Ids.FORM);
        return new ModelNodeForm.Builder<EjbNode>(id, metadata)
                .readOnly()
                .includeRuntime()
                .build();
    }

    @Override
    public void update(EjbNode ejb) {
        header.textContent = ejb.getName();
        lead.textContent = ejb.type.type;
        forms.get(ejb.type).view(ejb);
        forms.forEach((type, form) -> Elements.setVisible(form.element(), type == ejb.type));

        timersTable.update(formatTimers(ModelNodeHelper.failSafeList(ejb, TIMERS)));
        methodsTable.update(flattenMethods(ModelNodeHelper.failSafePropertyList(ejb, "methods")));
        removeMethodsTable.update(ModelNodeHelper.failSafeList(ejb, "remove-methods"));
    }

    @Override
    public void attach() {
        super.attach();

        timersTable.onSelectionChange(table -> {
            if (table.hasSelection()) {
                scheduleForm.view(table.selectedRow().get("schedule"));
            } else {
                scheduleForm.clear();
            }
        });
    }

    private List<ModelNode> formatTimers(List<ModelNode> timers) {
        for (ModelNode timer : timers) {
            String nextTimeout = Format.mediumDateTime(new Date(timer.get(NEXT_TIMEOUT).asLong()));
            String timeRemaining = Format.humanReadableDuration(timer.get(TIME_REMAINING).asLong());
            timer.get(NEXT_TIMEOUT).set(nextTimeout);
            timer.get(TIME_REMAINING).set(timeRemaining);
        }
        return timers;
    }

    // add method name to the model
    private List<ModelNode> flattenMethods(List<Property> methods) {
        List<ModelNode> flatMethods = new ArrayList<>();
        for (Property method : methods) {
            ModelNode flatMethod = method.getValue();
            flatMethod.get(NAME).set(method.getName());
            flatMethods.add(flatMethod);
        }
        return flatMethods;
    }
}
