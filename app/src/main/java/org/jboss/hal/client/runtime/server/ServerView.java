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

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

/**
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral"})
public abstract class ServerView extends MbuiViewImpl<ServerPresenter> implements ServerPresenter.MyView {

    public static ServerView create(final MbuiContext mbuiContext) {
        return new Mbui_ServerView(mbuiContext);
    }

    @MbuiElement("server-navigation") VerticalNavigation navigation;
    @MbuiElement("server-configuration-form") Form<Server> serverConfigurationForm;
    @MbuiElement("server-interface-table") NamedNodeTable<NamedNode> serverInterfaceTable;
    @MbuiElement("server-interface-form") Form<NamedNode> serverInterfaceForm;
    @MbuiElement("server-jvm-table") NamedNodeTable<NamedNode> serverJvmTable;
    @MbuiElement("server-jvm-form") Form<NamedNode> serverJvmForm;
    @MbuiElement("server-path-table") NamedNodeTable<NamedNode> serverPathTable;
    @MbuiElement("server-path-form") Form<NamedNode> serverPathForm;
    @MbuiElement("server-system-property-table") NamedNodeTable<NamedNode> serverSystemPropertyTable;
    @MbuiElement("server-system-property-form") Form<NamedNode> serverSystemPropertyForm;

    ServerView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @Override
    public void updateServer(final Server server) {
        serverConfigurationForm.view(server);
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
}
