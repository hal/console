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

import java.util.List;

import javax.annotation.PostConstruct;

import com.google.common.base.Splitter;
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

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

    private HTMLElement headerElement;

    ServerView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @PostConstruct
    void init() {
        serverConfigurationForm.getFormItem(HOST).setEnabled(false);
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
        }
    }

    private String pathWithNewLines(String path, String pathSeparator) {
        return String.join("\n", Splitter.on(pathSeparator).omitEmptyStrings().split(path));
    }
}
