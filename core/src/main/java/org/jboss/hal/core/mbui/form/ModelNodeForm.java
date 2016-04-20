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

import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.HelpTextBuilder;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.*;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Messages;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.*;

/**
 * TODO Add form based validations based on "alternatives" => ["foo"] information from the resource description
 * @author Harald Pehl
 */
public class ModelNodeForm<T extends ModelNode> extends DefaultForm<T> {

    private static class UnboundFormItem {

        final FormItem formItem;
        final int position;

        private UnboundFormItem(final FormItem formItem, final int position) {
            this.formItem = formItem;
            this.position = position;
        }
    }

    /**
     * 
     * <p>Builde useful to automatically inspects the read-resource-description and associated the 
     * attributes (by calling: include, customFormitem) and creates the required form itens and help text.</p>
     * <p>This will not work if the resource description is for attribute of type LIST. For this try, to use the ModelNodeFormAttributeList</p> 
     * 
     * @param <T>
     */
    public static class Builder<T extends ModelNode> {

        private static final String ILLEGAL_COMBINATION = "Illegal combination in ";

        final String id;
        private Metadata metadata;
        final Set<String> includes;
        final Set<String> excludes;
        final Map<String, FormItemProvider> providers;
        final List<UnboundFormItem> unboundFormItems;
        boolean createResource;
        boolean viewOnly;
        boolean addOnly;
        boolean unsorted;
        boolean requiredOnly;
        boolean includeRuntime;
        boolean attributeTypeList;
        SaveCallback<T> saveCallback;
        CancelCallback<T> cancelCallback;
        ResetCallback<T> resetCallback;
        DataMapping<T> dataMapping;


        // ------------------------------------------------------ configure required and optional settings

