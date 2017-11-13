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
package org.jboss.hal.core.mbui.table;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.table.Column;
import org.jboss.hal.ballroom.table.ColumnBuilder;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;

class ColumnFactory {

    private final LabelBuilder labelBuilder;

    ColumnFactory() {
        labelBuilder = new LabelBuilder();
    }

    <T extends ModelNode> Column<T> createColumn(final Property attributeDescription) {
        String name = attributeDescription.getName();
        String title = labelBuilder.label(attributeDescription);

        // TODO Think about other column types depending on ModelType
        Column.RenderCallback<T, Object> render = (cell, type, row, meta) -> {
            if (row.hasDefined(name)) {
                return row.get(name).asString();
            }
            return null;
        };
        return new ColumnBuilder<>(name, title, render).build();
    }
}
