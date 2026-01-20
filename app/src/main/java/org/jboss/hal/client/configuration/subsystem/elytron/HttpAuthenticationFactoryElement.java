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
package org.jboss.hal.client.configuration.subsystem.elytron;

import java.util.List;

import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.InlineAction;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeList;
import static org.jboss.hal.dmr.ModelNodeHelper.storeIndex;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.PAGE;
import static org.jboss.hal.resources.Ids.PAGES;

class HttpAuthenticationFactoryElement implements IsElement<HTMLElement>, Attachable,
        HasPresenter<FactoriesPresenter> {

    private final Table<NamedNode> factoryTable;
    private final Form<NamedNode> factoryForm;
    private final Table<ModelNode> mcTable; // mc = mechanism-configuration
    private final Form<ModelNode> mcForm;
    private final Table<ModelNode> mrcTable; // mrc = mechanism-realm-configurations
    private final Form<ModelNode> mrcForm;
    private final Pages pages;
    private FactoriesPresenter presenter;
    private String selectedFactory;
    private String selectedMc;
    private int mcIndex;
    private int mrcIndex;

    HttpAuthenticationFactoryElement(Metadata metadata, TableButtonFactory tableButtonFactory) {
        // HTTP authentication factory
        factoryTable = new ModelNodeTable.Builder<NamedNode>(id(Ids.TABLE), metadata)
                .button(tableButtonFactory.add(id(Ids.ADD), Names.HTTP_AUTHENTICATION_FACTORY,
                        metadata.getTemplate(), (n, a) -> presenter.reloadHttpAuthenticationFactories()))
                .button(tableButtonFactory.remove(Names.HTTP_AUTHENTICATION_FACTORY, metadata.getTemplate(),
                        (table) -> table.selectedRow().getName(), () -> presenter.reloadHttpAuthenticationFactories()))
                .nameColumn()
                .column(new InlineAction<>(Names.MECHANISM_CONFIGURATIONS, this::showMechanismConfiguration), "15em")
                .build();
        factoryForm = new ModelNodeForm.Builder<NamedNode>(id(FORM), metadata)
                .onSave((form, changedValues) -> presenter.saveHttpAuthenticationFactory(form, changedValues))
                .build();
        HTMLElement factorySection = section()
                .add(h(1).textContent(Names.HTTP_AUTHENTICATION_FACTORY))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .addAll(factoryTable, factoryForm).element();

        // mechanism configurations
        Metadata mcMetadata = metadata.forComplexAttribute(MECHANISM_CONFIGURATIONS);
        mcTable = new ModelNodeTable.Builder<>(id(MECHANISM_CONFIGURATIONS, TABLE), mcMetadata)
                .button(tableButtonFactory.add(mcMetadata.getTemplate(),
                        table -> presenter.addHttpMechanismConfiguration(selectedFactory)))
                .button(tableButtonFactory.remove(mcMetadata.getTemplate(),
                        table -> presenter.removeHttpMechanismConfiguration(selectedFactory,
                                table.selectedRow().get(HAL_INDEX).asInt())))
                .column(MECHANISM_NAME)
                .column(new InlineAction<>(Names.MECHANISM_REALM_CONFIGURATIONS, this::showMechanismRealmConfiguration),
                        "20em")
                .build();
        mcForm = new ModelNodeForm.Builder<>(id(MECHANISM_CONFIGURATIONS, FORM), mcMetadata)
                .onSave(((form, changedValues) -> presenter.saveHttpMechanismConfiguration(selectedFactory,
                        form.getModel().get(HAL_INDEX).asInt(), changedValues)))
                .build();
        HTMLElement mcSection = section()
                .add(h(1).textContent(Names.MECHANISM_CONFIGURATIONS))
                .add(p().textContent(mcMetadata.getDescription().getDescription()))
                .addAll(mcTable, mcForm).element();

        // mechanism realm configurations
        Metadata mrcMetadata = mcMetadata.forComplexAttribute(MECHANISM_REALM_CONFIGURATIONS);
        mrcTable = new ModelNodeTable.Builder<>(id(MECHANISM_REALM_CONFIGURATIONS, Ids.TABLE), mrcMetadata)
                .button(tableButtonFactory.add(mrcMetadata.getTemplate(),
                        table -> presenter.addHttpMechanismRealmConfiguration(selectedFactory, mcIndex)))
                .button(tableButtonFactory.remove(mrcMetadata.getTemplate(),
                        table -> presenter.removeHttpMechanismRealmConfiguration(selectedFactory, mcIndex,
                                table.selectedRow().get(HAL_INDEX).asInt())))
                .column(REALM_NAME)
                .build();
        mrcForm = new ModelNodeForm.Builder<>(id(MECHANISM_REALM_CONFIGURATIONS, FORM), mrcMetadata)
                .onSave(((form, changedValues) -> presenter.saveHttpMechanismRealmConfiguration(selectedFactory,
                        mcIndex,
                        mrcIndex, changedValues)))
                .build();
        HTMLElement mrcSection = section()
                .add(h(1).textContent(Names.MECHANISM_REALM_CONFIGURATIONS))
                .add(p().textContent(mrcMetadata.getDescription().getDescription()))
                .addAll(mrcTable, mrcForm).element();

        pages = new Pages(id(PAGES), id(PAGE), factorySection);
        pages.addPage(id(PAGE), id(MECHANISM_CONFIGURATIONS, PAGE),
                () -> Names.HTTP_AUTHENTICATION_FACTORY + ": " + selectedFactory,
                () -> Names.MECHANISM_CONFIGURATIONS,
                mcSection);
        pages.addPage(id(MECHANISM_CONFIGURATIONS, PAGE), id(MECHANISM_REALM_CONFIGURATIONS, PAGE),
                () -> Names.MECHANISM_CONFIGURATIONS + ": " + selectedMc,
                () -> Names.MECHANISM_REALM_CONFIGURATIONS,
                mrcSection);
    }

    private String id(String... ids) {
        return Ids.build(Ids.ELYTRON_HTTP_AUTHENTICATION_FACTORY, ids);
    }

    @Override
    public HTMLElement element() {
        return pages.element();
    }

    @Override
    public void attach() {
        factoryTable.attach();
        factoryForm.attach();
        factoryTable.bindForm(factoryForm);

        mcTable.attach();
        mcForm.attach();
        mcTable.bindForm(mcForm);
        mcTable.onSelectionChange(table -> mcTable.enableButton(1, mcTable.hasSelection()));

        mrcTable.attach();
        mrcForm.attach();
        mrcTable.bindForm(mrcForm);
        mrcTable.onSelectionChange(table -> {
            mrcTable.enableButton(1, mrcTable.hasSelection());
            if (table.hasSelection()) {
                mrcIndex = table.selectedRow().get(HAL_INDEX).asInt();
            }
        });
    }

    @Override
    public void setPresenter(FactoriesPresenter presenter) {
        this.presenter = presenter;
    }

    void update(List<NamedNode> nodes) {
        factoryForm.clear();
        factoryTable.update(nodes);

        if (id(MECHANISM_CONFIGURATIONS, PAGE).equals(pages.getCurrentId())) {
            nodes.stream()
                    .filter(factory -> selectedFactory.equals(factory.getName()))
                    .findFirst()
                    .ifPresent(this::showMechanismConfiguration);
        } else if (id(MECHANISM_REALM_CONFIGURATIONS, PAGE).equals(pages.getCurrentId())) {
            nodes.stream()
                    .filter(factory -> selectedFactory.equals(factory.getName()))
                    .findFirst()
                    .ifPresent(factory -> {
                        List<ModelNode> mcNodes = failSafeList(factory, MECHANISM_CONFIGURATIONS);
                        storeIndex(mcNodes);
                        mcForm.clear();
                        mcTable.update(mcNodes, modelNode -> modelNode.get(MECHANISM_NAME).asString());
                        mcNodes.stream()
                                .filter(mc -> selectedMc.equals(mc.get(MECHANISM_NAME).asString()))
                                .findFirst()
                                .ifPresent(this::showMechanismRealmConfiguration);
                    });
        }
    }

    private void showMechanismConfiguration(NamedNode httpAuthenticationFactory) {
        selectedFactory = httpAuthenticationFactory.getName();
        List<ModelNode> mcNodes = failSafeList(httpAuthenticationFactory, MECHANISM_CONFIGURATIONS);
        storeIndex(mcNodes);
        mcForm.clear();
        mcTable.update(mcNodes, modelNode -> modelNode.get(MECHANISM_NAME).asString());
        mcTable.enableButton(1, mcTable.hasSelection());
        pages.showPage(id(MECHANISM_CONFIGURATIONS, PAGE));
    }

    private void showMechanismRealmConfiguration(ModelNode mechanismConfiguration) {
        selectedMc = mechanismConfiguration.get(MECHANISM_NAME).asString();
        mcIndex = mechanismConfiguration.get(HAL_INDEX).asInt();
        List<ModelNode> mrcNodes = failSafeList(mechanismConfiguration, MECHANISM_REALM_CONFIGURATIONS);
        storeIndex(mrcNodes);
        mrcForm.clear();
        mrcTable.update(mrcNodes, modelNode -> modelNode.get(REALM_NAME).asString());
        mrcTable.enableButton(1, mrcTable.hasSelection());
        pages.showPage(id(MECHANISM_REALM_CONFIGURATIONS, PAGE));
    }
}
