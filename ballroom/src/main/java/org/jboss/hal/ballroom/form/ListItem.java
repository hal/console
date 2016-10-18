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

import java.util.Collections;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.form.InputElement.Context;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;

import static org.jboss.hal.ballroom.form.InputElement.EMPTY_CONTEXT;
import static org.jboss.hal.resources.CSS.formControl;
import static org.jboss.hal.resources.CSS.tagManagerContainer;
import static org.jboss.hal.resources.CSS.tags;
import static org.jboss.hal.resources.Ids.uniqueId;

/**
 * @author Harald Pehl
 */
public class ListItem extends AbstractFormItem<List<String>> {

    private ListElement listElement;
    private Element tagsContainer;

    public ListItem(final String name, final String label) {
        super(name, label, null, EMPTY_CONTEXT);
    }

    @Override
    protected InputElement<List<String>> newInputElement(Context<?> context) {
        listElement = new ListElement();
        listElement.setClassName(formControl + " " + tags);
        TagsManager.Bridge.element(listElement.asElement()).onRefresh((event, cst) -> {
            List<String> value = Splitter.on(',')
                    .trimResults()
                    .omitEmptyStrings()
                    .splitToList(cst);
            setModified(true);
            setUndefined(value.isEmpty());
            signalChange(value);
        });
        return listElement;
    }

    @Override
    protected void assembleUI() {
        super.assembleUI();

        inputGroupContainer.getClassList().add(tags);

        errorText.setInnerHTML(MESSAGES.listHint().asString());
        errorText.getClassList().add(CSS.hint);
        Elements.setVisible(errorText, true);

        //noinspection DuplicateStringLiteralInspection
        tagsContainer = new Elements.Builder().div()
                .id(Ids.build("tags", "container", uniqueId()))
                .css(tagManagerContainer)
                .end()
                .build();
        inputContainer.insertBefore(tagsContainer, errorText);
    }


    @Override
    public void clearError() {
        super.clearError();
        errorText.setInnerHTML(MESSAGES.listHint().asString());
        errorText.getClassList().add(CSS.hint);
        Elements.setVisible(errorText, true);
    }

    @Override
    public void showError(final String message) {
        super.showError(message);
        errorText.getClassList().remove(CSS.hint);
    }

    @Override
    public void onSuggest(final String suggestion) {
        TagsManager.Bridge.element(listElement.asElement()).addTag(suggestion);
        setModified(true);
        setUndefined(false);
    }

    @Override
    public void attach() {
        super.attach();
        TagsManager.Options options = TagsManager.Defaults.get();
        options.tagsContainer = "#" + tagsContainer.getId();
        TagsManager.Bridge.element(listElement.asElement()).tagsManager(options);
    }

    @Override
    String asString(final List<String> value) {
        return Joiner.on(", ").join(value);
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return getValue().isEmpty() || isUndefined();
    }


    private static class ListElement extends InputElement<List<String>> {

        final elemental.html.InputElement element;

        ListElement() {
            element = Browser.getDocument().createInputElement();
            element.setType("text"); //NON-NLS
        }

        @Override
        public List<String> getValue() {
            return isAttached() ? TagsManager.Bridge.element(asElement()).getTags() : Collections.emptyList();
        }

        @Override
        public void setValue(final List<String> value) {
            if (isAttached()) {
                TagsManager.Bridge.element(asElement()).setTags(value);
            }
        }

        @Override
        public void clearValue() {
            if (isAttached()) {
                TagsManager.Bridge.element(asElement()).removeAll();
            }
        }

        @Override
        public int getTabIndex() {
            return element.getTabIndex();
        }

        @Override
        public void setAccessKey(final char c) {
            element.setAccessKey(String.valueOf(c));
        }

        @Override
        public void setFocus(final boolean b) {
            if (b) {
                element.focus();
            } else {
                element.blur();
            }
        }

        @Override
        public void setTabIndex(final int i) {
            element.setTabIndex(i);
        }

        @Override
        public boolean isEnabled() {
            return !element.isDisabled();
        }

        @Override
        public void setEnabled(final boolean b) {
            element.setDisabled(!b);
        }

        @Override
        public void setName(final String s) {
            element.setName(s);
        }

        @Override
        public String getName() {
            return element.getName();
        }

        @Override
        public String getText() {
            return Joiner.on(", ").join(getValue());
        }

        @Override
        public void setText(final String s) {
            // not supported
        }

        @Override
        public Element asElement() {
            return element;
        }
    }
}
