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
import java.util.function.Function;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.jboss.hal.ballroom.JsCallback;
import org.jboss.hal.ballroom.table.ButtonHandler;
import org.jboss.hal.ballroom.table.Column;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.GenericOptionsBuilder;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.ballroom.table.RefreshMode;
import org.jboss.hal.ballroom.table.Scope;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.Core;
import org.jboss.hal.core.CrudOperations.AddCallback;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.security.AuthorisationDecision;
import org.jboss.hal.meta.security.ElementGuard;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.EsParam;
import org.jboss.hal.spi.EsReturn;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.asList;
import static org.jboss.hal.ballroom.table.RefreshMode.RESET;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.resources.UIConstants.data;

public class ModelNodeTable<T extends ModelNode> extends DataTable<T> {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(ModelNodeTable.class);

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
    }

    @Override
    @JsMethod
    public void attach() {
        super.attach();
        if (!options.buttonConstraints.isEmpty()) {
            options.buttonConstraints.forEach((index, constraint) ->
                    buttonElement(index).attr(data(UIConstants.CONSTRAINT), constraint));
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
            if (data.hasDefined(NAME)) {
                identifier = model -> model.get(NAME).asString();
            }
            identifierChecked = true;
        }
    }

    private void applySecurity() {
        AuthorisationDecision ad = AuthorisationDecision.from(Core.INSTANCE.environment(),
                metadata.getSecurityContext());
        ElementGuard.processElements(ad, asElement());
    }


    // ------------------------------------------------------ JS methods

    @JsProperty(name = "element")
    public HTMLElement jsElement() {
        return asElement();
    }

    @SuppressWarnings("unchecked")
    @JsProperty(name = "rows")
    public T[] jsRows() {
        List<T> rows = getRows();
        return rows.toArray((T[]) new Object[rows.size()]);
    }

    @SuppressWarnings("unchecked")
    @JsProperty(name = "selectedRows")
    public T[] jsSelectedRows() {
        List<T> rows = selectedRows();
        return rows.toArray((T[]) new Object[rows.size()]);
    }

    @JsMethod(name = "update")
    public void jsUpdate(T[] rows) {
        update(asList(rows));
    }


    /** Builder to create tables based on resource metadata. By default the table has no columns and no actions. */
    @SuppressWarnings("DuplicateStringLiteralInspection")
    @JsType(namespace = "hal.ui", name = "TableBuilder")
    public static class Builder<T extends ModelNode> extends GenericOptionsBuilder<Builder<T>, T> {

        private final String id;
        private final Metadata metadata;
        private final ColumnFactory columnFactory;

        @JsIgnore
        public Builder(@NonNls String id, Metadata metadata) {
            this.id = id;
            this.metadata = metadata;
            this.columnFactory = new ColumnFactory();
        }

        @JsIgnore
        public Builder<T> columns(@NonNls String first, @NonNls String... rest) {
            List<String> columns = Lists.asList(first, rest);
            for (String column : columns) {
                column(column);
            }
            return that();
        }

        @JsIgnore
        public Builder<T> columns(Iterable<String> attributes) {
            if (attributes != null) {
                attributes.forEach(this::column);
            }
            return that();
        }

        /** Adds a column which maps to the specified attribute. */
        @EsReturn("TableBuilder")
        public Builder<T> column(@NonNls String attribute) {
            Property attributeDescription = metadata.getDescription().findAttribute(ATTRIBUTES, attribute);
            if (attributeDescription != null) {
                Column<T> column = columnFactory.createColumn(attributeDescription);
                return column(column);
            } else {
                logger.error("No attribute description for column '{}' found in resource description\n{}",
                        attribute, metadata.getDescription());
                return that();
            }
        }

        @Override
        protected Builder<T> that() {
            return this;
        }

        /** Creates and returns the table. */
        @EsReturn("Table")
        public ModelNodeTable<T> build() {
            return new ModelNodeTable<>(this);
        }


        // ------------------------------------------------------ JS methods


        /**
         * Adds a button to add a new resource.
         *
         * @param type       The human readable resource type used in the dialog header and success message.
         * @param template   The address template for the add operation. Must end in <code>&lt;resource
         *                   type&gt;=&lt;resource
         *                   name&gt;</code>.
         * @param attributes attributes which should be part of the add resource dialog
         * @param callback   the callback executed after the resource has been added
         */
        @JsMethod(name = "add")
        @EsReturn("TableBuilder")
        public Builder<T> jsAdd(String type,
                @EsParam("AddressTemplate|string") Object template,
                @EsParam("string[]") String[] attributes,
                @EsParam("function(name: string, address: ResourceAddress)") AddCallback callback) {
            TableButtonFactory buttonFactory = Core.INSTANCE.tableButtonFactory();
            String id = Ids.build(Ids.uniqueId(), Ids.ADD);
            return button(buttonFactory.add(id, type, jsTemplate("add", template), asList(attributes),
                    callback));
        }

        /**
         * Adds a button to remove the selected resource.
         *
         * @param type     The human readable resource type used in the success message.
         * @param template The address template for the add operation. Must end in <code>&lt;resource
         *                 type&gt;=&lt;resource
         * @param name     A function to get the name of the selected resource.
         * @param callback The callback executed after the resource has been removed.
         */
        @JsMethod(name = "remove")
        @EsReturn("TableBuilder")
        public Builder<T> jsRemove(String type,
                @EsParam("AddressTemplate|string") Object template,
                @EsParam("function(table: Table): string") JsNameFunction<T> name,
                @EsParam("function()") JsCallback callback) {
            TableButtonFactory buttonFactory = Core.INSTANCE.tableButtonFactory();
            return button(buttonFactory.remove(type, jsTemplate("remove", template), name::getName, callback::execute));
        }

        /**
         * Add a button which executes the specified callback.
         *
         * @param text    The text on the button
         * @param scope   The scope: "selected" or "selectedSingle"
         * @param handler The callback to execute when the button is clicked
         */
        @JsMethod(name = "button")
        @EsReturn("TableBuilder")
        public Builder<T> jsButton(String text, String scope,
                @EsParam("function(table: Table)") ButtonHandler<T> handler) {
            return button(text, handler, Scope.fromSelector(scope));
        }

        /**
         * Adds columns for the specified attributes.
         *
         * @param columns The attributes
         */
        @JsMethod(name = "columns")
        @EsReturn("TableBuilder")
        public Builder<T> jsColumns(@EsParam("string[]") String[] columns) {
            return columns(asList(columns));
        }

        private AddressTemplate jsTemplate(String method, Object template) {
            AddressTemplate t;
            if (template instanceof String) {
                t = AddressTemplate.of(((String) template));
            } else if (template instanceof AddressTemplate) {
                t = (AddressTemplate) template;
            } else {
                throw new IllegalArgumentException(
                        "Invalid 2nd argument: Use TableBuilder." + method + "(string, (string|AddressTemplate), string[], function(string, ResourceAddress))");
            }
            return t;
        }

        @JsFunction
        public interface JsNameFunction<T> {

            String getName(Table<T> table);
        }
    }
}
