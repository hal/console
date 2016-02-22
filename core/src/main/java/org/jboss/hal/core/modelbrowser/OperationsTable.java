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

import com.google.common.collect.Ordering;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Resources;

import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.NBSP;

/**
 * @author Harald Pehl
 */
class OperationsTable implements IsElement {

    private final Element root;
    private final Resources resources;

    OperationsTable(final List<Property> operations, final Resources resources) {
        this.resources = resources;

        Elements.Builder builder = new Elements.Builder().table()
                .css(table, tableBordered, tableStriped, CSS.operations)
                .thead()
                .tr()
                .th().innerText(resources.constants().name()).end()
                .th().innerText(resources.constants().input()).end()
                .th().innerText(resources.constants().output()).end()
                .end()
                .end();

        builder.tbody();
        for (Property property : Ordering.natural().onResultOf(Property::getName).sortedCopy(operations)) {
            ModelNode operation = property.getValue();
            String description = operation.hasDefined(DESCRIPTION) ? operation.get(DESCRIPTION).asString() : null;

            builder.tr();

            // operation name & description
            SafeHtmlBuilder html = new SafeHtmlBuilder();
            html.appendHtmlConstant("<strong>") //NON-NLS
                    .appendEscaped(property.getName())
                    .appendHtmlConstant("</strong>"); //NON-NLS
            if (description != null) {
                html.appendHtmlConstant("<br/>").appendEscaped(description); //NON-NLS
            }
            builder.td().innerHtml(html.toSafeHtml()).end();

            // input
            builder.td();
            if (operation.hasDefined(REQUEST_PROPERTIES) && !operation.get(REQUEST_PROPERTIES).asPropertyList()
                    .isEmpty()) {
                List<Property> input = operation.get(REQUEST_PROPERTIES).asPropertyList();
                builder.ul().css(operationParameter);
                for (Property parameter : Ordering.natural().onResultOf(Property::getName).sortedCopy(input)) {
                    builder.li();
                    buildParameter(builder, parameter.getName(), parameter.getValue());
                    builder.end();
                }
                builder.end();
            } else {
                builder.innerHtml(SafeHtmlUtils.fromSafeConstant(NBSP));
            }
            builder.end();

            // output
            builder.td();
            if (operation.hasDefined(REPLY_PROPERTIES) && !operation.get(REPLY_PROPERTIES).asList().isEmpty()) {
                buildParameter(builder, null, operation.get(REPLY_PROPERTIES));
            } else {
                builder.innerHtml(SafeHtmlUtils.fromSafeConstant(NBSP));
            }
            builder.end();

            builder.end(); // tr
        }

        builder.end(); // tbody
        builder.end(); // table
        this.root = builder.build();
    }

    private void buildParameter(Elements.Builder builder, String name, ModelNode parameter) {
        boolean required = parameter.hasDefined(REQUIRED) && parameter.get(REQUIRED).asBoolean();
        String type = parameter.get(TYPE).asString();
        String description = parameter.hasDefined(DESCRIPTION) ? parameter.get(DESCRIPTION).asString() : null;

        SafeHtmlBuilder html = new SafeHtmlBuilder();
        if (name != null) {
            //noinspection HardCodedStringLiteral
            html.appendHtmlConstant("<code>").appendEscaped(name).appendHtmlConstant("</code>").appendEscaped(": ");
        }
        html.appendEscaped(type);
        if (required) {
            html.appendHtmlConstant(NBSP).append(resources.messages().requiredMarker());
        }
        if (description != null) {
            html.appendHtmlConstant("<br/>").appendEscaped(description); //NON-NLS
        }
        builder.innerHtml(html.toSafeHtml());
    }

    @Override
    public Element asElement() {
        return root;
    }
}
