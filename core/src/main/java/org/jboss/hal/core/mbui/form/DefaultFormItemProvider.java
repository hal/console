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
package org.jboss.hal.core.mbui.form;

import com.google.common.collect.Lists;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.FormItemProvider;
import org.jboss.hal.ballroom.form.NumberItem;
import org.jboss.hal.ballroom.form.SingleSelectBoxItem;
import org.jboss.hal.ballroom.form.SwitchItem;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.core.mbui.LabelBuilder;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.jboss.hal.ballroom.form.NumberItem.MAX_SAFE_LONG;
import static org.jboss.hal.ballroom.form.NumberItem.MIN_SAFE_LONG;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

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
        String label = labelBuilder.label(attributeDescription);
        ModelNode modelNode = attributeDescription.getValue();

        if (modelNode.hasDefined(TYPE)) {
            ModelType type = modelNode.get(TYPE).asType();
            switch (type) {
                case DOUBLE:
                case BIG_DECIMAL:
                    break;

                case INT:
                    long iMin = modelNode.get(MIN).asLong(Integer.MIN_VALUE);
                    long iMax = modelNode.get(MAX).asLong(Integer.MAX_VALUE);
                    formItem = new NumberItem(name, label, iMin, iMax);
                    break;

                case LONG:
                case BIG_INTEGER:
                    long lMin = modelNode.get(MIN).asLong(MIN_SAFE_LONG);
                    long lMax = modelNode.get(MAX).asLong(MAX_SAFE_LONG);
                    formItem = new NumberItem(name, label, lMin, lMax);
                    break;

                case BOOLEAN:
                    formItem = new SwitchItem(name, label);
                    break;
                case BYTES:
                    break;
                case EXPRESSION:
                    break;
                case LIST:
                    break;
                case OBJECT:
                    break;
                case PROPERTY:
                    break;
                case STRING:
                    List<ModelNode> allowedNodes = ModelNodeHelper
                            .getOrDefault(modelNode, () -> modelNode.get(ALLOWED).asList(), emptyList());
                    List<String> allowedValues = Lists.transform(allowedNodes, ModelNode::asString);
                    if (allowedValues.isEmpty()) {
                        formItem = new TextBoxItem(name, label);
                    } else {
                        formItem = new SingleSelectBoxItem(name, label, allowedValues);
                    }
                    break;
                case TYPE:
                    break;
                case UNDEFINED:
                    break;
            }

            if (formItem != null) {
                formItem.setRequired(!modelNode.get(NILLABLE).asBoolean(true));
                if (formItem.supportsExpressions()) {
                    formItem.setExpressionAllowed(modelNode.get(EXPRESSION_ALLOWED).asBoolean(false));
                }
                if (modelNode.hasDefined(DEFAULT)) {
                    String defaultValue = modelNode.get(DEFAULT).asString();
                    formItem.setPlaceholder(defaultValue);
                }
            }
        }

        return formItem;
    }
}
