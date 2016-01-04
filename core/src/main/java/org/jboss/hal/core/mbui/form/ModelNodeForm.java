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

import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.jboss.hal.ballroom.form.DefaultForm;
import org.jboss.hal.ballroom.form.DefaultStateMachine;
import org.jboss.hal.ballroom.form.EditOnlyStateMachine;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.FormItemProvider;
import org.jboss.hal.ballroom.form.StateMachine;
import org.jboss.hal.ballroom.form.ViewOnlyStateMachine;
import org.jboss.hal.core.mbui.LabelBuilder;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelType.BIG_DECIMAL;
import static org.jboss.hal.dmr.ModelType.BIG_INTEGER;
import static org.jboss.hal.dmr.ModelType.INT;

/**
 * @author Harald Pehl
 */
public class ModelNodeForm<T extends ModelNode> extends DefaultForm<T> {

    public static class Builder<T extends ModelNode> {

        final String id;
        final SecurityContext securityContext;
        final ResourceDescription resourceDescription;
        final Set<String> includes;
        final Set<String> excludes;
        final Map<String, FormItemProvider> providers;
        final Map<String, SaveOperationStep> saveOperations;
        boolean createResource;
        boolean viewOnly;
        boolean editOnly;
        boolean unsorted;
        boolean includeRuntime;
        boolean hideButtons;
        SaveCallback<T> saveCallback;
        CancelCallback<T> cancelCallback;
        ResetCallback<T> resetCallback;


        // ------------------------------------------------------ configure required and optional settings

        public Builder(final String id, final SecurityContext securityContext,
                final ResourceDescription resourceDescription) {
            this.id = id;
            this.securityContext = securityContext;
            this.resourceDescription = resourceDescription;
            this.includes = new HashSet<>();
            this.excludes = new HashSet<>();
            this.providers = new HashMap<>();
            this.saveOperations = new HashMap<>();
            this.createResource = false;
            this.viewOnly = false;
            this.editOnly = false;
            this.unsorted = false;
            this.includeRuntime = false;
        }

        public Builder<T> include(final String first, final String... rest) {
            includes.addAll(Lists.asList(first, rest));
            return this;
        }

        public Builder<T> exclude(final String first, final String... rest) {
            excludes.addAll(Lists.asList(first, rest));
            return this;
        }

        public Builder<T> createResource() {
            this.createResource = true;
            return this;
        }

        public Builder<T> viewOnly() {
            this.viewOnly = true;
            return this;
        }

        public Builder<T> editOnly() {
            this.editOnly = true;
            return this;
        }

        public Builder<T> unsorted() {
            this.unsorted = true;
            return this;
        }

        public Builder<T> includeRuntime() {
            this.includeRuntime = true;
            return this;
        }

        public Builder<T> customFormItem(final String attribute, final FormItemProvider provider) {
            return customFormItem(attribute, provider, null);
        }

        public Builder<T> customFormItem(final String attribute, final FormItemProvider provider,
                final SaveOperationStep saveOperation) {
            providers.put(attribute, provider);
            if (saveOperation != null) {
                saveOperations.put(attribute, saveOperation);
            }
            return this;
        }

        public Builder<T> onSave(final SaveCallback<T> saveCallback) {
            this.saveCallback = saveCallback;
            return this;
        }

        public Builder<T> onCancel(final CancelCallback<T> cancelCallback) {
            this.cancelCallback = cancelCallback;
            return this;
        }

        public Builder<T> onReset(final ResetCallback<T> resetCallback) {
            this.resetCallback = resetCallback;
            return this;
        }

        public Builder<T> hideButtons() {
            this.hideButtons = true;
            return this;
        }


        // ------------------------------------------------------ build

        public ModelNodeForm<T> build() {
            validate();
            return new ModelNodeForm<>(this);
        }

        void validate() {
            if (viewOnly && editOnly) {
                throw new IllegalStateException("Illegal combination for " + formId() + ": viewOnly && editOnly");
            }
            if (createResource) {
                if (viewOnly) {
                    throw new IllegalStateException(
                            "Illegal combination for " + formId() + ": createResource && viewOnly");
                }
                String path = OPERATIONS + "." + ADD + "." + REQUEST_PROPERTIES;
                if (!ModelNodeHelper.failSafeGet(resourceDescription, path).isDefined()) {
                    throw new IllegalStateException("No request properties found for " + formId() +
                            " / operation add in resource description " + resourceDescription);
                }
                if (!excludes.isEmpty()) {
                    List<Property> requiredRequestProperties = resourceDescription.getRequiredRequestProperties();
                    for (Property property : requiredRequestProperties) {
                        if (excludes.contains(property.getName())) {
                            throw new IllegalStateException("Required request property " + property.getName() +
                                    " must not be excluded from " + formId() + " when using createMode == true");
                        }
                    }
                }
            } else {
                if (!resourceDescription.hasDefined(ATTRIBUTES)) {
                    throw new IllegalStateException("No attributes found for " + formId() +
                            " in resource description " + resourceDescription);
                }
            }
        }

