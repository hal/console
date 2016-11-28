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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.form.TagsManager.Bridge;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;

import static org.jboss.hal.ballroom.form.CreationContext.EMPTY_CONTEXT;
import static org.jboss.hal.resources.CSS.formControl;
import static org.jboss.hal.resources.CSS.properties;
import static org.jboss.hal.resources.CSS.tagManagerContainer;
import static org.jboss.hal.resources.Ids.uniqueId;

/**
 * @author Harald Pehl
 */
public class PropertiesItem extends AbstractFormItem<Map<String, String>> {

    private final static RegExp PROPERTY_REGEX = RegExp.compile("^([\\w\\d\\-_]+)=([\\w\\d\\-_]+)$"); //NON-NLS

    private PropertiesElement propertiesElement;
    private Element tagsContainer;

    public PropertiesItem(final String name, final String label) {
        super(name, label, null, EMPTY_CONTEXT);
    }

    @Override
    protected InputElement<Map<String, String>> newInputElement(CreationContext<?> context) {
        propertiesElement = new PropertiesElement();
        propertiesElement.setClassName(formControl + " " + properties);
        return propertiesElement;
    }

    @Override
    protected <C> void assembleUI(CreationContext<C> context) {
        super.assembleUI(context);

        valueElement.getClassList().add(properties);

        errorText.setInnerHTML(MESSAGES.propertiesHint().asString());
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
        errorText.setInnerHTML(MESSAGES.propertiesHint().asString());
        errorText.getClassList().add(CSS.hint);
        Elements.setVisible(errorText, true);
    }

    @Override
    public void showError(final String message) {
        super.showError(message);
        errorText.getClassList().remove(CSS.hint);
    }

    @Override
    public void attach() {
        super.attach();
        TagsManager.Options options = TagsManager.Defaults.get();
        options.tagsContainer = "#" + tagsContainer.getId();
        options.validator = PROPERTY_REGEX::test;

        Bridge bridge = Bridge.element(propertiesElement.asElement());
        bridge.tagsManager(options);
        bridge.onRefresh((event, cst) -> {
            Map<String, String> value = Splitter.on(',')
                    .trimResults()
                    .omitEmptyStrings()
                    .withKeyValueSeparator('=')
                    .split(cst);
            setModified(true);
            setUndefined(value.isEmpty());
            signalChange(value);
        });
    }

    @Override
    protected void setReadonlyValue(final Map<String, String> value) {
        Elements.removeChildrenFrom(valueElement);
        if (value != null && !value.isEmpty()) {
            for (Element element : keyValueElements(value)) {
                valueElement.appendChild(element);
            }
        }
    }

    @Override
    void markDefaultValue(final boolean on, final Map<String, String> defaultValue) {
        if (on) {
            Elements.removeChildrenFrom(valueElement);
            for (Element element : keyValueElements(defaultValue)) {
                valueElement.appendChild(element);
            }
            valueElement.getClassList().add(CSS.defaultValue);
            valueElement.setTitle(CONSTANTS.defaultValue());
        } else {
            valueElement.getClassList().remove(CSS.defaultValue);
            valueElement.setTitle("");
        }
    }

    private Iterable<Element> keyValueElements(Map<String, String> value) {
        Elements.Builder builder = new Elements.Builder();
        for (Map.Entry<String, String> entry : value.entrySet()) {
            builder.span().css(CSS.key).textContent(entry.getKey()).end();
            builder.span().css(CSS.equals).innerHtml(SafeHtmlUtils.fromSafeConstant("&rArr;")).end(); //NON-NLS
            builder.span().css(CSS.value).textContent(entry.getValue()).end();
        }
        return builder.elements();
    }

    @Override
    String asString(final Map<String, String> value) {
        return Joiner.on(", ").join(asTags(value));
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }

    public void setProperties(final Map<String, String> properties) {
        propertiesElement.setValue(properties);
    }

    @Override
    public boolean isEmpty() {
        return getValue().isEmpty() || isUndefined();
    }


    private static class PropertiesElement extends InputElement<Map<String, String>> {

        final elemental.html.InputElement element;

        PropertiesElement() {
            element = Browser.getDocument().createInputElement();
            element.setType("text"); //NON-NLS
        }

        @Override
        public Map<String, String> getValue() {
            return isAttached() ? asProperties(Bridge.element(asElement()).getTags()) : Collections.emptyMap();
        }

        @Override
        public void setValue(final Map<String, String> value) {
            if (isAttached()) {
                Bridge.element(asElement()).setTags(asTags(value));
            }
        }

        @Override
        public void clearValue() {
            if (isAttached()) {
                Bridge.element(asElement()).removeAll();
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
            return Joiner.on(", ").join(asTags(getValue()));
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

    private static Map<String, String> asProperties(final List<String> tags) {
        if (tags.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> properties = new HashMap<>();
        Splitter splitter = Splitter.on('=');
        for (String tag : tags) {
            if (tag.contains("=")) {
                List<String> split = splitter.splitToList(tag);
                switch (split.size()) {
                    case 0:
                        properties.put("", "");
                        break;
                    case 1:
                        properties.put(split.get(0), "");
                        break;
                    case 2:
                        properties.put(split.get(0), split.get(1));
                        break;
                    default:
                        properties.put(split.get(0), Joiner.on("").join(split.subList(1, split.size() - 1)));
                        break;
                }
            } else {
                properties.put(tag, null);
            }
        }
        return properties;
    }

    private static List<String> asTags(final Map<String, String> properties) {
        if (properties.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> tags = new ArrayList<>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            tags.add(entry.getKey() + "=" + entry.getValue());
        }
        return tags;
    }
}
