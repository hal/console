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
package org.jboss.hal.core.mbui.table;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import org.jboss.hal.core.mbui.LabelBuilder;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;

/**
 * @author Harald Pehl
 */
class ColumnFactory {

    // I want to have tuples!
    static class HeaderColumn {

        final SafeHtml header;
        final Column<ModelNode, ?> column;

        HeaderColumn(final SafeHtml header, final Column<ModelNode, ?> column) {
            this.header = header;
            this.column = column;
        }
    }

    interface Templates extends SafeHtmlTemplates {

        @Template("<span>{0}</span>")
        SafeHtml header(String label);

        @Template("<span data-toggle=\"tooltip\" data-placement=\"top\" title=\"{1}\">{0}</span>")
        SafeHtml headerWithDescription(String label, String description);
    }

    private static final Templates templates = GWT.create(Templates.class);

    private final LabelBuilder labelBuilder;

    ColumnFactory() {
        labelBuilder = new LabelBuilder();
    }

    HeaderColumn createHeaderColumn(final Property attributeDescription) {
        SafeHtml header;
        String label = labelBuilder.label(attributeDescription);
        if (attributeDescription.getValue().hasDefined(DESCRIPTION)) {
            String description = attributeDescription.getValue().get(DESCRIPTION).asString();
            header = templates.headerWithDescription(label, description);
        } else {
            header = templates.header(label);
        }

        String name = attributeDescription.getName();
        // TODO Think about other columns type depending on ModelType
        TextColumn<ModelNode> column = new TextColumn<ModelNode>() {
            @Override
            public String getValue(final ModelNode value) {
                if (value.hasDefined(name)) {
                    return value.get(name).asString();
                }
                return null;
            }
        };

        return new HeaderColumn(header, column);
    }
}
