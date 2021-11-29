/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.core.mbui.form;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.JsPropertyMap;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.HelpTextBuilder;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.AbstractForm;
import org.jboss.hal.ballroom.form.AddOnlyStateMachine;
import org.jboss.hal.ballroom.form.ExistingStateMachine;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.FormItemProvider;
import org.jboss.hal.ballroom.form.ReadOnlyStateMachine;
import org.jboss.hal.ballroom.form.SingletonStateMachine;
import org.jboss.hal.ballroom.form.StateMachine;
import org.jboss.hal.core.Core;
import org.jboss.hal.dmr.ModelNode;
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
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Messages;
import org.jboss.hal.spi.Callback;
import org.jboss.hal.spi.EsParam;
import org.jboss.hal.spi.EsReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.jboss.hal.ballroom.form.Form.State.EMPTY;
import static org.jboss.hal.ballroom.form.Form.State.READONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeBoolean;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeList;
import static org.jboss.hal.js.JsHelper.asJsMap;

public class ModelNodeForm<T extends ModelNode> extends AbstractForm<T> {

    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final Messages MESSAGES = GWT.create(Messages.class);
    private static final Logger logger = LoggerFactory.getLogger(ModelNodeForm.class);

    private final boolean addOnly;
    private final boolean singleton;
    private final Supplier<org.jboss.hal.dmr.Operation> ping;
    private final Map<String, ModelNode> attributeDescriptions;
    private final ResourceDescription resourceDescription;
    private final String attributePath;
    private Metadata metadata;

    protected ModelNodeForm(Builder<T> builder) {
        super(builder.id, builder.stateMachine(),
                new ModelNodeMapping<>(builder.metadata.getDescription().getAttributes(builder.attributePath)),
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
        this.attributeDescriptions = properties.stream().collect(toMap(Property::getName, Property::getValue));

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

        // create form validations from requires and alternatives
        HashMultimap<String, String> requires = HashMultimap.create();
        Set<String> processedAlternatives = new HashSet<>();
        for (FormItem formItem : getBoundFormItems()) {
            String name = formItem.getName();

            // requires (1)
            ModelNode attributeDescription = attributeDescriptions.get(name);
            if (attributeDescription != null && attributeDescription.hasDefined(REQUIRES)) {
                // collect all attributes from the 'requires' list of this attribute
                // which are not required themselves.
                failSafeList(attributeDescription, REQUIRES).stream()
                        .map(ModelNode::asString)
                        .forEach(requiresName -> {
                            ModelNode requiresDescription = attributeDescriptions.get(requiresName);
                            if (requiresDescription != null && !failSafeBoolean(requiresDescription, REQUIRED)) {
                                requires.put(requiresName, name);
                            }
                        });
            }

            // alternatives
            List<String> alternatives = resourceDescription.findAlternatives(attributePath, name);
            HashSet<String> uniqueAlternatives = new HashSet<>(alternatives);
            uniqueAlternatives.add(name);
            uniqueAlternatives.removeAll(processedAlternatives);
            if (uniqueAlternatives.size() > 1) {

                Set<String> requiredAlternatives = new HashSet<>();
                uniqueAlternatives.forEach(alternative -> {
                    ModelNode attribute = attributeDescriptions.getOrDefault(alternative, new ModelNode());
                    if (failSafeBoolean(attribute, REQUIRED)) { // don't use 'nillable' here!
                        requiredAlternatives.add(alternative);
                    }
                });

                if (requiredAlternatives.size() > 1) {
                    // validate that exactly one of the required alternatives is defined
                    addFormValidation(new ExactlyOneAlternativeValidation<>(requiredAlternatives, CONSTANTS, MESSAGES));
                }

                if (builder.requiredOnly && requiredAlternatives.size() == 1) {
                    // if the form displays only one required alternative then display it as required
                    getFormItem(name).setRequired(true);
                }

                // validate that not more than one of the alternatives is defined
                addFormValidation(new NotMoreThanOneAlternativeValidation<>(uniqueAlternatives, this, CONSTANTS,
                        MESSAGES));

                processedAlternatives.addAll(uniqueAlternatives);
            }
        }

        // requires (2)
        requires.asMap().forEach((name, requiredBy) -> {
            FormItem<Object> formItem = getFormItem(name);
            if (formItem != null) {
                formItem.addValidationHandler(
                        new RequiredByValidation<>(formItem, requiredBy, this, CONSTANTS, MESSAGES));
            }
        });
    }

    @Override
    @JsMethod
    public void attach() {
        super.attach();

        if (Iterables.isEmpty(getFormItems())) {
            Alert alert = new Alert(Icons.INFO, MESSAGES.emptyModelNodeForm());
            Elements.removeChildrenFrom(element());
            element().appendChild(alert.element());
        }

        if (singleton && ping != null && ping.get() != null) {
            Core.INSTANCE.dispatcher().execute(ping.get(),
                    result -> {
                        if (!result.isDefined()) {
                            flip(EMPTY);
                        } else {
                            flip(READONLY);
                        }
                    }, (op, failure) -> flip(READONLY));
        }
    }

    @Override
    protected void prepare(State state) {
        super.prepare(state);

        SecurityContext securityContext = metadata.getSecurityContext();
        switch (state) {
            case EMPTY:
                ElementGuard.processElements(
                        AuthorisationDecision.from(Core.INSTANCE.environment(), securityContext), element());
                break;

            case READONLY:
            case EDITING:
                // change restricted and enabled state
                for (FormItem formItem : getBoundFormItems()) {
                    String name = formItem.getName();
                    int pos = name.indexOf('.');
                    if (pos > 0) {
                        name = name.substring(0,pos);
                    }
                    formItem.setRestricted(!securityContext.isReadable(name));
                    // don't touch disabled form items
                    if (formItem.isEnabled()) {
                        formItem.setEnabled(securityContext.isWritable(name));
                    }
                }
                break;
            default:
                break;
        }

        // adjust form links in any case
        if (!securityContext.isWritable()) {
            formLinks.setVisible(Operation.EDIT, false);
            formLinks.setVisible(Operation.RESET, false);
            formLinks.setVisible(Operation.REMOVE, false);
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
            ModelNode metadata = attributeDescriptions.get(entry.getKey());
            return metadata != null && metadata.hasDefined(ACCESS_TYPE) && !READ_WRITE
                    .equals(metadata.get(ACCESS_TYPE).asString());
        });
        return writableChanges;
    }

