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
package org.jboss.hal.client.runtime.host;

import java.util.List;

import javax.annotation.PostConstruct;

import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.client.runtime.managementinterface.ConstantHeadersElement;
import org.jboss.hal.client.runtime.managementinterface.HttpManagementInterfaceElement;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.client.runtime.host.AddressTemplates.HTTP_INTERFACE_TEMPLATE;
import static org.jboss.hal.client.runtime.host.AddressTemplates.NATIVE_INTERFACE_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONSTANT_HEADERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HTTP_INTERFACE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGEMENT_INTERFACE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NATIVE_INTERFACE;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeList;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.resources.Ids.CONSTANT_HEADERS_ITEM;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.HTTP_INTERFACE_ITEM;
import static org.jboss.hal.resources.Ids.ITEM;
import static org.jboss.hal.resources.Ids.NATIVE_INTERFACE_ITEM;

@MbuiView
public abstract class HostView extends MbuiViewImpl<HostPresenter> implements HostPresenter.MyView {

    public static HostView create(MbuiContext mbuiContext) {
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
    private HttpManagementInterfaceElement httpManagementInterfaceElement;
    private ConstantHeadersElement constantHeadersElement;
    private Form<ModelNode> nativeInterfaceForm;
    private HTMLElement nativeMgmtItemElement;

    HostView(MbuiContext mbuiContext) {
        super(mbuiContext);
        Resources resources = mbuiContext.resources();

        httpManagementInterfaceElement = new HttpManagementInterfaceElement(mbuiContext.metadataRegistry(),
                HTTP_INTERFACE_TEMPLATE, resources);
        constantHeadersElement = new ConstantHeadersElement(mbuiContext.metadataRegistry(), HTTP_INTERFACE_TEMPLATE,
                resources);

        String nativeTitle = resources.constants().nativeManagementInterface();
        Metadata nativeMetadata = mbuiContext.metadataRegistry().lookup(NATIVE_INTERFACE_TEMPLATE);
        String nativeId = Ids.build(NATIVE_INTERFACE, FORM);
        nativeInterfaceForm = new ModelNodeForm.Builder<>(nativeId, nativeMetadata)
                .onSave((form, changedValues) -> presenter.save(nativeTitle, NATIVE_INTERFACE_TEMPLATE, changedValues))
                .prepareReset(form -> presenter.reset(nativeTitle, NATIVE_INTERFACE_TEMPLATE, form, nativeMetadata))
                .unsorted()
                .build();

        nativeMgmtItemElement = section()
                .add(div()
                        .add(h(1).textContent(nativeTitle).element())
                        .add(p().textContent(nativeMetadata.getDescription().getDescription()).element()))
                .add(nativeInterfaceForm).element();
    }

    @PostConstruct
    void init() {
        String managementInterfaceId = Ids.build(MANAGEMENT_INTERFACE, ITEM);
        navigation.insertPrimary(managementInterfaceId, "host-path-item", Names.MANAGEMENT_INTERFACE,
                pfIcon("virtual-machine"));
        navigation.insertSecondary(managementInterfaceId, HTTP_INTERFACE_ITEM, null, "HTTP",
                httpManagementInterfaceElement.element());
        navigation.insertSecondary(managementInterfaceId, NATIVE_INTERFACE_ITEM, null, "Native", nativeMgmtItemElement);
        navigation.insertPrimary(CONSTANT_HEADERS_ITEM, "host-path-item", new LabelBuilder().label(CONSTANT_HEADERS),
                fontAwesome("bars"),
                constantHeadersElement);

        registerAttachable(httpManagementInterfaceElement);
        registerAttachable(nativeInterfaceForm);
        registerAttachable(constantHeadersElement);
    }

    @Override
    public void setPresenter(HostPresenter presenter) {
        super.setPresenter(presenter);
        httpManagementInterfaceElement.setPresenter(presenter);
        constantHeadersElement.setPresenter(presenter);
    }

    @Override
    public void updateManagementInterfaces(List<NamedNode> endpoints, int pathIndex) {
        boolean nativeExists = false;
        boolean httpExists = false;
        for (NamedNode named : endpoints) {
            if (named.getName().equals(NATIVE_INTERFACE)) {
                ModelNode model = named.asModelNode();
                nativeInterfaceForm.view(model);
                nativeExists = true;
            }
            if (named.getName().equals(HTTP_INTERFACE)) {
                ModelNode model = named.asModelNode();
                httpManagementInterfaceElement.update(model);
                httpExists = true;

                List<ModelNode> constantHeaders = failSafeList(model, CONSTANT_HEADERS);
                constantHeadersElement.update(constantHeaders);
                if (pathIndex >= 0 && pathIndex < constantHeaders.size()) {
                    constantHeadersElement.showHeaders(constantHeaders.get(pathIndex));
                }
            }
        }
        // slave host controller doesn't have an http-interface, but if the http-interface is currently displayed
        // for a domain controller and user switch to a slave host controller, display the native interface
        if (Elements.isVisible(httpManagementInterfaceElement.element()) && !httpExists) {
            navigation.show(NATIVE_INTERFACE_ITEM);
        }
        navigation.setVisible(NATIVE_INTERFACE_ITEM, nativeExists);
        navigation.setVisible(HTTP_INTERFACE_ITEM, httpExists);

    }

    @Override
    public void updateHost(Host host) {
        hostConfigurationForm.view(host);
        hostConfigurationForm.getFormItem(NAME).unmask(); // makes no sense that this is sensitive
        HTMLElement element = (HTMLElement) document.getElementById("host-configuration-title");
        if (element != null) {
            element.innerHTML = new SafeHtmlBuilder()
                    .appendEscaped(host.isDomainController() ? Names.DOMAIN_CONTROLLER : Names.HOST_CONTROLLER)
                    .appendEscaped(" ")
                    .appendHtmlConstant("<code>")
                    .appendEscaped(host.getName())
                    .appendHtmlConstant("</code>")
                    .toSafeHtml().asString();
        }
    }

    @Override
    public void updateInterfaces(List<NamedNode> interfaces) {
        hostInterfaceForm.clear();
        hostInterfaceTable.update(interfaces);
    }

    @Override
    public void updateJvms(List<NamedNode> interfaces) {
        hostJvmForm.clear();
        hostJvmTable.update(interfaces);
    }

    @Override
    public void updatePaths(List<NamedNode> paths) {
        hostPathForm.clear();
        hostPathTable.update(paths);
    }

    @Override
    public void updateSocketBindingGroups(List<NamedNode> groups) {
        hostSocketBindingGroupForm.clear();
        hostSocketBindingGroupTable.update(groups);
    }

    @Override
    public void updateSystemProperties(List<NamedNode> properties) {
        hostSystemPropertyForm.clear();
        hostSystemPropertyTable.update(properties);
    }
}
