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
package org.jboss.hal.client.configuration.subsystem.elytron;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtml;
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.ModelNodeItem;
import org.jboss.hal.ballroom.form.TagsItem;
import org.jboss.hal.ballroom.form.TagsManager.Validator;
import org.jboss.hal.ballroom.form.TagsMapping;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.Messages;

import static elemental2.dom.DomGlobal.document;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.ballroom.form.Decoration.*;
import static org.jboss.hal.ballroom.form.Form.State.READONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;

/**
 * Form item which is used for some attributes in the Elytron subsystem which are defined as
 * <pre>
 * "attribute-name" => {
 *     "type" => LIST,
 *     "value-type" => {
 *         "name" => {
 *             "type" => STRING,
 *             "required" => true
 *         },
 *         "value" => {
 *             "type" => LIST,
 *             "required" => true,
 *             "value-type" => STRING
 *         }
 *     }
 * }
 * </pre>
 */
class MultiValueListItem extends TagsItem<ModelNode> implements ModelNodeItem {

    private static final Messages MESSAGES = GWT.create(Messages.class);

    MultiValueListItem(String attribute, SafeHtml helpText) {
        super(attribute, new LabelBuilder().label(attribute), MESSAGES.multiValueListHint(),
                EnumSet.of(DEFAULT, DEPRECATED, ENABLED, HELP, INVALID, REQUIRED, RESTRICTED), new MapMapping(), helpText);
    }

    @Override
    public void attach() {
        super.attach();
        HTMLElement element = (HTMLElement) document.getElementById(getId(READONLY));
        if (element != null) {
            element.style.whiteSpace = "pre";
        }
    }

    @Override
    public boolean isEmpty() {
        return getValue() == null || !getValue().isDefined();
    }

    @Override
    public void addTag(ModelNode tag) {
        ModelNode value = getValue();
        ModelNode newValue = value != null ? value.clone() : new ModelNode();
        newValue.add(tag.get(0));
        modifyValue(newValue);
    }

    @Override
    public void removeTag(ModelNode tag) {
        List<ModelNode> list = new ArrayList<>(getValue().asList());
        list.remove(tag.get(0));
        ModelNode newValue = new ModelNode();
        newValue.set(list);
        modifyValue(newValue);
    }

    private static class MapMapping implements TagsMapping<ModelNode> {

        private static final String VALUE_SEPARATOR = ":";
        private static final RegExp REGEX = RegExp.compile(
                "^([\\w\\-\\.\\/]+)=([\\w\\-\\.\\/" + VALUE_SEPARATOR + "]+)$"); //NON-NLS

        @Override
        public Validator validator() {
            return REGEX::test;
        }

        @Override
        public ModelNode parseTag(final String tag) {
            String[] parts = tag.split("=");
            ModelNode kv = new ModelNode();
            kv.get(NAME).set(parts[0]);
            for (String v : parts[1].split(VALUE_SEPARATOR)) {
                kv.get(VALUE).add(v);
            }

            ModelNode node = new ModelNode();
            node.add(kv);
            return node;
        }

        @Override
        public List<String> tags(final ModelNode value) {
            if (!value.isDefined()) {
                return emptyList();
            }
            return value.asList().stream()
                    .map(kv -> kv.get(NAME).asString() + "=" + kv.get(VALUE).asList().stream()
                            .map(ModelNode::asString)
                            .collect(joining(VALUE_SEPARATOR)))
                    .collect(toList());
        }

        @Override
        public String asString(final ModelNode value) {
            if (!value.isDefined()) {
                return "";
            }
            return value.asList().stream()
                    .map(kv -> kv.get(NAME).asString() + " \u21D2 " + kv.get(VALUE).asList().stream()
                            .map(ModelNode::asString)
                            .collect(joining(VALUE_SEPARATOR)))
                    .collect(joining("\n"));
        }
    }
}
