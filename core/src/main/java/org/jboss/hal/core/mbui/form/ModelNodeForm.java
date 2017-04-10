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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.HelpTextBuilder;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.AbstractForm;
import org.jboss.hal.ballroom.form.AddOnlyStateMachine;
import org.jboss.hal.ballroom.form.DataMapping;
import org.jboss.hal.ballroom.form.ExistingStateMachine;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.FormItemProvider;
import org.jboss.hal.ballroom.form.ReadOnlyStateMachine;
import org.jboss.hal.ballroom.form.SingletonStateMachine;
import org.jboss.hal.ballroom.form.StateMachine;
import org.jboss.hal.core.Core;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.AuthorisationDecision;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.security.ElementGuard;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Messages;
import org.jboss.hal.spi.Callback;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.jboss.hal.ballroom.form.Form.State.EMPTY;
import static org.jboss.hal.ballroom.form.Form.State.READONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
public class ModelNodeForm<T extends ModelNode> extends AbstractForm<T> {

    /**
     * Builder useful to automatically inspect the read-resource-description and associate the
     * attributes (by calling: include, customFormItem). Creates the required form items and help texts.
     */
    public static class Builder<T extends ModelNode> {

        private static final String ILLEGAL_COMBINATION = "Illegal combination in ";

        final String id;
        private final Metadata metadata;
        final LinkedHashSet<String> includes;
        final Set<String> excludes;
        FormItemProvider defaultFormItemProvider;
        final Map<String, FormItemProvider> providers;
        final List<UnboundFormItem> unboundFormItems;
        boolean readOnly;
        boolean addOnly;
        boolean unsorted;
        boolean requiredOnly;
        boolean includeRuntime;
        boolean hideDeprecated;
        boolean singleton;
        Supplier<org.jboss.hal.dmr.model.Operation> ping;
        EmptyState emptyState;
        String attributePath;
        SaveCallback<T> saveCallback;
        CancelCallback<T> cancelCallback;
        PrepareReset<T> prepareReset;
        PrepareRemove<T> prepareRemove;
        DataMapping<T> dataMapping;


        // ------------------------------------------------------ configure required and optional settings

        public Builder(@NonNls final String id, final Metadata metadata) {
            this.id = id;
            this.metadata = metadata;
            this.includes = new LinkedHashSet<>();
            this.excludes = new HashSet<>();
            this.defaultFormItemProvider = new DefaultFormItemProvider(metadata);
            this.providers = new HashMap<>();
            this.unboundFormItems = new ArrayList<>();
            this.readOnly = false;
            this.addOnly = false;
            this.unsorted = false;
            this.requiredOnly = false;
            this.includeRuntime = false;
            this.hideDeprecated = true;
            this.attributePath = ATTRIBUTES;
        }

        public Builder<T> include(final String[] attributes) {
            includes.addAll(Arrays.asList(attributes));
            return this;
        }

        public Builder<T> include(final Iterable<String> attributes) {
            Iterables.addAll(includes, attributes);
            return this;
        }

        public Builder<T> include(@NonNls final String first, @NonNls final String... rest) {
            includes.addAll(Lists.asList(first, rest));
            return this;
        }

        public Builder<T> exclude(final String[] attributes) {
            excludes.addAll(Arrays.asList(attributes));
            return this;
        }

        public Builder<T> exclude(final Iterable<String> attributes) {
            Iterables.addAll(excludes, attributes);
            return this;
        }

        public Builder<T> exclude(@NonNls final String first, @NonNls final String... rest) {
            excludes.addAll(Lists.asList(first, rest));
            return this;
        }

        /**
         * Use this flag if you just want to use the form to add model nodes. This will create a form with an
         * {@link AddOnlyStateMachine}.
         * <p>
         * The attributes will be taken from the {@code ATTRIBUTES} child node.
         */
        public Builder<T> addOnly() {
            this.addOnly = true;
            this.attributePath = ATTRIBUTES;
            return this;
        }

        /**
         * Use this flag if you just want to use the form to add model nodes. This will create a form with an
         * {@link AddOnlyStateMachine}.
         * <p>
         * The attributes will be taken from the {@code REQUEST_PROPERTIES} node of the {@code ADD} operation.
         */
        public Builder<T> fromRequestProperties() {
            this.addOnly = true;
            this.attributePath = OPERATIONS + "/" + ADD + "/" + REQUEST_PROPERTIES;
            return this;
        }

        public Builder<T> readOnly() {
            this.readOnly = true;
            return this;
        }

        public Builder<T> unsorted() {
            this.unsorted = true;
            return this;
        }

        public Builder<T> requiredOnly() {
            this.requiredOnly = true;
            return this;
        }

