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

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.client.configuration.subsystem.webservice.AddressTemplates.HANDLER_CHAIN_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * Element to configure handler chains of a client or endpoint configuration.
 *
 * @author Harald Pehl
 */
class HandlerChainElement implements IsElement, Attachable, HasPresenter<WebservicePresenter> {

    private static final String HEADER_ELEMENT = "headerElement";

    private final Element root;
    private final Element header;
    private final Table<NamedNode> table;
    private final Form<NamedNode> form;
    private WebservicePresenter presenter;

    @SuppressWarnings("ConstantConditions")
    HandlerChainElement(final Config configType, final MetadataRegistry metadataRegistry,
            final TableButtonFactory tableButtonFactory) {

        String tableId = Ids.build(configType.baseId, "handler-chain", Ids.TABLE_SUFFIX);
        Metadata metadata = metadataRegistry.lookup(HANDLER_CHAIN_TEMPLATE);
        table = new ModelNodeTable.Builder<NamedNode>(tableId, metadata)
                .button(tableButtonFactory.add(HANDLER_CHAIN_TEMPLATE, table -> presenter.addHandlerChain()))
                .button(tableButtonFactory.remove(HANDLER_CHAIN_TEMPLATE,
                        table -> presenter.removeHandlerChain(table.selectedRow().getName())))
                .column(NAME, (cell, t, row, meta) -> row.getName())
                .column(Names.HANDLER, row -> presenter.showHandlers(row))
                .build();

        String formId = Ids.build(configType.baseId, "handler-chain", Ids.FORM_SUFFIX);
        form = new ModelNodeForm.Builder<NamedNode>(formId, metadata)
                .onSave((form, changedValues) -> presenter.saveHandlerChain(form.getModel().getName(), changedValues))
                .prepareReset(form -> presenter.resetHandlerChain(form.getModel().getName(), form))
                .build();

        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .section()
                .h(1).rememberAs(HEADER_ELEMENT).end()
                .add(table)
                .add(form)
            .end();
        // @formatter:on

        header = builder.referenceFor(HEADER_ELEMENT);
        root = builder.build();
    }

    @Override
    public Element asElement() {
        return root;
    }

    @Override
    public void attach() {
        table.attach();
        form.attach();
        table.bindForm(form);
    }

    @Override
    public void detach() {
        form.detach();
        table.detach();
    }

    @Override
    public void setPresenter(final WebservicePresenter presenter) {
        this.presenter = presenter;
    }

    void update(HandlerChain handlerChainType, List<NamedNode> handlerChains) {
        header.setTextContent(handlerChainType.type);
        form.clear();
        table.update(handlerChains);
    }
}