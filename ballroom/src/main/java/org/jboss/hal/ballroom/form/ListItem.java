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
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.input;
import static org.jboss.gwt.elemento.core.InputType.text;
import static org.jboss.hal.ballroom.form.Decoration.*;
import static org.jboss.hal.resources.CSS.*;
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

        private final HTMLElement tagsContainer;

        ListEditingAppearance(HTMLInputElement inputElement) {
            super(EnumSet.of(DEFAULT, DEPRECATED, ENABLED, INVALID, REQUIRED, RESTRICTED, SUGGESTIONS), inputElement);

            tagsContainer = div().css(tagManagerContainer)
                    .id(Ids.build("tags", "container", uniqueId()))
                    .asElement();

            helpBlock.classList.add(CSS.hint);
            helpBlock.innerHTML = MESSAGES.listHint().asString();

            inputContainer.appendChild(tagsContainer);
            inputContainer.appendChild(helpBlock);
            inputGroup.classList.add(tags);
        }

        @Override
        protected String name() {
            return "ListEditingAppearance";
        }

        @Override
        public void attach() {
            super.attach();
            TagsManager.Options options = TagsManager.Defaults.get();
            options.tagsContainer = "#" + tagsContainer.id;

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
                inputElement.value = asString(value);
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
                inputElement.value = "";
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
            inputContainer.classList.remove(disabled);
        }

        @Override
        void unapplyEnabled() {
            super.unapplyEnabled();
            inputContainer.classList.add(disabled);
        }

        @Override
        void applyInvalid(final String errorMessage) {
            root.classList.add(hasError);
            helpBlock.classList.remove(CSS.hint);
            helpBlock.textContent = errorMessage;
        }

        @Override
        void unapplyInvalid() {
            root.classList.remove(hasError);
            helpBlock.classList.add(CSS.hint);
            helpBlock.innerHTML = MESSAGES.listHint().asString();
        }
    }


    private final ListEditingAppearance editingAppearance;

    public ListItem(final String name, final String label) {
        super(name, label, null);

        // read-only appearance
        addAppearance(Form.State.READONLY, new ListReadOnlyAppearance());

        // editing appearance
        editingAppearance = new ListEditingAppearance(input(text).css(formControl, tags).asElement());
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
