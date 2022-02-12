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
package org.jboss.hal.client.configuration.subsystem.microprofile;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.CompositeFormItem;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.Ids;

import static java.util.Arrays.asList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLASS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLASS_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MODULE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * Form item used in the microprofile-config subsystem to configure the complex attribute {@code class} defined as
 *
 * <pre>
 * "class" => {
 *     "type" => OBJECT,
 *     "value-type" => {
 *         "name" => {
 *             "type" => STRING,
 *             ...
 *         },
 *         "module" => {
 *             "type" => STRING,
 *             ...
 *         }
 *     },
 * }
 * </pre>
 */
class ClassFormItem extends CompositeFormItem {

    private FormItem<String> name;
    private FormItem<String> module;

    public ClassFormItem() {
        super(CLASS, new LabelBuilder().label(CLASS));

        name = new TextBoxItem(NAME, new LabelBuilder().label(CLASS_NAME));
        name.setId(Ids.uniqueId());
        module = new TextBoxItem(MODULE, new LabelBuilder().label(MODULE));
        module.setId(Ids.uniqueId());

        addFormItems(asList(name, module));
    }

    @Override
    protected void populateFormItems(ModelNode modelNode) {
        if (modelNode.hasDefined(NAME)) {
            name.setValue(modelNode.get(NAME).asString());
        }
        if (modelNode.hasDefined(MODULE)) {
            module.setValue(modelNode.get(MODULE).asString());
        }
    }

    @Override
    protected void persistModel(ModelNode modelNode) {
        if (!name.isEmpty()) {
            modelNode.get(NAME).set(name.getValue());
        }
        if (!module.isEmpty()) {
            modelNode.get(MODULE).set(module.getValue());
        }
    }
}
