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
package org.jboss.hal.client.configuration.subsystem.webservice;

import javax.annotation.PostConstruct;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static org.jboss.hal.client.configuration.subsystem.webservice.AddressTemplates.CLIENT_CONFIG_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.webservice.AddressTemplates.ENDPOINT_CONFIG_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLIENT_CONFIG;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ENDPOINT_CONFIG;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;

/**
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings("DuplicateStringLiteralInspection")
public class WebserviceView extends MbuiViewImpl<WebservicePresenter> implements WebservicePresenter.MyView {

    public static WebserviceView create(final MbuiContext mbuiContext) {
        return new Mbui_WebserviceView(mbuiContext);
    }

    @MbuiElement("webservice-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("webservice-configuration-form") Form<ModelNode> configurationForm;

    private ConfigElement clientConfig;
    private ConfigElement endpointConfig;

    WebserviceView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @PostConstruct
    @SuppressWarnings({"ConstantConditions", "HardCodedStringLiteral"})
    void init() {
        clientConfig = new ConfigElement(presenter, mbuiContext, Ids.WEBSERVICES_CLIENT_CONFIG,
                Names.CLIENT_CONFIG, CLIENT_CONFIG_TEMPLATE);
        registerAttachable(clientConfig);
        navigation.addPrimary(Ids.WEBSERVICES_CLIENT_CONFIG_ENTRY, Names.CLIENT_CONFIG, fontAwesome("laptop"),
                clientConfig);

        endpointConfig = new ConfigElement(presenter, mbuiContext, Ids.WEBSERVICES_ENDPOINT_CONFIG,
                Names.ENDPOINT_CONFIG, ENDPOINT_CONFIG_TEMPLATE);
        registerAttachable(endpointConfig);
        navigation.addPrimary(Ids.WEBSERVICES_ENDPOINT_CONFIG_ENTRY, Names.ENDPOINT_CONFIG, pfIcon("service"),
                endpointConfig);

        // rebuild root layout with new navigation entries (kind a hack, but it works)
        Elements.removeChildrenFrom(asElement().getParentElement());
        LayoutBuilder layoutBuilder = new LayoutBuilder()
                .row()
                .column()
                .addAll(navigation.panes())
                .end()
                .end();
        Element root = layoutBuilder.build();
        initElement(root);
    }

    @Override
    public void attach() {
        super.attach();
    }

    @Override
    public void update(final ModelNode data) {
        configurationForm.view(data);
        clientConfig.update(asNamedNodes(failSafePropertyList(data, CLIENT_CONFIG)));
        endpointConfig.update(asNamedNodes(failSafePropertyList(data, ENDPOINT_CONFIG)));
    }
}