    boolean isEmptyOrDefault(FormItem formItem) {
        boolean emptyOrDefault = formItem == null;
        if (formItem != null) {
            String name = formItem.getName();
            Object value = formItem.getValue();
            ModelNode attributeDescription = attributeDescriptions.get(name);
            if (attributeDescription != null) {
                if (attributeDescription.hasDefined(DEFAULT)) {
                    emptyOrDefault = resourceDescription.isDefaultValue(attributePath, name,
                            value) || formItem.isEmpty();
                } else if (attributeDescription.get(TYPE).asType() == ModelType.BOOLEAN) {
                    emptyOrDefault = value == null || !(Boolean) value;
                } else {
                    emptyOrDefault = formItem.isEmpty();
                }
            } else {
                emptyOrDefault = formItem.isEmpty();
            }
        }
        return emptyOrDefault;
    }


    // ------------------------------------------------------ JS methods

    @JsProperty(name = "element")
    public HTMLElement jsElement() {
        return element();
    }


    // ------------------------------------------------------ inner classes


    /**
     * Builder to create forms based on resource metadata. By default the form includes all non-deprecated attributes
     * with <code>"storage" =&gt; "configuration"</code>.
     */
    @SuppressWarnings("unused")
    @JsType(namespace = "hal.ui", name = "FormBuilder")
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
        boolean verifyExcludes;
        Supplier<org.jboss.hal.dmr.Operation> ping;
        EmptyState emptyState;
        String attributePath;
        SaveCallback<T> saveCallback;
        CancelCallback<T> cancelCallback;
        PrepareReset<T> prepareReset;
        PrepareRemove<T> prepareRemove;
        boolean panelForOptionalAttributes;


        // ------------------------------------------------------ configure required and optional settings

