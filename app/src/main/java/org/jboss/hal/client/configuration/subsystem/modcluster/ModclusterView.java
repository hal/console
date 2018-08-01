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
package org.jboss.hal.client.configuration.subsystem.modcluster;

import java.util.List;

import javax.annotation.PostConstruct;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.client.configuration.subsystem.modcluster.AddressTemplates.DYNAMIC_LOAD_PROVIDER_TEMPLATE;
import static org.jboss.hal.resources.Names.DYNAMIC_LOAD_PROVIDER;

@MbuiView
@SuppressWarnings("DuplicateStringLiteralInspection")
public abstract class ModclusterView extends MbuiViewImpl<ModclusterPresenter> implements ModclusterPresenter.MyView {

    public static ModclusterView create(final MbuiContext mbuiContext) {
        return new Mbui_ModclusterView(mbuiContext);
    }

    @MbuiElement("proxy-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("proxy-conf-form") Form<ModelNode> configurationForm;
    @MbuiElement("load-metrics-table") Table<NamedNode> loadMetricTable;
    @MbuiElement("load-metrics-form") Form<NamedNode> loadMetricForm;
    @MbuiElement("custom-load-metrics-table") Table<NamedNode> customLoadMetricTable;
    @MbuiElement("custom-load-metrics-form") Form<NamedNode> customLoadMetricForm;
    private Form<ModelNode> dynamicLoadProviderForm;
    private Alert alertDynamicLoadProvider = new Alert(Icons.WARNING,
            mbuiContext.resources().messages().dynamicLoadProviderWarning());
    private Alert alertDynamicLoadProvider2 = new Alert(Icons.WARNING,
            mbuiContext.resources().messages().dynamicLoadProviderWarning());

    ModclusterView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @PostConstruct
    void init() {
        Metadata metadata = mbuiContext.metadataRegistry().lookup(DYNAMIC_LOAD_PROVIDER_TEMPLATE);
        dynamicLoadProviderForm = new ModelNodeForm.Builder<>("dynamic-load-provider-form", metadata)
                .singleton(
                        () -> presenter.dynamicLoadProviderOperation(),
                        () -> presenter.addDynamicLoadProvider())
                .prepareRemove(form -> presenter.removeDynamicLoadProvider())
                .onSave((form, changedValues) -> {
                    String name = presenter.getProxyName();
                    AddressTemplate template = DYNAMIC_LOAD_PROVIDER_TEMPLATE.replaceWildcards(
                            presenter.getProxyName());
                    saveForm(Names.DYNAMIC_LOAD_PROVIDER, name, template.resolve(mbuiContext.statementContext(), name),
                            changedValues, metadata);
                })
                .prepareReset(form -> {
                    String name = presenter.getProxyName();
                    AddressTemplate template = DYNAMIC_LOAD_PROVIDER_TEMPLATE.replaceWildcards(
                            presenter.getProxyName());
                    resetForm(Names.DYNAMIC_LOAD_PROVIDER, name, template.resolve(mbuiContext.statementContext(), name),
                            form, metadata);
                })
                .build();
        HTMLElement dynamicLoadProviderElement = section()
                .add(div()
                        .add(h(1).textContent(Names.DYNAMIC_LOAD_PROVIDER))
                        .add(p().textContent(metadata.getDescription().getDescription()))
                        .asElement())
                .add(dynamicLoadProviderForm)
                .asElement();
        navigation.insertPrimary("dynamic-load-provider-item", "custom-load-metrics-item", DYNAMIC_LOAD_PROVIDER,
                "fa fa-shield", dynamicLoadProviderElement);
        registerAttachable(dynamicLoadProviderForm);
    }

    @Override
    public void attach() {
        super.attach();
        Element customLoadMetricElement = asElement().querySelector("section[data-vn-item-for=custom-load-metrics-item] > div");
        Element loadMetricElement = asElement().querySelector("section[data-vn-item-for=load-metrics-item] > div");
        loadMetricElement.appendChild(alertDynamicLoadProvider.asElement());
        customLoadMetricElement.appendChild(alertDynamicLoadProvider2.asElement());
    }

    @Override
    public void updateConfiguration(final ModelNode payload) {
        configurationForm.view(payload);
    }

    @Override
    public void updateDynamicLoadProvider(ModelNode payload) {
        dynamicLoadProviderForm.view(payload);
        // the dynamic-load-provider=configuration resource is a parent resource of the load-metrics tables
        // disable the "add" buttons if there is no dynamic-load-provider=configuration
        customLoadMetricTable.enableButton(0, payload.isDefined());
        loadMetricTable.enableButton(0, payload.isDefined());
        Elements.setVisible(alertDynamicLoadProvider.asElement(), !payload.isDefined());
        Elements.setVisible(alertDynamicLoadProvider2.asElement(), !payload.isDefined());
    }

    @Override
    public void updateCustomLoadMetrics(List<NamedNode> nodes) {
        customLoadMetricForm.clear();
        customLoadMetricTable.update(nodes);
    }

    @Override
    public void updateLoadMetrics(List<NamedNode> nodes) {
        loadMetricForm.clear();
        loadMetricTable.update(nodes);
    }
}
