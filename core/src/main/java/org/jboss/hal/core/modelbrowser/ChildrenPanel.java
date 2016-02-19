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
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.ballroom.table.Api.RefreshMode;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.ballroom.table.OptionsBuilder;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;

/**
 * Panel which holds the children of the selected resource.
 *
 * @author Harald Pehl
 */
class ChildrenPanel implements HasElements, Attachable {

    private static final String HEADER_ELEMENT = "headerElement";

    private final Dispatcher dispatcher;
    private final Elements.Builder builder;
    private final Element header;
    private final DataTable<String> table;

    ChildrenPanel(final Dispatcher dispatcher, final Resources resources) {
        this.dispatcher = dispatcher;

        Options<String> options = new OptionsBuilder<String>()
                .column("resource", "Resource", (cell, type, row, meta) -> row) //NON-NLS
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
    }

    @SuppressWarnings("HardCodedStringLiteral")
    void update(final Node<Context> node, final ResourceAddress address) {
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
