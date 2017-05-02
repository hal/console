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
package org.jboss.hal.client.runtime.host;

import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral", "unused", "WeakerAccess"})
public abstract class HostView extends MbuiViewImpl<HostPresenter> implements HostPresenter.MyView {

    public static HostView create(final MbuiContext mbuiContext) {
        return new Mbui_HostView(mbuiContext);
    }

    @MbuiElement("host-navigation") VerticalNavigation navigation;
    @MbuiElement("host-configuration-form") Form<Host> hostConfigurationForm;
    @MbuiElement("host-interface-table") Table<NamedNode> hostInterfaceTable;
    @MbuiElement("host-interface-form") Form<NamedNode> hostInterfaceForm;
    @MbuiElement("host-jvm-table") Table<NamedNode> hostJvmTable;
    @MbuiElement("host-jvm-form") Form<NamedNode> hostJvmForm;
    @MbuiElement("host-path-table") Table<NamedNode> hostPathTable;
    @MbuiElement("host-path-form") Form<NamedNode> hostPathForm;
    @MbuiElement("host-socket-binding-group-table") Table<NamedNode> hostSocketBindingGroupTable;
    @MbuiElement("host-socket-binding-group-form") Form<NamedNode> hostSocketBindingGroupForm;
    @MbuiElement("host-system-property-table") Table<NamedNode> hostSystemPropertyTable;
    @MbuiElement("host-system-property-form") Form<NamedNode> hostSystemPropertyForm;

    HostView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @Override
    public void updateHost(final Host host) {
        hostConfigurationForm.view(host);
        hostConfigurationForm.getFormItem(NAME).unmask(); // makes no sense that this is sensitive
        Element element = Browser.getDocument().getElementById("host-configuration-title");
        if (element != null) {
            element.setInnerHTML(new SafeHtmlBuilder()
                    .appendEscaped(host.isDomainController() ? Names.DOMAIN_CONTROLLER : Names.HOST_CONTROLLER)
                    .appendEscaped(" ")
                    .appendHtmlConstant("<code>")
                    .appendEscaped(host.getName())
                    .appendHtmlConstant("</code>")
                    .toSafeHtml().asString());
        }
    }

    @Override
    public void updateInterfaces(final List<NamedNode> interfaces) {
        hostInterfaceForm.clear();
        hostInterfaceTable.update(interfaces);
    }

    @Override
    public void updateJvms(final List<NamedNode> interfaces) {
        hostJvmForm.clear();
        hostJvmTable.update(interfaces);
    }

    @Override
    public void updatePaths(final List<NamedNode> paths) {
        hostPathForm.clear();
        hostPathTable.update(paths);
    }

    @Override
    public void updateSocketBindingGroups(final List<NamedNode> groups) {
        hostSocketBindingGroupForm.clear();
        hostSocketBindingGroupTable.update(groups);
    }

    @Override
    public void updateSystemProperties(final List<NamedNode> properties) {
        hostSystemPropertyForm.clear();
        hostSystemPropertyTable.update(properties);
    }
}
