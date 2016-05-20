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
package org.jboss.hal.core.mbui.form;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.FormItemProvider;
import org.jboss.hal.ballroom.form.ListItem;
import org.jboss.hal.ballroom.form.MultiSelectBoxItem;
import org.jboss.hal.ballroom.form.NumberItem;
import org.jboss.hal.ballroom.form.PasswordItem;
import org.jboss.hal.ballroom.form.PropertiesItem;
import org.jboss.hal.ballroom.form.SingleSelectBoxItem;
import org.jboss.hal.ballroom.form.SuggestHandler;
import org.jboss.hal.ballroom.form.SwitchItem;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.ballroom.typeahead.Typeahead;
import org.jboss.hal.core.CoreStatementContext;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.capabilitiy.Capabilities;

import static java.util.Collections.emptyList;
import static org.jboss.hal.ballroom.form.NumberItem.MAX_SAFE_LONG;
import static org.jboss.hal.ballroom.form.NumberItem.MIN_SAFE_LONG;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
class DefaultFormItemProvider implements FormItemProvider {

    private final Capabilities capabilities;
    private final LabelBuilder labelBuilder;
    private final StatementContext statementContext;

    DefaultFormItemProvider(final Capabilities capabilities) {
        this.capabilities = capabilities;
        this.labelBuilder = new LabelBuilder();
        this.statementContext = CoreStatementContext.INSTANCE;
    }

