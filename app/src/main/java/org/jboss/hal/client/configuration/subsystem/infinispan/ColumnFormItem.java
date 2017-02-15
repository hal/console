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
package org.jboss.hal.client.configuration.subsystem.infinispan;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.CompositeFormItem;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.Ids;

import static java.util.Arrays.asList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;

/**
 * Form item used in the infinispan subsystem to configure complex attributes like {@code id-column} which is roughly
 * defined as
 * <pre>
 * "id-column" => {
 *   "type" => OBJECT,
 *   "value-type" => {
 *     "name" => {
 *       "type" => STRING,
 *     },
 *     "type" => {
 *       "type" => STRING,
 *     }
 *   }
 * }
 * </pre>
 *
 * @author Harald Pehl
 */
class ColumnFormItem extends CompositeFormItem {

    private FormItem<String> name;
    private FormItem<String> type;

    ColumnFormItem(final String attribute) {
        super(attribute, new LabelBuilder().label(attribute));

        name = new TextBoxItem(NAME, new LabelBuilder().label(attribute + "-" + NAME));
        name.setId(Ids.uniqueId());
        type = new TextBoxItem(TYPE, new LabelBuilder().label(attribute + "-" + TYPE));
        type.setId(Ids.uniqueId());

        addFormItems(asList(name, type));
    }

    @Override
    protected void populateFormItems(final ModelNode modelNode) {
        if (modelNode.hasDefined(NAME)) {
            name.setValue(modelNode.get(NAME).asString());
        }
        if (modelNode.hasDefined(TYPE)) {
            type.setValue(modelNode.get(TYPE).asString());
        }
    }

    @Override
    protected void persistModel(final ModelNode modelNode) {
        if (name.isUndefined()) {
            modelNode.get(NAME).set(new ModelNode());
        } else {
            modelNode.get(NAME).set(name.getValue());
        }
        if (type.isUndefined()) {
            modelNode.get(TYPE).set(new ModelNode());
        } else {
            modelNode.get(TYPE).set(type.getValue());
        }
    }
}
