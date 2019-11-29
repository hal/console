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
import java.util.Set;

import com.google.gwt.safehtml.shared.SafeHtml;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.hal.ballroom.form.TagsManager.Defaults;
import org.jboss.hal.ballroom.form.TagsManager.Options;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.input;
import static org.jboss.gwt.elemento.core.InputType.text;
import static org.jboss.hal.ballroom.form.Decoration.DEFAULT;
import static org.jboss.hal.ballroom.form.Decoration.DEPRECATED;
import static org.jboss.hal.ballroom.form.Decoration.RESTRICTED;
import static org.jboss.hal.ballroom.form.Decoration.SUGGESTIONS;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Ids.uniqueId;
import static org.jboss.hal.resources.UIConstants.HASH;

public abstract class TagsItem<T> extends AbstractFormItem<T> {

    private final Set<Decoration> editingDecorations;
    private final TagsMapping<T> mapping;
    private final TagsEditingAppearance editingAppearance;

    protected TagsItem(String name, String label, SafeHtml inputHelp,
            Set<Decoration> editingDecorations, TagsMapping<T> mapping) {
        super(name, label, null);

        this.editingDecorations = editingDecorations;
        this.mapping = mapping;

        // read-only appearance
        addAppearance(Form.State.READONLY, new TagsReadOnlyAppearance());

        // editing appearance
        editingAppearance = new TagsEditingAppearance(input(text).css(formControl, tags).asElement(), inputHelp,
                editingDecorations, mapping);
        addAppearance(Form.State.EDITING, editingAppearance);
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }

    public String allowedCharacters() {
        return null;
    }

    @Override
    public void onSuggest(String suggestion) {
        if (editingDecorations.contains(SUGGESTIONS)) {
            editingAppearance.onSuggest(suggestion);
        }
    }

    public abstract void addTag(T tag);
    public abstract void removeTag(T tag);

    private class TagsReadOnlyAppearance extends ReadOnlyAppearance<T> {

        TagsReadOnlyAppearance() {
            super(EnumSet.of(DEFAULT, DEPRECATED, RESTRICTED));
        }

        @Override
        protected String name() {
            return "TagsReadOnlyAppearance";
        }

        @Override
        public String asString(T value) {
            return mapping.asString(value);
        }
    }


    private class TagsEditingAppearance extends EditingAppearance<T> {

        private final HTMLElement tagsContainer;
        private final TagsMapping<T> mapping;

        private boolean skipAdding;

        TagsEditingAppearance(HTMLInputElement inputElement, SafeHtml inputHelp,
                Set<Decoration> supportedDecorations, TagsMapping<T> mapping) {
            super(supportedDecorations, inputElement);
            this.mapping = mapping;

            tagsContainer = div().css(tagManagerContainer)
                    .id(Ids.build("tags", "container", uniqueId()))
                    .asElement();

            helpBlock.classList.add(CSS.hint);
            helpBlock.innerHTML = inputHelp.asString();

            inputContainer.appendChild(tagsContainer);
            inputContainer.appendChild(helpBlock);
            inputGroup.classList.add(properties);

            Options options = Defaults.get();
            options.tagsContainer = HASH + tagsContainer.id;
            options.validator = mapping.validator();

            TagsManager.Api api = TagsManager.Api.element(inputElement);
            api.tagsManager(options);
            api.onInvalid((event, cst) -> {
                String message = allowedCharacters() != null ? MESSAGES.invalidTagFormat(allowedCharacters())
                        :  MESSAGES.invalidFormat();
                showError(message);
            });

            api.onAdded((event, tag) -> {
                if (skipAdding) {
                    return;
                }
                addTag(mapping.parseTag(tag));
                clearError();
            });

            api.onRemoved((event, tag) -> {
                removeTag(mapping.parseTag(tag));
                clearError();
            });
        }

        @Override
        protected String name() {
            return "TagsEditingAppearance";
        }

        @Override
        public void showValue(T value) {
            if (attached) {
                skipAdding = true;
                TagsManager.Api.element(inputElement).setTags(mapping.tags(value));
                skipAdding = false;
            } else {
                inputElement.value = asString(value);
            }
        }

        @Override
        public String asString(T value) {
            return mapping.asString(value);
        }

        @Override
        public void clearValue() {
            if (attached) {
                TagsManager.Api.element(inputElement).removeAll();
            } else {
                inputElement.value = "";
            }
        }

        void onSuggest(String suggestion) {
            if (attached) {
                TagsManager.Api.element(inputElement).addTag(suggestion);
            }
            setModified(true);
            setUndefined(false);
        }

        @Override
        void applyInvalid(String errorMessage) {
            root.classList.add(hasError);
            helpBlock.classList.remove(CSS.hint);
            helpBlock.textContent = errorMessage;
        }

        @Override
        void unapplyInvalid() {
            root.classList.remove(hasError);
            helpBlock.classList.add(CSS.hint);
            helpBlock.innerHTML = MESSAGES.propertiesHint().asString();
        }
    }
}
