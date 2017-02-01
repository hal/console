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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.gwt.safehtml.shared.SafeHtml;
import elemental.dom.Element;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.FormItemProvider;
import org.jboss.hal.ballroom.form.FormValidation;
import org.jboss.hal.ballroom.form.StateMachine;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jetbrains.annotations.NonNls;

import static com.google.common.collect.Lists.asList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;

/**
 * A form which groups attributes on different tabs. Each group will include the attributes specified by the
 * {@linkplain Builder grouped form builder}.
 * <p>
 * Internally this class uses a separate form for each group / tab. All forms share the same save, cancel and reset
 * callbacks.
 *
 * @author Harald Pehl
 */
public class GroupedForm<T extends ModelNode> implements Form<T> {

    private static class Group {

        final String id;
        final String title;
        final LinkedHashSet<String> includes;
        final Set<String> excludes;
        final Map<String, FormItemProvider> providers;
        final List<UnboundFormItem> unboundFormItems;

        private Group(final String id, final String title) {
            this.id = id;
            this.title = title;
            this.includes = new LinkedHashSet<>();
            this.excludes = new HashSet<>();
            this.providers = new HashMap<>();
            this.unboundFormItems = new ArrayList<>();
        }
    }


    private enum Mode {ADD_ONLY, FROM_REQUEST_PROPERTIES, VIEW_ONLY}


    // ------------------------------------------------------ grouped form builder


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static class Builder<T extends ModelNode> {

        private final String id;
        private final Metadata metadata;
        private final List<Group> groups;
        private Group currentGroup;
        private Mode mode;
        private SaveCallback<T> saveCallback;
        private CancelCallback<T> cancelCallback;
        private ResetCallback<T> resetCallback;

        public Builder(final String id, final Metadata metadata) {
            this.id = id;
            this.metadata = metadata;
            this.groups = new ArrayList<>();
            this.mode = null;
        }

        /**
         * Starts a custom group with custom attributes. Use one of the {@code include()} and {@code exclude()} methods
         * to include and exclude attributes.
         */
        public Builder<T> customGroup(final String id, final String title) {
            assertNoCurrentGroup();
            currentGroup = new Group(id, title);
            return this;
        }

        /**
         * Starts an attribute group backed by a group definition from the resource description. All attributes defined
         * in the specified group are included in alphabetic order. The id and title of the group is derived from the
         * attribute group name.
         */
        public Builder<T> attributeGroup(final String name) {
            return attributeGroup(Ids.build(id, "group", name), name, new LabelBuilder().label(name));
        }

        public Builder<T> attributeGroup(final String name, final String title) {
            return attributeGroup(Ids.build(id, "group", name), name, title);
        }

        public Builder<T> attributeGroup(final String id, final String name, final String title) {
            assertNoCurrentGroup();
            currentGroup = new Group(id, title);
            List<Property> attributes = metadata.getDescription().getAttributes(ATTRIBUTES, name);
            include(attributes.stream().map(Property::getName).sorted().collect(toList()));
            return this;
        }

        public Builder<T> end() {
            assertCurrentGroup();
            groups.add(currentGroup);
            currentGroup = null;
            return this;
        }

        public Builder<T> include(final String[] attributes) {
            assertCurrentGroup();
            currentGroup.includes.addAll(Arrays.asList(attributes));
            return this;
        }

        public Builder<T> include(final Iterable<String> attributes) {
            assertCurrentGroup();
            Iterables.addAll(currentGroup.includes, attributes);
            return this;
        }

        public Builder<T> include(@NonNls final String first, @NonNls final String... rest) {
            assertCurrentGroup();
            currentGroup.includes.addAll(asList(first, rest));
            return this;
        }

        public Builder<T> exclude(final Iterable<String> attributes) {
            assertCurrentGroup();
            Iterables.addAll(currentGroup.excludes, attributes);
            return this;
        }

        public Builder<T> exclude(@NonNls final String first, @NonNls final String... rest) {
            assertCurrentGroup();
            currentGroup.excludes.addAll(asList(first, rest));
            return this;
        }

        public Builder<T> addOnly() {
            assertNoCurrentGroup();
            this.mode = Mode.ADD_ONLY;
            return this;
        }

        public Builder<T> addFromRequestProperties() {
            assertNoCurrentGroup();
            this.mode = Mode.FROM_REQUEST_PROPERTIES;
            return this;
        }

        public Builder<T> viewOnly() {
            assertNoCurrentGroup();
            this.mode = Mode.VIEW_ONLY;
            return this;
        }

        public Builder<T> customFormItem(@NonNls final String attribute, final FormItemProvider provider) {
            assertCurrentGroup();
            currentGroup.includes.add(attribute);
            currentGroup.providers.put(attribute, provider);
            return this;
        }

        public Builder<T> unboundFormItem(final FormItem formItem) {
            return unboundFormItem(formItem, -1, null);
        }

        public Builder<T> unboundFormItem(final FormItem formItem, final int position) {
            return unboundFormItem(formItem, position, null);
        }

        public Builder<T> unboundFormItem(final FormItem formItem, final int position, final SafeHtml helpText) {
            assertCurrentGroup();
            currentGroup.unboundFormItems.add(new UnboundFormItem(formItem, position, helpText));
            return this;
        }

        public Builder<T> onSave(final SaveCallback<T> saveCallback) {
            assertNoCurrentGroup();
            this.saveCallback = saveCallback;
            return this;
        }

        public Builder<T> onCancel(final CancelCallback<T> cancelCallback) {
            assertNoCurrentGroup();
            this.cancelCallback = cancelCallback;
            return this;
        }

        public Builder<T> onReset(final ResetCallback<T> resetCallback) {
            assertNoCurrentGroup();
            this.resetCallback = resetCallback;
            return this;
        }

        public GroupedForm<T> build() {
            assertNoCurrentGroup();
            if (groups.isEmpty()) {
                throw new IllegalStateException("No groups in " + formId());
            }
            return new GroupedForm<>(this);
        }

        private void assertCurrentGroup() {
            if (currentGroup == null) {
                throw new IllegalStateException("No current group in " + formId());
            }
        }

        private void assertNoCurrentGroup() {
            if (currentGroup != null) {
                throw new IllegalStateException("Open group in " + formId());
            }
        }

        private String formId() {
            return "grouped form(" + id + ")"; //NON-NLS
        }
    }


