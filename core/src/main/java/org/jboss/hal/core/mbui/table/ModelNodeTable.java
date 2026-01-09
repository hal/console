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
package org.jboss.hal.core.mbui.table;

import java.util.List;
import java.util.function.Function;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.table.Column;
import org.jboss.hal.ballroom.table.ColumnBuilder;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.GenericOptionsBuilder;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.ballroom.table.RefreshMode;
import org.jboss.hal.core.Core;
import org.jboss.hal.core.NameI18n;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.security.AuthorisationDecision;
import org.jboss.hal.meta.security.ElementGuard;
import org.jboss.hal.resources.UIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import static org.jboss.hal.ballroom.table.RefreshMode.RESET;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INDEX;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.resources.UIConstants.HASH;
import static org.jboss.hal.resources.UIConstants.data;

public class ModelNodeTable<T extends ModelNode> extends DataTable<T> {

    private static final Logger logger = LoggerFactory.getLogger(ModelNodeTable.class);

    private final Metadata metadata;
    private final Options<T> options;
    private Function<T, String> identifier;
    private boolean identifierChecked;

    private ModelNodeTable(Builder<T> builder) {
        super(builder.id, builder.options());
        this.options = builder.options();
        this.metadata = builder.metadata;
        this.identifier = null;
        this.identifierChecked = false;

        for (Column<T> column : options.columns) {
            if (NameI18n.shouldBeLocalized(column)) {
                NameI18n.localize(column);
            }
        }
    }

    @Override
    public void attach() {
        super.attach();
        if (!options.buttonConstraints.isEmpty()) {
            options.buttonConstraints
                    .forEach((index, constraint) -> buttonElement(index).attr(data(UIConstants.CONSTRAINT), constraint));
        }
        applySecurity();
    }

    /** Shortcut for {@code super.select(data, NamedNode::getName)} */
    public void select(T data) {
        if (!identifierChecked) {
            checkIdentifier(data);
        }
        select(data, identifier);
    }

    @Override
    public void select(T data, Function<T, String> identifier) {
        super.select(data, identifier);
        applySecurity();
    }

    /** Shortcut for {@code super.update(data, NamedNode::getName)} */
    public void update(Iterable<T> data) {
        if (!identifierChecked) {
            checkIdentifier(Iterables.isEmpty(data) ? null : data.iterator().next());
        }
        update(data, RESET, identifier);
    }

    @Override
    public void update(Iterable<T> data, RefreshMode mode, Function<T, String> identifier) {
        super.update(data, mode, identifier);
        applySecurity();
    }

    private void checkIdentifier(T data) {
        if (data != null) {
            if (data.hasDefined(INDEX)) {
                identifier = model -> model.get(INDEX).asString();
            } else if (data.hasDefined(NAME)) {
                identifier = model -> model.get(NAME).asString();
            }
            identifierChecked = true;
        }
    }

    private void applySecurity() {
        AuthorisationDecision ad = AuthorisationDecision.from(Core.INSTANCE.environment(),
                metadata.getSecurityContext());
        if (element() == null) {
            ElementGuard.processElements(ad, HASH + id() + " [" + data(UIConstants.CONSTRAINT + "]"));

        } else {
            ElementGuard.processElements(ad, element());
        }
    }

    /** Builder to create tables based on resource metadata. By default the table has no columns and no actions. */
    @SuppressWarnings("DuplicateStringLiteralInspection")
    public static class Builder<T extends ModelNode> extends GenericOptionsBuilder<Builder<T>, T> {

        private final String id;
        private final Metadata metadata;
        private final ColumnFactory columnFactory;

        public Builder(String id, Metadata metadata) {
            this.id = id;
            this.metadata = metadata;
            this.columnFactory = new ColumnFactory();
        }

        public Builder<T> columns(String first, String... rest) {
            List<String> columns = Lists.asList(first, rest);
            for (String column : columns) {
                column(column);
            }
            return that();
        }

        public Builder<T> columns(Iterable<String> attributes) {
            if (attributes != null) {
                attributes.forEach(this::column);
            }
            return that();
        }

        /** Adds a column which maps to the specified attribute. */
        public Builder<T> column(String attribute) {
            Property attributeDescription = metadata.getDescription().attributes().property(attribute);
            if (attributeDescription.getValue().isDefined()) {
                Column<T> column = columnFactory.createColumn(attributeDescription);
                return column(column);
            } else {
                logger.error("No attribute description for column '{}' found in resource description\n{}",
                        attribute, metadata.getDescription());
                return that();
            }
        }

        public Builder<T> indexColumn() {
            column(new ColumnBuilder<T>(INDEX, new LabelBuilder().label(INDEX),
                    (c, t, r, meta) -> String.valueOf(meta.row))
                    .width("8rem")
                    .build());
            return that();
        }

        public Builder<T> nameColumn() {
            column(NAME, (c, t, row, m) -> row.get(NAME).asString());
            return that();
        }

        @Override
        protected Builder<T> that() {
            return this;
        }

        /** Creates and returns the table. */
        public ModelNodeTable<T> build() {
            return new ModelNodeTable<>(this);
        }
    }
}
