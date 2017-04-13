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

import java.util.EnumSet;
import java.util.List;

import com.google.common.base.Splitter;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.InputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;

import static org.jboss.hal.ballroom.form.Decoration.*;
import static org.jboss.hal.resources.CSS.disabled;
import static org.jboss.hal.resources.CSS.formControl;
import static org.jboss.hal.resources.CSS.hasError;
import static org.jboss.hal.resources.CSS.tagManagerContainer;
import static org.jboss.hal.resources.CSS.tags;
import static org.jboss.hal.resources.Ids.uniqueId;

/**
 * @author Harald Pehl
 */
public class ListItem extends AbstractFormItem<List<String>> {

    private static class ListReadOnlyAppearance extends ReadOnlyAppearance<List<String>> {

        ListReadOnlyAppearance() {
            super(EnumSet.of(DEFAULT, DEPRECATED, RESTRICTED));
        }

        @Override
        protected String name() {
            return "ListReadOnlyAppearance";
        }

        @Override
        public String asString(final List<String> value) {
            return String.join(", ", value);
        }
    }


    private class ListEditingAppearance extends EditingAppearance<List<String>> {

        private final Element tagsContainer;

        ListEditingAppearance(InputElement inputElement) {
            super(EnumSet.of(DEFAULT, DEPRECATED, ENABLED, INVALID, REQUIRED, RESTRICTED, SUGGESTIONS), inputElement);

            // @formatter:off
            tagsContainer = new Elements.Builder()
                .div()
                    .id(Ids.build("tags", "container", uniqueId())).css(tagManagerContainer)
                .end()
            .build();
            // @formatter:on

            helpBlock.getClassList().add(CSS.hint);
            helpBlock.setInnerHTML(MESSAGES.listHint().asString());

            inputContainer.appendChild(tagsContainer);
            inputContainer.appendChild(helpBlock);
            inputGroup.getClassList().add(tags);
        }

        @Override
        protected String name() {
            return "ListEditingAppearance";
        }

        @Override
        public void attach() {
            super.attach();
            TagsManager.Options options = TagsManager.Defaults.get();
            options.tagsContainer = "#" + tagsContainer.getId();

            TagsManager.Bridge bridge = TagsManager.Bridge.element(inputElement);
            bridge.tagsManager(options);
            bridge.onRefresh((event, cst) -> {
                List<String> value = Splitter.on(',')
                        .trimResults()
                        .omitEmptyStrings()
                        .splitToList(cst);
                modifyValue(value);
            });
        }

        @Override
        public void showValue(final List<String> value) {
            if (attached) {
                TagsManager.Bridge.element(inputElement).setTags(value);
            } else {
                inputElement.setValue(asString(value));
            }
        }

        @Override
        public String asString(final List<String> value) {
            return String.join(", ", value);
        }

        @Override
        public void clearValue() {
            if (attached) {
                TagsManager.Bridge.element(inputElement).removeAll();
            } else {
                inputElement.setValue("");
            }
        }

        void onSuggest(final String suggestion) {
            if (attached) {
                TagsManager.Bridge.element(inputElement).addTag(suggestion);
            }
            setModified(true);
            setUndefined(false);
        }

        @Override
        void applyEnabled() {
            super.applyEnabled();
            inputContainer.getClassList().remove(disabled);
        }

        @Override
        void unapplyEnabled() {
            super.unapplyEnabled();
            inputContainer.getClassList().add(disabled);
        }

        @Override
        void applyInvalid(final String errorMessage) {
            root.getClassList().add(hasError);
            helpBlock.getClassList().remove(CSS.hint);
            helpBlock.setTextContent(errorMessage);
        }

        @Override
        void unapplyInvalid() {
            root.getClassList().remove(hasError);
            helpBlock.getClassList().add(CSS.hint);
            helpBlock.setInnerHTML(MESSAGES.listHint().asString());
        }
    }


    private final ListEditingAppearance editingAppearance;

    public ListItem(final String name, final String label) {
        super(name, label, null);

        // read-only appearance
        addAppearance(Form.State.READONLY, new ListReadOnlyAppearance());

        // editing appearance
        InputElement inputElement = Browser.getDocument().createInputElement();
        inputElement.setType("text"); //NON-NLS
        inputElement.getClassList().add(formControl);
        inputElement.getClassList().add(tags);

        editingAppearance = new ListEditingAppearance(inputElement);
        addAppearance(Form.State.EDITING, editingAppearance);
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return getValue() == null || getValue().isEmpty();
    }

    @Override
    public void onSuggest(final String suggestion) {
        editingAppearance.onSuggest(suggestion);
    }
}