    // ------------------------------------------------------ grouped from setup

    private final String id;
    private final Tabs tabs;
    private final List<Form<T>> forms;
    private Form<T> currentForm;

    private GroupedForm(final Builder<T> builder) {
        this.id = builder.id;
        this.tabs = new Tabs();
        this.forms = new ArrayList<>();

        builder.groups.forEach(group -> {
            ModelNodeForm.Builder<T> fb = new ModelNodeForm.Builder<>(Ids.build(group.id, Ids.FORM_SUFFIX),
                    builder.metadata);
            if (!group.excludes.isEmpty()) {
                fb.exclude(group.excludes);
            }
            if (!group.includes.isEmpty()) {
                fb.include(group.includes);
                fb.unsorted();
            }
            group.providers.forEach(fb::customFormItem);
            group.unboundFormItems.forEach(fb::unboundFormItem);

            if (builder.mode != null) {
                switch (builder.mode) {
                    case ADD_ONLY:
                        fb.addOnly();
                        break;
                    case FROM_REQUEST_PROPERTIES:
                        fb.addFromRequestProperties();
                        break;
                    case VIEW_ONLY:
                        fb.viewOnly();
                        break;
                }
            }

            if (builder.saveCallback != null) {
                fb.onSave(builder.saveCallback);
            }
            if (builder.cancelCallback != null) {
                fb.onCancel(builder.cancelCallback);
            }
            if (builder.resetCallback != null) {
                fb.onReset(builder.resetCallback);
            }

            Form<T> form = fb.build();
            forms.add(form);

            String tabId = Ids.build(group.id, Ids.TAB_SUFFIX);
            tabs.add(tabId, group.title, form.asElement());
            tabs.onShow(tabId, () -> currentForm = form);
        });

        currentForm = forms.get(0);
    }


    // ------------------------------------------------------ element and attachable contract

    @Override
    public Element asElement() {
        return tabs.asElement();
    }

    @Override
    public void attach() {
        forms.forEach(Form::attach);
    }

    @Override
    public void detach() {
        forms.forEach(Form::detach);
    }


    // ------------------------------------------------------ form contract

    /**
     * Calls {@link Form#add(Object)} on all forms.
     */
    @Override
    public void add(final T model) {
        forms.forEach(form -> form.add(model));
    }

    /**
     * Calls {@link Form#view(Object)} on all forms.
     */
    @Override
    public void view(final T model) {
        forms.forEach(form -> form.view(model));
    }

    /**
     * Calls {@link Form#clear()} on all forms.
     */
    @Override
    public void clear() {
        forms.forEach(Form::clear);
    }

    /**
     * Calls {@link Form#reset()} on all forms.
     */
    @Override
    public void reset() {
        forms.forEach(Form::reset);
    }

    @Override
    public void setResetCallback(final ResetCallback<T> resetCallback) {
        forms.forEach(form -> form.setResetCallback(resetCallback));
    }

    /**
     * Calls {@link Form#edit(Object)} on the currently active form.
     */
    @Override
    public void edit(final T model) {
        currentForm.edit(model);
    }

    /**
     * Calls {@link Form#save()} on the currently active form.
     */
    @Override
    public boolean save() {
        return currentForm.save();
    }

    @Override
    public void setSaveCallback(final SaveCallback<T> saveCallback) {
        forms.forEach(form -> form.setSaveCallback(saveCallback));
    }

    /**
     * Calls {@link Form#cancel()} on the currently active form.
     */
    @Override
    public void cancel() {
        currentForm.cancel();
    }

    @Override
    public void setCancelCallback(final CancelCallback<T> cancelCallback) {
        forms.forEach(form -> form.setCancelCallback(cancelCallback));

    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public T getModel() {
        forms.get(0).getModel();
        return null;
    }

    @Override
    public StateMachine getStateMachine() {
        return forms.get(0).getStateMachine();
    }

    @Override
    public <F> FormItem<F> getFormItem(final String name) {
        for (Form<T> form : forms) {
            FormItem<F> formItem = form.getFormItem(name);
            if (formItem != null) {
                return formItem;
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Iterable<FormItem> getFormItems() {
        List<FormItem> formItems = new ArrayList<>();
        forms.forEach(form -> Iterables.addAll(formItems, form.getFormItems()));
        return formItems;
    }

    @Override
    public Iterable<FormItem> getBoundFormItems() {
        List<FormItem> formItems = new ArrayList<>();
        forms.forEach(form -> Iterables.addAll(formItems, form.getBoundFormItems()));
        return formItems;
    }

    @Override
    public void addFormValidation(final FormValidation<T> formValidation) {
        forms.get(0).addFormValidation(formValidation);
    }
}