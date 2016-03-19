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

import com.google.common.collect.Ordering;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.HelpTextBuilder;
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
class AttributesTable implements IsElement {

    private final Element root;

    AttributesTable(final List<Property> attributes, final Resources resources) {

        HelpTextBuilder helpTextBuilder = new HelpTextBuilder();
        Elements.Builder builder = new Elements.Builder().table()
                .css(table, tableBordered, tableStriped, CSS.attributes)
                .thead()
                .tr()
                .th().textContent(resources.constants().attribute()).end()
                .th().textContent(resources.constants().type()).end()
                .th().textContent(resources.constants().storage()).end()
                .th().textContent(resources.constants().accessType()).end()
                .end()
                .end();

        builder.tbody();
        for (Property property : Ordering.natural().onResultOf(Property::getName).sortedCopy(attributes)) {
            ModelNode attribute = property.getValue();
            boolean required = attribute.hasDefined(NILLABLE) && !attribute.get(NILLABLE).asBoolean();
            SafeHtml description = helpTextBuilder.helpText(property);

            builder.tr();

            // attribute name & description
            SafeHtmlBuilder html = new SafeHtmlBuilder();
            html.appendHtmlConstant("<strong>") //NON-NLS
                    .appendEscaped(property.getName())
                    .appendHtmlConstant("</strong>"); //NON-NLS
            if (required) {
                html.appendHtmlConstant(NBSP).append(resources.messages().requiredMarker());
            }
            if (description != null) {
                html.appendHtmlConstant("<br/>").append(description); //NON-NLS
            }
            builder.td().innerHtml(html.toSafeHtml()).end();

            // type
            builder.td().textContent(attribute.get(TYPE).asString()).end();

            // storage
            builder.td();
            if (attribute.hasDefined(STORAGE)) {
                switch (attribute.get(STORAGE).asString()) {
                    case CONFIGURATION:
                        builder.start("i").css(fontAwesome("database")).title(CONFIGURATION).end();
                        break;
                    case RUNTIME:
                        builder.start("i").css(pfIcon("memory")).title(RUNTIME).end();
                        break;
                    default:
                        builder.innerHtml(SafeHtmlUtils.fromSafeConstant(NBSP));
                        break;
                }
            } else {
                builder.innerHtml(SafeHtmlUtils.fromSafeConstant(NBSP));
            }
            builder.end();

            // access type
            builder.td();
            if (attribute.hasDefined(ACCESS_TYPE)) {
                switch (attribute.get(ACCESS_TYPE).asString()) {
                    case READ_WRITE:
                        builder.start("i").css(pfIcon("edit")).title(READ_WRITE).end();
                        break;
                    case READ_ONLY:
                        builder.start("i").css(fontAwesome("lock")).title(READ_ONLY).end();
                        break;
                    case METRIC:
                        builder.start("i").css(pfIcon("trend-up")).title(METRIC).end();
                        break;
                    default:
                        builder.innerHtml(SafeHtmlUtils.fromSafeConstant(NBSP));
                        break;
                }
            } else {
                builder.innerHtml(SafeHtmlUtils.fromSafeConstant(NBSP));
            }
            builder.end(); // td
            builder.end(); // tr
        }

        builder.end(); // tbody
        builder.end(); // table
        this.root = builder.build();
    }

    @Override
    public Element asElement() {
        return root;
    }
}
