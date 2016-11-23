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
package org.jboss.hal.ballroom.form;

import java.util.Iterator;
import java.util.List;

import com.google.common.base.Strings;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.HRElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.Ids;

import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.ballroom.form.Form.State.READONLY;
import static org.jboss.hal.resources.CSS.separator;

/**
 * A form item composed from a list of other form items. The composite form item uses the following semantics:
 * <dl>
 * <dt>Layout</dt>
 * <dd>The form items are placed into a {@code div} element. In the readonly mode they're separated by {@code hr}
 * elements.</dd>
 * <dt>Modified</dt>
 * <dd>The composite form item is modified as soon as one of the form items is modified.</dd>
 * <dt>Expressions</dt>
 * <dd>The composite form item does not support expressions.</dd>
 * <dt>Validation</dt>
 * <dd>The composite form item is valid if <strong>all</strong> form items are valid. Adding new validation handlers is
 * not supported.</dd>
 * <dt>Restricted</dt>
 * <dd>The composite form item is restricted if <strong>any of</strong> the form items is restricted.</dd>
 * <dt>Enabled</dt>
 * <dd>The composite form item is enabled if <strong>all</strong> form items are enabled.</dd>
 * <dt>Required</dt>
 * <dd>The composite form item is required if <strong>any of</strong> the form items is required.</dd>
 * </dl>
 *
 * @author Harald Pehl
 */
public abstract class CompositeFormItem extends AbstractFormItem<ModelNode> implements ModelNodeItem {

    private final String name;
    private Element editContainer;
    private Element readonlyContainer;
    private List<FormItem> formItems;

    protected <C> CompositeFormItem(final String name, CreationContext<C> context) {
        super(name, new LabelBuilder().label(name), null, context);
        this.name = name;
    }

    protected abstract List<FormItem> createFormItems();

    /**
     * Called during {@link #setValue(Object)} to set the form items using the provided model.
     */
    protected abstract void populateFormItems(ModelNode modelNode);

    /**
     * Called during {@link #getValue()} to persist the form items into the provided model.
     */
    protected abstract void persistModel(ModelNode modelNode);

    @Override
    protected <C> void assembleUI(CreationContext<C> context) {
        editContainer = Browser.getDocument().createDivElement();
        readonlyContainer = Browser.getDocument().createDivElement();

        formItems = createFormItems();
        for (Iterator<FormItem> iterator = formItems.iterator(); iterator.hasNext(); ) {
            FormItem formItem = iterator.next();
            editContainer.appendChild(formItem.asElement(EDITING));
            readonlyContainer.appendChild(formItem.asElement(READONLY));
            if (iterator.hasNext()) {
                HRElement hr = Browser.getDocument().createHRElement();
                hr.getClassList().add(separator);
                readonlyContainer.appendChild(hr);
            }
        }
    }

    @Override
    protected InputElement<ModelNode> newInputElement(final CreationContext<?> context) {
        return new NoopInputElement();
    }

    @Override
    public Element asElement(final Form.State state) {
        if (state == EDITING) {
            return editContainer;
        } else if (state == READONLY) {
            return readonlyContainer;
        } else {
            throw new IllegalStateException("Unknown state in CompositeFormItem.asElement(" + state + ")");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void attach() {
        for (FormItem formItem : formItems) {
            formItem.attach();
            formItem.addValueChangeHandler(new FormItemChangeHandler(formItem));
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<ModelNode> valueChangeHandler) {
        return null; // not supported
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ModelNode getValue() {
        ModelNode value = new ModelNode();
        persistModel(value);
        return value;
    }

    @Override
    public void setValue(final ModelNode value, final boolean fireEvent) {
        populateFormItems(value);
    }

    @Override
    public void clearValue() {
        formItems.forEach(FormItem::clearValue);
    }

    @Override
    public String getId(final Form.State state) {
        if (state == EDITING) {
            return editContainer.getId();
        } else if (state == READONLY) {
            return readonlyContainer.getId();
        }
        return null;
    }

    @Override
    public void setId(final String id) {
        String editId = Ids.build(id, EDITING.name().toLowerCase());
        String readonlyId = Ids.build(id, READONLY.name().toLowerCase());
        editContainer.setId(editId);
        readonlyContainer.setId(readonlyId);
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }

    @Override
    public void addValidationHandler(final FormItemValidation<ModelNode> validationHandler) {
        // not supported
    }

    @Override
    public boolean validate() {
        boolean valid = true;
        for (FormItem formItem : formItems) {
            valid = valid && formItem.validate();
        }
        return valid;
    }

    @Override
    public void registerSuggestHandler(final SuggestHandler suggestHandler) {
        // not supported
    }

    @Override
    public boolean isRestricted() {
        for (FormItem formItem : formItems) {
            if (formItem.isRestricted()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setRestricted(final boolean restricted) {
        formItems.forEach(formItem -> formItem.setRestricted(restricted));
    }

    @Override
    public boolean isEnabled() {
        for (FormItem formItem : formItems) {
            if (!formItem.isEnabled()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        formItems.forEach(formItem -> formItem.setEnabled(enabled));
    }

    @Override
    public int getTabIndex() {
        return formItems.isEmpty() ? 0 : formItems.get(0).getTabIndex();
    }

    @Override
    public void setTabIndex(final int index) {
        int i = index;
        for (FormItem formItem : formItems) {
            formItem.setTabIndex(i);
            i++;
        }
    }

    @Override
    public void setFocus(final boolean focus) {
        if (!formItems.isEmpty()) {
            formItems.get(0).setFocus(focus);
        }
    }

    @Override
    public boolean isRequired() {
        for (FormItem formItem : formItems) {
            if (formItem.isRequired()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setRequired(final boolean required) {
        // not supported;
    }

    @Override
    public void setUndefined(final boolean undefined) {
        formItems.forEach(formItem -> formItem.setUndefined(undefined));
    }


    private class FormItemChangeHandler implements ValueChangeHandler {

        private final FormItem formItem;

        private FormItemChangeHandler(final FormItem formItem) {this.formItem = formItem;}

        @Override
        public void onValueChange(final ValueChangeEvent event) {
            formItem.setModified(true);
            formItem.setUndefined(Strings.isNullOrEmpty(String.valueOf(event.getValue())));
            setModified(true);
        }
    }

    private static class NoopInputElement extends InputElement<ModelNode> {

        private final Element element;

        private NoopInputElement() {
            element = Browser.getDocument().createDivElement();
            element.setTextContent("Noop element for CompositeFormItem"); //NON-NLS
            Elements.setVisible(element, false);
        }

        @Override
        public ModelNode getValue() {
            return new ModelNode();
        }

        @Override
        public void setValue(final ModelNode value) {}

        @Override
        public void clearValue() {}

        @Override
        public int getTabIndex() {
            return 0;
        }

        @Override
        public void setAccessKey(final char key) {}

        @Override
        public void setFocus(final boolean focused) {}

        @Override
        public void setTabIndex(final int index) {}

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public void setEnabled(final boolean enabled) {}

        @Override
        public void setName(final String name) {}

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getText() {
            return null;
        }

        @Override
        public void setText(final String text) {}

        @Override
        public Element asElement() {
            return element;
        }
    }
}
