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
package org.jboss.hal.client.configuration.subsystem.logging;

import java.util.List;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.CompositeFormItem;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.client.configuration.PathsTypeahead;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.IdBuilder;

import static java.util.Arrays.asList;

/**
 * @author Harald Pehl
 */
public class FileFormItem extends CompositeFormItem {

    private static final String FILE = "file";
    private static final String PATH = "path";
    private static final String RELATIVE_TO = "relative-to";

    private FormItem<String> path;
    private FormItem<String> relativeTo;

    FileFormItem() {
        super(FILE);
    }

    @Override
    protected List<FormItem> createFormItems() {
        path = new TextBoxItem(PATH, new LabelBuilder().label(PATH));
        path.setRequired(true);
        path.setId(IdBuilder.uniqueId());
        relativeTo = new TextBoxItem(RELATIVE_TO, new LabelBuilder().label(RELATIVE_TO));
        relativeTo.setId(IdBuilder.uniqueId());
        relativeTo.registerSuggestHandler(new PathsTypeahead());
        return asList(path, relativeTo);
    }

    @Override
    protected void populateFormItems(final ModelNode modelNode) {
        path.setValue(modelNode.get(PATH).asString());
        if (modelNode.hasDefined(RELATIVE_TO)) {
            relativeTo.setValue(modelNode.get(RELATIVE_TO).asString());
        }
    }

    @Override
    protected void persistModel(final ModelNode modelNode) {
        modelNode.get(PATH).set(path.getValue());
        if (relativeTo.isUndefined()) {
            modelNode.get(RELATIVE_TO).set(new ModelNode());
        } else {
            modelNode.get(RELATIVE_TO).set(relativeTo.getValue());
        }
    }
}