        public Builder<T> includeRuntime() {
            this.includeRuntime = true;
            return this;
        }

        public Builder<T> showDeprecated() {
            this.hideDeprecated = false;
            return this;
        }

        /**
         * Use this method if you want to manage a singleton resource. This will create a form with an
         * {@link org.jboss.hal.ballroom.form.SingletonStateMachine}.
         * <p>
         * The specified operation is used to check whether the resource exists.
         * <p>
         * If the resource does not exist, a default empty state is displayed. The empty state will contain a button
         * which will trigger the specified add action.
         */
        public Builder<T> singleton(final Supplier<org.jboss.hal.dmr.model.Operation> ping, final Callback addAction) {
            EmptyState emptyState = new EmptyState.Builder(CONSTANTS.noResource())
                    .description(MESSAGES.noResource())
                    .primaryAction(CONSTANTS.add(), addAction, Constraint.executable(metadata.getTemplate(), ADD))
                    .build();
            return singleton(ping, emptyState);
        }

        /**
         * Use this method if you want to manage a singleton resource. This will create a form with an
         * {@link org.jboss.hal.ballroom.form.SingletonStateMachine}.
         * <p>
         * The specified operation is used to check whether the resource exists.
         * <p>
         * If the resource does not exist, the specified empty state is displayed. The empty state must have a
         * button which triggers the creation of the singleton resource.
         * <p>
         * Please make sure that the primary action of the empty state has a {@linkplain Constraint constraint} attached
         * to it.
         */
        public Builder<T> singleton(final Supplier<org.jboss.hal.dmr.model.Operation> ping,
                final EmptyState emptyState) {
            this.singleton = true;
            this.ping = ping;
            this.emptyState = emptyState;
            return this;
        }

        Builder<T> defaultFormItemProvider(FormItemProvider formItemProvider) {
            this.defaultFormItemProvider = formItemProvider;
            return this;
        }

        public Builder<T> customFormItem(@NonNls final String attribute, final FormItemProvider provider) {
            includes.add(attribute);
            providers.put(attribute, provider);
            return this;
        }

        public Builder<T> unboundFormItem(final FormItem formItem) {
            return unboundFormItem(formItem, -1, null);
        }

        public Builder<T> unboundFormItem(final FormItem formItem, final int position) {
            return unboundFormItem(formItem, position, null);
        }

        public Builder<T> unboundFormItem(final FormItem formItem, final int position, final SafeHtml helpText) {
            this.unboundFormItems.add(new UnboundFormItem(formItem, position, helpText));
            return this;
        }

        Builder<T> unboundFormItem(final UnboundFormItem unboundFormItem) {
            this.unboundFormItems.add(unboundFormItem);
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

        public Builder<T> prepareReset(final PrepareReset<T> prepareReset) {
            this.prepareReset = prepareReset;
            return this;
        }

        public Builder<T> prepareRemove(final PrepareRemove<T> removeCallback) {
            this.prepareRemove = removeCallback;
            return this;
        }


        // ------------------------------------------------------ build

        public ModelNodeForm<T> build() {
            validate();
            return new ModelNodeForm<>(this);
        }

        void validate() {
            if (singleton) {
                if (readOnly || addOnly) {
                    throw new IllegalStateException(
                            ILLEGAL_COMBINATION + formId() + ": singleton && (readOnly || addOnly)");
                }
                if (ping == null) {
                    throw new IllegalStateException("No ping operation specified for singleton " + formId());
                }
                if (emptyState == null) {
                    throw new IllegalStateException("No empty state specified for singleton " + formId());
                }
            }

            if (readOnly && addOnly) {
                throw new IllegalStateException(ILLEGAL_COMBINATION + formId() + ": readOnly && addOnly");
            }

            if (!excludes.isEmpty() && !readOnly) {
                List<Property> requiredAttributes = metadata.getDescription().getRequiredAttributes(attributePath);
                for (Property attribute : requiredAttributes) {
                    if (excludes.contains(attribute.getName())) {
                        throw new IllegalStateException(
                                "Required attribute " + attribute.getName() + " must not be excluded from " + formId());
                    }
                }
            }

            if (!addOnly && !readOnly && saveCallback == null) {
                logger.warn("No save callback specified in {}", formId());
            }
        }

        StateMachine stateMachine() {
            if (addOnly) {
                return new AddOnlyStateMachine();
            } else if (readOnly) {
                return new ReadOnlyStateMachine();
            } else if (singleton) {
                return new SingletonStateMachine(prepareReset != null);
            } else {
                return new ExistingStateMachine(prepareReset != null);
            }
        }

        private String formId() {
            return "form(" + id + ")"; //NON-NLS
        }
    }


    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final Messages MESSAGES = GWT.create(Messages.class);
    @NonNls private static final Logger logger = LoggerFactory.getLogger(ModelNodeForm.class);

