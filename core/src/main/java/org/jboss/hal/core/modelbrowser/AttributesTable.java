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
                .th().innerText(resources.constants().attribute()).end()
                .th().innerText(resources.constants().type()).end()
                .th().innerText(resources.constants().storage()).end()
                .th().innerText(resources.constants().accessType()).end()
                .end()
                .end();

        builder.tbody();
        for (Property property : Ordering.natural().onResultOf(Property::getName).sortedCopy(attributes)) {
            ModelNode attribute = property.getValue();
            boolean required = attribute.hasDefined(NILLABLE) && !attribute.get(NILLABLE).asBoolean();
            String description = helpTextBuilder.helpText(property);

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
                html.appendHtmlConstant("<br/>").appendEscaped(description); //NON-NLS
            }
            builder.td().innerHtml(html.toSafeHtml()).end();

            // type
            builder.td().innerText(attribute.get(TYPE).asString()).end();

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
