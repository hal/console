/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableRowElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.core.builder.HtmlContentBuilder;
import org.jboss.hal.ballroom.HelpTextBuilder;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Resources;
import org.jetbrains.annotations.NonNls;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.NBSP;

class AttributesTable implements IsElement {

    private final HTMLElement root;

    AttributesTable(List<Property> attributes, Resources resources) {

        HTMLElement tbody;
        this.root = table()
                .css(table, tableBordered, tableStriped, CSS.attributes)
                .add(thead()
                        .add(tr()
                                .add(th().textContent(resources.constants().attribute()))
                                .add(th().textContent(resources.constants().type()))
                                .add(th().textContent(resources.constants().storage()))
                                .add(th().textContent(resources.constants().accessType()))))
                .add(tbody = tbody().get())
                .get();

        HelpTextBuilder helpTextBuilder = new HelpTextBuilder();
        for (Property property : Ordering.natural().onResultOf(Property::getName).sortedCopy(attributes)) {
            ModelNode attribute = property.getValue();
            boolean required = attribute.hasDefined(NILLABLE) && !attribute.get(NILLABLE).asBoolean();
            boolean deprecated = attribute.hasDefined(DEPRECATED) && attribute.get(DEPRECATED).asBoolean();
            SafeHtml description = helpTextBuilder.helpText(property);

            // start a new table row
            HtmlContentBuilder<HTMLTableRowElement> builder = tr();

            // attribute name & description
            @NonNls SafeHtmlBuilder html = new SafeHtmlBuilder();
            html.appendHtmlConstant(
                    "<strong" + (deprecated ? " class=\"" + CSS.deprecated + "\" title=\"deprecated\"" : "") + ">")
                    .appendEscaped(property.getName())
                    .appendHtmlConstant("</strong>");
            if (required) {
                html.appendHtmlConstant(NBSP).append(resources.messages().requiredMarker());
            }
            if (description != null) {
                html.appendHtmlConstant("<br/>").append(description);
            }
            builder.add(td().innerHtml(html.toSafeHtml()));

            // type
            builder.add(td().textContent(Types.formatType(attribute)));

            // storage
            HTMLElement storageTd;
            builder.add(storageTd = td().get());
            if (attribute.hasDefined(STORAGE)) {
                switch (attribute.get(STORAGE).asString()) {
                    case CONFIGURATION:
                        storageTd.appendChild(i().css(fontAwesome("database")).title(CONFIGURATION).get());
                        break;
                    case RUNTIME:
                        storageTd.appendChild(i().css(pfIcon("memory")).title(RUNTIME).get());
                        break;
                    default:
                        storageTd.innerHTML = SafeHtmlUtils.fromSafeConstant(NBSP).asString();
                        break;
                }
            } else {
                builder.innerHtml(SafeHtmlUtils.fromSafeConstant(NBSP));
            }

            // access type
            HTMLElement accessTypeTd;
            builder.add(accessTypeTd = td().get());
            if (attribute.hasDefined(ACCESS_TYPE)) {
                switch (attribute.get(ACCESS_TYPE).asString()) {
                    case READ_WRITE:
                        accessTypeTd.appendChild(i().css(pfIcon("edit")).title(READ_WRITE).get());
                        break;
                    case READ_ONLY:
                        accessTypeTd.appendChild(i().css(fontAwesome("lock")).title(READ_ONLY).get());
                        break;
                    case METRIC:
                        accessTypeTd.appendChild(i().css(pfIcon("trend-up")).title(METRIC).get());
                        break;
                    default:
                        accessTypeTd.innerHTML = SafeHtmlUtils.fromSafeConstant(NBSP).asString();
                        break;
                }
            } else {
                builder.innerHtml(SafeHtmlUtils.fromSafeConstant(NBSP));
            }

            tbody.appendChild(builder.get());
        }
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}