    private final boolean addOnly;
    private final boolean singleton;
    private final Supplier<org.jboss.hal.dmr.model.Operation> ping;
    private final Map<String, ModelNode> attributeMetadata;
    private final ResourceDescription resourceDescription;
    private final String attributePath;
    private Metadata metadata;

    @SuppressWarnings("unchecked")
    protected ModelNodeForm(final Builder<T> builder) {
        super(builder.id, builder.stateMachine(),
                builder.dataMapping != null
                        ? builder.dataMapping
                        : new ModelNodeMapping<>(
                        builder.metadata.getDescription().getAttributes(builder.attributePath)),
                builder.emptyState);

        this.addOnly = builder.addOnly;
        this.singleton = builder.singleton;
        this.ping = builder.ping;
        this.saveCallback = builder.saveCallback;
        this.cancelCallback = builder.cancelCallback;
        this.prepareReset = builder.prepareReset;
        this.prepareRemove = builder.prepareRemove;
        this.resourceDescription = builder.metadata.getDescription();
        this.attributePath = builder.attributePath;
        this.metadata = builder.metadata;

        List<Property> properties = new ArrayList<>();
        List<Property> filteredProperties = resourceDescription.getAttributes(attributePath)
                .stream()
                .filter(new PropertyFilter(builder))
                .collect(toList());
        LinkedHashMap<String, Property> filteredByName = new LinkedHashMap<>();
        for (Property property : filteredProperties) {
            filteredByName.put(property.getName(), property);
        }

        if (builder.unsorted && !builder.includes.isEmpty()) {
            // re-shuffle the properties:
            // 1. the ones specified in 'builder.includes'
            // 2. the remaining from 'filteredProperties'
            for (String include : builder.includes) {
                Property removed = filteredByName.remove(include);
                if (removed != null) {
                    properties.add(removed);
                }
            }
            properties.addAll(filteredByName.values());
        } else if (builder.unsorted) {
            properties.addAll(filteredByName.values());
        } else {
            properties.addAll(filteredProperties);
            properties.sort(Comparator.comparing(Property::getName));
        }
        this.attributeMetadata = properties.stream().collect(toMap(Property::getName, Property::getValue));

        int index = 0;
        LabelBuilder labelBuilder = new LabelBuilder();
        HelpTextBuilder helpTextBuilder = new HelpTextBuilder();
        for (Property property : properties) {

            // any unbound form items for the current index?
            for (Iterator<UnboundFormItem> iterator = builder.unboundFormItems.iterator(); iterator.hasNext(); ) {
                UnboundFormItem unboundFormItem = iterator.next();
                if (unboundFormItem.position == index) {
                    addFormItem(unboundFormItem.formItem);
                    markAsUnbound(unboundFormItem.formItem.getName());
                    if (unboundFormItem.helpText != null) {
                        addHelp(labelBuilder.label(unboundFormItem.formItem.getName()), unboundFormItem.helpText);
                    }
                    iterator.remove();
                    index++;
                }
            }

            String name = property.getName();
            ModelNode attribute = property.getValue();

            FormItem formItem;
            if (builder.providers.containsKey(name)) {
                formItem = builder.providers.get(name).createFrom(property);
            } else {
                formItem = builder.defaultFormItemProvider.createFrom(property);
            }
            if (formItem != null) {
                addFormItem(formItem);
                if (attribute.hasDefined(DESCRIPTION)) {
                    SafeHtml helpText = helpTextBuilder.helpText(property);
                    addHelp(labelBuilder.label(property), helpText);
                }
                index++;
            } else {
                logger.warn("Unable to create form item for '{}' in form '{}'", name, builder.id);
            }
        }

        // add remaining unbound form items
        for (UnboundFormItem unboundFormItem : builder.unboundFormItems) {
            addFormItem(unboundFormItem.formItem);
            markAsUnbound(unboundFormItem.formItem.getName());
            if (unboundFormItem.helpText != null) {
                addHelp(labelBuilder.label(unboundFormItem.formItem.getName()), unboundFormItem.helpText);
            }
        }

        // requires & alternatives
        Set<String> processedAlternatives = new HashSet<>();
        getFormItems().forEach((FormItem formItem) -> {
            String name = formItem.getName();

            // requires
            List<FormItem> requires = resourceDescription.findRequires(attributePath, name).stream()
                    .map(this::getFormItem)
                    .filter(Objects::nonNull)
                    .collect(toList());
            if (!requires.isEmpty()) {
                formItem.addValueChangeHandler(
                        event -> requires.forEach(rf -> rf.setEnabled(!isEmptyOrDefault(formItem))));
            }

            // alternatives
            List<String> alternatives = resourceDescription.findAlternatives(attributePath, name);
            HashSet<String> uniqueAlternatives = new HashSet<>(alternatives);
            uniqueAlternatives.add(name);
            uniqueAlternatives.removeAll(processedAlternatives);
            if (uniqueAlternatives.size() > 1) {

                Set<String> requiredAlternatives = new HashSet<>();
                uniqueAlternatives.forEach(alternative -> {
                    ModelNode attribute = attributeMetadata.getOrDefault(alternative, new ModelNode());
                    if (ModelNodeHelper.failSafeBoolean(attribute, REQUIRED)) { // don't use 'nillable' here!
                        requiredAlternatives.add(alternative);
                    }
                });

                if (requiredAlternatives.size() > 1) {
                    // validate that exactly one of the required alternatives is defined
                    addFormValidation(new ExactlyOneAlternativeValidation<>(requiredAlternatives, CONSTANTS, MESSAGES));
                }
                // validate that not more than one of the alternatives is defined
                addFormValidation(new NotMoreThanOneAlternativeValidation<>(uniqueAlternatives, this, CONSTANTS,
                        MESSAGES));

                processedAlternatives.addAll(uniqueAlternatives);
            }
        });
    }

