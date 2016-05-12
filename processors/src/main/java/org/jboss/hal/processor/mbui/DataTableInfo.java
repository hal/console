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
package org.jboss.hal.processor.mbui;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Harald Pehl
 */
public class DataTableInfo extends MbuiElementInfo {

    public static class Column {

        private final String name;
        private final String title;
        private final String value;

        Column(final String name, final String title, final String value) {
            this.name = name;
            this.title = Handlebars.templateSafeValue(title); // title can be a simple value or an expression
            this.value = Handlebars.stripHandlebar(value); // value has to be an expression
        }

        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }

        public String getValue() {
            return value;
        }

        public boolean isSimple() {
            return title == null && value == null;
        }

        public boolean isSimpleWithTitle() {
            return title != null && value == null;
        }

        public boolean isHasValue() {
            return value != null;
        }
    }


    private final String typeParameter;
    private final MetadataInfo metadata;
    private FormInfo formRef;
    private final List<Column> columns;

    DataTableInfo(final String name, final String selector, String typeParameter, MetadataInfo metadata) {
        super(name, selector);
        this.typeParameter = typeParameter;
        this.metadata = metadata;
        this.columns = new ArrayList<>();
    }

    public String getTypeParameter() {
        return typeParameter;
    }

    public MetadataInfo getMetadata() {
        return metadata;
    }

    public FormInfo getFormRef() {
        return formRef;
    }

    void setFormRef(final FormInfo formRef) {
        this.formRef = formRef;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public boolean isOnlySimpleColumns() {
        for (Column column : columns) {
            if (!column.isSimple()) {
                return false;
            }
        }
        return true;
    }

    void addColumn(Column column) {
        columns.add(column);
    }
}
