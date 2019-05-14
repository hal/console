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
package org.jboss.hal.core.ui;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.CompositeFormItem;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.core.configuration.PathsAutoComplete;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.Ids;

import static java.util.Arrays.asList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELATIVE_TO;

/**
 * Form item used in the logging subsystem to configure the complex attribute {@code file} which is roughly defined as
 * <pre>
 * "&lt;name&gt;" => {
 *   "type" => OBJECT,
 *   "value-type" => {
 *     "relative-to" => {
 *       "type" => STRING,
 *     },
 *     "path" => {
 *       "type" => STRING,
 *     }
 *   }
 * }
 * </pre>
 */
public class FileFormItem extends CompositeFormItem {

    private FormItem<String> path;
    private FormItem<String> relativeTo;

    public FileFormItem(String name, boolean pathRequired) {
        super(name, new LabelBuilder().label(name));

        path = new TextBoxItem(PATH, new LabelBuilder().label(PATH));
        path.setRequired(pathRequired);
        path.setId(Ids.uniqueId());
        relativeTo = new TextBoxItem(RELATIVE_TO, new LabelBuilder().label(RELATIVE_TO));
        relativeTo.setId(Ids.uniqueId());
        relativeTo.registerSuggestHandler(new PathsAutoComplete());

        addFormItems(asList(path, relativeTo));
    }

    @Override
    protected void populateFormItems(ModelNode modelNode) {
        if (modelNode.hasDefined(PATH)) {
            path.setValue(modelNode.get(PATH).asString());
        }
        if (modelNode.hasDefined(RELATIVE_TO)) {
            relativeTo.setValue(modelNode.get(RELATIVE_TO).asString());
        }
    }

    @Override
    protected void persistModel(ModelNode modelNode) {
        if (!path.isEmpty()) {
            modelNode.get(PATH).set(path.getValue());
        }
        if (!relativeTo.isEmpty()) {
            modelNode.get(RELATIVE_TO).set(relativeTo.getValue());
        }
    }
}
