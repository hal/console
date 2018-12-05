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
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableRowElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.core.builder.HtmlContentBuilder;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.li;
import static org.jboss.gwt.elemento.core.Elements.table;
import static org.jboss.gwt.elemento.core.Elements.tbody;
import static org.jboss.gwt.elemento.core.Elements.td;
import static org.jboss.gwt.elemento.core.Elements.th;
import static org.jboss.gwt.elemento.core.Elements.thead;
import static org.jboss.gwt.elemento.core.Elements.tr;
import static org.jboss.gwt.elemento.core.Elements.ul;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REPLY_PROPERTIES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUIRED;
import static org.jboss.hal.resources.CSS.operationParameter;
import static org.jboss.hal.resources.CSS.table;
import static org.jboss.hal.resources.CSS.tableBordered;
import static org.jboss.hal.resources.CSS.tableStriped;
import static org.jboss.hal.resources.UIConstants.NBSP;

class OperationsTable implements IsElement {

    private final HTMLElement root;
    private final Resources resources;

    OperationsTable(List<Property> operations, Resources resources) {
        HTMLElement tbody;

        this.resources = resources;
        this.root = table()
                .css(table, tableBordered, tableStriped, CSS.operations)
                .add(thead()
                        .add(tr()
                                .add(th().textContent(Names.NAME))
                                .add(th().textContent(resources.constants().input()))
                                .add(th().textContent(resources.constants().output()))))
                .add(tbody = tbody().get())
                .get();

        for (Property property : Ordering.natural().onResultOf(Property::getName).sortedCopy(operations)) {
            ModelNode operation = property.getValue();
            String description = operation.hasDefined(DESCRIPTION) ? operation.get(DESCRIPTION).asString() : null;

            // start a new table row
            HtmlContentBuilder<HTMLTableRowElement> builder = tr();

            // operation name & description
            SafeHtmlBuilder html = new SafeHtmlBuilder();
            html.appendHtmlConstant("<strong>") //NON-NLS
                    .appendEscaped(property.getName())
                    .appendHtmlConstant("</strong>"); //NON-NLS
            if (description != null) {
                html.appendHtmlConstant("<br/>").appendEscaped(description); //NON-NLS
            }
            builder.add(td().innerHtml(html.toSafeHtml()));

            // input
            HTMLElement inputTd;
            builder.add(inputTd = td().get());
            if (operation.hasDefined(REQUEST_PROPERTIES) && !operation.get(REQUEST_PROPERTIES).asPropertyList()
                    .isEmpty()) {
                List<Property> input = operation.get(REQUEST_PROPERTIES).asPropertyList();
                HTMLElement ul;
                inputTd.appendChild(ul = ul().css(operationParameter).get());
                for (Property parameter : Ordering.natural().onResultOf(Property::getName).sortedCopy(input)) {
                    HTMLElement li;
                    ul.appendChild(li = li().get());
                    buildParameter(li, parameter.getName(), parameter.getValue());
                }
            } else {
                inputTd.innerHTML = SafeHtmlUtils.fromSafeConstant(NBSP).asString();
            }

            // output
            HTMLElement outputTd;
            builder.add(outputTd = td().get());
            if (operation.hasDefined(REPLY_PROPERTIES) && !operation.get(REPLY_PROPERTIES).asList().isEmpty()) {
                buildParameter(outputTd, null, operation.get(REPLY_PROPERTIES));
            } else {
                outputTd.innerHTML = SafeHtmlUtils.fromSafeConstant(NBSP).asString();
            }

            tbody.appendChild(builder.get());
        }
    }

    private void buildParameter(HTMLElement element, String name, ModelNode parameter) {
        boolean required = parameter.hasDefined(REQUIRED) && parameter.get(REQUIRED).asBoolean();
        String description = parameter.hasDefined(DESCRIPTION) ? parameter.get(DESCRIPTION).asString() : null;

        SafeHtmlBuilder html = new SafeHtmlBuilder();
        if (name != null) {
            //noinspection HardCodedStringLiteral
            html.appendHtmlConstant("<code>").appendEscaped(name).appendHtmlConstant("</code>").appendEscaped(": ");
        }
        html.appendEscaped(Types.formatType(parameter));
        if (required) {
            html.appendHtmlConstant(NBSP).append(resources.messages().requiredMarker());
        }
        if (description != null) {
            html.appendHtmlConstant("<br/>").appendEscaped(description); //NON-NLS
        }
        element.innerHTML = html.toSafeHtml().asString();
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}
