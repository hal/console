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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.jboss.hal.ballroom.form.DefaultMapping;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.description.ResourceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelType.BIG_INTEGER;
import static org.jboss.hal.dmr.ModelType.INT;

/**
 * @author Harald Pehl
 */
public class ModelNodeMapping<T extends ModelNode> extends DefaultMapping<T> {

    private static final Logger logger = LoggerFactory.getLogger(ModelNodeMapping.class);

    private final ResourceDescription resourceDescription;

    public ModelNodeMapping(final ResourceDescription resourceDescription) {
        this.resourceDescription = resourceDescription;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void populateFormItems(final T model, final Form<T> form) {
        for (FormItem formItem : form.getBoundFormItems()) {
            formItem.clearError();

            String name = formItem.getName();
            if (model.hasDefined(name)) {
                ModelNode attributeDescription = resourceDescription.find(name);
                if (attributeDescription == null) {
                    //noinspection HardCodedStringLiteral
                    logger.error("{}: Unable to populate form item '{}': No attribute description found in\n{}",
                            id(form), name, resourceDescription);
                    continue;
                }

                ModelNode value = model.get(formItem.getName());
                ModelType type = attributeDescription.get(TYPE).asType();
                switch (type) {
                    case BOOLEAN:
                        formItem.setValue(value.asBoolean());
                        break;

                    case INT:
                        // NumberFormItem uses *always* long
                        formItem.setValue((long) value.asInt());
                        break;
                    case BIG_INTEGER:
                    case LONG:
                        formItem.setValue(value.asLong());
                        break;

                    case LIST:
                        List<String> list = Lists.transform(value.asList(), ModelNode::asString);
                        formItem.setValue(list);
                        break;

                    case OBJECT:
                        List<Property> properties = value.asPropertyList();
                        Map<String, String> map = new HashMap<>();
                        for (Property property : properties) {
                            map.put(property.getName(), property.getValue().asString());
                        }
                        formItem.setValue(map);
                        break;

                    case STRING:
                        formItem.setValue(value.asString());
                        break;

                    // unsupported types
                    case BIG_DECIMAL:
                    case DOUBLE:
                    case BYTES:
                    case EXPRESSION:
                    case PROPERTY:
                    case TYPE:
                    case UNDEFINED:
                        //noinspection HardCodedStringLiteral
                        logger.warn("{}: populating form field '{}' of type '{}' not implemented", id(form), name,
                                type);
                        break;
                }
                formItem.setUndefined(false);

            } else {
                formItem.clearValue();
                formItem.setUndefined(true);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void persistModel(final T model, final Form<T> form) {
        for (FormItem formItem : form.getBoundFormItems()) {
            String name = formItem.getName();

            if (model.hasDefined(name) && formItem.isUndefined()) {
                // TODO Check default value
                model.remove(name);

            } else if (formItem.isModified()) {
                ModelNode attributeDescription = resourceDescription.find(name);
                if (attributeDescription == null) {
                    //noinspection HardCodedStringLiteral
                    logger.error("{}: Unable to persist attribute '{}': No attribute description found in\n{}",
                            id(form), name, resourceDescription);
                    continue;
                }
                ModelType type = attributeDescription.get(TYPE).asType();
                Object value = formItem.getValue();
                switch (type) {
                    case BOOLEAN:
                        model.get(name).set((Boolean) value);
                        break;

                    case BIG_INTEGER:
                    case INT:
                    case LONG:
                        Long longValue = (Long) value;
                        if (type == BIG_INTEGER) {
                            model.get(name).set(BigInteger.valueOf(longValue));
                        } else if (type == INT) {
                            model.get(name).set(longValue.intValue());
                        } else {
                            model.get(name).set(longValue);
                        }
                        break;

                    case LIST:
                        List<String> list = (List<String>) value;
                        if (list.isEmpty()) {
                            model.remove(name);
                        } else {
                            ModelNode listNode = new ModelNode();
                            for (String s : list) {
                                listNode.add(s);
                            }
                            model.get(name).set(listNode);
                        }
                        break;

                    case OBJECT:
                        Map<String, String> map = (Map<String, String>) value;
                        if (map.isEmpty()) {
                            model.remove(name);
                        } else {
                            ModelNode mapNode = new ModelNode();
                            for (Map.Entry<String, String> entry : map.entrySet()) {
                                mapNode.get(entry.getKey()).set(entry.getValue());
                            }
                            model.get(name).set(mapNode);
                        }
                        break;

                    case STRING:
                        String stringValue = String.valueOf(value);
                        if (Strings.isNullOrEmpty(stringValue)) {
                            model.remove(name);
                        } else {
                            model.get(name).set(stringValue);
                        }
                        break;

                    // unsupported types
                    case BIG_DECIMAL:
                    case BYTES:
                    case DOUBLE:
                    case EXPRESSION:
                    case PROPERTY:
                    case TYPE:
                    case UNDEFINED:
                        //noinspection HardCodedStringLiteral
                        logger.warn("{}: persisting form field '{}' to type '{}' not implemented", id(form), name,
                                type);
                        break;
                }
            }
        }
    }

    private String id(Form<T> form) {
        return "form(" + form.getId() + ")"; //NON-NLS
    }
}