        @JsIgnore
        public Builder(String id, Metadata metadata) {
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
            this.verifyExcludes = true;
            this.attributePath = ATTRIBUTES;
        }

        @JsIgnore
        public Builder<T> include(String[] attributes) {
            includes.addAll(asList(attributes));
            return this;
        }

        @JsIgnore
        public Builder<T> include(Iterable<String> attributes) {
            Iterables.addAll(includes, attributes);
            return this;
        }

        @JsIgnore
        public Builder<T> include(String first, String... rest) {
            includes.addAll(Lists.asList(first, rest));
            return this;
        }

        @JsIgnore
        public Builder<T> exclude(String[] attributes) {
            excludes.addAll(asList(attributes));
            return this;
        }

        @JsIgnore
        public Builder<T> exclude(Iterable<String> attributes) {
            Iterables.addAll(excludes, attributes);
            return this;
        }

        @JsIgnore
        public Builder<T> exclude(String first, String... rest) {
            excludes.addAll(Lists.asList(first, rest));
            return this;
        }

        /**
         * Use this flag if you just want to use the form to add model nodes. The attributes will be taken from the
         * ATTRIBUTES child node.
         */
        @EsReturn("FormBuilder")
        public Builder<T> addOnly() {
            this.addOnly = true;
            this.attributePath = ATTRIBUTES;
            return this;
        }

        /**
         * Use this flag if you just want to use the form to add model nodes. The attributes will be taken from the
         * REQUEST_PROPERTIES node of the ADD operation.
         */
        @EsReturn("FormBuilder")
        public Builder<T> fromRequestProperties() {
            this.addOnly = true;
            this.attributePath = OPERATIONS + "/" + ADD + "/" + REQUEST_PROPERTIES;
            return this;
        }

        /** Makes the form read-only. */
        @EsReturn("FormBuilder")
        public Builder<T> readOnly() {
            this.readOnly = true;
            return this;
        }

        /** Doesn't sort the attributes alphabetically. */
        @EsReturn("FormBuilder")
        public Builder<T> unsorted() {
            this.unsorted = true;
            return this;
        }

        /** Includes only required attributes */
        @EsReturn("FormBuilder")
        public Builder<T> requiredOnly() {
            this.requiredOnly = true;
            return this;
        }

        /** Includes also attributes with <code>"storage" =&gt; "runtime"</code> */
        @EsReturn("FormBuilder")
        public Builder<T> includeRuntime() {
            this.includeRuntime = true;
            return this;
        }

        /** Includes also deprecated attributes */
        @EsReturn("FormBuilder")
        public Builder<T> showDeprecated() {
            this.hideDeprecated = false;
            return this;
        }

        public Builder<T> dontVerifyExcludes() {
            this.verifyExcludes = false;
            return this;
        }

        /**
         * Use this method if you want to manage a singleton resource. This will create a form with an {@link
         * org.jboss.hal.ballroom.form.SingletonStateMachine}.
         * <p>
         * The specified operation is used to check whether the resource exists.
         * <p>
         * If the resource does not exist, a default empty state is displayed. The empty state will contain a button
         * which will trigger the specified add action.
         */
        @JsIgnore
        public Builder<T> singleton(Supplier<org.jboss.hal.dmr.Operation> ping, Callback addAction) {
            EmptyState emptyState = new EmptyState.Builder(Ids.build(id, Ids.EMPTY), CONSTANTS.noResource())
                    .description(MESSAGES.noResource())
                    .primaryAction(CONSTANTS.add(), addAction, Constraint.executable(metadata.getTemplate(), ADD))
                    .build();
            return singleton(ping, emptyState);
        }

        /**
         * Use this method if you want to manage a singleton resource. This will create a form with an {@link
         * org.jboss.hal.ballroom.form.SingletonStateMachine}.
         * <p>
         * The specified operation is used to check whether the resource exists.
         * <p>
         * If the resource does not exist, the specified empty state is displayed. The empty state must have a button
         * which triggers the creation of the singleton resource.
         * <p>
         * Please make sure that the primary action of the empty state has a {@linkplain Constraint constraint} attached
         * to it.
         */
        @JsIgnore
        public Builder<T> singleton(Supplier<org.jboss.hal.dmr.Operation> ping,
                EmptyState emptyState) {
            this.singleton = true;
            this.ping = ping;
            this.emptyState = emptyState;
            return this;
        }