        StateMachine stateMachine() {
            return createResource || editOnly ?
                    new EditOnlyStateMachine() :
                    (viewOnly ? new ViewOnlyStateMachine() : new DefaultStateMachine());
        }

        private String formId() {
            return "form(" + id + ")"; //NON-NLS
        }
    }


    private static final Logger logger = LoggerFactory.getLogger(ModelNodeForm.class);

    private final ResourceDescription resourceDescription;
    private final FormItemProvider defaultFormItemProvider;
    private final Map<String, SaveOperationStep> saveOperations;

    private ModelNodeForm(final Builder<T> builder) {
        super(builder.id, builder.stateMachine(), builder.securityContext);

        this.resourceDescription = builder.resourceDescription;
        this.defaultFormItemProvider = new DefaultFormItemProvider();
        this.saveOperations = builder.saveOperations;
        this.saveCallback = builder.saveCallback;
        this.cancelCallback = builder.cancelCallback;
        this.resetCallback = builder.resetCallback;

        String path = builder.createResource ? Joiner.on('.').join(OPERATIONS, ADD, REQUEST_PROPERTIES) : ATTRIBUTES;
        Iterable<Property> allProperties = ModelNodeHelper.failSafeGet(builder.resourceDescription, path)
                .asPropertyList();
        FluentIterable<Property> fi = FluentIterable.from(allProperties).filter(new PropertyFilter(builder));
        Iterable<Property> filtered = builder.unsorted ? fi.toList() :
                fi.toSortedList((p1, p2) -> p1.getName().compareTo(p2.getName()));

        LabelBuilder labelBuilder = new LabelBuilder();
        for (Property property : filtered) {
            String name = property.getName();
            ModelNode attribute = property.getValue();

            FormItem formItem;
            if (builder.providers.containsKey(name)) {
                formItem = builder.providers.get(name).createFrom(property);
            } else {
                formItem = defaultFormItemProvider.createFrom(property);
            }
            if (formItem != null) {
                addFormItem(formItem);
                if (attribute.hasDefined(DESCRIPTION)) {
                    addHelp(labelBuilder.label(property), attribute.get(DESCRIPTION).asString());
                }
            } else {
                logger.warn("Unable to create form item for '{}' in form '{}'", name, builder.id); //NON-NLS
            }
        }
    }

    @Override
    public void persistModel() {
        T model = getModel();

        for (FormItem formItem : getFormItems()) {
            String name = formItem.getName();
            ModelNode attribute = model.get(name);

            if (formItem.isUndefined()) {
                attribute.set(ModelType.UNDEFINED);

            } else if (formItem.isModified()) {
                ModelNode attributeDescription = resourceDescription.find(name);
                if (attributeDescription == null) {
                    //noinspection HardCodedStringLiteral
                    logger.error("{}: Unable to persist '{}': No attribute description found in\n{}", formId(), name,
                            resourceDescription);
                    continue;
                }
                ModelType type = attributeDescription.get(TYPE).asType();
                Object value = formItem.getValue();
                switch (type) {
                    case BOOLEAN:
                        attribute.set((Boolean) value);
                        break;

                    case BIG_INTEGER:
                    case INT:
                    case LONG:
                        Long longValue = (Long) value;
                        if (type == BIG_INTEGER) {
                            attribute.set(BigInteger.valueOf(longValue));
                        } else if (type == INT) {
                            attribute.set(longValue.intValue());
                        } else {
                            attribute.set(longValue);
                        }
                        break;

                    case BIG_DECIMAL:
                    case DOUBLE:
                        Double doubleValue = (Double) value;
                        if (type == BIG_DECIMAL) {
                            attribute.set(BigDecimal.valueOf(doubleValue));
                        } else {
                            attribute.set(doubleValue);
                        }
                        break;

                    case STRING:
                        attribute.set(String.valueOf(value));
                        break;

                    case BYTES:
                    case EXPRESSION:
                    case LIST:
                    case OBJECT:
                    case PROPERTY:
                    case TYPE:
                    case UNDEFINED:
                        //noinspection HardCodedStringLiteral
                        logger.warn("{}: persisting form field '{}' to type '{}' not yet implemented", formId(), name,
                                type);
                        break;
                }
            }
        }
    }
}
