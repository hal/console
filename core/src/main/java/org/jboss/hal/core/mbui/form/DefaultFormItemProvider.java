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

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.autocomplete.SuggestCapabilitiesAutoComplete;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.FormItemProvider;
import org.jboss.hal.ballroom.form.ListItem;
import org.jboss.hal.ballroom.form.MultiSelectBoxItem;
import org.jboss.hal.ballroom.form.NumberItem;
import org.jboss.hal.ballroom.form.PropertiesItem;
import org.jboss.hal.ballroom.form.SingleSelectBoxItem;
import org.jboss.hal.ballroom.form.SuggestHandler;
import org.jboss.hal.ballroom.form.SwitchItem;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.core.Core;
import org.jboss.hal.dmr.Deprecation;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.ballroom.form.NumberItem.MAX_SAFE_LONG;
import static org.jboss.hal.ballroom.form.NumberItem.MIN_SAFE_LONG;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;

class DefaultFormItemProvider implements FormItemProvider {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(DefaultFormItemProvider.class);

    private final Metadata metadata;
    private final LabelBuilder labelBuilder;

    DefaultFormItemProvider(Metadata metadata) {
        this.metadata = metadata;
        this.labelBuilder = new LabelBuilder();
    }

    @Override
    public FormItem<?> createFrom(Property property) {
        FormItem<?> formItem = null;

        String name = property.getName();
        String label = labelBuilder.label(property);
        ModelNode attributeDescription = property.getValue();
        // don't use 'required' here!
        boolean required = attributeDescription.hasDefined(NILLABLE) && !attributeDescription.get(NILLABLE).asBoolean();
        boolean expressionAllowed = attributeDescription.hasDefined(EXPRESSIONS_ALLOWED) &&
                attributeDescription.get(EXPRESSIONS_ALLOWED).asBoolean();
        boolean readOnly = attributeDescription.hasDefined(ACCESS_TYPE) &&
                (READ_ONLY.equals(attributeDescription.get(ACCESS_TYPE).asString())
                        || METRIC.equals(attributeDescription.get(ACCESS_TYPE).asString()));
        String unit = attributeDescription.hasDefined(UNIT) ? attributeDescription.get(UNIT).asString() : null;
        Deprecation deprecation = attributeDescription.hasDefined(DEPRECATED) ? new Deprecation(
                attributeDescription.get(DEPRECATED)) : null;

        if (attributeDescription.hasDefined(TYPE)) {
            ModelType type = attributeDescription.get(TYPE).asType();
            ModelType valueType = (attributeDescription.has(VALUE_TYPE) && attributeDescription.get(VALUE_TYPE)
                    .getType() != ModelType.OBJECT)
                    ? ModelType.valueOf(attributeDescription.get(VALUE_TYPE).asString()) : null;

            switch (type) {
                case BOOLEAN: {
                    SwitchItem switchItem = new SwitchItem(name, label);
                    if (attributeDescription.hasDefined(DEFAULT)) {
                        switchItem.assignDefaultValue(attributeDescription.get(DEFAULT).asBoolean());
                    }
                    formItem = switchItem;
                    break;
                }

                case BIG_INTEGER:
                case INT:
                case LONG: {
                    long min, max;
                    if (type == ModelType.INT) {
                        min = attributeDescription.get(MIN).asLong(Integer.MIN_VALUE);
                        max = attributeDescription.get(MAX).asLong(Integer.MAX_VALUE);
                    } else {
                        min = attributeDescription.get(MIN).asLong(MIN_SAFE_LONG);
                        max = attributeDescription.get(MAX).asLong(MAX_SAFE_LONG);
                    }
                    NumberItem numberItem = new NumberItem(name, label, unit, min, max);
                    if (attributeDescription.hasDefined(DEFAULT)) {
                        long defaultValue = attributeDescription.get(DEFAULT).asLong();
                        numberItem.assignDefaultValue(defaultValue);
                    }
                    formItem = numberItem;
                    break;
                }

                case LIST: {
                    if (valueType != null && ModelType.STRING == valueType) {
                        List<String> allowedValues = stringValues(attributeDescription, ALLOWED);
                        if (!allowedValues.isEmpty()) {
                            MultiSelectBoxItem multiSelectBoxItem = new MultiSelectBoxItem(name, label, allowedValues);
                            if (attributeDescription.hasDefined(DEFAULT)) {
                                List<String> defaultValues = stringValues(attributeDescription, DEFAULT);
                                if (!defaultValues.isEmpty()) {
                                    multiSelectBoxItem.assignDefaultValue(defaultValues);
                                }
                            }
                            formItem = multiSelectBoxItem;
                        } else {
                            ListItem listItem = new ListItem(name, label);
                            if (attributeDescription.hasDefined(DEFAULT)) {
                                List<String> defaultValues = stringValues(attributeDescription, DEFAULT);
                                if (!defaultValues.isEmpty()) {
                                    listItem.assignDefaultValue(defaultValues);
                                }
                            }
                            formItem = listItem;
                            checkCapabilityReference(attributeDescription, formItem);
                        }
                    } else {
                        logger.warn(
                                "Unsupported model type {} for attribute {} in metadata {}. Unable to create a form item. Attribute will be skipped.",
                                type.name(), property.getName(), metadata.getTemplate());
                        break;
                    }
                    break;
                }

                case OBJECT: {
                    if (valueType != null && ModelType.STRING == valueType) {
                        PropertiesItem propertiesItem = new PropertiesItem(name, label);
                        List<Property> properties = ModelNodeHelper.getOrDefault(attributeDescription, DEFAULT,
                                () -> attributeDescription.get(DEFAULT).asPropertyList(), emptyList());
                        if (!properties.isEmpty()) {
                            Map<String, String> defaultValues = new HashMap<>();
                            for (Property p : properties) {
                                defaultValues.put(p.getName(), p.getValue().asString());
                            }
                            propertiesItem.assignDefaultValue(defaultValues);
                        }
                        formItem = propertiesItem;
                    }
                    break;
                }

                case STRING: {
                    List<String> allowedValues = stringValues(attributeDescription, ALLOWED);
                    if (allowedValues.isEmpty()) {
                        FormItem<String> textBoxItem = new TextBoxItem(name, label, null);
                        boolean sensitive = failSafeGet(attributeDescription,
                                ACCESS_CONSTRAINTS + "/" + SENSITIVE).isDefined();
                        if (PASSWORD.equals(name) || sensitive) {
                            textBoxItem.mask();
                        }
                        if (attributeDescription.hasDefined(DEFAULT)) {
                            textBoxItem.assignDefaultValue(attributeDescription.get(DEFAULT).asString());
                        }
                        formItem = textBoxItem;
                        checkCapabilityReference(attributeDescription, formItem);
                    } else {
                        SingleSelectBoxItem singleSelectBoxItem = new SingleSelectBoxItem(name, label,
                                allowedValues, !required);
                        if (attributeDescription.hasDefined(DEFAULT)) {
                            singleSelectBoxItem.assignDefaultValue(attributeDescription.get(DEFAULT).asString());
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
                    logger.warn(
                            "Unsupported model type {} for attribute {} in metadata {}. Unable to create a form item. Attribute will be skipped.",
                            type.name(), property.getName(), metadata.getTemplate());
                    break;
                default:
                    break;
            }

            if (formItem != null) {
                formItem.setRequired(required);
                formItem.setDeprecated(deprecation);
                if (formItem.supportsExpressions()) {
                    formItem.setExpressionAllowed(expressionAllowed);
                    formItem.addResolveExpressionHandler(event -> {
                        // resend as application event
                        Core.INSTANCE.eventBus().fireEvent(event);
                    });
                }
                if (readOnly) {
                    formItem.setEnabled(false);
                }
            }
        }

        return formItem;
    }

    private void checkCapabilityReference(ModelNode attributeDescription, FormItem<?> formItem) {
        SuggestHandler suggestHandler = null;

        if (attributeDescription.hasDefined(CAPABILITY_REFERENCE)) {
            Dispatcher dispatcher = Core.INSTANCE.dispatcher();
            StatementContext statementContext = Core.INSTANCE.statementContext();
            String reference = attributeDescription.get(CAPABILITY_REFERENCE).asString();
            Capabilities capabilities = metadata.getCapabilities();

            if (capabilities.supportsSuggestions()) {
                suggestHandler = new SuggestCapabilitiesAutoComplete(dispatcher, statementContext, reference,
                        metadata.getTemplate());
            } else if (capabilities.contains(reference)) {
                suggestHandler = new ReadChildrenAutoComplete(dispatcher, statementContext,
                        capabilities.lookup(reference));
            }
        }
        if (suggestHandler != null) {
            formItem.registerSuggestHandler(suggestHandler);
        }
    }

    private List<String> stringValues(ModelNode modelNode, String property) {
        if (modelNode.hasDefined(property)) {
            List<ModelNode> nodes = ModelNodeHelper.getOrDefault(modelNode, property,
                    () -> modelNode.get(property).asList(), emptyList());
            return nodes.stream().map(ModelNode::asString).collect(toList());
        }
        return emptyList();
    }
}
