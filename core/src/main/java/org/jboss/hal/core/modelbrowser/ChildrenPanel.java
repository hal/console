/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.core.modelbrowser;

import com.google.common.collect.Lists;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.dom.NodeList;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.js.JsHelper;
import org.jboss.hal.ballroom.table.Api.RefreshMode;
import org.jboss.hal.ballroom.table.Button.Scope;
import org.jboss.hal.ballroom.table.ColumnBuilder;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.ballroom.table.OptionsBuilder;
import org.jboss.hal.ballroom.table.Selector;
import org.jboss.hal.ballroom.table.SelectorBuilder;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import java.util.List;

import static org.jboss.hal.ballroom.table.Selector.Page.all;
import static org.jboss.hal.core.modelbrowser.ReadChildren.uniqueId;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.resources.CSS.clickable;

/**
 * Panel which holds the children of the selected resource.
 *
 * @author Harald Pehl
 */
class ChildrenPanel implements HasElements, Attachable {

    private static final String HEADER_ELEMENT = "headerElement";
    private static final String VIEW_RESOURCE_DATA = "resource";

    private final ModelBrowser modelBrowser;
    private final Dispatcher dispatcher;
    private final Elements.Builder builder;
    private final Element header;
    private final DataTable<String> table;
    private Node<Context> parent;

    ChildrenPanel(final ModelBrowser modelBrowser, final Dispatcher dispatcher, final Resources resources) {
        this.modelBrowser = modelBrowser;
        this.dispatcher = dispatcher;

        //noinspection HardCodedStringLiteral
        Options<String> options = new OptionsBuilder<String>()
                .column("resource", Names.RESOURCE, (cell, type, row, meta) -> row)
                .column(new ColumnBuilder<String>("action", resources.constants().action(),
                        (cell, type, row, meta) -> "<a data-" + VIEW_RESOURCE_DATA + "=\"" + row +
                                "\" class=\"" + clickable + "\">" + resources.constants().view() + "</a>")
                        .orderable(false)
                        .searchable(false)
                        .width("10em")
                        .build())
                .button(resources.constants().add(), (event, api) -> {
                    Selector selector = new SelectorBuilder().page(all).build();
                    modelBrowser.add(parent, JsHelper.asList(api.rows(selector).data().toArray()));
                })

                .button(resources.constants().remove(), Scope.SELECTED,
                        (event, api) -> DialogFactory.confirmation(resources.constants().removeResource(),
                                resources.messages().removeResourceConfirmationQuestion(api.selectedRow()),
                                () -> {
                                    ResourceAddress fq = parent.data.getAddress().getParent()
                                            .add(parent.text, api.selectedRow());
                                    modelBrowser.remove(fq);
                                    return true;
                                }).show())
                .paging(false)
                .build();

        table = new DataTable<>(IdBuilder.build(Ids.MODEL_BROWSER, "children", "table"),
                SecurityContext.RWX, options);

        builder = new Elements.Builder()
                .h(1).rememberAs(HEADER_ELEMENT).end()
                .add(table.asElement());
        header = builder.referenceFor(HEADER_ELEMENT);
    }

    @Override
    public Iterable<Element> asElements() {
        return builder.elements();
    }

    @Override
    public void attach() {
        table.attach();
        registerLinkClickHandler();
    }

    private void registerLinkClickHandler() {
        NodeList links = table.asElement().querySelectorAll("." + clickable);
        for (int i = 0; i < links.getLength(); i++) {
            Element link = (Element) links.item(i);
            String name = String.valueOf(link.getDataset().at(VIEW_RESOURCE_DATA));
            link.setOnclick(event -> modelBrowser.tree.api().openNode(parent.id,
                    () -> modelBrowser.select(uniqueId(parent, name), false)));
        }
    }

    @SuppressWarnings("HardCodedStringLiteral")
    void update(final Node<Context> node, final ResourceAddress address) {
        this.parent = node;

        SafeHtmlBuilder safeHtml = new SafeHtmlBuilder();
        if (node.data.hasSingletons()) {
            safeHtml.appendEscaped("Singleton ");
        }
        safeHtml.appendEscaped("Child Resources of ")
                .appendHtmlConstant("<code>")
                .appendEscaped(node.text)
                .appendHtmlConstant("</code>");
        header.setInnerHTML(safeHtml.toSafeHtml().asString());

        Operation operation = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION, address.getParent())
                .param(CHILD_TYPE, node.text)
                .build();
        dispatcher.execute(operation, result -> {
            List<String> names = Lists.transform(result.asList(), ModelNode::asString);
            table.api().clear().add(names).refresh(RefreshMode.RESET);
            registerLinkClickHandler();
            if (node.data.hasSingletons()) {
                Browser.getWindow().getConsole()
                        .log("Read " + names.size() + " / " + node.data.getSingletons().size() + " singletons");
            }
        });
    }

    void show() {
        for (Element element : asElements()) {
            Elements.setVisible(element, true);
        }
        table.show();
    }

    void hide() {
        for (Element element : asElements()) {
            Elements.setVisible(element, false);
        }
        table.hide();
    }
}