    @Override
    public FormItem<?> createFrom(final Property attributeDescription) {
        FormItem<?> formItem = null;

        String name = attributeDescription.getName();
        String label = labelBuilder.label(attributeDescription);
        ModelNode modelNode = attributeDescription.getValue();
        boolean required = modelNode.hasDefined(NILLABLE) && !modelNode.get(NILLABLE).asBoolean();
        boolean expressionAllowed = modelNode.hasDefined(EXPRESSIONS_ALLOWED) && modelNode.get(EXPRESSIONS_ALLOWED)
                .asBoolean();
        boolean runtime = modelNode.hasDefined(STORAGE) && RUNTIME.equals(modelNode.get(STORAGE).asString());
        boolean readOnly = modelNode.hasDefined(ACCESS_TYPE) && READ_ONLY.equals(modelNode.get(ACCESS_TYPE).asString());
        String unit = modelNode.hasDefined(UNIT) ? modelNode.get(UNIT).asString() : null;

        if (modelNode.hasDefined(TYPE)) {
            ModelType type = modelNode.get(TYPE).asType();
            ModelType valueType = (modelNode.has(VALUE_TYPE) && modelNode.get(VALUE_TYPE).getType() != ModelType.OBJECT)
                    ? ModelType.valueOf(modelNode.get(VALUE_TYPE).asString()) : null;

            switch (type) {
                case BOOLEAN: {
                    SwitchItem switchItem = new SwitchItem(name, label);
                    if (modelNode.hasDefined(DEFAULT)) {
                        switchItem.setDefaultValue(modelNode.get(DEFAULT).asBoolean());
                    }
                    formItem = switchItem;
                    break;
                }

                case BIG_INTEGER:
                case INT:
                case LONG: {
                    long min, max;
                    if (type == ModelType.INT) {
                        min = modelNode.get(MIN).asLong(Integer.MIN_VALUE);
                        max = modelNode.get(MAX).asLong(Integer.MAX_VALUE);
                    } else {
                        min = modelNode.get(MIN).asLong(MIN_SAFE_LONG);
                        max = modelNode.get(MAX).asLong(MAX_SAFE_LONG);
                    }
                    NumberItem numberItem = new NumberItem(name, label, unit, min, max);
                    if (modelNode.hasDefined(DEFAULT)) {
                        long defaultValue = modelNode.get(DEFAULT).asLong();
                        numberItem.setDefaultValue(defaultValue);
                    }
                    formItem = numberItem;
                    break;
                }

                case LIST: {
                    if (valueType != null && ModelType.STRING == valueType) {
                        List<String> allowedValues = stringValues(modelNode, ALLOWED);
                        if (!allowedValues.isEmpty()) {
                            MultiSelectBoxItem multiSelectBoxItem = new MultiSelectBoxItem(name, label, allowedValues);
                            if (modelNode.hasDefined(DEFAULT)) {
                                List<String> defaultValues = stringValues(modelNode, DEFAULT);
                                if (!defaultValues.isEmpty()) {
                                    multiSelectBoxItem.setDefaultValue(defaultValues);
                                }
                            }
                            formItem = multiSelectBoxItem;
                        } else {
                            ListItem listItem = new ListItem(name, label);
                            if (modelNode.hasDefined(DEFAULT)) {
                                List<String> defaultValues = stringValues(modelNode, DEFAULT);
                                if (!defaultValues.isEmpty()) {
                                    listItem.setDefaultValue(defaultValues);
                                }
                            }
                            formItem = listItem;
                            checkCapabilityReference(modelNode, formItem);
                        }
                    }
                    break;
                }

                case OBJECT: {
                    if (valueType != null && ModelType.STRING == valueType) {
                        PropertiesItem propertiesItem = new PropertiesItem(name, label);
                        List<Property> properties = ModelNodeHelper.getOrDefault(modelNode, DEFAULT,
                                () -> modelNode.get(DEFAULT).asPropertyList(), emptyList());
                        if (!properties.isEmpty()) {
                            Map<String, String> defaultValues = new HashMap<>();
                            for (Property property : properties) {
                                defaultValues.put(property.getName(), property.getValue().asString());
                            }
                            propertiesItem.setDefaultValue(defaultValues);
                        }
                        formItem = propertiesItem;
                    }
                    break;
                }

                case STRING: {
                    List<String> allowedValues = stringValues(modelNode, ALLOWED);
                    if (allowedValues.isEmpty()) {
                        TextBoxItem textBoxItem = PASSWORD.equals(name) ? new PasswordItem(name,
                                label) : new TextBoxItem(name, label);
                        if (modelNode.hasDefined(DEFAULT)) {
                            textBoxItem.setDefaultValue(modelNode.get(DEFAULT).asString());
                        }
                        formItem = textBoxItem;
                        checkCapabilityReference(modelNode, formItem);
                    } else {
                        SingleSelectBoxItem singleSelectBoxItem = new SingleSelectBoxItem(name, label,
                                allowedValues, !required);
                        if (modelNode.hasDefined(DEFAULT)) {
                            singleSelectBoxItem.setDefaultValue(modelNode.get(DEFAULT).asString());
                        }
                        formItem = singleSelectBoxItem;
                    }
                    break;
                }

                // unsupported types
                case BIG_DECIMAL:
                case BYTES:
                case DOUBLE:
                case EXPRESSION:
                case PROPERTY:
                case TYPE:
                case UNDEFINED:
                    break;
            }

            if (formItem != null) {
                formItem.setRequired(required);
                if (formItem.supportsExpressions()) {
                    formItem.setExpressionAllowed(expressionAllowed);
                }
                if (readOnly || runtime) {
                    formItem.setEnabled(false);
                }
            }
        }

        return formItem;
    }

    private void checkCapabilityReference(final ModelNode modelNode, final FormItem<?> formItem) {
        if (modelNode.hasDefined(CAPABILITY_REFERENCE)) {
            String reference = modelNode.get(CAPABILITY_REFERENCE).asString();
            if (capabilities.contains(reference)) {
                SuggestHandler suggestHandler = new Typeahead(capabilities.lookup(reference), statementContext);
                formItem.registerSuggestHandler(suggestHandler);
            }
        }
    }

    private List<String> stringValues(ModelNode modelNode, String property) {
        if (modelNode.hasDefined(property)) {
            List<ModelNode> nodes = ModelNodeHelper.getOrDefault(modelNode, property,
                    () -> modelNode.get(property).asList(), emptyList());
            return Lists.transform(nodes, ModelNode::asString);
        }
        return emptyList();
    }
}
