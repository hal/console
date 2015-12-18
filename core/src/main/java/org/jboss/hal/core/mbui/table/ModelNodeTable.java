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

import com.google.common.collect.Lists;
import org.jboss.hal.ballroom.table.Column;
import org.jboss.hal.ballroom.table.GenericOptionsBuilder;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;

/**
 * @author Harald Pehl
 */
public class ModelNodeTable<T extends ModelNode> extends DataTable<T> {

    public static class Builder<T extends ModelNode> extends GenericOptionsBuilder<Builder<T>, T> {

        private final ResourceDescription resourceDescription;
        private final ColumnFactory columnFactory;

        public Builder(final ResourceDescription resourceDescription) {
            this.resourceDescription = resourceDescription;
            this.columnFactory = new ColumnFactory();
        }

        public Builder<T> columns(String first, String... rest) {
            List<String> columns = Lists.asList(first, rest);
            for (String column : columns) {
                column(column);
            }
            return that();
        }

        public Builder<T> column(String attribute) {
            Property attributeDescription = findDescription(resourceDescription.getAttributes(), attribute);
            if (attributeDescription != null) {
                Column<T> column = columnFactory.createColumn(attributeDescription);
                return column(column);
            } else {
                logger.error("No attribute description for column '{}' found in resource description\n{}", //NON-NLS
                        attribute, resourceDescription);
                return that();
            }
        }

        private Property findDescription(final List<Property> attributeDescriptions, final String column) {
            for (Property attributeDescription : attributeDescriptions) {
                if (attributeDescription.getName().equals(column)) {
                    return attributeDescription;
                }
            }
            return null;
        }

        @Override
        protected Builder<T> that() {
            return this;
        }

        @Override
        protected void validate() {
            super.validate();
            if (!resourceDescription.hasDefined(ATTRIBUTES)) {
                throw new IllegalStateException("No attributes found in resource description\n" + resourceDescription);
            }
        }
    }


    private static final Logger logger = LoggerFactory.getLogger(ModelNodeTable.class);

    public ModelNodeTable(final String id, final SecurityContext securityContext, final Options<T> options) {
        super(id, securityContext, options);
    }
}
