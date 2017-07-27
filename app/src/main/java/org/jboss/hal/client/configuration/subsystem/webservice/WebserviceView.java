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

import java.util.List;
import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.configuration.subsystem.webservice.AddressTemplates.WEBSERVICES_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.webservice.Config.CLIENT_CONFIG;
import static org.jboss.hal.client.configuration.subsystem.webservice.Config.ENDPOINT_CONFIG;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;

public class WebserviceView extends HalViewImpl implements WebservicePresenter.MyView {

    private final Form<ModelNode> webservicesForm;
    private final ConfigElement clientConfig;
    private final ConfigElement endpointConfig;
    private WebservicePresenter presenter;

    @Inject
    public WebserviceView(final MetadataRegistry metadataRegistry, final TableButtonFactory tableButtonFactory) {

        Metadata metadata = metadataRegistry.lookup(WEBSERVICES_TEMPLATE);
        webservicesForm = new ModelNodeForm.Builder<>(Ids.WEBSERVICES_FORM, metadata)
                .onSave((form, changedValues) -> presenter.saveWebservicesConfiguration(changedValues))
                .prepareReset(form -> presenter.resetWebservicesConfiguration(form, metadata))
                .build();

        HTMLElement webservicesSection = section()
                .add(h(1).textContent(Names.WEBSERVICES_CONFIGURATION))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(webservicesForm)
                .asElement();

        clientConfig = new ConfigElement(CLIENT_CONFIG, metadataRegistry, tableButtonFactory);
        endpointConfig = new ConfigElement(Config.ENDPOINT_CONFIG, metadataRegistry, tableButtonFactory);

        VerticalNavigation navigation = new VerticalNavigation();
        navigation.addPrimary(Ids.WEBSERVICES_ENTRY, Names.CONFIGURATION, pfIcon("settings"), webservicesSection);
        navigation.addPrimary(Ids.WEBSERVICES_CLIENT_CONFIG_ENTRY, Names.CLIENT_CONFIGURATION,
                fontAwesome("laptop"), clientConfig);
        navigation.onShow(Ids.WEBSERVICES_CLIENT_CONFIG_ENTRY, () -> presenter.selectConfig(CLIENT_CONFIG));
        navigation.addPrimary(Ids.WEBSERVICES_ENDPOINT_CONFIG_ENTRY, Names.ENDPOINT_CONFIGURATION,
                pfIcon("service"), endpointConfig);
        navigation.onShow(Ids.WEBSERVICES_ENDPOINT_CONFIG_ENTRY, () -> presenter.selectConfig(ENDPOINT_CONFIG));

        registerAttachables(asList(navigation, webservicesForm, clientConfig, endpointConfig));

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
    }

    @Override
    public void setPresenter(final WebservicePresenter presenter) {
        this.presenter = presenter;
        clientConfig.setPresenter(presenter);
        endpointConfig.setPresenter(presenter);
    }

    @Override
    public void update(final ModelNode payload) {
        webservicesForm.view(payload);
        clientConfig.update(asNamedNodes(failSafePropertyList(payload, CLIENT_CONFIG.resource)));
        endpointConfig.update(asNamedNodes(failSafePropertyList(payload, ENDPOINT_CONFIG.resource)));
    }

    @Override
    public void updateHandlerChains(final Config configType, final HandlerChain handlerChainType,
            final List<NamedNode> handlerChains) {
        if (configType == CLIENT_CONFIG) {
            clientConfig.updateHandlerChains(configType, handlerChainType, handlerChains);
        } else {
            endpointConfig.updateHandlerChains(configType, handlerChainType, handlerChains);
        }
    }

    @Override
    public void updateHandlers(final Config configType, final HandlerChain handlerChainType,
            final List<NamedNode> handlers) {
        if (configType == CLIENT_CONFIG) {
            clientConfig.updateHandlers(configType, handlers);
        } else {
            endpointConfig.updateHandlers(configType, handlers);
        }
    }
}
