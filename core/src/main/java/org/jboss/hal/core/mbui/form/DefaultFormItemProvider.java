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

import org.jboss.hal.ballroom.JsHelper;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.autocomplete.AutoComplete;
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
import org.jboss.hal.ballroom.typeahead.ReadChildResourcesTypeahead;
import org.jboss.hal.core.CoreStatementContext;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.capabilitiy.Capabilities;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.ballroom.form.NumberItem.MAX_SAFE_LONG;
import static org.jboss.hal.ballroom.form.NumberItem.MIN_SAFE_LONG;
import static org.jboss.hal.ballroom.form.SuggestHandler.SHOW_ALL_VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.meta.StatementContext.Tuple.DOMAIN_CONTROLLER;

/**
 * @author Harald Pehl
 */
class DefaultFormItemProvider implements FormItemProvider {

    private final LabelBuilder labelBuilder;
    private final Metadata metadata;

    DefaultFormItemProvider(Metadata metadata) {
        this.metadata = metadata;
        this.labelBuilder = new LabelBuilder();
    }

    @Override
    public FormItem<?> createFrom(final Property property) {
        FormItem<?> formItem = null;

        String name = property.getName();
        String label = labelBuilder.label(property);
        ModelNode attributeDescription = property.getValue();
        boolean required = attributeDescription.hasDefined(NILLABLE) && !attributeDescription.get(NILLABLE).asBoolean();
        boolean expressionAllowed = attributeDescription.hasDefined(EXPRESSIONS_ALLOWED) &&
                attributeDescription.get(EXPRESSIONS_ALLOWED).asBoolean();
        boolean runtime = attributeDescription.hasDefined(STORAGE) &&
                RUNTIME.equals(attributeDescription.get(STORAGE).asString());
        boolean readOnly = attributeDescription.hasDefined(ACCESS_TYPE) &&
                READ_ONLY.equals(attributeDescription.get(ACCESS_TYPE).asString());
        String unit = attributeDescription.hasDefined(UNIT) ? attributeDescription.get(UNIT).asString() : null;

        if (attributeDescription.hasDefined(TYPE)) {
            ModelType type = attributeDescription.get(TYPE).asType();
            ModelType valueType = (attributeDescription.has(VALUE_TYPE) && attributeDescription.get(VALUE_TYPE)
                    .getType() != ModelType.OBJECT)
                    ? ModelType.valueOf(attributeDescription.get(VALUE_TYPE).asString()) : null;

            switch (type) {
                case BOOLEAN: {
                    SwitchItem switchItem = new SwitchItem(name, label);
                    if (attributeDescription.hasDefined(DEFAULT)) {
                        switchItem.setDefaultValue(attributeDescription.get(DEFAULT).asBoolean());
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
                        numberItem.setDefaultValue(defaultValue);
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
                                    multiSelectBoxItem.setDefaultValue(defaultValues);
                                }
                            }
                            formItem = multiSelectBoxItem;
                        } else {
                            ListItem listItem = new ListItem(name, label);
                            if (attributeDescription.hasDefined(DEFAULT)) {
                                List<String> defaultValues = stringValues(attributeDescription, DEFAULT);
                                if (!defaultValues.isEmpty()) {
                                    listItem.setDefaultValue(defaultValues);
                                }
                            }
                            formItem = listItem;
                            checkCapabilityReference(attributeDescription, formItem);
                        }
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
                            propertiesItem.setDefaultValue(defaultValues);
                        }
                        formItem = propertiesItem;
                    }
                    break;
                }

                case STRING: {
                    List<String> allowedValues = stringValues(attributeDescription, ALLOWED);
                    if (allowedValues.isEmpty()) {
                        TextBoxItem textBoxItem = PASSWORD.equals(name) ? new PasswordItem(name,
                                label) : new TextBoxItem(name, label);
                        if (attributeDescription.hasDefined(DEFAULT)) {
                            textBoxItem.setDefaultValue(attributeDescription.get(DEFAULT).asString());
                        }
                        formItem = textBoxItem;
                        checkCapabilityReference(attributeDescription, formItem);
                    } else {
                        SingleSelectBoxItem singleSelectBoxItem = new SingleSelectBoxItem(name, label,
                                allowedValues, !required);
                        if (attributeDescription.hasDefined(DEFAULT)) {
                            singleSelectBoxItem.setDefaultValue(attributeDescription.get(DEFAULT).asString());
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

    private void checkCapabilityReference(final ModelNode attributeDescription, final FormItem<?> formItem) {
        SuggestHandler suggestHandler = null;
        StatementContext statementContext = CoreStatementContext.INSTANCE;

        if (attributeDescription.hasDefined(CAPABILITY_REFERENCE)) {
            String reference = attributeDescription.get(CAPABILITY_REFERENCE).asString();
            Capabilities capabilities = metadata.getCapabilities();

            if (capabilities.supportsSuggestions()) {
                AddressTemplate template = AddressTemplate.of(DOMAIN_CONTROLLER, "core-service=capability-registry");
                Operation operation = new Operation.Builder("suggest-capabilities",
                        template.resolve(statementContext))
                        .param(NAME, reference)
                        .param("dependent-address", metadata.getTemplate().resolve(statementContext))
                        .build();
                suggestHandler = new AutoComplete<String>(
                        (query, response) -> Dispatcher.INSTANCE.execute(operation, result -> {
                            List<String> list = result.asList().stream()
                                    .map(ModelNode::asString)
                                    .filter(value -> SHOW_ALL_VALUE.equals(query) || value.contains(query))
                                    .collect(toList());
                            response.response(JsHelper.asJsArray(list));
                        }));
                // suggestHandler = new SuggestCapabilitiesTypeahead(statementContext, reference, metadata.getTemplate());
            } else if (capabilities.contains(reference)) {
                suggestHandler = new ReadChildResourcesTypeahead(capabilities.lookup(reference), statementContext);
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
