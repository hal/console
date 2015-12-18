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

import org.jboss.hal.ballroom.table.Column;
import org.jboss.hal.ballroom.table.ColumnBuilder;
import org.jboss.hal.core.mbui.LabelBuilder;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;

/**
 * @author Harald Pehl
 */
class ColumnFactory {

    private final LabelBuilder labelBuilder;

    ColumnFactory() {
        labelBuilder = new LabelBuilder();
    }

    <T extends ModelNode> Column<T> createColumn(final Property attributeDescription) {
        String name = attributeDescription.getName();
        String title = labelBuilder.label(attributeDescription);

        // TODO Think about other columns type depending on ModelType
        Column.RenderCallback<T, Object> render = (cell, type, row, meta) -> {
            if (row.hasDefined(name)) {
                return row.get(name).asString();
            }
            return null;
        };
        return new ColumnBuilder<>(name, title, render).build();
    }
}
