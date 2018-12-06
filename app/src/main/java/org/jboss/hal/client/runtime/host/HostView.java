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

import javax.annotation.PostConstruct;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Button;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
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

import static elemental2.dom.DomGlobal.document;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.client.runtime.host.HostPresenter.HTTP_INTERFACE_TEMPLATE;
import static org.jboss.hal.client.runtime.host.HostPresenter.NATIVE_INTERFACE_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.resources.CSS.pullRight;
import static org.jboss.hal.resources.Ids.*;

@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral", "unused", "WeakerAccess"})
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
    private Form<ModelNode> httpInterfaceForm;
    private Form<ModelNode> nativeInterfaceForm;
    private HTMLButtonElement enableSslButton;
    private HTMLButtonElement disableSslButton;
    private HTMLElement httpMgmtItemElement;
    private HTMLElement nativeMgmtItemElement;

    HostView(MbuiContext mbuiContext) {
        super(mbuiContext);

        Resources resources = mbuiContext.resources();
        enableSslButton = button().id(ENABLE_SSL)
                .textContent(resources.constants().enableSSL())
                .css(Button.DEFAULT_CSS, pullRight)
                .get();
        bind(enableSslButton, click, ev -> presenter.launchEnableSSLWizard());

        disableSslButton = button().id(DISABLE_SSL)
                .textContent(resources.constants().disableSSL())
                .css(Button.DEFAULT_CSS, pullRight)
                .get();
        bind(disableSslButton, click, ev -> presenter.disableSSLWizard());

        String httpTitle = resources.constants().httpManagementInterface();
        Metadata httpMetadata = mbuiContext.metadataRegistry().lookup(HTTP_INTERFACE_TEMPLATE);
        String httpId = Ids.build(HTTP_INTERFACE, FORM);
        httpInterfaceForm = new ModelNodeForm.Builder<>(httpId, httpMetadata)
                .onSave((form, changedValues) -> presenter.save(httpTitle, HTTP_INTERFACE_TEMPLATE, changedValues))
                .prepareReset(form -> presenter.reset(httpTitle, HTTP_INTERFACE_TEMPLATE, form, httpMetadata))
                .unsorted()
                .build();

        httpMgmtItemElement = section()
                .add(div()
                        .add(h(1).textContent(httpTitle).get())
                        .add(p().textContent(httpMetadata.getDescription().getDescription()).get())
                        .add(enableSslButton)
                        .add(disableSslButton))
                .add(httpInterfaceForm)
                .get();

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
                        .add(h(1).textContent(nativeTitle).get())
                        .add(p().textContent(nativeMetadata.getDescription().getDescription()).get()))
                .add(nativeInterfaceForm)
                .get();
    }

    @PostConstruct
    void init() {
        String id = Ids.build(MANAGEMENT_INTERFACE, ITEM);
        navigation.insertPrimary(id, "host-path-item", Names.MANAGEMENT_INTERFACE, pfIcon("virtual-machine"));
        navigation.insertSecondary(id, HTTP_INTERFACE_ITEM, null, "HTTP", httpMgmtItemElement);
        navigation.insertSecondary(id, NATIVE_INTERFACE_ITEM, null, "Native", nativeMgmtItemElement);

        registerAttachable(httpInterfaceForm);
        registerAttachable(nativeInterfaceForm);
    }

    @Override
    public void updateManagementInterfaces(List<NamedNode> endpoints) {
        boolean nativeExists = false;
        boolean httpExists = false;
        for (NamedNode named: endpoints) {
            if (named.getName().equals(NATIVE_INTERFACE)) {
                ModelNode model = named.asModelNode();
                nativeInterfaceForm.view(model);
                nativeExists = true;
            }
            if (named.getName().equals(HTTP_INTERFACE)) {
                ModelNode model = named.asModelNode();
                httpInterfaceForm.view(model);
                boolean isSslEnabled = model.hasDefined(SSL_CONTEXT) && model.get(SSL_CONTEXT).asString() != null;
                toggleSslButton(isSslEnabled);
                httpExists = true;
            }
        }
        // slave host controller doesn't have an http-interface, but if the http-interface is currently displayed
        // for a domain controller and user switch to a slave host controller, display the native interface
        if (Elements.isVisible(httpMgmtItemElement) && !httpExists) {
            navigation.show(NATIVE_INTERFACE_ITEM);
        }
        navigation.setVisible(NATIVE_INTERFACE_ITEM, nativeExists);
        navigation.setVisible(HTTP_INTERFACE_ITEM, httpExists);

    }

    private void toggleSslButton(boolean enable) {
        Elements.setVisible(enableSslButton, !enable);
        Elements.setVisible(disableSslButton, enable);
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
