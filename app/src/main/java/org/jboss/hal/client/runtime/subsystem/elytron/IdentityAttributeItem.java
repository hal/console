/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.runtime.subsystem.elytron;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.hal.ballroom.form.TagsItem;
import org.jboss.hal.ballroom.form.TagsManager;
import org.jboss.hal.ballroom.form.TagsMapping;
import org.jboss.hal.resources.Messages;

import com.google.common.base.Joiner;
import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.hal.ballroom.form.Decoration.*;
import static org.jboss.hal.ballroom.form.Form.State.READONLY;

public class IdentityAttributeItem extends TagsItem<Map<String, List<String>>> {

    private static final Messages MESSAGES = GWT.create(Messages.class);

    public IdentityAttributeItem(final String name, final String label) {
        super(name, label, MESSAGES.multiValueListHint(),
                EnumSet.of(DEFAULT, DEPRECATED, ENABLED, INVALID, REQUIRED, RESTRICTED),
                new MapMapping());
    }

    @Override
    public boolean isEmpty() {
        return getValue() == null || getValue().isEmpty();
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
    public void addTag(Map<String, List<String>> tag) {
        Map<String, List<String>> value = getValue();
        Map<String, List<String>> newValue = new HashMap<>();
        if (value != null) {
            newValue.putAll(value);
        }
        Map.Entry<String, List<String>> parsed = tag.entrySet().iterator().next();
        newValue.put(parsed.getKey(), parsed.getValue());
        modifyValue(newValue);
    }

    @Override
    public void removeTag(Map<String, List<String>> tag) {
        Map<String, List<String>> newValue = new HashMap<>(getValue());
        newValue.remove(tag.keySet().iterator().next());
        modifyValue(newValue);
    }

    private static class MapMapping implements TagsMapping<Map<String, List<String>>> {

        private static final String VALUE_SEPARATOR = ":";
        private static final RegExp REGEX = RegExp.compile("^([\\w\\-\\.\\/]+)=([\\w\\-\\.\\/:\\;]+)$"); // NON-NLS

        @Override
        public TagsManager.Validator validator() {
            return REGEX::test;
        }

        @Override
        public Map<String, List<String>> parseTag(final String tag) {
            String[] parts = tag.split("=");

            Map<String, List<String>> map = new HashMap<>();
            map.put(parts[0], asList(parts[1].split(VALUE_SEPARATOR)));
            return map;
        }

        @Override
        public List<String> tags(final Map<String, List<String>> sourceValue) {
            if (sourceValue.isEmpty()) {
                return emptyList();
            }
            List<String> tags = new ArrayList<>();
            sourceValue.forEach((key, values) -> {
                String tag = key + "=" + Joiner.on(VALUE_SEPARATOR).join(values);
                tags.add(tag);
            });
            return tags;
        }

        @Override
        public String asString(final Map<String, List<String>> sourceValue) {
            if (sourceValue.isEmpty()) {
                return "";
            }
            StringBuilder result = new StringBuilder();
            sourceValue.forEach((key, values) -> {
                String tag = key + " \u21D2 " + Joiner.on(",").join(values) + "\n";
                result.append(tag);
            });
            return result.toString();
        }
    }
}
