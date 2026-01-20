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
package org.jboss.hal.client.configuration.subsystem.webservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.PropertiesItem;
import org.jboss.hal.ballroom.table.InlineAction;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toMap;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.client.configuration.subsystem.webservice.HandlerChain.POST_HANDLER_CHAIN;
import static org.jboss.hal.client.configuration.subsystem.webservice.HandlerChain.PRE_HANDLER_CHAIN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROPERTY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

/** Element to configure client and endpoint configurations. */
class ConfigElement implements IsElement<HTMLElement>, Attachable, HasPresenter<WebservicePresenter> {

    private final Pages pages;
    private final Table<NamedNode> table;
    private final Form<NamedNode> form;
    private final PropertiesItem propertiesItem;
    private final HandlerChainElement handlerChain;
    private final HandlerElement handler;
    private WebservicePresenter presenter;

    @SuppressWarnings({ "ConstantConditions", "HardCodedStringLiteral" })
    ConfigElement(Config configType, MetadataRegistry metadataRegistry,
            TableButtonFactory tableButtonFactory) {

        List<InlineAction<NamedNode>> inlineActions = new ArrayList<>();
        inlineActions.add(new InlineAction<>("Pre", row -> presenter.showHandlerChains(row, PRE_HANDLER_CHAIN)));
        inlineActions.add(new InlineAction<>("Post", row -> presenter.showHandlerChains(row, POST_HANDLER_CHAIN)));

        Metadata metadata = metadataRegistry.lookup(configType.template);
        table = new ModelNodeTable.Builder<NamedNode>(Ids.build(configType.baseId, Ids.TABLE), metadata)
                .button(tableButtonFactory.add(configType.template, table -> presenter.addConfig()))
                .button(tableButtonFactory.remove(configType.template,
                        table -> presenter.removeConfig(table.selectedRow().getName())))
                .nameColumn()
                .column(inlineActions)
                .build();

        String propertyDescription = metadata.getDescription().children().description(PROPERTY);
        propertiesItem = new PropertiesItem(PROPERTY);
        form = new ModelNodeForm.Builder<NamedNode>(Ids.build(configType.baseId, Ids.FORM), metadata)
                .unboundFormItem(propertiesItem, 0, SafeHtmlUtils.fromString(propertyDescription))
                .onSave((form, changedValues) -> presenter.saveConfig(form, changedValues, PROPERTY))
                .prepareReset(form -> presenter.resetConfig(form))
                .build();

        HTMLElement section = section()
                .add(h(1).textContent(configType.type))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(table)
                .add(form).element();

        handlerChain = new HandlerChainElement(configType, metadataRegistry, tableButtonFactory);
        handler = new HandlerElement(configType, metadataRegistry, tableButtonFactory);

        String id = Ids.build(configType.baseId, Ids.PAGES);
        String mainId = Ids.build(configType.baseId, Ids.PAGE);
        pages = new Pages(id, mainId, section);
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
        return Ids.build(configType.baseId, "handler", Ids.PAGE);
    }

    private String handlerChainPageId(Config configType) {
        return Ids.build(configType.baseId, "handler-chain", Ids.PAGE);
    }

    @Override
    public HTMLElement element() {
        return pages.element();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void attach() {
        table.attach();
        form.attach();
        table.bindForm(form);

        table.onSelectionChange(t -> {
            if (t.hasSelection()) {
                Map<String, String> properties = failSafePropertyList(t.selectedRow(), PROPERTY).stream()
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
    public void setPresenter(WebservicePresenter presenter) {
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

    void updateHandlers(Config configType, List<NamedNode> handlers) {
        handler.update(handlers);
        pages.showPage(handlerPageId(configType));
    }
}
