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
package org.jboss.hal.core.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.form.ModelNodeItem;
import org.jboss.hal.ballroom.form.TagsItem;
import org.jboss.hal.ballroom.form.TagsManager;
import org.jboss.hal.ballroom.form.TagsMapping;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Messages;

import com.google.gwt.core.client.GWT;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;

import static java.util.Collections.emptyList;
import static org.jboss.gwt.elemento.core.Elements.button;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.i;
import static org.jboss.gwt.elemento.core.Elements.input;
import static org.jboss.gwt.elemento.core.Elements.label;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.gwt.elemento.core.InputType.checkbox;
import static org.jboss.hal.ballroom.form.Decoration.DEFAULT;
import static org.jboss.hal.ballroom.form.Decoration.DEPRECATED;
import static org.jboss.hal.ballroom.form.Decoration.ENABLED;
import static org.jboss.hal.ballroom.form.Decoration.INVALID;
import static org.jboss.hal.ballroom.form.Decoration.REQUIRED;
import static org.jboss.hal.ballroom.form.Decoration.RESTRICTED;
import static org.jboss.hal.ballroom.form.Decoration.SUGGESTIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NILLABLE;
import static org.jboss.hal.resources.CSS.btn;
import static org.jboss.hal.resources.CSS.btnDefault;
import static org.jboss.hal.resources.CSS.controlLabel;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.formGroup;
import static org.jboss.hal.resources.CSS.halFormInput;
import static org.jboss.hal.resources.CSS.halFormLabel;

/**
 * Form item which is used for some attributes which are defined as
 *
 * <pre>
 * "attribute-name" => {
 *     "type" => LIST,
 *     "value-type" => {
 *         "subattribute1" => {
 *             "type" => BIG_INTEGER | BOOLEAN | DOUBLE | INT | LONG | STRING
 *         },
 *         "subattribute2" => {}, …
 *     }
 * }
 * </pre>
 */
public class TuplesListItem extends TagsItem<ModelNode> implements ModelNodeItem {
    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final Messages MESSAGES = GWT.create(Messages.class);

    public TuplesListItem(String name, String label, Metadata metadata) {
        this(name, label, metadata, createButton(), new TuplesListMapping(metadata));
    }

    @SuppressWarnings("unchecked")
    private TuplesListItem(String name, String label, Metadata metadata, HTMLElement addButton, TuplesListMapping mapping) {
        super(name, label, MESSAGES.tuplesHint(String.join(",", getAttributeNames(metadata, true))),
                EnumSet.of(DEFAULT, DEPRECATED, ENABLED, INVALID, REQUIRED, RESTRICTED, SUGGESTIONS),
                mapping, addButton);

        Dialog addTupleDialog;
        ModelNodeForm dialogForm;
        HTMLInputElement checkBox;

        ModelNodeForm.Builder<ModelNode> builder = new ModelNodeForm.Builder<>(Ids.RESOLVE_EXPRESSION_FORM, metadata)
                .addOnly()
                .onSave((f, changedValues) -> {
                    onSuggest(mapping.tupleToString(f.getModel())); // add item from dialog form to the tags
                });
        dialogForm = builder.build();

        addTupleDialog = new Dialog.Builder(MESSAGES.addResourceTitle(getLabel()))
                .add(dialogForm.element())
                .add(div().css(formGroup)
                        .add(label(MESSAGES.keepDialogOpen()).css(controlLabel, halFormLabel).element())
                        .add(div().css(halFormInput)
                                .add(checkBox = input(checkbox).element())
                                .element())
                        .element())
                .primary(CONSTANTS.add(), () -> {
                    if (dialogForm.save()) {
                        dialogForm.edit(new ModelNode());
                        return !checkBox.checked;
                    }
                    return false;
                })
                .secondary(CONSTANTS.close(), () -> true)
                .size(Dialog.Size.MEDIUM)
                .build();
        addTupleDialog.registerAttachable(dialogForm);

        bind(addButton, click, event -> {
            addTupleDialog.show();
            dialogForm.edit(new ModelNode());
        });
    }

    public static String[] getAttributeNames(Metadata metadata, boolean markRequired) {
        return metadata.getDescription().get(ATTRIBUTES).asPropertyList().stream()
                .map(prop -> prop.getName()
                        + (markRequired && prop.getValue().hasDefined(NILLABLE) && !prop.getValue().get(NILLABLE).asBoolean()
                                ? "*"
                                : ""))
                .toArray(String[]::new);
    }

    public static HTMLElement createButton() {
        return button()
                .css(btn, btnDefault)
                .title(CONSTANTS.add())
                .add(i().css(fontAwesome("plus"))).element();
    }

    @Override
    public void addTag(ModelNode tag) {
        ModelNode value = getValue();
        ModelNode newValue = value != null ? value.clone() : new ModelNode();
        newValue.add(tag);
        modifyValue(newValue);
    }

    @Override
    public void removeTag(ModelNode tag) {
        List<ModelNode> list = new ArrayList<>(getValue().asList());
        list.remove(tag);
        ModelNode newValue = new ModelNode();
        newValue.set(list);
        modifyValue(newValue);
    }

    @Override
    public boolean isEmpty() {
        return getValue() == null || getValue().asInt() == 0;
    }

    private static class TuplesListMapping implements TagsMapping<ModelNode> {
        private final String[] attributeNames;
        private static final String TUPLE_SEPARATOR = "; ";
        private static final String SEPARATOR = ",";
        private static final String BLANK = "—";

        public TuplesListMapping(Metadata metadata) {
            this.attributeNames = getAttributeNames(metadata, false);
        }

        @Override
        public TagsManager.Validator validator() {
            return (value) -> {
                String[] attrs = value.split(SEPARATOR, -1);
                return attrs.length == attributeNames.length && !Arrays.stream(attrs).allMatch(String::isEmpty);
            };
        }

        @Override
        public ModelNode parseTag(final String tag) {
            ModelNode tuple = stringToTuple(tag);
            return tuple;
        }

        @Override
        public List<String> tags(ModelNode value) {
            if (!value.isDefined()) {
                return emptyList();
            }
            return value.asList().stream()
                    .map(this::tupleToString)
                    .collect(Collectors.toList());
        }

        @Override
        public String asString(ModelNode value) {
            return String.join(TUPLE_SEPARATOR, tags(value));
        }

        private ModelNode stringToTuple(String tag) {
            ModelNode tuple = new ModelNode();
            String[] attrValues = tag.split(SEPARATOR, -1);
            for (int i = 0; i < attributeNames.length; i++) {
                if (!attrValues[i].isEmpty() && !attrValues[i].equals(BLANK)) {
                    tuple.get(attributeNames[i]).set(attrValues[i]);
                }
            }
            return tuple;
        }

        private String tupleToString(ModelNode value) {
            String[] attrValues = new String[attributeNames.length];
            for (int i = 0; i < attributeNames.length; i++) {
                attrValues[i] = value.hasDefined(attributeNames[i]) ? value.get(attributeNames[i]).asString() : BLANK;
            }
            return String.join(SEPARATOR, attrValues);
        }
    }
}
