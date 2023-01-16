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

import org.jboss.hal.dmr.ModelNode;

/**
 * Takes care of the mapping between form fields and the model.
 */
public interface DataMapping<T> {

    void addAttributeDescription(String name, ModelNode attributeDescription);

    void newModel(T model, Form<T> form);

    void populateFormItems(T model, Form<T> form);

    void populateFormItem(String id, String name, ModelNode attributeDescription, ModelNode value, FormItem formItem);

    void clearFormItems(Form<T> form);

    void persistModel(T model, Form<T> form);

    void persistModel(String id, T model, Iterable<FormItem> formItems);
}
