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
package org.jboss.hal.core.mbui.form;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.hal.ballroom.form.DefaultMapping;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.ModelNodeItem;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;
import static org.jboss.hal.dmr.ModelType.BIG_INTEGER;
import static org.jboss.hal.dmr.ModelType.EXPRESSION;
import static org.jboss.hal.dmr.ModelType.INT;

class ModelNodeMapping<T extends ModelNode> extends DefaultMapping<T> {

    private static final Logger logger = LoggerFactory.getLogger(ModelNodeMapping.class);
    private final List<Property> attributeDescriptions;

    ModelNodeMapping(List<Property> attributeDescriptions) {
        this.attributeDescriptions = attributeDescriptions;
    }

    @Override
    public void addAttributeDescription(final String name, final ModelNode attributeDescription) {
        attributeDescriptions.add(new Property(name, attributeDescription));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void populateFormItems(T model, Form<T> form) {
        String id = id(form);
        for (FormItem formItem : form.getBoundFormItems()) {
            formItem.clearError();

            String name = formItem.getName();
            boolean nested = name.indexOf('.') != -1;
            ModelNode value = nested
                    ? ModelNodeHelper.failSafeGet(model, name.replace('.', '/'))
                    : model.get(name);
            if (value.isDefined()) {
                ModelNode attributeDescription = findAttribute(name);
                if (attributeDescription == null) {
                    logger.error("{}: Unable to populate form item '{}': No attribute description found in\n{}",
                            id, name, attributeDescriptions);
                    continue;
                }

                ModelType valueType = value.getType();
                if (valueType == EXPRESSION) {
                    if (formItem.supportsExpressions()) {
                        formItem.setExpressionValue(value.asString());
                        formItem.setUndefined(false);
                    } else {
                        logger.error(
                                "{}: Unable to populate form item '{}': Value is an expression, but form item does not support expressions",
                                id, name);
                        continue;
                    }

                } else if (formItem instanceof ModelNodeItem) {
                    formItem.setValue(value);

                } else {
                    populateFormItem(id, name, attributeDescription, value, formItem);
                }
                formItem.setUndefined(false);

            } else {
                formItem.clearValue();
                formItem.setUndefined(true);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void populateFormItem(String id, String name, ModelNode attributeDescription, ModelNode value,
            FormItem formItem) {

        ModelType descriptionType = attributeDescription.get(TYPE).asType();
        try {
            switch (descriptionType) {
                case BOOLEAN:
                    formItem.setValue(value.asBoolean());
                    break;

                case INT:
                    // NumberItem uses *always* long
                    try {
                        formItem.setValue((long) value.asInt());
                    } catch (IllegalArgumentException e) {
                        logger.error(
                                "{}: Unable to populate form item '{}': Metadata says it's an INT, but value is not '{}'",
                                id, name, value.asString());

                    }
                    break;
                case DOUBLE:
                    try {
                        formItem.setValue(value.asDouble());
                    } catch (IllegalArgumentException e) {
                        logger.error(
                                "{}: Unable to populate form item '{}': Metadata says it's an DOUBLE, but value is not '{}'",
                                id, name, value.asString());

                    }
                    break;
                case BIG_INTEGER:
                case LONG:
                    try {
                        formItem.setValue(value.asLong());
                    } catch (IllegalArgumentException e) {
                        logger.error(
                                "{}: Unable to populate form item '{}': Metadata says it's a {}, but value is not '{}'",
                                id, name, descriptionType.name(), value.asString());
                    }
                    break;

                case LIST:
                    List<String> list = value.asList().stream().map(ModelNode::asString).collect(toList());
                    formItem.setValue(list);
                    break;

                case OBJECT:

                    boolean stringValueType = attributeDescription.get(VALUE_TYPE).getType().equals(ModelType.TYPE)
                            && attributeDescription.get(VALUE_TYPE).asType().equals(ModelType.STRING);
                    if (stringValueType) {
                        List<Property> properties = value.asPropertyList();
                        Map<String, String> map = new HashMap<>();
                        for (Property property : properties) {
                            map.put(property.getName(), property.getValue().asString());
                        }
                        formItem.setValue(map);
                    } else {
                        formItem.setValue(value);
                    }
                    break;

                case STRING:
                    formItem.setValue(value.asString());
                    break;

                // unsupported types
                case BIG_DECIMAL:
                case BYTES:
                case EXPRESSION:
                case PROPERTY:
                case TYPE:
                case UNDEFINED:
                    logger.warn("{}: populating form field '{}' of type '{}' not implemented", id,
                            name,
                            descriptionType);
                    break;
                default:
                    break;
            }
        } catch (IllegalArgumentException e) {
            logger.error(
                    "{}: Unable to populate form item '{}'. Declared type in r-r-d does not match type in model node: '{}' != '{}'",
                    id, name, descriptionType.name(), value.getType());
            formItem.setEnabled(false);
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public void persistModel(T model, Form<T> form) {
        persistModel(id(form), model, form.getBoundFormItems());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void persistModel(String id, T model, Iterable<FormItem> formItems) {
        for (FormItem formItem : formItems) {
            String name = formItem.getName();

            if (formItem.isModified()) {
                ModelNode attributeDescription = findAttribute(name);
                if (attributeDescription == null) {
                    logger.error("{}: Unable to persist attribute '{}': No attribute description found in\n{}",
                            id, name, attributeDescriptions);
                    continue;
                }
                if (formItem instanceof ModelNodeItem) {
                    if (formItem.getValue() == null) {
                        failSafeRemove(model, name);
                    } else {
                        model.get(name).set(((ModelNodeItem) formItem).getValue());
                    }

                } else {
                    if (formItem.isExpressionValue()) {
                        model.get(name).setExpression(formItem.getExpressionValue());

                    } else {
                        ModelType type = attributeDescription.get(TYPE).asType();
                        Object value = formItem.getValue();
                        switch (type) {
                            case BOOLEAN:
                                Boolean booleanValue = (Boolean) value;
                                if (booleanValue == null) {
                                    failSafeRemove(model, name);
                                } else {
                                    model.get(name).set(booleanValue);
                                }
                                break;

                            case BIG_INTEGER:
                            case INT:
                            case LONG:
                                Long longValue = (Long) value;
                                if (longValue == null) {
                                    failSafeRemove(model, name);
                                } else {
                                    if (type == BIG_INTEGER) {
                                        model.get(name).set(BigInteger.valueOf(longValue));
                                    } else if (type == INT) {
                                        model.get(name).set(longValue.intValue());
                                    } else {
                                        model.get(name).set(longValue);
                                    }
                                }
                                break;

                            case DOUBLE:
                                Double doubleValue = (Double) value;
                                if (doubleValue == null) {
                                    failSafeRemove(model, name);
                                } else {
                                    model.get(name).set(doubleValue);
                                }
                                break;

                            case LIST:
                                List<String> list = (List<String>) value;
                                if (list == null || list.isEmpty()) {
                                    failSafeRemove(model, name);
                                } else {
                                    ModelNode listNode = new ModelNode();
                                    for (String s : list) {
                                        listNode.add(s);
                                    }
                                    model.get(name).set(listNode);
                                }
                                break;

                            case OBJECT:
                                boolean stringValueType = attributeDescription.get(VALUE_TYPE)
                                        .getType()
                                        .equals(ModelType.TYPE)
                                        && attributeDescription.get(VALUE_TYPE).asType().equals(ModelType.STRING);
                                if (stringValueType) {
                                    Map<String, String> map = (Map<String, String>) value;
                                    if (map == null || map.isEmpty()) {
                                        failSafeRemove(model, name);
                                    } else {
                                        ModelNode mapNode = new ModelNode();
                                        for (Map.Entry<String, String> entry : map.entrySet()) {
                                            mapNode.get(entry.getKey()).set(entry.getValue());
                                        }
                                        model.get(name).set(mapNode);
                                    }
                                }
                                break;

                            case STRING:
                                String stringValue = value == null ? null : String.valueOf(value);
                                if (Strings.isNullOrEmpty(stringValue)) {
                                    failSafeRemove(model, name);
                                } else {
                                    model.get(name).set(stringValue);
                                }
                                break;

                            // unsupported types
                            case BIG_DECIMAL:
                            case BYTES:
                            case EXPRESSION:
                            case PROPERTY:
                            case TYPE:
                            case UNDEFINED:
                                logger.warn("{}: persisting form field '{}' to type '{}' not implemented", id,
                                        name, type);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
    }

    private void failSafeRemove(T model, String attribute) {
        if (model.isDefined()) {
            model.remove(attribute);
        }
    }

    private ModelNode findAttribute(String name) {
        for (Property property : attributeDescriptions) {
            if (name.equals(property.getName())) {
                return property.getValue();
            }
        }
        return null;
    }

    private String id(Form<T> form) {
        return "form(" + form.getId() + ")"; // NON-NLS
    }
}
