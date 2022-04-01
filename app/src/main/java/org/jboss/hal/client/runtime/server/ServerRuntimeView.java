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
package org.jboss.hal.client.runtime.server;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.PreListItem;
import org.jboss.hal.ballroom.form.PreTextItem;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.ballroom.table.ColumnBuilder;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.ballroom.table.OptionsBuilder;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import com.google.common.base.Splitter;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;

public class ServerRuntimeView extends HalViewImpl implements ServerRuntimePresenter.MyView {

    private static final String[] MAIN_ATTRIBUTES = {
            NAME,
            VM_NAME,
            VM_VENDOR,
            VM_VERSION,
            SPEC_NAME,
            SPEC_VENDOR,
            SPEC_VERSION,
            MANAGEMENT_SPEC_VERSION
    };

    private static final String[] BOOTSTRAP_ATTRIBUTES = {
            BOOT_CLASS_PATH_SUPPORTED,
            BOOT_CLASS_PATH,
            CLASS_PATH,
            LIBRARY_PATH,
            INPUT_ARGUMENTS,
    };

    private final Form<ModelNode> mainAttributes;
    private final Form<ModelNode> bootstrapAttributes;
    private final Table<Property> systemProperties;
    private final HTMLElement headerElement;

    @Inject
    public ServerRuntimeView(MetadataRegistry metadataRegistry, Resources resources) {
        Metadata metadata = metadataRegistry.lookup(ServerRuntimePresenter.SERVER_RUNTIME_TEMPLATE);
        mainAttributes = new ModelNodeForm.Builder<>(Ids.SERVER_RUNTIME_JVM_ATTRIBUTES_FORM, metadata)
                .readOnly()
                .includeRuntime()
                .include(MAIN_ATTRIBUTES)
                .unboundFormItem(new TextBoxItem(START_TIME, new LabelBuilder().label(START_TIME)))
                .unboundFormItem(new TextBoxItem(UPTIME, new LabelBuilder().label(UPTIME)))
                .unsorted()
                .build();

        bootstrapAttributes = new ModelNodeForm.Builder<>(Ids.SERVER_RUNTIME_BOOTSTRAP_FORM, metadata)
                .readOnly()
                .includeRuntime()
                .include(BOOTSTRAP_ATTRIBUTES)
                .customFormItem(BOOT_CLASS_PATH, attributeDescription -> new PreTextItem(BOOT_CLASS_PATH))
                .customFormItem(CLASS_PATH, attributeDescription -> new PreTextItem(CLASS_PATH))
                .customFormItem(LIBRARY_PATH, attributeDescription -> new PreTextItem(LIBRARY_PATH))
                .customFormItem(INPUT_ARGUMENTS, attributeDescription -> new PreListItem(INPUT_ARGUMENTS))
                .unsorted()
                .build();

        Options<Property> options = new OptionsBuilder<Property>()
                .column(NAME, resources.constants().name(), (cell, t, row, meta) -> row.getName())
                .column(new ColumnBuilder<Property>(VALUE, Names.VALUE,
                        (cell, t, row, meta) -> row.getValue().asString())
                                .width("66%")
                                .searchable(false)
                                .orderable(false)
                                .build())
                .options();
        systemProperties = new DataTable<>(Ids.SERVER_RUNTIME_PROPERTIES_TABLE, options);

        registerAttachable(mainAttributes, bootstrapAttributes, systemProperties);

        HTMLElement mainAttributes = section()
                .add(headerElement = h(1).element())
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(this.mainAttributes).element();

        HTMLElement bootstrapSection = section()
                .add(h(1).textContent(Names.BOOTSTRAP))
                .add(bootstrapAttributes).element();

        HTMLElement systemPropertiesSection = section()
                .add(h(1).textContent(Names.SYSTEM_PROPERTIES))
                .add(systemProperties).element();

        VerticalNavigation navigation = new VerticalNavigation();
        registerAttachable(navigation);

        navigation.addPrimary(Ids.SERVER_STATUS_MAIN_ATTRIBUTES_ITEM, resources.constants().mainAttributes(),
                fontAwesome("list-ul"), mainAttributes);
        navigation
                .addPrimary(Ids.SERVER_STATUS_BOOTSTRAP_ITEM, Names.BOOTSTRAP, fontAwesome("play"), bootstrapSection);
        navigation
                .addPrimary(Ids.SERVER_STATUS_SYSTEM_PROPERTIES_ITEM, Names.SYSTEM_PROPERTIES, pfIcon("resource-pool"),
                        systemPropertiesSection);

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
    }

    @Override
    public void update(ModelNode modelNode) {
        List<Property> sp = modelNode.get(SYSTEM_PROPERTIES).asPropertyList();
        String pathSeparator = sp.stream()
                .filter(p -> "path.separator".equals(p.getName())) // NON-NLS
                .findAny()
                .map(p -> p.getValue().asString())
                .orElse(":");

        headerElement.textContent = modelNode.get(NAME).asString();

        mainAttributes.view(modelNode);
        mainAttributes.<String> getFormItem(START_TIME)
                .setValue(Format.shortDateTime(new Date(modelNode.get(START_TIME).asLong())));
        mainAttributes.<String> getFormItem(UPTIME)
                .setValue(Format.humanReadableDuration(modelNode.get(UPTIME).asLong()));

        bootstrapAttributes.view(modelNode);
        bootstrapAttributes.<String> getFormItem(BOOT_CLASS_PATH)
                .setValue(pathWithNewLines(modelNode.get(BOOT_CLASS_PATH).asString(), pathSeparator));
        bootstrapAttributes.<String> getFormItem(CLASS_PATH)
                .setValue(pathWithNewLines(modelNode.get(CLASS_PATH).asString(), pathSeparator));
        bootstrapAttributes.<String> getFormItem(LIBRARY_PATH)
                .setValue(pathWithNewLines(modelNode.get(LIBRARY_PATH).asString(), pathSeparator));

        systemProperties.update(sp, Property::getName);
    }

    private String pathWithNewLines(String path, String pathSeparator) {
        return String.join("\n", Splitter.on(pathSeparator).omitEmptyStrings().split(path));
    }
}
