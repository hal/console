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

import com.google.common.collect.Ordering;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.operationParameter;
import static org.jboss.hal.resources.CSS.table;
import static org.jboss.hal.resources.CSS.tableBordered;
import static org.jboss.hal.resources.CSS.tableStriped;
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
                .th().textContent(Names.NAME).end()
                .th().textContent(resources.constants().input()).end()
                .th().textContent(resources.constants().output()).end()
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
        if (parameter.hasDefined(VALUE_TYPE)) {
            type += "<" + parameter.get(VALUE_TYPE).asString() + ">";
        }
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