        public Builder(@NonNls final String id, final Metadata metadata) {
            this.id = id;
            this.metadata = metadata;
            this.includes = new HashSet<>();
            this.excludes = new HashSet<>();
            this.providers = new HashMap<>();
            this.unboundFormItems = new ArrayList<>();
            this.createResource = false;
            this.viewOnly = false;
            this.addOnly = false;
            this.unsorted = false;
            this.includeRuntime = false;
            this.dataMapping = new ModelNodeMapping<>(metadata.getDescription());
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

        public Builder<T> exclude(@NonNls final String first, @NonNls final String... rest) {
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

        public Builder<T> addOnly() {
            this.addOnly = true;
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

        public Builder<T> customFormItem(@NonNls final String attribute, final FormItemProvider provider) {
            providers.put(attribute, provider);
            return this;
        }

        public Builder<T> unboundFormItem(final FormItem formItem) {
            return unboundFormItem(formItem, -1);
        }

        public Builder<T> unboundFormItem(final FormItem formItem, final int position) {
            this.unboundFormItems.add(new UnboundFormItem(formItem, position));
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

        public Builder<T> dataMapping(DataMapping<T> dataMapping) {
            this.dataMapping = dataMapping;
            return this;
        }

        /**
         * Makes the automatic bindings to work with a single attribute of type LIST, each
         * item list, comprising of a set of attributes (but these attributes as not regular).
         * @return this Builder itself.
         */
        public Builder<T> attributeTypeList() {
            this.attributeTypeList = true;
            return this;
        }


        // ------------------------------------------------------ build

        public ModelNodeForm<T> build() {
            validate();
            return new ModelNodeForm<>(this);
        }

        void validate() {
            if (viewOnly && addOnly) {
                throw new IllegalStateException(ILLEGAL_COMBINATION + formId() + ": viewOnly && addOnly");
            }
            if (createResource) {
                if (viewOnly) {
                    throw new IllegalStateException(
                            ILLEGAL_COMBINATION + formId() + ": createResource && viewOnly");
                }
                if (!attributeTypeList) {
                    String path = OPERATIONS + "." + ADD + "." + REQUEST_PROPERTIES;
                    if (!ModelNodeHelper.failSafeGet(metadata.getDescription(), path).isDefined()) {
                        throw new IllegalStateException("No request properties found for " + formId() +
                                " / operation add in resource description " + metadata.getDescription());
                    }
                    if (!excludes.isEmpty()) {
                        List<Property> requiredRequestProperties = metadata.getDescription().getRequiredRequestProperties();
                        for (Property property : requiredRequestProperties) {
                            if (excludes.contains(property.getName())) {
                                throw new IllegalStateException("Required request property " + property.getName() +
                                        " must not be excluded from " + formId() + " when using createMode == true");
                            }
                        }
                    }
                }
            } else {
                if (!metadata.getDescription().hasDefined(ATTRIBUTES)) {
                    throw new IllegalStateException("No attributes found for " + formId() +
                            " in resource description " + metadata.getDescription());
                }
            }
        }

        StateMachine stateMachine() {
            return createResource || addOnly ?
                    new AddOnlyStateMachine() :
                    (viewOnly ? new ViewOnlyStateMachine() : new ExistingModelStateMachine());
        }

        private String formId() {
            return "form(" + id + ")"; //NON-NLS
        }
    }


    private static final Messages MESSAGES = GWT.create(Messages.class);
    private static final Logger logger = LoggerFactory.getLogger(ModelNodeForm.class);

    private final FormItemProvider formItemProvider;

    private ModelNodeForm(final Builder<T> builder) {
        super(builder.id, builder.stateMachine(), builder.dataMapping, builder.metadata.getSecurityContext());

        this.formItemProvider = new DefaultFormItemProvider(builder.metadata.getCapabilities());
        this.saveCallback = builder.saveCallback;
        this.cancelCallback = builder.cancelCallback;
        this.resetCallback = builder.resetCallback;

        Iterable<Property> formProperties = Collections.emptyList();
        if (builder.attributeTypeList) {
            formProperties = builder.metadata.getDescription().asPropertyList();
        } else {
            String path = builder.createResource ? Joiner.on('.').join(OPERATIONS, ADD, REQUEST_PROPERTIES) : ATTRIBUTES;
            formProperties = ModelNodeHelper.failSafeGet(builder.metadata.getDescription(), path).asPropertyList();
            //noinspection Guava
        }
        FluentIterable<Property> fi = FluentIterable.from(formProperties).filter(new PropertyFilter(builder));
        formProperties = builder.unsorted ? fi.toList() : fi.toSortedList((p1, p2) -> p1.getName().compareTo(p2.getName()));
        int index = 0;
        LabelBuilder labelBuilder = new LabelBuilder();
        HelpTextBuilder helpTextBuilder = new HelpTextBuilder();
        for (Property property : formProperties) {

            // any unbound form items for the current index?
            for (Iterator<UnboundFormItem> iterator = builder.unboundFormItems.iterator(); iterator.hasNext(); ) {
                UnboundFormItem unboundFormItem = iterator.next();
                if (unboundFormItem.position == index) {
                    addFormItem(unboundFormItem.formItem);
                    markAsUnbound(unboundFormItem.formItem.getName());
                    iterator.remove();
                }
            }

            String name = property.getName();
            ModelNode attribute = property.getValue();

            FormItem formItem;
            if (builder.providers.containsKey(name)) {
                formItem = builder.providers.get(name).createFrom(property);
            } else {
                formItem = formItemProvider.createFrom(property);
            }
            if (formItem != null) {
                addFormItem(formItem);
                if (attribute.hasDefined(DESCRIPTION)) {
                    SafeHtml helpText = helpTextBuilder.helpText(property);
                    addHelp(labelBuilder.label(property), helpText);
                }
                index++;
            } else {
                logger.warn("Unable to create form item for '{}' in form '{}'", name, builder.id); //NON-NLS
            }
        }

        // add remaining unbound form items
        for (UnboundFormItem unboundFormItem : builder.unboundFormItems) {
            addFormItem(unboundFormItem.formItem);
            markAsUnbound(unboundFormItem.formItem.getName());
        }
    }

    @Override
    public void attach() {
        super.attach();
        if (Iterables.isEmpty(getFormItems())) {
            // if there's really nothing at all show an info
            Element empty = new Elements.Builder()
                    .div().css(alert, alertInfo)
                    .span().css(pfIcon("info")).end()
                    .span().innerHtml(MESSAGES.emptyModelNodeForm()).end()
                    .end()
                    .build();
            Elements.removeChildrenFrom(asElement());
            asElement().appendChild(empty);
        }
    }
}
