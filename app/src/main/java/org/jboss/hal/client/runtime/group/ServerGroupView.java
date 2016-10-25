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
package org.jboss.hal.client.runtime.group;

import java.util.List;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Api;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.runtime.group.ServerGroup;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

/**
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral"})
public class ServerGroupView extends MbuiViewImpl<ServerGroupPresenter> implements ServerGroupPresenter.MyView {

    public static ServerGroupView create(final MbuiContext mbuiContext) {
        return new Mbui_ServerGroupView(mbuiContext);
    }

    @MbuiElement("server-group-navigation") VerticalNavigation navigation;
    @MbuiElement("server-group-configuration-form") Form<ServerGroup> serverGroupConfigurationForm;
    @MbuiElement("server-group-jvm-table") DataTable<NamedNode> serverGroupJvmTable;
    @MbuiElement("server-group-jvm-form") Form<NamedNode> serverGroupJvmForm;
    @MbuiElement("server-group-system-property-table") DataTable<NamedNode> serverGroupSystemPropertyTable;
    @MbuiElement("server-group-system-property-form") Form<NamedNode> serverGroupSystemPropertyForm;

    ServerGroupView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @Override
    public void updateServerGroup(final ServerGroup serverGroup) {
        serverGroupConfigurationForm.view(serverGroup);
    }

    @Override
    public void updateJvms(final List<NamedNode> interfaces) {
        serverGroupJvmTable.api().clear().add(interfaces).refresh(Api.RefreshMode.RESET);
        serverGroupJvmForm.clear();
    }

    @Override
    public void updateSystemProperties(final List<NamedNode> interfaces) {
        serverGroupSystemPropertyTable.api().clear().add(interfaces).refresh(Api.RefreshMode.RESET);
        serverGroupSystemPropertyForm.clear();
    }
}
