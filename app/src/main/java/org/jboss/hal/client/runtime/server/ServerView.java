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
package org.jboss.hal.client.runtime.server;

import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;

import com.google.common.base.Splitter;
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.Tabs;
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
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.client.runtime.server.ServerPresenter.SERVER_RUNTIME_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.pfIcon;

@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral", "unused", "WeakerAccess"})
public abstract class ServerView extends MbuiViewImpl<ServerPresenter> implements ServerPresenter.MyView {

    private static final String[] JVM_ATTRIBUTES = {
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

    public static ServerView create(final MbuiContext mbuiContext) {
        return new Mbui_ServerView(mbuiContext);
    }

    @MbuiElement("server-navigation") VerticalNavigation navigation;
    @MbuiElement("server-configuration-form") Form<Server> serverConfigurationForm;
    @MbuiElement("server-interface-table") Table<NamedNode> serverInterfaceTable;
    @MbuiElement("server-interface-form") Form<NamedNode> serverInterfaceForm;
    @MbuiElement("server-jvm-table") Table<NamedNode> serverJvmTable;
    @MbuiElement("server-jvm-form") Form<NamedNode> serverJvmForm;
    @MbuiElement("server-path-table") Table<NamedNode> serverPathTable;
    @MbuiElement("server-path-form") Form<NamedNode> serverPathForm;
    @MbuiElement("server-system-property-table") Table<NamedNode> serverSystemPropertyTable;
    @MbuiElement("server-system-property-form") Form<NamedNode> serverSystemPropertyForm;

    private Form<ModelNode> jvmAttributes;
    private Form<ModelNode> bootstrapAttributes;
    private Table<Property> runtimeProperties;
    private HTMLElement headerElement;

    ServerView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @PostConstruct
    void init() {
        serverConfigurationForm.getFormItem(HOST).setEnabled(false);

        Metadata metadata = mbuiContext.metadataRegistry().lookup(SERVER_RUNTIME_TEMPLATE);
        jvmAttributes = new ModelNodeForm.Builder<>(Ids.SERVER_RUNTIME_JVM_ATTRIBUTES_FORM, metadata)
                .readOnly()
                .includeRuntime()
                .include(JVM_ATTRIBUTES)
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
                .column(NAME, Names.NAME, (cell, t, row, meta) -> row.getName())
                .column(new ColumnBuilder<Property>(VALUE, Names.VALUE,
                        (cell, t, row, meta) -> row.getValue().asString())
                        .width("66%")
                        .searchable(false)
                        .orderable(false)
                        .build())
                .options();
        runtimeProperties = new DataTable<>(Ids.SERVER_RUNTIME_PROPERTIES_TABLE, options);

        registerAttachable(jvmAttributes, bootstrapAttributes, runtimeProperties);

        Tabs tabs = new Tabs();
        tabs.add(Ids.SERVER_RUNTIME_JVM_ATTRIBUTES_TAB, Names.JVM_ATTRIBUTES, jvmAttributes.asElement());
        tabs.add(Ids.SERVER_RUNTIME_BOOTSTRAP_TAB, Names.BOOTSTRAP, bootstrapAttributes.asElement());
        tabs.add(Ids.SERVER_RUNTIME_PROPERTIES_TAB, Names.RUNTIME_PROPERTIES, runtimeProperties.asElement());
        HTMLElement runtime = section()
                .add(headerElement = h(1).asElement())
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(tabs)
                .asElement();
        navigation.insertPrimary(Ids.SERVER_RUNTIME_ITEM, "server-system-property-item", Names.RUNTIME, pfIcon("resource-pool"), runtime);
    }

    @Override
    public void updateServer(final Server server) {
        serverConfigurationForm.view(server);
        serverConfigurationForm.getFormItem(HOST).<String>setValue(server.getHost());
    }

    @Override
    public void updateInterfaces(final List<NamedNode> interfaces) {
        serverInterfaceForm.clear();
        serverInterfaceTable.update(interfaces);
    }

    @Override
    public void updateJvms(final List<NamedNode> jvms) {
        serverJvmForm.clear();
        serverJvmTable.update(jvms);
    }

    @Override
    public void updatePaths(final List<NamedNode> paths) {
        serverPathForm.clear();
        serverPathTable.update(paths);
    }

    @Override
    public void updateSystemProperties(final List<NamedNode> properties) {
        serverSystemPropertyForm.clear();
        serverSystemPropertyTable.update(properties);
    }

    @Override
    public void updateRuntime(ModelNode modelNode) {
        navigation.setVisible(Ids.SERVER_RUNTIME_ITEM, modelNode != null);
        if (modelNode != null) {
            List<Property> sp = modelNode.get(SYSTEM_PROPERTIES).asPropertyList();
            String pathSeparator = sp.stream()
                    .filter(p -> "path.separator".equals(p.getName())) //NON-NLS
                    .findAny()
                    .map(p -> p.getValue().asString())
                    .orElse(":");

            headerElement.textContent = modelNode.get(NAME).asString();

            jvmAttributes.view(modelNode);
            jvmAttributes.<String>getFormItem(START_TIME)
                    .setValue(Format.shortDateTime(new Date(modelNode.get(START_TIME).asLong())));
            jvmAttributes.<String>getFormItem(UPTIME)
                    .setValue(Format.humanReadableDuration(modelNode.get(UPTIME).asLong()));

            bootstrapAttributes.view(modelNode);
            bootstrapAttributes.<String>getFormItem(BOOT_CLASS_PATH)
                    .setValue(pathWithNewLines(modelNode.get(BOOT_CLASS_PATH).asString(), pathSeparator));
            bootstrapAttributes.<String>getFormItem(CLASS_PATH)
                    .setValue(pathWithNewLines(modelNode.get(CLASS_PATH).asString(), pathSeparator));
            bootstrapAttributes.<String>getFormItem(LIBRARY_PATH)
                    .setValue(pathWithNewLines(modelNode.get(LIBRARY_PATH).asString(), pathSeparator));

            runtimeProperties.update(sp, Property::getName);
        }
    }

    private String pathWithNewLines(String path, String pathSeparator) {
        return String.join("\n", Splitter.on(pathSeparator).omitEmptyStrings().split(path));
    }
}