    @Override
    public void attach() {
        super.attach();
        if (Iterables.isEmpty(getFormItems())) {
            Alert alert = new Alert(Icons.INFO, MESSAGES.emptyModelNodeForm());
            Elements.removeChildrenFrom(asElement());
            asElement().appendChild(alert.asElement());
        }
        if (singleton && ping != null && ping.get() != null) {
            Core.INSTANCE.dispatcher().execute(ping.get(), result -> flip(READONLY), (op, failure) -> flip(EMPTY));
        }
    }

    @Override
    protected void prepare(final State state) {
        super.prepare(state);

        Metadata update = metadata.updateSecurityContext();
        if (update != metadata) {
            metadata = update;
            SecurityContext securityContext = metadata.getSecurityContext();

            switch (state) {
                case EMPTY:
                    ElementGuard.processElements(
                            AuthorisationDecision.strict(Core.INSTANCE.environment(), securityContext), asElement());
                    break;

                case READONLY:
                case EDITING:
                    // change restricted and enabled state
                    getBoundFormItems().forEach(formItem -> {
                        formItem.setRestricted(!securityContext.isReadable(formItem.getName()));
                        formItem.setEnabled(securityContext.isWritable(formItem.getName()));
                    });
                    break;
            }
        }

        // adjust form links in any case
        if (!metadata.getSecurityContext().isWritable()) {
            formLinks.setVisible(Operation.EDIT, false);
            formLinks.setVisible(Operation.RESET, false);
            formLinks.setVisible(Operation.REMOVE, false);
        }
    }

    @Override
    protected void prepareEditState() {
        super.prepareEditState();
        getFormItems().forEach(this::evalRequires);
    }

    private void evalRequires(FormItem formItem) {
        String name = formItem.getName();
        List<FormItem> requires = resourceDescription.findRequires(attributePath, name).stream()
                .map(this::getFormItem)
                .filter(Objects::nonNull)
                .collect(toList());
        if (!requires.isEmpty()) {
            requires.forEach(rf -> rf.setEnabled(!isEmptyOrDefault(formItem)));
        }
    }

    @Override
    public boolean isUndefined() {
        return getModel() == null || !getModel().isDefined();
    }

    @Override
    public boolean isTransient() {
        return addOnly || (getModel() != null && !getModel().isDefined());
    }

    /**
     * @return only the changed values w/ {@code "access-type" => "read-write"}.
     */
    @Override
    protected Map<String, Object> getChangedValues() {
        Map<String, Object> writableChanges = new HashMap<>(super.getChangedValues());
        writableChanges.entrySet().removeIf(entry -> {
            ModelNode metadata = attributeMetadata.get(entry.getKey());
            return metadata != null && metadata.hasDefined(ACCESS_TYPE) && !READ_WRITE
                    .equals(metadata.get(ACCESS_TYPE).asString());
        });
        return writableChanges;
    }

    boolean isEmptyOrDefault(FormItem formItem) {
        String name = formItem.getName();
        Object value = formItem.getValue();
        ModelNode attributeDescription = attributeMetadata.get(name);
        if (attributeDescription != null) {
            if (attributeDescription.hasDefined(DEFAULT)) {
                return resourceDescription.isDefaultValue(attributePath, name, value);
            } else if (attributeDescription.get(TYPE).asType() == ModelType.BOOLEAN) {
                return !(Boolean) value;
            } else {
                return formItem.isEmpty();
            }
        }
        return formItem.isEmpty();
    }
}
