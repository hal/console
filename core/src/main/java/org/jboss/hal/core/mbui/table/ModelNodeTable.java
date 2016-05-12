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

import java.util.List;

import com.google.common.collect.Lists;
import org.jboss.hal.ballroom.table.Column;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.GenericOptionsBuilder;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;

/**
 * @author Harald Pehl
 */
public class ModelNodeTable<T extends ModelNode> extends DataTable<T> {

    public static class Builder<T extends ModelNode> extends GenericOptionsBuilder<Builder<T>, T> {

        private final Metadata metadata;
        private final ColumnFactory columnFactory;

        public Builder(final Metadata metadata) {
            this.metadata = metadata;
            this.columnFactory = new ColumnFactory();
        }

        public Builder<T> columns(@NonNls String first, @NonNls String... rest) {
            List<String> columns = Lists.asList(first, rest);
            for (String column : columns) {
                column(column);
            }
            return that();
        }

        public Builder<T> column(@NonNls String attribute) {
            return column(attribute, null);
        }

        public Builder<T> column(@NonNls String attribute, String title) {
            Property attributeDescription = metadata.getDescription().findAttribute(ATTRIBUTES, attribute);
            if (attributeDescription != null) {
                Column<T> column = columnFactory.createColumn(attributeDescription, title);
                return column(column);
            } else {
                logger.error("No attribute description for column '{}' found in resource description\n{}", //NON-NLS
                        attribute, metadata.getDescription());
                return that();
            }
        }

        @Override
        protected Builder<T> that() {
            return this;
        }

        @Override
        protected void validate() {
            super.validate();
            if (!metadata.getDescription().hasDefined(ATTRIBUTES)) {
                throw new IllegalStateException(
                        "No attributes found in resource description\n" + metadata.getDescription());
            }
        }
    }


    private static final Logger logger = LoggerFactory.getLogger(ModelNodeTable.class);

    public ModelNodeTable(@NonNls final String id, final Options<T> options) {
        super(id, options);
    }
}
