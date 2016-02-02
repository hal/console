/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.ballroom.form;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.form.InputElement.Context;
import org.jboss.hal.ballroom.form.TagsManager.Bridge;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jboss.hal.ballroom.IdBuilder.build;
import static org.jboss.hal.ballroom.IdBuilder.uniquId;
import static org.jboss.hal.ballroom.form.InputElement.EMPTY_CONTEXT;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class PropertiesItem extends AbstractFormItem<Map<String, String>> {

    private final static Messages MESSAGES = GWT.create(Messages.class);
    private final static RegExp PROPERTY_REGEX = RegExp.compile("^([\\w\\d]+)=([\\w\\d]+)$"); //NON-NLS

    private PropertiesElement propertiesElement;
    private Element tagsContainer;

    public PropertiesItem(final String name, final String label) {
        super(name, label, null, EMPTY_CONTEXT);
    }

    @Override
    protected InputElement<Map<String, String>> newInputElement(Context<?> context) {
        propertiesElement = new PropertiesElement();
        propertiesElement.setClassName(formControl + " " + properties);
        Bridge.element(propertiesElement.asElement()).onRefresh((event, cst) -> {
            Map<String, String> value = Splitter.on(", ")
                    .trimResults()
                    .omitEmptyStrings()
                    .withKeyValueSeparator('=')
                    .split(cst);
            setModified(true);
            setUndefined(value.isEmpty());
            signalChange(value);
        });
        return propertiesElement;
    }

    @Override
    void assembleUI() {
        super.assembleUI();

        errorText.setInnerHTML(MESSAGES.propertiesHint().asString());
        errorText.getClassList().add(CSS.hint);
        Elements.setVisible(errorText, true);

        //noinspection DuplicateStringLiteralInspection
        tagsContainer = new Elements.Builder().div()
                .id(build("tags", "container", uniquId()))
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
        Bridge.element(propertiesElement.asElement()).tagsManager(options);
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


    static class PropertiesElement extends InputElement<Map<String, String>> {

        final elemental.html.InputElement element;

        PropertiesElement() {
            element = Browser.getDocument().createInputElement();
            element.setType("text"); //NON-NLS
        }

        @Override
        public Map<String, String> getValue() {
            return asProperties(Bridge.element(asElement()).getTags());
        }

        @Override
        public void setValue(final Map<String, String> value) {
            Bridge.element(asElement()).setTags(asTags(value));
        }

        @Override
        public void clearValue() {
            Bridge.element(asElement()).removeAll();
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
