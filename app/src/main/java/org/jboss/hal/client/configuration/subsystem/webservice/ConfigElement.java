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
import java.util.Map;

import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.PropertiesItem;
import org.jboss.hal.ballroom.table.ColumnBuilder;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static java.util.stream.Collectors.toMap;
import static org.jboss.hal.client.configuration.subsystem.webservice.AddressTemplates.CLIENT_CONFIG_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.columnAction;
import static org.jboss.hal.resources.Ids.ADD_SUFFIX;
import static org.jboss.hal.resources.Ids.FORM_SUFFIX;
import static org.jboss.hal.resources.Ids.TABLE_SUFFIX;

/**
 * An element to configure the client and endpoint configurations with their pre- and post-handler chains.
 *
 * @author Harald Pehl
 */
class ConfigElement implements IsElement, Attachable {

    private static final String PRE_CHAIN = "pre-chain";
    private static final String POST_CHAIN = "post-chain";

    private final String configType;
    private String configName;

    private final Pages pages;

    private final NamedNodeTable<NamedNode> configTable;
    private final PropertiesItem configProperties;
    private final Form<NamedNode> configForm;

    private final String preChainPage;
    private final NamedNodeTable<NamedNode> preChainTable;
    private final Form<NamedNode> preChainForm;
    // private final NamedNodeTable<NamedNode> preHandlerTable;
    // private final Form<NamedNode> preHandlerForm;

    private final String postChainPage;
    private final NamedNodeTable<NamedNode> postChainTable;
    private final Form<NamedNode> postChainForm;
    // private final NamedNodeTable<NamedNode> postHandlerTable;
    // private final Form<NamedNode> postHandlerForm;

    @SuppressWarnings({"ConstantConditions", "HardCodedStringLiteral"})
    ConfigElement(final WebservicePresenter presenter, final MbuiContext mbuiContext,
            final String baseId, final String type, final AddressTemplate template) {
        this.configType = type;

        // ------------------------------------------------------ config

        Metadata configMetadata = mbuiContext.metadataRegistry().lookup(template);
        Options<NamedNode> configOptions = new ModelNodeTable.Builder<NamedNode>(configMetadata)
                .button(mbuiContext.tableButtonFactory()
                        .add(Ids.build(baseId, ADD_SUFFIX), type, template, (n, a) -> presenter.reload()))
                .button(mbuiContext.tableButtonFactory()
                        .remove(type, template, api -> api.selectedRow().getName(), presenter::reload))
                .column(NAME, (cell, t, row, meta) -> row.getName())
                .column(columnActions -> new ColumnBuilder<NamedNode>(Ids.WEBSERVICES_HANDLER_CHAIN_COLUMN,
                        Names.HANDLER_CHAIN,
                        (cell, t, row, meta) -> {
                            String id1 = Ids.uniqueId();
                            String id2 = Ids.uniqueId();
                            columnActions.add(id1, this::preHandlerChain);
                            columnActions.add(id2, this::postHandlerChain);
                            return "<a id=\"" + id1 + "\" class=\"" + columnAction + "\">Pre</a> / " +
                                    "<a id=\"" + id2 + "\" class=\"" + columnAction + "\">Post</a>";
                        })
                        .orderable(false)
                        .searchable(false)
                        .width("12em")
                        .build())
                .build();
        configTable = new NamedNodeTable<>(Ids.build(baseId, TABLE_SUFFIX), configOptions);

        configProperties = new PropertiesItem(PROPERTY, mbuiContext.resources().constants().properties());
        configForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(baseId, FORM_SUFFIX), configMetadata)
                .unboundFormItem(configProperties)
                .onSave((form, changedValues) -> mbuiContext.po()
                        .saveWithProperties(Names.CLIENT_CONFIG, form.getModel().getName(), CLIENT_CONFIG_TEMPLATE,
                                form, changedValues, PROPERTY, presenter::reload))
                .build();

        // @formatter:off
        Element configSection = new Elements.Builder()
            .section()
                .h(1).textContent(Names.CLIENT_CONFIG).end()
                .p().textContent(configMetadata.getDescription().getDescription()).end()
                .add(configTable)
                .add(configForm)
            .end()
        .build();
        // @formatter:on

        // ------------------------------------------------------ pre handler chain

        preChainPage = Ids.build(baseId, PRE_CHAIN, "page");
        AddressTemplate preChainTemplate = template.append("/" + PRE_HANDLER_CHAIN + "=*");
        Metadata preChainMetadata = mbuiContext.metadataRegistry().lookup(preChainTemplate);
        Options<NamedNode> preChainOptions = new ModelNodeTable.Builder<NamedNode>(configMetadata)
                .button(mbuiContext.tableButtonFactory()
                        .add(Ids.build(baseId, PRE_CHAIN, ADD_SUFFIX), type, template, (n, a) -> presenter.reload()))
                .button(mbuiContext.tableButtonFactory()
                        .remove(type, template, api -> api.selectedRow().getName(), presenter::reload))
                .column(NAME, (cell, t, row, meta) -> row.getName())
                .column(Names.HANDLER, row -> handler())
                .build();
        preChainTable = new NamedNodeTable<>(Ids.build(baseId, PRE_CHAIN, TABLE_SUFFIX), preChainOptions);

        preChainForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(baseId, PRE_CHAIN, FORM_SUFFIX),
                preChainMetadata)
                .onSave((form, changedValues) -> {
                    String name = form.getModel().getName();
                    ResourceAddress address = preChainTemplate.resolve(mbuiContext.statementContext(),
                            configTable.api().selectedRow().getName(), name);
                    mbuiContext.crud().save(Names.PRE_HANDLER_CHAIN, name, address, changedValues, presenter::reload);
                })
                .build();

        // @formatter:off
        Element preChainSection = new Elements.Builder()
            .section()
                .h(1).textContent(Names.PRE_HANDLER_CHAIN).end()
                .p().textContent(preChainMetadata.getDescription().getDescription()).end()
                .add(preChainTable)
                .add(preChainForm)
            .end()
        .build();
        // @formatter:on

        // ------------------------------------------------------ post handler chain

        postChainPage = Ids.build(baseId, POST_CHAIN, "page");
        AddressTemplate postChainTemplate = template.append("/" + POST_HANDLER_CHAIN + "=*");
        Metadata postChainMetadata = mbuiContext.metadataRegistry().lookup(postChainTemplate);
        Options<NamedNode> postChainOptions = new ModelNodeTable.Builder<NamedNode>(configMetadata)
                .button(mbuiContext.tableButtonFactory()
                        .add(Ids.build(baseId, POST_CHAIN, ADD_SUFFIX), type, template, (n, a) -> presenter.reload()))
                .button(mbuiContext.tableButtonFactory()
                        .remove(type, template, api -> api.selectedRow().getName(), presenter::reload))
                .column(NAME, (cell, t, row, meta) -> row.getName())
                .column(Names.HANDLER, row -> handler())
                .build();
        postChainTable = new NamedNodeTable<>(Ids.build(baseId, POST_CHAIN, TABLE_SUFFIX), postChainOptions);

        postChainForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(baseId, POST_CHAIN, FORM_SUFFIX),
                postChainMetadata)
                .onSave((form, changedValues) -> {
                    String name = form.getModel().getName();
                    ResourceAddress address = postChainTemplate.resolve(mbuiContext.statementContext(),
                            configTable.api().selectedRow().getName(), name);
                    mbuiContext.crud().save(Names.POST_HANDLER_CHAIN, name, address, changedValues, presenter::reload);
                })
                .build();

        // @formatter:off
        Element postChainSection = new Elements.Builder()
            .section()
                .h(1).textContent(Names.POST_HANDLER_CHAIN).end()
                .p().textContent(postChainMetadata.getDescription().getDescription()).end()
                .add(postChainTable)
                .add(postChainForm)
            .end()
        .build();
        // @formatter:on

        // ------------------------------------------------------ pages

        String mainId = Ids.build(baseId, "page");
        pages = new Pages(mainId, () -> configType + ": " + configName, configSection);
        pages.addPage(mainId, preChainPage, Names.PRE_HANDLER_CHAIN, preChainSection);
        pages.addPage(mainId, postChainPage, Names.POST_HANDLER_CHAIN, postChainSection);
    }

    @Override
    public Element asElement() {
        return pages.asElement();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void attach() {
        configTable.attach();
        configForm.attach();
        configTable.bindForm(configForm);
        configTable.api().onSelectionChange(api -> {
            if (api.hasSelection()) {
                Map<String, String> properties = failSafePropertyList(api.selectedRow(), PROPERTY).stream()
                        .collect(toMap(Property::getName, property -> property.getValue().get(VALUE).asString()));
                configProperties.setValue(properties);
            } else {
                configProperties.clearValue();
            }
        });

        preChainTable.attach();
        preChainForm.attach();
        preChainTable.bindForm(preChainForm);

        postChainTable.attach();
        postChainForm.attach();
        postChainTable.bindForm(postChainForm);
    }

    private void preHandlerChain(NamedNode config) {
        configName = config.getName();
        preChainForm.clear();
        preChainTable.update(asNamedNodes(failSafePropertyList(config, PRE_HANDLER_CHAIN)));

        pages.showPage(preChainPage);
    }

    private void postHandlerChain(NamedNode config) {
        configName = config.getName();
        postChainForm.clear();
        postChainTable.update(asNamedNodes(failSafePropertyList(config, POST_HANDLER_CHAIN)));

        pages.showPage(postChainPage);
    }

    private void handler() {
        Browser.getWindow().alert("Handler " + Names.NYI);
    }

    void update(List<NamedNode> items) {
        configForm.clear();
        configTable.update(items);

        if (configTable.api().hasSelection()) {
            NamedNode config = configTable.api().selectedRow();
            preChainForm.clear();
            preChainTable.update(asNamedNodes(failSafePropertyList(config, PRE_HANDLER_CHAIN)));
            postChainForm.clear();
            postChainTable.update(asNamedNodes(failSafePropertyList(config, POST_HANDLER_CHAIN)));
        }
    }
}
