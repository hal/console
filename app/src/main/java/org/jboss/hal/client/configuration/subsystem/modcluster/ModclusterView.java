/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.hal.client.configuration.subsystem.modcluster.AddressTemplates.LOAD_PROVIDER_DYNAMIC_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.modcluster.AddressTemplates.LOAD_PROVIDER_SIMPLE_TEMPLATE;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.Names.LOAD_PROVIDER_DYNAMIC;
import static org.jboss.hal.resources.Names.LOAD_PROVIDER_SIMPLE;

@MbuiView
@SuppressWarnings("DuplicateStringLiteralInspection")
public abstract class ModclusterView extends MbuiViewImpl<ModclusterPresenter> implements ModclusterPresenter.MyView {

    public static ModclusterView create(MbuiContext mbuiContext) {
        return new Mbui_ModclusterView(mbuiContext);
    }

    @MbuiElement("proxy-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("proxy-conf-form") Form<ModelNode> configurationForm;
    @MbuiElement("load-metrics-table") Table<NamedNode> loadMetricTable;
    @MbuiElement("load-metrics-form") Form<NamedNode> loadMetricForm;
    @MbuiElement("custom-load-metrics-table") Table<NamedNode> customLoadMetricTable;
    @MbuiElement("custom-load-metrics-form") Form<NamedNode> customLoadMetricForm;
    private Form<ModelNode> loadProviderSimpleForm;
    private Form<ModelNode> loadProviderDynamicForm;
    private Alert alertLoadProviderDynamic = new Alert(Icons.WARNING,
            mbuiContext.resources().messages().loadProviderDynamicWarning());
    private Alert alertLoadProviderDynamic2 = new Alert(Icons.WARNING,
            mbuiContext.resources().messages().loadProviderDynamicWarning());


    ModclusterView(MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @PostConstruct
    void init() {

        // --------- load-provider=dynamic

        Metadata dynamicMetadata = mbuiContext.metadataRegistry().lookup(LOAD_PROVIDER_DYNAMIC_TEMPLATE);
        loadProviderDynamicForm = new ModelNodeForm.Builder<>("load-provider-dynamic-form", dynamicMetadata)
                .singleton(
                        () -> presenter.loadProviderDynamicOperation(),
                        () -> presenter.addLoadProviderDynamic())
                .prepareRemove(form -> presenter.removeLoadProviderDynamic())
                .onSave((form, changedValues) -> {
                    String name = presenter.getProxyName();
                    AddressTemplate template = LOAD_PROVIDER_DYNAMIC_TEMPLATE.replaceWildcards(
                            presenter.getProxyName());
                    saveForm(Names.LOAD_PROVIDER_DYNAMIC, name, template.resolve(mbuiContext.statementContext(), name),
                            changedValues, dynamicMetadata);
                })
                .prepareReset(form -> {
                    String name = presenter.getProxyName();
                    AddressTemplate template = LOAD_PROVIDER_DYNAMIC_TEMPLATE.replaceWildcards(
                            presenter.getProxyName());
                    resetForm(Names.LOAD_PROVIDER_DYNAMIC, name, template.resolve(mbuiContext.statementContext(), name),
                            form, dynamicMetadata);
                })
                .build();
        HTMLElement loadProviderDynamicElement = section()
                .add(div()
                        .add(h(1).textContent(Names.LOAD_PROVIDER_DYNAMIC))
                        .add(p().textContent(dynamicMetadata.getDescription().getDescription()))
                        .get())
                .add(loadProviderDynamicForm)
                .get();
        navigation.insertPrimary("load-provider-dynamic-item", "custom-load-metrics-item", LOAD_PROVIDER_DYNAMIC,
                "fa fa-shield", loadProviderDynamicElement);
        registerAttachable(loadProviderDynamicForm);

        // --------- load-provider=simple

        Metadata simpleMetadata = mbuiContext.metadataRegistry().lookup(LOAD_PROVIDER_SIMPLE_TEMPLATE);
        loadProviderSimpleForm = new ModelNodeForm.Builder<>("load-provider-simple-form", simpleMetadata)
                .singleton(
                        () -> presenter.loadProviderSimpleOperation(),
                        () -> presenter.addLoadProviderSimple())
                .prepareRemove(form -> presenter.removeLoadProviderSimple())
                .onSave((form, changedValues) -> {
                    String name = presenter.getProxyName();
                    AddressTemplate template = LOAD_PROVIDER_SIMPLE_TEMPLATE.replaceWildcards(
                            presenter.getProxyName());
                    saveForm(LOAD_PROVIDER_SIMPLE, name, template.resolve(mbuiContext.statementContext(), name),
                            changedValues, simpleMetadata);
                })
                .prepareReset(form -> {
                    String name = presenter.getProxyName();
                    AddressTemplate template = LOAD_PROVIDER_SIMPLE_TEMPLATE.replaceWildcards(
                            presenter.getProxyName());
                    resetForm(LOAD_PROVIDER_SIMPLE, name, template.resolve(mbuiContext.statementContext(), name),
                            form, simpleMetadata);
                })
                .build();
        HTMLElement loadProviderSimpleElement = section()
                .add(div()
                        .add(h(1).textContent(LOAD_PROVIDER_SIMPLE))
                        .add(p().textContent(simpleMetadata.getDescription().getDescription()))
                        .get())
                .add(loadProviderSimpleForm)
                .get();
        navigation.insertPrimary("load-provider-simple-item", "load-provider-dynamic-item", LOAD_PROVIDER_SIMPLE,
                "fa fa-exchange", loadProviderSimpleElement);
        registerAttachable(loadProviderSimpleForm);



    }

    @Override
    public void attach() {
        super.attach();
        Element customLoadMetricElement = element().querySelector(
                "section[data-vn-item-for=custom-load-metrics-item] > div");
        Element loadMetricElement = element().querySelector("section[data-vn-item-for=load-metrics-item] > div");
        loadMetricElement.appendChild(alertLoadProviderDynamic.element());
        customLoadMetricElement.appendChild(alertLoadProviderDynamic2.element());
    }

    @Override
    public void updateConfiguration(ModelNode payload) {
        configurationForm.view(payload);
        loadProviderSimpleForm.view(failSafeGet(payload, "load-provider/simple"));
    }

    @Override
    public void updateLoadProviderDynamic(ModelNode payload) {
        loadProviderDynamicForm.view(payload);
        // the load-provider=dynamic resource is a parent resource of the load-metrics tables
        // disable the "add" buttons if there is no load-provider=dynamic
        customLoadMetricTable.enableButton(0, payload.isDefined());
        loadMetricTable.enableButton(0, payload.isDefined());
        setVisible(alertLoadProviderDynamic.element(), !payload.isDefined());
        setVisible(alertLoadProviderDynamic2.element(), !payload.isDefined());
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
