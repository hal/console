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

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.PropertiesItem;
import org.jboss.hal.ballroom.table.Button;
import org.jboss.hal.ballroom.table.ColumnBuilder;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.stream.Collectors.toMap;
import static org.jboss.hal.client.configuration.subsystem.webservice.HandlerChain.POST_HANDLER_CHAIN;
import static org.jboss.hal.client.configuration.subsystem.webservice.HandlerChain.PRE_HANDLER_CHAIN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROPERTY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.columnAction;

/**
 * Element to configure client and endpoint configurations.
 *
 * @author Harald Pehl
 */
class ConfigElement implements IsElement, Attachable, HasPresenter<WebservicePresenter> {

    private final Pages pages;
    private final NamedNodeTable<NamedNode> table;
    private final Form<NamedNode> form;
    private final PropertiesItem propertiesItem;
    private final HandlerChainElement handlerChain;
    private final HandlerElement handler;
    private WebservicePresenter presenter;

    @SuppressWarnings({"ConstantConditions", "HardCodedStringLiteral"})
    ConfigElement(final Config configType, final MetadataRegistry metadataRegistry, final Resources resources) {

        Metadata metadata = metadataRegistry.lookup(configType.template);
        Options<NamedNode> options = new ModelNodeTable.Builder<NamedNode>(metadata)
                .button(resources.constants().add(), (event, api) -> presenter.addConfig())
                .button(resources.constants().add(), Button.Scope.SELECTED,
                        (event, api) -> presenter.removeConfig(api.selectedRow().getName()))
                .column(NAME, (cell, t, row, meta) -> row.getName())
                .column(columnActions -> new ColumnBuilder<NamedNode>(Ids.WEBSERVICES_HANDLER_CHAIN_COLUMN,
                        Names.HANDLER_CHAIN,
                        (cell, t, row, meta) -> {
                            String id1 = Ids.uniqueId();
                            String id2 = Ids.uniqueId();
                            columnActions.add(id1, row1 -> presenter.showHandlerChains(row1, PRE_HANDLER_CHAIN));
                            columnActions.add(id2, row2 -> presenter.showHandlerChains(row2, POST_HANDLER_CHAIN));
                            return "<a id=\"" + id1 + "\" class=\"" + columnAction + "\">Pre</a> / " +
                                    "<a id=\"" + id2 + "\" class=\"" + columnAction + "\">Post</a>";
                        })
                        .orderable(false)
                        .searchable(false)
                        .width("12em")
                        .build())
                .build();
        table = new NamedNodeTable<>(Ids.build(configType.baseId, Ids.TABLE_SUFFIX), options);

        ModelNode propertyDescription = failSafeGet(metadata.getDescription(), "children/property/description");
        propertiesItem = new PropertiesItem(PROPERTY);
        form = new ModelNodeForm.Builder<NamedNode>(Ids.build(configType.baseId, Ids.FORM_SUFFIX), metadata)
                .unboundFormItem(propertiesItem, 0, SafeHtmlUtils.fromString(propertyDescription.asString()))
                .onSave((form, changedValues) -> presenter.saveConfig(form, changedValues, PROPERTY))
                .build();

        // @formatter:off
        Element section = new Elements.Builder()
            .section()
                .h(1).textContent(configType.type).end()
                .p().textContent(metadata.getDescription().getDescription()).end()
                .add(table)
                .add(form)
            .end()
        .build();
        // @formatter:on

        handlerChain = new HandlerChainElement(configType, metadataRegistry, resources);
        handler = new HandlerElement(configType, metadataRegistry, resources);

        String mainId = Ids.build(configType.baseId, Ids.PAGE_SUFFIX);
        pages = new Pages(mainId, section);
        pages.addPage(mainId, handlerChainPageId(configType),
                () -> presenter.configSegment(),
                () -> presenter.handlerChainTypeSegment(),
                handlerChain);
        pages.addPage(handlerChainPageId(configType), handlerPageId(configType),
                () -> presenter.handlerChainSegment(),
                () -> Names.HANDLER,
                handler);
    }

    private String handlerPageId(Config configType) {
        return Ids.build(configType.baseId, "handler", Ids.PAGE_SUFFIX);
    }

    private String handlerChainPageId(Config configType) {
        return Ids.build(configType.baseId, "handler-chain", Ids.PAGE_SUFFIX);
    }

    @Override
    public Element asElement() {
        return pages.asElement();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void attach() {
        table.attach();
        form.attach();
        table.bindForm(form);

        table.api().onSelectionChange(api -> {
            if (api.hasSelection()) {
                Map<String, String> properties = failSafePropertyList(api.selectedRow(), PROPERTY).stream()
                        .collect(toMap(Property::getName, property -> property.getValue().get(VALUE).asString()));
                propertiesItem.setValue(properties);
            } else {
                propertiesItem.clearValue();
            }
        });

        handlerChain.attach();
        handler.attach();
    }

    @Override
    public void detach() {
        handler.detach();
        handlerChain.detach();
        form.detach();
        table.detach();
    }

    @Override
    public void setPresenter(final WebservicePresenter presenter) {
        this.presenter = presenter;
        handlerChain.setPresenter(presenter);
        handler.setPresenter(presenter);
    }

    void update(List<NamedNode> configItems) {
        form.clear();
        table.update(configItems);
    }

    void updateHandlerChains(Config configType, HandlerChain handlerChainType, List<NamedNode> handlerChainItems) {
        handlerChain.update(handlerChainType, handlerChainItems);
        pages.showPage(handlerChainPageId(configType));
    }

    void updateHandlers(Config configType, HandlerChain handlerChainType, List<NamedNode> handlers) {
        handler.update(handlers);
        pages.showPage(handlerPageId(configType));
    }
}
