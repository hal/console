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
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Messages;

import static org.jboss.hal.ballroom.form.Decoration.*;
import static org.jboss.hal.resources.CSS.disabled;
import static org.jboss.hal.resources.CSS.formControl;
import static org.jboss.hal.resources.CSS.hasError;
import static org.jboss.hal.resources.CSS.properties;
import static org.jboss.hal.resources.CSS.tagManagerContainer;
import static org.jboss.hal.resources.Ids.uniqueId;

/**
 * @author Harald Pehl
 */
public class PropertiesItem extends AbstractFormItem<Map<String, String>> {

    private static class PropertiesReadOnlyAppearance extends ReadOnlyAppearance<Map<String, String>> {

        PropertiesReadOnlyAppearance() {
            super(EnumSet.of(DEFAULT, DEPRECATED, RESTRICTED));
        }

        @Override
        protected String name() {
            return "PropertiesReadOnlyAppearance";
        }

        @Override
        public String asString(final Map<String, String> value) {
            return Joiner.on(", ").withKeyValueSeparator(" \u21D2 ").join(value);
        }
    }


    private class PropertiesEditingAppearance extends EditingAppearance<Map<String, String>> {

        private final Element tagsContainer;

        PropertiesEditingAppearance(elemental.html.InputElement inputElement, SafeHtml inputHelp) {
            super(EnumSet.of(DEFAULT, DEPRECATED, ENABLED, INVALID, REQUIRED, RESTRICTED), inputElement);

            // @formatter:off
            tagsContainer = new Elements.Builder()
                .div()
                    .id(Ids.build("tags", "container", uniqueId())).css(tagManagerContainer)
                .end()
            .build();
            // @formatter:on

            helpBlock.getClassList().add(CSS.hint);
            helpBlock.setInnerHTML(inputHelp.asString());

            inputContainer.appendChild(tagsContainer);
            inputContainer.appendChild(helpBlock);
            inputGroup.getClassList().add(properties);
        }

        @Override
        protected String name() {
            return "PropertiesEditingAppearance";
        }

        @Override
        public void attach() {
            super.attach();
            TagsManager.Options options = TagsManager.Defaults.get();
            options.tagsContainer = "#" + tagsContainer.getId();
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
                inputElement.setValue(asString(value));
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
                inputElement.setValue("");
            }
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
            helpBlock.setInnerHTML(MESSAGES.propertiesHint().asString());
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
        super(name, label, null);

        // read-only appearance
        addAppearance(Form.State.READONLY, new PropertiesReadOnlyAppearance());

        // editing appearance
        elemental.html.InputElement inputElement = Browser.getDocument().createInputElement();
        inputElement.setType("text"); //NON-NLS
        inputElement.getClassList().add(formControl);
        inputElement.getClassList().add(properties);

        addAppearance(Form.State.EDITING, new PropertiesEditingAppearance(inputElement, inputHelp));
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
