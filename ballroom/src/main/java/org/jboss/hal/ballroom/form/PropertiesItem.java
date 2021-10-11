/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.ballroom.form;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtml;
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.resources.Messages;

import static elemental2.dom.DomGlobal.document;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static org.jboss.hal.ballroom.form.Decoration.*;
import static org.jboss.hal.ballroom.form.Form.State.READONLY;

public class PropertiesItem extends TagsItem<Map<String, String>> {

    private static final Messages MESSAGES = GWT.create(Messages.class);

    public PropertiesItem(String name) {
        this(name, new LabelBuilder().label(name), MESSAGES.propertiesHint(), null);
    }

    public PropertiesItem(String name, String label) {
        this(name, label, MESSAGES.propertiesHint(), null);
    }

    public PropertiesItem(String name, String label, SafeHtml helpText) {
        this(name, label, MESSAGES.propertiesHint(), helpText);
    }

    public PropertiesItem(String name, String label, SafeHtml inputHelp, SafeHtml helpText) {
        super(name, label, inputHelp,
                EnumSet.of(DEFAULT, DEPRECATED, ENABLED, HELP, INVALID, REQUIRED, RESTRICTED, SUGGESTIONS),
                new MapMapping(), helpText);
    }

    @Override
    public boolean isEmpty() {
        return getValue() == null || getValue().isEmpty();
    }

    @Override
    public String allowedCharacters() {
        return "- . : @ ; = ? ! # $ % & [ ] ( ) * _";
    }

    @Override
    public void attach() {
        super.attach();
        HTMLElement element = (HTMLElement) document.getElementById(getId(READONLY));
        if (element != null) {
            // this pre style allows the \n line separator in the MapMapping.asString method
            element.style.whiteSpace = "pre";
        }
    }

    @Override
    public void addTag(Map<String, String> tag) {
        Map<String, String> value = getValue();
        Map<String, String> newValue = new HashMap<>();
        if (value != null) {
            newValue.putAll(value);
        }
        Map.Entry<String, String> tagEntry = tag.entrySet().iterator().next();
        newValue.put(tagEntry.getKey(), tagEntry.getValue());
        modifyValue(newValue);
    }

    @Override
    public void removeTag(Map<String, String> tag) {
        Map<String, String> newValue = new HashMap<>(getValue());
        newValue.remove(tag.keySet().iterator().next());
        modifyValue(newValue);
    }

    static class MapMapping implements TagsMapping<Map<String, String>> {

        private static final RegExp REGEX = RegExp.compile(
                "^([\\w\\-\\.\\/]+)=([\\w\\-\\.\\/:\\@\\;\\=\\?\\!\\#\\$\\%\\&\\[\\]\\,\\(\\)\\*\\_]+)$"); //NON-NLS
        private static final String EQ = "=";

        @Override
        public TagsManager.Validator validator() {
            return REGEX::test;
        }

        @Override
        public Map<String, String> parseTag(String tag) {
            int firstEq = tag.indexOf(EQ);
            String keyPart = tag.substring(0, firstEq);
            String valuePart = tag.substring(firstEq + 1);
            Map<String, String> map = new HashMap<>();
            map.put(keyPart, valuePart);
            return map;
        }

        @Override
        public List<String> tags(Map<String, String> value) {
            if (value.isEmpty()) {
                return emptyList();
            }
            List<String> tags = new ArrayList<>();
            for (Map.Entry<String, String> entry : value.entrySet()) {
                tags.add(entry.getKey() + EQ + entry.getValue());
            }
            return tags;
        }

        @Override
        public String asString(Map<String, String> value) {
            // the \n line separator, works as there is a style: whitespace pre added in attach() method
            return value.entrySet().stream()
                    .map(entry -> entry.getKey() + " \u21D2 " + entry.getValue())
                    .collect(joining("\n"));
        }
    }
}
