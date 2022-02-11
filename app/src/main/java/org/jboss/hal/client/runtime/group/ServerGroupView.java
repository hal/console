/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.runtime.group;

import java.util.List;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.runtime.group.ServerGroup;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

@MbuiView
@SuppressWarnings({ "DuplicateStringLiteralInspection", "HardCodedStringLiteral", "unused", "WeakerAccess" })
public abstract class ServerGroupView extends MbuiViewImpl<ServerGroupPresenter>
        implements ServerGroupPresenter.MyView {

    public static ServerGroupView create(final MbuiContext mbuiContext) {
        return new Mbui_ServerGroupView(mbuiContext);
    }

    @MbuiElement("server-group-navigation") VerticalNavigation navigation;
    @MbuiElement("server-group-configuration-form") Form<ServerGroup> serverGroupConfigurationForm;
    @MbuiElement("server-group-jvm-table") Table<NamedNode> serverGroupJvmTable;
    @MbuiElement("server-group-jvm-form") Form<NamedNode> serverGroupJvmForm;
    @MbuiElement("server-group-system-property-table") Table<NamedNode> serverGroupSystemPropertyTable;
    @MbuiElement("server-group-system-property-form") Form<NamedNode> serverGroupSystemPropertyForm;

    ServerGroupView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @Override
    public void updateServerGroup(final ServerGroup serverGroup) {
        serverGroupConfigurationForm.view(serverGroup);
    }

    @Override
    public void updateJvms(final List<NamedNode> jvms) {
        serverGroupJvmForm.clear();
        serverGroupJvmTable.update(jvms);
    }

    @Override
    public void updateSystemProperties(final List<NamedNode> properties) {
        serverGroupSystemPropertyForm.clear();
        serverGroupSystemPropertyTable.update(properties);
    }
}
