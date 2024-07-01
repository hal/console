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
package org.jboss.hal.core.modelbrowser;

import java.util.List;

import org.jboss.elemento.HtmlContentBuilder;
import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.StabilityLabel;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.StabilityLevel;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Resources;

import com.google.common.collect.Ordering;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableRowElement;

import static org.jboss.elemento.Elements.li;
import static org.jboss.elemento.Elements.table;
import static org.jboss.elemento.Elements.tbody;
import static org.jboss.elemento.Elements.td;
import static org.jboss.elemento.Elements.th;
import static org.jboss.elemento.Elements.thead;
import static org.jboss.elemento.Elements.tr;
import static org.jboss.elemento.Elements.ul;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPRECATED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REPLY_PROPERTIES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUIRED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STABILITY;
import static org.jboss.hal.resources.CSS.operationParameter;
import static org.jboss.hal.resources.CSS.table;
import static org.jboss.hal.resources.CSS.tableBordered;
import static org.jboss.hal.resources.CSS.tableStriped;
import static org.jboss.hal.resources.UIConstants.NBSP;

class OperationsTable implements IsElement {

    private final HTMLElement root;
    private final Resources resources;

    OperationsTable(List<Property> operations, Environment environment, Resources resources) {
        HTMLElement tbody;

        this.resources = resources;
        this.root = table()
                .css(table, tableBordered, tableStriped, CSS.operations)
                .add(thead()
                        .add(tr()
                                .add(th().textContent(resources.constants().name()))
                                .add(th().textContent(resources.constants().input()))
                                .add(th().textContent(resources.constants().output()))))
                .add(tbody = tbody().element()).element();

        for (Property property : Ordering.natural().onResultOf(Property::getName).sortedCopy(operations)) {
            ModelNode operation = property.getValue();
            String description = operation.hasDefined(DESCRIPTION) ? operation.get(DESCRIPTION).asString() : null;
            boolean deprecated = operation.hasDefined(DEPRECATED) && operation.get(DEPRECATED).asBoolean();
            StabilityLevel stabilityLevel = ModelNodeHelper.asEnumValue(operation, STABILITY, StabilityLevel::valueOf,
                    environment.getHalBuild().defaultStability);

            // start a new table row
            HtmlContentBuilder<HTMLTableRowElement> builder = tr();

            // operation name & description
            SafeHtmlBuilder html = new SafeHtmlBuilder();
            if (environment.highlightStabilityLevel(stabilityLevel)) {
                html.append(StabilityLabel.stabilityLevelHtml(stabilityLevel, false));
            }
            html.appendHtmlConstant(
                    "<strong" + (deprecated ? " class=\"" + CSS.deprecated + "\" title=\"deprecated\"" : "") + ">")
                    .appendEscaped(property.getName())
                    .appendHtmlConstant("</strong>");
            if (description != null) {
                html.appendHtmlConstant("<br/>").appendEscaped(description); // NON-NLS
            }
            builder.add(td().innerHtml(html.toSafeHtml()));

            // input
            HTMLElement inputTd;
            builder.add(inputTd = td().element());
            if (operation.hasDefined(REQUEST_PROPERTIES) && !operation.get(REQUEST_PROPERTIES).asPropertyList()
                    .isEmpty()) {
                List<Property> input = operation.get(REQUEST_PROPERTIES).asPropertyList();
                HTMLElement ul;
                inputTd.appendChild(ul = ul().css(operationParameter).element());
                for (Property parameter : Ordering.natural().onResultOf(Property::getName).sortedCopy(input)) {
                    HTMLElement li;
                    ul.appendChild(li = li().element());
                    buildParameter(li, parameter.getName(), parameter.getValue());
                }
            } else {
                inputTd.innerHTML = SafeHtmlUtils.fromSafeConstant(NBSP).asString();
            }

            // output
            HTMLElement outputTd;
            builder.add(outputTd = td().element());
            if (operation.hasDefined(REPLY_PROPERTIES) && !operation.get(REPLY_PROPERTIES).asList().isEmpty()) {
                buildParameter(outputTd, null, operation.get(REPLY_PROPERTIES));
            } else {
                outputTd.innerHTML = SafeHtmlUtils.fromSafeConstant(NBSP).asString();
            }

            tbody.appendChild(builder.element());
        }
    }

    private void buildParameter(HTMLElement element, String name, ModelNode parameter) {
        boolean required = parameter.hasDefined(REQUIRED) && parameter.get(REQUIRED).asBoolean();
        String description = parameter.hasDefined(DESCRIPTION) ? parameter.get(DESCRIPTION).asString() : null;

        SafeHtmlBuilder html = new SafeHtmlBuilder();
        if (name != null) {
            // noinspection HardCodedStringLiteral
            html.appendHtmlConstant("<code>").appendEscaped(name).appendHtmlConstant("</code>").appendEscaped(": ");
        }
        html.appendEscaped(Types.formatType(parameter));
        if (required) {
            html.appendHtmlConstant(NBSP).append(resources.messages().requiredMarker());
        }
        if (description != null) {
            html.appendHtmlConstant("<br/>").appendEscaped(description); // NON-NLS
        }
        element.innerHTML = html.toSafeHtml().asString();
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}