        Builder<T> defaultFormItemProvider(FormItemProvider formItemProvider) {
            this.defaultFormItemProvider = formItemProvider;
            return this;
        }

        @JsIgnore
        public Builder<T> customFormItem(String attribute, FormItemProvider provider) {
            includes.add(attribute);
            providers.put(attribute, provider);
            return this;
        }

        @JsIgnore
        public Builder<T> unboundFormItem(FormItem formItem) {
            return unboundFormItem(formItem, -1, null);
        }

        @JsIgnore
        public Builder<T> unboundFormItem(FormItem formItem, int position) {
            return unboundFormItem(formItem, position, null);
        }

        @JsIgnore
        public Builder<T> unboundFormItem(FormItem formItem, int position, SafeHtml helpText) {
            this.unboundFormItems.add(new UnboundFormItem(formItem, position, helpText));
            return this;
        }

        Builder<T> unboundFormItem(UnboundFormItem unboundFormItem) {
            this.unboundFormItems.add(unboundFormItem);
            return this;
        }

        @JsIgnore
        public Builder<T> onSave(SaveCallback<T> saveCallback) {
            this.saveCallback = saveCallback;
            return this;
        }

        @JsIgnore
        public Builder<T> onCancel(CancelCallback<T> cancelCallback) {
            this.cancelCallback = cancelCallback;
            return this;
        }

        @JsIgnore
        public Builder<T> prepareReset(PrepareReset<T> prepareReset) {
            this.prepareReset = prepareReset;
            return this;
        }

        @JsIgnore
        public Builder<T> prepareRemove(PrepareRemove<T> removeCallback) {
            this.prepareRemove = removeCallback;
            return this;
        }

        /**
         * By default the non-requried attributes are displayed together with the required attributes. Call this method
         * to put the non-required attributes on a collapsible panel beneath the required attributes.
         */
        @JsIgnore
        public Builder<T> panelForOptionalAttributes() {
            this.panelForOptionalAttributes = true;
            return this;
        }


        // ------------------------------------------------------ build

        /**
         * Creates and returns the form.
         */
        @EsReturn("Form")
        public ModelNodeForm<T> build() {
            validate();
            ModelNodeForm<T> form = new ModelNodeForm<>(this);
            form.separateOptionalFields(panelForOptionalAttributes);
            return form;
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

            if (!excludes.isEmpty() && !readOnly && verifyExcludes) {
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
                EnumSet<Operation> operations = EnumSet.allOf(Operation.class);
                if (prepareReset == null) {
                    operations.remove(Operation.RESET);
                }
                if (prepareRemove == null) {
                    operations.remove(Operation.REMOVE);
                }
                return new SingletonStateMachine(operations);
            } else {
                return new ExistingStateMachine(prepareReset != null);
            }
        }

        private String formId() {
            return "form(" + id + ")"; //NON-NLS
        }


        // ------------------------------------------------------ JS methods

        /** Includes the specified attributes */
        @JsMethod(name = "include")
        @EsReturn("FormBuilder")
        public Builder<T> jsInclude(@EsParam("string[]") String[] attributes) {
            return include(asList(attributes));
        }

        /** Excludes the specified attributes */
        @JsMethod(name = "exclude")
        @EsReturn("FormBuilder")
        public Builder<T> jsExclude(@EsParam("string[]") String[] attributes) {
            return exclude(asList(attributes));
        }

        /** Calls the specified callback when the save button was clicked and no validation errors occurred. */
        @JsMethod(name = "onSave")
        @EsReturn("FormBuilder")
        public Builder<T> jsOnSave(JsSaveCallback<T> callback) {
            this.saveCallback = (form, changedValues) -> callback.onSave(form, asJsMap(changedValues));
            return this;
        }

        @JsFunction
        interface JsSaveCallback<T> {

            void onSave(Form<T> form, JsPropertyMap<Object> changedValues);
        }
    }
}
