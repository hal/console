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
package org.jboss.hal.core.modelbrowser;

import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.JsHelper;
import org.jboss.hal.ballroom.table.Button.Scope;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.ballroom.table.OptionsBuilder;
import org.jboss.hal.ballroom.table.SelectorModifier;
import org.jboss.hal.ballroom.table.SelectorModifierBuilder;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.ballroom.table.SelectorModifier.Page.all;
import static org.jboss.hal.core.modelbrowser.ReadChildren.uniqueId;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;

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
                .column(resources.constants().view(), row -> modelBrowser.tree.api().openNode(parent.id,
                        () -> modelBrowser.select(uniqueId(parent, row), false)))
                .button(resources.constants().add(), (event, api) -> {
                    SelectorModifier selectorModifier = new SelectorModifierBuilder().page(all).build();
                    modelBrowser.add(parent, JsHelper.asList(api.rows(selectorModifier).data().toArray()));
                })

                .button(resources.constants().remove(), Scope.SELECTED,
                        (event, api) -> {
                            ResourceAddress fq = parent.data.getAddress()
                                    .getParent()
                                    .add(parent.text, api.selectedRow());
                            modelBrowser.remove(fq);
                        })
                .paging(false)
                .build();

        table = new DataTable<>(Ids.build(Ids.MODEL_BROWSER, "children", Ids.TABLE_SUFFIX), options);

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
            List<String> names = result.asList().stream().map(ModelNode::asString).collect(toList());
            table.update(names);
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
