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
package org.jboss.hal.core.mbui;

import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.FormItemProvider;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;

/**
 * @author Harald Pehl
 */
public class DefaultFormItemProvider implements FormItemProvider {

    private final LabelBuilder labelBuilder;

    public DefaultFormItemProvider() {labelBuilder = new LabelBuilder();}

    @Override
    public FormItem<?> createFrom(final Property attributeDescription) {
        FormItem<?> formItem = null;

        String name = attributeDescription.getName();
        ModelNode modelNode = attributeDescription.getValue();
        ModelType type = modelNode.get(ModelDescriptionConstants.TYPE).asType();
        switch (type) {
            case BIG_DECIMAL:
                break;
            case BIG_INTEGER:
                break;
            case BOOLEAN:
                break;
            case BYTES:
                break;
            case DOUBLE:
                break;
            case EXPRESSION:
                break;
            case INT:
                break;
            case LIST:
                break;
            case LONG:
                break;
            case OBJECT:
                break;
            case PROPERTY:
                break;
            case STRING:
                formItem = new TextBoxItem(name, labelBuilder.label(attributeDescription));
                break;
            case TYPE:
                break;
            case UNDEFINED:
                break;
        }
        return formItem;
    }
}
