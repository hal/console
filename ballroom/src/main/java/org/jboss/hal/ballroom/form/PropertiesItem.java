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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtml;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Messages;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.input;
import static org.jboss.gwt.elemento.core.InputType.text;
import static org.jboss.hal.ballroom.form.Decoration.*;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Ids.uniqueId;

/**
 * @author Harald Pehl
 */
public class PropertiesItem extends AbstractFormItem<Map<String, String>> {

    private static class PropertiesReadOnlyAppearance extends ReadOnlyAppearance<Map<String, String>> {

        String viewSeparator;

        PropertiesReadOnlyAppearance(String viewSeparator) {
            super(EnumSet.of(DEFAULT, DEPRECATED, RESTRICTED));
            this.viewSeparator = viewSeparator;
        }

        @Override
        protected String name() {
            return "PropertiesReadOnlyAppearance";
        }

        @Override
        public String asString(final Map<String, String> value) {
            return Joiner.on(viewSeparator).withKeyValueSeparator(" \u21D2 ").join(value);
        }
    }


    private class PropertiesEditingAppearance extends EditingAppearance<Map<String, String>> {

        private final HTMLElement tagsContainer;

        PropertiesEditingAppearance(HTMLInputElement inputElement, SafeHtml inputHelp) {
            super(EnumSet.of(DEFAULT, DEPRECATED, ENABLED, INVALID, REQUIRED, RESTRICTED), inputElement);

            tagsContainer = div().css(tagManagerContainer)
                    .id(Ids.build("tags", "container", uniqueId()))
                    .asElement();

            helpBlock.classList.add(CSS.hint);
            helpBlock.innerHTML = inputHelp.asString();

            inputContainer.appendChild(tagsContainer);
            inputContainer.appendChild(helpBlock);
            inputGroup.classList.add(properties);
        }

        @Override
        protected String name() {
            return "PropertiesEditingAppearance";
        }

        @Override
        public void attach() {
            super.attach();
            TagsManager.Options options = TagsManager.Defaults.get();
            options.tagsContainer = "#" + tagsContainer.id;
            options.validator = PROPERTY_REGEX::test;

            TagsManager.Bridge bridge = TagsManager.Bridge.element(inputElement);
            bridge.tagsManager(options);
            bridge.onRefresh((event, cst) -> {
                Map<String, String> value = Splitter.on(',')
                        .trimResults()
                        .omitEmptyStrings()
                        .withKeyValueSeparator('=')
                        .split(cst);
                modifyValue(value);
            });
        }

        @Override
        public void showValue(final Map<String, String> value) {
            if (attached) {
                TagsManager.Bridge.element(inputElement).setTags(asTags(value));
            } else {
                inputElement.value = asString(value);
            }
        }

        private List<String> asTags(final Map<String, String> properties) {
            if (properties.isEmpty()) {
                return Collections.emptyList();
            }
            List<String> tags = new ArrayList<>();
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                tags.add(entry.getKey() + "=" + entry.getValue());
            }
            return tags;
        }

        @Override
        public String asString(final Map<String, String> value) {
            return Joiner.on(", ").withKeyValueSeparator(" \u21D2 ").join(value);
        }

        @Override
        public void clearValue() {
            if (attached) {
                TagsManager.Bridge.element(inputElement).removeAll();
            } else {
                inputElement.value = "";
            }
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
            helpBlock.innerHTML = MESSAGES.propertiesHint().asString();
        }
    }


    private static final Messages MESSAGES = GWT.create(Messages.class);
    private static final RegExp PROPERTY_REGEX = RegExp.compile("^([\\w\\-\\.\\/]+)=([\\w\\-\\.\\/]+)$"); //NON-NLS

    public PropertiesItem(final String name) {
        this(name, new LabelBuilder().label(name), MESSAGES.propertiesHint());
    }

    public PropertiesItem(final String name, final String label) {
        this(name, label, MESSAGES.propertiesHint());
    }

    public PropertiesItem(final String name, final String label, final SafeHtml inputHelp) {
        this(name, label, inputHelp, ", ");
    }

    public PropertiesItem(final String name, final String label, final SafeHtml inputHelp, String viewSeparator) {
        super(name, label, null);

        // read-only appearance
        addAppearance(Form.State.READONLY, new PropertiesReadOnlyAppearance(viewSeparator));

        // editing appearance
        addAppearance(Form.State.EDITING, new PropertiesEditingAppearance(
                input(text).css(formControl, properties).asElement(), inputHelp));
    }

    @Override
    public boolean isEmpty() {
        return getValue() == null || getValue().isEmpty();
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }
}
